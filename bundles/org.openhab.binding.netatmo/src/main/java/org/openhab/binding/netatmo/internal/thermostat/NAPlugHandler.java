/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.thermostat;

import static org.openhab.binding.netatmo.internal.APIUtils.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAYearMonth;

/**
 * {@link NAPlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
@NonNullByDefault
public class NAPlugHandler extends NetatmoDeviceHandler<NAPlug> {

    public NAPlugHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected Optional<NAPlug> updateReadings() {
        Optional<NAPlug> result = getBridgeHandler().flatMap(handler -> handler.getThermostatsDataBody(getId()))
                .map(dataBody -> nonNullStream(dataBody.getDevices())
                        .filter(device -> device.getId().equalsIgnoreCase(getId())).findFirst().orElse(null));
        result.ifPresent(device -> {
            nonNullList(device.getModules()).forEach(child -> childs.put(child.getId(), child));
        });
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
                return getDevice().map(d -> toOnOffType(d.getPlugConnectedBoiler())).orElse(UnDefType.UNDEF);
            case CHANNEL_LAST_PLUG_SEEN:
                return getDevice().map(d -> toDateTimeType(d.getLastPlugSeen(), timeZoneProvider.getTimeZone()))
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_LAST_BILAN:
                return toDateTimeType(getLastBilan());
        }
        return super.getNAThingProperty(channelId);
    }

    public @Nullable ZonedDateTime getLastBilan() {
        Optional<NAYearMonth> lastBilan = getDevice().map(d -> d.getLastBilan());
        if (lastBilan.isPresent()) {
            ZonedDateTime zonedDT = ZonedDateTime.of(lastBilan.get().getY(), lastBilan.get().getM(), 1, 0, 0, 0, 0,
                    ZonedDateTime.now().getZone());
            return zonedDT.with(TemporalAdjusters.lastDayOfMonth());
        }
        return null;
    }

    @Override
    protected Optional<Integer> getDataTimestamp() {
        return getDevice().map(d -> d.getLastStatusStore());
    }
}
