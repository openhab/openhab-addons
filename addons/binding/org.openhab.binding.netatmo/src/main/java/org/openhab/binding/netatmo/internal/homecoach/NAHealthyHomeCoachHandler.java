/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.homecoach;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;

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
            userAdministrative = homecoachDataBody.getUser().getAdministrative();
            result = homecoachDataBody.getDevices().get(0);
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
                case CHANNEL_HEALTH_INDEX:
                    return toStringType(toHealthIndexString(dashboardData.getHealthIdx()));
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
                case CHANNEL_WIND_UNIT:
                    return new DecimalType(userAdministrative.getWindunit());
                case CHANNEL_PRESSURE_UNIT:
                    return new DecimalType(userAdministrative.getPressureunit());
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
