/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors: 
 *     Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.nebula.widgets.opal.notifier;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * This class creates the colors associated to a given theme
 * 
 */
public class NotifierColorsFactory {

	public enum NotifierTheme {
		YELLOW_THEME, GRAY_THEME, BLUE_THEME
	};

	/**
	 * Constructor
	 */
	private NotifierColorsFactory() {

	}

	/**
	 * @param theme a theme for the notifier widget
	 * @return the color set for the given theme
	 */
	static NotifierColors getColorsForTheme(final NotifierTheme theme) {
		final NotifierColors colors = new NotifierColors();
		Display display = Display.getDefault();
		switch (theme) {
		case BLUE_THEME:
			colors.textColor = new Color(4, 64, 140);
			colors.titleColor = new Color(0, 0, 0);
			colors.borderColor = new Color(153, 188, 232);
			colors.leftColor = new Color(210, 225, 244);
			colors.rightColor = new Color(182, 207, 238);
			break;
		case GRAY_THEME:
			colors.textColor = new Color(0, 0, 0);
			colors.titleColor = new Color(255, 20, 20);
			colors.borderColor = new Color(208, 208, 208);
			colors.leftColor = new Color(255, 255, 255);
			colors.rightColor = new Color(208, 208, 208);
			break;
		default:
			colors.textColor = new Color(0, 0, 0);
			colors.titleColor = new Color(0, 0, 0);
			colors.borderColor = new Color(218, 178, 85);
			colors.leftColor = new Color(220, 220, 160);
			colors.rightColor = new Color(255, 255, 191);
			break;
		}
		return colors;
	}

}
