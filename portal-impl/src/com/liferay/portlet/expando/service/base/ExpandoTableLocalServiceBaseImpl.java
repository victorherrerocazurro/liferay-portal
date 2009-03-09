/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portlet.expando.service.base;

import com.liferay.counter.service.CounterLocalService;
import com.liferay.counter.service.CounterService;

import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.annotation.BeanReference;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;

import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.service.ExpandoColumnLocalService;
import com.liferay.portlet.expando.service.ExpandoColumnService;
import com.liferay.portlet.expando.service.ExpandoRowLocalService;
import com.liferay.portlet.expando.service.ExpandoTableLocalService;
import com.liferay.portlet.expando.service.ExpandoValueLocalService;
import com.liferay.portlet.expando.service.ExpandoValueService;
import com.liferay.portlet.expando.service.persistence.ExpandoColumnFinder;
import com.liferay.portlet.expando.service.persistence.ExpandoColumnPersistence;
import com.liferay.portlet.expando.service.persistence.ExpandoRowFinder;
import com.liferay.portlet.expando.service.persistence.ExpandoRowPersistence;
import com.liferay.portlet.expando.service.persistence.ExpandoTablePersistence;
import com.liferay.portlet.expando.service.persistence.ExpandoValueFinder;
import com.liferay.portlet.expando.service.persistence.ExpandoValuePersistence;

import java.util.List;

/**
 * <a href="ExpandoTableLocalServiceBaseImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public abstract class ExpandoTableLocalServiceBaseImpl
	implements ExpandoTableLocalService {
	public ExpandoTable addExpandoTable(ExpandoTable expandoTable)
		throws SystemException {
		expandoTable.setNew(true);

		return expandoTablePersistence.update(expandoTable, false);
	}

	public ExpandoTable createExpandoTable(long tableId) {
		return expandoTablePersistence.create(tableId);
	}

	public void deleteExpandoTable(long tableId)
		throws PortalException, SystemException {
		expandoTablePersistence.remove(tableId);
	}

	public void deleteExpandoTable(ExpandoTable expandoTable)
		throws SystemException {
		expandoTablePersistence.remove(expandoTable);
	}

	public List<Object> dynamicQuery(DynamicQuery dynamicQuery)
		throws SystemException {
		return expandoTablePersistence.findWithDynamicQuery(dynamicQuery);
	}

	public List<Object> dynamicQuery(DynamicQuery dynamicQuery, int start,
		int end) throws SystemException {
		return expandoTablePersistence.findWithDynamicQuery(dynamicQuery,
			start, end);
	}

	public ExpandoTable getExpandoTable(long tableId)
		throws PortalException, SystemException {
		return expandoTablePersistence.findByPrimaryKey(tableId);
	}

	public List<ExpandoTable> getExpandoTables(int start, int end)
		throws SystemException {
		return expandoTablePersistence.findAll(start, end);
	}

	public int getExpandoTablesCount() throws SystemException {
		return expandoTablePersistence.countAll();
	}

	public ExpandoTable updateExpandoTable(ExpandoTable expandoTable)
		throws SystemException {
		expandoTable.setNew(false);

		return expandoTablePersistence.update(expandoTable, true);
	}

	public ExpandoTable updateExpandoTable(ExpandoTable expandoTable,
		boolean merge) throws SystemException {
		expandoTable.setNew(false);

		return expandoTablePersistence.update(expandoTable, merge);
	}

	public ExpandoColumnLocalService getExpandoColumnLocalService() {
		return expandoColumnLocalService;
	}

	public void setExpandoColumnLocalService(
		ExpandoColumnLocalService expandoColumnLocalService) {
		this.expandoColumnLocalService = expandoColumnLocalService;
	}

	public ExpandoColumnService getExpandoColumnService() {
		return expandoColumnService;
	}

	public void setExpandoColumnService(
		ExpandoColumnService expandoColumnService) {
		this.expandoColumnService = expandoColumnService;
	}

	public ExpandoColumnPersistence getExpandoColumnPersistence() {
		return expandoColumnPersistence;
	}

	public void setExpandoColumnPersistence(
		ExpandoColumnPersistence expandoColumnPersistence) {
		this.expandoColumnPersistence = expandoColumnPersistence;
	}

	public ExpandoColumnFinder getExpandoColumnFinder() {
		return expandoColumnFinder;
	}

	public void setExpandoColumnFinder(ExpandoColumnFinder expandoColumnFinder) {
		this.expandoColumnFinder = expandoColumnFinder;
	}

	public ExpandoRowLocalService getExpandoRowLocalService() {
		return expandoRowLocalService;
	}

	public void setExpandoRowLocalService(
		ExpandoRowLocalService expandoRowLocalService) {
		this.expandoRowLocalService = expandoRowLocalService;
	}

	public ExpandoRowPersistence getExpandoRowPersistence() {
		return expandoRowPersistence;
	}

	public void setExpandoRowPersistence(
		ExpandoRowPersistence expandoRowPersistence) {
		this.expandoRowPersistence = expandoRowPersistence;
	}

	public ExpandoRowFinder getExpandoRowFinder() {
		return expandoRowFinder;
	}

	public void setExpandoRowFinder(ExpandoRowFinder expandoRowFinder) {
		this.expandoRowFinder = expandoRowFinder;
	}

	public ExpandoTableLocalService getExpandoTableLocalService() {
		return expandoTableLocalService;
	}

	public void setExpandoTableLocalService(
		ExpandoTableLocalService expandoTableLocalService) {
		this.expandoTableLocalService = expandoTableLocalService;
	}

	public ExpandoTablePersistence getExpandoTablePersistence() {
		return expandoTablePersistence;
	}

	public void setExpandoTablePersistence(
		ExpandoTablePersistence expandoTablePersistence) {
		this.expandoTablePersistence = expandoTablePersistence;
	}

	public ExpandoValueLocalService getExpandoValueLocalService() {
		return expandoValueLocalService;
	}

	public void setExpandoValueLocalService(
		ExpandoValueLocalService expandoValueLocalService) {
		this.expandoValueLocalService = expandoValueLocalService;
	}

	public ExpandoValueService getExpandoValueService() {
		return expandoValueService;
	}

	public void setExpandoValueService(ExpandoValueService expandoValueService) {
		this.expandoValueService = expandoValueService;
	}

	public ExpandoValuePersistence getExpandoValuePersistence() {
		return expandoValuePersistence;
	}

	public void setExpandoValuePersistence(
		ExpandoValuePersistence expandoValuePersistence) {
		this.expandoValuePersistence = expandoValuePersistence;
	}

	public ExpandoValueFinder getExpandoValueFinder() {
		return expandoValueFinder;
	}

	public void setExpandoValueFinder(ExpandoValueFinder expandoValueFinder) {
		this.expandoValueFinder = expandoValueFinder;
	}

	public CounterLocalService getCounterLocalService() {
		return counterLocalService;
	}

	public void setCounterLocalService(CounterLocalService counterLocalService) {
		this.counterLocalService = counterLocalService;
	}

	public CounterService getCounterService() {
		return counterService;
	}

	public void setCounterService(CounterService counterService) {
		this.counterService = counterService;
	}

	@BeanReference(name = "com.liferay.portlet.expando.service.ExpandoColumnLocalService.impl")
	protected ExpandoColumnLocalService expandoColumnLocalService;
	@BeanReference(name = "com.liferay.portlet.expando.service.ExpandoColumnService.impl")
	protected ExpandoColumnService expandoColumnService;
	@BeanReference(name = "com.liferay.portlet.expando.service.persistence.ExpandoColumnPersistence.impl")
	protected ExpandoColumnPersistence expandoColumnPersistence;
	@BeanReference(name = "com.liferay.portlet.expando.service.persistence.ExpandoColumnFinder.impl")
	protected ExpandoColumnFinder expandoColumnFinder;
	@BeanReference(name = "com.liferay.portlet.expando.service.ExpandoRowLocalService.impl")
	protected ExpandoRowLocalService expandoRowLocalService;
	@BeanReference(name = "com.liferay.portlet.expando.service.persistence.ExpandoRowPersistence.impl")
	protected ExpandoRowPersistence expandoRowPersistence;
	@BeanReference(name = "com.liferay.portlet.expando.service.persistence.ExpandoRowFinder.impl")
	protected ExpandoRowFinder expandoRowFinder;
	@BeanReference(name = "com.liferay.portlet.expando.service.ExpandoTableLocalService.impl")
	protected ExpandoTableLocalService expandoTableLocalService;
	@BeanReference(name = "com.liferay.portlet.expando.service.persistence.ExpandoTablePersistence.impl")
	protected ExpandoTablePersistence expandoTablePersistence;
	@BeanReference(name = "com.liferay.portlet.expando.service.ExpandoValueLocalService.impl")
	protected ExpandoValueLocalService expandoValueLocalService;
	@BeanReference(name = "com.liferay.portlet.expando.service.ExpandoValueService.impl")
	protected ExpandoValueService expandoValueService;
	@BeanReference(name = "com.liferay.portlet.expando.service.persistence.ExpandoValuePersistence.impl")
	protected ExpandoValuePersistence expandoValuePersistence;
	@BeanReference(name = "com.liferay.portlet.expando.service.persistence.ExpandoValueFinder.impl")
	protected ExpandoValueFinder expandoValueFinder;
	@BeanReference(name = "com.liferay.counter.service.CounterLocalService.impl")
	protected CounterLocalService counterLocalService;
	@BeanReference(name = "com.liferay.counter.service.CounterService.impl")
	protected CounterService counterService;
}