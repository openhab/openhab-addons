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

import static org.openhab.binding.netatmo.internal.APIUtils.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.WeatherUtils;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAMain;

/**
 * {@link NAMainHandler} is the base class for all current Netatmo
 * weather station equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Rob Nielsen - Added day, week, and month measurements to the weather station and modules
 *
 */
@NonNullByDefault
public class NAMainHandler extends NetatmoDeviceHandler<NAMain> {
    private Map<String, Float> channelMeasurements = new ConcurrentHashMap<>();

    public NAMainHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected Optional<NAMain> updateReadings() {
        Optional<NAMain> result = getBridgeHandler().flatMap(handler -> handler.getStationsDataBody(getId()))
                .map(dataBody -> nonNullStream(dataBody.getDevices())
                        .filter(device -> device.getId().equalsIgnoreCase(getId())).findFirst().orElse(null));
        result.ifPresent(device -> {
            nonNullList(device.getModules()).forEach(child -> childs.put(child.getId(), child));
        });

        updateMeasurements();

        childs.keySet().forEach((childId) -> {
            findNAThing(childId).map(NetatmoModuleHandler.class::cast).ifPresent(naChildModule -> {
                naChildModule.updateMeasurements();
            });
        });

        return result;
    }

    @Override
    protected void updateProperties(NAMain deviceData) {
        updateProperties(deviceData.getFirmware(), deviceData.getType());
    }

    @Override
    public void updateMeasurements() {
        updateDayMeasurements();
        updateWeekMeasurements();
        updateMonthMeasurements();
    }

    private void updateDayMeasurements() {
        List<String> channels = new ArrayList<>();
        List<String> types = new ArrayList<>();
        addMeasurement(channels, types, CHANNEL_MIN_CO2, MIN_CO2);
        addMeasurement(channels, types, CHANNEL_MAX_CO2, MAX_CO2);
        addMeasurement(channels, types, CHANNEL_MIN_HUMIDITY, MIN_HUM);
        addMeasurement(channels, types, CHANNEL_MAX_HUMIDITY, MAX_HUM);
        addMeasurement(channels, types, CHANNEL_MIN_NOISE, MIN_NOISE);
        addMeasurement(channels, types, CHANNEL_MAX_NOISE, MAX_NOISE);
        addMeasurement(channels, types, CHANNEL_MIN_PRESSURE, MIN_PRESSURE);
        addMeasurement(channels, types, CHANNEL_MAX_PRESSURE, MAX_PRESSURE);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_CO2, DATE_MIN_CO2);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_CO2, DATE_MAX_CO2);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_HUMIDITY, DATE_MIN_HUM);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_HUMIDITY, DATE_MAX_HUM);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_NOISE, DATE_MIN_NOISE);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_NOISE, DATE_MAX_NOISE);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_PRESSURE, DATE_MIN_PRESSURE);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_PRESSURE, DATE_MAX_PRESSURE);
        if (!channels.isEmpty()) {
            getMeasurements(getId(), null, ONE_DAY, types, channels, channelMeasurements);
        }
    }

    private void updateWeekMeasurements() {
        List<String> channels = new ArrayList<>();
        List<String> types = new ArrayList<>();
        addMeasurement(channels, types, CHANNEL_MIN_CO2_THIS_WEEK, MIN_CO2);
        addMeasurement(channels, types, CHANNEL_MAX_CO2_THIS_WEEK, MAX_CO2);
        addMeasurement(channels, types, CHANNEL_MIN_HUMIDITY_THIS_WEEK, MIN_HUM);
        addMeasurement(channels, types, CHANNEL_MAX_HUMIDITY_THIS_WEEK, MAX_HUM);
        addMeasurement(channels, types, CHANNEL_MIN_NOISE_THIS_WEEK, MIN_NOISE);
        addMeasurement(channels, types, CHANNEL_MAX_NOISE_THIS_WEEK, MAX_NOISE);
        addMeasurement(channels, types, CHANNEL_MIN_PRESSURE_THIS_WEEK, MIN_PRESSURE);
        addMeasurement(channels, types, CHANNEL_MAX_PRESSURE_THIS_WEEK, MAX_PRESSURE);
        addMeasurement(channels, types, CHANNEL_MIN_TEMP_THIS_WEEK, MIN_TEMP);
        addMeasurement(channels, types, CHANNEL_MAX_TEMP_THIS_WEEK, MAX_TEMP);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_CO2_THIS_WEEK, DATE_MIN_CO2);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_CO2_THIS_WEEK, DATE_MAX_CO2);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_HUMIDITY_THIS_WEEK, DATE_MIN_HUM);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_HUMIDITY_THIS_WEEK, DATE_MAX_HUM);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_NOISE_THIS_WEEK, DATE_MIN_NOISE);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_NOISE_THIS_WEEK, DATE_MAX_NOISE);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_PRESSURE_THIS_WEEK, DATE_MIN_PRESSURE);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_PRESSURE_THIS_WEEK, DATE_MAX_PRESSURE);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_TEMP_THIS_WEEK, DATE_MIN_TEMP);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_TEMP_THIS_WEEK, DATE_MAX_TEMP);
        if (!channels.isEmpty()) {
            getMeasurements(getId(), null, ONE_WEEK, types, channels, channelMeasurements);
        }
    }

    private void updateMonthMeasurements() {
        List<String> channels = new ArrayList<>();
        List<String> types = new ArrayList<>();
        addMeasurement(channels, types, CHANNEL_MIN_CO2_THIS_MONTH, MIN_CO2);
        addMeasurement(channels, types, CHANNEL_MAX_CO2_THIS_MONTH, MAX_CO2);
        addMeasurement(channels, types, CHANNEL_MIN_HUMIDITY_THIS_MONTH, MIN_HUM);
        addMeasurement(channels, types, CHANNEL_MAX_HUMIDITY_THIS_MONTH, MAX_HUM);
        addMeasurement(channels, types, CHANNEL_MIN_NOISE_THIS_MONTH, MIN_NOISE);
        addMeasurement(channels, types, CHANNEL_MAX_NOISE_THIS_MONTH, MAX_NOISE);
        addMeasurement(channels, types, CHANNEL_MIN_PRESSURE_THIS_MONTH, MIN_PRESSURE);
        addMeasurement(channels, types, CHANNEL_MAX_PRESSURE_THIS_MONTH, MAX_PRESSURE);
        addMeasurement(channels, types, CHANNEL_MIN_TEMP_THIS_MONTH, MIN_TEMP);
        addMeasurement(channels, types, CHANNEL_MAX_TEMP_THIS_MONTH, MAX_TEMP);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_CO2_THIS_MONTH, DATE_MIN_CO2);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_CO2_THIS_MONTH, DATE_MAX_CO2);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_HUMIDITY_THIS_MONTH, DATE_MIN_HUM);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_HUMIDITY_THIS_MONTH, DATE_MAX_HUM);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_NOISE_THIS_MONTH, DATE_MIN_NOISE);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_NOISE_THIS_MONTH, DATE_MAX_NOISE);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_PRESSURE_THIS_MONTH, DATE_MIN_PRESSURE);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_PRESSURE_THIS_MONTH, DATE_MAX_PRESSURE);
        addMeasurement(channels, types, CHANNEL_DATE_MIN_TEMP_THIS_MONTH, DATE_MIN_TEMP);
        addMeasurement(channels, types, CHANNEL_DATE_MAX_TEMP_THIS_MONTH, DATE_MAX_TEMP);
        if (!channels.isEmpty()) {
            getMeasurements(getId(), null, ONE_MONTH, types, channels, channelMeasurements);
        }
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        NADashboardData dashboardData = getDevice().map(d -> d.getDashboardData()).orElse(null);
        if (dashboardData != null) {
            switch (channelId) {
                case CHANNEL_CO2:
                    return toQuantityType(dashboardData.getCo2(), API_CO2_UNIT);
                case CHANNEL_TEMPERATURE:
                    return toQuantityType(dashboardData.getTemperature(), API_TEMPERATURE_UNIT);
                case CHANNEL_MIN_TEMP:
                    return toQuantityType(dashboardData.getMinTemp(), API_TEMPERATURE_UNIT);
                case CHANNEL_MAX_TEMP:
                    return toQuantityType(dashboardData.getMaxTemp(), API_TEMPERATURE_UNIT);
                case CHANNEL_TEMP_TREND:
                    return toStringType(dashboardData.getTempTrend());
                case CHANNEL_NOISE:
                    return toQuantityType(dashboardData.getNoise(), API_NOISE_UNIT);
                case CHANNEL_PRESSURE:
                    return toQuantityType(dashboardData.getPressure(), API_PRESSURE_UNIT);
                case CHANNEL_PRESS_TREND:
                    return toStringType(dashboardData.getPressureTrend());
                case CHANNEL_ABSOLUTE_PRESSURE:
                    return toQuantityType(dashboardData.getAbsolutePressure(), API_PRESSURE_UNIT);
                case CHANNEL_TIMEUTC:
                    return toDateTimeType(dashboardData.getTimeUtc(), timeZoneProvider.getTimeZone());
                case CHANNEL_DATE_MIN_TEMP:
                    return toDateTimeType(dashboardData.getDateMinTemp(), timeZoneProvider.getTimeZone());
                case CHANNEL_DATE_MAX_TEMP:
                    return toDateTimeType(dashboardData.getDateMaxTemp(), timeZoneProvider.getTimeZone());
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
                    Double dewPoint = WeatherUtils.getDewPoint(dashboardData.getTemperature(),
                            dashboardData.getHumidity());
                    return toQuantityType(WeatherUtils.getDewPointDep(dashboardData.getTemperature(), dewPoint),
                            API_TEMPERATURE_UNIT);
            }
        }

        switch (channelId) {
            case CHANNEL_MIN_CO2:
            case CHANNEL_MIN_CO2_THIS_WEEK:
            case CHANNEL_MIN_CO2_THIS_MONTH:
            case CHANNEL_MAX_CO2:
            case CHANNEL_MAX_CO2_THIS_WEEK:
            case CHANNEL_MAX_CO2_THIS_MONTH:
                return toQuantityType(channelMeasurements.get(channelId), API_CO2_UNIT);
            case CHANNEL_MIN_HUMIDITY:
            case CHANNEL_MIN_HUMIDITY_THIS_WEEK:
            case CHANNEL_MIN_HUMIDITY_THIS_MONTH:
            case CHANNEL_MAX_HUMIDITY:
            case CHANNEL_MAX_HUMIDITY_THIS_WEEK:
            case CHANNEL_MAX_HUMIDITY_THIS_MONTH:
                return toQuantityType(channelMeasurements.get(channelId), API_HUMIDITY_UNIT);
            case CHANNEL_MIN_NOISE:
            case CHANNEL_MIN_NOISE_THIS_WEEK:
            case CHANNEL_MIN_NOISE_THIS_MONTH:
            case CHANNEL_MAX_NOISE:
            case CHANNEL_MAX_NOISE_THIS_WEEK:
            case CHANNEL_MAX_NOISE_THIS_MONTH:
                return toQuantityType(channelMeasurements.get(channelId), API_NOISE_UNIT);
            case CHANNEL_MIN_PRESSURE:
            case CHANNEL_MIN_PRESSURE_THIS_WEEK:
            case CHANNEL_MIN_PRESSURE_THIS_MONTH:
            case CHANNEL_MAX_PRESSURE:
            case CHANNEL_MAX_PRESSURE_THIS_WEEK:
            case CHANNEL_MAX_PRESSURE_THIS_MONTH:
                return toQuantityType(channelMeasurements.get(channelId), API_PRESSURE_UNIT);
            case CHANNEL_MIN_TEMP_THIS_WEEK:
            case CHANNEL_MIN_TEMP_THIS_MONTH:
            case CHANNEL_MAX_TEMP_THIS_WEEK:
            case CHANNEL_MAX_TEMP_THIS_MONTH:
                return toQuantityType(channelMeasurements.get(channelId), API_TEMPERATURE_UNIT);
            case CHANNEL_DATE_MIN_CO2:
            case CHANNEL_DATE_MIN_CO2_THIS_WEEK:
            case CHANNEL_DATE_MIN_CO2_THIS_MONTH:
            case CHANNEL_DATE_MAX_CO2:
            case CHANNEL_DATE_MAX_CO2_THIS_WEEK:
            case CHANNEL_DATE_MAX_CO2_THIS_MONTH:
            case CHANNEL_DATE_MIN_NOISE:
            case CHANNEL_DATE_MIN_NOISE_THIS_WEEK:
            case CHANNEL_DATE_MIN_NOISE_THIS_MONTH:
            case CHANNEL_DATE_MAX_NOISE:
            case CHANNEL_DATE_MAX_NOISE_THIS_WEEK:
            case CHANNEL_DATE_MAX_NOISE_THIS_MONTH:
            case CHANNEL_DATE_MIN_HUMIDITY:
            case CHANNEL_DATE_MIN_HUMIDITY_THIS_WEEK:
            case CHANNEL_DATE_MIN_HUMIDITY_THIS_MONTH:
            case CHANNEL_DATE_MAX_HUMIDITY:
            case CHANNEL_DATE_MAX_HUMIDITY_THIS_WEEK:
            case CHANNEL_DATE_MAX_HUMIDITY_THIS_MONTH:
            case CHANNEL_DATE_MIN_PRESSURE:
            case CHANNEL_DATE_MIN_PRESSURE_THIS_WEEK:
            case CHANNEL_DATE_MIN_PRESSURE_THIS_MONTH:
            case CHANNEL_DATE_MAX_PRESSURE:
            case CHANNEL_DATE_MAX_PRESSURE_THIS_WEEK:
            case CHANNEL_DATE_MAX_PRESSURE_THIS_MONTH:
            case CHANNEL_DATE_MIN_TEMP_THIS_WEEK:
            case CHANNEL_DATE_MIN_TEMP_THIS_MONTH:
            case CHANNEL_DATE_MAX_TEMP_THIS_WEEK:
            case CHANNEL_DATE_MAX_TEMP_THIS_MONTH:
                return toDateTimeType(channelMeasurements.get(channelId), timeZoneProvider.getTimeZone());
        }

        return super.getNAThingProperty(channelId);
    }

    @Override
    protected Optional<Integer> getDataTimestamp() {
        return getDevice().map(d -> d.getLastStatusStore());
    }

    @Override
    protected boolean isReachable() {
        boolean result = false;
        Optional<NAMain> device = getDevice();
        if (device.isPresent()) {
            Boolean reachable = device.get().isReachable();
            result = reachable != null ? reachable.booleanValue() : false;
        }
        return result;
    }
}
