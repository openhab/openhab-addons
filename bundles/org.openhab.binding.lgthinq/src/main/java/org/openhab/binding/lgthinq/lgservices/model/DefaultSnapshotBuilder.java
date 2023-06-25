/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.*;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqUnmarshallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link DefaultSnapshotBuilder}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class DefaultSnapshotBuilder<S extends AbstractSnapshotDefinition> implements SnapshotBuilder<S> {
    protected final Class<S> snapClass;
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(DefaultSnapshotBuilder.class);

    public DefaultSnapshotBuilder(Class<S> clazz) {
        snapClass = clazz;
    }

    private static final Map<String, Map<String, Map<String, Object>>> modelsCachedBitKeyDefinitions = new HashMap<>();

    /**
     * Create a Snapshot result based on snapshotData collected from LG API (V1/C2)
     *
     * @param binaryData V1: decoded returnedData
     * @param capDef
     * @return returns Snapshot implementation based on device type provided
     * @throws LGThinqApiException any error.
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
                Method m = property.getReadMethod(); // getter
                if (m.isAnnotationPresent(JsonProperty.class)) {
                    String value = m.getAnnotation(JsonProperty.class).value();
                    aliasesMethod.putIfAbsent(value, property);
                }
                if (m.isAnnotationPresent(JsonAlias.class)) {
                    String[] values = m.getAnnotation(JsonAlias.class).value();
                    for (String v : values) {
                        aliasesMethod.putIfAbsent(v, property);
                    }
                }
            }
            for (MonitoringBinaryProtocol protField : prot) {
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
     * Create a Snapshot result based on snapshotData collected from LG API (V1/C2)
     *
     * @param snapshotDataJson V1: decoded returnedData; V2: snapshot body
     * @param deviceType device type
     * @return returns Snapshot implementation based on device type provided
     * @throws LGThinqApiException any error.
     */
    @Override
    public S createFromJson(String snapshotDataJson, DeviceTypes deviceType, CapabilityDefinition capDef)
            throws LGThinqUnmarshallException, LGThinqApiException {
        try {
            Map<String, Object> snapshotMap = objectMapper.readValue(snapshotDataJson, new TypeReference<>() {
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
        DeviceTypes type = getDeviceType(deviceSettings);
        Map<String, Object> snapMap = ((Map<String, Object>) deviceSettings.get("snapshot"));
        if (snapMap == null) {
            throw new LGThinqApiException("snapshot node not present in device monitoring result.");
        }
        LGAPIVerion version = discoveryAPIVersion(snapMap, type);
        return getSnapshot(snapMap, capDef);
    }

    protected abstract S getSnapshot(Map<String, Object> snapMap, CapabilityDefinition capDef);

    protected DeviceTypes getDeviceType(Map<String, Object> rootMap) {
        Integer deviceTypeId = (Integer) rootMap.get("deviceType");
        // device code is only present in v2 devices snapshot.
        String deviceCode = Objects.requireNonNullElse((String) rootMap.get("deviceCode"), "");
        Objects.requireNonNull(deviceTypeId, "Unexpected error. deviceType field not present in snapshot schema");
        return DeviceTypes.fromDeviceTypeId(deviceTypeId, deviceCode);
    }

    protected abstract LGAPIVerion discoveryAPIVersion(Map<String, Object> snapMap, DeviceTypes type);

    /**
     * Used
     * 
     * @param key
     * @param capFeatureValues
     * @param cachedBitKey
     * @return
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

                List<Map<String, Object>> optionList = (List<Map<String, Object>>) option.get("option");

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
     * Return the value related to the bit-value definition. It's used in Waser/Dryer V1 snapshot parser.
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
        int startBit = (int) bitKey.get("startbit");
        int length = (int) bitKey.get("length");
        int val = 0;

        for (int i = 0; i < length; i++) {
            int bitIndex = (int) Math.pow(2, (startBit + i));
            int bit = (bitValue & bitIndex) != 0 ? 1 : 0;
            val += bit * (int) Math.pow(2, i);
        }

        return Integer.toString(val);
    }

    protected synchronized Map<String, Map<String, Object>> getSpecificCacheBitKey(CapabilityDefinition capDef) {
        return Objects.requireNonNull(
                modelsCachedBitKeyDefinitions.computeIfAbsent(capDef.getModelName(), k -> new HashMap<>()));
    }
}
