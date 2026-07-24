/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_GROUP_LIVE;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_BATTERY_CHARGE;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_BATTERY_CHARGE_DISCHARGE;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_BATTERY_CRITICAL;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_BATTERY_DISCHARGE;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_BATTERY_LEVEL;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_BATTERY_STATUS;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_CONSUMPTION;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_EXPORT;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_GRID_STATUS;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_IMPORT;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_LOAD_STATUS;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_PRODUCTION;
import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.CHANNEL_ID_PV_STATUS;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solaredge.internal.handler.ChannelProvider;
import org.openhab.binding.solaredge.internal.model.LiveDataResponsePrivateApi.Consumption;
import org.openhab.binding.solaredge.internal.model.LiveDataResponsePrivateApi.DcStorage;
import org.openhab.binding.solaredge.internal.model.LiveDataResponsePrivateApi.Grid;
import org.openhab.binding.solaredge.internal.model.LiveDataResponsePrivateApi.SolarProduction;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

/**
 * transforms the private API http response into the openhab datamodel
 * (instances of State)
 *
 * @author Georg Kunz - Initial contribution
 * @author Ronny Grun - Add battery handling
 */
@NonNullByDefault
public class LiveDataResponseTransformerPrivateApi extends AbstractDataResponseTransformer {
    private static final Double ZERO_POWER = 0.0;

    private final ChannelProvider channelProvider;

    public LiveDataResponseTransformerPrivateApi(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transform(LiveDataResponsePrivateApi response) {
        Map<Channel, State> result = new HashMap<>(20);

        SolarProduction solarProduction = response.solarProduction;
        if (solarProduction != null) {
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PRODUCTION),
                    solarProduction.currentPower, "kW");
            putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_PV_STATUS),
                    Boolean.TRUE.equals(solarProduction.isActive) ? "active" : "idle");
        }

        Consumption consumption = response.consumption;
        if (consumption != null) {
            putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_CONSUMPTION),
                    consumption.currentPower, "kW");
            putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_LOAD_STATUS),
                    Boolean.TRUE.equals(consumption.isActive) ? "active" : "idle");
        }

        // init fields with zero
        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_IMPORT), ZERO_POWER, "kW");
        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_EXPORT), ZERO_POWER, "kW");
        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE), ZERO_POWER,
                "kW");
        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_DISCHARGE), ZERO_POWER,
                "kW");
        putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                ZERO_POWER, "kW");

        DcStorage dcStorage = response.dcStorage;
        if (dcStorage != null) {
            putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_STATUS),
                    dcStorage.status);
            putPercentType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_LEVEL),
                    dcStorage.chargeLevel);

            // battery_critical does not exist in new PrivateApi
            putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CRITICAL),
                    Boolean.TRUE.equals(dcStorage.isActive) ? "active" : "idle");

            Double currentPower = dcStorage.currentPower;
            currentPower = currentPower != null ? currentPower : 0;
            if ("charging".equalsIgnoreCase(dcStorage.status)) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE),
                        currentPower, "kW");
                putPowerType(result,
                        channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                        currentPower, "kW");
            } else if ("discharging".equalsIgnoreCase(dcStorage.status)) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_DISCHARGE),
                        currentPower, "kW");
                putPowerType(result,
                        channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CHARGE_DISCHARGE),
                        -1 * currentPower, "kW");
            }
        }

        Grid grid = response.grid;
        if (grid != null) {
            putStringType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_GRID_STATUS),
                    Boolean.TRUE.equals(grid.isActive) ? "active" : "idle");
            if ("import".equalsIgnoreCase(grid.status)) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_IMPORT),
                        grid.currentPower, "kW");
            } else if ("export".equalsIgnoreCase(grid.status)) {
                putPowerType(result, channelProvider.getChannel(CHANNEL_GROUP_LIVE, CHANNEL_ID_EXPORT),
                        grid.currentPower, "kW");
            }
        }
        return result;
    }
}
