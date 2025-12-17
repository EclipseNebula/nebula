/*******************************************************************************
 * Copyright (c) 2014 Mirko Paturzo (Exeura srl).
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mirko Paturzo - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Manages the visual data of the {@link GridItem}s. With this interface it is
 * possible to create your own data visualization manager. With an instance of
 * this class it is much easier to avoid memory leaks by using background colors
 * and fonts.
 * 
 * @author Mirko Paturzo <mirko.paturzo@exeura.eu>
 *
 */
public interface DataVisualizer {

	/**
	 * Inserts a column on DataVisualizer
	 * 
	 * @param columnIndex the index of the column to add
	 */
	void addColumn(int columnIndex);

	/**
	 * Clear all data on {@link DataVisualizer}
	 */
	void clearAll();

	/**
	 * Clear column dataVisualizer values
	 * 
	 * @param columnIndex the index of the column to clear
	 */
	void clearColumn(int columnIndex);

	/**
	 * Clear rows dataVisualizer values
	 * 
	 * @param gridItem the grid item whose row data should be cleared
	 */
	void clearRow(GridItem gridItem);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return {@link Color}
	 * @see GridItem#getBackground(int)
	 */
	Color getBackground(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return boolean
	 * @see GridItem#getCheckable(int)
	 */
	boolean getCheckable(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return boolean
	 * @see GridItem#getChecked(int)
	 */
	boolean getChecked(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return int
	 * @see GridItem#getColumnSpan(int)
	 */
	int getColumnSpan(GridItem gridItem, int columnIndex);

	/**
	 * @return default background
	 */
	Color getDefaultBackground();

	/**
	 * @return default font
	 */
	Font getDefaultFont();

	/**
	 * @return default foreground
	 */
	Color getDefaultForeground();

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return {@link Font}
	 * @see GridItem#getFont(int)
	 */
	Font getFont(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return {@link Color}
	 * @see GridItem#getForeground(int)
	 */
	Color getForeground(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return boolean
	 * @see GridItem#getGrayed(int)
	 */
	boolean getGrayed(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return {@link Image}
	 * @see GridItem#getImage(int)
	 */
	Image getImage(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return int
	 * @see GridItem#getRowSpan(int)
	 */
	int getRowSpan(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return {@link String}
	 * @see GridItem#getText(int)
	 */
	String getText(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @return String
	 * @see GridItem#getToolTipText(int)
	 */
	String getToolTipText(GridItem gridItem, int columnIndex);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param color the background color to set
	 * @see GridItem#setBackground(int, Color)
	 */
	void setBackground(GridItem gridItem, int columnIndex, Color color);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param checked true if the cell should be checkable, false otherwise
	 * @see GridItem#setCheckable(int, boolean)
	 */
	void setCheckable(GridItem gridItem, int columnIndex, boolean checked);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param checked true if the cell should be checked, false otherwise
	 * @see GridItem#setChecked(int, boolean)
	 */
	void setChecked(GridItem gridItem, int columnIndex, boolean checked);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param span the number of columns to span
	 * @see GridItem#setColumnSpan(int, int)
	 */
	void setColumnSpan(GridItem gridItem, int columnIndex, int span);

	/**
	 * set default background
	 * 
	 * @param defaultBackground the default background color
	 */
	void setDefaultBackground(Color defaultBackground);

	/**
	 * set default font
	 * 
	 * @param defaultFont the default font
	 */
	void setDefaultFont(Font defaultFont);

	/**
	 * set default foreground
	 * 
	 * @param defaultForeground the default foreground color
	 */
	void setDefaultForeground(Color defaultForeground);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param font the font to set
	 * @see GridItem#setFont(int, Font)
	 */
	void setFont(GridItem gridItem, int columnIndex, Font font);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param foreground the foreground color to set
	 * @see GridItem#setForeground(int, Color)
	 */
	void setForeground(GridItem gridItem, int columnIndex, Color foreground);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param grayed true if the cell should be grayed, false otherwise
	 * @see GridItem#setGrayed(int, boolean)
	 */
	void setGrayed(GridItem gridItem, int columnIndex, boolean grayed);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param image the image to set
	 * @see GridItem#setImage(int, Image)
	 */
	void setImage(GridItem gridItem, int columnIndex, Image image);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param span the number of rows to span
	 * @see GridItem#setRowSpan(int, int)
	 */
	void setRowSpan(GridItem gridItem, int columnIndex, int span);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param text the text to set
	 * @see GridItem#setText(int, String)
	 */
	void setText(GridItem gridItem, int columnIndex, String text);

	/**
	 * Method substitute GridItem method
	 * 
	 * @param gridItem the grid item
	 * @param columnIndex the column index
	 * @param tooltip the tooltip text to set
	 * @see GridItem#setToolTipText(int, String)
	 */
	void setToolTipText(GridItem gridItem, int columnIndex, String tooltip);

}
