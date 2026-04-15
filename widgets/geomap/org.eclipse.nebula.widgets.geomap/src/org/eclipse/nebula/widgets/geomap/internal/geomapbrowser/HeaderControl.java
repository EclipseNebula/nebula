/*******************************************************************************
 * Copyright (c) 2008, 2012 Stepan Rutz.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Stepan Rutz - initial implementation
 *******************************************************************************/

package org.eclipse.nebula.widgets.geomap.internal.geomapbrowser;

import org.eclipse.nebula.widgets.geomap.GeoMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * A simple custom-drawn swt control that renders a header (eg some type of
 * prominent label.
 *
 * <p>
 * This file is part of the showcase for the {@link GeoMap} but does not include
 * any core functionality that is typically embedded. Users who only want to
 * embed the swt-map as a widget don't typically use this class.
 * </p>
 *
 * @author stepan.rutz
 * @version $Revision$
 */
public class HeaderControl extends Canvas {

	private static final int TOP_SPACE = 6;
	private Font font;
	private Color foreground;
	private String text = "";
	private Point size = new Point(1, 1);

	public HeaderControl(Composite parent) {
		super(parent, SWT.DOUBLE_BUFFERED);
		addPaintListener(e -> onPaint(e));
		addDisposeListener(e -> onDispose(e));

		font = new Font(getDisplay(), "Tahoma", 10, SWT.BOLD);
		foreground = new Color(87, 166, 212);
		measureString("M");
	}

	private void measureString(String s) {
		GC gc = new GC(this);
		try {
			gc.setFont(font);
			size = gc.stringExtent(s);
			size.y += TOP_SPACE;
		} finally {
			gc.dispose();
		}
	}

	private void onDispose(DisposeEvent e) {
		font.dispose();
		foreground.dispose();
	}

	private void onPaint(PaintEvent e) {
		GC gc = e.gc;
		gc.setFont(font);
		gc.setForeground(foreground);
		gc.drawString(text, 0, TOP_SPACE, true);
		Point size = getSize();
		int w = size.x;
		int h = size.y;
		gc.drawLine(0, h - 1, w, h - 1);
	}

	public String getText() {
		return text;
	}

	/**
	 * Sets the texts and recomputes the size of the receiver. Finally a redraw
	 * is scheduled.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
		measureString(text);
		redraw();
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(size.x, size.y);
	}

}
