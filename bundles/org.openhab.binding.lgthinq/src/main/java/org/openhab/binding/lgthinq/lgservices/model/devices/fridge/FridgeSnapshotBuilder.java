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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.REFRIGERATOR_SNAPSHOT_NODE_V2;
import static org.openhab.binding.lgthinq.lgservices.model.DeviceTypes.REFRIGERATOR;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.*;

/**
 * The {@link FridgeSnapshotBuilder}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FridgeSnapshotBuilder extends DefaultSnapshotBuilder<FridgeCanonicalSnapshot> {
    public FridgeSnapshotBuilder() {
        super(FridgeCanonicalSnapshot.class);
    }

    @Override
    public FridgeCanonicalSnapshot createFromBinary(String binaryData, List<MonitoringBinaryProtocol> prot,
            CapabilityDefinition capDef) throws LGThinqUnmarshallException, LGThinqApiException {
        return super.createFromBinary(binaryData, prot, capDef);
    }

    @Override
    protected FridgeCanonicalSnapshot getSnapshot(Map<String, Object> snapMap, CapabilityDefinition capDef) {
        FridgeCanonicalSnapshot snap;
        if (REFRIGERATOR.equals(capDef.getDeviceType())) {
            switch (capDef.getDeviceVersion()) {
                case V1_0: {
                    throw new IllegalArgumentException("Version 1.0 for Washer is not supported yet.");
                }
                case V2_0: {
                    Map<String, Object> refMap = Objects.requireNonNull(
                            (Map<String, Object>) snapMap.get(REFRIGERATOR_SNAPSHOT_NODE_V2),
                            "washerDryer node must be present in the snapshot");
                    snap = objectMapper.convertValue(refMap, snapClass);
                    snap.setRawData(snapMap);
                    return snap;
                }
            }
        }

        throw new IllegalStateException("Snapshot for device type " + capDef.getDeviceType()
                + " not supported for this builder. It most likely a bug");
    }

    @Override
    protected LGAPIVerion discoveryAPIVersion(Map<String, Object> snapMap, DeviceTypes type) {
        if (REFRIGERATOR.equals(type)) {
            if (snapMap.containsKey(REFRIGERATOR_SNAPSHOT_NODE_V2)) {
                return LGAPIVerion.V2_0;
            } else {
                throw new IllegalStateException(
                        "Unexpected error. Can't find key node attributes to determine ACCapability API version.");
            }
        }
        throw new IllegalStateException(
                "Unexpected capability. The type " + type + " is not supported by this builder. It most likely a bug");
    }
}
