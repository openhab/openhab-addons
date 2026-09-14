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

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solaredge.internal.handler.ChannelProvider;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi.ConsumptionDistribution;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi.Summary;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

/**
 * transforms the energy dashboard response into the openHAB datamodel (instances of State)
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AggregateDataResponseTransformerPrivateApi extends AbstractDataResponseTransformer {
    private final ChannelProvider channelProvider;

    public AggregateDataResponseTransformerPrivateApi(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transform(AggregateDataResponsePrivateApi response, AggregatePeriod period) {
        Map<Channel, State> result = new HashMap<>(20);
        Summary summary = response.getSummary();

        String group = convertPeriodToGroup(period);

        if (summary != null) {
            putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_PRODUCTION), summary.production,
                    UNIT_WH);

            putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_CONSUMPTION), summary.consumption,
                    UNIT_WH);

            putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_IMPORT), summary.imported, UNIT_WH);

            putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_EXPORT), summary.export, UNIT_WH);

            ConsumptionDistribution consumptionDistribution = summary.consumptionDistribution;
            if (consumptionDistribution != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_SELF_CONSUMPTION_FOR_CONSUMPTION),
                        consumptionDistribution.consumptionFromSolar, UNIT_WH);

                putPercentType(result, channelProvider.getChannel(group, CHANNEL_ID_SELF_CONSUMPTION_COVERAGE),
                        consumptionDistribution.consumptionFromSolarPercentage);

                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_BATTERY_SELF_CONSUMPTION),
                        consumptionDistribution.consumptionFromBattery, UNIT_WH);
            }
        }

        return result;
    }
}
