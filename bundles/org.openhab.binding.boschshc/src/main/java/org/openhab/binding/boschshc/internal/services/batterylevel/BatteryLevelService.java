/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.batterylevel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

/**
 * Service to retrieve battery levels.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class BatteryLevelService extends BoschSHCService<DeviceServiceData> {

    public BatteryLevelService() {
        super("BatteryLevel", DeviceServiceData.class);
    }

    @Override
    public @Nullable DeviceServiceData getState()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {

        String deviceId = getDeviceId();
        if (deviceId == null) {
            return null;
        }

        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return null;
        }
        return bridgeHandler.getServiceData(deviceId, getServiceName());
    }
}
