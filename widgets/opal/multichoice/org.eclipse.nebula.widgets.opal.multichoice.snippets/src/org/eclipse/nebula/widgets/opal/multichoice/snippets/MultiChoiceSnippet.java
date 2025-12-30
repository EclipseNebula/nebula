/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron@gmail.com)
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.multichoice.snippets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.opal.multichoice.MultiChoice;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoiceSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple snippet for the MultiChoice Widget
 */
public class MultiChoiceSnippet {

	public static void main(final String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(4, false));
		shell.setText("MultiChoice Example");

		// Data
		final String[] euroZone = new String[] { "Austria", "Belgium", "Cyprus", "Estonia", "Finland", "France",
				"Germany", "Greece", "Ireland", "Italy", "Luxembourg", "Malta", "Netherlands", "Portugal", "Slovakia",
				"Slovenia", "Spain" };

		final List<Country> membersOfEuropeanUnion = new ArrayList<>();
		membersOfEuropeanUnion.add(new Country("Austria", 8372930));
		membersOfEuropeanUnion.add(new Country("Belgium", 10827519));
		membersOfEuropeanUnion.add(new Country("Bulgaria", 7576751));
		membersOfEuropeanUnion.add(new Country("Cyprus", 801851));
		membersOfEuropeanUnion.add(new Country("Czech Republic", 10512397));
		membersOfEuropeanUnion.add(new Country("Denmark", 5547088));
		membersOfEuropeanUnion.add(new Country("Estonia", 1340274));
		membersOfEuropeanUnion.add(new Country("Finland", 5530575));
		membersOfEuropeanUnion.add(new Country("France", 64709480));
		membersOfEuropeanUnion.add(new Country("Germany", 81757595));
		membersOfEuropeanUnion.add(new Country("Greece", 11125179));
		membersOfEuropeanUnion.add(new Country("Hungary", 10013628));
		membersOfEuropeanUnion.add(new Country("Ireland", 4450878));
		membersOfEuropeanUnion.add(new Country("Italy", 60397353));
		membersOfEuropeanUnion.add(new Country("Latvia", 2248961));
		membersOfEuropeanUnion.add(new Country("Lithuania", 3329227));
		membersOfEuropeanUnion.add(new Country("Luxembourg", 502207));
		membersOfEuropeanUnion.add(new Country("Malta", 416333));
		membersOfEuropeanUnion.add(new Country("Netherlands", 16576800));
		membersOfEuropeanUnion.add(new Country("Poland", 38163895));
		membersOfEuropeanUnion.add(new Country("Portugal", 11317192));
		membersOfEuropeanUnion.add(new Country("Romania", 21466174));
		membersOfEuropeanUnion.add(new Country("Slovakia", 5424057));
		membersOfEuropeanUnion.add(new Country("Slovenia", 2054119));
		membersOfEuropeanUnion.add(new Country("Spain", 46087170));
		membersOfEuropeanUnion.add(new Country("Sweden", 9347899));
		membersOfEuropeanUnion.add(new Country("United Kingdom", 62041708));

		final List<Country> membersOfEUSelectAll = new ArrayList<>();
		membersOfEUSelectAll.addAll(membersOfEuropeanUnion);
		membersOfEUSelectAll.add(new Country("Select All", -1));

		final List<Country> countryCodes = new ArrayList<>();
		countryCodes.add(new Country("France", "FR"));
		countryCodes.add(new Country("United states", "US"));
		countryCodes.add(new Country("United Kingdom", "UK"));
		countryCodes.add(new Country("Germany", "DE"));
		countryCodes.add(new Country("Belgium", "BE"));
		countryCodes.add(new Country("Netherland", "NL"));
		countryCodes.add(new Country("Italy", "IT"));
		countryCodes.add(new Country("Spain", "ES"));
		countryCodes.add(new Country("Portugal", "PT"));

		// Fill the content of the window
		drawLabel(shell, "Simple Multichoice :");
		final MultiChoice<String> mcSimple = new MultiChoice<>(shell, SWT.READ_ONLY);
		mcSimple.setEditable(false);
		final GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
		gridData.widthHint = 200;
		mcSimple.setLayoutData(gridData);
		mcSimple.addAll(euroZone);
		addButons(mcSimple);

		drawLabel(shell, "Simple Multichoice with select/deselect all links:");
		final MultiChoice<String> mcSelectDeselectAll = new MultiChoice<>(shell, SWT.READ_ONLY);
		mcSelectDeselectAll.setEditable(false);
		mcSelectDeselectAll.setShowSelectUnselectAll(true);
		final GridData gridDataSelectDeselectAll = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
		gridDataSelectDeselectAll.widthHint = 200;
		mcSelectDeselectAll.setLayoutData(gridDataSelectDeselectAll);
		mcSelectDeselectAll.addAll(euroZone);
		addButons(mcSelectDeselectAll);

		drawLabel(shell, "Multichoice with beans :");
		final MultiChoice<Country> mcBeans = new MultiChoice<>(shell, SWT.READ_ONLY);
		mcBeans.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
		mcBeans.addAll(membersOfEuropeanUnion);
		mcBeans.setText("Non european country");
		addButons(mcBeans);

		drawLabel(shell, "Selection listener :");
		final MultiChoice<Country> mcSL = new MultiChoice<>(shell, SWT.READ_ONLY);
		mcSL.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
		mcSL.setSelectionListener(new MultiChoiceSelectionListener<>(mcSL) {

			@Override
			public void handle(final MultiChoice<Country> parent, final Country receiver, final boolean selection,
					final Shell popup) {
				if ("Select All".equals(receiver.toString())) {
					if (selection) {
						parent.deselectAll();
						parent.selectAll();
					} else {
						parent.deselectAll();
					}
					popup.setVisible(false);
				}

			}
		});
		mcSL.addAll(membersOfEUSelectAll);
		addButons(mcSL);

		drawLabel(shell, "3 columns :");
		final MultiChoice<String> mc3Columns = new MultiChoice<>(shell, SWT.READ_ONLY);
		mc3Columns.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
		mc3Columns.addAll(euroZone);
		mc3Columns.setNumberOfColumns(3);
		addButons(mc3Columns);

		drawLabel(shell, "Other separator :");
		final MultiChoice<String> mcOtherSeparator = new MultiChoice<>(shell, SWT.READ_ONLY);
		mcOtherSeparator.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
		mcOtherSeparator.addAll(euroZone);
		mcOtherSeparator.setSeparator(" - ");
		addButons(mcOtherSeparator);

		drawLabel(shell, "Modifiable combo :");
		final MultiChoice<Country> mcModify = new MultiChoice<>(shell, SWT.NONE);
		mcModify.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
		mcModify.setLabelProvider(element -> {
			if (element == null || !(element instanceof Country)) {
				return "";
			}
			return ((Country) element).getCode();
		});
		mcModify.addAll(countryCodes);
		addButons(mcModify);

		drawLabel(shell, "Lot of data :");
		final List<String> data = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			data.add("Data #" + i);
		}
		final MultiChoice<String> mcLotOfData = new MultiChoice<>(shell, SWT.NONE);
		mcLotOfData.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
		mcLotOfData.setLabelProvider(element -> {
			if (element == null) {
				return "";
			}
			return (String) element;
		});
		mcLotOfData.addAll(data);
		addButons(mcLotOfData);

		drawLabel(shell, "Non Editable Multichoice :");
		final MultiChoice<String> mcNotEditable = new MultiChoice<>(shell, SWT.READ_ONLY);
		final GridData gridDataNotEditable = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
		gridDataNotEditable.widthHint = 200;
		mcNotEditable.setLayoutData(gridDataNotEditable);
		mcNotEditable.addAll(euroZone);
		mcNotEditable.setEditable(false);
		addButons(mcNotEditable);

		drawLabel(shell, "Not Enabled Multichoice :");
		final MultiChoice<String> mcNotEnabled = new MultiChoice<>(shell, SWT.READ_ONLY);
		final GridData gridDataNotEnabled = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
		gridDataNotEnabled.widthHint = 200;
		mcNotEnabled.setLayoutData(gridDataNotEnabled);
		mcNotEnabled.addAll(euroZone);
		mcNotEnabled.setEditable(false);
		addButons(mcNotEnabled);

		drawLabel(shell, "Multichoice with search panel :");
		final MultiChoice<String> mcSearch = new MultiChoice<>(shell, SWT.READ_ONLY | SWT.SEARCH);
		mcSearch.setEditable(false);
		final GridData gridDataMcSearch = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
		gridDataMcSearch.widthHint = 200;
		mcSearch.setLayoutData(gridDataMcSearch);
		mcSearch.addAll(euroZone);
		addButons(mcSearch);

		// Open the shell
		shell.open();
		shell.pack();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private static void drawLabel(final Shell shell, final String text) {
		final Label label = new Label(shell, SWT.NONE);
		label.setText(text);
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
	}

	private static void addButons(final MultiChoice<?> mc) {
		final Button buttonShowSelection = new Button(mc.getParent(), SWT.PUSH);
		buttonShowSelection.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		buttonShowSelection.setText("Show selection");
		buttonShowSelection.addListener(SWT.Selection, e -> {
			final Iterator<?> it = mc.getSelection().iterator();
			final StringBuilder sb = new StringBuilder();
			while (it.hasNext()) {
				sb.append(it.next().toString());
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			final MessageBox mb = new MessageBox(mc.getShell(), SWT.OK);
			mb.setMessage(sb.toString());
			mb.open();
		});

		final Button buttonShowSelectedIndex = new Button(mc.getParent(), SWT.PUSH);
		buttonShowSelectedIndex.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		buttonShowSelectedIndex.setText("Show selected index");
		buttonShowSelectedIndex.addListener(SWT.Selection, e -> {
			final StringBuilder sb = new StringBuilder();
			final int[] selectedIndex = mc.getSelectedIndex();
			if (selectedIndex.length > 0) {
				sb.append(selectedIndex[0]);
				for (int i = 1; i < selectedIndex.length; i++) {
					sb.append(",");
					sb.append(selectedIndex[i]);
				}
			} else {
				sb.append("Empty");
			}
			final MessageBox mb = new MessageBox(mc.getShell(), SWT.OK);
			mb.setMessage(sb.toString());
			mb.open();
		});
	}
}
