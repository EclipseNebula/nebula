/*******************************************************************************
 * Copyright (c) 2019 Akuiteo (http://www.akuiteo.com).
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API and
 * implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.led;

import java.time.LocalDateTime;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Snippet for the LED widget
 */
public class LEDSnippet {
	private static Shell shell;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Display display = new Display();
		shell = new Shell(display);
		shell.setText("LED Snippet");
		shell.setLayout(new GridLayout(1, true));
		shell.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		createTopPart();
		createBottomPart();

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();

	}

	private static void createTopPart() {
		new Label(shell, SWT.NONE);
		Composite top = new Composite(shell, SWT.NONE);
		top.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		top.setLayout(new GridLayout(2, true));

		LED led = new LED(top, SWT.ICON);
		led.setLayoutData(new GridData(GridData.END, GridData.FILL, true, true, 1, 2));
		led.setCharacter(LEDCharacter.CLEAR);
		Color idleColor = new Color(60, 60, 60);
		led.setIdleColor(idleColor);
		SWTGraphicUtil.addDisposer(led, idleColor);

		Color selectedColor = new Color(255, 0, 0);
		led.setSelectedColor(selectedColor);
		SWTGraphicUtil.addDisposer(led, selectedColor);

		Combo combo = new Combo(top, SWT.READ_ONLY);
		GridData gdCombo = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		gdCombo.minimumWidth = 100;
		combo.setLayoutData(gdCombo);
		for (LEDCharacter element : LEDCharacter.values()) {
			combo.add(element.name());
		}
		combo.setText("CLEAR");
		combo.addListener(SWT.Selection, e -> {
			LEDCharacter c = LEDCharacter.valueOf(combo.getText());
			led.setCharacter(c);
		});

		Button checkBox = new Button(top, SWT.CHECK);
		checkBox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		checkBox.setText("Dot ?");
		checkBox.addListener(SWT.Selection, e -> {
			led.setShowDot(checkBox.getSelection());
		});

		new Label(shell, SWT.NONE);
	}

	private static void createBottomPart() {
		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		bottom.setLayout(new GridLayout(8, false));
		bottom.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));

		// Date
		LED day1 = new LED(bottom, SWT.NONE);
		day1.setLayoutData(new GridData(GridData.END, GridData.FILL, true, false));

		LED day2 = new LED(bottom, SWT.NONE);
		day2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LEDSeparator dash1 = new LEDSeparator(bottom, SWT.NONE);
		dash1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED month1 = new LED(bottom, SWT.NONE);
		month1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED month2 = new LED(bottom, SWT.NONE);
		month2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LEDSeparator dash2 = new LEDSeparator(bottom, SWT.NONE);
		dash2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED year1 = new LED(bottom, SWT.NONE);
		year1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED year2 = new LED(bottom, SWT.NONE);
		year2.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, true, false));

		// Time
		LED hour1 = new LED(bottom, SWT.NONE);
		hour1.setLayoutData(new GridData(GridData.END, GridData.FILL, true, false));

		LED hour2 = new LED(bottom, SWT.NONE);
		hour2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		DotsLed dots1 = new DotsLed(bottom, SWT.NONE);
		dots1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED min1 = new LED(bottom, SWT.NONE);
		min1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED min2 = new LED(bottom, SWT.NONE);
		min2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		DotsLed dots2 = new DotsLed(bottom, SWT.NONE);
		dots2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED sec1 = new LED(bottom, SWT.NONE);
		sec1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));

		LED sec2 = new LED(bottom, SWT.NONE);
		sec2.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, true, false));

		shell.getDisplay().timerExec(0, new Runnable() {
			boolean highligtDash = false;

			@Override
			public void run() {
				LocalDateTime now = LocalDateTime.now();

				day1.setCharacter(LEDCharacter.getByNumber(getMajorDigit(now.getDayOfMonth())));
				day2.setCharacter(LEDCharacter.getByNumber(now.getDayOfMonth() % 10));

				month1.setCharacter(LEDCharacter.getByNumber(getMajorDigit(now.getMonthValue())));
				month2.setCharacter(LEDCharacter.getByNumber(now.getMonthValue() % 10));

				year1.setCharacter(LEDCharacter.getByNumber(getMajorDigit(now.getYear() % 100)));
				year2.setCharacter(LEDCharacter.getByNumber(now.getYear() % 10));

				hour1.setCharacter(LEDCharacter.getByNumber(getMajorDigit(now.getHour())));
				hour2.setCharacter(LEDCharacter.getByNumber(now.getHour() % 10));

				min1.setCharacter(LEDCharacter.getByNumber(getMajorDigit(now.getMinute())));
				min2.setCharacter(LEDCharacter.getByNumber(now.getMinute() % 10));

				sec1.setCharacter(LEDCharacter.getByNumber(getMajorDigit(now.getSecond())));
				sec2.setCharacter(LEDCharacter.getByNumber(now.getSecond() % 10));

				if (highligtDash) {
					dots1.switchOn();
					dots2.switchOn();
				} else {
					dots1.switchOff();
					dots2.switchOff();
				}

				highligtDash = !highligtDash;

				shell.getDisplay().timerExec(1000, this);

			}

			private int getMajorDigit(int value) {
				return (value - (value % 10)) / 10;
			}

		});

	}

}
