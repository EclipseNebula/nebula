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
 *    Claes Rosell<claes.rosell@solme.se> - rowspan in bug 272384
 *    Marco Maccaferri<macca@maccasoft.com> - fixed arrow scrolling in bug 294767
 *    higerinbeijing@gmail.com . fixed selectionEvent.item in bug 286617
 *    balarkrishnan@yahoo.com - fix in bug 298684
 *    Enrico Schnepel<enrico.schnepel@randomice.net> - new API in 238729, bugfix in 294952, 322114
 *    Benjamin Bortfeldt<bbortfeldt@gmail.com> - new tooltip support in 300797
 *    Thomas Halm <thha@fernbach.com> - bugfix in 315397
 *    Justin Dolezy <justin@neckdiagrams.com> - bugfix in 316598
 *    Cosmin Ghita <cghita@ansis.eu> - bugfix in 323687
 *    Pinard-Legry Guilhaume <guilhaume_pl@yahoo.fr> - bugfix in 267057
 *    Thorsten Schenkel <thorsten.schenkel@compeople.de> - bugfix in 356803
 *    Mirko Paturzo <mirko.paturzo@exeura.eu> - improvement (bugfix in 419928)
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import org.eclipse.nebula.widgets.grid.internal.DefaultBottomLeftRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultColumnGroupHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultDropPointRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultEmptyColumnFooterRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultEmptyColumnHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultEmptyRowHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultFocusRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultInsertMarkRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultRowHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.DefaultTopLeftRenderer;
import org.eclipse.nebula.widgets.grid.internal.GridToolTip;
import org.eclipse.nebula.widgets.grid.internal.IScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.NullScrollBarProxy;
import org.eclipse.nebula.widgets.grid.internal.ScrollBarProxyAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * The Grid widget is a spreadsheet/table component that offers features not
 * currently found in the base SWT Table. Features include cell selection,
 * column grouping, column spanning, row headers, and more.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type {@code GridItem}.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.SINGLE, SWT.MULTI, SWT.NO_FOCUS, SWT.CHECK, SWT.VIRTUAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 *
 * @author chris.gross@us.ibm.com
 * @author Mirko Paturzo <mirko.paturzo@exeura.eu>
 *
 */
public class Grid extends Canvas {

	/**
	 * Cached default font of Control.getFont.
	 */
	private Font defaultFont;

	// TODO: figure out better way to allow renderers to trigger events
	// TODO: scroll as necessary when performing drag select (current strategy ok)
	// TODO: need to refactor the way the range select remembers older selection
	// TODO: remember why i decided i needed to refactor the way the range select
	// remembers older selection
	// TODO: need to alter how column drag selection works to allow selection of
	// spanned cells
	// TODO: JAVADOC!
	// TODO: column freezing

	// TODO: Performance - need to cache top index

	/**
	 * @return {@link DataVisualizer}
	 */
	public DataVisualizer getDataVisualizer() {
		return dataVisualizer;
	}

	/**
	 * Object holding the visible range
	 */
	public static class GridVisibleRange {
		private GridItem[] items = {};
		private GridColumn[] columns = {};

		/**
		 * @return the current items shown
		 */
		public GridItem[] getItems() {
			return items;
		}

		/**
		 * @return the current columns shown
		 */
		public GridColumn[] getColumns() {
			return columns;
		}
	}

	/**
	 * Clear simply all GridItems
	 */
	public void clearItems() {
		items.clear();
		rootItems.clear();
		deselectAll();
		redraw();
	}

	/**
	 * Accessibility default action for column headers and column group headers.
	 */
	private static final String ACC_COLUMN_DEFAULT_ACTION = "Click";

	/**
	 * Accessibility default action for items.
	 */
	private static final String ACC_ITEM_DEFAULT_ACTION = "Double Click";

	/**
	 * Accessibility expand action for tree items.
	 */
	private static final String ACC_ITEM_ACTION_EXPAND = "Expand";

	/**
	 * Accessibility collapse action for tree items.
	 */
	private static final String ACC_ITEM_ACTION_COLLAPSE = "Collapse";

	/**
	 * Accessibility name for the column group header toggle button.
	 */
	private static final String ACC_TOGGLE_BUTTON_NAME = "Toggle Button";

	/**
	 * Alpha blending value used when drawing the dragged column header.
	 */
	private static final int COLUMN_DRAG_ALPHA = 128;

	/**
	 * Number of pixels below the header to draw the drop point.
	 */
	private static final int DROP_POINT_LOWER_OFFSET = 3;

	/**
	 * Horizontal scrolling increment, in pixels.
	 */
	private static final int HORZ_SCROLL_INCREMENT = 5;

	/**
	 * The area to the left and right of the column boundary/resizer that is still
	 * considered the resizer area. This prevents a user from having to be *exactly*
	 * over the resizer.
	 */
	private static final int COLUMN_RESIZER_THRESHOLD = 4;

	/**
	 * @see #COLUMN_RESIZER_THRESHOLD
	 */
	private static final int ROW_RESIZER_THRESHOLD = 3;

	/**
	 * The minimum width of a column header.
	 */
	private static final int MIN_COLUMN_HEADER_WIDTH = 20;
	/**
	 * The minimum height of a row header.
	 */
	private static final int MIN_ROW_HEADER_HEIGHT = 10;

	/**
	 * The number used when sizing the row header (i.e. size it for '1000')
	 * initially.
	 */
	// private static final int INITIAL_ROW_HEADER_SIZING_VALUE = 1000;

	/**
	 * The factor to multiply the current row header sizing value by when
	 * determining the next sizing value. Used for performance reasons.
	 */
	// private static final int ROW_HEADER_SIZING_MULTIPLIER = 10;

	/**
	 * Tracks whether the scroll values are correct. If not they will be recomputed
	 * in onPaint. This allows us to get a free ride on top of the OS's paint event
	 * merging to assure that we don't perform this expensive operation when
	 * unnecessary.
	 */
	private boolean scrollValuesObsolete = false;

	/**
	 * When this variable is true, the pack is based only on the visible lines on
	 * the screen.
	 */
	private boolean visibleLinesBasedColumnPack = false;

	/**
	 * All items in the table, not just root items.
	 */
	private final List<GridItem> items = new ArrayList<>();

	/**
	 * All root items.
	 */
	private final List<GridItem> rootItems = new ArrayList<>();

	/**
	 * List of selected items.
	 */
	private final List<GridItem> selectedItems = new ArrayList<>();

	/**
	 * Reference to the item in focus.
	 */
	private GridItem focusItem;

	private boolean cellSelectionEnabled = false;
	private boolean cellDragSelectionEnabled = true;

	private final List<Point> selectedCells = new ArrayList<>();
	private final List<Point> selectedCellsBeforeRangeSelect = new ArrayList<>();

	private boolean cellDragSelectionOccuring = false;
	private boolean cellRowDragSelectionOccuring = false;
	private boolean cellColumnDragSelectionOccuring = false;
	private boolean cellDragCTRL = false;
	private boolean followupCellSelectionEventOwed = false;

	private boolean cellSelectedOnLastMouseDown;
	private boolean cellRowSelectedOnLastMouseDown;
	private boolean cellColumnSelectedOnLastMouseDown;

	private GridColumn shiftSelectionAnchorColumn;

	private GridColumn focusColumn;

	private final List<GridColumn> selectedColumns = new ArrayList<>();

	/**
	 * This is the column that the user last navigated to, but may not be the
	 * focusColumn because that column may be spanned in the current row. This is
	 * only used in situations where the user has used the keyboard to navigate up
	 * or down in the table and the focusColumn has switched to a new column because
	 * the intended column (was maintained in this var) was spanned. The table will
	 * attempt to set focus back to the intended column during subsequent up/down
	 * navigations.
	 */
	private GridColumn intendedFocusColumn;

	/**
	 * List of table columns in creation/index order.
	 */
	private final List<GridColumn> columns = new ArrayList<>();

	/**
	 * List of the table columns in the order they are displayed.
	 */
	private final List<GridColumn> displayOrderedColumns = new ArrayList<>();

	private GridColumnGroup[] columnGroups = {};

	/**
	 * Renderer to paint the top left area when both column and row headers are
	 * shown.
	 */
	private IRenderer topLeftRenderer = new DefaultTopLeftRenderer();

	/**
	 * Renderer to paint the bottom left area when row headers and column footers
	 * are shown
	 */
	private IRenderer bottomLeftRenderer = new DefaultBottomLeftRenderer();

	/**
	 * Renderer used to paint row headers.
	 */
	private IRenderer rowHeaderRenderer = new DefaultRowHeaderRenderer();

	/**
	 * Renderer used to paint empty column headers, used when the columns don't fill
	 * the horz space.
	 */
	private IRenderer emptyColumnHeaderRenderer = new DefaultEmptyColumnHeaderRenderer();

	/**
	 * Renderer used to paint empty column footers, used when the columns don't fill
	 * the horz space.
	 */
	private IRenderer emptyColumnFooterRenderer = new DefaultEmptyColumnFooterRenderer();

	/**
	 * Renderer used to paint empty cells to fill horz and vert space.
	 */
	private GridCellRenderer emptyCellRenderer = new DefaultEmptyCellRenderer();

	/**
	 * Renderer used to paint empty row headers when the rows don't fill the
	 * vertical space.
	 */
	private IRenderer emptyRowHeaderRenderer = new DefaultEmptyRowHeaderRenderer();

	/**
	 * Renderers the UI affordance identifying where the dragged column will be
	 * dropped.
	 */
	private final IRenderer dropPointRenderer = new DefaultDropPointRenderer();

	/**
	 * Renderer used to paint on top of an already painted row to denote focus.
	 */
	private IRenderer focusRenderer = new DefaultFocusRenderer();

	/**
	 * Are row headers visible?
	 */
	private boolean rowHeaderVisible = false;

	/**
	 * Are column headers visible?
	 */
	private boolean columnHeadersVisible = false;

	/**
	 * Are column footers visible?
	 */
	private boolean columnFootersVisible = false;

	/**
	 * Type of selection behavior. Valid values are SWT.SINGLE and SWT.MULTI.
	 */
	private GridSelectionType selectionType = GridSelectionType.SINGLE;

	/**
	 * True if selection highlighting is enabled.
	 */
	private boolean selectionEnabled = true;

	/**
	 * Default height of items. This value is used for <code>GridItem</code>s with a
	 * height of -1.
	 */
	private int itemHeight = 1;

	private boolean userModifiedItemHeight = false;

	/**
	 * Width of each row header.
	 */
	private int rowHeaderWidth = 0;

	/**
	 * The row header width is variable. The row header width gets larger as more
	 * rows are added to the table to ensure that the row header has enough room to
	 * display the longest string of numbers that display in the row header. This
	 * determination of how wide to make the row header is rather slow and therefore
	 * is only done at every 1000 items (or so). This variable remembers how many
	 * items were last computed and therefore when the number of items is greater
	 * than this value, we need to recalculate the row header width. See newItem().
	 */
	// private int lastRowHeaderWidthCalculationAt = 0;

	/**
	 * Height of each column header.
	 */
	private int headerHeight = 0;

	/**
	 * Height of each column footer
	 */
	private int footerHeight = 0;

	/**
	 * True if mouse is hover on a column boundary and can resize the column.
	 */
	boolean hoveringOnColumnResizer = false;

	/**
	 * Reference to the column being resized.
	 */
	private GridColumn columnBeingResized;

	/**
	 * Are this <code>Grid</code>'s rows resizeable?
	 */
	private boolean rowsResizeable = false;

	/**
	 * Is the user currently resizing a column?
	 */
	private boolean resizingColumn = false;

	/**
	 * The mouse X position when the user starts the resize.
	 */
	private int resizingStartX = 0;

	/**
	 * The width of the column when the user starts the resize. This, together with
	 * the resizingStartX determines the current width during resize.
	 */
	private int resizingColumnStartWidth = 0;

	/**
	 * True if we're resizing the column to the right of a FILL column when dragging
	 * the FILL column's right edge (inverted resize). In this case, dragging right
	 * makes the column smaller, not larger.
	 */
	private boolean resizingInverted = false;

	private boolean hoveringOnRowResizer = false;
	private GridItem rowBeingResized;
	private boolean resizingRow = false;
	private int resizingStartY;
	private int resizingRowStartHeight;

	/**
	 * Reference to the column whose header is currently in a pushed state.
	 */
	private GridColumn columnBeingPushed;

	/**
	 * Is the user currently pushing a column header?
	 */
	private boolean pushingColumn = false;

	/**
	 * Is the user currently pushing a column header and hovering over that same
	 * header?
	 */
	private boolean pushingAndHovering = false;

	/**
	 * X position of the mouse when the user first pushes a column header.
	 */
	private int startHeaderPushX = 0;

	/**
	 * X position of the mouse when the user has initiated a drag. This is different
	 * than startHeaderPushX because the mouse is allowed some 'wiggle-room' until
	 * the header is put into drag mode.
	 */
	private int startHeaderDragX = 0;

	/**
	 * The current X position of the mouse during a header drag.
	 */
	private int currentHeaderDragX = 0;

	/**
	 * Are we currently dragging a column header?
	 */
	private boolean draggingColumn = false;

	private GridColumn dragDropBeforeColumn = null;

	private GridColumn dragDropAfterColumn = null;

	/**
	 * True if the current dragDropPoint is a valid drop point for the dragged
	 * column. This is false if the column groups are involved and a column is being
	 * dropped into or out of its column group.
	 */
	private boolean dragDropPointValid = true;

	/**
	 * Reference to the currently item that the mouse is currently hovering over.
	 */
	private GridItem hoveringItem;

	/**
	 * Reference to the column that the mouse is currently hovering over. Includes
	 * the header and all cells (all rows) in this column.
	 */
	private GridColumn hoveringColumn;

	private GridColumn hoveringColumnHeader;

	private GridColumnGroup hoverColumnGroupHeader;

	/**
	 * String-based detail of what is being hovered over in a cell. This allows a
	 * renderer to differentiate between hovering over different parts of the cell.
	 * For example, hovering over a checkbox in the cell or hovering over a tree
	 * node in the cell. The table does nothing with this string except to set it
	 * back in the renderer when its painted. The renderer sets this during its
	 * notify method (InternalWidget.HOVER) and the table pulls it back and
	 * maintains it so it can be set back when the cell is painted. The renderer
	 * determines what the hover detail means and how it affects painting.
	 */
	private String hoveringDetail = "";

	/**
	 * True if the mouse is hovering of a cell's text.
	 */
	private boolean hoveringOverText = false;

	/**
	 * Are the grid lines visible?
	 */
	private boolean linesVisible = true;

	/**
	 * Are tree lines visible?
	 */
	private boolean treeLinesVisible = true;

	/**
	 * Grid line color.
	 */
	private Color lineColor;

	/**
	 * Vertical scrollbar proxy.
	 * <p>
	 * Note:
	 * <ul>
	 * <li>{@link Grid#getTopIndex()} is the only method allowed to call
	 * vScroll.getSelection() (except #updateScrollbars() of course)</li>
	 * <li>{@link Grid#setTopIndex(int)} is the only method allowed to call
	 * vScroll.setSelection(int)</li>
	 * </ul>
	 */
	private IScrollBarProxy vScroll;

	/**
	 * Horizontal scrollbar proxy.
	 */
	private IScrollBarProxy hScroll;

	/**
	 * The number of GridItems whose visible = true. Maintained for performance
	 * reasons (rather than iterating over all items).
	 */
	private int currentVisibleItems = 0;

	/**
	 * Item selected when a multiple selection using shift+click first occurs. This
	 * item anchors all further shift+click selections.
	 */
	private GridItem shiftSelectionAnchorItem;

	private boolean columnScrolling = false;

	private int groupHeaderHeight;

	private Color cellHeaderSelectionBackground;

	/**
	 * Dispose listener. This listener is removed during the dispose event to allow
	 * re-firing of the event.
	 */
	private Listener disposeListener;

	/**
	 * The inplace tooltip.
	 */
	private GridToolTip inplaceToolTip;

	private Color backgroundColor;

	/**
	 * True if the widget is being disposed. When true, events are not fired.
	 */
	private boolean disposing = false;

	/**
	 * True if there is at least one tree node. This is used by accessibility and
	 * various places for optimization.
	 */
	private boolean isTree = false;

	/**
	 * True if there is at least one <code>GridItem</code> with an individual
	 * height. This value is only set to true in
	 * {@link GridItem#setHeight(int,boolean)} and it is never reset to false.
	 */
	boolean hasDifferingHeights = false;

	/**
	 * True if three is at least one cell spanning columns. This is used in various
	 * places for optimizatoin.
	 */
	private boolean hasSpanning = false;

	/**
	 * Index of first visible item. The value must never be read directly. It is
	 * cached and updated when appropriate. #getTopIndex should be called for every
	 * client (even internal callers). A value of -1 indicates that the value is old
	 * and will be recomputed.
	 *
	 * @see #bottomIndex
	 */
	int topIndex = -1;
	/**
	 * Index of last visible item. The value must never be read directly. It is
	 * cached and updated when appropriate. #getBottomIndex() should be called for
	 * every client (even internal callers). A value of -1 indicates that the value
	 * is old and will be recomputed.
	 * <p>
	 * Note that the item with this index is often only partly visible; maybe only a
	 * single line of pixels is visible. In extreme cases, bottomIndex may be the
	 * same as topIndex.
	 *
	 * @see #topIndex
	 */
	int bottomIndex = -1;

	/**
	 * Index of the first visible column. A value of -1 indicates that the value is
	 * old and will be recomputed.
	 */
	int startColumnIndex = -1;

	/**
	 * Index of the the last visible column. A value of -1 indicates that the value
	 * is old and will be recomputed.
	 */
	int endColumnIndex = -1;

	/**
	 * True if the last visible item is completely visible. The value must never be
	 * read directly. It is cached and updated when appropriate. #isShown() should
	 * be called for every client (even internal callers).
	 *
	 * @see #bottomIndex
	 */
	private boolean bottomIndexShownCompletely = false;

	/**
	 * Tooltip text - overriden because we have cell specific tooltips
	 */
	private String toolTipText = null;

	/**
	 * Flag that is set to true as soon as one image is set on any one item. This is
	 * used to mimic Table behavior that resizes the rows on the first image added.
	 * See imageSetOnItem.
	 */
	private boolean firstImageSet = false;

	/**
	 * Mouse capture flag. Used for inplace tooltips. This flag must be used to
	 * ensure that we don't setCapture(false) in situations where we didn't do
	 * setCapture(true). The OS (SWT?) will automatically capture the mouse for us
	 * during a drag operation.
	 */
	private boolean inplaceTooltipCapture;

	/**
	 * This is the tooltip text currently used. This could be the tooltip text for
	 * the currently hovered cell, or the general grid tooltip. See handleCellHover.
	 */
	private String displayedToolTipText;

	/**
	 * The height of the area at the top and bottom of the visible grid area in
	 * which scrolling is initiated while dragging over this Grid.
	 */
	private static final int DRAG_SCROLL_AREA_HEIGHT = 12;

	/**
	 * Threshold for the selection border used for drag n drop in mode
	 * (!{@link #dragOnFullSelection}}.
	 */
	private static final int SELECTION_DRAG_BORDER_THRESHOLD = 2;

	private boolean hoveringOnSelectionDragArea = false;

	private GridItem insertMarkItem = null;
	private GridColumn insertMarkColumn = null;
	private boolean insertMarkBefore = false;
	private final IRenderer insertMarkRenderer = new DefaultInsertMarkRenderer();
	private boolean sizeOnEveryItemImageChange;
	private boolean autoHeight = false;
	private boolean autoWidth = true;
	private boolean wordWrapRowHeader = false;

	private final DataVisualizer dataVisualizer;

	private Listener defaultKeyListener;

	private boolean defaultKeyListenerEnabled = true;

	/**
	 * caching column orders improves grid rendering
	 */
	private int[] columnOrders;

	/**
	 * If true, when user types TAB the selection moved to the next line, and
	 * SHIFT-TAB move to the previous line
	 */
	private boolean moveOnTab = false;

	/**
	 * A range of rows in a <code>Grid</code>.
	 * <p>
	 * A row in this sense exists only for visible items (i.e. items with
	 * {@link GridItem#isVisible()} == true). Therefore, the items at 'startIndex'
	 * and 'endIndex' are always visible.
	 *
	 * @see Grid#getRowRange(int, int, boolean, boolean)
	 */
	private static class RowRange {
		/** index of first item in range */
		public int startIndex;
		/** index of last item in range */
		public int endIndex;
		/** number of rows (i.e. <em>visible</em> items) in this range */
		public int rows;
		/**
		 * height in pixels of this range (including horizontal separator between rows)
		 */
		public int height;
	}

	/**
	 * Filters out unnecessary styles, adds mandatory styles and generally manages
	 * the style to pass to the super class.
	 *
	 * @param style user specified style.
	 * @return style to pass to the super class.
	 */
	private static int checkStyle(final int style) {
		final int mask = SWT.BORDER | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
				| SWT.MULTI | SWT.NO_FOCUS | SWT.CHECK | SWT.VIRTUAL;
		int newStyle = style & mask;
		newStyle |= SWT.DOUBLE_BUFFERED;
		return newStyle;
	}

	/**
	 * Grid with generic DataVisualizer
	 *
	 * @param parent component
	 * @param style  grid style
	 */
	public Grid(final Composite parent, final int style) {
		this(new GridItemDataVisualizer(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE),
				Display.getCurrent().getSystemColor(SWT.COLOR_BLACK), null), parent, style);
	}

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 * <p>
	 *
	 * @param dataVisualizer manage all data of grid and its items
	 * @param parent         a composite control which will be the parent of the new
	 *                       instance (cannot be null)
	 * @param style          the style of control to construct
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the parent
	 *                                      is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      parent</li>
	 *                                      </ul>
	 * @see SWT#SINGLE
	 * @see SWT#MULTI
	 */
	public Grid(final DataVisualizer dataVisualizer, final Composite parent, final int style) {
		super(parent, checkStyle(style));

		this.dataVisualizer = dataVisualizer;

		// initialize drag & drop support
		setData("DEFAULT_DRAG_SOURCE_EFFECT", new GridDragSourceEffect(this));
		setData("DEFAULT_DROP_TARGET_EFFECT", new GridDropTargetEffect(this));

		topLeftRenderer.setDisplay(getDisplay());
		bottomLeftRenderer.setDisplay(getDisplay());
		rowHeaderRenderer.setDisplay(getDisplay());
		emptyColumnHeaderRenderer.setDisplay(getDisplay());
		emptyColumnFooterRenderer.setDisplay(getDisplay());
		emptyCellRenderer.setDisplay(getDisplay());
		dropPointRenderer.setDisplay(getDisplay());
		focusRenderer.setDisplay(getDisplay());
		emptyRowHeaderRenderer.setDisplay(getDisplay());
		insertMarkRenderer.setDisplay(getDisplay());

		setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		setLineColor(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		if ((style & SWT.MULTI) != 0) {
			selectionType = GridSelectionType.MULTI;
		}

		if (getVerticalBar() != null) {
			getVerticalBar().setVisible(false);
			vScroll = new ScrollBarProxyAdapter(getVerticalBar());
		} else {
			vScroll = new NullScrollBarProxy();
		}

		if (getHorizontalBar() != null) {
			getHorizontalBar().setVisible(false);
			hScroll = new ScrollBarProxyAdapter(getHorizontalBar());
		} else {
			hScroll = new NullScrollBarProxy();
		}

		scrollValuesObsolete = true;

		initListeners();
		initAccessible();

		estimate(sizingGC -> itemHeight = sizingGC.getFontMetrics().getHeight() + 2);

		final RGB sel = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
		final RGB white = getDisplay().getSystemColor(SWT.COLOR_WHITE).getRGB();

		final RGB cellSel = blend(sel, white, 50);

		cellHeaderSelectionBackground = new Color(cellSel);

		setDragDetect(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Color getBackground() {
		checkWidget();
		if (backgroundColor == null) {
			return getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		}
		return backgroundColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBackground(final Color color) {
		checkWidget();
		backgroundColor = color;
		dataVisualizer.setDefaultBackground(color);
		redraw();
	}

	/**
	 * Returns the background color of column and row headers when a cell in the row
	 * or header is selected.
	 *
	 * @return cell header selection background color
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public Color getCellHeaderSelectionBackground() {
		checkWidget();
		return cellHeaderSelectionBackground;
	}

	/**
	 * Sets the background color of column and row headers displayed when a cell in
	 * the row or header is selected.
	 *
	 * @param cellSelectionBackground color to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setCellHeaderSelectionBackground(final Color cellSelectionBackground) {
		checkWidget();
		cellHeaderSelectionBackground = cellSelectionBackground;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the receiver's selection changes, by sending it one of the messages defined
	 * in the {@code SelectionListener} interface.
	 * <p>
	 * Cell selection events may have <code>Event.detail = SWT.DRAG</code> when the
	 * user is drag selecting multiple cells. A follow up selection event will be
	 * generated when the drag is complete.
	 *
	 * @param listener the listener which should be notified
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the
	 *                                      listener is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the receiver's items changes, by sending it one of the messages defined in
	 * the {@code TreeListener} interface.
	 *
	 * @param listener the listener which should be notified
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the
	 *                                      listener is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see TreeListener
	 * @see #removeTreeListener
	 * @see org.eclipse.swt.events.TreeEvent
	 */
	public void addTreeListener(final TreeListener listener) {
		addTypedListener(listener, SWT.Expand, SWT.Collapse);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();

		Point prefSize = null;
		if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
			prefSize = getTableSize();
			prefSize.x += 2 * getBorderWidth();
			prefSize.y += 2 * getBorderWidth();
		}

		int x = 0;
		int y = 0;

		if (wHint == SWT.DEFAULT) {
			x += prefSize.x;
			if (getVerticalBar() != null) {
				x += getVerticalBar().getSize().x;
			}
		} else {
			x = wHint;
		}

		if (hHint == SWT.DEFAULT) {
			y += prefSize.y;
			if (getHorizontalBar() != null) {
				y += getHorizontalBar().getSize().y;
			}
		} else {
			y = hHint;
		}

		return new Point(x, y);
	}

	/**
	 * Deselects the item at the given zero-relative index in the receiver. If the
	 * item at the index was already deselected, it remains deselected. Indices that
	 * are out of range are ignored.
	 * <p>
	 * If cell selection is enabled, all cells in the specified item are deselected.
	 *
	 * @param index the index of the item to deselect
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void deselect(final int index) {
		checkWidget();

		if (index < 0 || index > items.size() - 1) {
			return;
		}

		final GridItem item = items.get(index);

		if (!cellSelectionEnabled) {
			if (selectedItems.contains(item)) {
				selectedItems.remove(item);
			}
		} else {
			deselectCells(getCells(item));
		}
		redraw();
	}

	/**
	 * Deselects the items at the given zero-relative indices in the receiver. If
	 * the item at the given zero-relative index in the receiver is selected, it is
	 * deselected. If the item at the index was not selected, it remains deselected.
	 * The range of the indices is inclusive. Indices that are out of range are
	 * ignored.
	 * <p>
	 * If cell selection is enabled, all cells in the given range are deselected.
	 *
	 * @param start the start index of the items to deselect
	 * @param end   the end index of the items to deselect
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void deselect(final int start, final int end) {
		checkWidget();

		for (int i = start; i <= end; i++) {
			if (i < 0) {
				continue;
			}
			if (i > items.size() - 1) {
				break;
			}

			final GridItem item = items.get(i);

			if (!cellSelectionEnabled) {
				if (selectedItems.contains(item)) {
					selectedItems.remove(item);
				}
			} else {
				deselectCells(getCells(item));
			}
		}
		redraw();
	}

	/**
	 * Deselects the items at the given zero-relative indices in the receiver. If
	 * the item at the given zero-relative index in the receiver is selected, it is
	 * deselected. If the item at the index was not selected, it remains deselected.
	 * Indices that are out of range and duplicate indices are ignored.
	 * <p>
	 * If cell selection is enabled, all cells in the given items are deselected.
	 *
	 * @param indices the array of indices for the items to deselect
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the set of
	 *                                      indices is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void deselect(final int[] indices) {
		checkWidget();
		if (indices == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		for (final int j : indices) {
			if (j >= 0 && j < items.size()) {
				final GridItem item = items.get(j);

				if (!cellSelectionEnabled) {
					if (selectedItems.contains(item)) {
						selectedItems.remove(item);
					}
				} else {
					deselectCells(getCells(item));
				}
			}
		}
		redraw();
	}

	/**
	 * Deselects all selected items in the receiver. If cell selection is enabled,
	 * all cells are deselected.
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void deselectAll() {
		checkWidget();

		if (!cellSelectionEnabled) {
			selectedItems.clear();
			redraw();
		} else {
			deselectAllCells();
		}
	}

	/**
	 * Returns the column at the given, zero-relative index in the receiver. Throws
	 * an exception if the index is out of range. If no {@code GridColumn}s were
	 * created by the programmer, this method will throw {@code ERROR_INVALID_RANGE}
	 * despite the fact that a single column of data may be visible in the table.
	 * This occurs when the programmer uses the table like a list, adding items but
	 * never creating a column.
	 *
	 * @param index the index of the column to return
	 * @return the column at the given index
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_RANGE - if the index
	 *                                      is not between 0 and the number of
	 *                                      elements in the list minus 1
	 *                                      (inclusive)</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridColumn getColumn(final int index) {
		checkWidget();

		if (index < 0 || index > getColumnCount() - 1) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}

		return columns.get(index);
	}

	/**
	 * Returns the column at the given point in the receiver or null if no such
	 * column exists. The point is in the coordinate system of the receiver.
	 *
	 * @param point the point used to locate the column
	 * @return the column at the given point
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the point
	 *                                      is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridColumn getColumn(final Point point) {
		return getColumn(null, point);
	}

	/**
	 * Returns the column at the given point and a known item in the receiver or
	 * null if no such column exists. The point is in the coordinate system of the
	 * receiver.
	 *
	 * @param item  a known GridItem
	 * @param point the point used to locate the column
	 * @return the column at the given point
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the point
	 *                                      is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	private GridColumn getColumn(GridItem item, final Point point) {
		checkWidget();
		if (point == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		GridColumn overThis = null;

		int x2 = 0;

		if (rowHeaderVisible) {
			if (point.x <= rowHeaderWidth) {
				return null;
			}

			x2 += rowHeaderWidth;
		}

		x2 -= getHScrollSelectionInPixels();

		for (final GridColumn column : displayOrderedColumns) {
			if (!column.isVisible()) {
				continue;
			}

			if (point.x >= x2 && point.x < x2 + column.getWidth()) {
				overThis = column;
				break;
			}

			x2 += column.getWidth();
		}

		if (overThis == null) {
			return null;
		}

		if (hasSpanning) {
			// special logic for column spanning
			if (item == null) {
				item = getItem(point);
			}

			if (item != null) {
				final int displayColIndex = displayOrderedColumns.indexOf(overThis);

				// track back all previous columns and check their spanning
				for (int i = 0; i < displayColIndex; i++) {
					if (!displayOrderedColumns.get(i).isVisible()) {
						continue;
					}

					final int colIndex = displayOrderedColumns.get(i).index;
					final int span = item.getColumnSpan(colIndex);

					if (i + span >= displayColIndex) {
						overThis = displayOrderedColumns.get(i);
						break;
					}
				}
			}
		}

		return overThis;
	}

	/**
	 * Returns the number of columns contained in the receiver. If no
	 * {@code GridColumn}s were created by the programmer, this value is zero,
	 * despite the fact that visually, one column of items may be visible. This
	 * occurs when the programmer uses the table like a list, adding items but never
	 * creating a column.
	 *
	 * @return the number of columns
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getColumnCount() {
		checkWidget();
		return columns.size();
	}

	/**
	 * Returns an array of zero-relative integers that map the creation order of the
	 * receiver's items to the order in which they are currently being displayed.
	 * <p>
	 * Specifically, the indices of the returned array represent the current visual
	 * order of the items, and the contents of the array represent the creation
	 * order of the items.
	 * </p>
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the current visual order of the receiver's items
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int[] getColumnOrder() {
		checkWidget();
		if (columnOrders == null) {
			columnOrders = new int[columns.size()];
			int i = 0;
			for (final GridColumn col : displayOrderedColumns) {
				columnOrders[i] = col.index;
				i++;
			}
		}
		return columnOrders;
	}

	/**
	 * Returns the number of column groups contained in the receiver.
	 *
	 * @return the number of column groups
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getColumnGroupCount() {
		checkWidget();
		return columnGroups.length;
	}

	/**
	 * Returns an array of {@code GridColumnGroup}s which are the column groups in
	 * the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the column groups in the receiver
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridColumnGroup[] getColumnGroups() {
		checkWidget();
		final GridColumnGroup[] newArray = new GridColumnGroup[columnGroups.length];
		System.arraycopy(columnGroups, 0, newArray, 0, columnGroups.length);
		return newArray;
	}

	/**
	 * Returns the column group at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range.
	 *
	 * @param index the index of the column group to return
	 * @return the column group at the given index
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_RANGE - if the index
	 *                                      is not between 0 and the number of
	 *                                      elements in the list minus 1
	 *                                      (inclusive)</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridColumnGroup getColumnGroup(final int index) {
		checkWidget();

		if (index < 0 || index >= columnGroups.length) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}

		return columnGroups[index];
	}

	/**
	 * Sets the order that the items in the receiver should be displayed in to the
	 * given argument which is described in terms of the zero-relative ordering of
	 * when the items were added.
	 *
	 * @param order the new order to display the items
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS -if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the item
	 *                                      order is null</li>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if the
	 *                                      order is not the same length as the
	 *                                      number of items, or if an item is listed
	 *                                      twice, or if the order splits a column
	 *                                      group</li>
	 *                                      </ul>
	 */
	public void setColumnOrder(final int[] order) {
		checkWidget();

		if (order == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (order.length != displayOrderedColumns.size()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		final boolean[] seen = new boolean[displayOrderedColumns.size()];

		for (int i = 0; i < order.length; i++) {
			if (order[i] < 0 || order[i] >= displayOrderedColumns.size()) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
			if (seen[order[i]]) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
			seen[order[i]] = true;
		}

		if (columnGroups.length != 0) {
			GridColumnGroup currentGroup = null;
			int colsInGroup = 0;

			for (final int element : order) {
				final GridColumn col = getColumn(element);

				if (currentGroup != null) {
					if (col.getColumnGroup() != currentGroup && colsInGroup > 0) {
						SWT.error(SWT.ERROR_INVALID_ARGUMENT);
					} else {
						colsInGroup--;
						if (colsInGroup <= 0) {
							currentGroup = null;
						}
					}
				} else if (col.getColumnGroup() != null) {
					currentGroup = col.getColumnGroup();
					colsInGroup = currentGroup.getColumns().length - 1;
				}
			}
		}

		final GridColumn[] cols = getColumns();

		displayOrderedColumns.clear();

		for (final int element : order) {
			displayOrderedColumns.add(cols[element]);
		}
		clearDisplayOrderedCache();
	}

	/**
	 * This method is used for clearing columns displayed ordering cache
	 */
	private void clearDisplayOrderedCache() {
		columnOrders = null;
	}

	/**
	 * Returns an array of {@code GridColumn}s which are the columns in the
	 * receiver. If no {@code GridColumn}s were created by the programmer, the array
	 * is empty, despite the fact that visually, one column of items may be visible.
	 * This occurs when the programmer uses the table like a list, adding items but
	 * never creating a column.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the items in the receiver
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridColumn[] getColumns() {
		checkWidget();
		return columns.toArray(new GridColumn[columns.size()]);
	}

	/**
	 * Returns the empty cell renderer.
	 *
	 * @return Returns the emptyCellRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridCellRenderer getEmptyCellRenderer() {
		checkWidget();
		return emptyCellRenderer;
	}

	/**
	 * Returns the empty column header renderer.
	 *
	 * @return Returns the emptyColumnHeaderRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public IRenderer getEmptyColumnHeaderRenderer() {
		checkWidget();
		return emptyColumnHeaderRenderer;
	}

	/**
	 * Returns the empty column footer renderer.
	 *
	 * @return Returns the emptyColumnFooterRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public IRenderer getEmptyColumnFooterRenderer() {
		checkWidget();
		return emptyColumnFooterRenderer;
	}

	/**
	 * Returns the empty row header renderer.
	 *
	 * @return Returns the emptyRowHeaderRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public IRenderer getEmptyRowHeaderRenderer() {
		checkWidget();
		return emptyRowHeaderRenderer;
	}

	/**
	 * Returns the externally managed horizontal scrollbar.
	 *
	 * @return the external horizontal scrollbar.
	 * @see #setHorizontalScrollBarProxy(IScrollBarProxy)
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	protected IScrollBarProxy getHorizontalScrollBarProxy() {
		checkWidget();
		return hScroll;
	}

	/**
	 * Returns the externally managed vertical scrollbar.
	 *
	 * @return the external vertical scrollbar.
	 * @see #setlVerticalScrollBarProxy(IScrollBarProxy)
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	protected IScrollBarProxy getVerticalScrollBarProxy() {
		checkWidget();
		return vScroll;
	}

	/**
	 * Gets the focus renderer.
	 *
	 * @return Returns the focusRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public IRenderer getFocusRenderer() {
		checkWidget();
		return focusRenderer;
	}

	/**
	 * Returns the height of the column headers. If this table has column groups,
	 * the returned value includes the height of group headers.
	 *
	 * @return height of the column header row
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getHeaderHeight() {
		checkWidget();
		return headerHeight;
	}

	/**
	 * Cached default font of Control.getFont
	 *
	 * @see org.eclipse.swt.widgets.Control#getFont()
	 */
	@Override
	public Font getFont() {
		if (defaultFont == null) {
			defaultFont = super.getFont();
		}
		return defaultFont;
	}

	/**
	 * Returns the height of the column footers.
	 *
	 * @return height of the column footer row
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getFooterHeight() {
		checkWidget();
		return footerHeight;
	}

	/**
	 * Returns the height of the column group headers.
	 *
	 * @return height of column group headers
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getGroupHeaderHeight() {
		checkWidget();
		return groupHeaderHeight;
	}

	/**
	 * Returns {@code true} if the receiver's header is visible, and {@code false}
	 * otherwise.
	 *
	 * @return the receiver's header's visibility state
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean getHeaderVisible() {
		checkWidget();
		return columnHeadersVisible;
	}

	/**
	 * Returns {@code true} if the receiver's footer is visible, and {@code false}
	 * otherwise
	 *
	 * @return the receiver's footer's visibility state
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean getFooterVisible() {
		checkWidget();
		return columnFootersVisible;
	}

	/**
	 * Returns the item at the given, zero-relative index in the receiver. Throws an
	 * exception if the index is out of range.
	 *
	 * @param index the index of the item to return
	 * @return the item at the given index
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_RANGE - if the index
	 *                                      is not between 0 and the number of
	 *                                      elements in the list minus 1
	 *                                      (inclusive)</li> *
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem getItem(final int index) {
		checkWidget();

		if (index < 0 || index >= items.size()) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}

		return items.get(index);
	}

	/**
	 * Returns the item at the given point in the receiver or null if no such item
	 * exists. The point is in the coordinate system of the receiver.
	 *
	 * @param point the point used to locate the item
	 * @return the item at the given point
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the point
	 *                                      is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem getItem(final Point point) {
		checkWidget();

		if (point == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (point.x < 0 || point.x > getClientArea().width) {
			return null;
		}

		final Point p = new Point(point.x, point.y);

		int y2 = 0;

		if (columnHeadersVisible) {
			if (p.y <= headerHeight) {
				return null;
			}
			y2 += headerHeight;
		}

		GridItem itemToReturn = null;

		int row = getTopIndex();
		while (row < items.size() && y2 <= getClientArea().height) {
			final GridItem currItem = items.get(row);
			if (currItem.isVisible()) {
				final int currItemHeight = currItem.getHeight();

				if (p.y >= y2 && p.y < y2 + currItemHeight + 1) {
					itemToReturn = currItem;
					break;
				}

				y2 += currItemHeight + 1;
			}
			row++;
		}

		if (hasSpanning) {
			if (itemToReturn != null) {
				final int itemIndex = getIndexOfItem(itemToReturn);

				final GridColumn gridColumn = getColumn(itemToReturn, point);
				final int displayColIndex = displayOrderedColumns.indexOf(gridColumn);

				// track back all previous columns and check their spanning
				int indexNextItemToCheck = 0;
				for (int i = 0; i < itemIndex; i++) {
					if (i < indexNextItemToCheck) {
						continue;
					}
					final GridItem gridItem = this.getItem(i);
					if (gridItem.isVisible() == false) {
						continue;
					}
					final int span = gridItem.getRowSpan(displayColIndex);

					if (i + span >= itemIndex) {
						itemToReturn = gridItem;
						break;
					}
					indexNextItemToCheck = i + span + 1;
				}
			}
		}

		return itemToReturn;
	}

	/**
	 * Returns the number of items contained in the receiver.
	 *
	 * @return the number of items
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getItemCount() {
		checkWidget();
		return items.size();
	}

	/**
	 * Returns the default height of the items in this <code>Grid</code>. See
	 * {@link #setItemHeight(int)} for details.
	 *
	 * <p>
	 * IMPORTANT: The Grid's items need not all have the height returned by this
	 * method, because an item's height may have been changed by calling
	 * {@link GridItem#setHeight(int)}.
	 *
	 * @return default height of items
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see #setItemHeight(int)
	 */
	public int getItemHeight() {
		checkWidget();
		return itemHeight;
	}

	/**
	 * Sets the default height for this <code>Grid</code>'s items. When this method
	 * is called, all existing items are resized to the specified height and items
	 * created afterwards will be initially sized to this height.
	 * <p>
	 * As long as no default height was set by the client through this method, the
	 * preferred height of the first item in this <code>Grid</code> is used as a
	 * default for all items (and is returned by {@link #getItemHeight()}).
	 *
	 * @param height default height in pixels
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if the
	 *                                      height is < 1</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 *
	 * @see GridItem#getHeight()
	 * @see GridItem#setHeight(int)
	 */
	public void setItemHeight(final int height) {
		checkWidget();
		if (height < 1) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		itemHeight = height;
		userModifiedItemHeight = true;
		for (final GridItem item : items) {
			item.setHeight(height);
		}
		hasDifferingHeights = false;
		setScrollValuesObsolete();
		redraw();
	}

	/**
	 * Returns true if the rows are resizable.
	 *
	 * @return the row resizeable state
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see #setRowsResizeable(boolean)
	 */
	public boolean getRowsResizeable() {
		checkWidget();
		return rowsResizeable;
	}

	/**
	 * Sets the rows resizeable state of this <code>Grid</code>. The default is
	 * 'false'.
	 * <p>
	 * If a row in a <code>Grid</code> is resizeable, then the user can
	 * interactively change its height by dragging the border of the row header.
	 * <p>
	 * Note that for rows to be resizable the row headers must be visible.
	 *
	 * @param rowsResizeable true if this <code>Grid</code>'s rows should be
	 *                       resizable
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see #setRowHeaderVisible(boolean)
	 */
	public void setRowsResizeable(final boolean rowsResizeable) {
		checkWidget();
		this.rowsResizeable = rowsResizeable;
	}

	/**
	 * Returns a (possibly empty) array of {@code GridItem}s which are the items in
	 * the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the items in the receiver
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem[] getItems() {
		checkWidget();
		return items.toArray(new GridItem[items.size()]);
	}

	/**
	 * Returns the index of the given item in the grid.
	 *
	 * @param item the grid item
	 * @return the index of the item
	 */
	public int getIndexOfItem(final GridItem item) {
		checkWidget();

		return item.getRowIndex();
	}

	/**
	 * Returns the line color.
	 *
	 * @return Returns the lineColor.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public Color getLineColor() {
		checkWidget();
		return lineColor;
	}

	/**
	 * Returns true if the lines are visible.
	 *
	 * @return Returns the linesVisible.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean getLinesVisible() {
		checkWidget();
		return linesVisible;
	}

	/**
	 * Returns true if the tree lines are visible.
	 *
	 * @return Returns the treeLinesVisible.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean getTreeLinesVisible() {
		checkWidget();
		return treeLinesVisible;
	}

	/**
	 * Returns the next visible item in the table.
	 *
	 * @param item item
	 * @return next visible item or null
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem getNextVisibleItem(final GridItem item) {
		checkWidget();

		int index = item.getRowIndex();
		if (items.size() == index + 1) {
			return null;
		}

		GridItem nextItem = items.get(index + 1);

		while (!nextItem.isVisible()) {
			index++;
			if (items.size() == index + 1) {
				return null;
			}

			nextItem = items.get(index + 1);
		}

		return nextItem;
	}

	/**
	 * Returns the previous visible item in the table. Passing null for the item
	 * will return the last visible item in the table.
	 *
	 * @param item item or null
	 * @return previous visible item or if item==null last visible item
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem getPreviousVisibleItem(final GridItem item) {
		checkWidget();

		int index = 0;
		if (item == null) {
			index = items.size();
		} else {
			index = item.getRowIndex();
			if (index <= 0) {
				return null;
			}
		}

		GridItem prevItem = items.get(index - 1);

		while (!prevItem.isVisible()) {
			index--;
			if (index == 0) {
				return null;
			}

			prevItem = items.get(index - 1);
		}

		return prevItem;
	}

	/**
	 * Returns the previous visible column in the table.
	 *
	 * @param column column
	 * @return previous visible column or null
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridColumn getPreviousVisibleColumn(final GridColumn column) {
		checkWidget();

		int index = displayOrderedColumns.indexOf(column);

		if (index == 0) {
			return null;
		}

		index--;

		GridColumn previous = displayOrderedColumns.get(index);

		while (!previous.isVisible()) {
			if (index == 0) {
				return null;
			}

			index--;
			previous = displayOrderedColumns.get(index);
		}

		return previous;
	}

	/**
	 * Returns the next visible column in the table.
	 *
	 * @param column column
	 * @return next visible column or null
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridColumn getNextVisibleColumn(final GridColumn column) {
		checkWidget();

		int index = displayOrderedColumns.indexOf(column);

		if (index == displayOrderedColumns.size() - 1) {
			return null;
		}

		index++;

		GridColumn next = displayOrderedColumns.get(index);

		while (!next.isVisible()) {
			if (index == displayOrderedColumns.size() - 1) {
				return null;
			}

			index++;
			next = displayOrderedColumns.get(index);
		}

		return next;
	}

	/**
	 * Returns the number of root items contained in the receiver.
	 *
	 * @return the number of items
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getRootItemCount() {
		checkWidget();
		return rootItems.size();
	}

	/**
	 * Returns a (possibly empty) array of {@code GridItem}s which are the root
	 * items in the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the root items in the receiver
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem[] getRootItems() {
		checkWidget();

		return rootItems.toArray(new GridItem[rootItems.size()]);
	}

	/**
	 * Returns the root item at the given index.
	 *
	 * @param index the index of the root item
	 * @return the root item at the specified index
	 */
	public GridItem getRootItem(final int index) {
		checkWidget();

		if (index < 0 || index >= rootItems.size()) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}

		return rootItems.get(index);
	}

	/**
	 * Gets the row header renderer.
	 *
	 * @return Returns the rowHeaderRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public IRenderer getRowHeaderRenderer() {
		checkWidget();
		return rowHeaderRenderer;
	}

	/**
	 * Returns a array of {@code GridItem}s that are currently selected in the
	 * receiver. The order of the items is unspecified. An empty array indicates
	 * that no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * selection, so modifying the array will not affect the receiver.
	 * <p>
	 * If cell selection is enabled, any items which contain at least one selected
	 * cell are returned.
	 *
	 * @return an array representing the selection
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem[] getSelection() {
		checkWidget();

		if (!cellSelectionEnabled) {
			return selectedItems.toArray(new GridItem[selectedItems.size()]);
		} else {
			final Vector<GridItem> items = new Vector<>();
			final int itemCount = getItemCount();

			for (final Point cell : selectedCells) {
				if (cell.y >= 0 && cell.y < itemCount) {
					final GridItem item = getItem(cell.y);
					if (!items.contains(item)) {
						items.add(item);
					}
				}
			}
			return items.toArray(new GridItem[] {});
		}
	}

	/**
	 * Returns the number of selected items contained in the receiver. If cell
	 * selection is enabled, the number of items with at least one selected cell are
	 * returned.
	 *
	 * @return the number of selected items
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getSelectionCount() {
		checkWidget();

		if (!cellSelectionEnabled) {
			return selectedItems.size();
		} else {
			final Vector<GridItem> items = new Vector<>();
			for (final Point cell : selectedCells) {
				final GridItem item = getItem(cell.y);
				if (!items.contains(item)) {
					items.add(item);
				}
			}
			return items.size();
		}
	}

	/**
	 * Returns the number of selected cells contained in the receiver.
	 *
	 * @return the number of selected cells
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getCellSelectionCount() {
		checkWidget();
		return selectedCells.size();
	}

	/**
	 * Returns the zero-relative index of the item which is currently selected in
	 * the receiver, or -1 if no item is selected. If cell selection is enabled,
	 * returns the index of first item that contains at least one selected cell.
	 *
	 * @return the index of the selected item
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getSelectionIndex() {
		checkWidget();

		if (!cellSelectionEnabled) {
			if (selectedItems.size() == 0) {
				return -1;
			}

			return selectedItems.get(0).getRowIndex();
		} else {
			if (selectedCells.size() == 0) {
				return -1;
			}

			return selectedCells.get(0).y;
		}
	}

	/**
	 * Returns the zero-relative indices of the items which are currently selected
	 * in the receiver. The order of the indices is unspecified. The array is empty
	 * if no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * selection, so modifying the array will not affect the receiver.
	 * <p>
	 * If cell selection is enabled, returns the indices of any items which contain
	 * at least one selected cell.
	 *
	 * @return the array of indices of the selected items
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int[] getSelectionIndices() {
		checkWidget();

		if (!cellSelectionEnabled) {
			final int[] indices = new int[selectedItems.size()];
			int i = 0;
			for (final GridItem item : selectedItems) {
				indices[i] = item.getRowIndex();
				i++;
			}
			return indices;
		} else {
			final Vector<GridItem> selectedRows = new Vector<>();
			for (final Point cell : selectedCells) {
				final GridItem item = getItem(cell.y);
				if (!selectedRows.contains(item)) {
					selectedRows.add(item);
				}
			}
			final int[] indices = new int[selectedRows.size()];
			int i = 0;
			for (final GridItem item : selectedRows) {
				indices[i] = item.getRowIndex();
				i++;
			}
			return indices;
		}
	}

	/**
	 * Returns the zero-relative index of the item which is currently at the top of
	 * the receiver. This index can change when items are scrolled or new items are
	 * added or removed.
	 *
	 * @return the index of the top item
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getTopIndex() {
		checkWidget();

		if (topIndex != -1) {
			return topIndex;
		}

		if (!vScroll.getVisible()) {
			topIndex = 0;
		} else {
			// figure out first visible row and last visible row
			int firstVisibleIndex = vScroll.getSelection();

			if (isTree) {
				final Iterator<GridItem> itemsIter = items.iterator();
				int row = firstVisibleIndex + 1;

				while (row > 0 && itemsIter.hasNext()) {
					final GridItem item = itemsIter.next();

					if (item.isVisible()) {
						row--;
						if (row == 0) {
							firstVisibleIndex = item.getRowIndex();
						}
					}
				}
			}

			topIndex = firstVisibleIndex;

			/*
			 * MOPR here lies more potential for increasing performance for the case (isTree
			 * || hasDifferingHeights) the topIndex could be derived from the previous value
			 * depending on a delta of the vScroll.getSelection() instead of being
			 * calculated completely anew
			 */
		}

		return topIndex;
	}

	/**
	 * Returns the zero-relative index of the item which is currently at the bottom
	 * of the receiver. This index can change when items are scrolled, expanded or
	 * collapsed or new items are added or removed.
	 * <p>
	 * Note that the item with this index is often only partly visible; maybe only a
	 * single line of pixels is visible. Use {@link #isShown(GridItem)} to find out.
	 * <p>
	 * In extreme cases, getBottomIndex() may return the same value as
	 * {@link #getTopIndex()}.
	 *
	 * @return the index of the bottom item
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	int getBottomIndex() {
		checkWidget();

		if (bottomIndex != -1) {
			return bottomIndex;
		}

		if (items.size() == 0) {
			bottomIndex = 0;
		} else if (getVisibleGridHeight() < 1) {
			bottomIndex = getTopIndex();
		} else {
			final RowRange range = getRowRange(getTopIndex(), getVisibleGridHeight(), false, false);

			bottomIndex = range.endIndex;
			bottomIndexShownCompletely = range.height <= getVisibleGridHeight();
		}

		return bottomIndex;
	}

	/**
	 * Returns a {@link RowRange} ranging from the grid item at startIndex to that
	 * at endIndex.
	 * <p>
	 * This is primarily used to measure the height in pixel of such a range and to
	 * count the number of visible grid items within the range.
	 *
	 * @param startIndex index of the first item in the range or -1 to the first
	 *                   visible item in this grid
	 * @param endIndex   index of the last item in the range or -1 to use the last
	 *                   visible item in this grid
	 * @return
	 */
	private RowRange getRowRange(int startIndex, int endIndex) {

		// parameter preparation
		if (startIndex == -1) {
			// search frist visible item
			do {
				startIndex++;
			} while (startIndex < items.size() && !items.get(startIndex).isVisible());
			if (startIndex == items.size()) {
				return null;
			}
		}
		if (endIndex == -1) {
			// search last visible item
			endIndex = items.size();
			do {
				endIndex--;
			} while (endIndex >= 0 && !items.get(endIndex).isVisible());
			if (endIndex == -1) {
				return null;
			}
		}

		// fail fast
		if (startIndex < 0 || endIndex < 0 || startIndex >= items.size() || endIndex >= items.size()
				|| endIndex < startIndex || items.get(startIndex).isVisible() == false
				|| items.get(endIndex).isVisible() == false) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		final RowRange range = new RowRange();
		range.startIndex = startIndex;
		range.endIndex = endIndex;

		if (isTree || hasDifferingHeights) {
			for (int idx = startIndex; idx <= endIndex; idx++) {
				final GridItem currItem = items.get(idx);

				if (currItem.isVisible()) {
					if (range.rows > 0) {
						range.height++; // height of horizontal row separator
					}
					range.height += currItem.getHeight();
					range.rows++;
				}
			}
		} else {
			range.rows = range.endIndex - range.startIndex + 1;
			range.height = (itemHeight + 1) * range.rows - 1;
		}

		return range;
	}

	/**
	 * This method can be used to build a range of grid rows that is allowed to span
	 * a certain height in pixels.
	 * <p>
	 * It returns a {@link RowRange} that contains information about the range,
	 * especially the index of the last element in the range (or if inverse == true,
	 * then the index of the first element).
	 * <p>
	 * Note: Even if 'forceEndCompletelyInside' is set to true, the last item will
	 * not lie completely within the availableHeight, if (height of item at
	 * startIndex < availableHeight).
	 *
	 * @param startIndex               index of the first (if inverse==false) or
	 *                                 last (if inverse==true) item in the range
	 * @param availableHeight          height in pixels
	 * @param forceEndCompletelyInside if true, the last item in the range will lie
	 *                                 completely within the availableHeight,
	 *                                 otherwise it may lie partly outside this
	 *                                 range
	 * @param inverse                  if true, then the first item in the range
	 *                                 will be searched, not the last
	 * @return range of grid rows
	 * @see RowRange
	 */
	private RowRange getRowRange(int startIndex, final int availableHeight, final boolean forceEndCompletelyInside,
			final boolean inverse) {
		// parameter preparation
		if (startIndex == -1) {
			if (!inverse) {
				// search frist visible item
				do {
					startIndex++;
				} while (startIndex < items.size() && !items.get(startIndex).isVisible());
				if (startIndex == items.size()) {
					return null;
				}
			} else {
				// search last visible item
				startIndex = items.size();
				do {
					startIndex--;
				} while (startIndex >= 0 && !items.get(startIndex).isVisible());
				if (startIndex == -1) {
					return null;
				}
			}
		}

		// fail fast
		if (startIndex < 0 || startIndex >= items.size() || items.get(startIndex).isVisible() == false) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		final RowRange range = new RowRange();

		if (availableHeight <= 0) {
			// special case: empty range
			range.startIndex = startIndex;
			range.endIndex = startIndex;
			range.rows = 0;
			range.height = 0;
			return range;
		}

		if (isTree || hasDifferingHeights) {
			int otherIndex = startIndex; // tentative end index
			int consumedItems = 0;
			int consumedHeight = 0;

			// consume height for startEnd (note: no separator pixel added here)
			consumedItems++;
			consumedHeight += items.get(otherIndex).getHeight();

			// note: we use "+2" in next line, because we only try to add another row if
			// there
			// is room for the separator line + at least one pixel row for the additional
			// item
			while (consumedHeight + 2 <= availableHeight) {
				// STEP 1:
				// try to find a visible item we can add

				int nextIndex = otherIndex;
				GridItem nextItem;

				do {
					if (!inverse) {
						nextIndex++;
					} else {
						nextIndex--;
					}

					if (nextIndex >= 0 && nextIndex < items.size()) {
						nextItem = items.get(nextIndex);
					} else {
						nextItem = null;
					}
				} while (nextItem != null && !nextItem.isVisible());

				if (nextItem == null) {
					// no visible item found
					break;
				}

				if (forceEndCompletelyInside) {
					// must lie completely within the allowed height
					if (!(consumedHeight + 1 + nextItem.getHeight() <= availableHeight)) {
						break;
					}
				}

				// we found one !!

				// STEP 2:
				// Consume height for this item

				consumedItems++;
				consumedHeight += 1; // height of separator line
				consumedHeight += nextItem.getHeight();

				// STEP 3:
				// make this item it the current guess for the other end
				otherIndex = nextIndex;
			}

			range.startIndex = !inverse ? startIndex : otherIndex;
			range.endIndex = !inverse ? otherIndex : startIndex;
			range.rows = consumedItems;
			range.height = consumedHeight;
		} else {
			int availableRows = (availableHeight + 1) / (itemHeight + 1);

			if ((itemHeight + 1) * range.rows - 1 + 1 < availableHeight) {
				// not all available space used yet
				// - so add another row if it need not be completely within availableHeight
				if (!forceEndCompletelyInside) {
					availableRows++;
				}
			}

			int otherIndex = startIndex + (availableRows - 1) * (!inverse ? 1 : -1);
			if (otherIndex < 0) {
				otherIndex = 0;
			}
			if (otherIndex >= items.size()) {
				otherIndex = items.size() - 1;
			}

			range.startIndex = !inverse ? startIndex : otherIndex;
			range.endIndex = !inverse ? otherIndex : startIndex;
			range.rows = range.endIndex - range.startIndex + 1;
			range.height = (itemHeight + 1) * range.rows - 1;
		}

		return range;
	}

	/**
	 * Returns the height of the plain grid in pixels.
	 * <p>
	 * This includes all rows for visible items (i.e. items that return true on
	 * {@link GridItem#isVisible()} ; not only those currently visible on screen)
	 * and the 1 pixel separator between rows.
	 * <p>
	 * This does <em>not</em> include the height of the column headers.
	 *
	 * @return height of plain grid
	 */
	int getGridHeight() {
		final RowRange range = getRowRange(-1, -1);
		return range != null ? range.height : 0;
		/*
		 * MOPR currently this method is only used in #getTableSize() ; if it will be
		 * used for more important things in the future (e.g. the max value for
		 * vScroll.setValues() when doing pixel-by-pixel vertical scrolling) then this
		 * value should at least be cached or even updated incrementally when grid items
		 * are added/removed or expaned/collapsed (similar as #currentVisibleItems).
		 * (this is only necessary in the case (isTree || hasDifferingHeights))
		 */
	}

	/**
	 * Returns the height of the on-screen area that is available for showing the
	 * grid's rows, i.e. the client area of the scrollable minus the height of the
	 * column headers (if shown).
	 *
	 * @return height of visible grid in pixels
	 */
	int getVisibleGridHeight() {
		return getClientArea().height - (columnHeadersVisible ? headerHeight : 0)
				- (columnFootersVisible ? footerHeight : 0);
	}

	/**
	 * Returns the height of the screen area that is available for showing the grid
	 * columns
	 *
	 * @return
	 */
	int getVisibleGridWidth() {
		return getClientArea().width - (rowHeaderVisible ? rowHeaderWidth : 0);
	}

	/**
	 * Gets the top left renderer.
	 *
	 * @return Returns the topLeftRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public IRenderer getTopLeftRenderer() {
		checkWidget();
		return topLeftRenderer;
	}

	/**
	 * Gets the bottom left renderer.
	 *
	 * @return Returns the bottomLeftRenderer.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public IRenderer getBottomLeftRenderer() {
		checkWidget();
		return bottomLeftRenderer;
	}

	/**
	 * Searches the receiver's list starting at the first column (index 0) until a
	 * column is found that is equal to the argument, and returns the index of that
	 * column. If no column is found, returns -1.
	 *
	 * @param column the search column
	 * @return the index of the column
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the column
	 *                                      is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int indexOf(final GridColumn column) {
		checkWidget();

		if (column == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (column.getParent() != this) {
			return -1;
		}

		return column.index;
	}

	/**
	 * Searches the receiver's list starting at the first item (index 0) until an
	 * item is found that is equal to the argument, and returns the index of that
	 * item. If no item is found, returns -1.
	 *
	 * @param item the search item
	 * @return the index of the item
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the item is
	 *                                      null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int indexOf(final GridItem item) {
		checkWidget();

		if (item == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (item.getParent() != this) {
			return -1;
		}

		return items.indexOf(item);
	}

	/**
	 * Returns {@code true} if the receiver's row header is visible, and
	 * {@code false} otherwise.
	 * <p>
	 *
	 * @return the receiver's row header's visibility state
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean isRowHeaderVisible() {
		checkWidget();
		return rowHeaderVisible;
	}

	/**
	 * Returns {@code true} if the item is selected, and {@code false} otherwise.
	 * Indices out of range are ignored. If cell selection is enabled, returns true
	 * if the item at the given index contains at least one selected cell.
	 *
	 * @param index the index of the item
	 * @return the visibility state of the item at the index
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean isSelected(final int index) {
		checkWidget();

		if (index < 0 || index >= items.size()) {
			return false;
		}

		if (!cellSelectionEnabled) {
			return isSelected(items.get(index));
		} else {
			for (final Point cell : selectedCells) {
				if (cell.y == index) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Returns true if the given item is selected. If cell selection is enabled,
	 * returns true if the given item contains at least one selected cell.
	 *
	 * @param item item
	 * @return true if the item is selected.
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the item is
	 *                                      null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean isSelected(final GridItem item) {
		checkWidget();
		if (!cellSelectionEnabled) {
			return selectedItems.contains(item);
		} else {
			final int index = item.getRowIndex();
			if (index == -1) {
				return false;
			}
			for (final Point cell : selectedCells) {
				if (cell.y == index) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Returns true if the given cell is selected.
	 *
	 * @param cell cell
	 * @return true if the cell is selected.
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the cell is
	 *                                      null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean isCellSelected(final Point cell) {
		checkWidget();

		if (cell == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		return selectedCells.contains(cell);
	}

	/**
	 * Removes the item from the receiver at the given zero-relative index.
	 *
	 * @param index the index for the item
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_RANGE - if the index
	 *                                      is not between 0 and the number of
	 *                                      elements in the list minus 1
	 *                                      (inclusive)</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void remove(final int index) {
		checkWidget();
		if (index < 0 || index > items.size() - 1) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}
		final GridItem item = items.get(index);
		item.dispose();
		redraw();
	}

	/**
	 * Removes the items from the receiver which are between the given zero-relative
	 * start and end indices (inclusive).
	 *
	 * @param start the start of the range
	 * @param end   the end of the range
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_RANGE - if either the
	 *                                      start or end are not between 0 and the
	 *                                      number of elements in the list minus 1
	 *                                      (inclusive)</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void remove(final int start, final int end) {
		checkWidget();

		for (int i = end; i >= start; i--) {
			if (i < 0 || i > items.size() - 1) {
				SWT.error(SWT.ERROR_INVALID_RANGE);
			}
			final GridItem item = items.get(i);
			item.dispose();
		}
		redraw();
	}

	/**
	 * Removes the items from the receiver's list at the given zero-relative
	 * indices.
	 *
	 * @param indices the array of indices of the items
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_RANGE - if the index
	 *                                      is not between 0 and the number of
	 *                                      elements in the list minus 1
	 *                                      (inclusive)</li>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the indices
	 *                                      array is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void remove(final int[] indices) {
		checkWidget();

		if (indices == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		final GridItem[] removeThese = new GridItem[indices.length];
		for (int i = 0; i < indices.length; i++) {
			final int j = indices[i];
			if (j < items.size() && j >= 0) {
				removeThese[i] = items.get(j);
			} else {
				SWT.error(SWT.ERROR_INVALID_RANGE);
			}

		}
		for (final GridItem item : removeThese) {
			item.dispose();
		}
		redraw();
	}

	/**
	 * Removes all of the items from the receiver.
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 *                                      Call {@link Grid} disposeAllItems and
	 *                                      clearItems.. Is faster
	 */
	@Deprecated
	public void removeAll() {
		checkWidget();

		while (items.size() > 0) {
			items.get(0).dispose();
		}
		deselectAll();
		redraw();
	}

	/**
	 * All items needs to call the disposeOnly method
	 */
	public void disposeAllItems() {
		checkWidget();

		final GridItem[] items = getItems();
		for (final GridItem gridItem : items) {
			gridItem.disposeOnly();
		}
		clearItems();
		scrollValuesObsolete = true;
		topIndex = -1;
		bottomIndex = -1;
		currentVisibleItems = 0;
		updateColumnSelection();
		focusItem = null;
		selectedItems.clear();
		redraw();
		// Need to update the scrollbars see see 375327
		updateScrollbars();
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified
	 * when the receiver's selection changes.
	 *
	 * @param listener the listener which should no longer be notified
	 * @see SelectionListener
	 * @see #addSelectionListener(SelectionListener)
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		removeTypedListener(SWT.Selection, listener);
		removeTypedListener(SWT.DefaultSelection, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified
	 * when the receiver's items changes.
	 *
	 * @param listener the listener which should no longer be notified
	 * @see TreeListener
	 * @see #addTreeListener(TreeListener)
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void removeTreeListener(final TreeListener listener) {
		removeTypedListener(SWT.Expand, listener);
		removeTypedListener(SWT.Collapse, listener);
	}

	/**
	 * Selects the item at the given zero-relative index in the receiver. If the
	 * item at the index was already selected, it remains selected. Indices that are
	 * out of range are ignored.
	 * <p>
	 * If cell selection is enabled, selects all cells at the given index.
	 *
	 * @param index the index of the item to select
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void select(final int index) {
		checkWidget();

		if (!selectionEnabled) {
			return;
		}

		if (index < 0 || index >= items.size()) {
			return;
		}

		final GridItem item = items.get(index);

		if (!cellSelectionEnabled) {
			if (selectionType == GridSelectionType.MULTI && selectedItems.contains(item)) {
				return;
			}

			if (selectionType == GridSelectionType.SINGLE) {
				selectedItems.clear();
			}

			selectedItems.add(item);
		} else {
			selectCells(getCells(item));
		}

		redraw();
	}

	/**
	 * Selects the items in the range specified by the given zero-relative indices
	 * in the receiver. The range of indices is inclusive. The current selection is
	 * not cleared before the new items are selected.
	 * <p>
	 * If an item in the given range is not selected, it is selected. If an item in
	 * the given range was already selected, it remains selected. Indices that are
	 * out of range are ignored and no items will be selected if start is greater
	 * than end. If the receiver is single-select and there is more than one item in
	 * the given range, then all indices are ignored.
	 * <p>
	 * If cell selection is enabled, all cells within the given range are selected.
	 *
	 * @param start the start of the range
	 * @param end   the end of the range
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see Grid#setSelection(int,int)
	 */
	public void select(final int start, final int end) {
		checkWidget();

		if (!selectionEnabled) {
			return;
		}

		if (selectionType == GridSelectionType.SINGLE && start != end) {
			return;
		}

		if (!cellSelectionEnabled) {
			if (selectionType == GridSelectionType.SINGLE) {
				selectedItems.clear();
			}
		}

		for (int i = start; i <= end; i++) {
			if (i < 0) {
				continue;
			}
			if (i > items.size() - 1) {
				break;
			}

			final GridItem item = items.get(i);

			if (!cellSelectionEnabled) {
				if (!selectedItems.contains(item)) {
					selectedItems.add(item);
				}
			} else {
				selectCells(getCells(item));
			}
		}

		redraw();
	}

	/**
	 * Selects the items at the given zero-relative indices in the receiver. The
	 * current selection is not cleared before the new items are selected.
	 * <p>
	 * If the item at a given index is not selected, it is selected. If the item at
	 * a given index was already selected, it remains selected. Indices that are out
	 * of range and duplicate indices are ignored. If the receiver is single-select
	 * and multiple indices are specified, then all indices are ignored.
	 * <p>
	 * If cell selection is enabled, all cells within the given indices are
	 * selected.
	 *
	 * @param indices the array of indices for the items to select
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the array
	 *                                      of indices is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see Grid#setSelection(int[])
	 */
	public void select(final int[] indices) {
		checkWidget();

		if (indices == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (!selectionEnabled) {
			return;
		}

		if (selectionType == GridSelectionType.SINGLE && indices.length > 1) {
			return;
		}

		if (!cellSelectionEnabled) {
			if (selectionType == GridSelectionType.SINGLE) {
				selectedItems.clear();
			}
		}

		for (final int j : indices) {
			if (j >= 0 && j < items.size()) {
				final GridItem item = items.get(j);

				if (!cellSelectionEnabled) {
					if (!selectedItems.contains(item)) {
						selectedItems.add(item);
					}
				} else {
					selectCells(getCells(item));
				}
			}
		}
		redraw();
	}

	/**
	 * Selects all of the items in the receiver.
	 * <p>
	 * If the receiver is single-select, do nothing. If cell selection is enabled,
	 * all cells are selected.
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void selectAll() {
		checkWidget();

		if (!selectionEnabled) {
			return;
		}

		if (selectionType == GridSelectionType.SINGLE) {
			return;
		}

		if (cellSelectionEnabled) {
			selectAllCells();
			return;
		}

		selectedItems.clear();
		selectedItems.addAll(items);
		redraw();
	}

	/**
	 * Sets the empty cell renderer.
	 *
	 * @param emptyCellRenderer The emptyCellRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setEmptyCellRenderer(final GridCellRenderer emptyCellRenderer) {
		checkWidget();
		emptyCellRenderer.setDisplay(getDisplay());
		this.emptyCellRenderer = emptyCellRenderer;
	}

	/**
	 * Sets the empty column header renderer.
	 *
	 * @param emptyColumnHeaderRenderer The emptyColumnHeaderRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setEmptyColumnHeaderRenderer(final IRenderer emptyColumnHeaderRenderer) {
		checkWidget();
		emptyColumnHeaderRenderer.setDisplay(getDisplay());
		this.emptyColumnHeaderRenderer = emptyColumnHeaderRenderer;
	}

	/**
	 * Sets the empty column footer renderer.
	 *
	 * @param emptyColumnFooterRenderer The emptyColumnFooterRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setEmptyColumnFooterRenderer(final IRenderer emptyColumnFooterRenderer) {
		checkWidget();
		emptyColumnFooterRenderer.setDisplay(getDisplay());
		this.emptyColumnFooterRenderer = emptyColumnFooterRenderer;
	}

	/**
	 * Sets the empty row header renderer.
	 *
	 * @param emptyRowHeaderRenderer The emptyRowHeaderRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setEmptyRowHeaderRenderer(final IRenderer emptyRowHeaderRenderer) {
		checkWidget();
		emptyRowHeaderRenderer.setDisplay(getDisplay());
		this.emptyRowHeaderRenderer = emptyRowHeaderRenderer;
	}

	/**
	 * Sets the external horizontal scrollbar. Allows the scrolling to be managed
	 * externally from the table. This functionality is only intended when
	 * SWT.H_SCROLL is not given.
	 * <p>
	 * Using this feature, a ScrollBar could be instantiated outside the table,
	 * wrapped in IScrollBar and thus be 'connected' to the table.
	 *
	 * @param scroll The horizontal scrollbar to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	protected void setHorizontalScrollBarProxy(final IScrollBarProxy scroll) {
		checkWidget();
		if (getHorizontalBar() != null) {
			return;
		}
		hScroll = scroll;

		hScroll.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				onScrollSelection();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});
	}

	/**
	 * Sets the external vertical scrollbar. Allows the scrolling to be managed
	 * externally from the table. This functionality is only intended when
	 * SWT.V_SCROLL is not given.
	 * <p>
	 * Using this feature, a ScrollBar could be instantiated outside the table,
	 * wrapped in IScrollBar and thus be 'connected' to the table.
	 *
	 * @param scroll The vertical scrollbar to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	protected void setlVerticalScrollBarProxy(final IScrollBarProxy scroll) {
		checkWidget();
		if (getVerticalBar() != null) {
			return;
		}
		vScroll = scroll;

		vScroll.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				onScrollSelection();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});
	}

	/**
	 * Sets the focus renderer.
	 *
	 * @param focusRenderer The focusRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setFocusRenderer(final IRenderer focusRenderer) {
		checkWidget();
		this.focusRenderer = focusRenderer;
	}

	/**
	 * Marks the receiver's header as visible if the argument is {@code true}, and
	 * marks it invisible otherwise.
	 *
	 * @param show the new visibility state
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setHeaderVisible(final boolean show) {
		checkWidget();
		columnHeadersVisible = show;
		redraw();
	}

	/**
	 * Marks the receiver's footer as visible if the argument is {@code true}, and
	 * marks it invisible otherwise.
	 *
	 * @param show the new visibility state
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setFooterVisible(final boolean show) {
		checkWidget();
		columnFootersVisible = show;
		redraw();
	}

	/**
	 * Sets the line color.
	 *
	 * @param lineColor The lineColor to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setLineColor(final Color lineColor) {
		checkWidget();
		this.lineColor = lineColor;
	}

	/**
	 * Sets the line visibility.
	 *
	 * @param linesVisible Te linesVisible to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setLinesVisible(final boolean linesVisible) {
		checkWidget();
		this.linesVisible = linesVisible;
		redraw();
	}

	/**
	 * Sets the tree line visibility.
	 *
	 * @param treeLinesVisible true if tree lines should be visible, false otherwise
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setTreeLinesVisible(final boolean treeLinesVisible) {
		checkWidget();
		this.treeLinesVisible = treeLinesVisible;
		redraw();
	}

	/**
	 * Sets the row header renderer.
	 *
	 * @param rowHeaderRenderer The rowHeaderRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setRowHeaderRenderer(final IRenderer rowHeaderRenderer) {
		checkWidget();
		rowHeaderRenderer.setDisplay(getDisplay());
		this.rowHeaderRenderer = rowHeaderRenderer;
	}

	/**
	 * Marks the receiver's row header as visible if the argument is {@code true},
	 * and marks it invisible otherwise. When row headers are visible, horizontal
	 * scrolling is always done by column rather than by pixel.
	 *
	 * @param show the new visibility state
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setRowHeaderVisible(final boolean show) {
		setRowHeaderVisible(show, 1);
	}

	/**
	 * Marks the receiver's row header as visible if the argument is {@code true},
	 * and marks it invisible otherwise. When row headers are visible, horizontal
	 * scrolling is always done by column rather than by pixel.
	 *
	 * @param show     the new visibility state
	 * @param minWidth the minimun width of the row column
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setRowHeaderVisible(final boolean show, final int minWidth) {
		checkWidget();
		rowHeaderVisible = show;
		setColumnScrolling(true);

		if (show && isAutoWidth()) {
			computeRowHeaderWidth(minWidth);
		}

		redraw();
	}

	/**
	 * Selects the item at the given zero-relative index in the receiver. The
	 * current selection is first cleared, then the new item is selected.
	 * <p>
	 * If cell selection is enabled, all cells within the item at the given index
	 * are selected.
	 *
	 * @param index the index of the item to select
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setSelection(final int index) {
		checkWidget();

		if (!selectionEnabled) {
			return;
		}

		if (index >= 0 && index < items.size()) {
			if (!cellSelectionEnabled) {
				selectedItems.clear();
				selectedItems.add(items.get(index));
				redraw();
			} else {
				selectedCells.clear();
				selectCells(getCells(items.get(index)));
			}
		}
	}

	/**
	 * Selects the items in the range specified by the given zero-relative indices
	 * in the receiver. The range of indices is inclusive. The current selection is
	 * cleared before the new items are selected.
	 * <p>
	 * Indices that are out of range are ignored and no items will be selected if
	 * start is greater than end. If the receiver is single-select and there is more
	 * than one item in the given range, then all indices are ignored.
	 * <p>
	 * If cell selection is enabled, all cells within the given range are selected.
	 *
	 * @param start the start index of the items to select
	 * @param end   the end index of the items to select
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see Grid#deselectAll()
	 * @see Grid#select(int,int)
	 */
	public void setSelection(final int start, final int end) {
		checkWidget();

		if (!selectionEnabled) {
			return;
		}

		if (selectionType == GridSelectionType.SINGLE && start != end) {
			return;
		}

		if (!cellSelectionEnabled) {
			selectedItems.clear();
		} else {
			selectedCells.clear();
		}

		for (int i = start; i <= end; i++) {
			if (i < 0) {
				continue;
			}
			if (i > items.size() - 1) {
				break;
			}

			final GridItem item = items.get(i);

			if (!cellSelectionEnabled) {
				selectedItems.add(item);
			} else {
				selectCells(getCells(item));
			}
		}
		redraw();
	}

	/**
	 * If <code>true</code>, column pack is based only with the visible lines (from
	 * topIndex to bottomIndex). <code>false</code> pack is in default mode.
	 *
	 * @return optimizedColumnPack value
	 */
	public boolean isVisibleLinesColumnPack() {
		return visibleLinesBasedColumnPack;
	}

	/**
	 * Set optimizedColumnPack to <code>true</code> for column pack based only with
	 * the visible lines.
	 *
	 * @param visibleLinesBasedColumnPack true to pack based on visible lines only,
	 *                                    false otherwise
	 */
	public void setVisibleLinesColumnPack(final boolean visibleLinesBasedColumnPack) {
		this.visibleLinesBasedColumnPack = visibleLinesBasedColumnPack;
	}

	/**
	 * Selects the items at the given zero-relative indices in the receiver. The
	 * current selection is cleared before the new items are selected.
	 * <p>
	 * Indices that are out of range and duplicate indices are ignored. If the
	 * receiver is single-select and multiple indices are specified, then all
	 * indices are ignored.
	 * <p>
	 * If cell selection is enabled, all cells within the given indices are
	 * selected.
	 *
	 * @param indices the indices of the items to select
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the array
	 *                                      of indices is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see Grid#deselectAll()
	 * @see Grid#select(int[])
	 */
	public void setSelection(final int[] indices) {
		checkWidget();

		if (!selectionEnabled) {
			return;
		}

		if (selectionType == GridSelectionType.SINGLE && indices.length > 1) {
			return;
		}

		if (!cellSelectionEnabled) {
			selectedItems.clear();
		} else {
			selectedCells.clear();
		}

		for (final int j : indices) {
			if (j < 0) {
				continue;
			}
			if (j > items.size() - 1) {
				break;
			}

			final GridItem item = items.get(j);

			if (!cellSelectionEnabled) {
				selectedItems.add(item);
			} else {
				selectCells(getCells(item));
			}
		}
		redraw();
	}

	/**
	 * Sets the receiver's selection to be the given array of items. The current
	 * selection is cleared before the new items are selected.
	 * <p>
	 * Items that are not in the receiver are ignored. If the receiver is
	 * single-select and multiple items are specified, then all items are ignored.
	 * If cell selection is enabled, all cells within the given items are selected.
	 *
	 * @param _items the array of items
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the array
	 *                                      of items is null</li>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if one of
	 *                                      the items has been disposed</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 * @see Grid#deselectAll()
	 * @see Grid#select(int[])
	 * @see Grid#setSelection(int[])
	 */
	public void setSelection(final GridItem[] _items) {
		checkWidget();

		if (!selectionEnabled) {
			return;
		}

		if (_items == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (selectionType == GridSelectionType.SINGLE && _items.length > 1) {
			return;
		}

		if (!cellSelectionEnabled) {
			selectedItems.clear();
		} else {
			selectedCells.clear();
		}

		for (final GridItem item : _items) {
			if (item == null) {
				continue;
			}
			if (item.isDisposed()) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
			if (item.getParent() != this) {
				continue;
			}

			if (!cellSelectionEnabled) {
				selectedItems.add(item);
			} else {
				selectCells(getCells(item));
			}
		}

		redraw();
	}

	/**
	 * Sets the zero-relative index of the item which is currently at the top of the
	 * receiver. This index can change when items are scrolled or new items are
	 * added and removed.
	 *
	 * @param index the index of the top item
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setTopIndex(final int index) {
		checkWidget();
		if (index < 0 || index >= items.size()) {
			return;
		}

		final GridItem item = items.get(index);
		if (!item.isVisible()) {
			return;
		}

		if (!vScroll.getVisible()) {
			return;
		}

		int vScrollAmount = 0;

		for (int i = 0; i < index; i++) {
			if (items.get(i).isVisible()) {
				vScrollAmount++;
			}
		}

		vScroll.setSelection(vScrollAmount);
		topIndex = -1;
		bottomIndex = -1;
		redraw();
	}

	/**
	 * Sets the top left renderer.
	 *
	 * @param topLeftRenderer The topLeftRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setTopLeftRenderer(final IRenderer topLeftRenderer) {
		checkWidget();
		topLeftRenderer.setDisplay(getDisplay());
		this.topLeftRenderer = topLeftRenderer;
	}

	/**
	 * Sets the bottom left renderer.
	 *
	 * @param bottomLeftRenderer The topLeftRenderer to set.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setBottomLeftRenderer(final IRenderer bottomLeftRenderer) {
		checkWidget();
		bottomLeftRenderer.setDisplay(getDisplay());
		this.bottomLeftRenderer = bottomLeftRenderer;
	}

	/**
	 * Shows the column. If the column is already showing in the receiver, this
	 * method simply returns. Otherwise, the columns are scrolled until the column
	 * is visible.
	 *
	 * @param col the column to be shown
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void showColumn(final GridColumn col) {
		checkWidget();

		if (!col.isVisible()) {
			final GridColumnGroup group = col.getColumnGroup();
			group.setExpanded(!group.getExpanded());
			if (group.getExpanded()) {
				group.notifyListeners(SWT.Expand, new Event());
			} else {
				group.notifyListeners(SWT.Collapse, new Event());
			}
		}

		if (!hScroll.getVisible()) {
			return;
		}

		int x = getColumnHeaderXPosition(col);

		int firstVisibleX = 0;
		if (rowHeaderVisible) {
			firstVisibleX = rowHeaderWidth;
		}

		// if its visible just return
		if (x >= firstVisibleX && x + col.getWidth() <= firstVisibleX + getClientArea().width - firstVisibleX) {
			return;
		}

		if (!getColumnScrolling()) {
			if (x < firstVisibleX) {
				hScroll.setSelection(getHScrollSelectionInPixels() - (firstVisibleX - x));
			} else {
				if (col.getWidth() > getClientArea().width - firstVisibleX) {
					hScroll.setSelection(getHScrollSelectionInPixels() + x - firstVisibleX);
				} else {
					x -= getClientArea().width - firstVisibleX - col.getWidth();
					hScroll.setSelection(getHScrollSelectionInPixels() + x - firstVisibleX);
				}
			}
		} else {
			if (x < firstVisibleX || col.getWidth() > getClientArea().width - firstVisibleX) {
				final int sel = displayOrderedColumns.indexOf(col);
				hScroll.setSelection(sel);
			} else {
				int availableWidth = getClientArea().width - firstVisibleX - col.getWidth();

				GridColumn prevCol = getPreviousVisibleColumn(col);
				GridColumn currentScrollTo = col;

				while (true) {
					if (prevCol == null || prevCol.getWidth() > availableWidth) {
						final int sel = displayOrderedColumns.indexOf(currentScrollTo);
						hScroll.setSelection(sel);
						break;
					} else {
						availableWidth -= prevCol.getWidth();
						currentScrollTo = prevCol;
						prevCol = getPreviousVisibleColumn(prevCol);
					}
				}
			}
		}

		redraw();
	}

	/**
	 * Returns true if 'item' is currently being <em>completely</em> shown in this
	 * <code>Grid</code>'s visible on-screen area.
	 *
	 * <p>
	 * Here, "completely" only refers to the item's height, not its width. This
	 * means this method returns true also if some cells are horizontally scrolled
	 * away.
	 *
	 * @param item the grid item to check
	 * @return true if 'item' is shown
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if 'item'
	 *                                      is not contained in the receiver</li>
	 *                                      </ul>
	 */
	boolean isShown(final GridItem item) {
		checkWidget();

		if (!item.isVisible()) {
			return false;
		}

		final int itemIndex = item.getRowIndex();

		if (itemIndex == -1) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		final int firstVisibleIndex = getTopIndex();
		final int lastVisibleIndex = getBottomIndex();

		return itemIndex >= firstVisibleIndex && itemIndex < lastVisibleIndex
				|| itemIndex == lastVisibleIndex && bottomIndexShownCompletely;
	}

	/**
	 * Shows the item. If the item is already showing in the receiver, this method
	 * simply returns. Otherwise, the items are scrolled until the item is visible.
	 *
	 * @param item the item to be shown
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if 'item'
	 *                                      is not contained in the receiver</li>
	 *                                      </ul>
	 */
	public void showItem(final GridItem item) {
		checkWidget();

		updateScrollbars();

		// if no items are visible on screen then abort
		if (getVisibleGridHeight() < 1) {
			return;
		}

		// if its visible just return
		if (isShown(item)) {
			return;
		}

		if (!item.isVisible()) {
			GridItem parent = item.getParentItem();
			do {
				if (!parent.isExpanded()) {
					parent.setExpanded(true);
					parent.fireEvent(SWT.Expand);
				}
				parent = parent.getParentItem();
			} while (parent != null);
		}

		int newTopIndex = item.getRowIndex();

		if (newTopIndex >= getBottomIndex()) {
			final RowRange range = getRowRange(newTopIndex, getVisibleGridHeight(), true, true); // note: inverse==true
			newTopIndex = range.startIndex; // note: use startIndex because of inverse==true
		}

		setTopIndex(newTopIndex);
	}

	/**
	 * Shows the selection. If the selection is already showing in the receiver,
	 * this method simply returns. Otherwise, the items are scrolled until the
	 * selection is visible.
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void showSelection() {
		checkWidget();

		if (scrollValuesObsolete) {
			updateScrollbars();
		}

		if (!cellSelectionEnabled) {
			if (selectedItems.size() == 0) {
				return;
			}

			showItem(selectedItems.get(0));
		} else {
			if (selectedCells.size() == 0) {
				return;
			}

			showItem(getItem(selectedCells.get(0).y));
			showColumn(getColumn(selectedCells.get(0).x));
		}

	}

	/**
	 * Enables selection highlighting if the argument is <code>true</code>.
	 *
	 * @param selectionEnabled the selection enabled state
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setSelectionEnabled(final boolean selectionEnabled) {
		checkWidget();

		if (!selectionEnabled) {
			selectedItems.clear();
			redraw();
		}

		this.selectionEnabled = selectionEnabled;
	}

	/**
	 * Returns <code>true</code> if selection is enabled, false otherwise.
	 *
	 * @return the selection enabled state
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean getSelectionEnabled() {
		checkWidget();
		return selectionEnabled;
	}

	/**
	 * Computes and sets the height of the header row. This method will ask for the
	 * preferred size of all the column headers and use the max.
	 *
	 * @param gc GC for font metrics, etc.
	 */
	private void computeHeaderHeight(final GC gc) {

		int colHeaderHeight = 0;
		for (final GridColumn column : columns) {
			colHeaderHeight = Math.max(column.getHeaderHeight(gc), colHeaderHeight);
		}

		int groupHeight = 0;
		for (final GridColumnGroup group : columnGroups) {
			groupHeight = Math.max(group.getHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, group).y,
					groupHeight);
		}

		headerHeight = colHeaderHeight + groupHeight;
		groupHeaderHeight = groupHeight;
	}

	private void computeHeaderHeight() {
		estimate(this::computeHeaderHeight);
	}

	private void computeFooterHeight(final GC gc) {

		int colFooterHeight = 0;
		for (final GridColumn column : columns) {
			colFooterHeight = Math.max(column.getFooterHeight(gc), colFooterHeight);
		}

		footerHeight = colFooterHeight;
	}

	/**
	 * Returns the computed default item height. Currently this method just gets the
	 * preferred size of all the cells in the given row and returns that (it is then
	 * used as the height of all rows with items having a height of -1).
	 *
	 * @param item item to use for sizing
	 * @param gc   GC used to perform font metrics,etc.
	 * @return the row height
	 */
	private int computeItemHeight(final GridItem item, final GC gc) {
		int height = 1;

		if (columns.size() == 0 || items.size() == 0) {
			return height;
		}

		for (final GridColumn column : columns) {
			column.getCellRenderer().setColumn(column.index);
			height = Math.max(height, column.getCellRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, item).y);
		}

		if (rowHeaderVisible && rowHeaderRenderer != null) {
			height = Math.max(height, rowHeaderRenderer.computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, item).y);
		}

		return height <= 0 ? 16 : height;
	}

	private int computeItemHeight(final GridItem item) {
		return estimateWithResult(sizingGC -> computeItemHeight(item, sizingGC));
	}

	/**
	 * Returns the x position of the given column. Takes into account scroll
	 * position.
	 *
	 * @param column given column
	 * @return x position
	 */
	private int getColumnHeaderXPosition(final GridColumn column) {
		if (!column.isVisible()) {
			return -1;
		}

		int x = 0;

		x -= getHScrollSelectionInPixels();

		if (rowHeaderVisible) {
			x += rowHeaderWidth;
		}
		for (final GridColumn column2 : displayOrderedColumns) {

			if (!column2.isVisible()) {
				continue;
			}

			if (column2 == column) {
				break;
			}

			x += column2.getWidth();
		}

		return x;
	}

	/**
	 * Returns the hscroll selection in pixels. This method abstracts away the
	 * differences between column by column scrolling and pixel based scrolling.
	 *
	 * @return the horizontal scroll selection in pixels
	 */
	private int getHScrollSelectionInPixels() {
		int selection = hScroll.getSelection();
		if (columnScrolling) {
			int pixels = 0;
			for (int i = 0; i < selection; i++) {
				final GridColumn gridColumn = displayOrderedColumns.get(i);
				if (gridColumn.isVisible()) {
					pixels += gridColumn.getWidth();
				} else if (selection < displayOrderedColumns.size() - 1) {
					selection += 1;
				}
			}
			selection = pixels;
		}
		return selection;
	}

	/**
	 * Returns the size of the preferred size of the inner table.
	 *
	 * @return the preferred size of the table.
	 */
	private Point getTableSize() {
		int x = 0;
		int y = 0;

		if (columnHeadersVisible) {
			y += headerHeight;
		}

		if (columnFootersVisible) {
			y += footerHeight;
		}

		y += getGridHeight();

		if (rowHeaderVisible) {
			x += rowHeaderWidth;
		}

		for (final GridColumn column : columns) {
			if (column.isVisible()) {
				x += column.getWidth();
			}
		}

		return new Point(x, y);
	}

	/**
	 * Manages the header column dragging and calculates the drop point, triggers a
	 * redraw.
	 *
	 * @param x mouse x
	 * @return true if this event has been consumed.
	 */
	private boolean handleColumnDragging(final int x) {

		GridColumn local_dragDropBeforeColumn = null;
		GridColumn local_dragDropAfterColumn = null;

		int x2 = 1;

		if (rowHeaderVisible) {
			x2 += rowHeaderWidth + 1;
		}

		x2 -= getHScrollSelectionInPixels();

		GridColumn previousVisibleCol = null;
		boolean nextVisibleColumnIsBeforeCol = false;
		GridColumn firstVisibleCol = null;
		GridColumn lastVisibleCol = null;

		if (x < x2) {
			for (final GridColumn column : displayOrderedColumns) {
				if (!column.isVisible()) {
					continue;
				}
				local_dragDropBeforeColumn = column;
				break;
			}
			local_dragDropAfterColumn = null;
		} else {
			for (final GridColumn column : displayOrderedColumns) {
				if (!column.isVisible()) {
					continue;
				}

				if (firstVisibleCol == null) {
					firstVisibleCol = column;
				}
				lastVisibleCol = column;

				if (nextVisibleColumnIsBeforeCol) {
					local_dragDropBeforeColumn = column;
					nextVisibleColumnIsBeforeCol = false;
				}

				if (x >= x2 && x <= x2 + column.getWidth()) {
					if (x <= x2 + column.getWidth() / 2) {
						local_dragDropBeforeColumn = column;
						local_dragDropAfterColumn = previousVisibleCol;
					} else {
						local_dragDropAfterColumn = column;

						// the next visible column is the before col
						nextVisibleColumnIsBeforeCol = true;
					}
				}

				x2 += column.getWidth();
				previousVisibleCol = column;
			}

			if (local_dragDropBeforeColumn == null) {
				local_dragDropAfterColumn = lastVisibleCol;
			}
		}

		currentHeaderDragX = x;

		if (local_dragDropBeforeColumn != dragDropBeforeColumn
				|| dragDropBeforeColumn == null && dragDropAfterColumn == null) {
			dragDropPointValid = true;

			// Determine if valid drop point
			if (columnGroups.length != 0) {

				if (columnBeingPushed.getColumnGroup() == null) {
					if (local_dragDropBeforeColumn != null && local_dragDropAfterColumn != null
							&& local_dragDropBeforeColumn.getColumnGroup() != null && local_dragDropBeforeColumn
									.getColumnGroup() == local_dragDropAfterColumn.getColumnGroup()) {
						// Dont move a column w/o a group in between two columns
						// in the same group
						dragDropPointValid = false;
					}
				} else {
					if (!(local_dragDropBeforeColumn != null
							&& local_dragDropBeforeColumn.getColumnGroup() == columnBeingPushed.getColumnGroup())
							&& !(local_dragDropAfterColumn != null && local_dragDropAfterColumn
									.getColumnGroup() == columnBeingPushed.getColumnGroup())) {
						// Dont move a column with a group
						dragDropPointValid = false;
					}
				}
			} else {
				dragDropPointValid = true;
			}
		}

		dragDropBeforeColumn = local_dragDropBeforeColumn;
		dragDropAfterColumn = local_dragDropAfterColumn;

		final Rectangle clientArea = getClientArea();
		redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

		return true;
	}

	/**
	 * Handles the moving of columns after a column is dropped.
	 */
	private void handleColumnDrop() {
		draggingColumn = false;

		if (dragDropBeforeColumn != columnBeingPushed && dragDropAfterColumn != columnBeingPushed
				&& (columnGroups.length == 0 || dragDropPointValid)) {

			int notifyFrom = displayOrderedColumns.indexOf(columnBeingPushed);
			int notifyTo = notifyFrom;

			displayOrderedColumns.remove(columnBeingPushed);

			if (dragDropBeforeColumn == null) {

				notifyTo = displayOrderedColumns.size();
				displayOrderedColumns.add(columnBeingPushed);
			} else if (dragDropAfterColumn == null) {
				displayOrderedColumns.add(0, columnBeingPushed);
				notifyFrom = 0;
			} else {
				int insertAtIndex = 0;

				if (columnGroups.length != 0) {
					// ensure that we aren't putting this column into a group,
					// this is possible if
					// there are invisible columns between the after and before
					// cols

					if (dragDropBeforeColumn.getColumnGroup() == columnBeingPushed.getColumnGroup()) {
						insertAtIndex = displayOrderedColumns.indexOf(dragDropBeforeColumn);
					} else if (dragDropAfterColumn.getColumnGroup() == columnBeingPushed.getColumnGroup()) {
						insertAtIndex = displayOrderedColumns.indexOf(dragDropAfterColumn) + 1;
					} else {
						if (dragDropBeforeColumn.getColumnGroup() == null) {
							insertAtIndex = displayOrderedColumns.indexOf(dragDropBeforeColumn);
						} else {
							final GridColumnGroup beforeGroup = dragDropBeforeColumn.getColumnGroup();
							insertAtIndex = displayOrderedColumns.indexOf(dragDropBeforeColumn);
							while (insertAtIndex > 0
									&& displayOrderedColumns.get(insertAtIndex - 1).getColumnGroup() == beforeGroup) {
								insertAtIndex--;
							}

						}
					}
				} else {
					insertAtIndex = displayOrderedColumns.indexOf(dragDropBeforeColumn);
				}
				displayOrderedColumns.add(insertAtIndex, columnBeingPushed);
				notifyFrom = Math.min(notifyFrom, insertAtIndex);
				notifyTo = Math.max(notifyTo, insertAtIndex);
			}

			for (int i = notifyFrom; i <= notifyTo; i++) {
				displayOrderedColumns.get(i).fireMoved();
			}
			clearDisplayOrderedCache();
		}
		redraw();
	}

	/**
	 * Determines if the mouse is pushing the header but has since move out of the
	 * header bounds and therefore should be drawn unpushed. Also initiates a column
	 * header drag when appropriate.
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return true if this event has been consumed.
	 */
	private boolean handleColumnHeaderHoverWhilePushing(final int x, final int y) {
		final GridColumn overThis = overColumnHeader(x, y);

		if (overThis == columnBeingPushed != pushingAndHovering) {
			pushingAndHovering = overThis == columnBeingPushed;
			redraw();
		}
		if (columnBeingPushed.getMoveable()) {

			if (pushingAndHovering && Math.abs(startHeaderPushX - x) > 3) {

				// stop pushing
				pushingColumn = false;
				columnBeingPushed.getHeaderRenderer().setMouseDown(false);
				columnBeingPushed.getHeaderRenderer().setHover(false);

				// now dragging
				draggingColumn = true;
				columnBeingPushed.getHeaderRenderer().setMouseDown(false);

				startHeaderDragX = x;

				dragDropAfterColumn = null;
				dragDropBeforeColumn = null;
				dragDropPointValid = true;

				handleColumnDragging(x);
			}
		}

		return true;
	}

	/**
	 * Determines if a column group header has been clicked and forwards the event
	 * to the header renderer.
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return true if this event has been consumed.
	 */
	private boolean handleColumnGroupHeaderClick(final int x, final int y) {

		if (!columnHeadersVisible) {
			return false;
		}

		final GridColumnGroup overThis = overColumnGroupHeader(x, y);

		if (overThis == null) {
			return false;
		}

		int headerX = 0;
		if (rowHeaderVisible) {
			headerX += rowHeaderWidth;
		}

		int width = 0;
		boolean firstCol = false;

		for (final GridColumn col : displayOrderedColumns) {
			if (col.getColumnGroup() == overThis && col.isVisible()) {
				firstCol = true;
				width += col.getWidth();
			}
			if (!firstCol && col.isVisible()) {
				headerX += col.getWidth();
			}
		}

		overThis.getHeaderRenderer().setBounds(headerX - getHScrollSelectionInPixels(), 0, width, groupHeaderHeight);
		return overThis.getHeaderRenderer().notify(IInternalWidget.LeftMouseButtonDown, new Point(x, y), overThis);
	}

	/**
	 * Determines if a column header has been clicked, updates the renderer state
	 * and triggers a redraw if necesary.
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return true if this event has been consumed.
	 */
	private boolean handleColumnHeaderPush(final int x, final int y) {
		if (!columnHeadersVisible) {
			return false;
		}

		final ScrollBar verticalBar = getVerticalBar();
		final boolean clickOnScrollBar = x >= getClientArea().width;
		if (clickOnScrollBar && verticalBar != null && verticalBar.isVisible()) {
			// Bug 273916 : if one clicks on the tooltip and the mouse is located on the
			// scrollbar, simulate a click on the scrollbar
			verticalBar.setSelection(verticalBar.getSelection() - verticalBar.getIncrement());
		}

		final GridColumn overThis = overColumnHeader(x, y);
		if (overThis == null) {
			return false;
		}

		columnBeingPushed = overThis;

		// draw pushed
		columnBeingPushed.getHeaderRenderer().setMouseDown(true);
		columnBeingPushed.getHeaderRenderer().setHover(true);
		pushingAndHovering = true;
		redraw();

		startHeaderPushX = x;
		pushingColumn = true;

		setCapture(true);

		return true;
	}

	private boolean handleColumnFooterPush(final int x, final int y) {
		if (!columnFootersVisible) {
			return false;
		}

		return overColumnFooter(x, y) != null;
	}

	/**
	 * Sets the new width of the column being resized and fires the appropriate
	 * listeners.
	 *
	 * @param x mouse x
	 */
	private void handleColumnResizerDragging(final int x) {
		int delta = x - resizingStartX;
		// If resizing inverted (next column after FILL), invert the delta
		if (resizingInverted) {
			delta = -delta;
		}
		int newWidth = resizingColumnStartWidth + delta;
		if (newWidth < MIN_COLUMN_HEADER_WIDTH) {
			newWidth = MIN_COLUMN_HEADER_WIDTH;
		}

		if (columnScrolling) {
			int maxWidth = getClientArea().width;
			if (rowHeaderVisible) {
				maxWidth -= rowHeaderWidth;
			}
			if (newWidth > maxWidth) {
				newWidth = maxWidth;
			}
		}

		if (newWidth == columnBeingResized.getWidth()) {
			return;
		}

		columnBeingResized.setWidth(newWidth, false);
		scrollValuesObsolete = true;

		final Rectangle clientArea = getClientArea();
		redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

		columnBeingResized.fireResized();

		fireColumnsMoved();
	}

	void fireColumnsMoved() {
		for (int index = displayOrderedColumns.indexOf(columnBeingResized) + 1; index < displayOrderedColumns
				.size(); index++) {
			final GridColumn col = displayOrderedColumns.get(index);
			if (col.isVisible()) {
				col.fireMoved();
			}
		}
	}

	void handlePacked(final GridColumn column) {
		int index = 0;

		if (getHorizontalBar() != null) {
			if (!getHorizontalBar().isVisible()) {
				index = displayOrderedColumns.indexOf(column);
			}
		}

		for (; index < displayOrderedColumns.size(); index++) {
			final GridColumn col = displayOrderedColumns.get(index);
			if (col.isVisible()) {
				col.fireMoved();
			}
		}
	}

	/**
	 * Sets the new height of the item of the row being resized and fires the
	 * appropriate listeners.
	 *
	 * @param x mouse x
	 */
	private void handleRowResizerDragging(final int y) {
		int newHeight = resizingRowStartHeight + y - resizingStartY;
		if (newHeight < MIN_ROW_HEADER_HEIGHT) {
			newHeight = MIN_ROW_HEADER_HEIGHT;
		}

		if (newHeight > getClientArea().height) {
			newHeight = getClientArea().height;
		}

		if (rowBeingResized == null || newHeight == rowBeingResized.getHeight()) {
			return;
		}

		final Event e = new Event();
		e.item = rowBeingResized;
		e.widget = this;
		e.detail = newHeight;

		rowBeingResized.notifyListeners(SWT.Resize, e);

		if (e.doit == false) {
			return;
		}

		newHeight = e.detail;

		if (newHeight < MIN_ROW_HEADER_HEIGHT) {
			newHeight = MIN_ROW_HEADER_HEIGHT;
		}

		if (newHeight > getClientArea().height) {
			newHeight = getClientArea().height;
		}

		rowBeingResized.setHeight(newHeight);
		scrollValuesObsolete = true;

		final Rectangle clientArea = getClientArea();
		redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);
	}

	/**
	 * Determines if the mouse is hovering on a column resizer and changes the
	 * pointer and sets field appropriately.
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return true if this event has been consumed.
	 */
	private boolean handleHoverOnColumnResizer(final int x, final int y) {
		boolean over = false;
		if (y <= headerHeight) {
			int x2 = 0;

			if (rowHeaderVisible) {
				x2 += rowHeaderWidth;
			}

			x2 -= getHScrollSelectionInPixels();
			final int extraFill = getExtraFill();
			for (int i = 0; i < displayOrderedColumns.size(); i++) {
				final GridColumn column = displayOrderedColumns.get(i);
				if (!column.isVisible()) {
					continue;
				}
				x2 += column.getWidth(extraFill);

				if (x2 >= x - COLUMN_RESIZER_THRESHOLD && x2 <= x + COLUMN_RESIZER_THRESHOLD) {
					if (column.getResizeable()) {
						if (column.getColumnGroup() != null && y <= groupHeaderHeight) {
							// if this is not the last column
							if (column != column.getColumnGroup().getLastVisibleColumn()) {
								break;
							}
						}

						over = true;
						columnBeingResized = column;
						resizingInverted = false;

						// If this column has FILL style, we should resize the next column instead
						if (column.isFill()) {
							// Find the next visible column
							GridColumn nextColumn = null;
							for (int j = i + 1; j < displayOrderedColumns.size(); j++) {
								final GridColumn candidate = displayOrderedColumns.get(j);
								if (candidate.isVisible() && candidate.getResizeable()) {
									nextColumn = candidate;
									break;
								}
							}
							if (nextColumn != null) {
								columnBeingResized = nextColumn;
								resizingInverted = true;
							}
						}
					}
					break;
				}
			}
		}

		if (over != hoveringOnColumnResizer) {
			if (over) {
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE));
			} else {
				columnBeingResized = null;
				resizingInverted = false;
				setCursor(null);
			}
			hoveringOnColumnResizer = over;
		}
		return over;
	}

	/**
	 * Determines if the mouse is hovering on a row resizer and changes the pointer
	 * and sets field appropriately.
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return true if this event has been consumed.
	 */
	private boolean handleHoverOnRowResizer(final int x, final int y) {
		rowBeingResized = null;
		boolean over = false;
		if (x <= rowHeaderWidth) {
			int y2 = 0;

			if (columnHeadersVisible) {
				y2 += headerHeight;
			}

			int row = getTopIndex();
			while (row < items.size() && y2 <= getClientArea().height) {
				final GridItem currItem = items.get(row);
				if (currItem.isVisible()) {
					y2 += currItem.getHeight() + 1;

					if (y2 >= y - ROW_RESIZER_THRESHOLD && y2 <= y + ROW_RESIZER_THRESHOLD) {
						// if (currItem.isResizeable())
						{
							over = true;
							rowBeingResized = currItem;
						}
						// do not brake here, because in case of overlapping
						// row resizers we need to find the last one
					} else {
						if (rowBeingResized != null) {
							// we have passed all (overlapping) row resizers, so break
							break;
						}
					}
				}
				row++;
			}
		}

		if (over != hoveringOnRowResizer) {
			if (over) {
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
			} else {
				rowBeingResized = null;
				setCursor(null);
			}
			hoveringOnRowResizer = over;
		}
		return over;
	}

	/**
	 * Returns the cell at the given point in the receiver or null if no such cell
	 * exists. The point is in the coordinate system of the receiver.
	 *
	 * @param point the point used to locate the item
	 * @return the cell at the given point
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the point
	 *                                      is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public Point getCell(final Point point) {
		checkWidget();

		if (point == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (point.x < 0 || point.x > getClientArea().width) {
			return null;
		}

		final GridItem item = getItem(point);
		final GridColumn column = getColumn(point);

		if (item != null && column != null) {
			return new Point(column.index, item.getRowIndex());
		}

		return null;
	}

	/**
	 * Paints.
	 *
	 * @param event paint event
	 */
	private void onPaint(final PaintEvent event) {
		final InsertMark insertMark = new InsertMark(); // we will populate these values while drawing the cells

		final GridCellSpanManager cellSpanManager = new GridCellSpanManager();
		final GC gc = event.gc;
		final Rectangle originalClipping = gc.getClipping();

		gc.setBackground(getBackground());
		final Point controlSize = getSize();
		this.drawBackground(gc, 0, 0, controlSize.x, controlSize.y);

		if (scrollValuesObsolete) {
			updateScrollbars();
			scrollValuesObsolete = false;
		}

		int y = 0;
		final int extraFill = getExtraFill(controlSize);
		if (columnHeadersVisible) {
			paintHeader(gc, extraFill);
			y += headerHeight;
		}

		final Rectangle clientArea = getClientArea();
		final int availableHeight = clientArea.height - y;
		int visibleRows = availableHeight / itemHeight + 1;
		if (items.size() > 0 && availableHeight > 0) {
			final RowRange range = getRowRange(getTopIndex(), availableHeight, false, false);
			if (range.height >= availableHeight) {
				visibleRows = range.rows;
			} else {
				visibleRows = range.rows + (availableHeight - range.height) / itemHeight + 1;
			}
		}

		final int firstVisibleIndex = getTopIndex();
		int firstItemToDraw = firstVisibleIndex;

		final List<GridColumn> cols = displayOrderedColumns;
		if (hasSpanning) {
			// We need to find the first Item to draw. An earlier item can row-span the
			// first visible item.
			for (int rowIndex = 0; rowIndex < firstVisibleIndex && rowIndex < items.size(); rowIndex++) {
				int colIndex = 0;

				int maxRowSpanForItem = 0;
				for (final GridColumn column : cols) {

					if (!column.isVisible()) {
						colIndex++;
						continue;
					}

					final int rowSpan = items.get(rowIndex).getRowSpan(colIndex);
					maxRowSpanForItem = rowSpan > maxRowSpanForItem ? rowSpan : maxRowSpanForItem;
					colIndex++;
				}

				if (rowIndex + maxRowSpanForItem >= firstVisibleIndex) {
					firstItemToDraw = rowIndex;
					break;
				} else {
					rowIndex += maxRowSpanForItem;
				}
			}

			for (int rowIndex = firstItemToDraw; rowIndex < firstVisibleIndex && rowIndex < items.size(); rowIndex++) {
				final GridItem itemForRow = items.get(rowIndex);
				if (itemForRow.isVisible()) {
					y = y - itemForRow.getHeight() - 1;
				}
			}
		}
		final int hscroll = getHScrollSelectionInPixels();
		paintRows(cols, false, firstItemToDraw, visibleRows, hscroll, cellSpanManager, gc, originalClipping, y,
				clientArea, firstVisibleIndex, insertMark, extraFill);

		// draw drop point
		if (draggingColumn) {
			if ((dragDropAfterColumn != null || dragDropBeforeColumn != null)
					&& dragDropAfterColumn != columnBeingPushed && dragDropBeforeColumn != columnBeingPushed
					&& dragDropPointValid) {
				int x;
				if (dragDropBeforeColumn != null) {
					x = getColumnHeaderXPosition(dragDropBeforeColumn);
				} else {
					x = getColumnHeaderXPosition(dragDropAfterColumn) + dragDropAfterColumn.getWidth();
				}

				final Point size = dropPointRenderer.computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, null);
				x -= size.x / 2;
				if (x < 0) {
					x = 0;
				}
				dropPointRenderer.setBounds(x - 1, headerHeight + DROP_POINT_LOWER_OFFSET, size.x, size.y);
				dropPointRenderer.paint(gc, null);
			}
		}
		final FixedGridColumns fixed = getFixedGridColumns();
		if (fixed.hasColumns() && hscroll > fixed.offset()) {
			paintRows(fixed.columns(), true, firstItemToDraw, visibleRows, 0, cellSpanManager, gc, originalClipping, y,
					clientArea, firstVisibleIndex, insertMark, extraFill);
		}

		// draw insertion mark
		if (insertMark.posFound) {
			final Rectangle rect = new Rectangle(rowHeaderVisible ? rowHeaderWidth : 0,
					columnHeadersVisible ? headerHeight : 0, clientArea.width, clientArea.height);
			gc.setClipping(originalClipping.intersection(rect));
			insertMarkRenderer.paint(gc,
					new Rectangle(insertMark.posX1, insertMark.posY, insertMark.posX2 - insertMark.posX1, 0));
		}

		if (columnFootersVisible) {
			paintFooter(gc);
		}
	}

	private FixedGridColumns getFixedGridColumns() {
		final List<GridColumn> fixedColumns = new ArrayList<>();
		int fixedOffset = 0;
		for (final GridColumn gridColumn : displayOrderedColumns) {
			if (gridColumn.isFixed()) {
				fixedColumns.add(gridColumn);
			} else if (fixedColumns.isEmpty()) {
				fixedOffset += gridColumn.getWidth();
			}
		}
		return new FixedGridColumns(fixedColumns, fixedOffset);
	}

	private void paintRows(final List<GridColumn> cols, final boolean fixed, final int firstRow, final int visibleRows, final int hScroll,
			final GridCellSpanManager cellSpanManager, final GC gc, final Rectangle originalClipping, int y,
			final Rectangle clientArea, final int firstVisibleIndex, final InsertMark insertMark, final int extraFill) {
		int row = firstRow;
		final int columnCount = cols.size();
		for (int i = 0; i < visibleRows + firstVisibleIndex - firstRow; i++) {

			int x = -hScroll;
			// get the item to draw
			GridItem item = null;
			if (row < items.size()) {
				item = items.get(row);

				while (!item.isVisible() && row < items.size() - 1) {
					row++;
					item = items.get(row);
				}
			}
			if (item != null && !item.isVisible()) {
				item = null;
			}

			if (item != null) {
				boolean cellInRowSelected = false;

				if (rowHeaderVisible) {

					// row header is actually painted later
					x += rowHeaderWidth;
				}

				final int focusY = y;

				int colIndex = 0;

				// draw regular cells for each column
				for (final GridColumn column : cols) {

					final boolean skipCell = cellSpanManager.skipCell(colIndex, row);
					final int indexOfColumn = column.index;

					if (!column.isVisible()) {
						colIndex++;
						if (skipCell) {
							cellSpanManager.consumeCell(colIndex, row);
						}
						continue;
					}

					final Point cellSize = item.getCellSize(indexOfColumn, extraFill);
					final int width = cellSize.x;

					if (skipCell == false) {

						final int nrRowsToSpan = item.getRowSpan(indexOfColumn);
						final int nrColumnsToSpan = item.getColumnSpan(indexOfColumn);

						if (nrRowsToSpan > 0 || nrColumnsToSpan > 0) {
							cellSpanManager.addCellSpanInfo(colIndex, row, nrColumnsToSpan, nrRowsToSpan);
						}

						if (x + width >= 0 && x < clientArea.width) {
							final Point sizeOfColumn = cellSize;

							column.getCellRenderer().setBounds(x, y, width, sizeOfColumn.y);
							final int cellInHeaderDelta = columnHeadersVisible ? headerHeight - y : 0;
							if (cellInHeaderDelta > 0) {
								final Rectangle cellRect = new Rectangle(x - 1, y + cellInHeaderDelta, width + 1,
										sizeOfColumn.y + 2 - cellInHeaderDelta);
								gc.setClipping(originalClipping.intersection(cellRect));
							} else {
								final Rectangle cellRect = new Rectangle(x - 1, y - 1, width + 1, sizeOfColumn.y + 2);
								gc.setClipping(originalClipping.intersection(cellRect));
							}

							column.getCellRenderer().setRow(i + 1);

							column.getCellRenderer().setSelected(selectedItems.contains(item));
							column.getCellRenderer().setFocus(isFocusControl());
							column.getCellRenderer().setRowFocus(focusItem == item);
							column.getCellRenderer()
									.setCellFocus(cellSelectionEnabled && focusItem == item && focusColumn == column);

							column.getCellRenderer().setRowHover(hoveringItem == item);
							column.getCellRenderer().setColumnHover(hoveringColumn == column);

							column.getCellRenderer().setColumn(indexOfColumn);

							if (selectedCells.contains(new Point(indexOfColumn, row))) {
								column.getCellRenderer().setCellSelected(true);
								cellInRowSelected = true;
							} else {
								column.getCellRenderer().setCellSelected(false);
							}

							if (hoveringItem == item && hoveringColumn == column) {
								column.getCellRenderer().setHoverDetail(hoveringDetail);
							} else {
								column.getCellRenderer().setHoverDetail("");
							}

							column.getCellRenderer().paint(gc, item);

							gc.setClipping((Rectangle) null);

							// collect the insertMark position
							if (!insertMark.posFound && insertMarkItem == item
									&& (insertMarkColumn == null || insertMarkColumn == column)) {
								// y-pos
								insertMark.posY = y - 1;
								if (!insertMarkBefore) {
									insertMark.posY += item.getHeight() + 1;
								}
								// x1-pos
								insertMark.posX1 = x;
								if (column.isTree()) {
									insertMark.posX1 += Math.min(width,
											column.getCellRenderer().getTextBounds(item, false).x);
								}

								// x2-pos
								if (insertMarkColumn == null) {
									insertMark.posX2 = clientArea.x + clientArea.width;
								} else {
									insertMark.posX2 = x + width;
								}

								insertMark.posFound = true;
							}
						}
					} else {
						cellSpanManager.consumeCell(colIndex, row);
					}
					if (x > clientArea.width) {
						break;
					}
					x += column.getWidth(extraFill);
					colIndex++;

				}

				if (x < clientArea.width) {
					// insertMarkPos needs correction
					if (insertMark.posFound && insertMarkColumn == null) {
						insertMark.posX2 = x;
					}

					if (!fixed) {
						emptyCellRenderer.setSelected(selectedItems.contains(item));
						emptyCellRenderer.setFocus(isFocusControl());
						emptyCellRenderer.setRow(i + 1);
						emptyCellRenderer.setBounds(x, y, clientArea.width - x + 1, item.getHeight());
						emptyCellRenderer.setColumn(columnCount);
						emptyCellRenderer.paint(gc, item);
					}
				}

				x = 0;

				if (rowHeaderVisible) {

					if (!cellSelectionEnabled) {
						rowHeaderRenderer.setSelected(selectedItems.contains(item));
					} else {
						rowHeaderRenderer.setSelected(cellInRowSelected);
					}
					if (!columnHeadersVisible || y >= headerHeight) {
						rowHeaderRenderer.setBounds(0, y, rowHeaderWidth, item.getHeight() + 1);
						rowHeaderRenderer.paint(gc, item);
					}
					x += rowHeaderWidth;
				}

				// focus
				if (isFocusControl() && !cellSelectionEnabled) {
					if (item == focusItem) {
						if (focusRenderer != null) {
							int focusX = 0;
							if (rowHeaderVisible) {
								focusX = rowHeaderWidth;
							}
							focusRenderer.setBounds(focusX, focusY - 1, clientArea.width - focusX - 1,
									item.getHeight() + 1);
							focusRenderer.paint(gc, item);
						}
					}
				}

				y += item.getHeight() + 1;
			} else {

				if (rowHeaderVisible) {
					// row header is actually painted later
					x += rowHeaderWidth;
				}
				if (!fixed) {
					emptyCellRenderer.setBounds(x, y, clientArea.width - x, itemHeight);
					emptyCellRenderer.setFocus(false);
					emptyCellRenderer.setSelected(false);
					emptyCellRenderer.setRow(i + 1);
				}

				for (final GridColumn column : cols) {

					if (column.isVisible()) {
						final int width = column.getWidth(extraFill);
						if (x + width >= 0 && !fixed) {
							emptyCellRenderer.setBounds(x, y, width, itemHeight);
							emptyCellRenderer.setColumn(column.index);
							emptyCellRenderer.paint(gc, this);
						}
						if (x > clientArea.width) {
							break;
						}
						x += width;
					}
				}

				if (x < clientArea.width && !fixed) {
					emptyCellRenderer.setBounds(x, y, clientArea.width - x + 1, itemHeight);
					emptyCellRenderer.setColumn(columnCount);
					emptyCellRenderer.paint(gc, this);
				}

				x = 0;

				if (rowHeaderVisible) {
					if (!fixed) {
						emptyRowHeaderRenderer.setBounds(x, y, rowHeaderWidth, itemHeight + 1);
						emptyRowHeaderRenderer.paint(gc, this);
					}

					x += rowHeaderWidth;
				}

				y += itemHeight + 1;
			}

			row++;
		}
	}

	/**
	 * Returns a column reference if the x,y coordinates are over a column header
	 * (header only).
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return column reference which mouse is over, or null.
	 */
	private GridColumn overColumnHeader(final int x, final int y) {
		GridColumn col = null;

		if (y <= headerHeight && y > 0) {
			col = getColumn(new Point(x, y));
			if (col != null && col.getColumnGroup() != null) {
				if (y <= groupHeaderHeight) {
					return null;
				}
			}
		}

		return col;
	}

	/**
	 * Returns a column reference if the x,y coordinates are over a column header
	 * (header only).
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return column reference which mouse is over, or null.
	 */
	private GridColumn overColumnFooter(final int x, final int y) {
		if (y >= getClientArea().height - footerHeight) {
			return getColumn(new Point(x, y));
		}

		return null;
	}

	/**
	 * Returns a column group reference if the x,y coordinates are over a column
	 * group header (header only).
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return column group reference which mouse is over, or null.
	 */
	private GridColumnGroup overColumnGroupHeader(final int x, final int y) {
		GridColumnGroup group = null;

		if (y <= groupHeaderHeight && y > 0) {
			final GridColumn col = getColumn(new Point(x, y));
			if (col != null) {
				group = col.getColumnGroup();
			}
		}

		return group;
	}

	/**
	 * Paints the header.
	 *
	 * @param gc        gc from paint event
	 * @param extraFill the size of the control
	 */
	private void paintHeader(final GC gc, final int extraFill) {
		int x = 0;
		boolean hasFixedColumns = false;
		int firstFixed = 0;

		final int hScroll = getHScrollSelectionInPixels();
		x -= hScroll;

		if (rowHeaderVisible) {
			// paint left corner
			// topLeftRenderer.setBounds(0, y, rowHeaderWidth, headerHeight);
			// topLeftRenderer.paint(gc, null);
			x += rowHeaderWidth;
		}

		GridColumnGroup previousPaintedGroup = null;
		for (final GridColumn column : displayOrderedColumns) {
			if (x > getClientArea().width) {
				break;
			}
			if (!column.isVisible()) {
				continue;
			}
			if (!hasFixedColumns) {
				if (column.isFixed()) {
					hasFixedColumns = true;
				} else {
					firstFixed += column.getWidth();
				}
			}
			previousPaintedGroup = paintColumnHeaderWithGroup(column, x, gc, previousPaintedGroup, extraFill);
			x += column.getWidth(extraFill);
		}

		if (x < getClientArea().width) {
			emptyColumnHeaderRenderer.setBounds(x, 0, getClientArea().width - x, headerHeight);
			emptyColumnHeaderRenderer.paint(gc, null);
		}

		x = 0;

		if (rowHeaderVisible) {
			// paint left corner
			topLeftRenderer.setBounds(0, 0, rowHeaderWidth, headerHeight);
			topLeftRenderer.paint(gc, this);
			x += rowHeaderWidth;
		}

		if (draggingColumn) {

			gc.setAlpha(COLUMN_DRAG_ALPHA);

			columnBeingPushed.getHeaderRenderer().setSelected(false);

			int height = 0;
			int y;
			if (columnBeingPushed.getColumnGroup() != null) {
				height = headerHeight - groupHeaderHeight;
				y = groupHeaderHeight;
			} else {
				height = headerHeight;
				y = 0;
			}

			columnBeingPushed.getHeaderRenderer().setBounds(
					getColumnHeaderXPosition(columnBeingPushed) + currentHeaderDragX - startHeaderDragX, y,
					columnBeingPushed.getWidth(), height);
			columnBeingPushed.getHeaderRenderer().paint(gc, columnBeingPushed);
			columnBeingPushed.getHeaderRenderer().setSelected(false);

			gc.setAlpha(-1);
			gc.setAdvanced(false);
		} else if (hasFixedColumns && hScroll > firstFixed) {
			// Now paint all fixed columns without the horizontal scroll offset!
			x = 0;
			if (rowHeaderVisible) {
				x += rowHeaderWidth;
			}
			previousPaintedGroup = null;
			for (final GridColumn column : displayOrderedColumns) {
				if (x > getClientArea().width) {
					break;
				}
				if (!column.isVisible() || !column.isFixed()) {
					continue;
				}
				previousPaintedGroup = paintColumnHeaderWithGroup(column, x, gc, previousPaintedGroup, extraFill);
				x += column.getWidth();
			}
		}
	}

	int getExtraFill() {
		return getExtraFill(getSize());
	}

	int getExtraFill(final Point size) {
		int totalWidth = rowHeaderVisible ? rowHeaderWidth : 0;
		int fillColumns = 0;
		for (final GridColumn column : displayOrderedColumns) {
			if (!column.isVisible()) {
				continue;
			}
			totalWidth += column.getWidth();
			if (column.isFill()) {
				fillColumns++;
			}
		}
		if (fillColumns == 0) {
			return 0;
		}
		return Math.max(size.x - totalWidth, 0) / fillColumns;
	}

	private GridColumnGroup paintColumnHeaderWithGroup(final GridColumn column, final int x, final GC gc,
			GridColumnGroup previousPaintedGroup, final int extraFill) {
		int height;
		int y;
		final GridColumnGroup group = column.getColumnGroup();
		int width = column.getWidth(extraFill);
		if (group != null) {
			if (group != previousPaintedGroup) {
				GridColumn nextCol = null;
				if (displayOrderedColumns.indexOf(column) + 1 < displayOrderedColumns.size()) {
					nextCol = displayOrderedColumns.get(displayOrderedColumns.indexOf(column) + 1);
				}

				while (nextCol != null && nextCol.getColumnGroup() == group) {

					if (nextCol.getColumnGroup().getExpanded() && !nextCol.isDetail()
							|| !nextCol.getColumnGroup().getExpanded() && !nextCol.isSummary()) {
					} else if (nextCol.isVisible()) {
						width += nextCol.getWidth(extraFill);
					}

					if (displayOrderedColumns.indexOf(nextCol) + 1 < displayOrderedColumns.size()) {
						nextCol = displayOrderedColumns.get(displayOrderedColumns.indexOf(nextCol) + 1);
					} else {
						nextCol = null;
					}
				}

				boolean selected = true;

				for (int i = 0; i < group.getColumns().length; i++) {
					final GridColumn col = group.getColumns()[i];
					if (col.isVisible() && (column.getMoveable() || !selectedColumns.contains(col))) {
						selected = false;
						break;
					}
				}

				group.getHeaderRenderer().setSelected(selected);
				group.getHeaderRenderer().setHover(hoverColumnGroupHeader == group);
				group.getHeaderRenderer().setHoverDetail(hoveringDetail);

				group.getHeaderRenderer().setBounds(x, 0, width, groupHeaderHeight);

				group.getHeaderRenderer().paint(gc, group);

				previousPaintedGroup = group;
			}

			height = headerHeight - groupHeaderHeight;
			y = groupHeaderHeight;
		} else {
			height = headerHeight;
			y = 0;
		}

		final GridHeaderRenderer renderer = column.getHeaderRenderer();
		if (pushingColumn) {
			renderer.setHover(columnBeingPushed == column && pushingAndHovering);
		} else {
			renderer.setHover(hoveringColumnHeader == column);
		}

		renderer.setHoverDetail(hoveringDetail);
		renderer.setBounds(x, y, width, height);

		if (cellSelectionEnabled) {
			renderer.setSelected(selectedColumns.contains(column));
		}

		if (x + width >= 0) {
			renderer.paint(gc, column);
		}
		return previousPaintedGroup;
	}

	private void paintFooter(final GC gc) {
		int x = 0;
		int y = 0;

		x -= getHScrollSelectionInPixels();

		if (rowHeaderVisible) {
			// paint left corner
			// topLeftRenderer.setBounds(0, y, rowHeaderWidth, headerHeight);
			// topLeftRenderer.paint(gc, null);
			x += rowHeaderWidth;
		}

		for (final GridColumn column : displayOrderedColumns) {
			if (x > getClientArea().width) {
				break;
			}

			int height = 0;

			if (!column.isVisible()) {
				continue;
			}

			height = footerHeight;
			y = getClientArea().height - height;

			column.getFooterRenderer().setBounds(x, y, column.getWidth(), height);
			if (x + column.getWidth() >= 0) {
				column.getFooterRenderer().paint(gc, column);
			}

			x += column.getWidth();
		}

		if (x < getClientArea().width) {
			emptyColumnFooterRenderer.setBounds(x, getClientArea().height - footerHeight, getClientArea().width - x,
					footerHeight);
			emptyColumnFooterRenderer.paint(gc, null);
		}

		if (rowHeaderVisible) {
			// paint left corner
			bottomLeftRenderer.setBounds(0, getClientArea().height - footerHeight, rowHeaderWidth, footerHeight);
			bottomLeftRenderer.paint(gc, this);
			x += rowHeaderWidth;
		}
	}

	/**
	 * Manages the state of the scrollbars when new items are added or the bounds
	 * are changed.
	 */
	private void updateScrollbars() {
		final Point preferredSize = getTableSize();

		Rectangle clientArea = getClientArea();

		// First, figure out if the scrollbars should be visible and turn them
		// on right away
		// this will allow the computations further down to accommodate the
		// correct client
		// area

		// Turn the scrollbars on if necessary and do it all over again if
		// necessary. This ensures
		// that if a scrollbar is turned on/off, the other scrollbar's
		// visibility may be affected (more
		// area may have been added/removed.
		for (int doublePass = 1; doublePass <= 2; doublePass++) {

			if (preferredSize.y > clientArea.height) {
				vScroll.setVisible(true);
			} else {
				vScroll.setVisible(false);
				vScroll.setValues(0, 0, 1, 1, 1, 1);
			}
			if (preferredSize.x > clientArea.width) {
				hScroll.setVisible(true);
			} else {
				hScroll.setVisible(false);
				hScroll.setValues(0, 0, 1, 1, 1, 1);
			}

			// get the clientArea again with the now visible/invisible
			// scrollbars
			clientArea = getClientArea();
		}

		// if the scrollbar is visible set its values
		if (vScroll.getVisible()) {
			int max = currentVisibleItems;
			int thumb = 1;

			if (!hasDifferingHeights) {
				// in this case, the number of visible rows on screen is constant,
				// so use this as thumb
				thumb = (getVisibleGridHeight() + 1) / (itemHeight + 1);
			} else {
				// in this case, the number of visible rows on screen is variable,
				// so we have to use 1 as thumb and decrease max by the number of
				// rows on the last page
				if (getVisibleGridHeight() >= 1) {
					final RowRange range = getRowRange(-1, getVisibleGridHeight(), true, true);
					max -= range.rows - 1;
				}
			}

			// if possible, remember selection, if selection is too large, just
			// make it the max you can
			final int selection = Math.min(vScroll.getSelection(), max);

			vScroll.setValues(selection, 0, max, thumb, 1, thumb);
		}

		// if the scrollbar is visible set its values
		if (hScroll.getVisible()) {

			if (!columnScrolling) {
				// horizontal scrolling works pixel by pixel

				final int hiddenArea = preferredSize.x - clientArea.width + 1;

				// if possible, remember selection, if selection is too large,
				// just
				// make it the max you can
				final int selection = Math.min(hScroll.getSelection(), hiddenArea - 1);

				hScroll.setValues(selection, 0, hiddenArea + clientArea.width - 1, clientArea.width,
						HORZ_SCROLL_INCREMENT, clientArea.width);
			} else {
				// horizontal scrolling is column by column

				int hiddenArea = preferredSize.x - clientArea.width + 1;

				int max = 0;
				int i = 0;

				while (hiddenArea > 0 && i < getColumnCount()) {
					final GridColumn col = displayOrderedColumns.get(i);

					i++;

					if (col.isVisible()) {
						hiddenArea -= col.getWidth();
						max++;
					}
				}

				max++;

				// max should never be greater than the number of visible cols
				int visCols = 0;
				for (final GridColumn element : columns) {
					if (element.isVisible()) {
						visCols++;
					}
				}
				max = Math.min(visCols, max);

				// if possible, remember selection, if selection is too large,
				// just
				// make it the max you can
				final int selection = Math.min(hScroll.getSelection(), max);

				hScroll.setValues(selection, 0, max, 1, 1, 1);
			}
		}

	}

	/**
	 * Adds/removes items from the selected items list based on the
	 * selection/deselection of the given item.
	 *
	 * @param item      item being selected/unselected
	 * @param stateMask key state during selection
	 *
	 * @return selection event that needs to be fired or null
	 */
	private Event updateSelection(final GridItem item, final int stateMask) {
		if (!selectionEnabled) {
			return null;
		}

		Event selectionEvent = null;

		if (selectionType == GridSelectionType.SINGLE) {
			if (selectedItems.contains(item)) {
				// Deselect when pressing CTRL
				if ((stateMask & SWT.MOD1) == SWT.MOD1) {
					selectedItems.clear();
				}
			} else {
				selectedItems.clear();
				selectedItems.add(item);
			}
			final Rectangle clientArea = getClientArea();
			redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

			selectionEvent = new Event();
			selectionEvent.item = item;
		} else if (selectionType == GridSelectionType.MULTI) {
			boolean shift = false;
			boolean ctrl = false;

			if ((stateMask & SWT.MOD2) == SWT.MOD2) {
				shift = true;
			}

			if ((stateMask & SWT.MOD1) == SWT.MOD1) {
				ctrl = true;
			}

			if (!shift && !ctrl) {
				if (selectedItems.size() == 1 && selectedItems.contains(item)) {
					return null;
				}

				selectedItems.clear();

				selectedItems.add(item);

				final Rectangle clientArea = getClientArea();
				redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

				shiftSelectionAnchorItem = null;

				selectionEvent = new Event();
				selectionEvent.item = item;
			} else if (shift) {

				if (shiftSelectionAnchorItem == null) {
					shiftSelectionAnchorItem = focusItem;
				}

				// if (shiftSelectionAnchorItem == item)
				// {
				// return;
				// }

				boolean maintainAnchorSelection = false;

				if (!ctrl) {
					if (selectedItems.contains(shiftSelectionAnchorItem)) {
						maintainAnchorSelection = true;
					}
					selectedItems.clear();
				}

				final int anchorIndex = items.indexOf(shiftSelectionAnchorItem);
				final int itemIndex = item.getRowIndex();

				int min = 0;
				int max = 0;

				if (anchorIndex < itemIndex) {
					if (maintainAnchorSelection) {
						min = anchorIndex;
					} else {
						min = anchorIndex + 1;
					}
					max = itemIndex;
				} else {
					if (maintainAnchorSelection) {
						max = anchorIndex;
					} else {
						max = anchorIndex - 1;
					}
					min = itemIndex;
				}

				for (int i = min; i <= max; i++) {
					if (!selectedItems.contains(items.get(i)) && items.get(i).isVisible()) {
						selectedItems.add(items.get(i));
					}
				}
				final Rectangle clientArea = getClientArea();
				redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

				selectionEvent = new Event();
			} else if (ctrl) {
				if (selectedItems.contains(item)) {
					selectedItems.remove(item);
				} else {
					selectedItems.add(item);
				}
				final Rectangle clientArea = getClientArea();
				redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

				shiftSelectionAnchorItem = null;

				selectionEvent = new Event();
				selectionEvent.item = item;
			}
		}

		final Rectangle clientArea = getClientArea();
		redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

		return selectionEvent;
	}

	/**
	 * Updates cell selection.
	 *
	 * @param newCell                    newly clicked, navigated to cell.
	 * @param stateMask                  statemask during preceeding mouse or key
	 *                                   event.
	 * @param dragging                   true if the user is dragging.
	 * @param reverseDuplicateSelections true if the user is reversing selection
	 *                                   rather than adding to.
	 *
	 * @return selection event that will need to be fired or null.
	 */
	private Event updateCellSelection(final Point newCell, final int stateMask, final boolean dragging,
			final boolean reverseDuplicateSelections) {
		final Vector<Point> v = new Vector<>();
		v.add(newCell);
		return updateCellSelection(v, stateMask, dragging, reverseDuplicateSelections);
	}

	/**
	 * Updates cell selection.
	 *
	 * @param newCell                    newly clicked, navigated to cells.
	 * @param stateMask                  statemask during preceeding mouse or key
	 *                                   event.
	 * @param dragging                   true if the user is dragging.
	 * @param reverseDuplicateSelections true if the user is reversing selection
	 *                                   rather than adding to.
	 *
	 * @return selection event that will need to be fired or null.
	 */
	private Event updateCellSelection(final Vector<Point> newCells, final int stateMask, final boolean dragging,
			final boolean reverseDuplicateSelections) {
		boolean shift = false;
		boolean ctrl = false;

		if ((stateMask & SWT.MOD2) == SWT.MOD2) {
			shift = true;
		} else {
			shiftSelectionAnchorColumn = null;
			shiftSelectionAnchorItem = null;
		}

		if ((stateMask & SWT.MOD1) == SWT.MOD1) {
			ctrl = true;
		}

		if (!shift && !ctrl) {
			if (newCells.equals(selectedCells)) {
				return null;
			}

			selectedCells.clear();
			for (final Point newCell : newCells) {
				addToCellSelection(newCell);
			}

		} else if (shift) {

			final Point newCell = newCells.get(0); // shift selection should only occur with one
			// cell, ignoring others

			if (focusColumn == null || focusItem == null) {
				return null;
			}

			shiftSelectionAnchorColumn = getColumn(newCell.x);
			shiftSelectionAnchorItem = getItem(newCell.y);

			if (ctrl) {
				selectedCells.clear();
				addCellstThatDoNotAlreadyExist(selectedCells, selectedCellsBeforeRangeSelect);
			} else {
				selectedCells.clear();
			}

			GridColumn currentColumn = focusColumn;
			GridItem currentItem = focusItem;

			GridColumn endColumn = getColumn(newCell.x);
			GridItem endItem = getItem(newCell.y);

			final Point newRange = getSelectionRange(currentItem, currentColumn, endItem, endColumn);

			currentColumn = getColumn(newRange.x);
			endColumn = getColumn(newRange.y);

			final GridColumn startCol = currentColumn;

			if (currentItem.getRowIndex() > endItem.getRowIndex()) {
				final GridItem temp = currentItem;
				currentItem = endItem;
				endItem = temp;
			}

			boolean firstLoop = true;

			do {
				if (!firstLoop) {
					currentItem = getNextVisibleItem(currentItem);
				}

				firstLoop = false;

				boolean firstLoop2 = true;

				currentColumn = startCol;

				do {
					if (!firstLoop2) {
						final int index = displayOrderedColumns.indexOf(currentColumn) + 1;

						if (index < displayOrderedColumns.size()) {
							currentColumn = getVisibleColumn_DegradeRight(currentItem,
									displayOrderedColumns.get(index));
						} else {
							currentColumn = null;
						}

						if (currentColumn != null) {
							if (displayOrderedColumns.indexOf(currentColumn) > displayOrderedColumns
									.indexOf(endColumn)) {
								currentColumn = null;
							}
						}
					}

					firstLoop2 = false;

					if (currentColumn != null) {
						final Point cell = new Point(currentColumn.index, currentItem.getRowIndex());
						addToCellSelection(cell);
					}
				} while (currentColumn != endColumn && currentColumn != null);
			} while (currentItem != endItem);
		} else if (ctrl) {
			boolean reverse = reverseDuplicateSelections;
			if (!selectedCells.containsAll(newCells)) {
				reverse = false;
			}

			if (dragging) {
				selectedCells.clear();
				addCellstThatDoNotAlreadyExist(selectedCells, selectedCellsBeforeRangeSelect);
			}

			if (reverse) {
				selectedCells.removeAll(newCells);
			} else {
				for (final Point newCell : newCells) {
					addToCellSelection(newCell);
				}
			}
		}

		updateColumnSelection();

		final Event e = new Event();
		if (dragging) {
			e.detail = SWT.DRAG;
			followupCellSelectionEventOwed = true;
		}

		final Rectangle clientArea = getClientArea();
		redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

		return e;
	}

	/**
	 * Adds Point objects from the itemsToBeAdded list that do not currently exist
	 * in the sourceList.
	 *
	 * @param sourceList     the source list to add items to
	 * @param itemsToBeAdded the list of items to be added
	 */
	private void addCellstThatDoNotAlreadyExist(final List<Point> sourceList, final List<Point> itemsToBeAdded) {
		if (itemsToBeAdded.size() > 0) {
			// add all the cells from the itemsToBeAdded list that don't already exist in
			// the sourceList
			for (final Point cell : itemsToBeAdded) {
				if (!sourceList.contains(cell)) {
					sourceList.add(cell);
				}

			}
		}
	}

	private void addToCellSelection(final Point newCell) {
		if (newCell.x < 0 || newCell.x >= columns.size()) {
			return;
		}

		if (newCell.y < 0 || newCell.y >= items.size()) {
			return;
		}

		if (getColumn(newCell.x).getCellSelectionEnabled()) {
			boolean found = false;
			for (final Point p : selectedCells) {
				if (newCell.equals(p)) {
					found = true;
					break;
				}
			}

			if (!found) {
				if (selectionType == GridSelectionType.SINGLE && selectedCells.size() > 0) {
					return;
				}
				selectedCells.add(newCell);
			}
		}
	}

	void updateColumnSelection() {
		// Update the list of which columns have all their cells selected
		selectedColumns.clear();

		for (final Point cell : selectedCells) {
			selectedColumns.add(getColumn(cell.x));
		}
	}

	/**
	 * Initialize all listeners.
	 */
	private void initListeners() {
		disposeListener = this::onDispose;
		addListener(SWT.Dispose, disposeListener);

		addPaintListener(this::onPaint);

		addListener(SWT.Resize, e -> onResize());

		if (getVerticalBar() != null) {
			getVerticalBar().addListener(SWT.Selection, e -> onScrollSelection());
		}

		if (getHorizontalBar() != null) {
			getHorizontalBar().addListener(SWT.Selection, e -> onScrollSelection());
		}

		defaultKeyListener = this::onKeyDown;
		addListener(SWT.KeyDown, defaultKeyListener);

		addTraverseListener(e -> {
			if (moveOnTab) {
				e.doit = false;
				if (selectedItems.isEmpty()) {
					select(0);
					return;
				}
				if (selectedItems.size() == 1) {
					final int index = getSelectionIndex();
					if (SWT.TRAVERSE_TAB_NEXT == e.detail) {
						select(index == getItemCount() - 1 ? 0 : index + 1);
					} else if (SWT.TRAVERSE_TAB_PREVIOUS == e.detail) {
						select(index == 0 ? getItemCount() - 1 : index - 1);
					}
					return;
				}
				return;
			} else {
				e.doit = true;
			}
		});

		addListener(SWT.MouseDoubleClick, this::onMouseDoubleClick);
		addListener(SWT.MouseDown, this::onMouseDown);
		addListener(SWT.MouseUp, this::onMouseUp);

		addMouseMoveListener(this::onMouseMove);

		addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseEnter(final MouseEvent e) {
			}

			@Override
			public void mouseExit(final MouseEvent e) {
				onMouseExit(e);
			}

			@Override
			public void mouseHover(final MouseEvent e) {
			}
		});

		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent e) {
				onFocusIn();
				redraw();
			}

			@Override
			public void focusLost(final FocusEvent e) {
				redraw();
			}
		});

		// Special code to reflect mouse wheel events if using an external
		// scroller
		addListener(SWT.MouseWheel, this::onMouseWheel);
	}

	/**
	 * Disable default key listener
	 */
	public void disableDefaultKeyListener() {
		if (defaultKeyListenerEnabled) {
			removeListener(SWT.KeyDown, defaultKeyListener);
		}
		defaultKeyListenerEnabled = false;
	}

	/**
	 * Enable default key listener
	 */
	public void enableDefaultKeyListener() {
		if (!defaultKeyListenerEnabled) {
			addListener(SWT.KeyDown, defaultKeyListener);
		}
		defaultKeyListenerEnabled = true;
	}

	private void onFocusIn() {
		if (!items.isEmpty() && focusItem == null) {
			focusItem = items.get(0);
		}
	}

	private void onDispose(final Event event) {
		// We only want to dispose of our items and such *after* anybody else who may
		// have been
		// listening to the dispose has had a chance to do whatever.
		removeListener(SWT.Dispose, disposeListener);
		notifyListeners(SWT.Dispose, event);
		event.type = SWT.None;

		disposing = true;

		cellHeaderSelectionBackground.dispose();

		for (final GridItem item : items) {
			item.dispose();
		}

		for (final GridColumnGroup columnGroup : columnGroups) {
			columnGroup.dispose();
		}

		for (final GridColumn col : columns) {
			col.dispose();
		}
	}

	/**
	 * Mouse wheel event handler.
	 *
	 * @param e event
	 */
	private void onMouseWheel(final Event e) {
		if (vScroll.getVisible()) {
			vScroll.handleMouseWheel(e);
			if (getVerticalBar() == null) {
				e.doit = false;
			}
		} else if (hScroll.getVisible()) {
			hScroll.handleMouseWheel(e);
			if (getHorizontalBar() == null) {
				e.doit = false;
			}
		}
	}

	/**
	 * Mouse down event handler.
	 *
	 * @param e event
	 */
	private void onMouseDown(final Event e) {
		// for some reason, SWT prefers the children to get focus if
		// there are any children
		// the setFocus method on Composite will not set focus to the
		// Composite if one of its
		// children can get focus instead. This only affects the table
		// when an editor is open
		// and therefore the table has a child. The solution is to
		// forceFocus()
		if ((getStyle() & SWT.NO_FOCUS) != SWT.NO_FOCUS) {
			forceFocus();
		}

		hideToolTip();

		// if populated will be fired at end of method.
		Event selectionEvent = null;

		cellSelectedOnLastMouseDown = false;
		cellRowSelectedOnLastMouseDown = false;
		cellColumnSelectedOnLastMouseDown = false;

		if (hoveringOnColumnResizer) {
			if (e.button == 1) {
				resizingColumn = true;
				resizingStartX = e.x;
				resizingColumnStartWidth = columnBeingResized.getWidth();
			}
			return;
		}
		if (rowsResizeable && hoveringOnRowResizer) {
			if (e.button == 1) {
				resizingRow = true;
				resizingStartY = e.y;
				resizingRowStartHeight = rowBeingResized.getHeight();
			}
			return;
		}

		if (e.button == 1 && handleColumnHeaderPush(e.x, e.y)) {
			return;
		}

		if (e.button == 1 && handleColumnGroupHeaderClick(e.x, e.y)) {
			return;
		}

		if (e.button == 1 && handleColumnFooterPush(e.x, e.y)) {
			return;
		}

		final GridItem item = getItem(new Point(e.x, e.y));

		if (e.button == 1 && item != null && handleCellClick(item, e.x, e.y)) {
			return;
		}

		if (isListening(SWT.DragDetect)) {
			if (cellSelectionEnabled && hoveringOnSelectionDragArea
					|| !cellSelectionEnabled && item != null && selectedItems.contains(item)) {
				if (dragDetect(e)) {
					return;
				}
			}
		}

		if (item != null) {
			if (cellSelectionEnabled) {
				final GridColumn col = getColumn(new Point(e.x, e.y));
				boolean isSelectedCell = false;
				if (col != null) {
					isSelectedCell = selectedCells.contains(new Point(col.index, item.getRowIndex()));
				}

				if (e.button == 1 || e.button == 3 && col != null && !isSelectedCell) {
					if (col != null) {
						selectionEvent = updateCellSelection(new Point(col.index, item.getRowIndex()), e.stateMask,
								false, true);
						cellSelectedOnLastMouseDown = getCellSelectionCount() > 0;

						if (e.stateMask != SWT.MOD2) {
							focusColumn = col;
							focusItem = item;
						}
						// showColumn(col);
						showItem(item);
						redraw();
					} else if (rowHeaderVisible) {
						if (e.x <= rowHeaderWidth) {

							final boolean shift = (e.stateMask & SWT.MOD2) != 0;
							boolean ctrl = false;
							if (!shift) {
								ctrl = (e.stateMask & SWT.MOD1) != 0;
							}

							final Vector<Point> cells = new Vector<>();

							if (shift) {
								getCells(item, focusItem, cells);
							} else {
								getCells(item, cells);
							}

							int newStateMask = SWT.NONE;
							if (ctrl) {
								newStateMask = SWT.MOD1;
							}

							selectionEvent = updateCellSelection(cells, newStateMask, shift, ctrl);
							cellRowSelectedOnLastMouseDown = getCellSelectionCount() > 0;

							if (!shift) {
								// set focus back to the first visible column
								focusColumn = getColumn(new Point(rowHeaderWidth + 1, e.y));

								focusItem = item;
							}
							showItem(item);
							redraw();
						}
					}
					intendedFocusColumn = focusColumn;
				}
			} else {
				if (e.button == 2 || e.button > 3) {
					return;
				}

				if (e.button == 3 && selectionType == GridSelectionType.MULTI) {
					if ((e.stateMask & SWT.MOD2) == SWT.MOD2) {
						return;
					}

					if ((e.stateMask & SWT.MOD1) == SWT.MOD1) {
						return;
					}

					if (selectedItems.contains(item)) {
						return;
					}
				}
				selectionEvent = updateSelection(item, e.stateMask);

				focusItem = item;
				showItem(item);
				redraw();
			}
		} else if (e.button == 1 && rowHeaderVisible && e.x <= rowHeaderWidth && e.y < headerHeight) {
			// Nothing to select
			if (items.size() == 0) {
				return;
			}
			if (cellSelectionEnabled) {
				// click on the top left corner means select everything
				selectionEvent = selectAllCellsInternal();

				focusColumn = getColumn(new Point(rowHeaderWidth + 1, 1));
			} else {
				// click on the top left corner means select everything
				selectionEvent = selectAllRowsInternal();
			}
			focusItem = getItem(getTopIndex());
		} else if (cellSelectionEnabled && e.button == 1 && columnHeadersVisible && e.y <= headerHeight) {
			// column cell selection
			final GridColumn col = getColumn(new Point(e.x, e.y));

			if (col == null) {
				return;
			}

			if (getItemCount() == 0) {
				return;
			}

			final Vector<Point> cells = new Vector<>();

			final GridColumnGroup group = col.getColumnGroup();
			if (group != null && e.y < groupHeaderHeight) {
				getCells(group, cells);
			} else {
				getCells(col, cells);
			}

			selectionEvent = updateCellSelection(cells, e.stateMask, false, true);
			cellColumnSelectedOnLastMouseDown = getCellSelectionCount() > 0;

			GridItem newFocusItem = getItem(0);

			while (newFocusItem != null && getSpanningColumn(newFocusItem, col) != null) {
				newFocusItem = getNextVisibleItem(newFocusItem);
			}

			if (newFocusItem != null) {
				focusColumn = col;
				focusItem = newFocusItem;
			}

			showColumn(col);
			redraw();
		}

		if (selectionEvent != null) {
			selectionEvent.stateMask = e.stateMask;
			selectionEvent.button = e.button;
			selectionEvent.item = item;
			selectionEvent.x = e.x;
			selectionEvent.y = e.y;
			notifyListeners(SWT.Selection, selectionEvent);

			if (!cellSelectionEnabled) {
				if (isListening(SWT.DragDetect)) {
					dragDetect(e);
				}
			}
		}

	}

	/**
	 * Mouse double click event handler.
	 *
	 * @param e event
	 */
	private void onMouseDoubleClick(final Event e) {
		if (e.button != 1) {
			return;
		}

		if (hoveringOnColumnResizer) {
			columnBeingResized.pack();
			columnBeingResized.fireResized();
			for (int index = displayOrderedColumns.indexOf(columnBeingResized) + 1; index < displayOrderedColumns
					.size(); index++) {
				final GridColumn col = displayOrderedColumns.get(index);
				if (col.isVisible()) {
					col.fireMoved();
				}
			}
			resizingColumn = false;
			handleHoverOnColumnResizer(e.x, e.y);
			e.doit = false;
			return;
		} else if (rowsResizeable && hoveringOnRowResizer) {
			final List<GridItem> sel = Arrays.asList(getSelection());
			if (sel.contains(rowBeingResized)) {
				// the user double-clicked a row resizer of a selected row
				// so update all selected rows
				for (final GridItem element : sel) {
					element.pack();
				}
				redraw();
			} else {
				// otherwise only update the row the user double-clicked
				rowBeingResized.pack();
			}

			resizingRow = false;
			handleHoverOnRowResizer(e.x, e.y);
			e.doit = false;
			return;
		}

		if (e.y < headerHeight && columnHeadersVisible) {
			e.doit = false;
			return;
		}

		final GridItem item = getItem(new Point(e.x, e.y));
		if (item != null) {
			if (isListening(SWT.DefaultSelection)) {
				final Event newEvent = new Event();
				newEvent.item = item;

				notifyListeners(SWT.DefaultSelection, newEvent);
			} else if (item.getItemCount() > 0) {
				item.setExpanded(!item.isExpanded());

				if (item.isExpanded()) {
					item.fireEvent(SWT.Expand);
				} else {
					item.fireEvent(SWT.Collapse);
				}
			}
		}
	}

	/**
	 * Mouse up handler.
	 *
	 * @param e event
	 */
	private void onMouseUp(final Event e) {
		cellSelectedOnLastMouseDown = false;

		if (resizingColumn) {
			resizingColumn = false;
			handleHoverOnColumnResizer(e.x, e.y); // resets cursor if
			// necessary
			return;
		}
		if (resizingRow) {
			resizingRow = false;
			handleHoverOnRowResizer(e.x, e.y); // resets cursor if
			// necessary
			return;
		}

		if (pushingColumn) {
			pushingColumn = false;
			columnBeingPushed.getHeaderRenderer().setMouseDown(false);
			columnBeingPushed.getHeaderRenderer().setHover(false);
			redraw();
			if (pushingAndHovering) {
				columnBeingPushed.fireListeners();
			}
			setCapture(false);
			return;
		}

		if (draggingColumn) {
			handleColumnDrop();
			return;
		}

		if (cellDragSelectionOccuring || cellRowDragSelectionOccuring || cellColumnDragSelectionOccuring) {
			cellDragSelectionOccuring = false;
			cellRowDragSelectionOccuring = false;
			cellColumnDragSelectionOccuring = false;
			setCursor(null);

			if (followupCellSelectionEventOwed) {
				final Event se = new Event();
				se.button = e.button;
				se.item = getItem(new Point(e.x, e.y));
				se.stateMask = e.stateMask;
				se.x = e.x;
				se.y = e.y;

				notifyListeners(SWT.Selection, se);
				followupCellSelectionEventOwed = false;
			}
		}
	}

	/**
	 * Mouse move event handler.
	 *
	 * @param e event
	 */
	private void onMouseMove(final MouseEvent e) {
		// check to see if the mouse is outside the grid
		// this should only happen when the mouse is captured for inplace
		// tooltips - see bug 203364
		if (inplaceTooltipCapture && (e.x < 0 || e.y < 0 || e.x >= getBounds().width || e.y >= getBounds().height)) {
			setCapture(false);
			inplaceTooltipCapture = false;
			return; // a mouseexit event should occur immediately
		}

		// if populated will be fired at end of method.
		Event selectionEvent = null;

		if ((e.stateMask & SWT.BUTTON1) == 0) {
			handleHovering(e.x, e.y);
		} else {
			if (draggingColumn) {
				handleColumnDragging(e.x);
				return;
			}

			if (resizingColumn) {
				handleColumnResizerDragging(e.x);
				return;
			}
			if (resizingRow) {
				handleRowResizerDragging(e.y);
				return;
			}
			if (pushingColumn) {
				handleColumnHeaderHoverWhilePushing(e.x, e.y);
				return;
			}
			if (cellSelectionEnabled) {
				if (cellDragSelectionEnabled && !cellDragSelectionOccuring && cellSelectedOnLastMouseDown) {
					cellDragSelectionOccuring = true;
					// XXX: make this user definable
					setCursor(getDisplay().getSystemCursor(SWT.CURSOR_CROSS));
					cellDragCTRL = (e.stateMask & SWT.MOD1) != 0;
					if (cellDragCTRL) {
						selectedCellsBeforeRangeSelect.clear();
						selectedCellsBeforeRangeSelect.addAll(selectedCells);
					}
				}
				if (!cellRowDragSelectionOccuring && cellRowSelectedOnLastMouseDown) {
					cellRowDragSelectionOccuring = true;
					setCursor(getDisplay().getSystemCursor(SWT.CURSOR_CROSS));
					cellDragCTRL = (e.stateMask & SWT.MOD1) != 0;
					if (cellDragCTRL) {
						selectedCellsBeforeRangeSelect.clear();
						selectedCellsBeforeRangeSelect.addAll(selectedCells);
					}
				}

				if (!cellColumnDragSelectionOccuring && cellColumnSelectedOnLastMouseDown) {
					cellColumnDragSelectionOccuring = true;
					setCursor(getDisplay().getSystemCursor(SWT.CURSOR_CROSS));
					cellDragCTRL = (e.stateMask & SWT.MOD1) != 0;
					if (cellDragCTRL) {
						selectedCellsBeforeRangeSelect.clear();
						selectedCellsBeforeRangeSelect.addAll(selectedCells);
					}
				}

				final int ctrlFlag = cellDragCTRL ? SWT.MOD1 : SWT.NONE;

				if (cellDragSelectionOccuring && handleCellHover(e.x, e.y)) {
					GridColumn intentColumn = hoveringColumn;
					GridItem intentItem = hoveringItem;

					if (hoveringItem == null) {
						if (e.y > headerHeight) {
							// then we must be hovering way to the bottom
							intentItem = getPreviousVisibleItem(null);
						} else {
							intentItem = items.get(0);
						}
					}

					if (hoveringColumn == null) {
						if (e.x > rowHeaderWidth) {
							// then we must be hovering way to the right
							intentColumn = getVisibleColumn_DegradeLeft(intentItem,
									displayOrderedColumns.get(displayOrderedColumns.size() - 1));
						} else {
							GridColumn firstCol = displayOrderedColumns.get(0);
							if (!firstCol.isVisible()) {
								firstCol = getNextVisibleColumn(firstCol);
							}
							intentColumn = firstCol;
						}
					}

					showColumn(intentColumn);
					showItem(intentItem);
					selectionEvent = updateCellSelection(new Point(intentColumn.index, intentItem.getRowIndex()),
							ctrlFlag | SWT.MOD2, true, false);
				}
				if (cellRowDragSelectionOccuring && handleCellHover(e.x, e.y)) {
					GridItem intentItem = hoveringItem;

					if (hoveringItem == null) {
						if (e.y > headerHeight) {
							// then we must be hovering way to the bottom
							intentItem = getPreviousVisibleItem(null);
						} else {
							if (getTopIndex() > 0) {
								intentItem = getPreviousVisibleItem(items.get(getTopIndex()));
							} else {
								intentItem = items.get(0);
							}
						}
					}

					final Vector<Point> cells = new Vector<>();

					getCells(intentItem, focusItem, cells);

					showItem(intentItem);
					selectionEvent = updateCellSelection(cells, ctrlFlag, true, false);
				}
				if (cellColumnDragSelectionOccuring && handleCellHover(e.x, e.y)) {
					final GridColumn intentCol = hoveringColumn;

					if (intentCol == null) {
						if (e.y < rowHeaderWidth) {
							// TODO: get the first col to the left
						} else {
							// TODO: get the first col to the right
						}
					}

					if (intentCol == null) {
						return; // temporary
					}

					GridColumn iterCol = intentCol;

					final Vector<Point> newSelected = new Vector<>();

					final boolean decreasing = displayOrderedColumns.indexOf(iterCol) > displayOrderedColumns
							.indexOf(focusColumn);

					do {
						getCells(iterCol, newSelected);

						if (iterCol == focusColumn) {
							break;
						}

						if (decreasing) {
							iterCol = getPreviousVisibleColumn(iterCol);
						} else {
							iterCol = getNextVisibleColumn(iterCol);
						}

					} while (true);

					selectionEvent = updateCellSelection(newSelected, ctrlFlag, true, false);
				}

			}
		}

		if (selectionEvent != null) {
			selectionEvent.stateMask = e.stateMask;
			selectionEvent.button = e.button;
			selectionEvent.item = getItem(new Point(e.x, e.y));
			selectionEvent.x = e.x;
			selectionEvent.y = e.y;
			notifyListeners(SWT.Selection, selectionEvent);
		}
	}

	/**
	 * Handles the assignment of the correct values to the hover* field variables
	 * that let the painting code now what to paint as hovered.
	 *
	 * @param x mouse x coordinate
	 * @param y mouse y coordinate
	 */
	private void handleHovering(final int x, final int y) {
		// TODO: need to clean up and refactor hover code
		handleCellHover(x, y);

		// Is this Grid a DragSource ??
		if (cellSelectionEnabled && getData("DragSource") != null) {
			if (handleHoverOnSelectionDragArea(x, y)) {
				return;
			}
		}

		if (columnHeadersVisible) {
			if (handleHoverOnColumnResizer(x, y)) {
				// if (hoveringItem != null || !hoveringDetail.equals("") || hoveringColumn !=
				// null
				// || hoveringColumnHeader != null || hoverColumnGroupHeader != null)
				// {
				// hoveringItem = null;
				// hoveringDetail = "";
				// hoveringColumn = null;
				// hoveringColumnHeader = null;
				// hoverColumnGroupHeader = null;
				//
				// Rectangle clientArea = getClientArea();
				// redraw(clientArea.x,clientArea.y,clientArea.width,clientArea.height,false);
				// }
				return;
			}
		}
		if (rowsResizeable && rowHeaderVisible) {
			if (handleHoverOnRowResizer(x, y)) {
				return;
			}
		}

		// handleCellHover(x, y);
	}

	/**
	 * Refreshes the hover* variables according to the mouse location and current
	 * state of the table. This is useful is some method call, caused the state of
	 * the table to change and therefore the hover effects may have become out of
	 * date.
	 */
	protected void refreshHoverState() {
		final Point p = getDisplay().map(null, this, getDisplay().getCursorLocation());
		handleHovering(p.x, p.y);
	}

	/**
	 * Mouse exit event handler.
	 *
	 * @param e event
	 */
	private void onMouseExit(final MouseEvent e) {
		hoveringItem = null;
		hoveringDetail = "";
		hoveringColumn = null;
		hoveringColumnHeader = null;
		hoverColumnGroupHeader = null;
		hoveringOverText = false;
		hideToolTip();
		redraw();
	}

	/**
	 * Key down event handler.
	 *
	 * @param e event
	 */
	protected void onKeyDown(final Event e) {
		if (focusColumn == null || focusColumn.isDisposed()) {
			if (columns.size() == 0) {
				return;
			}

			focusColumn = getColumn(0);
			intendedFocusColumn = focusColumn;
		}

		if (e.character == '\r' && focusItem != null) {
			final Event newEvent = new Event();
			newEvent.item = focusItem;

			notifyListeners(SWT.DefaultSelection, newEvent);
			return;
		}

		int attemptExpandCollapse = 0;
		if ((e.character == '-' || !cellSelectionEnabled && e.keyCode == SWT.ARROW_LEFT) && focusItem != null
				&& focusItem.isExpanded()) {
			attemptExpandCollapse = SWT.Collapse;
		} else if ((e.character == '+' || !cellSelectionEnabled && e.keyCode == SWT.ARROW_RIGHT) && focusItem != null
				&& !focusItem.isExpanded()) {
			attemptExpandCollapse = SWT.Expand;
		}

		if (attemptExpandCollapse != 0 && focusItem != null && focusItem.hasChildren()) {
			int performExpandCollapse = 0;

			if (cellSelectionEnabled && focusColumn != null && focusColumn.isTree()) {
				performExpandCollapse = attemptExpandCollapse;
			} else if (!cellSelectionEnabled) {
				performExpandCollapse = attemptExpandCollapse;
			}

			if (performExpandCollapse == SWT.Expand) {
				focusItem.setExpanded(true);
				focusItem.fireEvent(SWT.Expand);
				return;
			}
			if (performExpandCollapse == SWT.Collapse) {
				focusItem.setExpanded(false);
				focusItem.fireEvent(SWT.Collapse);
				return;
			}
		}

		if (e.character == ' ') {
			handleSpaceBarDown(e);
		}

		GridItem newSelection = null;
		GridColumn newColumnFocus = null;

		// These two variables are used because the key navigation when the shift key is
		// down is
		// based, not off the focus item/column, but rather off the implied focus (i.e.
		// where the
		// keyboard has extended focus to).
		GridItem impliedFocusItem = focusItem == null || focusItem.isDisposed() ? null : focusItem;
		GridColumn impliedFocusColumn = focusColumn.isDisposed() ? null : focusColumn;

		if (cellSelectionEnabled && e.stateMask == SWT.MOD2) {
			if (shiftSelectionAnchorColumn != null) {
				if (shiftSelectionAnchorItem == null || shiftSelectionAnchorItem.isDisposed()) {
					impliedFocusItem = focusItem;
				} else {
					impliedFocusItem = shiftSelectionAnchorItem;
				}
				impliedFocusColumn = shiftSelectionAnchorColumn.isDisposed() ? null : shiftSelectionAnchorColumn;
			}
		}

		switch (e.keyCode) {
		case SWT.ARROW_RIGHT:
			if (cellSelectionEnabled) {
				if (impliedFocusItem != null && impliedFocusColumn != null) {
					newSelection = impliedFocusItem;

					int index = displayOrderedColumns.indexOf(impliedFocusColumn);

					int jumpAhead = impliedFocusItem.getColumnSpan(impliedFocusColumn.index);

					jumpAhead++;

					while (jumpAhead > 0) {
						index++;
						if (index < displayOrderedColumns.size()) {
							if (displayOrderedColumns.get(index).isVisible()) {
								jumpAhead--;
							}
						} else {
							break;
						}
					}

					if (index < displayOrderedColumns.size()) {
						newColumnFocus = displayOrderedColumns.get(index);
					} else {
						newColumnFocus = impliedFocusColumn;
					}
				}
				intendedFocusColumn = newColumnFocus;
			} else {
				if (impliedFocusItem != null && impliedFocusItem.hasChildren()) {
					newSelection = impliedFocusItem.getItem(0);
				}
			}
			break;
		case SWT.ARROW_LEFT:
			if (cellSelectionEnabled) {
				if (impliedFocusItem != null && impliedFocusColumn != null) {
					newSelection = impliedFocusItem;

					final int index = displayOrderedColumns.indexOf(impliedFocusColumn);

					if (index != 0) {
						newColumnFocus = displayOrderedColumns.get(index - 1);

						newColumnFocus = getVisibleColumn_DegradeLeft(impliedFocusItem, newColumnFocus);

						if (newColumnFocus == null) {
							newColumnFocus = impliedFocusColumn;
						}

					} else {
						newColumnFocus = impliedFocusColumn;
					}
				}
				intendedFocusColumn = newColumnFocus;
			} else {
				if (impliedFocusItem != null && impliedFocusItem.getParentItem() != null) {
					newSelection = impliedFocusItem.getParentItem();
				}
			}
			break;
		case SWT.ARROW_UP:
			if (impliedFocusItem != null) {
				newSelection = getPreviousVisibleItem(impliedFocusItem);
			}

			if (impliedFocusColumn != null) {
				if (newSelection != null) {
					newColumnFocus = getVisibleColumn_DegradeLeft(newSelection, intendedFocusColumn);
				} else {
					newColumnFocus = impliedFocusColumn;
				}
			}

			break;
		case SWT.ARROW_DOWN:
			if (impliedFocusItem != null) {
				newSelection = getNextVisibleItem(impliedFocusItem);
			} else {
				if (items.size() > 0) {
					newSelection = items.get(0);
				}
			}

			if (impliedFocusColumn != null) {
				if (newSelection != null && intendedFocusColumn != null) {
					newColumnFocus = getVisibleColumn_DegradeLeft(newSelection, intendedFocusColumn);
				} else {
					newColumnFocus = impliedFocusColumn;
				}
			}
			break;
		case SWT.HOME:
			if (!cellSelectionEnabled) {
				if (items.size() > 0) {
					newSelection = items.get(0);
				}
			} else {
				if (e.stateMask == SWT.MOD1 && items.size() > 0) {
					impliedFocusItem = items.get(0);
				}
				newSelection = impliedFocusItem;
				newColumnFocus = getVisibleColumn_DegradeRight(newSelection, displayOrderedColumns.get(0));
				intendedFocusColumn = newColumnFocus;
			}

			break;
		case SWT.END:
			if (!cellSelectionEnabled) {
				if (items.size() > 0) {
					newSelection = getPreviousVisibleItem(null);
				}
			} else {
				newSelection = impliedFocusItem;
				newColumnFocus = getVisibleColumn_DegradeLeft(newSelection,
						displayOrderedColumns.get(displayOrderedColumns.size() - 1));
			}

			break;
		case SWT.PAGE_UP:
			final int topIndex = getTopIndex();

			newSelection = items.get(topIndex);

			if (focusItem == newSelection) {
				final RowRange range = getRowRange(getTopIndex(), getVisibleGridHeight(), false, true);
				newSelection = items.get(range.startIndex);
			}

			if (impliedFocusColumn != null) {
				if (newSelection != null && intendedFocusColumn != null) {
					newColumnFocus = getVisibleColumn_DegradeLeft(newSelection, intendedFocusColumn);
				} else {
					newColumnFocus = impliedFocusColumn;
				}
			}
			break;
		case SWT.PAGE_DOWN:
			final int bottomIndex = getBottomIndex();

			newSelection = items.get(bottomIndex);

			if (!isShown(newSelection)) {
				// the item at bottom index is not shown completely
				final GridItem tmpItem = getPreviousVisibleItem(newSelection);
				if (tmpItem != null) {
					newSelection = tmpItem;
				}
			}

			if (focusItem == newSelection) {
				final RowRange range = getRowRange(getBottomIndex(), getVisibleGridHeight(), true, false);
				newSelection = items.get(range.endIndex);
			}

			if (impliedFocusColumn != null) {
				if (newSelection != null && intendedFocusColumn != null) {
					newColumnFocus = getVisibleColumn_DegradeLeft(newSelection, intendedFocusColumn);
				} else {
					newColumnFocus = impliedFocusColumn;
				}
			}
			break;
		default:
			break;
		}

		if (newSelection == null) {
			return;
		}

		if (cellSelectionEnabled) {
			if (e.stateMask != SWT.MOD2) {
				focusColumn = newColumnFocus;
			}
			showColumn(newColumnFocus);

			if (e.stateMask != SWT.MOD2) {
				focusItem = newSelection;
			}
			showItem(newSelection);

			if (e.stateMask != SWT.MOD1 || isMod1Home(e)) {
				final int stateMask = e.stateMask == SWT.MOD1 ? SWT.NONE : e.stateMask;
				final Event selEvent = updateCellSelection(new Point(newColumnFocus.index, newSelection.getRowIndex()),
						stateMask, false, false);
				if (selEvent != null) {
					selEvent.stateMask = stateMask;
					selEvent.character = e.character;
					selEvent.keyCode = e.keyCode;
					notifyListeners(SWT.Selection, selEvent);
				}
			}

			redraw();
		} else {
			Event selectionEvent = null;
			if (selectionType == GridSelectionType.SINGLE || e.stateMask != SWT.MOD1) {
				selectionEvent = updateSelection(newSelection, e.stateMask);
				if (selectionEvent != null) {
					selectionEvent.stateMask = e.stateMask;
					selectionEvent.character = e.character;
					selectionEvent.keyCode = e.keyCode;
				}
			}

			focusItem = newSelection;
			showItem(newSelection);
			redraw();

			if (selectionEvent != null) {
				notifyListeners(SWT.Selection, selectionEvent);
			}
		}
	}

	private boolean isMod1Home(final Event e) {
		return e.stateMask == SWT.MOD1 && e.keyCode == SWT.HOME;
	}

	private void handleSpaceBarDown(final Event event) {
		if (focusItem == null) {
			return;
		}

		if (selectionEnabled && !cellSelectionEnabled && !selectedItems.contains(focusItem)) {
			selectedItems.add(focusItem);
			redraw();
			final Event e = new Event();
			e.item = focusItem;
			e.stateMask = event.stateMask;
			e.character = event.character;
			e.keyCode = event.keyCode;
			notifyListeners(SWT.Selection, e);
		}

		if (!cellSelectionEnabled) {
			boolean checkFirstCol = false;
			boolean first = true;

			for (final GridColumn col : columns) {

				if (first) {
					if (!col.isCheck()) {
						break;
					}

					first = false;
					checkFirstCol = true;
				} else {
					if (col.isCheck()) {
						checkFirstCol = false;
						break;
					}
				}
			}

			if (checkFirstCol) {
				focusItem.setChecked(!focusItem.getChecked());
				redraw();
				focusItem.fireCheckEvent(0);
			}
		}
	}

	/**
	 * Resize event handler.
	 */
	private void onResize() {

		// CGross 1/2/08 - I don't really want to be doing this....
		// I shouldn't be changing something you user configured...
		// leaving out for now
		// if (columnScrolling)
		// {
		// int maxWidth = getClientArea().width;
		// if (rowHeaderVisible)
		// maxWidth -= rowHeaderWidth;
		//
		// for (Iterator cols = columns.iterator(); cols.hasNext();) {
		// GridColumn col = (GridColumn) cols.next();
		// if (col.getWidth() > maxWidth)
		// col.setWidth(maxWidth);
		// }
		// }

		scrollValuesObsolete = true;
		topIndex = -1;
		bottomIndex = -1;
	}

	/**
	 * Scrollbar selection event handler.
	 */
	private void onScrollSelection() {
		topIndex = -1;
		bottomIndex = -1;
		refreshHoverState();
		redraw(getClientArea().x, getClientArea().y, getClientArea().width, getClientArea().height, false);
	}

	/**
	 * Returns the intersection of the given column and given item.
	 *
	 * @param column column
	 * @param item   item
	 * @return x,y of top left corner of the cell
	 */
	Point getOrigin(final GridColumn column, final GridItem item) {
		int x = 0;

		if (rowHeaderVisible) {
			x += rowHeaderWidth;
		}

		x -= getHScrollSelectionInPixels();

		for (final GridColumn colIter : displayOrderedColumns) {

			if (colIter == column) {
				break;
			}

			if (colIter.isVisible()) {
				x += colIter.getWidth();
			}
		}

		int y = 0;
		if (item != null) {
			if (columnHeadersVisible) {
				y += headerHeight;
			}

			int currIndex = getTopIndex();
			final int itemIndex = item.getRowIndex();

			if (itemIndex == -1) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}

			while (currIndex != itemIndex) {
				if (currIndex < itemIndex) {
					final GridItem currItem = items.get(currIndex);
					if (currItem.isVisible()) {
						y += currItem.getHeight() + 1;
					}
					currIndex++;
				} else if (currIndex > itemIndex) {
					currIndex--;
					final GridItem currItem = items.get(currIndex);
					if (currItem.isVisible()) {
						y -= currItem.getHeight() + 1;
					}
				}
			}
		} else {
			if (column.getColumnGroup() != null) {
				y += groupHeaderHeight;
			}
		}

		return new Point(x, y);
	}

	/**
	 * Determines (which cell/if a cell) has been clicked (mouse down really) and
	 * notifies the appropriate renderer. Returns true when a cell has responded to
	 * this event in some way and prevents the event from triggering an action
	 * further down the chain (like a selection).
	 *
	 * @param item item clicked
	 * @param x    mouse x
	 * @param y    mouse y
	 * @return true if this event has been consumed.
	 */
	private boolean handleCellClick(final GridItem item, final int x, final int y) {

		// if(!isTree)
		// return false;

		final GridColumn col = getColumn(new Point(x, y));
		if (col == null) {
			return false;
		}

		col.getCellRenderer().setBounds(item.getBounds(col.index));
		return col.getCellRenderer().notify(IInternalWidget.LeftMouseButtonDown, new Point(x, y), item);

	}

	/**
	 * Sets the hovering variables (hoverItem,hoveringColumn) as well as hoverDetail
	 * by talking to the cell renderers. Triggers a redraw if necessary.
	 *
	 * @param x mouse x
	 * @param y mouse y
	 * @return true if a new section of the table is now being hovered
	 */
	private boolean handleCellHover(final int x, final int y) {

		String detail = "";

		boolean overText = false;

		final GridColumn col = getColumn(new Point(x, y));
		final GridItem item;
		// If the user select cells and drag, the mouse pointer could be out of the
		// grid's bounds
		if (x >= getClientArea().width - 1) {
			item = hoveringItem;
		} else if (x < getClientArea().x) {
			item = hoveringItem;
		} else {
			item = getItem(new Point(x, y));
		}

		GridColumnGroup hoverColGroup = null;
		GridColumn hoverColHeader = null;

		if (col != null) {
			if (item != null) {
				if (y < getClientArea().height - (columnFootersVisible ? footerHeight : 0)) {
					col.getCellRenderer().setBounds(item.getBounds(col.index));

					if (col.getCellRenderer().notify(IInternalWidget.MouseMove, new Point(x, y), item)) {
						detail = col.getCellRenderer().getHoverDetail();
					}

					final Rectangle textBounds = col.getCellRenderer().getTextBounds(item, false);

					if (textBounds != null) {
						final Point p = new Point(x - col.getCellRenderer().getBounds().x,
								y - col.getCellRenderer().getBounds().y);
						overText = textBounds.contains(p);
					}
				}
			} else {
				if (y < headerHeight) {
					if (columnGroups.length != 0 && y < groupHeaderHeight && col.getColumnGroup() != null) {
						hoverColGroup = col.getColumnGroup();
						hoverColGroup.getHeaderRenderer().setBounds(hoverColGroup.getBounds());
						if (hoverColGroup.getHeaderRenderer().notify(IInternalWidget.MouseMove, new Point(x, y),
								hoverColGroup)) {
							detail = hoverColGroup.getHeaderRenderer().getHoverDetail();
						}

						final Rectangle textBounds = hoverColGroup.getHeaderRenderer().getTextBounds(hoverColGroup,
								false);

						if (textBounds != null) {
							final Point p = new Point(x - hoverColGroup.getHeaderRenderer().getBounds().x,
									y - hoverColGroup.getHeaderRenderer().getBounds().y);
							overText = textBounds.contains(p);
						}
					} else {
						// on col header
						hoverColHeader = col;

						col.getHeaderRenderer().setBounds(col.getBounds());
						if (col.getHeaderRenderer().notify(IInternalWidget.MouseMove, new Point(x, y), col)) {
							detail = col.getHeaderRenderer().getHoverDetail();
						}

						final Rectangle textBounds = col.getHeaderRenderer().getTextBounds(col, false);

						if (textBounds != null) {
							final Point p = new Point(x - col.getHeaderRenderer().getBounds().x,
									y - col.getHeaderRenderer().getBounds().y);
							overText = textBounds.contains(p);
						}
					}
				}
			}
		}

		boolean hoverChange = false;

		if (hoveringItem != item || !hoveringDetail.equals(detail) || hoveringColumn != col
				|| hoverColGroup != hoverColumnGroupHeader || hoverColHeader != hoveringColumnHeader) {
			hoveringItem = item;
			hoveringDetail = detail;
			hoveringColumn = col;
			hoveringColumnHeader = hoverColHeader;
			hoverColumnGroupHeader = hoverColGroup;

			final Rectangle clientArea = getClientArea();
			redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);

			hoverChange = true;
		}

		// do inplace toolTip stuff
		if (hoverChange || hoveringOverText != overText) {
			hoveringOverText = overText;

			if (overText) {

				Rectangle cellBounds = null;
				Rectangle textBounds = null;
				Rectangle preferredTextBounds = null;

				if (hoveringItem != null && hoveringItem.getToolTipText(col.index) == null && // no inplace tooltips
				// when regular
				// tooltip
						!col.getWordWrap()) // dont show inplace tooltips for cells with wordwrap
				{
					cellBounds = col.getCellRenderer().getBounds();
					if (cellBounds.x + cellBounds.width > getSize().x) {
						cellBounds.width = getSize().x - cellBounds.x;
					}
					textBounds = col.getCellRenderer().getTextBounds(item, false);
					preferredTextBounds = col.getCellRenderer().getTextBounds(item, true);
				} else if (hoveringColumnHeader != null && hoveringColumnHeader.getHeaderTooltip() == null) // no
				// inplace
				// tooltips
				// when
				// regular
				// tooltip
				{
					cellBounds = hoveringColumnHeader.getHeaderRenderer().getBounds();
					if (cellBounds.x + cellBounds.width > getSize().x) {
						cellBounds.width = getSize().x - cellBounds.x;
					}
					textBounds = hoveringColumnHeader.getHeaderRenderer().getTextBounds(col, false);
					preferredTextBounds = hoveringColumnHeader.getHeaderRenderer().getTextBounds(col, true);
				} else if (hoverColumnGroupHeader != null) {
					cellBounds = hoverColumnGroupHeader.getHeaderRenderer().getBounds();
					if (cellBounds.x + cellBounds.width > getSize().x) {
						cellBounds.width = getSize().x - cellBounds.x;
					}
					textBounds = hoverColumnGroupHeader.getHeaderRenderer().getTextBounds(hoverColumnGroupHeader,
							false);
					preferredTextBounds = hoverColumnGroupHeader.getHeaderRenderer()
							.getTextBounds(hoverColumnGroupHeader, true);
				}

				// if we are truncated
				if (textBounds != null && textBounds.width < preferredTextBounds.width) {
					showToolTip(item, col, hoverColumnGroupHeader,
							new Point(cellBounds.x + textBounds.x, cellBounds.y + textBounds.y));
					// the following 2 lines are done here rather than in showToolTip to allow
					// that method to be overridden yet still capture the mouse.
					setCapture(true);
					inplaceTooltipCapture = true;
				}
			} else {
				hideToolTip();
			}
		}

		// do normal cell specific tooltip stuff
		if (hoverChange) {
			String newTip = null;
			if (hoveringItem != null && hoveringColumn != null) {
				// get cell specific tooltip
				newTip = hoveringItem.getToolTipText(hoveringColumn.index);
			} else if (hoveringColumn != null && hoveringColumnHeader != null) {
				// get column header specific tooltip
				newTip = hoveringColumn.getHeaderTooltip();
			}

			if (newTip == null) { // no cell or column header specific tooltip then use base Grid tooltip
				newTip = getToolTipText();
			}

			// Avoid unnecessarily resetting tooltip - this will cause the tooltip to jump
			// around
			if (newTip != null && !newTip.equals(displayedToolTipText)) {
				updateToolTipText(newTip);
			} else if (newTip == null && displayedToolTipText != null) {
				updateToolTipText(null);
			}
			displayedToolTipText = newTip;
		}

		return hoverChange;
	}

	/**
	 * Sets the tooltip for the whole Grid to the given text. This method is made
	 * available for subclasses to override, when a subclass wants to display a
	 * different than the standard SWT/OS tooltip. Generally, those subclasses would
	 * override this event and use this tooltip text in their own tooltip or just
	 * override this method to prevent the SWT/OS tooltip from displaying.
	 *
	 * @param text the tooltip text to display
	 */
	protected void updateToolTipText(final String text) {
		super.setToolTipText(text);
	}

	/**
	 * Marks the scroll values obsolete so they will be recalculated.
	 */
	protected void setScrollValuesObsolete() {
		scrollValuesObsolete = true;
		redraw();
	}

	/**
	 * Inserts a new column into the table.
	 *
	 * @param column new column
	 * @param index  index to insert new column
	 * @return current number of columns
	 */
	int newColumn(final GridColumn column, final int index) {

		final int size = columns.size();
		if (index == -1) {
			column.index = size;
			columns.add(column);
			displayOrderedColumns.add(column);
		} else {
			column.index = index;
			columns.add(index, column);
			for (int i = index + 1; i < size; i++) {
				columns.get(i).index = i;
			}
			displayOrderedColumns.add(index, column);

			dataVisualizer.addColumn(index);
			for (int i = 0; i < size; i++) {
				columns.get(i).setColumnIndex(i);
			}
		}

		estimate(sizingGC -> {
			computeHeaderHeight(sizingGC);
			computeFooterHeight(sizingGC);
		});

		updatePrimaryCheckColumn();

		for (final GridItem item : items) {
			item.columnAdded(index);
		}

		scrollValuesObsolete = true;
		redraw();
		clearDisplayOrderedCache();
		return size - 1;
	}

	/**
	 * Removes the given column from the table.
	 *
	 * @param column column to remove
	 */
	void removeColumn(final GridColumn column) {
		boolean selectionModified = false;

		final int index = column.index;

		if (cellSelectionEnabled) {
			final Vector<Point> removeSelectedCells = new Vector<>();

			for (final Point cell : selectedCells) {
				if (cell.x == index) {
					removeSelectedCells.add(cell);
				}
			}

			if (removeSelectedCells.size() > 0) {
				selectedCells.removeAll(removeSelectedCells);
				selectionModified = true;
			}

			for (final Point cell : selectedCells) {
				if (cell.x >= index) {
					cell.x--;
					selectionModified = true;
				}
			}
		}

		columns.remove(column);
		final int size = columns.size();
		for (int i = index; i < size; i++) {
			columns.get(i).index = i;
		}
		displayOrderedColumns.remove(column);
		dataVisualizer.clearColumn(index);

		if (focusColumn == column) {
			focusColumn = null;
		}

		updatePrimaryCheckColumn();

		scrollValuesObsolete = true;

		redraw();

		int i = 0;
		for (final GridColumn col : columns) {
			col.setColumnIndex(i);
			i++;
		}

		if (selectionModified && !disposing) {
			updateColumnSelection();
		}
		clearDisplayOrderedCache();
	}

	/**
	 * Manages the setting of the checkbox column when the SWT.CHECK style was given
	 * to the table. This method will ensure that the first column of the table
	 * always has a checkbox when SWT.CHECK is given to the table.
	 */
	private void updatePrimaryCheckColumn() {
		if ((getStyle() & SWT.CHECK) == SWT.CHECK) {
			boolean firstCol = true;

			for (final GridColumn col : columns) {
				col.setTableCheck(firstCol);
				firstCol = false;
			}
		}
	}

	void newRootItem(final GridItem item, final int index) {
		if (index == -1 || index >= rootItems.size()) {
			rootItems.add(item);
		} else {
			rootItems.add(index, item);
		}
	}

	void removeRootItem(final GridItem item) {
		rootItems.remove(item);
	}

	/**
	 * Creates the new item at the given index. Only called from GridItem
	 * constructor.
	 *
	 * @param item  new item
	 * @param index index to insert the item at
	 * @return the index where the item was insert
	 */
	int newItem(final GridItem item, int index, final boolean root) {
		int row = 0;

		if (!isTree) {
			if (item.getParentItem() != null) {
				isTree = true;
			}
		}

		// Have to convert indexes, this method needs a flat index, the method is called
		// with indexes
		// that are relative to the level
		if (root && index != -1) {
			if (index >= rootItems.size()) {
				index = -1;
			} else {
				index = rootItems.get(index).getRowIndex();
			}
		} else if (!root) {
			if (index == -1 || index >= item.getParentItem().getItemCount()) {
				GridItem rightMostDescendent = item.getParentItem();

				while (rightMostDescendent.getItemCount() > 0) {
					rightMostDescendent = rightMostDescendent.getItem(rightMostDescendent.getItemCount() - 1);
				}

				index = rightMostDescendent.getRowIndex() + 1;
			} else {
				index = item.getParentItem().getItem(index).getRowIndex();
			}
		}

		if (index == -1) {
			items.add(item);
			row = items.size() - 1;
		} else {
			items.add(index, item);
			row = index;
			for (int i = index + 1; i < items.size(); i++) {
				items.get(i).increaseRow();
			}
		}

		estimate(sizingGC -> {
			if (items.size() == 1 && !userModifiedItemHeight) {
				itemHeight = computeItemHeight(item, sizingGC);
				// virtual problems here
				if ((getStyle() & SWT.VIRTUAL) != 0) {
					item.setHasSetData(false);
				}
			}

			item.initializeHeight(itemHeight);

			if (isRowHeaderVisible() && isAutoWidth()) {
				rowHeaderWidth = Math.max(rowHeaderWidth, //
						rowHeaderRenderer.computeSize(sizingGC, SWT.DEFAULT, SWT.DEFAULT, item).x);
			}
		});

		scrollValuesObsolete = true;
		topIndex = -1;
		bottomIndex = -1;

		currentVisibleItems++;

		redraw();

		return row;
	}

	/**
	 * Removes the given item from the table. This method is only called from the
	 * item's dispose method.
	 *
	 * @param item item to remove
	 */
	void removeItem(final GridItem item) {

		final Point[] cells = getCells(item);
		boolean selectionModified = false;

		final int index = item.getRowIndex();

		items.remove(item);

		dataVisualizer.clearRow(item);

		if (disposing) {
			return;
		}

		for (int i = index; i < items.size(); i++) {
			items.get(i).decreaseRow();
		}

		if (selectedItems.remove(item)) {
			selectionModified = true;
		}

		for (final Point cell : cells) {
			if (selectedCells.remove(cell)) {
				selectionModified = true;
			}
		}

		if (focusItem == item) {
			focusItem = null;
		}

		scrollValuesObsolete = true;
		topIndex = -1;
		bottomIndex = -1;
		if (item.isVisible()) {
			currentVisibleItems--;
		}

		if (selectionModified && !disposing) {
			updateColumnSelection();
		}

		redraw();
		// Need to update the scrollbars see see 375327
		updateScrollbars();
	}

	/**
	 * Creates the given column group at the given index. This method is only called
	 * from the {@code GridColumnGroup}'s constructor.
	 *
	 * @param group group to add.
	 */
	void newColumnGroup(final GridColumnGroup group) {
		final GridColumnGroup[] newColumnGroups = new GridColumnGroup[columnGroups.length + 1];
		System.arraycopy(columnGroups, 0, newColumnGroups, 0, columnGroups.length);
		newColumnGroups[newColumnGroups.length - 1] = group;
		columnGroups = newColumnGroups;

		// if we just added the first col group, then we need to up the row
		// height
		if (columnGroups.length == 1) {
			computeHeaderHeight();
		}

		scrollValuesObsolete = true;
		redraw();
	}

	/**
	 * Removes the given column group from the table. This method is only called
	 * from the {@code GridColumnGroup}'s dispose method.
	 *
	 * @param group group to remove.
	 */
	void removeColumnGroup(final GridColumnGroup group) {
		final GridColumnGroup[] newColumnGroups = new GridColumnGroup[columnGroups.length - 1];
		int newIndex = 0;
		for (final GridColumnGroup columnGroup : columnGroups) {
			if (columnGroup != group) {
				newColumnGroups[newIndex] = columnGroup;
				newIndex++;
			}
		}
		columnGroups = newColumnGroups;

		if (columnGroups.length == 0) {
			computeHeaderHeight();
		}

		scrollValuesObsolete = true;
		redraw();
	}

	/**
	 * Updates the cached number of visible items by the given amount.
	 *
	 * @param amount amount to update cached total
	 */
	void updateVisibleItems(final int amount) {
		currentVisibleItems += amount;
	}

	/**
	 * Returns the current item in focus.
	 *
	 * @return item in focus or {@code null}.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public GridItem getFocusItem() {
		checkWidget();
		return focusItem;
	}

	/**
	 * Returns the current cell in focus. If cell selection is disabled, this method
	 * returns null.
	 *
	 * @return cell in focus or {@code null}. x represents the column and y the row
	 *         the cell is in
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public Point getFocusCell() {
		checkWidget();
		if (!cellSelectionEnabled) {
			return null;
		}

		int x = -1;
		int y = -1;

		if (focusColumn != null) {
			x = focusColumn.index;
		}

		if (focusItem != null) {
			y = focusItem.getRowIndex();
		}

		return new Point(x, y);
	}

	/**
	 * Sets the focused item to the given item.
	 *
	 * @param item item to focus.
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if item is
	 *                                      disposed</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setFocusItem(final GridItem item) {
		checkWidget();
		// TODO: check and make sure this item is valid for focus
		if (item == null || item.isDisposed() || item.getParent() != this) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		focusItem = item;
	}

	/**
	 * Sets the focused item to the given column. Column focus is only applicable
	 * when cell selection is enabled.
	 *
	 * @param column column to focus.
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if item is
	 *                                      disposed</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setFocusColumn(final GridColumn column) {
		checkWidget();
		// TODO: check and make sure this item is valid for focus
		if (column == null || column.isDisposed() || column.getParent() != this || !column.isVisible()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		focusColumn = column;
		intendedFocusColumn = column;
	}

	/**
	 * Returns an array of the columns in their display order.
	 *
	 * @return columns in display order
	 */
	GridColumn[] getColumnsInOrder() {
		checkWidget();
		return displayOrderedColumns.toArray(new GridColumn[columns.size()]);
	}

	/**
	 * Returns true if the table is set to horizontally scroll column-by-column
	 * rather than pixel-by-pixel.
	 *
	 * @return true if the table is scrolled horizontally by column
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean getColumnScrolling() {
		checkWidget();
		return columnScrolling;
	}

	/**
	 * Sets the table scrolling method to either scroll column-by-column (true) or
	 * pixel-by-pixel (false).
	 *
	 * @param columnScrolling true to horizontally scroll by column, false to scroll
	 *                        by pixel
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setColumnScrolling(final boolean columnScrolling) {
		checkWidget();
		if (rowHeaderVisible && !columnScrolling) {
			return;
		}

		this.columnScrolling = columnScrolling;
		scrollValuesObsolete = true;
		redraw();
	}

	/**
	 * Returns the first visible column that is not spanned by any other column that
	 * is either the given column or any of the columns displaying to the left of
	 * the given column. If the given column and subsequent columns to the right are
	 * either not visible or spanned, this method will return null.
	 *
	 * @param item the grid item
	 * @param col  the grid column index
	 * @return the first visible unspanned column, or null if none exists
	 */
	GridColumn getVisibleColumn_DegradeLeft(final GridItem item, final GridColumn col) {
		int index = displayOrderedColumns.indexOf(col);

		GridColumn prevCol = col;

		int i = 0;
		while (!prevCol.isVisible()) {
			i++;
			if (index - i < 0) {
				return null;
			}

			prevCol = displayOrderedColumns.get(index - i);
		}

		index = displayOrderedColumns.indexOf(prevCol);

		for (int j = 0; j < index; j++) {
			final GridColumn tempCol = displayOrderedColumns.get(j);

			if (!tempCol.isVisible()) {
				continue;
			}

			if (item.getColumnSpan(tempCol.index) >= index - j) {
				prevCol = tempCol;
				break;
			}
		}

		return prevCol;
	}

	/**
	 * Returns the first visible column that is not spanned by any other column that
	 * is either the given column or any of the columns displaying to the right of
	 * the given column. If the given column and subsequent columns to the right are
	 * either not visible or spanned, this method will return null.
	 *
	 * @param item the grid item
	 * @param col  the grid column index
	 * @return the first visible unspanned column, or null if none exists
	 */
	GridColumn getVisibleColumn_DegradeRight(final GridItem item, final GridColumn col) {
		int index = displayOrderedColumns.indexOf(col);

		int i = 0;
		GridColumn nextCol = col;
		while (!nextCol.isVisible()) {
			i++;
			if (index + i == displayOrderedColumns.size()) {
				return null;
			}

			nextCol = displayOrderedColumns.get(index + i);
		}

		index = displayOrderedColumns.indexOf(nextCol);
		final int startIndex = index;

		while (index > 0) {

			index--;
			final GridColumn prevCol = displayOrderedColumns.get(index);

			if (item.getColumnSpan(prevCol.index) >= startIndex - index) {
				if (startIndex == displayOrderedColumns.size() - 1) {
					return null;
				} else {
					return getVisibleColumn_DegradeRight(item, displayOrderedColumns.get(startIndex + 1));
				}
			}

		}

		return nextCol;
	}

	/**
	 * Returns true if the cells are selectable in the reciever.
	 *
	 * @return cell selection enablement status.
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public boolean getCellSelectionEnabled() {
		checkWidget();
		return cellSelectionEnabled;
	}

	/**
	 * Sets whether cells are selectable in the receiver.
	 *
	 * @param cellSelection the cellSelection to set
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setCellSelectionEnabled(final boolean cellSelection) {
		checkWidget();
		if (!cellSelection) {
			selectedCells.clear();
			redraw();
		} else {
			if ((getStyle() & SWT.SINGLE) == 0) {
				// To keep compatibility, one can selected multiple cells
				selectionType = GridSelectionType.MULTI;
			}
			selectedItems.clear();
			redraw();
		}

		cellSelectionEnabled = cellSelection;
	}

	/**
	 * @return <code>true</code> if cell selection is enabled
	 */
	public boolean isCellSelectionEnabled() {
		return cellSelectionEnabled;
	}

	/**
	 * Sets whether cells are selectable in the receiver by dragging the mouse
	 * cursor.
	 *
	 * @param cellDragSelection the cellDragSelection to set
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setCellDragSelectionEnabled(final boolean cellDragSelection) {
		checkWidget();
		cellDragSelectionEnabled = cellDragSelection;
	}

	/**
	 * @return <code>true</code> if cell drag selection is enabled
	 */
	public boolean isCellDragSelectionEnabled() {
		return cellDragSelectionEnabled;
	}

	/**
	 * Deselects the given cell in the receiver. If the given cell is already
	 * deselected it remains deselected. Invalid cells are ignored.
	 *
	 * @param cell cell to deselect.
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the cell is
	 *                                      null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void deselectCell(final Point cell) {
		checkWidget();

		if (cell == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		selectedCells.remove(cell);
		updateColumnSelection();
		redraw();
	}

	/**
	 * Deselects the given cells. Invalid cells are ignored.
	 *
	 * @param cells the cells to deselect.
	 *
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the set of
	 *                                      cells or any cell is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void deselectCells(final Point[] cells) {
		checkWidget();

		if (cells == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		for (final Point cell : cells) {
			if (cell == null) {
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			}
		}

		for (final Point cell : cells) {
			selectedCells.remove(cell);
		}

		updateColumnSelection();

		redraw();
	}

	/**
	 * Deselects all selected cells in the receiver.
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void deselectAllCells() {
		checkWidget();
		selectedCells.clear();
		updateColumnSelection();
		redraw();
	}

	/**
	 * Selects the given cell. Invalid cells are ignored.
	 *
	 * @param cell point whose x values is a column index and y value is an item
	 *             index
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the item is
	 *                                      null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void selectCell(final Point cell) {
		checkWidget();

		if (!cellSelectionEnabled) {
			return;
		}

		if (cell == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		addToCellSelection(cell);
		updateColumnSelection();
		redraw();
	}

	/**
	 * Selects the given cells. Invalid cells are ignored.
	 *
	 * @param cells an arry of points whose x value is a column index and y value is
	 *              an item index
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the set of
	 *                                      cells or an individual cell is null</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void selectCells(final Point[] cells) {
		checkWidget();

		if (!cellSelectionEnabled) {
			return;
		}

		if (cells == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		for (final Point cell : cells) {
			if (cell == null) {
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			}
		}

		for (final Point cell : cells) {
			addToCellSelection(cell);
		}

		updateColumnSelection();
		redraw();
	}

	/**
	 * Selects all cells in the receiver.
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void selectAllCells() {
		checkWidget();
		selectAllCellsInternal();
	}

	/**
	 * Selects all cells in the receiver.
	 *
	 * @return An Event object
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	private Event selectAllCellsInternal() {
		if (!cellSelectionEnabled) {
			return selectAllRowsInternal();
		}

		if (columns.size() == 0) {
			return null;
		}

		if (items.size() == 0) {
			return null;
		}

		int index = 0;
		GridColumn column = displayOrderedColumns.get(index);

		while (!column.isVisible()) {
			index++;

			if (index >= columns.size()) {
				return null;
			}

			column = displayOrderedColumns.get(index);
		}

		final GridColumn oldFocusColumn = focusColumn;
		final GridItem oldFocusItem = focusItem;

		focusColumn = column;
		focusItem = items.get(0);

		final GridItem lastItem = getPreviousVisibleItem(null);
		final GridColumn lastCol = getVisibleColumn_DegradeLeft(lastItem,
				displayOrderedColumns.get(displayOrderedColumns.size() - 1));

		final Event event = updateCellSelection(new Point(lastCol.index, lastItem.getRowIndex()), SWT.MOD2, true,
				false);

		focusColumn = oldFocusColumn;
		focusItem = oldFocusItem;

		updateColumnSelection();

		redraw();
		return event;
	}

	/**
	 * Selects rows in the receiver.
	 *
	 * @return An Event object
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	private Event selectAllRowsInternal() {
		if (cellSelectionEnabled) {
			return selectAllCellsInternal();
		}

		if (GridSelectionType.MULTI != selectionType) {
			return null;
		}

		if (items.size() == 0) {
			return null;
		}

		deselectAll();

		final GridItem oldFocusItem = focusItem;

		final GridItem firstItem = getItem(getTopIndex());
		final GridItem lastItem = getPreviousVisibleItem(null);

		setFocusItem(firstItem);
		updateSelection(firstItem, SWT.NONE);

		final Event event = updateSelection(lastItem, SWT.MOD2);

		setFocusItem(oldFocusItem);

		redraw();
		return event;
	}

	/**
	 * Selects all cells in the given column in the receiver.
	 *
	 * @param col the column to select
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void selectColumn(final int col) {
		checkWidget();
		final Vector<Point> cells = new Vector<>();
		getCells(getColumn(col), cells);
		selectCells(cells.toArray(new Point[0]));
	}

	/**
	 * Selects all cells in the given column group in the receiver.
	 *
	 * @param colGroup the column group
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void selectColumnGroup(final int colGroup) {
		selectColumnGroup(getColumnGroup(colGroup));
	}

	/**
	 * Selects all cells in the given column group in the receiver.
	 *
	 * @param colGroup the column group
	 *
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void selectColumnGroup(final GridColumnGroup colGroup) {
		checkWidget();
		final Vector<Point> cells = new Vector<>();
		getCells(colGroup, cells);
		selectCells(cells.toArray(new Point[0]));
	}

	/**
	 * Selects the selection to the given cell. The existing selection is cleared
	 * before selecting the given cell.
	 *
	 * @param cell point whose x values is a column index and y value is an item
	 *             index
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the item is
	 *                                      null</li>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if the cell
	 *                                      is invalid</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setCellSelection(final Point cell) {
		checkWidget();

		if (!cellSelectionEnabled) {
			return;
		}

		if (cell == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (!isValidCell(cell)) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		selectedCells.clear();
		addToCellSelection(cell);
		updateColumnSelection();
		redraw();
	}

	/**
	 * Selects the selection to the given set of cell. The existing selection is
	 * cleared before selecting the given cells.
	 *
	 * @param cells point array whose x values is a column index and y value is an
	 *              item index
	 * @throws IllegalArgumentException
	 *                                      <ul>
	 *                                      <li>ERROR_NULL_ARGUMENT - if the cell
	 *                                      array or an individual cell is null</li>
	 *                                      <li>ERROR_INVALID_ARGUMENT - if the a
	 *                                      cell is invalid</li>
	 *                                      </ul>
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public void setCellSelection(final Point[] cells) {
		checkWidget();

		if (!cellSelectionEnabled) {
			return;
		}

		if (cells == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		for (final Point cell : cells) {
			if (cell == null) {
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			}

			if (!isValidCell(cell)) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
		}

		selectedCells.clear();
		for (final Point cell : cells) {
			addToCellSelection(cell);
		}

		updateColumnSelection();
		redraw();
	}

	/**
	 * Returns an array of cells that are currently selected in the receiver. The
	 * order of the items is unspecified. An empty array indicates that no items are
	 * selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * selection, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return an array representing the cell selection
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public Point[] getCellSelection() {
		checkWidget();
		return selectedCells.toArray(new Point[selectedCells.size()]);
	}

	GridColumn getFocusColumn() {
		return focusColumn;
	}

	void updateColumnFocus() {
		if (!focusColumn.isVisible()) {
			final int index = displayOrderedColumns.indexOf(focusColumn);
			if (index > 0) {
				GridColumn prev = displayOrderedColumns.get(index - 1);
				prev = getVisibleColumn_DegradeLeft(focusItem, prev);
				if (prev == null) {
					prev = getVisibleColumn_DegradeRight(focusItem, focusColumn);
				}
				focusColumn = prev;
			} else {
				focusColumn = getVisibleColumn_DegradeRight(focusItem, focusColumn);
			}
		}
	}

	private void getCells(final GridColumn col, final Vector<Point> cells) {

		final int colIndex = col.index;

		int columnAtPosition = 0;
		for (final GridColumn nextCol : displayOrderedColumns) {
			if (!nextCol.isVisible()) {
				continue;
			}

			if (nextCol == col) {
				break;
			}

			columnAtPosition++;
		}

		GridItem item = null;
		if (getItemCount() > 0) {
			item = getItem(0);
		}

		while (item != null) {
			// is cell spanned
			final int position = -1;
			boolean spanned = false;
			for (final GridColumn nextCol : displayOrderedColumns) {
				if (!nextCol.isVisible()) {
					continue;
				}

				if (nextCol == col) {
					break;
				}

				final int span = item.getColumnSpan(nextCol.index);

				if (position + span >= columnAtPosition) {
					spanned = true;
					break;
				}
			}

			if (!spanned && item.getColumnSpan(colIndex) == 0) {
				cells.add(new Point(colIndex, item.getRowIndex()));
			}

			item = getNextVisibleItem(item);
		}
	}

	private void getCells(final GridColumnGroup colGroup, final Vector<Point> cells) {
		final GridColumn[] cols = colGroup.getColumns();
		for (final GridColumn col : cols) {
			getCells(col, cells);
		}
	}

	private void getCells(final GridItem item, final Vector<Point> cells) {
		final int itemIndex = item.getRowIndex();

		int span = 0;

		for (final GridColumn nextCol : displayOrderedColumns) {

			if (span > 0) {
				span--;
				continue;
			}

			if (!nextCol.isVisible()) {
				continue;
			}

			span = item.getColumnSpan(nextCol.index);

			cells.add(new Point(nextCol.index, itemIndex));
		}
	}

	private Point[] getCells(final GridItem item) {
		final Vector<Point> cells = new Vector<>();

		final int itemIndex = item.getRowIndex();

		int span = 0;

		for (final GridColumn nextCol : displayOrderedColumns) {

			if (span > 0) {
				span--;
				continue;
			}

			if (!nextCol.isVisible()) {
				continue;
			}

			span = item.getColumnSpan(nextCol.index);

			cells.add(new Point(nextCol.index, itemIndex));
		}
		return cells.toArray(new Point[] {});
	}

	private void getCells(final GridItem fromItem, final GridItem toItem, final Vector<Point> cells) {
		final boolean descending = fromItem.getRowIndex() < toItem.getRowIndex();

		GridItem iterItem = toItem;

		do {
			getCells(iterItem, cells);

			if (iterItem == fromItem) {
				break;
			}

			if (descending) {
				iterItem = getPreviousVisibleItem(iterItem);
			} else {
				iterItem = getNextVisibleItem(iterItem);
			}
		} while (true);
	}

	private int blend(final int v1, final int v2, final int ratio) {
		return (ratio * v1 + (100 - ratio) * v2) / 100;
	}

	private RGB blend(final RGB c1, final RGB c2, final int ratio) {
		final int r = blend(c1.red, c2.red, ratio);
		final int g = blend(c1.green, c2.green, ratio);
		final int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

	/**
	 * Returns a point whose x and y values are the to and from column indexes of
	 * the new selection range inclusive of all spanned columns.
	 *
	 * @param fromItem   the starting grid item
	 * @param fromColumn the starting column
	 * @param toItem     the ending grid item
	 * @param toColumn   the ending column
	 * @return a point representing the selection range
	 */
	private Point getSelectionRange(GridItem fromItem, GridColumn fromColumn, GridItem toItem, GridColumn toColumn) {
		if (displayOrderedColumns.indexOf(fromColumn) > displayOrderedColumns.indexOf(toColumn)) {
			final GridColumn temp = fromColumn;
			fromColumn = toColumn;
			toColumn = temp;
		}

		if (fromItem.getRowIndex() > toItem.getRowIndex()) {
			final GridItem temp = fromItem;
			fromItem = toItem;
			toItem = temp;
		}

		boolean firstTime = true;
		GridItem iterItem = fromItem;

		final int fromIndex = fromColumn.index;
		final int toIndex = toColumn.index;

		do {
			if (!firstTime) {
				iterItem = getNextVisibleItem(iterItem);
			} else {
				firstTime = false;
			}

			final Point cols = getRowSelectionRange(iterItem, fromColumn, toColumn);

			// check and see if column spanning means that the range increased
			if (cols.x != fromIndex || cols.y != toIndex) {
				final GridColumn newFrom = getColumn(cols.x);
				final GridColumn newTo = getColumn(cols.y);

				// Unfortunately we have to start all over again from the top with the new range
				return getSelectionRange(fromItem, newFrom, toItem, newTo);
			}
		} while (iterItem != toItem);

		return new Point(fromColumn.index, toColumn.index);
	}

	/**
	 * Returns a point whose x and y value are the to and from column indexes of the
	 * new selection range inclusive of all spanned columns.
	 *
	 * @param item       the grid item
	 * @param fromColumn the starting column
	 * @param toColumn   the ending column
	 * @return a point representing the row selection range
	 */
	private Point getRowSelectionRange(final GridItem item, final GridColumn fromColumn, final GridColumn toColumn) {

		int newFrom = fromColumn.index;
		int newTo = toColumn.index;

		int span = 0;
		int spanningColIndex = -1;
		boolean spanningBeyondToCol = false;

		for (final GridColumn col : displayOrderedColumns) {

			if (!col.isVisible()) {
				if (span > 0) {
					span--;
				}
				continue;
			}

			if (span > 0) {
				if (col == fromColumn) {
					newFrom = spanningColIndex;
				} else if (col == toColumn && span > 1) {
					spanningBeyondToCol = true;
				}

				span--;

				if (spanningBeyondToCol && span == 0) {
					newTo = col.index;
					break;
				}
			} else {
				final int index = col.index;
				span = item.getColumnSpan(index);
				if (span > 0) {
					spanningColIndex = index;
				}

				if (col == toColumn && span > 0) {
					spanningBeyondToCol = true;
				}
			}

			if (col == toColumn && !spanningBeyondToCol) {
				break;
			}

		}

		return new Point(newFrom, newTo);
	}

	/**
	 * Returns the column which is spanning the given column for the given item or
	 * null if it is not being spanned.
	 *
	 * @param item   the grid item
	 * @param column the grid column
	 * @return the spanning column, or null if not spanned
	 */
	private GridColumn getSpanningColumn(final GridItem item, final GridColumn column) {
		int span = 0;
		GridColumn spanningCol = null;

		for (final GridColumn col : displayOrderedColumns) {

			if (col == column) {
				return spanningCol;
			}

			if (span > 0) {
				span--;
				if (span == 0) {
					spanningCol = null;
				}
			} else {
				span = item.getColumnSpan(col.index);

				if (span > 0) {
					spanningCol = col;
				}
			}
		}
		return spanningCol;
	}

	/**
	 * Returns true if the given cell's x and y values are valid column and item
	 * indexes respectively.
	 *
	 * @param cell the cell point to validate
	 * @return true if the cell is valid, false otherwise
	 */
	private boolean isValidCell(final Point cell) {
		if (cell.x < 0 || cell.x >= columns.size()) {
			return false;
		}

		if (cell.y < 0 || cell.y >= items.size()) {
			return false;
		}

		return true;
	}

	/**
	 * Shows the inplace tooltip for the given item and column. The location is the
	 * x and y origin of the text in the cell.
	 * <p>
	 * This method may be overriden to provide their own custom tooltips.
	 *
	 * @param item     the item currently hovered over or null.
	 * @param column   the column currently hovered over or null.
	 * @param group    the group currently hovered over or null.
	 * @param location the x,y origin of the text in the hovered object.
	 */
	protected void showToolTip(final GridItem item, final GridColumn column, final GridColumnGroup group,
			final Point location) {
		if (inplaceToolTip == null) {
			inplaceToolTip = new GridToolTip(this);
		}

		if (group != null) {
			inplaceToolTip.setFont(getFont());
			inplaceToolTip.setText(group.getText());
		} else if (item != null) {
			inplaceToolTip.setFont(item.getFont(column.index));
			inplaceToolTip.setText(item.getText(column.index));
		} else if (column != null) {
			inplaceToolTip.setFont(getFont());
			inplaceToolTip.setText(column.getText());
		}

		final Point p = getDisplay().map(this, null, location);

		inplaceToolTip.setLocation(p);

		inplaceToolTip.setVisible(true);
	}

	/**
	 * Hides the inplace tooltip.
	 * <p>
	 * This method must be overriden when showToolTip is overriden. Subclasses must
	 * call super when overriding this method.
	 */
	protected void hideToolTip() {
		if (inplaceToolTip != null) {
			inplaceToolTip.setVisible(false);
		}
		if (inplaceTooltipCapture) {
			setCapture(false);
			inplaceTooltipCapture = false;
		}
	}

	void recalculateRowHeaderHeight(final GridItem item, final int oldHeight, final int newHeight) {
		checkWidget();

		if (newHeight > itemHeight) {
			itemHeight = newHeight;

			userModifiedItemHeight = false;
			hasDifferingHeights = false;

			itemHeight = computeItemHeight(items.get(0));

			for (final GridItem item2 : items) {
				item2.setHeight(itemHeight);
			}

			setScrollValuesObsolete();
			redraw();
		}

	}

	void recalculateRowHeaderWidth(final GridItem item, final int oldWidth, final int newWidth) {
		if (!isAutoWidth()) {
			return;
		}

		if (newWidth > rowHeaderWidth) {
			rowHeaderWidth = newWidth;
		} else if (newWidth < rowHeaderWidth && oldWidth == rowHeaderWidth) {
			// if the changed width is smaller, and the previous width of that rows header
			// was equal
			// to the current row header width then its possible that we may need to make
			// the new
			// row header width smaller, but to do that we need to ask all the rows all over
			// again
			computeRowHeaderWidth(newWidth);
		}
		redraw();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFont(final Font font) {
		dataVisualizer.setDefaultFont(font);
		defaultFont = font;
		super.setFont(font);
	}

	/**
	 * Returns the row header width or 0 if row headers are not visible.
	 *
	 * @return the width of the row headers
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getItemHeaderWidth() {
		checkWidget();
		if (!rowHeaderVisible) {
			return 0;
		}
		return rowHeaderWidth;
	}

	/**
	 * Sets the row header width to the specified value. This automatically disables
	 * the auto width feature of the grid.
	 *
	 * @param width the width of the row header
	 * @see #getItemHeaderWidth()
	 * @see #setAutoWidth(boolean)
	 */
	public void setItemHeaderWidth(final int width) {
		checkWidget();
		rowHeaderWidth = width;
		setAutoWidth(false);
		redraw();
	}

	/**
	 * Sets the number of items contained in the receiver.
	 *
	 * @param count the number of items
	 *
	 * @exception org.eclipse.swt.SWTException
	 *                                         <ul>
	 *                                         <li>ERROR_WIDGET_DISPOSED - if the
	 *                                         receiver has been disposed</li>
	 *                                         <li>ERROR_THREAD_INVALID_ACCESS - if
	 *                                         not called from the thread that
	 *                                         created the receiver</li>
	 *                                         </ul>
	 */
	public void setItemCount(int count) {
		checkWidget();
		setRedraw(false);
		if (count < 0) {
			count = 0;
		}

		if (count < items.size()) {

			selectedCells.clear();
			for (int i = items.size() - 1; i >= count; i--) {
				final GridItem removed = items.remove(i);
				rootItems.remove(i);

				selectedItems.remove(removed);

				if (removed.isVisible()) {
					currentVisibleItems--;
				}
				removed.disposeOnly();
			}
			if (!disposing) {
				updateColumnSelection();
			}
			scrollValuesObsolete = true;
			topIndex = -1;
			bottomIndex = -1;
		}

		while (count > items.size()) {
			new GridItem(this, SWT.NONE);
		}
		setRedraw(true);
	}

	/**
	 * Initialize accessibility.
	 */
	private void initAccessible() {
		final Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getDescription(final AccessibleEvent e) {
				final int childID = e.childID;
				if (childID >= 0 && childID < items.size()) {
					String descrption = "";
					for (int i = 0; i < columns.size(); i++) {
						if (i != 0) {
							descrption += columns.get(i).getText() + " : ";
							descrption += items.get(childID).getText(i) + " ";
						}
					}
					e.result = descrption;
				}
			}

			@Override
			public void getName(final AccessibleEvent e) {
				final int childID = e.childID;
				if (childID >= 0 && childID < items.size()) {
					// Name of the items
					e.result = items.get(childID).getText();
				} else if (childID >= items.size() && childID < items.size() + columns.size()) {
					// Name of the column headers
					e.result = columns.get(childID - items.size()).getText();
				} else if (childID >= items.size() + columns.size()
						&& childID < items.size() + columns.size() + columnGroups.length) {
					// Name of the column group headers
					e.result = columnGroups[childID - items.size() - columns.size()].getText();
				} else if (childID >= items.size() + columns.size() + columnGroups.length
						&& childID < items.size() + columns.size() + columnGroups.length + columnGroups.length) {
					// Name of the toggle button for column group headers
					e.result = ACC_TOGGLE_BUTTON_NAME;
				}
			}
		});

		accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getChildAtPoint(final AccessibleControlEvent e) {
				final Point location = toControl(e.x, e.y);
				e.childID = ACC.CHILDID_SELF;

				// Grid Items
				final GridItem item = getItem(location);
				if (item != null) {
					for (int i = 0; i < getItemCount(); i++) {
						if (item.equals(getItem(i))) {
							e.childID = i;
							return;
						}
					}
				} else {
					// Column Headers
					final GridColumn column = overColumnHeader(location.x, location.y);
					final int itemCount = getItemCount();
					if (column != null) {
						for (int i = 0; i < getColumns().length; i++) {
							if (column.equals(getColumn(i))) {
								e.childID = itemCount + i;
								return;
							}
						}
					} else {
						// Column Group headers
						final GridColumnGroup columnGroup = overColumnGroupHeader(location.x, location.y);
						if (columnGroup != null) {
							for (int i = 0; i < getColumnGroups().length; i++) {
								if (columnGroup.equals(getColumnGroup(i))) {
									final Rectangle toggle = ((DefaultColumnGroupHeaderRenderer) columnGroup
											.getHeaderRenderer()).getToggleBounds();
									if (toggle.contains(location.x, location.y)) {
										// Toggle button for column group
										// header
										e.childID = itemCount + getColumns().length + getColumnGroups().length + i;
									} else {
										// Column Group header
										e.childID = itemCount + getColumns().length + i;
									}
									return;
								}
							}
						}
					}
				}
			}

			@Override
			public void getChildCount(final AccessibleControlEvent e) {
				if (e.childID == ACC.CHILDID_SELF) {
					int length = items.size();

					if (isTree) {
						// Child count for parent. Here if the item parent
						// is not an other item,
						// it is consider as children of Grid
						for (final GridItem item : items) {
							if (item.getParentItem() != null) {
								length--;
							}
						}
					}
					e.detail = length;
				}
			}

			@Override
			public void getChildren(final AccessibleControlEvent e) {
				if (e.childID == ACC.CHILDID_SELF) {
					int length = items.size();
					if (isTree) {
						for (final GridItem item : items) {
							if (item.getParentItem() != null) {
								length--;
							}
						}

						final Object[] children = new Object[length];
						int j = 0;

						for (int i = 0; i < items.size(); i++) {
							if (items.get(i).getParentItem() == null) {
								children[j] = Integer.valueOf(i);
								j++;
							}
						}
						e.children = children;
					} else {
						final Object[] children = new Object[length];
						for (int i = 0; i < items.size(); i++) {
							children[i] = Integer.valueOf(i);
						}
						e.children = children;
					}
				}
			}

			@Override
			public void getDefaultAction(final AccessibleControlEvent e) {
				final int childID = e.childID;
				if (childID >= 0 && childID < items.size()) {
					if (getItem(childID).hasChildren()) {
						// Action of tree items
						if (getItem(childID).isExpanded()) {
							e.result = ACC_ITEM_ACTION_COLLAPSE;
						} else {
							e.result = ACC_ITEM_ACTION_EXPAND;
						}
					} else {
						// action of default items
						e.result = ACC_ITEM_DEFAULT_ACTION;
					}
				} else if (childID >= items.size() && childID < items.size() + columns.size() + columnGroups.length) {
					// action of column and column group header
					e.result = ACC_COLUMN_DEFAULT_ACTION;
				} else if (childID >= items.size() + columns.size() + columnGroups.length
						&& childID < items.size() + columns.size() + columnGroups.length + columnGroups.length) {
					// action of toggle button of column group header
					e.result = SWT.getMessage("SWT_Press");
				}
			}

			@Override
			public void getLocation(final AccessibleControlEvent e) {
				// location of parent
				Rectangle location = getBounds();
				location.x = 0;
				location.y = 0;
				final int childID = e.childID;

				if (childID >= 0 && childID < items.size()) {
					// location of items
					final GridItem item = getItem(childID);
					if (item != null) {
						final Point p = getOrigin(columns.get(0), item);
						location.y = p.y;
						location.height = item.getHeight();
					}
				} else if (childID >= items.size() && childID < items.size() + columns.size()) {
					// location of columns headers
					final GridColumn column = getColumn(childID - items.size());
					if (column != null) {
						location.x = getColumnHeaderXPosition(column);
						if (column.getColumnGroup() == null) {
							location.y = 0;
						} else {
							location.y = groupHeaderHeight;
						}
						location.height = headerHeight;
						location.width = column.getWidth();
					}
				} else if (childID >= items.size() + columns.size()
						&& childID < items.size() + columns.size() + columnGroups.length) {
					// location of column group header
					final GridColumnGroup columnGroup = getColumnGroup(childID - items.size() - columns.size());
					if (columnGroup != null) {
						location.y = 0;
						location.height = groupHeaderHeight;
						location.x = getColumnHeaderXPosition(columnGroup.getFirstVisibleColumn());
						int width = 0;
						for (int i = 0; i < columnGroup.getColumns().length; i++) {
							if (columnGroup.getColumns()[i].isVisible()) {
								width += columnGroup.getColumns()[i].getWidth();
							}
						}
						location.width = width;
					}
				} else if (childID >= items.size() + columns.size() + columnGroups.length
						&& childID < items.size() + columns.size() + columnGroups.length + columnGroups.length) {
					// location of toggle button of column group header
					final GridColumnGroup columnGroup = getColumnGroup(
							childID - items.size() - columns.size() - columnGroups.length);
					location = ((DefaultColumnGroupHeaderRenderer) columnGroup.getHeaderRenderer()).getToggleBounds();
				}

				if (location != null) {
					final Point pt = toDisplay(location.x, location.y);
					e.x = pt.x;
					e.y = pt.y;
					e.width = location.width;
					e.height = location.height;
				}
			}

			@Override
			public void getRole(final AccessibleControlEvent e) {
				final int childID = e.childID;
				if (childID >= 0 && childID < items.size()) {
					// role of items
					if (isTree) {
						e.detail = ACC.ROLE_TREEITEM;
					} else {
						e.detail = ACC.ROLE_LISTITEM;
					}
				} else if (childID >= items.size() && childID < items.size() + columns.size() + columnGroups.length) {
					// role of columns headers and column group headers
					e.detail = ACC.ROLE_TABLECOLUMNHEADER;
				} else if (childID >= items.size() + columns.size() + columnGroups.length
						&& childID < items.size() + columns.size() + columnGroups.length + columnGroups.length) {
					// role of toggle button of column group headers
					e.detail = ACC.ROLE_PUSHBUTTON;
				} else if (childID == ACC.CHILDID_SELF) {
					// role of parent
					if (isTree) {
						e.detail = ACC.ROLE_TREE;
					} else {
						e.detail = ACC.ROLE_TABLE;
					}
				}
			}

			@Override
			public void getSelection(final AccessibleControlEvent e) {
				e.childID = ACC.CHILDID_NONE;
				if (selectedItems.size() == 1) {
					// Single selection
					e.childID = selectedItems.get(0).getRowIndex();
				} else if (selectedItems.size() > 1) {
					// multiple selection
					e.childID = ACC.CHILDID_MULTIPLE;
					final int length = selectedItems.size();
					final Object[] children = new Object[length];

					for (int i = 0; i < length; i++) {
						final GridItem item = selectedItems.get(i);
						children[i] = Integer.valueOf(item.getRowIndex());
					}
					e.children = children;
				}
			}

			@Override
			public void getState(final AccessibleControlEvent e) {
				final int childID = e.childID;
				if (childID >= 0 && childID < items.size()) {
					// state of items
					e.detail = ACC.STATE_SELECTABLE;
					if (getDisplay().getActiveShell() == getParent().getShell()) {
						e.detail |= ACC.STATE_FOCUSABLE;
					}

					if (selectedItems.contains(getItem(childID))) {
						e.detail |= ACC.STATE_SELECTED;
						if (getDisplay().getActiveShell() == getParent().getShell()) {
							e.detail |= ACC.STATE_FOCUSED;
						}
					}

					if (getItem(childID).getChecked()) {
						e.detail |= ACC.STATE_CHECKED;
					}

					// only for tree type items
					if (getItem(childID).hasChildren()) {
						if (getItem(childID).isExpanded()) {
							e.detail |= ACC.STATE_EXPANDED;
						} else {
							e.detail |= ACC.STATE_COLLAPSED;
						}
					}

					if (!getItem(childID).isVisible()) {
						e.detail |= ACC.STATE_INVISIBLE;
					}
				} else if (childID >= items.size() && childID < items.size() + columns.size() + columnGroups.length) {
					// state of column headers and column group headers
					e.detail = ACC.STATE_READONLY;
				} else if (childID >= items.size() + columns.size() + columnGroups.length
						&& childID < items.size() + columns.size() + columnGroups.length + columnGroups.length) {
					// state of toggle button of column group headers
					if (getColumnGroup(childID - items.size() - columns.size() - columnGroups.length).getExpanded()) {
						e.detail = ACC.STATE_EXPANDED;
					} else {
						e.detail = ACC.STATE_COLLAPSED;
					}
				}
			}

			@Override
			public void getValue(final AccessibleControlEvent e) {
				final int childID = e.childID;
				if (childID >= 0 && childID < items.size()) {
					// value for tree items
					if (isTree) {
						e.result = "" + getItem(childID).getLevel();
					}
				}
			}
		});

		addListener(SWT.Selection, event -> {
			if (selectedItems.size() > 0) {
				accessible.setFocus(selectedItems.get(selectedItems.size() - 1).getRowIndex());
			}
		});

		addTreeListener(new TreeListener() {
			@Override
			public void treeCollapsed(final TreeEvent e) {
				if (getFocusItem() != null) {
					accessible.setFocus(getFocusItem().getRowIndex());
				}
			}

			@Override
			public void treeExpanded(final TreeEvent e) {
				if (getFocusItem() != null) {
					accessible.setFocus(getFocusItem().getRowIndex());
				}
			}
		});
	}

	/**
	 * @return the disposing
	 */
	boolean isDisposing() {
		return disposing;
	}

	/**
	 * @param hasSpanning the hasSpanning to set
	 */
	void setHasSpanning(final boolean hasSpanning) {
		this.hasSpanning = hasSpanning;
	}

	/**
	 * Returns the receiver's tool tip text, or null if it has not been set.
	 *
	 * @return the receiver's tool tip text
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 */
	@Override
	public String getToolTipText() {
		checkWidget();
		return toolTipText;
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which may be null
	 * indicating that no tool tip text should be shown.
	 *
	 * @param string the new tool tip text (or null)
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 */
	@Override
	public void setToolTipText(final String string) {
		checkWidget();
		toolTipText = string;
	}

	/**
	 * Updates the row height when the first image is set on an item.
	 *
	 * @param column the column the image is change
	 * @param item   item which images has just been set on.
	 */
	void imageSetOnItem(final int column, final GridItem item) {
		if (sizeOnEveryItemImageChange) {
			if (item == null || item.getImage(column) == null) {
				return;
			}

			int height = item.getImage(column).getBounds().height;
			// FIXME Needs better algorithm
			if (height + 20 > itemHeight) {
				height = computeItemHeight(item);
				setItemHeight(height);
			}
		} else {
			if (firstImageSet || userModifiedItemHeight) {
				return;
			}

			final int height = computeItemHeight(item);
			setItemHeight(height);

			firstImageSet = true;
		}
	}

	/**
	 * Determines if the mouse is hovering on the selection drag area and changes
	 * the pointer and sets field appropriately.
	 * <p>
	 * Note: The 'selection drag area' is that part of the selection, on which a
	 * drag event can be initiated. This is either the border of the selection (i.e.
	 * a cell border between a slected and a non-selected cell) or the complete
	 * selection (i.e. anywhere on a selected cell). What area serves as drag area
	 * is determined by {@link #setDragOnFullSelection(boolean)}.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return true if the location is in the drag area, false otherwise
	 * @see #setDragOnFullSelection(boolean)
	 */
	private boolean handleHoverOnSelectionDragArea(final int x, final int y) {
		boolean over = false;
		// Point inSelection = null;

		if ((!rowHeaderVisible || x > rowHeaderWidth - SELECTION_DRAG_BORDER_THRESHOLD)
				&& (!columnHeadersVisible || y > headerHeight - SELECTION_DRAG_BORDER_THRESHOLD)) {
			// not on a header

			// if(!dragOnFullSelection)
			// {
			// // drag area is the border of the selection
			//
			// if(cellSelectionEnabled)
			// {
			// Point neP = new Point( x-SELECTION_DRAG_BORDER_THRESHOLD,
			// y-SELECTION_DRAG_BORDER_THRESHOLD );
			// Point ne = getCell(neP);
			// Point nwP = new Point( x+SELECTION_DRAG_BORDER_THRESHOLD,
			// y-SELECTION_DRAG_BORDER_THRESHOLD );
			// Point nw = getCell(nwP);
			// Point swP = new Point( x+SELECTION_DRAG_BORDER_THRESHOLD,
			// y+SELECTION_DRAG_BORDER_THRESHOLD );
			// Point sw = getCell(swP);
			// Point seP = new Point( x-SELECTION_DRAG_BORDER_THRESHOLD,
			// y+SELECTION_DRAG_BORDER_THRESHOLD );
			// Point se = getCell(seP);
			//
			// boolean neSel = ne != null && isCellSelected(ne);
			// boolean nwSel = nw != null && isCellSelected(nw);
			// boolean swSel = sw != null && isCellSelected(sw);
			// boolean seSel = se != null && isCellSelected(se);
			//
			// over = (neSel || nwSel || swSel || seSel) && (!neSel || !nwSel || !swSel ||
			// !seSel);
			//// inSelection = neSel ? neP : nwSel ? nwP : swSel ? swP : seSel ? seP : null;
			// }
			// else
			// {
			// Point nP = new Point( x, y-SELECTION_DRAG_BORDER_THRESHOLD );
			// GridItem n = getItem(nP);
			// Point sP = new Point( x, y+SELECTION_DRAG_BORDER_THRESHOLD );
			// GridItem s = getItem(sP);
			//
			// boolean nSel = n != null && isSelected(n);
			// boolean sSel = s != null && isSelected(s);
			//
			// over = nSel != sSel;
			//// inSelection = nSel ? nP : sSel ? sP : null;
			// }
			// }
			// else
			// {
			// drag area is the entire selection

			if (cellSelectionEnabled) {
				final Point p = new Point(x, y);
				final Point cell = getCell(p);
				over = cell != null && isCellSelected(cell);
				// inSelection = over ? p : null;
			} else {
				final Point p = new Point(x, y);
				final GridItem item = getItem(p);
				over = item != null && isSelected(item);
				// inSelection = over ? p : null;
			}
		}
		// }

		if (over != hoveringOnSelectionDragArea) {
			// if (over)
			// {
			// // use drag cursor only in border mode
			// if (!dragOnFullSelection)
			// setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
			//// potentialDragStart = inSelection;
			// }
			// else
			// {
			// setCursor(null);
			//// potentialDragStart = null;
			// }
			hoveringOnSelectionDragArea = over;
		}
		return over;
	}

	/**
	 * Display a mark indicating the point at which an item will be inserted. This
	 * is used as a visual hint to show where a dragged item will be inserted when
	 * dropped on the grid. This method should not be called directly, instead
	 * {@link DND#FEEDBACK_INSERT_BEFORE} or {@link DND#FEEDBACK_INSERT_AFTER}
	 * should be set in {@link DropTargetEvent#feedback} from within a
	 * {@link DropTargetListener}.
	 *
	 * @param item   the insert item. Null will clear the insertion mark.
	 * @param column the column of the cell. Null will make the insertion mark span
	 *               all columns.
	 * @param before true places the insert mark above 'item'. false places the
	 *               insert mark below 'item'.
	 *
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_INVALID_ARGUMENT - if the item
	 *                                     or column has been disposed</li>
	 *                                     </ul>
	 * @exception SWTException
	 *                                     <ul>
	 *                                     <li>ERROR_WIDGET_DISPOSED - if the
	 *                                     receiver has been disposed</li>
	 *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                     called from the thread that created the
	 *                                     receiver</li>
	 *                                     </ul>
	 */
	void setInsertMark(final GridItem item, final GridColumn column, final boolean before) {
		checkWidget();
		if (item != null) {
			if (item.isDisposed()) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
		}
		if (column != null) {
			if (column.isDisposed()) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
		}
		insertMarkItem = item;
		insertMarkColumn = column;
		insertMarkBefore = before;
		redraw();
	}

	/**
	 * A helper method for {@link GridDropTargetEffect#dragOver(DropTargetEvent)}.
	 *
	 * @param point the point to check
	 * @return true if point is near the top or bottom border of the visible grid
	 *         area
	 */
	boolean isInDragScrollArea(final Point point) {
		final int rhw = rowHeaderVisible ? rowHeaderWidth : 0;
		final int chh = columnHeadersVisible ? headerHeight : 0;
		final Rectangle top = new Rectangle(rhw, chh, getClientArea().width - rhw, DRAG_SCROLL_AREA_HEIGHT);
		final Rectangle bottom = new Rectangle(rhw, getClientArea().height - DRAG_SCROLL_AREA_HEIGHT,
				getClientArea().width - rhw, DRAG_SCROLL_AREA_HEIGHT);
		return top.contains(point) || bottom.contains(point);
	}

	/**
	 * Clears the item at the given zero-relative index in the receiver. The text,
	 * icon and other attributes of the item are set to the default value. If the
	 * table was created with the <code>SWT.VIRTUAL</code> style, these attributes
	 * are requested again as needed.
	 *
	 * @param index       the index of the item to clear
	 * @param allChildren <code>true</code> if all child items of the indexed item
	 *                    should be cleared recursively, and <code>false</code>
	 *                    otherwise
	 *
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_INVALID_RANGE - if the index is
	 *                                     not between 0 and the number of elements
	 *                                     in the list minus 1 (inclusive)</li>
	 *                                     </ul>
	 * @exception SWTException
	 *                                     <ul>
	 *                                     <li>ERROR_WIDGET_DISPOSED - if the
	 *                                     receiver has been disposed</li>
	 *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                     called from the thread that created the
	 *                                     receiver</li>
	 *                                     </ul>
	 *
	 * @see SWT#VIRTUAL
	 * @see SWT#SetData
	 */
	public void clear(final int index, final boolean allChildren) {
		checkWidget();
		if (index < 0 || index >= items.size()) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}

		final GridItem item = getItem(index);
		item.clear(allChildren);
		redraw();
	}

	/**
	 * Clears the items in the receiver which are between the given zero-relative
	 * start and end indices (inclusive). The text, icon and other attributes of the
	 * items are set to their default values. If the table was created with the
	 * <code>SWT.VIRTUAL</code> style, these attributes are requested again as
	 * needed.
	 *
	 * @param start       the start index of the item to clear
	 * @param end         the end index of the item to clear
	 * @param allChildren <code>true</code> if all child items of the range of items
	 *                    should be cleared recursively, and <code>false</code>
	 *                    otherwise
	 *
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_INVALID_RANGE - if either the
	 *                                     start or end are not between 0 and the
	 *                                     number of elements in the list minus 1
	 *                                     (inclusive)</li>
	 *                                     </ul>
	 * @exception SWTException
	 *                                     <ul>
	 *                                     <li>ERROR_WIDGET_DISPOSED - if the
	 *                                     receiver has been disposed</li>
	 *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                     called from the thread that created the
	 *                                     receiver</li>
	 *                                     </ul>
	 *
	 * @see SWT#VIRTUAL
	 * @see SWT#SetData
	 */
	public void clear(final int start, final int end, final boolean allChildren) {
		checkWidget();
		if (start > end) {
			return;
		}

		final int count = items.size();
		if (!(0 <= start && start <= end && end < count)) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}
		for (int i = start; i <= end; i++) {
			final GridItem item = items.get(i);
			item.clear(allChildren);
		}
		redraw();
	}

	/**
	 * Clears the items at the given zero-relative indices in the receiver. The
	 * text, icon and other attributes of the items are set to their default values.
	 * If the table was created with the <code>SWT.VIRTUAL</code> style, these
	 * attributes are requested again as needed.
	 *
	 * @param indices     the array of indices of the items
	 * @param allChildren <code>true</code> if all child items of the indexed items
	 *                    should be cleared recursively, and <code>false</code>
	 *                    otherwise
	 *
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_INVALID_RANGE - if the index is
	 *                                     not between 0 and the number of elements
	 *                                     in the list minus 1 (inclusive)</li>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the indices
	 *                                     array is null</li>
	 *                                     </ul>
	 * @exception SWTException
	 *                                     <ul>
	 *                                     <li>ERROR_WIDGET_DISPOSED - if the
	 *                                     receiver has been disposed</li>
	 *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                     called from the thread that created the
	 *                                     receiver</li>
	 *                                     </ul>
	 *
	 * @see SWT#VIRTUAL
	 * @see SWT#SetData
	 */
	public void clear(final int[] indices, final boolean allChildren) {
		checkWidget();
		if (indices == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		if (indices.length == 0) {
			return;
		}

		final int count = items.size();
		for (final int index : indices) {
			if (!(0 <= index && index < count)) {
				SWT.error(SWT.ERROR_INVALID_RANGE);
			}
		}
		for (final int indice : indices) {
			final GridItem item = items.get(indice);
			item.clear(allChildren);
		}
		redraw();
	}

	/**
	 * Clears all the items in the receiver. The text, icon and other attributes of
	 * the items are set to their default values. If the table was created with the
	 * <code>SWT.VIRTUAL</code> style, these attributes are requested again as
	 * needed.
	 *
	 * @param allChildren <code>true</code> if all child items of each item should
	 *                    be cleared recursively, and <code>false</code> otherwise
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 *
	 * @see SWT#VIRTUAL
	 * @see SWT#SetData
	 */
	public void clearAll(final boolean allChildren) {
		checkWidget();
		if (items.size() > 0) {
			clear(0, items.size() - 1, allChildren);
		}
	}

	/**
	 * Recalculate the height of the header
	 */
	public void recalculateHeader() {
		final int previous = getHeaderHeight();
		computeHeaderHeight();

		if (previous != getHeaderHeight()) {
			scrollValuesObsolete = true;
			redraw();
		}
	}

	/**
	 * Query the grid for all currently visible rows and columns
	 * <p>
	 * <b>This support is provisional and may change</b>
	 * </p>
	 *
	 * @return all currently visible rows and columns
	 */
	public GridVisibleRange getVisibleRange() {
		// FIXME I think we should remember the topIndex in the onPaint-method
		final int topIndex = getTopIndex();
		final int bottomIndex = getBottomIndex();
		final int startColumnIndex = getStartColumnIndex();
		final int endColumnIndex = getEndColumnIndex();

		final GridVisibleRange range = new GridVisibleRange();
		range.items = new GridItem[0];
		range.columns = new GridColumn[0];

		if (topIndex <= bottomIndex) {
			if (items.size() > 0) {
				range.items = new GridItem[bottomIndex - topIndex + 1];
				for (int i = topIndex; i <= bottomIndex; i++) {
					range.items[i - topIndex] = items.get(i);
				}
			}
		}

		if (startColumnIndex <= endColumnIndex) {
			if (displayOrderedColumns.size() > 0) {
				final List<GridColumn> cols = new ArrayList<>();
				for (int i = startColumnIndex; i <= endColumnIndex; i++) {
					final GridColumn col = displayOrderedColumns.get(i);
					if (col.isVisible()) {
						cols.add(col);
					}
				}

				range.columns = new GridColumn[cols.size()];
				cols.toArray(range.columns);
			}
		}

		return range;
	}

	int getStartColumnIndex() {
		checkWidget();

		if (startColumnIndex != -1) {
			return startColumnIndex;
		}

		if (!hScroll.getVisible()) {
			startColumnIndex = 0;
		}

		startColumnIndex = hScroll.getSelection();

		return startColumnIndex;
	}

	int getEndColumnIndex() {
		checkWidget();

		if (endColumnIndex != -1) {
			return endColumnIndex;
		}

		if (displayOrderedColumns.size() == 0) {
			endColumnIndex = 0;
		} else if (getVisibleGridWidth() < 1) {
			endColumnIndex = getStartColumnIndex();
		} else {
			int x = 0;
			x -= getHScrollSelectionInPixels();

			if (rowHeaderVisible) {
				// row header is actually painted later
				x += rowHeaderWidth;
			}

			final int startIndex = getStartColumnIndex();
			final GridColumn[] columns = new GridColumn[displayOrderedColumns.size()];
			displayOrderedColumns.toArray(columns);

			for (int i = startIndex; i < columns.length; i++) {
				endColumnIndex = i;
				final GridColumn column = columns[i];

				if (column.isVisible()) {
					x += column.getWidth();
				}

				if (x > getClientArea().width) {

					break;
				}
			}

		}

		endColumnIndex = Math.max(0, endColumnIndex);

		return endColumnIndex;
	}

	void setSizeOnEveryItemImageChange(final boolean sizeOnEveryItemImageChange) {
		this.sizeOnEveryItemImageChange = sizeOnEveryItemImageChange;
	}

	/**
	 * Returns the width of the row headers.
	 *
	 * @return width of the column header row
	 * @throws org.eclipse.swt.SWTException
	 *                                      <ul>
	 *                                      <li>ERROR_WIDGET_DISPOSED - if the
	 *                                      receiver has been disposed</li>
	 *                                      <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                      called from the thread that created the
	 *                                      receiver</li>
	 *                                      </ul>
	 */
	public int getRowHeaderWidth() {
		checkWidget();
		return rowHeaderWidth;
	}

	/**
	 * Sets the value of the auto-height feature. When enabled, this feature resizes
	 * the height of rows to reflect the content of cells with word-wrapping
	 * enabled. Cell word-wrapping is enabled via the
	 * GridColumn.setWordWrap(boolean) method. If column headers have word-wrapping
	 * enabled, this feature will also resize the height of the column headers as
	 * necessary.
	 *
	 * @param enabled Set to true to enable this feature, false (default) otherwise.
	 */
	public void setAutoHeight(final boolean enabled) {
		if (autoHeight == enabled) {
			return;
		}

		checkWidget();
		autoHeight = enabled;
		setRowsResizeable(false); // turn of resizing of row height since it conflicts with this property
		redraw();
	}

	/**
	 * Returns the value of the auto-height feature, which resizes row heights and
	 * column header heights based on word-wrapped content.
	 *
	 * @return Returns whether or not the auto-height feature is enabled.
	 * @see #setAutoHeight(boolean)
	 */
	public boolean isAutoHeight() {
		return autoHeight;
	}

	/**
	 * Sets the value of the auto-width feature. When enabled, this feature resizes
	 * the width of the row headers to reflect the content of row headers.
	 *
	 * @param enabled Set to true to enable this feature, false (default) otherwise.
	 * @see #isAutoWidth()
	 */
	public void setAutoWidth(final boolean enabled) {
		if (autoWidth == enabled) {
			return;
		}

		checkWidget();
		autoWidth = enabled;
		redraw();
	}

	/**
	 * Returns the value of the auto-height feature, which resizes row header width
	 * based on content.
	 *
	 * @return Returns whether or not the auto-width feature is enabled.
	 * @see #setAutoWidth(boolean)
	 */
	public boolean isAutoWidth() {
		return autoWidth;
	}

	/**
	 * Sets the value of the word-wrap feature for row headers. When enabled, this
	 * feature will word-wrap the contents of row headers.
	 *
	 * @param enabled Set to true to enable this feature, false (default) otherwise.
	 * @see #isWordWrapHeader()
	 */
	public void setWordWrapHeader(final boolean enabled) {
		if (wordWrapRowHeader == enabled) {
			return;
		}

		checkWidget();
		wordWrapRowHeader = enabled;
		redraw();
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setForeground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setForeground(final Color color) {
		dataVisualizer.setDefaultForeground(color);
		super.setForeground(color);
	}

	/**
	 * Returns the value of the row header word-wrap feature, which word-wraps the
	 * content of row headers.
	 *
	 * @return Returns whether or not the row header word-wrap feature is enabled.
	 * @see #setWordWrapHeader(boolean)
	 */
	public boolean isWordWrapHeader() {
		return wordWrapRowHeader;
	}

	/**
	 * Refresh hasData {@link GridItem} state if {@link Grid} is virtual
	 */
	public void refreshData() {
		if ((getStyle() & SWT.VIRTUAL) != 0) {
			for (final GridItem item : items) {
				item.setHasSetData(false);
			}
		}
	}

	/**
	 * @return <code>true</code> if the mouse navigation is enabled on tab/shift tab
	 */
	public boolean isMoveOnTab() {
		checkWidget();
		return moveOnTab;
	}

	/**
	 * This param allows user to change the current selection by pressing TAB and
	 * SHIFT-TAB
	 *
	 * @param moveOnTab if <code>true</code>, navigation with tab key is enabled.
	 */
	public void setMoveOnTab(final boolean moveOnTab) {
		checkWidget();
		this.moveOnTab = moveOnTab;
	}

	private void computeRowHeaderWidth(final int minWidth) {
		estimate(sizingGC -> {//
			final int width = items.stream() //
					.mapToInt(item -> rowHeaderRenderer.computeSize(sizingGC, SWT.DEFAULT, SWT.DEFAULT, item).x) //
					.max() //
					.orElse(minWidth);
			rowHeaderWidth = width > minWidth ? width : minWidth;
		});
	}

	private int estimateWithResult(final ToIntFunction<GC> function) {
		final GC gc = new GC(Grid.this);
		try {
			return function.applyAsInt(gc);
		} finally {
			gc.dispose();
		}
	}

	private void estimate(final Consumer<GC> consumer) {
		estimateWithResult(gc -> {
			consumer.accept(gc);
			return 0;
		});
	}

	/**
	 * @return true if the grid has focus
	 */
	public boolean isFocusOnGrid() {
		checkWidget();
		return getDisplay().getFocusControl() == this;
	}

	/**
	 * Change selection type (single or multi)
	 *
	 * @param selectionType the new selection type
	 */
	public void setSelectionType(final GridSelectionType selectionType) {
		checkWidget();
		this.selectionType = selectionType;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	@Override
	public void addMouseListener(final MouseListener sourceListener) {
		checkWidget();
		if (sourceListener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		addListener(SWT.MouseUp, e -> {
			sourceListener.mouseUp(new MouseEvent(e));
		});
		addListener(SWT.MouseDown, e -> {
			sourceListener.mouseDown(new MouseEvent(e));
		});
		addListener(SWT.MouseDoubleClick, e -> {
			if (!isDisposed() && e.doit) {
				sourceListener.mouseDoubleClick(new MouseEvent(e));
			}
		});
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#addListener(int,
	 *      org.eclipse.swt.widgets.Listener)
	 */
	@Override
	public void addListener(final int eventType, final Listener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		if (eventType == SWT.MouseDoubleClick) {
			super.addListener(SWT.MouseDoubleClick, e -> {
				if (e.doit) {
					listener.handleEvent(e);
				}
			});
		} else {
			super.addListener(eventType, listener);
		}

	}

}
