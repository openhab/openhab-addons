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

/**
 * Abstract implementation for services that only allow setting states via HTTP POST requests.
 * State-less services can not receive any states from the bridge.
 * <p>
 * This implementation does not support request bodies when submitting the POST request.
 * Request bodies are supported by the subclass {@link AbstractStatelessBoschSHCServiceWithRequestBody}.
 * <p>
 * Examples for this kind of service are the following actions of the intrusion detection system:
 * 
 * <pre>
 * /intrusion/actions/arm
 * /intrusion/actions/disarm
 * /intrusion/actions/mute
 * </pre>
 * <p>
 * The services of the devices and their official APIs can be found
 * <a href="https://apidocs.bosch-smarthome.com/local/">here</a>.
 * 
 * @author David Pace - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractStatelessBoschSHCService extends AbstractBoschSHCService {

    private String endpoint;

    protected AbstractStatelessBoschSHCService(String serviceName, String endpoint) {
        super(serviceName);
        this.endpoint = endpoint;
    }

    /**
     * Sends a HTTP POST request without request body to the endpoint specified by {@link #endpoint}.
     * 
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public void postAction() throws InterruptedException, TimeoutException, ExecutionException {
        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null)
            return;

        bridgeHandler.postAction(endpoint);
    }

    public String getEndpoint() {
        return endpoint;
    }
}
