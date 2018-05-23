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

package com.liferay.portal.kernel.portlet;

import com.liferay.portal.kernel.util.Validator;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;

/**
 * @author Brian Wing Shun Chan
 * @author Neil Griffin
 */
public class PortletModeFactory {

	public static PortletMode getPortletMode(String name) {
		return getPortletMode(name, 2);
	}

	public static PortletMode getPortletMode(
		String name, int portletMajorVersion) {

		if (Validator.isNull(name)) {
			if (portletMajorVersion < 3) {
				return PortletMode.VIEW;
			}
			else {
				return PortletMode.UNDEFINED;
			}
		}

		PortletMode portletMode = _portletModes.get(name);

		if (portletMode == null) {
			portletMode = new PortletMode(name);
		}

		return portletMode;
	}

	private static final String _ABOUT = LiferayPortletMode.ABOUT.toString();

	private static final String _CONFIG = LiferayPortletMode.CONFIG.toString();

	private static final String _EDIT = PortletMode.EDIT.toString();

	private static final String _EDIT_DEFAULTS =
		LiferayPortletMode.EDIT_DEFAULTS.toString();

	private static final String _EDIT_GUEST =
		LiferayPortletMode.EDIT_GUEST.toString();

	private static final String _HELP = PortletMode.HELP.toString();

	private static final String _PREVIEW =
		LiferayPortletMode.PREVIEW.toString();

	private static final String _PRINT = LiferayPortletMode.PRINT.toString();

	private static final String _UNDEFINED = PortletMode.UNDEFINED.toString();

	private static final String _VIEW = PortletMode.VIEW.toString();

	private static final Map<String, PortletMode> _portletModes =
		new HashMap<>();

	static {
		_portletModes.put(_EDIT, LiferayPortletMode.EDIT);
		_portletModes.put(_HELP, LiferayPortletMode.HELP);
		_portletModes.put(_VIEW, LiferayPortletMode.VIEW);
		_portletModes.put(_ABOUT, LiferayPortletMode.ABOUT);
		_portletModes.put(_CONFIG, LiferayPortletMode.CONFIG);
		_portletModes.put(_EDIT_DEFAULTS, LiferayPortletMode.EDIT_DEFAULTS);
		_portletModes.put(_EDIT_GUEST, LiferayPortletMode.EDIT_GUEST);
		_portletModes.put(_PREVIEW, LiferayPortletMode.PREVIEW);
		_portletModes.put(_PRINT, LiferayPortletMode.PRINT);
		_portletModes.put(_UNDEFINED, LiferayPortletMode.UNDEFINED);
	}

}