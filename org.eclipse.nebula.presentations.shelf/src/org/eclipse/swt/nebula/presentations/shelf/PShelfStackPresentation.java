// ==================================================================
// ShelfStackPresentation.java
// ==================================================================
// IBM Confidential
// OCO Source Materials
// © Copyright IBM Corp. 2005
// ==================================================================

package org.eclipse.swt.nebula.presentations.shelf;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.nebula.widgets.pshelf.PShelf;
import org.eclipse.swt.nebula.widgets.pshelf.PShelfItem;
import org.eclipse.swt.nebula.widgets.pshelf.RedmondShelfRenderer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

import java.net.URL;

/**
 * 
 *
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class PShelfStackPresentation extends StackPresentation
{
    private static final String DATAKEY_MENUBUTTONFLAG = "hasMenuButton";
    private static final String DATAKEY_MENUTOOL = "Menu Toolbar";
    private static final String DATAKEY_DESCLABEL = "Description Label";
    private static final String DATAKEY_PART = "IPresentablePart";
    private static final String DATAKEY_SEPHEIGHT = "Separator Height";
    private static final String DATAKEY_ANIMATION = "Busy Anim Player";
    private static final String DATAKEY_CLOSEBUTTONFLAG = "hasCloseButton";
    private PShelf shelf;
    private Composite partParent;
    
    private boolean ignoreSelection = false;
    
    private Color toolbarBackground;
    private Color border;
    
    private Image menuTBImage;
    private Image closeTBImage;
    
    private Image[] busyImages;
    
    /**
     * @param stackSite
     */
    public PShelfStackPresentation(IStackPresentationSite stackSite, Composite parent)
    {
        super(stackSite);
        
        partParent = parent;
        
        shelf = new PShelf(parent,SWT.SIMPLE | SWT.BORDER);
        shelf.setRenderer(new RedmondShelfRenderer());
        
        shelf.addListener(SWT.Selection, new Listener()
        {        
            public void handleEvent(Event event)
            {
                if (event.item == null) return;
                
                if (!ignoreSelection)
                    getSite().selectPart((IPresentablePart)event.item.getData(DATAKEY_PART));
            }        
        });
        
        RGB sel = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
        RGB blendwith = new RGB(255,255,255);
        RGB blended = blend(sel,blendwith,20);
        
        toolbarBackground = new Color(blended);
        
        blended = blend(sel,blendwith,60);
        
        border = new Color(blended);
        
        URL imageURL = Platform.getBundle("org.eclipse.swt.nebula.presentations.shelf").getResource("icons/view_menu.gif");
        menuTBImage = ImageDescriptor.createFromURL(imageURL).createImage();

        imageURL = Platform.getBundle("org.eclipse.swt.nebula.presentations.shelf").getResource("icons/close_view.gif");
        closeTBImage = ImageDescriptor.createFromURL(imageURL).createImage();
                
        PresentationUtil.addDragListener(shelf,new Listener()
        {        
            public void handleEvent(Event event)
            {
               PShelfItem item = shelf.getItem(new Point(event.x,event.y));
               if (item == null) return;
               
               IPresentablePart part = (IPresentablePart)item.getData(DATAKEY_PART);
               
               
               getSite().selectPart(part);
               getSite().dragStart(part, new Point(event.x, event.y), false);
            }        
        });
        
        //init busy images
        busyImages = new Image[8];
        for (int i = 0; i < 8; i++)
        {            
            URL imgURL = Platform.getBundle("org.eclipse.swt.nebula.presentations.shelf").getResource("icons/busy" + (i + 1) + ".gif");
            Image img = ImageDescriptor.createFromURL(imgURL).createImage();
            busyImages[i] = img;
        }
        
    }
    
    private static int blend(int v1, int v2, int ratio)
    {
        return (ratio * v1 + (100 - ratio) * v2) / 100;
    }

    private static RGB blend(RGB c1, RGB c2, int ratio)
    {
        int r = blend(c1.red, c2.red, ratio);
        int g = blend(c1.green, c2.green, ratio);
        int b = blend(c1.blue, c2.blue, ratio);
        return new RGB(r, g, b);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void addPart(final IPresentablePart newPart, Object cookie)
    {
        ignoreSelection = true;
        final PShelfItem item = new PShelfItem(shelf,SWT.NONE);
        ignoreSelection = false;
        item.setData(DATAKEY_PART,newPart);
        
        item.getBody().setLayout(null);        
        item.getBody().addListener(SWT.Paint, new Listener()
        {        
            public void handleEvent(Event e)
            {
                Integer separatorY = (Integer)item.getBody().getData(DATAKEY_SEPHEIGHT);
                if (separatorY == null) return;
                e.gc.setForeground(border);
                e.gc.drawLine(0,separatorY.intValue(),item.getBody().getSize().x,separatorY.intValue());
            }        
        });
        
        CLabel descLabel = new CLabel(item.getBody(),SWT.WRAP);
        item.setData(DATAKEY_DESCLABEL,descLabel);
        
        descLabel.setBackground(toolbarBackground);
        
        ToolBar tb = new ToolBar(item.getBody(),SWT.NONE);
        item.setData(DATAKEY_MENUTOOL,tb);
        
        tb.setBackground(toolbarBackground);
        
        updateItem(newPart);
        
        newPart.addPropertyListener(new IPropertyListener()
        {        
            public void propertyChanged(Object source, int propId)
            {
                updateItem(newPart);
            }        
        });
    }
    
    private void updateItem(final IPresentablePart part)
    {
        PShelfItem item = getItem(part);
        
        String dirty = "";
        if (part.isDirty())
            dirty = "*";
        
        item.setText(dirty + part.getName());
        item.setImage(part.getTitleImage());
        
        CLabel descLabel = (CLabel)item.getData(DATAKEY_DESCLABEL);
        descLabel.setText(part.getTitleStatus());
        
        final ToolBar menuTB = (ToolBar)item.getData(DATAKEY_MENUTOOL);
        if (part.getMenu() != null && menuTB.getData(DATAKEY_MENUBUTTONFLAG) == null)
        {            
            final ToolItem ti = new ToolItem(menuTB,0,SWT.NONE);
            ti.setImage(menuTBImage);
            ti.addListener(SWT.Selection, new Listener()
            {            
                public void handleEvent(Event event)
                { 
                    Point loc = new Point(ti.getBounds().x,ti.getBounds().y + ti.getBounds().height);
                    loc = ti.getDisplay().map(menuTB, null, loc);
                    part.getMenu().showMenu(loc);
                }            
            });
            ti.setToolTipText("Show Menu");
            
            menuTB.setData(DATAKEY_MENUBUTTONFLAG,"AnyValue");
        }
        
        if (part.isCloseable() && menuTB.getData(DATAKEY_CLOSEBUTTONFLAG) == null)
        {
            final ToolItem ti = new ToolItem(menuTB,SWT.NONE);
            ti.setImage(closeTBImage);
            ti.addListener(SWT.Selection, new Listener()
            {            
                public void handleEvent(Event event)
                { 
                    getSite().close(new IPresentablePart[]{part});
                }            
            });
            ti.setToolTipText("Close");
            
            menuTB.setData(DATAKEY_CLOSEBUTTONFLAG,"AnyValue");
        }

        Control partTB = part.getToolBar();
        if (partTB != null)
        {
            partTB.setBackground(toolbarBackground);
        }
        
        resizeSelectedPart();
                
        if (part.isBusy() && item.getData(DATAKEY_ANIMATION) == null)
        {
            BusyItemAnimation busyAnim = new BusyItemAnimation(item);
            busyAnim.setImages(busyImages);
            item.setData(DATAKEY_ANIMATION,busyAnim);
            busyAnim.start();
        }
        else if (!part.isBusy() && item.getData(DATAKEY_ANIMATION) != null)
        {
            BusyItemAnimation busyAnim = (BusyItemAnimation)item.getData(DATAKEY_ANIMATION);
            busyAnim.stop();
            item.setData(DATAKEY_ANIMATION,null);
            item.setImage(part.getTitleImage());
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        shelf.dispose();
        toolbarBackground.dispose();
        border.dispose();
        menuTBImage.dispose();
        for (int i = 0; i < busyImages.length; i++)
        {
            busyImages[i].dispose();
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public StackDropResult dragOver(Control currentControl, Point location)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Control getControl()
    {
        return shelf;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Control[] getTabList(IPresentablePart part)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void removePart(IPresentablePart oldPart)
    {
        getItem(oldPart).dispose();
        resizeSelectedPart();
        
        oldPart.setVisible(false);
        if (oldPart.getToolBar() != null)
            oldPart.getToolBar().setVisible(false);
    }
    
    private PShelfItem getItem(IPresentablePart part)
    {
        PShelfItem[] items = shelf.getItems();
        for (int i = 0; i < items.length; i++)
        {
            if (items[i].getData(DATAKEY_PART) == part)
            {
                return items[i];
            }
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void selectPart(IPresentablePart toSelect)
    {
        shelf.setSelection(getItem(toSelect));
        
        IPresentablePart[] parts = getSite().getPartList();
        for (int i = 0; i < parts.length; i++)
        {
            parts[i].setVisible(parts[i] == toSelect);
        }
        
        updateItem(toSelect);
        resizeSelectedPart();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setActive(int newState)
    {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setBounds(Rectangle bounds)
    {
        shelf.setBounds(bounds);
        shelf.getDisplay().asyncExec(new Runnable()
        {
        
            public void run()
            {
                // TODO Auto-generated method stub
                resizeSelectedPart(); 
            }
        
        });
        resizeSelectedPart(); 
    }
    
    private void resizeSelectedPart()
    {
        IPresentablePart part = getSite().getSelectedPart();
        
        if (part == null) return;
        
        PShelfItem item = getItem(part);
        
        if (item == null) return;
        
        CLabel descLabel = (CLabel)item.getData(DATAKEY_DESCLABEL);
        ToolBar menuTB = (ToolBar)item.getData(DATAKEY_MENUTOOL);
        Control partTB = part.getToolBar();
        
        Point partTBSize = new Point(0,0);
        if (partTB != null)
            partTBSize = partTB.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        Point menuTBSize = menuTB.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        if (menuTB.getItemCount() == 0)
            menuTBSize = new Point(0,0);
       
        Rectangle bounds = item.getBody().getBounds();
        
        int maxWidth = bounds.width - menuTBSize.x;
        
        boolean partTBonLine1 = (partTB == null? true : (partTBSize.x <= maxWidth));
        
        
        int height = 0;
        
        if (partTBonLine1)
        {
            height = Math.max(menuTBSize.y, partTBSize.y);
            
            menuTB.setBounds(bounds.width - menuTBSize.x,0,menuTBSize.x,height);
            
            if (partTB != null)
            {
                Rectangle toolBounds = new Rectangle(bounds.width - menuTBSize.x - partTBSize.x,0,partTBSize.x,height);
                toolBounds = shelf.getDisplay().map(item.getBody(), partTB.getParent(), toolBounds);
                toolBounds.height = Math.min(item.getBody().getSize().y, toolBounds.height);
                partTB.setBounds(toolBounds);
            }
            
            descLabel.setBounds(0,0,bounds.width - menuTBSize.x - partTBSize.x,height);
        }
        else
        {
            menuTB.setBounds(bounds.width - menuTBSize.x,0,menuTBSize.x,menuTBSize.y);            
            descLabel.setBounds(0,0,bounds.width - menuTBSize.x,menuTBSize.y);

            partTBSize = partTB.computeSize(bounds.width,SWT.DEFAULT);
            
            Rectangle toolBounds = new Rectangle(0,menuTBSize.y,bounds.width,partTBSize.y);
            
            height = menuTBSize.y + toolBounds.height;
            
            toolBounds.height = Math.min(toolBounds.height, item.getBody().getSize().y - toolBounds.y);

            toolBounds = shelf.getDisplay().map(item.getBody(), partTB.getParent(), toolBounds);
            partTB.setBounds(toolBounds);
            
            
        }       
        
        if (height > 0)
        {
            //add space for the separator
            height += 1;
            item.getBody().setData(DATAKEY_SEPHEIGHT,new Integer(height -1));
            item.getBody().redraw();
        }

        bounds.height -= height;
        bounds.y += height;
        
        bounds = shelf.getDisplay().map(shelf.getSelection().getBody().getParent(), partParent, bounds);

        part.setBounds(bounds);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setState(int state)
    {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean isVisible)
    {
        shelf.setVisible(isVisible);
        if (getSite().getSelectedPart() == null) return;
        getSite().getSelectedPart().setVisible(isVisible);
        if (getSite().getSelectedPart().getToolBar() != null)
            getSite().getSelectedPart().getToolBar().setVisible(isVisible);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void showPaneMenu()
    {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void showSystemMenu()
    {
    }
}
