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

import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThermostatCluster;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ThermostatDevice} is a device that represents a Thermostat.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThermostatDevice extends BaseDevice {
    private final Map<String, GenericItem> itemMap = new HashMap<>();
    private final Map<String, String> attributeToItemNameMap = new HashMap<>();
    private final SystemModeMapper systemModeMapper = new SystemModeMapper();

    public ThermostatDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "Thermostat";
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        String pathName = clusterName + "." + attributeName;
        String itemUid = attributeToItemNameMap.get(pathName);
        if (itemUid != null) {
            GenericItem item = itemMap.get(itemUid);
            if (item != null) {
                switch (attributeName) {
                    case ThermostatCluster.ATTRIBUTE_OCCUPIED_HEATING_SETPOINT:
                    case ThermostatCluster.ATTRIBUTE_OCCUPIED_COOLING_SETPOINT:
                        if (item instanceof NumberItem numberItem) {
                            QuantityType<Temperature> t = ValueUtils
                                    .valueToTemperature(Float.valueOf(data.toString()).intValue());
                            numberItem.send(t);
                        }
                        break;
                    case ThermostatCluster.ATTRIBUTE_SYSTEM_MODE:
                        try {
                            int mode = ((Double) data).intValue();
                            String mappedMode = systemModeMapper.toCustomValue(mode);
                            if (item instanceof NumberItem numberItem) {
                                numberItem.send(new DecimalType(mappedMode));
                            } else if (item instanceof StringItem stringItem) {
                                stringItem.send(new StringType(mappedMode));
                            } else if (item instanceof SwitchItem switchItem) {
                                switchItem.send(OnOffType.from(mode > 0));
                            }
                        } catch (SystemModeMappingException e) {
                            logger.debug("Could not convert {} to custom value", data);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void updateState(Item item, State state) {
        attributeToItemNameMap.forEach((attribute, itemUid) -> {
            if (itemUid.equals(item.getUID())) {
                // we need to do conversion here
                String[] pair = attribute.split("\\.");
                if (pair.length != 2) {
                    logger.debug("Unknown attribute format {}", attribute);
                    return;
                }
                String clusterName = pair[0];
                String attributeName = pair[1];
                switch (attributeName) {
                    case ThermostatCluster.ATTRIBUTE_LOCAL_TEMPERATURE:
                    case ThermostatCluster.ATTRIBUTE_OUTDOOR_TEMPERATURE:
                    case ThermostatCluster.ATTRIBUTE_OCCUPIED_HEATING_SETPOINT:
                    case ThermostatCluster.ATTRIBUTE_OCCUPIED_COOLING_SETPOINT:
                        Integer value = ValueUtils.temperatureToValue(state);
                        if (value != null) {
                            logger.debug("Setting {} to {}", attributeName, value);
                            setEndpointState(clusterName, attributeName, value);
                        } else {
                            logger.debug("Could not convert {} to matter value", state.toString());
                        }
                        break;
                    case ThermostatCluster.ATTRIBUTE_SYSTEM_MODE:
                        try {
                            int mode = systemModeMapper.fromCustomValue(state.toString()).value;
                            setEndpointState(clusterName, attributeName, mode);
                        } catch (SystemModeMappingException e) {
                            logger.debug("Could not convert {} to matter value", state.toString());
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        Map<String, Object> attributeMap = new HashMap<>();
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        // add any settings for attributes from config, like thermostat.minHeatSetpointLimit=0
        attributeMap.putAll(primaryMetadata.getAttributeOptions());
        for (Item member : ((GroupItem) primaryItem).getAllMembers()) {
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
                        case ThermostatCluster.ATTRIBUTE_LOCAL_TEMPERATURE:
                        case ThermostatCluster.ATTRIBUTE_OUTDOOR_TEMPERATURE:
                        case ThermostatCluster.ATTRIBUTE_OCCUPIED_HEATING_SETPOINT:
                        case ThermostatCluster.ATTRIBUTE_OCCUPIED_COOLING_SETPOINT:
                            if (state instanceof UnDefType) {
                                attributeMap.put(attribute, 0);
                            } else {
                                Integer value = ValueUtils.temperatureToValue(state);
                                attributeMap.put(attribute, value != null ? value : 0);
                            }
                            break;
                        case ThermostatCluster.ATTRIBUTE_SYSTEM_MODE:
                            try {
                                systemModeMapper.initializeMappings(metadata.config);
                                int mode = systemModeMapper.fromCustomValue(state.toString()).value;
                                attributeMap.put(attribute, mode);
                            } catch (SystemModeMappingException e) {
                                logger.debug("Could not convert {} to matter value", state.toString());
                            }
                            break;
                        default:
                            continue;
                    }
                    if (!itemMap.containsKey(genericMember.getUID())) {
                        itemMap.put(genericMember.getUID(), genericMember);
                        genericMember.addStateChangeListener(this);
                    }
                    // add any settings for attributes from config, like thermostat.minHeatSetpointLimit=0
                    attributeMap.putAll(metadata.getAttributeOptions());
                    attributeToItemNameMap.put(attribute, genericMember.getUID());
                }
            }
        }
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

    class SystemModeMapper {
        private final Map<Integer, String> intToCustomMap = new HashMap<>();
        private final Map<String, ThermostatCluster.SystemModeEnum> customToEnumMap = new HashMap<>();

        public SystemModeMapper() {
            Map<String, Object> mappings = new HashMap<>();
            ThermostatCluster.SystemModeEnum[] modes = ThermostatCluster.SystemModeEnum.values();
            for (ThermostatCluster.SystemModeEnum mode : modes) {
                mappings.put(mode.name(), mode.getValue());
            }
            initializeMappings(mappings);
        }

        public SystemModeMapper(Map<String, Object> mappings) {
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
                    ThermostatCluster.SystemModeEnum mode = ThermostatCluster.SystemModeEnum.valueOf(customKey);
                    intToCustomMap.put(mode.value, customValue);
                    customToEnumMap.put(customValue, mode);
                } catch (IllegalArgumentException e) {
                    // ignore unknown values
                }
            }
        }

        public String toCustomValue(int modeValue) throws SystemModeMappingException {
            String value = intToCustomMap.get(modeValue);
            if (value == null) {
                throw new SystemModeMappingException("No mapping for mode: " + modeValue);
            }
            return value;
        }

        public ThermostatCluster.SystemModeEnum fromCustomValue(String customValue) throws SystemModeMappingException {
            ThermostatCluster.SystemModeEnum value = customToEnumMap.get(customValue);
            if (value == null) {
                throw new SystemModeMappingException("No mapping for custom value: " + customValue);
            }
            return value;
        }
    }

    class SystemModeMappingException extends Exception {
        private static final long serialVersionUID = 1L;

        public SystemModeMappingException(String message) {
            super(message);
        }
    }
}
