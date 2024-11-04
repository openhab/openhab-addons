/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.WMD_SNAPSHOT_WASHER_DRYER_NODE_V2;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DefaultSnapshotBuilder;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringBinaryProtocol;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * The {@link WasherDryerSnapshotBuilder}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WasherDryerSnapshotBuilder extends DefaultSnapshotBuilder<WasherDryerSnapshot> {
    public WasherDryerSnapshotBuilder() {
        super(WasherDryerSnapshot.class);
    }

    @Override
    public WasherDryerSnapshot createFromBinary(String binaryData, List<MonitoringBinaryProtocol> prot,
            CapabilityDefinition capDef) throws LGThinqUnmarshallException, LGThinqApiException {
        WasherDryerSnapshot snap = super.createFromBinary(binaryData, prot, capDef);
        snap.setRemoteStart(
                bitValue(((WasherDryerCapability) capDef).getRemoteStartFeatName(), snap.getRawData(), capDef));
        if (((WasherDryerCapability) capDef).hasDoorLook()) {
            snap.setDoorLock(
                    bitValue(((WasherDryerCapability) capDef).getDoorLockFeatName(), snap.getRawData(), capDef));
        }
        snap.setChildLock(bitValue(((WasherDryerCapability) capDef).getChildLockFeatName(), snap.getRawData(), capDef));
        return snap;
    }

    @Override
    protected WasherDryerSnapshot getSnapshot(Map<String, Object> snapMap, CapabilityDefinition capDef) {
        WasherDryerSnapshot snap;
        DeviceTypes type = capDef.getDeviceType();
        LGAPIVerion version = capDef.getDeviceVersion();
        switch (type) {
            case DRYER_TOWER:
            case DRYER:
            case WASHER_TOWER:
            case WASHERDRYER_MACHINE:
                switch (version) {
                    case V1_0: {
                        if (type == DeviceTypes.DRYER || type == DeviceTypes.DRYER_TOWER) {
                            throw new IllegalArgumentException("Version 1.0 for Dryer is not supported yet.");
                        } else {
                            snap = objectMapper.convertValue(snapMap, snapClass);
                            snap.setRawData(snapMap);
                        }
                    }
                    case V2_0: {
                        Map<String, Object> washerDryerMap = Objects.requireNonNull(objectMapper
                                .convertValue(snapMap.get(WMD_SNAPSHOT_WASHER_DRYER_NODE_V2), new TypeReference<>() {
                                }), "washerDryer node must be present in the snapshot");
                        snap = objectMapper.convertValue(washerDryerMap, snapClass);
                        setAltCourseNodeName(capDef, snap, washerDryerMap);
                        snap.setRawData(washerDryerMap);
                        return snap;
                    }
                    default:
                        throw new IllegalStateException("Snapshot for device type " + type + " and version " + version
                                + " are not supported for this builder. It most likely a bug");
                }
            default:
                throw new IllegalStateException(
                        "Snapshot for device type " + type + " not supported for this builder. It most likely a bug");
        }
    }

    private static void setAltCourseNodeName(CapabilityDefinition capDef, WasherDryerSnapshot snap,
            Map<String, Object> washerDryerMap) {
        if (snap.getCourse().isEmpty() && capDef instanceof WasherDryerCapability) {
            String altCourseNodeName = ((WasherDryerCapability) capDef).getDefaultCourseFieldName();
            String altSmartCourseNodeName = ((WasherDryerCapability) capDef).getDefaultSmartCourseFeatName();
            snap.setCourse(Objects.requireNonNullElse((String) washerDryerMap.get(altCourseNodeName), ""));
            snap.setSmartCourse(Objects.requireNonNullElse((String) washerDryerMap.get(altSmartCourseNodeName), ""));
        }
    }
}
