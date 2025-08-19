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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.bridge.AttributeState;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;

/**
 * The {@link DimmableLightDevice} is a device that represents a Dimmable Light.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DimmableLightDevice extends BaseDevice {

    private State lastOnOffState = OnOffType.OFF;

    public DimmableLightDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "DimmableLight";
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        PercentType level = Optional.ofNullable(primaryItem.getStateAs(PercentType.class))
                .orElseGet(() -> new PercentType(0));
        lastOnOffState = level.intValue() > 0 ? OnOffType.ON : OnOffType.OFF;
        attributeMap.put(LevelControlCluster.CLUSTER_PREFIX + "." + LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL,
                Math.max(1, ValueUtils.percentToLevel(level)));
        attributeMap.put(OnOffCluster.CLUSTER_PREFIX + "." + OnOffCluster.ATTRIBUTE_ON_OFF, level.intValue() > 0);
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        switch (attributeName) {
            case OnOffCluster.ATTRIBUTE_ON_OFF:
                updateOnOff(OnOffType.from(Boolean.valueOf(data.toString())));
                break;
            case LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL:
                if (lastOnOffState == OnOffType.ON) {
                    updateLevel(ValueUtils.levelToPercent(((Double) data).intValue()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void updateState(Item item, State state) {
        List<AttributeState> states = new ArrayList<>();
        if (state instanceof HSBType hsb) {
            states.add(new AttributeState(LevelControlCluster.CLUSTER_PREFIX,
                    LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL, ValueUtils.percentToLevel(hsb.getBrightness())));
            states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF,
                    hsb.getBrightness().intValue() > 0));
            lastOnOffState = hsb.getBrightness().intValue() > 0 ? OnOffType.ON : OnOffType.OFF;
        } else if (state instanceof PercentType percentType) {
            states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF,
                    percentType.intValue() > 0));
            if (percentType.intValue() > 0) {
                states.add(new AttributeState(LevelControlCluster.CLUSTER_PREFIX,
                        LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL, ValueUtils.percentToLevel(percentType)));
                lastOnOffState = OnOffType.ON;
            } else {
                lastOnOffState = OnOffType.OFF;
            }
        } else if (state instanceof OnOffType onOffType) {
            states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF,
                    onOffType == OnOffType.ON));
            lastOnOffState = onOffType;
        }
        if (!states.isEmpty()) {
            setEndpointStates(states);
        }
    }

    private void updateOnOff(OnOffType onOffType) {
        lastOnOffState = onOffType;
        if (primaryItem instanceof GroupItem groupItem) {
            groupItem.send(onOffType);
        } else {
            ((SwitchItem) primaryItem).send(onOffType);
        }
    }

    private void updateLevel(PercentType level) {
        if (primaryItem instanceof GroupItem groupItem) {
            groupItem.send(level);
        } else {
            ((DimmerItem) primaryItem).send(level);
        }
    }
}
