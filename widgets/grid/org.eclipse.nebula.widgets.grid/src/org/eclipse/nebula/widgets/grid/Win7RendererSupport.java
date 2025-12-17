package org.eclipse.nebula.widgets.grid;

import org.eclipse.nebula.widgets.grid.internal.win7.Win7ColumnGroupHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.win7.Win7EmptyColumnHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.win7.Win7GridColumnHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.win7.Win7PaletteProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

/**
 * Support class for adding Win7 column header rendering to a given grid.
 */
public class Win7RendererSupport {

	private Win7PaletteProvider palette;
	private Win7GridColumnHeaderRenderer headerRenderer;
	private Win7EmptyColumnHeaderRenderer emptyHeaderRenderer;
	private Win7ColumnGroupHeaderRenderer groupHeaderRenderer;
	private Grid grid;

	/**
	 * @param agrid
	 *
	 */
	private Win7RendererSupport(Grid agrid) {
		this.grid = agrid;
		this.palette = new Win7PaletteProvider();
		grid.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				grid.removeDisposeListener(this);
				if (palette != null) {
					palette.dispose();
				}
				headerRenderer = null;
				emptyHeaderRenderer = null;
				groupHeaderRenderer = null;
			}
		});
	}

	/**
	 * @param grid the Grid to apply Windows 7 rendering support to
	 * @return {@link Win7RendererSupport}
	 */
	public static Win7RendererSupport create(Grid grid) {
		return new Win7RendererSupport(grid);
	}

	/**
	 * Decorate a single column header
	 *
	 * @param col the GridColumn to decorate
	 * @return {@link Win7RendererSupport}
	 */
	public Win7RendererSupport decorateColumnHeader(GridColumn col) {
		if (headerRenderer == null) {
			headerRenderer = new Win7GridColumnHeaderRenderer(palette);
		}
		col.setHeaderRenderer(headerRenderer);
		return this;
	}

	/**
	 * Decorate an array of column headers
	 *
	 * @param cols the array of GridColumns to decorate
	 * @return {@link Win7RendererSupport}
	 */
	public Win7RendererSupport decorateColumnHeaders(GridColumn[] cols) {
		for (int i = 0; i < cols.length; i++) {
			decorateColumnHeader(cols[i]);
		}
		return this;
	}

	/**
	 * Decorate a single grid column group header
	 *
	 * @param group the GridColumnGroup to decorate
	 * @return {@link Win7RendererSupport}
	 */
	public Win7RendererSupport decorateColumnGroupHeader(GridColumnGroup group) {
		if (groupHeaderRenderer == null) {
			groupHeaderRenderer = new Win7ColumnGroupHeaderRenderer(palette);
		}
		group.setHeaderRenderer(groupHeaderRenderer);
		decorateColumnHeaders(group.getColumns());
		return this;
	}

	/**
	 * Decorate an array of grid column group headers
	 *
	 * @param groups the array of GridColumnGroups to decorate
	 * @return {@link Win7RendererSupport}
	 */
	public Win7RendererSupport decorateColumnGroupHeaders(GridColumnGroup[] groups) {
		for (int i = 0; i < groups.length; i++) {
			decorateColumnGroupHeader(groups[i]);
		}
		return this;
	}

	/**
	 * Decorate the empty column header
	 *
	 * @return {@link Win7RendererSupport}
	 */
	public Win7RendererSupport decorateEmptyColumnHeader() {
		if (emptyHeaderRenderer == null) {
			emptyHeaderRenderer = new Win7EmptyColumnHeaderRenderer(palette);
		}
		grid.setEmptyColumnHeaderRenderer(emptyHeaderRenderer);
		return this;
	}

	/**
	 * Decorate all column headers, all column header groups and the empty column
	 * header.
	 *
	 * @return {@link Win7RendererSupport}
	 */
	public Win7RendererSupport decorate() {
		decorateEmptyColumnHeader();
		decorateColumnGroupHeaders(grid.getColumnGroups());
		decorateColumnHeaders(grid.getColumns());
		return this;
	}

}