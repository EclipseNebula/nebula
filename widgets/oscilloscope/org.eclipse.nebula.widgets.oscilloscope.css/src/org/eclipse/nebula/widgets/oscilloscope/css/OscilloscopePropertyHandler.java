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
package org.eclipse.nebula.widgets.oscilloscope.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues;
import org.eclipse.nebula.widgets.oscilloscope.multichannel.Oscilloscope;
import org.eclipse.swt.graphics.Color;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public class OscilloscopePropertyHandler implements ICSSPropertyHandler {
	@Override
	public boolean applyCSSProperty(final Object element, final String property, final CSSValue value,
			final String pseudo, final CSSEngine engine) throws Exception {

		final Oscilloscope osc = (Oscilloscope) ((OscilloscopeElement) element).getNativeWidget();

		// General properties
		if ("grid-square-size".equals(property)) {
			if (value instanceof CssValues.CssNumeric numeric) {
				final int size = Math.round(Math.round((float) numeric.value()));
				osc.setGridSquareSize(size);
			}
		}

		if ("grid-line-width".equals(property)) {
			if (value instanceof CssValues.CssNumeric numeric) {
				final int width = Math.round(Math.round((float) numeric.value()));
				osc.setGridLineWidth(width);
			}
		}

		if ("grid-color".equals(property) && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, osc.getDisplay());
			osc.setGridForeground(newColor);
		}

		if ("grid-background-color".equals(property) && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, osc.getDisplay());
			osc.setGridBackground(newColor);
		}

		// Channel properties
		if (property != null && property.startsWith("channel-color-")
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, osc.getDisplay());
			osc.setForeground(extractChannel(property), newColor);
		}

		if (property != null && property.startsWith("channel-active-color-")
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, osc.getDisplay());
			osc.getDispatcher(extractChannel(property)).setActiveForegoundColor(newColor);
		}

		if (property != null && property.startsWith("channel-inactive-color-")
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, osc.getDisplay());
			osc.getDispatcher(extractChannel(property)).setInactiveForegoundColor(newColor);
		}

		return true;
	}

	private int extractChannel(final String property) {
		if (property.endsWith("-")) {
			return 0;
		}

		final int position = property.lastIndexOf("-");
		final String extract = property.substring(position + 1);
		try {
			return Integer.valueOf(extract);
		} catch (final Exception e) {
			return 0;
		}
	}

	@Override
	public String retrieveCSSProperty(final Object element, final String property, final String pseudo,
			final CSSEngine engine) throws Exception {
		return null;
	}
}