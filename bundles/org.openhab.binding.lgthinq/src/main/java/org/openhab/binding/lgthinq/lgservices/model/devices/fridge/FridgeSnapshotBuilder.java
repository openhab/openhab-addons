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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.RE_SNAPSHOT_NODE_V2;
import static org.openhab.binding.lgthinq.lgservices.model.DeviceTypes.FRIDGE;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DefaultSnapshotBuilder;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringBinaryProtocol;

import com.fasterxml.jackson.core.type.TypeReference;

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
        if (FRIDGE.equals(capDef.getDeviceType())) {
            switch (capDef.getDeviceVersion()) {
                case V1_0:
                    throw new IllegalArgumentException("Version 1.0 for Fridge driver is not supported yet.");
                case V2_0: {
                    Map<String, Object> refMap = Objects.requireNonNull(
                            MAPPER.convertValue(snapMap.get(RE_SNAPSHOT_NODE_V2), new TypeReference<>() {
                            }), "washerDryer node must be present in the snapshot");
                    snap = MAPPER.convertValue(refMap, snapClass);
                    snap.setRawData(snapMap);
                    return snap;
                }
                default:
                    throw new IllegalArgumentException("Version informed is not supported for the Fridge driver.");
            }
        }

        throw new IllegalStateException("Snapshot for device type " + capDef.getDeviceType()
                + " not supported for this builder. It is most likely a bug");
    }
}
