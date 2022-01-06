/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.spotify.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Interface to decouple Spotify Bridge Handler implementation from other code.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface SpotifyAccountHandler extends ThingHandler {

    /**
     * @return The {@link ThingUID} associated with this Spotify Account Handler
     */
    ThingUID getUID();

    /**
     * @return The label of the Spotify Bridge associated with this Spotify Account Handler
     */
    String getLabel();

    /**
     * @return The Spotify user name associated with this Spotify Account Handler
     */
    String getUser();

    /**
     * @return Returns true if the Spotify Bridge is authorized.
     */
    boolean isAuthorized();

    /**
     * @return List of Spotify devices associated with this Spotify Account Handler
     */
    List<Device> listDevices();

    /**
     * @return Returns true if the device is online
     */
    boolean isOnline();

    /**
     * Calls Spotify Api to obtain refresh and access tokens and persist data with Thing.
     *
     * @param redirectUrl The redirect url Spotify calls back to
     * @param reqCode The unique code passed by Spotify to obtain the refresh and access tokens
     * @return returns the name of the Spotify user that is authorized
     */
    String authorize(String redirectUrl, String reqCode);

    /**
     * Returns true if the given Thing UID relates to this {@link SpotifyAccountHandler} instance.
     *
     * @param thingUID The Thing UID to check
     * @return true if it relates to the given Thing UID
     */
    boolean equalsThingUID(String thingUID);

    /**
     * Formats the Url to use to call Spotify to authorize the application.
     *
     * @param redirectUri The uri Spotify will redirect back to
     * @return the formatted url that should be used to call Spotify Web Api with
     */
    String formatAuthorizationUrl(String redirectUri);
}
