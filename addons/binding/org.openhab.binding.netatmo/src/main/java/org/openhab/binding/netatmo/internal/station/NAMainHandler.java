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
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.WeatherUtils;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAStationDataBody;

/**
 * {@link NAMainHandler} is the base class for all current Netatmo
 * weather station equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAMainHandler extends NetatmoDeviceHandler<NAMain> {

    public NAMainHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected NAMain updateReadings() {
        NAMain result = null;
        NAStationDataBody stationDataBody = getBridgeHandler().getStationsDataBody(getId());
        if (stationDataBody != null) {
            userAdministrative = stationDataBody.getUser().getAdministrative();

            result = stationDataBody.getDevices().stream().filter(device -> device.getId().equalsIgnoreCase(getId()))
                    .findFirst().get();
            if (result != null) {
                result.getModules().forEach(child -> childs.put(child.getId(), child));
            }
        }
        return result;
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (device != null) {
            NADashboardData dashboardData = device.getDashboardData();
            switch (channelId) {
                case CHANNEL_CO2:
                    return toDecimalType(dashboardData.getCO2());
                case CHANNEL_TEMPERATURE:
                    return toDecimalType(dashboardData.getTemperature());
                case CHANNEL_MIN_TEMP:
                    return toDecimalType(dashboardData.getMinTemp());
                case CHANNEL_MAX_TEMP:
                    return toDecimalType(dashboardData.getMaxTemp());
                case CHANNEL_TEMP_TREND:
                    return toStringType(dashboardData.getTempTrend());
                case CHANNEL_NOISE:
                    return toDecimalType(dashboardData.getNoise());
                case CHANNEL_PRESSURE:
                    return toDecimalType(dashboardData.getPressure());
                case CHANNEL_PRESS_TREND:
                    return toStringType(dashboardData.getPressureTrend());
                case CHANNEL_ABSOLUTE_PRESSURE:
                    return toDecimalType(dashboardData.getAbsolutePressure());
                case CHANNEL_TIMEUTC:
                    return toDateTimeType(dashboardData.getTimeUtc());
                case CHANNEL_DATE_MIN_TEMP:
                    return toDateTimeType(dashboardData.getDateMinTemp());
                case CHANNEL_DATE_MAX_TEMP:
                    return toDateTimeType(dashboardData.getDateMaxTemp());
                case CHANNEL_HUMIDITY:
                    return toDecimalType(dashboardData.getHumidity());
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
                    Double dewpoint = WeatherUtils.getDewPoint(dashboardData.getTemperature(),
                            dashboardData.getHumidity());
                    return toDecimalType(WeatherUtils.getDewPointDep(dashboardData.getTemperature(), dewpoint));
                case CHANNEL_WIND_UNIT:
                    return new DecimalType(userAdministrative.getWindunit());
                case CHANNEL_PRESSURE_UNIT:
                    return new DecimalType(userAdministrative.getPressureunit());
            }
        }
        return super.getNAThingProperty(channelId);
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
