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
package org.openhab.binding.solaredge.internal.model;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.solaredge.internal.handler.ChannelProvider;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse.Connection;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse.SiteCurrentPowerFlow;
import org.openhab.binding.solaredge.internal.model.LiveDataResponseMeterless.Overview;

/**
 * transforms the http response into the openhab datamodel (instances of State)
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class LiveDataResponseTransformer extends AbstractDataResponseTransformer {
    private static final Double ZERO_POWER = 0.0;

    private final ChannelProvider channelProvider;

    public LiveDataResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transform(LiveDataResponseMeterless response) {
        Map<Channel, State> result = new HashMap<>(20);
        Overview overview = response.getOverview();

        if (overview != null) {
            if (overview.currentPower != null) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION),
                        overview.currentPower.power, UNIT_W);
            } else {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION), null,
                        UNIT_W);
            }

            if (overview.lastDayData != null) {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_DAY, CHANNEL_ID_PRODUCTION),
                        overview.lastDayData.energy, UNIT_WH);
            } else {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_DAY, CHANNEL_ID_PRODUCTION),
                        null, UNIT_WH);
            }

            if (overview.lastMonthData != null) {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_MONTH, CHANNEL_ID_PRODUCTION),
                        overview.lastMonthData.energy, UNIT_WH);
            } else {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_MONTH, CHANNEL_ID_PRODUCTION),
                        null, UNIT_WH);
            }

            if (overview.lastYearData != null) {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_YEAR, CHANNEL_ID_PRODUCTION),
                        overview.lastYearData.energy, UNIT_WH);
            } else {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_YEAR, CHANNEL_ID_PRODUCTION),
                        null, UNIT_WH);
            }

            // week production is not available
            putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_WEEK, CHANNEL_ID_PRODUCTION), null,
                    UNIT_WH);
        }
        return result;
    }

    public Map<Channel, State> transform(LiveDataResponse response) {
        Map<Channel, State> result = new HashMap<>(20);
        SiteCurrentPowerFlow siteCurrentPowerFlow = response.getSiteCurrentPowerFlow();

        if (siteCurrentPowerFlow != null) {
            if (siteCurrentPowerFlow.pv != null) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION),
                        siteCurrentPowerFlow.pv.currentPower, siteCurrentPowerFlow.unit);
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PV_STATUS),
                        siteCurrentPowerFlow.pv.status);
            }

            if (siteCurrentPowerFlow.load != null) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_CONSUMPTION),
                        siteCurrentPowerFlow.load.currentPower, siteCurrentPowerFlow.unit);
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_LOAD_STATUS),
                        siteCurrentPowerFlow.load.status);
            }

            if (siteCurrentPowerFlow.storage != null) {
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_STATUS),
                        siteCurrentPowerFlow.storage.status);
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CRITICAL),
                        siteCurrentPowerFlow.storage.critical);
                putPercentType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_LEVEL),
                        siteCurrentPowerFlow.storage.chargeLevel);
            }

            if (siteCurrentPowerFlow.grid != null) {
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_GRID_STATUS),
                        siteCurrentPowerFlow.grid.status);
            }

            // init fields with zero
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_IMPORT), ZERO_POWER,
                    siteCurrentPowerFlow.unit);
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_EXPORT), ZERO_POWER,
                    siteCurrentPowerFlow.unit);
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE), ZERO_POWER,
                    siteCurrentPowerFlow.unit);
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_DISCHARGE),
                    ZERO_POWER, siteCurrentPowerFlow.unit);
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                    ZERO_POWER, siteCurrentPowerFlow.unit);

            // determine power flow from connection list
            if (siteCurrentPowerFlow.connections != null) {
                for (Connection con : siteCurrentPowerFlow.connections) {
                    if (con.from.equalsIgnoreCase(LiveDataResponse.GRID)) {
                        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_IMPORT),
                                siteCurrentPowerFlow.grid.currentPower, siteCurrentPowerFlow.unit);
                    } else if (con.to.equalsIgnoreCase(LiveDataResponse.GRID)) {
                        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_EXPORT),
                                siteCurrentPowerFlow.grid.currentPower, siteCurrentPowerFlow.unit);
                    }
                    if (con.from.equalsIgnoreCase(LiveDataResponse.STORAGE)) {
                        putPowerType(result,
                                channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_DISCHARGE),
                                siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                        putPowerType(result,
                                channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                                -1 * siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                    } else if (con.to.equalsIgnoreCase(LiveDataResponse.STORAGE)) {
                        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE),
                                siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                        putPowerType(result,
                                channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                                siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                    }
                }
            }
        }
        return result;
    }
}
