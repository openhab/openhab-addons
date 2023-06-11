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
package org.openhab.binding.solaredge.internal.model;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solaredge.internal.handler.ChannelProvider;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi.UtilizationMeasures;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi.Value;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi.ValueAndPercent;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

/**
 * transforms the http response into the openhab datamodel (instances of State)
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
        UtilizationMeasures utilizationMeasures = response.getUtilizationMeasures();

        String group = convertPeriodToGroup(period);

        if (utilizationMeasures != null) {
            Value production = utilizationMeasures.production;
            if (production != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_PRODUCTION), production);
            }

            Value consumption = utilizationMeasures.consumption;
            if (consumption != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_CONSUMPTION), consumption);
            }

            ValueAndPercent selfConsumptionForConsumption = utilizationMeasures.selfConsumptionForConsumption;
            if (selfConsumptionForConsumption != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_SELF_CONSUMPTION_FOR_CONSUMPTION),
                        selfConsumptionForConsumption);
                putPercentType(result, channelProvider.getChannel(group, CHANNEL_ID_SELF_CONSUMPTION_COVERAGE),
                        selfConsumptionForConsumption);
            }

            Value batterySelfConsumption = utilizationMeasures.batterySelfConsumption;
            if (batterySelfConsumption != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_BATTERY_SELF_CONSUMPTION),
                        batterySelfConsumption);
            }

            Value imported = utilizationMeasures.imported;
            if (imported != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_IMPORT), imported);
            }

            Value export = utilizationMeasures.export;
            if (export != null) {
                putEnergyType(result, channelProvider.getChannel(group, CHANNEL_ID_EXPORT), export);
            }
        }
        return result;
    }
}
