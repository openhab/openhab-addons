/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify;

import org.openhab.ui.dashboard.DashboardTile;
import org.osgi.service.component.annotations.Component;

/**
 * Tile class to add Spotify authorization servlet to the Dashboard.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@Component(service = DashboardTile.class, immediate = true, configurationPid = "binding.spotify.dashboardtile")
public class SpotifyDashboardTile implements DashboardTile {

    @Override
    public String getName() {
        return "Connect Spotify";
    }

    @Override
    public String getUrl() {
        return ".." + SpotifyBindingConstants.SPOTIFY_ALIAS;
    }

    @Override
    public String getOverlay() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return getUrl() + SpotifyBindingConstants.SPOTIFY_IMG_ALIAS + "/osc.svg";
    }
}
