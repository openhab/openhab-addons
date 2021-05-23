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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;

/**
 * {@link CarNetRemoteServicePreHeat} implements the remote heater service
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetRemoteServicePreHeat extends CarNetRemoteBaseService {
    public CarNetRemoteServicePreHeat(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_REMOTE_HEATING, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        // Honk&Flash requires CarFinder service to get geo position
        if (api.isRemoteServiceAvailable(CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION)) {
            addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PREHEAT, ITEMT_SWITCH, null, false, false);
            return true;
        }
        return false;
    }

}
