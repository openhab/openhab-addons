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
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;

/**
 * The {@link OnOffLightDevice}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OnOffLightDevice extends GenericDevice {

    public OnOffLightDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "OnOffLight";
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        switch (attributeName) {
            case OnOffCluster.ATTRIBUTE_ON_OFF: {
                if (primaryItem instanceof GroupItem groupItem) {
                    groupItem.send(OnOffType.from(Boolean.valueOf(data.toString())));
                } else {
                    ((SwitchItem) primaryItem).send(OnOffType.from(Boolean.valueOf(data.toString())));
                }
            }
                break;
            case LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL: {
                if (primaryItem instanceof GroupItem groupItem) {
                    groupItem.send(OnOffType.from(data.toString()));
                } else {
                    ((SwitchItem) primaryItem).send(OnOffType.from(data.toString()));
                }
            }
                break;
            default:
                break;
        }
    }

    @Override
    public MatterDeviceOptions activate() {
        dispose();
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        attributeMap.put(OnOffCluster.CLUSTER_PREFIX + "." + OnOffCluster.ATTRIBUTE_ON_OFF, Optional
                .ofNullable(primaryItem.getStateAs(OnOffType.class)).orElseGet(() -> OnOffType.OFF) == OnOffType.ON);
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void updateState(Item item, State state) {
        if (state instanceof HSBType hsb) {
            setEndpointState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF,
                    hsb.getBrightness().intValue() > 0 ? true : false);
        } else if (state instanceof PercentType percentType) {
            setEndpointState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF,
                    percentType.intValue() > 0 ? true : false);
        } else if (state instanceof OnOffType onOffType) {
            setEndpointState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF,
                    onOffType == OnOffType.ON ? true : false);
        }
    }
}
