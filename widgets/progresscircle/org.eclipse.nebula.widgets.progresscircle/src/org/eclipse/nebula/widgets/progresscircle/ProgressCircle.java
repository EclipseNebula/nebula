/*******************************************************************************
 * Copyright (c) 2018 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API
 * and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.progresscircle;

import java.time.LocalTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class represents a progress circle (also known as <i>donuts</i>), which represents a ratio.
 * It can also represent a remaining time, like an egg cooker.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 */
public class ProgressCircle extends Canvas {
	public static final String PERCENTAGE_PATTERN = "%d%%";
	public static final String INTEGER_PATTERN = "%d";

	private int value;
	private int circleSize;
	private int thickness;
	private boolean showText;
	private Color highlightColor;
	private static int MARGIN = 2;
	private boolean firstDisplay = true;
	private int maximum;
	private int minimum;
	private String textPattern = PERCENTAGE_PATTERN;
	private float floatValue;
	private boolean isTimer;
	private int fDelay;

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
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *                subclass</li>
	 *                </ul>
	 *
	 * @see Widget#getStyle()
	 */
	public ProgressCircle(Composite parent, int style) {
		super(parent, checkStyle(style) | SWT.DOUBLE_BUFFERED);
		value = 0;
		minimum = 0;
		maximum = 100;
		circleSize = 100;
		thickness = 10;
		highlightColor = getAndDisposeColor(119, 167, 251);
		setForeground(getAndDisposeColor(232, 232, 232));
		setFont(createDefaultFont());
		addPaintListener(e -> {
			paintControl(e);
		});
	}

	private static int checkStyle(final int style) {
		if ((style & SWT.BORDER) != 0) {
			return SWT.BORDER;
		}
		return 0;
	}

	private Color getAndDisposeColor(int r, int g, int b) {
		final Color color = new Color(r, g, b);
		addDisposeListener(e -> {
			if (!color.isDisposed()) {
				color.dispose();
			}
		});
		return color;
	}

	private Font createDefaultFont() {
		final FontData fontData = getFont().getFontData()[0];
		final Font newFont = new Font(getDisplay(), fontData.getName(), Math.max(fontData.getHeight(), 20), fontData.getStyle());
		addDisposeListener(e -> {
			if (!newFont.isDisposed()) {
				newFont.dispose();
			}
		});
		return newFont;
	}

	private void paintControl(final PaintEvent e) {
		firstDisplay = false;
		final GC gc = e.gc;
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);

		// Draw the selected part
		final Path pathHighlight = new Path(getDisplay());
		float ratio = 1.0f * value / (maximum - minimum);
		if (minimum < 0 && maximum < 0) {
			ratio = -1.0f * (minimum - value) / (maximum - minimum);
		}

		float angle = ratio * 360f;
		if (minimum < 0 && maximum > 0) {
			angle += 180;
		}

		pathHighlight.addArc(MARGIN, MARGIN, circleSize, circleSize, 90, -angle);
		pathHighlight.lineTo((MARGIN + circleSize) / 2, (MARGIN + circleSize) / 2);
		pathHighlight.close();

		gc.setBackground(getHighlightColor());
		gc.fillPath(pathHighlight);
		pathHighlight.dispose();

		// Draw the unselected part
		final Path path = new Path(getDisplay());
		final float unselectedAngle = 360f - angle;

		path.addArc(MARGIN, MARGIN, circleSize, circleSize, 90 - angle, -unselectedAngle);
		path.lineTo((MARGIN + circleSize) / 2, (MARGIN + circleSize) / 2);
		path.close();

		gc.setBackground(getForeground());
		gc.fillPath(path);
		pathHighlight.dispose();

		// Draw the hole
		gc.setBackground(getBackground());
		gc.fillOval(MARGIN + thickness, MARGIN + thickness, circleSize - thickness * 2, circleSize - thickness * 2);

		if (showText) {
			gc.setForeground(getHighlightColor());
			final String text;
			if (isTimer) {
				final LocalTime time = LocalTime.ofSecondOfDay(value);
				if (time.getHour() == 0) {
					if (time.getMinute() == 0) {
						// Seconds only
						text = String.format("%02d", time.getSecond());
					} else {
						// Minutes+secondes
						text = String.format("%02d:%02d", time.getMinute(), time.getSecond());
					}
				} else {
					// Hour/Min/sec
					text = String.format("%02d:%02d:%02d", time.getHour(), time.getMinute(), time.getSecond());
				}
			} else {
				text = String.format(textPattern, value);
			}
			final Point textSize = gc.stringExtent(text);
			final int x = MARGIN + (circleSize - textSize.x) / 2;
			final int y = (circleSize - textSize.y) / 2;
			gc.drawText(text, x, y, true);
			path.dispose();
		}
	}

	// ----------------------------- Getters and Setters

	/**
	 * Returns the maximum value which the receiver will allow.
	 *
	 * @return the maximum
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int getMaximum() {
		checkWidget();
		return maximum;
	}

	/**
	 * Sets the maximum value that the receiver will allow.
	 *
	 * @param value the new maximum
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - If maximum is lower than the minimum value</li>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setMaximum(int maximum) {
		checkWidget();
		if (maximum < minimum) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, null, String.format("The value %d is lower than the minimum (%d)", maximum, minimum));
		}
		this.maximum = maximum;
		redraw();
		update();
	}

	/**
	 * Returns the minimum value which the receiver will allow.
	 *
	 * @return the minimum
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int getMinimum() {
		checkWidget();
		return minimum;
	}

	/**
	 * Sets the minimum value that the receiver will allow.
	 *
	 * @param value the new minimum
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - If minimum is greater than the maximum value</li>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setMinimum(int minimum) {
		checkWidget();
		if (minimum > maximum) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, null, String.format("The value %d is greater than the maximum (%d)", minimum, maximum));
		}
		this.minimum = minimum;
		redraw();
		update();
	}

	/**
	 * @return selection value
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getSelection() {
		checkWidget();
		return value;
	}

	/**
	 * Set the selection value of this widget
	 *
	 * @param value the new percentage value
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(int selection) {
		checkWidget();
		if (selection < minimum || selection > maximum) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, null, String.format("Value %d is out of range [%d - %d]", selection, minimum, maximum));
		}
		final int previousValue = value;
		value = selection;
		if (firstDisplay || previousValue == selection) {
			return;
		}
		startAnimation(previousValue);
	}

	private void startAnimation(int startValue) {
		final int endValue = value;
		final float delta = (endValue - startValue) / 10f;
		floatValue = 1.0f * startValue;
		boolean animate = true;

		while (animate) {
			floatValue = floatValue + delta;
			value = (int) floatValue;
			if (isDisposed()) {
				return;
			}
			redraw();
			flush();

			if (delta > 0 && value >= endValue || delta < 0 && value <= endValue) {
				value = endValue;
				redraw();
				flush();
				animate = false;
			}
			else {
				try {
					Thread.sleep(fDelay);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Sets the delay in animation time in milliseconds. The higher the delay the
	 * slower the animation. The default is 10 ms.
	 * 
	 * @param delay the new delay
	 * @return this object
	 */
	public ProgressCircle setAnimationDelay(int delay) {
		fDelay = delay;
		return this;
	}

	private void flush() {
		while (getDisplay().readAndDispatch()) {
			// flush the event queue to keep the animation going
		}
	}

	/**
	 * @return the size of the circle
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getCircleSize() {
		checkWidget();
		return circleSize;
	}

	/**
	 * Set the size of the circle
	 *
	 * @param circleSize the new size of the circle
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setCircleSize(int circleSize) {
		checkWidget();
		if (this.circleSize == circleSize) {
			return;
		}
		this.circleSize = circleSize;
		getParent().layout(new Control[] { this });
	}

	/**
	 * @return the thickness of the circle
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getThickness() {
		checkWidget();
		return thickness;
	}

	/**
	 * Set the thickness value
	 *
	 * @param thickness the new value
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setThickness(int thickness) {
		checkWidget();
		this.thickness = thickness;
		redraw();
		update();
	}

	/**
	 * @return <code>true</code> if the text is displayed, false otherwise
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean isShowText() {
		checkWidget();
		return showText;
	}

	/**
	 * Displays or not the text
	 *
	 * @param showPercentage if <code>true</code>, displays the percentage label
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the display mode is not percentage</li>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setShowText(boolean showPercentage) {
		checkWidget();
		showText = showPercentage;
		redraw();
		update();
	}

	/**
	 * @return the highlight color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getHighlightColor() {
		checkWidget();
		return highlightColor;
	}

	/**
	 * Set the highlight color of the circle
	 *
	 * @param highlightColor the new highlight color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setHighlightColor(Color highlightColor) {
		checkWidget();
		this.highlightColor = highlightColor;
		redraw();
		update();
	}

	/**
	 * @return the text pattern used to display the value
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getTextPattern() {
		checkWidget();
		return textPattern;
	}

	/**
	 * Set the pattern used to display the value
	 *
	 * @param textPattern the new text pattern used to display the value
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parameter is NULL</li>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTextPattern(String textPattern) {
		checkWidget();
		if (textPattern == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.textPattern = textPattern;
	}

	public void startCountDown(LocalTime startTime) {
		checkWidget();
		setMinimum(0);
		final int numberOfSeconds = startTime.getHour() * 3600 + //
				startTime.getMinute() * 60 + //
				startTime.getSecond();
		minimum = 0;
		maximum = numberOfSeconds;
		value = numberOfSeconds;
		isTimer = true;
		redraw();
		update();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (isDisposed()) {
					return;
				}
				value--;
				redraw();
				update();
				if (value != 0) {
					getDisplay().timerExec(1000, this);
				}
			}
		};
		getDisplay().timerExec(1000, runnable);
	}

	// ----------- Overriden methods

	/**
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(Math.max(circleSize + 2 * MARGIN, wHint), Math.max(circleSize + 2 * MARGIN, hHint));
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setForeground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
		redraw();
		update();
	}

	/**
	 * @see org.eclipse.swt.widgets.Canvas#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		redraw();
		update();
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		redraw();
		update();
	}

}
