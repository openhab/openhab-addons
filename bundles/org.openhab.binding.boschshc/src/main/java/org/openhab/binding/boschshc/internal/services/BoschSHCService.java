package org.openhab.binding.boschshc.internal.services;

import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.BoschSHCBridgeHandler;

/**
 * Base class of a service of a Bosch Smart Home device.
 * The services of the devices and their official APIs can be found here: https://apidocs.bosch-smarthome.com/local/
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
     * Bridge to use for communication from/to the device
     */
    @NonNullByDefault({})
    private BoschSHCBridgeHandler bridgeHandler;

    /**
     * Id of device the service belongs to
     */
    @NonNullByDefault({})
    private String deviceId;

    /**
     * Function to call after receiving state updates from the device
     */
    @Nullable
    private Consumer<TState> stateUpdateListener;

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
        TState state = this.bridgeHandler.getState(deviceId, this.serviceName, this.stateClass);
        this.onStateUpdate(state);
    }

    /**
     * Requests the current state of the device with the specified id.
     * 
     * @return Current state of the device.
     */
    public @Nullable TState getState() {
        return this.bridgeHandler.getState(this.deviceId, this.serviceName, this.stateClass);
    }

    /**
     * Sets the state of the device with the specified id.
     * 
     * @param deviceId Id of device to set state for.
     * @param state State to set.
     */
    public void setState(TState state) {
        this.bridgeHandler.putState(this.deviceId, this.serviceName, state);
    }

    /**
     * A state update was received from the bridge
     * 
     * @param stateData Current state of service. Serialized as JSON.
     */
    public void onStateUpdate(JsonElement stateData) {
        Gson gson = new Gson();
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
