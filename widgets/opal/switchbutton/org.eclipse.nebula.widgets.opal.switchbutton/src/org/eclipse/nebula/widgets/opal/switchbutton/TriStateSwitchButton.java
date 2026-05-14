/*******************************************************************************
 * Copyright (c) 2026 Christoph Läubrich
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Christoph Läubrich - initial API and implementation
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
 * Instances of this class are tri-state switch buttons with two active states
 * and one off state.
 *
 * <p>
 * The button has three equal sections:
 * <ol>
 * <li><b>Left (FIRST)</b> – the first active state.</li>
 * <li><b>Center (OFF)</b> – the neutral/off state.</li>
 * <li><b>Right (SECOND)</b> – the second active state.</li>
 * </ol>
 * A sliding toggle knob indicates which state is currently active.
 * </p>
 *
 * <p>
 * <b>Click behavior:</b>
 * <ul>
 * <li>When the button is in {@link TriState#FIRST} or {@link TriState#SECOND},
 * any click switches it to {@link TriState#OFF}.</li>
 * <li>When the button is in {@link TriState#OFF}, clicking the left section
 * activates {@link TriState#FIRST} and clicking the right section activates
 * {@link TriState#SECOND}. Clicking the center section has no effect.</li>
 * </ul>
 * </p>
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 */
public class TriStateSwitchButton extends Canvas {

	/** Current tri-state selection. */
	private TriState state;

	/** Text displayed inside the first (left) section. Default: "A". */
	private String textForFirst;

	/** Text displayed inside the off (center) section. Default: "Off". */
	private String textForOff;

	/** Text displayed inside the second (right) section. Default: "B". */
	private String textForSecond;

	/** Label text displayed beside the button. Default: "". */
	private String text;

	/** If true, use round rectangles instead of sharp rectangles. Default: true. */
	private boolean round;

	/** If not null, draw a border around the whole widget. Default: null. */
	private Color borderColor;

	/** If not null, draw a glow ring around the toggle when hovered. Default: dark grey. */
	private Color focusColor;

	/** Foreground/background colors for the first (left, FIRST state) section. */
	private Color firstForegroundColor, firstBackgroundColor;

	/** Foreground/background colors for the off (center, OFF state) section. */
	private Color offForegroundColor, offBackgroundColor;

	/** Foreground/background colors for the second (right, SECOND state) section. */
	private Color secondForegroundColor, secondBackgroundColor;

	/** Colors for the sliding toggle knob. */
	private Color buttonBorderColor, buttonBackgroundColor1, buttonBackgroundColor2;

	/** Gap between the button body and the label text. Default: 5. */
	private int gap = 5;

	/** Horizontal padding inside each section. Default: 5. */
	private int insideMarginX = 5;

	/** Vertical padding inside the button. Default: 5. */
	private int insideMarginY = 5;

	/** Arc radius for rounded rectangles. Default: 3. */
	private int arc = 3;

	/** Current paint GC, set during {@link #onPaint(PaintEvent)}. */
	private GC gc;

	/** True when the mouse is inside the widget. */
	private boolean mouseInside;

	/**
	 * Cached button body size from the most recent paint, used for click
	 * hit-testing. May be null before the first paint.
	 */
	private Point lastButtonSize;

	/**
	 * Constructs a new tri-state switch button.
	 *
	 * @param parent the parent composite (cannot be null)
	 * @param style  the SWT style bits
	 *
	 * @exception IllegalArgumentException if parent is null
	 * @exception SWTException             if called from a wrong thread
	 */
	public TriStateSwitchButton(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);

		state = TriState.OFF;
		text = "";
		textForFirst = "A";
		textForOff = "Off";
		textForSecond = "B";
		round = true;
		borderColor = null;
		focusColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);

		firstForegroundColor = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		firstBackgroundColor = SWTGraphicUtil.getDefaultColor(this, 0, 112, 195);

		offForegroundColor = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		offBackgroundColor = SWTGraphicUtil.getDefaultColor(this, 203, 203, 203);

		secondForegroundColor = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		secondBackgroundColor = SWTGraphicUtil.getDefaultColor(this, 0, 150, 50);

		buttonBorderColor = SWTGraphicUtil.getDefaultColor(this, 96, 96, 96);
		buttonBackgroundColor1 = SWTGraphicUtil.getDefaultColor(this, 254, 254, 254);
		buttonBackgroundColor2 = SWTGraphicUtil.getDefaultColor(this, 192, 192, 192);

		addPaintListener(this::onPaint);

		addListener(SWT.MouseUp, e -> {
			final TriState previousState = state;
			final TriState newState = computeNewState(e.x);
			if (newState == previousState) {
				return;
			}
			state = newState;
			if (SelectionListenerUtil.fireSelectionListeners(this, e)) {
				redraw();
			} else {
				state = previousState;
			}
		});

		mouseInside = false;
		addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(final MouseEvent e) {
				mouseInside = true;
				redraw();
			}

			@Override
			public void mouseExit(final MouseEvent e) {
				mouseInside = false;
				redraw();
			}

			@Override
			public void mouseEnter(final MouseEvent e) {
				mouseInside = true;
				redraw();
			}
		});
	}

	/**
	 * Computes the new state based on the current state and the click X coordinate.
	 *
	 * @param clickX the X coordinate of the mouse-up event
	 * @return the new state; equals the current state if no transition should occur
	 */
	private TriState computeNewState(final int clickX) {
		if (state != TriState.OFF) {
			// Any click when active → switch to OFF
			return TriState.OFF;
		}
		// Currently OFF: determine which section was clicked
		if (lastButtonSize == null) {
			return TriState.OFF;
		}
		final int sectionWidth = lastButtonSize.x / 3;
		// Button body spans x=[2, 2+buttonSize.x)
		if (clickX < 2 || clickX >= 2 + lastButtonSize.x) {
			return TriState.OFF;
		}
		final int relX = clickX - 2;
		if (relX < sectionWidth) {
			return TriState.FIRST;
		}
		if (relX >= 2 * sectionWidth) {
			return TriState.SECOND;
		}
		// Center section click while OFF → no change
		return TriState.OFF;
	}

	// -------------------------------------------------------------------------
	// Painting
	// -------------------------------------------------------------------------

	private void onPaint(final PaintEvent event) {
		final Rectangle rect = getClientArea();
		if (rect.width == 0 || rect.height == 0) {
			return;
		}
		gc = event.gc;
		gc.setAntialias(SWT.ON);

		final Point buttonSize = computeButtonSize();
		lastButtonSize = buttonSize;
		drawSwitchButton(buttonSize);
		drawText(buttonSize);

		if (borderColor != null) {
			drawBorder();
		}
	}

	/**
	 * Draws the three-section button body and the sliding toggle knob.
	 */
	private void drawSwitchButton(final Point buttonSize) {
		gc.setForeground(buttonBorderColor);
		if (round) {
			gc.drawRoundRectangle(2, 2, buttonSize.x, buttonSize.y, arc, arc);
		} else {
			gc.drawRectangle(2, 2, buttonSize.x, buttonSize.y);
		}

		final boolean enabled = isEnabled();
		drawSection(buttonSize, 0, textForFirst, firstForegroundColor, firstBackgroundColor, enabled);
		drawSection(buttonSize, 1, textForOff, offForegroundColor, offBackgroundColor, enabled);
		drawSection(buttonSize, 2, textForSecond, secondForegroundColor, secondBackgroundColor, enabled);

		gc.setClipping(getClientArea());
		drawToggleButton(buttonSize);
	}

	/**
	 * Draws one of the three sections of the button.
	 *
	 * @param buttonSize   total size of the button body
	 * @param sectionIndex 0 = left (FIRST), 1 = center (OFF), 2 = right (SECOND)
	 * @param label        the text label for this section
	 * @param fgColor      foreground (text) color when enabled
	 * @param bgColor      background color when enabled
	 * @param enabled      whether the widget is enabled
	 */
	private void drawSection(final Point buttonSize, final int sectionIndex, final String label,
			final Color fgColor, final Color bgColor, final boolean enabled) {
		final int sectionWidth = buttonSize.x / 3;
		final int sectionX = sectionIndex * sectionWidth;

		final Color bg;
		final Color fg;
		if (enabled) {
			bg = bgColor;
			fg = fgColor;
		} else {
			bg = gc.getDevice().getSystemColor(SWT.COLOR_TEXT_DISABLED_BACKGROUND);
			fg = gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND);
		}

		gc.setForeground(bg);
		gc.setBackground(bg);
		gc.setClipping(sectionX + 3, 3, sectionWidth, buttonSize.y - 1);
		if (round) {
			gc.fillRoundRectangle(2, 2, buttonSize.x, buttonSize.y, arc, arc);
		} else {
			gc.fillRectangle(2, 2, buttonSize.x, buttonSize.y);
		}

		gc.setForeground(fg);
		final Point textSize = gc.textExtent(label);
		gc.drawString(label, sectionX + (sectionWidth - textSize.x) / 2 + arc,
				(buttonSize.y - textSize.y) / 2 + arc);
	}

	/**
	 * Draws the gradient toggle knob over the currently active section.
	 */
	private void drawToggleButton(final Point buttonSize) {
		final int sectionWidth = buttonSize.x / 3;
		final int knobX;
		if (state == TriState.FIRST) {
			knobX = 2;
		} else if (state == TriState.SECOND) {
			knobX = 2 * sectionWidth + 2;
		} else {
			knobX = sectionWidth + 2;
		}

		gc.setForeground(buttonBackgroundColor1);
		gc.setBackground(buttonBackgroundColor2);
		gc.fillGradientRectangle(knobX, arc, sectionWidth, buttonSize.y - 1, true);

		gc.setForeground(buttonBorderColor);
		gc.drawRoundRectangle(knobX, 2, sectionWidth, buttonSize.y, arc, arc);

		if (focusColor != null && mouseInside) {
			gc.setForeground(focusColor);
			gc.setLineWidth(2);
			gc.drawRoundRectangle(knobX + 1, 3, sectionWidth - 2, buttonSize.y - 2, 3, 3);
			gc.setLineWidth(1);
		}
	}

	/**
	 * Computes the size of the button body (excluding the label text).
	 * Each section is equally wide: {@code sectionWidth = maxTextWidth + 2 * insideMarginX}.
	 */
	private Point computeButtonSize() {
		final Point sizeFirst = gc.stringExtent(textForFirst);
		final Point sizeOff = gc.stringExtent(textForOff);
		final Point sizeSecond = gc.stringExtent(textForSecond);

		final int maxWidth = Math.max(Math.max(sizeFirst.x, sizeOff.x), sizeSecond.x);
		final int maxHeight = Math.max(Math.max(sizeFirst.y, sizeOff.y), sizeSecond.y);

		final int sectionWidth = maxWidth + 2 * insideMarginX;
		final int width = sectionWidth * 3;
		final int height = maxHeight + 2 * insideMarginY;

		return new Point(width, height);
	}

	/**
	 * Draws the optional label text to the right of the button body.
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
	 * Draws the optional border around the entire widget.
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

	// -------------------------------------------------------------------------
	// Selection listeners
	// -------------------------------------------------------------------------

	/**
	 * Adds a selection listener that is notified when the state changes.
	 *
	 * @param listener the listener to add (cannot be null)
	 *
	 * @exception IllegalArgumentException if listener is null
	 * @exception SWTException             if the widget is disposed or called from a wrong thread
	 *
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addTypedListener(listener, SWT.Selection);
	}

	/**
	 * Removes a previously added selection listener.
	 *
	 * @param listener the listener to remove (cannot be null)
	 *
	 * @exception IllegalArgumentException if listener is null
	 * @exception SWTException             if the widget is disposed or called from a wrong thread
	 *
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		removeTypedListener(SWT.Selection, listener);
	}

	// -------------------------------------------------------------------------
	// Size
	// -------------------------------------------------------------------------

	/** @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean) */
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

	// -------------------------------------------------------------------------
	// State
	// -------------------------------------------------------------------------

	/**
	 * Returns the current tri-state of the button.
	 *
	 * @return the current {@link TriState}
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public TriState getState() {
		checkWidget();
		return state;
	}

	/**
	 * Sets the current tri-state of the button and redraws it.
	 *
	 * @param state the new state (cannot be null)
	 *
	 * @exception IllegalArgumentException if state is null
	 * @exception SWTException             if the widget is disposed or called from a wrong thread
	 */
	public void setState(final TriState state) {
		checkWidget();
		if (state == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.state = state;
		redraw();
	}

	// -------------------------------------------------------------------------
	// Text properties
	// -------------------------------------------------------------------------

	/**
	 * Returns the label text of the first (left) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public String getTextForFirst() {
		checkWidget();
		return textForFirst;
	}

	/**
	 * Sets the label text of the first (left) section.
	 *
	 * @param textForFirst the new text (cannot be null)
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setTextForFirst(final String textForFirst) {
		checkWidget();
		this.textForFirst = textForFirst;
		redraw();
	}

	/**
	 * Returns the label text of the off (center) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public String getTextForOff() {
		checkWidget();
		return textForOff;
	}

	/**
	 * Sets the label text of the off (center) section.
	 *
	 * @param textForOff the new text (cannot be null)
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setTextForOff(final String textForOff) {
		checkWidget();
		this.textForOff = textForOff;
		redraw();
	}

	/**
	 * Returns the label text of the second (right) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public String getTextForSecond() {
		checkWidget();
		return textForSecond;
	}

	/**
	 * Sets the label text of the second (right) section.
	 *
	 * @param textForSecond the new text (cannot be null)
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setTextForSecond(final String textForSecond) {
		checkWidget();
		this.textForSecond = textForSecond;
		redraw();
	}

	/**
	 * Returns the label text displayed beside the button.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public String getText() {
		checkWidget();
		return text;
	}

	/**
	 * Sets the label text displayed beside the button.
	 *
	 * @param text the new text (cannot be null)
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setText(final String text) {
		checkWidget();
		this.text = text;
		redraw();
	}

	// -------------------------------------------------------------------------
	// Visual style
	// -------------------------------------------------------------------------

	/**
	 * Returns whether the widget uses round rectangles.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public boolean isRound() {
		checkWidget();
		return round;
	}

	/**
	 * Sets whether the widget uses round rectangles.
	 *
	 * @param round if true, round rectangles are used; otherwise sharp rectangles
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setRound(final boolean round) {
		checkWidget();
		this.round = round;
		redraw();
	}

	/**
	 * Returns the border color. Null means no border is drawn.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getBorderColor() {
		checkWidget();
		return borderColor;
	}

	/**
	 * Sets the border color. Pass null to suppress the border.
	 *
	 * @param borderColor the new border color, or null
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setBorderColor(final Color borderColor) {
		checkWidget();
		this.borderColor = borderColor;
		redraw();
	}

	/**
	 * Returns the focus (hover glow) color. Null means no glow effect.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getFocusColor() {
		checkWidget();
		return focusColor;
	}

	/**
	 * Sets the focus (hover glow) color. Pass null to suppress the glow.
	 *
	 * @param focusColor the new focus color, or null
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setFocusColor(final Color focusColor) {
		checkWidget();
		this.focusColor = focusColor;
		redraw();
	}

	// -------------------------------------------------------------------------
	// Section colors – FIRST state
	// -------------------------------------------------------------------------

	/**
	 * Returns the foreground color of the first (left, FIRST) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getFirstForegroundColor() {
		checkWidget();
		return firstForegroundColor;
	}

	/**
	 * Sets the foreground color of the first (left, FIRST) section.
	 *
	 * @param firstForegroundColor the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setFirstForegroundColor(final Color firstForegroundColor) {
		checkWidget();
		this.firstForegroundColor = firstForegroundColor;
		redraw();
	}

	/**
	 * Returns the background color of the first (left, FIRST) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getFirstBackgroundColor() {
		checkWidget();
		return firstBackgroundColor;
	}

	/**
	 * Sets the background color of the first (left, FIRST) section.
	 *
	 * @param firstBackgroundColor the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setFirstBackgroundColor(final Color firstBackgroundColor) {
		checkWidget();
		this.firstBackgroundColor = firstBackgroundColor;
		redraw();
	}

	// -------------------------------------------------------------------------
	// Section colors – OFF state
	// -------------------------------------------------------------------------

	/**
	 * Returns the foreground color of the off (center, OFF) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getOffForegroundColor() {
		checkWidget();
		return offForegroundColor;
	}

	/**
	 * Sets the foreground color of the off (center, OFF) section.
	 *
	 * @param offForegroundColor the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setOffForegroundColor(final Color offForegroundColor) {
		checkWidget();
		this.offForegroundColor = offForegroundColor;
		redraw();
	}

	/**
	 * Returns the background color of the off (center, OFF) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getOffBackgroundColor() {
		checkWidget();
		return offBackgroundColor;
	}

	/**
	 * Sets the background color of the off (center, OFF) section.
	 *
	 * @param offBackgroundColor the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setOffBackgroundColor(final Color offBackgroundColor) {
		checkWidget();
		this.offBackgroundColor = offBackgroundColor;
		redraw();
	}

	// -------------------------------------------------------------------------
	// Section colors – SECOND state
	// -------------------------------------------------------------------------

	/**
	 * Returns the foreground color of the second (right, SECOND) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getSecondForegroundColor() {
		checkWidget();
		return secondForegroundColor;
	}

	/**
	 * Sets the foreground color of the second (right, SECOND) section.
	 *
	 * @param secondForegroundColor the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setSecondForegroundColor(final Color secondForegroundColor) {
		checkWidget();
		this.secondForegroundColor = secondForegroundColor;
		redraw();
	}

	/**
	 * Returns the background color of the second (right, SECOND) section.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getSecondBackgroundColor() {
		checkWidget();
		return secondBackgroundColor;
	}

	/**
	 * Sets the background color of the second (right, SECOND) section.
	 *
	 * @param secondBackgroundColor the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setSecondBackgroundColor(final Color secondBackgroundColor) {
		checkWidget();
		this.secondBackgroundColor = secondBackgroundColor;
		redraw();
	}

	// -------------------------------------------------------------------------
	// Toggle knob colors
	// -------------------------------------------------------------------------

	/**
	 * Returns the border color of the toggle knob.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getButtonBorderColor() {
		checkWidget();
		return buttonBorderColor;
	}

	/**
	 * Sets the border color of the toggle knob.
	 *
	 * @param buttonBorderColor the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setButtonBorderColor(final Color buttonBorderColor) {
		checkWidget();
		this.buttonBorderColor = buttonBorderColor;
		redraw();
	}

	/**
	 * Returns the first (top) gradient color of the toggle knob.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getButtonBackgroundColor1() {
		checkWidget();
		return buttonBackgroundColor1;
	}

	/**
	 * Sets the first (top) gradient color of the toggle knob.
	 *
	 * @param buttonBackgroundColor1 the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setButtonBackgroundColor1(final Color buttonBackgroundColor1) {
		checkWidget();
		this.buttonBackgroundColor1 = buttonBackgroundColor1;
		redraw();
	}

	/**
	 * Returns the second (bottom) gradient color of the toggle knob.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Color getButtonBackgroundColor2() {
		checkWidget();
		return buttonBackgroundColor2;
	}

	/**
	 * Sets the second (bottom) gradient color of the toggle knob.
	 *
	 * @param buttonBackgroundColor2 the new color
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setButtonBackgroundColor2(final Color buttonBackgroundColor2) {
		checkWidget();
		this.buttonBackgroundColor2 = buttonBackgroundColor2;
		redraw();
	}

	// -------------------------------------------------------------------------
	// Layout metrics
	// -------------------------------------------------------------------------

	/**
	 * Returns the gap between the button body and the label text.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public int getGap() {
		checkWidget();
		return gap;
	}

	/**
	 * Sets the gap between the button body and the label text.
	 *
	 * @param gap the new gap value in pixels
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setGap(final int gap) {
		checkWidget();
		this.gap = gap;
		redraw();
	}

	/**
	 * Returns the horizontal and vertical padding inside each section as a Point.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public Point getInsideMargin() {
		checkWidget();
		return new Point(insideMarginX, insideMarginY);
	}

	/**
	 * Sets the horizontal and vertical padding inside each section.
	 *
	 * @param insideMarginX horizontal margin in pixels
	 * @param insideMarginY vertical margin in pixels
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setInsideMargin(final int insideMarginX, final int insideMarginY) {
		checkWidget();
		this.insideMarginX = insideMarginX;
		this.insideMarginY = insideMarginY;
		redraw();
	}

	/**
	 * Sets the horizontal and vertical padding inside each section.
	 *
	 * @param insideMargin the new margin (x=horizontal, y=vertical)
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setInsideMargin(final Point insideMargin) {
		checkWidget();
		insideMarginX = insideMargin.x;
		insideMarginY = insideMargin.y;
		redraw();
	}

	/**
	 * Returns the arc radius used for rounded rectangles.
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public int getArc() {
		checkWidget();
		return arc;
	}

	/**
	 * Sets the arc radius used for rounded rectangles.
	 *
	 * @param arc the new arc radius in pixels
	 *
	 * @exception SWTException if the widget is disposed or called from a wrong thread
	 */
	public void setArc(final int arc) {
		checkWidget();
		this.arc = arc;
		redraw();
	}

}
