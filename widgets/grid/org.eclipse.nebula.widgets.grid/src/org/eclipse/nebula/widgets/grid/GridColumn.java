/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    chris.gross@us.ibm.com    - initial API and implementation
 *    Chuck.Mastrandrea@sas.com - wordwrapping in bug 222280
 *    smcduff@hotmail.com       - wordwrapping in bug 222280
 *    Marty Jones<martybjones@gmail.com> - custom header/footer font in bug 293743
 *    Cserveny Tamas <cserveny.tamas@gmail.com> - min width in bug 295468
 *    Benjamin Bortfeldt<bbortfeldt@gmail.com> - new tooltip support in 300797
 *    Thomas Halm <thha@fernbach.com> - bugfix in 315397
 *    Cserveny Tamas <cserveny.tamas@gmail.com> - bugfix in 318984
 *    Mirko Paturzo <mirko.paturzo@yahoo.it> - bugfix in 248388, 525390
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.nebula.widgets.grid.internal.DefaultColumnFooterRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultColumnHeaderRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;

/**
 * <p>
 * NOTE: THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT. THIS IS A
 * PRE-RELEASE ALPHA VERSION. USERS SHOULD EXPECT API CHANGES IN FUTURE
 * VERSIONS.
 * </p>
 * Instances of this class represent a column in a grid widget.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.LEFT, SWT.RIGHT, SWT.CENTER, SWT.CHECK, SWT.FILL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Move, Resize, Selection, Show, Hide</dd>
 * </dl>
 *
 * @author chris.gross@us.ibm.com
 */
public class GridColumn extends Item {

	private static final boolean IS_MAC ;
	static {
		final String osProperty = System.getProperty("os.name");
		if (osProperty != null) {
			final String osName = osProperty.toUpperCase(Locale.getDefault());
			IS_MAC = osName.indexOf("MAC") > -1;
		} else {
			IS_MAC = false;
		}
	}

	private GridHeaderEditor controlEditor;

	/**
	 * Default width of the column.
	 */
	private static final int DEFAULT_WIDTH = 10;

	/**
	 * Parent table.
	 */
	private Grid parent;

	/**
	 * Header renderer.
	 */
	private GridHeaderRenderer headerRenderer = new DefaultColumnHeaderRenderer();

	private GridFooterRenderer footerRenderer = new DefaultColumnFooterRenderer();

	/**
	 * Cell renderer.
	 */
	private GridCellRenderer cellRenderer = new DefaultCellRenderer();

	private static int NOT_CALCULATED_YET = -1;

	/**
	 * Caching of footerHeight
	 */
	private int footerHeight = NOT_CALCULATED_YET;

	private int headerHeight = NOT_CALCULATED_YET;

	int getFooterHeight(final GC gc)
	{
		if(footerHeight == NOT_CALCULATED_YET) {
			footerHeight = getFooterRenderer().computeSize(gc, getWidth(), SWT.DEFAULT, this).y;
		}
		return footerHeight;
	}

	int getHeaderHeight(final GC gc)
	{
		if(headerHeight == NOT_CALCULATED_YET) {
			headerHeight = getHeaderRenderer().computeSize(gc, getWidth(), SWT.DEFAULT, this).y;
		}
		return headerHeight;
	}

	/**
	 * Width of column.
	 */
	int width = DEFAULT_WIDTH;

	/**
	 * Sort style of column. Only used to draw indicator, does not actually sort
	 * data.
	 */
	private int sortStyle = SWT.NONE;

	/**
	 * Determines if this column shows toggles.
	 */
	private boolean tree = false;

	/**
	 * Does this column contain check boxes? Did the user specify SWT.CHECK in
	 * the constructor of the column.
	 */
	private boolean check = false;

	/**
	 * Specifies if this column should display a checkbox because SWT.CHECK was
	 * passed to the parent table (not necessarily the column).
	 */
	private boolean tableCheck = false;

	/**
	 * Is this column resizable?
	 */
	private boolean resizeable = true;

	/**
	 * Is this column moveable?
	 */
	private boolean moveable = false;

	/**
	 * Is a summary column in a column group. Not applicable if this column is
	 * not in a group.
	 */
	private boolean summary = true;

	/**
	 * Is a detail column in a column group. Not applicable if this column is
	 * not in a group.
	 */
	private boolean detail = true;

	private boolean visible = true;

	/**
	 * If a column is fixed, it is always drawn at its inital position regardless of
	 * scrolling
	 */
	private boolean fixed = false;

	private boolean cellSelectionEnabled = true;

	private GridColumnGroup group;

	private boolean checkable = true;

	private Image footerImage;

	private String footerText = "";

	private Font headerFont;

	private Font footerFont;

	private int minimumWidth = 0;

	private String headerTooltip = null;
	int index;

	private boolean fill;

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Grid</code>) and a style value describing its behavior and
	 * appearance. The item is added to the end of the items maintained by its
	 * parent.
	 *
	 * @param parent
	 *            an Grid control which will be the parent of the new instance
	 *            (cannot be null)
	 * @param style
	 *            the style of control to construct
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the parent</li>
	 *             <li>
	 *             ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *             subclass</li>
	 *             </ul>
	 */
	public GridColumn(final Grid parent, final int style) {
		this(parent, style, -1);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Grid</code>), a style value describing its behavior and appearance,
	 * and the index at which to place it in the items maintained by its parent.
	 *
	 * @param parent
	 *            an Grid control which will be the parent of the new instance
	 *            (cannot be null)
	 * @param style
	 *            the style of control to construct
	 * @param index
	 *            the index to store the receiver in its parent
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the parent</li>
	 *             <li>
	 *             ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *             subclass</li>
	 *             </ul>
	 */
	public GridColumn(final Grid parent, final int style, final int index) {
		super(parent, style, index);

		init(parent, style, index);
	}

	/**
	 * Constructs a new instance of this class given its parent column group
	 * (which must be a <code>GridColumnGroup</code>), a style value describing
	 * its behavior and appearance, and the index at which to place it in the
	 * items maintained by its parent.
	 *
	 * @param parent
	 *            an Grid control which will be the parent of the new instance
	 *            (cannot be null)
	 * @param style
	 *            the style of control to construct
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the parent</li>
	 *             <li>
	 *             ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *             subclass</li>
	 *             </ul>
	 */
	public GridColumn(final GridColumnGroup parent, final int style) {
		super(parent.getParent(), style, parent.getNewColumnIndex());

		init(parent.getParent(), style, parent.getNewColumnIndex());

		group = parent;

		group.newColumn(this, -1);
	}

	private void init(final Grid table, final int style, final int index) {
		parent = table;

		table.newColumn(this, index);

		if ((style & SWT.CHECK) == SWT.CHECK) {
			check = true;
		}

		if ((style & SWT.FILL) != 0) {
			fill = true;
		}

		initHeaderRenderer();
		initFooterRenderer();
		initCellRenderer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (!parent.isDisposing()) {
			parent.removeColumn(this);
			if (group != null) {
				group.removeColumn(this);
			}

			if (controlEditor != null ) {
				controlEditor.dispose();
			}
		}

		if(cellRenderer != null)
		{
			cellRenderer.setDisplay(null);
			cellRenderer = null;
		}

		if(headerRenderer != null)
		{
			headerRenderer.setDisplay(null);
			headerRenderer = null;
		}

		super.dispose();
	}

	/**
	 * Initialize header renderer.
	 */
	private void initHeaderRenderer() {
		headerRenderer.setDisplay(getDisplay());
	}

	private void initFooterRenderer() {
		footerRenderer.setDisplay(getDisplay());
	}

	boolean isFill() {
		return fill;
	}

	/**
	 * Initialize cell renderer.
	 */
	private void initCellRenderer() {
		cellRenderer.setDisplay(getDisplay());

		cellRenderer.setCheck(check);
		cellRenderer.setTree(tree);
		cellRenderer.setColumn(index);

		if ((getStyle() & SWT.RIGHT) == SWT.RIGHT) {
			cellRenderer.setAlignment(SWT.RIGHT);
		}

		if ((getStyle() & SWT.CENTER) == SWT.CENTER) {
			cellRenderer.setAlignment(SWT.CENTER);
		}

	}

	/**
	 * Returns the header renderer.
	 *
	 * @return header renderer
	 */
	public GridHeaderRenderer getHeaderRenderer() {
		return headerRenderer;
	}

	GridFooterRenderer getFooterRenderer() {
		return footerRenderer;
	}

	/**
	 * Returns the cell renderer.
	 *
	 * @return cell renderer.
	 */
	public GridCellRenderer getCellRenderer() {
		return cellRenderer;
	}

	/**
	 * Returns the width of the column.
	 *
	 * @return width of column
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getWidth() {
		checkWidget();
		return width;
	}

	int getWidth(int extra) {
		if (fill) {
			return width + extra;
		}
		return width;
	}

	/**
	 * Sets the width of the column.
	 *
	 * @param width
	 *            new width
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setWidth(final int width) {
		checkWidget();
		setWidth(width, true);
	}

	void setWidth(final int width, final boolean redraw) {
		int widthToSet = Math.max(minimumWidth, width);
		if (parent.getColumnScrolling()) {
			/*
			 * width should not be greater than visible available width for columns, as we
			 * can't scroll all to the right
			 */
			int availableVisibleWidthForColumns = parent.getClientArea().width;
			if (IS_MAC && availableVisibleWidthForColumns == 1 && parent.getClientArea().height <= 1) {
				// One sets column width before the grid size has been layouted
				availableVisibleWidthForColumns = width;
			}
			if(availableVisibleWidthForColumns > 0) {
				if (parent.isRowHeaderVisible()) {
					availableVisibleWidthForColumns -= parent.getRowHeaderWidth();
				}
				widthToSet = Math.min(availableVisibleWidthForColumns, widthToSet);
			}
		}
		this.width = widthToSet;
		if (redraw) {
			parent.setScrollValuesObsolete();
			parent.redraw();
		}
		parent.handlePacked(this);
		footerHeight = NOT_CALCULATED_YET;
		headerHeight = NOT_CALCULATED_YET;
	}

	/**
	 * Sets the sort indicator style for the column. This method does not actual
	 * sort the data in the table. Valid values include: SWT.UP, SWT.DOWN,
	 * SWT.NONE.
	 *
	 * @param style
	 *            SWT.UP, SWT.DOWN, SWT.NONE
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setSort(final int style) {
		checkWidget();
		sortStyle = style;
		parent.redraw();
	}

	/**
	 * Returns the sort indicator value.
	 *
	 * @return SWT.UP, SWT.DOWN, SWT.NONE
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getSort() {
		checkWidget();
		return sortStyle;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the receiver's is pushed, by sending it one of the messages defined
	 * in the <code>SelectionListener</code> interface.
	 *
	 * @param listener
	 *            the listener which should be notified
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addTypedListener(listener, SWT.Selection);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the receiver's selection changes.
	 *
	 * @param listener
	 *            the listener which should no longer be notified
	 * @see SelectionListener
	 * @see #addSelectionListener(SelectionListener)
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		removeTypedListener(SWT.Selection, listener);
	}

	/**
	 * Fires selection listeners.
	 */
	void fireListeners() {
		final Event e = new Event();
		e.display = getDisplay();
		e.item = this;
		e.widget = parent;

		notifyListeners(SWT.Selection, e);
	}

	/**
	 * Returns true if the column is visible, false otherwise. If the column is
	 * in a group and the group is not expanded and this is a detail column,
	 * returns false (and vice versa).
	 *
	 * @return true if visible, false otherwise
	 *
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean isVisible() {
		checkWidget();
		if (group != null) {
			if (group.getExpanded() && !isDetail()
					|| !group.getExpanded() && !isSummary()) {
				return false;
			}
		}
		return visible;
	}

	/**
	 * Returns the visibility state as set with {@code setVisible}.
	 *
	 * @return the visible
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getVisible() {
		checkWidget();
		return visible;
	}

	/**
	 * Sets the column's visibility.
	 *
	 * @param visible
	 *            the visible to set
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setVisible(final boolean visible) {
		checkWidget();

		final boolean before = isVisible();

		this.visible = visible;

		if (isVisible() != before) {
			if (visible) {
				notifyListeners(SWT.Show, new Event());
			} else {
				notifyListeners(SWT.Hide, new Event());
			}

			/*
			 *  Move focus to the next visible column on the right
			 *  (or left if it is not possible)
			 */
			if (parent.getFocusColumn() == this) {
				final GridItem focusItem = parent.getFocusItem();
				if (focusItem != null) {
					GridColumn column = parent.getVisibleColumn_DegradeRight(focusItem, this);
					if (column != null) {
						parent.setFocusColumn(column);
					} else {
						column = parent.getVisibleColumn_DegradeLeft(focusItem, this);
						if (column != null) {
							parent.setFocusColumn(column);
						}
					}
				}
			}

			final GridColumn[] colsOrdered = parent.getColumnsInOrder();
			for (final GridColumn column : colsOrdered) {
				if (column != this && column.isVisible()) {
					column.fireMoved();
				}
			}

			parent.redraw();
		}
	}

	/**
	 * Update the text of the grid column
	 * 
	 * @param text
	 *                 the new text
	 */
	@Override
	public void setText(String text) {

		if(Objects.equals(getText(), text)) {
			return;
		}
		super.setText(text);
		Rectangle bounds = getBounds();
		parent.redraw(bounds.x, bounds.y, bounds.width, bounds.height, false);
	}

	/**
	 * Causes the receiver to be resized to its preferred size.
	 *
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void pack() {
		checkWidget();

		final GC gc = new GC(parent);
		int newWidth = getHeaderRenderer().computeSize(gc, SWT.DEFAULT,
				SWT.DEFAULT, this).x;

		getCellRenderer().setColumn(index);
		final boolean virtual = (getParent().getStyle() & SWT.VIRTUAL) != 0;
		final int bottomIndex = getParent().getBottomIndex() + 1;
		final int topIndex = getParent().getTopIndex();
		if (parent.isVisibleLinesColumnPack())
		{
			for (int i = topIndex; i < bottomIndex; i++)
			{
				final GridItem item = parent.getItem(i);
				if (item.isVisible())
				{
					newWidth = Math.max(newWidth, getCellRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, item).x);
					if (virtual && (i > bottomIndex || i < topIndex))
					{
						getParent().getDataVisualizer().clearRow(item);
						item.setHasSetData(false);
					}
				}
			}

		}
		else
		{
			for (int i = 0; i < parent.getItemCount(); i++)
			{
				final GridItem item = parent.getItem(i);
				if (item.isVisible())
				{
					newWidth = Math.max(newWidth, getCellRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, item).x);
					if (virtual && (i > bottomIndex || i < topIndex))
					{
						getParent().getDataVisualizer().clearRow(item);
						item.setHasSetData(false);
					}
				}
			}
		}
		gc.dispose();
		setWidth(newWidth);
		parent.redraw();
	}

	/**
	 * Returns true if this column includes a tree toggle.
	 *
	 * @return true if the column includes the tree toggle.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean isTree() {
		checkWidget();
		return tree;
	}

	/**
	 * Returns true if the column includes a check box.
	 *
	 * @return true if the column includes a check box.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean isCheck() {
		checkWidget();

		return check || tableCheck;
	}

	/**
	 * Sets the cell renderer.
	 *
	 * @param cellRenderer
	 *            The cellRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setCellRenderer(final GridCellRenderer cellRenderer) {
		checkWidget();

		this.cellRenderer = cellRenderer;
		initCellRenderer();
	}

	/**
	 * Sets the header renderer.
	 *
	 * @param headerRenderer
	 *            The headerRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setHeaderRenderer(final GridHeaderRenderer headerRenderer) {
		checkWidget();
		this.headerRenderer = headerRenderer;
		initHeaderRenderer();
	}

	/**
	 * Sets the header renderer.
	 *
	 * @param footerRenderer
	 *            The footerRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setFooterRenderer(final GridFooterRenderer footerRenderer) {
		checkWidget();
		this.footerRenderer = footerRenderer;
		initFooterRenderer();
	}

	/**
	 * Adds a listener to the list of listeners notified when the column is
	 * moved or resized.
	 *
	 * @param listener
	 *            listener
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void addControlListener(final ControlListener listener) {
		addTypedListener(listener, SWT.Resize, SWT.Move);
	}

	/**
	 * Removes the given control listener.
	 *
	 * @param listener
	 *            listener.
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void removeControlListener(final ControlListener listener) {
		removeTypedListener(SWT.Resize, listener);
		removeTypedListener(SWT.Move, listener);
	}

	/**
	 * Fires moved event.
	 */
	void fireMoved() {
		final Event e = new Event();
		e.display = getDisplay();
		e.item = this;
		e.widget = parent;

		notifyListeners(SWT.Move, e);
	}

	/**
	 * Fires resized event.
	 */
	void fireResized() {
		final Event e = new Event();
		e.display = getDisplay();
		e.item = this;
		e.widget = parent;

		notifyListeners(SWT.Resize, e);
	}

	/**
	 * Adds or removes the columns tree toggle.
	 *
	 * @param tree
	 *            true to add toggle.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setTree(final boolean tree) {
		checkWidget();

		this.tree = tree;
		cellRenderer.setTree(tree);
		parent.redraw();
	}

	/**
	 * Returns the column alignment.
	 *
	 * @return SWT.LEFT, SWT.RIGHT, SWT.CENTER
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getAlignment() {
		checkWidget();
		return cellRenderer.getAlignment();
	}

	/**
	 * Sets the column alignment.
	 *
	 * @param alignment
	 *            SWT.LEFT, SWT.RIGHT, SWT.CENTER
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setAlignment(final int alignment) {
		checkWidget();
		cellRenderer.setAlignment(alignment);
	}

	/**
	 * Returns the vertical alignment.
	 *
	 * @return SWT.TOP (default), SWT.CENTER, SWT.BOTTOM
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getVerticalAlignment() {
		checkWidget();
		return cellRenderer.getVerticalAlignment();
	}

	/**
	 * Sets the column's vertical text alignment.
	 *
	 * @param alignment SWT.TOP (default), SWT.CENTER, SWT.BOTTOM
	 * @throws org.eclipse.swt.SWTException
	 * <ul>
	 * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
	 * created the receiver</li>
	 * </ul>
	 */
	public void setVerticalAlignment(final int alignment) {
		checkWidget();
		cellRenderer.setVerticalAlignment(alignment);
	}


	/**
	 * Returns true if this column is moveable.
	 *
	 * @return true if moveable.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getMoveable() {
		checkWidget();
		return moveable;
	}

	/**
	 * Sets the column moveable or fixed.
	 *
	 * @param moveable
	 *            true to enable column moving
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setMoveable(final boolean moveable) {
		checkWidget();
		this.moveable = moveable;
		parent.redraw();
	}

	/**
	 * Returns true if the column is resizeable.
	 *
	 * @return true if the column is resizeable.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getResizeable() {
		checkWidget();
		return resizeable;
	}

	/**
	 * Sets the column resizeable.
	 *
	 * @param resizeable
	 *            true to make the column resizeable
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setResizeable(final boolean resizeable) {
		checkWidget();
		this.resizeable = resizeable;
	}

	/**
	 * Returns the column group if this column was created inside a group, or
	 * {@code null} otherwise.
	 *
	 * @return the column group.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public GridColumnGroup getColumnGroup() {
		checkWidget();
		return group;
	}

	/**
	 * Returns true if this column is set as a detail column in a column group.
	 * Detail columns are shown when the group is expanded.
	 *
	 * @return true if the column is a detail column.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean isDetail() {
		checkWidget();
		return detail;
	}

	/**
	 * Sets the column as a detail column in a column group. Detail columns are
	 * shown when a column group is expanded. If this column was not created in
	 * a column group, this method has no effect.
	 *
	 * @param detail
	 *            true to show this column when the group is expanded.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setDetail(final boolean detail) {
		checkWidget();
		this.detail = detail;
	}

	/**
	 * Returns true if this column is set as a summary column in a column group.
	 * Summary columns are shown when the group is collapsed.
	 *
	 * @return true if the column is a summary column.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean isSummary() {
		checkWidget();
		return summary;
	}

	/**
	 * Sets the column as a summary column in a column group. Summary columns
	 * are shown when a column group is collapsed. If this column was not
	 * created in a column group, this method has no effect.
	 *
	 * @param summary
	 *            true to show this column when the group is collapsed.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setSummary(final boolean summary) {
		checkWidget();
		this.summary = summary;
	}

	/**
	 * Returns the bounds of this column's header.
	 *
	 * @return bounds of the column header
	 */
	Rectangle getBounds() {
		final Rectangle bounds = new Rectangle(0, 0, 0, 0);

		if (!isVisible()) {
			return bounds;
		}

		final Point loc = parent.getOrigin(this, null);
		bounds.x = loc.x;
		bounds.y = loc.y;
		bounds.width = getWidth();
		bounds.height = parent.getHeaderHeight();
		if (getColumnGroup() != null) {
			bounds.height -= parent.getGroupHeaderHeight();
		}

		return bounds;
	}

	/**
	 * @return the tableCheck
	 */
	protected boolean isTableCheck() {
		return tableCheck;
	}

	/**
	 * @param tableCheck
	 *            the tableCheck to set
	 */
	protected void setTableCheck(final boolean tableCheck) {
		this.tableCheck = tableCheck;

		cellRenderer.setCheck(tableCheck || check);
	}

	/**
	 * Returns true if cells in the receiver can be selected.
	 *
	 * @return the cellSelectionEnabled
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getCellSelectionEnabled() {
		checkWidget();
		return cellSelectionEnabled;
	}

	/**
	 * Sets whether cells in the receiver can be selected.
	 *
	 * @param cellSelectionEnabled
	 *            the cellSelectionEnabled to set
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setCellSelectionEnabled(final boolean cellSelectionEnabled) {
		checkWidget();
		this.cellSelectionEnabled = cellSelectionEnabled;
	}

	/**
	 * Returns the parent grid.
	 *
	 * @return the parent grid.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Grid getParent() {
		checkWidget();
		return parent;
	}

	/**
	 * Returns the checkable state. If false the checkboxes in the column cannot
	 * be checked.
	 *
	 * @return true if the column is checkable (only applicable when style is
	 *         SWT.CHECK).
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getCheckable() {
		checkWidget();
		return checkable;
	}

	/**
	 * Sets the checkable state. If false the checkboxes in the column cannot be
	 * checked.
	 *
	 * @param checkable
	 *            the new checkable state.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setCheckable(final boolean checkable) {
		checkWidget();
		this.checkable = checkable;
	}

	void setColumnIndex(final int newIndex) {
		cellRenderer.setColumn(newIndex);
	}

	/**
	 * Returns the true if the cells in receiver wrap their text.
	 *
	 * @return true if the cells wrap their text.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getWordWrap() {
		checkWidget();
		return cellRenderer.isWordWrap();
	}

	/**
	 * If the argument is true, wraps the text in the receiver's cells. This
	 * feature will not cause the row height to expand to accommodate the
	 * wrapped text. Please use <code>Grid#setItemHeight</code> to change the
	 * height of each row.
	 *
	 * @param wordWrap
	 *            true to make cells wrap their text.
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setWordWrap(final boolean wordWrap) {
		checkWidget();
		cellRenderer.setWordWrap(wordWrap);
		parent.redraw();
	}

	/**
	 * Sets whether or not text is word-wrapped in the header for this column.
	 * If Grid.setAutoHeight(true) is set, the row height is adjusted to
	 * accommodate word-wrapped text.
	 *
	 * @param wordWrap
	 *            Set to true to wrap the text, false otherwise
	 * @see #getHeaderWordWrap()
	 */
	public void setHeaderWordWrap(final boolean wordWrap) {
		checkWidget();
		headerRenderer.setWordWrap(wordWrap);
		parent.redraw();
	}

	/**
	 * Returns whether or not text is word-wrapped in the header for this
	 * column.
	 *
	 * @return true if the header wraps its text.
	 * @see GridColumn#setHeaderWordWrap(boolean)
	 */
	public boolean getHeaderWordWrap() {
		checkWidget();
		return headerRenderer.isWordWrap();
	}

	/**
	 * Set a new editor at the top of the control. If there's an editor already
	 * set it is disposed.
	 *
	 * @param control
	 *            the control to be displayed in the header
	 */
	public void setHeaderControl(final Control control) {
		if (controlEditor == null) {
			controlEditor = new GridHeaderEditor(this);
			controlEditor.initColumn();
		}
		controlEditor.setEditor(control);
		getParent().recalculateHeader();

		if (control != null) {
			// We need to realign if multiple editors are set it is possible
			// that
			// a later one needs more space
			control.getDisplay().asyncExec(() -> {
				if (controlEditor != null
						&& controlEditor.getEditor() != null) {
					controlEditor.layout();
				}
			});
		}
	}

	/**
	 * @return the current header control
	 */
	public Control getHeaderControl() {
		if (controlEditor != null) {
			return controlEditor.getEditor();
		}
		return null;
	}


	/**
	 * Returns the tooltip of the column header.
	 *
	 * @return the tooltip text
	 */
	public String getHeaderTooltip() {
		checkWidget();
		return headerTooltip;
	}

	/**
	 * Sets the tooltip text of the column header.
	 *
	 * @param tooltip the tooltip text
	 */
	public void setHeaderTooltip(final String tooltip) {
		checkWidget();
		headerTooltip = tooltip;
	}

	/**
	 * Sets the receiver's footer image to the argument, which may be null
	 * indicating that no image should be displayed.
	 *
	 * @param image
	 *            the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setFooterImage(final Image image) {
		checkWidget();
		if (image != null && image.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		footerImage = image;
	}

	/**
	 * Sets the receiver's footer text.
	 *
	 * @param string
	 *            the new text
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setFooterText(final String string) {
		checkWidget();
		if (string == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		footerText = string;
	}

	/**
	 * Returns the receiver's footer image if it has one, or null if it does
	 * not.
	 *
	 * @return the receiver's image
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getFooterImage() {
		checkWidget();
		return footerImage;
	}

	/**
	 * Returns the receiver's footer text, which will be an empty string if it
	 * has never been set.
	 *
	 * @return the receiver's text
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getFooterText() {
		checkWidget();
		return footerText;
	}

	/**
	 * Returns the font that the receiver will use to paint textual information
	 * for the header.
	 *
	 * @return the receiver's font
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Font getHeaderFont() {
		checkWidget();

		if (headerFont == null) {
			return parent.getFont();
		}
		return headerFont;
	}

	/**
	 * Sets the Font to be used when displaying the Header text.
	 *
	 * @param font
	 *                 the new font
	 */
	public void setHeaderFont(final Font font) {
		checkWidget();
		headerFont = font;
	}

	/**
	 * Returns the font that the receiver will use to paint textual information
	 * for the footer.
	 *
	 * @return the receiver's font
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Font getFooterFont() {
		checkWidget();

		if (footerFont == null) {
			return parent.getFont();
		}
		return footerFont;
	}

	/**
	 * Sets the Font to be used when displaying the Footer text.
	 *
	 * @param font
	 *                 the new font
	 */
	public void setFooterFont(final Font font) {
		checkWidget();
		footerFont = font;
	}

	/**
	 * @return the minimum width
	 */
	public int getMinimumWidth() {
		return minimumWidth;
	}

	/**
	 * Set the minimum width of the column
	 *
	 * @param minimumWidth
	 *            the minimum width
	 */
	public void setMinimumWidth(final int minimumWidth) {
		this.minimumWidth = Math.max(0, minimumWidth);
		if( minimumWidth > getWidth() ) {
			setWidth(minimumWidth, true);
		}
	}

	/**
	 * @return if the column is fixed or not
	 */
	public boolean isFixed() {
		return fixed;
	}

	/**
	 * Set this column as a fixed column, fixed columns are always drawn at the
	 * front of the grid regardless of other columns and the current scrolling
	 * position.
	 * 
	 * @param fixed if the column should be fixed or not
	 */
	public void setFixed(boolean fixed) {
		checkWidget();
		this.fixed = fixed;
		parent.redraw();
	}
}