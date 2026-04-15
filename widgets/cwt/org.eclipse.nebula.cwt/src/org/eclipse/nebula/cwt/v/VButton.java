/****************************************************************************
 * Copyright (c) 2008, 2009 Jeremy Dowdall
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jeremy Dowdall <jeremyd@aspencloud.com> - initial API and implementation
 *****************************************************************************/

package org.eclipse.nebula.cwt.v;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Event;

/**
 * Control to mimic a button.
 */
public class VButton extends VControl {

	ImageData oldImageData;
	boolean paintNative = false;
	boolean paintInactive = false;
	private boolean armed = false;

	Color hoverBackgroundColor, defaultHoverBackgroundColor;
	Color hoverBorderColor, defaultHoverBorderColor;

	Color selectedBackgroundColor, defaultSelectedBackgroundColor;
	Color selectedBorderColor, defaultSelectedBorderColor;

	/**
	 * Constructor
	 * @param panel parent panel
	 * @param style button style
	 */
	public VButton(VPanel panel, int style) {
		super(panel, style);
		if (!hasStyle(SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) {
			setStyle(SWT.PUSH, true);
		}
		setPainter(new VButtonPainter());
		addListener(SWT.MouseDown);
		addListener(SWT.MouseUp);

		defaultSelectedBorderColor = new Color(1, 85, 153);
		defaultSelectedBackgroundColor = new Color(204, 228, 247);
		defaultHoverBorderColor = new Color(1, 121, 215);
		defaultHoverBackgroundColor = new Color(229, 241, 251);
	}

	public void dispose() {
		super.dispose();
		defaultHoverBackgroundColor.dispose();
		defaultHoverBorderColor.dispose();
		defaultSelectedBackgroundColor.dispose();
		defaultSelectedBorderColor.dispose();
	}

	/**
	 * @return the paintNative flag
	 * @see #setPaintNative(boolean)
	 */
	public boolean getNativeBackground() {
		return paintNative;
	}

	/**
	 * @return true if this button is currently selected.
	 */
	public boolean getSelection() {
		return hasState(STATE_SELECTED);
	}

	@Override
	public Type getType() {
		return VControl.Type.Button;
	}

	protected void filterEvent(Event event) {
		if (hasState(STATE_ACTIVE)) {
			switch (event.type) {
			case SWT.MouseDown:
				setFocus();

				if (event.button != 1) {
					break;
				}

				if (hasStyle(SWT.PUSH)) {
					if (setState(STATE_SELECTED, true)) {
						redraw();
					}
					armed = true;
				} else {
					setState(STATE_SELECTED, !hasState(STATE_SELECTED));
					Event e = createEvent(event);
					notifyListeners(SWT.Selection, e);
					redraw();
				}
				break;

			case SWT.MouseUp:
				if (event.button != 1) {
					break;
				}

				if (hasStyle(SWT.PUSH)) {
					if (setState(STATE_SELECTED, false)) {
						redraw();
					}
				}
				if (armed) {
					Event e = createEvent(event);
					notifyListeners(SWT.Selection, e);
				}
				armed = false;
				break;
			}
		}
	}

	private Event createEvent(Event event) {
		Event e = new Event();
		e.type = SWT.Selection;
		e.data = VButton.this;
		e.button = event.button;
		e.detail = event.detail;
		e.display = event.display;
		e.gc = event.gc;
		e.height = event.height;
		e.stateMask = event.stateMask;
		e.time = event.time;
		e.width = event.width;
		e.x = event.x;
		e.y = event.y;
		return e;
	}

	/**
	 * @param paintInactive
	 *            true to paint the button in an inactive state.
	 */
	public void setPaintInactive(boolean paintInactive) {
		this.paintInactive = paintInactive;
	}

	/**
	 * Setting this value currently does nothing but subclasses can take
	 * advantage of this flag.
	 * <p/>
	 * Painting native is the act of letting the underlying OS take care of
	 * painting the background in some way or another.
	 * 
	 * @param paintNative
	 *            true to paint native
	 */
	public void setPaintNative(boolean paintNative) {
		this.paintNative = paintNative;
	}

	/**
	 * Marks this button as being "selected" by the user.
	 * 
	 * @param select
	 *            true to make the button "selected".
	 */
	public void setSelection(boolean select) {
		if (!hasStyle(SWT.PUSH)) {
			if (setState(STATE_SELECTED, select)) {
				redraw();
			}
		}
	}

	/**
	 * Set the background color when the mouse is hover the button
	 * @param hoverBackgroundColor new color
	 */
	public void setHoverBackgroundColor(Color hoverBackgroundColor) {
		this.hoverBackgroundColor = hoverBackgroundColor;
	}

	/**
	 * border color when the mouse is hover the button
	 * @param hoverBorderColor new color
	 */
	public void setHoverBorderColor(Color hoverBorderColor) {
		this.hoverBorderColor = hoverBorderColor;
	}

	/**
	 * background color when the button is selected
	 * @param selectedBackgroundColor new color
	 */
	public void setSelectedBackgroundColor(Color selectedBackgroundColor) {
		this.selectedBackgroundColor = selectedBackgroundColor;
	}

	/**
	 * background color when the button is selected
	 * @param selectedBorderColor new color
	 */
	public void setSelectedBorderColor(Color selectedBorderColor) {
		this.selectedBorderColor = selectedBorderColor;
	}

	
	
}