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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.DW_SNAPSHOT_WASHER_DRYER_NODE_V2;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DefaultSnapshotBuilder;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;

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
                Map<String, Object> dishWasher = Objects.requireNonNull(
                        MAPPER.convertValue(snapMap.get(DW_SNAPSHOT_WASHER_DRYER_NODE_V2), new TypeReference<>() {
                        }), "dishwasher node must be present in the snapshot");
                snap = MAPPER.convertValue(dishWasher, snapClass);
                snap.setRawData(dishWasher);
                return snap;
            default:
                throw new IllegalArgumentException(
                        String.format("Version %s for DishWasher is not supported.", version));
        }
    }
}
