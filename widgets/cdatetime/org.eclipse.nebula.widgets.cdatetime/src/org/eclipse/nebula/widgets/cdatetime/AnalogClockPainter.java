/****************************************************************************
 * Copyright (c) 2008 Jeremy Dowdall
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jeremy Dowdall <jeremyd@aspencloud.com> - initial API and implementation
 *****************************************************************************/

package org.eclipse.nebula.widgets.cdatetime;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.nebula.cwt.v.IControlPainter;
import org.eclipse.nebula.cwt.v.VControl;
import org.eclipse.nebula.cwt.v.VPanel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;

class AnalogClockPainter implements IControlPainter {

	/**
	 * Flag indicating a 24 hour clock
	 */
	private static final String HOUR_24 = "H"; //$NON-NLS-1$

	/**
	 * Flag indicating a 12 hour clock
	 */
	private static final String HOUR_12 = "h"; //$NON-NLS-1$
	private CDateTime cdt;
	private AnalogTimePicker picker;
	private boolean paintMinorTicks = true;
	private boolean paintShadows = true;

	public AnalogClockPainter(CDateTime cdt, AnalogTimePicker picker) {
		this.cdt = cdt;
		this.picker = picker;
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public void paintBackground(VControl control, Event e) {
		// nothing to do
	}

	@Override
	public void paintBorders(VControl control, Event e) {
		// nothing to do
	}

	@Override
	public final void paintContent(VControl control, Event e) {
		e.gc.setAdvanced(true);
		e.gc.setAntialias(SWT.ON);
		e.gc.setTextAntialias(SWT.ON);

		Calendar cal = cdt.getCalendarInstance();
		float angleS = cal.get(Calendar.SECOND) * 6;
		float angleM = cal.get(Calendar.MINUTE) * 6;
		float angleH;
		if (picker.is24Hour) {
			angleH = cal.get(Calendar.HOUR_OF_DAY) * 15
					+ cal.get(Calendar.MINUTE) / 4;
		} else {
			angleH = cal.get(Calendar.HOUR) * 30 + cal.get(Calendar.MINUTE) / 2;
		}

		Transform o = new Transform(e.display);
		e.gc.getTransform(o);

		int lwidth = e.gc.getLineWidth();
		e.gc.setLineWidth(1);

		int lcap = e.gc.getLineCap();
		e.gc.setLineCap(SWT.CAP_ROUND);

		paintFace((VPanel) control, e);

		if (paintShadows) {
			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
			e.gc.setLineWidth(2);
			control.setAlpha(e.gc, 50);
			if (picker.secHand) {
				setTransform(e, angleS, true);
				paintSecondHand((VPanel) control, e, (int) angleS);
			}
			if (picker.minHand) {
				setTransform(e, angleM, true);
				paintMinuteHand((VPanel) control, e, (int) angleM);
			}
			if (picker.hourHand) {
				setTransform(e, angleH, true);
				paintHourHand((VPanel) control, e, (int) angleH);
			}

			control.setAlpha(e.gc);
			e.gc.setTransform(o);
			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_GRAY));
			e.gc.fillOval(picker.dialCenter.x + 1, picker.dialCenter.y + 1, 6,
					6);
		}

		if (picker.secHand) {
			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLACK));
			if (picker.overSec) {
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
				e.gc.setLineWidth(4);
			} else {
				e.gc.setForeground(
						e.display.getSystemColor(SWT.COLOR_DARK_RED));
				e.gc.setLineWidth(2);
			}
			setTransform(e, angleS, false);
			paintSecondHand((VPanel) control, e, (int) angleS);
		}

		if (picker.minHand) {
			if (picker.overMin) {
				e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLACK));
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
				e.gc.setLineWidth(4);
			} else {
				e.gc.setForeground(
						e.display.getSystemColor(SWT.COLOR_DARK_BLUE));
				e.gc.setLineWidth(2);
			}
			setTransform(e, angleM, false);
			paintMinuteHand((VPanel) control, e, (int) angleM);
		}

		if (picker.hourHand) {
			if (picker.overHour) {
				e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLACK));
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
				e.gc.setLineWidth(4);
			} else {
				e.gc.setForeground(
						e.display.getSystemColor(SWT.COLOR_DARK_BLUE));
				e.gc.setLineWidth(2);
			}
			setTransform(e, angleH, false);
			paintHourHand((VPanel) control, e, (int) angleH);
		}

		if (picker.overHour || picker.overMin || picker.overSec) {
			e.gc.setLineWidth(2);
		} else {
			e.gc.setLineWidth(1);
		}

		e.gc.setTransform(o);

		control.setAlpha(e.gc);

		e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
		e.gc.fillOval(picker.dialCenter.x - 3, picker.dialCenter.y - 3, 6, 6);

		e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
		e.gc.drawOval(picker.dialCenter.x - 3, picker.dialCenter.y - 3, 6, 6);

		if (picker.timeNow != null) {
			picker.timeNow.paintControl(e);
		}

		if (picker.timeAmPm != null) {
			picker.timeAmPm.paintControl(e);
		}

		e.gc.setLineWidth(lwidth);
		e.gc.setLineCap(lcap);
		o.dispose();
	}

	protected void paintFace(VPanel panel, Event e) {
		int dia = 2 * picker.dialRadius;
		int x = picker.dialCenter.x - picker.dialRadius;
		int y = picker.dialCenter.y - picker.dialRadius;

		Color c1 = new Color(e.display, 220, 220, 225);
		Color c2 = new Color(e.display, 200, 200, 200);

		Pattern p = new Pattern(e.display, 0, y, 0, y + dia,
				e.display.getSystemColor(SWT.COLOR_WHITE), c1);

		e.gc.setBackgroundPattern(p);
		e.gc.fillOval(x, y, dia, dia);

		e.gc.setForeground(c2);
		e.gc.drawOval(x, y, dia, dia);

		int inc;

		Transform o = new Transform(e.display);
		e.gc.getTransform(o);

		Transform t;

		t = new Transform(e.display);
		t.translate(picker.dialCenter.x, picker.dialCenter.y);
		t.rotate(-90);

		inc = 12;
		for (int i = 0; i < inc; i++) {
			e.gc.setTransform(t);
			e.gc.drawLine(picker.dialRadius - 15, 0, picker.dialRadius - 8, 0);
			t.rotate(30);
		}

		if (paintMinorTicks || picker.is24Hour) {
			inc = picker.is24Hour ? 24 : 60;
			int skip = picker.is24Hour ? 2 : 5;
			for (int i = 0; i < inc; i++) {
				if (i % skip != 0) {
					e.gc.setTransform(t);
					e.gc.drawLine(picker.dialRadius - 13, 0,
							picker.dialRadius - 10, 0);
				}
				t.rotate(picker.is24Hour ? 15 : 6);
			}
		}

		t.dispose();

		Calendar tmpcal = cdt.getCalendarInstance();
		tmpcal.set(1, 1, 1, 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat(
				picker.is24Hour ? HOUR_24 : HOUR_12);
		sdf.setTimeZone(tmpcal.getTimeZone());
		e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_BLUE));
		panel.setAlpha(e.gc, 200);

		e.gc.setTransform(o);
		inc = 12;
		for (int i = 0; i < inc; i++) {
			int x2 = x + picker.dialRadius + (int) ((picker.dialRadius - 25)
					* Math.cos(2 * (double) i * Math.PI / inc - Math.PI / 2));
			int y2 = y + picker.dialRadius + (int) ((picker.dialRadius - 25)
					* Math.sin(2 * (double) i * Math.PI / inc - Math.PI / 2));
			String str = sdf.format(tmpcal.getTime());
			Point ss = e.gc.stringExtent(str);
			e.gc.drawString(str, x2 - ss.x / 2, y2 - ss.y / 2);
			tmpcal.add(Calendar.HOUR_OF_DAY, picker.is24Hour ? 2 : 1);
		}

		p.dispose();
		c1.dispose();
		c2.dispose();
		o.dispose();
	}

	protected void paintHourHand(VPanel panel, Event e, int angle) {
		e.gc.drawLine(-15, 0, picker.dialRadius - 35, 0);
	}

	protected void paintMinuteHand(VPanel panel, Event e, int angle) {
		e.gc.drawLine(-15, 0, picker.dialRadius - 17, 0);
	}

	protected void paintSecondHand(VPanel panel, Event e, int angle) {
		e.gc.drawLine(-15, 0, picker.dialRadius - 12, 0);
	}

	private void setTransform(Event e, float angle, boolean shadow) {
		Transform t = new Transform(e.display);
		int x = picker.dialCenter.x;
		int y = picker.dialCenter.y;
		if (shadow) {
			x += 4;
			y += 4;
		}
		t.translate(x, y);
		t.rotate(angle - 90);
		e.gc.setTransform(t);
		t.dispose();
	}

}
