/*******************************************************************************
 * Copyright (c) 2013 Tom Schindl.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0 
 * 
 * Contributors: Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.pshelf.css.internal;

import org.eclipse.e4.ui.css.core.css2.CSS2FontPropertiesHelpers;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.nebula.widgets.pshelf.css.CSSShelfRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

@SuppressWarnings("restriction")
public class CSSEngineHelper {
	Control control;
	IStylingEngine styleEngine;
	CSSStyleDeclaration style;
	CSSShelfRenderer renderer;
	CSSEngine cssEngine;

	public CSSEngineHelper(IStylingEngine styleEngine, Control control, CSSShelfRenderer renderer) {
		this.styleEngine = styleEngine;
		this.cssEngine = WidgetElement.getEngine(control);
		this.control = control;
		this.renderer = renderer;
		initialize();
	}

	private void initialize() {
		if (styleEngine != null) {
			style = styleEngine.getStyle(control);
		}
	}
	
	public Color getBaseColorSelected() {
		if( style == null ) {
			return control.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
		}
		return getColor(style.getPropertyCSSValue("pshelf-basecolor-selected"), control.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
	}

	public Color getBaseColorUnselected() {
		if( style == null ) {
			return control.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		}
		return getColor(style.getPropertyCSSValue("pshelf-basecolor-unselected"), control.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
	}
	
	public Color getBaseColorHover() {
		if( style == null ) {
			return control.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		}
		return getColor(style.getPropertyCSSValue("pshelf-basecolor-hover"), control.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
	}
	
	public Color getSelectedColorStart() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-selectedcolor-start"), null);
	}
	
	public Color getSelectedColorEnd() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-selectedcolor-end"), null);
	}
	
	public Color getUnselectedColorStart() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-unselectedcolor-start"), null);
	}
	
	public Color getUnselectedColorEnd() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-unselectedcolor-end"), null);
	}
	
	public Color getHoverColorStart() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-hovercolor-start"), null);
	}
	
	public Color getHoverColorEnd() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-hovercolor-end"), null);
	}
	
	public Color getForegroundColor() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-foregroundcolor"), null);
	}
	
	public Color getSelectedForegroundColor() {
		if( style == null ) {
			return null;
		}
		return getColor(style.getPropertyCSSValue("pshelf-selected-foregroundcolor"), null);
	}
	
	public Font getSelectedFont() {
		if( style == null ) {
			return null;
		}
		return getFont("pshelf-selected-");
	}
	
	public Font getUnselectedFont() {
		if( style == null ) {
			return null;
		}
		return getFont("pshelf-unselected-");
	}
	
	private Color getColor(CSSValue value, Color defaultValue) {
		if( value instanceof RGBColor ) {
			RGBColor rgbValue = (RGBColor) value;
			RGB rgb = new RGB(
					Integer.parseInt(rgbValue.getRed().getCssText()), 
					Integer.parseInt(rgbValue.getGreen().getCssText()),
					Integer.parseInt(rgbValue.getBlue().getCssText()));
			return new Color(rgb);
		} else if( value != null ) {
			try {
				Color c = (Color) cssEngine.convert(value, Color.class, control.getDisplay());
				// Create a copy because we are disposing this colors!!!
				return new Color(c.getRed(),c.getGreen(),c.getBlue());
			} catch (Exception e) {
			}
		}
		
		
		return defaultValue;
	}
	
	private Font getFont(String prefix) {
		CSS2FontPropertiesImpl properties = new CSS2FontPropertiesImpl();
		boolean modified = false;
		
		{
			CSSValue v = style.getPropertyCSSValue(prefix+"font-family");
			if( v != null ) {
				modified = true;
				CSS2FontPropertiesHelpers.updateCSSPropertyFont(properties, "font-family", v);
			}
		}
		
		{
			CSSValue v = style.getPropertyCSSValue(prefix+"font-size");
			if( v != null ) {
				modified = true;
				CSS2FontPropertiesHelpers.updateCSSPropertyFont(properties, "font-size", v);
			}
		}
		
		{
			CSSValue v = style.getPropertyCSSValue(prefix+"font-style");
			if( v != null ) {
				modified = true;
				CSS2FontPropertiesHelpers.updateCSSPropertyFont(properties, "font-style", v);
			}
		}
		
		{
			CSSValue v = style.getPropertyCSSValue(prefix+"font");
			if( v != null ) {
				modified = true;
				CSS2FontPropertiesHelpers.updateCSSPropertyFont(properties, "font", v);
			}
		}
		
		if( modified ) {
			return CSSSWTFontHelper.getFont(properties, control);	
		}
		
		return null;
	}
}
