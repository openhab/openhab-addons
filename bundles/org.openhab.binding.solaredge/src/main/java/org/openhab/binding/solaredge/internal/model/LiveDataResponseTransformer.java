/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solaredge.internal.handler.ChannelProvider;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse.BatteryValue;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse.Connection;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse.SiteCurrentPowerFlow;
import org.openhab.binding.solaredge.internal.model.LiveDataResponse.Value;
import org.openhab.binding.solaredge.internal.model.LiveDataResponseMeterless.Energy;
import org.openhab.binding.solaredge.internal.model.LiveDataResponseMeterless.Overview;
import org.openhab.binding.solaredge.internal.model.LiveDataResponseMeterless.Power;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

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
            Power currentPower = overview.currentPower;
            if (currentPower != null) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION),
                        currentPower.power, UNIT_W);
            } else {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION), null,
                        UNIT_W);
            }

            Energy lastDayData = overview.lastDayData;
            if (lastDayData != null) {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_DAY, CHANNEL_ID_PRODUCTION),
                        lastDayData.energy, UNIT_WH);
            } else {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_DAY, CHANNEL_ID_PRODUCTION),
                        null, UNIT_WH);
            }

            Energy lastMonthData = overview.lastMonthData;
            if (lastMonthData != null) {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_MONTH, CHANNEL_ID_PRODUCTION),
                        lastMonthData.energy, UNIT_WH);
            } else {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_MONTH, CHANNEL_ID_PRODUCTION),
                        null, UNIT_WH);
            }

            Energy lastYearData = overview.lastYearData;
            if (lastYearData != null) {
                putEnergyType(result, channelProvider.getChannel(CHANNEL_GROUP_AGGREGATE_YEAR, CHANNEL_ID_PRODUCTION),
                        lastYearData.energy, UNIT_WH);
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
            Value pv = siteCurrentPowerFlow.pv;
            Value load = siteCurrentPowerFlow.load;
            BatteryValue storage = siteCurrentPowerFlow.storage;
            Value grid = siteCurrentPowerFlow.grid;

            if (pv != null) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION),
                        pv.currentPower, siteCurrentPowerFlow.unit);
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PV_STATUS), pv.status);
            }

            if (load != null) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_CONSUMPTION),
                        load.currentPower, siteCurrentPowerFlow.unit);
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_LOAD_STATUS),
                        load.status);
            }

            if (storage != null) {
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_STATUS),
                        storage.status);
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CRITICAL),
                        storage.critical);
                putPercentType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_LEVEL),
                        storage.chargeLevel);
            }

            if (grid != null) {
                putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_GRID_STATUS),
                        grid.status);
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
            List<Connection> connections = siteCurrentPowerFlow.connections;
            if (connections != null) {
                for (Connection con : connections) {
                    String conFrom = con.from;
                    String conTo = con.to;
                    if (grid != null) {
                        if (conFrom != null && conFrom.equalsIgnoreCase(LiveDataResponse.GRID)) {
                            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_IMPORT),
                                    grid.currentPower, siteCurrentPowerFlow.unit);
                        } else if (conTo != null && conTo.equalsIgnoreCase(LiveDataResponse.GRID)) {
                            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_EXPORT),
                                    grid.currentPower, siteCurrentPowerFlow.unit);
                        }
                    }

                    if (storage != null) {
                        Double currentPower = storage.currentPower;
                        currentPower = currentPower != null ? currentPower : 0;
                        if (conFrom != null && conFrom.equalsIgnoreCase(LiveDataResponse.STORAGE)) {
                            putPowerType(result,
                                    channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_DISCHARGE),
                                    currentPower, siteCurrentPowerFlow.unit);
                            putPowerType(result,
                                    channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                                    -1 * currentPower, siteCurrentPowerFlow.unit);
                        } else if (conTo != null && conTo.equalsIgnoreCase(LiveDataResponse.STORAGE)) {
                            putPowerType(result,
                                    channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE),
                                    currentPower, siteCurrentPowerFlow.unit);
                            putPowerType(result,
                                    channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                                    currentPower, siteCurrentPowerFlow.unit);
                        }
                    }
                }
            }
        }
        return result;
    }
}
