package org.eclipse.nebula.widgets.grid.internal.win7;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Palette provider to provide/maintain a set of palettes
 * specifically used for Win7 looknfeel
 */
public class Win7PaletteProvider {

	/**
	 * Palette type for normal grid column header.
	 */
	public static final Integer NORMAL_GRID_COLUMN_HEADER = Integer.valueOf(0);

	private static final Object[] NORMAL_GRID_COLUMN_HEADER_RGB = {
		Integer.valueOf(SWT.COLOR_WHITE),
		Integer.valueOf(SWT.COLOR_WHITE),
		Integer.valueOf(SWT.COLOR_WHITE),
		Integer.valueOf(SWT.COLOR_WHITE),
		new RGB(242,242,242),
        new RGB(239,239,239),
        new RGB(251,251,252),
        new RGB(247,248,249),
        new RGB(247,248,250),
        new RGB(241,242,244),
        new RGB(252,252,253),
        new RGB(251,251,252),
        new RGB(231,232,234),
        new RGB(222,223,225),
        new RGB(213,213,213),
        new RGB(251,251,251)
	};

	/**
	 * Palette type for hover grid column header.
	 */
	public static final Integer HOVER_GRID_COLUMN_HEADER = Integer.valueOf(1);

	private static final Object[] HOVER_GRID_COLUMN_HEADER_RGB = {
		Integer.valueOf(SWT.COLOR_WHITE),
        new RGB(136,203,235),
        new RGB(227,247,255),
        new RGB(227,247,255),
        null,
        new RGB(136,203,235),
        null,
        new RGB(105,187,227),
        null,
        new RGB(183,231,251),
        null,
        new RGB(183,231,251),
        null,
        new RGB(105,187,227),
        new RGB(147,201,227),
        new RGB(251,251,251)
	};

	/**
	 * Palette type for mousedown grid column header.
	 */
	public static final Integer MOUSEDOWN_GRID_COLUMN_HEADER = Integer.valueOf(3);

	private static final Object[] MOUSEDOWN_GRID_COLUMN_HEADER_RGB = {
		Integer.valueOf(SWT.COLOR_WHITE),
        new RGB(122,158,177),
        new RGB(188,228,249),
        new RGB(162,203,224),
        null,
        new RGB(122,158,177),
        new RGB(80,145,175),
        new RGB(77,141,173),
        new RGB(141,214,247),
        new RGB(138,209,245),
        new RGB(114,188,223),
        new RGB(110,184,220),
        new RGB(80,145,175),
        new RGB(77,141,173),
        new RGB(147,201,227),
        new RGB(251,251,251)
	};


	/**
	 * Palette type for selected grid column header.
	 */
	public static final Integer SELECTED_GRID_COLUMN_HEADER = Integer.valueOf(3);

	private static final Object[] SELECTED_GRID_COLUMN_HEADER_RGB = {
		Integer.valueOf(SWT.COLOR_WHITE),
        new RGB(150,217,249),
        new RGB(242,249,252),
        new RGB(242,249,252),
        null,
        new RGB(150,217,249),
        null,
        new RGB(150,217,249),
        new RGB(225,241,249),
        new RGB(216,236,246),
        new RGB(225,241,249),
        new RGB(216,236,246),
        null,
        new RGB(150,217,249),
        new RGB(150,217,249),
		Integer.valueOf(SWT.COLOR_WHITE)
	};


	/**
	 * Palette type for shadow grid column header.
	 */
	public static final Integer SHADOW_GRID_COLUMN_HEADER = Integer.valueOf(4);

	private static final Object[] SHADOW_GRID_COLUMN_HEADER_RGB = {
        new RGB(134,163,178),
        new RGB(170,206,225)
	};

	/**
	 * Pool of palettes
	 */
    private final Map<Integer, Palette> palettes = new HashMap<>();

    /**
     * Dispose the
     */
    public void dispose(){
    	for ( Iterator<Palette> it = palettes.values().iterator() ; it.hasNext() ; ){
            it.next().dispose();
    	}
    }

    /**
     * Initializes the palette for the given display and type.
     * 
     * @param display the display
     * @param type the palette type
     */
    public void initializePalette(Display display, Integer type){
    	getPalette(display, type);
    }

    /**
     * Utility method to create/pool a Color
     * @param display the display
     * @param type the palette type
     * @return Color
     */
    public Palette getPalette(Display display, Integer type){
    	Palette palette = palettes.get(type);
        if ( palette != null ) {
			return palette;
		}

        //create the palette
        if ( type == NORMAL_GRID_COLUMN_HEADER ){
        	return createPalette(display,type,NORMAL_GRID_COLUMN_HEADER_RGB);
        } else if ( type == HOVER_GRID_COLUMN_HEADER ){
        	return createPalette(display,type,HOVER_GRID_COLUMN_HEADER_RGB);
        } else if ( type == MOUSEDOWN_GRID_COLUMN_HEADER ){
        	return createPalette(display,type,MOUSEDOWN_GRID_COLUMN_HEADER_RGB);
        } else if ( type == SHADOW_GRID_COLUMN_HEADER ){
        	return createPalette(display,type,SHADOW_GRID_COLUMN_HEADER_RGB);
        } else if ( type == SELECTED_GRID_COLUMN_HEADER ){
        	return createPalette(display,type,SELECTED_GRID_COLUMN_HEADER_RGB);
        }
        return null;
    }


    /**
     * Creates a palette from the given definition.
     * 
     * @param display the display
     * @param type the palette type
     * @param def the palette definition
     * @return Palette
     */
    protected Palette createPalette(Display display, Integer type, Object[] def){
    	Color[] colors = new Color[def.length];
    	for ( int i = 0 ; i < colors.length ; i++ ){
    		if ( def[i] == null ){
    			colors[i] = null;
    		} else if ( def[i] instanceof Integer ){
    			colors[i] = (display==null?Display.getCurrent():display).getSystemColor(((Integer)def[i]).intValue());
    		} else {
				colors[i] = new Color((RGB)def[i]);
    		}
    	}
    	return new Palette(type, colors);
    }


    /**
     *
     */
    public class Palette {

    	private final Integer type;
    	private Color[] colors = {};

    	/**
    	 * Creates a new palette.
    	 * 
    	 * @param type the palette type
    	 * @param colors the array of colors
    	 */
    	public Palette(Integer type, Color[] colors) {
    		this.type = type;
    		this.colors = colors;
		}

    	/**
    	 * @return int
    	 */
    	public Integer getType() {
			return type;
		}

    	/**
    	 * @return Color[]
    	 */
    	public Color[] getColors(){
    		return colors;
    	}

    	/**
    	 * Dispose colors
    	 */
    	public void dispose(){
    		if ( colors != null ){
	    		for ( int i = 0 ; i < colors.length ; i++ ){
	    			if ( colors[i] != null ) {
						colors[i].dispose();
					}
	    		}
    		}
    	}

    }

}