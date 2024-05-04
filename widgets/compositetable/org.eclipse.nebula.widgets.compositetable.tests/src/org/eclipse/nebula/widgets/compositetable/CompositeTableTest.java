/*******************************************************************************
 * Copyright (c) 2009 compeople AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * compeople AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.compositetable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import junit.framework.TestCase;

public class CompositeTableTest extends TestCase {
	private boolean createdDisplay = false;

	private static final class Row extends Composite {
		private final Text text;

		public Row(final Composite parent, final int style) {
			super(parent, style);
			setLayout(new ResizableGridRowLayout());
			text = new Text(this, SWT.BORDER | SWT.SINGLE);
		}

		@Override
		public String toString() {
			return text.getText();
		}
	}

	private Display display;
	private Shell shell;
	private CompositeTable table;
	private String[] words;

	@Override
	protected void setUp() throws Exception {
		display = Display.getCurrent();
		if (display == null) {
			display = new Display();
			createdDisplay = true;
		}
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(200, 200);

		table = new CompositeTable(shell, SWT.NONE);
		new Row(table, SWT.NONE);

		words = new String[] { "Basic", "Bash", "C", "C++", "Clipper", "Cobol", "Java", "ML", "Pascal", "Perl", "Python", "Ruby", "Scheme", "Smalltalk", };
		table.addRowContentProvider((sender, currentObjectOffset, rowControl) -> {
			final Row row = (Row) rowControl;
			final String word = words[currentObjectOffset];
			// System.out.println("refresh: " + currentObjectOffset + " " +
			// word);
			row.text.setText(word);
		});
		table.setNumRowsInCollection(words.length);
		table.setRunTime(true);

		shell.open();
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		if (createdDisplay) {
			display.dispose();
		}
	}

	// test methods
	// /////////////

	public void testGetRowControlsWithTopRowZero() throws Exception {
		assertEquals(0, table.getTopRow());
		final Control[] rowControls = table.getRowControls();
		for (int i = 0; i < rowControls.length; i++) {
			final Row row = (Row) rowControls[i];
			assertEquals(words[i], row.toString());
		}
	}

	public void testSetSelection() {
		table.setSelection(0, 0);
		readAndDispatch();

		assertEquals(new Point(0, 0), table.getSelection());

		table.setSelection(0, 1);
		readAndDispatch();

		assertEquals(new Point(0, 1), table.getSelection());

		table.setSelection(0, 10);
		readAndDispatch();

		assertEquals(new Point(0, 10 - table.getTopRow()), table.getSelection());
	}

	public void testClearSelection() {
		// shell.open() will set the selection to the first row
		assertEquals(new Point(-1, 0), table.getSelection());

		table.clearSelection();
		readAndDispatch();

		assertEquals(null, table.getSelection());
	}

	// helping methods
	// ////////////////

	private void readAndDispatch() {
		while (display.readAndDispatch()) {
			// next
		}
	}

}
