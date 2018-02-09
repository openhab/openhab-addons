/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.eclipse.smarthome.core.thing.ChannelUID;

import name.eskildsen.zoneminder.IZoneMinderConnectionInfo;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;

/**
 * Interface for ZoneMinder handlers.
 *
 * @author Martin S. Eskildsen
 */
public interface ZoneMinderHandler {

    String getZoneMinderId();

    /**
     * Method used to relate a log entry to a thing
     */
    String getLogIdentifier();

    void updateAvaliabilityStatus(IZoneMinderConnectionInfo connection);

    void updateChannel(ChannelUID channel);

    void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionInfo connection)
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException;

    void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge);

}
