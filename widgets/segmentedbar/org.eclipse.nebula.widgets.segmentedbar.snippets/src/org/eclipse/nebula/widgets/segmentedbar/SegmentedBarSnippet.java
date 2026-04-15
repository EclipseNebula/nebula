/*******************************************************************************
 * Copyright (c) 2020 Laurent CARON
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API
 * and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.segmentedbar;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This snippet demonstrates the SegmentedBar widget
 *
 */
public class SegmentedBarSnippet {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));

		// Simple bar
		final Label lbl1 = new Label(shell, SWT.NONE);
		lbl1.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		lbl1.setText("Simple bar");

		Color blue = new Color(70, 130, 180);
		Color violet = new Color(128, 0, 128);
		Color lightGreen = new Color(95, 158, 60);
		Color pink = new Color(240, 128, 180);
		Color orange = new Color(255, 165, 0);

		SegmentedBar sb1 = new SegmentedBar(shell, SWT.NONE);
		sb1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		sb1.addSegment(Segment.create().setBackground(blue).setValue(10d));
		sb1.addSegment(Segment.create().setBackground(blue).setValue(10d));
		sb1.addSegment(Segment.create().setBackground(blue).setValue(10d));
		sb1.addSegment(Segment.create().setBackground(blue).setValue(20d));
		sb1.addSegment(Segment.create().setBackground(blue).setValue(50d));

		// Simple bar with tooltip
		final Label lbl2 = new Label(shell, SWT.NONE);
		lbl2.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		lbl2.setText("Simple bar with tooltip");

		SegmentedBar sb2 = new SegmentedBar(shell, SWT.NONE);
		sb2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		sb2.addSegment(Segment.create().setBackground(blue).setValue(10d).setTooltip("First Value"));
		sb2.addSegment(Segment.create().setBackground(blue).setValue(10d).setTooltip("Second Value"));
		sb2.addSegment(Segment.create().setBackground(blue).setValue(10d).setTooltip("Third Value"));
		sb2.addSegment(Segment.create().setBackground(blue).setValue(20d).setTooltip("Fourth Value"));
		sb2.addSegment(Segment.create().setBackground(blue).setValue(50d).setTooltip("Fifth Value"));

		// Simple bar with colors, no spacing
		final Label lbl3 = new Label(shell, SWT.NONE);
		lbl3.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		lbl3.setText("Simple bar with colors, no spacing");

		SegmentedBar sb3 = new SegmentedBar(shell, SWT.NONE);
		sb3.setSpacing(0);
		sb3.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		sb3.addSegment(Segment.create().setBackground(blue).setValue(10d).setBackground(blue));
		sb3.addSegment(Segment.create().setBackground(blue).setValue(20d).setBackground(display.getSystemColor(SWT.COLOR_GREEN)));
		sb3.addSegment(Segment.create().setBackground(blue).setValue(70d).setBackground(display.getSystemColor(SWT.COLOR_DARK_MAGENTA)));

		// Bar with text
		final Label lbl4 = new Label(shell, SWT.NONE);
		lbl4.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		lbl4.setText("Bar with text");

		SegmentedBar sb4 = new SegmentedBar(shell, SWT.NONE);
		sb4.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		sb4.addSegment(Segment.create().setBackground(blue).setValue(15d).setBackground(blue).setText("Videos"));
		sb4.addSegment(Segment.create().setBackground(blue).setValue(20d).setBackground(violet).setText("Photos"));
		sb4.addSegment(Segment.create().setBackground(blue).setValue(5d).setBackground(lightGreen).setText("Documents"));
		sb4.addSegment(Segment.create().setBackground(blue).setValue(40d).setBackground(pink).setText("Games"));
		sb4.addSegment(Segment.create().setBackground(blue).setValue(20d).setBackground(orange).setText("Applications"));

		// Foreground & fonts 
		final Label lbl5 = new Label(shell, SWT.NONE);
		lbl5.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		lbl5.setText("Bar with text, different foregrounds and fonts");
		
		Font boldFont = SWTGraphicUtil.buildFontFrom(shell, SWT.BOLD);
		Font bigItalicFont = SWTGraphicUtil.buildFontFrom(shell, SWT.ITALIC, 14);
		
		SegmentedBar sb5 = new SegmentedBar(shell, SWT.NONE);
		sb5.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		sb5.addSegment(Segment.create().setBackground(blue).setValue(15d).setBackground(blue).//
				setText("Videos").setForeground(display.getSystemColor(SWT.COLOR_GRAY)));
		sb5.addSegment(Segment.create().setBackground(blue).setValue(20d).setBackground(violet).//
				setText("Photos").setForeground(display.getSystemColor(SWT.COLOR_WHITE)));
		sb5.addSegment(Segment.create().setBackground(blue).setValue(5d).setBackground(lightGreen).setText("Documents"));
		sb5.addSegment(Segment.create().setBackground(blue).setValue(40d).setBackground(pink).//
				setText("Games").setFont(boldFont));
		sb5.addSegment(Segment.create().setBackground(blue).setValue(20d).setBackground(orange).//
				setText("Applications").setFont(bigItalicFont));

		
		shell.setSize(700, 350);
		shell.open();
		SWTGraphicUtil.centerShell(shell);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		blue.dispose();
		violet.dispose();
		lightGreen.dispose();
		pink.dispose();
		orange.dispose();
		boldFont.dispose();
		bigItalicFont.dispose();

		display.dispose();
	}

}
