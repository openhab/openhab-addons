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
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;

/**
 * The {@link LGThinQFridgeApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQFridgeApiV2ClientServiceImpl
        extends LGThinQAbstractApiV2ClientService<FridgeCapability, FridgeCanonicalSnapshot>
        implements LGThinQFridgeApiClientService {

    private static final LGThinQFridgeApiClientService instance;
    static {
        instance = new LGThinQFridgeApiV2ClientServiceImpl(FridgeCapability.class, FridgeCanonicalSnapshot.class);
    }

    protected LGThinQFridgeApiV2ClientServiceImpl(Class<FridgeCapability> capabilityClass,
            Class<FridgeCanonicalSnapshot> snapshotClass) {
        super(capabilityClass, snapshotClass);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) {
        // TODO - Analise what to do here
    }

    public static LGThinQFridgeApiClientService getInstance() {
        return instance;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
