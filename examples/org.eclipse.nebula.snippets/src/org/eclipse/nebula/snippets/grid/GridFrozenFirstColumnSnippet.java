/*******************************************************************************
 * Copyright (c) 2026 Eclipse Nebula contributors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.nebula.snippets.grid;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridEditor;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Demonstrates a frozen first column in {@link Grid}.
 *
 * <p>Two scenarios are shown side by side:</p>
 * <ul>
 *   <li><b>Plain frozen column</b> &mdash; the first column stays visible while
 *       horizontal scrolling moves the rest of the columns behind it. The first
 *       column is also editable to verify {@link GridEditor} positioning.</li>
 *   <li><b>Frozen column inside a column group</b> &mdash; the frozen first
 *       column shares a {@link GridColumnGroup} with scrollable columns. This
 *       configuration is unsupported; the grid logs a one-time warning to
 *       stderr and degrades gracefully by omitting the group header from the
 *       frozen overlay.</li>
 * </ul>
 */
public class GridFrozenFirstColumnSnippet {

	private static final int COLUMN_COUNT = 8;
	private static final int ROW_COUNT = 30;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Grid - Frozen First Column");
		shell.setLayout(new GridLayout(1, false));
		shell.setSize(700, 500);

		createSimpleSection(shell);
		createGroupSection(shell);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private static void createSimpleSection(Composite parent) {
		Composite section = new Composite(parent, SWT.NONE);
		section.setLayout(new GridLayout(1, false));
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Grid grid = new Grid(section, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		grid.setHeaderVisible(true);
		grid.setLinesVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridColumn frozen = new GridColumn(grid, SWT.NONE);
		frozen.setText("Name (frozen)");
		frozen.setWidth(160);
		frozen.setFixed(true);

		for (int c = 1; c < COLUMN_COUNT; c++) {
			GridColumn column = new GridColumn(grid, SWT.NONE);
			column.setText("Column " + c);
			column.setWidth(120);
		}

		for (int r = 0; r < ROW_COUNT; r++) {
			GridItem item = new GridItem(grid, SWT.NONE);
			item.setText(0, "Row " + (r + 1));
			for (int c = 1; c < COLUMN_COUNT; c++) {
				item.setText(c, "r" + (r + 1) + "c" + c);
			}
		}

		attachFirstColumnEditor(grid);
	}

	private static void createGroupSection(Composite parent) {
		Composite section = new Composite(parent, SWT.NONE);
		section.setLayout(new GridLayout(1, false));
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Grid grid = new Grid(section, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		grid.setHeaderVisible(true);
		grid.setLinesVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridColumnGroup group = new GridColumnGroup(grid, SWT.NONE);
		group.setText("Spans frozen + scrolled (unsupported, warns once)");

		GridColumn frozen = new GridColumn(group, SWT.NONE);
		frozen.setText("Name (frozen)");
		frozen.setWidth(160);
		frozen.setFixed(true);

		GridColumn inGroup = new GridColumn(group, SWT.NONE);
		inGroup.setText("Group col");
		inGroup.setWidth(120);

		for (int c = 2; c < COLUMN_COUNT; c++) {
			GridColumn column = new GridColumn(grid, SWT.NONE);
			column.setText("Column " + c);
			column.setWidth(120);
		}

		for (int r = 0; r < ROW_COUNT; r++) {
			GridItem item = new GridItem(grid, SWT.NONE);
			for (int c = 0; c < COLUMN_COUNT; c++) {
				item.setText(c, "r" + (r + 1) + "c" + c);
			}
		}
	}

	private static void attachFirstColumnEditor(Grid grid) {
		final GridEditor editor = new GridEditor(grid);
		editor.grabHorizontal = true;
		grid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control old = editor.getEditor();
				if (old != null) {
					old.dispose();
				}
				GridItem[] selection = grid.getSelection();
				if (selection.length == 0) {
					return;
				}
				GridItem item = selection[0];
				Text text = new Text(grid, SWT.NONE);
				text.setText(item.getText(0));
				text.selectAll();
				text.setFocus();
				text.addListener(SWT.FocusOut, ev -> {
					item.setText(0, text.getText());
					text.dispose();
				});
				editor.setEditor(text, item, 0);
			}
		});
	}
}
