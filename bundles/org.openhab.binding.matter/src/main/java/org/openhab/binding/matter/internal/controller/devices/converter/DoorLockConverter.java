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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_DOORLOCK_STATE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_DOORLOCK_STATE;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;

/**
 * A converter for translating {@link DoorLockCluster} events and attributes to openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DoorLockConverter extends GenericConverter<DoorLockCluster> {

    public DoorLockConverter(DoorLockCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_DOORLOCK_STATE), CoreItemFactory.SWITCH)
                .withType(CHANNEL_DOORLOCK_STATE).build();

        return Collections.singletonMap(channel, null);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType onOffType) {
            ClusterCommand doorLockCommand = onOffType == OnOffType.ON ? DoorLockCluster.lockDoor(null)
                    : DoorLockCluster.unlockDoor(null);
            handler.sendClusterCommand(endpointNumber, DoorLockCluster.CLUSTER_NAME, doorLockCommand);
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case "lockState":
                if (message.value instanceof DoorLockCluster.LockStateEnum lockState) {
                    updateState(CHANNEL_ID_DOORLOCK_STATE,
                            lockState == DoorLockCluster.LockStateEnum.LOCKED ? OnOffType.ON : OnOffType.OFF);
                }
            default:
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_DOORLOCK_STATE,
                initializingCluster.lockState == DoorLockCluster.LockStateEnum.LOCKED ? OnOffType.ON : OnOffType.OFF);
    }
}
