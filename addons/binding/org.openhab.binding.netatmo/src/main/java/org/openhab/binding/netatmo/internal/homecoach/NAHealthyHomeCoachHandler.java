/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.homecoach;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.NADeviceAdapter;
import org.openhab.binding.netatmo.internal.NAHealthyHomeCoachAdapter;
import org.openhab.binding.netatmo.internal.config.NetatmoDeviceConfiguration;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAHealthyHomeCoachDataBody;
import io.swagger.client.model.NAUserAdministrative;

/**
 * {@link NAHealthyHomeCoachHandler} is the class used to handle the Health Home Coach device
 *
 * @author Michael Svinth - Initial contribution OH2 version
 *
 */
public class NAHealthyHomeCoachHandler extends NetatmoDeviceHandler<NetatmoDeviceConfiguration> {

    public NAHealthyHomeCoachHandler(Thing thing) {
        super(thing, NetatmoDeviceConfiguration.class);
    }

    @Override
    protected NADeviceAdapter<?> updateReadings(String equipmentId) {
        NAHealthyHomeCoachDataBody homecoachDataBody = getBridgeHandler().getHomecoachDataBody(equipmentId);
        if (homecoachDataBody != null) {
            return new NAHealthyHomeCoachAdapter(homecoachDataBody);
        } else {
            return null;
        }
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        NAHealthyHomeCoachAdapter adapter = (NAHealthyHomeCoachAdapter) device;
        NADashboardData dashboardData = adapter.getDashboardData();
        NAUserAdministrative userAdministrative = device.getUserAdministrative();
        switch (channelId) {
            case CHANNEL_CO2:
                return ChannelTypeUtils.toDecimalType(dashboardData.getCO2());
            case CHANNEL_TEMPERATURE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getTemperature());
            case CHANNEL_HEALTH_INDEX:
                return ChannelTypeUtils.toStringType(toHealthIndexString(dashboardData.getHealthIdx()));
            case CHANNEL_MIN_TEMP:
                return ChannelTypeUtils.toDecimalType(dashboardData.getMinTemp());
            case CHANNEL_MAX_TEMP:
                return ChannelTypeUtils.toDecimalType(dashboardData.getMaxTemp());
            case CHANNEL_TEMP_TREND:
                return ChannelTypeUtils.toStringType(dashboardData.getTempTrend());
            case CHANNEL_NOISE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getNoise());
            case CHANNEL_PRESSURE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getPressure());
            case CHANNEL_PRESS_TREND:
                return ChannelTypeUtils.toStringType(dashboardData.getPressureTrend());
            case CHANNEL_ABSOLUTE_PRESSURE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getAbsolutePressure());
            case CHANNEL_TIMEUTC:
                return ChannelTypeUtils.toDateTimeType(dashboardData.getTimeUtc());
            case CHANNEL_DATE_MIN_TEMP:
                return ChannelTypeUtils.toDateTimeType(dashboardData.getDateMinTemp());
            case CHANNEL_DATE_MAX_TEMP:
                return ChannelTypeUtils.toDateTimeType(dashboardData.getDateMaxTemp());
            case CHANNEL_HUMIDITY:
                return ChannelTypeUtils.toDecimalType(dashboardData.getHumidity());
            case CHANNEL_WIND_UNIT:
                return new DecimalType(userAdministrative.getWindunit());
            case CHANNEL_PRESSURE_UNIT:
                return new DecimalType(userAdministrative.getPressureunit());

            default:
                return super.getNAThingProperty(channelId);
        }
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
}
