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

    horizontalBar.setSelection( 150 );

    Point origin = grid.getOrigin( columns[ 0 ], items[ 0 ] );

    assertEquals( "Frozen column origin should ignore horizontal scroll", 0, origin.x );
  }

  @Test
  public void testGetOrigin_NonFrozenColumn_StillScrolls() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    GridItem[] items = createGridItems( grid, 20, 0 );
    columns[ 0 ].setFixed( true );

    Point unscrolled = grid.getOrigin( columns[ 3 ], items[ 0 ] );
    horizontalBar.setSelection( 150 );
    Point scrolled = grid.getOrigin( columns[ 3 ], items[ 0 ] );

    assertEquals( "Non-frozen column origin must shift by scroll delta",
                  unscrolled.x - 150, scrolled.x );
  }

  @Test
  public void testGetColumn_FrozenOverlayWinsHitTesting() {
    GridColumn[] columns = createGridColumns( grid, 10, SWT.NONE );
    createGridItems( grid, 20, 0 );
    GridColumn frozen = columns[ 0 ];
    frozen.setFixed( true );

    // Force the overlay to be active by scrolling past the frozen offset.
    horizontalBar.setSelection( 150 );

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

    horizontalBar.setSelection( 150 );

    GridColumn hit = grid.getColumn( new Point( 100, 5 ) );
    assertNotNull( hit );
    assertFalse( "Hit outside the frozen overlay must resolve to a scrolled column",
                 hit == columns[ 0 ] );
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

    horizontalBar.setSelection( 150 );

    PrintStream originalErr = System.err;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    System.setErr( new PrintStream( captured ) );
    try {
      grid.redraw();
      grid.update();
      while( display.readAndDispatch() ) {
        // drain
      }
      // Trigger a second paint to ensure the warning only fires once.
      grid.redraw();
      grid.update();
      while( display.readAndDispatch() ) {
        // drain
      }
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
