/*******************************************************************************
 * Copyright (c) 2018 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron@gmail.com) - initial API and
 * implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.roundedcheckbox;

import org.eclipse.nebula.widgets.opal.commons.SelectionListenerUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Instances of this class provide a checkbox button.<br/>
 * Please notive that this widget draws only the checkbox (you can not attach a text like regular <code>Button</code> SWT Widget)
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * </p>
 */
public class RoundedCheckbox extends Canvas {
	private static final int DEFAULT_WIDTH = 20;
	private static final int DEFAULT_HEIGHT = 20;
	private boolean selected;
	private boolean grayed;
	private Color unselectedColor, selectionBackground, selectionForeground, hoverColor;
	private final Color disabledColor;
	private GC gc;
	private boolean cursorInside;

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
	public RoundedCheckbox(final Composite parent, final int style) {
		super(parent, checkStyle(style) | SWT.DOUBLE_BUFFERED);
		disabledColor = getAndDisposeColor(204, 204, 204);
		selectionBackground = getAndDisposeColor(0, 122, 255);
		selectionForeground = getAndDisposeColor(255, 255, 255);
		unselectedColor = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		hoverColor = getAndDisposeColor(56, 143, 188);

		addListener(SWT.Paint, e -> {
			gc = e.gc;
			drawElement();
		});

		addListener(SWT.MouseUp, e -> onClick(e));
		addListener(SWT.KeyUp, e -> onKeyPress(e));

		addListener(SWT.MouseEnter, e -> {
			cursorInside = true;
			redraw();
		});
		addListener(SWT.MouseExit, e -> {
			cursorInside = false;
			redraw();
		});
	}

	private static int checkStyle(final int style) {
		if ((style & SWT.BORDER) != 0) {
			return style & ~SWT.BORDER;
		}
		return 0;
	}

	private void drawElement() {
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		final Color previousForeground = gc.getForeground();
		final Color previousBackground = gc.getBackground();

		if (!selected) {
			drawUnselected();
		} else {
			if (grayed) {
				drawGrayed();
			} else {
				drawTicker();
			}
		}

		gc.setBackground(previousBackground);
		gc.setForeground(previousForeground);

	}

	private void drawUnselected() {
		final Rectangle rect = getClientArea();
		Color color;
		if (isEnabled()) {
			color = cursorInside ? hoverColor : unselectedColor;
		} else {
			color = disabledColor;
		}

		gc.setForeground(color);
		gc.setLineWidth(1);
		gc.drawOval(2, 2, rect.width - 4, rect.height - 4);
	}

	private void drawGrayed() {
		final Rectangle rect = getClientArea();
		Color color;
		if (isEnabled()) {
			color = cursorInside ? hoverColor : selectionBackground;
		} else {
			color = disabledColor;
		}

		gc.setForeground(color);
		gc.setLineWidth(1);
		gc.drawOval(2, 2, rect.width - 4, rect.height - 4);
		gc.setBackground(color);
		gc.fillOval(5, 5, rect.width - 9, rect.height - 9);

	}

	private void drawTicker() {
		final Rectangle rect = getClientArea();
		Color color;
		if (isEnabled()) {
			color = cursorInside ? hoverColor : selectionBackground;
		} else {
			color = disabledColor;
		}

		gc.setBackground(color);
		gc.fillOval(2, 2, rect.width - 4, rect.height - 4);

		gc.setForeground(selectionForeground);
		gc.setLineWidth(2);
		final int centerX = rect.width / 2;
		final int centerY = rect.height / 2;
		gc.drawLine(5, centerY, 8, centerY + 4);
		gc.drawLine(8, centerY + 4, centerX + 5, centerY - 3);

	}

	private void onClick(Event e) {
		final Rectangle rect = getClientArea();
		final int centerX = (rect.width - rect.x) / 2;
		final int centerY = (rect.height - rect.y) / 2;
		final int distance = (int) Math.sqrt(Math.abs(e.x - centerX ^ 2 - (e.y - centerY) ^ 2));
		if (distance < rect.width - 4) {
			if (SelectionListenerUtil.fireSelectionListeners(this,e)) {
				setSelection(!selected);
			}
		}
	}
	

	private void onKeyPress(Event e) {
		if (e.character == ' ' || e.character == '+') {
			setSelection(!selected);
		}
	}

	private Color getAndDisposeColor(int r, int g, int b) {
		final Color color = new Color(getDisplay(), r, g, b);
		addDisposeListener(e -> {
			if (!color.isDisposed()) {
				color.dispose();
			}
		});
		return color;
	}


	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the control is selected by the user, by sending it one of the messages
	 * defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 *
	 * @param listener the listener which should be notified when the control is
	 *            selected by the user,
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
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width = DEFAULT_WIDTH;
		int height = DEFAULT_HEIGHT;
		if (wHint != SWT.DEFAULT) {
			width = wHint;
		}
		if (hHint != SWT.DEFAULT) {
			height = hHint;
		}
		final int border = getBorderWidth();
		width += border * 2;
		height += border * 2;
		return new Point(width, height);
	}

	/**
	 * Returns <code>true</code> if the receiver is grayed,
	 * and false otherwise. 
	 *
	 * @return the grayed state of the checkbox
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.4
	 */
	public boolean getGrayed() {
		checkWidget();
		return grayed;
	}

	/**
	 * Returns the receiver's color when the mouse is hover the widget.
	 * 
	 * @return the receiver's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getHoverColor() {
		checkWidget();
		return hoverColor;
	}

	/**
	 * Returns the receiver's background color when the widget is selected.
	 * 
	 * @return the background color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getSelectedBackgroundColor() {
		checkWidget();
		return selectionBackground;
	}

	/**
	 * Returns the receiver's foreground color when the widget is selected.
	 * 
	 * @return the foreground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getSelectedForegroundColor() {
		checkWidget();
		return selectionForeground;
	}

	/**
	 * Returns <code>true</code> if the receiver is selected,
	 * and false otherwise.
	 *
	 * @return the selection state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getSelection() {
		checkWidget();
		return selected;
	}

	/**
	 * Returns the receiver's foreground color when the widget is not selected.
	 * 
	 * @return the foreground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getUnselectedColor() {
		checkWidget();
		return unselectedColor;
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified
	 * when the control is selected by the user.
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
	 * Sets the grayed state of the receiver. This state change
	 * only applies if the control was created with the SWT.CHECK
	 * style.
	 *
	 * @param grayed the new grayed state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setGrayed(boolean grayed) {
		checkWidget();
		this.grayed = grayed;
		redraw();
		update();
	}

	/**
	 * Sets the button's color when the mouse is hover the widget to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * 
	 * @param color the new color (or null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setHoverColor(Color color) {
		checkWidget();
		if (color == null) {
			hoverColor = getAndDisposeColor(56, 143, 188);
		} else {
			hoverColor = color;
		}
	}

	/**
	 * Sets the button's background color when the widget is selected to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * 
	 * @param color the new color (or null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelectedBackgroundColor(Color color) {
		checkWidget();
		if (color == null) {
			selectionBackground = getAndDisposeColor(0, 122, 255);
		} else {
			selectionBackground = color;
		}
	}

	/**
	 * Sets the button's foreground color when the widget is selected to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * 
	 * @param color the new color (or null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelectedForegroundColor(Color color) {
		checkWidget();
		if (color == null) {
			selectionForeground = getAndDisposeColor(255, 255, 255);
		} else {
			selectionForeground = color;
		}
	}

	/**
	 * Sets the selection state of the receiver.
	 *
	 * @param selected the new selection state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(boolean selected) {
		checkWidget();
		this.selected = selected;
		redraw();
		update();
	}

	/**
	 * Sets the button's drawing color when the widget is not selected to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * 
	 * @param color the new color (or null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setUnselectedColor(Color color) {
		checkWidget();
		if (color == null) {
			unselectedColor = getAndDisposeColor(204, 204, 204);
		} else {
			unselectedColor = color;
		}
	}

}
