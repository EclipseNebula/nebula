/*******************************************************************************
 * Copyright (c) 2020 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Inspired by the Segmented Bar Control of the ControlsFXProjet
 * (https://controlsfx.bitbucket.io/org/controlsfx/control/SegmentedBar.html)
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API and
 * implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.segmentedbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Instances of this class provide an horizontal bar that represents the segmentation of a total value.<br/>
 * It consists of several <b>segments</b>, each one associated to a value. The sum of all values is the total value of the bar.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * </p>
 */
public class SegmentedBar extends Canvas {

	private static final int DEFAULT_SPACING = 3;
	private static final int DEFAULT_WIDTH = 50;
	private static final int HORIZONTAL_BORDER = 2;

	private List<Segment> segments = new ArrayList<>();
	private int spacing = DEFAULT_SPACING;
	GC gc;
	int currentX;
	boolean isFirstItem;
	boolean isLastItem;
	private DefaultToolTip toolTip;

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must be
	 * built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
	 * constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style the style of control to construct
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                </ul>
	 *
	 */
	public SegmentedBar(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		addListeners();
		toolTip = new DefaultToolTip(this, ToolTip.RECREATE, false);
		toolTip.setBackgroundColor(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		toolTip.setShift(new Point(10, 5));
	}

	private void addListeners() {
		addListener(SWT.Paint, e -> {
			gc = e.gc;
			drawWidget();
		});

		addListener(SWT.MouseMove, event -> {
			toolTip.setText(null);
			for (Segment segment : segments) {
				if (segment.drawingArea == null) {
					continue;
				}
				if (segment.drawingArea.contains(event.x, event.y)) {
					toolTip.setText(segment.getTooltip());
				}
			}
		});

		addListener(SWT.MouseExit, event -> {
			toolTip.setText(null);
		});
	}

	private void drawWidget() {
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		final Color previousForeground = gc.getForeground();
		final Color previousBackground = gc.getBackground();

		int totalWidth = getSize().x;
		double total = getTotal();
		currentX = HORIZONTAL_BORDER;
		Iterator<Segment> it = segments.iterator();
		isFirstItem = true;
		while (it.hasNext()) {
			Segment segment = it.next();
			int segmentSize = (int) ((totalWidth - HORIZONTAL_BORDER * 2) * (segment.getValue() / total));
			isLastItem = !it.hasNext();
			drawItem(segment, segmentSize);
			currentX += segmentSize + spacing;
			isFirstItem = false;
		}

		gc.setBackground(previousBackground);
		gc.setForeground(previousForeground);
	}

	private void drawItem(Segment segment, int segmentSize) {
		segment.setParent(this);
		segment.draw(segmentSize);
	}

	/**
	 * Returns the total value of the bar (sum of segments values).
	 *
	 * @return the total value of the bar (sum of segments values).
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public double getTotal() {
		checkWidget();
		double total = 0d;
		for (Segment segment : segments) {
			total += segment.getValue();
		}
		return total;
	}

	/**
	 * Returns the list of all segments
	 *
	 * @return the list of all segments (may be empty)
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public List<Segment> getSegments() {
		checkWidget();
		return segments;
	}

	/**
	 * Sets the receiver's list of segments
	 *
	 * @param segments the new list of segments
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the list is null</li>
	 *                </ul>
	 */
	public void setSegments(List<Segment> segments) {
		checkWidget();
		if (segments == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.segments = segments;
		redraw();
		update();
	}

	/**
	 * Return the spacing between 2 segments
	 *
	 * @return the spacing in pixels between 2 segments
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public int getSpacing() {
		checkWidget();
		return spacing;
	}

	/**
	 * Sets the receiver's spacing between 2 segments
	 *
	 * @param spacing new spacing between 2 segments
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the new value is lower than 0</li>
	 *                </ul>
	 */
	public void setSpacing(int spacing) {
		checkWidget();
		if (spacing < 0) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		this.spacing = spacing;
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width = DEFAULT_WIDTH, height = 0;
		for (Segment segment : segments) {
			height = Math.max(height, segment.computeHeight());
		}

		if (wHint != SWT.DEFAULT) {
			width = wHint;
		}
		if (hHint != SWT.DEFAULT) {
			height = hHint;
		}
		final int border = getBorderWidth();
		width += border * 2;
		height += border * 2;
		return new Point(width, height);
	}

	/**
	 * Add a segment to this widget
	 *
	 * @param segment segment to add
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the segment is null</li>
	 *                </ul>
	 */
	public void addSegment(Segment segment) {
		checkWidget();
		if (segment == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		segments.add(segment);
		redraw();
		update();
	}

	/**
	 * Remove a segment to this widget
	 *
	 * @param segment segment to remove
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                 <li>ERROR_NULL_ARGUMENT - if the segment is null</li>
	 *                </ul>
	 */
	public void removeSegment(Segment segment) {
		checkWidget();
		if (segment == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		segments.remove(segment);
		redraw();
		update();
	}
}
