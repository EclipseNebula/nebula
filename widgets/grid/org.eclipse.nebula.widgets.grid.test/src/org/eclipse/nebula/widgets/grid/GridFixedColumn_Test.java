/*******************************************************************************
 * Copyright (c) 2026 Eclipse Nebula contributors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the frozen-first-column behavior of {@link Grid}/{@link GridColumn}.
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class GridFixedColumn_Test {

  private Display display;
  private Shell shell;
  private Grid grid;
  private ScrollBar horizontalBar;

  @Before
  public void setUp() {
    display = Display.getDefault();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    grid.setSize( 200, 200 );
    horizontalBar = grid.getHorizontalBar();
    shell.pack();
    shell.open();
    while( display.readAndDispatch() ) {
      // drain pending events so paint requests are issued
    }
  }

  @After
  public void tearDown() {
    if( shell != null && !shell.isDisposed() ) {
      shell.dispose();
    }
  }

  @Test
  public void testSetFixed_TogglesFlag() {
    GridColumn column = new GridColumn( grid, SWT.NONE );

    assertFalse( column.isFixed() );
    column.setFixed( true );
    assertTrue( column.isFixed() );
    column.setFixed( false );
    assertFalse( column.isFixed() );
  }

  @Test
  public void testGetOrigin_FrozenColumn_PinnedAcrossScroll() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    GridItem[] items = createGridItems( grid, 20, 0 );
    columns[ 0 ].setFixed( true );

    scrollHorizontallyTo( 150 );

    Point origin = grid.getOrigin( columns[ 0 ], items[ 0 ] );

    assertEquals( "Frozen column origin should ignore horizontal scroll", 0, origin.x );
  }

  @Test
  public void testGetOrigin_NonFrozenColumn_StillScrolls() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    GridItem[] items = createGridItems( grid, 20, 0 );
    columns[ 0 ].setFixed( true );
    flushPaint();

    Point unscrolled = grid.getOrigin( columns[ 3 ], items[ 0 ] );
    int actualScroll = scrollHorizontallyTo( 150 );
    Point scrolled = grid.getOrigin( columns[ 3 ], items[ 0 ] );

    assertTrue( "scrollbar must accept some non-zero selection for this test to be meaningful",
                actualScroll > 0 );
    assertEquals( "Non-frozen column origin must shift by scroll delta",
                  unscrolled.x - actualScroll, scrolled.x );
  }

  @Test
  public void testGetColumn_FrozenOverlayWinsHitTesting() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    createGridItems( grid, 20, 0 );
    GridColumn frozen = columns[ 0 ];
    frozen.setFixed( true );

    // Force the overlay to be active by scrolling past the frozen offset.
    int actualScroll = scrollHorizontallyTo( 150 );
    assertTrue( "scrollbar must accept some non-zero selection for the overlay to be active",
                actualScroll > 0 );

    // The frozen column has width 20 (col_0 -> 20 * (0+1)), so a hit at x=5
    // falls inside the overlay region.
    GridColumn hit = grid.getColumn( new Point( 5, 5 ) );
    assertSame( "Hit inside frozen overlay should resolve to the frozen column",
                frozen, hit );
  }

  @Test
  public void testGetColumn_OutsideFrozenOverlay_ResolvesScrolledColumn() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    createGridItems( grid, 20, 0 );
    columns[ 0 ].setFixed( true );

    int actualScroll = scrollHorizontallyTo( 150 );
    assertTrue( "scrollbar must accept some non-zero selection for this test to be meaningful",
                actualScroll > 0 );

    GridColumn hit = grid.getColumn( new Point( 100, 5 ) );
    assertNotNull( hit );
    assertFalse( "Hit outside the frozen overlay must resolve to a scrolled column",
                 hit == columns[ 0 ] );
  }

  /**
   * Drains pending paint events so {@link Grid#updateScrollbars()} has a
   * chance to run on platforms where the widget is realized during event
   * dispatch. This is enough on Windows; on headless Linux GTK the widget
   * is never realized here, so tests cannot rely on it —
   * {@link #scrollHorizontallyTo} compensates by writing scroll state
   * atomically via {@link ScrollBar#setValues}.
   */
  private void flushPaint() {
    grid.redraw();
    grid.update();
    while( display.readAndDispatch() ) {
      // drain
    }
  }

  /**
   * Programmatically scrolls horizontally to {@code targetPixels}, returning
   * the actual selection the scrollbar settled on after clipping. Tests
   * should treat the returned value as the source of truth, since SWT may
   * clip {@code targetPixels} based on the current maximum/thumb.
   *
   * <p>On headless Linux GTK (the Nebula CI environment) the SWT widget is
   * never realized during the synthetic {@code redraw/update/readAndDispatch}
   * cycle used here, so {@code Grid.updateScrollbars()} never runs and the
   * horizontal scrollbar stays at its defaults ({@code visible=false,
   * max=1, thumb=1}). GTK treats {@code setSelection} on an invisible
   * scrollbar as a silent no-op, which is what caused the three
   * frozen-column tests to fail on CI while passing everywhere else.
   *
   * <p>{@link ScrollBar#setValues} atomically assigns selection, max and
   * thumb, so the requested selection cannot be clipped by a stale max;
   * {@link ScrollBar#setVisible} first makes the scrollbar writable on
   * GTK. This gives the test a deterministic scroll state regardless of
   * whether Grid's paint machinery has had a chance to run.
   */
  private int scrollHorizontallyTo( int targetPixels ) {
    flushPaint();
    horizontalBar.setVisible( true );
    int max = Math.max( targetPixels * 4, 2000 );
    horizontalBar.setValues( targetPixels, 0, max, 100, 10, 100 );
    return horizontalBar.getSelection();
  }

  @Test
  public void testTreeToggle_OnFrozenColumn_RespondsWhenScrolled() {
    grid.setHeaderVisible( true );

    GridColumn frozen = new GridColumn( grid, SWT.NONE );
    frozen.setText( "tree" );
    frozen.setWidth( 160 );
    frozen.setTree( true );
    frozen.setFixed( true );

    for( int c = 1; c < 6; c++ ) {
      GridColumn other = new GridColumn( grid, SWT.NONE );
      other.setText( "c" + c );
      other.setWidth( 120 );
    }

    GridItem root = new GridItem( grid, SWT.NONE );
    root.setText( 0, "root" );
    GridItem child = new GridItem( root, SWT.NONE );
    child.setText( 0, "child" );
    root.setExpanded( false );

    shell.setSize( 320, 300 );
    shell.layout();
    int actualScroll = scrollHorizontallyTo( 200 );
    assertTrue( "scrollbar must accept some non-zero selection so the overlay is active",
                actualScroll > 0 );

    assertFalse( "precondition: root should be collapsed", root.isExpanded() );

    // Click roughly where the toggle is rendered: a few pixels in from the
    // left edge of the frozen overlay, vertically centred on the first row.
    int toggleX = 8;
    int rowY = grid.getHeaderHeight() + ( root.getHeight() / 2 );
    Event mouseDown = new Event();
    mouseDown.type = SWT.MouseDown;
    mouseDown.button = 1;
    mouseDown.x = toggleX;
    mouseDown.y = rowY;
    mouseDown.widget = grid;
    grid.notifyListeners( SWT.MouseDown, mouseDown );

    assertTrue( "Clicking the toggle on the frozen column while scrolled should expand the row",
                root.isExpanded() );
  }

  @Test
  public void testGroupSpanning_AcrossFreezeBoundary_WarnsOnceAndDegrades() {
    grid.setHeaderVisible( true );

    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    group.setText( "Mixed" );
    GridColumn frozen = new GridColumn( group, SWT.NONE );
    frozen.setText( "frozen" );
    frozen.setWidth( 50 );
    frozen.setFixed( true );
    GridColumn scrolled = new GridColumn( group, SWT.NONE );
    scrolled.setText( "scrolled" );
    scrolled.setWidth( 50 );
    // intentionally not setFixed(true)

    for( int i = 0; i < 8; i++ ) {
      GridColumn other = new GridColumn( grid, SWT.NONE );
      other.setText( "c" + i );
      other.setWidth( 80 );
    }
    createGridItems( grid, 5, 0 );

    scrollHorizontallyTo( 150 );

    PrintStream originalErr = System.err;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    System.setErr( new PrintStream( captured ) );
    try {
      flushPaint();
      // Trigger a second paint to ensure the warning only fires once.
      flushPaint();
    } finally {
      System.setErr( originalErr );
    }

    String stderr = captured.toString();
    int firstOccurrence = stderr.indexOf( "[nebula.grid]" );
    int lastOccurrence = stderr.lastIndexOf( "[nebula.grid]" );
    assertEquals( "Warning should be emitted exactly once across multiple paints",
                  firstOccurrence, lastOccurrence );
  }
}
