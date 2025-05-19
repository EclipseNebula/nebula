/*******************************************************************************
 * Copyright (C) 2011 Angelo Zerr <angelo.zerr@gmail.com>, Pascal Leclercq <pascal.leclercq@gmail.com>
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo ZERR - initial API and implementation
 *     Pascal Leclercq - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.pagination.renderers.pagesize;

import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.pagination.AbstractPageControllerComposite;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.Resources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This SWT {@link Composite} display a SWT {@link Combo} which is populate with
 * several page list that it can be select to change the page size of the linked
 * pagination controller.
 *
 */
public class PageSizeComboRenderer extends
		AbstractPageControllerComposite implements
		SelectionListener {

	private static final ILabelProvider INSTANCE = new LabelProvider();

	private ComboViewer comboViewer;
	private Label itemsPerPageLabel;

	public PageSizeComboRenderer(Composite parent, int style,
			PageableController controller, Integer[] pageSizeList) {
		super(parent, style, controller);
		comboViewer.setInput(pageSizeList);
	}

	/**
	 * @see
	 * org.eclipse.nebula.widgets.pagination.PageChangedListener#pageIndexChanged
	 * (int, int, org.eclipse.nebula.widgets.pagination.PaginationController)
	 */
	public void pageIndexChanged(int oldPageIndex, int newPageIndex,
			PageableController controller) {
		Integer selected = getSelectedPageSize();
		if (selected == null) {
			selectPageSize(controller.getPageSize());
		}
	}

	/**
	 * @see org.eclipse.nebula.widgets.pagination.PageChangedListener#
	 * totalElementsChanged(long, long,
	 * org.eclipse.nebula.widgets.pagination.PaginationController)
	 */
	public void totalElementsChanged(long oldTotalElements,
			long newTotalElements, PageableController controller) {
		// Do nothing.
	}

	/**
	 * @see
	 * org.eclipse.nebula.widgets.pagination.PageChangedListener#sortChanged
	 * (java.lang.String, java.lang.String, int, int,
	 * org.eclipse.nebula.widgets.pagination.PaginationController)
	 */
	public void sortChanged(String oldPopertyName, String propertyName,
			int oldSortDirection, int sortDirection,
			PageableController paginationController) {
		// Do nothing.
	}

	/**
	 * @see
	 * org.eclipse.nebula.widgets.pagination.PageChangedListener#pageSizeChanged
	 * (int, int, org.eclipse.nebula.widgets.pagination.PaginationController)
	 */
	public void pageSizeChanged(int oldPageSize, int newPageSize,
			PageableController paginationController) {
		selectPageSize(newPageSize);
	}

	@Override
	protected void createUI(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		this.setLayout(layout);

		itemsPerPageLabel = new Label(parent, SWT.NONE);
		itemsPerPageLabel.setText(Resources.getText(
				Resources.PaginationRenderer_itemsPerPage, getLocale()));
		itemsPerPageLabel.setLayoutData(new GridData());

		comboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(INSTANCE);
		comboViewer.getCombo().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		comboViewer.getCombo().addSelectionListener(this);

	}

	@Override
	public void dispose() {
		comboViewer.getCombo().removeSelectionListener(this);
		super.dispose();
	}

	public void widgetDefaultSelected(SelectionEvent e) {

	}

	public void widgetSelected(SelectionEvent e) {
		int pageSize = Integer.parseInt(comboViewer.getCombo().getItem(
				comboViewer.getCombo().getSelectionIndex()));
		getController().setPageSize(pageSize);
	}

	private void selectPageSize(int pageSize) {
		comboViewer.setSelection(new StructuredSelection(pageSize));
	}

	private Integer getSelectedPageSize() {
		if (comboViewer.getSelection().isEmpty()) {
			return null;
		}
		return (Integer) ((IStructuredSelection) comboViewer.getSelection())
				.getFirstElement();
	}

	@Override
	public void setLocale(Locale locale) {
		super.setLocale(locale);
		itemsPerPageLabel.setText(Resources.getText(
				Resources.PaginationRenderer_itemsPerPage, getLocale()));
	}
}
