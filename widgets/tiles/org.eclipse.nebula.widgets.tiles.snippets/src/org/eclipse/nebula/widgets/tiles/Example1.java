/* ******************************************************************************
 * Copyright (c) 2014 - 2015 Fabian Prasser.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Fabian Prasser - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.tiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Example for using the Tiles SWT Widget
 * 
 * @author Fabian Prasser
 */
public class Example1 {

    /**
     * Main entry point
     * @param args
     */
	public static void main(final String[] args) {
		
		// Create shell
		final Display display = new Display ();
		final Shell shell = new Shell(display);
		shell.setText("SWT Tiles Example");
		shell.setLayout(new FillLayout());
	
		// Create tiles
		final Tiles<Integer> tiles = new Tiles<>(shell, SWT.NONE);
		
		// Add items
		final List<Integer> items = new ArrayList<>();
		for (int i=0; i < 500; i++){
			items.add(i);
		}
		tiles.setItems(items);
		
		// Set layout
		tiles.setTileLayout(new TileLayoutDynamic(10, 10, 5, 5));
		
		// Order and filter
		final Filter<Integer> filter1 = getFilter1();
		final Filter<Integer> filter2 = getFilter2();
		tiles.setComparator(getComparator());
		tiles.setFilter(filter1);
		
		// Decorators
		tiles.setDecoratorBackgroundColor(getDecoratorBackgroundColor(tiles));
		tiles.setDecoratorForegroundColor(getDecoratorForegroundColor(tiles));
		tiles.setDecoratorLabel(getDecoratorLabel(tiles));
		tiles.setDecoratorLineColor(getDecoratorLineColor(tiles));
		tiles.setDecoratorLineStyle(getDecoratorLineStyle(tiles));
		tiles.setDecoratorLineWidth(getDecoratorLineWidth(tiles));
		tiles.setDecoratorTooltip(getDecoratorTooltip(tiles));
		tiles.update();
		
		// Listeners
		tiles.addSelectionListener(new SelectionAdapter(){
			
			Filter<Integer> filter = filter1;
			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				if (arg0.data.equals(80)){
					if (filter == filter1) {
						filter = filter2;
					} else {
						filter = filter1;
					}
					tiles.setFilter(filter);
					tiles.update();
				}
			}
		});
		
		tiles.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDown(final MouseEvent arg0) {
				if (arg0.button == 3) {
					if (tiles.getMenu() != null) {
						tiles.getMenu().dispose();
					}
					
					final Integer selected = tiles.getSelectedItem();
					
					if (selected != null) {
    					final Menu menu = new Menu(tiles);
    					final MenuItem item1 = new MenuItem(menu, SWT.NONE);
    					item1.setText(String.valueOf(selected));
    					tiles.setMenu(menu);
    					menu.setLocation(tiles.toDisplay(arg0.x, arg0.y));
    					menu.setVisible(true);
					}
				}
			}
		});
		
		// Pack and show
		shell.pack();
		shell.open ();
		shell.setSize(800, 600);
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) {
				display.sleep ();
			}
		}
		display.dispose ();
	}

	/**
	 * Returns the comparator
	 * @return
	 */
	private static Comparator<Integer> getComparator() {
		return (t1, t2) -> t1.compareTo(t2);
	}

	/**
	 * Returns the background decorator
	 * @param tiles
	 * @return
	 */
	private static DecoratorColor<Integer> getDecoratorBackgroundColor(final Tiles<Integer> tiles) {
		
		final Gradient gradient = new GradientHeatscale(tiles);
		
		final DecoratorColor<Integer> decorator = new DecoratorColorGradient<Integer>(gradient){
			@Override
			protected double getValue(final Integer element) {
				final double result = element / 140d;
				return result <= 1.0d ? result : 1.0d;
			}
		};
		
		decorator.addDecoratorListener(() -> gradient.dispose());
		
		return decorator;
	}

	/**
	 * Returns the foreground decorator
	 * @param tiles
	 * @return
	 */
	private static DecoratorColor<Integer> getDecoratorForegroundColor(final Tiles<Integer> tiles) {
		
		final Color black = new Color(tiles.getDisplay(), 0, 0, 0);
		
		final DecoratorColor<Integer> decorator = new DecoratorColor<Integer>() {
			@Override
			public Color decorate(final Integer t) {
				return black;
			}
		};
		
		decorator.addDecoratorListener(() -> black.dispose());
		
		return decorator;
	}

	/**
	 * Returns the label decorator
	 * @param tiles
	 * @return
	 */
	private static DecoratorString<Integer> getDecoratorLabel(final Tiles<Integer> tiles) {
		return new DecoratorString<Integer>(){
			@Override
			public String decorate(final Integer t) {
				return t.toString();
			}
		};
	}

	/**
	 * Returns the line color decorator
	 * @param tiles
	 * @return
	 */
	private static DecoratorColor<Integer> getDecoratorLineColor(final Tiles<Integer> tiles) {
		
		final Color black = new Color(tiles.getDisplay(), 0, 0, 0);
		final Color blue = new Color(tiles.getDisplay(), 0, 0, 255);
		
		final DecoratorColor<Integer> decorator = new DecoratorColor<Integer>() {
			@Override
			public Color decorate(final Integer t) {
				if (isPrime(t)) {
					return black;
				} else {
					return t % 2 == 0 ? blue : black;
				}
			}
		};
		
		decorator.addDecoratorListener(() -> {
			black.dispose();
			blue.dispose();
		});
		
		return decorator;
	}

	/**
	 * Returns the line style decorator
	 * @param tiles
	 * @return
	 */
	private static DecoratorInteger<Integer> getDecoratorLineStyle(final Tiles<Integer> tiles) {
		return new DecoratorInteger<Integer>() {

			@Override
			public Integer decorate(final Integer t) {
				if (isPrime(t)) {
					return SWT.LINE_DASH;
				} else {
					return SWT.LINE_SOLID;
				}
			}
		};
	}

	/**
	 * Returns the line width decorator
	 * @param tiles
	 * @return
	 */
	private static DecoratorInteger<Integer> getDecoratorLineWidth(final Tiles<Integer> tiles) {
		return new DecoratorInteger<Integer>() {
			@Override
			public Integer decorate(final Integer t) {
				if (isPrime(t)) {
					return 3;
				} else {
					return t % 2 == 0 ? 3 : 1;
				}
			}
		};
	}

	/**
	 * Returns the tooltip decorator
	 * @param tiles
	 * @return
	 */
	private static DecoratorString<Integer> getDecoratorTooltip(final Tiles<Integer> tiles) {
		return new DecoratorString<Integer>() {
			@Override
			public String decorate(final Integer t) {
				return "Item: " + t+"\nAgain: "+t;
			}
		};
	}

	/**
	 * Returns the first filter
	 * @return
	 */
	private static Filter<Integer> getFilter1() {
		return t -> t < 90;
	}

	/**
	 * Returns the second filter
	 * @return
	 */
	private static Filter<Integer> getFilter2() {
		return t -> t >= 40 && t < 130;
	}
	
	/**
	 * Simple prime test
	 * @param number
	 * @return
	 */
	private static boolean isPrime(final int number){
		if (number<=1) {
			return false;
		}
		for (int i=2; i<500; i++){
			if (i!=number && number%i==0){
				return false;
			}
		}
		return true;
	}
}
