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
package org.openhab.binding.boschshc.internal.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Abstract implementation of a system service that does not represent a physical device.
 * Examples for system services are the intrusion detection system and the water detection system.
 * <p>
 * The endpoints to retrieve system states are different from the ones for physical devices, i.e. they do not follow the
 * pattern
 *
 * <pre>
 * https://{IP}:8444/smarthome/devices/{deviceId}/services/{serviceName}/state
 * </pre>
 *
 * Instead, system services have endpoints like
 *
 * <pre>
 * /intrusion/states/system
 * </pre>
 *
 * <p>
 * The services of the devices and their official APIs can be found
 * <a href="https://apidocs.bosch-smarthome.com/local/">here</a>.
 *
 * @param <TState> type used for representing the service state
 *
 * @author David Pace - Initial contribution
 */
@NonNullByDefault
public abstract class BoschSHCSystemService<TState extends BoschSHCServiceState> extends BoschSHCService<TState> {

    private String endpoint;

    /**
     * Constructs a system service instance.
     *
     * @param serviceName name of the service, such as <code>intrusionDetectionService</code>
     * @param stateClass the class representing states of the system
     * @param endpoint the part of the URL after <code>https://{IP}:8444/smarthome/</code>, e.g.
     *            <code>intrusion/states/system</code>
     */
    protected BoschSHCSystemService(String serviceName, Class<TState> stateClass, String endpoint) {
        super(serviceName, stateClass);
        this.endpoint = endpoint;
    }

    /**
     * Uses the endpoint directly instead of constructing a device-specific URL.
     */
    @Override
    public @Nullable TState getState()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return null;
        }
        return bridgeHandler.getState(this.endpoint, getStateClass());
    }
}
