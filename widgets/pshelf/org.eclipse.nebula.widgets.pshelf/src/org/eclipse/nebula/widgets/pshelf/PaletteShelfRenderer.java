/*******************************************************************************
 * Copyright (c) 2006 Chris Gross.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: schtoo@schtoo.com(Chris Gross) - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.pshelf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p>
 *
 * @author cgross
 */
public class PaletteShelfRenderer extends AbstractRenderer {

	private int textMargin = 2;
	private int margin = 2;
	private PShelf parent;
	private int spacing = 4;

	private Color shadeColor;


	/**
     * {@inheritDoc}
     */
    @Override
	public Point computeSize(GC gc, int wHint, int hHint, Object value)
    {
        PShelfItem item = (PShelfItem)value;

		if (item.getImage() == null)
			return new Point(wHint,gc.getFontMetrics().getHeight() + (2*(margin+textMargin)));

		int h = Math.max(item.getImage().getBounds().height,gc.getFontMetrics().getHeight() + (2*textMargin)) + (2*margin);

		if (h % 2 != 0)
			h ++;

		return new Point(wHint,h);
	}

	/**
     * {@inheritDoc}
     */
    @Override
	public void paint(GC gc, Object value)
    {
        PShelfItem item = (PShelfItem)value;
		int fontHeight = gc.getFontMetrics().getHeight();

		Color fore = parent.getForeground();

		gc.fillRectangle(0,getBounds().y,getBounds().width-1,getBounds().height-1);

        gc.setForeground(shadeColor);

		gc.fillGradientRectangle(0,getBounds().y,40,getBounds().height-1,false);

		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		gc.drawLine(0,getBounds().y,0,getBounds().y +getBounds().height -1);
		gc.drawLine(0,getBounds().y,getBounds().width -1,getBounds().y);

		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawLine(0,getBounds().y +getBounds().height -1,getBounds().width-1,getBounds().y +getBounds().height-1);
		gc.drawLine(getBounds().width-1,getBounds().y,getBounds().width-1,getBounds().y +getBounds().height-1);

		int x = 6;
		if (item.getImage() != null){
			int y2 = (getBounds().height - item.getImage().getBounds().height)/2;
			if ((getBounds().height - item.getImage().getBounds().height) % 2 != 0)
				y2 ++;

			gc.drawImage(item.getImage(),x,getBounds().y + y2);

			x += item.getImage().getBounds().width + spacing;
		}
		gc.setForeground(fore);

		int y2 = (getBounds().height - fontHeight)/2;
		if ((getBounds().height - fontHeight) % 2 != 0)
			y2 ++;

        if (isHover() && !isSelected())
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION));

        String text = getShortString(gc, item.getText(), getBounds().width - x - 4);
		gc.drawString(text,x,getBounds().y +y2,true);

		if (isFocus()){
			gc.drawFocus(1,1,getBounds().width-2,getBounds().height-1);
		}
	}

	@Override
	public void initialize(Control control){
		this.parent = (PShelf)control;
		shadeColor = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
	}

	public Color getShadeColor() {
		return shadeColor;
	}

	public void setShadeColor(Color shadeColor) {
		this.shadeColor = shadeColor;
	}

    private static String getShortString(GC gc, String t, int width)
    {

        if (t == null)
        {
            return null;
        }

        if (t.equals(""))
        {
            return "";
        }

        if (width >= gc.stringExtent(t).x)
        {
            return t;
        }

        int w = gc.stringExtent("...").x;
        String text = t;
        int l = text.length();
        int pivot = l / 2;
        int s = pivot;
        int e = pivot + 1;
        while (s >= 0 && e < l)
        {
            String s1 = text.substring(0, s);
            String s2 = text.substring(e, l);
            int l1 = gc.stringExtent(s1).x;
            int l2 = gc.stringExtent(s2).x;
            if (l1 + w + l2 < width)
            {
                text = s1 + "..." + s2;
                break;
            }
            s--;
            e++;
        }

        if (s == 0 || e == l)
        {
            text = text.substring(0, 1) + "..." + text.substring(l - 1, l);
        }

        return text;
    }
}
