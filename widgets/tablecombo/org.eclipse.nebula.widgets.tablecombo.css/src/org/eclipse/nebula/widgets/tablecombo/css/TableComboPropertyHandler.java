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
package org.eclipse.nebula.widgets.tablecombo.css;

import org.eclipse.e4.ui.css.core.css2.CSS2FontHelper;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

@SuppressWarnings("restriction")
public class TableComboPropertyHandler implements ICSSPropertyHandler {
	@Override
	public boolean applyCSSProperty(final Object element, final String property, final CSSValue value,
			final String pseudo, final CSSEngine engine) throws Exception {
		final TableCombo tc = (TableCombo) ((TableComboElement) element).getNativeWidget();
		// General properties
		if ("tablecombo-show-table-lines".equals(property)) {
			final boolean showTableLines = value == null ? false : Boolean.parseBoolean(value.getCssText());
			tc.setShowTableLines(showTableLines);
		}
		if ("tablecombo-show-table-header".equals(property)) {
			final boolean showTableHeader = value == null ? false : Boolean.parseBoolean(value.getCssText());
			tc.setShowTableHeader(showTableHeader);
		}
		if ("tablecombo-border-color".equals(property)) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.setBorderColor(newColor);
		}

		// Text styling
		if ("tablecombo-text-background-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.getTextControl().setBackground(newColor);
		}
		if ("tablecombo-text-color".equals(property) && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.getTextControl().setForeground(newColor);
		}

		if ("tablecombo-text-font".equals(property)) {
			applyCSSPropertyFont(element, tc.getTextControl(), value);
		}
		if ("tablecombo-text-font-style".equals(property)) {
			applyCSSPropertyStyle(element, tc.getTextControl(), value);
		}
		if ("tablecombo-text-font-size".equals(property)) {
			applyCSSPropertySize(element, tc.getTextControl(), value);
		}
		if ("tablecombo-text-font-weight".equals(property)) {
			applyCSSPropertyWeight(element, tc.getTextControl(), value);
		}
		if ("tablecombo-text-font-family".equals(property)) {
			applyCSSPropertyFamily(element, tc.getTextControl(), value);
		}

		// Button styling
		if ("tablecombo-button-color".equals(property) && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.getArrowControl().setForeground(newColor);
		}
		if ("tablecombo-button-background-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.getArrowControl().setBackground(newColor);
		}

		// Table styling
		if ("tablecombo-table-color".equals(property) && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.getTable().setForeground(newColor);
		}
		if ("tablecombo-table-background-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.getTable().setBackground(newColor);
		}
		if ("tablecombo-table-font".equals(property)) {
			applyCSSPropertyFont(element, tc.getTable(), value);
		}
		if ("tablecombo-table-font-style".equals(property)) {
			applyCSSPropertyStyle(element, tc.getTable(), value);
		}
		if ("tablecombo-table-font-size".equals(property)) {
			applyCSSPropertySize(element, tc.getTable(), value);
		}
		if ("tablecombo-table-font-weight".equals(property)) {
			applyCSSPropertyWeight(element, tc.getTable(), value);
		}
		if ("tablecombo-table-font-family".equals(property)) {
			applyCSSPropertyFamily(element, tc.getTable(), value);
		}

		if ("tablecombo-table-width-percentage".equals(property)) {
			if (value instanceof CssValues.CssNumeric numeric) {
				final int width = Math.round((float) numeric.value());
				tc.setTableWidthPercentage(width);
			}
		}

		// Odd line styling
		if ("tablecombo-table-odd-lines-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.setOddLinesForegroundColor(newColor);
		}
		if ("tablecombo-table-odd-lines-background-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.setOddLinesBackgroundColor(newColor);
		}

		// Even line styling
		if ("tablecombo-table-even-lines-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.setEvenLinesForegroundColor(newColor);
		}
		if ("tablecombo-table-even-lines-background-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, tc.getDisplay());
			tc.setEvenLinesBackgroundColor(newColor);
		}
		return true;
	}

	private void applyCSSPropertyFont(final Object element, final Control widget, final CSSValue value)
			throws Exception {

		if (value instanceof CSSValueList valueList) {
			for (int i = 0; i < valueList.getLength(); i++) {
				final CSSValue value2 = valueList.item(i);

				if (value2 instanceof CssValues.CssPrimitive primitive) {
					final String cssProp = CSS2FontHelper.getCSSFontPropertyName(primitive);

					if ("font-family".equals(cssProp)) {
						applyCSSPropertyFamily(element, widget, value2);
					} else if ("font-size".equals(cssProp)) {
						applyCSSPropertySize(element, widget, value2);
					} else if ("font-weight".equals(cssProp)
							&& ("bold".equals(value2.getCssText()) || "bolder".equals(value2.getCssText()))) {
						applyCSSPropertyWeight(element, widget, value2);
					} else if ("font-style".equals(cssProp)
							&& ("italic".equals(value2.getCssText()) || "oblique".equals(value2.getCssText()))) {
						applyCSSPropertyStyle(element, widget, value2);
					}
				}
			}
		}
	}

	private boolean applyCSSPropertyStyle(final Object element, final Control widget, final CSSValue value)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(widget);
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
				applyFont(widget, fd);
			}

		}
		return true;
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

	private boolean applyCSSPropertySize(final Object element, final Control widget, final CSSValue value)
			throws Exception {

		if (value instanceof CssValues.CssNumeric numeric) {
			final FontData fd = CSSEngineHelper.getFontData(widget);
			final int newSize = Math.round((float) numeric.value());

			if (fd.getHeight() != newSize) {
				fd.setHeight(newSize);
				applyFont(widget, fd);
			}
		}
		return true;
	}

	private boolean applyCSSPropertyWeight(final Object element, final Control widget, final CSSValue value)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(widget);
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
				applyFont(widget, fd);
			}

		}
		return true;
	}

	private boolean applyCSSPropertyFamily(final Object element, final Control widget, final CSSValue value)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(widget);
			final boolean modified = !fd.getName().equals(value.getCssText());
			if (modified) {
				fd.setName(value.getCssText());
				applyFont(widget, fd);
			}
		}
		return true;
	}

	@Override
	public String retrieveCSSProperty(final Object element, final String property, final String pseudo,
			final CSSEngine engine) throws Exception {
		return null;
	}
}