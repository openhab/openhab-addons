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
package org.openhab.binding.lgthinq.lgservices;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQDishWasherApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQDishWasherApiV1ClientServiceImpl
        extends LGThinQAbstractApiV1ClientService<DishWasherCapability, DishWasherSnapshot>
        implements LGThinQDishWasherApiClientService {
    private final Logger logger = LoggerFactory.getLogger(LGThinQDishWasherApiV1ClientServiceImpl.class);

    protected LGThinQDishWasherApiV1ClientServiceImpl(HttpClient httpClient) {
        super(DishWasherCapability.class, DishWasherSnapshot.class, httpClient);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) {
        // Nothing to do for V1 thinq
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not Supported for this device");
    }

    @Override
    @Nullable
    public DishWasherSnapshot getDeviceData(@NonNull String bridgeName, @NonNull String deviceId,
            @NonNull CapabilityDefinition capDef) throws LGThinqApiException {
        throw new UnsupportedOperationException("Method not supported in V1 API device.");
    }

    @Override
    public void remoteStart(String bridgeName, DishWasherCapability cap, String deviceId, Map<String, Object> data)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void wakeUp(String bridgeName, String deviceId, Boolean wakeUp) throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
