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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;

/**
 * The {@link LGThinQFridgeApiClientService} - Interface with specific methods for Fridge Devices
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQFridgeApiClientService
        extends LGThinQApiClientService<FridgeCapability, FridgeCanonicalSnapshot> {
    /**
     * Set fridge temperature
     * 
     * @param bridgeId Bridge ID
     * @param deviceId LG Device ID
     * @param fridgeCapability Fridge Capabilities
     * @param targetTemperatureIndex target temperature
     * @param tempUnit Temperature Unit
     * @param snapCmdData Snapshot template for the target temperature command
     * @throws LGThinqApiException If some error is reported from LG API
     */
    void setFridgeTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException;

    /**
     * Set fridge temperature
     * 
     * @param bridgeId Bridge ID
     * @param deviceId LG Device ID
     * @param fridgeCapability Fridge Capabilities
     * @param targetTemperatureIndex target temperature
     * @param tempUnit Temperature Unit
     * @param snapCmdData Snapshot template for the target temperature command
     * @throws LGThinqApiException If some error is reported from LG API
     */
    void setFreezerTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException;

    /**
     * Setup Express Mode
     * 
     * @param bridgeId Bridge ID
     * @param deviceId LG Device ID
     * @param expressModeIndex Empress mode desired
     * @throws LGThinqApiException If some error is reported from LG API
     */
    void setExpressMode(String bridgeId, String deviceId, String expressModeIndex) throws LGThinqApiException;

    /**
     * Set the Express Cool Mode
     * 
     * @param bridgeId Bridge ID
     * @param deviceId LG Device id
     * @param trueOnFalseOff ON/OFF the Cool Mode
     * @throws LGThinqApiException If some error is reported from LG API
     */
    void setExpressCoolMode(String bridgeId, String deviceId, boolean trueOnFalseOff) throws LGThinqApiException;

    /**
     * Set the Express Cool Mode
     * 
     * @param bridgeId Bridge ID
     * @param deviceId LG Device id
     * @param trueOnFalseOff ON/OFF the Eco Mode
     * @throws LGThinqApiException If some error is reported from LG API
     */
    void setEcoFriendlyMode(String bridgeId, String deviceId, boolean trueOnFalseOff) throws LGThinqApiException;

    /**
     *
     * @param bridgeId Bridge ID
     * @param deviceId LG Thinq Device ID
     * @param fridgeCapability Fridge Capabilities
     * @param trueOnFalseOff Set ON/OFF the ICE Plus
     * @param snapCmdData Snapshot template for the ICE Plus Command
     * @throws LGThinqApiException If some error is reported from LG API
     */
    void setIcePlus(String bridgeId, String deviceId, FridgeCapability fridgeCapability, boolean trueOnFalseOff,
            Map<String, Object> snapCmdData) throws LGThinqApiException;
}
