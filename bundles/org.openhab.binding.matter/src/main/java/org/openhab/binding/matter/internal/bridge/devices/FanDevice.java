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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.bridge.AttributeState;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.FanControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * The {@link FanDevice} is a device that represents a Fan.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class FanDevice extends BaseDevice {
    private final Map<String, GenericItem> itemMap = new HashMap<>();
    private final Map<String, String> attributeToItemNameMap = new HashMap<>();
    private final FanModeMapper fanModeMapper = new FanModeMapper();
    @Nullable
    private Integer lastSpeed;
    @Nullable
    private Integer lastMode;
    @Nullable
    private OnOffType lastOnOff;

    public FanDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "Fan";
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        GenericItem item = itemForAttribute(clusterName, attributeName);
        // if we have an item bound to this attribute, we can just update it, otherwise we need to handle updating other
        // items (see else block)
        if (item != null) {
            switch (attributeName) {
                case FanControlCluster.ATTRIBUTE_FAN_MODE:
                    try {
                        int mode = ((Double) data).intValue();
                        String mappedMode = fanModeMapper.toCustomValue(mode);
                        if (item instanceof NumberItem numberItem) {
                            numberItem.send(new DecimalType(mappedMode));
                        } else if (item instanceof StringItem stringItem) {
                            stringItem.send(new StringType(mappedMode));
                        } else if (item instanceof SwitchItem switchItem) {
                            switchItem.send(mode > 0 ? OnOffType.ON : OnOffType.OFF);
                        }
                    } catch (FanModeMappingException e) {
                        logger.debug("Could not convert {} to custom value", data);
                    }
                    break;
                case FanControlCluster.ATTRIBUTE_PERCENT_SETTING:
                    int level = ((Double) data).intValue();
                    if (item instanceof GroupItem groupItem) {
                        groupItem.send(new PercentType(level));
                    } else if (item instanceof DimmerItem dimmerItem) {
                        dimmerItem.send(new PercentType(level));
                    }
                    break;
                case OnOffCluster.ATTRIBUTE_ON_OFF:
                    if (item instanceof SwitchItem switchItem) {
                        OnOffType onOff = OnOffType.from((Boolean) data);
                        switchItem.send(onOff);
                        lastOnOff = onOff;
                    }
                    break;
                default:
                    break;
            }
        } else {
            // if there is not an item bound to a specific attribute, we need to handle updating other items and fake it
            switch (attributeName) {
                case FanControlCluster.ATTRIBUTE_PERCENT_SETTING: {
                    int level = ((Double) data).intValue();
                    // try and update the on/off state if set
                    GenericItem genericItem = itemForAttribute(OnOffCluster.CLUSTER_PREFIX,
                            OnOffCluster.ATTRIBUTE_ON_OFF);
                    if (genericItem instanceof SwitchItem switchItem) {
                        switchItem.send(OnOffType.from(level > 0));
                    }
                    // try and update the fan mode if set
                    genericItem = itemForAttribute(FanControlCluster.CLUSTER_PREFIX,
                            FanControlCluster.ATTRIBUTE_FAN_MODE);
                    try {
                        String mappedMode = fanModeMapper
                                .toCustomValue(level > 0 ? FanControlCluster.FanModeEnum.ON.value
                                        : FanControlCluster.FanModeEnum.OFF.value);
                        if (genericItem instanceof NumberItem numberItem) {
                            numberItem.send(new DecimalType(mappedMode));
                        } else if (genericItem instanceof StringItem stringItem) {
                            stringItem.send(new StringType(mappedMode));
                        } else if (genericItem instanceof SwitchItem switchItem) {
                            switchItem.send(OnOffType.from(level > 0));
                        }
                    } catch (FanModeMappingException e) {
                        logger.debug("Could not convert {} to custom value", data);
                    }
                }
                    break;
                case FanControlCluster.ATTRIBUTE_FAN_MODE: {
                    int mode = ((Double) data).intValue();
                    GenericItem genericItem = itemForAttribute(FanControlCluster.CLUSTER_PREFIX,
                            FanControlCluster.ATTRIBUTE_PERCENT_SETTING);
                    PercentType level = mode > 0 ? PercentType.HUNDRED : PercentType.ZERO;
                    if (genericItem instanceof GroupItem groupItem) {
                        groupItem.send(level);
                    } else if (genericItem instanceof DimmerItem dimmerItem) {
                        dimmerItem.send(level);
                    }
                    // try and update the on/off state if set
                    genericItem = itemForAttribute(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF);
                    if (genericItem instanceof SwitchItem switchItem) {
                        switchItem.send(OnOffType.from(mode > 0));
                    }
                }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        Map<String, Object> attributeMap = new HashMap<>();
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        attributeMap.putAll(primaryMetadata.getAttributeOptions());
        Set<Item> members = new HashSet<>();
        members.add(primaryItem);
        if (primaryItem instanceof GroupItem groupItem) {
            members.addAll(groupItem.getAllMembers());
        }
        for (Item member : members) {
            if (member instanceof GenericItem genericMember) {
                MetaDataMapping metadata = metaDataMapping(genericMember);
                State state = genericMember.getState();
                for (String attribute : metadata.attributes) {
                    String[] pair = attribute.split("\\.");
                    if (pair.length != 2) {
                        logger.debug("Unknown attribute format {}", attribute);
                        continue;
                    }
                    String attributeName = pair[1];
                    switch (attributeName) {
                        case FanControlCluster.ATTRIBUTE_PERCENT_SETTING:
                            if (state instanceof PercentType percentType) {
                                int speed = percentType.intValue();
                                attributeMap.put(attribute, speed);
                                lastSpeed = speed;
                            } else {
                                attributeMap.put(attribute, 0);
                                lastSpeed = 0;
                            }
                            break;
                        case FanControlCluster.ATTRIBUTE_FAN_MODE:
                            int mode = 0;
                            if (state instanceof DecimalType decimalType) {
                                mode = decimalType.intValue();
                            }
                            attributeMap.put(attribute, mode);
                            fanModeMapper.initializeMappings(metadata.config);
                            lastMode = mode;
                            break;
                        case OnOffCluster.ATTRIBUTE_ON_OFF:
                            if (state instanceof OnOffType onOffType) {
                                attributeMap.put(attribute, onOffType == OnOffType.ON);
                                lastOnOff = onOffType;
                            } else {
                                attributeMap.put(attribute, false);
                                lastOnOff = OnOffType.OFF;
                            }
                            break;
                        default:
                            continue;
                    }
                    if (!itemMap.containsKey(genericMember.getUID())) {
                        itemMap.put(genericMember.getUID(), genericMember);
                        genericMember.addStateChangeListener(this);
                    }
                    attributeMap.putAll(metadata.getAttributeOptions());
                    attributeToItemNameMap.put(attribute, genericMember.getUID());
                }
            }
        }
        updateMissingAttributes().forEach((attribute, value) -> {
            attributeMap.put(attribute, value);
        });
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        attributeToItemNameMap.clear();
        primaryItem.removeStateChangeListener(this);
        itemMap.forEach((uid, item) -> {
            ((GenericItem) item).removeStateChangeListener(this);
        });
        itemMap.clear();
    }

    @Override
    public void updateState(Item item, State state) {
        List<AttributeState> states = new ArrayList<>();
        attributeToItemNameMap.forEach((attribute, itemUid) -> {
            if (itemUid.equals(item.getUID())) {
                String[] pair = attribute.split("\\.");
                if (pair.length != 2) {
                    logger.debug("Unknown attribute format {}", attribute);
                    return;
                }
                String clusterName = pair[0];
                String attributeName = pair[1];
                switch (attributeName) {
                    case FanControlCluster.ATTRIBUTE_PERCENT_SETTING:
                        if (state instanceof PercentType percentType) {
                            int speed = percentType.intValue();
                            states.add(new AttributeState(clusterName, attributeName, speed));
                            states.add(new AttributeState(clusterName, FanControlCluster.ATTRIBUTE_PERCENT_CURRENT,
                                    speed));
                            lastSpeed = speed;
                        }
                        break;
                    case FanControlCluster.ATTRIBUTE_FAN_MODE:
                        if (state instanceof OnOffType onOffType) {
                            int mode = onOffType == OnOffType.ON ? FanControlCluster.FanModeEnum.ON.value
                                    : FanControlCluster.FanModeEnum.OFF.value;
                            states.add(new AttributeState(clusterName, attributeName, mode));
                            lastMode = mode;
                        } else {
                            try {
                                int mode = fanModeMapper.fromCustomValue(state.toString()).value;
                                states.add(new AttributeState(clusterName, attributeName, mode));
                                lastMode = mode;
                            } catch (FanModeMappingException e) {
                                logger.debug("Could not convert {} to matter value", state.toString());
                            }
                        }
                        break;
                    case OnOffCluster.ATTRIBUTE_ON_OFF:
                        if (state instanceof OnOffType onOffType) {
                            states.add(new AttributeState(clusterName, attributeName, onOffType == OnOffType.ON));
                            lastOnOff = onOffType;
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        states.addAll(generateMissingAttributes());
        setEndpointStates(states);
    }

    /**
     * Fan device types mandates mode and speed tp be present, this fakes that if those are missing.
     * 
     * @return
     */
    private Map<String, Object> updateMissingAttributes() {
        Map<String, Object> attributeMap = new HashMap<>();
        OnOffType onOff = lastOnOff;
        Integer mode = lastMode;
        Integer speed = lastSpeed;
        if (lastSpeed == null) {
            if (onOff != null) {
                attributeMap.put(FanControlCluster.CLUSTER_PREFIX + "." + FanControlCluster.ATTRIBUTE_PERCENT_CURRENT,
                        onOff == OnOffType.ON ? 100 : 0);
                attributeMap.put(FanControlCluster.CLUSTER_PREFIX + "." + FanControlCluster.ATTRIBUTE_PERCENT_SETTING,
                        onOff == OnOffType.ON ? 100 : 0);
            } else if (mode != null) {
                attributeMap.put(FanControlCluster.CLUSTER_PREFIX + "." + FanControlCluster.ATTRIBUTE_PERCENT_CURRENT,
                        mode == 0 ? 0 : 100);
                attributeMap.put(FanControlCluster.CLUSTER_PREFIX + "." + FanControlCluster.ATTRIBUTE_PERCENT_SETTING,
                        mode == 0 ? 0 : 100);
            }
        }
        if (mode == null) {
            if (onOff != null) {
                attributeMap.put(FanControlCluster.CLUSTER_PREFIX + "." + FanControlCluster.ATTRIBUTE_FAN_MODE,
                        onOff == OnOffType.ON ? FanControlCluster.FanModeEnum.ON.value
                                : FanControlCluster.FanModeEnum.OFF.value);
            } else if (speed != null) {
                attributeMap.put(FanControlCluster.CLUSTER_PREFIX + "." + FanControlCluster.ATTRIBUTE_FAN_MODE,
                        speed == 0 ? FanControlCluster.FanModeEnum.OFF.value : FanControlCluster.FanModeEnum.ON.value);
            }
        }
        return attributeMap;
    }

    private List<AttributeState> generateMissingAttributes() {
        List<AttributeState> states = new ArrayList<>();
        updateMissingAttributes().forEach((attribute, value) -> {
            String[] pair = attribute.split("\\.");
            if (pair.length != 2) {
                logger.debug("Unknown attribute format {}", attribute);
                return;
            }
            String clusterName = pair[0];
            String attributeName = pair[1];
            states.add(new AttributeState(clusterName, attributeName, value));
        });
        return states;
    }

    private @Nullable GenericItem itemForAttribute(String clusterName, String attributeName) {
        String pathName = clusterName + "." + attributeName;
        String itemUid = attributeToItemNameMap.get(pathName);
        if (itemUid != null) {
            return itemMap.get(itemUid);
        }
        return null;
    }

    class FanModeMapper {
        private final Map<Integer, String> intToCustomMap = new HashMap<>();
        private final Map<String, FanControlCluster.FanModeEnum> customToEnumMap = new HashMap<>();

        public FanModeMapper() {
            Map<String, Object> mappings = new HashMap<>();
            FanControlCluster.FanModeEnum[] modes = FanControlCluster.FanModeEnum.values();
            for (FanControlCluster.FanModeEnum mode : modes) {
                mappings.put(mode.name(), mode.getValue());
            }
            initializeMappings(mappings);
        }

        public FanModeMapper(Map<String, Object> mappings) {
            initializeMappings(mappings);
        }

        private void initializeMappings(Map<String, Object> mappings) {
            if (mappings.isEmpty()) {
                return;
            }

            // don't bother mapping if there's no OFF
            if (!mappings.containsKey("OFF")) {
                return;
            }

            intToCustomMap.clear();
            customToEnumMap.clear();
            for (Map.Entry<String, Object> entry : mappings.entrySet()) {
                String customKey = entry.getKey().trim();
                Object valueObj = entry.getValue();
                String customValue = valueObj.toString().trim();

                try {
                    FanControlCluster.FanModeEnum mode = FanControlCluster.FanModeEnum.valueOf(customKey);
                    intToCustomMap.put(mode.value, customValue);
                    customToEnumMap.put(customValue, mode);
                } catch (IllegalArgumentException e) {
                    // ignore unknown values
                }
            }
        }

        public String toCustomValue(int modeValue) throws FanModeMappingException {
            String value = intToCustomMap.get(modeValue);
            if (value == null) {
                throw new FanModeMappingException("No mapping for mode: " + modeValue);
            }
            return value;
        }

        public FanControlCluster.FanModeEnum fromCustomValue(String customValue) throws FanModeMappingException {
            FanControlCluster.FanModeEnum value = customToEnumMap.get(customValue);
            if (value == null) {
                throw new FanModeMappingException("No mapping for custom value: " + customValue);
            }
            return value;
        }
    }

    class FanModeMappingException extends Exception {
        private static final long serialVersionUID = 1L;

        public FanModeMappingException(String message) {
            super(message);
        }
    }
}
