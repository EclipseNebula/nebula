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
package org.eclipse.nebula.widgets.grid.css;

import org.eclipse.e4.ui.css.core.css2.CSS2FontHelper;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.Win7RendererSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

@SuppressWarnings("restriction")
public class GridPropertyHandler implements ICSSPropertyHandler {
	private static final String FOOTER = "footer";
	private static final String HEADER = "header";

	/**
	 * @see org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler#applyCSSProperty(java.lang.Object,
	 *      java.lang.String, org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.eclipse.e4.ui.css.core.engine.CSSEngine)
	 */
	@Override
	public boolean applyCSSProperty(final Object element, final String property, final CSSValue value,
			final String pseudo, final CSSEngine engine) throws Exception {

		final Grid grid = (Grid) ((GridElement) element).getNativeWidget();

		// Theme
		if ("grid-theme".equals(property)) {
			if (value.getCssText() != null && value.getCssText().equals("win7")) {
				Win7RendererSupport.create(grid).decorate();
			}
		}

		// General
		if ("grid-cell-header-selection-background-color".equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, grid.getDisplay());
			grid.setCellHeaderSelectionBackground(newColor);
		}

		if ("grid-header-visible".equals(property)) {
			final boolean headerVisible = value == null ? false : Boolean.parseBoolean(value.getCssText());
			grid.setHeaderVisible(headerVisible);
		}

		if ("grid-footer-visible".equals(property)) {
			final boolean footerVisible = value == null ? false : Boolean.parseBoolean(value.getCssText());
			grid.setFooterVisible(footerVisible);
		}

		// Items
		if ("grid-item-height".equals(property)) {
			if (value instanceof CssValues.CssNumeric numeric) {
				final int width = Math.round(Math.round((float) numeric.value()));
				grid.setItemHeight(width);
			}
		}

		if ("grid-item-header-width".equals(property)) {
			if (value instanceof CssValues.CssNumeric numeric) {
				final int width = Math.round(Math.round((float) numeric.value()));
				grid.setItemHeaderWidth(width);
			}
		}

		// Lines
		if ("grid-line-color".equals(property) && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Color newColor = (Color) engine.convert(value, Color.class, grid.getDisplay());
			grid.setLineColor(newColor);
		}

		if ("grid-lines-visible".equals(property)) {
			final boolean linesVisible = value == null ? false : Boolean.parseBoolean(value.getCssText());
			grid.setLinesVisible(linesVisible);
		}

		if ("grid-tree-lines-visible".equals(property)) {
			final boolean treeLinesVisible = value == null ? false : Boolean.parseBoolean(value.getCssText());
			grid.setTreeLinesVisible(treeLinesVisible);
		}

		// Columns
		if ("grid-columns-alignment".equals(property) && grid.getColumns() != null) {
			final int alignment;
			if ("left".equals(value.getCssText())) {
				alignment = SWT.LEFT;
			} else if ("right".equals(value.getCssText())) {
				alignment = SWT.RIGHT;
			} else if ("center".equals(value.getCssText())) {
				alignment = SWT.CENTER;
			} else {
				alignment = SWT.NONE;
			}
			if (alignment == SWT.LEFT || alignment == SWT.CENTER || alignment == SWT.RIGHT)
				for (GridColumn col : grid.getColumns()) {
					col.setAlignment(alignment);
				}
		}

		if ("grid-columns-header-font".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyFont(element, grid, value, HEADER);
		}
		if ("grid-columns-header-font-style".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyStyle(element, grid, value, HEADER);
		}
		if ("grid-columns-header-font-size".equals(property) && grid.getColumns() != null) {
			applyCSSPropertySize(element, grid, value, HEADER);
		}
		if ("grid-columns-header-font-weight".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyWeight(element, grid, value, HEADER);
		}
		if ("grid-columns-header-font-family".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyFamily(element, grid, value, HEADER);
		}

		if ("grid-columns-footer-font".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyFont(element, grid, value, FOOTER);
		}
		if ("grid-columns-footer-font-style".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyStyle(element, grid, value, FOOTER);
		}
		if ("grid-columns-footer-font-size".equals(property) && grid.getColumns() != null) {
			applyCSSPropertySize(element, grid, value, FOOTER);
		}
		if ("grid-columns-footer-font-weight".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyWeight(element, grid, value, FOOTER);
		}
		if ("grid-columns-footer-font-family".equals(property) && grid.getColumns() != null) {
			applyCSSPropertyFamily(element, grid, value, FOOTER);
		}

		return true;
	}

	private void applyCSSPropertyFont(final Object element, final Grid grid, final CSSValue value, String target)
			throws Exception {

		if (value instanceof CSSValueList valueList) {
			final int length = valueList.getLength();

			for (int i = 0; i < length; i++) {
				final CSSValue value2 = valueList.item(i);

				if (value2 instanceof CssValues.CssPrimitive primitive) {
					final String cssProp = CSS2FontHelper.getCSSFontPropertyName(primitive);

					if ("font-family".equals(cssProp)) {
						applyCSSPropertyFamily(element, grid, value2, target);
					} else if ("font-size".equals(cssProp)) {
						applyCSSPropertySize(element, grid, value2, target);
					} else if ("font-weight".equals(cssProp)
							&& ("bold".equals(value2.getCssText()) || "bolder".equals(value2.getCssText()))) {
						applyCSSPropertyWeight(element, grid, value2, target);
					} else if ("font-style".equals(cssProp)
							&& ("italic".equals(value2.getCssText()) || "oblique".equals(value2.getCssText()))) {
						applyCSSPropertyStyle(element, grid, value2, target);
					}
				}
			}
		}
	}

	private boolean applyCSSPropertyStyle(final Object element, final Grid grid, final CSSValue value, String target)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(grid);
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
				applyFont(grid, fd, target);
			}

		}
		return true;
	}

	private void applyFont(final Grid grid, final FontData fd, String target) {
		for (GridColumn column : grid.getColumns()) {
			applyFont(column, fd, target);
		}
	}

	private void applyFont(final GridColumn column, final FontData fd, String target) {
		if (target.equals(HEADER)) {
			if (column.getHeaderFont() != null && !column.getHeaderFont().equals(column.getDisplay().getSystemFont())) {
				column.getHeaderFont().dispose();
			}
		} else {
			if (column.getFooterFont() != null && !column.getFooterFont().equals(column.getDisplay().getSystemFont())) {
				column.getFooterFont().dispose();
			}
		}
		final Font newFont = new Font(column.getDisplay(), fd);
		if (target.equals(HEADER)) {
			column.setHeaderFont(newFont);
		} else {
			column.setFooterFont(newFont);
		}

		column.getParent().addListener(SWT.Dispose, e -> {
			if (newFont != null && !newFont.isDisposed()) {
				newFont.dispose();
			}
		});
	}

	private boolean applyCSSPropertySize(final Object element, final Grid grid, final CSSValue value, String target)
			throws Exception {

		if (value instanceof CssValues.CssNumeric numeric) {
			final FontData fd = CSSEngineHelper.getFontData(grid);
			final int newSize = Math.round((float) numeric.value());

			final boolean modified = fd.getHeight() != newSize;
			if (modified) {
				fd.setHeight(newSize);
				applyFont(grid, fd, target);
			}
		}

		return true;
	}

	private boolean applyCSSPropertyWeight(final Object element, final Grid grid, final CSSValue value, String target)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(grid);
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
				applyFont(grid, fd, target);
			}

		}
		return true;
	}

	private boolean applyCSSPropertyFamily(final Object element, final Grid grid, final CSSValue value, String target)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final FontData fd = CSSEngineHelper.getFontData(grid);
			final boolean modified = !fd.getName().equals(value.getCssText());
			if (modified) {
				fd.setName(value.getCssText());
				applyFont(grid, fd, target);
			}
		}
		return true;
	}

	/**
	 * @see org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler#retrieveCSSProperty(java.lang.Object,
	 *      java.lang.String, java.lang.String,
	 *      org.eclipse.e4.ui.css.core.engine.CSSEngine)
	 */
	@Override
	public String retrieveCSSProperty(final Object element, final String property, final String pseudo,
			final CSSEngine engine) throws Exception {
		return null;
	}
}