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
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherSnapshot;

/**
 * The {@link LGThinQDishWasherApiClientService} - implements specific methods for DishWashers
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQDishWasherApiClientService
        extends LGThinQApiClientService<DishWasherCapability, DishWasherSnapshot> {
    /**
     * Remote start machine funcion
     * 
     * @param bridgeName Bridge name
     * @param cap Capabilities of the device
     * @param deviceId LG Device ID
     * @param data data to sent to remote start
     */
    void remoteStart(String bridgeName, DishWasherCapability cap, String deviceId, Map<String, Object> data);
}
