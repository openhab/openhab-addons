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
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;

/**
 * The {@link LGThinQWMApiClientService} - Methods specifics for Washing/Drier Machines
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQWMApiClientService extends LGThinQApiClientService<WasherDryerCapability, WasherDryerSnapshot> {
    /**
     * Control the remote start feature
     * 
     * @param bridgeName Bridge Name
     * @param cap Capabilities of the device
     * @param deviceId LG Device ID
     * @param data Data to control the remote start
     * @throws LGThinqApiException if some error is reported from the LG API
     */
    void remoteStart(String bridgeName, WasherDryerCapability cap, String deviceId, Map<String, Object> data)
            throws LGThinqApiException;

    /**
     * Waking UP feature
     * 
     * @param bridgeName Bridge Name
     * @param deviceId LG Device Name
     * @param wakeUp to Wake Up (true/false)
     * @throws LGThinqApiException if some error is reported from the LG API
     */
    void wakeUp(String bridgeName, String deviceId, Boolean wakeUp) throws LGThinqApiException;
}
