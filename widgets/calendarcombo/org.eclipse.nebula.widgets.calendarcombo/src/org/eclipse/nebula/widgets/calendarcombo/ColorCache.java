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

package org.eclipse.nebula.widgets.calendarcombo;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorCache {

    public static final RGB BLACK = new RGB(0, 0, 0);
    public static final RGB WHITE = new RGB(255, 255, 255);

    private static HashMap mColorTable;
    private static ColorCache mInstance;

    public static final int SKIN_NONE = -1;
    public static final int SKIN_AUTO = 0; // auto detect
    public static final int SKIN_BLUE = 1;
    public static final int SKIN_OLIVE = 2;
    public static final int SKIN_SILVER = 3;
    public static final int SKIN_OFFICE_2007 = 4;

    public static final int SKIN_FALLBACK = SKIN_BLUE; // if auto fails, what skin to use

    public static int SKIN_CURRENT = SKIN_AUTO;

    /**
     * Disposes all colors held in the cache and colors created when class is created.
     * <p>
     * <b>IMPORTANT: ONLY CALL WHEN YOU WANT TO DISPOSE THE WIDGET USING THIS CLASS!</b>
     * <p>
     * If you only wish to dispose colors you have created through the use of the class, please use disposeCachedColors()
     *
     * @see #disposeCachedColor()
     */
    public static void disposeAll() {
        mInstance.dispose();
        
        blueHeaderColor = null;
        lightBlueButtonColor = null;
        blueButtonBackground = null;
        blueToolbarColor = null;
        lightBlueToolbarcolor = null;
        oliveHeaderColor = null;
        lightOliveButtonColor = null;
        lightOliveButtonColor = null;
        oliveButtonBackground = null;
        oliveToolbarColor = null;
        lightOliveToolbarcolor = null;
        silverHeaderColor = null;
        lightSilverButtonColor = null;
        silverButtonBackground = null;
        silverToolbarColor = null;
        lightSilverToolbarcolor = null;
        lightBrownColor = null;
        lightBrownColorReverse = null;
        darkBrownColor = null;
        calendarBlueHeader = null;
        calendarBlueBorder = null;
        calendarOliveHeader = null;
        calendarOliveBorder = null;
        calendarSilverBorder = null;
        calendarSilverHeader = null;
    }
    
    /**
     * Disposes the cached colors only.
     */
    public static void disposeCachedColor() {
        Iterator e = mColorTable.values().iterator();
        while (e.hasNext()) ((Color)e.next()).dispose();

        mColorTable.clear();
    }

    // -- blue skin
    public static Color [] blueHeaderColor = new Color[]{
            ColorCache.getColor(89, 135, 214),
            ColorCache.getColor(3, 56, 148),
            ColorCache.getColor(89, 135, 214)
    };

    public static Color [] lightBlueButtonColor = new Color[]{
            ColorCache.getColor(203, 225, 252),
            ColorCache.getColor(125, 165, 224),
            ColorCache.getColor(203, 225, 252)
    };

    public static Color blueButtonBackground = ColorCache.getColor(3, 56, 148);

    public static Color blueToolbarColor = ColorCache.getColor(77, 124, 205);
    public static Color lightBlueToolbarcolor = ColorCache.getColor(170, 199, 246);
    // -- end blue skin

    // toolbar
    public static Color [] oliveHeaderColor = new Color[]{
            ColorCache.getColor(175, 192, 130), // light
            ColorCache.getColor(99, 122, 68), // dark
            ColorCache.getColor(175, 192, 130)       // light
    };

    // buttons
    public static Color [] lightOliveButtonColor = new Color[]{
            ColorCache.getColor(232, 238, 204),
            ColorCache.getColor(177, 192, 140),
            ColorCache.getColor(232, 238, 204)
    };

    public static Color oliveButtonBackground = ColorCache.getColor(99, 122, 68);

    public static Color oliveToolbarColor = ColorCache.getColor(230, 230, 200);
    public static Color lightOliveToolbarcolor = ColorCache.getColor(232, 232, 206);
    // -- end olive skin

    // -- silver skin
    public static Color [] silverHeaderColor = new Color[]{
            ColorCache.getColor(168, 167, 191), // light
            ColorCache.getColor(124, 124, 148), // dark
            ColorCache.getColor(168, 167, 191)       // light
    };

    // buttons
    public static Color [] lightSilverButtonColor = new Color[]{
            ColorCache.getColor(225, 226, 236),
            ColorCache.getColor(149, 147, 177),
            ColorCache.getColor(225, 226, 236)
    };

    public static Color silverButtonBackground = ColorCache.getColor(124, 124, 148);

    public static Color silverToolbarColor = ColorCache.getColor(164, 163, 187);
    public static Color lightSilverToolbarcolor = ColorCache.getColor(231, 231, 239);
    // -- end silver skin

    public static Color [] lightBrownColor = new Color[]{
            ColorCache.getColor(254, 252, 215),
            ColorCache.getColor(247, 192, 91),
            ColorCache.getColor(254, 252, 215)
    };

    public static Color [] lightBrownColorReverse = new Color[]{
            ColorCache.getColor(247, 192, 91),
            ColorCache.getColor(254, 252, 215),
            ColorCache.getColor(247, 192, 91)
    };

    public static Color [] darkBrownColor = new Color[]{
            ColorCache.getColor(232, 127, 8),
            ColorCache.getColor(247, 218, 124),
            ColorCache.getColor(232, 127, 8)
    };

    public static Color calendarBlueHeader = ColorCache.getColor(158, 190, 245);
    public static Color calendarBlueBorder = ColorCache.getColor(127, 157, 185);

    public static Color calendarOliveHeader = ColorCache.getColor(217, 217, 167);
    public static Color calendarOliveBorder = ColorCache.getColor(164, 185, 127);

    public static Color calendarSilverHeader = ColorCache.getColor(215, 215, 229);
    public static Color calendarSilverBorder = ColorCache.getColor(157, 157, 161);

    // office 2007 does chrome gradients, top color goes 12 pixels down
    public static Color o2007blueTop = ColorCache.getColor(227, 239, 255);
    public static Color o2007blueMid = ColorCache.getColor(173, 209, 255);
    public static Color o2007blueBot = ColorCache.getColor(192, 219, 255);
    
    public static Color o2007orangeSelectedTop = ColorCache.getColor(255, 217, 170);
    public static Color o2007orangeSelectedMid = ColorCache.getColor(255, 187, 110);
    public static Color o2007orangeSelectedBot = ColorCache.getColor(254, 225, 122);
    
    public static Color o2007orangeHoveredTop = ColorCache.getColor(255, 254, 228);
    public static Color o2007orangeHoveredMid = ColorCache.getColor(255, 232, 167);
    public static Color o2007orangeHoveredBot = ColorCache.getColor(255, 230, 158);
    
    public static Color o2007buttonBackgroundColor = ColorCache.getColor(101, 147, 207);
    
    private ColorCache() {
        if (mColorTable == null) {
            mColorTable = new HashMap();
        }
    }

    private static void checkInstance() {
        if (mInstance == null) 
            mInstance = new ColorCache();
    }

    // see disposeAll();
    private void dispose() {
    	checkInstance();

        Iterator e = mColorTable.values().iterator();
        while (e.hasNext()) ((Color)e.next()).dispose();

        mColorTable.clear();

        for (int i = 0; i < blueHeaderColor.length; i++) {
            Color color = blueHeaderColor[i];
            color.dispose();
        }

        for (int i = 0; i < darkBrownColor.length; i++) {
            Color color = darkBrownColor[i];
            color.dispose();
        }

        for (int i = 0; i < lightBlueButtonColor.length; i++) {
            Color color = lightBlueButtonColor[i];
            color.dispose();
        }

        for (int i = 0; i < lightBrownColor.length; i++) {
            Color color = lightBrownColor[i];
            color.dispose();
        }

        for (int i = 0; i < lightBrownColorReverse.length; i++) {
            Color color = lightBrownColorReverse[i];
            color.dispose();
        }

        for (int i = 0; i < lightOliveButtonColor.length; i++) {
            Color color = lightOliveButtonColor[i];
            color.dispose();
        }

        for (int i = 0; i < lightSilverButtonColor.length; i++) {
            Color color = lightSilverButtonColor[i];
            color.dispose();
        }

        for (int i = 0; i < oliveHeaderColor.length; i++) {
            Color color = oliveHeaderColor[i];
            color.dispose();
        }

        for (int i = 0; i < silverHeaderColor.length; i++) {
            Color color = silverHeaderColor[i];
            color.dispose();
        }

        blueButtonBackground.dispose();
        blueToolbarColor.dispose();
        silverButtonBackground.dispose();
        silverToolbarColor.dispose();
        oliveButtonBackground.dispose();
        oliveToolbarColor.dispose();
        lightBlueToolbarcolor.dispose();
        lightOliveToolbarcolor.dispose();
        lightSilverToolbarcolor.dispose();

        calendarBlueBorder.dispose();
        calendarBlueHeader.dispose();
        calendarOliveBorder.dispose();
        calendarOliveHeader.dispose();
        calendarSilverBorder.dispose();
        calendarSilverHeader.dispose();
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
    public static Color getColor(RGB rgb) {
    	checkInstance();
        Color color = (Color) mColorTable.get(rgb);

        if (color == null) {
            color = new Color(rgb);
            mColorTable.put(rgb, color);
        }

        return color;
    }

    /**
     * Returns a color that is also cached if it has not been created before.
     * 
     * @param r Red
     * @param g Green
     * @param b Blue
     * @return Color
     */
    public static Color getColor(int r, int g, int b) {
    	checkInstance();
        RGB rgb = new RGB(r, g, b);
        return getColor(rgb);
    }
}
