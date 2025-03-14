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
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;

/**
 * Represents an API client service for LG ThinQ Washer/Dryer devices.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQWMApiClientService extends LGThinQApiClientService<WasherDryerCapability, WasherDryerSnapshot> {
    /**
     * Start the LG ThinQ Washer/Dryer device remotely using the specified capability and data.
     *
     * @param bridgeName The name of the bridge connected to the device
     * @param cap The WasherDryerCapability object representing the capabilities of the device
     * @param deviceId The ID of the LG ThinQ device
     * @param data A Map containing key-value pairs of data to be used for starting the device
     * @throws LGThinqApiException if an error occurs while trying to start the device remotely
     */
    void remoteStart(String bridgeName, WasherDryerCapability cap, String deviceId, Map<String, Object> data)
            throws LGThinqApiException;

    /**
     * Controls the wake-up feature of the LG ThinQ Washer/Dryer device.
     *
     * @param bridgeName The name of the bridge connected to the device
     * @param deviceId The ID of the LG device
     * @param wakeUp Boolean value indicating whether to wake up the device
     * @throws LGThinqApiException if an error occurs while trying to wake up the device
     */
    void wakeUp(String bridgeName, String deviceId, Boolean wakeUp) throws LGThinqApiException;
}
