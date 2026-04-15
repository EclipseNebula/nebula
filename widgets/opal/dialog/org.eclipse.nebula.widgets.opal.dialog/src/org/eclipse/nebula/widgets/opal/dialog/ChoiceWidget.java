/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - Initial
 * implementation and API
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Instance of this class are composite that represents a choice like in Windows
 * Vista and Seven. It is composed of a green arrow, instruction and text
 */
public class ChoiceWidget extends Composite {
	private Image oldImage;

	private ChoiceItem choiceItem;

	private Label image;
	private Label instruction;
	private Label text;

	private final List<SelectionListener> selectionListeners;

	private boolean selection;
	private boolean insideComposite;
	private boolean insideImage;
	private boolean insideText;
	private boolean insideInstruction;

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must be
	 * built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
	 * constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot
	 *            be null)
	 * @param style the style of widget to construct
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                </ul>
	 *
	 * @see Composite#Composite(Composite, int)
	 * @see SWT#NO_BACKGROUND
	 * @see SWT#NO_FOCUS
	 * @see SWT#NO_MERGE_PAINTS
	 * @see SWT#NO_REDRAW_RESIZE
	 * @see SWT#NO_RADIO_GROUP
	 * @see SWT#EMBEDDED
	 * @see SWT#DOUBLE_BUFFERED
	 * @see Widget#getStyle
	 */
	public ChoiceWidget(final Composite parent, final int style) {
		super(parent, style);

		setBackgroundMode(SWT.INHERIT_DEFAULT);
		setLayout(new GridLayout(2, false));

		buildGreenArrow();
		buildInstruction();
		buildText();
		addMouseListeners();

		selectionListeners = new ArrayList<SelectionListener>();
		addListener(SWT.Resize, event -> {
			drawComposite();
		});

	}

	/**
	 * Build the green arrow
	 */
	private void buildGreenArrow() {
		final Image greenArrow = SWTGraphicUtil.createImageFromFile("images/arrowGreenRight.png");
		image = new Label(this, SWT.NONE);
		image.setImage(greenArrow);
		image.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, false, false, 1, 2));
		SWTGraphicUtil.addDisposer(this, greenArrow);
	}

	/**
	 * Build the instruction
	 */
	private void buildInstruction() {
		final Color color = new Color(35, 107, 178);
		SWTGraphicUtil.addDisposer(this, color);

		instruction = new Label(this, SWT.NONE);
		instruction.setForeground(color);
		instruction.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
	}

	/**
	 * Build the panel
	 */
	private void buildText() {
		text = new Label(this, SWT.NONE);
		text.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		text.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, true));
	}

	/**
	 * Add mouse listeners
	 */
	private void addMouseListeners() {
		final Listener mouseEnterListener = event -> {

			if (event.widget.equals(ChoiceWidget.this)) {
				insideComposite = true;
			}

			if (event.widget.equals(image)) {
				insideImage = true;
			}
			if (event.widget.equals(text)) {
				insideText = true;
			}
			if (event.widget.equals(instruction)) {
				insideInstruction = true;
			}

			drawComposite();
		};

		final Listener mouseExitListener = event -> {
			if (event.widget.equals(ChoiceWidget.this)) {
				insideComposite = false;
			}

			if (event.widget.equals(image)) {
				insideImage = false;
			}
			if (event.widget.equals(text)) {
				insideText = false;
			}
			if (event.widget.equals(instruction)) {
				insideInstruction = false;
			}
			drawComposite();
		};

		final Listener mouseClickListener = event -> {
			for (final SelectionListener selectionListener : selectionListeners) {
				selectionListener.widgetSelected(null);
			}
		};

		addListener(SWT.MouseEnter, mouseEnterListener);
		image.addListener(SWT.MouseEnter, mouseEnterListener);
		text.addListener(SWT.MouseEnter, mouseEnterListener);
		instruction.addListener(SWT.MouseEnter, mouseEnterListener);

		addListener(SWT.MouseExit, mouseExitListener);
		image.addListener(SWT.MouseExit, mouseExitListener);
		text.addListener(SWT.MouseExit, mouseExitListener);
		instruction.addListener(SWT.MouseExit, mouseExitListener);

		addListener(SWT.MouseUp, mouseClickListener);
		image.addListener(SWT.MouseUp, mouseClickListener);
		text.addListener(SWT.MouseUp, mouseClickListener);
		instruction.addListener(SWT.MouseUp, mouseClickListener);
	}

	/**
	 * Draw the composite
	 */
	private void drawComposite() {

		final Rectangle rect = getClientArea();
		final Image newImage = new Image(getDisplay(), Math.max(1, rect.width), Math.max(1, rect.height));

		final GC gc = new GC(newImage);

		final boolean inside = insideComposite || insideImage || insideInstruction || insideText;

		if (!inside && !selection) {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.drawRectangle(rect.x, rect.y, rect.width, rect.height);
		} else {
			// The mouse is over OR the item is selected
			final Color gradientColor = inside ? new Color(220, 231, 243) : new Color(241, 241, 241);
			final Color borderColor = inside ? new Color(35, 107, 178) : new Color(192, 192, 192);

			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.setBackground(gradientColor);
			gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true);

			gc.setForeground(borderColor);
			gc.drawRoundRectangle(rect.x, rect.y, rect.width - 1, rect.height - 1, 2, 2);

			gradientColor.dispose();
			borderColor.dispose();
		}
		gc.dispose();

		setBackgroundImage(newImage);
		if (oldImage != null) {
			oldImage.dispose();
		}
		oldImage = newImage;
	}

	/**
	 * @return the current choice item
	 */
	public ChoiceItem getChoiceItem() {
		return choiceItem;
	}

	/**
	 * @param choiceItem the choiceItem to set
	 */
	public void setChoiceItem(final ChoiceItem choiceItem) {
		this.choiceItem = choiceItem;
		instruction.setText(choiceItem.getInstruction());
		text.setText(choiceItem.getText());
	}

	/**
	 * Add a selection listener to this widget
	 *
	 * @param listener listener to add
	 */
	public void addSelectionListener(final SelectionListener listener) {
		selectionListeners.add(listener);
	}

	/**
	 * Remove a selection listener
	 *
	 * @param listener listener to remove
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		selectionListeners.remove(listener);
	}

	public void setSelection(final boolean selection) {
		this.selection = selection;
	}

}
