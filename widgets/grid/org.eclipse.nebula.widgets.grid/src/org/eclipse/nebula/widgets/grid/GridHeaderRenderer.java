/*******************************************************************************
 * Copyright (c) 2008 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tom.schindl@bestsolution.at - initial API and implementation
 *    Chuck.Mastrandrea@sas.com - wordwrapping in bug 222280
 *    smcduff@hotmail.com       - wordwrapping in bug 222280
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p>
 * The super class for all grid header renderers.  Contains the properties specific
 * to a grid header.
 *
 * @author chris.gross@us.ibm.com
 */
public abstract class GridHeaderRenderer extends AbstractInternalWidget
{
    private boolean wordWrap = false;
    private int horizontalAlignment = SWT.LEFT;
    /**
     * Truncation style
     */
    protected int truncationStyle = SWT.CENTER;

    /**
     * Returns the bounds of the text in the cell.  This is used when displaying in-place tooltips.
     * If <code>null</code> is returned here, in-place tooltips will not be displayed.  If the
     * <code>preferred</code> argument is <code>true</code> then the returned bounds should be large
     * enough to show the entire text.  If <code>preferred</code> is <code>false</code> then the
     * returned bounds should be be relative to the current bounds.
     *
     * @param value the object being rendered.
     * @param preferred true if the preferred width of the text should be returned.
     * @return bounds of the text.
     */
    public Rectangle getTextBounds(Object value, boolean preferred)
    {
        return null;
    }

    /**
     * Returns the bounds of the toggle within the header (typically only group headers have toggles)
     * or null.
     *
     * @return toggle bounds or null if no toggle exists.
     */
    public Rectangle getToggleBounds()
    {
        return null;
    }

	/**
	 * Returns the bounds of the control to display
	 *
	 * @param value the control to display
	 * @param preferred if <code>true</code>, compute the preferred size
	 * @return the bounds for the control or <code>null</code> if no control is
	 *         rendered
	 */
	protected Rectangle getControlBounds(Object value, boolean preferred) {
		return null;
	}
	 /**
   * Returns whether or not text will be word-wrapped during the render
   * @return the wordWrap True if word wrapping is enabled
   */
  public boolean isWordWrap()
  {
      return wordWrap;
  }
  /**
   * Sets whether or not text should be word-wrapped during the render
   * @param wordWrap True to wrap text, false otherwise
   */
  public void setWordWrap(boolean wordWrap)
  {
      this.wordWrap = wordWrap;
  }

	/**
	 * Returns the header horizontal alignment.
	 *
	 * @return SWT.LEFT, SWT.RIGHT, SWT.CENTER
	 * @throws org.eclipse.swt.SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
	 *             </li>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             </ul>
	 */
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}


	/**
	 * Sets the header horizontal alignment.
	 *
	 * @param alignment
	 *            The alignment to set.
	 */
	public void setHorizontalAlignment(int alignment) {
		this.horizontalAlignment = alignment;
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
