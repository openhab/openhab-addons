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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ICDMANAGEMENT_CHECKINREGISTERED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ICDMANAGEMENT_LASTCHECKIN;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_ICDMANAGEMENT_CHECKINREGISTERED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_ICDMANAGEMENT_LASTCHECKIN;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.IcdManagementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.IcdState;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link IcdManagementCluster} attributes into Thing properties.
 *
 * The presence of this cluster means the device is an Intermittently Connected Device (ICD),
 * so this converter also flips the parent {@link NodeHandler}'s cached sleepy flag. That serves
 * as a defensive fallback when the bridge does not forward {@code PhysicalDeviceProperties}
 * on Connected events (e.g., when running against an older matter-server build).
 *
 * Devices that support long idle times (LIT) wake up regularly to check in with registered controllers. For them
 * this converter adds channels for the check-in registration and the last check-in, which matter.js reports
 * separately from the cluster attributes. {@code registeredClients} and {@code icdCounter} are intentionally skipped
 * (noisy / privacy / rapidly changing).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class IcdManagementConverter extends GenericConverter<IcdManagementCluster> {

    public IcdManagementConverter(IcdManagementCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();
        if (supportsCheckIn()) {
            channels.put(
                    ChannelBuilder.create(new ChannelUID(channelGroupUID, CHANNEL_ID_ICDMANAGEMENT_CHECKINREGISTERED),
                            CoreItemFactory.SWITCH).withType(CHANNEL_ICDMANAGEMENT_CHECKINREGISTERED).build(),
                    null);
            channels.put(ChannelBuilder.create(new ChannelUID(channelGroupUID, CHANNEL_ID_ICDMANAGEMENT_LASTCHECKIN),
                    CoreItemFactory.DATETIME).withType(CHANNEL_ICDMANAGEMENT_LASTCHECKIN).build(), null);
        }
        return channels;
    }

    /**
     * Whether the device sends check-ins, which only devices that support long idle times do
     */
    public boolean supportsCheckIn() {
        IcdManagementCluster.FeatureMap features = initializingCluster.featureMap;
        return features != null && features.longIdleTimeSupport;
    }

    public void updateIcdState(IcdState state) {
        if (!supportsCheckIn()) {
            return;
        }
        updateState(CHANNEL_ID_ICDMANAGEMENT_CHECKINREGISTERED, OnOffType.from(state.registered));
        Long lastCheckIn = state.lastCheckIn;
        updateState(CHANNEL_ID_ICDMANAGEMENT_LASTCHECKIN,
                lastCheckIn != null ? new DateTimeType(Instant.ofEpochMilli(lastCheckIn)) : UnDefType.UNDEF);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case IcdManagementCluster.ATTRIBUTE_IDLE_MODE_DURATION:
            case IcdManagementCluster.ATTRIBUTE_ACTIVE_MODE_DURATION:
            case IcdManagementCluster.ATTRIBUTE_ACTIVE_MODE_THRESHOLD:
            case IcdManagementCluster.ATTRIBUTE_OPERATING_MODE:
            case IcdManagementCluster.ATTRIBUTE_CLIENTS_SUPPORTED_PER_FABRIC:
            case IcdManagementCluster.ATTRIBUTE_USER_ACTIVE_MODE_TRIGGER_HINT:
            case IcdManagementCluster.ATTRIBUTE_USER_ACTIVE_MODE_TRIGGER_INSTRUCTION:
            case IcdManagementCluster.ATTRIBUTE_MAXIMUM_CHECK_IN_BACKOFF:
                updateThingAttributeProperty(message.path.attributeName, message.value);
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        logger.debug("initState");
        updateThingProperties(initializingCluster);
        if (handler instanceof NodeHandler nh) {
            nh.setSleepyFromIcd(true);
        }
    }

    private void updateThingProperties(IcdManagementCluster cluster) {
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_IDLE_MODE_DURATION, cluster.idleModeDuration);
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_ACTIVE_MODE_DURATION, cluster.activeModeDuration);
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_ACTIVE_MODE_THRESHOLD, cluster.activeModeThreshold);
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_OPERATING_MODE, cluster.operatingMode);
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_CLIENTS_SUPPORTED_PER_FABRIC,
                cluster.clientsSupportedPerFabric);
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_USER_ACTIVE_MODE_TRIGGER_HINT,
                cluster.userActiveModeTriggerHint);
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_USER_ACTIVE_MODE_TRIGGER_INSTRUCTION,
                cluster.userActiveModeTriggerInstruction);
        updateThingAttributeProperty(IcdManagementCluster.ATTRIBUTE_MAXIMUM_CHECK_IN_BACKOFF,
                cluster.maximumCheckInBackoff);
    }
}
