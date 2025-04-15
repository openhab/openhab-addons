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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_CHANNEL;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_NETWORKNAME;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_PANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_RLOC16;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_ROUTINGROLE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADNETWORKDIAGNOSTICS_CHANNEL;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADNETWORKDIAGNOSTICS_NETWORKNAME;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADNETWORKDIAGNOSTICS_PANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADNETWORKDIAGNOSTICS_RLOC16;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADNETWORKDIAGNOSTICS_ROUTINGROLE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.ITEM_TYPE_NUMBER;
import static org.openhab.binding.matter.internal.MatterBindingConstants.ITEM_TYPE_STRING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ThreadNetworkDiagnosticsConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThreadNetworkDiagnosticsConverter extends GenericConverter<ThreadNetworkDiagnosticsCluster> {

    public ThreadNetworkDiagnosticsConverter(ThreadNetworkDiagnosticsCluster cluster, MatterBaseThingHandler handler,
            int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public void pollCluster() {
        // read the whole cluster
        handler.readCluster(ThreadNetworkDiagnosticsCluster.class, endpointNumber, initializingCluster.id)
                .thenAccept(cluster -> {
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL,
                            cluster.channel != null ? new DecimalType(cluster.channel) : UnDefType.NULL);
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE,
                            cluster.routingRole != null ? new DecimalType(cluster.routingRole.value) : UnDefType.NULL);
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME,
                            cluster.networkName != null ? new StringType(cluster.networkName) : UnDefType.NULL);
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID,
                            cluster.panId != null ? new DecimalType(cluster.panId) : UnDefType.NULL);
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID,
                            cluster.extendedPanId != null ? new DecimalType(cluster.extendedPanId) : UnDefType.NULL);
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16,
                            cluster.rloc16 != null ? new DecimalType(cluster.rloc16) : UnDefType.NULL);
                }).exceptionally(e -> {
                    logger.debug("Error polling thread network diagnostics", e);
                    return null;
                });
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID thingUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        Channel channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL), ITEM_TYPE_NUMBER)
                .withType(CHANNEL_THREADNETWORKDIAGNOSTICS_CHANNEL)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_CHANNEL)).build();
        channels.put(channel, null);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE), ITEM_TYPE_NUMBER)
                .withType(CHANNEL_THREADNETWORKDIAGNOSTICS_ROUTINGROLE)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_ROUTINGROLE)).build();
        List<StateOption> roleOptions = new ArrayList<>();
        roleOptions.add(new StateOption(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.UNSPECIFIED.value.toString(),
                ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.UNSPECIFIED.label));
        roleOptions.add(new StateOption(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.UNASSIGNED.value.toString(),
                ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.UNASSIGNED.label));
        roleOptions
                .add(new StateOption(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.SLEEPY_END_DEVICE.value.toString(),
                        ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.SLEEPY_END_DEVICE.label));
        roleOptions.add(new StateOption(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.END_DEVICE.value.toString(),
                ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.END_DEVICE.label));
        roleOptions.add(new StateOption(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.REED.value.toString(),
                ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.REED.label));
        roleOptions.add(new StateOption(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.ROUTER.value.toString(),
                ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.ROUTER.label));
        roleOptions.add(new StateOption(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER.value.toString(),
                ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER.label));
        StateDescription roleDesc = StateDescriptionFragmentBuilder.create().withPattern("%d").withOptions(roleOptions)
                .build().toStateDescription();
        channels.put(channel, roleDesc);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME), ITEM_TYPE_STRING)
                .withType(CHANNEL_THREADNETWORKDIAGNOSTICS_NETWORKNAME)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_NETWORKNAME)).build();
        channels.put(channel, null);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID), ITEM_TYPE_NUMBER)
                .withType(CHANNEL_THREADNETWORKDIAGNOSTICS_PANID)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_PANID)).build();
        channels.put(channel, null);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID), ITEM_TYPE_NUMBER)
                .withType(CHANNEL_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID)).build();
        channels.put(channel, null);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16), ITEM_TYPE_NUMBER)
                .withType(CHANNEL_THREADNETWORKDIAGNOSTICS_RLOC16)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADNETWORKDIAGNOSTICS_RLOC16)).build();
        channels.put(channel, null);

        return channels;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL, new DecimalType(number));
                }
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE:
                if (message.value instanceof ThreadNetworkDiagnosticsCluster.RoutingRoleEnum role) {
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE, new DecimalType(role.value));
                }
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME:
                if (message.value instanceof String string) {
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME, new StringType(string));
                }
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID, new DecimalType(number));
                }
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID, new DecimalType(number));
                }
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16, new DecimalType(number));
                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL,
                initializingCluster.channel != null ? new DecimalType(initializingCluster.channel) : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE,
                initializingCluster.routingRole != null ? new DecimalType(initializingCluster.routingRole.value)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME,
                initializingCluster.networkName != null ? new StringType(initializingCluster.networkName)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID,
                initializingCluster.panId != null ? new DecimalType(initializingCluster.panId) : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID,
                initializingCluster.extendedPanId != null ? new DecimalType(initializingCluster.extendedPanId)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16,
                initializingCluster.rloc16 != null ? new DecimalType(initializingCluster.rloc16) : UnDefType.NULL);
    }
}
