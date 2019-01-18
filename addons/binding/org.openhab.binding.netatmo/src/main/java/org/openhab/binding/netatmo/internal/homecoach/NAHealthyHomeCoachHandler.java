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
package org.openhab.binding.netatmo.internal.homecoach;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAHealthyHomeCoach;
import io.swagger.client.model.NAHealthyHomeCoachDataBody;

/**
 * {@link NAHealthyHomeCoachHandler} is the class used to handle the Health Home Coach device
 *
 * @author Michael Svinth - Initial contribution OH2 version
 *
 */
public class NAHealthyHomeCoachHandler extends NetatmoDeviceHandler<NAHealthyHomeCoach> {

    public NAHealthyHomeCoachHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected NAHealthyHomeCoach updateReadings() {
        NAHealthyHomeCoach result = null;
        NAHealthyHomeCoachDataBody homecoachDataBody = getBridgeHandler().getHomecoachDataBody(getId());
        if (homecoachDataBody != null) {
            result = homecoachDataBody.getDevices().get(0);
        }
        return result;
    }

    @Override
    protected void updateProperties(NAHealthyHomeCoach deviceData) {
        updateProperties(deviceData.getFirmware(), deviceData.getType());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (device != null) {
            NADashboardData dashboardData = device.getDashboardData();
            switch (channelId) {
                case CHANNEL_CO2:
                    return toQuantityType(dashboardData.getCO2(), API_CO2_UNIT);
                case CHANNEL_TEMPERATURE:
                    return toQuantityType(dashboardData.getTemperature(), API_TEMPERATURE_UNIT);
                case CHANNEL_HEALTH_INDEX:
                    return toStringType(toHealthIndexString(dashboardData.getHealthIdx()));
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
                    return toDateTimeType(dashboardData.getTimeUtc());
                case CHANNEL_DATE_MIN_TEMP:
                    return toDateTimeType(dashboardData.getDateMinTemp());
                case CHANNEL_DATE_MAX_TEMP:
                    return toDateTimeType(dashboardData.getDateMaxTemp());
                case CHANNEL_HUMIDITY:
                    return toQuantityType(dashboardData.getHumidity(), API_HUMIDITY_UNIT);
            }
        }
        return super.getNAThingProperty(channelId);
    }

    private String toHealthIndexString(Integer healthIndex) {
        if (healthIndex == null) {
            return null;
        }
        switch (healthIndex) {
            case 0:
                return "healthy";
            case 1:
                return "fine";
            case 2:
                return "fair";
            case 3:
                return "poor";
            case 4:
                return "unhealthy";
            default:
                return healthIndex.toString();
        }
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
