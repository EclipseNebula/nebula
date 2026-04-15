/*******************************************************************************
 * Copyright (c) 2020 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API and
 * implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.roundedswitch;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * This class represents the configuration for a given state (enable state+selection state)
 */
class RoundedSwitchConfiguration {

	Color borderColor;
	Color circleColor;
	Color backgroundColor;

	private RoundedSwitchConfiguration(Color borderColor, Color circleColor, Color backgroundColor) {
		this.borderColor = borderColor;
		this.circleColor = circleColor;
		this.backgroundColor = backgroundColor;
	}

	static RoundedSwitchConfiguration createCheckedEnabledConfiguration(RoundedSwitch parent) {
		Display display = parent.getDisplay();
		RoundedSwitchConfiguration config = new RoundedSwitchConfiguration(display.getSystemColor(SWT.COLOR_BLACK), //
				display.getSystemColor(SWT.COLOR_WHITE), display.getSystemColor(SWT.COLOR_BLACK));
		return config;
	}

	static RoundedSwitchConfiguration createUncheckedEnabledConfiguration(RoundedSwitch parent) {
		Display display = parent.getDisplay();
		RoundedSwitchConfiguration config = new RoundedSwitchConfiguration(display.getSystemColor(SWT.COLOR_BLACK), //
				display.getSystemColor(SWT.COLOR_BLACK), display.getSystemColor(SWT.COLOR_WHITE));
		return config;
	}

	static RoundedSwitchConfiguration createCheckedDisabledConfiguration(RoundedSwitch parent) {
		Display display = parent.getDisplay();
		Color lightGrey = new Color(233, 233, 233);
		Color darkGrey = new Color(208, 208, 208);
		SWTGraphicUtil.addDisposer(parent, lightGrey, darkGrey);
		RoundedSwitchConfiguration config = new RoundedSwitchConfiguration(lightGrey, darkGrey, lightGrey);
		return config;
	}

	static RoundedSwitchConfiguration createUncheckedDisabledConfiguration(RoundedSwitch parent) {
		Display display = parent.getDisplay();
		Color lightGrey = new Color(233, 233, 233);
		Color darkGrey = new Color(208, 208, 208);
		SWTGraphicUtil.addDisposer(parent, lightGrey, darkGrey);
		RoundedSwitchConfiguration config = new RoundedSwitchConfiguration(lightGrey, darkGrey, lightGrey);
		return config;
	}

}
