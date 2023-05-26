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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.*;

/**
 * The {@link ACSnapshotBuilder}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACSnapshotBuilder extends DefaultSnapshotBuilder<ACCanonicalSnapshot> {
    public ACSnapshotBuilder() {
        super(ACCanonicalSnapshot.class);
    }

    @Override
    public ACCanonicalSnapshot createFromBinary(String binaryData, List<MonitoringBinaryProtocol> prot,
            CapabilityDefinition capDef) throws LGThinqUnmarshallException, LGThinqApiException {
        return super.createFromBinary(binaryData, prot, capDef);
    }

    @Override
    protected ACCanonicalSnapshot getSnapshot(Map<String, Object> snapMap, CapabilityDefinition capDef) {
        ACCanonicalSnapshot snap;
        switch (capDef.getDeviceType()) {
            case AIR_CONDITIONER:
            case HEAT_PUMP:
                snap = objectMapper.convertValue(snapMap, snapClass);
                snap.setRawData(snapMap);
                return snap;
        }
        throw new IllegalStateException("Snapshot for device type " + capDef.getDeviceType()
                + " not supported for this builder. It most likely a bug");
    }

    @Override
    protected LGAPIVerion discoveryAPIVersion(Map<String, Object> snapMap, DeviceTypes type) {
        switch (type) {
            case AIR_CONDITIONER:
            case HEAT_PUMP:
                if (snapMap.containsKey("airState.opMode")) {
                    return LGAPIVerion.V2_0;
                } else if (snapMap.containsKey("OpMode")) {
                    return LGAPIVerion.V1_0;
                } else {
                    throw new IllegalStateException(
                            "Unexpected error. Can't find key node attributes to determine ACCapability API version.");
                }
            default:
                throw new IllegalStateException("Discovery version for device type " + type
                        + " not supported for this builder. It most likely a bug");
        }
    }
}
