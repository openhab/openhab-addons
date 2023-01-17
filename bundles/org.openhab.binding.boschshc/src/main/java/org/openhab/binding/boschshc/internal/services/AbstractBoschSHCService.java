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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;

/**
 * Base class for Bosch Smart Home services containing what all services have in common.
 * <p>
 * The services of the devices and their official APIs can be found
 * <a href="https://apidocs.bosch-smarthome.com/local/">here</a>.
 * 
 * @author Christian Oeing - Initial contribution
 * @author David Pace - Service abstraction
 */
@NonNullByDefault
public abstract class AbstractBoschSHCService {

    /**
     * Unique service name
     */
    private final String serviceName;

    /**
     * Bridge to use for communication from/to the device
     */
    private @Nullable BridgeHandler bridgeHandler;

    /**
     * Id of device the service belongs to
     */
    private @Nullable String deviceId;

    protected AbstractBoschSHCService(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Initializes the service
     * 
     * @param bridgeHandler Bridge to use for communication from/to the device
     * @param deviceId Id of device this service is for
     */
    public void initialize(BridgeHandler bridgeHandler, String deviceId) {
        this.bridgeHandler = bridgeHandler;
        this.deviceId = deviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    protected @Nullable BridgeHandler getBridgeHandler() {
        return bridgeHandler;
    }

    protected @Nullable String getDeviceId() {
        return deviceId;
    }
}
