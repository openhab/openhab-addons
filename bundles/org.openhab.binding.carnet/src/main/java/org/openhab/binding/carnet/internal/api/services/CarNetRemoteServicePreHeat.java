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
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_REMOTE_HEATING;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.core.library.unit.SIUnits;

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
        // rheating includes pre-heater and ventilation
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PREHEAT, ITEMT_SWITCH, null, false, false);
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_VENT, ITEMT_SWITCH, null, false, false);
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CLIMATER_TARGET_TEMP, ITEMT_TEMP, SIUnits.CELSIUS, false,
                false);
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_DURATION, ITEMT_NUMBER, null, true, false);
        return true;
    }
}
