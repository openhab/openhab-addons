/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal;

import org.openhab.binding.spotify.handler.SpotifyHandler;

/**
 * The {@link SpotifyAuthService} is used to register {@link SpotifyHandler} for authorization with Spotify Web
 * API
 *
 * @author Andreas Stenlund - Initial contribution
 */
public interface SpotifyAuthService {

    public void authenticateSpotifyPlayer(SpotifyHandler handler);

}
