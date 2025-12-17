package org.eclipse.nebula.widgets.grid.internal.win7;

import org.eclipse.nebula.widgets.grid.internal.win7.Win7PaletteProvider.Palette;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Utility to perform the common drawing functions for all Win7 grid columns
 * headers
 */
public class Win7ColumnHeaderUtil {

	/**
	 * Based on the provided state (hover/selected) generate the appropriate column
	 * header rendering.
	 *
	 * @param graphics the graphics context
	 * @param bounds the bounds to draw within
	 * @param palette the palette provider
	 * @param hover
	 *            indicates whether the mouse is hovering over the column header
	 * @param selected
	 *            indicates whether the column is selected (mousedown)
	 * @param mousedown indicates whether the mouse button is pressed
	 */
	public static void drawColumn(GC graphics, Rectangle bounds, Win7PaletteProvider palette, boolean hover,
			boolean selected, boolean mousedown) {
		if (mousedown) {
			drawColumnHeader(graphics, bounds, palette.getPalette((Display) graphics.getDevice(),
					Win7PaletteProvider.MOUSEDOWN_GRID_COLUMN_HEADER));
			drawColumnSelectedTopShadow(graphics, bounds,
					palette.getPalette((Display) graphics.getDevice(), Win7PaletteProvider.SHADOW_GRID_COLUMN_HEADER));
		} else if (hover) {
			drawColumnHeader(graphics, bounds,
					palette.getPalette((Display) graphics.getDevice(), Win7PaletteProvider.HOVER_GRID_COLUMN_HEADER));
		} else if (selected) {
			drawColumnHeader(graphics, bounds, palette.getPalette((Display) graphics.getDevice(),
					Win7PaletteProvider.SELECTED_GRID_COLUMN_HEADER));
		} else {
			drawColumnHeader(graphics, bounds,
					palette.getPalette((Display) graphics.getDevice(), Win7PaletteProvider.NORMAL_GRID_COLUMN_HEADER));
		}
	}

	/**
	 * Draw an additional shadow for the selected column state
	 *
	 * @param graphics the graphics context
	 * @param bounds the bounds to draw within
	 * @param palette the palette to use for colors
	 */
	protected static void drawColumnSelectedTopShadow(GC graphics, Rectangle bounds, Palette palette) {
		int x = bounds.x;
		int y = bounds.y;

		graphics.setForeground(palette.getColors()[0]);
		graphics.drawLine(x + 1, y, x + bounds.width - 2, y);
		graphics.setForeground(palette.getColors()[1]);
		graphics.drawLine(x + 1, y + 1, x + bounds.width - 2, y + 1);
	}

	/**
	 * Draw the column header based on the given colors
	 *
	 * @param graphics the graphics context
	 * @param bounds the bounds to draw within
	 * @param palette the palette to use for colors
	 */
	protected static void drawColumnHeader(GC graphics, Rectangle bounds, Palette palette) {
		int x = bounds.x;
		int y = bounds.y;

		int topRectHeight = (int) Math.round((bounds.height - 3) * .45);
		int bottomRectHeight = bounds.height - 3 - topRectHeight;
		int bottomRectY = y + topRectHeight + 1;

		// 2 - left upper
		graphics.setBackground(palette.getColors()[1]);
		graphics.fillRectangle(x, y, 1, topRectHeight + 1);

		// 3 - upper fill
		graphics.setBackground(palette.getColors()[2]);
		graphics.fillRectangle(x + 1, y, bounds.width - 3, topRectHeight + 1);

		// 4 - right upper
		graphics.setBackground(palette.getColors()[3]);
		graphics.fillRectangle(x + bounds.width - 2, y, 1, topRectHeight + 1);

		// 5 - right upper gradient (shadow/highlight)
		graphics.setBackground(palette.getColors()[5]);
		if (palette.getColors()[4] != null) {
			graphics.setForeground(palette.getColors()[4]);
			graphics.fillGradientRectangle(x + bounds.width - 1, y, 1, topRectHeight + 1, true);
		} else {
			graphics.fillRectangle(x + bounds.width - 1, y, 1, topRectHeight + 1);
		}

		// 6 - left bottom gradient (shadow/hightlight)
		graphics.setBackground(palette.getColors()[7]);
		if (palette.getColors()[6] != null) {
			graphics.setForeground(palette.getColors()[6]);
			graphics.fillGradientRectangle(x, bottomRectY, 1, bottomRectHeight, true);
		} else {
			graphics.fillRectangle(x, bottomRectY, 1, bottomRectHeight);
		}

		// 7 - bottom fill
		graphics.setBackground(palette.getColors()[9]);
		if (palette.getColors()[8] != null) {
			graphics.setForeground(palette.getColors()[8]);
			graphics.fillGradientRectangle(x + 1, bottomRectY, bounds.width - 3, bottomRectHeight, true);
		} else {
			graphics.fillRectangle(x + 1, bottomRectY, bounds.width - 3, bottomRectHeight);
		}

		// 8 - right bottom gradient (shadow/highlight)
		graphics.setBackground(palette.getColors()[11]);
		if (palette.getColors()[10] != null) {
			graphics.setForeground(palette.getColors()[10]);
			graphics.fillGradientRectangle(x + bounds.width - 2, bottomRectY, 1, bottomRectHeight, true);
		} else {
			graphics.fillRectangle(x + bounds.width - 2, bottomRectY, 1, bottomRectHeight);
		}

		// 9 - right bottom cell border
		graphics.setBackground(palette.getColors()[13]);
		if (palette.getColors()[12] != null) {
			graphics.setForeground(palette.getColors()[12]);
			graphics.fillGradientRectangle(x + bounds.width - 1, bottomRectY, 1, bottomRectHeight, true);
		} else {
			graphics.fillRectangle(x + bounds.width - 1, bottomRectY, 1, bottomRectHeight);
		}

		// 10 - bottom shadow
		graphics.setForeground(palette.getColors()[14]);
		graphics.drawLine(x, y + bounds.height - 2, x + bounds.width - 1, y + bounds.height - 2);

		// 11 - bottom cell border
		graphics.setForeground(palette.getColors()[15]);
		graphics.drawLine(x, y + bounds.height - 1, x + bounds.width - 1, y + bounds.height - 1);
	}

}