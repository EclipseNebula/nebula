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
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal;

import org.eclipse.nebula.widgets.grid.AbstractRenderer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;

/**
 * The row header renderer.
 *
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class DefaultRowHeaderRenderer extends AbstractRenderer
{

    int leftMargin = 6;

    int rightMargin = 8;

    int topMargin = 3;

    int bottomMargin = 3;

    private TextLayout textLayout;

	private int truncationStyle =SWT.CENTER;


    /**
     * {@inheritDoc}
     */
    @Override
	public void paint(GC gc, Object value)
    {
        GridItem item = (GridItem) value;

        String text = getHeaderText(item);

		if (getHeaderFont(item) == null) {
			gc.setFont(item.getParent().getFont());
		} else {
			gc.setFont(getHeaderFont(item));
		}

        Color background = getHeaderBackground(item);
        if( background == null ) {
        	background = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        }
        gc.setBackground(background);

        if (isSelected() && item.getParent().getCellSelectionEnabled())
        {
            gc.setBackground(item.getParent().getCellHeaderSelectionBackground());
        }

        gc.fillRectangle(getBounds().x, getBounds().y, getBounds().width, getBounds().height + 1);

        if (!item.getParent().getCellSelectionEnabled())
        {
            if (isSelected())
            {
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
            }
            else
            {
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
            }



            gc.drawLine(getBounds().x, getBounds().y, getBounds().x + getBounds().width - 1,
                        getBounds().y);
            gc.drawLine(getBounds().x, getBounds().y, getBounds().x, getBounds().y + getBounds().height
                                                                     - 1);

            if (!isSelected())
            {
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                gc.drawLine(getBounds().x + 1, getBounds().y + 1,
                            getBounds().x + getBounds().width - 2, getBounds().y + 1);
                gc.drawLine(getBounds().x + 1, getBounds().y + 1, getBounds().x + 1,
                            getBounds().y + getBounds().height - 2);
            }

            if (isSelected())
            {
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
            }
            else
            {
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            }
            gc.drawLine(getBounds().x + getBounds().width - 1, getBounds().y, getBounds().x
                                                                              + getBounds().width - 1,
                        getBounds().y + getBounds().height - 1);
            gc.drawLine(getBounds().x, getBounds().y + getBounds().height - 1, getBounds().x
                                                                               + getBounds().width - 1,
                        getBounds().y + getBounds().height - 1);

            if (!isSelected())
            {
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
                gc.drawLine(getBounds().x + getBounds().width - 2, getBounds().y + 1,
                            getBounds().x + getBounds().width - 2, getBounds().y + getBounds().height
                                                                   - 2);
                gc.drawLine(getBounds().x + 1, getBounds().y + getBounds().height - 2,
                            getBounds().x + getBounds().width - 2, getBounds().y + getBounds().height
                                                                   - 2);
            }
        }
        else
        {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));

            gc.drawLine(getBounds().x + getBounds().width - 1, getBounds().y, getBounds().x
                                                                              + getBounds().width - 1,
                        getBounds().y + getBounds().height - 1);
            gc.drawLine(getBounds().x, getBounds().y + getBounds().height - 1, getBounds().x
                                                                               + getBounds().width - 1,
                        getBounds().y + getBounds().height - 1);
        }

        int x = leftMargin;

        Image image = getHeaderImage(item);

        if( image != null ) {
        	if( isSelected() && !item.getParent().getCellSelectionEnabled() ) {
        		gc.drawImage(image, x + 1, getBounds().y + 1 + (getBounds().height - image.getBounds().height)/2);
        		x += 1;
        	} else {
        		gc.drawImage(image, x, getBounds().y + (getBounds().height - image.getBounds().height)/2);
        	}
        	x += image.getBounds().width + 5;
        }

        int width = getBounds().width - x;

        width -= rightMargin;

        Color foreground = getHeaderForeground(item);
        if( foreground == null ) {
        	foreground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        }

        gc.setForeground(foreground);


        int y = getBounds().y;
        int selectionOffset = 0;
        if (isSelected() && !item.getParent().getCellSelectionEnabled())
        {
            selectionOffset = 1;
        }

        if (!item.getParent().isWordWrapHeader())
        {
            y += (getBounds().height - gc.stringExtent(text).y) / 2;
            gc.drawString(TextUtils.getShortStr(gc, text, width, truncationStyle), getBounds().x + x + selectionOffset, y + selectionOffset, true);
        }
        else
        {
          getTextLayout(gc, item);
          textLayout.setWidth(width < 1 ? 1 : width);
          textLayout.setText(text);

          if (item.getParent().isAutoHeight())
          {
            // Look through all columns to get the max height needed for this item
          int columnCount = item.getParent().getColumnCount();
          int maxHeight = textLayout.getBounds().height + topMargin + bottomMargin;
          for (int i=0; i<columnCount; i++)
          {
            GridColumn column = item.getParent().getColumn(i);
            if (column.getWordWrap())
            {
              int height = column.getCellRenderer().computeSize(gc, column.getWidth(), SWT.DEFAULT, item).y;
              maxHeight = Math.max(maxHeight, height);
            }
          }

          if (maxHeight != item.getHeight())
          {
            item.setHeight(maxHeight);
          }
          }

          textLayout.draw(gc, getBounds().x + x + selectionOffset, y + selectionOffset);
        }

    }

    private Font getHeaderFont(GridItem item){
    	return item.getHeaderFont();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public Point computeSize(GC gc, int wHint, int hHint, Object value)
    {
        GridItem item = (GridItem) value;

        String text = getHeaderText(item);
        Image image = getHeaderImage(item);

        Font previousFont = gc.getFont();
		if (getHeaderFont(item) == null) {
			gc.setFont(item.getParent().getFont());
		} else {
			gc.setFont(getHeaderFont(item));
		}

        int x = leftMargin;

        if( image != null ) {
        	x += image.getBounds().width + 5;
        }

        x += gc.stringExtent(text).x + rightMargin;

        int y = topMargin;

        if( image != null ) {
        	y += Math.max(gc.getFontMetrics().getHeight(),image.getBounds().height);
        } else {
        	y += gc.getFontMetrics().getHeight();
        }


        y += bottomMargin;
        gc.setFont(previousFont);

        return new Point(x, y);
    }

    private Image getHeaderImage(GridItem item) {
    	return item.getHeaderImage();
    }

    private String getHeaderText(GridItem item)
    {
        String text = item.getHeaderText();
        if (text == null)
        {
            text = (item.getRowIndex() + 1) + "";
        }
        return text;
    }

    private Color getHeaderBackground(GridItem item) {
    	return item.getHeaderBackground();
    }

    private Color getHeaderForeground(GridItem item) {
    	return item.getHeaderForeground();
    }

    private void getTextLayout(GC gc, GridItem gridItem)
    {
        if (textLayout == null)
        {
            textLayout = new TextLayout(gc.getDevice());
            textLayout.setFont(gc.getFont());
            gridItem.getParent().addDisposeListener(new DisposeListener()
            {
                @Override
				public void widgetDisposed(DisposeEvent e)
                {
                    textLayout.dispose();
                }
            });
        }
    }

    /**
     * Get the truncation style
     * @return the truncation style.
     */
	public int getTruncationStyle() {
		return truncationStyle;
	}

	/**
	 * Set the truncation style to use when cell content is too large.
	 * @see SWT#LEFT
	 * @see SWT#CENTER
	 * @see SWT#RIGHT
	 * @param truncationStyle the truncation style
	 */
	public void setTruncationStyle(int truncationStyle) {
		this.truncationStyle = truncationStyle;
	}
}
