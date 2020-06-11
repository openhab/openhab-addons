/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link NeoStatHandler} is the openHAB Handler for NeoStat devices Note:
 * inherits almost all the functionality of a {@link NeoBaseHandler}
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class NeoStatHandler extends NeoBaseHandler {

    public NeoStatHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String toNeoHubBuildCommandString(String channelId, Command command) {
        if (command instanceof QuantityType<?> && channelId.equals(CHAN_TARGET_TEMP)) {
            return String.format(CMD_CODE_TEMP, ((QuantityType<?>) command).toBigDecimal().toString(),
                    config.deviceNameInHub);
        } else

        if (command instanceof OnOffType && channelId.equals(CHAN_OCC_MODE_PRESENT)) {
            return String.format(CMD_CODE_AWAY, invert((OnOffType) command).toString(), config.deviceNameInHub);
        }
        return "";
    }

    @Override
    protected void toOpenHabSendChannelValues(NeoHubInfoResponse.DeviceInfo deviceInfo) {
        toOpenHabSendValueDebounced(CHAN_TARGET_TEMP,
                new QuantityType<Temperature>(deviceInfo.getTargetTemperature(), SIUnits.CELSIUS));

        toOpenHabSendValueDebounced(CHAN_ROOM_TEMP,
                new QuantityType<Temperature>(deviceInfo.getRoomTemperature(), SIUnits.CELSIUS));

        toOpenHabSendValueDebounced(CHAN_FLOOR_TEMP,
                new QuantityType<Temperature>(deviceInfo.getFloorTemperature(), SIUnits.CELSIUS));

        toOpenHabSendValueDebounced(CHAN_OCC_MODE_PRESENT, OnOffType.from(!deviceInfo.isStandby()));

        toOpenHabSendValueDebounced(CHAN_STAT_OUTPUT_STATE,
                (deviceInfo.isHeating() || deviceInfo.isPreHeating() ? new StringType(VAL_HEATING)
                        : new StringType(VAL_OFF)));
    }

}
