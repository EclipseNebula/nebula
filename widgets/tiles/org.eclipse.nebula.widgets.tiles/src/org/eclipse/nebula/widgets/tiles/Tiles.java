/*****************************************************************************
 * Copyright (c) 2014, 2021 Fabian Prasser, Laurent Caron
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Fabian Prasser - Initial API and implementation
 * Laurent Caron <laurent dot caron at gmail dot com> - Integration into the Nebula Project
 *****************************************************************************/
package org.eclipse.nebula.widgets.tiles;

import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 *
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class Tiles<T> extends Canvas {

	/** Current frame*/
	private Frame<T>              current           = null;

	/** Next frame*/
	private FrameStatic<T>        next              = new FrameStatic<>(this);

	/** Default layout*/
	private TileLayout            layout            = new TileLayoutDynamic(10, 10, 5, 5);

	/** Tooltip decorator*/
	private DecoratorString<T>    tooltipDecorator  = new DecoratorStringToString<>();

	/** Listener*/
	private final PaintListener         paintListener     = getPaintListener();

	/** Listener*/
	private final ResizeListener        resizeListener    = getResizeListener();

	/** Listener*/
	private final MouseListener         mouseListener     = getMouseListener();

	/** Listener*/
	private final MouseMoveListener     mouseMoveListener = getMouseMoveListener();

	/** Listener*/
	private final DisposeListener       disposeListener   = getDisposeListener();

	/** Animation*/
	private final Runnable              animation         = getAnimation();

	/** Animation settings*/
	private TileAnimationSettings settings          = new TileAnimationSettings(30, 3000);

	/** Selected item*/
	private T                     selectedItem      = null;

	/** Active item*/
	private T                     activeItem        = null;

	/** Color */
	private final Color                 black;

	/**
	 * Creates a new instance
	 * @param parent
	 * @param style
	 */
	public Tiles(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		addPaintListener(paintListener);
		addControlListener(resizeListener);
		addDisposeListener(disposeListener);
		addMouseListener(mouseListener);
		addMouseMoveListener(mouseMoveListener);
		addControlListener(resizeListener);
		this.black = new Color(0, 0, 0);
		getDisplay().timerExec(settings.getDelta(), animation);
	}

	/**
	 * Adds a selection listener
	 * @param listener
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addTypedListener(listener, SWT.Selection);
	}

	/**
	 * Returns the settings
	 * @return
	 */
	public TileAnimationSettings getAnimationSettings(){
		super.checkWidget();
		return this.settings;
	}

	/**
	 * Returns the comparator
	 * @return
	 */
	public Comparator<T> getComparator() {
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getComparator();
		}
	}

	/**
	 * Returns a decorator
	 * @return
	 */
	public DecoratorColor<T> getDecoratorBackgroundColor(){
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getDecoratorBackgroundColor();
		}
	}

	/**
	 * Returns a decorator
	 * @return
	 */
	public DecoratorColor<T> getDecoratorForegroundColor(){
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getDecoratorForegroundColor();
		}
	}

	/**
	 * Returns a decorator
	 * @return
	 */
	public DecoratorString<T> getDecoratorLabel(){
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getDecoratorLabel();
		}
	}

	/**
	 * Returns a decorator
	 * @return
	 */
	public DecoratorColor<T> getDecoratorLineColor(){
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getDecoratorLineColor();
		}
	}

	/**
	 * Returns a decorator
	 * @return
	 */
	public DecoratorInteger<T> getDecoratorLineStyle(){
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getDecoratorLineStyle();
		}
	}

	/**
	 * Returns a decorator
	 * @return
	 */
	public DecoratorInteger<T> getDecoratorLineWidth(){
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getDecoratorLineWidth();
		}
	}

	/**
	 * Returns the filter
	 * @return
	 */
	public Filter<T> getFilter() {
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getFilter();
		}
	}

	/**
	 * Returns the items displayed by this widget
	 * @return
	 */
	public List<T> getItems() {
		super.checkWidget();
		if (current == null) {
			return null;
		} else {
			return current.getItems();
		}
	}

	/**
	 * Returns the selected item
	 * @return
	 */
	public T getSelectedItem() {
		super.checkWidget();
		return selectedItem;
	}

	/**
	 * Removes a selection listener
	 * @param listener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		removeTypedListener(SWT.Selection, listener);
	}

	/**
	 * Sets the settings
	 * @return
	 */
	public void setAnimationSettings(final TileAnimationSettings settings){
		super.checkWidget();
		this.settings = settings;
	}

	@Override
	public void setBackground(final Color arg0) {
		super.checkWidget();
		super.setBackground(arg0);
		this.addFrame();
		this.next.setBackground(arg0);
	}

	/**
	 * Sets an Comparator
	 *
	 * @param Comparator
	 */
	public void setComparator(final Comparator<T> Comparator) {
		super.checkWidget();
		this.addFrame();
		this.next.setComparator(Comparator);
	}

	/**
	 * Sets a decorator
	 *
	 * @param decorator
	 */
	public void setDecoratorBackgroundColor(final DecoratorColor<T> decorator) {
		super.checkWidget();
		this.addFrame();
		this.next.setDecoratorBackgroundColor(decorator);
	}

	/**
	 * Sets a decorator
	 *
	 * @param decorator
	 */
	public void setDecoratorForegroundColor(final DecoratorColor<T> decorator) {
		super.checkWidget();
		this.addFrame();
		this.next.setDecoratorForegroundColor(decorator);
	}

	/**
	 * Sets a decorator
	 *
	 * @param decorator
	 */
	public void setDecoratorLabel(final DecoratorString<T> decorator) {
		super.checkWidget();
		this.addFrame();
		this.next.setDecoratorLabel(decorator);
	}

	/**
	 * Sets a decorator
	 *
	 * @param decorator
	 */
	public void setDecoratorLineColor(final DecoratorColor<T> decorator) {
		super.checkWidget();
		this.addFrame();
		this.next.setDecoratorLineColor(decorator);
	}

	/**
	 * Sets a decorator
	 *
	 * @param decorator
	 */
	public void setDecoratorLineStyle(final DecoratorInteger<T> decorator) {
		super.checkWidget();
		this.addFrame();
		this.next.setDecoratorLineStyle(decorator);
	}

	/**
	 * Sets a decorator
	 *
	 * @param decorator
	 */
	public void setDecoratorLineWidth(final DecoratorInteger<T> decorator) {
		super.checkWidget();
		this.addFrame();
		this.next.setDecoratorLineWidth(decorator);
	}

	/**
	 * Sets a decorator
	 *
	 * @param decorator
	 */
	public void setDecoratorTooltip(final DecoratorString<T> decorator) {
		super.checkWidget();
		this.tooltipDecorator = decorator;
	}

	/**
	 * Sets a filter
	 *
	 * @param filter
	 */
	public void setFilter(final Filter<T> filter) {
		super.checkWidget();
		this.addFrame();
		this.next.setFilter(filter);
	}

	/**
	 * Sets the selected item
	 * @return
	 */
	public void setSelectedItem(final T item) {
		super.checkWidget();
		this.selectedItem = item;
		this.redraw();
	}

	/**
	 * Adds an item
	 *
	 * @param t
	 */
	public void setItems(final List<T> items) {
		super.checkWidget();
		this.addFrame();
		this.next.setItems(items);
	}

	/**
	 * Sets the tile layout
	 *
	 * @param width
	 */
	public void setTileLayout(final TileLayout layout) {
		super.checkWidget();
		this.layout = layout;
		this.addFrame();
		this.next.setTileHeight(this.layout.getHeight(Tiles.this));
		this.next.setTileWidth(this.layout.getWidth(Tiles.this));
	}

	/**
	 * Updates the tiles.
	 * TODO: Good to override?
	 */
	@Override
	public void update() {

		super.checkWidget();
		super.update();

		if (current == null){
			this.current = next.clone();
			this.current.update();
		} else if (current instanceof FrameStatic) {
			final FrameStatic<T> target = next.clone();
			target.update();
			current.update();
			this.current = new FrameDynamic<>(this, (FrameStatic<T>)current, target);
			this.current.update();
		}
	}

	/**
	 * Creates a new frame
	 */
	private void addFrame(){
		final FrameStatic<T> current = this.next;
		this.next = this.next.clone();
		current.dispose();
	}

	/**
	 * Fires a selection event
	 * @param tile
	 */
	private void fireSelectionEvent(final Tile<T> tile){
		final Event event = new Event();
		event.data = tile.item;
		event.display = getDisplay();
		event.widget = this;
		event.x = tile.x;
		event.y = tile.y;
		event.width = tile.width;
		event.height = tile.height;
		notifyListeners(SWT.Selection, event);
	}

	/**
	 * Returns the animation thread
	 * @return
	 */
	private Runnable getAnimation() {
		return new Runnable() {
			@Override
			public void run() {
				final Frame<T> frame = current;
				if (frame != null && frame instanceof FrameDynamic) {
					((FrameDynamic<T>)frame).tick();
					frame.update();
					redraw();
				}
				getDisplay().timerExec(settings.getDelta(), this);
			}
		};
	}

	/**
	 * Creates a listener
	 * @return
	 */
	private DisposeListener getDisposeListener() {
		return new DisposeListener(){
			@Override
			public void widgetDisposed(final DisposeEvent arg0) {
				getDisplay().timerExec(-1, animation);
				if (current != null) {
					current.dispose();
				}
				if (next != null) {
					next.dispose();
				}
				black.dispose();
				removePaintListener(paintListener);
				removeControlListener(resizeListener);
				removeMouseListener(mouseListener);
				removeMouseMoveListener(mouseMoveListener);
				removeDisposeListener(this);
			}
		};
	}

	/**
	 * Returns the mouse listener
	 * @return
	 */
	private MouseListener getMouseListener() {
		return new MouseAdapter(){
			@Override
			public void mouseDown(final MouseEvent arg0) {
				final Tile<T> tile = getTileAt(arg0.x, arg0.y);
				if (tile != null && tile.item != selectedItem) {
					selectedItem = tile.item;
					redraw();
					fireSelectionEvent(tile);
				}
			}
		};
	}

	/**
	 * Returns the mouse motion listener
	 * @return
	 */
	private MouseMoveListener getMouseMoveListener() {
		return arg0 -> {

			// Check
			final Tile<T> tile = getTileAt(arg0.x, arg0.y);
			final T item = tile == null ? null : tile.item;

			// Break
			if (item == activeItem) {
				return;
			}

			// Set
			activeItem = item;
			redraw();
			final String tooltip = activeItem != null ? tooltipDecorator.decorate(activeItem) : null;
			Tiles.this.setToolTipText(tooltip);
		};
	}

	/**
	 * Creates a listener
	 * @return
	 */
	private PaintListener getPaintListener() {
		return e -> {
			final Point size = getSize();
			paintFrame(e.gc, current, size.x, size.y);
		};
	}

	/**
	 * Creates a listener
	 * @return
	 */
	private ResizeListener getResizeListener() {
		return new ResizeListener(this){
			@Override
			protected void controlResized() {
				addFrame();
				final Point p = Tiles.this.getSize();
				next.setWidth(p.x);
				next.setHeight(p.y);
				next.setTileHeight(layout.getHeight(Tiles.this));
				next.setTileWidth(layout.getWidth(Tiles.this));
				next.setTileMarginX(layout.getMarginX());
				next.setTileMarginY(layout.getMarginY());
				update();
			}
		};
	}

	/**
	 * Returns the tile at the given location, null if there is none
	 * @param x
	 * @param y
	 * @return
	 */
	private Tile<T> getTileAt(final int x, final int y){
		if (current == null) {
			return null;
		}
		for (final Tile<T> tile : current.getTiles()) {
			if (x >= tile.x && y >= tile.y && x <= tile.x + tile.width
					&& y <= tile.y + tile.height) {
				return tile;
			}
		}
		return null;
	}

	/**
	 * Paints a frame
	 * @param height
	 * @param width
	 * @param decorator
	 */
	private void paintFrame(final GC gc, final Frame<T> frame, final int width, final int height) {

		if (frame == null) {
			return;
		}

		// Background
		gc.setBackground(frame.getBackground());
		gc.fillRectangle(0, 0, width, height);

		// Render each tile
		for (final Tile<T> tile : frame.getTiles()) {

			// Background
			gc.setClipping(0, 0, width, height);

			Color backgroundColor = tile.backgroundColor;
			if (tile.item == activeItem) {
				backgroundColor = lightDarker(tile.backgroundColor);
			} else if (tile.item == selectedItem) {
				backgroundColor = strongDarker(tile.backgroundColor);
			}

			gc.setBackground(backgroundColor);
			gc.fillRectangle(tile.x, tile.y, tile.width, tile.height);

			// Border
			gc.setForeground(tile.lineColor);
			gc.setLineStyle(tile.lineStyle);
			gc.setLineWidth(tile.lineWidth);
			gc.drawRectangle(tile.x, tile.y, tile.width, tile.height);

			// Text
			gc.setForeground(tile.foregroundColor);
			gc.setClipping(tile.x, tile.y, tile.width, tile.height);
			paintString(gc, tile.label, tile.x, tile.y, tile.width, tile.height);

			// Dispose
			if (backgroundColor != tile.backgroundColor) {
				gc.setBackground(tile.backgroundColor);
				backgroundColor.dispose();
			}
		}

		// Draw border
		if ((getStyle() & SWT.BORDER) != 0) {
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			gc.drawRectangle(0, 0, getSize().x-1, getSize().y-1);
		}
	}

	/**
	 * Makes it darker
	 * @param color
	 * @return
	 */
	private Color strongDarker(final Color color) {
		int r = color.getRed() - 125;
		int g = color.getGreen() - 125;
		int b = color.getBlue() - 125;
		r = r >= 0 ? r : 0;
		g = g >= 0 ? g : 0;
		b = b >= 0 ? b : 0;
		return new Color(color.getDevice(), r, g, b);
	}

	/**
	 * Makes it a little darker
	 * @param color
	 * @return
	 */
	private Color lightDarker(final Color color) {
		int r = color.getRed() - 75;
		int g = color.getGreen() - 75;
		int b = color.getBlue() - 75;
		r = r >= 0 ? r : 0;
		g = g >= 0 ? g : 0;
		b = b >= 0 ? b : 0;
		return new Color(color.getDevice(), r, g, b);
	}

	/**
	 * Paints the given string within the given rectangle
	 *
	 * @param gc
	 * @param string
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintString(final GC gc, final String string, final int x, final int y, final int width, final int height) {
		final Point extent = gc.textExtent(string);
		final int xx = x + (width - extent.x) / 2;
		final int yy = y + (height - extent.y) / 2;

		if (extent.x <= width * 0.9f) {
			gc.drawText(string, xx, yy, true);
		} else {

			// Enable anti-aliasing
			gc.setTextAntialias(SWT.ON);
			gc.setAntialias(SWT.ON);

			// Compute position and factor
			final float factor1 = width * 0.9f / extent.x;
			final float factor2 = height * 0.9f / extent.y;
			final float factor = Math.min(factor1, factor2);
			final int positionX = x + (int)((width - extent.x * factor) / 2f);
			final int positionY = y + (int)((height - extent.y * factor) / 2f);

			// Initialize transformation
			final Transform transform = new Transform(gc.getDevice());
			transform.identity();
			transform.translate(positionX, positionY);
			transform.scale(factor, factor);
			gc.setTransform(transform);

			// Prepare
			final Path path = new Path(getDisplay());
			path.addString(string, 0, 0, gc.getFont());

			// Draw and reset
			final Color back = gc.getBackground();
			gc.setBackground(gc.getForeground());
			gc.fillPath(path);
			gc.setTransform(null);
			gc.setTextAntialias(SWT.OFF);
			gc.setAntialias(SWT.OFF);
			gc.setBackground(back);
			path.dispose();
			transform.dispose();
		}
	}

	/**
	 * Removes the current frame
	 */
	protected void removeFrame() {

		if (current instanceof FrameStatic) {
			if (!next.equals(current)) {
				update();
			} else {
				current.update();
				redraw();
			}
		} else {
			final Frame<T> old = current;
			final FrameStatic<T> source = ((FrameDynamic<T>) current).getTarget();
			current = source.clone();
			old.dispose();
			if (!next.equals(current)) {
				update();
			} else {
				current.update();
				redraw();
			}
		}
	}
}
