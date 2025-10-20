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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BooleanStateCluster;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;

/**
 * The {@link ContactSensorDevice} is a device that represents a Contact Sensor.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ContactSensorDevice extends BaseDevice {

    public ContactSensorDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "ContactSensor";
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        attributeMap.put(BooleanStateCluster.CLUSTER_PREFIX + "." + BooleanStateCluster.ATTRIBUTE_STATE_VALUE,
                contactState(primaryItem.getState()));
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void updateState(Item item, State state) {
        setEndpointState(BooleanStateCluster.CLUSTER_PREFIX, BooleanStateCluster.ATTRIBUTE_STATE_VALUE,
                contactState(primaryItem.getState()));
    }

    /**
     * Matter Device Library Specification R1.3
     * 7.1.4.2. Boolean State Cluster
     * True: Closed or contact
     * False: Open or no contact
     * 
     * @param state
     * @return closed or open
     */
    private boolean contactState(State state) {
        boolean open = true;
        if (state instanceof OnOffType onOffType) {
            open = onOffType == OnOffType.ON;
        }
        if (state instanceof OpenClosedType openClosedType) {
            open = openClosedType == OpenClosedType.OPEN;
        }
        return !open;
    }
}
