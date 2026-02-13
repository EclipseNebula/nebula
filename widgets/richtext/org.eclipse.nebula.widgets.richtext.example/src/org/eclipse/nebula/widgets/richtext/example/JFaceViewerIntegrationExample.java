/*****************************************************************************
 * Copyright (c) 2015, 2026 CEA LIST.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.richtext.example;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.richtext.RichTextCellEditor;
import org.eclipse.nebula.widgets.richtext.RichTextCellLabelProvider;
import org.eclipse.nebula.widgets.richtext.RichTextEditorConfiguration;
import org.eclipse.nebula.widgets.richtext.toolbar.ToolbarButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class JFaceViewerIntegrationExample {

	public static void main(String[] args) {
		Display display = new Display();

		final Shell shell = new Shell(display);
		shell.setText("Rich Text Editor JFace viewer integration example");
		shell.setSize(800,  600);

		shell.setLayout(new GridLayout(1, true));

		JFaceViewerIntegrationExample example = new JFaceViewerIntegrationExample();
		example.createControls(shell);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public void createControls(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));

		final TableViewer viewer = new TableViewer(parent,
				SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		TableViewerColumn firstnameColumn = new TableViewerColumn(viewer, SWT.NONE);
		firstnameColumn.getColumn().setText("Firstname");
		firstnameColumn.getColumn().setWidth(100);
		firstnameColumn.setLabelProvider(new FirstNameLabelProvider());
		firstnameColumn.setEditingSupport(new FirstNameEditingSupport(viewer));

		TableViewerColumn lastnameColumn = new TableViewerColumn(viewer, SWT.NONE);
		lastnameColumn.getColumn().setText("Lastname");
		lastnameColumn.getColumn().setWidth(100);
		// lastnameColumn.setLabelProvider(new LastNameLabelProvider());
		lastnameColumn.setLabelProvider(new RichTextCellLabelProvider<Person>(viewer.getControl()) {

			@Override
			public String getRichText(Person element) {
				if (element.getLastName().equals("Simpson")) {
					return "<em>" + element.getLastName() + "</em>";
				}
				else if (element.getLastName().equals("Smithers")) {
					return "<span style=\"background-color:rgb(255, 0, 0)\"><strong><s><u>" + element.getLastName() + "</u></s></strong></span>";
				}
				return element.getLastName();
			}

		});
		lastnameColumn.setEditingSupport(new LastNameEditingSupport(viewer));

		TableViewerColumn marriedColumn = new TableViewerColumn(viewer, SWT.NONE);
		marriedColumn.getColumn().setText("Married");
		marriedColumn.getColumn().setWidth(60);
		marriedColumn.setLabelProvider(new MarriedLabelProvider());
		marriedColumn.setEditingSupport(new MarriedEditingSupport(viewer));

		TableViewerColumn genderColumn = new TableViewerColumn(viewer, SWT.NONE);
		genderColumn.getColumn().setText("Gender");
		genderColumn.getColumn().setWidth(80);
		genderColumn.setLabelProvider(new GenderLabelProvider());
		genderColumn.setEditingSupport(new GenderEditingSupport(viewer));

		TableViewerColumn descColumn = new TableViewerColumn(viewer, SWT.NONE);
		descColumn.getColumn().setText("Description");
		descColumn.getColumn().setWidth(200);
		// descColumn.setLabelProvider(new DescriptionLabelProvider());
		descColumn.setLabelProvider(new RichTextCellLabelProvider<Person>(viewer.getControl()) {

			@Override
			public String getRichText(Person element) {
				return element.getDescription();
			}

		});
		descColumn.setEditingSupport(new DescriptionEditingSupport(viewer));

		viewer.setInput(PersonService.getPersons(10));

		// add a tree
		TreeViewer treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(new TreeViewerContentProvider());
		treeViewer.setLabelProvider(new RichTextCellLabelProvider<File>(treeViewer.getControl()) {

			@Override
			public String getRichText(File file) {
				String name = file.getName();
				String result = name.isEmpty() ? file.getPath() : name;
				if (file.isDirectory()) {
					result = "<strong><u>" + result + "</u></strong>";
				}
				return result;
			}

		});
		treeViewer.setInput(File.listRoots());
	}

	class FirstNameLabelProvider extends StyledCellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Person element = (Person) cell.getElement();
			cell.setText(element.getFirstName());
			super.update(cell);
		}
	}

	class FirstNameEditingSupport extends EditingSupport {

		public FirstNameEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).getFirstName();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).setFirstName(String.valueOf(value));
			getViewer().update(element, null);
		}
	}

	class LastNameLabelProvider extends StyledCellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Person element = (Person) cell.getElement();
			cell.setText(element.getLastName());
			super.update(cell);
		}
	}

	class LastNameEditingSupport extends EditingSupport {

		public LastNameEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).getLastName();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).setLastName(String.valueOf(value));
			getViewer().update(element, null);
		}
	}

	class GenderLabelProvider extends StyledCellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Person element = (Person) cell.getElement();
			cell.setText(element.getGender().toString());
			super.update(cell);
		}
	}

	class GenderEditingSupport extends EditingSupport {

		private ComboBoxViewerCellEditor cellEditor;

		public GenderEditingSupport(ColumnViewer viewer) {
			super(viewer);
			cellEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
			cellEditor.setLabelProvider(new LabelProvider());
			cellEditor.setContentProvider(new ArrayContentProvider());
			cellEditor.setInput(Person.Gender.values());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).getGender();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).setGender((Person.Gender) value);
			getViewer().update(element, null);
		}

	}

	class MarriedLabelProvider extends StyledCellLabelProvider {

		private final Image uncheckedImg;
		private final Image checkedImg;

		public MarriedLabelProvider() {
			LocalResourceManager resourceMgr = new LocalResourceManager(JFaceResources.getResources());
			URL checked = JFaceViewerIntegrationExample.class.getResource("images/checked.gif");
			URL unchecked = JFaceViewerIntegrationExample.class.getResource("images/unchecked.gif");
			this.checkedImg = resourceMgr.create(ImageDescriptor.createFromURL(checked));
			this.uncheckedImg = resourceMgr.create(ImageDescriptor.createFromURL(unchecked));
		}

		@Override
		public void update(ViewerCell cell) {
			if (((Person) cell.getElement()).isMarried()) {
				cell.setImage(checkedImg);
			} else {
				cell.setImage(uncheckedImg);
			}
		}
	}

	class MarriedEditingSupport extends EditingSupport {

		public MarriedEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor((Composite) getViewer().getControl(), SWT.CHECK);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).isMarried();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).setMarried((Boolean) value);
			getViewer().update(element, null);
		}

	}

	class DescriptionLabelProvider extends StyledCellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Person element = (Person) cell.getElement();
			cell.setText(element.getDescription());
			super.update(cell);
		}
	}

	class DescriptionEditingSupport extends EditingSupport {

		public DescriptionEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			RichTextEditorConfiguration config = new RichTextEditorConfiguration();
			config.setToolbarCollapsible(true);
			config.setToolbarInitialExpanded(false);

			final RichTextCellEditor editor = new RichTextCellEditor((Composite) getViewer().getControl(), config, SWT.RESIZE | SWT.MIN);
			editor.getRichTextEditor().addToolbarButton(new ToolbarButton("addContentButton",
					"addContentCommand", "Add content", "other",
					JFaceViewerIntegrationExample.class.getResource("images/debug_exc.gif")) {

				@Override
				public Object execute() {
					editor.getRichTextEditor().insertHTML("<em>Dynamically added content</em>");
					return null;
				}
			});
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).getDescription();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).setDescription(String.valueOf(value));
			getViewer().update(element, null);
		}
	}

	static class TreeViewerContentProvider implements ITreeContentProvider {
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return (File[]) inputElement;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			File file = (File) parentElement;
			return file.listFiles();
		}

		@Override
		public Object getParent(Object element) {
			File file = (File) element;
			return file.getParentFile();
		}

		@Override
		public boolean hasChildren(Object element) {
			File file = (File) element;
			if (file.isDirectory()) {
				return true;
			}
			return false;
		}

	}
}
