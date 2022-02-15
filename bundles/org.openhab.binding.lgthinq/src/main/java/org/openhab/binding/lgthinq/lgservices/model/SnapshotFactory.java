/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.WM_SNAPSHOT_WASHER_DRYER_NODE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.ac.ACSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.dryer.DryerSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.washer.WasherSnapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link SnapshotFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class SnapshotFactory {
    private static final SnapshotFactory instance;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        instance = new SnapshotFactory();
    }

    public static SnapshotFactory getInstance() {
        return instance;
    }

    /**
     * Create a Snapshot result based on snapshotData collected from LG API (V1/C2)
     * 
     * @param snapshotDataJson V1: decoded returnedData; V2: snapshot body
     * @param deviceType device type
     * @return returns Snapshot implementation based on device type provided
     * @throws LGThinqApiException any error.
     */
    public <S extends Snapshot> S create(String snapshotDataJson, DeviceTypes deviceType, Class<S> clazz)
            throws LGThinqApiException {
        try {
            Map<String, Object> snapshotMap = objectMapper.readValue(snapshotDataJson, new TypeReference<>() {
            });
            Map<String, Object> deviceSetting = new HashMap<>();
            deviceSetting.put("deviceType", deviceType.deviceTypeId());
            deviceSetting.put("snapshot", snapshotMap);
            return create(deviceSetting, clazz);
        } catch (JsonProcessingException e) {
            throw new LGThinqApiException("Unexpected Error unmarshalling json to map", e);
        }
    }

    public <S extends Snapshot> S create(Map<String, Object> deviceSettings, Class<S> clazz)
            throws LGThinqApiException {
        DeviceTypes type = getDeviceType(deviceSettings);
        Map<String, Object> snapMap = ((Map<String, Object>) deviceSettings.get("snapshot"));
        if (snapMap == null) {
            throw new LGThinqApiException("snapshot node not present in device monitoring result.");
        }
        LGAPIVerion version = discoveryAPIVersion(snapMap, type);
        switch (type) {
            case AIR_CONDITIONER:
                return clazz.cast(objectMapper.convertValue(snapMap, ACSnapshot.class));
            case WASHING_MACHINE:
                switch (version) {
                    case V1_0: {
                        throw new IllegalArgumentException("Version 1.0 for Washer is not supported yet.");
                    }
                    case V2_0: {
                        Map<String, String> washerDryerMap = Objects.requireNonNull(
                                (Map<String, String>) snapMap.get(WM_SNAPSHOT_WASHER_DRYER_NODE),
                                "washerDryer node must be present in the snapshot");
                        return clazz.cast(objectMapper.convertValue(washerDryerMap, WasherSnapshot.class));
                    }
                }
            case DRYER:
                switch (version) {
                    case V1_0: {
                        throw new IllegalArgumentException("Version 1.0 for Washer is not supported yet.");
                    }
                    case V2_0: {
                        Map<String, String> washerDryerMap = Objects.requireNonNull(
                                (Map<String, String>) snapMap.get(WM_SNAPSHOT_WASHER_DRYER_NODE),
                                "washerDryer node must be present in the snapshot");
                        return clazz.cast(objectMapper.convertValue(washerDryerMap, DryerSnapshot.class));
                    }
                }
            default:
                throw new IllegalStateException("Unexpected capability. The type " + type + " was not implemented yet");
        }
    }

    private DeviceTypes getDeviceType(Map<String, Object> rootMap) {
        Integer deviceTypeId = (Integer) rootMap.get("deviceType");
        Objects.requireNonNull(deviceTypeId, "Unexpected error. deviceType field not present in snapshot schema");
        return DeviceTypes.fromDeviceTypeId(deviceTypeId);
    }

    private LGAPIVerion discoveryAPIVersion(Map<String, Object> snapMap, DeviceTypes type) {
        switch (type) {
            case AIR_CONDITIONER:
                if (snapMap.containsKey("airState.opMode")) {
                    return LGAPIVerion.V2_0;
                } else if (snapMap.containsKey("OpMode")) {
                    return LGAPIVerion.V1_0;
                } else {
                    throw new IllegalStateException(
                            "Unexpected error. Can't find key node attributes to determine AC API version.");
                }
            case DRYER:
            case WASHING_MACHINE:
                return LGAPIVerion.V2_0;
            default:
                throw new IllegalStateException("Unexpected capability. The type " + type + " was not implemented yet");
        }
    }
}
