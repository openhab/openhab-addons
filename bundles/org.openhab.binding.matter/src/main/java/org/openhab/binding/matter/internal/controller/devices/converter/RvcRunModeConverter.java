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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcRunModeCluster;
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
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link RvcRunModeCluster} events and attributes to openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class RvcRunModeConverter extends GenericConverter<RvcRunModeCluster> {

    public RvcRunModeConverter(RvcRunModeCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_RVCRUNMODE_MODE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_RVCRUNMODE_MODE).build();

        List<StateOption> modeOptions = new ArrayList<>();
        initializingCluster.supportedModes
                .forEach(mode -> modeOptions.add(new StateOption(mode.mode.toString(), mode.label)));

        StateDescription stateDescriptionMode = StateDescriptionFragmentBuilder.create().withOptions(modeOptions)
                .build().toStateDescription();

        return Collections.singletonMap(channel, stateDescriptionMode);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType decimalType) {
            ClusterCommand cc = RvcRunModeCluster.changeToMode(decimalType.intValue());
            handler.sendClusterCommand(endpointNumber, RvcRunModeCluster.CLUSTER_NAME, cc);
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case RvcRunModeCluster.ATTRIBUTE_CURRENT_MODE:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_RVCRUNMODE_MODE, new DecimalType(number.intValue()));
                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_RVCRUNMODE_MODE,
                initializingCluster.currentMode != null ? new DecimalType(initializingCluster.currentMode)
                        : UnDefType.NULL);
    }
}
