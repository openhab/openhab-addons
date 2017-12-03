/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.WeatherUtils;
import org.openhab.binding.netatmo.internal.config.NetatmoModuleConfiguration;

import io.swagger.client.model.NADashboardData;

/**
 * {@link NAModule1Handler} is the class used to handle the outdoor module
 * capable of reporting temperature and humidity
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModule1Handler extends NetatmoModuleHandler<NetatmoModuleConfiguration> {

    public NAModule1Handler(Thing thing) {
        super(thing, NetatmoModuleConfiguration.class);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            NADashboardData dashboardData = module.getDashboardData();
            switch (channelId) {
                case CHANNEL_TEMP_TREND:
                    return ChannelTypeUtils.toStringType(dashboardData.getTempTrend());
                case CHANNEL_TEMPERATURE:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getTemperature());
                case CHANNEL_DATE_MIN_TEMP:
                    return ChannelTypeUtils.toDateTimeType(dashboardData.getDateMinTemp());
                case CHANNEL_DATE_MAX_TEMP:
                    return ChannelTypeUtils.toDateTimeType(dashboardData.getDateMaxTemp());
                case CHANNEL_MIN_TEMP:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getMinTemp());
                case CHANNEL_MAX_TEMP:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getMaxTemp());
                case CHANNEL_HUMIDITY:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getHumidity());
                case CHANNEL_TIMEUTC:
                    return ChannelTypeUtils.toDateTimeType(dashboardData.getTimeUtc());
                case CHANNEL_HUMIDEX:
                    return ChannelTypeUtils.toDecimalType(
                            WeatherUtils.getHumidex(dashboardData.getTemperature(), dashboardData.getHumidity()));
                case CHANNEL_HEATINDEX:
                    return ChannelTypeUtils.toDecimalType(
                            WeatherUtils.getHeatIndex(dashboardData.getTemperature(), dashboardData.getHumidity()));
                case CHANNEL_DEWPOINT:
                    return ChannelTypeUtils.toDecimalType(
                            WeatherUtils.getDewPoint(dashboardData.getTemperature(), dashboardData.getHumidity()));
                case CHANNEL_DEWPOINTDEP:
                    Double dewpoint = WeatherUtils.getDewPoint(dashboardData.getTemperature(),
                            dashboardData.getHumidity());
                    return ChannelTypeUtils
                            .toDecimalType(WeatherUtils.getDewPointDep(dashboardData.getTemperature(), dewpoint));

            }
        }
        return super.getNAThingProperty(channelId);
    }

}
