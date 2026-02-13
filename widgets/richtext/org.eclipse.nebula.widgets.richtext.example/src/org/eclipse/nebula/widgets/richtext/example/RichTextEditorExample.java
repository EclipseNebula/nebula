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

import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.richtext.RichTextEditor;
import org.eclipse.nebula.widgets.richtext.RichTextEditorConfiguration;
import org.eclipse.nebula.widgets.richtext.toolbar.ToolbarButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RichTextEditorExample {

	boolean buttonVisible = false;

	public static void main(String[] args) {
		Display display = new Display();

		final Shell shell = new Shell(display);
		shell.setText("SWT Rich Text Editor example");
		shell.setSize(800, 600);

		shell.setLayout(new GridLayout(1, true));

		RichTextEditorExample example = new RichTextEditorExample();
		example.createControls(shell);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(1, true));

		RichTextEditorConfiguration editorConfig = new RichTextEditorConfiguration();
		editorConfig.setEnableNativeSpellChecker(true);
		editorConfig.setLanguage(Locale.ENGLISH);
		final RichTextEditor editor = new RichTextEditor(parent, editorConfig);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editor);

		final Text htmlOutput = new Text(parent,
				SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 100).applyTo(htmlOutput);

		editor.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				htmlOutput.setText(editor.getText());
			}
		});

		editor.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("keycode: " + e.keyCode);
				System.out.println("character: " + e.character);
			}
		});

		editor.addToolbarButton(new ToolbarButton("jsButton",
				"myScriptCommand", "Execute Javascript", "other,0",
				RichTextEditorExample.class.getResource("images/environment_co.gif")) {
			@Override
			public String getJavascriptToExecute() {
				return "alert('Javascript call - navigator.userAgent:\\n\\n' + navigator.userAgent)";
			}
		});

		// add additional controls for showing interactions
		createControlPanel(parent, editor);
	}

	protected void createControlPanel(Composite parent, final RichTextEditor editor) {
		Composite controlPanel = new Composite(parent, SWT.NONE);
		controlPanel.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(controlPanel);

		Label inputLabel = new Label(controlPanel, SWT.NONE);
		inputLabel.setText("Text to set:");
		GridDataFactory.fillDefaults().applyTo(inputLabel);

		final Text input = new Text(controlPanel, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(input);

		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					editor.setText(input.getText());
				}
			}
		});

		Button setButton = new Button(controlPanel, SWT.PUSH);
		setButton.setText("Set Text");
		GridDataFactory.defaultsFor(setButton).applyTo(setButton);

		setButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.setText(input.getText());
			}
		});

		Composite buttonPanel = new Composite(controlPanel, SWT.NONE);
		buttonPanel.setLayout(new RowLayout());
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(buttonPanel);

		Button getButton = new Button(buttonPanel, SWT.PUSH);
		getButton.setText("Get text");
		getButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(null, "Editor Input",
						editor.getText());
			}
		});

		final Button enableButton = new Button(buttonPanel, SWT.PUSH);
		boolean editable = editor.isEditable();
		enableButton.setText(!editable ? "Enable" : "Disable");
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean editable = editor.isEditable();
				editor.setEditable(!editable);

				enableButton.setText(editable ? "Enable" : "Disable");
			}
		});

		final Button updateButton = new Button(buttonPanel, SWT.PUSH);
		updateButton.setText("Update Toolbar");

		final ToolbarButton button = new ToolbarButton("javaButton",
				"myJavaCommand", "Execute Java", "other",
				RichTextEditorExample.class.getResource("images/debug_exc.gif")) {

			@Override
			public Object execute() {
				MessageDialog.openInformation(null, "Information",
						"Java callback: " + editor.getSelectedHTML());

				editor.insertHTML("<em>" + editor.getSelectedHTML() + "</em>");
				return null;
			}
		};

		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!buttonVisible) {
					editor.addToolbarButton(button);
				} else {
					editor.removeToolbarButton(button);
				}
				buttonVisible = !buttonVisible;
				editor.updateToolbar();
			}
		});

		Button setFocusButton = new Button(buttonPanel, SWT.PUSH);
		setFocusButton.setText("Set Focus");
		setFocusButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.setFocus();
			}
		});

		Button changeLocaleButton = new Button(buttonPanel, SWT.PUSH);
		changeLocaleButton.setText("Change Locale");
		changeLocaleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object currentLang =
						editor.getEditorConfiguration().getAllOptions().get(RichTextEditorConfiguration.LANGUAGE);
				if ("de".equals(currentLang)) {
					editor.setLanguage("en");
				} else {
					editor.setLanguage("de");
				}
			}
		});
	}
}
