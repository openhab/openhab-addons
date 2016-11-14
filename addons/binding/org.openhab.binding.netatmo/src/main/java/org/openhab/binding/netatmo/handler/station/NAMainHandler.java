/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoDeviceConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.NADeviceAdapter;
import org.openhab.binding.netatmo.internal.NAStationAdapter;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAUserAdministrative;

/**
 * {@link NAMainHandler} is the base class for all current Netatmo
 * weather station equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAMainHandler extends NetatmoDeviceHandler<NetatmoDeviceConfiguration> {

    public NAMainHandler(Thing thing) {
        super(thing, NetatmoDeviceConfiguration.class);
    }

    @Override
    protected NADeviceAdapter<?> updateReadings(NetatmoBridgeHandler bridgeHandler, String equipmentId) {
        NAStationDataBody stationDataBody = bridgeHandler.getStationsDataBody(equipmentId);
        if (stationDataBody != null) {
            return new NAStationAdapter(stationDataBody);
        } else {
            return null;
        }
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        NAStationAdapter stationAdapter = (NAStationAdapter) device;
        NADashboardData dashboardData = stationAdapter.getDashboardData();
        NAUserAdministrative userAdministrative = device.getUserAdministrative();
        switch (channelId) {
            case CHANNEL_CO2:
                return toDecimalType(dashboardData.getCO2());
            case CHANNEL_TEMPERATURE:
                return toDecimalType(dashboardData.getTemperature());
            case CHANNEL_NOISE:
                return toDecimalType(dashboardData.getNoise());
            case CHANNEL_PRESSURE:
                return toDecimalType(dashboardData.getPressure());
            case CHANNEL_ABSOLUTE_PRESSURE:
                return toDecimalType(dashboardData.getAbsolutePressure());
            case CHANNEL_TIMEUTC:
                return toDateTimeType(dashboardData.getTimeUtc());
            case CHANNEL_HUMIDITY:
                return new PercentType(dashboardData.getHumidity().intValue());
            case CHANNEL_HUMIDEX:
                return toDecimalType(
                        WeatherUtils.getHumidex(dashboardData.getTemperature(), dashboardData.getHumidity()));
            case CHANNEL_HEATINDEX:
                return toDecimalType(
                        WeatherUtils.getHeatIndex(dashboardData.getTemperature(), dashboardData.getHumidity()));
            case CHANNEL_DEWPOINT:
                return toDecimalType(
                        WeatherUtils.getDewPoint(dashboardData.getTemperature(), dashboardData.getHumidity()));
            case CHANNEL_DEWPOINTDEP:
                Double dewpoint = WeatherUtils.getDewPoint(dashboardData.getTemperature(), dashboardData.getHumidity());
                return toDecimalType(WeatherUtils.getDewPointDep(dashboardData.getTemperature(), dewpoint));
            case CHANNEL_WIND_UNIT:
                return new DecimalType(userAdministrative.getWindunit());
            case CHANNEL_PRESSURE_UNIT:
                return new DecimalType(userAdministrative.getPressureunit());

            default:
                return super.getNAThingProperty(channelId);
        }
    }

}
