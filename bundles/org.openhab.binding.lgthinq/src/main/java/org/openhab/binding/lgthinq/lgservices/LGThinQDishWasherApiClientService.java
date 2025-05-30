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
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherSnapshot;

/**
 * {@link LGThinQDishWasherApiClientService} provides specific methods for interacting with LG ThinQ dishwashers.
 * It extends the {@link LGThinQApiClientService} to inherit core functionalities while adding specialized methods
 * for dishwashers.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQDishWasherApiClientService
        extends LGThinQApiClientService<DishWasherCapability, DishWasherSnapshot> {

    /**
     * Initiates a remote start operation for the dishwasher.
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param cap The capability definition of the dishwasher.
     * @param deviceId The unique identifier of the LG ThinQ dishwasher.
     * @param data A map containing the required parameters for remote start.
     */
    void remoteStart(String bridgeName, DishWasherCapability cap, String deviceId, Map<String, Object> data);
}
