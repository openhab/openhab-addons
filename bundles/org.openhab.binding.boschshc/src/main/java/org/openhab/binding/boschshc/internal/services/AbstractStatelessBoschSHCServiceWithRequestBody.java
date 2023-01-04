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
package org.openhab.binding.boschshc.internal.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Abstract implementation for services that allow setting states via HTTP POST requests containing a JSON request body.
 * State-less services can not receive any states from the bridge.
 * <p>
 * An example of such a service is the <code>arm</code> action of the intrusion detection system.
 * <p>
 * The services of the devices and their official APIs can be found
 * <a href="https://apidocs.bosch-smarthome.com/local/">here</a>.
 * 
 * @param <TRequest> Type to represent JSON requests sent by this service
 * 
 * @author David Pace - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractStatelessBoschSHCServiceWithRequestBody<TRequest extends BoschSHCServiceState>
        extends AbstractStatelessBoschSHCService {

    protected AbstractStatelessBoschSHCServiceWithRequestBody(String serviceName, String endpoint) {
        super(serviceName, endpoint);
    }

    /**
     * Sends a HTTP POST request containing the serialized request body to the endpoint specified by
     * {@link #getEndpoint()}.
     * 
     * @param request a JSON object representing the request body
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public void postAction(TRequest request) throws InterruptedException, TimeoutException, ExecutionException {
        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }

        bridgeHandler.postAction(getEndpoint(), request);
    }
}
