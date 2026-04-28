/*******************************************************************************
 * Copyright (c) 2026 Christoph Läubrich
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.switchbutton.snippets;

import org.eclipse.nebula.widgets.opal.switchbutton.TriState;
import org.eclipse.nebula.widgets.opal.switchbutton.TriStateSwitchButton;
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
 * A simple snippet for the TriStateSwitchButton widget.
 *
 * <p>
 * Click behavior:
 * <ul>
 * <li>When FIRST or SECOND: any click → OFF</li>
 * <li>When OFF: click left section → FIRST, click right section → SECOND, click
 * center section → no change</li>
 * </ul>
 * </p>
 */
public class TriStateSwitchButtonSnippet {

	public static void main(final String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("TriStateSwitchButton Snippet");
		shell.setSize(700, 700);
		shell.setLayout(new GridLayout(1, false));

		// Default – starts in OFF state
		final TriStateSwitchButton button1 = new TriStateSwitchButton(shell, SWT.NONE);
		button1.setText("Default (starts OFF)");

		// Custom section labels
		final TriStateSwitchButton button2 = new TriStateSwitchButton(shell, SWT.NONE);
		button2.setTextForFirst("Left");
		button2.setTextForOff("Neutral");
		button2.setTextForSecond("Right");
		button2.setText("Custom labels");

		// Starts in FIRST state
		final TriStateSwitchButton button3First = new TriStateSwitchButton(shell, SWT.NONE);
		button3First.setState(TriState.FIRST);
		button3First.setText("Starts in FIRST state");

		// Starts in SECOND state
		final TriStateSwitchButton button3Second = new TriStateSwitchButton(shell, SWT.NONE);
		button3Second.setState(TriState.SECOND);
		button3Second.setText("Starts in SECOND state");

		// With a border
		final TriStateSwitchButton button4 = new TriStateSwitchButton(shell, SWT.NONE);
		button4.setBorderColor(display.getSystemColor(SWT.COLOR_DARK_RED));
		button4.setText("With border");

		// Disabled – all three starting states
		final TriStateSwitchButton button5Off = new TriStateSwitchButton(shell, SWT.NONE);
		button5Off.setEnabled(false);
		button5Off.setText("Disabled (OFF)");

		final TriStateSwitchButton button5First = new TriStateSwitchButton(shell, SWT.NONE);
		button5First.setState(TriState.FIRST);
		button5First.setEnabled(false);
		button5First.setText("Disabled (FIRST)");

		final TriStateSwitchButton button5Second = new TriStateSwitchButton(shell, SWT.NONE);
		button5Second.setState(TriState.SECOND);
		button5Second.setEnabled(false);
		button5Second.setText("Disabled (SECOND)");

		// Without glow / focus effect
		final TriStateSwitchButton button6 = new TriStateSwitchButton(shell, SWT.NONE);
		button6.setFocusColor(null);
		button6.setText("No focus/hover effect");

		// Square (non-rounded)
		final TriStateSwitchButton button7 = new TriStateSwitchButton(shell, SWT.NONE);
		button7.setRound(false);
		button7.setText("Square style");

		// Custom colors for all three sections
		final TriStateSwitchButton button8 = new TriStateSwitchButton(shell, SWT.NONE);
		button8.setFirstBackgroundColor(display.getSystemColor(SWT.COLOR_DARK_BLUE));
		button8.setFirstForegroundColor(display.getSystemColor(SWT.COLOR_WHITE));
		button8.setOffBackgroundColor(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		button8.setOffForegroundColor(display.getSystemColor(SWT.COLOR_WHITE));
		button8.setSecondBackgroundColor(display.getSystemColor(SWT.COLOR_DARK_RED));
		button8.setSecondForegroundColor(display.getSystemColor(SWT.COLOR_WHITE));
		button8.setButtonBorderColor(display.getSystemColor(SWT.COLOR_BLACK));
		button8.setText("Custom section colors");

		// Selection listener – reports the new state
		final TriStateSwitchButton button9 = new TriStateSwitchButton(shell, SWT.NONE);
		button9.setText("Selection listener");
		button9.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				System.out.println("New state: " + button9.getState());
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		// Selection listener with doit=false – cancels the state change
		final TriStateSwitchButton button10 = new TriStateSwitchButton(shell, SWT.NONE);
		button10.setText("Listener with doit=false (change blocked)");
		button10.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				System.out.println("Change vetoed – state stays: " + button10.getState());
				e.doit = false;
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		// Low-level SWT listener
		final TriStateSwitchButton button11 = new TriStateSwitchButton(shell, SWT.NONE);
		button11.setFocusColor(null);
		button11.setText("Low-level SWT listener (beeps on change)");
		button11.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				event.display.beep();
			}
		});

		// Custom font
		final TriStateSwitchButton button12 = new TriStateSwitchButton(shell, SWT.NONE);
		final Font font = new Font(display, "Courier New", 16, SWT.BOLD | SWT.ITALIC);
		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				font.dispose();
			}
		});
		button12.setFont(font);
		button12.setText("Custom font");

		// Custom margins and arc
		final TriStateSwitchButton button13 = new TriStateSwitchButton(shell, SWT.NONE);
		button13.setInsideMargin(10, 3);
		button13.setArc(4);
		button13.setText("Custom margins & arc");

		// Widget background and foreground (label area)
		final TriStateSwitchButton button14 = new TriStateSwitchButton(shell, SWT.NONE);
		button14.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
		button14.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
		button14.setText("Custom widget background/foreground");

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
