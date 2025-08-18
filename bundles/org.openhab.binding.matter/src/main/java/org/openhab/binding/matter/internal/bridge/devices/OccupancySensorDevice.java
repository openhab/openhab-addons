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
package org.openhab.binding.matter.internal.bridge.devices;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OccupancySensingCluster;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;

import com.google.gson.JsonObject;

/**
 * The {@link OccupancySensorDevice} is a device that represents an Occupancy Sensor.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OccupancySensorDevice extends BaseDevice {

    public OccupancySensorDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "OccupancySensor";
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        attributeMap.put(OccupancySensingCluster.CLUSTER_PREFIX + "." + OccupancySensingCluster.ATTRIBUTE_OCCUPANCY,
                occupiedState(primaryItem.getState()));
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void updateState(Item item, State state) {
        setEndpointState(OccupancySensingCluster.CLUSTER_PREFIX, OccupancySensingCluster.ATTRIBUTE_OCCUPANCY,
                occupiedState(primaryItem.getState()));
    }

    public static JsonObject occupiedState(State state) {
        boolean occupied = false;
        if (state instanceof OnOffType onOffType) {
            occupied = onOffType == OnOffType.ON;
        }
        if (state instanceof OpenClosedType openClosedType) {
            occupied = openClosedType == OpenClosedType.OPEN;
        }
        JsonObject stateJson = new JsonObject();
        stateJson.addProperty("occupied", occupied);
        return stateJson;
    }
}
