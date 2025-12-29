/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - Initial
 * implementation and API
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.preferencewindow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Instances of this class are a container that allows the user to select a tab
 */
class PWTabContainer extends Composite {

	private final List<PWTab> tabs;
	private Composite container;
	private final List<FlatButton> buttons;
	private Composite buttonContainer;
	private final Color grey;

	/**
	 * Constructor
	 *
	 * @param parent parent composite
	 * @param style  style (not used)
	 * @param tabs   list of tabs
	 */
	PWTabContainer(final Composite parent, final int style, final List<PWTab> tabs) {
		super(parent, style);
		this.tabs = new ArrayList<>();
		this.tabs.addAll(tabs);

		buttons = new ArrayList<>();

		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);

		grey = new Color(getDisplay(), 204, 204, 204);
		SWTGraphicUtil.addDisposer(this, grey);
	}

	/**
	 * Build the container
	 */
	void build() {
		createButtonsContainer();
		createButtons();
		createContentContainer();

		select(PreferenceWindow.getInstance().getSelectedTab());
	}

	/**
	 * Create the buttons container
	 */
	private void createButtonsContainer() {
		createContainer();
		createButtonsContainerBackground();
	}

	/**
	 * Create the container
	 */
	private void createContainer() {
		buttonContainer = new Composite(this, SWT.NONE);
		final GridData buttonContainerGridData = new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1);
		buttonContainer.setLayoutData(buttonContainerGridData);

		buttonContainer.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		final GridLayout gridLayout = new GridLayout(tabs.size(), false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		gridLayout.marginBottom = 2;
		buttonContainer.setLayout(gridLayout);
	}

	/**
	 * Create the background of the container
	 */
	private void createButtonsContainerBackground() {
		buttonContainer.addListener(SWT.Resize, event -> {
			final Rectangle rect = buttonContainer.getClientArea();
			final Image image = new Image(getDisplay(), Math.max(1, rect.width), Math.max(1, rect.height));
			final GC gc = new GC(image);
			gc.setForeground(grey);
			gc.drawLine(0, rect.height - 1, rect.width, rect.height - 1);
			gc.dispose();
			if (buttonContainer.getBackgroundImage() != null && !buttonContainer.getBackgroundImage().isDisposed()) {
				buttonContainer.getBackgroundImage().dispose();
			}
			buttonContainer.setBackgroundImage(image);
		});
	}

	/**
	 * Create the buttons
	 */
	private void createButtons() {
		for (int i = 0; i < tabs.size(); i++) {
			final PWTab tab = tabs.get(i);
			final FlatButton button = new FlatButton(buttonContainer, SWT.NONE);
			button.setText(tab.getText());
			button.setImage(tab.getImage());

			final GridData gd;
			if (i == tabs.size() - 1) {
				gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false);
			} else {
				gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
			}
			if (i == 0) {
				gd.horizontalIndent = 5;
			}
			gd.widthHint = 75;
			button.setLayoutData(gd);

			final int index = i;
			button.addListener(SWT.Selection, e -> {
				select(index);
			});
			buttons.add(button);
		}
	}

	/**
	 * Select a given button
	 *
	 * @param index index of the selected button
	 */
	void select(final int index) {
		for (final Control c : container.getChildren()) {
			c.dispose();
		}

		tabs.get(index).build(container);
		container.layout();

		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).setSelection(i == index);
		}
	}

	/**
	 * Create the content container, ie the composite that will contain all widgets
	 */
	private void createContentContainer() {
		container = new Composite(this, SWT.NONE);
		final GridData tempContainer = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		tempContainer.widthHint = 700;
		tempContainer.heightHint = 550;
		container.setLayoutData(tempContainer);
	}

}
