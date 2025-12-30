/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron@gmail.com) - initial API and
 * implementation
 *******************************************************************************/

package org.eclipse.nebula.widgets.opal.multichoice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.opal.commons.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The MultiChoice class represents a selectable user interface object that
 * combines a read-only text-field and a set of checkboxes.
 *
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>, it
 * does not make sense to add children to it, or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>NONE</dd>
 * <dt><b>Events:</b>
 * <dd>Selection</dd>
 * </dl>
 *
 * @param <T> Class of objects represented by this widget
 */
public class MultiChoice<T> extends Composite {

	private Text text;
	private Button arrow;
	private Shell popup;
	private ScrolledComposite scrolledComposite;
	private Listener listener, filter;
	private int numberOfColumns = 2;
	private List<T> elements;
	private Set<T> selection;
	private List<Button> checkboxes;
	private boolean hasFocus;
	private MultiChoiceSelectionListener<T> selectionListener;
	private T lastModified;
	private Color foreground, background;
	private Font font;
	private String separator;
	private MultiChoiceLabelProvider labelProvider;
	private int preferredHeightOfPopup;
	private boolean showSelectUnselectAll;
	private boolean searchFieldVisible;
	private String searchedText;

	/**
	 * Constructs a new instance of this class given its parent.
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot
	 *               be null)
	 * @param style  not used
	 *
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the parent
	 *                                     is null</li>
	 *                                     </ul>
	 * @exception SWTException
	 *                                     <ul>
	 *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                     called from the thread that created the
	 *                                     parent</li>
	 *                                     </ul>
	 */
	public MultiChoice(final Composite parent, final int style) {
		this(parent, style, null);
	}

	/**
	 * Constructs a new instance of this class given its parent.
	 *
	 * @param parent   a widget which will be the parent of the new instance (cannot
	 *                 be null)
	 * @param style    not used
	 * @param elements list of elements displayed by this widget
	 *
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the parent
	 *                                     is null</li>
	 *                                     </ul>
	 * @exception SWTException
	 *                                     <ul>
	 *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                     called from the thread that created the
	 *                                     parent</li>
	 *                                     </ul>
	 */
	public MultiChoice(final Composite parent, final int style, final List<T> elements) {
		super(parent, style);

		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = gridLayout.marginWidth = gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		int readOnlyStyle;
		if ((style & SWT.READ_ONLY) == SWT.READ_ONLY) {
			readOnlyStyle = SWT.READ_ONLY;
		} else {
			readOnlyStyle = SWT.NONE;
		}

		searchFieldVisible = (style & SWT.SEARCH) == SWT.SEARCH;

		text = new Text(this, SWT.SINGLE | readOnlyStyle | SWT.BORDER);
		text.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		text.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		arrow = new Button(this, SWT.ARROW | SWT.RIGHT);
		arrow.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		createGlobalListener();
		addListeners();

		filter = event -> {
			final Shell shell = ((Control) event.widget).getShell();
			if (shell == MultiChoice.this.getShell()) {
				handleFocusEvents(SWT.FocusOut);
			}
		};

		selection = new LinkedHashSet<>();
		this.elements = elements;
		separator = ",";
		labelProvider = new MultiChoiceDefaultLabelProvider();

		createPopup();
		setLabel();
	}

	private void createGlobalListener() {
		listener = event -> {
			if (MultiChoice.this.popup == event.widget) {
				handlePopupEvent(event);
				return;
			}

			if (MultiChoice.this.arrow == event.widget) {
				handleButtonEvent(event);
				return;
			}

			if (MultiChoice.this == event.widget) {
				handleMultiChoiceEvents(event);
				return;
			}

			if (getShell() == event.widget) {
				getDisplay().asyncExec(() -> {
					if (isDisposed()) {
						return;
					}
					handleFocusEvents(SWT.FocusOut);
				});
			}
		};
	}

	private void addListeners() {

		final int[] multiChoiceEvent = { SWT.Dispose, SWT.Move, SWT.Resize };
		for (final int element : multiChoiceEvent) {
			addListener(element, listener);
		}

		if ((getStyle() & SWT.READ_ONLY) == 0) {
			final Listener validationListener = event -> {
				if (!MultiChoice.this.popup.isDisposed() && !MultiChoice.this.popup.isVisible()) {
					validateEntry();
				}
			};
			text.addListener(SWT.FocusOut, validationListener);
		}

		final int[] buttonEvents = { SWT.Selection, SWT.FocusIn };
		for (final int buttonEvent : buttonEvents) {
			arrow.addListener(buttonEvent, listener);
		}
	}

	protected void validateEntry() {
		final String toValidate = text.getText();
		final String[] elementsToValidate = toValidate.split(separator);
		final List<String> fieldsInError = new ArrayList<>();
		selection.clear();
		for (final String elementToValidate : elementsToValidate) {
			final String temp = elementToValidate.trim();
			if ("".equals(temp)) {
				continue;
			}

			final T entry = convertEntry(temp);

			if (entry == null) {
				fieldsInError.add(temp);
			} else {
				selection.add(entry);
			}
		}

		if (fieldsInError.size() == 0) {
			updateSelection();
			return;
		}

		final String messageToDisplay;
		if (fieldsInError.size() == 1) {
			messageToDisplay = String.format(ResourceManager.getLabel(ResourceManager.MULTICHOICE_MESSAGE), //
					fieldsInError.get(0));
		} else {
			final StringBuilder sb = new StringBuilder();
			final Iterator<String> it = fieldsInError.iterator();
			while (it.hasNext()) {
				sb.append(it.next());
				if (it.hasNext()) {
					sb.append(",");
				}
			}
			messageToDisplay = String.format(ResourceManager.getLabel(ResourceManager.MULTICHOICE_MESSAGE_PLURAL), //
					sb.toString());
		}
		getDisplay().asyncExec(() -> {
			final MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(messageToDisplay);
			mb.open();
			MultiChoice.this.text.forceFocus();
		});

	}

	private T convertEntry(final String elementToValidate) {
		for (final T elt : elements) {
			if (labelProvider.getText(elt).trim().equals(elementToValidate)) {
				return elt;
			}
		}
		return null;
	}

	/**
	 * Adds the argument to the end of the receiver's list.
	 *
	 * @param values new item
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the string
	 *                                     is null</li>
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
	public void add(final T value) {
		checkWidget();
		if (value == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (elements == null) {
			elements = new ArrayList<>();
		}
		elements.add(value);
		refresh();
	}

	/**
	 * Adds the argument to the receiver's list at the given zero-relative index.
	 *
	 * @param values new item
	 * @param index  the index for the item
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the string
	 *                                     is null</li>
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
	 */
	public void add(final T value, final int index) {
		checkWidget();
		checkNullElement();
		if (value == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		checkRange(index);

		elements.add(index, value);
		refresh();
	}

	/**
	 * Adds the argument to the end of the receiver's list.
	 *
	 * @param values new items
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the string
	 *                                     is null</li>
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
	public void addAll(final List<T> values) {
		checkWidget();
		if (values == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (elements == null) {
			elements = new ArrayList<>();
		}
		elements.addAll(values);
		refresh();
	}

	/**
	 * Adds the argument to the end of the receiver's list.
	 *
	 * @param values new items
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the string
	 *                                     is null</li>
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
	public void addAll(final T[] values) {
		checkWidget();
		if (values == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		if (elements == null) {
			elements = new ArrayList<>();
		}
		for (final T value : values) {
			elements.add(value);
		}
		refresh();
	}

	/**
	 * Returns the editable state.
	 *
	 * @return whether or not the receiver is editable
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 */
	public boolean getEditable() {
		return text.getEditable();
	}

	/**
	 * Returns the item at the given, zero-relative index in the receiver's list.
	 * Throws an exception if the index is out of range.
	 *
	 * @param index the index of the item to return
	 * @return the item at the given index
	 *
	 * @exception NullPointerException     if there is no item in the receiver
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
	 */
	public T getItem(final int index) {
		checkWidget();
		checkNullElement();
		checkRange(index);

		return elements.get(index);
	}

	/**
	 * Returns the number of items contained in the receiver's list.
	 *
	 * @return the number of items
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 */
	public int getItemCount() {
		checkWidget();
		if (elements == null) {
			return 0;
		}
		return elements.size();
	}

	/**
	 * Returns the list of items in the receiver's list.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the items in the receiver's list
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 */
	public List<T> getItems() {
		checkWidget();
		if (elements == null) {
			return null;
		}
		return new ArrayList<>(elements);
	}

	/**
	 * Removes the item from the receiver's list at the given zero-relative index.
	 *
	 * @param index the index for the item
	 * @exception NullPointerException     if there is no item in the receiver
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
	 */
	public void removeAt(final int index) {
		checkWidget();
		checkNullElement();
		checkRange(index);
		final Object removedElement = elements.remove(index);
		selection.remove(removedElement);
		refresh();
	}

	/**
	 * Searches the receiver's list starting at the first item until an item is
	 * found that is equal to the argument, and removes that item from the list.
	 *
	 * @param object the item to remove
	 * @exception NullPointerException     if there is no item in the receiver
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the object
	 *                                     is null</li>
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
	public void remove(final T object) {
		if (object == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		checkWidget();
		checkNullElement();
		elements.remove(object);
		selection.remove(object);
		refresh();
	}

	/**
	 * Remove all items of the receiver
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 */
	public void removeAll() {
		checkWidget();
		checkNullElement();
		if (elements != null) {
			elements.clear();
		}
		selection.clear();
		refresh();
	}

	/**
	 * @param labelProvider the Label Provider to set
	 */
	public void setLabelProvider(final MultiChoiceLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Sets the selection of the receiver. If the item was already selected, it
	 * remains selected.
	 *
	 * @param selection the new selection
	 * @exception NullPointerException     if there is no item in the receiver
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the
	 *                                     selection is null</li>
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
	public void setSelection(final Set<T> selection) {
		checkWidget();
		checkNullElement();
		if (selection == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.selection = selection;
		updateSelection();
	}

	/**
	 * Selects all selected items in the receiver's list.
	 *
	 * @exception NullPointerException if there is no item in the receiver
	 * @exception SWTException
	 *                                 <ul>
	 *                                 <li>ERROR_WIDGET_DISPOSED - if the receiver
	 *                                 has been disposed</li>
	 *                                 <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                 called from the thread that created the
	 *                                 receiver</li>
	 *                                 </ul>
	 */
	public void selectAll() {
		checkWidget();
		checkNullElement();
		selection.addAll(elements);
		updateSelection();
	}

	/**
	 * Selects the item at the given zero-relative index in the receiver's list. If
	 * the item at the index was already selected, it remains selected.
	 *
	 * @param index the index of the item to select
	 * @exception NullPointerException     if there is no item in the receiver
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
	 */
	public void selectAt(final int index) {
		checkWidget();
		checkNullElement();
		checkRange(index);
		selection.add(elements.get(index));
		updateSelection();
	}

	/**
	 * Selects an item the receiver's list. If the item was already selected, it
	 * remains selected.
	 *
	 * @param index the index of the item to select
	 * @exception NullPointerException     if there is no item in the receiver
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the
	 *                                     selection is null</li>
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
	public void select(final T value) {
		checkWidget();
		checkNullElement();
		if (!elements.contains(value)) {
			throw new IllegalArgumentException("Value not present in the widget");
		}
		selection.add(value);
		updateSelection();
	}

	/**
	 * Selects items in the receiver. If the items were already selected, they
	 * remain selected.
	 *
	 * @param index the indexes of the items to select
	 * @exception NullPointerException     if there is no item in the receiver
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the
	 *                                     selection is null</li>
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
	 */
	public void setSelectedIndex(final int[] index) {
		checkWidget();
		checkNullElement();
		for (final int i : index) {
			checkRange(i);
			selection.add(elements.get(i));
		}
		updateSelection();
	}

	/**
	 * Returns the zero-relative indices of the items which are currently selected
	 * in the receiver. The order of the indices is unspecified. The array is empty
	 * if no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * selection, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the array of indices of the selected items
	 * @exception NullPointerException if there is no item in the receiver
	 * @exception SWTException
	 *                                 <ul>
	 *                                 <li>ERROR_WIDGET_DISPOSED - if the receiver
	 *                                 has been disposed</li>
	 *                                 <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                 called from the thread that created the
	 *                                 receiver</li>
	 *                                 </ul>
	 */
	public int[] getSelectedIndex() {
		checkWidget();
		checkNullElement();
		final List<Integer> selectedIndex = new ArrayList<>();
		for (int i = 0; i < elements.size(); i++) {
			if (selection.contains(elements.get(i))) {
				selectedIndex.add(i);
			}
		}

		final int[] returned = new int[selectedIndex.size()];
		for (int i = 0; i < selectedIndex.size(); i++) {
			returned[i] = selectedIndex.get(i);
		}
		return returned;
	}

	/**
	 * Returns an array of <code>Object</code>s that are currently selected in the
	 * receiver. The order of the items is unspecified. An empty array indicates
	 * that no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its
	 * selection, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return an array representing the selection
	 * @exception NullPointerException if there is no item in the receiver
	 * @exception SWTException
	 *                                 <ul>
	 *                                 <li>ERROR_WIDGET_DISPOSED - if the receiver
	 *                                 has been disposed</li>
	 *                                 <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                 called from the thread that created the
	 *                                 receiver</li>
	 *                                 </ul>
	 */
	public List<T> getSelection() {
		checkWidget();
		checkNullElement();
		return new ArrayList<>(selection);
	}

	/**
	 * Deselects the item at the given zero-relative index in the receiver's list.
	 * If the item at the index was already deselected, it remains deselected.
	 * Indices that are out of range are ignored.
	 *
	 * @param index the index of the item to deselect
	 * @exception NullPointerException     if there is no item in the receiver
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
	 */
	public void deselectAt(final int index) {
		checkWidget();
		checkNullElement();

		if (index < 0 || index >= elements.size()) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}

		selection.remove(index);
		updateSelection();
	}

	/**
	 * Deselects the item in the receiver's list. If the item at the index was
	 * already deselected, it remains deselected.
	 *
	 * @param value the item to deselect
	 * @exception NullPointerException     if there is no item in the receiver
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
	 */
	public void deselect(final T value) {
		checkWidget();
		checkNullElement();
		selection.remove(value);
		updateSelection();
	}

	/**
	 * Deselects all items in the receiver's list.
	 *
	 * @param value the item to deselect
	 * @exception NullPointerException if there is no item in the receiver
	 *
	 * @exception SWTException
	 *                                 <ul>
	 *                                 <li>ERROR_WIDGET_DISPOSED - if the receiver
	 *                                 has been disposed</li>
	 *                                 <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                 called from the thread that created the
	 *                                 receiver</li>
	 *                                 </ul>
	 */
	public void deselectAll() {
		checkWidget();
		checkNullElement();
		selection.clear();
		updateSelection();
	}

	/**
	 * @return the number of columns
	 */
	public int getNumberOfColumns() {
		checkWidget();
		return numberOfColumns;
	}

	/**
	 * @param numberOfColumns the number of columns
	 */
	public void setNumberOfColumns(final int numberOfColumns) {
		checkWidget();
		this.numberOfColumns = numberOfColumns;
		popup.dispose();
		popup = null;
		createPopup();
	}

	/**
	 * @return the separator used in the text field. Default value is ","
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @param separator the new value of the separator
	 */
	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#getForeground()
	 */
	@Override
	public Color getForeground() {
		return foreground;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setForeground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setForeground(final Color foreground) {
		this.foreground = foreground;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#getBackground()
	 */
	@Override
	public Color getBackground() {
		return background;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setBackground(final Color background) {
		this.background = background;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#getFont()
	 */
	@Override
	public Font getFont() {
		return font;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(final Font font) {
		this.font = font;
	}

	/**
	 * Refresh the widget (after the add of a new element for example)
	 */
	public void refresh() {
		checkWidget();
		popup.dispose();
		popup = null;
		createPopup();
		updateSelection();
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		int width = 0, height = 0;

		final GC gc = new GC(text);
		final int spacer = gc.stringExtent(" ").x;
		final int textWidth = gc.stringExtent(text.getText()).x;
		gc.dispose();
		final Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		final Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		final int borderWidth = getBorderWidth();

		height = Math.max(textSize.y, arrowSize.y);
		width = textWidth + 2 * spacer + arrowSize.x + 2 * borderWidth;
		if (wHint != SWT.DEFAULT) {
			width = wHint;
		}
		if (hHint != SWT.DEFAULT) {
			height = hHint;
		}
		return new Point(width + 2 * borderWidth, height + 2 * borderWidth);
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		checkWidget();
		arrow.setEnabled(enabled);
		text.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/**
	 * Sets the editable state.
	 *
	 * @param editable the new editable state
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the receiver</li>
	 *                         </ul>
	 */
	public void setEditable(final boolean editable) {
		checkWidget();
		text.setEditable(editable);
		text.setBackground(null);
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(final String txt) {
		checkWidget();
		text.setToolTipText(txt);
	}

	/**
	 * @return the selection listener
	 */
	public SelectionListener getSelectionListener() {
		checkWidget();
		return selectionListener;
	}

	/**
	 * @param selectionListener the new selection listener
	 */
	public void setSelectionListener(final MultiChoiceSelectionListener<T> selectionListener) {
		checkWidget();
		this.selectionListener = selectionListener;
		refresh();
	}

	/**
	 * Update the selection
	 */
	public void updateSelection() {
		checkWidget();
		if (isDisposed()) {
			return;
		}

		if (popup == null || popup.isDisposed() || checkboxes == null) {
			return;
		}

		for (final Button currentButton : checkboxes) {
			if (!currentButton.isDisposed()) {
				final Object content = currentButton.getData();
				currentButton.setSelection(selection.contains(content));
			}
		}
		setLabel();
	}

	/**
	 * @return the last modified item
	 */
	T getLastModified() {
		return lastModified;
	}

	/**
	 * @return the popup
	 */
	Shell getPopup() {
		return popup;
	}

	/**
	 * Create the popup that contains all checkboxes
	 */
	private void createPopup() {
		popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);

		Composite parent;
		if (showSelectUnselectAll || searchFieldVisible) {
			popup.setLayout(new FillLayout());
			parent = new Composite(popup, SWT.BORDER);
		} else {
			parent = popup;
		}
		final GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginWidth = gridLayout.marginHeight = gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);

		final int[] popupEvents = { SWT.Close, SWT.Deactivate, SWT.Dispose };
		for (final int popupEvent : popupEvents) {
			popup.addListener(popupEvent, listener);
		}

		if (elements == null) {
			return;
		}

		if (searchFieldVisible) {
			final Text searchText = new Text(parent, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			searchText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1));
			searchText.addListener(SWT.DefaultSelection, e -> {
				searchedText = searchText.getText().trim().toLowerCase();
				rebuildCheckboxAfterSearchInputChanged();
			});

		}

		if (showSelectUnselectAll) {
			final Link selectAllLink = new Link(parent, SWT.NONE);
			selectAllLink.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false));
			selectAllLink.setText("<a>" + ResourceManager.getLabel(ResourceManager.SELECT_ALL) + "</a>");
			selectAllLink.addListener(SWT.Selection, e -> {
				selectAll();
			});

			final Link deselectAllLink = new Link(parent, SWT.NONE);
			deselectAllLink.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false));
			deselectAllLink.setText("<a>" + ResourceManager.getLabel(ResourceManager.DESELECT_ALL) + "</a>");
			deselectAllLink.addListener(SWT.Selection, e -> {
				deselectAll();
			});
		}

		scrolledComposite = new ScrolledComposite(parent,
				SWT.V_SCROLL | (showSelectUnselectAll ? SWT.NONE : SWT.BORDER));
		scrolledComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
		final Composite content = new Composite(scrolledComposite, SWT.NONE);
		content.setLayout(new GridLayout(numberOfColumns, true));
		scrolledComposite.setContent(content);
		scrolledComposite.setExpandHorizontal(false);
		scrolledComposite.setExpandVertical(true);

		buildCheckboxes();
		content.pack();
		preferredHeightOfPopup = content.getSize().y;
	}

	private void rebuildCheckboxAfterSearchInputChanged() {
		final Composite content = (Composite) scrolledComposite.getContent();
		try {
			setRedraw(false);
			for (final Control c : content.getChildren()) {
				c.dispose();
			}
			buildCheckboxes();
		} finally {
			setRedraw(true);
			content.pack();
			redraw();
			update();
		}
	}

	private void buildCheckboxes() {
		final Composite content = (Composite) scrolledComposite.getContent();
		if (checkboxes == null) {
			checkboxes = new ArrayList<>(elements.size());
		} else {
			checkboxes.clear();
		}
		for (final T o : elements) {
			final String checkboxLabel = labelProvider.getText(o);

			if (searchedText != null && !searchedText.isEmpty() && //
					checkboxLabel.toLowerCase().indexOf(searchedText) < 0) {
				continue;
			}

			final Button checkBoxButton = new Button(content, SWT.CHECK);

			if (font != null) {
				checkBoxButton.setFont(font);
			}
			if (foreground != null) {
				checkBoxButton.setForeground(foreground);
			}
			if (background != null) {
				checkBoxButton.setBackground(background);
			}
			checkBoxButton.setData(o);
			checkBoxButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
			checkBoxButton.setText(checkboxLabel);
			checkBoxButton.addListener(SWT.Selection, e -> {
				if (checkBoxButton.getSelection()) {
					MultiChoice.this.selection.add(o);
				} else {
					MultiChoice.this.selection.remove(o);
				}
				MultiChoice.this.lastModified = o;
				setLabel();
			});

			if (selectionListener != null) {
				checkBoxButton.addSelectionListener(selectionListener);
			}

			checkBoxButton.setSelection(selection.contains(o));
			checkboxes.add(checkBoxButton);
		}
	}

	/**
	 * Set the value of the label, based on the selected items
	 */
	private void setLabel() {
		if (checkboxes == null) {
			text.setText("");
			return;
		}

		final List<String> values = new ArrayList<>();
		for (final Button current : checkboxes) {
			if (current.getSelection()) {
				values.add(current.getText());
			}
		}

		final StringBuilder sb = new StringBuilder();
		final Iterator<String> it = values.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(separator);
			}
		}

		text.setText(sb.toString());
	}

	/**
	 * Handle a focus event
	 *
	 * @param type type of the event to handle
	 */
	private void handleFocusEvents(final int type) {
		if (isDisposed()) {
			return;
		}
		switch (type) {
		case SWT.FocusIn: {
			if (hasFocus) {
				return;
			}
			hasFocus = true;
			final Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			shell.addListener(SWT.Deactivate, listener);
			final Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			display.addFilter(SWT.FocusIn, filter);
			final Event e = new Event();
			notifyListeners(SWT.FocusIn, e);
			break;
		}
		case SWT.FocusOut: {
			if (!hasFocus) {
				return;
			}
			final Control focusControl = getDisplay().getFocusControl();
			if (focusControl == arrow) {
				return;
			}
			hasFocus = false;
			final Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			final Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			final Event e = new Event();
			notifyListeners(SWT.FocusOut, e);
			break;
		}
		}
	}

	/**
	 * Handle a multichoice event
	 *
	 * @param event event to handle
	 */
	private void handleMultiChoiceEvents(final Event event) {
		switch (event.type) {
		case SWT.Dispose:
			if (popup != null && !popup.isDisposed()) {
				popup.dispose();
			}
			final Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			final Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			popup = null;
			arrow = null;
			break;
		case SWT.Move:
			changeVisibilityOfPopupWindow(false);
			break;
		case SWT.Resize:
			if (isDropped()) {
				changeVisibilityOfPopupWindow(false);
			}
			break;
		}

	}

	/**
	 * Handle a button event
	 *
	 * @param event event to hangle
	 */
	private void handleButtonEvent(final Event event) {
		switch (event.type) {
		case SWT.FocusIn: {
			handleFocusEvents(SWT.FocusIn);
			break;
		}
		case SWT.Selection: {
			changeVisibilityOfPopupWindow(!isDropped());
			break;
		}
		}
	}

	/**
	 * @return <code>true</code> if the popup is visible and not dropped,
	 *         <code>false</code> otherwise
	 */
	private boolean isDropped() {
		return !popup.isDisposed() && popup.getVisible();
	}

	/**
	 * Handle a popup event
	 *
	 * @param event event to handle
	 */
	private void handlePopupEvent(final Event event) {
		switch (event.type) {
		case SWT.Close:
			event.doit = false;
			changeVisibilityOfPopupWindow(false);
			break;
		case SWT.Deactivate:
			changeVisibilityOfPopupWindow(false);
			break;
		case SWT.Dispose:
			if (checkboxes != null) {
				checkboxes.clear();
			}
			checkboxes = null;
			break;
		}
	}

	/**
	 * Display/Hide the popup window
	 *
	 * @param show if <code>true</code>, displays the popup window. If
	 *             <code>false</code>, hide the popup window
	 */
	private void changeVisibilityOfPopupWindow(final boolean show) {
		if (show == isDropped()) {
			return;
		}

		if (!show) {
			popup.setVisible(false);
			if (!isDisposed()) {
				text.setFocus();
			}
			return;
		}

		if (getShell() != popup.getParent()) {
			popup.dispose();
			popup = null;
			createPopup();
		}

		final Point arrowRect = arrow.toDisplay(arrow.getSize().x - 5, arrow.getSize().y + arrow.getBorderWidth() - 3);
		int x = arrowRect.x;
		int y = arrowRect.y;

		final Rectangle displayRect = getMonitor().getClientArea();
		final Rectangle parentRect = getDisplay().map(getParent(), null, getBounds());
		popup.pack();

		final int width = popup.getBounds().width;

		final int maxHeight = 2 * displayRect.height / 3;
		int height = popup.getBounds().height;

		if (height > maxHeight) {
			height = maxHeight;
			popup.setSize(width, height);
			scrolledComposite.setMinHeight(preferredHeightOfPopup);
			popup.layout(true);
		}

		if (y + height > displayRect.y + displayRect.height) {
			y = parentRect.y - height;
			if (y < 0) {
				height += y;
				y = parentRect.y - height + 5;
				popup.setSize(width, height);
				scrolledComposite.setMinHeight(preferredHeightOfPopup);
				popup.layout(true);
			}
		}
		if (x + width > displayRect.x + displayRect.width) {
			x = displayRect.x + displayRect.width - width;
		}

		popup.setLocation(x, y);
		popup.setVisible(true);
		popup.setFocus();
	}

	/**
	 * Check if the elements attributes is not null
	 *
	 * @exception NullPointerException if there is no item in the receiver
	 */
	private void checkNullElement() {
		if (elements == null) {
			throw new NullPointerException("There is no element associated to this widget");
		}
	}

	/**
	 * @param index
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	private void checkRange(final int index) throws NullPointerException {
		checkNullElement();
		if (index < 0 || index >= elements.size()) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}
	}

	/**
	 * Fill the text box. Please notice that the setted element MAY not be in the
	 * items list.<br/>
	 * For instance, your widget contains a list of european countries. If you use
	 * setText("USA"), the text will display "USA", but <code>getSelection()</code>
	 * will return and empty text. To retrieve "USA", you have to use the method
	 * getText();
	 *
	 * @param textValue new text value
	 */
	public void setText(final String textValue) {
		checkWidget();
		checkNullElement();
		if (textValue == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		selection.clear();
		text.setText(textValue);
	}

	/**
	 * @return the display value as text
	 */
	public String getText() {
		checkWidget();
		checkNullElement();
		return text.getText();
	}

	/**
	 * @return <code>true</code> if the hyperlinks "Select all" and "Deselect all"
	 *         are displayted
	 */
	public boolean isShowSelectUnselectAll() {
		checkWidget();
		return showSelectUnselectAll;
	}

	/**
	 * @param showSelectUnselectAll set to "true" to display the hyperlinks "Select
	 *                              all" and "Deselect all"
	 */
	public void setShowSelectUnselectAll(final boolean showSelectUnselectAll) {
		checkWidget();
		this.showSelectUnselectAll = showSelectUnselectAll;
		if (popup != null && !popup.isDisposed()) {
			popup.dispose();
		}
		createPopup();
	}

	/**
	 * @param selection elements to select
	 */
	public void setSelected(final Set<T> selection) {
		setSelection(selection);
	}

	/**
	 * @return selected elements
	 */
	public Set<T> getSelected() {
		return selection;
	}

	/**
	 * @return text control
	 */
	public Text getTextControl() {
		return text;
	}

	/**
	 * @return the arrow button
	 */
	public Button getArrowButton() {
		return arrow;
	}

}
