/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.WeatherUtils;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAStationModule;

/**
 * {@link NAModule4Handler} is the class used to handle the additional
 * indoor module capable of reporting temperature, humidity and CO2 level
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModule4Handler extends NetatmoModuleHandler<NAStationModule> {

    public NAModule4Handler(@NonNull Thing thing) {
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
            switch (channelId) {
                case CHANNEL_TEMP_TREND:
                    return toStringType(dashboardData.getTempTrend());
                case CHANNEL_CO2:
                    return toQuantityType(dashboardData.getCO2(), API_CO2_UNIT);
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
                case CHANNEL_TIMEUTC:
                    return toDateTimeType(dashboardData.getTimeUtc());
                case CHANNEL_HUMIDITY:
                    return toQuantityType(dashboardData.getHumidity(), API_HUMIDITY_UNIT);
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
        return super.getNAThingProperty(channelId);
    }

}
