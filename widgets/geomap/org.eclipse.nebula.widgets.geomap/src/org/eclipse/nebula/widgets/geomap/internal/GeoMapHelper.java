/*******************************************************************************
 * Copyright (c) 2008, 2012 Stepan Rutz.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Stepan Rutz - initial implementation
 *    Hallvard Trætteberg - further cleanup and development
 *******************************************************************************/

package org.eclipse.nebula.widgets.geomap.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.nebula.widgets.geomap.OsmTileServer;
import org.eclipse.nebula.widgets.geomap.TileServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * License is EPL (Eclipse Public License)
 * http://www.eclipse.org/legal/epl-v10.html. Contact at stepan.rutz@gmx.de
 * </p>
 *
 * @author stepan.rutz, hal
 * @version $Revision$
 */
public class GeoMapHelper implements GeoMapPositioned, GeoMapHelperListener {

	private final Display display;

	/**
	 * Initializes a new <code>GeoMapHelper</code> with a specific display. The
	 * display is used for async runnables and as device for images.
	 * 
	 * @param display
	 *            the display
	 */
	private GeoMapHelper(Display display) {
		super();
		this.display = display;
	}

	Display getDisplay() {
		return display;
	}

	/**
	 * basically not be changed, must be the same as GeoMapUtil's TILE_SIZE
	 */
	public static final int TILE_SIZE = 256;

	private static final int DEFAULT_NUMBER_OF_IMAGEFETCHER_THREADS = 4;

	private static final int MIN_CACHE_SIZE = 200;

	private Point mapSize = new Point(0, 0);
	private Point mapPosition = new Point(0, 0);
	private int zoom;

	AtomicLong zoomStamp = new AtomicLong();

	private TileServer tileServer = OsmTileServer.TILESERVERS[0];
	private int cacheSize;
	// must be readable from AsyncImage
	Map<TileRef, AsyncImage> cache;

	private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

	private ThreadFactory threadFactory = r -> {
		Thread thread = new Thread(r);
		thread.setName("Async Image Loader " + thread.getId() + " " //$NON-NLS-1$ //$NON-NLS-2$
				+ System.identityHashCode(thread));
		thread.setDaemon(true);
		return thread;
	};
	ThreadPoolExecutor executor = new ThreadPoolExecutor(DEFAULT_NUMBER_OF_IMAGEFETCHER_THREADS, 16, 2,
			TimeUnit.SECONDS, workQueue, threadFactory);
	private Color waitBackground, waitForeground;

	/**
	 * Initializes a new <code>GeoMapHelper</code> for a specific display, position,
	 * zoom level and cache size.
	 * 
	 * @param display
	 *            the <code>Display</code> to create images for.
	 * @param mapPosition
	 *            initial mapPosition.
	 * @param zoom
	 *            initial map zoom
	 * @param cacheSize
	 *            initial cache size, eg number of tile-images that are kept in
	 *            cache to prevent reloading from the network.
	 */
	@SuppressWarnings("serial")
	public GeoMapHelper(Display display, Point mapPosition, int zoom, int cacheSize) {
		this(display);
		if (cacheSize < MIN_CACHE_SIZE) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, null, " - Cache size should be greater than " + MIN_CACHE_SIZE); //$NON-NLS-1$
		}
		this.cacheSize = cacheSize;
		this.cache = Collections.synchronizedMap(new LinkedHashMap<TileRef, AsyncImage>(cacheSize, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<TileRef, AsyncImage> eldest) {
				boolean remove = size() > GeoMapHelper.this.cacheSize;
				if (remove) {
					eldest.getValue().dispose();
				}
				return remove;
			}

			@Override
			public void clear() {
				for (AsyncImage image : values()) {
					image.dispose();
				}
				super.clear();
			}
		});
		waitBackground = new Color(0x88, 0x88, 0x88);
		waitForeground = new Color(0x77, 0x77, 0x77);

		setZoom(zoom);
		setMapPosition(mapPosition.x, mapPosition.y);
	}

	/**
	 * Points the map to the provided graphics context (GC), which could be the one
	 * provided in an SWT control paint request or one created for rendering an SWT
	 * Image
	 * 
	 * @param gc
	 *            the graphics context
	 * @param clip
	 *            the area that needs updating, could be null
	 * @param size
	 *            the size of the map area
	 */
	public void paint(GC gc, Rectangle clip, Point size) {

		long startTime = System.currentTimeMillis();
		int tileCount = 0;

		int x0 = (int) Math.floor((double) mapPosition.x / TILE_SIZE);
		int y0 = (int) Math.floor((double) mapPosition.y / TILE_SIZE);
		int x1 = (int) Math.ceil(((double) mapPosition.x + size.x) / TILE_SIZE);
		int y1 = (int) Math.ceil(((double) mapPosition.y + size.y) / TILE_SIZE);

		int dy = y0 * TILE_SIZE - mapPosition.y;
		for (int y = y0; y < y1; y++) {
			int dx = x0 * TILE_SIZE - mapPosition.x;
			for (int x = x0; x < x1; x++) {
				if (clip == null || dx + TILE_SIZE >= clip.x && dy + TILE_SIZE >= clip.y && dx <= clip.x + clip.width
						&& dy <= clip.y + clip.height) {
					paintTile(gc, dx, dy, x, y);
				}
				dx += TILE_SIZE;
				tileCount++;
			}
			dy += TILE_SIZE;
		}

		long endTime = System.currentTimeMillis();
		for (InternalGeoMapListener listener : internalGeoMapListeners) {
			listener.mapPainted(tileCount, endTime - startTime);
		}
		for (InternalGeoMapListener listener : internalGeoMapListeners) {
			listener.tileCacheUpdated(cache.size(), this.cacheSize);
		}
	}

	void paintTile(GC gc, int dx, int dy, int x, int y) {
		boolean DRAW_IMAGES = true;
		boolean DEBUG = false;
		boolean DRAW_OUT_OF_BOUNDS = !false;

		boolean imageDrawn = false;
		int xTileCount = 1 << zoom;
		int yTileCount = 1 << zoom;
		boolean tileInBounds = x >= 0 && x < xTileCount && y >= 0 && y < yTileCount;
		boolean drawImage = DRAW_IMAGES && tileInBounds;
		if (drawImage) {
			TileRef tileRef = new TileRef(x, y, zoom);
			AsyncImage image = cache.get(tileRef);
			if (image == null) {
				image = new AsyncImage(this, tileRef, tileServer.getTileURL(tileRef));
				cache.put(tileRef, image);
			}
			Image swtImage = image.getImage(getDisplay());
			if (swtImage != null) {
				gc.drawImage(swtImage, dx, dy);
				imageDrawn = true;
			} else {
				// reuse tile from lower zoom level, i.e. half the resolution
				tileRef = new TileRef(x / 2, y / 2, zoom - 1);
				image = cache.get(tileRef);
				if (image != null) {
					swtImage = image.getImage(getDisplay());
					if (swtImage != null) {
						gc.drawImage(swtImage, x % 2 == 0 ? 0 : TILE_SIZE / 2, y % 2 == 0 ? 0 : TILE_SIZE / 2,
								TILE_SIZE / 2, TILE_SIZE / 2, dx, dy, TILE_SIZE, TILE_SIZE);
						imageDrawn = true;
					}
				}
			}
			for (InternalGeoMapListener listener : internalGeoMapListeners) {
				listener.tilePainted(tileRef);
			}
		}
		if (DEBUG && !imageDrawn && (tileInBounds || DRAW_OUT_OF_BOUNDS)) {
			gc.setBackground(display.getSystemColor(tileInBounds ? SWT.COLOR_GREEN : SWT.COLOR_RED));
			gc.fillRectangle(dx + 4, dy + 4, TILE_SIZE - 8, TILE_SIZE - 8);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			String s = "T " + x + ", " + y + (!tileInBounds ? " #" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			gc.drawString(s, dx + 4 + 8, dy + 4 + 12);
		} else if (!DEBUG && !imageDrawn && tileInBounds) {
			gc.setBackground(waitBackground);
			gc.fillRectangle(dx, dy, TILE_SIZE, TILE_SIZE);
			gc.setForeground(waitForeground);
			for (int yl = 0; yl < TILE_SIZE; yl += 32) {
				gc.drawLine(dx, dy + yl, dx + TILE_SIZE, dy + yl);
			}
			for (int xl = 0; xl < TILE_SIZE; xl += 32) {
				gc.drawLine(dx + xl, dy, dx + xl, dy + TILE_SIZE);
			}
		}
	}

	/**
	 * Dispose internal data
	 */
	public void dispose() {
		waitBackground.dispose();
		waitForeground.dispose();
		cache.clear();
	}

	//

	/**
	 * Gets the tile server used for fetching tiles
	 * 
	 * @return the tile server
	 */
	public TileServer getTileServer() {
		return tileServer;
	}

	/**
	 * Sets the tile server used for fetching tiles
	 * 
	 * @param tileServer
	 *            the new tile server to use
	 */
	public void setTileServer(TileServer tileServer) {
		this.tileServer = tileServer;
		cache.clear();
	}

	//

	/**
	 * @see org.eclipse.nebula.widgets.geomap.internal.GeoMapPositioned#getMapPosition()
	 */
	@Override
	public Point getMapPosition() {
		return new Point(mapPosition.x, mapPosition.y);
	}

	/**
	 * @see org.eclipse.nebula.widgets.geomap.internal.GeoMapPositioned#setMapPosition(int,
	 *      int)
	 */
	@Override
	public void setMapPosition(int x, int y) {
		mapPosition.x = x;
		mapPosition.y = y;
	}

	/**
	 * @see org.eclipse.nebula.widgets.geomap.internal.GeoMapPositioned#getMinZoom()
	 */
	@Override
	public int getMinZoom() {
		return getTileServer().getMinZoom();
	}

	/**
	 * @see org.eclipse.nebula.widgets.geomap.internal.GeoMapPositioned#getMaxZoom()
	 */
	@Override
	public int getMaxZoom() {
		return getTileServer().getMaxZoom();
	}

	/**
	 * @see org.eclipse.nebula.widgets.geomap.internal.GeoMapPositioned#getZoom()
	 */
	@Override
	public int getZoom() {
		return zoom;
	}

	/**
	 * @see org.eclipse.nebula.widgets.geomap.internal.GeoMapPositioned#setZoom(int)
	 */
	@Override
	public void setZoom(int zoom) {
		zoomStamp.incrementAndGet();
		this.zoom = Math.min(tileServer.getMaxZoom(), zoom);
		int size = TILE_SIZE * (1 << zoom);
		mapSize.x = size;
		mapSize.y = size;
	}

	//

	private List<GeoMapHelperListener> geoMapHelperListeners = new ArrayList<>();

	@Override
	public void tileUpdated(TileRef tileRef) {
		for (GeoMapHelperListener listener : geoMapHelperListeners) {
			listener.tileUpdated(tileRef);
		}
	}

	/**
	 * Adds a GeoMapHelperListener that will be notified about tile updates
	 * 
	 * @param listener
	 *            the GeoMapHelperListener
	 */
	public void addGeoMapHelperListener(GeoMapHelperListener listener) {
		geoMapHelperListeners.add(listener);
	}

	/**
	 * Removes a GeoMapHelperListener
	 * 
	 * @param listener
	 *            the GeoMapHelperListener
	 */
	public void removeGeoMapHelperListener(GeoMapHelperListener listener) {
		geoMapHelperListeners.remove(listener);
	}

	//

	private List<InternalGeoMapListener> internalGeoMapListeners = new ArrayList<>();

	/**
	 * Adds an InternalGeoMapListener that will be notified about painting and cache
	 * updates
	 * 
	 * @param listener
	 *            the InternalGeoMapListener
	 */
	public void addInternalGeoMapListener(InternalGeoMapListener listener) {
		internalGeoMapListeners.add(listener);
	}

	/**
	 * Removes an InternalGeoMapListener
	 * 
	 * @param listener
	 *            the InternalGeoMapListener
	 */
	public void removeInternalGeoMapListener(InternalGeoMapListener listener) {
		internalGeoMapListeners.remove(listener);
	}

	/**
	 * @return the number of tiles
	 */
	public int getNumberOfTiles() {
		return Math.max(cache.size(), cacheSize);
	}
}
