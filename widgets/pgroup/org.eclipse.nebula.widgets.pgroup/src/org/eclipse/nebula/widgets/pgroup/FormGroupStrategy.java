/*******************************************************************************
 * Copyright (c) 2006 Chris Gross.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0 Contributors: schtoo@schtoo.com
 * (Chris Gross) - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.pgroup;

import org.eclipse.nebula.widgets.pgroup.internal.GraphicUtils;
import org.eclipse.nebula.widgets.pgroup.internal.TextUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

/**
 * FormGroupStrategy makes a PGroup mimic the look and feel of an Eclipse Form
 * Section.
 *
 * @since 1.0
 * @author chris
 */
public class FormGroupStrategy extends AbstractGroupStrategy
{

    private Color initialBackColor;

    private Color initialBorderColor;

    private int titleTextMargin = 2;

    private int betweenSpacing = 6;

    private int margin = 0;

    private int vMargin = 2;

    private int hMargin = 6;

    private Color borderColor;

    private int titleHeight;

    private int textWidth;

    private int fontHeight;

    private Rectangle toolItemArea;

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#initialize()
     */
    public void initialize()
    {
        super.initialize();

        RGB borderRGB = GraphicUtils.blend(getGroup().getDisplay()
            .getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT).getRGB(), getGroup()
            .getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB(), 100);
        initialBorderColor = new Color(getGroup().getDisplay(), borderRGB);
        borderColor = initialBorderColor;

        RGB backRGB = GraphicUtils.blend(getGroup().getDisplay()
            .getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT).getRGB(), getGroup().getDisplay()
            .getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB(), 40);
        initialBackColor = new Color(getGroup().getDisplay(), backRGB);

    }

    /**
     * @deprecated use constructor with PGroup element instead
     */
    public FormGroupStrategy() {
    	this(null);
    }
    
    /**
     * Creates a FormGroupStrategy with the given toggle and style.
     *
     * @param toggle
     * @param style
     */
    public FormGroupStrategy(PGroup g)
    {
        super(g);
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#paint(org.eclipse.swt.graphics.GC)
     */
    public void paint(GC gc)
    {
        Color back = getGroup().internalGetBackground();
        if (back != null)
        {
            gc.fillRectangle(0,0,getGroup().getSize().x,getGroup().getSize().y);

            Region reg = new Region();
            reg.add(0, 0, 5, 1);
            reg.add(0, 1, 3, 1);
            reg.add(0, 2, 2, 1);
            reg.add(0, 3, 1, 1);
            reg.add(0, 4, 1, 1);

            reg.add(getGroup().getSize().x - 5, 0, 5, 1);
            reg.add(getGroup().getSize().x - 3, 1, 3, 1);
            reg.add(getGroup().getSize().x - 2, 2, 2, 1);
            reg.add(getGroup().getSize().x - 1, 3, 1, 1);
            reg.add(getGroup().getSize().x - 1, 4, 1, 1);

            gc.setClipping(reg);

            getGroup().drawBackground(gc, 0, 0, getGroup().getSize().x,5);

            gc.setClipping((Region)null);
            reg.dispose();
        }

        Point imagePoint = new Point(0, 0);

        if (getGroup().getToggleRenderer() != null)
        {
            Point p = getGroup().getToggleRenderer().getSize();
            int toggleY = 0;

            toggleY = (titleHeight - p.y) / 2;

            int toggleX = 0;
            if ((getGroup().getTogglePosition() & SWT.LEAD) != 0)
            {
                toggleX = hMargin;
            }
            else
            {
                toggleX = getGroup().getSize().x - hMargin - p.x;
            }
            getGroup().getToggleRenderer().setLocation(new Point(toggleX, toggleY));
        }

        Region reg = new Region(getGroup().getDisplay());
        reg.add(0, 0, getGroup().getSize().x, titleHeight);
        reg.subtract(0, 0, 5, 1);
        reg.subtract(0, 1, 3, 1);
        reg.subtract(0, 2, 2, 1);
        reg.subtract(0, 3, 1, 1);
        reg.subtract(0, 4, 1, 1);

        reg.subtract(getGroup().getSize().x - 5, 0, 5, 1);
        reg.subtract(getGroup().getSize().x - 3, 1, 3, 1);
        reg.subtract(getGroup().getSize().x - 2, 2, 2, 1);
        reg.subtract(getGroup().getSize().x - 1, 3, 1, 1);
        reg.subtract(getGroup().getSize().x - 1, 4, 1, 1);

        gc.setClipping(reg);

        back = gc.getBackground();
        Color fore = gc.getForeground();
        gc.setForeground(initialBackColor);
        gc.setBackground(getGroup().getParent().getBackground());
        Pattern p = new Pattern(getGroup().getDisplay(), 0, 0, 0, titleHeight,
                                initialBackColor, 255, getGroup().getBackground(), 0);
        gc.setBackgroundPattern(p);
        gc.fillRectangle(0, 0, getGroup().getSize().x, titleHeight);
        p.dispose();
        gc.setBackgroundPattern(null);

        if (getGroup().getExpanded() && getGroup().getSize().x > 1)
        {
            reg.subtract(1,titleHeight -1,getGroup().getSize().x -2,1);
            gc.setClipping(reg);
        }

        gc.setForeground(borderColor);
        GraphicUtils.drawRoundRectangle(gc, 0, 0, getGroup().getSize().x - 1, titleHeight, null,
                                        true, false);

        reg.dispose();
        gc.setClipping((Region)null);

        gc.setForeground(getGroup().getParent().getBackground());

        gc.setBackground(back);
        gc.setForeground(fore);

        Image image = getGroup().getImage();

        if (image != null)
        {
            imagePoint.x = hMargin;
            if ((getGroup().getImagePosition() & SWT.LEAD) != 0)
            {
                if (getGroup().getToggleRenderer() != null)
                {
                    if ((getGroup().getTogglePosition() & SWT.LEAD) != 0)
                    {
                        imagePoint.x += getGroup().getToggleRenderer().getSize().x + betweenSpacing;
                    }
                }
            }
            else
            {
                if (getGroup().getToggleRenderer() != null)
                {
                    if ((getGroup().getTogglePosition() & SWT.LEAD) != 0)
                    {
                        imagePoint.x = getGroup().getSize().x - (hMargin + image.getBounds().width);
                    }
                    else
                    {
                        imagePoint.x = getGroup().getSize().x
                                       - (hMargin + image.getBounds().width
                                          + getGroup().getToggleRenderer().getSize().x + betweenSpacing);
                    }
                }
                else
                {
                    imagePoint.x = getGroup().getSize().x - (hMargin + image.getBounds().width);
                }
            }
            imagePoint.y = ((titleHeight - image.getBounds().height) / 2);
            gc.drawImage(image, imagePoint.x, imagePoint.y);
        }

        Rectangle textBounds = getTextBounds();
        String shortened = TextUtils.getShortString(gc, getGroup().getText(), textBounds.width);

        if( getGroup().getToolItems().length > 0 && getGroup().getToolItemRenderer() != null ) {
        	PGroupToolItem[] items = getGroup().getToolItems();
        	AbstractToolItemRenderer renderer = getGroup().getToolItemRenderer();

        	Point size = new Point(0, 0);
        	Point minSize = new Point(0, 0);

        	int spacing = 3;

        	for(int i = 0; i < items.length; i++ ) {
        		PGroupToolItem item = items[i];
        		Point s0 = renderer.computeSize(gc, item, AbstractToolItemRenderer.DEFAULT);
        		Point s1 = renderer.computeSize(gc, item, AbstractToolItemRenderer.MIN);

        		size.x += s0.x + spacing;
    			minSize.x += s1.x + spacing;
        	}

        	boolean min = false;

        	if( shortened.length() != getGroup().getText().length() ) {
        		textBounds.width -= minSize.x;
        		min = true;
        	} else {
        		if( ! TextUtils.getShortString(gc, getGroup().getText(), textBounds.width - size.x).equals(getGroup().getText()) ) {
        			textBounds.width -= minSize.x;
        			min = true;
        		} else {
        			textBounds.width -= size.x;
        		}
        	}

        	shortened = TextUtils.getShortString(gc, getGroup().getText(), textBounds.width);

        	int x = textBounds.x + textBounds.width;

        	if( min ) {
        		toolItemArea = new Rectangle(x, 2, minSize.x, titleHeight - 4);
        	} else {
        		toolItemArea = new Rectangle(x, 2, size.x, titleHeight - 4);
        	}
        }

        gc.drawText(TextUtils.getShortString(gc, getGroup().getText(), textBounds.width),
                    textBounds.x, textBounds.y, true);

    }

    public Rectangle getToolItemArea() {
    	return toolItemArea;
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#isToggleLocation(int, int)
     */
    public boolean isToggleLocation(int x, int y)
    {
        if (super.isToggleLocation(x, y))
            return true;

        if (getGroup().getToggleRenderer() == null)
            return false;

        Rectangle textBounds = getTextBounds();
        textBounds.width = Math.min(textWidth,textBounds.width);
        if (textBounds.contains(x, y))
            return true;

        return false;
    }

    private Rectangle getTextBounds()
    {
        Point textPoint = new Point(0, 0);

        textPoint.x = hMargin;
        textPoint.y = (titleHeight - fontHeight) / 2;

        if (getGroup().getImage() != null)
        {
            if ((getGroup().getImagePosition() & SWT.LEAD) != 0)
            {
                textPoint.x += getGroup().getImage().getBounds().width + betweenSpacing;
            }
        }
        if (getGroup().getToggleRenderer() != null)
        {
            if ((getGroup().getTogglePosition() & SWT.LEAD) != 0)
            {
                textPoint.x += getGroup().getToggleRenderer().getSize().x + betweenSpacing;
            }
        }

        int textWidth = getGroup().getSize().x - (hMargin * 2);

        if (getGroup().getImage() != null)
        {
            textWidth -= (getGroup().getImage().getBounds().width + betweenSpacing);
        }

        if (getGroup().getToggleRenderer() != null)
        {
            textWidth -= getGroup().getToggleRenderer().getSize().x + betweenSpacing;
        }

        return new Rectangle(textPoint.x, textPoint.y, textWidth, fontHeight);
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#getClientArea()
     */
    public Rectangle getClientArea()
    {
        Rectangle area = getGroup().getBounds();
        area.x = margin;
        area.y = titleHeight;
        area.width -= (2 * margin);
        area.height -= titleHeight + margin;
        return area;
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle computeTrim(int x, int y, int width, int height)
    {
        Rectangle area = new Rectangle(x, y, Math.max(0, width), Math.max(0, height));
        area.x -= margin;
        area.y -= titleHeight;
        area.width += (2 * margin);
        area.height += titleHeight + margin;
        return area;
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#dispose()
     */
    public void dispose()
    {
        if (initialBackColor != null)
            initialBackColor.dispose();
        if (initialBorderColor != null)
            initialBorderColor.dispose();
    }

    /**
     * @return Returns the borderColor.
     */
    public Color getBorderColor()
    {
        return borderColor;
    }

    /**
     * @param borderColor The borderColor to set.
     */
    public void setBorderColor(Color borderColor)
    {
        this.borderColor = borderColor;
    }

    public void update()
    {
        GC gc = new GC(getGroup());

        titleHeight = 0;

        if (getGroup().getImage() != null)
            titleHeight = getGroup().getImage().getBounds().height + (2 * vMargin);

        if (getGroup().getToggleRenderer() != null)
        {
            int toggleHeight = getGroup().getToggleRenderer().getSize().y;
            titleHeight = Math.max(toggleHeight + (2 * vMargin), titleHeight);
        }
        titleHeight = Math.max(gc.getFontMetrics().getHeight() + (2 * titleTextMargin)
                               + (2 * vMargin), titleHeight);

        textWidth = gc.stringExtent(getGroup().getText()).x;

        fontHeight = gc.getFontMetrics().getHeight();

        gc.dispose();
    }

}
