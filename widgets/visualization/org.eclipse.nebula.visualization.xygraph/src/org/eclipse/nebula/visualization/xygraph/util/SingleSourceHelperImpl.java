/*******************************************************************************
 * Copyright (c) 2010, 2017 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.nebula.visualization.xygraph.util;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class SingleSourceHelperImpl extends SingleSourceHelper2 {

	@Override
	protected Cursor createInternalCursor(Display display, ImageData imageData, int width, int height, int style) {
		Cursor cursor = GraphicsUtil.createCursor(display, imageData, width, height);
		XYGraphMediaFactory.getInstance().registerCursor(cursor.toString(), cursor);
		return cursor;
	}

	@Override
	protected Image createInternalVerticalTextImage(String text, Font font, RGB color, boolean upToDown) {
		final Dimension titleSize = FigureUtilities.getTextExtents(text, font);

		final int w = titleSize.height;
		final int h = titleSize.width + 1;
		Image image = new Image(Display.getCurrent(), w, h);

		final GC gc = GraphicsUtil.createGC(image);
		final Color titleColor = new Color(color);
		RGB transparentRGB = new RGB(240, 240, 240);

		gc.setBackground(XYGraphMediaFactory.getInstance().getColor(transparentRGB));
		gc.fillRectangle(image.getBounds());
		gc.setForeground(titleColor);
		gc.setFont(font);
		final Transform tr = new Transform(Display.getCurrent());
		if (!upToDown) {
			tr.translate(0, h);
			tr.rotate(-90);
			GraphicsUtil.setTransform(gc, tr);
		} else {
			tr.translate(w, 0);
			tr.rotate(90);
			GraphicsUtil.setTransform(gc, tr);
		}
		gc.drawText(text, 0, 0);
		tr.dispose();
		gc.dispose();
		final ImageData imageData = image.getImageData();
		image.dispose();
		titleColor.dispose();
		imageData.transparentPixel = imageData.palette.getPixel(transparentRGB);
		image = new Image(Display.getCurrent(), imageData);
		return image;
	}

	@Override
	protected Image getInternalXYGraphSnapShot(IXYGraph xyGraph) {
		Rectangle bounds = xyGraph.getBounds();
		Image image = new Image(null, bounds.width + 6, bounds.height + 6);
		GC gc = GraphicsUtil.createGC(image);
		SWTGraphics graphics = new SWTGraphics(gc);
		// Needed because clipping is not set in GTK2
		graphics.setClip(new Rectangle(0, 0, image.getBounds().width, image.getBounds().height));
		graphics.translate(-bounds.x + 3, -bounds.y + 3);
		graphics.setForegroundColor(xyGraph.getForegroundColor());
		graphics.setBackgroundColor(xyGraph.getBackgroundColor());
		xyGraph.paint(graphics);
		gc.dispose();
		return image;
	}

	@Override
	protected String getInternalImageSavePath() {
		FileDialog dialog = new FileDialog(Display.getDefault().getShells()[0], SWT.SAVE);
		dialog.setFilterNames(new String[] { "PNG Files", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.png", "*.*" }); // Windows
		String path = dialog.open();
		return path;
	}

	@Override
	protected GC internalGetImageGC(Image image) {
		return GraphicsUtil.createGC(image);
	}

	@Override
	protected void internalSetLineStyle_LINE_SOLID(Graphics graphics) {
		graphics.setLineStyle(SWT.LINE_SOLID);
	}

}
