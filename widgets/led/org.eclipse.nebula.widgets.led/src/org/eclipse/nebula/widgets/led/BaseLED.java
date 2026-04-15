/*******************************************************************************
 * Copyright (c) 2019 Akuiteo (http://www.akuiteo.com).
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 * Frank DELPORTE - inspiration for his LED Number Display JavaFX widget
 * (https://webtechie.be/2019/10/02/led-number-display-javafx-library-published-to-maven)
 *******************************************************************************/
package org.eclipse.nebula.widgets.led;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Abstract class for LED and Double dots LED.
 */
public abstract class BaseLED extends Canvas {

	static final int DEFAULT_HEIGHT = 100;
	static final int THIN_DEFAULT_WIDTH = 30;
	protected static final int DOT_DIAMETER = 10;

	protected Color idleColor;
	protected Color selectedColor;
	protected GC gc;

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
	public BaseLED(Composite parent, int style) {
		super(parent, style );
		setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		addListener(SWT.Paint, e -> onPaint(e));

		Color defaultIdleColor = new Color(0, 29, 29);
		SWTGraphicUtil.addDisposer(this, defaultIdleColor);
		idleColor = defaultIdleColor;

		Color defaultSelectedColor = new Color(148, 237, 147);
		SWTGraphicUtil.addDisposer(this, defaultSelectedColor);
		selectedColor = defaultSelectedColor;
	}



	private  void onPaint(Event e) {
		gc = e.gc;
		gc.setBackground(getBackground());
		gc.fillRectangle(getClientArea());
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		paintInternal();
	}

	/**
	 * Paint the widget
	 */
	protected abstract void paintInternal();

	/**
	 * Returns the color used when the line is "off".
	 *
	 * @return the color used when the line is "off"
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getIdleColor() {
		checkWidget();
		return idleColor;
	}

	/**
	 * Sets the color used by the widget to display lines when they are "off"
	 *
	 * @param idleColor
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
	public void setIdleColor(Color idleColor) {
		checkWidget();
		checkColor(idleColor);
		this.idleColor = idleColor;
		redraw();
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
	 * Returns the color used when the line is "on".
	 *
	 * @return the color used when the line is "on"
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
	 * Sets the color used by the widget to display lines when they are "on"
	 *
	 * @param selectedColor
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
	public void setSelectedColor(Color selectedColor) {
		checkWidget();
		checkColor(selectedColor);
		this.selectedColor = selectedColor;
		redraw();
	}
}
