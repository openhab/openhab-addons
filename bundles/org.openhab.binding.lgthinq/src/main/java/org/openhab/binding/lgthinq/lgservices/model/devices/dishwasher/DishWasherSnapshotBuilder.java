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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.WM_SNAPSHOT_WASHER_DRYER_NODE_V2;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.*;

/**
 * The {@link WasherDryerSnapshotBuilder}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DishWasherSnapshotBuilder extends DefaultSnapshotBuilder<DishWasherSnapshot> {
    public DishWasherSnapshotBuilder() {
        super(DishWasherSnapshot.class);
    }

    @Override
    public DishWasherSnapshot createFromBinary(String binaryData, List<MonitoringBinaryProtocol> prot,
            CapabilityDefinition capDef) throws LGThinqUnmarshallException, LGThinqApiException {
        DishWasherSnapshot snap = super.createFromBinary(binaryData, prot, capDef);
        snap.setRemoteStart(
                bitValue(((DishWasherCapability) capDef).getRemoteStartFeatName(), snap.getRawData(), capDef));
        snap.setDoorLock(bitValue(((DishWasherCapability) capDef).getDoorLockFeatName(), snap.getRawData(), capDef));
        snap.setChildLock(bitValue(((DishWasherCapability) capDef).getChildLockFeatName(), snap.getRawData(), capDef));
        return snap;
    }

    @Override
    protected DishWasherSnapshot getSnapshot(Map<String, Object> snapMap, CapabilityDefinition capDef) {
        DishWasherSnapshot snap;
        DeviceTypes type = capDef.getDeviceType();
        LGAPIVerion version = capDef.getDeviceVersion();
        switch (type) {
            case WASHING_TOWER:
            case WASHERDRYER_MACHINE:
                switch (version) {
                    case V1_0: {
                        snap = objectMapper.convertValue(snapMap, snapClass);
                        snap.setRawData(snapMap);
                    }
                    case V2_0: {
                        Map<String, Object> washerDryerMap = Objects.requireNonNull(
                                (Map<String, Object>) snapMap.get(WM_SNAPSHOT_WASHER_DRYER_NODE_V2),
                                "washerDryer node must be present in the snapshot");
                        snap = objectMapper.convertValue(washerDryerMap, snapClass);
                        setAltCourseNodeName(capDef, snap, washerDryerMap);
                        snap.setRawData(washerDryerMap);
                        return snap;
                    }
                }
            case DRYER_TOWER:
            case DRYER:
                switch (version) {
                    case V1_0: {
                        throw new IllegalArgumentException("Version 1.0 for Washer is not supported yet.");
                    }
                    case V2_0: {
                        Map<String, Object> washerDryerMap = Objects.requireNonNull(
                                (Map<String, Object>) snapMap.get(WM_SNAPSHOT_WASHER_DRYER_NODE_V2),
                                "washerDryer node must be present in the snapshot");
                        snap = objectMapper.convertValue(washerDryerMap, snapClass);
                        setAltCourseNodeName(capDef, snap, washerDryerMap);
                        snap.setRawData(snapMap);
                        return snap;
                    }
                }
        }
        throw new IllegalStateException(
                "Snapshot for device type " + type + " not supported for this builder. It most likely a bug");
    }

    private static void setAltCourseNodeName(CapabilityDefinition capDef, DishWasherSnapshot snap,
            Map<String, Object> washerDryerMap) {
        if (snap.getCourse().isEmpty() && capDef instanceof DishWasherCapability) {
            String altCourseNodeName = ((DishWasherCapability) capDef).getDefaultCourseFieldName();
            String altSmartCourseNodeName = ((DishWasherCapability) capDef).getDefaultSmartCourseFeatName();
            snap.setCourse(Objects.requireNonNullElse((String) washerDryerMap.get(altCourseNodeName), ""));
            snap.setSmartCourse(Objects.requireNonNullElse((String) washerDryerMap.get(altSmartCourseNodeName), ""));
        }
    }

    @Override
    protected LGAPIVerion discoveryAPIVersion(Map<String, Object> snapMap, DeviceTypes type) {
        switch (type) {
            case DRYER_TOWER:
            case DRYER:
                return LGAPIVerion.V2_0;
            case WASHING_TOWER:
            case WASHERDRYER_MACHINE:
                if (snapMap.containsKey(WM_SNAPSHOT_WASHER_DRYER_NODE_V2)) {
                    return LGAPIVerion.V2_0;
                } else if (snapMap.containsKey("State")) {
                    return LGAPIVerion.V1_0;
                } else {
                    throw new IllegalStateException(
                            "Unexpected error. Can't find key node attributes to determine WASHERDRYER_MACHINE API version.");
                }
            default:
                throw new IllegalStateException("Discovery version for device type " + type
                        + " not supported for this builder. It most likely a bug");
        }
    }
}
