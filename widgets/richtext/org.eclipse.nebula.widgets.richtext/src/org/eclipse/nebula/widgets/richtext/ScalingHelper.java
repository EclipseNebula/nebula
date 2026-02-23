/*****************************************************************************
 * Copyright (c) 2020, 2026 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.richtext;

import org.eclipse.swt.widgets.Display;

/**
 * Helper class to handle display scaling.
 * 
 * @since 1.4
 */
public final class ScalingHelper {

	private ScalingHelper() {
		// private default constructor for helper class
	}

    /**
     * Returns the factor for scaling calculations of pixels regarding the DPI.
     *
     * @param dpi
     *            The DPI for which the factor is requested.
     * @return The factor for dpi scaling calculations.
     * @deprecated The factor can be retrieved via the zoom level of the shell.
     */
	@Deprecated
    public static float getDpiFactor(int dpi) {
        return Math.max(0.1f, Math.round((dpi / 96f) * 100) / 100f);
    }

    /**
     * Converts the given amount of pixels to a DPI scaled value using the
     * factor for the horizontal DPI value.
     *
     * @param pixel
     *            the amount of pixels to convert.
     * @return The converted pixels.
     * @deprecated Use {@link #getZoomedValue(int, int)} instead and pass the zoom level of the shell as second parameter.
     */
    @Deprecated
    public static int convertHorizontalPixelToDpi(int pixel) {
        return Math.round(pixel * getDpiFactor(Display.getDefault().getDPI().x));
    }

    /**
     * Converts the given DPI scaled value to a pixel value using the factor for
     * the horizontal DPI.
     *
     * @param dpi
     *            the DPI value to convert.
     * @return The pixel value related to the given DPI
     * @deprecated Use {@link #getUnzoomedValue(int, int)} instead and pass the zoom level of the shell as second parameter.
     */
    @Deprecated
    public static int convertHorizontalDpiToPixel(int dpi) {
        return Math.round(dpi / getDpiFactor(Display.getDefault().getDPI().x));
    }

    /**
     * Converts the given amount of pixels to a DPI scaled value using the
     * factor for the vertical DPI.
     *
     * @param pixel
     *            the amount of pixels to convert.
     * @return The converted pixels.
     * @deprecated Use {@link #getZoomedValue(int, int)} instead and pass the zoom level of the shell as second parameter.
     */
    @Deprecated
    public static int convertVerticalPixelToDpi(int pixel) {
        return Math.round(pixel * getDpiFactor(Display.getDefault().getDPI().y));
    }

    /**
     * Converts the given DPI scaled value to a pixel value using the factor for
     * the vertical DPI.
     *
     * @param dpi
     *            the DPI value to convert.
     * @return The pixel value related to the given DPI
     * @deprecated Use {@link #getUnzoomedValue(int, int)} instead and pass the zoom level of the shell as second parameter.
     */
    @Deprecated
    public static int convertVerticalDpiToPixel(int dpi) {
        return Math.round(dpi / getDpiFactor(Display.getDefault().getDPI().y));
    }

    /**
     * Converts the given amount of pixels to a scaled value using the zoom level of the shell.
     * @param pixel the amount of pixels to convert.
     * @param zoom the zoom level of the shell to use for the conversion.
     * @return The scaled pixel value.
     */
    public static int getZoomedValue(int pixel, int zoom) {
		return Math.round(pixel * (zoom / 100f));
	}
    
    /**
     * Converts the given scaled value to a pixel value using the zoom level of the shell.
     * @param pixel the scaled amount of pixels to convert.
     * @param zoom the zoom level of the shell to use for the conversion.
     * @return The unscaled pixel value.
     */
    public static int getUnzoomedValue(int pixel, int zoom) {
    	return Math.round(pixel / (zoom / 100f));
    }
    
    /**
	 * Returns the value of the "swt.autoScale" property. If the property is not set, it returns
	 * "quarter" if the display is rescaling at runtime, otherwise it returns "integer".
	 * 
	 * @param display
	 *            The Display to check for rescaling at runtime.
	 * @return The value of the "swt.autoScale" property or the default value based on the display's
	 *         rescaling behavior.
	 */
    public static String getAutoScaleProperty(Display display) {
		String autoScaleProperty = System.getProperty("swt.autoScale");
		if (autoScaleProperty == null) {
			autoScaleProperty = display.isRescalingAtRuntime() ? "quarter" : "integer";
		}
		return autoScaleProperty;
    }
}
