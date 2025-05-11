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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_MODESELECT_MODE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_MODESELECT_MODE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ModeSelectCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

/**
 * The {@link ModeSelectConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ModeSelectConverter extends GenericConverter<ModeSelectCluster> {

    public ModeSelectConverter(ModeSelectCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID thingUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_MODESELECT_MODE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_MODESELECT_MODE).withLabel(formatLabel(initializingCluster.description)).build();

        List<StateOption> modeOptions = new ArrayList<>();
        initializingCluster.supportedModes
                .forEach(mode -> modeOptions.add(new StateOption(mode.mode.toString(), mode.label)));

        @Nullable
        StateDescription stateDescriptionMode = StateDescriptionFragmentBuilder.create().withPattern("%d")
                .withOptions(modeOptions).build().toStateDescription();

        return Collections.singletonMap(channel, stateDescriptionMode);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType) {
            ClusterCommand cc = ModeSelectCluster.changeToMode(((DecimalType) command).intValue());
            handler.sendClusterCommand(endpointNumber, ModeSelectCluster.CLUSTER_NAME, cc);
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        Integer numberValue = message.value instanceof Number number ? number.intValue() : 0;
        switch (message.path.attributeName) {
            case ModeSelectCluster.ATTRIBUTE_CURRENT_MODE:
                initializingCluster.currentMode = numberValue;
                updateState(CHANNEL_ID_MODESELECT_MODE, new DecimalType(numberValue));
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_MODESELECT_MODE, new DecimalType(initializingCluster.currentMode));
    }
}
