/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api.services;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.util.Map;

import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;

/**
 * {@link CarNetServiceHonkFlash} implements honk&flash service.
 *
 * @author Markus Michels - Initial contribution
 */
public class CarNetServiceHonkFlash extends CarNetBaseService {
    public CarNetServiceHonkFlash(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_REMOTE_HONK_AND_FLASH, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        // Honk&Flash requires CarFinder service to get geo position
        if (api.isRemoteServiceAvailable(CNAPI_SERVICE_CAR_FINDER)) {
            addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_FLASH, ITEMT_SWITCH, null, false, false);
            addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_HONKFLASH, ITEMT_SWITCH, null, false, false);
            return true;
        }
        return false;
    }
}
