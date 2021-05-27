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
import org.openhab.binding.carnet.internal.CarNetUtils;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNHeaterVentilation.CarNetHeaterVentilationStatus;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;

/**
 * {@link CarNetServicePreHeat} implements the remote heater service
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServicePreHeat extends CarNetBaseService {
    public CarNetServicePreHeat(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_REMOTE_HEATING, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        // rheating includes pre-heater and ventilation
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PREHEAT, ITEMT_SWITCH, null, false, false);
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_VENT, ITEMT_SWITCH, null, false, false);
        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_DURATION, ITEMT_NUMBER, null, false, false);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        boolean updated = false;
        CarNetHeaterVentilationStatus hvs = api.getHeaterVentilationStatus();
        String group = CHANNEL_GROUP_CONTROL;
        if (hvs.climatisationStateReport != null) {
            if (hvs.climatisationStateReport.climatisationState != null) {
                String sd = hvs.climatisationStateReport.climatisationState;
                if ("heating".equalsIgnoreCase(sd)) {
                    updated |= updateChannel(group, CHANNEL_CONTROL_PREHEAT, CarNetUtils.getOnOff(1));
                } else if ("ventilation".equalsIgnoreCase(sd)) {
                    updated |= updateChannel(group, CHANNEL_CONTROL_VENT, CarNetUtils.getOnOff(1));
                } else {
                    updated |= updateChannel(group, CHANNEL_CONTROL_PREHEAT, CarNetUtils.getOnOff(0));
                    updated |= updateChannel(group, CHANNEL_CONTROL_VENT, CarNetUtils.getOnOff(0));
                }
            }
        }
        return updated;
    }
}
