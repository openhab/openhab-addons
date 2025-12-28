/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_CARBONMONOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_CARBONMONOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.CarbonMonoxideConcentrationMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link CarbonMonoxideConcentrationMeasurementCluster} events and attributes to openHAB
 * channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class CarbonMonoxideConcentrationMeasurementConverter
        extends GenericConverter<CarbonMonoxideConcentrationMeasurementCluster> {

    public CarbonMonoxideConcentrationMeasurementConverter(CarbonMonoxideConcentrationMeasurementCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID,
                        CHANNEL_ID_CARBONMONOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE), "Number:Dimensionless")
                .withType(CHANNEL_CARBONMONOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE).build();
        return Collections.singletonMap(channel, null);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case CarbonMonoxideConcentrationMeasurementCluster.ATTRIBUTE_MEASURED_VALUE:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_CARBONMONOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                            new QuantityType<>(number, Units.PARTS_PER_MILLION));
                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        Float measuredValue = initializingCluster.measuredValue;
        if (measuredValue != null) {
            updateState(CHANNEL_ID_CARBONMONOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                    new QuantityType<>(measuredValue, Units.PARTS_PER_MILLION));
        } else {
            updateState(CHANNEL_ID_CARBONMONOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE, UnDefType.NULL);
        }
    }
}
