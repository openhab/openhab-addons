/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

package org.openhab.binding.romyrobot.internal.api;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RomyApi} interface defines the functions which are
 * controllable on the Romy API interface.
 *
 * @author Bernhard Kreuz - Initial contribution
 */
@NonNullByDefault
public interface RomyApi {

    /**
     * get robots firmwware version and name
     * is available when robots http interface is locked
     * 
     * @throws Exception
     *
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    void refreshID() throws Exception;

    /**
     * get robots api protocol version
     * is available when robots http interface is locked
     * 
     * @throws Exception
     *
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    void refreshProtocolVersion() throws Exception;

    /**
     * Sends all the GET requests and stores/cache the responses for use by the API to prevent the need for multiple
     * requests.
     * 
     * @throws Exception
     *
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    void refresh() throws Exception;

    /**
     * 
     * @return Firmware Version of robot
     */
    @Nullable
    String getFirmwareVersion();

    /**
     *
     * @return Firmware Version of robot
     */
    @Nullable
    String getName();

    /**
     * 
     * @return Status / Mode robot is currently in
     */
    @Nullable
    String getModeString();

    /**
     * 
     * @return currently set pump volume
     */
    @Nullable
    String getActivePumpVolume();

    /**
     * 
     * @param volume the pump volume used on next start
     */
    void setActivePumpVolume(String volume);

    /**
     * 
     * @return cleaning strategy
     */
    @Nullable
    String getStrategy();

    /**
     * 
     * @param strategy cleaning strategy
     */
    void setStrategy(String strategy);

    /**
     * 
     * @return suction mode, see thing xml for details
     */
    @Nullable
    String getSuctionMode();

    /**
     * 
     * @param suctionMode suction mode to be used for next start
     */
    void setSuctionMode(String suctionMode);

    /**
     * 
     * @return current battery level
     */
    int getBatteryLevel();

    /**
     * 
     * @return weither the vacuum is charging
     */
    @Nullable
    String getChargingStatus();

    /**
     * 
     * @return WiFi rssi
     */
    int getRssi();

    /**
     * 
     * @return current power status of the vacuum
     */
    @Nullable
    String getPowerStatus();

    /**
     * 
     * @return a String listing the available maps
     */
    HashMap<String, String> getAvailableMaps();

    /**
     * 
     * @return a String listing the available maps
     */
    String getAvailableMapsJson();

    /**
     * 
     * @return Minor Interface Version
     */
    int getProtocolVersionMinor();

    /**
     * 
     * @return Major Interface Version
     */
    int getProtocolVersionMajor();

    /**
     * 
     * @param command command to execute
     * @throws Exception
     */
    void executeCommand(String command) throws Exception;
}
