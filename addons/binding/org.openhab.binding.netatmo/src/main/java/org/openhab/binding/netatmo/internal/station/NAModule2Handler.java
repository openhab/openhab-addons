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
package org.openhab.binding.netatmo.internal.station;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAStationModule;

/**
 * {@link NAModule2Handler} is the class used to handle the wind module
 * capable of reporting wind angle and strength
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NAModule2Handler extends NetatmoModuleHandler<NAStationModule> {

    public NAModule2Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateProperties(NAStationModule moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            NADashboardData dashboardData = module.getDashboardData();
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
                        return toDateTimeType(dashboardData.getTimeUtc());
                    case CHANNEL_MAX_WIND_STRENGTH:
                        return toQuantityType(dashboardData.getMaxWindStr(), API_WIND_SPEED_UNIT);
                    case CHANNEL_DATE_MAX_WIND_STRENGTH:
                        return toDateTimeType(dashboardData.getDateMaxWindStr());
                }
            }
        }
        return super.getNAThingProperty(channelId);
    }
}
