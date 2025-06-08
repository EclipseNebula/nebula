/*******************************************************************************
 * Copyright (c) 2012 Laurent CARON.
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
package org.eclipse.nebula.widgets.opal.roundedtoolbar.snippets;

import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.nebula.widgets.opal.roundedtoolbar.GradientColor;
import org.eclipse.nebula.widgets.opal.roundedtoolbar.RoundedToolItem;
import org.eclipse.nebula.widgets.opal.roundedtoolbar.RoundedToolbar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple snippet for the Rounded Toolbar Widget
 */
public class RoundedToolbarSnippet {

	private static Color grey1;
	private static Color grey2;
	private static Image iconBubble1b;
	private static Image iconBubble1w;
	private static Image iconBubble2b;
	private static Image iconBubble2w;
	private static Image iconBubble3b;
	private static Image iconBubble3w;
	private static Image emailb;
	private static Image emailw;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("RoundedToolbar Snippet");
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 20;
		shell.setLayout(gridLayout);

		grey1 = new Color(211, 211, 211);
		grey2 = new Color(255, 250, 250);

		iconBubble1b = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/bubble1_b.png"));
		iconBubble1w = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/bubble1_w.png"));

		iconBubble2b = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/bubble2_b.png"));
		iconBubble2w = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/bubble2_w.png"));

		iconBubble3b = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/bubble3_b.png"));
		iconBubble3w = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/bubble3_w.png"));

		emailb = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/email_b.png"));
		emailw = new Image(display, RoundedToolbarSnippet.class.getResourceAsStream("icons/email_w.png"));

		new Label(shell, SWT.NONE).setText("Toggle buttons");
		createToggleButtons(shell);

		new Label(shell, SWT.NONE).setText("Push buttons");
		createPushButtons(shell, false);

		final RoundedToolbar toolbar = createPushButtons(shell, true);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = SWT.DEFAULT;
		gd.heightHint = 100;
		toolbar.setLayoutData(gd);

		// CHECKBOX
		new Label(shell, SWT.NONE).setText("Checkbox buttons");
		createCheckButtons(shell, false);

		final RoundedToolbar toolbar2 = createCheckButtons(shell, true);
		final GridData gd2 = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd2.widthHint = SWT.DEFAULT;
		gd2.heightHint = 100;
		toolbar2.setLayoutData(gd);

		// RADIO
		new Label(shell, SWT.NONE).setText("Radio buttons");
		createRadioButtons(shell, false, true);

		final RoundedToolbar toolbar3 = createRadioButtons(shell, true, true);
		final GridData gd3 = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd3.widthHint = SWT.DEFAULT;
		gd3.heightHint = 100;
		toolbar3.setLayoutData(gd);

		new Label(shell, SWT.NONE).setText("Radio button behaviour (no button drawned)");
		createRadioButtons(shell, false, false);
		
		// MENU
		new Label(shell, SWT.NONE).setText("Menu buttons");
		RoundedToolbar menuBar = createMenuBar(shell);
		menuBar.setActiveGradientColor(new GradientColor(display.getSystemColor(SWT.COLOR_CYAN), display.getSystemColor(SWT.COLOR_DARK_BLUE)));
		menuBar.setBorderColor(new Color(255, 0, 0));
		menuBar.setCornerRadius(15);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		grey1.dispose();
		grey2.dispose();

		iconBubble1b.dispose();
		iconBubble1w.dispose();
		iconBubble2b.dispose();
		iconBubble2w.dispose();
		iconBubble3b.dispose();
		iconBubble3w.dispose();
		emailb.dispose();
		emailw.dispose();

		display.dispose();

	}

	private static RoundedToolbar createMenuBar(Shell shell) {

		final RoundedToolbar roundedToolBar = new RoundedToolbar(shell, SWT.NONE);
		final RoundedToolItem item1 = new RoundedToolItem(roundedToolBar, SWT.DROP_DOWN);
		item1.setText("Menu Toolbar Item");
		item1.setMenuCreator(new IMenuCreator() {

			private Menu menu;

			@Override
			public void dispose() {

				if(menu != null) {
					menu.dispose();
					menu = null;
				}
			}

			@Override
			public Menu getMenu(Control parent) {

				if(menu == null) {
					menu = new Menu(parent);
					for(int i = 1; i <= 3; i++) {
						MenuItem menuItem = new MenuItem(menu, SWT.NONE);
						String string = "Item " + i;
						menuItem.setText(string);
						menuItem.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent e) {

								item1.setText(string);
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {

							}
						});
					}
					parent.addDisposeListener(e -> dispose());
				}
				return menu;
			}

			@Override
			public Menu getMenu(Menu parent) {

				return null;
			}
		});
		final RoundedToolItem item2 = new RoundedToolItem(roundedToolBar, SWT.TOGGLE);
		item2.setText("Toggle Item");
		return roundedToolBar;
	}

	private static void createToggleButtons(final Shell shell) {
		final RoundedToolbar roundedToolBar = new RoundedToolbar(shell, SWT.NONE);

		roundedToolBar.setBackground(grey1);
		roundedToolBar.setCornerRadius(6);

		final RoundedToolItem item1 = new RoundedToolItem(roundedToolBar, SWT.TOGGLE);
		item1.setSelection(true);
		item1.setTooltipText("Multiple ballons");
		item1.setWidth(40);
		item1.setSelectionImage(iconBubble3w);
		item1.setImage(iconBubble3b);

		final RoundedToolItem item2 = new RoundedToolItem(roundedToolBar, SWT.TOGGLE);
		item2.setTooltipText("Simple item");
		item2.setSelectionImage(iconBubble1w);
		item2.setImage(iconBubble1b);
		item2.setWidth(40);

		final RoundedToolItem item3 = new RoundedToolItem(roundedToolBar, SWT.TOGGLE);
		item3.setTooltipText("Lot of lines\r\n\r\nThis item has a line-break");
		item3.setSelectionImage(iconBubble2w);
		item3.setImage(iconBubble2b);
		item3.setWidth(40);
	}

	private static RoundedToolbar createPushButtons(final Shell shell, final boolean verticalAlignment) {
		final RoundedToolbar toolbar = new RoundedToolbar(shell, SWT.NONE);
		toolbar.setCornerRadius(8);
		toolbar.setBackground(grey1);

		final RoundedToolItem mailItem = new RoundedToolItem(toolbar);
		mailItem.setSelectionImage(emailw);
		mailItem.setImage(emailb);
		mailItem.setWidth(32);
		mailItem.addListener(SWT.Selection, e -> {
			System.out.println("push/Button 1");
		});
		if (verticalAlignment) {
			mailItem.setVerticalAlignment(SWT.TOP);
		}

		final RoundedToolItem mailItemWithText = new RoundedToolItem(toolbar);
		mailItemWithText.setTextColorSelected(grey2);
		mailItemWithText.setText("Mails");
		mailItemWithText.setSelectionImage(emailw);
		mailItemWithText.setImage(emailb);
		mailItemWithText.setWidth(65);
		mailItemWithText.addListener(SWT.Selection, e -> {
			System.out.println("push/Button 2");
		});
		if (verticalAlignment) {
			mailItemWithText.setVerticalAlignment(SWT.CENTER);
		}

		final RoundedToolItem itemJustText = new RoundedToolItem(toolbar);
		itemJustText.setTextColorSelected(grey2);
		itemJustText.setText("Just text");
		itemJustText.setWidth(100);
		itemJustText.setAlignment(SWT.RIGHT);
		itemJustText.addListener(SWT.Selection, e -> {
			System.out.println("push/Button 3");
		});

		if (verticalAlignment) {
			itemJustText.setVerticalAlignment(SWT.BOTTOM);
		}
		return toolbar;

	}

	private static RoundedToolbar createCheckButtons(final Shell shell, final boolean verticalAlignment) {
		final RoundedToolbar toolBar = new RoundedToolbar(shell, SWT.NONE);
		toolBar.setCornerRadius(8);
		toolBar.setBackground(grey1);

		final RoundedToolItem mailItem = new RoundedToolItem(toolBar, SWT.CHECK);
		mailItem.setSelectionImage(emailw);
		mailItem.setImage(emailb);
		mailItem.setWidth(50);
		mailItem.addListener(SWT.Selection, e -> {
			System.out.println("check/Button 1");
		});
		if (verticalAlignment) {
			mailItem.setVerticalAlignment(SWT.TOP);
		}

		final RoundedToolItem mailItemWithText = new RoundedToolItem(toolBar, SWT.CHECK);
		mailItemWithText.setTextColorSelected(grey2);
		mailItemWithText.setText("Mails");
		mailItemWithText.setSelectionImage(emailw);
		mailItemWithText.setImage(emailb);
		mailItemWithText.setWidth(80);
		mailItemWithText.addListener(SWT.Selection, e -> {
			System.out.println("check/Button 2");
		});
		if (verticalAlignment) {
			mailItemWithText.setVerticalAlignment(SWT.CENTER);
		}

		final RoundedToolItem itemJustText = new RoundedToolItem(toolBar, SWT.CHECK);
		itemJustText.setTextColorSelected(grey2);
		itemJustText.setText("Just text");
		itemJustText.setWidth(100);
		itemJustText.setAlignment(SWT.RIGHT);
		itemJustText.addListener(SWT.Selection, e -> {
			System.out.println("check/Button 3");
		});

		if (verticalAlignment) {
			itemJustText.setVerticalAlignment(SWT.BOTTOM);
		}
		return toolBar;
	}

	private static RoundedToolbar createRadioButtons(final Shell shell, final boolean verticalAlignment, final boolean drawRadio) {
		final RoundedToolbar toolBar = new RoundedToolbar(shell, drawRadio ? SWT.NONE : SWT.HIDE_SELECTION);
		toolBar.setCornerRadius(8);
		toolBar.setBackground(grey1);

		final RoundedToolItem mailItem = new RoundedToolItem(toolBar, SWT.RADIO);
		mailItem.setSelectionImage(emailw);
		mailItem.setImage(emailb);
		mailItem.setWidth(drawRadio ? 50 : 32);
		mailItem.addListener(SWT.Selection, e -> {
			System.out.println("radio/Button 1");
		});
		if (verticalAlignment) {
			mailItem.setVerticalAlignment(SWT.TOP);
		}

		final RoundedToolItem mailItemWithText = new RoundedToolItem(toolBar, SWT.RADIO);
		mailItemWithText.setTextColorSelected(grey2);
		mailItemWithText.setText("Mails");
		mailItemWithText.setSelectionImage(emailw);
		mailItemWithText.setImage(emailb);
		mailItemWithText.setWidth(drawRadio ? 80 : 65);
		mailItemWithText.addListener(SWT.Selection, e -> {
			System.out.println("radio/Button 2");
		});
		if (verticalAlignment) {
			mailItemWithText.setVerticalAlignment(SWT.CENTER);
		}

		final RoundedToolItem itemJustText = new RoundedToolItem(toolBar, SWT.RADIO);
		itemJustText.setTextColorSelected(grey2);
		itemJustText.setText("Just text");
		itemJustText.setWidth(100);
		itemJustText.setAlignment(SWT.RIGHT);
		itemJustText.addListener(SWT.Selection, e -> {
			System.out.println("radio/Button 3");
		});

		if (verticalAlignment) {
			itemJustText.setVerticalAlignment(SWT.BOTTOM);
		}
		return toolBar;
	}
}
