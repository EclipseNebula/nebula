/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON
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
package org.eclipse.nebula.widgets.opal.preferencewindow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class represent a flat button.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>UP, DOWN, LEFT, RIGHT, CENTER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles LEFT, RIGHT, and CENTER may be specified.
 * </p>
 *
 */
class FlatButton extends Canvas {
	private static final int DEFAULT_PADDING = 5;
	private Image image;
	private String text;
	private boolean selection;
	private int alignment;
	private final List<SelectionListener> listeners;
	private boolean mouseIn;
	private Color backgroundColor;
	private Color selectedColor;
	private Color selectedTextColor;
	private Color mouseOverColor;

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
	 * @see SWT#DOWN
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see SWT#CENTER
	 */
	public FlatButton(final Composite parent, final int style) {
		super(parent, style);
		listeners = new ArrayList<SelectionListener>();
		buildAlignmentFromStyle(style);
		addListeners();
		initializeDefaultColors();
	}

	private void buildAlignmentFromStyle(final int style) {
		if ((style & SWT.LEFT) == SWT.LEFT) {
			alignment = SWT.LEFT;
		} else if ((style & SWT.RIGHT) == SWT.RIGHT) {
			alignment = SWT.RIGHT;
		} else {
			alignment = SWT.CENTER;

		}
	}

	private void addListeners() {
		addPaintListener(e -> {
			FlatButton.this.paintControl(e);
		});

		addListener(SWT.MouseEnter, event -> {
			mouseIn = true;
			redraw();
		});

		addListener(SWT.MouseExit, event -> {
			mouseIn = false;
			redraw();
		});

		addListener(SWT.MouseUp, event -> {
			boolean doIt = true;
			selection = !selection;
			for (final SelectionListener listener : listeners) {
				final SelectionEvent sEvent = new SelectionEvent(event);
				listener.widgetSelected(sEvent);
				doIt = doIt && sEvent.doit;
			}
			if (!doIt) {
				selection = !selection;
			}
		});
	}

	private void initializeDefaultColors() {
		backgroundColor = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		selectedColor = new Color(0, 112, 192);
		selectedTextColor = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		mouseOverColor = new Color(235, 234, 226);

		SWTGraphicUtil.addDisposer(this, selectedColor);
		SWTGraphicUtil.addDisposer(this, mouseOverColor);
		SWTGraphicUtil.addDisposer(this, image);
	}

	private void paintControl(final PaintEvent e) {
		final GC gc = e.gc;
		drawBackground(gc);
		if (image != null) {
			drawImage(gc);
		}
		if (text != null) {
			drawText(gc);
		}
	}

	private void drawBackground(final GC gc) {
		Color color;
		if (selection) {
			color = selectedColor;
		} else if (mouseIn) {
			color = mouseOverColor;
		} else {
			color = backgroundColor;
		}
		gc.setBackground(color);
		gc.fillRectangle(getClientArea());

	}

	private void drawImage(final GC gc) {
		final Rectangle rect = getClientArea();
		final Point imageSize = new Point(image.getBounds().width, image.getBounds().height);

		int x;
		if (alignment == SWT.LEFT) {
			x = DEFAULT_PADDING;
		} else if (alignment == SWT.RIGHT) {
			x = rect.width - imageSize.x - DEFAULT_PADDING;
		} else {
			x = (rect.width - imageSize.x) / 2;
		}
		gc.drawImage(image, x, DEFAULT_PADDING);
	}

	private void drawText(final GC gc) {
		final Rectangle rect = getClientArea();

		if (selection) {
			gc.setForeground(selectedTextColor);
		} else {
			gc.setForeground(getForeground());
		}

		gc.setFont(getFont());
		final Point textSize = gc.stringExtent(text);
		int x, y;

		if (alignment == SWT.LEFT) {
			x = DEFAULT_PADDING;
		} else if (alignment == SWT.RIGHT) {
			x = rect.width - textSize.x - DEFAULT_PADDING;
		} else {
			x = (rect.width - textSize.x) / 2;
		}
		if (image == null) {
			y = DEFAULT_PADDING;
		} else {
			y = 2 * DEFAULT_PADDING + image.getBounds().height;
		}
		gc.drawString(text, x, y, true);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the control is selected by the user, by sending it one of the messages
	 * defined in the <code>SelectionListener</code> interface.
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
		checkWidget();
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		int width = 2 * DEFAULT_PADDING, height = 3 * DEFAULT_PADDING;
		if (image != null) {
			final Rectangle bounds = image.getBounds();
			width += bounds.width;
			height += bounds.height;
		}

		if (text != null) {
			final GC gc = new GC(this);
			final Point extent = gc.stringExtent(text);
			gc.dispose();
			width = Math.max(width, extent.x + 2 * DEFAULT_PADDING);
			height = height + extent.y;
		}

		return new Point(Math.max(width, wHint), Math.max(height, hHint));
	}

	/**
	 * Returns a value which describes the position of the text in the receiver. The
	 * value will be one of <code>LEFT</code>, <code>RIGHT</code> or
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
	 * Returns a value which describes the default background color
	 *
	 * @return the default background color
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
	 * Returns a value which describes the color when the mouse is over the button
	 *
	 * @return the color when the mouse is over the button
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getMouseOverColor() {
		checkWidget();
		return mouseOverColor;
	}

	/**
	 * Returns a value which describes the color when the button is selected
	 *
	 * @return the color when the button is selected
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getSelectedColor() {
		checkWidget();
		return selectedColor;
	}

	/**
	 * Returns a value which describes the color of the text when the button is
	 * selected
	 *
	 * @return the color of the text when the button is selected
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getSelectedTextColor() {
		return selectedTextColor;
	}

	/**
	 * Returns <code>true</code> if the receiver is selected, and false otherwise.
	 * <p>
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
	 * Returns the receiver's text.
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
		checkWidget();
		listeners.remove(listener);
	}

	/**
	 * Controls how text, images and arrows will be displayed in the receiver. The
	 * argument should be one of <code>LEFT</code>, <code>RIGHT</code> or
	 * <code>CENTER</code>.
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
		if ((alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)) == 0) {
			return;
		}
		this.alignment = alignment;
		redraw();
	}

	/**
	 * Sets the receiver's background color to the color specified by the argument.
	 *
	 * @param color the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
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
	public void setBackgroundColor(final Color backgroundColor) {
		checkWidget();
		this.backgroundColor = backgroundColor;
	}

	/**
	 * Sets the receiver's image to the argument, which may be <code>null</code>
	 * indicating that no image should be displayed.
	 * <p>
	 * Note that a Button can display an image and text simultaneously on Windows
	 * (starting with XP), GTK+ and OSX. On other platforms, a Button that has an
	 * image and text set into it will display the image or text that was set most
	 * recently.
	 * </p>
	 *
	 * @param image the image to display on the receiver (may be <code>null</code>)
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
	public void setImage(final Image image) {
		checkWidget();
		this.image = image;
		redraw();
	}

	/**
	 * Sets the receiver's color when the mouse if over the button.
	 *
	 * @param color the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
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
	public void setMouseOverColor(final Color mouseOverColor) {
		checkWidget();
		this.mouseOverColor = mouseOverColor;
	}

	/**
	 * Sets the receiver's color when the button is selected.
	 *
	 * @param color the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
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
	public void setSelectedColor(final Color selectedColor) {
		checkWidget();
		this.selectedColor = selectedColor;
	}

	/**
	 * Sets the receiver's text color when the button is selected.
	 *
	 * @param color the new color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
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
	public void setSelectedTextColor(final Color selectedTextColor) {
		this.selectedTextColor = selectedTextColor;
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
		selection = selected;
		redraw();
	}

	/**
	 * Sets the receiver's text.
	 *
	 *
	 * @param string the new text
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
	public void setText(final String text) {
		checkWidget();
		this.text = text;
		redraw();
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#addListener(int, org.eclipse.swt.widgets.Listener)
	 */
	@Override
	public void addListener(int eventType, Listener listener) {
		if (eventType == SWT.Selection) {
			addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					listener.handleEvent(new Event());
				}
			});
			return;
		}
		super.addListener(eventType, listener);
	}
}
