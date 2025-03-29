/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * The {@link LGThinQACApiClientService} interface provides a common abstraction for handling AC-related
 * API interactions with LG ThinQ devices. It supports both protocol versions V1 and V2.
 * <p>
 * This interface allows external components to change various air conditioner settings, such as
 * operation mode, fan speed, temperature, and additional features like Jet Mode and Energy Saving Mode.
 * </p>
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQACApiClientService extends LGThinQApiClientService<ACCapability, ACCanonicalSnapshot> {

    /**
     * Changes the air conditioner's operation mode (e.g., Cool, Heat, Fan).
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param newOpMode The new operation mode to be set.
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException;

    /**
     * Adjusts the fan speed of the air conditioner.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param newFanSpeed The desired fan speed level.
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException;

    /**
     * Adjusts the vertical orientation of the AC fan.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param currentSnap The current snapshot of AC device data.
     * @param newStep The new vertical position.
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void changeStepUpDown(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException;

    /**
     * Adjusts the horizontal orientation of the AC fan.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param currentSnap The current snapshot of AC device data.
     * @param newStep The new horizontal position.
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void changeStepLeftRight(String bridgeName, String deviceId, ACCanonicalSnapshot currentSnap, int newStep)
            throws LGThinqApiException;

    /**
     * Changes the target temperature of the air conditioner.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param newTargetTemp The new target temperature to be set.
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException;

    /**
     * Enables or disables the Jet Mode feature.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param modeOnOff The desired state ("on" to enable, "off" to disable).
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void turnCoolJetMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Enables or disables the Air Clean mode.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param modeOnOff The desired state ("on" to enable, "off" to disable).
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void turnAirCleanMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Enables or disables the Auto Dry feature.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param modeOnOff The desired state ("on" to enable, "off" to disable).
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void turnAutoDryMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Enables or disables the Energy Saving mode.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @param modeOnOff The desired state ("on" to enable, "off" to disable).
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    void turnEnergySavingMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException;

    /**
     * Retrieves extended device information, such as energy consumption and filter status.
     *
     * @param bridgeName The name of the bridge managing the device connection.
     * @param deviceId The unique ID of the LG ThinQ AC device.
     * @return An {@link ExtendedDeviceInfo} object containing the extended data of the device.
     * @throws LGThinqApiException If an error occurs while invoking the LG API.
     */
    ExtendedDeviceInfo getExtendedDeviceInfo(String bridgeName, String deviceId) throws LGThinqApiException;
}
