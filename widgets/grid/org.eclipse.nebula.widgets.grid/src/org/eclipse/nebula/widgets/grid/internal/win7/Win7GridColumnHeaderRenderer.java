package org.eclipse.nebula.widgets.grid.internal.win7;

import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.internal.DefaultColumnHeaderRenderer;
import org.eclipse.nebula.widgets.grid.internal.SortArrowRenderer;
import org.eclipse.nebula.widgets.grid.internal.TextUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

/**
 * Column header renderer which emulates a default Win7 L&F.
 * This implementation does not take into account any theme(s) applied to the OS and only used
 * a pre-defined set of normalColors that seem to "mostly" match the default theme of Win7 normalColors.
 */
public class Win7GridColumnHeaderRenderer extends DefaultColumnHeaderRenderer {

    int leftMargin = 6;

    int rightMargin = 6;

    int topMargin = 3;

    int bottomMargin = 3;

    int arrowMargin = 6;

    int imageSpacing = 3;


    private SortArrowRenderer arrowRenderer = new SortArrowRenderer();

    private TextLayout textLayout;

    private Win7PaletteProvider palette;

    private int truncationStyle = SWT.CENTER;

    /**
     * Creates a new Windows 7 style column header renderer.
     * 
     * @param palette the palette provider for Windows 7 colors
     */
    public Win7GridColumnHeaderRenderer(Win7PaletteProvider palette) {
    	this.palette = palette;
	}

    /**
     * Set the display for the renderer
     * @param d Display
     */
    public void setDisplay(Display d) {
        super.setDisplay(d);
        arrowRenderer.setDisplay(d);
        palette.initializePalette(getDisplay(), Win7PaletteProvider.NORMAL_GRID_COLUMN_HEADER);
        palette.initializePalette(getDisplay(), Win7PaletteProvider.HOVER_GRID_COLUMN_HEADER);
        palette.initializePalette(getDisplay(), Win7PaletteProvider.MOUSEDOWN_GRID_COLUMN_HEADER);
        palette.initializePalette(getDisplay(), Win7PaletteProvider.SELECTED_GRID_COLUMN_HEADER);
    }


    /**
     * {@inheritDoc}
     */
    public void paint(GC gc, Object value) {
        GridColumn column = (GridColumn) value;

        // set the font to be used to display the text.
        gc.setFont(column.getHeaderFont());

        // workaround until the hover logic in the grid's onMouseExit is resolved
        boolean isHover = isHover();// && getDisplay().getCursorControl() == column.getParent();
        boolean isMouseDown = isMouseDown() && isHover;
        boolean isSelected = isSelected();


    	Win7ColumnHeaderUtil.drawColumn(gc, getBounds(), palette, isHover, isSelected, isMouseDown);

        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        int pushedDrawingOffset = 0;

        int x = leftMargin;

        int width = getBounds().width - x;

        if (column.getSort() == SWT.NONE) {
            width -= rightMargin;
        } else {
            width -= arrowMargin + arrowRenderer.getSize().x + arrowMargin;
        }

        int y = bottomMargin;

        if (column.getHeaderControl() == null) {
            y = getBounds().y + getBounds().height - bottomMargin - gc.getFontMetrics().getHeight();
        } else {
            y = getBounds().y + getBounds().height - bottomMargin - gc.getFontMetrics().getHeight() - computeControlSize(column).y;
        }

        String text = column.getText();

        if (!isWordWrap()) {
            text = TextUtils.getShortStr(gc, text, width,truncationStyle);
        }

        if (column.getAlignment() == SWT.RIGHT) {
            int len = gc.stringExtent(text).x;
            if (len < width) {
                x += width - len;
            }
        } else if (column.getAlignment() == SWT.CENTER) {
            int len = gc.stringExtent(text).x;
            if (len < width) {
                x += (width - len) / 2;
            }
        }

        if (!isWordWrap()) {
            gc.drawString(text, getBounds().x + x + pushedDrawingOffset, y + pushedDrawingOffset, true);
        } else {
            getTextLayout(gc, column);
            textLayout.setWidth(width < 1 ? 1 : width);
            textLayout.setText(text);
            y -= textLayout.getBounds().height;

            // remove the first line shift
            y = gc.getFontMetrics().getHeight();

            if (column.getParent().isAutoHeight()) {
                column.getParent().recalculateHeader();
            }
            textLayout.draw(gc, getBounds().x + x + pushedDrawingOffset, y + pushedDrawingOffset);
        }

        if (column.getSort() != SWT.NONE) {
            if (column.getHeaderControl() == null) {
                y = getBounds().y + ((getBounds().height - arrowRenderer.getBounds().height) / 2) + 1;
            } else {
                y = getBounds().y + ((getBounds().height - computeControlSize(column).y - arrowRenderer.getBounds().height) / 2) + 1;
            }

            arrowRenderer.setSelected(column.getSort() == SWT.UP);
             if (isMouseDown && isHover) {
                 arrowRenderer.setLocation(getBounds().x + getBounds().width - arrowMargin - arrowRenderer.getBounds().width + 1, y);
             } else {
                 if (column.getHeaderControl() == null) {
                     y = getBounds().y + ((getBounds().height - arrowRenderer.getBounds().height) / 2);
                 } else {
                     y = getBounds().y + ((getBounds().height - computeControlSize(column).y - arrowRenderer.getBounds().height) / 2);
                 }
                 arrowRenderer.setLocation(getBounds().x + getBounds().width - arrowMargin - arrowRenderer.getBounds().width, y);
             }
             arrowRenderer.paint(gc, null);
         }
     }

     /**
      * Draw an additional shadow for the selected column state
      * @param graphics the graphics context
      * @param bounds the bounds to draw within
      * @param colors the color array to use for drawing
      */
     protected static void drawColumnSelectedTopShadow(GC graphics, Rectangle bounds, Color[] colors){
         int x = bounds.x;
         int y = bounds.y;

         graphics.setForeground(colors[0]);
         graphics.drawLine(x+1, y+1, x+bounds.width-2, y+1);
         graphics.setForeground(colors[1]);
         graphics.drawLine(x+1, y+2, x+bounds.width-2, y+2);
     }


     /**
      * Draw the column header based on the given colors
      * @param graphics the graphics context
      * @param bounds the bounds to draw within
      * @param colors the color array to use for drawing
      */
     protected static void drawColumnHeader(GC graphics, Rectangle bounds, Color[] colors){
         int x = bounds.x;
         int y = bounds.y;

         int topRectHeight = (int)Math.round((bounds.height-3)*.45);
         int bottomRectHeight = bounds.height-3-topRectHeight;
         int bottomRectY = y+topRectHeight+1;

         //1 - top highlight
         graphics.setForeground(colors[0]);
         graphics.drawLine(x, y, x+bounds.width-1, y);

         //2 - left upper
         graphics.setBackground(colors[1]);
         graphics.fillRectangle(x, y+1, 1, topRectHeight);

         //3 - upper fill
         graphics.setBackground(colors[2]);
         graphics.fillRectangle(x+1, y+1, bounds.width-3, topRectHeight);

         //4 - right upper
         graphics.setBackground(colors[3]);
         graphics.fillRectangle(x+bounds.width-2, y+1, 1, topRectHeight);

         //5 - right upper gradient (shadow/highlight)
         graphics.setBackground(colors[5]);
         if ( colors[4] != null ){
             graphics.setForeground(colors[4]);
             graphics.fillGradientRectangle(x+bounds.width-1, y+1, 1, topRectHeight, true);
         } else {
             graphics.fillRectangle(x+bounds.width-1, y+1, 1, topRectHeight);
         }

         //6 - left bottom gradient (shadow/hightlight)
         graphics.setBackground(colors[7]);
         if ( colors[6] != null ){
             graphics.setForeground(colors[6]);
             graphics.fillGradientRectangle(x, bottomRectY, 1, bottomRectHeight, true);
         } else {
             graphics.fillRectangle(x, bottomRectY, 1, bottomRectHeight);
         }

         //7 - bottom fill
         graphics.setBackground(colors[9]);
         if ( colors[8] != null ){
             graphics.setForeground(colors[8]);
             graphics.fillGradientRectangle(x+1, bottomRectY, bounds.width-3, bottomRectHeight, true);
         } else {
             graphics.fillRectangle(x+1, bottomRectY, bounds.width-3, bottomRectHeight);
         }

         //8 - right bottom gradient (shadow/highlight)
         graphics.setBackground(colors[11]);
         if ( colors[10] != null ){
             graphics.setForeground(colors[10]);
             graphics.fillGradientRectangle(x+bounds.width-2, bottomRectY, 1, bottomRectHeight, true);
         } else {
             graphics.fillRectangle(x+bounds.width-2, bottomRectY, 1, bottomRectHeight);
         }

         //9 - right bottom cell border
         graphics.setBackground(colors[13]);
         if ( colors[12] != null ){
             graphics.setForeground(colors[12]);
             graphics.fillGradientRectangle(x+bounds.width-1, bottomRectY, 1, bottomRectHeight, true);
         } else {
             graphics.fillRectangle(x+bounds.width-1, bottomRectY, 1, bottomRectHeight);
         }

         //10 - bottom shadow
         graphics.setForeground(colors[14]);
         graphics.drawLine(x, y+bounds.height-2, x+bounds.width-1, y+bounds.height-2);

         //11 - bottom cell border
         graphics.setForeground(colors[15]);
         graphics.drawLine(x, y+bounds.height-1, x+bounds.width-1, y+bounds.height-1);
     }



     private void getTextLayout(GC gc, GridColumn column) {
         if (textLayout == null) {
             textLayout = new TextLayout(gc.getDevice());
             textLayout.setFont(gc.getFont());
             column.getParent().addListener(SWT.Dispose, e->
                  textLayout.dispose()
             );
         }
         textLayout.setAlignment(column.getAlignment());
     }

     private Point computeControlSize(GridColumn column) {
         if (column.getHeaderControl() != null) {
             return column.getHeaderControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
         }
         return new Point(0, 0);
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