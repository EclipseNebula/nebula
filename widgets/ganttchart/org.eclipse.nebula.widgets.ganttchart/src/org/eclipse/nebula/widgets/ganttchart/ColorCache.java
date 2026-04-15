/*******************************************************************************
 * Copyright (c) Emil Crumhorn - Hexapixel.com - emil.crumhorn@gmail.com
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    emil.crumhorn@gmail.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.nebula.widgets.ganttchart;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public final class ColorCache {

    public static final RGB   BLACK = new RGB(0, 0, 0);
    public static final RGB   WHITE = new RGB(255, 255, 255);

    private static Map        _cache;
    private static ColorCache _instance;

    /**
     * Disposes all colors held in the cache and colors created when class is created.
     * <p>
     * <b>IMPORTANT: ONLY CALL WHEN YOU WANT TO DISPOSE THE WIDGET USING THIS CLASS!</b>
     * <p>
     * If you only wish to dispose colors you have created through the use of the class, please use
     * disposeCachedColors()
     * 
     * @see #disposeCachedColor()
     */
    public static void disposeAll() {
        _instance.dispose();
    }

    /**
     * Disposes the cached colors only.
     */
    public static void disposeCachedColor() {
        final Iterator iterator = _cache.values().iterator();
        while (iterator.hasNext()) {
            ((Color) iterator.next()).dispose();
        }

        _cache.clear();
    }

    private ColorCache() {
        if (_cache == null) {
            _cache = new HashMap();
        }
    }

    private static void checkInstance() {
        if (_instance == null) {
            _instance = new ColorCache();
        }
    }

    // see disposeAll();
    private void dispose() {
        checkInstance();

        final Iterator iterator = _cache.values().iterator();
        while (iterator.hasNext()) {
            ((Color) iterator.next()).dispose();
        }

        _cache.clear();
    }

    /**
     * Returns the color white R255, G255, B255
     * 
     * @return White color
     */
    public static Color getWhite() {
        checkInstance();
        return getColor(WHITE);
    }

    /**
     * Returns the color black R0, G0, B0
     * 
     * @return Black color
     */
    public static Color getBlack() {
        checkInstance();
        return getColor(BLACK);
    }

    /**
     * Returns a color that is also cached if it has not been created before.
     * 
     * @param rgb RGB colors
     * @return Color
     */
    public static Color getColor(final RGB rgb) {
        checkInstance();
        Color color = (Color) _cache.get(rgb);

        if (color == null) {
            color = new Color(rgb);
            _cache.put(rgb, color);
        }

        return color;
    }

    /**
     * Returns a color that is also cached if it has not been created before.
     * 
     * @param red Red
     * @param green Green
     * @param blue Blue
     * @return Color
     */
    public static Color getColor(final int red, final int green, final int blue) {
        checkInstance();
        return getColor(new RGB(red, green, blue));
    }

    /**
     * Returns a random color.
     * 
     * @return random color
     */
    public static Color getRandomColor() {
        checkInstance();
        final Random rand = new Random();
        final int red = rand.nextInt(255);
        final int green = rand.nextInt(255);
        final int blue = rand.nextInt(255);

        return getColor(red, green, blue);
    }
}
