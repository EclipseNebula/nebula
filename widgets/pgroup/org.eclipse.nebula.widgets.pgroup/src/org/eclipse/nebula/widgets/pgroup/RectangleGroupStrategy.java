/*******************************************************************************
 * Copyright (c) 2006 Chris Gross.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		schtoo@schtoo.com (Chris Gross) - initial API and implementation
 * 		Tom Schindl <tom.schindl@bestsolution.at> - added support for PGroupToolItem
 ******************************************************************************/

package org.eclipse.nebula.widgets.pgroup;

import org.eclipse.nebula.widgets.pgroup.internal.GraphicUtils;
import org.eclipse.nebula.widgets.pgroup.internal.TextUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

/**
 * RectangleGroupStrategy is a very flexible painting strategy that displays a
 * (rounded) rectangle around the PGroup's body.
 *
 * @since 1.0
 * @author chris
 */
public class RectangleGroupStrategy extends AbstractGroupStrategy
{

    private final int vMargin = 2;

    private final int hMargin = 5;

    private final int titleTextMargin = 2;

    private final int betweenSpacing = 4;

    private final int margin = 1;

    private Color gradientColors[] = null;

    private int gradientPercents[] = null;

    private boolean gradientVertical = false;

    private Color borderColor = null;

    private Color g1;

    private Color g2;

    private int titleHeight;
	private int titleWidth;

    private int fontHeight;

    private int titleAreaHeight;

    private Rectangle toolItemArea;

    /**
     * @deprecated use constructor with PGroup element instead
     */
    @Deprecated
	public RectangleGroupStrategy() {
    	this(null);
    }
    
    /**
     * Constructs a RectangleGroupStrategy with the given toggle and style.
     *
     * @param toggleStrategy
     * @param style
     */
    public RectangleGroupStrategy(PGroup g)
    {
        super(g);
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#initialize()
     */
    @Override
	public void initialize()
    {
        super.initialize();

        if (gradientColors!=null && gradientColors != null) {
        	return;
        }
        g1 = getGroup().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
        g2 = getGroup().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);

        setBackground(new Color[] {g1, g2 }, new int[] {100 }, true);
        setBorderColor(g2);
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#paint(org.eclipse.swt.graphics.GC)
     */
    @Override
	public void paint(GC gc)
    {
		Region originalClip = new Region(gc.getDevice());
		gc.getClipping(originalClip);
		try {
	        Point imagePoint = new Point(0, 0);
	        Image image = getGroup().getImage();
	
	        if (getGroup().getToggleRenderer() != null)
	        {
	            Point p = getGroup().getToggleRenderer().getSize();
	            int toggleY = 0;
	            if ((getGroup().getImagePosition() & SWT.TOP) == 0)
	            {
	                toggleY = (titleHeight - p.y) / 2;
	            }
	            else
	            {
	                toggleY = (titleHeight - titleAreaHeight) + (titleAreaHeight - p.y) / 2;
	            }
	            if ((getGroup().getTogglePosition() & SWT.LEAD) != 0)
	            {
	                getGroup().getToggleRenderer().setLocation(new Point(hMargin, toggleY));
	            }
	            else
	            {
	                getGroup().getToggleRenderer().setLocation(
	                                                           new Point(getGroup().getSize().x
	                                                                     - hMargin - p.x, toggleY));
	            }
	        }
	
	        Color back = getGroup().internalGetBackground();
	        if (back != null)
	        {
	            gc.fillRectangle(0,0,getGroup().getSize().x,getGroup().getSize().y);
	
	            int yOffset = 0;
	            if ((getGroup().getImagePosition() & SWT.TOP) != 0 && image != null)
	            {
	                yOffset = titleHeight - titleAreaHeight;
	            }
	
	            Region reg = new Region();
	            reg.add(0,yOffset + 0, 5, 1);
	            reg.add(0,yOffset + 1, 3, 1);
	            reg.add(0,yOffset + 2, 2, 1);
	            reg.add(0,yOffset + 3, 1, 1);
	            reg.add(0,yOffset + 4, 1, 1);
	
	            reg.add(getGroup().getSize().x - 5,yOffset + 0, 5, 1);
	            reg.add(getGroup().getSize().x - 3,yOffset + 1, 3, 1);
	            reg.add(getGroup().getSize().x - 2,yOffset + 2, 2, 1);
	            reg.add(getGroup().getSize().x - 1,yOffset + 3, 1, 1);
	            reg.add(getGroup().getSize().x - 1,yOffset + 4, 1, 1);
	
	            int height = getGroup().getSize().y;
	            if (!getGroup().getExpanded())
	            {
	                height = titleHeight;
	            }
	
	            reg.add(0, height - 1, 5, 1);
	            reg.add(0, height - 2, 3, 1);
	            reg.add(0, height - 3, 2, 1);
	            reg.add(0, height - 4, 1, 1);
	            reg.add(0, height - 5, 1, 1);
	
	            reg.add(getGroup().getSize().x - 5, height - 1, 5, 1);
	            reg.add(getGroup().getSize().x - 3, height - 2, 3, 1);
	            reg.add(getGroup().getSize().x - 2, height - 3, 2, 1);
	            reg.add(getGroup().getSize().x - 1, height - 4, 1, 1);
	            reg.add(getGroup().getSize().x - 1, height - 5, 1, 1);
	
	            if (yOffset != 0)
	            {
	                reg.add(0,0,getGroup().getSize().x,yOffset);
	            }
	
	            if (!getGroup().getExpanded())
	            {
	            	int regionHeight = getGroup().getSize().y - titleHeight;
	           	    if( regionHeight < 0 ) {
						regionHeight = 0;
					}
	                reg.add(new Rectangle(0,titleHeight,getGroup().getSize().x,regionHeight));
	            }
	
	            gc.setClipping(reg);
	
	            getGroup().drawBackground(gc, 0, 0, getGroup().getSize().x,getGroup().getSize().y);
	
	            gc.setClipping((Region)null);
	            reg.dispose();
	        }
	
	        // Paint rectangle
	        int toggleHeight = 0;
	        if (getGroup().getToggleRenderer() != null)
	        {
	            toggleHeight = getGroup().getToggleRenderer().getSize().y + (2 * vMargin);
	        }
	
	        Region reg = null;
	        if ((getGroup().getStyle() & SWT.SMOOTH) != 0)
	        {
	            reg = new Region(getGroup().getDisplay());
	            reg.add(0, 0, getGroup().getSize().x, titleHeight);
	
	            int yOffset = 0;
	            if ((getGroup().getImagePosition() & SWT.TOP) != 0 && image != null)
	            {
	                yOffset = titleHeight - titleAreaHeight;
	            }
	
	            reg.subtract(0, yOffset + 0, 5, 1);
	            reg.subtract(0, yOffset + 1, 3, 1);
	            reg.subtract(0, yOffset + 2, 2, 1);
	            reg.subtract(0, yOffset + 3, 1, 1);
	            reg.subtract(0, yOffset + 4, 1, 1);
	
	            reg.subtract(getGroup().getSize().x - 5, yOffset + 0, 5, 1);
	            reg.subtract(getGroup().getSize().x - 3, yOffset + 1, 3, 1);
	            reg.subtract(getGroup().getSize().x - 2, yOffset + 2, 2, 1);
	            reg.subtract(getGroup().getSize().x - 1, yOffset + 3, 1, 1);
	            reg.subtract(getGroup().getSize().x - 1, yOffset + 4, 1, 1);
	
	            if (!getGroup().getExpanded())
	            {
	                yOffset = titleHeight;
	
	                reg.subtract(0, yOffset - 1, 5, 1);
	                reg.subtract(0, yOffset - 2, 3, 1);
	                reg.subtract(0, yOffset - 3, 2, 1);
	                reg.subtract(0, yOffset - 4, 1, 1);
	                reg.subtract(0, yOffset - 5, 1, 1);
	
	                reg.subtract(getGroup().getSize().x - 5, yOffset - 1, 5, 1);
	                reg.subtract(getGroup().getSize().x - 3, yOffset - 2, 3, 1);
	                reg.subtract(getGroup().getSize().x - 2, yOffset - 3, 2, 1);
	                reg.subtract(getGroup().getSize().x - 1, yOffset - 4, 1, 1);
	                reg.subtract(getGroup().getSize().x - 1, yOffset - 5, 1, 1);
	            }
				reg.intersect(originalClip);
	            gc.setClipping(reg);
	        }
	
	        if ((getGroup().getImagePosition() & SWT.TOP) == 0 || image == null)
	        {
	
	            if (gradientColors != null)
	            {
	                GraphicUtils.fillGradientRectangle(gc, 0, 0, getGroup().getSize().x, Math
	                    .max(titleHeight, toggleHeight), gradientColors, gradientPercents,
	                                                   gradientVertical);
	            }
	            else
	            {
	                gc.fillRectangle(0, 0, getGroup().getSize().x, Math.max(titleHeight, toggleHeight));
	                GraphicUtils.fillRoundRectangle(gc, 0, 0, getGroup().getSize().x, Math
	                    .max(titleHeight, toggleHeight), null, true, !getGroup().getExpanded());
	            }
	            if ((getGroup().getStyle() & SWT.SMOOTH) != 0)
	            {
	                GraphicUtils.drawRoundRectangle(gc, 0, 0, getGroup().getSize().x, Math
	                    .max(titleHeight, toggleHeight), null, null, true, !getGroup().getExpanded());
	            }
	        }
	        else
	        {
	
	            if (gradientColors != null)
	            {
	                GraphicUtils.fillGradientRectangle(gc, 0, titleHeight - titleAreaHeight, getGroup()
	                    .getSize().x, Math.max(titleAreaHeight, toggleHeight), gradientColors,
	                                                   gradientPercents, gradientVertical);
	            }
	            else
	            {
	                gc.setBackground(getGroup().getBackground());
	                gc.fillRectangle(0, titleHeight - titleAreaHeight, getGroup().getSize().x, Math
	                    .max(titleAreaHeight, toggleHeight));
	            }
	
	        }
	
	        if ((getGroup().getStyle() & SWT.SMOOTH) != 0)
	        {
	            gc.setClipping((Region)null);
	            reg.dispose();
	        }
	
	        // Paint Image
	        if (image != null)
	        {
	            if ((getGroup().getImagePosition() & SWT.LEAD) != 0)
	            {
	                if (getGroup().getToggleRenderer() != null)
	                {
	                    if (getGroup().getTogglePosition() == SWT.LEAD)
	                    {
	                        imagePoint.x = hMargin + getGroup().getToggleRenderer().getSize().x
	                                       + betweenSpacing;
	                    }
	                    else
	                    {
	                        imagePoint.x = hMargin;
	                    }
	                }
	                else
	                {
	                    imagePoint.x = hMargin;
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
	            if ((getGroup().getImagePosition() & SWT.TOP) == 0
	                && image.getImageData().height > titleHeight)
	            {
	                imagePoint.y = (titleHeight - image.getImageData().height) / 2;
	            }
	            else
	            {
	                imagePoint.y = (titleHeight - image.getImageData().height) / 2;
	            }
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
	        		toolItemArea = new Rectangle(x, titleHeight - titleAreaHeight + 2, minSize.x, Math.max(titleAreaHeight, toggleHeight)-4);
	        	} else {
	        		toolItemArea = new Rectangle(x, titleHeight - titleAreaHeight + 2, size.x, Math.max(titleAreaHeight, toggleHeight)-4);
	        	}
	        }
	
	        gc.setForeground(getGroup().getDisplay().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
	        gc.drawText(shortened, textBounds.x, textBounds.y, true);
	        gc.setForeground(getGroup().getForeground());
	
	        if (!getGroup().getExpanded())
	        {
	            gc.setBackground(getGroup().getParent().getBackground());
	        }
	        else
	        {
	            Color _borderColor;
	            if (borderColor == null)
	            {
	                _borderColor = getGroup().getBackground();
	            }
	            else
	            {
	                _borderColor = borderColor;
	            }
	
	            if ((getGroup().getStyle() & SWT.SMOOTH) != 0)
	            {
	                gc.setBackground(getGroup().getBackground());
	                gc.setForeground(_borderColor);
	
	                reg = new Region(getGroup().getDisplay());
	                reg.add(0, 0, getGroup().getSize().x, getGroup().getSize().y);
	
	                int yOffset = getGroup().getSize().y;
	
	                reg.subtract(0, yOffset - 1, 5, 1);
	                reg.subtract(0, yOffset - 2, 3, 1);
	                reg.subtract(0, yOffset - 3, 2, 1);
	                reg.subtract(0, yOffset - 4, 1, 1);
	                reg.subtract(0, yOffset - 5, 1, 1);
	
	                reg.subtract(getGroup().getSize().x - 5, yOffset - 1, 5, 1);
	                reg.subtract(getGroup().getSize().x - 3, yOffset - 2, 3, 1);
	                reg.subtract(getGroup().getSize().x - 2, yOffset - 3, 2, 1);
	                reg.subtract(getGroup().getSize().x - 1, yOffset - 4, 1, 1);
	                reg.subtract(getGroup().getSize().x - 1, yOffset - 5, 1, 1);
					reg.intersect(originalClip);
	                gc.setClipping(reg);
	
	                GraphicUtils.drawRoundRectangle(gc, 0, titleHeight, getGroup().getSize().x - 1,
	                                                getGroup().getSize().y - titleHeight, null, false,
	                                                true);
	
	                reg.dispose();
	                gc.setClipping((Region)null);
	            }
	            else
	            {
	                gc.setForeground(_borderColor);
	                gc.drawRectangle(0, titleHeight, getGroup().getBounds().width - 1, getGroup()
	                    .getBounds().height
	                                                                                   - titleHeight
	                                                                                   - 1);
	            }
	        }
	
	        gc.setBackground(getGroup().getBackground());
	        gc.setForeground(getGroup().getForeground());
		} finally {
			gc.setClipping(originalClip);
			originalClip.dispose();
		}
    }

    @Override
	public Rectangle getToolItemArea() {
    	return toolItemArea;
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#isToggleLocation(int, int)
     */
    @Override
	public boolean isToggleLocation(int x, int y)
    {
    	if( getGroup().getToolItems().length == 0 ) {
            if (y >= titleHeight - titleAreaHeight && y <= titleHeight) {
                return true;
            } else {
            	return false;
            }
    	} else {
    		return super.isToggleLocation(x, y);
    	}
    }

    protected Rectangle getTextBounds()
    {
        Point textPoint = new Point(0, 0);
        int textWidth = 0;
        Image image = getGroup().getImage();

        int titleAreaHeight = 0;

        if (image != null && !((getGroup().getImagePosition() & SWT.TOP) == 0))
        {
            titleAreaHeight = fontHeight + (2 * titleTextMargin) + (2 * vMargin);
            if (getGroup().getToggleRenderer() != null)
            {
                titleAreaHeight = Math.max(titleAreaHeight, getGroup().getToggleRenderer()
                    .getSize().y
                                                            + (2 * vMargin));
            }
        }
        textPoint.x = hMargin;
        if (image != null)
        {
            if ((getGroup().getImagePosition() & SWT.LEAD) != 0)
            {
                textPoint.x += image.getBounds().width + betweenSpacing;
            }
            if ((getGroup().getImagePosition() & SWT.TOP) == 0)
            {
                textPoint.y = (titleHeight - fontHeight) / 2;
            }
            else
            {
                textPoint.y = (titleHeight - titleAreaHeight) + (titleAreaHeight - fontHeight) / 2;
            }
        }
        else
        {
            textPoint.y = (titleHeight - fontHeight) / 2;
        }

        // Part 2, Toggle
        if (getGroup().getToggleRenderer() != null)
        {
            if ((getGroup().getTogglePosition() & SWT.LEAD) != 0)
            {
                textPoint.x += getGroup().getToggleRenderer().getSize().x + betweenSpacing;
            }
        }

        textWidth = getGroup().getSize().x - (hMargin * 2);

        if (image != null)
        {
            textWidth -= (image.getBounds().width + betweenSpacing);
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
    @Override
	public Rectangle getClientArea()
    {
        Rectangle area = getGroup().getBounds();
        area.x = margin;
        area.y = titleHeight +1;
        area.width -= (2 * margin);
        if ((getGroup().getStyle() & SWT.SMOOTH) != 0)
        {
            area.height -= titleHeight + 5 + 1;
        }
        else
        {
            area.height -= titleHeight + margin + 1;
        }
        return area;
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#computeTrim(int, int, int, int)
     */
    @Override
	public Rectangle computeTrim(int x, int y, int widthHint, int heightHint)
    {

		Rectangle area = new Rectangle(x, y, Math.max(0, widthHint), Math.max(0, heightHint));
        area.x -= margin;
        area.y -= titleHeight;
		area.width += (2 * margin);
		PGroup group = getGroup();
		if(widthHint == SWT.DEFAULT) {
			// This indicates the caller wants the preferred size to be computed so we should give a clue how much space the control will take up without hiding anything
			area.width += titleWidth + 2 * hMargin + betweenSpacing;
			AbstractRenderer toggleRenderer = group.getToggleRenderer();
			if(toggleRenderer != null) {
				Point p = toggleRenderer.getSize();
				area.width += p.x;
			}
		}
        area.height += titleHeight;
		if(group.getExpanded())
        {
			if((group.getStyle() & SWT.SMOOTH) != 0)
            {
                area.height += 5;
            }
            else
            {
                area.height += margin;
            }
        }
        return area;
    }

    /** 
     * @see org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy#dispose()
     */
    @Override
	public void dispose()
    {
        g1.dispose();
        g2.dispose();
    }

    /**
     * Specify a gradient of colours to be drawn in the background of the group.
     * <p>
     * For example, to draw a gradient that varies from dark blue to blue and
     * then to white and stays white for the right half of the label, use the
     * following call to setBackground:
     * </p>
     *
     * <pre>
     * setBackground(new Color[] {display.getSystemColor(SWT.COLOR_DARK_BLUE),
     *                            display.getSystemColor(SWT.COLOR_BLUE),
     *                            display.getSystemColor(SWT.COLOR_WHITE),
     *                            display.getSystemColor(SWT.COLOR_WHITE) }, new int[] {25, 50, 100 });
     * </pre>
     *
     * @param colors an array of Color that specifies the colors to appear in
     * the gradient in order of appearance from left to right; The value
     * <code>null</code> clears the background gradient; the value
     * <code>null</code> can be used inside the array of Color to specify the
     * background color.
     * @param percents an array of integers between 0 and 100 specifying the
     * percent of the width of the widget at which the color should change; the
     * size of the percents array must be one less than the size of the colors
     * array.
     */
    public void setBackground(Color[] colors, int[] percents)
    {
        setBackground(colors, percents, false);
    }

    /**
     * Specify a gradient of colours to be drawn in the background of the group.
     * <p>
     * For example, to draw a gradient that varies from dark blue to white in
     * the vertical, direction use the following call to setBackground:
     * </p>
     *
     * <pre>
     * setBackground(new Color[] {display.getSystemColor(SWT.COLOR_DARK_BLUE),
     *                            display.getSystemColor(SWT.COLOR_WHITE) }, new int[] {100 }, true);
     * </pre>
     *
     * @param colors an array of Color that specifies the colors to appear in
     * the gradient in order of appearance from left/top to right/bottom; The
     * value <code>null</code> clears the background gradient; the value
     * <code>null</code> can be used inside the array of Color to specify the
     * background color.
     * @param percents an array of integers between 0 and 100 specifying the
     * percent of the width/height of the widget at which the color should
     * change; the size of the percents array must be one less than the size of
     * the colors array.
     * @param vertical indicate the direction of the gradient. True is vertical
     * and false is horizontal.
     */
    public void setBackground(Color[] colors, int[] percents, boolean vertical)
    {
        if (colors != null)
        {
            if (percents == null || percents.length != colors.length - 1)
            {
                throw new RuntimeException(
                                           "Percents array must be one item less than the colors array");
            }
            if (getGroup().getDisplay().getDepth() < 15)
            {
                // Don't use gradients on low color displays
                colors = new Color[] {colors[colors.length - 1] };
                percents = new int[] {};
            }
            for (int i = 0; i < percents.length; i++)
            {
                if (percents[i] < 0 || percents[i] > 100)
                {
                    throw new RuntimeException("Percent array item out of range");
                }
            }
        }

        // Are these settings the same as before?
        final Color background = getGroup().getBackground();

        if ((gradientColors != null) && (colors != null)
            && (gradientColors.length == colors.length))
        {
            boolean same = false;
            for (int i = 0; i < gradientColors.length; i++)
            {
                same = (gradientColors[i] == colors[i])
                       || ((gradientColors[i] == null) && (colors[i] == background))
                       || ((gradientColors[i] == background) && (colors[i] == null));
                if (!same) {
					break;
				}
            }
            if (same)
            {
                for (int i = 0; i < gradientPercents.length; i++)
                {
                    same = gradientPercents[i] == percents[i];
                    if (!same) {
						break;
					}
                }
            }
            if (same && this.gradientVertical == vertical) {
				return;
			}
        }

        // Store the new settings
        if (colors == null)
        {
            gradientColors = null;
            gradientPercents = null;
            gradientVertical = false;
        }
        else
        {
            gradientColors = new Color[colors.length];
            for (int i = 0; i < colors.length; ++i) {
				gradientColors[i] = (colors[i] != null) ? colors[i] : background;
			}
            gradientPercents = new int[percents.length];
            for (int i = 0; i < percents.length; ++i) {
				gradientPercents[i] = percents[i];
			}
            gradientVertical = vertical;
        }
        // Refresh with the new settings
        getGroup().redraw();
    }

    /**
     * Returns the color of the one pixel border drawn around the body when the
     * group is expanded.
     *
     * @return the border color
     */
    public Color getBorderColor()
    {
        return borderColor;
    }

    /**
     * Sets the border color. The border is the one pixel border drawn around
     * the body when the group is expanded.
     *
     * @param borderColor the border color, or null for no border
     */
    public void setBorderColor(Color borderColor)
    {
        this.borderColor = borderColor;
    }

	@Override
	public void update() {

		PGroup group = getGroup();
		GC gc = new GC(group);
		try {
			String text = group.getText();
			Point extent = gc.stringExtent(text);
			titleWidth = extent.x;
			int imageHeight = 0;
			if(group.getImage() != null) {
				imageHeight = group.getImage().getBounds().height;
			}
			if((group.getImagePosition() & SWT.TOP) == 0) {
				titleHeight = Math.max(gc.getFontMetrics().getHeight() + (2 * titleTextMargin), imageHeight);
				titleHeight += (2 * vMargin);
			} else {
				titleHeight = Math.max(gc.getFontMetrics().getHeight() + (2 * titleTextMargin) + (2 * vMargin), imageHeight + 1);
			}
			if(group.getToggleRenderer() != null) {
				int toggleHeight = group.getToggleRenderer().getSize().y;
				titleHeight = Math.max(toggleHeight + (2 * vMargin), titleHeight);
			}
			fontHeight = gc.getFontMetrics().getHeight();
			titleAreaHeight = fontHeight + (2 * titleTextMargin) + (2 * vMargin);
			if(group.getToggleRenderer() != null) {
				titleAreaHeight = Math.max(titleAreaHeight, group.getToggleRenderer().getSize().y + (2 * vMargin));
			}
		} finally {
			gc.dispose();
		}
	}
}
