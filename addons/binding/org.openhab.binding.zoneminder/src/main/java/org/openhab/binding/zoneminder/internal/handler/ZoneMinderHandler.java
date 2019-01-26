/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.handler;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.eclipse.smarthome.core.thing.ChannelUID;

import name.eskildsen.zoneminder.IZoneMinderConnectionInfo;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;

/**
 * Interface for ZoneMinder handlers.
 *
 * @author Martin S. Eskildsen - Initial contribution
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
