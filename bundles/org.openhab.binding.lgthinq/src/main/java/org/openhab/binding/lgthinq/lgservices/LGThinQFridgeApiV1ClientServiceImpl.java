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
package org.openhab.binding.lgthinq.lgservices;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;

/**
 * The {@link LGThinQFridgeApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQFridgeApiV1ClientServiceImpl
        extends LGThinQAbstractApiV1ClientService<FridgeCapability, FridgeCanonicalSnapshot>
        implements LGThinQFridgeApiClientService {

    private static final LGThinQFridgeApiClientService instance;
    static {
        instance = new LGThinQFridgeApiV1ClientServiceImpl(FridgeCapability.class, FridgeCanonicalSnapshot.class);
    }

    protected LGThinQFridgeApiV1ClientServiceImpl(Class<FridgeCapability> capabilityClass,
            Class<FridgeCanonicalSnapshot> snapshotClass) {
        super(capabilityClass, snapshotClass);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) {
        // Nothing to do for V1 thinq
    }

    public static LGThinQFridgeApiClientService getInstance() {
        return instance;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    @Nullable
    public FridgeCanonicalSnapshot getDeviceData(@NonNull String bridgeName, @NonNull String deviceId,
            @NonNull CapabilityDefinition capDef) throws LGThinqApiException {
        throw new UnsupportedOperationException("Method not supported in V1 API device.");
    }
}
