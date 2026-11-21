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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DefaultSnapshotBuilder;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringBinaryProtocol;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * The {@link DishWasherSnapshotBuilder}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DishWasherSnapshotBuilder extends DefaultSnapshotBuilder<DishWasherSnapshot> {
    public DishWasherSnapshotBuilder() {
        super(DishWasherSnapshot.class);
    }

    @Override
    public DishWasherSnapshot createFromBinary(byte[] binaryData, List<MonitoringBinaryProtocol> prot,
            CapabilityDefinition capDef) throws LGThinqUnmarshallException, LGThinqApiException {
        DishWasherSnapshot snap = super.createFromBinary(binaryData, prot, capDef);
        if (!(capDef instanceof DishWasherCapability dwCap)) {
            throw new IllegalArgumentException(
                    "Capability must be a DishWasherCapability for DishWasherSnapshotBuilder. It is most likely a bug!");
        }
        // binary data carries the index of the state (ex. "0" or "4") instead of its V2 name (POWEROFF)
        String stateLabel = dwCap.getStateFeat().getValuesMapping().get(snap.getState());
        snap.setPowerStatus(DW_STATE_POWER_OFF_LABEL.equals(stateLabel) ? DevicePowerState.DV_POWER_OFF
                : DevicePowerState.DV_POWER_ON);
        // some models report the door state only as a bit of an Option field
        String doorFeatureName = dwCap.getDoorStateFeat().getName();
        if (!snap.getRawData().containsKey(doorFeatureName) && hasBitValue(doorFeatureName, capDef)) {
            snap.setDoorLock(bitValue(doorFeatureName, snap.getRawData(), capDef));
        }
        return snap;
    }

    @Override
    protected DishWasherSnapshot getSnapshot(Map<String, Object> snapMap, CapabilityDefinition capDef) {
        DishWasherSnapshot snap;
        DeviceTypes type = capDef.getDeviceType();
        LGAPIVerion version = capDef.getDeviceVersion();
        if (!type.equals(DeviceTypes.DISH_WASHER)) {
            throw new IllegalArgumentException(
                    String.format("Device Type %s not supported by this builder. It's most likely a bug.", type));
        }
        switch (version) {
            case V1_0:
                throw new IllegalArgumentException("Version 1.0 for DishWasher is not supported yet.");
            case V2_0:
                Object dishWasherNode = snapMap.get(DW_SNAPSHOT_WASHER_DRYER_NODE_V2);
                if (dishWasherNode == null) {
                    throw new IllegalArgumentException("dishwasher node must be present in the snapshot");
                }
                Map<String, Object> dishWasher = Objects
                        .requireNonNull(MAPPER.convertValue(dishWasherNode, new TypeReference<Map<String, Object>>() {
                        }));
                snap = Objects.requireNonNull(MAPPER.convertValue(dishWasher, snapClass));
                snap.setRawData(dishWasher);
                return snap;
            default:
                throw new IllegalArgumentException(
                        String.format("Version %s for DishWasher is not supported.", version));
        }
    }
}
