package org.eclipse.nebula.widgets.grid.internal.win7;

import org.eclipse.nebula.widgets.grid.AbstractRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * Empty column header renderer which emulates a default Win7 L&F. This
 * implementation does not take into account any theme(s) applied to the OS and
 * only used a pre-defined set of normalColors that seem to "mostly" match the
 * default theme of Win7 normalColors.
 */
public class Win7EmptyColumnHeaderRenderer extends AbstractRenderer {

	private Win7PaletteProvider palette;

	/**
	 * Creates a new Windows 7 style empty column header renderer.
	 * 
	 * @param palette the palette provider for Windows 7 colors
	 */
	public Win7EmptyColumnHeaderRenderer(Win7PaletteProvider palette) {
		this.palette = palette;
	}

	/**
	 * {@inheritDoc}
	 */
	public Point computeSize(GC gc, int wHint, int hHint, Object value) {
		return new Point(wHint, hHint);
	}

	/**
	 * Set the display for the renderer
	 *
	 * @param d
	 *            Display
	 */
	public void setDisplay(Display d) {
		super.setDisplay(d);
		palette.initializePalette(getDisplay(), Win7PaletteProvider.NORMAL_GRID_COLUMN_HEADER);
	}

	/**
	 * {@inheritDoc}
	 */
	public void paint(GC gc, Object value) {
		Win7ColumnHeaderUtil.drawColumn(gc, getBounds(), palette, false, false, false);

		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

	}

}