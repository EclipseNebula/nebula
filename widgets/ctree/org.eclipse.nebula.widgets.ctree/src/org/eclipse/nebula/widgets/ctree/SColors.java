/****************************************************************************
* Copyright (c) 2005-2006 Jeremy Dowdall
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

package org.eclipse.nebula.widgets.ctree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA 
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p> 
 */
public class SColors {

	public static final boolean gtk = "gtk".equals(SWT.getPlatform());
	private Display display;
	private List disposable = new ArrayList();

	private Color grid;
	private Color border;
	private Color itemBackgroundNormal;
	private Color itemBackgroundSelected;
	private Color itemBackgroundSelectedNoFocus;
	private Color itemForegroundNormal;
	private Color itemForegroundSelected;
	private Color listBackground;
	
	private boolean hasFocus = false;

	
	public SColors(Display display) {
		this.display = display;
		setBorder(SWT.COLOR_WIDGET_BORDER);
		setGrid(new RGB(238,238,238));
		setItemBackgroundNormal(SWT.COLOR_LIST_BACKGROUND);
		setItemBackgroundSelected(SWT.COLOR_LIST_SELECTION);
		if(gtk) {
			setItemBackgroundSelectedNoFocus(new RGB(150,150,130)); // TODO: color: inactive List background
		} else {
			setItemBackgroundSelectedNoFocus(SWT.COLOR_WIDGET_BACKGROUND); // TODO: color: inactive List background
		}
		setItemForegroundNormal(SWT.COLOR_LIST_FOREGROUND);
		setItemForegroundSelected(SWT.COLOR_LIST_SELECTION_TEXT);
		setTableBackground(SWT.COLOR_LIST_BACKGROUND);
	}

	public Color getBorder() {
		return border;
	}
	
	public Color getGrid() {
		return grid;
	}
	
	public Color getItemBackgroundNormal() {
		return itemBackgroundNormal;
	}
	
	public Color getItemBackgroundSelected() {
		return hasFocus ? itemBackgroundSelected : itemBackgroundSelectedNoFocus;
	}
	
	public Color getItemForegroundNormal() {
		return itemForegroundNormal;
	}
	
	public Color getItemForegroundSelected() {
		return itemForegroundSelected;
	}

	public Color getTableBackground() {
		return listBackground;
	}
	
	public void setFocus(boolean focus) {
		hasFocus = focus;
	}
	
	public void setBorder(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				border = color;
			}
		}
	}
	
	public void setBorder(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			border = color;
			disposable.add(color);
		}
	}
	
	public void setGrid(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				grid = color;
			}
		}
	}
	
	public void setGrid(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			grid = color;
			disposable.add(color);
		}
	}
	
	public void setItemBackgroundNormal(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				itemBackgroundNormal = color;
			}
		}
	}
	
	public void setItemBackgroundNormal(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			itemBackgroundNormal = color;
			disposable.add(color);
		}
	}
	
	public void setItemBackgroundSelected(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				itemBackgroundSelected = color;
			}
		}
	}
	
	public void setItemBackgroundSelected(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			itemBackgroundSelected = color;
			disposable.add(color);
		}
	}
	
	public void setItemBackgroundSelectedNoFocus(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				itemBackgroundSelectedNoFocus = color;
			}
		}
	}
	
	public void setItemBackgroundSelectedNoFocus(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			itemBackgroundSelectedNoFocus = color;
			disposable.add(color);
		}
	}
	
	public void setItemForegroundNormal(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				itemForegroundNormal = color;
			}
		}
	}
	
	public void setItemForegroundNormal(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			itemForegroundNormal = color;
			disposable.add(color);
		}
	}
	
	public void setItemForegroundSelected(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				itemForegroundSelected = color;
			}
		}
	}
	
	public void setItemForegroundSelected(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			itemForegroundSelected = color;
			disposable.add(color);
		}
	}

	public void setTableBackground(int swtConstant) {
		if((display != null) && !display.isDisposed()) {
			Color color = display.getSystemColor(swtConstant);
			if(color != null) {
				listBackground = color;
			}
		}
	}
	
	public void setTableBackground(RGB rgb) {
		if((display != null) && !display.isDisposed()) {
			Color color = new Color(rgb);
			listBackground = color;
			disposable.add(color);
		}
	}

	public void dispose() {
		for(Iterator i = disposable.iterator(); i.hasNext(); ) {
			Color color = (Color) i.next();
			if((color != null) && (!color.isDisposed())) {
				color.dispose();
			}
		}
	}
}
