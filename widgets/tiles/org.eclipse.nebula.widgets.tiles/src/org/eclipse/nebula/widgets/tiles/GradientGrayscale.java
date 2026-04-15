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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Implements a grayscale gradient
 *
 * @author Fabian Prasser
 */
public class GradientGrayscale extends Gradient{

	/**
	 * Returns the according colors
	 * @param tiles
	 * @return
	 */
	private static final Color[] getColors(final Tiles<?> tiles){
		final Display device = tiles.getDisplay();
		final Color[] colors = new Color[]{	new Color(0, 0, 0),
				new Color(128, 128, 128),
				new Color(255, 255, 255)};
		tiles.addDisposeListener(arg0 -> {
			for (final Color c : colors) {
				if (!c.isDisposed()) {
					c.dispose();
				}
			}
		});
		return colors;
	}

	/**
	 * Creates a new instance
	 * @param tiles
	 */
	public GradientGrayscale(final Tiles<?> tiles) {
		super(tiles, getColors(tiles));
	}
}
