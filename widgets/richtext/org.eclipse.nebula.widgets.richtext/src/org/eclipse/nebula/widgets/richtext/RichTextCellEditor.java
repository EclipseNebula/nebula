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

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.widgets.richtext.toolbar.JavaCallbackListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that manages HTML entry fields. It uses the {@link RichTextEditor} as editing
 * control.
 *
 * <p>
 * It creates the {@link RichTextEditor} instance always using the style bit {@link SWT#EMBEDDED} to
 * ensure the editor is opened with a minimum size. Otherwise the editing framework will set the
 * bounds to the size of the current cell, which makes the editor unusable.
 * </p>
 * <p>
 * Additionally it supports the style bit {@link SWT#RESIZE} which is set by default if no
 * specialized style is set. This enables resizing support of the embedded inline
 * {@link RichTextEditor}. By additionally specifying the {@link SWT#MIN} style bit, it is not
 * possible for a user to resize the editor below the specified minimum size via
 * </p>
 * <p>
 * As the {@link RichTextEditor} uses a {@link Browser} internally, it is also possible to specify
 * the browser type via style bit.
 * </p>
 */
public class RichTextCellEditor extends CellEditor {

	/**
	 * The rich text editor control, initially <code>null</code>.
	 */
	protected RichTextEditor editor;

	/**
	 * The {@link RichTextEditorConfiguration} that should be used for creating the inline rich text editor
	 * control. If <code>null</code> the default {@link RichTextEditorConfiguration} will be used.
	 */
	protected RichTextEditorConfiguration editorConfiguration;

	private ModifyListener modifyListener;
	
	private Composite parent;

	/**
	 * Create a resizable {@link RichTextCellEditor} with the default {@link RichTextEditorConfiguration}.
	 *
	 * @param parent
	 *            The parent composite.
	 */
	public RichTextCellEditor(Composite parent) {
		this(parent, (RichTextEditorConfiguration) null, SWT.RESIZE);
	}

	/**
	 * Create a resizable {@link RichTextCellEditor} with the given
	 * {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration}.
	 *
	 * @param parent
	 *            The parent composite.
	 * @param toolbarConfiguration
	 *            The {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration} that
	 *            should be used for creating the {@link RichTextEditor}.
	 * @deprecated Use a constructor with {@link RichTextEditorConfiguration} parameter
	 */
	@Deprecated
	public RichTextCellEditor(Composite parent, org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration toolbarConfiguration) {
		this(parent, toolbarConfiguration, SWT.RESIZE);
	}

	/**
	 * Create a resizable {@link RichTextCellEditor} with the default {@link RichTextEditorConfiguration}
	 * and the given style bits.
	 *
	 * @param parent
	 *            The parent composite.
	 * @param style
	 *            The style bits to use.
	 */
	public RichTextCellEditor(Composite parent, int style) {
		this(parent, (RichTextEditorConfiguration) null, style);
	}

	/**
	 * Create a resizable {@link RichTextCellEditor} with the given
	 * {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration} and the given style
	 * bits.
	 *
	 * @param parent
	 *            The parent composite.
	 * @param toolbarConfiguration
	 *            The {@link org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration} that
	 *            should be used for creating the {@link RichTextEditor}.
	 * @param style
	 *            The style bits to use.
	 * @deprecated Use a constructor with {@link RichTextEditorConfiguration} parameter
	 */
	@Deprecated
	public RichTextCellEditor(Composite parent, org.eclipse.nebula.widgets.richtext.toolbar.ToolbarConfiguration toolbarConfiguration, int style) {
		this(parent, new RichTextEditorConfiguration(toolbarConfiguration), style);
	}

	/**
	 * Create a resizable {@link RichTextCellEditor} with the given {@link RichTextEditorConfiguration} and
	 * the given style bits.
	 *
	 * @param parent
	 *            The parent composite.
	 * @param editorConfiguration
	 *            The {@link RichTextEditorConfiguration} that should be used for creating the
	 *            {@link RichTextEditor}.
	 * @param style
	 *            The style bits to use.
	 */
	public RichTextCellEditor(Composite parent, RichTextEditorConfiguration editorConfiguration, int style) {
		super(parent, style | SWT.EMBEDDED);
		
		this.editorConfiguration = editorConfiguration;
		
		this.parent = parent;

		// call super#create(Composite) now because we override it locally empty
		// to be able to set member variables like the ToolbarConfiguration.
		super.create(parent);
	}

	@Override
	public void create(Composite parent) {
		// We override this method to be empty in here because it is called by
		// the super constructor.
		// Therefore we are not able to set member variables, e.g. the
		// RichTextEditorConfiguration before the RichTextEditor control is created.
	}

	@Override
	protected Control createControl(Composite parent) {
		this.editor = new RichTextEditor(parent, this.editorConfiguration, getStyle()) {
			@Override
			protected int getMinimumHeight() {
				return getMinimumDimension().y;
			}

			@Override
			protected int getMinimumWidth() {
				return getMinimumDimension().x;
			}
		};

		this.editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					fireCancelEditor();
				}

				// apply the value on key combination CTRL + RETURN
				// because RETURN will add a new line to the editor
				if (e.keyCode == SWT.CR && e.stateMask == SWT.MOD1) {
					fireApplyEditorValue();
				}

				// as a result of processing the above call, clients may have
				// disposed this cell editor
				if ((getControl() == null) || getControl().isDisposed()) {
					return;
				}
			}
		});

		this.editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				RichTextCellEditor.this.focusLost();
			}
		});

		this.editor.addModifyListener(getModifyListener());

		this.editor.addJavaCallbackListener(new JavaCallbackListener() {

			@Override
			public void javaExecutionStarted() {
				editor.setHandleFocusChanges(false);
			}

			@Override
			public void javaExecutionFinished() {
				editor.setHandleFocusChanges(true);
			}
		});

		return this.editor;
	}

	/**
	 * @return The minimum dimension used for the rich text editor control.
	 */
	protected Point getMinimumDimension() {
		String autoScaleProperty = ScalingHelper.getAutoScaleProperty(this.parent.getDisplay());
		
		if ("false".equals(autoScaleProperty) || "integer".equals(autoScaleProperty)) {
			// the browser returns the dimensions according to the display scaling and is not aware of the SWT auto scaling
			// in case SWT auto scaling is disabled or set to integer, we need to manually increase the minimum dimensions
			// according to the display scaling to ensure the shell is large enough for the browser content
			return new Point(
					ScalingHelper.getZoomedValue(370,  parent.getShell().getZoom()), 
					ScalingHelper.getZoomedValue(230,  parent.getShell().getZoom()));
		} else {
			try {
				int zoomLevel = Integer.parseInt(autoScaleProperty);
				if (zoomLevel < parent.getShell().getZoom()) {
					return new Point(
							ScalingHelper.getZoomedValue(370,  parent.getShell().getZoom()), 
							ScalingHelper.getZoomedValue(230,  parent.getShell().getZoom()));
				}
			} catch (NumberFormatException e) {
				// in case of an invalid value, do nothing
			}
			
		}
		return new Point(370, 230);
	}

	@Override
	protected Object doGetValue() {
		return this.editor.getText();
	}

	@Override
	protected void doSetFocus() {
		if (this.editor != null && !this.editor.isDisposed()) {
			this.editor.setFocus();
		}
	}

	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(this.editor != null);
		this.editor.removeModifyListener(getModifyListener());
		this.editor.setText(value != null ? (String) value : "");
		this.editor.addModifyListener(getModifyListener());
	}

	/**
	 * Processes a modify event that occurred in this rich text cell editor. This framework method
	 * performs validation and sets the error message accordingly, and then reports a change via
	 * <code>fireEditorValueChanged</code>. Subclasses should call this method at appropriate times.
	 * Subclasses may extend or reimplement.
	 *
	 * @param e
	 *            the SWT modify event
	 *
	 * @see TextCellEditor
	 */
	protected void editOccured(ModifyEvent e) {
		String value = this.editor.getText();
		if (value == null) {
			value = "";//$NON-NLS-1$
		}
		Object typedValue = value;
		boolean oldValidState = isValueValid();
		boolean newValidState = isCorrect(typedValue);
		if (!newValidState) {
			// try to insert the current value into the error message.
			setErrorMessage(MessageFormat.format(getErrorMessage(),
					new Object[] { value }));
		}
		valueChanged(oldValidState, newValidState);
	}

	/**
	 * Return the modify listener.
	 *
	 * @see TextCellEditor
	 */
	private ModifyListener getModifyListener() {
		if (modifyListener == null) {
			modifyListener = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					editOccured(e);
				}
			};
		}
		return modifyListener;
	}

	@Override
	protected void focusLost() {
		if (this.editor != null && this.editor.isHandleFocusChanges()) {
			super.focusLost();
		}
	}

	/**
	 * Return the created {@link RichTextEditor} control.
	 *
	 * @return The {@link RichTextEditor} control, or <code>null</code> if this cell editor has no
	 *         control.
	 */
	public RichTextEditor getRichTextEditor() {
		return (RichTextEditor) getControl();
	}
}
