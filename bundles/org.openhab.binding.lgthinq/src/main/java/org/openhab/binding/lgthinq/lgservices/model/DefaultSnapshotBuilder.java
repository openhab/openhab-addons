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
package org.openhab.binding.lgthinq.lgservices.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An abstract class representing a Default Snapshot Builder for creating different types of snapshots.
 *
 * @param <S> The type parameter representing the Abstract Snapshot Definition
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class DefaultSnapshotBuilder<S extends AbstractSnapshotDefinition> implements SnapshotBuilder<S> {
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, Map<String, Map<String, Object>>> MODEL_CACHED_BITKEY_DEF = new HashMap<>();
    protected final Class<S> snapClass;
    private final Logger logger = LoggerFactory.getLogger(DefaultSnapshotBuilder.class);

    public DefaultSnapshotBuilder(Class<S> clazz) {
        snapClass = clazz;
    }

    /**
     * Create a Snapshot object from binary data using the provided monitoring binary protocols and capability
     * definitions.
     *
     * @param binaryData The binary data to create the Snapshot from
     * @param prot The list of MonitoringBinaryProtocol objects for defining how to parse the binary data
     * @param capDef The CapabilityDefinition object for the Snapshot
     * @return The created Snapshot object based on the binary data
     * @throws LGThinqUnmarshallException if unmarshalling the binary data encounters an error
     * @throws LGThinqApiException if any LG Thinq API related error occurs
     */
    @Override
    public S createFromBinary(String binaryData, List<MonitoringBinaryProtocol> prot, CapabilityDefinition capDef)
            throws LGThinqUnmarshallException, LGThinqApiException {
        try {
            Map<String, Object> snapValues = new HashMap<>();
            byte[] data = binaryData.getBytes();
            BeanInfo beanInfo = Introspector.getBeanInfo(snapClass);
            S snap = snapClass.getConstructor().newInstance();
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            Map<String, PropertyDescriptor> aliasesMethod = new HashMap<>();
            for (PropertyDescriptor property : pds) {
                // all attributes of class.
                Method m = property.getReadMethod();
                if (m == null) {
                    logger.warn("Property {} has no getter method. It's most likely a bug!", property.getName());
                    continue;
                }
                JsonProperty jsonProAn = m.getAnnotation(JsonProperty.class);
                if (jsonProAn != null) {
                    String value = jsonProAn.value();
                    aliasesMethod.putIfAbsent(value, property);
                }
                JsonAlias jsonAliasAn = m.getAnnotation(JsonAlias.class);
                if (jsonAliasAn != null) {
                    String[] values = jsonAliasAn.value();
                    for (String v : values) {
                        aliasesMethod.putIfAbsent(v, property);
                    }
                }
            }
            for (MonitoringBinaryProtocol protField : prot) {
                if (protField.startByte + protField.length > data.length) {
                    // end of data. If have more fields in the protocol, will be ignored
                    break;
                }
                String fName = protField.fieldName;
                int value = 0;
                for (int i = protField.startByte; i < protField.startByte + protField.length; i++) {
                    value = (value << 8) + data[i];
                }
                snapValues.put(fName, value);
                PropertyDescriptor property = aliasesMethod.get(fName);
                if (property != null) {
                    // found property. Get bit value
                    Method m = property.getWriteMethod();
                    if (m.getParameters()[0].getType() == String.class) {
                        m.invoke(snap, String.valueOf(value));
                    } else if (m.getParameters()[0].getType() == Double.class) {
                        m.invoke(snap, (double) value);
                    } else if (m.getParameters()[0].getType() == Integer.class) {
                        m.invoke(snap, value);
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Parameter type not supported for this factory:%s",
                                        m.getParameters()[0].getType().toString()));
                    }
                }
            }
            snap.setRawData(snapValues);
            return snap;
        } catch (IntrospectionException | InvocationTargetException | InstantiationException | IllegalAccessException
                | NoSuchMethodException e) {
            throw new LGThinqUnmarshallException("Unexpected Error unmarshalling binary data", e);
        }
    }

    /**
     * Create a Snapshot object from JSON data with the provided device type and capability definition.
     *
     * @param snapshotDataJson The JSON data to create the Snapshot from
     * @param deviceType The DeviceTypes enum representing the type of device
     * @param capDef The CapabilityDefinition object for the Snapshot
     * @return The created Snapshot object
     * @throws LGThinqUnmarshallException if unmarshalling the JSON data encounters an error
     * @throws LGThinqApiException if any LG Thinq API related error occurs
     */
    @Override
    public S createFromJson(String snapshotDataJson, DeviceTypes deviceType, CapabilityDefinition capDef)
            throws LGThinqUnmarshallException, LGThinqApiException {
        try {
            Map<String, Object> snapshotMap = MAPPER.readValue(snapshotDataJson, new TypeReference<>() {
            });
            Map<String, Object> deviceSetting = new HashMap<>();
            deviceSetting.put("deviceType", deviceType.deviceTypeId());
            deviceSetting.put("snapshot", snapshotMap);
            return createFromJson(deviceSetting, capDef);
        } catch (JsonProcessingException e) {
            throw new LGThinqUnmarshallException("Unexpected Error unmarshalling json to map", e);
        }
    }

    @Override
    public S createFromJson(Map<String, Object> deviceSettings, CapabilityDefinition capDef)
            throws LGThinqApiException {
        Map<String, Object> snapMap = MAPPER.convertValue(deviceSettings.get("snapshot"), new TypeReference<>() {
        });
        if (snapMap == null) {
            throw new LGThinqApiException("snapshot node not present in device monitoring result.");
        }
        return getSnapshot(snapMap, capDef);
    }

    /**
     * Retrieves a snapshot object based on the provided map and capability definition.
     *
     * @param snapMap The map containing snapshot data
     * @param capDef The CapabilityDefinition object defining the capabilities
     * @return The retrieved snapshot object
     */
    protected abstract S getSnapshot(Map<String, Object> snapMap, CapabilityDefinition capDef);

    /**
     * Retrieves the DeviceTypes enum based on the deviceType field and deviceCode from the provided root map.
     *
     * @param rootMap The map containing the deviceType and deviceCode fields
     * @return The DeviceTypes enum corresponding to the deviceType and deviceCode
     */
    protected DeviceTypes getDeviceType(Map<String, Object> rootMap) {
        Integer deviceTypeId = (Integer) rootMap.get("deviceType");
        // device code is only present in v2 devices snapshot.
        String deviceCode = Objects.requireNonNullElse((String) rootMap.get("deviceCode"), "");
        Objects.requireNonNull(deviceTypeId, "Unexpected error. deviceType field not present in snapshot schema");
        return DeviceTypes.fromDeviceTypeId(deviceTypeId, deviceCode);
    }

    /**
     * Retrieves the bit key for the given key from the capability feature values map.
     *
     * @param key The key for which the bit key is needed
     * @param capFeatureValues The map containing capability feature values
     * @param cachedBitKey The cached bit key values map
     * @return The bit key as a map containing 'option', 'startbit', and 'length' entries
     */
    private Map<String, Object> getBitKey(String key, final Map<String, Map<String, Object>> capFeatureValues,
            final Map<String, Map<String, Object>> cachedBitKey) {
        // Define a local function to search for the bit key
        Function<Map<String, Map<String, Object>>, Map<String, Object>> searchBitKey = data -> {
            if (data.isEmpty()) {
                return Collections.emptyMap();
            }

            for (int i = 1; i <= 3; i++) {
                String optKey = "Option" + i;
                Map<String, Object> option = data.get(optKey);

                if (option == null) {
                    continue;
                }

                List<Map<String, Object>> optionList = MAPPER.convertValue(option.get("option"), new TypeReference<>() {
                });

                if (optionList == null) {
                    continue;
                }

                for (Map<String, Object> opt : optionList) {
                    String value = (String) opt.get("value");

                    if (key.equals(value)) {
                        Integer startBit = (Integer) opt.get("startbit");
                        Integer length = (Integer) opt.getOrDefault("length", 1);

                        if (startBit == null) {
                            return Collections.emptyMap();
                        }

                        Map<String, Object> bitKey = new HashMap<>();
                        bitKey.put("option", optKey);
                        bitKey.put("startbit", startBit);
                        bitKey.put("length", length);

                        return bitKey;
                    }
                }
            }

            return Collections.emptyMap();
        };

        Map<String, Object> bitKey = cachedBitKey.get(key);

        if (bitKey == null) {
            // cache the bitKey if it doesn't was fetched yet.
            bitKey = searchBitKey.apply(capFeatureValues);
            cachedBitKey.put(key, bitKey);
        }

        return bitKey;
    }

    /**
     * Return the value related to the bit-value definition. It's used in Washer/Dryer V1 snapshot parser.
     * It was here, in the parent, because maybe other devices need the same functionality. If not,
     * We can transfer these methods to the WasherDryer Snapshot Builder.
     *
     * @param key Key trying to get the value
     * @param snapRawValues snap raw value
     * @param capDef capability
     * @return return value associated or blank string
     */
    protected String bitValue(String key, Map<String, Object> snapRawValues, final CapabilityDefinition capDef) {
        // get the capability Values/MonitoringValues Map
        // Look up the bit value for a specific key
        if (snapRawValues.isEmpty()) {
            logger.warn("No snapshot raw values provided. Corrupted data returned or bug");
            return "";
        }
        Map<String, Map<String, Object>> cachedBitKey = getSpecificCacheBitKey(capDef);
        Map<String, Object> bitKey = this.getBitKey(key, capDef.getFeatureValuesRawData(), cachedBitKey);
        if (bitKey.isEmpty()) {
            logger.warn("BitKey {} not found in the Options feature values description capability. It's mostly a bug",
                    key);
            return "";
        }
        // Get the name of the option (Option1, Option2, etc) that contains the key (ex. LoadItem, RemoteStart) desired
        String option = (String) bitKey.get("option");
        Object bitValueDef = snapRawValues.get(option);
        if (bitValueDef == null) {
            logger.warn("Value definition not found for the bitValue definition: {}. It's mostly a bug", option);
            return "";
        }
        String value = bitValueDef.toString();
        if (value.isEmpty()) {
            return "0";
        }

        int bitValue = Integer.parseInt(value);
        int startBit = (int) Objects.requireNonNull(bitKey.get("startbit"), "Not expected null here");
        int length = (int) bitKey.getOrDefault("length", 0);
        int val = 0;

        for (int i = 0; i < length; i++) {
            int bitIndex = (int) Math.pow(2, (startBit + i));
            int bit = (bitValue & bitIndex) != 0 ? 1 : 0;
            val += bit * (int) Math.pow(2, i);
        }

        return Integer.toString(val);
    }

    /**
     * Retrieves a specific cache bit key based on the provided CapabilityDefinition object.
     *
     * @param capDef The CapabilityDefinition object representing the device capabilities.
     * @return A map containing the specific cache bit key for the given CapabilityDefinition.
     */
    protected synchronized Map<String, Map<String, Object>> getSpecificCacheBitKey(CapabilityDefinition capDef) {
        return Objects
                .requireNonNull(MODEL_CACHED_BITKEY_DEF.computeIfAbsent(capDef.getModelName(), k -> new HashMap<>()));
    }
}
