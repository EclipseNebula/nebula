/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Copilot - initial implementation for FILL column resize fix
 *******************************************************************************/
package org.eclipse.nebula.snippets.grid;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates FILL column resize behavior.
 * 
 * Column A has SWT.FILL style and fills available space.
 * Column B is a normal fixed-width column.
 * 
 * When you drag the divider between Column A and Column B:
 * - Column B's width changes (gets smaller or larger)
 * - Column A automatically adjusts its fill to use remaining space
 * 
 * This snippet demonstrates the fix for the issue where resizing on the
 * right edge of a FILL column didn't work properly.
 */
public class GridSnippetFillColumnResize {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Grid FILL Column Resize Demo");
        shell.setLayout(new FillLayout());

        Grid grid = new Grid(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        grid.setHeaderVisible(true);
        
        // Column A with FILL style - will fill available space
        GridColumn columnA = new GridColumn(grid, SWT.FILL);
        columnA.setText("Column A (FILL)");
        columnA.setWidth(150);
        
        // Column B - normal fixed-width column
        GridColumn columnB = new GridColumn(grid, SWT.NONE);
        columnB.setText("Column B (Fixed)");
        columnB.setWidth(150);
        
        // Column C - another normal column
        GridColumn columnC = new GridColumn(grid, SWT.NONE);
        columnC.setText("Column C (Fixed)");
        columnC.setWidth(100);
        
        // Add some data to visualize the columns
        for (int i = 0; i < 20; i++) {
            GridItem item = new GridItem(grid, SWT.NONE);
            item.setText(0, "Row " + i + " Col A");
            item.setText(1, "Row " + i + " Col B");
            item.setText(2, "Row " + i + " Col C");
        }
        
        shell.setSize(600, 400);
        shell.open();
        
        System.out.println("Instructions:");
        System.out.println("1. Try resizing the divider between Column A (FILL) and Column B (Fixed)");
        System.out.println("2. Column B should resize, and Column A should fill the remaining space");
        System.out.println("3. The resize handle on the right edge of Column A should now work correctly");
        
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
