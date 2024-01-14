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
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePublicApi.EnergyDetails;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePublicApi.MeterTelemetries;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePublicApi.MeterTelemetry;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * transforms the http response into the openhab datamodel (instances of State)
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AggregateDataResponseTransformerPublicApi extends AbstractDataResponseTransformer {
    private final Logger logger = LoggerFactory.getLogger(AggregateDataResponseTransformerPublicApi.class);

    private static final String METER_TYPE_PRODUCTION = "Production";
    private static final String METER_TYPE_CONSUMPTION = "Consumption";
    private static final String METER_TYPE_SELFCONSUMPTION = "SelfConsumption";
    private static final String METER_TYPE_IMPORT = "Purchased";
    private static final String METER_TYPE_EXPORT = "FeedIn";

    private final ChannelProvider channelProvider;

    public AggregateDataResponseTransformerPublicApi(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transform(AggregateDataResponsePublicApi response, AggregatePeriod period) {
        Map<Channel, State> result = new HashMap<>(20);
        EnergyDetails energyDetails = response.getEnergyDetails();

        if (energyDetails != null) {
            AggregatePeriod timeUnit = energyDetails.timeUnit;
            String unit = energyDetails.unit;
            List<MeterTelemetries> meters = energyDetails.meters;
            if (timeUnit != null && unit != null && meters != null) {
                for (MeterTelemetries meter : meters) {
                    String type = meter.type;
                    if (type != null) {
                        if (type.equals(METER_TYPE_PRODUCTION)) {
                            fillAggregateData(timeUnit, unit, meter, CHANNEL_ID_PRODUCTION, result);
                        } else if (type.equals(METER_TYPE_CONSUMPTION)) {
                            fillAggregateData(timeUnit, unit, meter, CHANNEL_ID_CONSUMPTION, result);
                        } else if (type.equals(METER_TYPE_SELFCONSUMPTION)) {
                            fillAggregateData(timeUnit, unit, meter, CHANNEL_ID_SELF_CONSUMPTION_FOR_CONSUMPTION,
                                    result);
                        } else if (type.equals(METER_TYPE_IMPORT)) {
                            fillAggregateData(timeUnit, unit, meter, CHANNEL_ID_IMPORT, result);
                        } else if (type.equals(METER_TYPE_EXPORT)) {
                            fillAggregateData(timeUnit, unit, meter, CHANNEL_ID_EXPORT, result);
                        }
                    }
                }
                fillSelfConsumptionCoverage(timeUnit, result);
            }
        }
        return result;
    }

    /**
     * copies production data to the result map
     *
     * @param meter meter raw data
     * @param valueMap target structure
     */
    private final void fillAggregateData(AggregatePeriod period, String unit, MeterTelemetries meter, String channelId,
            Map<Channel, State> valueMap) {
        String group = convertPeriodToGroup(period);
        List<MeterTelemetry> values = meter.values;

        if (values != null) {
            switch (period) {
                case WEEK:
                    if (values.size() == 1) {
                        putEnergyType(valueMap, channelProvider.getChannel(group, channelId), unit, values.get(0));
                    } else if (values.size() == 2) {
                        putEnergyType(valueMap, channelProvider.getChannel(group, channelId), unit, values.get(0),
                                values.get(1));
                    } else {
                        logger.warn("Response for weekly data has unexpected format, expected 2 entries got {}",
                                values.size());
                    }
                    break;
                case DAY:
                case MONTH:
                case YEAR:
                    putEnergyType(valueMap, channelProvider.getChannel(group, channelId), unit, values.get(0));
                    break;
            }
        }
    }

    /**
     * calculates the self consumption coverage
     *
     * @param valueMap target structure
     */
    private final void fillSelfConsumptionCoverage(AggregatePeriod period, Map<Channel, State> valueMap) {
        State selfConsumption = null;
        State consumption = null;

        String group = convertPeriodToGroup(period);

        selfConsumption = valueMap.get(channelProvider.getChannel(group, CHANNEL_ID_SELF_CONSUMPTION_FOR_CONSUMPTION));
        consumption = valueMap.get(channelProvider.getChannel(group, CHANNEL_ID_CONSUMPTION));
        putPercentType(valueMap, channelProvider.getChannel(group, CHANNEL_ID_SELF_CONSUMPTION_COVERAGE),
                selfConsumption, consumption);
    }
}
