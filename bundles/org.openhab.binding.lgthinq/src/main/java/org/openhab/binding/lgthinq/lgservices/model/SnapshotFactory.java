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

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.ac.ACSnapshotV1;
import org.openhab.binding.lgthinq.lgservices.model.ac.ACSnapshotV2;
import org.openhab.binding.lgthinq.lgservices.model.washer.WMSnapshot;

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

    public static final SnapshotFactory getInstance() {
        return instance;
    }

    public Snapshot create(Map<String, Object> deviceSettings) throws LGThinqApiException {
        DeviceTypes type = getDeviceType(deviceSettings);
        Map<String, Object> snapMap = (Map<String, Object>) deviceSettings.get("snapshot");
        if (snapMap == null) {
            throw new LGThinqApiException("snapshot node not present in device monitoring result.");
        }
        LGAPIVerion version = discoveryAPIVersion(snapMap, type);
        switch (type) {
            case AIR_CONDITIONER:
                switch (version) {
                    case V1_0: {
                        return objectMapper.convertValue(deviceSettings, ACSnapshotV2.class);
                    }
                    case V2_0: {
                        return objectMapper.convertValue(deviceSettings, ACSnapshotV1.class);
                    }
                }
            case WASHING_MACHINE:
                switch (version) {
                    case V1_0: {
                        throw new IllegalArgumentException("Version 1.0 for Washer is not supported yet.");
                    }
                    case V2_0: {
                        return objectMapper.convertValue(deviceSettings, WMSnapshot.class);
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

            case WASHING_MACHINE:
                return LGAPIVerion.V2_0;
            default:
                throw new IllegalStateException("Unexpected capability. The type " + type + " was not implemented yet");
        }
    }
}
