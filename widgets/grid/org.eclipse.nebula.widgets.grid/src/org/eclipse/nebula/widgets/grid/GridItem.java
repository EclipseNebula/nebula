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
 *    chris.gross@us.ibm.com - initial API and implementation
 *    Claes Rosell<claes.rosell@solme.se> - rowspan in bug 272384
 *    Stefan Widmaier<stefan.widmaier@faktorzehn.de> - rowspan in 304797
 *    Mirko Paturzo <mirko.paturzo@exeura.eu> - improvement (bug in 419928)
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;

/**
 * <p>
 * NOTE: THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT. THIS IS A
 * PRE-RELEASE ALPHA VERSION. USERS SHOULD EXPECT API CHANGES IN FUTURE
 * VERSIONS.
 * </p>
 * Instances of this class represent a selectable user interface object that
 * represents an item in a grid.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 * @author chris.gross@us.ibm.com
 * @author Mirko Paturzo <mirko.paturzo@exeura.eu>
 *
 *         Mirko removed all collections, improve dispose performance, reduce
 *         used memory
 */
public class GridItem extends Item {
	private static final int NO_ROW = -1;

	/**
	 * List of children.
	 */
	private List<GridItem> children;

	/**
	 * Default background color.
	 */
	@Deprecated
	private Color defaultBackground;

	/**
	 * Default font.
	 */
	@Deprecated
	private Font defaultFont;

	/**
	 * Default foreground color.
	 */
	@Deprecated
	private Color defaultForeground;

	/**
	 * The height of this <code>GridItem</code>.
	 */
	private int height = 1;

	/**
	 * Is expanded?
	 */
	private boolean expanded = false;

	/**
	 * True if has children.
	 */
	private boolean hasChildren = false;

	/**
	 * Level of item in a tree.
	 */
	private int level = 0;

	/**
	 * Parent grid instance.
	 */
	private final Grid parent;

	/**
	 * Parent item (if a child item).
	 */
	private GridItem parentItem;

	/**
	 * Is visible?
	 */
	private boolean visible = true;

	/**
	 * Row header text.
	 */
	private String headerText = null;

	/**
	 * Row header image
	 */
	private Image headerImage = null;

	/**
	 * Background color of the header
	 */
	private Color headerBackground = null;

	/**
	 * Foreground color of the header
	 */
	public Color headerForeground = null;

	/**
	 * Font of the header
	 */
	private Font headerFont = null;

	/**
	 * (SWT.VIRTUAL only) Flag that specifies whether the client has already
	 * been sent a SWT.SetData event.
	 */
	private boolean hasSetData = false;

	private int row = NO_ROW;

	private final Object ROW_LOCK = new Object();

	/**
	 * Creates a new instance of this class and places the item at the end of
	 * the grid.
	 *
	 * @param parent
	 *            parent grid
	 * @param style
	 *            item style
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the parent</li>
	 *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *             subclass</li>
	 *             </ul>
	 */
	public GridItem(Grid parent, int style) {
		this(parent, style, NO_ROW);
	}

	/**
	 * Creates a new instance of this class and places the item in the grid at
	 * the given index.
	 *
	 * @param parent
	 *            parent grid
	 * @param style
	 *            item style
	 * @param index
	 *            index where to insert item
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the parent</li>
	 *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *             subclass</li>
	 *             </ul>
	 */
	public GridItem(Grid parent, int style, int index) {
		super(parent, style, index);

		this.parent = parent;

		row = parent.newItem(this, index, true);
		parent.newRootItem(this, index);
	}

	/**
	 * @return grid row index
	 */
	public int getRowIndex() {
		synchronized (ROW_LOCK) {
			if (row != NO_ROW)
				return row;
		}
		return parent.indexOf(this);
	}

	void increaseRow() {
		synchronized (ROW_LOCK) {
			row++;
		}
	}

	void decreaseRow() {
		synchronized (ROW_LOCK) {
			row--;
		}
	}

	/**
	 * Creates a new instance of this class as a child node of the given
	 * GridItem and places the item at the end of the parents items.
	 *
	 * @param parent
	 *            parent item
	 * @param style
	 *            item style
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the parent</li>
	 *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *             subclass</li>
	 *             </ul>
	 */
	public GridItem(GridItem parent, int style) {
		this(parent, style, NO_ROW);
	}

	/**
	 * Creates a new instance of this class as a child node of the given Grid
	 * and places the item at the given index in the parent items list.
	 *
	 * @param parent
	 *            parent item
	 * @param style
	 *            item style
	 * @param index
	 *            index to place item
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the parent</li>
	 *             <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
	 *             subclass</li>
	 *             </ul>
	 */
	public GridItem(GridItem parent, int style, int index) {
		super(parent, style, index);

		parentItem = parent;
		this.parent = parentItem.getParent();

		row = this.parent.newItem(this, index, false);

		level = parentItem.getLevel() + 1;

		parentItem.newItem(this, index);

		parentItem.indexOf(this);

		if (parent.isVisible() && parent.isExpanded()) {
			setVisible(true);
		} else {
			setVisible(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (!parent.isDisposing()) {
			parent.removeItem(this);

			if (parentItem != null) {
				parentItem.remove(this);
			} else {
				parent.removeRootItem(this);
			}
			if (hasChildren)
				for (int i = children.size() - 1; i >= 0; i--) {
					children.get(i).dispose();
				}
		}
		if (parent.getDataVisualizer() != null) {
			parent.getDataVisualizer().clearRow(this);
		}
		noRow();
		super.dispose();
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the row is resized, by sending it one of the messages defined in the
	 * <code>ControlListener</code> interface.
	 * <p>
	 * Clients who wish to override the standard row resize logic should use the
	 * untyped listener mechanisms. The untyped <code>Event</code> object passed
	 * to an untyped listener will have its <code>detail</code> field populated
	 * with the new row height. Clients may alter this value to, for example,
	 * enforce minimum or maximum row sizes. Clients may also set the
	 * <code>doit</code> field to false to prevent the entire resize operation.
	 *
	 * @param listener
	 *            the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void addControlListener(ControlListener listener) {
		addTypedListener(listener, SWT.Resize);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the row is resized.
	 *
	 * @param listener
	 *            the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void removeControlListener(ControlListener listener) {
		removeTypedListener(SWT.Resize, listener);
	}

	/**
	 * Fires the given event type on the parent Grid instance. This method
	 * should only be called from within a cell renderer. Any other use is not
	 * intended.
	 *
	 * @param eventId
	 *            SWT event constant
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void fireEvent(int eventId) {
		checkWidget();

		Event e = new Event();
		e.display = getDisplay();
		e.widget = this;
		e.item = this;
		e.type = eventId;

		getParent().notifyListeners(eventId, e);
	}

	/**
	 * Fires the appropriate events in response to a user checking/unchecking an
	 * item. Checking an item fires both a selection event (with event.detail of
	 * SWT.CHECK) if the checkbox is in the first column and the seperate check
	 * listener (all columns). This method manages that behavior. This method
	 * should only be called from within a cell renderer. Any other use is not
	 * intended.
	 *
	 * @param column
	 *            the column where the checkbox resides
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void fireCheckEvent(int column) {
		checkWidget();

		Event selectionEvent = new Event();
		selectionEvent.display = getDisplay();
		selectionEvent.widget = this;
		selectionEvent.item = this;
		selectionEvent.type = SWT.Selection;
		selectionEvent.detail = SWT.CHECK;
		selectionEvent.index = column;

		getParent().notifyListeners(SWT.Selection, selectionEvent);
	}

	/**
	 * Returns the receiver's background color.
	 *
	 * @return the background color
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Color getBackground() {
		checkWidget();

		if (defaultBackground == null) {
			return parent.getBackground();
		}
		return defaultBackground;
	}

	/**
	 * Returns the background color at the given column index in the receiver.
	 *
	 * @param index
	 *            the column index
	 * @return the background color
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Color getBackground(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getBackground(this, index);

	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative
	 * to its parent at a column in the table.
	 *
	 * @param columnIndex
	 *            the index that specifies the column
	 * @return the receiver's bounding column rectangle
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Rectangle getBounds(int columnIndex) {
		checkWidget();

		final Point origin = parent.getOrigin(parent.getColumn(columnIndex), this);
		final Point cellSize = this.getCellSize(columnIndex, parent.getExtraFill());
		return new Rectangle(origin.x, origin.y, cellSize.x, cellSize.y);
	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative
	 * to its parent at a column in the table.
	 * Handle not visible items.
	 *
	 * @param columnIndex
	 *            the index that specifies the column
	 * @return the receiver's bounding column rectangle
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Rectangle getBoundsCorrected(final int columnIndex) {
		checkWidget();

		// HACK: The -1000,-1000 xy coordinates below are a hack to deal with
		// GridEditor issues. In
		// normal SWT Table, when an editor is created on Table and its
		// positioned in the header area
		// the header overlays the editor. Because Grid (header and everything)
		// is drawn on one
		// composite, when an editor is positioned in the header area the editor
		// overlays the header.
		// So to fix this, when the editor is anywhere its not supposed to be
		// seen (the editor
		// coordinates are determined by this getBounds) we position it out in
		// timbuktu.
		if (!isVisible())
			return new Rectangle(-1000, -1000, 0, 0);

		if (!parent.isShown(this))
			return new Rectangle(-1000, -1000, 0, 0);

		Point origin = parent.getOrigin(parent.getColumn(columnIndex), this);

		if (origin.x < 0 && parent.isRowHeaderVisible())
			return new Rectangle(-1000, -1000, 0, 0);

		Point cellSize = this.getCellSize(columnIndex, parent.getExtraFill());

		return new Rectangle(origin.x, origin.y, cellSize.x, cellSize.y);
	}

	/**
	 *
	 * @param columnIndex index of the column to get the cell size
	 * @return width and height
	 */
	protected Point getCellSize(int columnIndex) {
		return getCellSize(columnIndex, parent.getExtraFill());
	}
	/**
	 *
	 * @param columnIndex index of the column to get the cell size
	 * @param extraFill any extra width that should be applied to columns that have fill property
	 * @return width and height
	 */
	protected Point getCellSize(int columnIndex, int extraFill) {
		/* width */
		int width = 0;

		int span = getColumnSpan(columnIndex);

		int[] columnOrder = parent.getColumnOrder();
		int visualColumnIndex = columnIndex;
		for (int i = 0; i < columnOrder.length; i++) {
			if (columnOrder[i] == columnIndex) {
				visualColumnIndex = i;
				break;
			}
		}

		for (int i = 0; i <= span; i++) {
			if (visualColumnIndex + i >= columnOrder.length) {
				break;
			}
			int nextColumnIndex = columnOrder[visualColumnIndex + i];
			width += parent.getColumn(nextColumnIndex).getWidth(extraFill);
		}

		/* height */
		int indexOfCurrentItem = parent.getIndexOfItem(this);

		GridItem item = parent.getItem(indexOfCurrentItem);
		int height = item.getHeight();
		span = getRowSpan(columnIndex);

		int itemCount = parent.getItemCount();

		for (int i = 1; i <= span; i++) {
			/* We will probably need another escape condition here */
			if (itemCount <= indexOfCurrentItem + i) {
				break;
			}

			item = parent.getItem(indexOfCurrentItem + i);
			if (item.isVisible()) {
				height += item.getHeight() + 1;
			}
		}

		return new Point(width, height);
	}

	/**
	 * Returns the checked state at the first column in the receiver.
	 *
	 * @return the checked state
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getChecked() {
		checkWidget();
		return parent.getDataVisualizer().getChecked(this, 0);
	}

	/**
	 * Returns the checked state at the given column index in the receiver.
	 *
	 * @param index
	 *            the column index
	 * @return the checked state
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getChecked(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getChecked(this, index);
	}

	/**
	 * Returns the column span for the given column index in the receiver.
	 *
	 * @param index
	 *            the column index
	 * @return the number of columns spanned (0 equals no columns spanned)
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getColumnSpan(int index) {
		checkWidget();
		return parent.getDataVisualizer().getColumnSpan(this, index);
	}

	/**
	 * Returns the row span for the given column index in the receiver.
	 *
	 * @param index
	 *            the row index
	 * @return the number of row spanned (0 equals no row spanned)
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getRowSpan(int index) {
		checkWidget();

		return parent.getDataVisualizer().getRowSpan(this, index);
	}

	/**
	 * Returns the font that the receiver will use to paint textual information
	 * for this item.
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
	public Font getFont() {
		if (defaultFont == null) {
			return parent.getFont();
		}
		return defaultFont;
	}

	/**
	 * Returns the font that the receiver will use to paint textual information
	 * for the specified cell in this item.
	 *
	 * @param index
	 *            the column index
	 * @return the receiver's font
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Font getFont(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getFont(this, index);
	}

	/**
	 * Returns the foreground color that the receiver will use to draw.
	 *
	 * @return the receiver's foreground color
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Color getForeground() {
		if (defaultForeground == null) {
			return parent.getForeground();
		}
		return defaultForeground;
	}

	/**
	 * Returns the foreground color at the given column index in the receiver.
	 *
	 * @param index
	 *            the column index
	 * @return the foreground color
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Color getForeground(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getForeground(this, index);
	}

	/**
	 * Returns <code>true</code> if the first column in the receiver is grayed,
	 * and false otherwise. When the GridColumn does not have the
	 * <code>CHECK</code> style, return false.
	 *
	 * @return the grayed state of the checkbox
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getGrayed() {
		return getGrayed(0);
	}

	/**
	 * Returns <code>true</code> if the column at the given index in the
	 * receiver is grayed, and false otherwise. When the GridColumn does not
	 * have the <code>CHECK</code> style, return false.
	 *
	 * @param index
	 *            the column index
	 * @return the grayed state of the checkbox
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getGrayed(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getGrayed(this, index);
	}

	/**
	 * Returns the height of this <code>GridItem</code>.
	 *
	 * @return height of this <code>GridItem</code>
	 */
	public int getHeight() {
		checkWidget();
		return height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {
		checkWidget();
		return parent.getDataVisualizer().getImage(this, 0);
	}

	/**
	 * Returns the image stored at the given column index in the receiver, or
	 * null if the image has not been set or if the column does not exist.
	 *
	 * @param index
	 *            the column index
	 * @return the image stored at the given column index in the receiver
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Image getImage(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getImage(this, index);
	}

	/**
	 * Returns the item at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range.
	 *
	 * @param index
	 *            the index of the item to return
	 * @return the item at the given index
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_RANGE - if the index is not between 0 and
	 *             the number of elements in the list minus 1 (inclusive)</li>
	 *             </ul>
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public GridItem getItem(int index) {
		checkWidget();
		if (!hasChildren)
			throw new IllegalArgumentException("GridItem has no children!");
		return children.get(index);
	}

	/**
	 * Returns the number of items contained in the receiver that are direct
	 * item children of the receiver.
	 *
	 * @return the number of items
	 *
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getItemCount() {
		checkWidget();
		if (!hasChildren)
			return 0;
		return children.size();
	}

	/**
	 * Searches the receiver's list starting at the first item (index 0) until
	 * an item is found that is equal to the argument, and returns the index of
	 * that item. If no item is found, returns -1.
	 *
	 * @param item
	 *            the search item
	 * @return the index of the item
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the item is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the item has been disposed
	 *                </li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int indexOf(GridItem item) {
		checkWidget();
		if (item == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (item.isDisposed())
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		if (!hasChildren)
			throw new IllegalArgumentException("GridItem has no children!");

		return children.indexOf(item);
	}

	/**
	 * Returns a (possibly empty) array of <code>GridItem</code>s which are the
	 * direct item children of the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the receiver's items
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public GridItem[] getItems() {
		if (!hasChildren)
			return new GridItem[0];
		return children.toArray(new GridItem[children.size()]);
	}

	/**
	 * Returns the level of this item in the tree.
	 *
	 * @return the level of the item in the tree
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getLevel() {
		checkWidget();
		return level;
	}

	/**
	 * Returns the receiver's parent, which must be a <code>Grid</code>.
	 *
	 * @return the receiver's parent
	 * @throws SWTException
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
	 * Returns the receiver's parent item, which must be a <code>GridItem</code>
	 * or null when the receiver is a root.
	 *
	 * @return the receiver's parent item
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public GridItem getParentItem() {
		checkWidget();
		return parentItem;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		checkWidget();
		return parent.getDataVisualizer().getText(this, 0);
	}

	/**
	 * Returns the text stored at the given column index in the receiver, or
	 * empty string if the text has not been set.
	 *
	 * @param index
	 *            the column index
	 * @return the text stored at the given column index in the receiver
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public String getText(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getText(this, index);
	}

	/**
	 * Returns true if this item has children.
	 *
	 * @return true if this item has children
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean hasChildren() {
		checkWidget();
		return hasChildren;
	}

	/**
	 * Returns <code>true</code> if the receiver is expanded, and false
	 * otherwise.
	 * <p>
	 *
	 * @return the expanded state
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean isExpanded() {
		checkWidget();
		return expanded;
	}

	/**
	 * Sets the receiver's background color to the color specified by the
	 * argument, or to the default system color for the item if the argument is
	 * null.
	 *
	 * @param background
	 *            the new color (or null)
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *             disposed</li>
	 *             </ul>
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setBackground(Color background) {
		checkWidget();

		if (background != null && background.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		for (int i = 0; i < getParent().getColumnCount(); i++) {
			setBackground(i, background);
		}
		defaultBackground = background;
		parent.redraw();
	}

	/**
	 * Sets the background color at the given column index in the receiver to
	 * the color specified by the argument, or to the default system color for
	 * the item if the argument is null.
	 *
	 * @param index
	 *            the column index
	 * @param background
	 *            the new color (or null)
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *             disposed</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setBackground(int index, Color background) {
		checkWidget();
		if (background != null && background.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		parent.getDataVisualizer().setBackground(this, index, background);
		parent.redraw();
	}

	/**
	 * Sets the checked state at the first column in the receiver.
	 *
	 * @param checked
	 *            the new checked state
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setChecked(boolean checked) {
		checkWidget();
		parent.getDataVisualizer().setChecked(this, 0, checked);
		parent.redraw();
	}

	/**
	 * Sets the checked state at the given column index in the receiver.
	 *
	 * @param index
	 *            the column index
	 * @param checked
	 *            the new checked state
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setChecked(int index, boolean checked) {
		checkWidget();
		parent.getDataVisualizer().setChecked(this, index, checked);
		parent.redraw();
	}

	/**
	 * Sets the column spanning for the column at the given index to span the
	 * given number of subsequent columns.
	 *
	 * @param index
	 *            column index that should span
	 * @param span
	 *            number of subsequent columns to span
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setColumnSpan(int index, int span) {
		checkWidget();
		parent.getDataVisualizer().setColumnSpan(this, index, span);
		parent.setHasSpanning(true);
		parent.redraw();
	}

	/**
	 * Sets the row spanning for the row at the given index to span the given
	 * number of subsequent rows.
	 *
	 * @param index
	 *            row index that should span
	 * @param span
	 *            number of subsequent rows to span
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setRowSpan(int index, int span) {
		checkWidget();
		parent.getDataVisualizer().setRowSpan(this, index, span);
		parent.setHasSpanning(true);
		parent.redraw();
	}

	/**
	 * Sets the expanded state of the receiver.
	 * <p>
	 *
	 * @param expanded
	 *            the new expanded state
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setExpanded(boolean expanded) {
		checkWidget();
		this.expanded = expanded;

		// We must unselect any items that are becoming invisible
		// and thus if we change the selection we have to fire a selection event
		boolean unselected = doUnselect(expanded);

		this.getParent().topIndex = NO_ROW;
		this.getParent().bottomIndex = NO_ROW;
		this.getParent().setScrollValuesObsolete();

		if (unselected) {
			Event e = new Event();
			e.item = this;
			getParent().notifyListeners(SWT.Selection, e);
		}
		if (getParent().getFocusItem() != null && !getParent().getFocusItem().isVisible()) {
			getParent().setFocusItem(this);
		}

		if (getParent().getCellSelectionEnabled()) {
			getParent().updateColumnSelection();
		}
	}

	private boolean doUnselect(boolean expanded) {
		boolean unselected = false;

		if (hasChildren)
			for (GridItem item : children) {
				item.setVisible(expanded && visible);
				if (!expanded) {
					if (!getParent().getCellSelectionEnabled()) {
						if (getParent().isSelected(item)) {
							unselected = true;
							getParent().deselect(item.getRowIndex());
						}
						if (deselectChildren(item)) {
							unselected = true;
						}
					} else {
						if (deselectCells(item)) {
							unselected = true;
						}
					}
				}
			}
		return unselected;
	}

	private boolean deselectCells(GridItem item) {
		boolean flag = false;

		int index = item.getRowIndex();

		GridColumn[] columns = getParent().getColumns();

		for (int i = 0; i < columns.length; i++) {
			Point cell = new Point(columns[i].index, index);
			if (getParent().isCellSelected(cell)) {
				flag = true;
				getParent().deselectCell(cell);
			}
		}

		GridItem[] kids = item.getItems();
		for (int i = 0; i < kids.length; i++) {
			if (deselectCells(kids[i])) {
				flag = true;
			}
		}

		return flag;
	}

	/**
	 * Deselects the given item's children recursively.
	 *
	 * @param item
	 *            item to deselect children.
	 * @return true if an item was deselected
	 */
	private boolean deselectChildren(GridItem item) {
		boolean flag = false;
		GridItem[] kids = item.getItems();
		for (int i = 0; i < kids.length; i++) {
			if (getParent().isSelected(kids[i])) {
				flag = true;
			}
			getParent().deselect(kids[i].getRowIndex());
			if (deselectChildren(kids[i])) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * Sets the font that the receiver will use to paint textual information for
	 * this item to the font specified by the argument, or to the default font
	 * for that kind of control if the argument is null.
	 *
	 * @param f
	 *            the new font (or null)
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *             disposed</li>
	 *             </ul>
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setFont(Font f) {
		checkWidget();
		if (f != null && f.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		defaultFont = f;
		parent.redraw();
	}

	/**
	 * Sets the font that the receiver will use to paint textual information for
	 * the specified cell in this item to the font specified by the argument, or
	 * to the default font for that kind of control if the argument is null.
	 *
	 * @param index
	 *            the column index
	 * @param font
	 *            the new font (or null)
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *             disposed</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setFont(int index, Font font) {
		checkWidget();
		if (font != null && font.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		parent.getDataVisualizer().setFont(this, index, font);
		parent.redraw();
	}

	/**
	 * Sets the receiver's foreground color to the color specified by the
	 * argument, or to the default system color for the item if the argument is
	 * null.
	 *
	 * @param foreground
	 *            the new color (or null)
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *             disposed</li>
	 *             </ul>
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setForeground(Color foreground) {
		checkWidget();
		if (foreground != null && foreground.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		for (int i = 0; i < getParent().getColumnCount(); i++) {
			setForeground(i, foreground);
		}
		defaultForeground = foreground;
		parent.redraw();
	}

	/**
	 * Sets the foreground color at the given column index in the receiver to
	 * the color specified by the argument, or to the default system color for
	 * the item if the argument is null.
	 *
	 * @param index
	 *            the column index
	 * @param foreground
	 *            the new color (or null)
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *             disposed</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setForeground(int index, Color foreground) {
		checkWidget();
		if (foreground != null && foreground.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		parent.getDataVisualizer().setForeground(this, index, foreground);
		parent.redraw();
	}

	/**
	 * Sets the grayed state of the checkbox for the first column. This state
	 * change only applies if the GridColumn was created with the SWT.CHECK
	 * style.
	 *
	 * @param grayed
	 *            the new grayed state of the checkbox;
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setGrayed(boolean grayed) {
		checkWidget();
		parent.getDataVisualizer().setGrayed(this, 0, grayed);
		parent.redraw();
	}

	/**
	 * Sets the grayed state of the checkbox for the given column index. This
	 * state change only applies if the GridColumn was created with the
	 * SWT.CHECK style.
	 *
	 * @param index
	 *            the column index
	 * @param grayed
	 *            the new grayed state of the checkbox;
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setGrayed(int index, boolean grayed) {
		checkWidget();
		parent.getDataVisualizer().setGrayed(this, index, grayed);
		parent.redraw();
	}

	/**
	 * Sets the height of this <code>GridItem</code>.
	 *
	 * @param newHeight
	 *            new height in pixels
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setHeight(int newHeight) {
		checkWidget();
		if (newHeight < 1)
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		height = newHeight;
		parent.hasDifferingHeights = true;
		if (isVisible()) {
			int myIndex = this.getRowIndex();
			// note: cannot use Grid#isShown() here, because that returns false
			// for partially shown items
			if (parent.getTopIndex() <= myIndex && myIndex <= parent.getBottomIndex())
				parent.bottomIndex = NO_ROW;
		}
		parent.setScrollValuesObsolete();
		parent.redraw();
	}

	/**
	 * Sets this <code>GridItem</code> to its preferred height.
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

		int maxPrefHeight = 2;
		GridColumn[] columns = parent.getColumns();
		GC gc = new GC(parent);
		for (int cnt = 0; cnt < columns.length; cnt++) {
			if (!columns[cnt].isVisible())
				continue; // invisible columns do not affect item/row height

			GridCellRenderer renderer = columns[cnt].getCellRenderer();

			renderer.setAlignment(columns[cnt].getAlignment());
			renderer.setCheck(columns[cnt].isCheck());
			renderer.setColumn(cnt);
			renderer.setTree(columns[cnt].isTree());
			renderer.setWordWrap(columns[cnt].getWordWrap());

			Point size = renderer.computeSize(gc, columns[cnt].getWidth(), SWT.DEFAULT, this);
			if (size != null)
				maxPrefHeight = Math.max(maxPrefHeight, size.y);
		}
		gc.dispose();

		setHeight(maxPrefHeight);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setImage(Image image) {
		parent.getDataVisualizer().setImage(this, 0, image);
		parent.redraw();
	}

	/**
	 * Sets the receiver's image at a column.
	 *
	 * @param index
	 *            the column index
	 * @param image
	 *            the new image
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_INVALID_ARGUMENT - if the image has been disposed
	 *             </li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setImage(int index, Image image) {
		checkWidget();
		if (image != null && image.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		parent.getDataVisualizer().setImage(this, index, image);

		parent.imageSetOnItem(index, this);

		parent.redraw();
	}

	/**
	 * Sets the receiver's text at a column.
	 *
	 * @param index
	 *            the column index
	 * @param text
	 *            the new text
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setText(int index, String text) {
		checkWidget();
		if (text == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		parent.getDataVisualizer().setText(this, index, text);
		parent.redraw();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String string) {
		parent.getDataVisualizer().setText(this, 0, string);
		parent.redraw();
	}

	/**
	 * Removes the given child item from the list of children.
	 *
	 * @param child
	 *            child to remove
	 */
	private void remove(GridItem child) {
		if (!hasChildren)
			throw new IllegalArgumentException("GridItem has no children!");
		children.remove(child);
		parent.getDataVisualizer().clearRow(child);
		hasChildren = !children.isEmpty();
	}

	/**
	 * Returns true if the item is visible because its parent items are all
	 * expanded. This method does not determine if the item is in the currently
	 * visible range.
	 *
	 * @return Returns the visible.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Creates a new child item in this item at the given index.
	 *
	 * @param item
	 *            new child item
	 * @param index
	 *            index
	 */
	void newItem(GridItem item, int index) {
		setHasChildren(true);
		if (children == null)
			children = new ArrayList<>();
		if (index == NO_ROW) {
			children.add(item);
		} else {
			children.add(index, item);
		}
	}

	/**
	 * Sets whether this item has children.
	 *
	 * @param hasChildren
	 *            true if this item has children
	 */
	void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	/**
	 * Sets the visible state of this item. The visible state is determined by
	 * the expansion state of all of its parent items. If all parent items are
	 * expanded it is visible.
	 *
	 * @param visible
	 *            The visible to set.
	 */
	void setVisible(boolean visible) {
		if (this.visible == visible) {
			return;
		}

		this.visible = visible;

		if (visible) {
			parent.updateVisibleItems(1);
		} else {
			parent.updateVisibleItems(NO_ROW);
		}

		if (hasChildren) {
			boolean childrenVisible = visible;
			if (visible) {
				childrenVisible = expanded;
			}
			for (GridItem item : children) {
				item.setVisible(childrenVisible);
			}
		}
	}

	/**
	 * Returns the receiver's row header text. If the text is <code>null</code>
	 * the row header will display the row number.
	 *
	 * @return the text stored for the row header or code <code>null</code> if
	 *         the default has to be displayed
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public String getHeaderText() {
		checkWidget();

		// handleVirtual();

		return headerText;
	}

	/**
	 * Returns the receiver's row header image.
	 *
	 * @return the image stored for the header or <code>null</code> if none has
	 *         to be displayed
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Image getHeaderImage() {
		checkWidget();
		return headerImage;
	}

	/**
	 * Returns the receiver's row header background color
	 *
	 * @return the color or <code>null</code> if none
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Color getHeaderBackground() {
		checkWidget();
		return headerBackground;
	}

	/**
	 * Returns the receiver's row header foreground color
	 *
	 * @return the color or <code>null</code> if none
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Color getHeaderForeground() {
		checkWidget();
		return headerForeground;
	}

	/**
	 * Returns the receiver's row header font
	 *
	 * @return the font or <code>null</code> if none
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public Font getHeaderFont() {
		checkWidget();
		return headerFont;
	}

	/**
	 * Sets the receiver's row header text. If the text is <code>null</code> the
	 * row header will display the row number.
	 *
	 * @param text
	 *            the new text
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *             </ul>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setHeaderText(String text) {
		checkWidget();
		// if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (text != headerText) {
			GC gc = new GC(parent);

			int oldWidth = headerText == null ? 0
					: parent.getRowHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, this).x;

			this.headerText = text;

			int newWidth = parent.getRowHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, this).x;

			gc.dispose();

			parent.recalculateRowHeaderWidth(this, oldWidth, newWidth);
		}
		parent.redraw();
	}

	/**
	 * Sets the receiver's row header image. If the image is <code>null</code>
	 * none is shown in the header
	 *
	 * @param image
	 *            the new image
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setHeaderImage(Image image) {
		checkWidget();
		// if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (image != headerImage) {
			GC gc = new GC(parent);

			int oldWidth = parent.getRowHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, this).x;
			int oldHeight = parent.getRowHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, this).y;

			this.headerImage = image;

			int newWidth = parent.getRowHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, this).x;
			int newHeight = parent.getRowHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, this).y;

			gc.dispose();

			parent.recalculateRowHeaderWidth(this, oldWidth, newWidth);
			parent.recalculateRowHeaderHeight(this, oldHeight, newHeight);
		}
		parent.redraw();
	}

	/**
	 * Set the new header background
	 *
	 * @param headerBackground
	 *            the color or <code>null</code>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setHeaderBackground(Color headerBackground) {
		checkWidget();
		this.headerBackground = headerBackground;
		parent.redraw();
	}

	/**
	 * Set the new header foreground
	 *
	 * @param headerForeground
	 *            the color or <code>null</code>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setHeaderForeground(Color headerForeground) {
		checkWidget();
		this.headerForeground = headerForeground;
		parent.redraw();
	}

	/**
	 * Set the new header font
	 *
	 * @param headerFont
	 *            the font or <code>null</code>
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setHeaderFont(Font headerFont) {
		checkWidget();
		this.headerFont=headerFont;
		parent.redraw();
	}

	/**
	 * Returns the checkable state at the given column index in the receiver. If
	 * the column at the given index is not checkable then this will return
	 * false regardless of the individual cell's checkable state.
	 *
	 * @param index
	 *            the column index
	 * @return the checked state
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public boolean getCheckable(int index) {
		checkWidget();

		if (!parent.getColumn(index).getCheckable())
			return false;

		return parent.getDataVisualizer().getCheckable(this, index);
	}

	/**
	 * Sets the checkable state at the given column index in the receiver. A
	 * checkbox which is uncheckable will not be modifiable by the user but
	 * still make be modified programmatically. If the column at the given index
	 * is not checkable then individual cell will not be checkable regardless.
	 *
	 * @param index
	 *            the column index
	 * @param checked
	 *            the new checked state
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setCheckable(int index, boolean checked) {
		checkWidget();
		parent.getDataVisualizer().setCheckable(this, index, checked);
	}

	/**
	 * Returns the tooltip for the given cell.
	 *
	 * @param index
	 *            the column index
	 * @return the tooltip
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public String getToolTipText(int index) {
		checkWidget();

		handleVirtual();

		return parent.getDataVisualizer().getToolTipText(this, index);
	}

	/**
	 * Sets the tooltip for the given column index.
	 *
	 * @param index
	 *            the column index
	 * @param tooltip
	 *            the tooltip text
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public void setToolTipText(int index, String tooltip) {
		checkWidget();
		parent.getDataVisualizer().setToolTipText(this, index, tooltip);
	}

	void columnAdded(int index) {
		hasSetData = false;
	}

	private void handleVirtual() {
		if ((getParent().getStyle() & SWT.VIRTUAL) != 0 && !hasSetData) {
			hasSetData = true;
			Event event = new Event();
			event.item = this;
			if (parentItem == null) {
				event.index = getRowIndex();
			} else {
				event.index = parentItem.indexOf(this);
			}
			getParent().notifyListeners(SWT.SetData, event);
		}
	}

	/**
	 * Sets the initial item height for this item.
	 *
	 * @param height
	 *            initial height.
	 */
	void initializeHeight(int height) {
		this.height = height;
	}

	void setHasSetData(boolean hasSetData) {
		this.hasSetData = hasSetData;
	}

	/**
	 * Clears all properties of this item and resets values to their defaults.
	 *
	 * @param allChildren
	 *            <code>true</code> if all child items should be cleared
	 *            recursively, and <code>false</code> otherwise
	 */
	void clear(boolean allChildren) {

		defaultForeground = null;
		defaultBackground = null;
		defaultFont = null;

		hasSetData = false;
		headerText = null;
		headerImage = null;
		headerBackground = null;
		headerForeground = null;

		// Recursively clear children if requested.
		if (allChildren && hasChildren) {
			for (int i = children.size() - 1; i >= 0; i--) {
				children.get(i).clear(true);
			}
		}

	}

	/**
	 * this method call only super.dispose, nothing else..
	 */
	public void disposeOnly() {
		if (hasChildren)
			for (int i = children.size() - 1; i >= 0; i--) {
				children.get(i).disposeOnly();
			}
		if (parent.getDataVisualizer() != null) {
			parent.getDataVisualizer().clearRow(this);
		}
		noRow();
		super.dispose();
	}

	private void noRow() {
		synchronized (ROW_LOCK) {
			row = NO_ROW;
		}
	}
}
