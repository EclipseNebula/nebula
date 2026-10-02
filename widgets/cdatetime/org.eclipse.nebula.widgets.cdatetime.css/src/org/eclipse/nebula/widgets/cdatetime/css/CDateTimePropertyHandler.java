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
package org.eclipse.nebula.widgets.cdatetime.css;

import org.eclipse.e4.ui.css.core.css2.CSS2FontHelper;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues;
import org.eclipse.e4.ui.css.swt.dom.ControlElement;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

@SuppressWarnings("restriction")
public class CDateTimePropertyHandler implements ICSSPropertyHandler {
	@Override
	public boolean applyCSSProperty(final Object element, final String property, final CSSValue value,
			final String pseudo, final CSSEngine engine) throws Exception {
		final CDateTime cdt = (CDateTime) ((ControlElement) element).getNativeWidget();

		// General properties
		if ("cdt-background-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setBackground(newColor);
		}
		if ("cdt-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setForeground(newColor);
		}
		if ("cdt-font".equals(property)) {
			applyCSSPropertyFont(cdt, value, false);
		}
		if ("cdt-font-style".equals(property)) {
			applyCSSPropertyStyle(cdt, value, false);
		}
		if ("cdt-font-size".equals(property)) {
			applyCSSPropertySize(cdt, value, false);
		}
		if ("cdt-font-weight".equals(property)) {
			applyCSSPropertyWeight(cdt, value, false);
		}
		if ("cdt-font-family".equals(property)) {
			applyCSSPropertyFamily(cdt, value, false);
		}

		// Picker
		if ("cdt-picker-background-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setPickerBackgroundColor(newColor);
		}
		if ("cdt-picker-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setPickerForegroundColor(newColor);
		}
		if ("cdt-picker-font".equals(property)) {
			applyCSSPropertyFont(cdt, value, true);
		}
		if ("cdt-picker-font-style".equals(property)) {
			applyCSSPropertyStyle(cdt, value, true);
		}
		if ("cdt-picker-font-size".equals(property)) {
			applyCSSPropertySize(cdt, value, true);
		}
		if ("cdt-picker-font-weight".equals(property)) {
			applyCSSPropertyWeight(cdt, value, true);
		}
		if ("cdt-picker-font-family".equals(property)) {
			applyCSSPropertyFamily(cdt, value, true);
		}

		if ("cdt-picker-active-day-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setPickerActiveDayColor(newColor);
		}
		if ("cdt-picker-inactive-day-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setPickerInactiveDayColor(newColor);
		}
		if ("cdt-picker-today-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setPickerTodayColor(newColor);
		}

		// Minutes
		if ("cdt-picker-minutes-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setPickerMinutesColor(newColor);
		}
		if ("cdt-picker-minutes-background-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setPickerMinutesBackgroundColor(newColor);
		}

		// Picker buttons
		if ("cdt-button-hover-border-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setButtonHoverBorderColor(newColor);
		}
		if ("cdt-button-hover-background-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setButtonHoverBackgroundColor(newColor);
		}
		if ("cdt-button-selected-border-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setButtonSelectedBorderColor(newColor);
		}
		if ("cdt-button-selected-background-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setButtonSelectedBackgroundColor(newColor);
		}

		// Ok, Cancel & clear buttons
		if ("cdt-ok-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setOkButtonColor(newColor);
		}
		if ("cdt-cancel-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setCancelButtonColor(newColor);
		}
		if ("cdt-clear-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, cdt.getDisplay());
			cdt.setClearButtonForegroundColor(newColor);
		}

		if ("cdt-clear-font".equals(property)) {
			applyCSSPropertyFont(cdt, value, true);
		}
		if ("cdt-clear-font-style".equals(property)) {
			applyCSSPropertyStyle(cdt, value, true);
		}
		if ("cdt-clear-font-size".equals(property)) {
			applyCSSPropertySize(cdt, value, true);
		}
		if ("cdt-clear-font-weight".equals(property)) {
			applyCSSPropertyWeight(cdt, value, true);
		}
		if ("cdt-clear-font-family".equals(property)) {
			applyCSSPropertyFamily(cdt, value, true);
		}
		return true;
	}

	// CSS Font
	private void applyCSSPropertyFont(final Control widget, final CSSValue value, final boolean picker)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			final CSSValueList valueList = (CSSValueList) value;
			final int length = valueList.getLength();
			for (int i = 0; i < length; i++) {
				final CSSValue value2 = valueList.item(i);
				if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					final String cssProp = CSS2FontHelper.getCSSFontPropertyName((CssValues.CssPrimitive) value2);
					if (cssProp.equals("font-family")) {
						applyCSSPropertyFamily(widget, value2, picker);
					} else if (cssProp.equals("font-size")) {
						applyCSSPropertySize(widget, value2, picker);
					} else if (cssProp.equals("font-weight")
							&& ("bold".equals(value2.getCssText()) || "bolder".equals(value2.getCssText()))) {
						applyCSSPropertyWeight(widget, value2, picker);
					} else if (cssProp.equals("font-style")
							&& ("italic".equals(value2.getCssText()) || "oblique".equals(value2.getCssText()))) {
						applyCSSPropertyStyle(widget, value2, picker);
					}
				}
			}
		}
	}

	private void applyCSSPropertyStyle(final Control widget, final CSSValue value, final boolean picker)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(widget, picker);
			boolean modified = false;
			if ("italic".equals(value.getCssText()) || "oblique".equals(value.getCssText())) {
				modified = (fd.getStyle() & SWT.ITALIC) != SWT.ITALIC;
				if (modified) {
					fd.setStyle(fd.getStyle() | SWT.ITALIC);
				}
			} else {
				modified = (fd.getStyle() & SWT.ITALIC) == SWT.ITALIC;
				if (modified) {
					fd.setStyle(fd.getStyle() | ~SWT.ITALIC);
				}
			}
			if (modified) {
				if (picker) {
					applyFontForPicker((CDateTime) widget, fd);
				} else {
					applyFont(widget, fd);
				}
			}

		}
	}

	private void applyFont(final Control widget, final FontData fd) {
		if (widget.getFont() != null && !widget.getFont().equals(widget.getDisplay().getSystemFont())) {
			widget.getFont().dispose();
		}
		final Font newFont = new Font(widget.getDisplay(), fd);
		widget.setFont(newFont);
		widget.addListener(SWT.Dispose, e -> {
			if (newFont != null && !newFont.isDisposed()) {
				newFont.dispose();
			}
		});
	}

	private void applyFontForPicker(final CDateTime widget, final FontData fd) {
		if (widget.getPickerFont() != null && !widget.getPickerFont().equals(widget.getDisplay().getSystemFont())) {
			widget.getPickerFont().dispose();
		}
		final Font newFont = new Font(widget.getDisplay(), fd);
		widget.setPickerFont(newFont);
		widget.addListener(SWT.Dispose, e -> {
			if (newFont != null && !newFont.isDisposed()) {
				newFont.dispose();
			}
		});
	}

	private void applyCSSPropertySize(final Control widget, final CSSValue value, final boolean picker)
			throws Exception {

		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(widget, picker);
			final CSSPrimitiveValue m = (CSSPrimitiveValue) value;

			final int newSize = Math.round(m.getFloatValue((short) 0));
			final boolean modified = fd.getHeight() != newSize;
			if (modified) {
				fd.setHeight(newSize);
				if (picker) {
					applyFontForPicker((CDateTime) widget, fd);
				} else {
					applyFont(widget, fd);
				}
			}
		}
	}

	private void applyCSSPropertyWeight(final Control widget, final CSSValue value, final boolean picker)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(widget, picker);
			boolean modified = false;
			if ("bold".equals(value.getCssText()) || "bolder".equals(value.getCssText())) {
				modified = (fd.getStyle() & SWT.BOLD) != SWT.BOLD;
				if (modified) {
					fd.setStyle(fd.getStyle() | SWT.BOLD);
				}
			} else {
				modified = (fd.getStyle() & SWT.BOLD) == SWT.BOLD;
				if (modified) {
					fd.setStyle(fd.getStyle() | ~SWT.BOLD);
				}
			}
			if (modified) {
				if (picker) {
					applyFontForPicker((CDateTime) widget, fd);
				} else {
					applyFont(widget, fd);
				}
			}

		}
	}

	private void applyCSSPropertyFamily(final Control widget, final CSSValue value, final boolean picker)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(widget, picker);
			final boolean modified = !fd.getName().equals(value.getCssText());
			if (modified) {
				fd.setName(value.getCssText());
				if (picker) {
					applyFontForPicker((CDateTime) widget, fd);
				} else {
					applyFont(widget, fd);
				}
			}
		}
	}

	@Override
	public String retrieveCSSProperty(final Object element, final String property, final String pseudo,
			final CSSEngine engine) throws Exception {
		return null;
	}
}