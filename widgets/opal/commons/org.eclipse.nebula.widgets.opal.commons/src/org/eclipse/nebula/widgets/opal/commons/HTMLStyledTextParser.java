/*******************************************************************************
 * Copyright (c) 2012-2013 Laurent CARON
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
package org.eclipse.nebula.widgets.opal.commons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class are used to convert pseudo-HTML content of a styled
 * text into style ranges
 */
public class HTMLStyledTextParser {

	private final StyledText styledText;
	private StringBuilder output;
	private StringBuilder currentTag;
	private final List<StyleRange> listOfStyles;
	private final LinkedList<StyleRange> stack;
	private int currentPosition;
	private final int defaultHeight;
	private static final Map<String, Integer[]> HTML_CODES = initHTMLCode();

	/**
	 * Constructor
	 *
	 * @param styledText styled text to analyze
	 */
	HTMLStyledTextParser(final StyledText styledText) {
		this.styledText = styledText;
		listOfStyles = new ArrayList<>();
		stack = new LinkedList<>();
		final FontData data = styledText.getFont().getFontData()[0];
		defaultHeight = data.getHeight();
	}

	private static Map<String, Integer[]> initHTMLCode() {
		final Map<String, Integer[]> map = new HashMap<>();

		map.put("aliceblue", new Integer[] { 240, 248, 255 });
		map.put("antiquewhite", new Integer[] { 250, 235, 215 });
		map.put("aqua", new Integer[] { 0, 255, 255 });
		map.put("aquamarine", new Integer[] { 127, 255, 212 });
		map.put("azure", new Integer[] { 240, 255, 255 });
		map.put("beige", new Integer[] { 245, 245, 220 });
		map.put("bisque", new Integer[] { 255, 228, 196 });
		map.put("black", new Integer[] { 0, 0, 0 });
		map.put("blanchedalmond", new Integer[] { 255, 235, 205 });
		map.put("blue", new Integer[] { 0, 0, 255 });
		map.put("blueviolet", new Integer[] { 138, 43, 226 });
		map.put("brown", new Integer[] { 165, 42, 42 });
		map.put("burlywood", new Integer[] { 222, 184, 135 });
		map.put("cadetblue", new Integer[] { 95, 158, 160 });
		map.put("chartreuse", new Integer[] { 127, 255, 0 });
		map.put("chocolate", new Integer[] { 210, 105, 30 });
		map.put("coral", new Integer[] { 255, 127, 80 });
		map.put("cornflowerblue", new Integer[] { 100, 149, 237 });
		map.put("cornsilk", new Integer[] { 255, 248, 220 });
		map.put("crimson", new Integer[] { 220, 20, 60 });
		map.put("cyan", new Integer[] { 0, 255, 255 });
		map.put("darkblue", new Integer[] { 0, 0, 139 });
		map.put("darkcyan", new Integer[] { 0, 139, 139 });
		map.put("darkgoldenrod", new Integer[] { 184, 134, 11 });
		map.put("darkgray", new Integer[] { 169, 169, 169 });
		map.put("darkgreen", new Integer[] { 0, 100, 0 });
		map.put("darkgrey", new Integer[] { 169, 169, 169 });
		map.put("darkkhaki", new Integer[] { 189, 183, 107 });
		map.put("darkmagenta", new Integer[] { 139, 0, 139 });
		map.put("darkolivegreen", new Integer[] { 85, 107, 47 });
		map.put("darkorange", new Integer[] { 255, 140, 0 });
		map.put("darkorchid", new Integer[] { 153, 50, 204 });
		map.put("darkred", new Integer[] { 139, 0, 0 });
		map.put("darksalmon", new Integer[] { 233, 150, 122 });
		map.put("darkseagreen", new Integer[] { 143, 188, 143 });
		map.put("darkslateblue", new Integer[] { 72, 61, 139 });
		map.put("darkslategray", new Integer[] { 47, 79, 79 });
		map.put("darkslategrey", new Integer[] { 47, 79, 79 });
		map.put("darkturquoise", new Integer[] { 0, 206, 209 });
		map.put("darkviolet", new Integer[] { 148, 0, 211 });
		map.put("deeppink", new Integer[] { 255, 20, 147 });
		map.put("deepskyblue", new Integer[] { 0, 191, 255 });
		map.put("dimgray", new Integer[] { 105, 105, 105 });
		map.put("dimgrey", new Integer[] { 105, 105, 105 });
		map.put("dodgerblue", new Integer[] { 30, 144, 255 });
		map.put("firebrick", new Integer[] { 178, 34, 34 });
		map.put("floralwhite", new Integer[] { 255, 250, 240 });
		map.put("forestgreen", new Integer[] { 34, 139, 34 });
		map.put("fuchsia", new Integer[] { 255, 0, 255 });
		map.put("gainsboro", new Integer[] { 220, 220, 220 });
		map.put("ghostwhite", new Integer[] { 248, 248, 255 });
		map.put("gold", new Integer[] { 255, 215, 0 });
		map.put("goldenrod", new Integer[] { 218, 165, 32 });
		map.put("gray", new Integer[] { 128, 128, 128 });
		map.put("green", new Integer[] { 0, 128, 0 });
		map.put("greenyellow", new Integer[] { 173, 255, 47 });
		map.put("grey", new Integer[] { 128, 128, 128 });
		map.put("honeydew", new Integer[] { 240, 255, 240 });
		map.put("hotpink", new Integer[] { 255, 105, 180 });
		map.put("indianred", new Integer[] { 205, 92, 92 });
		map.put("indigo", new Integer[] { 75, 0, 130 });
		map.put("ivory", new Integer[] { 255, 255, 240 });
		map.put("khaki", new Integer[] { 240, 230, 140 });
		map.put("lavender", new Integer[] { 230, 230, 250 });
		map.put("lavenderblush", new Integer[] { 255, 240, 245 });
		map.put("lawngreen", new Integer[] { 124, 252, 0 });
		map.put("lemonchiffon", new Integer[] { 255, 250, 205 });
		map.put("lightblue", new Integer[] { 173, 216, 230 });
		map.put("lightcoral", new Integer[] { 240, 128, 128 });
		map.put("lightcyan", new Integer[] { 224, 255, 255 });
		map.put("lightgoldenrodyellow", new Integer[] { 250, 250, 210 });
		map.put("lightgray", new Integer[] { 211, 211, 211 });
		map.put("lightgreen", new Integer[] { 144, 238, 144 });
		map.put("lightgrey", new Integer[] { 211, 211, 211 });
		map.put("lightpink", new Integer[] { 255, 182, 193 });
		map.put("lightsalmon", new Integer[] { 255, 160, 122 });
		map.put("lightseagreen", new Integer[] { 32, 178, 170 });
		map.put("lightskyblue", new Integer[] { 135, 206, 250 });
		map.put("lightslategray", new Integer[] { 119, 136, 153 });
		map.put("lightslategrey", new Integer[] { 119, 136, 153 });
		map.put("lightsteelblue", new Integer[] { 176, 196, 222 });
		map.put("lightyellow", new Integer[] { 255, 255, 224 });
		map.put("lime", new Integer[] { 0, 255, 0 });
		map.put("limegreen", new Integer[] { 50, 205, 50 });
		map.put("linen", new Integer[] { 250, 240, 230 });
		map.put("magenta", new Integer[] { 255, 0, 255 });
		map.put("maroon", new Integer[] { 128, 0, 0 });
		map.put("mediumaquamarine", new Integer[] { 102, 205, 170 });
		map.put("mediumblue", new Integer[] { 0, 0, 205 });
		map.put("mediumorchid", new Integer[] { 186, 85, 211 });
		map.put("mediumpurple", new Integer[] { 147, 112, 219 });
		map.put("mediumseagreen", new Integer[] { 60, 179, 113 });
		map.put("mediumslateblue", new Integer[] { 123, 104, 238 });
		map.put("mediumspringgreen", new Integer[] { 0, 250, 154 });
		map.put("mediumturquoise", new Integer[] { 72, 209, 204 });
		map.put("mediumvioletred", new Integer[] { 199, 21, 133 });
		map.put("midnightblue", new Integer[] { 25, 25, 112 });
		map.put("mintcream", new Integer[] { 245, 255, 250 });
		map.put("mistyrose", new Integer[] { 255, 228, 225 });
		map.put("moccasin", new Integer[] { 255, 228, 181 });
		map.put("navajowhite", new Integer[] { 255, 222, 173 });
		map.put("navy", new Integer[] { 0, 0, 128 });
		map.put("oldlace", new Integer[] { 253, 245, 230 });
		map.put("olive", new Integer[] { 128, 128, 0 });
		map.put("olivedrab", new Integer[] { 107, 142, 35 });
		map.put("orange", new Integer[] { 255, 165, 0 });
		map.put("orangered", new Integer[] { 255, 69, 0 });
		map.put("orchid", new Integer[] { 218, 112, 214 });
		map.put("palegoldenrod", new Integer[] { 238, 232, 170 });
		map.put("palegreen", new Integer[] { 152, 251, 152 });
		map.put("paleturquoise", new Integer[] { 175, 238, 238 });
		map.put("palevioletred", new Integer[] { 219, 112, 147 });
		map.put("papayawhip", new Integer[] { 255, 239, 213 });
		map.put("peachpuff", new Integer[] { 255, 218, 185 });
		map.put("peru", new Integer[] { 205, 133, 63 });
		map.put("pink", new Integer[] { 255, 192, 203 });
		map.put("plum", new Integer[] { 221, 160, 221 });
		map.put("powderblue", new Integer[] { 176, 224, 230 });
		map.put("purple", new Integer[] { 128, 0, 128 });
		map.put("red", new Integer[] { 255, 0, 0 });
		map.put("rosybrown", new Integer[] { 188, 143, 143 });
		map.put("royalblue", new Integer[] { 65, 105, 225 });
		map.put("saddlebrown", new Integer[] { 139, 69, 19 });
		map.put("salmon", new Integer[] { 250, 128, 114 });
		map.put("sandybrown", new Integer[] { 244, 164, 96 });
		map.put("seagreen", new Integer[] { 46, 139, 87 });
		map.put("seashell", new Integer[] { 255, 245, 238 });
		map.put("sienna", new Integer[] { 160, 82, 45 });
		map.put("silver", new Integer[] { 192, 192, 192 });
		map.put("skyblue", new Integer[] { 135, 206, 235 });
		map.put("slateblue", new Integer[] { 106, 90, 205 });
		map.put("slategray", new Integer[] { 112, 128, 144 });
		map.put("slategrey", new Integer[] { 112, 128, 144 });
		map.put("snow", new Integer[] { 255, 250, 250 });
		map.put("springgreen", new Integer[] { 0, 255, 127 });
		map.put("steelblue", new Integer[] { 70, 130, 180 });
		map.put("tan", new Integer[] { 210, 180, 140 });
		map.put("teal", new Integer[] { 0, 128, 128 });
		map.put("thistle", new Integer[] { 216, 191, 216 });
		map.put("tomato", new Integer[] { 255, 99, 71 });
		map.put("turquoise", new Integer[] { 64, 224, 208 });
		map.put("violet", new Integer[] { 238, 130, 238 });
		map.put("wheat", new Integer[] { 245, 222, 179 });
		map.put("white", new Integer[] { 255, 255, 255 });
		map.put("whitesmoke", new Integer[] { 245, 245, 245 });
		map.put("yellow", new Integer[] { 255, 255, 0 });
		map.put("yellowgreen", new Integer[] { 154, 205, 50 });

		return map;
	}

	/**
	 * Parse the content, build the list of style ranges and apply them to the
	 * styled text widget
	 *
	 * @throws IOException
	 */
	public void parse() throws IOException {
		if (styledText == null || "".equals(styledText.getText().trim())) {
			return;
		}

		initBeforeParsing();

		final String text = styledText.getText().trim();
		final int max = text.length();
		boolean inTag = false;

		for (int i = 0; i < max; i++) {
			final char currentChar = text.charAt(i);
			if (currentChar == '<') {
				inTag = true;
				continue;
			} else if (currentChar == '>') {
				inTag = false;
				handleTag();
				currentTag.delete(0, currentTag.length());
			} else {
				if (inTag) {
					currentTag.append(currentChar);
				} else {
					currentPosition++;
					output.append(currentChar);
				}
			}
		}
		styledText.setText(output.toString());
		styledText.setStyleRanges(removeDoublons());
	}

	private StyleRange[] removeDoublons() {
		final Iterator<StyleRange> mainIt = listOfStyles.iterator();
		while (mainIt.hasNext()) {
			final StyleRange current = mainIt.next();
			final Iterator<StyleRange> it = listOfStyles.iterator();
			while (it.hasNext()) {
				final StyleRange other = it.next();
				if (current == other) {
					continue;
				}
				if (current.start == other.start && current.length == other.length) {
					current.fontStyle = current.fontStyle | other.fontStyle;
					if (current.font == null) {
						current.font = other.font;
					}
					if (current.foreground == null) {
						current.foreground = other.foreground;
					}
					if (current.background == null) {
						current.background = other.background;
					}
					it.remove();
				}
			}
		}

		return listOfStyles.toArray(new StyleRange[listOfStyles.size()]);
	}

	private void initBeforeParsing() {
		output = new StringBuilder();
		currentTag = new StringBuilder();
		listOfStyles.clear();
		stack.clear();
		currentPosition = 0;
	}

	private void handleTag() {
		final String tag = currentTag.toString().toLowerCase();
		if ("br".equals(tag) || "br/".equals(tag)) {
			output.append("\n");
			currentPosition++;
			return;
		}

		if ("b".equals(tag)) {
			processBeginBold();
			return;
		}
		if ("i".equals(tag)) {
			processBeginItalic();
			return;
		}
		if ("u".equals(tag)) {
			processBeginUnderline();
			return;
		}
		if (tag.startsWith("size=")) {
			processBeginSize();
			return;
		}
		if (tag.startsWith("color=")) {
			processBeginColor();
			return;
		}
		if (tag.startsWith("backgroundcolor=")) {
			processBeginBackgroundColor();
			return;
		}

		final String[] acceptedClosingTags = { "/b", "/i", "/u", "/size", "/color", "/backgroundcolor" };
		for (final String closingTag : acceptedClosingTags) {
			if (closingTag.equals(tag)) {
				processEndTag(closingTag);
				return;
			}
		}

		final String text = "<" + tag + ">";
		output.append(text);
		currentPosition += text.length();

	}

	private void processBeginBold() {
		final StyleRange currentStyleRange = new StyleRange();
		currentStyleRange.start = currentPosition;
		currentStyleRange.length = 0;
		currentStyleRange.fontStyle = SWT.BOLD;
		currentStyleRange.data = "</b>";
		stack.push(currentStyleRange);
	}

	private void processEndTag(final String expectedTag) {
		final StyleRange currentStyleRange = stack.pop();
		final String wholeExpectedTag = "<" + expectedTag + ">";
		if (!wholeExpectedTag.equals(currentStyleRange.data)) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Error at position #").append(currentPosition).//
					append(" - closing ").//
					append(wholeExpectedTag).//
					append(" tag found but "). //
					append(currentStyleRange.data).//
					append(" tag expected !");
			throw new RuntimeException(sb.toString());
		}
		currentStyleRange.length = currentPosition - currentStyleRange.start;
		listOfStyles.add(currentStyleRange);

	}

	private void processBeginItalic() {
		final StyleRange currentStyleRange = new StyleRange();
		currentStyleRange.start = currentPosition;
		currentStyleRange.length = 0;
		currentStyleRange.fontStyle = SWT.ITALIC;
		currentStyleRange.data = "</i>";
		stack.push(currentStyleRange);
	}

	private void processBeginUnderline() {
		final StyleRange currentStyleRange = new StyleRange();
		currentStyleRange.start = currentPosition;
		currentStyleRange.length = 0;
		currentStyleRange.fontStyle = SWT.NONE;
		currentStyleRange.underline = true;
		currentStyleRange.data = "</u>";
		stack.push(currentStyleRange);
	}

	private void processBeginSize() {
		final StyleRange currentStyleRange = new StyleRange();
		currentStyleRange.start = currentPosition;
		currentStyleRange.length = 0;
		currentStyleRange.fontStyle = SWT.NONE;
		currentStyleRange.font = computeFont();
		currentStyleRange.data = "</size>";
		stack.push(currentStyleRange);
	}

	private Font computeFont() {
		final String fontSize = currentTag.toString().toLowerCase().replace("size=", "");
		if (fontSize.length() == 0) {
			throw new RuntimeException("Argument size is empty !");
		}
		int newSize = defaultHeight;
		if (fontSize.startsWith("+")) {
			final int delta = Integer.valueOf(fontSize.substring(1));
			newSize += delta;
		} else if (fontSize.startsWith("-")) {
			final int delta = Integer.valueOf(fontSize.substring(1));
			newSize -= delta;
		}

		final FontData fd = styledText.getFont().getFontData()[0];
		final Font newFont = new Font(styledText.getDisplay(), fd.getName(), newSize, SWT.NONE);
		styledText.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				newFont.dispose();
			}
		});
		return newFont;
	}

	private void processBeginColor() {
		final StyleRange currentStyleRange = new StyleRange();
		currentStyleRange.start = currentPosition;
		currentStyleRange.length = 0;
		currentStyleRange.fontStyle = SWT.NONE;
		currentStyleRange.foreground = computeColor();
		currentStyleRange.data = "</color>";
		stack.push(currentStyleRange);
	}

	private Color computeColor() {
		final String fontColor = currentTag.toString().toLowerCase().replace("color=", "").replace("background", "");
		if (fontColor.length() == 0) {
			throw new RuntimeException("Argument color is empty !");
		}

		int red, green, blue;
		if (fontColor.startsWith("#")) {
			final String hexa = fontColor.substring(1);
			if (hexa.length() != 6) {
				throw new RuntimeException("Argument [" + hexa + "] is not valid !");
			}
			try {
				red = Integer.parseInt(hexa.substring(0, 2).toLowerCase(), 16);
				green = Integer.parseInt(hexa.substring(2, 4).toLowerCase(), 16);
				blue = Integer.parseInt(hexa.substring(4, 6).toLowerCase(), 16);
			} catch (final NumberFormatException nfe) {
				throw new RuntimeException("Argument [" + hexa + "] is not valid !");
			}
		} else if (fontColor.indexOf(',') > -1) {
			final String[] args = fontColor.split(",");
			if (args.length != 3) {
				throw new RuntimeException("Argument [" + fontColor + "] is not valid !");
			}
			try {
				red = Integer.parseInt(args[0]);
				green = Integer.parseInt(args[1]);
				blue = Integer.parseInt(args[2]);
			} catch (final NumberFormatException nfe) {
				throw new RuntimeException("Argument [" + fontColor + "] is not valid !");
			}
		} else {
			final Integer[] rgb = HTML_CODES.get(fontColor.toLowerCase());
			if (rgb == null) {
				red = 0;
				green = 0;
				blue = 0;
			} else {
				red = rgb[0];
				green = rgb[1];
				blue = rgb[2];
			}
		}
		return new Color(red, green, blue);
	}

	private void processBeginBackgroundColor() {
		final StyleRange currentStyleRange = new StyleRange();
		currentStyleRange.start = currentPosition;
		currentStyleRange.length = 0;
		currentStyleRange.fontStyle = SWT.NONE;
		currentStyleRange.background = computeColor();
		currentStyleRange.data = "</backgroundcolor>";
		stack.push(currentStyleRange);
	}

}