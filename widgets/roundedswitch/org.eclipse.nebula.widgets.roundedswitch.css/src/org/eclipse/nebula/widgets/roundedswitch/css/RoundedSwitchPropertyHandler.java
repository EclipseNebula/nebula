/*******************************************************************************
 * Copyright (c) 2020 Laurent Caron.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent Caron <laurent dot caron at gmail dot com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.roundedswitch.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues;
import org.eclipse.nebula.widgets.roundedswitch.RoundedSwitch;
import org.eclipse.swt.graphics.Color;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public class RoundedSwitchPropertyHandler implements ICSSPropertyHandler {
	@Override
	public boolean applyCSSProperty(final Object element, final String property, final CSSValue value, final String pseudo, final CSSEngine engine) throws Exception {
		final RoundedSwitch rs = (RoundedSwitch) ((RoundedSwitchElement) element).getNativeWidget();
		boolean enabled = isEmpty(pseudo) || pseudo.equals("checkedEnabled") || pseudo.equals("uncheckedEnabled");
		boolean checked = isEmpty(pseudo) || pseudo.equals("checkedEnabled") || pseudo.equals("checkedDisabled");

		if ("switch-border-width".equals(property)) {
		    if (value instanceof CssValues.CssNumeric numeric) {
		        final int width = Math.round((float) numeric.value());
		        rs.setBorderWidth(width);
		    }
		}

		if ("switch-border-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, rs.getDisplay());
			if (enabled) {
				if (checked) {
					rs.setBorderColorCheckedEnabled(newColor);
				} else {
					rs.setBorderColorUncheckedEnabled(newColor);
				}
			} else {
				if (checked) {
					rs.setBorderColorCheckedDisabled(newColor);
				} else {
					rs.setBorderColorUncheckedDisabled(newColor);
				}
			}
		}

		if ("switch-circle-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, rs.getDisplay());
			if (enabled) {
				if (checked) {
					rs.setCircleColorCheckedEnabled(newColor);
				} else {
					rs.setCircleColorUncheckedEnabled(newColor);
				}
			} else {
				if (checked) {
					rs.setCircleColorCheckedDisabled(newColor);
				} else {
					rs.setCircleColorUncheckedDisabled(newColor);
				}
			}
		}

		if ("switch-background-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, rs.getDisplay());
			if (enabled) {
				if (checked) {
					rs.setBackgroundColorCheckedEnabled(newColor);
				} else {
					rs.setBackgroundColorUncheckedEnabled(newColor);
				}
			} else {
				if (checked) {
					rs.setBackgroundColorCheckedDisabled(newColor);
				} else {
					rs.setBackgroundColorUncheckedDisabled(newColor);
				}
			}
		}

		return true;
	}

	private boolean isEmpty(String pseudo) {
		return pseudo == null || pseudo.trim().length() == 0;
	}

	@Override
	public String retrieveCSSProperty(final Object element, final String property, final String pseudo, final CSSEngine engine) throws Exception {
		return null;
	}
}