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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_TEMPERATUREMEASURMENT_MEASUREDVALUE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_TEMPERATUREMEASURMENT_MEASUREDVALUE;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.TemperatureMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;

/**
 * A converter for translating {@link TemperatureMeasurementCluster} events and attributes to openHAB channels and back
 * again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TemperatureMeasurementConverter extends GenericConverter<TemperatureMeasurementCluster> {

    public TemperatureMeasurementConverter(TemperatureMeasurementCluster cluster, MatterBaseThingHandler handler,
            int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_TEMPERATUREMEASURMENT_MEASUREDVALUE),
                        "Number:Temperature")
                .withType(CHANNEL_TEMPERATUREMEASURMENT_MEASUREDVALUE).build();
        return Collections.singletonMap(channel, null);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case TemperatureMeasurementCluster.ATTRIBUTE_MEASURED_VALUE:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_TEMPERATUREMEASURMENT_MEASUREDVALUE,
                            ValueUtils.valueToTemperature(number.intValue()));
                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_TEMPERATUREMEASURMENT_MEASUREDVALUE,
                ValueUtils.valueToTemperature(initializingCluster.measuredValue));
    }
}
