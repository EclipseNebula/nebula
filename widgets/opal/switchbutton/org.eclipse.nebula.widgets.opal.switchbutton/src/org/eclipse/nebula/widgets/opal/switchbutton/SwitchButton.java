/*******************************************************************************
 * Copyright (c) 2011-2021 Laurent CARON
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.switchbutton;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.nebula.widgets.opal.commons.SelectionListenerUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Instances of this class are simple switch button.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 */
public class SwitchButton extends Canvas {

	/**
	 * Selection
	 */
	private boolean selection;

	/**
	 * Text displayed for the selected value (default = "On")
	 */
	private String textForSelect;

	/**
	 * Text displayed for the unselected value (default = "Off")
	 */
	private String textForUnselect;

	/**
	 * Text corresponding to the button (default is "")
	 */
	private String text;

	/**
	 * If true, display round rectangles instead of rectangles (default value is
	 * true)
	 */
	private boolean round;

	/**
	 * if not null, displays a rectangle (or a round rectangle) around the whole
	 * widget. Default value is null.
	 */
	private Color borderColor;

	/**
	 * if not null, displays a glow effect when the mouse is over the widget.
	 * Default value is null.
	 */
	private Color focusColor;

	/**
	 * Colors when the button is selected
	 */
	private Color selectedForegroundColor, selectedBackgroundColor;

	/**
	 * Colors when the button is not selected
	 */
	private Color unselectedForegroundColor, unselectedBackgroundColor;

	/**
	 * Colors for the button
	 */
	private Color buttonBorderColor, buttonBackgroundColor1, buttonBackgroundColor2;

	/**
	 * Gap between the button and the text (default value is 5)
	 */
	private int gap = 5;

	/**
	 * Margin inside the button (default is 5)
	 */
	private int insideMarginX = 5;

	/**
	 * Margin inside the button (default is 5)
	 */
	private int insideMarginY = 5;

	/**
	 * Arc of rounded rectangles (default is 3)
	 */
	private int arc = 3;

	/**
	 * Graphical context for this button
	 */
	private GC gc;

	/**
	 * True when the mouse entered the widget
	 */
	private boolean mouseInside;

	/**
	 * Constructs a new instance of this class given its parent and a style
	 * value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style the style of control to construct
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
	 */
	public SwitchButton(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);

		selection = false;
		text = "";
		textForSelect = "On";
		textForUnselect = "Off";
		round = true;
		borderColor = null;
		focusColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
		selectedForegroundColor = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		selectedBackgroundColor = SWTGraphicUtil.getDefaultColor(this, 0, 112, 195);
		unselectedForegroundColor = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		unselectedBackgroundColor = SWTGraphicUtil.getDefaultColor(this, 203, 203, 203);
		buttonBorderColor = SWTGraphicUtil.getDefaultColor(this, 96, 96, 96);
		buttonBackgroundColor1 = SWTGraphicUtil.getDefaultColor(this, 254, 254, 254);
		buttonBackgroundColor2 = SWTGraphicUtil.getDefaultColor(this, 192, 192, 192);

		addPaintListener(event -> {
			SwitchButton.this.onPaint(event);
		});

		addListener(SWT.MouseUp, e -> {
			selection = !selection;
			if (SelectionListenerUtil.fireSelectionListeners(this,e)) {
				SwitchButton.this.redraw();
			} else {
				// SelectionChanged event canceled
				selection = !selection;
			}
		});

		mouseInside = false;
		addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(final MouseEvent e) {
				mouseInside = true;
				SwitchButton.this.redraw();
			}

			@Override
			public void mouseExit(final MouseEvent e) {
				mouseInside = false;
				SwitchButton.this.redraw();
			}

			@Override
			public void mouseEnter(final MouseEvent e) {
				mouseInside = true;
				SwitchButton.this.redraw();
			}
		});

	}

	/**
	 * Paint the widget
	 *
	 * @param event paint event
	 */
	private void onPaint(final PaintEvent event) {
		final Rectangle rect = getClientArea();
		if (rect.width == 0 || rect.height == 0) {
			return;
		}
		gc = event.gc;
		gc.setAntialias(SWT.ON);

		final Point buttonSize = computeButtonSize();
		drawSwitchButton(buttonSize);
		drawText(buttonSize);

		if (borderColor != null) {
			drawBorder();
		}

	}

	/**
	 * Draw the switch button
	 *
	 * @param buttonSize size of the button
	 */
	private void drawSwitchButton(final Point buttonSize) {
		// Draw the background of the button
		gc.setForeground(buttonBorderColor);
		if (round) {
			gc.drawRoundRectangle(2, 2, buttonSize.x, buttonSize.y, arc, arc);
		} else {
			gc.drawRectangle(2, 2, buttonSize.x, buttonSize.y);
		}
		boolean enabled = isEnabled();
		drawRightPart(buttonSize, enabled);
		drawLeftPart(buttonSize, enabled);
		gc.setClipping(getClientArea());
		drawToggleButton(buttonSize);
	}

	/**
	 * Draw the right part of the button
	 *
	 * @param buttonSize size of the button
	 */
	private void drawRightPart(final Point buttonSize, boolean enabled) {
		if (enabled) {
			gc.setForeground(selectedBackgroundColor);
			gc.setBackground(selectedBackgroundColor);
		} else {
			Color disabled = gc.getDevice().getSystemColor(SWT.COLOR_TEXT_DISABLED_BACKGROUND);
			gc.setForeground(disabled);
			gc.setBackground(disabled);
		}
		gc.setClipping(3, 3, buttonSize.x / 2, buttonSize.y - 1);
		if (round) {
			gc.fillRoundRectangle(2, 2, buttonSize.x, buttonSize.y, arc, arc);
		} else {
			gc.fillRectangle(2, 2, buttonSize.x, buttonSize.y);
		}
		if (enabled) {
			gc.setForeground(selectedForegroundColor);
		} else {
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND));
		}
		final Point textSize = gc.textExtent(textForSelect);
		gc.drawString(textForSelect, (buttonSize.x / 2 - textSize.x) / 2 + arc, (buttonSize.y - textSize.y) / 2 + arc);
	}

	/**
	 * Draw the left part of the button
	 *
	 * @param buttonSize size of the button
	 */
	private void drawLeftPart(final Point buttonSize, boolean enabled) {
		if (enabled) {
			gc.setForeground(unselectedBackgroundColor);
			gc.setBackground(unselectedBackgroundColor);
		} else {
			Color disabled = gc.getDevice().getSystemColor(SWT.COLOR_TEXT_DISABLED_BACKGROUND);
			gc.setForeground(disabled);
			gc.setBackground(disabled);
		}
		gc.setClipping(buttonSize.x / 2 + 3, 3, buttonSize.x / 2, buttonSize.y - 1);
		if (round) {
			gc.fillRoundRectangle(2, 2, buttonSize.x, buttonSize.y, arc, arc);
		} else {
			gc.fillRectangle(2, 2, buttonSize.x, buttonSize.y);
		}
		if (enabled) {
			gc.setForeground(unselectedForegroundColor);
		} else {
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND));
		}
		final Point textSize = gc.textExtent(textForUnselect);

		gc.drawString(textForUnselect, buttonSize.x / 2 + (buttonSize.x / 2 - textSize.x) / 2 + arc, //
				(buttonSize.y - textSize.y) / 2 + arc);
	}

	/**
	 * Draw the toggle button
	 *
	 * @param buttonSize size of the button
	 */
	private void drawToggleButton(final Point buttonSize) {
		gc.setForeground(buttonBackgroundColor1);
		gc.setBackground(buttonBackgroundColor2);
		if (selection) {
			gc.fillGradientRectangle(buttonSize.x / 2, arc, buttonSize.x / 2 + 2, buttonSize.y - 1, true);
		} else {
			gc.fillGradientRectangle(arc, arc, buttonSize.x / 2, buttonSize.y, true);
		}

		gc.setForeground(buttonBorderColor);
		if (selection) {
			gc.drawRoundRectangle(buttonSize.x / 2, 2, buttonSize.x / 2 + 2, buttonSize.y, arc, arc);
		} else {
			gc.drawRoundRectangle(2, 2, buttonSize.x / 2, buttonSize.y, arc, arc);
		}

		if (focusColor != null && mouseInside) {
			gc.setForeground(focusColor);
			gc.setLineWidth(2);
			if (selection) {
				gc.drawRoundRectangle(buttonSize.x / 2 + 1, 3, buttonSize.x / 2, buttonSize.y - 2, 3, 3);
			} else {
				gc.drawRoundRectangle(3, 3, buttonSize.x / 2, buttonSize.y - 1, 3, 3);
			}
			gc.setLineWidth(1);
		}

	}

	/**
	 * @return the button size
	 */
	private Point computeButtonSize() {
		// Compute size for the left part
		final Point sizeForLeftPart = gc.stringExtent(textForSelect);
		// Compute size for the right part
		final Point sizeForRightPart = gc.stringExtent(textForUnselect);

		// Compute whole size
		final int width = Math.max(sizeForLeftPart.x, sizeForRightPart.x) * 2 + 2 * insideMarginX;
		final int height = Math.max(sizeForLeftPart.y, sizeForRightPart.y) + 2 * insideMarginY;

		return new Point(width, height);
	}

	/**
	 * Draws the text besides the button
	 *
	 * @param buttonSize whole size of the button
	 */
	private void drawText(final Point buttonSize) {
		gc.setForeground(getForeground());
		gc.setBackground(getBackground());

		final int widgetHeight = this.computeSize(0, 0, true).y;
		final int textHeight = gc.stringExtent(text).y;
		final int x = 2 + buttonSize.x + gap;

		gc.drawString(text, x, (widgetHeight - textHeight) / 2);
	}

	/**
	 * Draw (eventually) the border around the button
	 */
	private void drawBorder() {
		if (borderColor == null) {
			return;
		}

		gc.setForeground(borderColor);
		final Point temp = this.computeSize(0, 0, false);
		if (round) {
			gc.drawRoundRectangle(0, 0, temp.x - 2, temp.y - 2, 3, 3);
		} else {
			gc.drawRectangle(0, 0, temp.x - 2, temp.y - 2);
		}

	}



	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control is selected by the user, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the control is selected by the
	 * user. <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addTypedListener(listener, SWT.Selection);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the control is selected by the user.
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		removeTypedListener(SWT.Selection, listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		boolean disposeGC = false;
		if (gc == null || gc.isDisposed()) {
			gc = new GC(this);
			disposeGC = true;
		}

		final Point buttonSize = computeButtonSize();
		int width = buttonSize.x;
		int height = buttonSize.y;

		if (text != null && text.trim().length() > 0) {
			final Point textSize = gc.textExtent(text);
			width += textSize.x + gap + 1;
		}

		width += 4;
		height += 6;

		if (disposeGC) {
			gc.dispose();
		}

		return new Point(width, height);
	}

	/**
	 * @return the selection state of the button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getSelection() {
		checkWidget();
		return selection;
	}

	/**
	 * @param selection the selection state of the button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(final boolean selection) {
		checkWidget();
		this.selection = selection;
		redraw();
	}

	/**
	 * @return the text used to display the selection
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getTextForSelect() {
		checkWidget();
		return textForSelect;
	}

	/**
	 * @param textForSelect the text used to display the selection
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTextForSelect(final String textForSelect) {
		checkWidget();
		this.textForSelect = textForSelect;
		redraw();
	}

	/**
	 * @return the text used to display the unselected option
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getTextForUnselect() {
		checkWidget();
		return textForUnselect;
	}

	/**
	 * @param textForUnselect the text used to display the unselected option
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTextForUnselect(final String textForUnselect) {
		checkWidget();
		this.textForUnselect = textForUnselect;
		redraw();
	}

	/**
	 * @return the text displayed in the widget
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getText() {
		checkWidget();
		return text;
	}

	/**
	 * @param text the text displayed in the widget
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(final String text) {
		checkWidget();
		this.text = text;
		redraw();
	}

	/**
	 * @return the round flag
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean isRound() {
		checkWidget();
		return round;
	}

	/**
	 * @param round the round flag to set. If true, the widget is composed of
	 *            round rectangle instead of rectangles
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setRound(final boolean round) {
		checkWidget();
		this.round = round;
		redraw();
	}

	/**
	 * @return the border's color. If null, no border is displayed
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBorderColor() {
		checkWidget();
		return borderColor;
	}

	/**
	 * @param borderColor the border's color. If null, no border is displayed.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBorderColor(final Color borderColor) {
		checkWidget();
		this.borderColor = borderColor;
		redraw();
	}

	/**
	 * @return the focus color. If null, no focus effect is displayed.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getFocusColor() {
		checkWidget();
		return focusColor;
	}

	/**
	 * @param focusColor the focus color to set. If null, no focus effect is
	 *            displayed.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setFocusColor(final Color focusColor) {
		checkWidget();
		this.focusColor = focusColor;
		redraw();
	}

	/**
	 * @return the foreground color of the left part of the widget (selection is
	 *         on)
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getSelectedForegroundColor() {
		checkWidget();
		return selectedForegroundColor;
	}

	/**
	 * @param selectedForegroundColor the foreground color of the left part of the
	 *                                widget (selection is on)
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelectedForegroundColor(final Color selectedForegroundColor) {
		checkWidget();
		this.selectedForegroundColor = selectedForegroundColor;
		redraw();
	}

	/**
	 * @return the background color of the left part of the widget (selection is
	 *         on)
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getSelectedBackgroundColor() {
		checkWidget();
		return selectedBackgroundColor;
	}

	/**
	 * @param selectedBackgroundColor the background color of the left part of the
	 *                                widget (selection is on)
	 */
	public void setSelectedBackgroundColor(final Color selectedBackgroundColor) {
		checkWidget();
		this.selectedBackgroundColor = selectedBackgroundColor;
		redraw();
	}

	/**
	 * @return the foreground color of the left part of the widget (selection is
	 *         on)
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getUnselectedForegroundColor() {
		checkWidget();
		return unselectedForegroundColor;
	}

	/**
	 * @param unselectedForegroundColor the foreground color of the left part of
	 *            the widget (selection is on)
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setUnselectedForegroundColor(final Color unselectedForegroundColor) {
		checkWidget();
		this.unselectedForegroundColor = unselectedForegroundColor;
		redraw();
	}

	/**
	 * @return the background color of the left part of the widget (selection is
	 *         on)
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getUnselectedBackgroundColor() {
		checkWidget();
		return unselectedBackgroundColor;
	}

	/**
	 * @param unselectedBackgroundColor the background color of the left part of
	 *            the widget (selection is on)
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setUnselectedBackgroundColor(final Color unselectedBackgroundColor) {
		checkWidget();
		this.unselectedBackgroundColor = unselectedBackgroundColor;
		redraw();
	}

	/**
	 * @return the border color of the switch button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getButtonBorderColor() {
		checkWidget();
		return buttonBorderColor;
	}

	/**
	 * @param buttonBorderColor the border color of the switch button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setButtonBorderColor(final Color buttonBorderColor) {
		checkWidget();
		this.buttonBorderColor = buttonBorderColor;
		redraw();
	}

	/**
	 * @return the first color of the toggle button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getButtonBackgroundColor1() {
		checkWidget();
		return buttonBackgroundColor1;
	}

	/**
	 * @param buttonBackgroundColor1 the first color of the toggle button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setButtonBackgroundColor1(final Color buttonBackgroundColor1) {
		checkWidget();
		this.buttonBackgroundColor1 = buttonBackgroundColor1;
		redraw();
	}

	/**
	 * @return the second color of the toggle button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getButtonBackgroundColor2() {
		checkWidget();
		return buttonBackgroundColor2;
	}

	/**
	 * @param buttonBackgroundColor2 the second color of the toggle button
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setButtonBackgroundColor2(final Color buttonBackgroundColor2) {
		checkWidget();
		this.buttonBackgroundColor2 = buttonBackgroundColor2;
		redraw();
	}

	/**
	 * @return the gap value
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getGap() {
		checkWidget();
		return gap;
	}

	/**
	 * @param gap the gap value to set
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setGap(final int gap) {
		checkWidget();
		this.gap = gap;
		redraw();
	}

	/**
	 * @return the margin value
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Point getInsideMargin() {
		checkWidget();
		return new Point(insideMarginX, insideMarginY);
	}

	/**
	 * @param insideMarginX the new margin value (horizontal)
	 * @param insideMarginY the new margin value (vertical)
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setInsideMargin(final int insideMarginX, final int insideMarginY) {
		checkWidget();
		this.insideMarginX = insideMarginX;
		this.insideMarginY = insideMarginY;
		redraw();
	}

	/**
	 * @param insideMargin the new margin value
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setInsideMargin(final Point insideMargin) {
		checkWidget();
		insideMarginX = insideMargin.x;
		insideMarginY = insideMargin.y;
		redraw();
	}

	/**
	 * @return the arc value
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getArc() {
		checkWidget();
		return arc;
	}

	/**
	 * @param arc the arc value to set
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setArc(final int arc) {
		checkWidget();
		this.arc = arc;
		redraw();
	}

}
