package org.eclipse.swt.nebula.presentations.shelf;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.nebula.widgets.pgroup.PGroup;
import org.eclipse.swt.nebula.widgets.pgroup.RectangleGroupStrategy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

import java.net.URL;

/**
 * STANDALONE ONLY
 *
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class PGroupStackPresentation extends StackPresentation
{
    private static final String DATAKEY_ANIMATION = "Busy Anim Player";
    
    private Composite partParent;
    private PGroup group;
    private Color toolbarBackground;

    private Image[] busyImages;
    
    private int minHeight = 0;
    
    /**
     * @param stackSite
     */
    public PGroupStackPresentation(IStackPresentationSite stackSite, Composite parent)
    {
        super(stackSite);
        
        partParent = parent;
        
        group = new PGroup(parent,SWT.NONE);
        group.setStrategy(new RectangleGroupStrategy());
        group.setImagePosition(SWT.LEFT | SWT.TOP);
        group.setBackground(group.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        
        //HACK
        parent.setBackground(parent.getBackground());
        parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
        //HACK
        
        new Composite(group,SWT.NONE);
        group.setLayout(new FillLayout());
        
        group.setExpanded(false);
        minHeight = group.computeSize(SWT.DEFAULT,SWT.DEFAULT).y;
        group.setExpanded(true);
        
        
        RGB sel = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
        RGB blendwith = new RGB(255,255,255);
        RGB blended = blend(sel,blendwith,20);
        
        toolbarBackground = new Color(blended);
        
        group.addExpandListener(new ExpandListener()
        {        
            public void itemExpanded(ExpandEvent e)
            {
                getSite().setState(IStackPresentationSite.STATE_RESTORED); 
            }        
            public void itemCollapsed(ExpandEvent e)
            { 
                getSite().setState(IStackPresentationSite.STATE_MINIMIZED);
            }        
        });
                        
        PresentationUtil.addDragListener(group,new Listener()
        {        
            public void handleEvent(Event event)
            {               
               getSite().setState(IStackPresentationSite.STATE_RESTORED);
               getSite().dragStart(getSite().getSelectedPart(), new Point(event.x, event.y), false);
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
        updateItem(newPart);
        
        newPart.addPropertyListener(new IPropertyListener()
        {        
            public void propertyChanged(Object source, int propId)
            {
                updateItem(newPart);
            }        
        });
        
        resizeSelectedPart();
    }
    
    private void updateItem(final IPresentablePart part)
    {
        String dirty = "";
        if (part.isDirty())
            dirty = "*";
        
        group.setText(dirty + part.getName());
        
        if (part.getTitleImage() != group.getImage())
        {            
            group.setImage(part.getTitleImage());
            boolean expanded = group.getExpanded();
            group.setExpanded(false);
            minHeight = group.computeSize(SWT.DEFAULT,SWT.DEFAULT).y;
            group.setExpanded(expanded);
        }
        
        
        if (part.getToolBar() != null)
        {
            part.getToolBar().setBackground(toolbarBackground);
        }
        
        resizeSelectedPart();
                
        if (part.isBusy() && group.getData(DATAKEY_ANIMATION) == null)
        {
            BusyGroupAnimation busyAnim = new BusyGroupAnimation(group);
            busyAnim.setImages(busyImages);
            group.setData(DATAKEY_ANIMATION,busyAnim);
            busyAnim.start();
        }
        else if (!part.isBusy() && group.getData(DATAKEY_ANIMATION) != null)
        {
            BusyGroupAnimation busyAnim = (BusyGroupAnimation)group.getData(DATAKEY_ANIMATION);
            busyAnim.stop();
            group.setData(DATAKEY_ANIMATION,null);
            group.setImage(part.getTitleImage());
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        group.dispose();
        toolbarBackground.dispose();
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
        return group;
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
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void selectPart(IPresentablePart toSelect)
    {
        toSelect.setVisible(true);
        if (toSelect.getToolBar() != null)
            toSelect.getToolBar().setVisible(true);
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
        getSite().getSelectedPart().setVisible(true);
        group.setBounds(bounds);
        group.getDisplay().asyncExec(new Runnable()
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
        
        Control partTB = part.getToolBar();
        
        Rectangle bounds = group.getClientArea();        
        
        Point partTBSize = new Point(0,0);
        if (partTB != null)
        {
            partTBSize = partTB.computeSize(bounds.width,SWT.DEFAULT);
       
            Rectangle tbBounds = new Rectangle(0,0,bounds.width,partTBSize.y);
            
            tbBounds = group.getDisplay().map(group.getChildren()[0], partTB.getParent(), tbBounds);
            
            partTB.setBounds(tbBounds);
            
            bounds.y += tbBounds.height;
            bounds.height -= tbBounds.height;        
        }
        
        bounds = group.getDisplay().map(group, partParent, bounds);

        part.setBounds(bounds);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setState(int state)
    {
        if (state == IStackPresentationSite.STATE_MINIMIZED)
        {
            group.setExpanded(false);
        }
        else if(state == IStackPresentationSite.STATE_RESTORED)
        {
            group.setExpanded(true);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean isVisible)
    {
        group.setVisible(isVisible);
        if (getSite().getSelectedPart() == null) return;
        getSite().getSelectedPart().setVisible(isVisible);
        if (getSite().getSelectedPart().getToolBar() != null)
            getSite().getSelectedPart().getToolBar().setVisible(isVisible);
    }

    
    
    @Override
    public Point computeMinimumSize()
    {
        return new Point(100,minHeight);
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
