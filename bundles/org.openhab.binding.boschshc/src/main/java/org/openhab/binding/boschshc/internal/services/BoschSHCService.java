/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BoschSHCBridgeHandler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Base class of a service of a Bosch Smart Home device.
 * The services of the devices and their official APIs can be found here: https://apidocs.bosch-smarthome.com/local/
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public abstract class BoschSHCService<TState extends BoschSHCServiceState> {
    /**
     * Unique service name
     */
    private final String serviceName;

    /**
     * Class of service state
     */
    private final Class<TState> stateClass;

    /**
     * gson instance to convert a class to json string and back.
     */
    private final Gson gson = new Gson();

    /**
     * Bridge to use for communication from/to the device
     */
    private @Nullable BoschSHCBridgeHandler bridgeHandler;

    /**
     * Id of device the service belongs to
     */
    private @Nullable String deviceId;

    /**
     * Function to call after receiving state updates from the device
     */
    private @Nullable Consumer<TState> stateUpdateListener;

    /**
     * Constructor
     * 
     * @param serviceName Unique name of the service.
     * @param stateClass State class that this service uses for data transfers from/to the device.
     */
    protected BoschSHCService(String serviceName, Class<TState> stateClass) {
        this.serviceName = serviceName;
        this.stateClass = stateClass;
    }

    /**
     * Initializes the service
     * 
     * @param bridgeHandler Bridge to use for communication from/to the device
     * @param deviceId Id of device this service is for
     * @param stateUpdateListener Function to call when a state update was received from the device.
     */
    public void initialize(BoschSHCBridgeHandler bridgeHandler, String deviceId,
            @Nullable Consumer<TState> stateUpdateListener) {
        this.bridgeHandler = bridgeHandler;
        this.deviceId = deviceId;
        this.stateUpdateListener = stateUpdateListener;
    }

    /**
     * Returns the unique name of this service.
     * 
     * @return Unique name of the service.
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * Returns the class of the state this service provides.
     * 
     * @return Class of the state this service provides.
     */
    public Class<TState> getStateClass() {
        return this.stateClass;
    }

    /**
     * Requests the current state of the service and updates it.
     */
    public void refreshState() {
        String deviceId = this.deviceId;
        if (deviceId == null) {
            return;
        }
        BoschSHCBridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler == null) {
            return;
        }

        @Nullable
        TState state = bridgeHandler.getState(deviceId, this.serviceName, this.stateClass);
        if (state != null) {
            this.onStateUpdate(state);
        }
    }

    /**
     * Requests the current state of the device with the specified id.
     * 
     * @return Current state of the device.
     */
    public @Nullable TState getState() {
        String deviceId = this.deviceId;
        if (deviceId == null) {
            return null;
        }
        BoschSHCBridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler == null) {
            return null;
        }
        return bridgeHandler.getState(deviceId, this.serviceName, this.stateClass);
    }

    /**
     * Sets the state of the device with the specified id.
     * 
     * @param state State to set.
     */
    public void setState(TState state) {
        String deviceId = this.deviceId;
        if (deviceId == null) {
            return;
        }
        BoschSHCBridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler == null) {
            return;
        }
        bridgeHandler.putState(deviceId, this.serviceName, state);
    }

    /**
     * A state update was received from the bridge
     * 
     * @param stateData Current state of service. Serialized as JSON.
     */
    public void onStateUpdate(JsonElement stateData) {
        TState state = gson.fromJson(stateData, this.stateClass);
        this.onStateUpdate(state);
    }

    /**
     * A state update was received from the bridge.
     * 
     * @param state Current state of service as an instance of the state class.
     */
    private void onStateUpdate(TState state) {
        Consumer<TState> stateUpdateListener = this.stateUpdateListener;
        if (stateUpdateListener != null) {
            stateUpdateListener.accept(state);
        }
    }
}
