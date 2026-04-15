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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * This class implements a static frame
 * @author Fabian Prasser
 *
 * @param <T>
 */
class FrameStatic<T> extends Frame<T> implements Cloneable  {

	/** Elements */
	private List<T>             elements    = new ArrayList<>();

	/** Filter*/
	private Filter<T>           filter;
	/** Comparator*/
	private Comparator<T>       comparator;

	/** Rendered elements*/
	private final List<Tile<T>>       rendered    = new ArrayList<>();
	/** The current background color */
	private Color               background;
	/** Layout*/
	private int                 width       = 0;
	/** Layout*/
	private int                 height      = 0;

	/** Layout*/
	private int                 tileWidth   = 0;
	/** Layout*/
	private int                 tileHeight  = 0;
	/** Layout*/
	private int                 tileMarginX = 0;
	/** Layout*/
	private int                 tileMarginY = 0;

	/** Decorator */
	private DecoratorInteger<T> lineWidthDecorator;
	/** Decorator */
	private DecoratorInteger<T> lineStyleDecorator;
	/** Decorator */
	private DecoratorColor<T>   foregroundColorDecorator;
	/** Decorator */
	private DecoratorColor<T>   backgroundColorDecorator;
	/** Decorator */
	private DecoratorColor<T>   lineColorDecorator;
	/** Decorator */
	private DecoratorString<T>  labelDecorator;

	/** Tiles*/
	private final Tiles<T>            tiles;

	/**
	 * Clone constructor
	 * @param backgroundColor
	 * @param foregroundColor
	 * @param lineColor
	 * @param elements
	 * @param filter
	 * @param Comparator
	 * @param background
	 * @param width
	 * @param height
	 * @param tileWidth
	 * @param tileHeight
	 * @param tileMarginX
	 * @param tileMarginY
	 * @param lineWidthDecorator
	 * @param lineStyleDecorator
	 * @param foregroundColorDecorator
	 * @param backgroundColorDecorator
	 * @param lineColorDecorator
	 * @param labelDecorator
	 * @param tiles
	 */
	FrameStatic(final List<T> elements, final Filter<T> filter, final Comparator<T> Comparator,
			final Color background, final int width, final int height, final int tileWidth,
			final int tileHeight, final int tileMarginX, final int tileMarginY,
			final DecoratorInteger<T> lineWidthDecorator,
			final DecoratorInteger<T> lineStyleDecorator,
			final DecoratorColor<T> foregroundColorDecorator,
			final DecoratorColor<T> backgroundColorDecorator,
			final DecoratorColor<T> lineColorDecorator,
			final DecoratorString<T> labelDecorator, final Tiles<T> tiles) {

		super(tiles);

		// Prepare resources
		this.elements = elements;
		this.filter = filter;
		this.comparator = Comparator;
		this.background = background;
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.tileMarginX = tileMarginX;
		this.tileMarginY = tileMarginY;
		this.lineWidthDecorator = lineWidthDecorator;
		this.lineStyleDecorator = lineStyleDecorator;
		this.foregroundColorDecorator = foregroundColorDecorator;
		this.backgroundColorDecorator = backgroundColorDecorator;
		this.lineColorDecorator = lineColorDecorator;
		this.labelDecorator = labelDecorator;
		this.tiles = tiles;

		this.lineWidthDecorator.use();
		this.lineStyleDecorator.use();
		this.foregroundColorDecorator.use();
		this.backgroundColorDecorator.use();
		this.lineColorDecorator.use();
		this.labelDecorator.use();
	}

	/**
	 * Creates a new instance
	 * @param tiles
	 */
	FrameStatic(final Tiles<T> tiles){

		super(tiles);

		// Store
		this.tiles = tiles;

		// Prepare resources
		final Color backgroundColor = new Color(255, 255, 255);
		final Color lineColor = new Color(0, 0, 0);
		final Color foregroundColor = new Color(0, 0, 0);

		// Initial settings
		this.background = tiles.getBackground();
		this.lineWidthDecorator = new DecoratorIntegerConstant<>(1);
		this.lineStyleDecorator = new DecoratorIntegerConstant<>(SWT.LINE_SOLID);
		this.foregroundColorDecorator = new DecoratorColorConstant<>(foregroundColor);
		this.foregroundColorDecorator.addDecoratorListener(() -> foregroundColor.dispose());
		this.backgroundColorDecorator = new DecoratorColorConstant<>(backgroundColor);
		this.backgroundColorDecorator.addDecoratorListener(() -> backgroundColor.dispose());
		this.lineColorDecorator = new DecoratorColorConstant<>(lineColor);
		this.lineColorDecorator.addDecoratorListener(() -> lineColor.dispose());
		this.labelDecorator = new DecoratorStringToString<>();
		this.filter = t -> true;
		this.comparator = (t1, t2) -> 0;

		// Control size
		final Point p = tiles.getSize();
		this.width = p.x;
		this.height = p.y;

		this.lineWidthDecorator.use();
		this.lineStyleDecorator.use();
		this.foregroundColorDecorator.use();
		this.backgroundColorDecorator.use();
		this.lineColorDecorator.use();
		this.labelDecorator.use();
	}

	@Override
	public FrameStatic<T> clone(){
		return new FrameStatic<>(elements, filter, comparator, background, width,
				height, tileWidth, tileHeight, tileMarginX, tileMarginY,
				lineWidthDecorator, lineStyleDecorator,
				foregroundColorDecorator, backgroundColorDecorator,
				lineColorDecorator, labelDecorator, tiles);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FrameStatic<?> other = (FrameStatic<?>) obj;
		if (!Objects.equals(background, other.background)) {
			return false;
		}
		if (!Objects.equals(backgroundColorDecorator, other.backgroundColorDecorator)) {
			return false;
		}
		if (!Objects.equals(comparator, other.comparator)) {
			return false;
		}
		if (!Objects.equals(elements, other.elements)) {
			return false;
		}
		if (!Objects.equals(filter, other.filter)) {
			return false;
		}
		if (!Objects.equals(foregroundColorDecorator, other.foregroundColorDecorator)) {
			return false;
		}
		if (height != other.height) {
			return false;
		}
		if (!Objects.equals(labelDecorator, other.labelDecorator)) {
			return false;
		}
		if (!Objects.equals(lineColorDecorator, other.lineColorDecorator)) {
			return false;
		}
		if (!Objects.equals(lineStyleDecorator, other.lineStyleDecorator)) {
			return false;
		}
		if (!Objects.equals(lineWidthDecorator, other.lineWidthDecorator)) {
			return false;
		}
		if (tileHeight != other.tileHeight) {
			return false;
		}
		if (tileMarginX != other.tileMarginX) {
			return false;
		}
		if (tileMarginY != other.tileMarginY) {
			return false;
		}
		if (tileWidth != other.tileWidth) {
			return false;
		}
		if (!Objects.equals(tiles, other.tiles)) {
			return false;
		}
		if (width != other.width) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(background, backgroundColorDecorator, comparator, elements, filter, foregroundColorDecorator, height, labelDecorator, lineColorDecorator, lineStyleDecorator, lineWidthDecorator, tileHeight, tileMarginX, tileMarginY, tileWidth, tiles, width);
	}

	@Override
	public String toString() {
		return "FrameStatic [elements=" + elements + ", filter=" + filter + ", comparator=" + comparator + ", background=" + background + ", width=" + width + ", height=" + height + ", tileWidth=" +
				tileWidth + ", tileHeight=" + tileHeight + ", tileMarginX=" + tileMarginX + ", tileMarginY=" + tileMarginY + ", lineWidthDecorator=" + lineWidthDecorator + ", lineStyleDecorator=" +
				lineStyleDecorator + ", foregroundColorDecorator=" + foregroundColorDecorator + ", backgroundColorDecorator=" + backgroundColorDecorator + ", lineColorDecorator=" + lineColorDecorator +
				", labelDecorator=" + labelDecorator + ", tiles=" + tiles + "]";
	}

	/**
	 * Free resources
	 */
	@Override
	protected void dispose() {

		this.lineWidthDecorator.free();
		this.lineStyleDecorator.free();
		this.foregroundColorDecorator.free();
		this.backgroundColorDecorator.free();
		this.lineColorDecorator.free();
		this.labelDecorator.free();
	}

	@Override
	protected Color getBackground() {
		return background;
	}

	@Override
	protected Comparator<T> getComparator() {
		return this.comparator;
	}

	@Override
	protected DecoratorColor<T> getDecoratorBackgroundColor(){
		return this.backgroundColorDecorator;
	}

	@Override
	protected DecoratorColor<T> getDecoratorForegroundColor(){
		return this.foregroundColorDecorator;
	}

	@Override
	protected DecoratorString<T> getDecoratorLabel(){
		return this.labelDecorator;
	}

	@Override
	protected DecoratorColor<T> getDecoratorLineColor(){
		return this.lineColorDecorator;
	}

	@Override
	protected DecoratorInteger<T> getDecoratorLineStyle(){
		return this.lineStyleDecorator;
	}

	@Override
	protected DecoratorInteger<T> getDecoratorLineWidth(){
		return this.lineWidthDecorator;
	}

	@Override
	protected Filter<T> getFilter() {
		return this.filter;
	}

	@Override
	protected List<T> getItems() {
		return new ArrayList<>(this.elements);
	}

	/**
	 * @return the tileHeight
	 */
	protected int getTileHeight() {
		return tileHeight;
	}

	@Override
	protected List<Tile<T>> getTiles() {
		return rendered;
	}

	/**
	 * @return the tileWidth
	 */
	protected int getTileWidth() {
		return tileWidth;
	}

	/**
	 * Setter
	 * @param arg0
	 */
	protected void setBackground(final Color arg0) {
		this.background = arg0;
	}

	/**
	 * Sets an Comparator
	 *
	 * @param comparator
	 */
	protected void setComparator(final Comparator<T> comparator) {
		this.comparator = comparator;
	}

	/**
	 * Setter
	 * @param decorator
	 */
	protected void setDecoratorBackgroundColor(final DecoratorColor<T> decorator){
		this.backgroundColorDecorator.free();
		this.backgroundColorDecorator = decorator;
		this.backgroundColorDecorator.use();
	}

	/**
	 * Setter
	 * @param decorator
	 */
	protected void setDecoratorForegroundColor(final DecoratorColor<T> decorator){
		this.foregroundColorDecorator.free();
		this.foregroundColorDecorator = decorator;
		this.foregroundColorDecorator.use();
	}

	/**
	 * Setter
	 * @param decorator
	 */
	protected void setDecoratorLabel(final DecoratorString<T> decorator){
		this.labelDecorator.free();
		this.labelDecorator = decorator;
		this.labelDecorator.use();
	}


	/**
	 * Setter
	 * @param decorator
	 */
	protected void setDecoratorLineColor(final DecoratorColor<T> decorator){
		this.lineColorDecorator.free();
		this.lineColorDecorator = decorator;
		this.lineColorDecorator.use();
	}


	/**
	 * Setter
	 * @param decorator
	 */
	protected void setDecoratorLineStyle(final DecoratorInteger<T> decorator){
		this.lineStyleDecorator.free();
		this.lineStyleDecorator = decorator;
		this.lineStyleDecorator.use();
	}

	/**
	 * Setter
	 * @param decorator
	 */
	protected void setDecoratorLineWidth(final DecoratorInteger<T> decorator){
		this.lineWidthDecorator.free();
		this.lineWidthDecorator = decorator;
		this.lineWidthDecorator.use();
	}


	/**
	 * Sets a filter
	 *
	 * @param filter
	 */
	protected void setFilter(final Filter<T> filter) {
		this.filter = filter;
	}


	/**
	 * Sets the height
	 * @param height
	 */
	protected void setHeight(final int height){
		this.height = height;
	}


	/**
	 * Sets the items
	 * @param items
	 */
	protected void setItems(final List<T> items){
		this.elements.clear();
		this.elements.addAll(items);
	}


	/**
	 * Sets the tile width
	 * @param width
	 */
	protected void setTileHeight(final int height) {
		this.tileHeight = height;
	}


	/**
	 * Sets the tile width
	 * @param width
	 */
	protected void setTileMarginX(final int margin) {
		this.tileMarginX = margin;
	}

	/**
	 * Sets the tile width
	 * @param width
	 */
	protected void setTileMarginY(final int margin) {
		this.tileMarginY = margin;
	}

	/**
	 * Sets the tile width
	 * @param width
	 */
	protected void setTileWidth(final int width) {
		this.tileWidth = width;
	}

	/**
	 * Sets the width
	 * @param width
	 */
	protected void setWidth(final int width){
		this.width = width;
	}

	@Override
	protected void update(){

		// Clear
		this.rendered.clear();

		// Tile location
		int x = tileMarginX;
		int y = tileMarginY;

		// Prepare
		final List<T> active = new ArrayList<>();
		for (final T element : this.elements) {
			if (filter.accepts(element)) {
				active.add(element);
			}
		}
		Collections.sort(active, comparator);
		for (final T element : active) {

			// Decorate
			final int lineWidth = lineWidthDecorator.decorate(element);
			final int lineStyle = lineStyleDecorator.decorate(element);
			final Color lineColor = lineColorDecorator.decorate(element);
			final Color foregroundColor = foregroundColorDecorator.decorate(element);
			final Color backgroundColor = backgroundColorDecorator.decorate(element);
			final String label = labelDecorator.decorate(element);

			// Store
			rendered.add(new Tile<>(element, x, y, tileWidth, tileHeight,
					label, lineWidth, lineStyle, lineColor, foregroundColor, backgroundColor));

			// Layout
			x += tileWidth + tileMarginX;
			if (x + tileWidth + tileMarginX > width) {
				y += tileHeight + tileMarginY;
				x = tileMarginX;
			}

			// Stop if no more space
			if (y + tileHeight + tileMarginY > height) {
				break;
			}
		}
	}
}
