/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.dashboard;

/**
 * A dashboard tile must be registered as a service in order to appear on the openHAB dashboard.
 * Note that it is currently not possible to provide a background image - this needs to be
 * available within the dashboard bundle itself at the moment.
 * 
 * @author Kai Kreuzer
 *
 */
public interface DashboardTile {

    /**
     * The name that should appear on the tile
     * @return name of the tile
     */
    String getName();
    
    /**
     * The url to point to (if it is a local UI, it should be a relative path starting with "../")
     * @return the url
     */
    String getUrl();
    
    /**
     * The url to point to for the dashboard tile.
     * (if it is a local UI, it should be a relative path starting with "../")
     * @return the tile url
     */
    String getImageUrl();

    /**
     * An HTML5 overlay icon to use for the tile, e.g. "html5", "android" or "apple".
     * 
     * @return the overlay to use
     */
    String getOverlay();

}
