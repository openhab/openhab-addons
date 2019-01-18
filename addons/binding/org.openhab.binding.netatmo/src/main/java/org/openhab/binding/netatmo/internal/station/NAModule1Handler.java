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
import org.openhab.binding.netatmo.internal.WeatherUtils;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAStationModule;

/**
 * {@link NAModule1Handler} is the class used to handle the outdoor module
 * capable of reporting temperature and humidity
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NAModule1Handler extends NetatmoModuleHandler<NAStationModule> {

    public NAModule1Handler(Thing thing) {
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
                    case CHANNEL_TEMP_TREND:
                        return toStringType(dashboardData.getTempTrend());
                    case CHANNEL_TEMPERATURE:
                        return toQuantityType(dashboardData.getTemperature(), API_TEMPERATURE_UNIT);
                    case CHANNEL_DATE_MIN_TEMP:
                        return toDateTimeType(dashboardData.getDateMinTemp());
                    case CHANNEL_DATE_MAX_TEMP:
                        return toDateTimeType(dashboardData.getDateMaxTemp());
                    case CHANNEL_MIN_TEMP:
                        return toQuantityType(dashboardData.getMinTemp(), API_TEMPERATURE_UNIT);
                    case CHANNEL_MAX_TEMP:
                        return toQuantityType(dashboardData.getMaxTemp(), API_TEMPERATURE_UNIT);
                    case CHANNEL_HUMIDITY:
                        return toQuantityType(dashboardData.getHumidity(), API_HUMIDITY_UNIT);
                    case CHANNEL_TIMEUTC:
                        return toDateTimeType(dashboardData.getTimeUtc());
                    case CHANNEL_HUMIDEX:
                        return toDecimalType(
                                WeatherUtils.getHumidex(dashboardData.getTemperature(), dashboardData.getHumidity()));
                    case CHANNEL_HEATINDEX:
                        return toQuantityType(
                                WeatherUtils.getHeatIndex(dashboardData.getTemperature(), dashboardData.getHumidity()),
                                API_TEMPERATURE_UNIT);
                    case CHANNEL_DEWPOINT:
                        return toQuantityType(
                                WeatherUtils.getDewPoint(dashboardData.getTemperature(), dashboardData.getHumidity()),
                                API_TEMPERATURE_UNIT);
                    case CHANNEL_DEWPOINTDEP:
                        Double dewpoint = WeatherUtils.getDewPoint(dashboardData.getTemperature(),
                                dashboardData.getHumidity());
                        return toQuantityType(WeatherUtils.getDewPointDep(dashboardData.getTemperature(), dewpoint),
                                API_TEMPERATURE_UNIT);
                }
            }
        }
        return super.getNAThingProperty(channelId);
    }
}
