/*******************************************************************************
 * Copyright (c) 2019 Akuiteo (http://www.akuiteo.com).
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
package org.eclipse.nebula.widgets.badgedlabel;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Instances of this class represent a non-selectable user interface object that
 * displays a string or image. A badge is displayed on this label, so you can
 * add extra information (the most common used is a notification label that
 * shows the number of unread notifications).
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.BORDER</dd>
 * <dd>SWT.LEFT or SWT.RIGHT (horizontal location)</dd>
 * <dd>SWT.TOP or SWT.BOTTOM (vertical location)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 */
public class BadgedLabel extends Canvas {
	private static final int PADDING = 8;
	private static final int MARGIN = 7;
	private static final int CIRCLE_DIAMETER = 18;
	private static final int MAX_BADGE_TEXT_SIZE = 11;
	private Image image;
	private String text;
	private String badgeValue;
	private Color textColor, backgroundColor, borderColor, badgeForeground, badgeBackground;
	private final Font badgeFont;
	private Font boldFont;
	private int horizontalLocation, verticalLocation;
	private GC gc;
	private int left;
	private int top;
	private Point buttonSize;
	private Point textSizeCache;
	private Point badgeTextSizeCache;

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
	 * @param parent
	 *            a composite control which will be the parent of the new instance
	 *            (cannot be null)
	 * @param style
	 *            the style of control to construct
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
	public BadgedLabel(Composite parent, int style) {
		super(parent, checkStyle(style) | SWT.DOUBLE_BUFFERED);
		badgeFont = SWTGraphicUtil.buildFontFrom(this, SWT.NONE, 8);
		SWTGraphicUtil.addDisposer(this, badgeFont);
		boldFont = SWTGraphicUtil.buildFontFrom(this, SWT.BOLD);
		SWTGraphicUtil.addDisposer(this, boldFont);
		horizontalLocation = (getStyle() & SWT.LEFT) != 0 ? SWT.LEFT : SWT.RIGHT;
		verticalLocation = (getStyle() & SWT.TOP) != 0 ? SWT.TOP : SWT.BOTTOM;
		initDefaultColors();
		addListener(SWT.Paint, e -> onPaint(e));
	}

	private static int checkStyle(final int style) {
		final int mask = SWT.BORDER | SWT.LEFT | SWT.RIGHT | SWT.TOP | SWT.BOTTOM;
		int newStyle = style & mask;
		newStyle |= SWT.DOUBLE_BUFFERED;
		return newStyle;
	}

	private void initDefaultColors() {
		textColor = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		SWTGraphicUtil.addDisposer(this, textColor);

		backgroundColor = new Color(247, 247, 247);
		SWTGraphicUtil.addDisposer(this, backgroundColor);

		borderColor = new Color(204, 204, 204);
		SWTGraphicUtil.addDisposer(this, borderColor);

		badgeForeground = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		badgeBackground = new Color(0, 123, 255);
		SWTGraphicUtil.addDisposer(this, badgeBackground);
	}

	private void onPaint(Event e) {
		gc = e.gc;
		gc.setAntialias(SWT.ON);
		gc.setAdvanced(true);
		final Color previousForeground = getForeground();
		final Color previousBackground = getBackground();

		drawButton();
		drawBadge();

		gc.setForeground(previousForeground);
		gc.setBackground(previousBackground);
	}

	private void drawButton() {
		drawShape();
		drawImageAndText();
	}

	private void drawShape() {
		buttonSize = computeButtonSize();
		final Rectangle area = getClientArea();
		left = MARGIN;
		if (horizontalLocation == SWT.LEFT) {
			left += MARGIN;
		}

		top = MARGIN;
		if (verticalLocation == SWT.TOP) {
			top += MARGIN;
		}

		// Background
		gc.setBackground(backgroundColor);
		gc.fillRoundRectangle(left, top, //
				Math.max(buttonSize.x, area.width - 2 * MARGIN) - left, //
				Math.max(buttonSize.y, area.height - 2 * MARGIN) - top, 3, 3);

		// Foreground
		gc.setForeground(borderColor);
		gc.drawRoundRectangle(left, top, //
				Math.max(buttonSize.x, area.width - 2 * MARGIN) - left, //
				Math.max(buttonSize.y, area.height - 2 * MARGIN) - top, 3, 3);
	}

	private void drawImageAndText() {
		final int textWidth = getTextSizeInPixels().x;
		int textDelta = 0;
		final Rectangle area = getClientArea();
		final int width = Math.max(buttonSize.x, area.width - 2 * MARGIN) - left;
		final int height = Math.max(buttonSize.y, area.height - 2 * MARGIN) - top;
		if (image != null) {
			final Point imageSize = new Point(image.getBounds().width, image.getBounds().height);
			int wholeLength = imageSize.x;
			if (textWidth != 0) {
				wholeLength += PADDING / 2 + getTextSizeInPixels().x;
			}

			final int x = left + (width - wholeLength) / 2;
			gc.drawImage(image, x, top + (height - imageSize.y) / 2);
			textDelta = x + imageSize.x + PADDING / 2;
		}

		if (textWidth == 0) {
			return;
		}

		final int x = textDelta == 0 ? left + (width - textWidth) / 2 : textDelta;
		gc.setFont(boldFont);
		gc.setForeground(isEnabled() ? textColor : getDisplay().getSystemColor(SWT.COLOR_GRAY));
		gc.drawText(text, x, top + (height - getTextSizeInPixels().y) / 2, false);
	}

	private Point getTextSizeInPixels() {
		if (text == null || text.equals("")) {
			return new Point(0, 0);
		}

		if (textSizeCache != null) {
			return textSizeCache;
		}

		final GC gc = new GC(this);
		gc.setFont(boldFont);
		textSizeCache = gc.stringExtent(text);
		gc.dispose();
		return textSizeCache;
	}

	private void drawBadge() {
		if (badgeValue == null || badgeValue.equals("")) {
			return;
		}

		gc.setForeground(badgeForeground);
		gc.setBackground(badgeBackground);

		final Point textSize = getBadgeTextSizeInPixels();
		final Rectangle area = getClientArea();
		final int width = Math.max(buttonSize.x, area.width - 2 * MARGIN) - left;
		final int height = Math.max(buttonSize.y, area.height - 2 * MARGIN) - top;
		int badgeWith;
		if (textSize.x > MAX_BADGE_TEXT_SIZE) {
			// Draw a round rectangle
			badgeWith = textSize.x + MAX_BADGE_TEXT_SIZE / 2;
		} else {
			// Draw a circle
			badgeWith = CIRCLE_DIAMETER;
		}

		int x;
		switch (horizontalLocation) {
		case SWT.RIGHT:
			x = left + width - badgeWith / 2;
			break;
		case SWT.LEFT:
			x = left - badgeWith / 2;
			break;
		default:
			return;
		}

		int y;
		switch (verticalLocation) {
		case SWT.TOP:
			y = top - CIRCLE_DIAMETER / 2;
			break;
		case SWT.BOTTOM:
			y = top + height - CIRCLE_DIAMETER / 2;
			break;
		default:
			return;
		}
		if (textSize.x > MAX_BADGE_TEXT_SIZE) {
			// Draw a round rectangle
			gc.fillRoundRectangle(x, y, badgeWith, CIRCLE_DIAMETER, 3, 3);
		} else {
			// Draw a circle
			gc.fillOval(x, y, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
		}
		gc.setFont(badgeFont);
		gc.drawText(badgeValue, x + (badgeWith - textSize.x) / 2, y + (CIRCLE_DIAMETER - textSize.y) / 2, true);
	}

	private Point getBadgeTextSizeInPixels() {
		if (badgeTextSizeCache != null) {
			return badgeTextSizeCache;
		}
		final GC gc = new GC(this);
		gc.setFont(badgeFont);
		badgeTextSizeCache = gc.stringExtent(badgeValue);
		gc.dispose();
		return badgeTextSizeCache;

	}

	/**
	 * Sets the badge's color theme to the theme specified by the argument
	 *
	 * @param color
	 *            the new color, can pick one of the following value:
	 *            SWT.COLOR_BLUE, SWT.COLOR_GRAY, SWT.COLOR_GREEN, SWT.COLOR_RED,
	 *            SWT.COLOR_YELLOW, SWT.COLOR_CYAN, SWT.COLOR_BLACK
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setPredefinedColor(int color) {
		checkWidget();

		badgeForeground = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		switch (color) {
		case SWT.COLOR_BLUE:
			badgeBackground = new Color(0, 123, 255);
			break;
		case SWT.COLOR_GRAY:
			badgeBackground = new Color(108, 117, 125);
			break;
		case SWT.COLOR_GREEN:
			badgeBackground = new Color(40, 167, 69);
			break;
		case SWT.COLOR_RED:
			badgeBackground = new Color(220, 53, 69);
			break;
		case SWT.COLOR_YELLOW:
			badgeForeground = getDisplay().getSystemColor(SWT.COLOR_BLACK);
			badgeBackground = new Color(255, 193, 7);
			break;
		case SWT.COLOR_CYAN:
			badgeBackground = new Color(23, 162, 184);
			break;
		default: // BLACK
			badgeBackground = new Color(52, 58, 64);
		}

		SWTGraphicUtil.addDisposer(this, badgeBackground);
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		
		if (image == null && text == null) {
			return super.computeSize(wHint, hHint, changed);
		}
		
		// Button
		final Point buttonSize = computeButtonSize();
		int width = buttonSize.x;
		int height = buttonSize.y;

		// Margin
		width += 3 * MARGIN;
		height += 3 * MARGIN;
		return new Point(Math.max(width, wHint), Math.max(height, hHint));
	}

	private Point computeButtonSize() {
		int width = 2 * PADDING, height = 2 * PADDING;
		if (image != null && text == null) {
			final Rectangle bounds = image.getBounds();
			width += bounds.width;
			height = Math.max(height, bounds.height + 2 * PADDING);
		} else if (text != null && image == null) {
			final Point extent = getTextSizeInPixels();
			width = Math.max(width, extent.x + 2 * PADDING);
			height = height + extent.y;
		} else {
			final Rectangle bounds = image.getBounds();
			final Point extent = getTextSizeInPixels();
			width += bounds.width + PADDING + extent.x + 2 * PADDING;
			final int maxTextAndImageHeight = Math.max(extent.y, bounds.y);
			height = Math.max(height, maxTextAndImageHeight + 2 * PADDING);
		}
		return new Point(width, height);
	}

	/**
	 * Returns the background color that the receiver will use to draw.
	 *
	 * @return the receiver's background color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBackgroundColor() {
		checkWidget();
		return backgroundColor;
	}

	/**
	 * Returns the background color that the badge will use to draw.
	 *
	 * @return the badge's backdground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBadgeBackground() {
		checkWidget();
		return badgeBackground;
	}

	/**
	 * Returns the foreground color that the badge will use to draw.
	 *
	 * @return the badge's foreground color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBadgeForeground() {
		checkWidget();
		return badgeForeground;
	}

	/**
	 * Returns the text of the badge
	 *
	 * @return the badge's text
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getBadgeValue() {
		checkWidget();
		return badgeValue;
	}

	/**
	 * Returns the border color that the receiver will use to draw.
	 *
	 * @return the receiver's border color
	 *
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
	 * Returns the receiver's image if it has one, or null if it does not.
	 *
	 * @return the receiver's image
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getImage() {
		checkWidget();
		return image;
	}

	/**
	 * Returns the receiver's text, which will be an empty string if it has never
	 * been set or if the receiver is a <code>SEPARATOR</code> label.
	 *
	 * @return the receiver's text
	 *
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
	 * Returns the text color that the receiver will use to draw.
	 *
	 * @return the receiver's text color
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getTextColor() {
		checkWidget();
		return textColor;
	}

	/**
	 * Sets the receiver's background color to the color specified by the argument
	 *
	 * @param color
	 *            the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBackgroundColor(Color backgroundColor) {
		checkWidget();
		checkColor(backgroundColor);
		this.backgroundColor = backgroundColor;
	}

	private void checkColor(Color color) {
		if (color == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		if (color.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
	}

	/**
	 * Sets the badge's background color to the color specified by the argument
	 *
	 * @param color
	 *            the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBadgeBackground(Color badgeBackground) {
		checkWidget();
		checkColor(badgeBackground);
		this.badgeBackground = badgeBackground;
	}

	/**
	 * Sets the badge's foreground color to the color specified by the argument
	 *
	 * @param color
	 *            the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBadgeForeground(Color badgeForeground) {
		checkWidget();
		checkColor(badgeForeground);
		this.badgeForeground = badgeForeground;
	}

	/**
	 * Sets the badge's value (text) to the text specified by the argument
	 *
	 * @param value
	 *            the new text value
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBadgeValue(String value) {
		checkWidget();
		if (value == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		badgeValue = value;
		badgeTextSizeCache = null;
	}

	/**
	 * Sets the receiver's border color to the color specified by the argument
	 *
	 * @param color
	 *            the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBorderColor(Color borderColor) {
		checkWidget();
		checkColor(borderColor);
		this.borderColor = borderColor;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		redraw();
	}

	/**
	 * @see org.eclipse.swt.widgets.Canvas#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		textSizeCache = null;
		boldFont.dispose();
		boldFont = SWTGraphicUtil.buildFontFrom(this, SWT.BOLD);
	}

	/**
	 * Sets the receiver's image to the argument, which may be null indicating that
	 * no image should be displayed.
	 *
	 * @param image
	 *            the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setImage(Image image) {
		checkWidget();
		this.image = image;
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the widget label. The label may include the mnemonic
	 * character and line delimiters.
	 * </p>
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next character to be
	 * the mnemonic. When the user presses a key sequence that matches the mnemonic,
	 * focus is assigned to the control that follows the label. On most platforms,
	 * the mnemonic appears underlined but may be emphasised in a platform specific
	 * manner. The mnemonic indicator character '&amp;' can be escaped by doubling
	 * it in the string, causing a single '&amp;' to be displayed.
	 * </p>
	 * <p>
	 * Note: If control characters like '\n', '\t' etc. are used in the string, then
	 * the behavior is platform dependent.
	 * </p>
	 *
	 * @param string
	 *            the new text
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(String text) {
		checkWidget();
		textSizeCache = null;
		this.text = text;
	}

	/**
	 * Sets the receiver's text color to the color specified by the argument
	 *
	 * @param color
	 *            the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTextColor(Color textColor) {
		checkWidget();
		checkColor(textColor);
		this.textColor = textColor;
	}

}
