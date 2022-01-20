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
package org.openhab.binding.netatmo.internal.station;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAStationModule;

/**
 * {@link NAModule2Handler} is the class used to handle the wind module
 * capable of reporting wind angle and strength
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAModule2Handler extends NetatmoModuleHandler<NAStationModule> {

    public NAModule2Handler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected void updateProperties(NAStationModule moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        NADashboardData dashboardData = getModule().map(m -> m.getDashboardData()).orElse(null);
        if (dashboardData != null) {
            switch (channelId) {
                case CHANNEL_WIND_ANGLE:
                    return toQuantityType(dashboardData.getWindAngle(), API_WIND_DIRECTION_UNIT);
                case CHANNEL_WIND_STRENGTH:
                    return toQuantityType(dashboardData.getWindStrength(), API_WIND_SPEED_UNIT);
                case CHANNEL_GUST_ANGLE:
                    return toQuantityType(dashboardData.getGustAngle(), API_WIND_DIRECTION_UNIT);
                case CHANNEL_GUST_STRENGTH:
                    return toQuantityType(dashboardData.getGustStrength(), API_WIND_SPEED_UNIT);
                case CHANNEL_TIMEUTC:
                    return toDateTimeType(dashboardData.getTimeUtc(), timeZoneProvider.getTimeZone());
                case CHANNEL_MAX_WIND_STRENGTH:
                    return toQuantityType(dashboardData.getMaxWindStr(), API_WIND_SPEED_UNIT);
                case CHANNEL_DATE_MAX_WIND_STRENGTH:
                    return toDateTimeType(dashboardData.getDateMaxWindStr(), timeZoneProvider.getTimeZone());
            }
        }
        return super.getNAThingProperty(channelId);
    }

    @Override
    protected boolean isReachable() {
        boolean result = false;
        Optional<NAStationModule> module = getModule();
        if (module.isPresent()) {
            Boolean reachable = module.get().isReachable();
            result = reachable != null ? reachable.booleanValue() : false;
        }
        return result;
    }
}
