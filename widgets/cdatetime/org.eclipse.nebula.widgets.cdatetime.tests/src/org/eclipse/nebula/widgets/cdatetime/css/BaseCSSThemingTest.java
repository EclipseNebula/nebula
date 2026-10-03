package org.eclipse.nebula.widgets.cdatetime.css;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandlerProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.SWTElementProvider;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.nebula.cwt.test.AbstractVTestCase;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.cdatetime.CdtTester;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * Base class for CSS tests
 */
@SuppressWarnings("restriction")
public class BaseCSSThemingTest extends AbstractVTestCase {

	protected CSSSWTEngineImpl engine;
	protected CdtTester tester;

	@FunctionalInterface
	protected interface ColorGetter {
		Color getColor(CDateTime cdt);
	}
	
	@FunctionalInterface
	protected interface FontGetter {
		Font getFont(CDateTime cdt);
	}

	@Override
	protected void setUp() throws Exception {
	    super.setUp();

	    engine = new CSSSWTEngineImpl(getShell().getDisplay());

	    String[] props = {
	        "cdt-background-color", "cdt-color", "cdt-font",
	        "cdt-font-style", "cdt-font-size", "cdt-font-weight",
	        "cdt-font-family", "cdt-picker-background-color",
	        "cdt-picker-color", "cdt-picker-font",
	        "cdt-picker-font-style", "cdt-picker-font-size",
	        "cdt-picker-font-weight", "cdt-picker-font-family",
	        "cdt-picker-active-day-color", "cdt-picker-inactive-day-color",
	        "cdt-picker-today-color", "cdt-picker-minutes-color",
	        "cdt-picker-minutes-background-color",
	        "cdt-button-hover-border-color",
	        "cdt-button-hover-background-color",
	        "cdt-button-selected-border-color",
	        "cdt-button-selected-background-color"
	    };

	    Set<String> names = Set.of(props);
	    ICSSPropertyHandler handler = new CDateTimePropertyHandler();

	    engine.registerCSSPropertyHandlerProvider(
	        new ICSSPropertyHandlerProvider() {

	            @Override
	            public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
	                    String property) {
	                return names.contains(property)
	                    ? List.of(handler) : List.of();
	            }

	            @Override
	            public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
	                    Object element, String property) {
	                return getCSSPropertyHandlers(property);
	            }

	            @Override
	            public Collection<String> getCSSProperties(Object element) {
	                return names;
	            }

	            @Override
	            public CSSStyleDeclaration getDefaultCSSStyleDeclaration(
	                    CSSEngine cssEngine, Object element,
	                    CSSStyleDeclaration newStyle, String pseudo)
	                    throws Exception {

	                if (!(cssEngine.getElement(element)
	                        instanceof CSSStylableElement styled)) {
	                    return null;
	                }

	                CSSStyleDeclaration defaults =
	                    styled.getDefaultStyleDeclaration(pseudo);
	                if (defaults != null) {
	                    return defaults;
	                }

	                StringBuilder css = new StringBuilder();
	                for (String property : names) {
	                    String original = cssEngine.retrieveCSSProperty(
	                        styled, property, pseudo);
	                    if (original != null) {
	                        css.append(property).append(':')
	                           .append(original).append(';');
	                    }
	                }

	                if (css.length() == 0) {
	                    return null;
	                }

	                defaults = cssEngine.parseStyleDeclaration(css.toString());
	                styled.setDefaultStyleDeclaration(pseudo, defaults);
	                return defaults;
	            }
	        });

	    engine.setElementProvider(new SWTElementProvider());
	    engine.setErrorHandler(e -> e.printStackTrace());
	}

	protected void checkColor(final String property, final ColorGetter getter) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					int r = (int) (Math.random() * 255);
					int g = (int) (Math.random() * 255);
					int b = (int) (Math.random() * 255);
					applyCSS(String.format("%s: rgb(%d, %d, %d);", property, r, g, b));
					Color bgColor = getter.getColor(tester.getCDateTime());
					result[0] = (bgColor != null && bgColor.getRed() == r && bgColor.getGreen() == g && bgColor.getBlue() == b);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		assertTrue(result[0]);
	}
	
	protected void checkFont(final String property, final FontGetter getter) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					applyCSS(property + ": Verdana 11px italic;");
					Font font = getter.getFont(tester.getCDateTime());
					if (font == null) {
						return;
					}
					FontData fd = font.getFontData()[0];
					boolean assertFontName = "Verdana".equals(fd.getName());
					boolean assertFontSize = fd.getHeight() == 11;
					boolean assertFontStyle = (fd.getStyle() & SWT.ITALIC) == SWT.ITALIC;

					result[0] = assertFontName && assertFontSize && assertFontStyle;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		assertTrue(result[0]);
	}

	protected void checkFontStyle(final String property, final FontGetter getter) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					applyCSS(property + ": italic;");
					Font font = getter.getFont(tester.getCDateTime());
					if (font == null) {
						return;
					}
					FontData fd = font.getFontData()[0];
					result[0] = (fd.getStyle() & SWT.ITALIC) == SWT.ITALIC;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		assertTrue(result[0]);
	}
	
	protected void checkFontSize(final String property, final FontGetter getter) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					applyCSS(property + ": 13px;");
					Font font = getter.getFont(tester.getCDateTime());
					if (font == null) {
						return;
					}
					FontData fd = font.getFontData()[0];
					result[0] = fd.getHeight() == 13;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		assertTrue(result[0]);
	}
	
	protected void checkFontWeight(final String property, final FontGetter getter) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					applyCSS(property + ": bold;");
					Font font = getter.getFont(tester.getCDateTime());
					if (font == null) {
						return;
					}
					FontData fd = font.getFontData()[0];
					result[0] = (fd.getStyle() & SWT.BOLD) == SWT.BOLD;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		assertTrue(result[0]);
	}
	
	protected void checkFontFamily(final String property, final FontGetter getter) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					applyCSS(property + ": Helvetica;");
					Font font = getter.getFont(tester.getCDateTime());
					if (font == null) {
						return;
					}
					FontData fd = font.getFontData()[0];
					result[0] = "Helvetica".equals(fd.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		assertTrue(result[0]);
	}
	
	private void applyCSS(String css) throws Exception {
		engine.parseStyleSheet(new StringReader("CDateTime {" + css + "}"));
		engine.applyStyles(getShell(), true);
	}

}
