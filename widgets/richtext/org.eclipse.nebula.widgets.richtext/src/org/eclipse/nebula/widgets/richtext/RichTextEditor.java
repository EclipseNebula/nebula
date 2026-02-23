/*****************************************************************************
 * Copyright (c) 2015, 2026 CEA LIST.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.richtext;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.nebula.widgets.richtext.painter.ResourceHelper;
import org.eclipse.nebula.widgets.richtext.toolbar.JavaCallbackListener;
import org.eclipse.nebula.widgets.richtext.toolbar.ToolbarButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Rich Text Editor control that wraps a {@link Browser} with enabled Javascript that shows a simple
 * HTML template containing a ckeditor as rich text editor.
 *
 * <p>
 * The following style bits are supported:
 * <ul>
 * <li>{@link SWT#RESIZE} - specify if the resize function of ckeditor is enabled (mostly used for
 * embedded usage)</li>
 * <li>{@link SWT#MIN} - specify if the configured minimum dimensions should be applied to the
 * resize function of ckeditor</li>
 * <li>{@link SWT#EMBEDDED} - specify if the rich text editor is used in embedded mode (e.g. as a
 * cell editor of a JFace viewer)</li>
 * </ul>
 * Additionally the SWT Browser style bits {@link SWT#MOZILLA} or {@link SWT#WEBKIT} can be set to
 * specify the native browser that should be used for rendering
 * </p>
 *
 * @see <a href="http://ckeditor.com/">http://ckeditor.com/</a>
 */
public class RichTextEditor extends Composite {

	private Double CKEDITOR_ALT;
	private Double CKEDITOR_CTRL;
	private Double CKEDITOR_SHIFT;

	private boolean editorLoaded = false;
	private String initialValue = null;
	private boolean initialSetFocus = false;
	private boolean initialSetEditable = true;

	protected Rectangle resizedBounds = null;

	private final Browser browser;

	private final List<BrowserFunction> browserFunctions = new ArrayList<>();

	private final ListenerList<ModifyListener> modifyListener = new ListenerList<>(ListenerList.IDENTITY);
	private final ListenerList<KeyListener> keyListener = new ListenerList<>(ListenerList.IDENTITY);
	private final ListenerList<FocusListener> focusListener = new ListenerList<>(ListenerList.IDENTITY);
	private final ListenerList<JavaCallbackListener> javaCallbackListener = new ListenerList<>(ListenerList.IDENTITY);

	private final RichTextEditorConfiguration editorConfig;

	private Shell embeddedShell;
	private Point mouseDragPosition;

	private boolean handleFocusChanges = true;
	
	private int[] containerEdgeDiffs = null;
	private String autoScaleProperty;

	/**
	 * Key of the system property to specify a fixed directory to unpack the ckeditor resources to.
	 * If a system property for that key is registered and the rich text control is deployed within
	 * a JAR, the resources will be unpacked into the specified directory. If no value is registered
	 * for that key and the rich text control is deployed in a JAR, the resources will be unpacked
	 * into a temporary directory, that gets deleted when the runtime is shutdown. If the rich text
	 * control is not deployed within a JAR but as part of an Eclipse application, the bundle will
	 * be unpacked automatically. In this case this system property won't get interpreted.
	 */
	public static final String JAR_UNPACK_LOCATION_PROPERTY = "org.eclipse.nebula.widgets.richtext.jar.unpackdir";

	private static URL templateURL;
	static {
		locateTemplateURL();
	}

	/**
	 * Creates a {@link RichTextEditor} that wraps a {@link Browser} using the style bit
	 * {@link SWT#NONE} and the default {@link RichTextEditorConfiguration}.
	 *
	 * @param parent
	 *            the parent composite where this rich text editor should be added to
	 */
	public RichTextEditor(final Composite parent) {
		this(parent, (RichTextEditorConfiguration) null, SWT.NONE);
	}

	/**
	 * Creates a {@link RichTextEditor} that wraps a {@link Browser} using the style bit
	 * {@link SWT#NONE} and creates a {@link RichTextEditorConfiguration} out of the given
	 * {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration}.
	 *
	 * @param parent
	 *            the parent composite where this rich text editor should be added to
	 * @param toolbarConfig
	 *            the {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration} to
	 *            use or <code>null</code> for using the default
	 *            {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration}
	 * @deprecated use constructors that take a {@link RichTextEditorConfiguration}
	 */
	@Deprecated
	public RichTextEditor(final Composite parent, final org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration toolbarConfig) {
		this(parent, toolbarConfig, SWT.NONE);
	}

	/**
	 * Creates a {@link RichTextEditor} that wraps a {@link Browser} using the style bit
	 * {@link SWT#NONE} and the given {@link RichTextEditorConfiguration}.
	 *
	 * @param parent
	 *            the parent composite where this rich text editor should be added to
	 * @param editorConfig
	 *            the {@link RichTextEditorConfiguration} to use or <code>null</code> for using the default
	 *            {@link RichTextEditorConfiguration}
	 */
	public RichTextEditor(final Composite parent, final RichTextEditorConfiguration editorConfig) {
		this(parent, editorConfig, SWT.NONE);
	}

	/**
	 * Creates a {@link RichTextEditor} that wraps a {@link Browser} using the given style bit
	 *  and the default {@link RichTextEditorConfiguration}.
	 *
	 * @param parent
	 *            the parent composite where this rich text editor should be added to
	 * @param style
	 *            the style of widget to construct, see {@link Browser} for further style bit
	 *            information
	 */
	public RichTextEditor(final Composite parent, final int style) {
		this(parent, (RichTextEditorConfiguration) null, style);
	}

	/**
	 * Creates a {@link RichTextEditor} that wraps a {@link Browser} using the given style bit and
	 * creates a {@link RichTextEditorConfiguration} out of the given {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration}.
	 *
	 * @param parent
	 *            the parent composite where this rich text editor should be added to
	 * @param toolbarConfig
	 *            the {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration} to use or <code>null</code> for using the default
	 *            {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration}
	 * @param style
	 *            the style of widget to construct, see {@link Browser} for further style bit
	 *            information
	 *
	 * @see Browser
	 * @deprecated use constructors that take a {@link RichTextEditorConfiguration}
	 */
	@Deprecated
	public RichTextEditor(final Composite parent, final org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration toolbarConfig, final int style) {
		this(parent, toolbarConfig != null ? new RichTextEditorConfiguration(toolbarConfig) : null, style);
	}

	/**
	 * Creates a {@link RichTextEditor} that wraps a {@link Browser} using the given style bit
	 * and the given {@link RichTextEditorConfiguration}.
	 *
	 * @param parent
	 *            the parent composite where this rich text editor should be added to
	 * @param editorConfig
	 *            the {@link RichTextEditorConfiguration} to use or <code>null</code> for using the default
	 *            {@link RichTextEditorConfiguration}
	 * @param style
	 *            the style of widget to construct, see {@link Browser} for further style bit
	 *            information
	 *
	 * @see Browser
	 */
	public RichTextEditor(final Composite parent, final RichTextEditorConfiguration editorConfig, final int style) {
		super(parent, style);

		setLayout(new FillLayout());

		final boolean resizable = (getStyle() & SWT.RESIZE) != 0;
		final boolean embedded = (getStyle() & SWT.EMBEDDED) != 0;

		if (embedded) {
			embeddedShell = new Shell(parent.getShell(), SWT.MODELESS);
			embeddedShell.setLayout(new FillLayout());
			
			// The Edge implementation is traversing the ESC key from the browser to the shell,
			// which leads to closing the shell. We need to avoid this to be able to reuse the
			// RichTextEditor control in embedded mode.
			embeddedShell.addTraverseListener(e -> {
				if (e.keyCode == SWT.ESC) {
					e.doit = false;
				}
			});
		}

		// remove styles that are not relevant for the browser
		final int browserStyle = style & ~SWT.RESIZE & ~SWT.MIN & ~SWT.EMBEDDED;

		browser = new Browser(!embedded ? this : embeddedShell, browserStyle);
		browser.setJavascriptEnabled(true);

		// init editor configuration
		if (editorConfig == null) {
			this.editorConfig = new RichTextEditorConfiguration();
		}
		else {
			this.editorConfig = editorConfig;
		}
		this.editorConfig.setBrowser(browser);

		// if SWT.RESIZE is set, we update the configuration
		if (resizable) {
			final boolean specifyMin = (getStyle() & SWT.MIN) != 0;
			final int minWidth = specifyMin ? getMinimumWidth() : 0;
			final int minHeight = specifyMin ? getMinimumHeight() : 0;
			this.editorConfig.setResizable(resizable);
			this.editorConfig.setMinSize(minWidth, minHeight);
			this.editorConfig.setResizeDirection("both");
		}

		browser.setUrl(templateURL.toString());

		browser.addProgressListener(new ProgressListener() {

			@Override
			public void completed(final ProgressEvent event) {
				createBrowserFunctions();
				browser.evaluate("initEditor();");

				CKEDITOR_ALT = (Double) browser.evaluate("return getCKEditorALT()");
				CKEDITOR_CTRL = (Double) browser.evaluate("return getCKEditorCTRL()");
				CKEDITOR_SHIFT = (Double) browser.evaluate("return getCKEditorSHIFT()");

				editorLoaded = true;

				if (RichTextEditor.this.editorConfig.isAutoUrlFormattingDisabled()) {
					browser.evaluate("document.execCommand('AutoUrlDetect', false, false);");
				}

				// only add this function for resizable inline editing
				if (resizable && embedded) {
					// register the callback to resize the browser if the
					// ckeditor is resized
					browserFunctions.add(new BrowserFunction(browser, "updateDimensions") {
						@Override
						public Object function(final Object[] arguments) {
							
							int argWidth = ((Double) arguments[0]).intValue();
							int argHeight = ((Double) arguments[1]).intValue();
							
							// width and height + containerEdgeDiffs because the editor is
							// smaller than the browser container on every side
							int newWidth = argWidth + (containerEdgeDiffs != null ? containerEdgeDiffs[0] : 2);
							int newHeight = argHeight + (containerEdgeDiffs != null ? containerEdgeDiffs[1] : 2);
							
							if ("false".equals(autoScaleProperty) || "integer".equals(autoScaleProperty)) {
					        	newWidth = ScalingHelper.getZoomedValue(newWidth, embeddedShell != null ? embeddedShell.getZoom() : 100);
					        	newHeight = ScalingHelper.getZoomedValue(newHeight, embeddedShell != null ? embeddedShell.getZoom() : 100);
							} else {
								try {
									int zoomLevel = Integer.parseInt(autoScaleProperty);
									// first we need to unapply the auto scale zoom, then apply the display zoom to the value 
									// from the browser to get the real base value
									int updateScaledWidth = ScalingHelper.getUnzoomedValue(argWidth, zoomLevel);
									updateScaledWidth = ScalingHelper.getZoomedValue(updateScaledWidth, embeddedShell.getZoom());
									int updateScaledHeight = ScalingHelper.getUnzoomedValue(argHeight, zoomLevel);
									updateScaledHeight = ScalingHelper.getZoomedValue(updateScaledHeight, embeddedShell.getZoom());
									
									newWidth = updateScaledWidth + (containerEdgeDiffs != null ? containerEdgeDiffs[0] : 2);
									newHeight = updateScaledHeight + (containerEdgeDiffs != null ? containerEdgeDiffs[1] : 2);
								} catch (NumberFormatException e) {
									// in case of an invalid value, do nothing
								}
							}

					        Rectangle currentBounds = embeddedShell != null ? embeddedShell.getBounds() : getBounds();
							setInlineContainerBounds(
									currentBounds.x,
									currentBounds.y,
									newWidth,
									newHeight);

							// also repaint the parent control to avoid
							// rendering glitches while resizing
							if (getParent() != null) {
								getParent().redraw();
								getParent().update();
							}

							return super.function(arguments);
						}
					});

					browserFunctions.add(new BrowserFunction(browser, "initialDimensions") {
						@Override
						public Object function(final Object[] arguments) {
							if (embeddedShell != null && containerEdgeDiffs == null) {
								
								autoScaleProperty = ScalingHelper.getAutoScaleProperty(getParent().getDisplay());
								
								int initialWidth = ((Double)arguments[0]).intValue();
								int initialHeight = ((Double)arguments[1]).intValue();

								Rectangle embeddedShellBounds = embeddedShell.getBounds();
								
								int widthDiff = 0;
					        	int heightDiff = 0;

					        	switch (autoScaleProperty) {
									case "false":
									case "integer":
										int shellBaseWidth = ScalingHelper.getUnzoomedValue(embeddedShellBounds.width, embeddedShell.getZoom());
										int shellBaseHeight = ScalingHelper.getUnzoomedValue(embeddedShellBounds.height, embeddedShell.getZoom());

										widthDiff = shellBaseWidth - initialWidth;
							        	heightDiff = shellBaseHeight - initialHeight;
							        	break;
									case "quarter":
									case "exact":
									case "half":
										// in this case the initial dimensions are already zoomed, so we can directly calculate the edge diffs
										widthDiff = embeddedShellBounds.width - initialWidth;
							        	heightDiff = embeddedShellBounds.height - initialHeight;
										break;
									default:
										// none of the others, try to parse the value as integer zoom level
										try {
											int zoomLevel = Integer.parseInt(autoScaleProperty);
											// first we need to unapply the auto scale zoom, then apply the display zoom to the value from the browser
											// to get the real base value for calculating the edge diffs
											int updateScaledWidth = ScalingHelper.getUnzoomedValue(initialWidth, zoomLevel);
											updateScaledWidth = ScalingHelper.getZoomedValue(updateScaledWidth, embeddedShell.getZoom());
											int updateScaledHeight = ScalingHelper.getUnzoomedValue(initialHeight, zoomLevel);
											updateScaledHeight = ScalingHelper.getZoomedValue(updateScaledHeight, embeddedShell.getZoom());
											
											widthDiff = embeddedShellBounds.width - updateScaledWidth;
								        	heightDiff = embeddedShellBounds.height - updateScaledHeight;
										} catch (NumberFormatException e) {
											// in case of an invalid value, do not apply any edge diffs
										}
								};

								containerEdgeDiffs = new int[] { widthDiff, heightDiff };
							}
							
							return super.function(arguments);
						}
					});

					browser.evaluate("enableResizeCallback();");

					if (embedded) {
						browserFunctions.add(new BrowserFunction(browser, "activateShellDragMode") {
							@Override
							public Object function(final Object[] arguments) {
								mouseDragPosition = new Point(((Double) arguments[0]).intValue(), ((Double) arguments[1]).intValue());
								return super.function(arguments);
							};
						});

						browserFunctions.add(new BrowserFunction(browser, "deactivateShellDragMode") {
							@Override
							public Object function(final Object[] arguments) {
								mouseDragPosition = null;
								return super.function(arguments);
							};
						});

						browserFunctions.add(new BrowserFunction(browser, "moveShell") {
							@Override
							public Object function(final Object[] arguments) {
								if (mouseDragPosition != null) {
									final Point cursorLocation = Display.getDefault().getCursorLocation();
									embeddedShell.setLocation(cursorLocation.x - mouseDragPosition.x, cursorLocation.y - mouseDragPosition.y);
								}
								return super.function(arguments);
							};
						});

						browser.evaluate("addMoveAnchor();");
					}
				}

				if (initialValue != null) {
					setText(initialValue);
				}

				if (initialSetFocus) {
					setFocus();
				}
			}

			@Override
			public void changed(final ProgressEvent event) {
			}
		});	
	}

	private void createBrowserFunctions() {
		browserFunctions.add(new ModifyFunction(browser, "textModified"));
		browserFunctions.add(new KeyPressedFunction(browser, "keyPressed"));
		browserFunctions.add(new KeyReleasedFunction(browser, "keyReleased"));
		browserFunctions.add(new FocusInFunction(browser, "focusIn"));
		browserFunctions.add(new FocusOutFunction(browser, "focusOut"));
		browserFunctions.add(new JavaExecutionStartedFunction(browser, "javaExecutionStarted"));
		browserFunctions.add(new JavaExecutionFinishedFunction(browser, "javaExecutionFinished"));
		browserFunctions.add(new BrowserFunction(browser, "customizeToolbar") {
			@Override
			public Object function(final Object[] arguments) {
				RichTextEditor.this.editorConfig.customizeToolbar();
				return super.function(arguments);
			}
		});
		browserFunctions.add(new BrowserFunction(browser, "getAllOptions") {
			@Override
			public Object function(final Object[] arguments) {
				// transform the configuration options map into an Object array
				// necessary as map is not a supported return value
				final Map<String, Object> options = RichTextEditor.this.editorConfig.getAllOptions();
				final Object[] result = new Object[options.size()*2];
				int i = 0;
				for (final Map.Entry<String, Object> entry : options.entrySet()) {
					result[i++] = entry.getKey();
					result[i++] = entry.getValue() != null ? entry.getValue() : "";
				}
				return result;
			}
		});
	}

	@Override
	public void dispose() {
		// dispose the editor configuration
		editorConfig.dispose();
		// dispose the registered BrowserFunctions
		for (final BrowserFunction function : browserFunctions) {
			function.dispose();
		}
		// dispose the Browser
		browser.dispose();

		// dispose the embedded shell if we are in embedded mode
		if (embeddedShell != null && !embeddedShell.isDisposed()) {
			embeddedShell.dispose();
		}
		// call super to ensure the resources of this composite are released
		super.dispose();
	}

	@Override
	public void setVisible(final boolean visible) {
		if (embeddedShell != null && !embeddedShell.isDisposed()) {
			embeddedShell.setVisible(visible);
		}
		super.setVisible(visible);
	}

	/**
	 * @return The text that is currently set in the editing area. Contains HTML tags for
	 *         formatting.
	 */
	public String getText() {
		if (browser != null && !browser.isDisposed()) {
			final Object result = browser.evaluate("return getText()");
			return result != null ? result.toString() : null;
		}
		return null;
	}

	/**
	 * Set text to the editing area. Can contain HTML tags for styling.
	 *
	 * @param text
	 *            The text to set to the editing area.
	 */
	public void setText(final String text) {
		if (editorLoaded) {
			try {
				browser.evaluate("setText('" + escapeToEvaluate(text) + "')");
			} catch (final Exception ignored) {
				// Ignore the exception
			}
		} else {
			initialValue = text;
		}
	}

	/**
	 * Insert text content into the currently selected position in the editor in WYSIWYG mode. The
	 * styles of the selected element will be applied to the inserted text. Spaces around the text
	 * will be left untouched.
	 *
	 * @param text
	 *            Text to be inserted into the editor.
	 */
	public void insertText(final String text) {
		browser.evaluate("insertText('" + escapeToEvaluate(text) + "')");
	}

	/**
	 * Inserts HTML code into the currently selected position in the editor in WYSIWYG mode.
	 *
	 * @param html
	 *            HTML code to be inserted into the editor.
	 */
	public void insertHTML(final String html) {
		browser.evaluate("insertHTML('" + escapeToEvaluate(html) + "')");
	}

	/**
	 * Returns the current selected text without any markup tags.
	 *
	 * @return The current selected text without any markup tags.
	 */
	public String getSelectedText() {
		return "" + browser.evaluate("return getSelectedText();");
	}

	/**
	 * Returns the current selected text containing the markup tags for styling.
	 * <p>
	 * <i>Note: It will not contain the parent tag.</i>
	 * </p>
	 *
	 * @return The current selected text containing any markup styling tags.
	 */
	public String getSelectedHTML() {
		final Object html = browser.evaluate("return getSelectedHTML()");
		return html != null ? html.toString() : "";
	}

	/**
	 * Returns the editable state.
	 *
	 * @return whether or not the receiver is editable
	 *
	 */
	public boolean isEditable() {
		if (editorLoaded) {
			final Object result = browser.evaluate("return isEditable()");
			return result != null ? !Boolean.valueOf(result.toString()) : true;
		} else {
			return initialSetEditable;
		}
	}

	/**
	 * Update the toolbar. Typically used if buttons where added or removed at runtime.
	 */
	public void updateToolbar() {
		editorConfig.customizeToolbar();
		browser.evaluate("updateToolbar();");
	}

	/**
	 * Update the editor. Basically it will destroy and recreate the CKEditor.
	 * Needed to be used if a basic configuration is changed, e.g. the language.
	 *
	 * @since 1.1
	 */
	public void updateEditor() {
		browser.evaluate("updateEditor();");
	}

	/**
	 * Sets the editable state.
	 *
	 * @param editable
	 *            the new editable state
	 */
	public void setEditable(final boolean editable) {
		if (editorLoaded) {
			browser.evaluate("setReadOnly(" + !editable + ")");
		}
		else {
			// initial readOnly mode can only be enabled via configuration option as described in
			// https://ckeditor.com/docs/ckeditor4/latest/features/readonly.html#read-only-mode-on-startup
			initialSetEditable = false;
			editorConfig.setOption("readOnly", true);
		}
	}

	/**
	 * This method returns the {@link RichTextEditorConfiguration} that is used to configure this
	 * {@link RichTextEditor}. It can be used to change some configurations at runtime.
	 * <p>
	 * <b>Note:</b> After configuration values have been changed it is necessary to call
	 * {@link #updateEditor()} so the configurations are applied.
	 * </p>
	 *
	 * @return The {@link RichTextEditorConfiguration} used to configure this
	 *         {@link RichTextEditor}.
	 *
	 * @since 1.1
	 */
	public RichTextEditorConfiguration getEditorConfiguration() {
		return editorConfig;
	}

	/**
	 * Sets the user interface language localization to use. Only the language part of the
	 * {@link Locale} will be used. This method triggers an immediate update of the editor instance.
	 *
	 * @param locale
	 *            The user interface language localization to use.
	 * @since 1.1
	 */
	public void setLanguage(final Locale locale) {
		setLanguage(locale, true);
	}

	/**
	 *
	 * @param locale
	 *            The user interface language localization to use.
	 * @param update
	 *            <code>true</code> if the editor should be updated immediately, <code>false</code>
	 *            if the update should not be executed. In that case {@link #updateEditor()} needs
	 *            to be executed explicitly.
	 * @since 1.1
	 */
	public void setLanguage(final Locale locale, final boolean update) {
		editorConfig.setLanguage(locale);
		if (update) {
			updateEditor();
		}
	}

	/**
	 * Sets the user interface language localization to use. This method triggers an immediate
	 * update of the editor instance.
	 *
	 * @param language
	 *            The user interface language localization to use.
	 * @since 1.1
	 */
	public void setLanguage(final String language) {
		setLanguage(language, true);
	}

	/**
	 *
	 * @param language
	 *            The user interface language localization to use.
	 * @param update
	 *            <code>true</code> if the editor should be updated immediately, <code>false</code>
	 *            if the update should not be executed. In that case {@link #updateEditor()} needs
	 *            to be executed explicitly.
	 * @since 1.1
	 */
	public void setLanguage(final String language, final boolean update) {
		editorConfig.setLanguage(language);
		if (update) {
			updateEditor();
		}
	}

	/**
	 * Adds the given {@link ToolbarButton} to the toolbar of the editor.
	 *
	 * @param button
	 *            The button to add.
	 *
	 * @see RichTextEditorConfiguration#addToolbarButton(ToolbarButton)
	 */
	public void addToolbarButton(final ToolbarButton button) {
		editorConfig.addToolbarButton(button);
	}

	/**
	 * Adds the given {@link ToolbarButton} to the toolbar of the editor. Uses the given
	 * {@link BrowserFunction} as callback for the button.
	 *
	 * @param button
	 *            The button to add.
	 * @param function
	 *            The function to use as callback.
	 *
	 * @see RichTextEditorConfiguration#addToolbarButton(ToolbarButton, BrowserFunction)
	 */
	public void addToolbarButton(final ToolbarButton button, final BrowserFunction function) {
		editorConfig.addToolbarButton(button, function);
	}

	/**
	 * Removes the given {@link ToolbarButton} from the toolbar of the editor.
	 *
	 * @param button
	 *            The button to remove.
	 *
	 * @see RichTextEditorConfiguration#removeToolbarButton(ToolbarButton)
	 */
	public void removeToolbarButton(final ToolbarButton button) {
		editorConfig.removeToolbarButton(button);
	}

	@Override
	public boolean setFocus() {
		if (editorLoaded) {
			browser.evaluate("setFocus();");
			final Object result = browser.evaluate("return hasFocus();");
			return result != null ? Boolean.valueOf(result.toString()) : false;
		}
		else {
			initialSetFocus = true;
			return true;
		}
	}

	@Override
	public boolean forceFocus() {
		return setFocus();
	}

	@Override
	public boolean isFocusControl() {
		if (editorLoaded) {
			final Object result = browser.evaluate("return hasFocus();");
			return result != null ? Boolean.valueOf(result.toString()) : false;
		}
		return false;
	}

	@Override
	public void addFocusListener(final FocusListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		focusListener.add(listener);
	}

	@Override
	public void removeFocusListener(final FocusListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		focusListener.remove(listener);
	}

	/**
	 * Notify the registered {@link FocusListener} that the editor gained focus.
	 *
	 * @param event
	 *            The event to fire.
	 */
	public void notifyFocusGained(final FocusEvent event) {
		checkWidget();
		// do not handle focus events, e.g. in case a Java callback execution is running
		if (event == null || !handleFocusChanges) {
			return;
		}

		if (event.display != null) {
			event.display.asyncExec(() -> doNotifyFocusGained(event));
		}
		else {
			// no display in the event, fire the events synchronously
			doNotifyFocusGained(event);
		}
	}

	private void doNotifyFocusGained(final FocusEvent event) {
		final Object[] listeners = focusListener.getListeners();
		for (final Object listener : listeners) {
			((FocusListener) listener).focusGained(event);
		}
	}

	/**
	 * Notify the registered {@link FocusListener} that the editor lost focus.
	 *
	 * @param event
	 *            The event to fire.
	 */
	public void notifyFocusLost(final FocusEvent event) {
		checkWidget();
		// do not handle focus events, e.g. in case a Java callback execution is running
		if (event == null || !handleFocusChanges) {
			return;
		}

		if (event.display != null) {
			event.display.asyncExec(() -> doNotifyFocusLost(event));
		}
		else {
			// no display in the event, fire the events synchronously
			doNotifyFocusLost(event);
		}
	}

	private void doNotifyFocusLost(final FocusEvent event) {
		final Object[] listeners = focusListener.getListeners();
		for (final Object listener : listeners) {
			((FocusListener) listener).focusLost(event);
		}
	}

	@Override
	public void setBounds(final Rectangle rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	@Override
	public void setBounds(final int x, final int y, final int width, final int height) {
		int newX = x;
		int newY = y;
		int newWidth = width;
		int newHeight = height;
		if (resizedBounds != null) {
			newX = resizedBounds.x;
			newY = resizedBounds.y;
			newWidth = resizedBounds.width;
			newHeight = resizedBounds.height;
		}
		else if ((getStyle() & SWT.EMBEDDED) != 0) {
			// ensure min size when opened inline
			newHeight = Math.max(height, getMinimumHeight());
			newWidth = Math.max(width, getMinimumWidth());
		}

		if (embeddedShell != null) {
			final Point shellLocation = super.toDisplay(newX, newY);
			embeddedShell.setBounds(shellLocation.x, shellLocation.y, newWidth, newHeight);
			embeddedShell.setVisible(true);
		}
		else {
			super.setBounds(new Rectangle(newX, newY, newWidth, newHeight));
		}
	}

	/**
	 * Used in embedded mode to support manual resizing of the editor. Executed via callback on
	 * ckeditor resize.
	 *
	 * @param x
	 *            the new x coordinate for the receiver
	 * @param y
	 *            the new y coordinate for the receiver
	 * @param width
	 *            the new width for the receiver
	 * @param height
	 *            the new height for the receiver
	 */
	void setInlineContainerBounds(final int x, final int y, int width, int height) {
		resizedBounds = new Rectangle(x, y, width, height);
		if (embeddedShell != null) {
			final Point shellLocation = embeddedShell.getLocation();
			embeddedShell.setBounds(
					shellLocation.x,
					shellLocation.y,
					width,
					height);
		}
		else {
			super.setBounds(x, y, width, height);
		}
	}

	/**
	 * Returns the minimum height that should be used for initially open the editor in embedded
	 * mode. It is also used to specify the resize minimum height if the editor was created using
	 * the style bit {@link SWT#MIN}. Using the default {@link RichTextEditorConfiguration} this is
	 * 150 for the toolbar and 50 for showing one row in the editor area.
	 *
	 * @return The minimum height to use for initially open the editor in embedded mode and for
	 *         editor resize minimum in case the editor was created with {@link SWT#MIN}
	 */
	protected int getMinimumHeight() {
		String autoScaleProperty = ScalingHelper.getAutoScaleProperty(getParent().getDisplay());
		
		if ("false".equals(autoScaleProperty) || "integer".equals(autoScaleProperty)) {
			// the browser returns the dimensions according to the display scaling and is not aware of the SWT auto scaling
			// in case SWT auto scaling is disabled or set to integer, we need to manually increase the minimum dimensions
			// according to the display scaling to ensure the shell is large enough for the browser content
			return ScalingHelper.getZoomedValue(230,  getShell().getZoom());
		} else {
			try {
				int zoomLevel = Integer.parseInt(autoScaleProperty);
				if (zoomLevel < getShell().getZoom()) {
					return ScalingHelper.getZoomedValue(230,  getShell().getZoom());
				}
			} catch (NumberFormatException e) {
				// in case of an invalid value, do nothing
			}
			
		}
		return 230;
	}

	/**
	 * Returns the minimum width that should be used for initially open the editor in embedded mode.
	 * It is also used to specify the resize minimum width if the editor was created using the style
	 * bit {@link SWT#MIN}. Using the default {@link RichTextEditorConfiguration} this is 370 for
	 * showing the default options in three lines of the toolbar.
	 *
	 * @return The minimum width to use for initially open the editor in embedded mode and for
	 *         editor resize minimum in case the editor was created with {@link SWT#MIN}
	 */
	protected int getMinimumWidth() {
		String autoScaleProperty = ScalingHelper.getAutoScaleProperty(getParent().getDisplay());
		
		if ("false".equals(autoScaleProperty) || "integer".equals(autoScaleProperty)) {
			// the browser returns the dimensions according to the display scaling and is not aware of the SWT auto scaling
			// in case SWT auto scaling is disabled or set to integer, we need to manually increase the minimum dimensions
			// according to the display scaling to ensure the shell is large enough for the browser content
			return ScalingHelper.getZoomedValue(370,  getShell().getZoom());
		} else {
			try {
				int zoomLevel = Integer.parseInt(autoScaleProperty);
				if (zoomLevel < getShell().getZoom()) {
					return ScalingHelper.getZoomedValue(370,  getShell().getZoom());
				}
			} catch (NumberFormatException e) {
				// in case of an invalid value, do nothing
			}
			
		}
		return 370;
	}

	/**
	 * Executes the specified script in the internal {@link Browser}. Can be used to execute
	 * Javascript directly in the browser from a listener if necessary.
	 *
	 * @param script
	 *            the script with javascript commands
	 * @return <code>true</code> if the operation was successful and <code>false</code> otherwise
	 * @see Browser#execute(String)
	 */
	public boolean executeJavascript(final String script) {
		return browser.execute(script);
	}

	/**
	 * Evaluates the specified script in the internal {@link Browser} and returns the result. Can be
	 * used to evaluate Javascript directly in the browser from a listener if necessary.
	 *
	 * @param script
	 *            the script with javascript commands
	 * @return the return value, if any, of executing the script
	 * @see Browser#evaluate(String)
	 */
	public Object evaluateJavascript(final String script) {
		return browser.evaluate(script);
	}

	/**
	 * @return <code>true</code> if focus changes are handled, <code>false</code> if not
	 */
	public boolean isHandleFocusChanges() {
		return handleFocusChanges;
	}

	/**
	 * Configure whether focus changes should be handled or not. A typical use case for disabling
	 * the focus handling is for example to open a dialog from a Java callback via custom toolbar
	 * button.
	 *
	 * @param handleFocusChanges
	 *            <code>true</code> if focus changes should be handled, <code>false</code> if not
	 */
	public void setHandleFocusChanges(final boolean handleFocusChanges) {
		this.handleFocusChanges = handleFocusChanges;
	}

	@Override
	public void addKeyListener(final KeyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		keyListener.add(listener);
	}

	@Override
	public void removeKeyListener(final KeyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		keyListener.remove(listener);
	}

	/**
	 * Notify the registered {@link KeyListener} that a key was pressed.
	 *
	 * @param event
	 *            The event to fire.
	 */
	public void notifyKeyPressed(final KeyEvent event) {
		checkWidget();
		if (event == null) {
			return;
		}

		if (event.display != null) {
			event.display.asyncExec(() -> doNotifyKeyPressed(event));
		}
		else {
			// no display in the event, fire the events synchronously
			doNotifyKeyPressed(event);
		}
	}

	private void doNotifyKeyPressed(final KeyEvent event) {
		final Object[] listeners = keyListener.getListeners();
		for (final Object listener : listeners) {
			((KeyListener) listener).keyPressed(event);
		}
	}

	/**
	 * Notify the registered {@link KeyListener} that a key was released.
	 *
	 * @param event
	 *            The event to fire.
	 */
	public void notifyKeyReleased(final KeyEvent event) {
		checkWidget();
		if (event == null) {
			return;
		}

		if (event.display != null) {
			event.display.asyncExec(() -> doNotifyKeyReleased(event));
		}
		else {
			// no display in the event, fire the events synchronously
			doNotifyKeyReleased(event);
		}
	}

	private void doNotifyKeyReleased(final KeyEvent event) {
		final Object[] listeners = keyListener.getListeners();
		for (final Object listener : listeners) {
			((KeyListener) listener).keyReleased(event);
		}
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the receiver's
	 * text is modified, by sending it one of the messages defined in the
	 * <code>ModifyListener</code> interface.
	 *
	 * @param listener
	 *            the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 *
	 * @see ModifyListener
	 * @see #removeModifyListener
	 */
	public void addModifyListener(final ModifyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		modifyListener.add(listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the
	 * receiver's text is modified.
	 *
	 * @param listener
	 *            the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 *
	 * @see ModifyListener
	 * @see #addModifyListener
	 */
	public void removeModifyListener(final ModifyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		modifyListener.remove(listener);
	}

	/**
	 * Notifies all of the receiver's listeners when the receiver's text is modified.
	 *
	 * @param eventType
	 *            the type of event which has occurred
	 * @param event
	 *            the event data
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 *
	 * @see #addModifyListener(ModifyListener)
	 * @see #removeModifyListener(ModifyListener)
	 */
	public void notifyModifyListeners(final ModifyEvent event) {
		checkWidget();
		if (event == null) {
			return;
		}

		if (event.display != null) {
			event.display.asyncExec(() -> doNotifyModifyText(event));
		}
		else {
			// no display in the event, fire the events synchronously
			doNotifyModifyText(event);
		}
	}

	private void doNotifyModifyText(final ModifyEvent event) {
		final Object[] listeners = modifyListener.getListeners();
		for (final Object listener : listeners) {
			((ModifyListener) listener).modifyText(event);
		}
	}

	/**
	 * Creates a SWT {@link KeyEvent} out of the given informations.
	 *
	 * @param keyCode
	 *            The keyCode sent by ckeditor.
	 * @param modifier
	 *            The modifier value sent by ckeditor.
	 * @return The {@link KeyEvent} containing the tranformed key event information.
	 */
	private KeyEvent createKeyEvent(final Double keyCode, final Double modifier) {
		final Event event = new Event();
		event.display = getDisplay();
		event.widget = this;
		event.keyCode = keyCode.intValue();

		final Double modifierOnly = modifier - keyCode;
		if (modifierOnly != 0) {
			if (modifierOnly.equals(CKEDITOR_ALT + CKEDITOR_CTRL + CKEDITOR_SHIFT)) {
				event.stateMask = SWT.MOD3 | SWT.MOD1 | SWT.MOD2;
			} else if (modifierOnly.equals(CKEDITOR_ALT + CKEDITOR_CTRL)) {
				event.stateMask = SWT.MOD3 | SWT.MOD1;
			} else if (modifierOnly.equals(CKEDITOR_ALT + CKEDITOR_SHIFT)) {
				event.stateMask = SWT.MOD3 | SWT.MOD2;
			} else if (modifierOnly.equals(CKEDITOR_CTRL + CKEDITOR_SHIFT)) {
				event.stateMask = SWT.MOD1 | SWT.MOD2;
			} else if (modifierOnly.equals(CKEDITOR_ALT)) {
				event.stateMask = SWT.MOD3;
			} else if (modifierOnly.equals(CKEDITOR_CTRL)) {
				event.stateMask = SWT.MOD1;
			} else if (modifierOnly.equals(CKEDITOR_SHIFT)) {
				event.stateMask = SWT.MOD2;
			}
		}

		// transform function keys
		switch (event.keyCode) {
			case 33:
				event.keyCode = SWT.PAGE_UP;
				break;
			case 34:
				event.keyCode = SWT.PAGE_DOWN;
				break;
			case 35:
				event.keyCode = SWT.END;
				break;
			case 36:
				event.keyCode = SWT.HOME;
				break;
			case 37:
				event.keyCode = SWT.ARROW_LEFT;
				break;
			case 38:
				event.keyCode = SWT.ARROW_UP;
				break;
			case 39:
				event.keyCode = SWT.ARROW_RIGHT;
				break;
			case 40:
				event.keyCode = SWT.ARROW_DOWN;
				break;
			case 45:
				event.keyCode = SWT.INSERT;
				break;
			case 46:
				event.keyCode = SWT.DEL;
				break;
			case 96:
				event.keyCode = SWT.KEYPAD_0;
				event.character = '0';
				break;
			case 97:
				event.keyCode = SWT.KEYPAD_1;
				event.character = '1';
				break;
			case 98:
				event.keyCode = SWT.KEYPAD_2;
				event.character = '2';
				break;
			case 99:
				event.keyCode = SWT.KEYPAD_3;
				event.character = '3';
				break;
			case 100:
				event.keyCode = SWT.KEYPAD_4;
				event.character = '4';
				break;
			case 101:
				event.keyCode = SWT.KEYPAD_5;
				event.character = '5';
				break;
			case 102:
				event.keyCode = SWT.KEYPAD_6;
				event.character = '6';
				break;
			case 103:
				event.keyCode = SWT.KEYPAD_7;
				event.character = '7';
				break;
			case 104:
				event.keyCode = SWT.KEYPAD_8;
				event.character = '8';
				break;
			case 105:
				event.keyCode = SWT.KEYPAD_9;
				event.character = '9';
				break;
			case 106:
				event.keyCode = SWT.KEYPAD_MULTIPLY;
				event.character = '*';
				break;
			case 107:
				event.keyCode = SWT.KEYPAD_ADD;
				event.character = '+';
				break;
			case 109:
				event.keyCode = SWT.KEYPAD_SUBTRACT;
				event.character = '-';
				break;
			case 110:
				event.keyCode = SWT.KEYPAD_DECIMAL;
				event.character = ',';
				break;
			case 111:
				event.keyCode = SWT.KEYPAD_DIVIDE;
				event.character = '/';
				break;
			case 112:
				event.keyCode = SWT.F1;
				break;
			case 113:
				event.keyCode = SWT.F2;
				break;
			case 114:
				event.keyCode = SWT.F3;
				break;
			case 115:
				event.keyCode = SWT.F4;
				break;
			case 116:
				event.keyCode = SWT.F5;
				break;
			case 117:
				event.keyCode = SWT.F6;
				break;
			case 118:
				event.keyCode = SWT.F7;
				break;
			case 119:
				event.keyCode = SWT.F8;
				break;
			case 120:
				event.keyCode = SWT.F9;
				break;
			case 121:
				event.keyCode = SWT.F10;
				break;
			case 122:
				event.keyCode = SWT.F11;
				break;
			case 123:
				event.keyCode = SWT.F12;
				break;
		}

		// character
		final String keyCharString = SWTKeySupport.getKeyFormatterForPlatform().format(event.keyCode);
		if (keyCharString.length() == 1) {
			final char keyChar = keyCharString.charAt(0);
			if (Character.isUpperCase(keyChar) && Character.isAlphabetic(keyChar)) {
				if (!((event.stateMask & SWT.MOD2) == SWT.MOD2)) {
					event.character = Character.toLowerCase(keyChar);
				} else {
					event.character = keyChar;
				}
			} else if (Character.isDigit(keyChar) && !((event.stateMask & SWT.MOD2) == SWT.MOD2) && !((event.stateMask & SWT.MOD3) == SWT.MOD3)) {
				event.character = keyChar;
			}
		}

		return new KeyEvent(event);
	}

	/**
	 * Add a {@link JavaCallbackListener} that is triggered on executing a Java callback via custom
	 * {@link ToolbarButton}.
	 *
	 * @param listener
	 *            The {@link JavaCallbackListener} to add.
	 */
	public void addJavaCallbackListener(final JavaCallbackListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		javaCallbackListener.add(listener);
	}

	/**
	 * Remove a {@link JavaCallbackListener} that is triggered on executing a Java callback via
	 * custom {@link ToolbarButton}.
	 *
	 * @param listener
	 *            The {@link JavaCallbackListener} to remove.
	 */
	public void removeJavaCallbackListener(final JavaCallbackListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		javaCallbackListener.remove(listener);
	}

	private void doNotifyJavaExecutionStarted() {
		final Object[] listeners = javaCallbackListener.getListeners();
		for (final Object listener : listeners) {
			((JavaCallbackListener) listener).javaExecutionStarted();
		}
	}

	private void doNotifyJavaExecutionFinished() {
		final Object[] listeners = javaCallbackListener.getListeners();
		for (final Object listener : listeners) {
			((JavaCallbackListener) listener).javaExecutionFinished();
		}
	}

	private FocusEvent createFocusEvent() {
		final Event event = new Event();
		event.display = getDisplay();
		event.widget = this;
		return new FocusEvent(event);
	}

	private ModifyEvent createModifyEvent() {
		final Event event = new Event();
		event.display = getDisplay();
		event.widget = this;
		return new ModifyEvent(event);
	}

	/**
	 * Callback function that is called via Javascript if a change event occurs in the editor part.
	 * Is registered for the Javascript function name <i>textModified</i>.
	 */
	class ModifyFunction extends BrowserFunction {

		public ModifyFunction(final Browser browser, final String name) {
			super(browser, name);
		}

		@Override
		public Object function(final Object[] arguments) {
			notifyModifyListeners(createModifyEvent());
			return super.function(arguments);
		}
	}

	/**
	 * Callback function that is called via Javascript if a keydown event occurs in the editor part.
	 * Is registered for the Javascript function name <i>keyPressed</i>.
	 */
	class KeyPressedFunction extends BrowserFunction {

		public KeyPressedFunction(final Browser browser, final String name) {
			super(browser, name);
		}

		@Override
		public Object function(final Object[] arguments) {
			final Double keyCode = (Double) arguments[0];
			final Double modifier = (Double) arguments[1];
			notifyKeyPressed(createKeyEvent(keyCode, modifier));
			return super.function(arguments);
		}
	}

	/**
	 * Callback function that is called via Javascript if a keyup event occurs in the editor part.
	 * Is registered for the Javascript function name <i>keyReleased</i>.
	 */
	class KeyReleasedFunction extends BrowserFunction {

		public KeyReleasedFunction(final Browser browser, final String name) {
			super(browser, name);
		}

		@Override
		public Object function(final Object[] arguments) {
			final Double keyCode = (Double) arguments[0];
			final Double modifier = (Double) arguments[1];
			notifyKeyReleased(createKeyEvent(keyCode, modifier));
			return super.function(arguments);
		}
	}

	/**
	 * Callback function that is called via Javascript if the editor gains focus. Is registered for
	 * the Javascript function name <i>focusIn</i>.
	 */
	class FocusInFunction extends BrowserFunction {

		public FocusInFunction(final Browser browser, final String name) {
			super(browser, name);
		}

		@Override
		public Object function(final Object[] arguments) {
			notifyFocusGained(createFocusEvent());
			return super.function(arguments);
		}
	}

	/**
	 * Callback function that is called via Javascript on blur in the editor part. Is registered for
	 * the Javascript function name <i>focusOut</i>.
	 */
	class FocusOutFunction extends BrowserFunction {

		public FocusOutFunction(final Browser browser, final String name) {
			super(browser, name);
		}

		@Override
		public Object function(final Object[] arguments) {
			notifyFocusLost(createFocusEvent());
			return super.function(arguments);
		}
	}

	/**
	 * Callback function that is called via Javascript before a java callback is triggered via
	 * custom toolbar button. Is registered for the Javascript function name
	 * <i>javaExecutionStarted</i>.
	 */
	class JavaExecutionStartedFunction extends BrowserFunction {

		public JavaExecutionStartedFunction(final Browser browser, final String name) {
			super(browser, name);
		}

		@Override
		public Object function(final Object[] arguments) {
			doNotifyJavaExecutionStarted();
			return super.function(arguments);
		}
	}

	/**
	 * Callback function that is called via Javascript after a java callback is triggered via custom
	 * toolbar button. Is registered for the Javascript function name <i>javaExecutionFinished</i>.
	 */
	class JavaExecutionFinishedFunction extends BrowserFunction {

		public JavaExecutionFinishedFunction(final Browser browser, final String name) {
			super(browser, name);
		}

		@Override
		public Object function(final Object[] arguments) {
			doNotifyJavaExecutionFinished();
			return super.function(arguments);
		}
	}

	private static void locateTemplateURL() {
		templateURL = RichTextEditor.class.getResource("resources/template.html");

		// if we are in an OSGi context, we need to convert the bundle URL to a file URL
		final Bundle bundle = FrameworkUtil.getBundle(RichTextEditor.class);
		if (bundle != null) {
			try {
				templateURL = FileLocator.toFileURL(templateURL);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else if (templateURL.toString().startsWith("jar")) {
			BusyIndicator.showWhile(Display.getDefault(), () -> templateURL = ResourceHelper.getRichTextResource("template.html"));
		}
	}

	/**
	 * browser.evaluate evaluates code from a String parameter.
	 * If a function is called with a javascript-string parameter its value needs to be escaped:
	 * escape sequences need to be handled (turn \ to \\)
	 * Single quotes need to be escaped (' to \')
	 * newlines can not be inside a javascript string literal: turn CR and LF to \r and \n respectivly
	 *
	 * @param text
	 *            Text to be escaped.
	 */
	private static String escapeToEvaluate(final String text) {
		return text.replace("\\", "\\\\").replace("\'", "\\\'").replace("\n", "\\n").replace("\r", "\\r");
	}
}
