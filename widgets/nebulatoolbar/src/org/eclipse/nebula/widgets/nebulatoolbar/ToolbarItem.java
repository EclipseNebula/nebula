/*******************************************************************************
 * Copyright (c) 2010, Lukasz Milewski and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Lukasz Milewski <lukasz.milewski@gmail.com> - Initial API and
 * implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nebulatoolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

/**
 * Default ToolbarItem implementation. Draw item using two different styles,
 * Vista and 7.
 * 
 * @author Lukasz Milewski <lukasz.milewski@gmail.com>
 * @since 25 May 2009
 */
public class ToolbarItem extends Item
{

	protected SelectionListener selectionListener;

	protected boolean hovered = false;

	protected Image image;

	protected Menu menu;

	private NebulaToolbar parent;

	protected boolean pushedDown = false;

	protected boolean selected = false;

	protected int style;

	protected String text = "";

	protected String tooltip = "";

	protected int width;

	/**
	 * Parameterized constructor with all fields.
	 * 
	 * @param parent Parent widget, should be NebulaToolbar
	 * @param action Selection listener
	 * @param text Text
	 * @param tooltip Tooltip text
	 * @param image Image
	 * @param style Style of item
	 */
	public ToolbarItem(Widget parent, SelectionListener action, String text, String tooltip, Image image, int style)
	{
		this(parent, style);

		this.selectionListener = action;
		this.text = text;
		this.tooltip = tooltip;
		this.image = image;
		this.style = style;

		((NebulaToolbar) parent).addItem(this);
	}

	/**
	 * Parameterized constructor.
	 * 
	 * @param parent Parent widget, should be NebulaToolbar
	 * @param style Style of item
	 */
	public ToolbarItem(Widget parent, int style)
	{
		super(parent, style);

		this.parent = (NebulaToolbar) parent;
		this.style = style;

		((NebulaToolbar) parent).addItem(this);
	}

	/**
	 * Calculate size of item.
	 */
	public void calculateSize()
	{
		Image calcSizeImage = new Image(Display.getDefault(), 1, 1);

		GC gc = new GC(calcSizeImage);
		gc.setAdvanced(true);
		gc.setAntialias(SWT.OFF);
		gc.setFont(parent.getFont());

		width = 10;

		if (image != null)
		{
			width += image.getImageData().width;
		}

		if (text.length() > 0)
		{
			width += getTextWidth(gc, text) + 9;
		}

		gc.dispose();
		calcSizeImage.dispose();
	}

	/**
	 * Returns context menu.
	 * 
	 * @return Menu
	 */
	public Menu getContextMenu()
	{
		return menu;
	}

	/**
	 * Returns item image.
	 * 
	 * @return Image
	 */
	public Image getImage()
	{
		return image;
	}

	/**
	 * Returns SelectionListener.
	 * TODO should be replaced with array of listeners
	 * 
	 * @return SelectionListener
	 */
	public SelectionListener getSelectionListener()
	{
		return selectionListener;
	}

	/**
	 * Returns style of item.
	 * 
	 * @return Style
	 */
	public int getStyle()
	{
		return style;
	}

	/**
	 * Returns text of item.
	 * 
	 * @return Text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Returns text width.
	 * 
	 * @param text Text
	 * @return Width
	 */
	private int getTextWidth(GC gc, String text)
	{
		if (text.endsWith(" "))
		{
			return gc.textExtent(text).x;
		}

		return gc.textExtent(text).x;
	}

	/**
	 * Returns tooltip text of item.
	 * 
	 * @return Tooltip text
	 */
	public String getTooltip()
	{
		return tooltip;
	}

	/**
	 * Returns width of item.
	 * 
	 * @return Width
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * Returns hovered state of item.
	 * 
	 * @return Hovered state
	 */
	public boolean isHovered()
	{
		return hovered;
	}

	/**
	 * Returns pushed down state of item.
	 * 
	 * @return Pushed down state
	 */
	public boolean isPushedDown()
	{
		return pushedDown;
	}

	/**
	 * Returns selected state of item.
	 * 
	 * @return Selected state
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * Returns visibility of item.
	 * 
	 * @return Visibility
	 */
	public boolean isVisible()
	{
		return true;
	}

	/**
	 * Paint item on graphical canvas.
	 * 
	 * @param gc GC
	 * @param x Left position
	 * @param y Top position
	 */
	public void paint(GC gc, int x, int y)
	{
		if (parent.getMode() == NebulaToolbar.MODE_VISTA)
		{
			paintVista(gc, x, y);
		}
		else
		{
			paintSeven(gc, x, y);
		}
	}

	/**
	 * Paint item on graphical canvas using Windows 7 style.
	 * 
	 * @param gc GC
	 * @param x Left position
	 * @param y Top position
	 */
	private void paintSeven(GC gc, int x, int y)
	{
		int imageX = x + 5;
		int imageY = y + 4;

		int imageWidth = 0;

		if (image != null)
		{
			imageWidth = image.getImageData().width;
		}

		if (pushedDown)
		{
			imageX++;
			imageY++;
		}

		int textX = imageX + imageWidth + 5;
		int textY = imageY;

		Color defaultForegroundColor = gc.getForeground();

		Color darkBorder = new Color(179, 196, 216);
		Color lightBorder = new Color(252, 253, 254);

		if (pushedDown)
		{
			darkBorder.dispose();
			darkBorder = new Color(181, 197, 217);
		}

		if (pushedDown || hovered || selected)
		{
			gc.setAntialias(SWT.ON);

			gc.setForeground(darkBorder);
			gc.drawRoundRectangle(x, y, width - 1, 23, 4, 4);

			if (!pushedDown)
			{
				gc.setForeground(lightBorder);
				gc.drawRoundRectangle(x + 1, y + 1, width - 3 + (pushedDown ? 1 : 0), 21 + (pushedDown ? 1 : 0), 4, 4);
			}

			if (pushedDown || hovered)
			{
				Color c1 = null;
				Color c2 = null;
				Color c3 = null;
				Color c4 = null;

				if (pushedDown)
				{
					c1 = new Color(201, 212, 228);
					c2 = new Color(216, 228, 241);
					c3 = new Color(207, 219, 236);
					c4 = new Color(207, 220, 237);
				}
				else
				{
					c1 = new Color(248, 251, 254);
					c2 = new Color(237, 242, 250);
					c3 = new Color(215, 228, 244);
					c4 = new Color(193, 210, 232);
				}

				int middle = y + 12;

				int pattLeft = x + 1 + (!pushedDown ? 1 : 0);
				int pattTop = y + 1 + (!pushedDown ? 1 : 0);
				int pattWidth = width - (2 + (!pushedDown ? 2 : 0));
				int pattHeight = y + 22 + (pushedDown ? 1 : 0);

				Pattern patternTop = new Pattern(gc.getDevice(), 0, pattTop, 0, middle, c1, c2);
				Pattern patternBtm = new Pattern(gc.getDevice(), 0, middle, 0, pattHeight, c3, c4);

				gc.setBackgroundPattern(patternTop);
				gc.fillRoundRectangle(pattLeft, pattTop, pattWidth, middle, 4, 4);

				gc.setBackgroundPattern(patternBtm);
				gc.fillRoundRectangle(pattLeft, middle, pattWidth, pattHeight - middle, 4, 4);
				gc.fillRectangle(pattLeft, middle, pattWidth, 5);

				gc.setBackgroundPattern(null);

				patternTop.dispose();
				patternBtm.dispose();

				c1.dispose();
				c2.dispose();
				c3.dispose();
				c4.dispose();
			}
		}

		if (image != null)
		{
			gc.drawImage(image, imageX, imageY);
		}

		if (text.length() > 0)
		{
			Font systemFont = null;
			Font defaultFont = gc.getFont();
			boolean adv = gc.getAdvanced();

			if (style == NebulaToolbar.CHEVRON)
			{
				systemFont = new Font(gc.getDevice(), "System", 8, 0); // TODO change that
				gc.setFont(systemFont);
				gc.setAdvanced(false);

				textX -= 1;
				textY -= 2;
			}

			gc.setForeground(defaultForegroundColor); // TODO change that
			gc.drawText(text, textX, textY, true);

			if (systemFont != null)
			{
				gc.setAdvanced(adv);
				gc.setFont(defaultFont);
				systemFont.dispose();
			}
		}

		gc.setForeground(defaultForegroundColor);

		darkBorder.dispose();
		lightBorder.dispose();
	}

	/**
	 * Paint item on graphical canvas using Windows Vista style.
	 * 
	 * @param gc GC
	 * @param x Left position
	 * @param y Top position
	 */
	private void paintVista(GC gc, int x, int y)
	{
		Display display = parent.getDisplay();

		int imageX = x + 5;
		int imageY = y + 4;

		int imageWidth = 0;

		if (image != null)
		{
			imageWidth = image.getImageData().width;
		}

		if (pushedDown)
		{
			imageX++;
			imageY++;
		}

		int textX = imageX + imageWidth + 5;
		int textY = imageY;

		int alphaTop = 75;
		int alphaBottom = 5;

		int borderAlphaDark = 0;
		int borderAlphaLight = 0;

		Color defaultForegroundColor = gc.getForeground();

		Color lightBorder = display.getSystemColor(SWT.COLOR_WHITE);

		if (pushedDown)
		{
			borderAlphaDark = 150;
			borderAlphaLight = 75;

			lightBorder = display.getSystemColor(SWT.COLOR_BLACK);
			alphaTop = 75;
		}
		else if (hovered)
		{
			borderAlphaDark = 100;
			borderAlphaLight = 150;
		}
		else if (selected)
		{
			borderAlphaDark = 65;
			borderAlphaLight = 105;
		}

		if (pushedDown || hovered || selected)
		{
			gc.setAntialias(SWT.ON);

			gc.setAlpha(borderAlphaDark);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.drawRoundRectangle(x, y, width - 1, 23, 4, 4);

			gc.setAlpha(borderAlphaLight);
			gc.setForeground(lightBorder);
			gc.drawRoundRectangle(x + 1, y + 1, width - 3 + (pushedDown ? 1 : 0), 21 + (pushedDown ? 1 : 0), 4, 4);

			gc.setAlpha(255);

			if (pushedDown || hovered)
			{
				int pattHeight = y + 22;

				if (pushedDown)
				{
					pattHeight = y + 16;
				}

				Pattern pattern = new Pattern(gc.getDevice(), 0, y + 2, 0, pattHeight, lightBorder, alphaTop, lightBorder, alphaBottom);

				gc.setBackgroundPattern(pattern);
				gc.fillRoundRectangle(x + 2, y + 2, width - 4 + (pushedDown ? 1 : 0), pattHeight - 5, 4, 4);
				gc.setBackgroundPattern(null);

				pattern.dispose();
			}
		}

		if (image != null)
		{
			gc.drawImage(image, imageX, imageY);
		}

		if (text.length() > 0)
		{
			Font systemFont = null;
			Font defaultFont = gc.getFont();

			if (style == NebulaToolbar.CHEVRON)
			{
				systemFont = new Font(gc.getDevice(), "System", 8, 0); // TODO change that
				gc.setFont(systemFont);

				textX -= 1;
				textY -= 2;
			}

			gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
			gc.drawText(text, textX, textY, true);

			if (systemFont != null)
			{
				gc.setFont(defaultFont);
				systemFont.dispose();
			}
		}

		gc.setForeground(defaultForegroundColor);
	}

	/**
	 * Set SelectionListener
	 * TODO should be replaced with array of listeners
	 * 
	 * @param selectionListener SelectionListener
	 */
	public void setSelectionListener(SelectionListener selectionListener)
	{
		this.selectionListener = selectionListener;
	}

	/**
	 * Sets context menu.
	 * 
	 * @param menu New context menu
	 */
	public void setContextMenu(Menu menu)
	{
		this.menu = menu;
	}

	/**
	 * Sets hovered state of item.
	 * 
	 * @param hovered New hovered state
	 */
	public void setHovered(boolean hovered)
	{
		this.hovered = hovered;
	}

	/**
	 * Sets image.
	 * 
	 * @param image New image
	 */
	public void setImage(Image image)
	{
		this.image = image;

		calculateSize();
	}

	/**
	 * Sets pushed down state of item.
	 * 
	 * @param pushedDown New pushed down state
	 */
	public void setPushedDown(boolean pushedDown)
	{
		this.pushedDown = pushedDown;
	}

	/**
	 * Sets selected state of item.
	 * 
	 * @param selected New selected state
	 */
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	/**
	 * Sets style of item.
	 * 
	 * @param style New style
	 */
	public void setStyle(int style)
	{
		this.style = style;
	}

	/**
	 * Sets text.
	 * 
	 * @param text New text
	 */
	public void setText(String text)
	{
		this.text = text;

		calculateSize();
	}

	/**
	 * Sets tooltip text of item.
	 * 
	 * @param tooltip New tooltip text
	 */
	public void setTooltip(String tooltip)
	{
		this.tooltip = tooltip;
	}

	/**
	 * Sets visibility of item.
	 * TODO visibility state is not currently handled.
	 * 
	 * @param visible New visibility
	 */
	public void setVisible(boolean visible)
	{
		// TODO Should and will be implemented along JFace stuff
	}

}
