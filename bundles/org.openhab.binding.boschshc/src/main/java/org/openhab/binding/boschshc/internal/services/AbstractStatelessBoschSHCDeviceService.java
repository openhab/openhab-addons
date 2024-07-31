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
package org.openhab.binding.boschshc.internal.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Abstract implementation for device services that only allow setting states
 * via HTTP POST requests. State-less services can not receive any states from
 * the bridge.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class AbstractStatelessBoschSHCDeviceService<TState extends BoschSHCServiceState>
        extends AbstractBoschSHCService {

    protected AbstractStatelessBoschSHCDeviceService(String serviceName) {
        super(serviceName);
    }

    /**
     * Sends a HTTP POST request to the bridge with a state payload.
     *
     * @param state the state object to be sent to the bridge
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public void setState(TState state) throws InterruptedException, TimeoutException, ExecutionException {
        String deviceId = getDeviceId();
        if (deviceId == null) {
            return;
        }

        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        bridgeHandler.postState(deviceId, getServiceName(), state);
    }
}
