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
package org.openhab.binding.lgthinq.lgservices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACTargetTmp;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ExtendedDeviceInfo;

/**
 * The {@link LGThinQACApiClientService} - Common interface to be used by the AC Handle to access LG API Services in V1
 * & v2
 * protocol versions
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQACApiClientService extends LGThinQApiClientService<ACCapability, ACCanonicalSnapshot> {
    /**
     * Change AC Operation Mode (Cool, Heat, etc.)
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param newOpMode - The new operation mode to be setup
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException;

    /**
     * Change the AC Fan Speed.
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param newFanSpeed - new Fan Speed to be setup
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException;

    /**
     * Change the fan vertical orientation
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param currentSnap - Current data snapshot
     * @param newStep - new vertical position
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void changeStepUpDown(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException;

    /**
     * Change the fan horizontal orientation
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param currentSnap - Current data snapshot
     * @param newStep - new horizontal position
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void changeStepLeftRight(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException;

    /**
     * Change the target temperature
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param newTargetTemp - new target temperature
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException;

    /**
     * Turn On/Off the Jet Mode feature
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param modeOnOff - turn on/off
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void turnCoolJetMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Turn On/Off the Air Clean feature
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param modeOnOff - turn on/off
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void turnAirCleanMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Turn On/Off the Auto Dry feature
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param modeOnOff - turn on/off
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void turnAutoDryMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Turn On/Off the Energy Saving feature
     * 
     * @param bridgeName - name of the bridge
     * @param deviceId - ID of the LG Thinq Device
     * @param modeOnOff - turn on/off
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    void turnEnergySavingMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Get Extended Device Information (Energy consumption, filter level, etc).
     * 
     * @param bridgeName Bridge name
     * @param deviceId - ID of the LG Thinq Device
     * @return ExtendedDeviceInfo containing the device extended data
     * @throws LGThinqApiException - If some error invoking LG API.
     */
    ExtendedDeviceInfo getExtendedDeviceInfo(String bridgeName, String deviceId) throws LGThinqApiException;
}
