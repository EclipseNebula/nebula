/*******************************************************************************
* Copyright (c) 2010, Lukasz Milewski and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Lukasz Milewski <lukasz.milewski@gmail.com> - Initial API and implementation
*******************************************************************************/
package org.eclipse.nebula.widgets.nebulatoolbar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Toolbar widget. Imitates Windows Vista/7 toolbar found in explorer
 * application.
 * 
 * @author Lukasz Milewski <lukasz.milewski@gmail.com>
 * @since 04 June 2009
 */
public class NebulaToolbar extends Canvas
{

	public static final int CHEVRON = 1;

	private static final int INDENT = 3;

	public static final int MODE_SEVEN = 1;

	public static final int MODE_VISTA = 0;

	private static String CHEVRON_CHARACTERS = String.valueOf((char) 0x27EB + "" + (char) 0x27EB);

	private boolean active = false;

	private boolean buttonPushed = false;

	private boolean chevronAdded = false;

	private int chevronIndex = -1;

	private int chevronPosition = 0;

	private List<Integer> hiddenItemsList = new LinkedList<Integer>();

	private ToolbarItem items[] = new ToolbarItem[]{};

	private int itemUnderCursor = -1;

	private int mode = MODE_VISTA;

	private int rightAlignedIndex = -1;

	private int selectedItemIndex = -1;

	/**
	 * Parameterized constructor.
	 * 
	 * @param parent Parent widget
	 * @param style Style
	 */
	public NebulaToolbar(Composite parent, int style)
	{
		super(parent, style | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);

		addInternalFocusListener();
		addInternalKeyListener();
		addInternalMouseListener();
		addInternalPaintListener();
	}

	/**
	 * Adds chevron item.
	 */
	private void addChevron()
	{
		chevronAdded = true;
		chevronIndex = items.length;

		ToolbarItem item = new ToolbarItem(this, CHEVRON);
		item.setText(CHEVRON_CHARACTERS);
		item.setSelectionListener(new ChevronAction());
	}

	/**
	 * Adds internal focus listener.
	 */
	private void addInternalFocusListener()
	{
		addFocusListener(new FocusListener()
		{

			public void focusGained(FocusEvent e)
			{
				active = true;

				if (selectedItemIndex >= 0)
				{
					items[selectedItemIndex].setSelected(active);
				}

				redraw();
			}

			public void focusLost(FocusEvent e)
			{
				active = false;

				if (selectedItemIndex >= 0)
				{
					items[selectedItemIndex].setSelected(active);
				}

				redraw();
			}

		});
	}

	/**
	 * Adds internal key listener.
	 */
	private void addInternalKeyListener()
	{
		addKeyListener(new KeyListener()
		{

			public void keyPressed(KeyEvent e)
			{
				handleKeyPressed(e);
			}

			public void keyReleased(KeyEvent e)
			{
				handleKeyReleased(e);
			}

		});
	}

	/**
	 * Adds internal mouse listeners.
	 */
	private void addInternalMouseListener()
	{
		addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseDown(MouseEvent e)
			{
				handleMouseDown(e);
			}

			@Override
			public void mouseUp(MouseEvent e)
			{
				handleMouseUp(e);
			}

		});

		addMouseTrackListener(new MouseTrackAdapter()
		{

			@Override
			public void mouseExit(MouseEvent e)
			{
				handleMouseHover(e);
			}

		});

		addMouseMoveListener(new MouseMoveListener()
		{

			public void mouseMove(MouseEvent e)
			{
				handleMouseHover(e);
			}

		});
	}

	/**
	 * Add internal paint listener.
	 */
	private void addInternalPaintListener()
	{
		addListener(SWT.Paint, new Listener()
		{

			public void handleEvent(Event e)
			{
				paint(e.gc);
			}

		});
	}

	/**
	 * Add item to toolbar as last.
	 * 
	 * @param item ToolbarItem
	 */
	public void addItem(ToolbarItem item)
	{
		addItem(item, getItemCount());
	}

	/**
	 * Add item to toolbar on specific position.
	 * 
	 * @param item ToolbarItem
	 * @param position Position
	 */
	public void addItem(ToolbarItem item, int position)
	{
		checkWidget();

		item.calculateSize();

		items = addItem(items, item, position);
	}

	/**
	 * Generates false mouse event.
	 * 
	 * @return False mouse event
	 */
	private MouseEvent generateFalseMouseEvent()
	{
		if (isDisposed())
		{
			return null;
		}

		Point cursPos = Display.getDefault().getCursorLocation();
		Point contPos = toControl(cursPos);

		Event event = new Event();
		event.display = Display.getDefault();
		event.widget = this;

		MouseEvent mouseEvent = new MouseEvent(event);
		mouseEvent.x = contPos.x;
		mouseEvent.y = contPos.y;

		return mouseEvent;
	}

	/**
	 * Returns items count.
	 * 
	 * @return Items count
	 */
	public int getItemCount()
	{
		checkWidget();

		if (items == null)
		{
			return 0;
		}

		return items.length;
	}

	/**
	 * Returns index of item currently under specific position. If no item
	 * found, -1 will be returned.
	 * 
	 * @param x Left position
	 * @param y Top position
	 * @return Item index
	 */
	private int getItemIndexUnderCursor(int x, int y)
	{
		int[] visibleItems = getVisibleItems();
		int start = INDENT;

		for (int i : visibleItems)
		{
			ToolbarItem item = items[i];

			int left = start;

			if (i == rightAlignedIndex)
			{
				left = getClientArea().width - INDENT - item.getWidth();
			}
			else
			{
				start += item.getWidth();
			}

			int end = left + item.getWidth();

			if (x >= left && x < end && y >= INDENT && y <= getClientArea().height - INDENT)
			{
				return i;
			}

		}

		return -1;
	}

	/**
	 * Returns mode of widget (Vista/7)
	 * 
	 * @return Mode
	 */
	public int getMode()
	{
		return mode;
	}

	/**
	 * Returns visibile items.
	 * 
	 * @return Array of indexes
	 */
	private int[] getVisibleItems()
	{
		return getVisibleItems(false);
	}

	/**
	 * Returns visible items, additionally can collect hidden items to list.
	 * 
	 * @param collectHidden Collect hidden items
	 * @return Array of indexes
	 */
	private int[] getVisibleItems(boolean collectHidden)
	{
		if (items == null)
		{
			return null;
		}

		if (collectHidden)
		{
			hiddenItemsList.clear();
		}

		int index = 0;
		int hiddenItems = 0;

		int currentWidth = INDENT;
		int maxWidth = getClientArea().width - INDENT;

		if (rightAlignedIndex != -1)
		{
			maxWidth -= items[rightAlignedIndex].getWidth();
		}

		if (chevronAdded)
		{
			maxWidth -= items[chevronIndex].getWidth();
		}

		int lastItemIndex = -1;

		List<Integer> list = new ArrayList<Integer>();

		while (index < items.length)
		{
			ToolbarItem item = items[index];

			if (!item.isVisible())
			{
				index++;

				continue;
			}

			if (rightAlignedIndex == index)
			{
				lastItemIndex = index;
			}
			else
			{
				currentWidth += item.getWidth();

				if (chevronAdded && index > items.length - 4 && index < items.length - 2 && item.getStyle() != CHEVRON)
				{
					ToolbarItem nextItem;

					if (rightAlignedIndex == index + 1)
					{
						nextItem = items[index + 2];
					}
					else
					{
						nextItem = items[index + 1];
					}

					if (nextItem.getStyle() == CHEVRON)
					{
						maxWidth += nextItem.getWidth();
					}
				}

				if (currentWidth > maxWidth && item.getStyle() != CHEVRON)
				{
					if (collectHidden)
					{
						hiddenItemsList.add(index);
					}

					index++;
					hiddenItems++;

					continue;
				}

				list.add(index);
			}

			index++;
		}

		if (hiddenItems > 0 && !chevronAdded)
		{
			addChevron();

			list.add(items.length - 1);
		}
		else if (hiddenItems == 0 && chevronAdded)
		{
			removeChevron();

			if (itemUnderCursor == chevronIndex)
			{
				itemUnderCursor = -1;
			}

			list.remove(list.indexOf(chevronIndex));
			chevronIndex = -1;
		}

		if (lastItemIndex != -1)
		{
			list.add(lastItemIndex);
		}

		int[] result = new int[list.size()];

		for (int i = 0; i < list.size(); i++)
		{
			result[i] = list.get(i);
		}

		return result;
	}

	/**
	 * Handles key pressed event
	 * 
	 * @param e KeyEvent
	 */
	private void handleKeyPressed(KeyEvent e)
	{
		if (e.stateMask != 0)
		{
			return;
		}

		int newSelectedItemIndex = -1;
		int[] visibleItems = getVisibleItems();

		if (visibleItems.length == 0)
		{
			return;
		}

		if (selectedItemIndex == -1)
		{
			if (e.keyCode == SWT.ARROW_LEFT)
			{
				newSelectedItemIndex = visibleItems[0];
			}
			else if (e.keyCode == SWT.ARROW_RIGHT)
			{
				newSelectedItemIndex = visibleItems[visibleItems.length - 1];
			}
		}
		else
		{
			int visibleSelectedIndex = -1;

			for (int i = 0; i < visibleItems.length; i++)
			{
				if (visibleItems[i] == selectedItemIndex)
				{
					visibleSelectedIndex = i;
				}
			}

			if (e.keyCode == SWT.ARROW_LEFT)
			{
				int newSelectedVisibleIndex = visibleSelectedIndex - 1;

				if (newSelectedVisibleIndex < 0)
				{
					newSelectedVisibleIndex = visibleItems.length - 1;
				}

				newSelectedItemIndex = visibleItems[newSelectedVisibleIndex];
			}
			else if (e.keyCode == SWT.ARROW_RIGHT)
			{
				int newSelectedVisibleIndex = visibleSelectedIndex + 1;

				if (newSelectedVisibleIndex > visibleItems.length - 1)
				{
					newSelectedVisibleIndex = 0;
				}

				newSelectedItemIndex = visibleItems[newSelectedVisibleIndex];
			}
			else if (e.keyCode == ' ' || e.keyCode == SWT.CR || e.keyCode == SWT.LF || e.keyCode == SWT.KEYPAD_CR)
			{
				// TODO TBD
				items[selectedItemIndex].setPushedDown(true);
				items[selectedItemIndex].setHovered(true);

				redraw();

				return;
			}
			else
			{
				return;
			}
		}

		if (selectedItemIndex == newSelectedItemIndex)
		{
			return;
		}

		if (selectedItemIndex >= 0)
		{
			items[selectedItemIndex].setSelected(false);
		}

		items[newSelectedItemIndex].setSelected(true);
		selectedItemIndex = newSelectedItemIndex;

		redraw();
	}

	/**
	 * Handles key released event.
	 * 
	 * @param e KeyEvent
	 */
	private void handleKeyReleased(KeyEvent e)
	{
		if (selectedItemIndex == -1)
		{
			return;
		}

		if (e.keyCode == ' ' || e.keyCode == SWT.CR || e.keyCode == SWT.LF || e.keyCode == SWT.KEYPAD_CR)
		{
			items[selectedItemIndex].setPushedDown(false);
			items[selectedItemIndex].setHovered(false);

			redraw();

			SelectionListener selectionListener = items[selectedItemIndex].getSelectionListener();

			if (selectionListener == null)
			{
				return;
			}

			Event event = new Event();
			event.widget = this;

			selectionListener.widgetSelected(new SelectionEvent(event));
		}
	}

	/**
	 * Handles mouse down event.
	 * 
	 * @param e MouseEvent
	 */
	private void handleMouseDown(MouseEvent e)
	{
		if (e.x < 0 || e.y < 0 || e.x > getClientArea().width || e.y > getClientArea().height || itemUnderCursor == -1)
		{
			return;
		}

		if (selectedItemIndex >= 0)
		{
			items[selectedItemIndex].setHovered(false);
			items[selectedItemIndex].setPushedDown(false);
			items[selectedItemIndex].setSelected(false);
		}

		buttonPushed = true;

		selectedItemIndex = getItemIndexUnderCursor(e.x, e.y);

		if (selectedItemIndex >= 0)
		{
			items[selectedItemIndex].setPushedDown(true);
		}

		redraw();
	}

	/**
	 * Handles mouse hover event.
	 * 
	 * @param e MouseEvent
	 */
	private void handleMouseHover(MouseEvent e)
	{
		int oldItem = itemUnderCursor;

		if (e.x < 0 || e.y < 0 || e.x > getClientArea().width || e.y > getClientArea().height)
		{
			itemUnderCursor = -1;
		}
		else
		{
			itemUnderCursor = getItemIndexUnderCursor(e.x, e.y);
		}

		if (buttonPushed)
		{
			if (selectedItemIndex >= 0)
			{
				items[selectedItemIndex].setHovered(true);
				items[selectedItemIndex].setPushedDown(selectedItemIndex == itemUnderCursor);
			}

			redraw();

			return;
		}

		if (oldItem != itemUnderCursor)
		{
			if (oldItem >= 0)
			{
				items[oldItem].setHovered(false);
				items[oldItem].setPushedDown(false);
			}

			if (itemUnderCursor >= 0)
			{
				items[itemUnderCursor].setHovered(true);

				if (buttonPushed)
				{
					items[itemUnderCursor].setPushedDown(true);
				}
			}

			redraw();
		}
	}

	/**
	 * Handles mouse up event.
	 * 
	 * @param e MouseEvent
	 */
	private void handleMouseUp(MouseEvent e)
	{
		if (!buttonPushed)
		{
			return;
		}

		buttonPushed = false;

		if (selectedItemIndex == -1)
		{
			return;
		}

		if (selectedItemIndex != itemUnderCursor)
		{
			items[selectedItemIndex].setHovered(false);
		}

		items[selectedItemIndex].setSelected(true);

		if (e.x < 0 || e.y < 0 || e.x > getClientArea().width || e.y > getClientArea().height || itemUnderCursor == -1)
		{
			if (selectedItemIndex != itemUnderCursor)
			{
				redraw();
			}

			return;
		}

		items[selectedItemIndex].setPushedDown(false);
		items[itemUnderCursor].setHovered(true);

		redraw();

		int selectedItemIndexFromUp = getItemIndexUnderCursor(e.x, e.y);
		if (selectedItemIndexFromUp != selectedItemIndex)
		{
			return;
		}

		SelectionListener selectionListener = items[selectedItemIndex].getSelectionListener();

		if (selectionListener == null)
		{
			return;
		}

		Event event = new Event();
		event.widget = this;

		selectionListener.widgetSelected(new SelectionEvent(event));

		MouseEvent newEvent = generateFalseMouseEvent();
		if (newEvent != null && getItemIndexUnderCursor(newEvent.x, newEvent.y) != selectedItemIndexFromUp)
		{
			items[selectedItemIndex].setHovered(false);
			redraw();
		}
	}

	/**
	 * Paint widget on graphical canvas.
	 * 
	 * @param gc GC
	 */
	private void paint(GC gc)
	{
		if (mode == MODE_VISTA)
		{
			paintVista(gc);
		}
		else if (mode == MODE_SEVEN)
		{
			paintSeven(gc);
		}

		paintItems(gc);
	}

	/**
	 * Paint items on graphical canvas.
	 * 
	 * @param gc GC
	 */
	private void paintItems(GC gc)
	{
		int[] visibleItems = getVisibleItems();

		int x = INDENT;
		int y = INDENT;

		for (int i = 0; i < visibleItems.length; i++)
		{
			ToolbarItem item = items[visibleItems[i]];

			if (visibleItems[i] == rightAlignedIndex)
			{
				item.paint(gc, getClientArea().width - INDENT - item.getWidth(), y);

				continue;
			}

			if (item.getStyle() == CHEVRON)
			{
				chevronPosition = x;
			}

			item.paint(gc, x, y);
			x += item.getWidth();
		}
	}

	/**
	 * Paint widgets using Windows Seven mode on graphical canvas.
	 * 
	 * @param gc GC
	 */
	private void paintSeven(GC gc)
	{
		Color defaultForeground = gc.getForeground();
		Color defaultBackground = gc.getBackground();

		Rectangle rect = getClientArea();
		Device device = gc.getDevice();

		Color c1 = new Color(249, 252, 255);
		Color c2 = new Color(230, 240, 250);
		Color c3 = new Color(220, 230, 244);
		Color c4 = new Color(221, 233, 247);

		Color ca = new Color(205, 218, 234);
		Color cb = new Color(160, 175, 195);

		int middle = (int) Math.ceil(rect.height / 2);

		Pattern patternBg1 = new Pattern(device, 0, 0, 0, middle, c1, c2);
		gc.setBackgroundPattern(patternBg1);
		gc.fillRectangle(new Rectangle(0, 0, rect.width, middle));
		gc.setBackgroundPattern(null);

		Pattern patternBg2 = new Pattern(device, 0, middle, 0, rect.height - middle, c3, c4);
		gc.setBackgroundPattern(patternBg2);
		gc.fillRectangle(new Rectangle(0, middle, rect.width, rect.height - middle));
		gc.setBackgroundPattern(null);

		gc.setForeground(ca);
		gc.drawLine(0, rect.height - 2, rect.width - 1, rect.height - 2);

		gc.setForeground(cb);
		gc.drawLine(0, rect.height - 1, rect.width - 1, rect.height - 1);

		gc.setForeground(defaultForeground);
		gc.setBackground(defaultBackground);
		gc.setAlpha(255);

		c1.dispose();
		c2.dispose();
		c3.dispose();
		c4.dispose();

		ca.dispose();
		cb.dispose();
	}

	/**
	 * Paint widget using Windows Vista mode on graphical canvas.
	 * 
	 * @param gc GC
	 */
	private void paintVista(GC gc)
	{
		Color defaultForeground = gc.getForeground();
		Color defaultBackground = gc.getBackground();

		Rectangle rect = getClientArea();
		Device device = gc.getDevice();

		Color c1 = new Color(5, 72, 117);
		Color c2 = new Color(25, 108, 119);
		Color c3 = new Color(28, 122, 134);
		Color wh = getDisplay().getSystemColor(SWT.COLOR_WHITE);

		int middle = (int) Math.ceil(rect.height / 2);

		Pattern patternBg1 = new Pattern(device, 0, 0, rect.width, middle, c1, 255, c3, 255);
		gc.setBackgroundPattern(patternBg1);
		gc.fillRectangle(new Rectangle(0, 0, rect.width, middle));
		gc.setBackgroundPattern(null);

		Pattern patternBg2 = new Pattern(device, 0, middle, rect.width, rect.height - middle, c1, 255, c2, 255);
		gc.setBackgroundPattern(patternBg2);
		gc.fillRectangle(new Rectangle(0, middle, rect.width, rect.height - middle));
		gc.setBackgroundPattern(null);

		Pattern patternTopGrad = new Pattern(device, 0, 0, 0, middle, wh, 120, wh, 50);
		gc.setBackgroundPattern(patternTopGrad);
		gc.fillRectangle(new Rectangle(0, 0, rect.width, middle));
		gc.setBackgroundPattern(null);

		Pattern patternBtmGrad = new Pattern(device, 0, middle + 5, 0, rect.height, c1, 0, wh, 125);
		gc.setBackgroundPattern(patternBtmGrad);
		gc.fillRectangle(new Rectangle(0, middle + 5, rect.width, rect.height));
		gc.setBackgroundPattern(null);

		gc.setAlpha(125);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.drawPolygon(new int[]{0, 0, rect.width - 1, 0, rect.width - 1, rect.height - 2, 0, rect.height - 2});

		gc.setAlpha(200);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.drawLine(0, rect.height - 1, rect.width - 1, rect.height - 1);

		gc.setForeground(defaultForeground);
		gc.setBackground(defaultBackground);
		gc.setAlpha(255);

		c1.dispose();
		c2.dispose();
		c3.dispose();

		patternBg1.dispose();
		patternBg2.dispose();
		patternTopGrad.dispose();
		patternBtmGrad.dispose();
	}

	/**
	 * Removes chevron item.
	 */
	private void removeChevron()
	{
		removeItem(chevronIndex);

		if (selectedItemIndex == chevronIndex)
		{
			selectedItemIndex = -1;
		}

		chevronAdded = false;
	}

	/**
	 * Removes item on specific position.
	 * 
	 * @param position Position to remove
	 */
	public void removeItem(int position)
	{
		items = removeItem(items, position);
	}

	/**
	 * Sets widget mode.
	 * 
	 * @param mode Mode
	 */
	public void setMode(int mode)
	{
		this.mode = mode;

		redraw();
	}

	/**
	 * Sets right-aligned item.
	 * 
	 * @param index Right-aligned item index
	 */
	public void setRightAligned(int index)
	{
		rightAlignedIndex = index;

		redraw();
	}

	/**
	 * Chevron Action
	 */
	private class ChevronAction implements SelectionListener
	{

		public void widgetSelected(SelectionEvent e)
		{
			getVisibleItems(true);

			final Menu menu = new Menu(getShell(), SWT.POP_UP);
			menu.addMenuListener(new MenuAdapter()
			{

				@Override
				public void menuShown(MenuEvent e)
				{
					items[selectedItemIndex].setHovered(false);
					items[selectedItemIndex].setSelected(true);

					redraw();
				}

			});

			for (int index : hiddenItemsList)
			{
				ToolbarItem item = items[index];

				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(item.getText());
				menuItem.setImage(item.getImage());

				if (item.getSelectionListener() != null)
				{
					menuItem.addSelectionListener(item.getSelectionListener());
				}
			}

			Point pos = toDisplay(chevronPosition, getClientArea().height);
			pos.y -= 3;

			menu.setLocation(pos.x, pos.y);
			menu.setVisible(true);
		}

		public void widgetDefaultSelected(SelectionEvent e)
		{
		}

	}

	/**
	 * Add object to array on specific index.
	 * 
	 * @param <T> Type of objects
	 * @param array Array of T objects
	 * @param object Object
	 * @param index Index
	 * @return Modified array of T objects
	 */
	private <T> T[] addItem(T[] array, T object, int index)
	{
		int length = 0;

		if (array == null)
		{
			return null;
		}

		length = array.length;

		@SuppressWarnings("unchecked")
		T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), length + 1);

		if (array != null)
		{
			System.arraycopy(array, 0, newArray, 0, length);
		}

		if (index != -1)
		{
			for (int i = newArray.length - 2; i >= index; i--)
			{
				if (i >= 0)
				{
					newArray[i + 1] = newArray[i];
				}
			}

			newArray[index] = object;
		}
		else
		{
			newArray[newArray.length - 1] = object;
		}

		return newArray;
	}

	/**
	 * Removes object from array on specific index.
	 * 
	 * @param <T> Type of objects
	 * @param array Array of T objects
	 * @param index Index
	 * @return Modified array of T objects
	 */
	private <T> T[] removeItem(T[] array, int index)
	{
		if (array == null)
		{
			return null;
		}

		@SuppressWarnings("unchecked")
		T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);

		if (index > 0)
		{
			System.arraycopy(array, 0, newArray, 0, index);
		}

		if (index + 1 < array.length)
		{
			System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
		}

		return newArray;
	}

}
