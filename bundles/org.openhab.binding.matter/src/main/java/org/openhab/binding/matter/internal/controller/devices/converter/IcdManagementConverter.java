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

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.IcdManagementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

/**
 * A converter for translating {@link IcdManagementCluster} attributes into Thing properties.
 *
 * The presence of this cluster means the device is an Intermittently Connected Device (ICD),
 * so this converter also flips the parent {@link NodeHandler}'s cached sleepy flag. That serves
 * as a defensive fallback when the bridge does not forward {@code PhysicalDeviceProperties}
 * on Connected events (e.g., when running against an older matter-server build).
 *
 * Read-only: no channels are exposed. {@code registeredClients} and {@code icdCounter} are
 * intentionally skipped (noisy / privacy / rapidly changing).
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
        return Collections.emptyMap();
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
