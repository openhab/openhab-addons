/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAStationModule;

/**
 * {@link NAModule3Handler} is the class used to handle the Rain Gauge
 * capable of measuring precipitation
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Rob Nielsen - Added day, week, and month measurements to the weather station and modules
 *
 */
public class NAModule3Handler extends NetatmoModuleHandler<NAStationModule> {
    private Map<String, Float> channelMeasurements = new ConcurrentHashMap<>();

    public NAModule3Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateProperties(NAStationModule moduleData) {
        updateProperties(moduleData.getFirmware(), moduleData.getType());
    }

    @Override
    public void updateMeasurements() {
        List<@NonNull String> types = Arrays.asList(SUM_RAIN);

        if (isLinked(CHANNEL_SUM_RAIN_THIS_WEEK)) {
            getMeasurements(getBridgeHandler(), getParentId(), getId(), ONE_WEEK, types,
                    Arrays.asList(CHANNEL_SUM_RAIN_THIS_WEEK), channelMeasurements);
        }

        if (isLinked(CHANNEL_SUM_RAIN_THIS_MONTH)) {
            getMeasurements(getBridgeHandler(), getParentId(), getId(), ONE_MONTH, types,
                    Arrays.asList(CHANNEL_SUM_RAIN_THIS_MONTH), channelMeasurements);
        }
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            NADashboardData dashboardData = module.getDashboardData();
            if (dashboardData != null) {
                switch (channelId) {
                    case CHANNEL_RAIN:
                        return toQuantityType(dashboardData.getRain(), API_RAIN_UNIT);
                    case CHANNEL_SUM_RAIN1:
                        return toQuantityType(dashboardData.getSumRain1(), API_RAIN_UNIT);
                    case CHANNEL_SUM_RAIN24:
                        return toQuantityType(dashboardData.getSumRain24(), API_RAIN_UNIT);
                    case CHANNEL_TIMEUTC:
                        return toDateTimeType(dashboardData.getTimeUtc());
                }
            }
        }

        switch (channelId) {
            case CHANNEL_SUM_RAIN_THIS_WEEK:
            case CHANNEL_SUM_RAIN_THIS_MONTH:
                return toQuantityType(channelMeasurements.get(channelId), API_RAIN_UNIT);
        }

        return super.getNAThingProperty(channelId);
    }
}
