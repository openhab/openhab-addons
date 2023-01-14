/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link NeoStatHandler} is the openHAB Handler for NeoStat devices Note:
 * inherits almost all the functionality of a {@link NeoBaseHandler}
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NeoStatHandler extends NeoBaseHandler {

    public NeoStatHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String toNeoHubBuildCommandString(String channelId, Command command) {
        NeoBaseConfiguration config = this.config;
        if (config != null) {
            if (command instanceof QuantityType<?> && channelId.equals(CHAN_TARGET_TEMP)) {
                Command doCommand = command;
                QuantityType<?> temp = ((QuantityType<?>) command).toUnit(getTemperatureUnit());
                if (temp != null) {
                    doCommand = temp;
                }
                return String.format(CMD_CODE_TEMP, ((QuantityType<?>) doCommand).toBigDecimal().toString(),
                        config.deviceNameInHub);
            }

            if (command instanceof OnOffType && channelId.equals(CHAN_OCC_MODE_PRESENT)) {
                return String.format(CMD_CODE_AWAY, invert((OnOffType) command).toString(), config.deviceNameInHub);
            }
        }
        return "";
    }

    @Override
    protected void toOpenHabSendChannelValues(NeoHubAbstractDeviceData.AbstractRecord deviceRecord) {
        Unit<?> unit = getTemperatureUnit();
        boolean offline = deviceRecord.offline();

        toOpenHabSendValueDebounced(CHAN_TARGET_TEMP, new QuantityType<>(deviceRecord.getTargetTemperature(), unit),
                offline);

        toOpenHabSendValueDebounced(CHAN_ROOM_TEMP, new QuantityType<>(deviceRecord.getActualTemperature(), unit),
                offline);

        toOpenHabSendValueDebounced(CHAN_FLOOR_TEMP, new QuantityType<>(deviceRecord.getFloorTemperature(), unit),
                offline);

        toOpenHabSendValueDebounced(CHAN_OCC_MODE_PRESENT, OnOffType.from(!deviceRecord.isStandby()), offline);

        toOpenHabSendValueDebounced(CHAN_STAT_OUTPUT_STATE,
                (deviceRecord.isHeating() || deviceRecord.isPreHeating() ? new StringType(VAL_HEATING)
                        : new StringType(VAL_OFF)),
                offline);

        toOpenHabSendValueDebounced(CHAN_BATTERY_LOW_ALARM, OnOffType.from(deviceRecord.isBatteryLow()), offline);
    }
}
