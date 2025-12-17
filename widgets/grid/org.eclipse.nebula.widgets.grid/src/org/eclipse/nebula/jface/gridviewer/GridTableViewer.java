/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    rmcamara@us.ibm.com - initial API and implementation
 *    tom.schindl@bestsolution.at - various significant contributions
 *    mirko.paturzo@exeura.eu - improve performance
 *******************************************************************************/

package org.eclipse.nebula.jface.gridviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.nebula.jface.gridviewer.internal.CellSelection;
import org.eclipse.nebula.jface.gridviewer.internal.SelectionWithFocusRow;
import org.eclipse.nebula.widgets.grid.DataVisualizer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on an Grid control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. It
 * is designed to be instantiated with a pre-existing Grid control and
 * configured with a domain-specific content provider, label provider, element
 * filter (optional), and element sorter (optional).
 * <p>
 * Content providers for grid table viewers must not implement the {@code
 * ITreeContentProvider} interface. Instead a {@link GridTreeViewer} should be
 * used.
 * <p>
 *
 * @author Unknown...
 * @author Mirko Paturzo <mirko.paturzo@exeura.eu>
 *
 *         Mirko modified improve performace and reduce used memory fix memory
 *         leak and slow disposed object
 */
public class GridTableViewer extends AbstractTableViewer {
    /** This viewer's grid control. */
    private final Grid grid;

    private GridViewerRow cachedRow;

    private CellLabelProvider rowHeaderLabelProvider;

    /**
     * If true, this grid viewer will ensure that the grid's rows / GridItems are
     * always sized to their preferred height.
     */
    private boolean autoPreferredHeight = false;

    /**
     * Creates a grid viewer on a newly-created grid control under the given parent.
     * The grid control is created using the SWT style bits
     * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The viewer
     * has no input, no content provider, a default label provider, no sorter, and
     * no filters.
     *
     * @param parent
     *            the parent control
     */
    public GridTableViewer(Composite parent) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    }

    /**
     * Creates a grid viewer on a newly-created grid control under the given parent.
     * The grid control is created using the given SWT style bits. The viewer has no
     * input, no content provider, a default label provider, no sorter, and no
     * filters.
     *
     * @param dataVisualizer the data visualizer for managing grid item data
     * @param parent
     *            the parent control
     * @param style
     *            the SWT style bits used to create the grid.
     */
    public GridTableViewer(DataVisualizer dataVisualizer, Composite parent, int style) {
        this(new Grid(dataVisualizer, parent, style));
    }

    /**
     * Creates a grid viewer on a newly-created grid control under the given parent.
     * The grid control is created using the SWT style bits
     * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The viewer
     * has no input, no content provider, a default label provider, no sorter, and
     * no filters.
     *
     * @param dataVisualizer the data visualizer for managing grid item data
     * @param parent
     *            the parent control
     */
    public GridTableViewer(DataVisualizer dataVisualizer, Composite parent) {
        this(dataVisualizer, parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    }

    /**
     * Creates a grid viewer on a newly-created grid control under the given parent.
     * The grid control is created using the given SWT style bits. The viewer has no
     * input, no content provider, a default label provider, no sorter, and no
     * filters.
     *
     * @param parent
     *            the parent control
     * @param style
     *            the SWT style bits used to create the grid.
     */
    public GridTableViewer(Composite parent, int style) {
        this(new Grid(parent, style));
    }

    /**
     * Creates a grid viewer on the given grid control. The viewer has no input, no
     * content provider, a default label provider, no sorter, and no filters.
     *
     * @param grid
     *            the grid control
     */
    public GridTableViewer(Grid grid) {
        this.grid = grid;
        hookControl(grid);
        setColumnProperties(new String[] {});
    }

    /**
     * Returns the underlying Grid Control.
     *
     * @return grid control.
     */
    public Grid getGrid() {
        return grid;
    }

    /** {@inheritDoc} */
    @Override
    protected ViewerRow internalCreateNewRowPart(int style, int rowIndex) {
        GridItem item;

        if (rowIndex >= 0) {
            item = new GridItem(grid, style, rowIndex);
        } else {
            item = new GridItem(grid, style);
        }

        return getViewerRowFromItem(item);
    }

    /** {@inheritDoc} */
    @Override
    protected ColumnViewerEditor createViewerEditor() {
        return new GridViewerEditor(this, new ColumnViewerEditorActivationStrategy(this), ColumnViewerEditor.DEFAULT);
    }

    /** {@inheritDoc} */
    @Override
    protected void doClear(int index) {
        grid.getDataVisualizer().clearRow(grid.getItem(index));
    }

    /** {@inheritDoc} */
    @Override
    protected void doClearAll() {
        grid.getDataVisualizer().clearAll();
    }

    /**
     * @see org.eclipse.jface.viewers.StructuredViewer#refresh()
     */
    @Override
    public void refresh() {
        try {
            super.refresh();
        } finally {
            grid.refreshData();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doSetItemCount(int count) {
        grid.setItemCount(count);
    }

    /** {@inheritDoc} */
    @Override
    protected void doDeselectAll() {
        grid.deselectAll();
    }

    /** {@inheritDoc} */
    @Override
    protected Widget doGetColumn(int index) {
        return grid.getColumn(index);
    }

    /** {@inheritDoc} */
    @Override
    protected int doGetColumnCount() {
        return grid.getColumnCount();
    }

    /** {@inheritDoc} */
    @Override
    protected Item doGetItem(int index) {
        return grid.getItem(index);
    }

    /** {@inheritDoc} */
    @Override
    protected int doGetItemCount() {
        return grid.getItemCount();
    }

    /** {@inheritDoc} */
    @Override
    protected Item[] doGetItems() {
        return grid.getItems();
    }

    /** {@inheritDoc} */
    @Override
    protected Item[] doGetSelection() {
        return grid.getSelection();
    }

    /** {@inheritDoc} */
    @Override
    protected int[] doGetSelectionIndices() {
        return grid.getSelectionIndices();
    }

    /** {@inheritDoc} */
    @Override
    protected int doIndexOf(Item item) {
        return ((GridItem) item).getRowIndex();
    }

    /** {@inheritDoc} */
    @Override
    protected void doRemove(int[] indices) {
        grid.remove(indices);
    }

    /** {@inheritDoc} */
    @Override
    protected void doRemove(int start, int end) {
        grid.remove(start, end);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    @Override
    protected void doRemoveAll() {
        grid.removeAll();
    }

    /**
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.AbstractTableViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
     *      fix crossed reference for GC
     */
    @Override
    protected void handleDispose(final DisposeEvent event) {
        super.handleDispose(event);

        cachedRow = null;
        rowHeaderLabelProvider = null;

        getGrid().setRedraw(false);
        getGrid().disposeAllItems();
        getGrid().clearItems();
    }

    /** {@inheritDoc} */
    @Override
    protected void doSetSelection(Item[] items) {
        GridItem[] items2 = new GridItem[items.length];
        for (int i = 0; i < items.length; i++) {
            items2[i] = (GridItem) items[i];
        }
        grid.setSelection(items2);
        grid.showSelection();
    }

    /** {@inheritDoc} */
    @Override
    protected void doSetSelection(int[] indices) {
        grid.setSelection(indices);
    }

    /** {@inheritDoc} */
    @Override
    protected void doShowItem(Item item) {
        grid.showItem((GridItem) item);
    }

    /** {@inheritDoc} */
    @Override
    protected void doShowSelection() {
        grid.showSelection();
    }

    /** {@inheritDoc} */
    @Override
    protected Item getItemAt(Point point) {
        return grid.getItem(point);
    }

    /** {@inheritDoc} */
    @Override
    public Control getControl() {
        return grid;
    }

    /** {@inheritDoc} */
    @Override
    protected ViewerRow getViewerRowFromItem(Widget item) {
        if (cachedRow == null) {
            cachedRow = new GridViewerRow((GridItem) item);
        } else {
            cachedRow.setItem((GridItem) item);
        }

        return cachedRow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doResetItem(Item item) {
        GridItem gridItem = (GridItem) item;
        int columnCount = Math.max(1, grid.getColumnCount());
        for (int i = 0; i < columnCount; i++) {
            gridItem.setText(i, ""); //$NON-NLS-1$
            gridItem.setImage(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSelect(int[] indices) {
        grid.select(indices);
    }

    /**
     * When set to true, this grid viewer will ensure that each of the grid's items
     * is always automatically sized to its preferred height. The default is false.
     * <p>
     * Since this mechanism usually leads to a grid with rows of different heights
     * and thus to a grid with decreased performance, it should only be applied if
     * that is intended. To set the height of all items to a specific value, use
     * {@link Grid#setItemHeight(int)} instead.
     * <p>
     * When a column with activated word wrapping is resized by dragging the column
     * resizer, the items are only auto-resized properly if you use
     * {@link GridViewerColumn} to create the columns.
     * <p>
     * When this method is called, existing rows are not resized to their preferred
     * height. Therefore it is suggested that this method be called before rows are
     * populated (i.e. before setInput).
     *
     * @param autoPreferredHeight true to enable automatic preferred height, false otherwise
     */
    public void setAutoPreferredHeight(boolean autoPreferredHeight) {
        this.autoPreferredHeight = autoPreferredHeight;
    }

    /**
     * @return true if this grid viewer sizes its rows to their preferred height
     * @see #setAutoPreferredHeight(boolean)
     */
    public boolean getAutoPreferredHeight() {
        return autoPreferredHeight;
    }

    /** {@inheritDoc} */
    @Override
    protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
        super.doUpdateItem(widget, element, fullMap);
        updateRowHeader(widget);
        if (autoPreferredHeight && !widget.isDisposed())
            ((GridItem) widget).pack();
    }

    private void updateRowHeader(Widget widget) {
        if (rowHeaderLabelProvider != null) {
            ViewerCell cell = getViewerRowFromItem(widget).getCell(Integer.MAX_VALUE);
            rowHeaderLabelProvider.update(cell);
        }
    }

    /**
     * Label provider used by calculate the row header text
     *
     * @param rowHeaderLabelProvider
     *            the provider
     */
    public void setRowHeaderLabelProvider(CellLabelProvider rowHeaderLabelProvider) {
        this.rowHeaderLabelProvider = rowHeaderLabelProvider;
    }

    /**
     * Refresh row headers only
     *
     * @param element
     *            the element to start or <code>null</code> if all rows should be
     *            refreshed
     */
    public void refreshRowHeaders(Object element) {
        boolean refresh = element == null;

        for (int i = 0; i < getGrid().getItemCount(); i++) {
            if (refresh || element.equals(getGrid().getItem(i).getData())) {
                refresh = true;
                updateRowHeader(getGrid().getItem(i));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void editElement(Object element, int column) {
        try {
            getControl().setRedraw(false);
            Widget item = findItem(element);
            if (item != null) {
                ViewerRow row = getViewerRowFromItem(item);
                if (row != null) {
                    ViewerCell cell = row.getCell(column);
                    if (cell != null) {
                        triggerEditorActivationEvent(new ColumnViewerEditorActivationEvent(cell));
                    }
                }
            }
        } finally {
            getControl().setRedraw(true);
        }
        // }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void setSelectionToWidget(ISelection selection, boolean reveal) {
        if (!grid.isCellSelectionEnabled() || !(selection instanceof CellSelection)) {
            super.setSelectionToWidget(selection, reveal);
            Object el = null;
            if (selection instanceof SelectionWithFocusRow) {
                el = ((SelectionWithFocusRow) selection).getFocusElement();
            } else if (selection instanceof IStructuredSelection) {
                el = ((IStructuredSelection) selection).getFirstElement();
            }
            if (el != null) {
                for (int i = 0; i < grid.getItemCount(); i++) {
                    GridItem item = grid.getItem(i);
                    if (item.getData() == el || el.equals(item.getData())
                            || (getComparer() != null && getComparer().equals(item.getData(), el))) {
                        grid.setFocusItem(item);
                        break;
                    }
                }
            }
        } else {
            CellSelection cellSelection = (CellSelection) selection;
            List l = cellSelection.toList();
            ArrayList pts = new ArrayList();

            for (int i = 0; i < grid.getItemCount(); i++) {
                Iterator it = l.iterator();
                Object itemObject = grid.getItem(i).getData();
                while (it.hasNext()) {
                    Object checkObject = it.next();
                    if (itemObject == checkObject
                            || (getComparer() != null && getComparer().equals(itemObject, checkObject))) {
                        Iterator idxIt = cellSelection.getIndices(checkObject).iterator();
                        while (idxIt.hasNext()) {
                            Integer idx = (Integer) idxIt.next();
                            pts.add(new Point(idx.intValue(), i));
                        }
                    }
                }
            }
            Point[] tmp = new Point[pts.size()];
            pts.toArray(tmp);
            grid.setCellSelection(tmp);
            if (cellSelection.getFocusElement() != null) {
                Object el = cellSelection.getFocusElement();
                for (int i = 0; i < grid.getItemCount(); i++) {
                    GridItem item = grid.getItem(i);
                    if (item.getData() == el || item.getData().equals(el)
                            || (getComparer() != null && getComparer().equals(item.getData(), el))) {
                        grid.setFocusItem(item);
                        break;
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISelection getSelection() {
        if (grid == null || grid.isDisposed()) {
            return StructuredSelection.EMPTY;
        }
        if (!grid.isCellSelectionEnabled()) {
            IStructuredSelection selection = (IStructuredSelection) super.getSelection();
            Object el = null;
            if (grid.getFocusItem() != null && !grid.getFocusItem().isDisposed()) {
                el = grid.getFocusItem().getData();
            }
            return new SelectionWithFocusRow(selection.toList(), el, getComparer());
        } else {
            return createCellSelection();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private CellSelection createCellSelection() {
        Point[] ps = grid.getCellSelection();
        Arrays.sort(ps, new Comparator() {

            @Override
            public int compare(Object arg0, Object arg1) {
                Point a = (Point) arg0;
                Point b = (Point) arg1;
                int rv = a.y - b.y;

                if (rv == 0) {
                    rv = a.x - b.x;
                }

                return rv;
            }
        });

        ArrayList objectList = new ArrayList();
        ArrayList indiceLists = new ArrayList();
        ArrayList indiceList = new ArrayList();

        int curLine = -1;

        for (int i = 0; i < ps.length; i++) {
            if (curLine != ps[i].y) {
                curLine = ps[i].y;
                indiceList = new ArrayList();

                indiceLists.add(indiceList);
                objectList.add(grid.getItem(curLine).getData());
            }
            indiceList.add(Integer.valueOf(ps[i].x));
        }

        Object focusElement = null;

        if (grid.getFocusItem() != null && !grid.getFocusItem().isDisposed()) {
            focusElement = grid.getFocusItem().getData();
        }

        return new CellSelection(objectList, indiceLists, focusElement, getComparer());
    }

}
