/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.switchbutton.snippets;

import org.eclipse.nebula.widgets.opal.switchbutton.SwitchButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple snippet for the SwitchButton Widget
 */
public class SwitchButtonSnippet {

	public static void main(final String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("SwitchButton Snippet");
		shell.setSize(600, 600);
		shell.setLayout(new GridLayout(1, false));

		// Default
		final SwitchButton button1 = new SwitchButton(shell, SWT.NONE);
		button1.setText("Default switchButton");
		button1.setSelection(false);
		
		// With a border
		final SwitchButton button2 = new SwitchButton(shell, SWT.NONE);
		button2.setBorderColor(display.getSystemColor(SWT.COLOR_DARK_RED));
		button2.setTextForSelect("Selected...");
		button2.setTextForUnselect("Unselected...");
		button2.setText("Default switchButton with border");

		// Disabled
		final SwitchButton button3Off = new SwitchButton(shell, SWT.NONE);
		button3Off.setEnabled(false);
		button3Off.setText("Default switchButton disabled (OFF State)");
		final SwitchButton button3On = new SwitchButton(shell, SWT.NONE);
		button3On.setSelection(true);
		button3On.setEnabled(false);
		button3On.setText("Default switchButton disabled (ON State)");

		// Without glow effect
		final SwitchButton button4 = new SwitchButton(shell, SWT.NONE);
		button4.setFocusColor(null);
		button4.setText("Default switchButton with no focus effect");

		// No text
		final SwitchButton button5 = new SwitchButton(shell, SWT.NONE);
		button5.setSelection(true);
		button5.setText("");

		// Square
		final SwitchButton button6 = new SwitchButton(shell, SWT.NONE);
		button6.setRound(false);
		button6.setText("Square");

		// Full of color
		final SwitchButton button7 = new SwitchButton(shell, SWT.NONE);
		button7.setButtonBackgroundColor1(display.getSystemColor(SWT.COLOR_DARK_BLUE));
		button7.setButtonBackgroundColor2(display.getSystemColor(SWT.COLOR_BLUE));
		button7.setButtonBorderColor(display.getSystemColor(SWT.COLOR_RED));

		button7.setSelectedBackgroundColor(display.getSystemColor(SWT.COLOR_BLACK));
		button7.setSelectedForegroundColor(display.getSystemColor(SWT.COLOR_WHITE));

		button7.setUnselectedBackgroundColor(display.getSystemColor(SWT.COLOR_RED));
		button7.setUnselectedForegroundColor(display.getSystemColor(SWT.COLOR_GRAY));

		button7.setText("New colors");

		// Listeners
		final SwitchButton button8 = new SwitchButton(shell, SWT.NONE);
		button8.setText("Listeners...");
		button8.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				System.out.println("The new selection is... " + button8.getSelection());
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		// Listeners with doit=false !
		final SwitchButton button9 = new SwitchButton(shell, SWT.NONE);
		button9.setText("Listeners with doit = false...");
		button9.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				System.out.println("I don't want this");
				e.doit = false;
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		// Change font
		final SwitchButton button10 = new SwitchButton(shell, SWT.NONE);
		final Font font = new Font(display, "Courier New", 18, SWT.BOLD | SWT.ITALIC);
		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				font.dispose();
			}
		});
		button10.setFont(font);
		button10.setText("New font");

		// Change background & foreground color
		final SwitchButton button11 = new SwitchButton(shell, SWT.NONE);
		button11.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
		button11.setForeground(display.getSystemColor(SWT.COLOR_RED));
		button11.setText("And now for something completely different");

		// Change margins
		final SwitchButton button12 = new SwitchButton(shell, SWT.NONE);
		button12.setInsideMargin(8, 2);
		button12.setText("With bigger margins and arc");
		button12.setArc(4);

		// Register listener via Widget
		final SwitchButton button13 = new SwitchButton(shell, SWT.NONE);
		button13.setTextForSelect("  ");
		button13.setTextForUnselect("  ");
		button13.setInsideMargin(8, 2);
		button13.setFocusColor(null);
		button13.setText("No text, Using low level SWT observer functions");
		button13.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				event.display.beep();
			}
		});

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();

	}
}
