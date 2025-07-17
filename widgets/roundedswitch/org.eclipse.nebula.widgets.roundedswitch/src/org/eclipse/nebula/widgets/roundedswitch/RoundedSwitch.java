/*******************************************************************************
 * Copyright (c) 2020 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API and
 * implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.roundedswitch;

import org.eclipse.nebula.widgets.opal.commons.SelectionListenerUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Instances of this class provide a checkbox button displayed as a switch.<br/>
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
public class RoundedSwitch extends Canvas {

	// Presentation
	private int borderWidth;
	private RoundedSwitchConfiguration checkedEnabledConfiguration;
	private RoundedSwitchConfiguration uncheckedEnabledConfiguration;
	private RoundedSwitchConfiguration checkedDisabledConfiguration;
	private RoundedSwitchConfiguration uncheckedDisabledConfiguration;

	private static final int DEFAULT_WIDTH = 45;
	private static final int DEFAULT_HEIGHT = 20;
	private boolean selected;
	private GC gc;

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
	public RoundedSwitch(Composite parent, int style) {
		super(parent, checkStyle(style) | SWT.DOUBLE_BUFFERED);
		selected = false;
		borderWidth = 2;
		checkedEnabledConfiguration = RoundedSwitchConfiguration.createCheckedEnabledConfiguration(this);
		uncheckedEnabledConfiguration = RoundedSwitchConfiguration.createUncheckedEnabledConfiguration(this);
		checkedDisabledConfiguration = RoundedSwitchConfiguration.createCheckedDisabledConfiguration(this);
		uncheckedDisabledConfiguration = RoundedSwitchConfiguration.createUncheckedDisabledConfiguration(this);

		addListener(SWT.Paint, e -> {
			gc = e.gc;
			drawWidget();
		});

		addListener(SWT.MouseUp, e -> onClick(e));
		addListener(SWT.KeyUp, e -> onKeyPress(e));

		addListener(SWT.MouseEnter, e -> {
			if (!isEnabled()) {
				return;
			}
			setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		});
		addListener(SWT.MouseExit, e -> {
			if (!isEnabled())
				return;
			setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		});
	}

	private static int checkStyle(final int style) {
		if ((style & SWT.BORDER) != 0) {
			return style & ~SWT.BORDER;
		}
		return 0;
	}

	private void drawWidget() {
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		final Color previousForeground = gc.getForeground();
		final Color previousBackground = gc.getBackground();

		if (isEnabled()) {
			if (selected) {
				drawCheckedEnabled();
			} else {
				drawUncheckedEnabled();
			}
		} else {
			if (selected) {
				drawCheckedDisabled();
			} else {
				drawUncheckedDisabled();
			}
		}

		gc.setBackground(previousBackground);
		gc.setForeground(previousForeground);
	}

	private void drawCheckedEnabled() {
		gc.setBackground(checkedEnabledConfiguration.backgroundColor);
		drawOuterRoundedRectangle();
		gc.setForeground(checkedEnabledConfiguration.borderColor);
		drawBorderRoundedRectangle();
		gc.setBackground(checkedEnabledConfiguration.circleColor);
		drawCircle();
	}

	private void drawOuterRoundedRectangle() {
		final int width = getSize().x;
		final int height = getSize().y;
		gc.fillRoundRectangle(2, 2, width - 4, height - 4, height - 4, height - 4);
	}

	private void drawBorderRoundedRectangle() {
		if (gc.getForeground().equals(gc.getBackground())) {
			return;
		}
		final int width = getSize().x;
		final int height = getSize().y;
		gc.setLineWidth(borderWidth);
		gc.drawRoundRectangle(2, 2, width - 4, height - 4, height - 4, height - 4);
	}

	private void drawCircle() {
		final int width = getSize().x;
		final int height = getSize().y;
		int x;
		if (selected) {
			x = width - height + 4;
		} else {
			x = 6;
		}
		gc.fillOval(x, 5, height - 10, height - 10);
	}

	private void drawUncheckedEnabled() {
		gc.setBackground(uncheckedEnabledConfiguration.backgroundColor);
		drawOuterRoundedRectangle();
		gc.setForeground(uncheckedEnabledConfiguration.borderColor);
		drawBorderRoundedRectangle();
		gc.setBackground(uncheckedEnabledConfiguration.circleColor);
		drawCircle();
	}

	private void drawCheckedDisabled() {
		gc.setBackground(checkedDisabledConfiguration.backgroundColor);
		drawOuterRoundedRectangle();
		gc.setForeground(checkedDisabledConfiguration.borderColor);
		drawBorderRoundedRectangle();
		gc.setBackground(checkedDisabledConfiguration.circleColor);
		drawCircle();
	}

	private void drawUncheckedDisabled() {
		gc.setBackground(uncheckedDisabledConfiguration.backgroundColor);
		drawOuterRoundedRectangle();
		gc.setForeground(uncheckedDisabledConfiguration.borderColor);
		drawOuterRoundedRectangle();
		gc.setBackground(uncheckedDisabledConfiguration.circleColor);
		drawCircle();
	}

	private void onClick(Event e) {
		if (!isEnabled()) {
			return;
		}
		if (SelectionListenerUtil.fireSelectionListeners(this,e)) {
			setSelection(!selected);
		}
	}

	
	private void onKeyPress(Event e) {
		if (!isEnabled()) {
			return;
		}
		if (e.character == ' ' || e.character == '+') {
			setSelection(!selected);
		}
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

	// -------- Getter & Setter for border width and color configuration
	/**
	 * Returns the receiver's border width
	 * 
	 * @return the border width
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int getBorderWidth() {
		checkWidget();
		return borderWidth;
	}

	/**
	 * Sets the receiver's border width
	 *
	 * @param borderWidth the new border width
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBorderWidth(int borderWidth) {
		checkWidget();
		this.borderWidth = borderWidth;
	}

	// -- BORDER

	/**
	 * Returns the border's color when the widget is checked and enabled.
	 * 
	 * @return the border's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBorderColorCheckedEnabled() {
		checkWidget();
		return checkedEnabledConfiguration.borderColor;
	}

	/**
	 * Sets the border's color when when the widget is checked and enabled or to the default system color for the control
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
	public void setBorderColorCheckedEnabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createCheckedEnabledConfiguration(this);
			checkedEnabledConfiguration.borderColor = temp.borderColor;
		} else {
			checkedEnabledConfiguration.borderColor = color;
		}
	}

	/**
	 * Returns the border's color when the widget is unchecked and enabled.
	 * 
	 * @return the border's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBorderColorUncheckedEnabled() {
		checkWidget();
		return uncheckedEnabledConfiguration.borderColor;
	}

	/**
	 * Sets the border's color when when the widget is unchecked and enabled or to the default system color for the control
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
	public void setBorderColorUncheckedEnabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createUncheckedEnabledConfiguration(this);
			uncheckedEnabledConfiguration.borderColor = temp.borderColor;
		} else {
			uncheckedEnabledConfiguration.borderColor = color;
		}
	}

	/**
	 * Returns the border's color when the widget is checked and disabled.
	 * 
	 * @return the border's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBorderColorCheckedDisabled() {
		checkWidget();
		return checkedDisabledConfiguration.borderColor;
	}

	/**
	 * Sets the border's color when when the widget is checked and disabled or to the default system color for the control
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
	public void setBorderColorCheckedDisabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createCheckedDisabledConfiguration(this);
			checkedDisabledConfiguration.borderColor = temp.borderColor;
		} else {
			checkedDisabledConfiguration.borderColor = color;
		}
	}

	/**
	 * Returns the border's color when the widget is unchecked and disabled.
	 * 
	 * @return the border's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBorderColorUncheckedDisabled() {
		checkWidget();
		return uncheckedDisabledConfiguration.borderColor;
	}

	/**
	 * Sets the border's color when when the widget is unchecked and disabled or to the default system color for the control
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
	public void setBorderColorUncheckedDisabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createUncheckedDisabledConfiguration(this);
			uncheckedDisabledConfiguration.borderColor = temp.borderColor;
		} else {
			uncheckedDisabledConfiguration.borderColor = color;
		}
	}

	// -- CIRCLE
	/**
	 * Returns the circle's color when the widget is checked and enabled.
	 * 
	 * @return the circle's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCircleColorCheckedEnabled() {
		checkWidget();
		return checkedEnabledConfiguration.circleColor;
	}

	/**
	 * Sets the circle's color when when the widget is checked and enabled or to the default system color for the control
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
	public void setCircleColorCheckedEnabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createCheckedEnabledConfiguration(this);
			checkedEnabledConfiguration.circleColor = temp.circleColor;
		} else {
			checkedEnabledConfiguration.circleColor = color;
		}
	}

	/**
	 * Returns the circle's color when the widget is unchecked and enabled.
	 * 
	 * @return the circle's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCircleColorUncheckedEnabled() {
		checkWidget();
		return uncheckedEnabledConfiguration.circleColor;
	}

	/**
	 * Sets the circle's color when when the widget is unchecked and enabled or to the default system color for the control
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
	public void setCircleColorUncheckedEnabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createUncheckedEnabledConfiguration(this);
			uncheckedEnabledConfiguration.circleColor = temp.circleColor;
		} else {
			uncheckedEnabledConfiguration.circleColor = color;
		}
	}

	/**
	 * Returns the circle's color when the widget is checked and disabled.
	 * 
	 * @return the circle's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCircleColorCheckedDisabled() {
		checkWidget();
		return checkedDisabledConfiguration.circleColor;
	}

	/**
	 * Sets the circle's color when when the widget is checked and disabled or to the default system color for the control
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
	public void setCircleColorCheckedDisabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createCheckedDisabledConfiguration(this);
			checkedDisabledConfiguration.circleColor = temp.circleColor;
		} else {
			checkedDisabledConfiguration.circleColor = color;
		}
	}

	/**
	 * Returns the circle's color when the widget is unchecked and disabled.
	 * 
	 * @return the circle's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCircleColorUncheckedDisabled() {
		checkWidget();
		return uncheckedDisabledConfiguration.circleColor;
	}

	/**
	 * Sets the circle's color when when the widget is unchecked and disabled or to the default system color for the control
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
	public void setCircleColorUncheckedDisabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createUncheckedDisabledConfiguration(this);
			uncheckedDisabledConfiguration.circleColor = temp.circleColor;
		} else {
			uncheckedDisabledConfiguration.circleColor = color;
		}
	}

	// -- Background
	/**
	 * Returns the background's color when the widget is checked and enabled.
	 * 
	 * @return the background's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBackgroundColorCheckedEnabled() {
		checkWidget();
		return checkedEnabledConfiguration.backgroundColor;
	}

	/**
	 * Sets the background's color when when the widget is checked and enabled or to the default system color for the control
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
	public void setBackgroundColorCheckedEnabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createCheckedEnabledConfiguration(this);
			checkedEnabledConfiguration.backgroundColor = temp.backgroundColor;
		} else {
			checkedEnabledConfiguration.backgroundColor = color;
		}
	}

	/**
	 * Returns the background's color when the widget is unchecked and enabled.
	 * 
	 * @return the background's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBackgroundColorUncheckedEnabled() {
		checkWidget();
		return uncheckedEnabledConfiguration.backgroundColor;
	}

	/**
	 * Sets the background's color when when the widget is unchecked and enabled or to the default system color for the control
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
	public void setBackgroundColorUncheckedEnabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createUncheckedEnabledConfiguration(this);
			uncheckedEnabledConfiguration.backgroundColor = temp.backgroundColor;
		} else {
			uncheckedEnabledConfiguration.backgroundColor = color;
		}
	}

	/**
	 * Returns the background's color when the widget is checked and disabled.
	 * 
	 * @return the background's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBackgroundColorCheckedDisabled() {
		checkWidget();
		return checkedDisabledConfiguration.backgroundColor;
	}

	/**
	 * Sets the background's color when when the widget is checked and disabled or to the default system color for the control
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
	public void setBackgroundColorCheckedDisabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createCheckedDisabledConfiguration(this);
			checkedDisabledConfiguration.backgroundColor = temp.backgroundColor;
		} else {
			checkedDisabledConfiguration.backgroundColor = color;
		}
	}

	/**
	 * Returns the background's color when the widget is unchecked and disabled.
	 * 
	 * @return the background's color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBackgroundColorUncheckedDisabled() {
		checkWidget();
		return uncheckedDisabledConfiguration.backgroundColor;
	}

	/**
	 * Sets the background's color when when the widget is unchecked and disabled or to the default system color for the control
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
	public void setBackgroundColorUncheckedDisabled(Color color) {
		checkWidget();
		if (color == null) {
			RoundedSwitchConfiguration temp = RoundedSwitchConfiguration.createUncheckedDisabledConfiguration(this);
			uncheckedDisabledConfiguration.backgroundColor = temp.backgroundColor;
		} else {
			uncheckedDisabledConfiguration.backgroundColor = color;
		}
	}
}
