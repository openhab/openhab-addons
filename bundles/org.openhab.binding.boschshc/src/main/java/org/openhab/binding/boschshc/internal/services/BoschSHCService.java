package org.openhab.binding.boschshc.internal.services;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.BoschSHCBridgeHandler;

public abstract class BoschSHCService<TState extends Object> {

    /**
     * Bridge to communicate
     */
    private final @NonNull BoschSHCBridgeHandler bridgeHandler;

    /**
     * Unique service name
     */
    private final String serviceName;

    /**
     * Class of service state
     */
    private final Class<TState> stateClass;

    protected BoschSHCService(BoschSHCBridgeHandler bridgeHandler, String serviceName, Class<TState> stateClass) {
        this.bridgeHandler = bridgeHandler;
        this.serviceName = serviceName;
        this.stateClass = stateClass;
    }

    public @Nullable TState getState(String deviceId) {
        return this.bridgeHandler.getState(deviceId, this.serviceName, this.stateClass);
    }

    public void setState(String deviceId, @NonNull TState state) {
        this.bridgeHandler.putState(deviceId, this.serviceName, state);
    }
}