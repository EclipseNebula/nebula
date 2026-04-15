/*****************************************************************************
 * Copyright (c) 2014, 2021 Fabian Prasser, Laurent Caron
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Fabian Prasser - Initial API and implementation
 * Laurent Caron <laurent dot caron at gmail dot com> - Integration into the Nebula Project
 *****************************************************************************/
package org.eclipse.nebula.widgets.tiles;

import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.Color;

/**
 * A gradient
 *
 * @author Fabian Prasser
 */
public class Gradient {

	/** Tiles*/
	private final Tiles<?> tiles;

	/** The resulting colors*/
	private final Color[] colors;

	/**
	 * Creates a new color gradients
	 * @param tiles tiles
	 * @param _colors
	 */
	public Gradient(final Tiles<?> tiles, final Color[] _colors){
		this(tiles, _colors, 100);
	}

	/**
	 * Creates a new color gradient
	 * @param tiles tiles
	 * @param _colors
	 * @param steps
	 */
	public Gradient(final Tiles<?> tiles, final Color[] _colors, final int steps){
		this.tiles = tiles;
		colors = getGradient(_colors, steps);
	}


	/**
	 * Dispose all colors
	 */
	public void dispose() {
		for (final Color c : colors) {
			if (!c.isDisposed()) {
				c.dispose();
			}
		}
	}


	/**
	 * Returns the color array
	 * @param colors
	 * @param steps
	 * @return
	 */
	private final Color[] getGradient(final Color[] colors, final int steps) {

		final java.awt.Color[] awtcolor = new java.awt.Color[colors.length];
		for (int i=0; i<colors.length; i++){
			final Color color = colors[i];
			awtcolor[i] = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
		}
		return getGradient(awtcolor, steps);
	}

	/**
	 * Returns the color array
	 * @param colors
	 * @param steps
	 * @return
	 */
	private final Color[] getGradient(final java.awt.Color[] colors, final int steps) {

		// Draw the gradient to a buffered image
		final Point2D start = new Point2D.Float(0, 0);
		final Point2D end = new Point2D.Float(1, steps);
		final float[] dist = new float[colors.length];
		for (int i=0; i<dist.length; i++){
			dist[i] = 1.0f / dist.length * i;
		}
		final LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);
		final BufferedImage legend = new BufferedImage(1,steps, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = (Graphics2D)legend.getGraphics();
		g2d.setPaint(p);
		g2d.drawRect(0,0,1,steps);
		g2d.dispose();

		// Convert to color array
		final Color[] result = new Color[steps];
		for (int y=0; y<steps; y++){
			final int rgb = legend.getRGB(0, y);
			result[y] = new Color(rgb >> 16 & 0x000000ff, rgb >> 8 & 0x000000ff, rgb & 0x000000ff);
		}

		// Return
		return result;
	}

	/**
	 * Returns the color for a value in [0, 1]
	 * @param value
	 * @return
	 */
	public Color getColor(final double value) {
		final int index = (int)Math.round((colors.length-1) * value);
		return colors[index];
	}
}
