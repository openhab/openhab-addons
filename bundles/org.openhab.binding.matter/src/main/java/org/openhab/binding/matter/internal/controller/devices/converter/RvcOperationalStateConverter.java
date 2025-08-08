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

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OperationalStateCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcOperationalStateCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * Converter for {@link RvcOperationalStateCluster}
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class RvcOperationalStateConverter extends GenericConverter<RvcOperationalStateCluster> {

    public RvcOperationalStateConverter(RvcOperationalStateCluster cluster, MatterBaseThingHandler handler,
            int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel stateChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_RVCOPERATIONALSTATE_STATE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_RVCOPERATIONALSTATE_STATE).build();

        Channel goHomeChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_RVCOPERATIONALSTATE_GOHOME), CoreItemFactory.SWITCH)
                .withType(CHANNEL_RVCOPERATIONALSTATE_GOHOME).build();

        List<StateOption> options = new ArrayList<>();
        for (RvcOperationalStateCluster.OperationalStateEnum e : RvcOperationalStateCluster.OperationalStateEnum
                .values()) {
            options.add(new StateOption(e.value.toString(), e.label));
        }
        StateDescription sd = StateDescriptionFragmentBuilder.create().withOptions(options).build()
                .toStateDescription();
        Map<Channel, @Nullable StateDescription> map = new java.util.HashMap<>();
        map.put(stateChannel, sd);
        map.put(goHomeChannel, null);
        return map;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getIdWithoutGroup();
        if (id.equals(CHANNEL_ID_RVCOPERATIONALSTATE_GOHOME) && command instanceof OnOffType onOff) {
            if (onOff == OnOffType.ON) {
                handler.sendClusterCommand(endpointNumber, RvcOperationalStateCluster.CLUSTER_NAME,
                        RvcOperationalStateCluster.goHome());
                // set the button back to off after the command is sent
                updateState(CHANNEL_ID_RVCOPERATIONALSTATE_GOHOME, OnOffType.OFF);
            }
            return;
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case OperationalStateCluster.ATTRIBUTE_OPERATIONAL_STATE:
                if (message.value instanceof RvcOperationalStateCluster.OperationalStateEnum state) {
                    updateState(CHANNEL_ID_RVCOPERATIONALSTATE_STATE, new DecimalType(state.value));
                } else if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_RVCOPERATIONALSTATE_STATE, new DecimalType(number.intValue()));
                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_RVCOPERATIONALSTATE_STATE,
                initializingCluster.operationalState != null
                        ? new DecimalType(initializingCluster.operationalState.value)
                        : UnDefType.NULL);
    }
}
