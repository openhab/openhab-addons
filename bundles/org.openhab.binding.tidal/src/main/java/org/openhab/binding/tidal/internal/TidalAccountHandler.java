/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tidal.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Interface to decouple Tidal Bridge Handler implementation from other code.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface TidalAccountHandler extends ThingHandler {

    /**
     * @return The {@link ThingUID} associated with this Tidal Account Handler
     */
    ThingUID getUID();

    /**
     * @return The label of the Tidal Bridge associated with this Tidal Account Handler
     */
    String getLabel();

    /**
     * @return The Tidal user name associated with this Tidal Account Handler
     */
    String getUser();

    /**
     * @return Returns true if the Tidal Bridge is authorized.
     */
    boolean isAuthorized();

    /**
     * @return Returns true if the device is online
     */
    boolean isOnline();

    /**
     * Calls Tidal Api to obtain refresh and access tokens and persist data with Thing.
     *
     * @param redirectUrl The redirect url Tidal calls back to
     * @param reqCode The unique code passed by Tidal to obtain the refresh and access tokens
     * @return returns the name of the Tidal user that is authorized
     */
    String authorize(String redirectUrl, String reqCode);

    /**
     * Returns true if the given Thing UID relates to this {@link TidalAccountHandler} instance.
     *
     * @param thingUID The Thing UID to check
     * @return true if it relates to the given Thing UID
     */
    boolean equalsThingUID(String thingUID);

    /**
     * Formats the Url to use to call Tidal to authorize the application.
     *
     * @param redirectUri The uri Tidal will redirect back to
     * @return the formatted url that should be used to call Tidal Web Api with
     */
    String formatAuthorizationUrl(String redirectUri);
}
