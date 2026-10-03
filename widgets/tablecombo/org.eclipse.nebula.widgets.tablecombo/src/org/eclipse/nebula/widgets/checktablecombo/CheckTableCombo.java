/****************************************************************************
 * Copyright (c) 2000 - 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Olivier Titillon  - Initial PR
 *  Laurent CARON <laurent dot caron at gmail dot ccom> - Port to Nebula
 *****************************************************************************/
package org.eclipse.nebula.widgets.checktablecombo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleTextAdapter;
import org.eclipse.swt.accessibility.AccessibleTextEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * The CheckTableCombo class represents a selectable user interface object that
 * combines a label, textfield, and a table and issues notification when an item
 * is selected from the table. The table has checkbox to allow selection of
 * multiple elements.
 *
 * Note: This widget is basically a extension of the CCombo widget. The list
 * control was replaced by a Table control and a Label control was added so that
 * images can be displayed when a value from the drop down items has a image
 * associated to it.
 *
 * <p>
 * TableCombo was written to allow the user to be able to display multiple
 * columns of data in the "Drop Down" portion of the combo.
 * </p>
 * <p>
 * Special Note: Although this class is a subclass of <code>Composite</code>, it
 * does not make sense to add children to it, or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>BORDER, READ_ONLY, FLAT</dd>
 * <dt><b>Events:</b>
 * <dd>DefaultSelection, Modify, Selection, Verify</dd>
 * </dl>
 *
 */

public class CheckTableCombo extends Composite {
	private Shell popup;
	private Button arrow;
	private Label selectedImage;
	private Text text;
	private Table table;
	private Font font;
	private boolean hasFocus;
	private int visibleItemCount = 7;
	private Listener listener;
	private Listener focusFilter;
	private int displayColumnIndex = 0;
	private Color foreground;
	private Color background;
	private int[] columnWidths;
	private int tableWidthPercentage = 100;
	private boolean showImageWithinSelection = true;
	private boolean showColorWithinSelection = true;
	private boolean showFontWithinSelection = true;
	private boolean closePopupAfterSelection = false;
	private String separator = ";";
	private Function<Collection<TableItem>, String> convertToString = items -> {
		final int colIndexToUse = getDisplayColumnIndex();
		final String textToDisplay = items.stream().map(tableItem -> tableItem.getText(colIndexToUse)).collect(Collectors.joining(separator));
		return textToDisplay;
	};

	/**
	 * Constructs a new instance of this class given its parent and a style value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the <code>int</code> "|" operator) two or
	 * more of those <code>SWT</code> style constants. The class description lists the style constants that are applicable to the class. Style bits
	 * are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style the style of widget to construct
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 *                </ul>
	 *
	 * @see SWT#BORDER
	 * @see SWT#READ_ONLY
	 * @see SWT#FLAT
	 * @see Widget#getStyle()
	 */
	public CheckTableCombo(final Composite parent, int style) {
		super(parent, style = checkStyle(style));

		// set the label style
		int textStyle = SWT.SINGLE;
		if ((style & SWT.READ_ONLY) != 0) {
			textStyle |= SWT.READ_ONLY;
		}
		if ((style & SWT.FLAT) != 0) {
			textStyle |= SWT.FLAT;
		}

		// set control background to white
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		// create label to hold image if necessary.
		selectedImage = new Label(this, SWT.NONE);
		selectedImage.setAlignment(SWT.RIGHT);

		getLayout();

		// create the control to hold the display text of what the user selected.
		text = new Text(this, textStyle);

		// set the arrow style.
		int arrowStyle = SWT.ARROW | SWT.DOWN;
		if ((style & SWT.FLAT) != 0) {
			arrowStyle |= SWT.FLAT;
		}

		// create the down arrow button
		arrow = new Button(this, arrowStyle);

		// now add a listener to listen to the events we are interested in.
		listener = event -> {
			if (CheckTableCombo.this.isDisposed()) {
				return;
			}

			// check for a popup event
			if (popup == event.widget) {
				CheckTableCombo.this.popupEvent(event);
				return;
			}

			if (text == event.widget) {
				CheckTableCombo.this.textEvent(event);
				return;
			}

			// check for a table event
			if (table == event.widget) {
				CheckTableCombo.this.tableEvent(event);
				return;
			}

			// check for arrow event
			if (arrow == event.widget) {
				CheckTableCombo.this.arrowEvent(event);
				return;
			}

			// check for this widget's event
			if (CheckTableCombo.this == event.widget) {
				CheckTableCombo.this.comboEvent(event);
				return;
			}

			// check for shell event
			if (CheckTableCombo.this.getShell() == event.widget) {
				CheckTableCombo.this.getDisplay().asyncExec(() -> {
					if (CheckTableCombo.this.isDisposed()) {
						return;
					}
					CheckTableCombo.this.handleFocus(SWT.FocusOut);
				});
			}
		};

		// create new focus listener
		focusFilter = event -> {
			if (CheckTableCombo.this.isDisposed()) {
				return;
			}
			final Shell shell = ((Control) event.widget).getShell();

			if (shell == CheckTableCombo.this.getShell()) {
				CheckTableCombo.this.handleFocus(SWT.FocusOut);
			}
		};

		// set the listeners for this control
		final int[] comboEvents = {SWT.Dispose, SWT.FocusIn, SWT.Move, SWT.Resize};
		for (final int comboEvent : comboEvents) {
			addListener(comboEvent, listener);
		}

		final int[] textEvents = {SWT.DefaultSelection, SWT.KeyDown, SWT.KeyUp, SWT.MenuDetect, SWT.Modify, SWT.MouseDown, SWT.MouseUp,
				SWT.MouseDoubleClick, SWT.MouseWheel, SWT.Traverse, SWT.FocusIn, SWT.Verify};
		for (final int textEvent : textEvents) {
			text.addListener(textEvent, listener);
		}

		// set the listeners for the arrow image
		final int[] arrowEvents = {SWT.Selection, SWT.FocusIn};
		for (final int arrowEvent : arrowEvents) {
			arrow.addListener(arrowEvent, listener);
		}

		// initialize the drop down
		createPopup(-1);

		initAccessible();
	}

	/**
	 * @param style
	 * @return
	 */
	private static int checkStyle(int style) {
		final int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
		style = SWT.NO_FOCUS | style & mask;
		return style;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the receiver's text is modified, by sending it one of the messages
	 * defined in the <code>ModifyListener</code> interface.
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see ModifyListener
	 * @see #removeModifyListener
	 */
	public void addModifyListener(final ModifyListener listener) {
		addTypedListener(listener, SWT.Modify);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the user changes the receiver's selection, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the combo's list selection changes. <code>widgetDefaultSelected</code> is typically called when
	 * ENTER is pressed the combo's text area.
	 * </p>
	 *
	 * @param listener the listener which should be notified when the user changes the receiver's selection
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the user presses keys in the text field. interface.
	 *
	 * @param listener the listener which should be notified when the user presses keys in the text control.
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void addTextControlKeyListener(final KeyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		text.addKeyListener(listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the user presses keys in the text control.
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 */
	public void removeTextControlKeyListener(final KeyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		text.removeKeyListener(listener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the receiver's text is verified, by sending it one of the messages
	 * defined in the <code>VerifyListener</code> interface.
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see VerifyListener
	 * @see #removeVerifyListener
	 *
	 * @since 3.3
	 */
	public void addVerifyListener(final VerifyListener listener) {
		addTypedListener(listener, SWT.Verify);
	}

	/**
	 * Handle Arrow Event
	 *
	 * @param event
	 */
	private void arrowEvent(final Event event) {
		switch (event.type) {
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.MouseDown: {
			final Event mouseEvent = new Event();
			mouseEvent.button = event.button;
			mouseEvent.count = event.count;
			mouseEvent.stateMask = event.stateMask;
			mouseEvent.time = event.time;
			mouseEvent.x = event.x;
			mouseEvent.y = event.y;
			notifyListeners(SWT.MouseDown, mouseEvent);
			event.doit = mouseEvent.doit;
			break;
		}
		case SWT.MouseUp: {
			final Event mouseEvent = new Event();
			mouseEvent.button = event.button;
			mouseEvent.count = event.count;
			mouseEvent.stateMask = event.stateMask;
			mouseEvent.time = event.time;
			mouseEvent.x = event.x;
			mouseEvent.y = event.y;
			notifyListeners(SWT.MouseUp, mouseEvent);
			event.doit = mouseEvent.doit;
			break;
		}
		case SWT.Selection: {
			text.setFocus();
			dropDown(!isDropped());
			break;
		}
		}
	}

	/**
	 * Sets the selection in the receiver's text field to an empty selection starting just before the first character. If the text field is editable,
	 * this has the effect of placing the i-beam at the start of the text.
	 * <p>
	 * Note: To clear the selected items in the receiver's list, use <code>deselectAll()</code>.
	 * </p>
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see #deselectAll
	 */
	public void clearSelection() {
		checkWidget();
		text.clearSelection();
		table.deselectAll();
	}

	/**
	 * Handle Combo events
	 *
	 * @param event
	 */
	private void comboEvent(final Event event) {
		switch (event.type) {
		case SWT.Dispose:
			this.removeListener(SWT.Dispose, listener);
			notifyListeners(SWT.Dispose, event);
			event.type = SWT.None;

			if (popup != null && !popup.isDisposed()) {
				table.removeListener(SWT.Dispose, listener);
				popup.dispose();
			}
			final Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			final Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, focusFilter);
			popup = null;
			text = null;
			table = null;
			arrow = null;
			selectedImage = null;
			break;
		case SWT.FocusIn:
			final Control focusControl = getDisplay().getFocusControl();
			if (focusControl == arrow || focusControl == table) {
				return;
			}
			if (isDropped()) {
				table.setFocus();
			} else {
				text.setFocus();
			}
			break;
		case SWT.Move:
			dropDown(false);
			break;
		case SWT.Resize:
			internalLayout(false, false);
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();

		int overallWidth = 0;
		int overallHeight = 0;
		final int borderWidth = getBorderWidth();

		// use user defined values if they are specified.
		if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
			overallWidth = wHint;
			overallHeight = hHint;
		} else {
			final TableItem[] tableItems = table.getItems();

			final GC gc = new GC(text);
			final int spacer = gc.stringExtent(" ").x; //$NON-NLS-1$
			int maxTextWidth = gc.stringExtent(text.getText()).x;
			final int colIndex = getDisplayColumnIndex();
			int maxImageHeight = 0;
			int currTextWidth = 0;

			// calculate the maximum text width and image height.
			for (final TableItem tableItem : tableItems) {
				currTextWidth = gc.stringExtent(tableItem.getText(colIndex)).x;

				// take image into account if there is one for the tableitem.
				if (tableItem.getImage() != null) {
					currTextWidth += tableItem.getImage().getBounds().width;
					maxImageHeight = Math.max(tableItem.getImage().getBounds().height, maxImageHeight);
				}

				maxTextWidth = Math.max(currTextWidth, maxTextWidth);
			}

			gc.dispose();
			final Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			final Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);

			overallHeight = Math.max(textSize.y, arrowSize.y);
			overallHeight = Math.max(maxImageHeight, overallHeight);
			overallWidth = maxTextWidth + 2 * spacer + arrowSize.x + 2 * borderWidth;

			// use user specified if they were entered.
			if (wHint != SWT.DEFAULT) {
				overallWidth = wHint;
			}
			if (hHint != SWT.DEFAULT) {
				overallHeight = hHint;
			}
		}

		return new Point(overallWidth + 2 * borderWidth, overallHeight + 2 * borderWidth);
	}

	/**
	 * Copies the selected text.
	 * <p>
	 * The current selection is copied to the clipboard.
	 * </p>
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.3
	 */
	public void copy() {
		checkWidget();
		text.copy();
	}

	/**
	 * creates the popup shell.
	 *
	 * @param selectionIndex
	 */
	void createPopup(final int selectionIndex) {
		// create shell and table
		popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);

		// create table
		table = new Table(popup, SWT.FULL_SELECTION | SWT.CHECK);

		if (font != null) {
			table.setFont(font);
		}
		if (foreground != null) {
			table.setForeground(foreground);
		}
		if (background != null) {
			table.setBackground(background);
		}

		// Add popup listeners
		final int[] popupEvents = {SWT.Close, SWT.Paint, SWT.Deactivate, SWT.Help};
		for (final int popupEvent : popupEvents) {
			popup.addListener(popupEvent, listener);
		}

		// add table listeners
		final int[] tableEvents = {SWT.MouseUp, SWT.Selection, SWT.Traverse, SWT.KeyDown, SWT.KeyUp, SWT.FocusIn, SWT.Dispose};
		// int[] tableEvents = { SWT.MouseUp, SWT.Traverse, SWT.KeyDown, SWT.KeyUp, SWT.FocusIn, SWT.Dispose };
		for (final int tableEvent : tableEvents) {
			table.addListener(tableEvent, listener);
		}

		// set the selection
		if (selectionIndex != -1) {
			table.setSelection(selectionIndex);
		}
	}

	/**
	 * Cuts the selected text.
	 * <p>
	 * The current selection is first copied to the clipboard and then deleted from the widget.
	 * </p>
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.3
	 */
	public void cut() {
		checkWidget();
		text.cut();
	}

	/**
	 * handle DropDown request
	 *
	 * @param drop
	 */
	void dropDown(final boolean drop) {

		// if already dropped then return
		if (drop == isDropped()) {
			return;
		}

		// closing the dropDown
		if (!drop) {
			popup.setVisible(false);
			if (!isDisposed() && isFocusControl()) {
				text.setFocus();
			}
			return;
		}

		// if not visible then return
		if (!isVisible()) {
			return;
		}

		// create a new popup if needed.
		if (getShell() != popup.getParent()) {
			final int selectionIndex = table.getSelectionIndex();
			table.removeListener(SWT.Dispose, listener);
			popup.dispose();
			popup = null;
			table = null;
			createPopup(selectionIndex);
		}

		// get the size of the TableCombo.
		final Point tableComboSize = getSize();

		// calculate the table height.
		int itemCount = table.getItemCount();
		itemCount = itemCount == 0 ? visibleItemCount : Math.min(visibleItemCount, itemCount);
		int itemHeight = table.getItemHeight() * itemCount;

		// add 1 to the table height if the table item count is less than the visible item count.
		if (table.getItemCount() <= visibleItemCount) {
			itemHeight += 1;
		}

		if (itemCount <= visibleItemCount) {
			if (table.getHorizontalBar() != null && !table.getHorizontalBar().isVisible()) {
				itemHeight -= table.getHorizontalBar().getSize().y;
			}
		}

		// add height of header if the header is being displayed.
		if (table.getHeaderVisible()) {
			itemHeight += table.getHeaderHeight();
		}

		// get table column references
		TableColumn[] tableColumns = table.getColumns();
		int totalColumns = tableColumns == null ? 0 : tableColumns.length;

		// check to make sure at least one column has been specified. if it hasn't
		// then just create a blank one.
		if (table.getColumnCount() == 0) {
			new TableColumn(table, SWT.NONE);
			totalColumns = 1;
			tableColumns = table.getColumns();
		}

		int totalColumnWidth = 0;
		// now pack any columns that do not have a explicit value set for them.
		for (int colIndex = 0; colIndex < totalColumns; colIndex++) {
			if (!wasColumnWidthSpecified(colIndex)) {
				tableColumns[colIndex].pack();
			}
			totalColumnWidth += tableColumns[colIndex].getWidth();
		}

		// reset the last column's width to the preferred size if it has a explicit value.
		final int lastColIndex = totalColumns - 1;
		if (wasColumnWidthSpecified(lastColIndex)) {
			tableColumns[lastColIndex].setWidth(columnWidths[lastColIndex]);
		}

		// calculate the table size after making adjustments.
		final Point tableSize = table.computeSize(SWT.DEFAULT, itemHeight, false);

		// calculate the table width and table height.
		final double pct = tableWidthPercentage / 100d;
		int tableWidth = (int) (Math.max(tableComboSize.x - 2, tableSize.x) * pct);
		int tableHeight = tableSize.y;

		// add the width of a horizontal scrollbar to the table height if we are
		// not viewing the full table.
		if (tableWidthPercentage < 100) {
			tableHeight += table.getHorizontalBar().getSize().y;
		}

		// set the bounds on the table.
		table.setBounds(1, 1, tableWidth, tableHeight);

		// check to see if we can adjust the table width to by the amount the vertical
		// scrollbar would have taken since the table auto allocates the space whether
		// it is needed or not.
		if (!table.getVerticalBar().getVisible() && tableSize.x - table.getVerticalBar().getSize().x >= tableComboSize.x - 2) {

			tableWidth = tableWidth - table.getVerticalBar().getSize().x;

			// reset the bounds on the table.
			table.setBounds(1, 1, tableWidth, tableHeight);
		}

		// adjust the last column to make sure that there is no empty space.
		autoAdjustColumnWidthsIfNeeded(tableColumns, tableWidth, totalColumnWidth);

		// set the table top index if there is a valid selection.
		final int index = table.getSelectionIndex();
		if (index != -1) {
			table.setTopIndex(index);
		}

		// calculate popup dimensions.
		final Display display = getDisplay();
		final Rectangle tableRect = table.getBounds();
		final Rectangle parentRect = display.map(getParent(), null, getBounds());
		final Point comboSize = getSize();
		final Rectangle displayRect = getMonitor().getClientArea();

		int overallWidth = 0;

		// now set what the overall width should be.
		if (tableWidthPercentage < 100) {
			overallWidth = tableRect.width + 2;
		} else {
			overallWidth = Math.max(comboSize.x, tableRect.width + 2);
		}

		final int overallHeight = tableRect.height + 2;
		int x = parentRect.x;
		int y = parentRect.y + comboSize.y;
		if (y + overallHeight > displayRect.y + displayRect.height) {
			y = parentRect.y - overallHeight;
		}
		if (x + overallWidth > displayRect.x + displayRect.width) {
			x = displayRect.x + displayRect.width - tableRect.width;
		}

		// set the bounds of the popup
		popup.setBounds(x, y, overallWidth, overallHeight);

		lastRefreshIndex = -1;

		// set the popup visible
		popup.setVisible(true);

		// set focus on the table.
		table.setFocus();
	}

	/*
	 * Return the Label immediately preceding the receiver in the z-order, or null if none.
	 */
	private Label getAssociatedLabel() {
		final Control[] siblings = getParent().getChildren();
		for (int i = 0; i < siblings.length; i++) {
			if (siblings[i] == CheckTableCombo.this) {
				if (i > 0 && siblings[i - 1] instanceof Label) {
					return (Label) siblings[i - 1];
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control[] getChildren() {
		checkWidget();
		return new Control[0];
	}

	/**
	 * Gets the editable state.
	 *
	 * @return whether or not the receiver is editable
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.0
	 */
	public boolean getEditable() {
		checkWidget();
		return text.getEditable();
	}

	/**
	 * Returns the item at the given, zero-relative index in the receiver's list. Throws an exception if the index is out of range.
	 *
	 * @param index the index of the item to return
	 * @return the item at the given index
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public String getItem(final int index) {
		checkWidget();
		return table.getItem(index).getText(getDisplayColumnIndex());
	}

	/**
	 * Returns the number of items contained in the receiver's list.
	 *
	 * @return the number of items
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int getItemCount() {
		checkWidget();
		return table.getItemCount();
	}

	/**
	 * Returns the height of the area which would be used to display <em>one</em> of the items in the receiver's list.
	 *
	 * @return the height of one item
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int getItemHeight() {
		checkWidget();
		return table.getItemHeight();
	}

	/**
	 * Returns an array of <code>String</code>s which are the items in the receiver's list.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the items in the receiver's list
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public String[] getItems() {
		checkWidget();

		// get a list of the table items.
		final TableItem[] tableItems = table.getItems();

		final int totalItems = tableItems == null ? 0 : tableItems.length;

		// create string array to hold the total number of items.
		final String[] stringItems = new String[totalItems];

		final int colIndex = getDisplayColumnIndex();

		// now copy the display string from the tableitems.
		for (int index = 0; index < totalItems; index++) {
			stringItems[index] = tableItems[index].getText(colIndex);
		}

		return stringItems;
	}

	/**
	 * Returns a <code>Point</code> whose x coordinate is the start of the selection in the receiver's text field, and whose y coordinate is the end
	 * of the selection. The returned values are zero-relative. An "empty" selection as indicated by the the x
	 * and y coordinates having the same value.
	 *
	 * @return a point representing the selection start and end
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Point getSelection() {
		checkWidget();
		return text.getSelection();
	}

	/**
	 * Returns the zero-relative index of the item which is currently selected in the receiver's list, or -1 if no item is selected.
	 *
	 * @return the index of the selected item
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int[] getSelectionIndices() {
		checkWidget();
		return table.getSelectionIndices();
	}

	/**
	 * Returns the selected table items.
	 */
	public Collection<TableItem> getSelectedTableItems() {
		final Collection<TableItem> selectedTableItems = new ArrayList<>();
		final TableItem[] children = getTable().getItems();
		for (final TableItem item : children) {
			if (item.getChecked()) {
				selectedTableItems.add(item);
			}
		}

		return selectedTableItems;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStyle() {
		checkWidget();

		int style = super.getStyle();
		style &= ~SWT.READ_ONLY;
		if (!text.getEditable()) {
			style |= SWT.READ_ONLY;
		}
		return style;
	}

	/**
	 * Returns a string containing a copy of the contents of the receiver's text field.
	 *
	 * @return the receiver's text
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public String getText() {
		checkWidget();
		return text.getText();
	}

	/**
	 * Returns the height of the receivers's text field.
	 *
	 * @return the text height
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int getTextHeight() {
		checkWidget();
		return text.getLineHeight();
	}

	/**
	 * Gets the number of items that are visible in the drop down portion of the receiver's list.
	 *
	 * @return the number of items that are visible
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.0
	 */
	public int getVisibleItemCount() {
		checkWidget();
		return visibleItemCount;
	}

	/**
	 * Handle Focus event
	 *
	 * @param type
	 */
	private void handleFocus(final int type) {
		switch (type) {
		case SWT.FocusIn: {
			if (hasFocus) {
				return;
			}
			if (getEditable()) {
				text.selectAll();
			}
			hasFocus = true;
			final Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			shell.addListener(SWT.Deactivate, listener);
			final Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, focusFilter);
			display.addFilter(SWT.FocusIn, focusFilter);
			final Event e = new Event();
			notifyListeners(SWT.FocusIn, e);
			break;
		}
		case SWT.FocusOut: {
			if (!hasFocus) {
				return;
			}
			final Control focusControl = getDisplay().getFocusControl();
			if (focusControl == arrow || focusControl == table || focusControl == text) {
				return;
			}
			hasFocus = false;
			final Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			final Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, focusFilter);
			final Event e = new Event();
			notifyListeners(SWT.FocusOut, e);
			break;
		}
		}
	}

	/**
	 * Searches the receiver's list starting at the first item (index 0) until an item is found that is equal to the argument, and returns the index
	 * of that item. If no item is found, returns -1.
	 *
	 * @param string the search item
	 * @return the index of the item
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the string is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int indexOf(final String string) {
		checkWidget();
		if (string == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		// get a list of the table items.
		final TableItem[] tableItems = table.getItems();

		final int totalItems = tableItems == null ? 0 : tableItems.length;
		final int colIndex = getDisplayColumnIndex();

		// now copy the display string from the tableitems.
		for (int index = 0; index < totalItems; index++) {
			if (string.equals(tableItems[index].getText(colIndex))) {
				return index;
			}
		}

		return -1;
	}

	/**
	 * Searches the receiver's list starting at the given, zero-relative index until an item is found that is equal to the argument, and returns the
	 * index of that item. If no item is found or the starting index is out of range, returns -1.
	 *
	 * @param string the search item
	 * @param start the zero-relative index at which to begin the search
	 * @return the index of the item
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the string is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int indexOf(final String string, final int start) {
		checkWidget();
		if (string == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		// get a list of the table items.
		final TableItem[] tableItems = table.getItems();

		final int totalItems = tableItems == null ? 0 : tableItems.length;

		if (start < totalItems) {

			final int colIndex = getDisplayColumnIndex();

			// now copy the display string from the tableitems.
			for (int index = start; index < totalItems; index++) {
				if (string.equals(tableItems[index].getText(colIndex))) {
					return index;
				}
			}
		}

		return -1;
	}

	/**
	 * sets whether or not to show table lines
	 *
	 * @param showTableLines
	 */
	public void setShowTableLines(final boolean showTableLines) {
		checkWidget();
		table.setLinesVisible(showTableLines);
	}

	/**
	 * sets whether or not to show table header.
	 *
	 * @param showTableHeader
	 */
	public void setShowTableHeader(final boolean showTableHeader) {
		checkWidget();
		table.setHeaderVisible(showTableHeader);
	}

	/**
	 * Add Accessbile listeners to label and table.
	 */
	void initAccessible() {
		final AccessibleAdapter accessibleAdapter = new AccessibleAdapter() {
			@Override
			public void getName(final AccessibleEvent e) {
				String name = null;
				final Label label = CheckTableCombo.this.getAssociatedLabel();
				if (label != null) {
					name = CheckTableCombo.this.stripMnemonic(text.getText());
				}
				e.result = name;
			}

			@Override
			public void getKeyboardShortcut(final AccessibleEvent e) {
				String shortcut = null;
				final Label label = CheckTableCombo.this.getAssociatedLabel();
				if (label != null) {
					final String text = label.getText();
					if (text != null) {
						final char mnemonic = CheckTableCombo.this._findMnemonic(text);
						if (mnemonic != '\0') {
							shortcut = "Alt+" + mnemonic; //$NON-NLS-1$
						}
					}
				}
				e.result = shortcut;
			}

			@Override
			public void getHelp(final AccessibleEvent e) {
				e.result = CheckTableCombo.this.getToolTipText();
			}
		};

		getAccessible().addAccessibleListener(accessibleAdapter);
		text.getAccessible().addAccessibleListener(accessibleAdapter);
		table.getAccessible().addAccessibleListener(accessibleAdapter);

		arrow.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(final AccessibleEvent e) {
				e.result = CheckTableCombo.this.isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			@Override
			public void getKeyboardShortcut(final AccessibleEvent e) {
				e.result = "Alt+Down Arrow"; //$NON-NLS-1$
			}

			@Override
			public void getHelp(final AccessibleEvent e) {
				e.result = CheckTableCombo.this.getToolTipText();
			}
		});

		getAccessible().addAccessibleTextListener(new AccessibleTextAdapter() {
			@Override
			public void getCaretOffset(final AccessibleTextEvent e) {
				e.offset = text.getCaretPosition();
			}

			@Override
			public void getSelectionRange(final AccessibleTextEvent e) {
				final Point sel = text.getSelection();
				e.offset = sel.x;
				e.length = sel.y - sel.x;
			}
		});

		getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getChildAtPoint(final AccessibleControlEvent e) {
				final Point testPoint = CheckTableCombo.this.toControl(e.x, e.y);
				if (CheckTableCombo.this.getBounds().contains(testPoint)) {
					e.childID = ACC.CHILDID_SELF;
				}
			}

			@Override
			public void getLocation(final AccessibleControlEvent e) {
				final Rectangle location = CheckTableCombo.this.getBounds();
				final Point pt = CheckTableCombo.this.getParent().toDisplay(location.x, location.y);
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			@Override
			public void getChildCount(final AccessibleControlEvent e) {
				e.detail = 0;
			}

			@Override
			public void getRole(final AccessibleControlEvent e) {
				e.detail = ACC.ROLE_COMBOBOX;
			}

			@Override
			public void getState(final AccessibleControlEvent e) {
				e.detail = ACC.STATE_NORMAL;
			}

			@Override
			public void getValue(final AccessibleControlEvent e) {
				e.result = text.getText();
			}
		});

		text.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getRole(final AccessibleControlEvent e) {
				e.detail = text.getEditable() ? ACC.ROLE_TEXT : ACC.ROLE_LABEL;
			}
		});

		arrow.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getDefaultAction(final AccessibleControlEvent e) {
				e.result = CheckTableCombo.this.isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	/**
	 * returns if the drop down is currently open
	 *
	 * @return
	 */
	private boolean isDropped() {
		return popup.getVisible();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFocusControl() {
		checkWidget();
		// if (label.isFocusControl () || arrow.isFocusControl () || table.isFocusControl () || popup.isFocusControl ()) {
		if (arrow.isFocusControl() || table.isFocusControl() || popup.isFocusControl()) {
			return true;
		}
		return super.isFocusControl();
	}

	/**
	 * This method is invoked when a resize event occurs.
	 *
	 * @param changed
	 * @param closeDropDown
	 */
	private void internalLayout(final boolean changed, final boolean closeDropDown) {
		if (closeDropDown && isDropped()) {
			dropDown(false);
		}
		final Rectangle rect = getClientArea();
		final int width = rect.width;
		final int height = rect.height;
		final Point arrowSize = arrow.computeSize(SWT.DEFAULT, height, changed);

		// calculate text vertical alignment.
		int textYPos = 0;
		final Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (textSize.y < height) {
			textYPos = (height - textSize.y) / 2;
		}

		// does the selected entry have a image associated with it?
		if (selectedImage.getImage() == null) {
			// set image, text, and arrow boundaries
			selectedImage.setBounds(0, 0, 0, 0);
			text.setBounds(0, textYPos, width - arrowSize.x, textSize.y);
			arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
		} else {
			// calculate the amount of width left in the control after taking into account the arrow selector
			int remainingWidth = width - arrowSize.x;
			final Point imageSize = selectedImage.computeSize(SWT.DEFAULT, height, changed);
			int imageWidth = imageSize.x + 2;

			// handle the case where the image is larger than the available space in the control.
			if (imageWidth > remainingWidth) {
				imageWidth = remainingWidth;
				remainingWidth = 0;
			} else {
				remainingWidth = remainingWidth - imageWidth;
			}

			// set the width of the text.
			final int textWidth = remainingWidth;

			// set image, text, and arrow boundaries
			selectedImage.setBounds(0, 0, imageWidth, imageSize.y);
			text.setBounds(imageWidth, textYPos, textWidth, textSize.y);
			arrow.setBounds(imageWidth + textWidth, 0, arrowSize.x, arrowSize.y);
		}
	}

	/**
	 * Handles Table Events.
	 *
	 * @param event
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void tableEvent(final Event event) {
		switch (event.type) {
		case SWT.Dispose:
			if (getShell() != popup.getParent()) {
				final int selectionIndex = table.getSelectionIndex();
				popup = null;
				table = null;
				createPopup(selectionIndex);
			}
			break;
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.MouseUp: {
			if (event.button != 1) {
				return;
			}
			if (closePopupAfterSelection) {
				dropDown(false);
			}
			break;
		}
		case SWT.Selection: {

			if (event.detail == SWT.CHECK) {
				// TableItem checkTableItem = (TableItem) event.item;
				this.refreshText();

				final int[] checkedIndices = getCheckIndices();
				table.select(checkedIndices);

				final Event e = new Event();
				e.time = event.time;
				e.item = event.item;
				e.data = event.data;
				e.stateMask = event.stateMask;
				e.doit = event.doit;
				notifyListeners(SWT.Selection, e);
				event.doit = e.doit;

				table.deselectAll();
				return;
			}

			final int index = table.getSelectionIndex();
			if (index == -1) {
				return;
			}

			// refresh the text.
			this.refreshText(index, event);

			// set the selection in the table.
			table.select(index);

			// Event e = new Event();
			// e.time = event.time;
			// e.stateMask = event.stateMask;
			// e.doit = event.doit;
			// notifyListeners(SWT.Selection, e);
			// event.doit = e.doit;
			break;
		}
		case SWT.Traverse: {
			switch (event.detail) {
			case SWT.TRAVERSE_RETURN:
			case SWT.TRAVERSE_ESCAPE:
			case SWT.TRAVERSE_ARROW_PREVIOUS:
			case SWT.TRAVERSE_ARROW_NEXT:
				event.doit = false;
				break;
			case SWT.TRAVERSE_TAB_NEXT:
			case SWT.TRAVERSE_TAB_PREVIOUS:
				event.doit = text.traverse(event.detail);
				event.detail = SWT.TRAVERSE_NONE;
				if (event.doit) {
					dropDown(false);
				}
				return;
			}
			final Event e = new Event();
			e.time = event.time;
			e.detail = event.detail;
			e.doit = event.doit;
			e.character = event.character;
			e.keyCode = event.keyCode;
			notifyListeners(SWT.Traverse, e);
			event.doit = e.doit;
			event.detail = e.detail;
			break;
		}
		case SWT.KeyUp: {
			final Event e = new Event();
			e.time = event.time;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.KeyUp, e);
			break;
		}
		case SWT.KeyDown: {
			if (event.character == SWT.ESC) {
				// Escape key cancels popup list
				dropDown(false);
			}
			if ((event.stateMask & SWT.ALT) != 0 && (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
				dropDown(false);
			}
			if (event.character == SWT.CR) {
				// Enter causes default selection
				dropDown(false);
				final Event e = new Event();
				e.time = event.time;
				e.stateMask = event.stateMask;
				notifyListeners(SWT.DefaultSelection, e);
			}
			// At this point the widget may have been disposed.
			// If so, do not continue.
			if (isDisposed()) {
				break;
			}
			final Event e = new Event();
			e.time = event.time;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.KeyDown, e);
			break;

		}
		}
	}

	/**
	 * @return
	 */
	private int[] getCheckIndices() {
		final TableItem[] items = getTable().getItems();
		final List<Integer> indexList = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				indexList.add(i);
			}
		}
		final int[] checkedIndices = indexList.stream().mapToInt(index -> index).toArray();
		return checkedIndices;
	}

	/**
	 * Pastes text from clipboard.
	 * <p>
	 * The selected text is deleted from the widget and new text inserted from the clipboard.
	 * </p>
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.3
	 */
	public void paste() {
		checkWidget();
		text.paste();
	}

	/**
	 * Handles Popup Events
	 *
	 * @param event
	 */
	private void popupEvent(final Event event) {
		switch (event.type) {
		case SWT.Paint:
			// draw rectangle around table
			final Rectangle tableRect = table.getBounds();
			event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
			event.gc.drawRectangle(0, 0, tableRect.width + 1, tableRect.height + 1);
			break;
		case SWT.Close:
			event.doit = false;
			dropDown(false);
			break;
		case SWT.Deactivate:
			/*
			 * Bug in GTK. When the arrow button is pressed the popup control receives a deactivate event and then the arrow button receives a
			 * selection event. If we hide the popup in the deactivate event, the selection event will show it again. To
			 * prevent the popup from showing again, we will let the selection event of the arrow button hide the popup. In Windows, hiding the
			 * popup during the deactivate causes the deactivate to be called twice and the selection event to be disappear.
			 */
			if (!"carbon".equals(SWT.getPlatform())) {
				final Point point = arrow.toControl(getDisplay().getCursorLocation());
				final Point size = arrow.getSize();
				final Rectangle rect = new Rectangle(0, 0, size.x, size.y);
				if (!rect.contains(point)) {
					dropDown(false);
				}
			} else {
				dropDown(false);
			}
			break;

		case SWT.Help:
			if (isDropped()) {
				dropDown(false);
			}
			Composite comp = CheckTableCombo.this;
			do {
				if (comp.getListeners(event.type) != null && comp.getListeners(event.type).length > 0) {
					comp.notifyListeners(event.type, event);
					break;
				}
				comp = comp.getParent();
			} while (null != comp);
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void redraw() {
		super.redraw();
		text.redraw();
		arrow.redraw();
		if (popup.isVisible()) {
			table.redraw();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void redraw(final int x, final int y, final int width, final int height, final boolean all) {
		super.redraw(x, y, width, height, true);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the receiver's text is modified.
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see ModifyListener
	 * @see #addModifyListener
	 */
	public void removeModifyListener(final ModifyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.removeTypedListener(SWT.Modify, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the user changes the receiver's selection.
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		this.removeTypedListener(SWT.Selection, listener);
		this.removeTypedListener(SWT.DefaultSelection, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the control is verified.
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see VerifyListener
	 * @see #addVerifyListener
	 *
	 * @since 3.3
	 */
	public void removeVerifyListener(final VerifyListener listener) {
		this.removeTypedListener(SWT.Verify, listener);
	}

	/**
	 * Selects the items at the given zero-relative index in the receiver's list. If the item at the index was already selected, it remains selected.
	 * Indices that are out of range are ignored.
	 *
	 * @param indices the indices of the items to select
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void select(final int[] indices) {
		checkWidget();

		final int[] sortedIndices = indices.clone();
		Arrays.sort(sortedIndices);

		final TableItem[] children = getTable().getItems();
		for (int i = 0; i < children.length; i++) {
			final boolean foundIndex = Arrays.binarySearch(sortedIndices, i) >= 0;
			if (foundIndex) {
				if (!children[i].getChecked()) {
					children[i].setChecked(true);
				}
			} else {
				if (children[i].getChecked()) {
					children[i].setChecked(false);
				}
			}
		}

		// refresh the text field and image label
		this.refreshText();

		// select the row in the table.
		// table.setSelection(indices);

		final Event e = new Event();
		notifyListeners(SWT.Selection, e);
	}

	/**
	 * Selects the items in the receiver's list. If the item at the index was already selected, it remains selected. Indices that are out of range are
	 * ignored.
	 *
	 * @param items the items to select
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void select(final TableItem[] items) {
		checkWidget();

		final List<TableItem> itemsList = Arrays.asList(items);

		final TableItem[] children = getTable().getItems();
		for (final TableItem child : children) {
			final boolean foundItem = itemsList.contains(child);
			if (foundItem) {
				if (!child.getChecked()) {
					child.setChecked(true);
				}
			} else {
				if (child.getChecked()) {
					child.setChecked(false);
				}
			}
		}

		// refresh the text field and image label
		this.refreshText();

		// select the row in the table.
		// table.setSelection(indices);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBackground(final Color color) {
		super.setBackground(color);
		background = color;
		if (text != null) {
			text.setBackground(color);
		}
		if (selectedImage != null) {
			selectedImage.setBackground(color);
		}
		if (table != null) {
			table.setBackground(color);
		}
		if (arrow != null) {
			arrow.setBackground(color);
		}
	}

	/**
	 * Sets the editable state.
	 *
	 * @param editable the new editable state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.0
	 */
	public void setEditable(final boolean editable) {
		checkWidget();
		text.setEditable(editable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		if (popup != null && !enabled) {
			popup.setVisible(false);
		}
		if (selectedImage != null) {
			selectedImage.setEnabled(enabled);
		}
		if (text != null) {
			text.setEnabled(enabled);
		}
		if (arrow != null) {
			arrow.setEnabled(enabled);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFocus() {
		checkWidget();
		if (!isEnabled() || !isVisible()) {
			return false;
		}
		if (isFocusControl()) {
			return true;
		}

		return text.setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFont(final Font font) {
		super.setFont(font);
		this.font = font;
		text.setFont(font);
		table.setFont(font);
		internalLayout(true, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setForeground(final Color color) {
		super.setForeground(color);
		foreground = color;
		if (text != null) {
			text.setForeground(color);
		}
		if (table != null) {
			table.setForeground(color);
		}
		if (arrow != null) {
			arrow.setForeground(color);
		}
	}

	/**
	 * Sets the layout which is associated with the receiver to be the argument which may be null.
	 * <p>
	 * Note : No Layout can be set on this Control because it already manages the size and position of its children.
	 * </p>
	 *
	 * @param layout the receiver's new layout or null
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	@Override
	public void setLayout(final Layout layout) {
		checkWidget();
		return;
	}

	/**
	 * Marks the receiver's list as visible if the argument is <code>true</code>, and marks it invisible otherwise.
	 * <p>
	 * If one of the receiver's ancestors is not visible or some other condition makes the receiver not visible, marking it visible may not actually
	 * cause it to be displayed.
	 * </p>
	 *
	 * @param visible the new visibility state
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.4
	 */
	public void setTableVisible(final boolean visible) {
		checkWidget();
		dropDown(visible);
	}

	/**
	 * Sets the selection in the receiver's text field to the range specified by the argument whose x coordinate is the start of the selection and
	 * whose y coordinate is the end of the selection.
	 *
	 * @param selection a point representing the new selection start and end
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(final Point selection) {
		checkWidget();
		if (selection == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		text.setSelection(selection.x, selection.y);
	}

	/**
	 * Sets the contents of the receiver's text field to the given string.
	 * <p>
	 * Note: The text field in a <code>Combo</code> is typically only capable of displaying a single line of text. Thus, setting the text to a string
	 * containing line breaks or other special characters will probably cause it to display incorrectly.
	 * </p>
	 *
	 * @param string the new text
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the string is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(final String string) {
		checkWidget();
		if (string == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		// find the index of the given string.
		final String[] texts = string.split(separator);
		final int[] indices = new int[texts.length];

		// get a list of the table items.
		final TableItem[] tableItems = table.getItems();

		final int totalItems = tableItems == null ? 0 : tableItems.length;
		final int colIndex = getDisplayColumnIndex();

		for (int i = 0; i < texts.length; i++) {
			indices[i] = -1;
			// now copy the display string from the tableitems.
			for (int index = 0; index < totalItems; index++) {
				if (texts[i].equals(tableItems[index].getText(colIndex))) {
					indices[i] = index;
					break;
				}
			}
		}

		// select the text and table row.
		this.select(indices);
		// clearSelection();
	}

	public void setDisplayText(final String displayText) {
		text.setText(displayText);
	}

	/**
	 * Sets the maximum number of characters that the receiver's text field is capable of holding to be the argument.
	 *
	 * @param limit new text limit
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTextLimit(final int limit) {
		checkWidget();
		text.setTextLimit(limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setToolTipText(final String tipText) {
		checkWidget();
		super.setToolTipText(tipText);
		if (selectedImage != null) {
			selectedImage.setToolTipText(tipText);
		}
		if (text != null) {
			text.setToolTipText(tipText);
		}
		if (arrow != null) {
			arrow.setToolTipText(tipText);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		/*
		 * At this point the widget may have been disposed in a FocusOut event. If so then do not continue.
		 */
		if (isDisposed()) {
			return;
		}
		// TEMPORARY CODE
		if (popup == null || popup.isDisposed()) {
			return;
		}
		if (!visible) {
			popup.setVisible(false);
		}
	}

	/**
	 * Sets the number of items that are visible in the drop down portion of the receiver's list.
	 *
	 * @param count the new number of items to be visible
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 *
	 * @since 3.0
	 */
	public void setVisibleItemCount(final int count) {
		checkWidget();
		if (count > 0) {
			visibleItemCount = count;
		}
	}

	private String stripMnemonic(final String string) {
		int index = 0;
		final int length = string.length();
		do {
			while (index < length && string.charAt(index) != '&') {
				index++;
			}
			if (++index >= length) {
				return string;
			}
			if (string.charAt(index) != '&') {
				return string.substring(0, index - 1) + string.substring(index, length);
			}
			index++;
		} while (index < length);
		return string;
	}

	/**
	 * Defines what columns the drop down table will have.
	 *
	 * Use this method when you don't care about the width of the columns but want to set the column header text.
	 */
	public void defineColumns(final String[] columnHeaders) {
		if (columnHeaders != null && columnHeaders.length > 0) {
			defineColumnsInternal(columnHeaders, null, columnHeaders.length);
		}
	}

	/**
	 * Defines what columns the drop down table will have.
	 *
	 * Use this method when you don't care about the column header text but you want the fields to be a specific width.
	 */
	public void defineColumns(final int[] columnBounds) {
		columnWidths = columnBounds;

		if (columnBounds != null && columnBounds.length > 0) {
			defineColumnsInternal(null, columnBounds, columnBounds.length);
		}
	}

	/**
	 * Defines what columns the drop down table will have.
	 *
	 * Use this method when you don't care about the column headers and you want the columns to be automatically sized based upon their content.
	 *
	 */
	public void defineColumns(final int numberOfColumnsToCreate) {
		if (numberOfColumnsToCreate > 0) {
			defineColumnsInternal(null, null, numberOfColumnsToCreate);
		}

	}

	/**
	 * Defines what columns the drop down table will have.
	 * Use this method when you want to specify the column header text and the column widths.
	 */
	public void defineColumns(final String[] columnHeaders, final int[] columnBounds) {
		if (columnHeaders != null || columnBounds != null) {
			int total = columnHeaders == null ? 0 : columnHeaders.length;
			if (columnBounds != null && columnBounds.length > total) {
				total = columnBounds.length;
			}

			columnWidths = columnBounds;

			// define the columns
			defineColumnsInternal(columnHeaders, columnBounds, total);
		}
	}

	/**
	 * Defines what columns the drop down table will have.
	 */
	private void defineColumnsInternal(final String[] columnHeaders, final int[] columnBounds, final int totalColumnsToBeCreated) {

		checkWidget();

		final int totalColumnHeaders = columnHeaders == null ? 0 : columnHeaders.length;
		final int totalColBounds = columnBounds == null ? 0 : columnBounds.length;

		if (totalColumnsToBeCreated > 0) {

			for (int index = 0; index < totalColumnsToBeCreated; index++) {
				final TableColumn column = new TableColumn(table, SWT.NONE);

				if (index < totalColumnHeaders) {
					column.setText(columnHeaders[index]);
				}

				if (index < totalColBounds) {
					column.setWidth(columnBounds[index]);
				}

				column.setResizable(true);
				column.setMoveable(true);
			}
		}
	}

	/**
	 * Sets the table width percentage in relation to the width of the label control.
	 *
	 * The default value if 100% which means that it will be the same size as the label control. If you want the table to be wider than the label then
	 * just display a value higher than 100%.
	 *
	 * @param ddWidthPct
	 */
	public void setTableWidthPercentage(final int ddWidthPct) {
		checkWidget();

		// don't accept invalid input.
		if (ddWidthPct > 0 && ddWidthPct <= 100) {
			tableWidthPercentage = ddWidthPct;
		}
	}

	/**
	 * Sets the zero-relative column index that will be used to display the currently selected item in the label control.
	 *
	 * @param displayColumnIndex
	 */
	public void setDisplayColumnIndex(final int displayColumnIndex) {
		checkWidget();

		if (displayColumnIndex >= 0) {
			this.displayColumnIndex = displayColumnIndex;
		}
	}

	/**
	 * Modifies the behavior of the popup after an entry was selected. If {@code true} the popup will be closed, if {@code false} it will remain open.
	 *
	 * @param closePopupAfterSelection
	 */
	public void setClosePopupAfterSelection(final boolean closePopupAfterSelection) {
		this.closePopupAfterSelection = closePopupAfterSelection;
	}

	/**
	 * returns the column index of the TableColumn to be displayed when selected.
	 *
	 * @return
	 */
	private int getDisplayColumnIndex() {
		// make sure the requested column index is valid.
		return displayColumnIndex <= table.getColumnCount() - 1 ? displayColumnIndex : 0;
	}

	/*
	 * Return the lowercase of the first non-'&' character following an '&' character in the given string. If there are no '&' characters in the given
	 * string, return '\0'.
	 */
	private char _findMnemonic(final String string) {
		if (string == null) {
			return '\0';
		}
		int index = 0;
		final int length = string.length();
		do {
			while (index < length && string.charAt(index) != '&') {
				index++;
			}
			if (++index >= length) {
				return '\0';
			}
			if (string.charAt(index) != '&') {
				return Character.toLowerCase(string.charAt(index));
			}
			index++;
		} while (index < length);
		return '\0';
	}

	int lastRefreshIndex = -1;

	/**
	 * Refreshes the label control with the selected object's details.
	 */
	private void refreshText(final int index, final Event event) {
		final TableItem[] children = getTable().getItems();
		final TableItem item = children[index];
		if (lastRefreshIndex == index) {

			final Event e = new Event();
			e.time = event.time;
			e.item = item;
			e.data = event.data;
			e.stateMask = event.stateMask;
			e.doit = event.doit;
			e.display = event.display;
			e.button = event.button;
			e.character = event.character;
			e.count = event.count;
			e.end = event.end;
			e.gc = event.gc;
			e.detail= SWT.CHECK;
			e.height = event.height;
			e.index = event.index;
			e.keyCode = event.keyCode;
			e.keyLocation = event.keyLocation;
			e.magnification = event.magnification;
			e.rotation = event.rotation;
			e.segments = event.segments;
			e.segmentsChars = event.segmentsChars;
			e.start = event.start;
			e.stateMask = event.stateMask;
			e.text = event.text;
			e.touches = event.touches;
			e.type = event.type;
			e.widget = event.widget;
			e.width = event.width;
			e.x = event.x;
			e.xDirection = event.xDirection;
			e.y = event.y;
			e.yDirection = event.yDirection;

			item.setChecked(!item.getChecked());

			getTable().notifyListeners(SWT.Selection, e);
		} else {
			lastRefreshIndex = index;
		}

		// // get a reference to the selected TableItem
		// TableItem tableItem = table.getItem(index);
		//
		// // get the TableItem index to use for displaying the text.
		// int colIndexToUse = getDisplayColumnIndex();
		//
		// // set image if requested
		// if (showImageWithinSelection) {
		// // set the selected image
		// selectedImage.setImage(tableItem.getImage(colIndexToUse));
		//
		// // refresh the layout of the widget
		// internalLayout(false, closePopupAfterSelection);
		// }
		//
		// // set color if requested
		// if (showColorWithinSelection) {
		// text.setForeground(tableItem.getForeground(colIndexToUse));
		// }
		//
		// // set font if requested
		// if (showFontWithinSelection) {
		// // set the selected font
		// text.setFont(tableItem.getFont(colIndexToUse));
		// }
		//
		// // set the label text.
		// text.setText(tableItem.getText(colIndexToUse));
		// System.out.println("TODO2");
		//
		// text.selectAll();
	}

	/**
	 * Set the separator
	 *
	 * @param separator
	 */
	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	/**
	 * Return the separator
	 */
	public String getSeparator() {
		return separator;
	}

	public void setConvertToString(final Function<Collection<TableItem>, String> convertToString) {
		this.convertToString = convertToString;
	}

	/**
	 * Refreshes the label control with the selected object's details.
	 */
	public void refreshText() {

		// get the TableItem index to use for displaying the text.
		final int colIndexToUse = getDisplayColumnIndex();

		final Collection<TableItem> selectedTableItems = getSelectedTableItems();
		final TableItem tableItem = selectedTableItems.isEmpty() ? null : selectedTableItems.iterator().next();

		// set image if requested
		if (showImageWithinSelection) {
			// set the selected image
			if (tableItem != null) {
				selectedImage.setImage(tableItem.getImage(colIndexToUse));
			}

			// refresh the layout of the widget
			internalLayout(false, closePopupAfterSelection);
		}

		// set color if requested
		if (showColorWithinSelection) {
			if (tableItem != null) {
				text.setForeground(tableItem.getForeground(colIndexToUse));
			}
		}

		// set font if requested
		if (showFontWithinSelection) {
			// set the selected font
			if (tableItem != null) {
				text.setFont(tableItem.getFont(colIndexToUse));
			}
		}

		// set the label text.
		final String displayText = convertToString.apply(selectedTableItems);
		text.setText(displayText);

		getParent().layout(true);
	}

	/**
	 * @param showImageWithinSelection
	 */
	public void setShowImageWithinSelection(final boolean showImageWithinSelection) {
		checkWidget();
		this.showImageWithinSelection = showImageWithinSelection;
	}

	/**
	 * @param showColorWithinSelection
	 */
	public void setShowColorWithinSelection(final boolean showColorWithinSelection) {
		checkWidget();
		this.showColorWithinSelection = showColorWithinSelection;
	}

	/**
	 * @param showFontWithinSelection
	 */
	public void setShowFontWithinSelection(final boolean showFontWithinSelection) {
		checkWidget();
		this.showFontWithinSelection = showFontWithinSelection;
	}

	/**
	 * returns the Table reference.
	 *
	 * NOTE: the access is public for now but will most likely be changed in a future release.
	 *
	 * @return
	 */
	public Table getTable() {
		checkWidget();
		return table;
	}

	/**
	 * determines if the user explicitly set a column width for a given column index.
	 *
	 * @param columnIndex
	 * @return
	 */
	private boolean wasColumnWidthSpecified(final int columnIndex) {
		return columnWidths != null && columnWidths.length > columnIndex && columnWidths[columnIndex] != SWT.DEFAULT;
	}

	void textEvent(final Event event) {
		switch (event.type) {
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.DefaultSelection: {
			dropDown(false);
			final Event e = new Event();
			e.time = event.time;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.DefaultSelection, e);
			break;
		}
		case SWT.KeyDown: {
			final Event keyEvent = new Event();
			keyEvent.time = event.time;
			keyEvent.character = event.character;
			keyEvent.keyCode = event.keyCode;
			keyEvent.stateMask = event.stateMask;
			notifyListeners(SWT.KeyDown, keyEvent);
			if (isDisposed()) {
				break;
			}
			event.doit = keyEvent.doit;
			if (!event.doit) {
				break;
			}
			if (event.character == SWT.ESC) {
				// Escape key cancels popup list
				dropDown(false);
			}
			if (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN) {
				event.doit = false;
				if ((event.stateMask & SWT.ALT) != 0) {
					final boolean dropped = isDropped();
					text.selectAll();
					if (!dropped) {
						setFocus();
					}
					dropDown(!dropped);
					break;
				}

				final int oldIndex = table.getSelectionIndex();
				if (event.keyCode == SWT.ARROW_UP) {
					this.select(new int[] {Math.max(oldIndex - 1, 0)});
				} else {
					this.select(new int[] {Math.min(oldIndex + 1, getItemCount() - 1)});
				}
				if (oldIndex != table.getSelectionIndex()) {
					final Event e = new Event();
					e.time = event.time;
					e.stateMask = event.stateMask;
					notifyListeners(SWT.Selection, e);
				}
				if (isDisposed()) {
					break;
				}
			}

			// Further work : Need to add support for incremental search in
			// pop up list as characters typed in text widget
			break;
		}
		case SWT.KeyUp: {
			final Event e = new Event();
			e.time = event.time;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.KeyUp, e);
			event.doit = e.doit;
			break;
		}
		case SWT.MenuDetect: {
			final Event e = new Event();
			e.time = event.time;
			notifyListeners(SWT.MenuDetect, e);
			break;
		}
		case SWT.Modify: {
			table.deselectAll();
			final Event e = new Event();
			e.time = event.time;
			notifyListeners(SWT.Modify, e);
			break;
		}
		case SWT.MouseDown: {
			final Event mouseEvent = new Event();
			mouseEvent.button = event.button;
			mouseEvent.count = event.count;
			mouseEvent.stateMask = event.stateMask;
			mouseEvent.time = event.time;
			mouseEvent.x = event.x;
			mouseEvent.y = event.y;
			notifyListeners(SWT.MouseDown, mouseEvent);
			if (isDisposed()) {
				break;
			}
			event.doit = mouseEvent.doit;
			if (!event.doit) {
				break;
			}
			if (event.button != 1) {
				return;
			}
			if (text.getEditable()) {
				return;
			}
			final boolean dropped = isDropped();
			text.selectAll();
			if (!dropped) {
				setFocus();
			}
			dropDown(!dropped);
			break;
		}
		case SWT.MouseUp: {
			final Event mouseEvent = new Event();
			mouseEvent.button = event.button;
			mouseEvent.count = event.count;
			mouseEvent.stateMask = event.stateMask;
			mouseEvent.time = event.time;
			mouseEvent.x = event.x;
			mouseEvent.y = event.y;
			notifyListeners(SWT.MouseUp, mouseEvent);
			if (isDisposed()) {
				break;
			}
			event.doit = mouseEvent.doit;
			if (!event.doit) {
				break;
			}
			if (event.button != 1) {
				return;
			}
			if (text.getEditable()) {
				return;
			}
			text.selectAll();
			break;
		}
		case SWT.MouseDoubleClick: {
			final Event mouseEvent = new Event();
			mouseEvent.button = event.button;
			mouseEvent.count = event.count;
			mouseEvent.stateMask = event.stateMask;
			mouseEvent.time = event.time;
			mouseEvent.x = event.x;
			mouseEvent.y = event.y;
			notifyListeners(SWT.MouseDoubleClick, mouseEvent);
			break;
		}
		case SWT.MouseWheel: {
			final Event keyEvent = new Event();
			keyEvent.time = event.time;
			keyEvent.keyCode = event.count > 0 ? SWT.ARROW_UP : SWT.ARROW_DOWN;
			keyEvent.stateMask = event.stateMask;
			notifyListeners(SWT.KeyDown, keyEvent);
			if (isDisposed()) {
				break;
			}
			event.doit = keyEvent.doit;
			if (!event.doit) {
				break;
			}
			if (event.count != 0) {
				event.doit = false;
				final int oldIndex = table.getSelectionIndex();
				if (event.count > 0) {
					this.select(new int[] {Math.max(oldIndex - 1, 0)});
				} else {
					this.select(new int[] {Math.min(oldIndex + 1, getItemCount() - 1)});
				}
				if (oldIndex != table.getSelectionIndex()) {
					final Event e = new Event();
					e.time = event.time;
					e.stateMask = event.stateMask;
					notifyListeners(SWT.Selection, e);
				}
				if (isDisposed()) {
					break;
				}
			}
			break;
		}
		case SWT.Traverse: {
			switch (event.detail) {
			case SWT.TRAVERSE_ARROW_PREVIOUS:
			case SWT.TRAVERSE_ARROW_NEXT:
				// The enter causes default selection and
				// the arrow keys are used to manipulate the list contents so
				// do not use them for traversal.
				event.doit = false;
				break;
			case SWT.TRAVERSE_TAB_PREVIOUS:
				event.doit = this.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
				event.detail = SWT.TRAVERSE_NONE;
				return;
			}
			final Event e = new Event();
			e.time = event.time;
			e.detail = event.detail;
			e.doit = event.doit;
			e.character = event.character;
			e.keyCode = event.keyCode;
			notifyListeners(SWT.Traverse, e);
			event.doit = e.doit;
			event.detail = e.detail;
			break;
		}
		case SWT.Verify: {
			final Event e = new Event();
			e.text = event.text;
			e.start = event.start;
			e.end = event.end;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.Verify, e);
			event.doit = e.doit;
			break;
		}
		}
	}

	/**
	 * adjusts the last table column width to fit inside of the table if the table column data does not fill out the table area.
	 */
	private void autoAdjustColumnWidthsIfNeeded(final TableColumn[] tableColumns, final int totalAvailWidth, final int totalColumnWidthUsage) {

		int scrollBarSize = 0;
		final int totalColumns = tableColumns == null ? 0 : tableColumns.length;

		// determine if the vertical scroll bar needs to be taken into account
		if (table.getVerticalBar().getVisible()) {
			scrollBarSize = table.getVerticalBar() == null ? 0 : table.getVerticalBar().getSize().x;
		}

		// is there any extra space that the table is not using?
		if (totalAvailWidth > totalColumnWidthUsage + scrollBarSize) {
			final int totalAmtToBeAllocated = totalAvailWidth - totalColumnWidthUsage - scrollBarSize;

			// add unused space to the last column.
			if (totalAmtToBeAllocated > 0) {
				tableColumns[totalColumns - 1].setWidth(tableColumns[totalColumns - 1].getWidth() + totalAmtToBeAllocated);
			}
		}
	}

	/**
	 * Returns the Text control reference.
	 *
	 * @return
	 */
	public Text getTextControl() {
		checkWidget();
		return text;
	}
}