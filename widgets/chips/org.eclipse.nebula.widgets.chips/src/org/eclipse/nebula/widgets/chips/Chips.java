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
package org.eclipse.nebula.widgets.chips;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.nebula.widgets.opal.commons.SelectionListenerUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class represent a "chips". This is a kind of rounded-shapped button. It can display information,
 * or be used like a check or a push button. You can also add a close button.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.CLOSE</dd>
 * <dd>SWT.CHECK</dd>
 * <dd>SWT.TOGGLE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>SWT.Close, SWT.Selection</dd>
 * </dl>
 */
public class Chips extends Canvas {

	private static final int CLOSE_CIRCLE_RAY = 7;

	private Color hoverForeground, hoverBackground;
	private Color closeButtonForeground, closeButtonBackground;
	private Color closeButtonHoverForeground, closeButtonHoverBackground;
	private Color pushedStateForeground, pushedStateBackground;
	private Color borderColor, hoverBorderColor, pushedStateBorderColor;
	private Color chipsBackground;
	private String text;
	private Image image, pushImage, hoverImage;
	private boolean selection;
	private final boolean isCheck;
	private final boolean isToggle;
	private final boolean isClose;
	private final List<CloseListener> closeListeners = new ArrayList<>();
	private boolean cursorInside;
	private Point closeCenter;

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
	 * @param parent a composite control which will be the parent of the new instance (cannot be null)
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
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *                subclass</li>
	 *                </ul>
	 *
	 */
	public Chips(final Composite parent, final int style) {
		super(parent, checkStyle(style));
		initDefaultColors();
		text = "";
		isCheck = (getStyle() & SWT.CHECK) != 0;
		isToggle = (getStyle() & SWT.TOGGLE) != 0 || (getStyle() & SWT.PUSH) != 0;
		isClose = (getStyle() & SWT.CLOSE) != 0;

		addListener(SWT.Paint, e -> {
			final GC gc = e.gc;
			gc.setFont(getFont());
			gc.setAdvanced(true);
			gc.setTextAntialias(SWT.ON);
			gc.setAntialias(SWT.ON);
			final Color previousForeground = gc.getForeground();
			final Color previousBackground = gc.getBackground();

			int x = drawBackground(gc);
			drawWidgetBorder(gc);
			if (isCheck && selection) {
				x = drawCheck(gc, x);
			}

			if (image != null) {
				x = drawImage(gc, x);
			}

			if (text != null) {
				x = drawText(gc, x);
			}

			if (isClose) {
				drawClose(gc, x);
			}
			gc.setBackground(previousBackground);
			gc.setForeground(previousForeground);

		});

		addListener(SWT.MouseEnter, e -> {
			cursorInside = isToggle || isCheck || isClose;
			if (cursorInside) {
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			}
			redraw();
		});
		addListener(SWT.MouseExit, e -> {
			cursorInside = false;
			if (isToggle || isCheck || isClose) {
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			}
			redraw();
		});
		addListener(SWT.MouseUp, e -> {
			if (!isClose && !isCheck && !isToggle) {
				return;
			}
			if (isClose) {
				final float dist = (float) Math.sqrt((e.x - closeCenter.x) * (e.x - closeCenter.x) + (e.y - closeCenter.y) * (e.y - closeCenter.y));
				if (dist < CLOSE_CIRCLE_RAY) {
					final CloseEvent event = new CloseEvent(e);
					for (final CloseListener listener : closeListeners) {
						listener.onClose(event);
						if (!event.doit) {
							break;
						}
					}
				}
			}
			if (isDisposed()) {
				return;
			}
			setSelection(!selection);
			SelectionListenerUtil.fireSelectionListeners(this, e);
		});
	}

	private static int checkStyle(final int style) {
		final int mask = SWT.CLOSE | SWT.CHECK | SWT.TOGGLE | SWT.PUSH;
		int newStyle = style & mask;
		newStyle |= SWT.DOUBLE_BUFFERED;
		return newStyle;
	}

	private int drawBackground(final GC gc) {
		final Rectangle rect = getClientArea();

		final Color color = determineBackgroundColor();
		gc.setBackground(color);
		gc.fillRoundRectangle(0, 0, rect.width, rect.height, rect.height, rect.height);

		return rect.height / 2 + 2;
	}

	private void drawWidgetBorder(final GC gc) {
		final Rectangle rect = getClientArea();
		Color color = borderColor;
		if (cursorInside) {
			color = hoverBorderColor;
		} else if (isToggle && selection) {
			color = pushedStateBorderColor;
		}

		if (color == null) {
			// No border
			return;
		}

		gc.setForeground(color);
		gc.drawRoundRectangle(0, 0, rect.width - 2, rect.height - 2, rect.height, rect.height);

	}

	private Color determineBackgroundColor() {
		if (cursorInside) {
			return hoverBackground == null ? getBackground() : hoverBackground;
		}
		if (isToggle && selection) {
			return pushedStateBackground == null ? getBackground() : pushedStateBackground;
		}
		return getChipsBackground() == null ? getBackground() : getChipsBackground();
	}

	private int drawCheck(final GC gc, final int x) {
		Color foreground = null;
		if (cursorInside) {
			foreground = hoverForeground;
		} else if (isToggle && selection) {
			foreground = pushedStateForeground;
		}
		foreground = foreground == null ? getForeground() : foreground;
		gc.setForeground(foreground);
		gc.setLineWidth(2);

		final Rectangle rect = getClientArea();

		final int centerX = x + 4 + CLOSE_CIRCLE_RAY;
		final int centerY = (rect.height - 2 * CLOSE_CIRCLE_RAY) / 2 + CLOSE_CIRCLE_RAY;
		gc.drawLine(x + 6, centerY, x + 9, centerY + 4);
		gc.drawLine(x + 9, centerY + 4, centerX + 4, centerY - 3);
		return x + 16;
	}

	private int drawImage(final GC gc, final int x) {
		Image img = image;
		if (cursorInside) {
			img = hoverImage == null ? img : hoverImage;
		}
		if (isToggle && selection) {
			img = pushImage == null ? img : pushImage;
		}

		final Rectangle rect = getClientArea();
		gc.drawImage(img, x + 2, (rect.height - img.getBounds().height) / 2);

		return x + 4 + img.getBounds().width;
	}

	private int drawText(final GC gc, final int x) {
		final Point textSize = gc.stringExtent(text);
		Color color = null;
		if (cursorInside) {
			color = hoverForeground;
		} else if (isToggle && selection) {
			color = pushedStateForeground;
		}
		color = color == null ? getForeground() : color;
		gc.setForeground(color);

		gc.drawText(text, x + 2, (getClientArea().height - textSize.y) / 2, true);

		return x + 2 + textSize.x;
	}

	private void drawClose(final GC gc, final int x) {
		final Color foreground = cursorInside ? closeButtonHoverForeground : closeButtonForeground;
		final Color background = cursorInside ? closeButtonHoverBackground : closeButtonBackground;
		final Rectangle rect = getClientArea();

		gc.setBackground(background);
		gc.setForeground(foreground);
		closeCenter = new Point(x + 4 + CLOSE_CIRCLE_RAY, (rect.height - 2 * CLOSE_CIRCLE_RAY) / 2 + CLOSE_CIRCLE_RAY);
		gc.fillOval(x + 4, (rect.height - 2 * CLOSE_CIRCLE_RAY) / 2, 2 * CLOSE_CIRCLE_RAY, 2 * CLOSE_CIRCLE_RAY);

		// Cross
		gc.setLineWidth(2);
		gc.drawLine(closeCenter.x - 3, closeCenter.y - 3, closeCenter.x + 3, closeCenter.y + 3);
		gc.drawLine(closeCenter.x + 3, closeCenter.y - 3, closeCenter.x - 3, closeCenter.y + 3);
	}

	private void initDefaultColors() {
		setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		chipsBackground = SWTGraphicUtil.getColorSafely(224, 224, 224);

		hoverForeground = SWTGraphicUtil.getColorSafely(62, 28, 96);
		hoverBackground = SWTGraphicUtil.getColorSafely(214, 214, 214);

		closeButtonForeground = SWTGraphicUtil.getColorSafely(229, 229, 229);
		closeButtonBackground = SWTGraphicUtil.getColorSafely(100, 100, 100);
		closeButtonHoverForeground = SWTGraphicUtil.getColorSafely(214, 214, 214);
		closeButtonHoverBackground = SWTGraphicUtil.getColorSafely(64, 64, 64);

		pushedStateForeground = SWTGraphicUtil.getColorSafely(224, 224, 224);
		pushedStateBackground = getDisplay().getSystemColor(SWT.COLOR_BLACK);
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		int width = 0; // Border
		int height = 20;
		if (image != null) {
			final Rectangle imageSize = image.getBounds();
			width += 4 + imageSize.width;
			height = Math.max(height, imageSize.height + 4);

		}

		if (text != null) {
			final GC gc = new GC(this);
			final Point textSize = gc.stringExtent(text);
			width += 4 + textSize.x;
			height = Math.max(height, textSize.y);
			gc.dispose();
		}

		if (isCheck && selection || isClose) {
			width += 20;
		}

		width += Math.max(height, hHint); // Size for left & right half-circle
		return new Point(Math.max(width, wHint), Math.max(height, hHint));
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the control is closed by the user, by sending it one of the messages
	 * defined in the <code>CodeListener</code> interface.
	 * <p>
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 *
	 * @param listener the listener which should be notified when the control is
	 *            closed by the user,
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
	 * @see CloseListener
	 * @see #removeCloseListener
	 * @see SelectionEvent
	 */
	public void addCloseListener(final CloseListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		closeListeners.add(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#addListener(int, org.eclipse.swt.widgets.Listener)
	 */
	@Override
	public void addListener(final int eventType, final Listener listener) {
		if (eventType == SWT.Close) {
			closeListeners.add(e -> {
				final Event event = new Event();
				event.widget = Chips.this;
				event.display = getDisplay();
				event.type = SWT.Close;
				listener.handleEvent(event);
			});
			return;
		}
		super.addListener(eventType, listener);
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
	 * Removes the listener from the collection of listeners who will be notified
	 * when the control is closed by the user.
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
	 * @see CloseListener
	 * @see #addCloseListener
	 */
	public void removeCloseListener(final CloseListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		closeListeners.remove(listener);
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

	// ---- Getters & Setters

	/**
	 * Returns the receiver's background color.
	 *
	 * @return the background color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getChipsBackground() {
		checkWidget();
		return chipsBackground;
	}

	/**
	 * Returns the receiver's foreground color when mouse is hover the widget.
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the foreground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getHoverForeground() {
		checkWidget();
		return hoverForeground;
	}

	/**
	 * Returns the receiver's background color when mouse is hover the widget.
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the background color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getHoverBackground() {
		checkWidget();
		return hoverBackground;
	}

	/**
	 * Returns the receiver's close item foreground color.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the foreground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCloseButtonForeground() {
		checkWidget();
		return closeButtonForeground;
	}

	/**
	 * Returns the receiver's close item background color.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the background color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCloseButtonBackground() {
		checkWidget();
		return closeButtonBackground;
	}

	/**
	 * Returns the receiver's close item foreground color when the mouse is hover the widget.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the foreground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCloseButtonHoverForeground() {
		checkWidget();
		return closeButtonHoverForeground;
	}

	/**
	 * Returns the receiver's close item background color when the mouse is hover the widget.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the background color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getCloseButtonHoverBackground() {
		checkWidget();
		return closeButtonHoverBackground;
	}

	/**
	 * Returns the receiver's foreground color when the widget is "pushed" (selected).
	 * <p>
	 * Note: This operation is only available if the SWT.TOGGLE flag is set.
	 * </p>
	 *
	 * @return the foreground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getPushedStateForeground() {
		checkWidget();
		return pushedStateForeground;
	}

	/**
	 * Returns the receiver's background color when the widget is "pushed" (selected).
	 * <p>
	 * Note: This operation is only available if the SWT.TOGGLE flag is set.
	 * </p>
	 *
	 * @return the background color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getPushedStateBackground() {
		checkWidget();
		return pushedStateBackground;
	}

	/**
	 * Returns the receiver's color for the border of the widget.
	 *
	 * @return the border color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBorderColor() {
		checkWidget();
		return borderColor;
	}

	/**
	 * Returns the receiver's color for the border when the mouse is hover the widget
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the border color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getHoverBorderColor() {
		checkWidget();
		return hoverBorderColor;
	}

	/**
	 * Returns the receiver's color for the border when the widget is "pushed" (selected)
	 * <p>
	 * Note: This operation is only available if the SWT.TOGGLE flag is set.
	 * </p>
	 *
	 * @return the border color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getPushedStateBorderColor() {
		checkWidget();
		return pushedStateBorderColor;
	}

	/**
	 * Returns the receiver's text, which will be an empty
	 * string if it has never been set.
	 *
	 * @return the receiver's text
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public String getText() {
		checkWidget();
		return text;
	}

	/**
	 * Returns the receiver's image if it has one, or null
	 * if it does not.
	 *
	 * @return the receiver's image
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getImage() {
		checkWidget();
		return image;
	}

	/**
	 * Returns the receiver's image when the widget is pushed (selected) if it has one, or null
	 * if it does not.
	 * <p>
	 * Note: This operation is only available if the SWT.TOGGLE flag is set.
	 * </p>
	 *
	 * @return the receiver's image
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getPushImage() {
		checkWidget();
		return pushImage;
	}

	/**
	 * Returns the receiver's image when the mouse is hover the widget if it has one, or null
	 * if it does not.
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @return the receiver's image
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getHoverImage() {
		checkWidget();
		return hoverImage;
	}

	/**
	 * Returns <code>true</code> if the receiver is selected,
	 * and false otherwise.
	 * <p>
	 * Note: This operation is only available if the SWT.CHECK or the SWT.TOGGLE
	 * flag is set.
	 * </p>
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
		return selection;
	}

	/**
	 * Sets the receiver's background color to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 *
	 * @param chipsBackground the new color (or null)
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
	public void setChipsBackground(final Color chipsBackground) {
		checkWidget();
		this.chipsBackground = chipsBackground;
	}

	/**
	 * Sets the receiver's foreground color to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @param hoverForeground the new color (or null)
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
	public void setHoverForeground(final Color hoverForeground) {
		checkWidget();
		this.hoverForeground = hoverForeground;
	}

	/**
	 * Sets the receiver's background color to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @param hoverBackground the new color (or null)
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
	public void setHoverBackground(final Color hoverBackground) {
		checkWidget();
		this.hoverBackground = hoverBackground;
	}

	/**
	 * Sets the receiver's close button foreground color to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
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
	public void setCloseButtonForeground(final Color closeButtonForeground) {
		checkWidget();
		this.closeButtonForeground = closeButtonForeground;
	}

	/**
	 * Sets the receiver's close button background color to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @param closeButtonBackground the new color (or null)
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
	public void setCloseButtonBackground(final Color closeButtonBackground) {
		checkWidget();
		this.closeButtonBackground = closeButtonBackground;
	}

	/**
	 * Sets the receiver's close button foreground color (when the mouse is hover the widget) to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
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
	public void setCloseButtonHoverForeground(final Color closeButtonHoverForeground) {
		checkWidget();
		this.closeButtonHoverForeground = closeButtonHoverForeground;
	}

	/**
	 * Sets the receiver's close button background color (when the mouse is hover the widget) to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if the SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @param closeButtonHoverBackground the new color (or null)
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
	public void setCloseButtonHoverBackground(final Color closeButtonHoverBackground) {
		checkWidget();
		this.closeButtonHoverBackground = closeButtonHoverBackground;
	}

	/**
	 * Sets the receiver's foreground color when the button is "pushed" (=selected) to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if the SWT.TOGGLE flag is set.
	 * </p>
	 *
	 * @param pushedStateForeground the new color (or null)
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
	public void setPushedStateForeground(final Color pushedStateForeground) {
		checkWidget();
		this.pushedStateForeground = pushedStateForeground;
	}

	/**
	 * Sets the receiver's background color when the button is "pushed" (=selected) to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if the SWT.TOGGLE flag is set.
	 * </p>
	 *
	 * @param pushedStateBackground the new color (or null)
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
	public void setPushedStateBackground(final Color pushedStateBackground) {
		checkWidget();
		this.pushedStateBackground = pushedStateBackground;
	}

	/**
	 * Sets the receiver's border color to the color specified
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
	public void setBorderColor(final Color borderColor) {
		checkWidget();
		this.borderColor = borderColor;
	}

	/**
	 * Sets the receiver's border color (when the mouse is hover the widget) to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @param hoverBorderColor the new color (or null)
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
	public void setHoverBorderColor(final Color hoverBorderColor) {
		checkWidget();
		this.hoverBorderColor = hoverBorderColor;
	}

	/**
	 * Sets the receiver's border color when the button is "pushed" (selected) to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * <p>
	 * Note: This operation is a hint and may be overridden by the platform.
	 * </p>
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
	public void setPushedStateBorderColor(final Color pushedStateBorderColor) {
		checkWidget();
		this.pushedStateBorderColor = pushedStateBorderColor;
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the widget label. The label may include
	 * the mnemonic character and line delimiters.
	 * </p>
	 *
	 * @param string the new text
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(final String text) {
		checkWidget();
		this.text = text;
	}

	/**
	 * Sets the receiver's image to the argument, which may be
	 * null indicating that no image should be displayed.
	 *
	 * @param image the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setImage(final Image image) {
		checkWidget();
		this.image = image;
	}

	/**
	 * Sets the receiver's image to the argument when the widget is "pushed" (=selected), which may be
	 * null indicating that no image should be displayed.
	 * <p>
	 * Note: This operation is only available if the SWT.TOGGLE flag is set.
	 * </p>
	 *
	 * @param pushImage the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setPushImage(final Image pushImage) {
		checkWidget();
		this.pushImage = pushImage;
	}

	/**
	 * Sets the receiver's image to the argument when the mouse is hover the widget, which may be
	 * null indicating that no image should be displayed.
	 * <p>
	 * Note: This operation is only available if at least one the SWT.CHECK,
	 * SWT.TOGGLE and SWT.CLOSE flag is set.
	 * </p>
	 *
	 * @param hoverImage the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setHoverImage(final Image hoverImage) {
		checkWidget();
		this.hoverImage = hoverImage;
	}

	/**
	 * Sets the selection state of the receiver, if it is of type <code>CHECK</code> or
	 * <code>PUSH</code>.
	 *
	 * <p>
	 * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
	 * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
	 * it is selected when it is pushed in.
	 *
	 * @param selected the new selection state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(final boolean selected) {
		checkWidget();
		selection = selected;
		if (isCheck) {
			getParent().layout(new Control[] { this });
		}
		redraw();
	}

}
