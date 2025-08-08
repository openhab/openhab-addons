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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_AIRQUALITY_AIRQUALITY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_AIRQUALITY_AIRQUALITY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.AirQualityCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link AirQualityCluster} events and attributes to openHAB channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AirQualityConverter extends GenericConverter<AirQualityCluster> {

    public AirQualityConverter(AirQualityCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_AIRQUALITY_AIRQUALITY), CoreItemFactory.NUMBER)
                .withType(CHANNEL_AIRQUALITY_AIRQUALITY).withLabel(formatLabel("Air Quality")).build();

        List<StateOption> options = new ArrayList<>();
        for (AirQualityCluster.AirQualityEnum e : AirQualityCluster.AirQualityEnum.values()) {
            options.add(new StateOption(e.value.toString(), e.label));
        }

        StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%d")
                .withOptions(options).build().toStateDescription();

        return Collections.singletonMap(channel, stateDescription);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case AirQualityCluster.ATTRIBUTE_AIR_QUALITY:
                if (message.value instanceof AirQualityCluster.AirQualityEnum aqEnum) {
                    updateState(CHANNEL_ID_AIRQUALITY_AIRQUALITY, new DecimalType(aqEnum.value));
                }
                break;
            default:
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_AIRQUALITY_AIRQUALITY,
                initializingCluster.airQuality != null ? new DecimalType(initializingCluster.airQuality.value)
                        : UnDefType.NULL);
    }
}
