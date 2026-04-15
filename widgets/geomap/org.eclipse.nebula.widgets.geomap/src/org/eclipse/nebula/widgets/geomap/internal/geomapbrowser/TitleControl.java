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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * An owner-drawn title label type of swt-control.
 *
 * @author stepan.rutz
 * @version $Revision$
 */
public class TitleControl extends Canvas {

	private static final int TOP_SPACE = 0;
	private Font font;
	private Color gradient1Color;
	private Color gradient2Color;
	private Color bottomLineColor;
	private Color writingColor;
	private Image image;
	private String text = "";
	private Point size = new Point(1, 1);

	public TitleControl(Composite parent, Image image) {
		super(parent, SWT.DOUBLE_BUFFERED);
		addPaintListener(e -> onPaint(e));
		addDisposeListener(e -> onDispose(e));

		font = new Font(getDisplay(), "Tahoma", 10, SWT.BOLD);
		gradient1Color = new Color(255, 255, 255);
		gradient2Color = new Color(205, 224, 244);
		bottomLineColor = new Color(200, 195, 216);
		writingColor = new Color(60, 60, 60);
		this.image = image;

		measureSize("M");
	}

	private void measureSize(String s) {
		GC gc = new GC(this);
		try {
			gc.setFont(font);
			size = gc.stringExtent(s);
			if (image != null) {
				size.y = Math.max(image.getBounds().height + 1, size.y);
			} else {
				size.y += 16;
			}
			size.y += TOP_SPACE;
		} finally {
			gc.dispose();
		}
	}

	private void onDispose(DisposeEvent e) {
		font.dispose();
		gradient1Color.dispose();
		gradient2Color.dispose();
		bottomLineColor.dispose();
		writingColor.dispose();
		if (image != null) {
			image.dispose();
		}
	}

	private void onPaint(PaintEvent e) {
		GC gc = e.gc;
		Point s = getSize();
		int w = s.x;
		int h = s.y;

		gc.setForeground(gradient1Color);
		gc.setBackground(gradient2Color);
		gc.fillGradientRectangle(0, 0, w, h - 1, true);

		Rectangle imgsize = new Rectangle(0, 0, 0, 0);
		if (image != null) {
			imgsize = image.getBounds();
			gc.drawImage(image, 12, 0);
		}
		gc.setForeground(bottomLineColor);
		gc.drawLine(0, h - 1, w, h - 1);

		gc.setFont(font);
		gc.setForeground(writingColor);
		Point textSize = gc.stringExtent(text);
		int ty = (h - textSize.y) / 2;
		gc.drawString(text, 22 + imgsize.width, TOP_SPACE + ty, true);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		measureSize(text);
		redraw();
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(size.x, size.y);
	}

}
