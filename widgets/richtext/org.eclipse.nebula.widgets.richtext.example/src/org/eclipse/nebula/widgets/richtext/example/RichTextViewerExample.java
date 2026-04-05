/*****************************************************************************
 * Copyright (c) 2015, 2026 CEA LIST.
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
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.richtext.example;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.richtext.RichTextEditor;
import org.eclipse.nebula.widgets.richtext.RichTextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RichTextViewerExample {

	public static void main(String[] args) {
		Display display = new Display();

		final Shell shell = new Shell(display);
		shell.setText("SWT Rich Text Editor example");
		shell.setSize(800, 600);

		shell.setLayout(new GridLayout(1, true));

		RichTextViewerExample example = new RichTextViewerExample();
		example.createControls(shell);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(2, true));

		final RichTextEditor editor = new RichTextEditor(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editor);

		final RichTextViewer viewer = new RichTextViewer(parent, SWT.BORDER | SWT.WRAP);
		viewer.setWordSplitRegex("\\s|\\-");//wrap after whitespace characters and delimiter
		GridDataFactory.fillDefaults().grab(true, true).span(1, 2).applyTo(viewer);

		final Text htmlOutput = new Text(parent,
				SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(SWT.DEFAULT, 100).applyTo(htmlOutput);

		Composite buttonPanel = new Composite(parent, SWT.NONE);
		buttonPanel.setLayout(new RowLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

		Button getButton = new Button(buttonPanel, SWT.PUSH);
		getButton.setText("Get text");
		getButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String htmlText = editor.getText();
				viewer.setText(htmlText);
				htmlOutput.setText(htmlText);
			}
		});
	}

}
