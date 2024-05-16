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
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;

/**
 * The {@link LGThinQFridgeApiClientService}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQFridgeApiClientService
        extends LGThinQApiClientService<FridgeCapability, FridgeCanonicalSnapshot> {
    void setFridgeTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException;

    void setFreezerTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException;

    void setExpressMode(String bridgeId, String deviceId, String expressModeIndex) throws LGThinqApiException;

    void setExpressCoolMode(String bridgeId, String deviceId, boolean trueOnFalseOff) throws LGThinqApiException;

    void setEcoFriendlyMode(String bridgeId, String deviceId, boolean trueOnFalseOff) throws LGThinqApiException;

    void setIcePlus(String bridgeId, String deviceId, FridgeCapability fridgeCapability, boolean trueOnFalseOff,
            Map<String, Object> snapCmdData) throws LGThinqApiException;
}
