/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.internal.upgrade.v1_1_2;

import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolder;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.SystemEvent;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.SystemEventLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Date;
import java.util.List;

import javax.portlet.PortletPreferences;

/**
 * @author Alexander Chow
 * @author Shinn Lok
 */
public class UpgradeJournalServiceVerify extends UpgradeProcess {

	public UpgradeJournalServiceVerify(
		AssetEntryLocalService assetEntryLocalService, Portal portal,
		ResourceLocalService resourceLocalService,
		SystemEventLocalService systemEventLocalService) {

		_assetEntryLocalService = assetEntryLocalService;
		_portal = portal;
		_resourceLocalService = resourceLocalService;
		_systemEventLocalService = systemEventLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		verifyContentSearch();
		verifyFolderAssets();
		verifyPermissions();

		verifyJournalArticleDeleteSystemEvents();
	}

	protected void updateContentSearch(long groupId, String portletId)
		throws Exception {

		try (PreparedStatement selectPreferencesPS =
				connection.prepareStatement(
					"select preferences from PortletPreferences inner join " +
						"Layout on PortletPreferences.plid = Layout.plid " +
							"where groupId = ? and portletId = ?");
			PreparedStatement selectSearchPS = connection.prepareStatement(
				"select companyId, privateLayout, layoutId, portletId from " +
					"JournalContentSearch where JournalContentSearch.groupId " +
						"= ? AND JournalContentSearch.articleId = ?");
			PreparedStatement deleteSearchPS = connection.prepareStatement(
				"DELETE FROM JournalContentSearch WHERE" +
					"JournalContentSearch.groupId = ? AND" +
						"JournalContentSearch.articleId = ?");
			PreparedStatement insertSearchPS = connection.prepareStatement(
				"INSERT INTO JournalContentSearch(contentSearchId, " +
					"companyId, groupId, privateLayout, layoutId, portletId, " +
						"articleId) values (?, ?, ?, ?, ?, ?, ?)")) {

			selectPreferencesPS.setLong(1, groupId);
			selectPreferencesPS.setString(2, portletId);

			try (ResultSet preferencesRS = selectPreferencesPS.executeQuery()) {
				while (preferencesRS.next()) {
					String xml = preferencesRS.getString("preferences");

					PortletPreferences portletPreferences =
						PortletPreferencesFactoryUtil.fromDefaultXML(xml);

					String articleId = portletPreferences.getValue(
						"articleId", null);

					selectSearchPS.setLong(1, groupId);
					selectSearchPS.setString(2, articleId);

					try (ResultSet searchRS = selectSearchPS.executeQuery()) {
						if (searchRS.next()) {
							long companyId = searchRS.getLong("companyId");
							boolean privateLayout = searchRS.getBoolean(
								"privateLayout");
							long layoutId = searchRS.getLong("layoutId");
							String journalContentSearchPortletId =
								searchRS.getString("portletId");

							deleteSearchPS.setLong(1, groupId);
							deleteSearchPS.setString(2, articleId);

							deleteSearchPS.executeUpdate();

							insertSearchPS.setLong(1, increment());
							insertSearchPS.setLong(2, companyId);
							insertSearchPS.setBoolean(3, privateLayout);
							insertSearchPS.setLong(4, layoutId);
							insertSearchPS.setString(
								5, journalContentSearchPortletId);
							insertSearchPS.setString(6, articleId);

							insertSearchPS.executeUpdate();
						}
					}
				}
			}
		}
	}

	protected void verifyContentSearch() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer();
			PreparedStatement ps = connection.prepareStatement(
				"select groupId, portletId from JournalContentSearch group " +
					"by groupId, portletId having count(groupId) > 1 and " +
						"count(portletId) > 1");
			ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				long groupId = rs.getLong("groupId");
				String portletId = rs.getString("portletId");

				updateContentSearch(groupId, portletId);
			}
		}
	}

	protected void verifyFolderAssets() throws Exception {
		long classNameId = _portal.getClassNameId(JournalFolder.class);

		try (LoggingTimer loggingTimer = new LoggingTimer();
			PreparedStatement ps = connection.prepareStatement(
				StringBundler.concat(
					"select JournalFolder.userId, JournalFolder.groupId, ",
					"JournalFolder.createDate, JournalFolder.modifiedDate, ",
					"JournalFolder.folderId, JournalFolder.uuid_, ",
					"JournalFolder.name, JournalFolder.description from ",
					"JournalFolder left join AssetEntry on ",
					"(AssetEntry.classNameId = ", String.valueOf(classNameId),
					") AND (AssetEntry.classPK = JournalFolder.folderId) ",
					"where AssetEntry.classPK IS NULL"));
			ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				long userId = rs.getLong("userId");
				long groupId = rs.getLong("groupId");
				Date createDate = rs.getTimestamp("createDate");
				Date modifiedDate = rs.getTimestamp("modifiedDate");
				long folderId = rs.getLong("folderId");
				String uuid = rs.getString("uuid_");
				String name = rs.getString("name");
				String description = rs.getString("description");

				try {
					_assetEntryLocalService.updateEntry(
						userId, groupId, createDate, modifiedDate,
						JournalFolder.class.getName(), folderId, uuid, 0, null,
						null, true, true, null, null, createDate, null,
						ContentTypes.TEXT_PLAIN, name, description, null, null,
						null, 0, 0, null);
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							StringBundler.concat(
								"Unable to update asset for folder ",
								String.valueOf(folderId), ": ",
								e.getMessage()));
					}
				}
			}

			if (_log.isDebugEnabled()) {
				_log.debug("Assets verified for folders");
			}
		}
	}

	protected void verifyJournalArticleDeleteSystemEvents() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			DynamicQuery dynamicQuery = _systemEventLocalService.dynamicQuery();

			Property classNameIdProperty = PropertyFactoryUtil.forName(
				"classNameId");

			dynamicQuery.add(
				classNameIdProperty.eq(
					_portal.getClassNameId(_CLASS_NAME_JOURNAL_ARTICLE)));

			Property typeProperty = PropertyFactoryUtil.forName("type");

			dynamicQuery.add(typeProperty.eq(SystemEventConstants.TYPE_DELETE));

			List<SystemEvent> systemEvents =
				_systemEventLocalService.dynamicQuery(dynamicQuery);

			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Processing ", String.valueOf(systemEvents.size()),
						" delete system events for journal articles"));
			}

			for (SystemEvent systemEvent : systemEvents) {
				JSONObject extraDataJSONObject =
					JSONFactoryUtil.createJSONObject(
						systemEvent.getExtraData());

				if (extraDataJSONObject.has("uuid") ||
					!extraDataJSONObject.has("version")) {

					continue;
				}

				String articleId = null;

				try (PreparedStatement ps = connection.prepareStatement(
						"select articleId from JournalArticleResource where " +
							"JournalArticleResource.uuid_ = ? AND " +
								"JournalArticleResource.groupId = ?")) {

					ps.setString(1, systemEvent.getClassUuid());
					ps.setLong(2, systemEvent.getGroupId());

					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							articleId = rs.getString(1);
						}
					}
				}

				if (articleId == null) {
					continue;
				}

				try (PreparedStatement ps = connection.prepareStatement(
						"select 1 from JournalArticle where groupId = ? and " +
							"articleId = ? and version = ? and status = ?")) {

					ps.setLong(1, systemEvent.getGroupId());
					ps.setString(2, articleId);
					ps.setDouble(3, extraDataJSONObject.getDouble("version"));
					ps.setInt(4, WorkflowConstants.STATUS_IN_TRASH);

					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							_systemEventLocalService.deleteSystemEvent(
								systemEvent);
						}
					}
				}
			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Delete system events verified for journal articles");
			}
		}
	}

	protected void verifyPermissions() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer();
			PreparedStatement ps = connection.prepareStatement(
				StringBundler.concat(
					"select JournalArticle.companyId, ",
					"JournalArticle.resourcePrimKey from JournalArticle left ",
					"join ResourcePermission on (ResourcePermission.companyId ",
					"= JournalArticle.companyId) and (ResourcePermission.name ",
					"= '", JournalArticle.class.getName(),
					"') and (ResourcePermission.primKeyId = ",
					"JournalArticle.resourcePrimKey) and ",
					"(ResourcePermission.scope = ",
					String.valueOf(ResourceConstants.SCOPE_INDIVIDUAL),
					") where ResourcePermission.primKey is null"));
			ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				long companyId = rs.getLong("companyId");
				long resourcePrimKey = rs.getLong("resourcePrimKey");

				_resourceLocalService.addResources(
					companyId, 0, 0, JournalArticle.class.getName(),
					resourcePrimKey, false, false, false);
			}
		}
	}

	private static final String _CLASS_NAME_JOURNAL_ARTICLE =
		"com.liferay.journal.model.JournalArticle";

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeJournalServiceVerify.class);

	private final AssetEntryLocalService _assetEntryLocalService;
	private final Portal _portal;
	private final ResourceLocalService _resourceLocalService;
	private final SystemEventLocalService _systemEventLocalService;

}