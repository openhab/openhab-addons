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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;

/**
 * {@link LGThinQFridgeApiClientService} defines methods for interacting with LG ThinQ refrigerator devices.
 * It extends {@link LGThinQApiClientService} to provide core functionalities while adding refrigerator-specific
 * operations.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQFridgeApiClientService
        extends LGThinQApiClientService<FridgeCapability, FridgeCanonicalSnapshot> {

    /**
     * Sets the refrigerator temperature.
     *
     * @param bridgeId The bridge ID managing the device.
     * @param deviceId The unique identifier of the LG refrigerator.
     * @param fridgeCapability The capabilities definition of the refrigerator.
     * @param targetTemperatureIndex The desired temperature index.
     * @param tempUnit The unit of temperature measurement.
     * @param snapCmdData Optional snapshot template for the temperature command.
     * @throws LGThinqApiException If an error occurs while communicating with the LG API.
     */
    void setFridgeTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException;

    /**
     * Sets the freezer temperature.
     *
     * @param bridgeId The bridge ID managing the device.
     * @param deviceId The unique identifier of the LG freezer.
     * @param fridgeCapability The capabilities definition of the freezer.
     * @param targetTemperatureIndex The desired temperature index.
     * @param tempUnit The unit of temperature measurement.
     * @param snapCmdData Optional snapshot template for the temperature command.
     * @throws LGThinqApiException If an error occurs while communicating with the LG API.
     */
    void setFreezerTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException;

    /**
     * Activates or deactivates the Express Mode.
     *
     * @param bridgeId The bridge ID managing the device.
     * @param deviceId The unique identifier of the LG refrigerator.
     * @param expressModeIndex The desired express mode setting.
     * @throws LGThinqApiException If an error occurs while communicating with the LG API.
     */
    void setExpressMode(String bridgeId, String deviceId, String expressModeIndex) throws LGThinqApiException;

    /**
     * Enables or disables the Express Cool Mode.
     *
     * @param bridgeId The bridge ID managing the device.
     * @param deviceId The unique identifier of the LG refrigerator.
     * @param enable {@code true} to enable Express Cool Mode, {@code false} to disable.
     * @throws LGThinqApiException If an error occurs while communicating with the LG API.
     */
    void setExpressCoolMode(String bridgeId, String deviceId, boolean enable) throws LGThinqApiException;

    /**
     * Enables or disables the Eco-Friendly Mode.
     *
     * @param bridgeId The bridge ID managing the device.
     * @param deviceId The unique identifier of the LG refrigerator.
     * @param enable {@code true} to enable Eco Mode, {@code false} to disable.
     * @throws LGThinqApiException If an error occurs while communicating with the LG API.
     */
    void setEcoFriendlyMode(String bridgeId, String deviceId, boolean enable) throws LGThinqApiException;

    /**
     * Enables or disables the Ice Plus feature.
     *
     * @param bridgeId The bridge ID managing the device.
     * @param deviceId The unique identifier of the LG refrigerator.
     * @param fridgeCapability The capabilities definition of the refrigerator.
     * @param enable {@code true} to enable Ice Plus, {@code false} to disable.
     * @param snapCmdData A map containing the snapshot template for the Ice Plus command.
     * @throws LGThinqApiException If an error occurs while communicating with the LG API.
     */
    void setIcePlus(String bridgeId, String deviceId, FridgeCapability fridgeCapability, boolean enable,
            Map<String, Object> snapCmdData) throws LGThinqApiException;
}
