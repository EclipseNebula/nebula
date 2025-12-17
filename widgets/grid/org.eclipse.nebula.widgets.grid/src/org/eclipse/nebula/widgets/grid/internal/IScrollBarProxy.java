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
 *    chris.gross@us.ibm.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Used by Grid to externalize the scrollbars from the table itself.
 *
 * @author chris.gross@us.ibm.com
 * @version 1.0.0
 */
public interface IScrollBarProxy
{

    /**
     * Returns the scrollbar's visibility.
     *
     * @return true if the scrollbar is visible.
     */
    public boolean getVisible();

    /**
     * Sets the scrollbar's visibility.
     *
     * @param visible visibilty
     */
    public void setVisible(boolean visible);

    /**
     * Returns the selection.
     *
     * @return the selection.
     */
    public int getSelection();

    /**
     * Sets the selection.
     *
     * @param selection selection to set
     */
    public void setSelection(int selection);

    /**
     * Sets the receiver's selection, minimum value, maximum value, thumb,
     * increment and page increment all at once.
     *
     * @param selection selection
     * @param min minimum
     * @param max maximum
     * @param thumb thumb
     * @param increment increment
     * @param pageIncrement page increment
     */
    public void setValues(int selection, int min, int max, int thumb, int increment,
                          int pageIncrement);


    /**
     * Handles mouse wheel events for scrolling.
     * 
     * @param e the mouse wheel event
     */
    public void handleMouseWheel(Event e);

    /**
     * Sets the minimum value.
     * 
     * @param min the minimum value
     */
    public void setMinimum(int min);

    /**
     * @return min
     */
    public int getMinimum();

    /**
     * Sets the maximum value.
     * 
     * @param max the maximum value
     */
    public void setMaximum(int max);

    /**
     * @return max
     */
    public int getMaximum();

    /**
     * Sets the thumb size.
     * 
     * @param thumb the thumb size
     */
    public void setThumb(int thumb);

    /**
     * @return thumb
     */
    public int getThumb();

    /**
     * Sets the increment value.
     * 
     * @param increment the increment value
     */
    public void setIncrement(int increment);

    /**
     * @return increment
     */
    public int getIncrement();

    /**
     * Sets the page increment value.
     * 
     * @param page the page increment value
     */
    public void setPageIncrement(int page);

    /**
     * @return page increment
     */
    public int getPageIncrement();

    /**
     * Adds a selection listener.
     * 
     * @param listener the selection listener to add
     */
    public void addSelectionListener(SelectionListener listener);

    /**
     * Removes a selection listener.
     * 
     * @param listener the selection listener to remove
     */
    public void removeSelectionListener(SelectionListener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an event of the given type occurs. When the
     * event does occur in the widget, the listener is notified by
     * sending it the <code>handleEvent()</code> message. The event
     * type is one of the event constants defined in class <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
     * @param listener the listener which should be notified when the event occurs
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Listener
     * @see SWT
     * @see #removeListener(int, Listener)
     */
    public void addListener(int eventType, Listener listener);

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an event of the given type occurs. The event
     * type is one of the event constants defined in class <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Listener
     * @see SWT
     * @see #addListener
     */
	public void removeListener(int eventType, Listener listener);
}
