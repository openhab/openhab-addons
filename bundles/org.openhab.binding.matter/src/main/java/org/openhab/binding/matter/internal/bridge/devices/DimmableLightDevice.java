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

    // Level Control couples the minimum level with "off": a move to MinLevel is what turns the light off, so an off
    // light reports MinLevel rather than whatever level it last passed through on the way down. The level to return
    // to on the next on is held by the global scene, not by CurrentLevel.
    private static final int MIN_LEVEL = 1;

    private State lastOnOffState = OnOffType.OFF;
    private int lastLevel = MIN_LEVEL;
    // The on/off value the endpoint was last told about. A brightness only says something about on/off when it
    // crosses zero, so this is what decides whether a state change carries the attribute at all.
    private boolean reportedOn;

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
        reportedOn = level.intValue() > 0;
        lastOnOffState = reportedOn ? OnOffType.ON : OnOffType.OFF;
        lastLevel = Math.max(MIN_LEVEL, ValueUtils.percentToLevel(level));
        attributeMap.put(LevelControlCluster.CLUSTER_PREFIX + "." + LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL,
                lastLevel);
        attributeMap.put(OnOffCluster.CLUSTER_PREFIX + "." + OnOffCluster.ATTRIBUTE_ON_OFF, reportedOn);
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
            states.addAll(lightStates(hsb.getBrightness()));
        } else if (state instanceof PercentType percentType) {
            states.addAll(lightStates(percentType));
        } else if (state instanceof OnOffType onOffType) {
            // An explicit on/off state is unambiguous, so it always carries the attribute
            boolean on = onOffType == OnOffType.ON;
            reportedOn = on;
            lastOnOffState = onOffType;
            states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF, on));
            states.add(new AttributeState(LevelControlCluster.CLUSTER_PREFIX,
                    LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL, on ? lastLevel : MIN_LEVEL));
        }
        if (!states.isEmpty()) {
            setEndpointStates(states);
        }
    }

    /**
     * Builds the states for a brightness.
     * <p>
     * A dimmer that ramps reports every level it passes through, and a brightness on its way to off still reads as
     * "on". Only a brightness that crosses zero says anything about on/off, so the levels in between carry the level
     * alone -- reporting them as on/off contradicts the command that started the ramp and flips the client back on.
     * <p>
     * An off light reports {@link #MIN_LEVEL} rather than the last level of the ramp, so the level a client keeps
     * for it is deterministic instead of wherever the dimmer happened to be sampled.
     */
    private List<AttributeState> lightStates(PercentType brightness) {
        boolean on = brightness.intValue() > 0;
        if (on) {
            // Only remember levels the light was actually on at, so turning it back on restores that brightness
            lastLevel = Math.max(MIN_LEVEL, ValueUtils.percentToLevel(brightness));
        }
        lastOnOffState = on ? OnOffType.ON : OnOffType.OFF;
        List<AttributeState> states = new ArrayList<>();
        if (on != reportedOn) {
            reportedOn = on;
            states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF, on));
        }
        states.add(new AttributeState(LevelControlCluster.CLUSTER_PREFIX, LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL,
                on ? lastLevel : MIN_LEVEL));
        return states;
    }

    private void updateOnOff(OnOffType onOffType) {
        lastOnOffState = onOffType;
        if (primaryItem instanceof GroupItem groupItem) {
            groupItem.send(onOffType, MATTER_SOURCE);
        } else {
            ((SwitchItem) primaryItem).send(onOffType, MATTER_SOURCE);
        }
    }

    private void updateLevel(PercentType level) {
        lastLevel = ValueUtils.percentToLevel(level);
        if (primaryItem instanceof GroupItem groupItem) {
            groupItem.send(level, MATTER_SOURCE);
        } else {
            ((DimmerItem) primaryItem).send(level, MATTER_SOURCE);
        }
    }
}
