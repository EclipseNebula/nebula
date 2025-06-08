/*******************************************************************************
 * Copyright (c) 2012 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.roundedtoolbar;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.nebula.widgets.opal.commons.AdvancedPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class represent a selectable user interface object that
 * represents a button in a rounded tool bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CHECK, PUSH, RADIO, TOGGLE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, PUSH, RADIO, TOGGLE and DROP_DOWN
 * may be specified.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem
 *      snippets</a>
 */
public class RoundedToolItem extends Item {

	private static final int MARGIN = 4;
	private final RoundedToolbar parentToolbar;
	private final List<SelectionListener> selectionListeners;
	private Rectangle bounds;
	private boolean enabled;
	private boolean selection;
	private int width;
	private int height;
	private Image disabledImage;
	private Image selectionImage;
	private int alignment;
	private int verticalAlignment;
	private Color textColorSelected;
	private Color textColor;
	private String tooltipText;
	private GC gc;
	private int toolbarHeight;
	private boolean isLast;
	private final boolean hideSelection;
	IMenuCreator creator;

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>ToolBar</code>) and a style value describing its behavior and
	 * appearance. The item is added to the end of the items maintained by its
	 * parent.
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
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 *
	 * @see Widget#getStyle
	 */
	public RoundedToolItem(final RoundedToolbar parent) {
		this(parent, SWT.PUSH);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>ToolBar</code>) and a style value describing its behavior and
	 * appearance. The item is added to the end of the items maintained by its
	 * parent.
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
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 *
	 * @see Widget#getStyle
	 */
	public RoundedToolItem(final RoundedToolbar parent, final int style) {
		super(parent, checkStyle(style));
		parent.addItem(this);
		parentToolbar = parent;
		textColor = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		textColorSelected = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		enabled = true;
		alignment = SWT.CENTER;
		verticalAlignment = SWT.CENTER;
		selectionListeners = new CopyOnWriteArrayList<>();
		width = -1;
		height = -1;
		hideSelection = (parent.getStyle() & SWT.HIDE_SELECTION) == SWT.HIDE_SELECTION;
	}

	private static int checkStyle(final int style) {
		if ((style & SWT.CHECK) != 0) {
			return SWT.CHECK;
		}
		if ((style & SWT.RADIO) != 0) {
			return SWT.RADIO;
		}
		if ((style & SWT.TOGGLE) != 0) {
			return SWT.TOGGLE;
		}
		if ((style & SWT.DROP_DOWN) != 0) {
			return SWT.DROP_DOWN;
		}
		return SWT.PUSH;
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#addListener(int, org.eclipse.swt.widgets.Listener)
	 */
	@Override
	public void addListener(int eventType, Listener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		if (eventType == SWT.Selection) {
			addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					final Event event = new Event();
					event.widget = RoundedToolItem.this;
					event.display = getDisplay();
					event.item = RoundedToolItem.this;
					event.type = SWT.Selection;
					listener.handleEvent(event);
				}

			});
			return;
		}
		super.addListener(eventType, listener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control is selected by the user, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
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
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		selectionListeners.add(listener);
	}

	/**
	 * @return the default size of the item
	 */
	Point computeDefaultSize() {
		final Point sizeOfTextAndImages = computeSizeOfTextAndImages();
		final int additionnalWidth = getAdditionnalWidth();
		return new Point(2 * MARGIN + sizeOfTextAndImages.x + additionnalWidth, 2 * MARGIN + sizeOfTextAndImages.y);
	}

	private Point computeSizeOfTextAndImages() {
		int width = 0, height = 0;
		final boolean textNotEmpty = getText() != null && !getText().equals("");

		if (textNotEmpty) {
			final GC gc = new GC(parentToolbar);
			final Point extent = gc.stringExtent(getText());
			gc.dispose();
			width += extent.x;
			height = extent.y;
		}

		final Point imageSize = new Point(-1, -1);
		computeImageSize(getImage(), imageSize);
		computeImageSize(selectionImage, imageSize);
		computeImageSize(disabledImage, imageSize);

		if (imageSize.x != -1) {
			width += imageSize.x;
			height = Math.max(imageSize.y, height);
			if (textNotEmpty) {
				width += MARGIN;
			}
		}
		return new Point(width, height);
	}

	private void computeImageSize(final Image image, final Point imageSize) {
		if (image == null) {
			return;
		}
		final Rectangle imageBounds = image.getBounds();
		imageSize.x = Math.max(imageBounds.width, imageSize.x);
		imageSize.y = Math.max(imageBounds.height, imageSize.y);
	}

	private int getAdditionnalWidth() {
		if (isCheckbox() || isRadio() && !hideSelection) {
			return 16;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		selectionListeners.clear();
		getParent().removeItem(this);
		bounds = null;
		disabledImage = null;
		selectionImage = null;
		textColor = null;
		textColorSelected = null;
		super.dispose();
	}

	void drawButton(final GC gc, final int x, final int toolbarHeight, final boolean isLast) {
		this.gc = gc;
		this.toolbarHeight = toolbarHeight;
		this.isLast = isLast;

		if (selection && (isToogleButon() || isPushButon() || isDropDown())) {
			drawBackground(x);
		}
		if (!isLast) {
			drawRightLine(x);
		}

		int xPosition = computeHorizontalPosition(x);
		if (isCheckbox()) {
			drawCheckBox(x + xPosition);
			xPosition += 16;
		}
		if (isRadio()) {
			if (hideSelection) {
				if (selection) {
					drawBackground(x);
				}
			} else {
				drawRadio(x + xPosition);
				xPosition += 16;
			}
		}

		xPosition += drawImage(x + xPosition);
		drawText(x + xPosition);

		int width = getWidth();
		if (isLast) {
			width = Math.max(getWidth(), getParent().getSize().x - x);
		}
		bounds = new Rectangle(x, 0, width, toolbarHeight);
	}

	private void drawBackground(final int x) {
		final AdvancedPath path = new AdvancedPath(getDisplay());
		final boolean isFirst = getParent().indexOf(this) == 0;

		int width = getWidth();
		if (isFirst) {
			path.addRoundRectangleStraightRight(x, 0, getWidth(), toolbarHeight, parentToolbar.getCornerRadius(), parentToolbar.getCornerRadius());
		} else if (isLast) {
			width = Math.max(getWidth(), getParent().getSize().x - x);
			path.addRoundRectangleStraightLeft(x, 0, width, toolbarHeight, parentToolbar.getCornerRadius(), parentToolbar.getCornerRadius());
		} else {
			path.addRectangle(x, 0, getWidth(), toolbarHeight);
		}

		gc.setClipping(path);

		gc.setForeground(parentToolbar.activeGradientColor.start());
		gc.setBackground(parentToolbar.activeGradientColor.end());
		gc.fillGradientRectangle(x, 0, width + parentToolbar.getCornerRadius(), toolbarHeight, true);

		gc.setClipping((Rectangle) null);
		path.dispose();
	}

	private void drawRightLine(final int x) {
		gc.setForeground(parentToolbar.borderColor);
		gc.drawLine(x + getWidth(), 0, x + getWidth(), toolbarHeight);
	}

	private int computeHorizontalPosition(final int x) {
		final int widthOfTextAndImage = computeSizeOfTextAndImages().x + getAdditionnalWidth();
		switch (alignment) {
			case SWT.CENTER:
				return (getWidth() - widthOfTextAndImage) / 2;
			case SWT.RIGHT:
				return getWidth() - widthOfTextAndImage - MARGIN;
			default:
				return MARGIN;
		}
	}

	private int computeVerticalPosition(final int height) {
		switch (verticalAlignment) {
			case SWT.CENTER:
				return (toolbarHeight - height) / 2;
			case SWT.TOP:
				return MARGIN;
			default:
				return toolbarHeight - height - MARGIN;
		}
	}

	void fireSelectionEvent() {
		final Event event = new Event();
		event.widget = parentToolbar;
		event.display = getDisplay();
		event.item = this;
		event.type = SWT.Selection;
		for (final SelectionListener selectionListener : selectionListeners) {
			selectionListener.widgetSelected(new SelectionEvent(event));
		}
	}

	private void drawCheckBox(int xPosition) {
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		final Point textSize = gc.stringExtent(getText());
		final int yPosition = computeVerticalPosition(textSize.y);
		gc.drawRectangle(xPosition, yPosition, 13, 13);

		if (!selection) {
			return;
		}
		gc.drawPolyline(new int[] { xPosition + 2, yPosition + 6, //
				xPosition + 5, yPosition + 9, //
				xPosition + 10, yPosition + 3 });
	}

	private void drawRadio(int xPosition) {
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		final Point textSize = gc.stringExtent(getText());
		final int yPosition = computeVerticalPosition(textSize.y);
		gc.drawOval(xPosition, yPosition, 13, 13);

		if (!selection) {
			return;
		}
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.fillOval(xPosition + 3, yPosition + 3, 8, 8);
	}

	private int drawImage(final int xPosition) {
		Image image;
		if (!isEnabled()) {
			image = disabledImage;
		} else if (selection && (isToogleButon() || isPushButon() || isDropDown())) {
			image = selectionImage;
		} else {
			image = getImage();
		}

		if (image == null) {
			return 0;
		}

		final int yPosition = computeVerticalPosition(image.getBounds().height);
		gc.drawImage(image, xPosition, yPosition);
		return image.getBounds().width + MARGIN;
	}

	private void drawText(final int xPosition) {
		gc.setFont(parentToolbar.getFont());
		if (selection && (isToogleButon() || isPushButon() || isDropDown())) {
			gc.setForeground(textColorSelected);
		} else {
			gc.setForeground(textColor);
		}

		final Point textSize = gc.stringExtent(getText());
		final int yPosition = computeVerticalPosition(textSize.y);

		gc.drawText(getText(), xPosition, yPosition, true);
	}

	/**
	 * Returns a value which describes the position of the text in the receiver.
	 * The value will be one of <code>LEFT</code>, <code>RIGHT</code> or
	 * <code>CENTER</code>.
	 *
	 * @return the alignment
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getAlignment() {
		checkWidget();
		return alignment;
	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative
	 * to its parent (or its display if its parent is null), unless the receiver
	 * is a shell. In this case, the location is relative to the display.
	 *
	 * @return the receiver's bounding rectangle
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Rectangle getBounds() {
		checkWidget();
		return bounds;
	}

	/**
	 * @return the image displayed when the button is disabled
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getDisabledImage() {
		checkWidget();
		return disabledImage;
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled, and
	 * <code>false</code> otherwise. A disabled control is typically not
	 * selectable from the user interface and draws with an inactive or "grayed"
	 * look.
	 *
	 * @return the receiver's enabled state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see #isEnabled
	 */
	public boolean getEnabled() {
		checkWidget();
		return enabled;
	}

	/**
	 * Returns the whole height of the widget.
	 *
	 * @return the receiver's height
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getHeight() {
		checkWidget();
		if (height == -1) {
			return computeDefaultSize().y;
		}
		return height;
	}

	/**
	 * Returns the receiver's parent, which must be a
	 * <code>RoundedToolBar</code>.
	 *
	 * @return the receiver's parent
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public RoundedToolbar getParent() {
		checkWidget();
		return parentToolbar;
	}

	/**
	 * Returns <code>true</code> if the receiver is selected, and false
	 * otherwise.
	 *
	 * @return the selection state
	 *
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
	 * @return the image displayed when the button is selected
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getSelectionImage() {
		checkWidget();
		return selectionImage;
	}

	/**
	 * Returns the color of the text when the button is enabled and not
	 * selected.
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
	 * Returns the color of the text when the button is not selected.
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

	public Color getTextColorSelected() {
		checkWidget();
		return textColorSelected;
	}

	/**
	 * Returns the receiver's tool tip text, or null if it has not been set.
	 *
	 * @return the receiver's tool tip text
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getTooltipText() {
		checkWidget();
		return tooltipText;
	}

	/**
	 * Returns a value which describes the position of the text in the receiver.
	 * The value will be one of <code>TOP</code>, <code>BOTTOM</code> or
	 * <code>CENTER</code>.
	 *
	 * @return the alignment
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getVerticalAlignment() {
		checkWidget();
		return verticalAlignment;
	}

	/**
	 * Returns the whole height of the widget.
	 *
	 * @return the receiver's height
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getWidth() {
		checkWidget();
		if (width == -1) {
			return computeDefaultSize().x;
		}
		return width;
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled, and
	 * <code>false</code> otherwise. A disabled control is typically not
	 * selectable from the user interface and draws with an inactive or "grayed"
	 * look.
	 *
	 * @return the receiver's enabled state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see #getEnabled
	 */
	public boolean isEnabled() {
		checkWidget();
		return enabled;
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
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		selectionListeners.remove(listener);
	}

	/**
	 * Controls how text will be displayed in the receiver. The argument should
	 * be one of <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
	 *
	 * @param alignment the new alignment
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setAlignment(final int alignment) {
		checkWidget();
		this.alignment = alignment;
	}

	/**
	 * Sets the receiver's size and location to the rectangular area specified
	 * by the argument. The <code>x</code> and <code>y</code> fields of the
	 * rectangle are relative to the receiver's parent (or its display if its
	 * parent is null).
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative
	 * number will cause that value to be set to zero instead.
	 * </p>
	 *
	 * @param rectangle the new bounds for the receiver
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBounds(final Rectangle rectangle) {
		checkWidget();
		if (bounds == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		bounds = new Rectangle(Math.max(0, rectangle.x), //
				Math.max(0, rectangle.y), //
				Math.max(0, rectangle.width), //
				Math.max(0, rectangle.height));

	}

	/**
	 * Sets the receiver's image to the argument when this is one is disabled,
	 * which may be null indicating that no image should be displayed.
	 *
	 * @param image the image to display on the receiver (may be null)
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
	public void setDisabledImage(final Image image) {
		checkWidget();
		disabledImage = image;
	}

	/**
	 * Enables the receiver if the argument is <code>true</code>, and disables
	 * it otherwise.
	 * <p>
	 * A disabled control is typically not selectable from the user interface
	 * and draws with an inactive or "grayed" look.
	 * </p>
	 *
	 * @param enabled the new enabled state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setEnabled(final boolean enabled) {
		checkWidget();
		this.enabled = enabled;
	}

	/**
	 * Sets the height of the receiver.
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative
	 * number will cause that value to be set to zero instead.
	 * </p>
	 *
	 * @param height the new width
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setHeight(final int height) {
		checkWidget();
		this.height = Math.max(height, 0);
	}

	/**
	 * Sets the selection state of the receiver.
	 *
	 * @param selected the new selection state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(final boolean selected) {
		checkWidget();
		if (isToogleButon() || isCheckbox() || isRadio() || !selected) {
			selection = selected;
		}
	}

	/**
	 * Sets the receiver's image to the argument when this one is selected,
	 * which may be null indicating that no image should be displayed.
	 *
	 * @param image the image to display on the receiver (may be null)
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
	public void setSelectionImage(final Image image) {
		checkWidget();
		selectionImage = image;
	}

	/**
	 * Sets the receiver's text color to the argument, which may be null
	 * indicating that no image should be displayed.
	 *
	 * @param textColor the text color to display on the receiver (may be null)
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
	public void setTextColor(final Color textColor) {
		checkWidget();
		this.textColor = textColor;
	}

	/**
	 * Sets the receiver's text color to the argument when this one is selected,
	 * which may be null indicating that no image should be displayed.
	 *
	 * @param textColor the text color to display on the receiver (may be null)
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

	public void setTextColorSelected(final Color textColor) {
		checkWidget();
		textColorSelected = textColor;
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which may be null
	 * indicating that the default tool tip for the control will be shown. For a
	 * control that has a default tool tip, such as the Tree control on Windows,
	 * setting the tool tip text to an empty string replaces the default,
	 * causing no tool tip text to be shown.
	 * <p>
	 * The mnemonic indicator (character '&amp;') is not displayed in a tool
	 * tip. To display a single '&amp;' in the tool tip, the character '&amp;'
	 * can be escaped by doubling it in the string.
	 * </p>
	 *
	 * @param string the new tool tip text (or null)
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTooltipText(final String string) {
		checkWidget();
		tooltipText = string == null ? "" : string;
	}

	/**
	 * Controls how text will be displayed in the receiver. The argument should
	 * be one of <code>TOP</code>, <code>BOTTOM</code> or <code>CENTER</code>.
	 *
	 * @param verticalAlignment the new alignment
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setVerticalAlignment(final int verticalAlignment) {
		checkWidget();
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * Sets the width of the receiver.
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative
	 * number will cause that value to be set to zero instead.
	 * </p>
	 *
	 * @param width the new width
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setWidth(final int width) {
		checkWidget();
		this.width = Math.max(0, width);
	}

	boolean isToogleButon() {
		return (getStyle() & SWT.TOGGLE) != 0;
	}

	boolean isPushButon() {
		return (getStyle() & SWT.PUSH) != 0;
	}

	boolean isCheckbox() {
		return (getStyle() & SWT.CHECK) != 0;
	}

	boolean isRadio() {
		return (getStyle() & SWT.RADIO) != 0;
	}

	boolean isDropDown() {
		return (getStyle() & SWT.DROP_DOWN) != 0;
	}

	void forceSelection(boolean newSelection) {
		selection = newSelection;
	}
	
	public void setMenuCreator(IMenuCreator creator) {

		this.creator = creator;
	}

}
