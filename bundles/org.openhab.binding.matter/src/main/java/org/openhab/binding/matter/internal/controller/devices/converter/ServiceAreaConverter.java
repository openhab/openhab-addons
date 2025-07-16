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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ServiceAreaCluster;
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
 * Converter for {@link ServiceAreaCluster}
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ServiceAreaConverter extends GenericConverter<ServiceAreaCluster> {

    private List<Integer> selectedAreasCache = new ArrayList<>();

    public ServiceAreaConverter(ServiceAreaCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
        if (cluster.selectedAreas != null) {
            selectedAreasCache.addAll(cluster.selectedAreas);
        }
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> map = new HashMap<>();
        if (initializingCluster.supportedAreas != null) {
            for (ServiceAreaCluster.AreaStruct area : initializingCluster.supportedAreas) {
                String channelId = CHANNEL_ID_SERVICEAREA_SELECTEDAREA_PREFIX + area.areaId;
                String label = area.areaInfo != null && area.areaInfo.locationInfo != null
                        && area.areaInfo.locationInfo.locationName != null ? area.areaInfo.locationInfo.locationName
                                : "Area " + area.areaId;
                Channel areaChannel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, channelId), CoreItemFactory.SWITCH)
                        .withType(CHANNEL_SERVICEAREA_SELECTEDAREA).withLabel(formatLabel(label)).build();
                map.put(areaChannel, null);
            }
        }
        return map;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getIdWithoutGroup();
        if (!id.startsWith(CHANNEL_ID_SERVICEAREA_SELECTEDAREA_PREFIX) || !(command instanceof OnOffType onOff)) {
            super.handleCommand(channelUID, command);
            return;
        }

        int areaId = Integer.parseInt(id.substring(CHANNEL_ID_SERVICEAREA_SELECTEDAREA_PREFIX.length()));
        List<Integer> selected = new ArrayList<>(selectedAreasCache);
        if (onOff == OnOffType.ON) {
            if (!selected.contains(areaId)) {
                selected.add(areaId);
            }
        } else {
            selected.remove(Integer.valueOf(areaId));
        }
        selectedAreasCache = selected;
        ClusterCommand cc = ServiceAreaCluster.selectAreas(selectedAreasCache);
        handler.sendClusterCommand(endpointNumber, ServiceAreaCluster.CLUSTER_NAME, cc);
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        if (ServiceAreaCluster.ATTRIBUTE_SELECTED_AREAS.equals(message.path.attributeName)) {
            if (message.value instanceof List<?> list) {
                List<Integer> newSel = new ArrayList<>();
                for (Object o : list) {
                    if (o instanceof Number n) {
                        newSel.add(n.intValue());
                    }
                }
                selectedAreasCache = newSel;
                if (initializingCluster.supportedAreas != null) {
                    for (ServiceAreaCluster.AreaStruct area : initializingCluster.supportedAreas) {
                        String channelId = CHANNEL_ID_SERVICEAREA_SELECTEDAREA_PREFIX + area.areaId;
                        updateState(channelId, selectedAreasCache.contains(area.areaId) ? OnOffType.ON : OnOffType.OFF);
                    }
                }
            }
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        selectedAreasCache = initializingCluster.selectedAreas != null
                ? new ArrayList<>(initializingCluster.selectedAreas)
                : new ArrayList<>();
        if (initializingCluster.supportedAreas != null) {
            for (ServiceAreaCluster.AreaStruct area : initializingCluster.supportedAreas) {
                String channelId = CHANNEL_ID_SERVICEAREA_SELECTEDAREA_PREFIX + area.areaId;
                updateState(channelId, selectedAreasCache.contains(area.areaId) ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }
}
