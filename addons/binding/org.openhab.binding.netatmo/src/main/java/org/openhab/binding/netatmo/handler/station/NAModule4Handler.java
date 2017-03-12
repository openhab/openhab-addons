/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.model.NADashboardData;

/**
 * {@link NAModule4Handler} is the class used to handle the additional
 * indoor module capable of reporting temperature, humidity and CO2 level
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModule4Handler extends NetatmoModuleHandler<NetatmoModuleConfiguration> {

    public NAModule4Handler(Thing thing) {
        super(thing, NetatmoModuleConfiguration.class);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            NADashboardData dashboardData = module.getDashboardData();
            switch (channelId) {
                case CHANNEL_CO2:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getCO2());
                case CHANNEL_TEMPERATURE:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getTemperature());
                case CHANNEL_TIMEUTC:
                    return ChannelTypeUtils.toDateTimeType(dashboardData.getTimeUtc());
                case CHANNEL_HUMIDITY:
                    return ChannelTypeUtils.toDecimalType(dashboardData.getHumidity());
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
