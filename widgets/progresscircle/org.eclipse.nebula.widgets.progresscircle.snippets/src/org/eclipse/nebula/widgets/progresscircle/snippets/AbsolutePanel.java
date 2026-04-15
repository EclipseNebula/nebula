/*******************************************************************************
 * Copyright (c) 2018 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron@gmail.com)
 *******************************************************************************/
package org.eclipse.nebula.widgets.progresscircle.snippets;

import org.eclipse.nebula.widgets.progresscircle.ProgressCircle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class AbsolutePanel extends BasePanel {

	public AbsolutePanel(Shell shell) {
		final Color white = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);

		final Group group = new Group(shell, SWT.NONE);
		group.setText("Absolute");
		group.setLayout(new GridLayout(3, false));
		group.setBackground(white);
		group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		createLeftLabel(group, "Minimum");
		final Text minimum = createTextWidget(group, -100);
		createConstraintsLabel(group, "(-5000>5000)");

		createLeftLabel(group, "Maximum");
		final Text maximum = createTextWidget(group, 100);
		createConstraintsLabel(group, "(-5000>5000)");

		createLeftLabel(group, "Value");
		final Text value = createTextWidget(group, 52);
		createConstraintsLabel(group, "(-5000>5000)");

		createCommonPart(group);

		final Button update = new Button(group, SWT.PUSH);
		update.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 3, 1));
		update.setText("Redraw circle");

		final ProgressCircle circle = new ProgressCircle(group, SWT.NONE);
		final GridData gdCircle = new GridData(GridData.CENTER, GridData.CENTER, true, true, 3, 1);
		gdCircle.minimumHeight = gdCircle.minimumWidth = 200;
		circle.setBackground(white);
		circle.setLayoutData(gdCircle);
		circle.setTextPattern(ProgressCircle.INTEGER_PATTERN);
		circle.setMinimum(-100);
		circle.setMaximum(100);
		circle.setSelection(52);
		circle.setThickness(10);
		circle.setCircleSize(100);
		circle.setShowText(true);

		final Color green = new Color(113, 178, 123);
		shell.addDisposeListener((e) -> green.dispose());
		circle.setHighlightColor(green);

		update.addListener(SWT.Selection, e -> {
			int newMinimum = 0;
			try {
				newMinimum = Integer.valueOf(minimum.getText());
			} catch (final NumberFormatException nfe) {
				showError(shell, "The value [" + minimum.getText() + "] is not a number");
				return;
			}

			int newMaximum = 0;
			try {
				newMaximum = Integer.valueOf(maximum.getText());
			} catch (final NumberFormatException nfe) {
				showError(shell, "The value [" + maximum.getText() + "] is not a number");
				return;
			}

			int newValue = 0;
			try {
				newValue = Integer.valueOf(value.getText());
			} catch (final NumberFormatException nfe) {
				showError(shell, "The value [" + value.getText() + "] is not a number");
				return;
			}

			if (newMinimum > newMaximum) {
				showError(shell, String.format("The minimum [%d] is greater than the maximum [%d]", newMinimum, newMaximum));
				return;
			}

			if (newValue < newMinimum || newValue > newMaximum) {
				showError(shell, String.format("The value [%d] should be between %d and %d", newValue, newMinimum, newMaximum));
				return;
			}
			//
			int newCircleSize = 0;
			try {
				newCircleSize = Integer.valueOf(circleSize.getText());
			} catch (final NumberFormatException nfe) {
				showError(shell, "The value [" + circleSize.getText() + "] is not a number");
				return;
			}
			if (newCircleSize > 1000) {
				showError(shell, "The value [" + newCircleSize + "] should be between lower than 1000");
				return;
			}
			//
			int newThickness = 0;
			try {
				newThickness = Integer.valueOf(thickness.getText());
			} catch (final NumberFormatException nfe) {
				showError(shell, "The value [" + thickness.getText() + "] is not a number");
				return;
			}
			if (newThickness < 1 || newThickness > 50) {
				showError(shell, "The value [" + newThickness + "] should be between 1 and 50");
				return;
			}
			int newDelay = 0;
			try {
				newDelay = Integer.valueOf(delay.getText());
			} catch (final NumberFormatException nfe) {
				showError(shell, "The value [" + delay.getText() + "] is not a number");
				return;
			}
			if (newDelay < 1 || newDelay > 5000) {
				showError(shell, "The value [" + newDelay + "] should be between 1 and 5000");
				return;
			}
			//
			circle.setAnimationDelay(newDelay);
			circle.setMinimum(newMinimum);
			circle.setMaximum(newMaximum);
			circle.setSelection(newValue);
			circle.setThickness(newThickness);
			circle.setCircleSize(newCircleSize);
			circle.setShowText(checkbox.getSelection());

		});
	}

}
