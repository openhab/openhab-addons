/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;

import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAThermostatDataBody;
import io.swagger.client.model.NAYearMonth;

/**
 * {@link NAPlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAPlugHandler extends NetatmoDeviceHandler<NAPlug> {

    public NAPlugHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected NAPlug updateReadings() {
        NAPlug result = null;
        NAThermostatDataBody thermostatDataBody = getBridgeHandler().getThermostatsDataBody(getId());
        if (thermostatDataBody != null) {
            result = thermostatDataBody.getDevices().stream().filter(device -> device.getId().equalsIgnoreCase(getId()))
                    .findFirst().orElse(null);
            if (result != null) {
                result.getModules().forEach(child -> childs.put(child.getId(), child));
            }
        }
        return result;
    }

    @Override
    protected void updateProperties(NAPlug deviceData) {
        updateProperties(deviceData.getFirmware(), deviceData.getType());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_CONNECTED_BOILER:
                return device != null ? toOnOffType(device.getPlugConnectedBoiler()) : UnDefType.UNDEF;
            case CHANNEL_LAST_PLUG_SEEN:
                return device != null ? toDateTimeType(device.getLastPlugSeen()) : UnDefType.UNDEF;
            case CHANNEL_LAST_BILAN:
                return toDateTimeType(getLastBilan());
        }
        return super.getNAThingProperty(channelId);
    }

    public @Nullable ZonedDateTime getLastBilan() {
        if (device != null) {
            NAYearMonth lastBilan = device.getLastBilan();
            if (lastBilan != null) {
                ZonedDateTime zonedDT = ZonedDateTime.of(lastBilan.getY(), lastBilan.getM(), 1, 0, 0, 0, 0,
                        ZonedDateTime.now().getZone());
                return zonedDT.with(TemporalAdjusters.lastDayOfMonth());
            }
        }
        return null;
    }

    @Override
    protected @Nullable Integer getDataTimestamp() {
        if (device != null) {
            Integer lastStored = device.getLastStatusStore();
            if (lastStored != null) {
                return lastStored;
            }
        }
        return null;
    }

}
