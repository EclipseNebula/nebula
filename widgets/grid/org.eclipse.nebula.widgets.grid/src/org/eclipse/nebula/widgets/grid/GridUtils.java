/*******************************************************************************
 * Copyright (c) 2014 Mirko Paturzo (Exeura srl).
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mirko Paturzo - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Some tools for Grid.
 * 
 * @author Mirko Paturzo <mirko.paturzo@exeura.eu>
 */
public class GridUtils
{

	private static final String INDET_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";
	private static final String INDENT_VALUE = "2";
	private static final String INDENT_ACCEPTED_VALUE = "yes";

	/**
	 * Tags in xml
	 */
	private static final String GRID_TAG = "grid";
	private static final String HEADER_TAG = "header";
	private static final String COLUMN_TAG = "column";
	private static final String CHILDREN_TAG = "children";
	private static final String ROWS_TAG = "rows";
	private static final String ROW_TAG = "row";
	private static final String ID_TAG = "id";

	/**
	 * This method export a grid into a outputstream using xml.
	 * SWT Main thread is required for the export.
	 * Full supports for Grid Table.
	 * Grid Tree only visible items was exported.
	 * 
	 * @param grid the grid who will be export to xml.
	 * @param outputStream used for the export.
	 * @throws ParserConfigurationException if a parser configuration error occurs
	 * @throws TransformerException if a transformation error occurs
	 */
	public static void gridToXml(Grid grid, OutputStream outputStream) throws ParserConfigurationException,
			TransformerException
	{

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
		try {
			docFactory.setFeature(FEATURE, true);
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("ParserConfigurationException was thrown. The feature '"
		+ FEATURE + "' is not supported by your XML processor.", e);
		}
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		final Document doc = docBuilder.newDocument();

		Element rootElement = doc.createElement(GRID_TAG);
		doc.appendChild(rootElement);

		GridColumn[] columnsArray = grid.getColumns();

		Element header = doc.createElement(HEADER_TAG);
		rootElement.appendChild(header);

		for (int column = 0; column < columnsArray.length; column++)
		{

			Element columnElement = doc.createElement(COLUMN_TAG);
			columnElement.appendChild(doc.createTextNode(columnsArray[column].getText()));
			header.appendChild(columnElement);
			Attr columnNumber = doc.createAttribute(ID_TAG);
			columnNumber.setValue(Integer.toString(column));
			columnElement.setAttributeNode(columnNumber);

		}

		GridItem[] itemsList = grid.getItems();

		DataVisualizer dataVisualizer = grid.getDataVisualizer();

		Element rowsElement = doc.createElement(ROWS_TAG);
		rootElement.appendChild(rowsElement);
		writeChildren(doc, rowsElement, columnsArray, itemsList, dataVisualizer, 0);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, INDENT_ACCEPTED_VALUE);
		transformer.setOutputProperty(INDET_PROPERTY, INDENT_VALUE);
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outputStream);

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
		transformer.transform(source, result);

	}

	private static void writeChildren(Document doc, Element rootElement, GridColumn[] columnsList,
			GridItem[] itemsArray, DataVisualizer dataVisualizer, int level)
	{
		for (int row = 0; row < itemsArray.length; row++)
		{
			GridItem gridItem = itemsArray[row];

			if (gridItem.isVisible() && gridItem.getLevel() == level)
			{
				Element rowElement = writeGridItemInformation(doc, rootElement, columnsList, dataVisualizer, gridItem);

				GridItem[] items = gridItem.getItems();
				if (items.length > 0)
				{
					Element childrenElement = doc.createElement(CHILDREN_TAG);
					rowElement.appendChild(childrenElement);
					writeChildren(doc, childrenElement, columnsList, items,
							gridItem.getParent().getDataVisualizer(), level + 1);
				}
			}
		}
	}

	private static Element writeGridItemInformation(Document doc, Element rootElement, GridColumn[] columnsList,
			DataVisualizer dataVisualizer, GridItem item)
	{
		Element rowElement = doc.createElement(ROW_TAG);
		rootElement.appendChild(rowElement);
		//		Attr rowNumber = doc.createAttribute("id");
		//		rowNumber.setValue(Integer.toString(item.getRowIndex()));
		//		rowElement.setAttributeNode(rowNumber);

		for (int column = 0; column < columnsList.length; column++)
		{

			String text = dataVisualizer.getText(item, column);

			if (text != null)
			{
				Element columnElement = doc.createElement(COLUMN_TAG);
				columnElement.appendChild(doc.createTextNode(text));
				rowElement.appendChild(columnElement);
				Attr columnNumber = doc.createAttribute(ID_TAG);
				columnNumber.setValue(Integer.toString(column));
				columnElement.setAttributeNode(columnNumber);
			}

		}

		return rowElement;
	}
}
