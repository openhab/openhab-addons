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
package org.openhab.binding.boschshc.internal.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.AbstractBoschSHCService;
import org.openhab.binding.boschshc.internal.services.AbstractStatelessBoschSHCService;
import org.openhab.binding.boschshc.internal.services.AbstractStatelessBoschSHCServiceWithRequestBody;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * The {@link BoschSHCHandler} represents Bosch Things. Each type of device
 * or system service inherits from this abstract thing handler.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - refactorings of e.g. server registration
 * @author David Pace - Handler abstraction
 */
@NonNullByDefault
public abstract class BoschSHCHandler extends BaseThingHandler {

    /**
     * Service State for a Bosch device.
     */
    class DeviceService<TState extends BoschSHCServiceState> {
        /**
         * Constructor.
         *
         * @param service Service which belongs to the device.
         * @param affectedChannels Channels which are affected by the state of this service.
         */
        public DeviceService(BoschSHCService<TState> service, Collection<String> affectedChannels) {
            this.service = service;
            this.affectedChannels = affectedChannels;
        }

        /**
         * Service which belongs to the device.
         */
        public final BoschSHCService<TState> service;

        /**
         * Channels which are affected by the state of this service.
         */
        public final Collection<String> affectedChannels;
    }

    /**
     * Reusable gson instance to convert a class to json string and back in derived classes.
     */
    protected static final Gson GSON = new Gson();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Services of the device.
     */
    private List<DeviceService<? extends BoschSHCServiceState>> services = new ArrayList<>();

    protected BoschSHCHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns the unique id of the Bosch device or service.
     * <p>
     * For physical devices, the ID looks like
     *
     * <pre>
     * hdm:Cameras:d20354de-44b5-3acc-924c-24c98d59da42
     * hdm:ZigBee:000d6f0016d1c087
     * </pre>
     *
     * For virtual devices / services, static IDs like the following are used:
     *
     * <pre>
     * ventilationService
     * smokeDetectionSystem
     * intrusionDetectionSystem
     * </pre>
     *
     * @return Unique ID of the Bosch device or service.
     */
    public abstract @Nullable String getBoschID();

    /**
     * Initializes this handler. Use this method to register all services of the device with
     * {@link #registerService(BoschSHCService)}.
     */
    @Override
    public void initialize() {

        // Initialize device services
        try {
            this.initializeServices();
        } catch (BoschSHCException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        this.updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Handles the refresh command of all registered services. Override it to handle custom commands (e.g. to update
     * states of services).
     *
     * @param channelUID {@link ChannelUID} of the channel to which the command was sent
     * @param command {@link Command}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Refresh state of services that affect the channel
            for (DeviceService<? extends BoschSHCServiceState> deviceService : this.services) {
                if (deviceService.affectedChannels.contains(channelUID.getIdWithoutGroup())) {
                    this.refreshServiceState(deviceService.service);
                }
            }
        }
    }

    /**
     * Processes an update which is received from the bridge.
     *
     * @param serviceName Name of service the update came from.
     * @param stateData Current state of device service. Serialized as JSON.
     */
    public void processUpdate(String serviceName, @Nullable JsonElement stateData) {
        // Check services of device to correctly
        for (DeviceService<? extends BoschSHCServiceState> deviceService : this.services) {
            BoschSHCService<? extends BoschSHCServiceState> service = deviceService.service;
            if (serviceName.equals(service.getServiceName())) {
                service.onStateUpdate(stateData);
            }
        }
    }

    /**
     * Should be used by handlers to create their required services.
     */
    protected void initializeServices() throws BoschSHCException {
    }

    /**
     * Returns the bridge handler for this thing handler.
     *
     * @return Bridge handler for this thing handler. Null if no or an invalid bridge was set in the configuration.
     * @throws BoschSHCException If bridge for handler is not set or an invalid bridge is set.
     */
    protected BridgeHandler getBridgeHandler() throws BoschSHCException {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            throw new BoschSHCException(String.format("No valid bridge set for %s (%s)", this.getThing().getLabel(),
                    this.getThing().getUID().getAsString()));
        }
        BridgeHandler bridgeHandler = (BridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            throw new BoschSHCException(String.format("Bridge of %s (%s) has no valid bridge handler",
                    this.getThing().getLabel(), this.getThing().getUID().getAsString()));
        }
        return bridgeHandler;
    }

    /**
     * Query the Bosch Smart Home Controller for the state of the service with the specified name.
     *
     * @note Use services instead of directly requesting a state.
     *
     * @param stateName Name of the service to query
     * @param classOfT Class to convert the resulting JSON to
     */
    protected <T extends BoschSHCServiceState> @Nullable T getState(String stateName, Class<T> classOfT) {
        String deviceId = this.getBoschID();
        if (deviceId == null) {
            return null;
        }
        try {
            BridgeHandler bridgeHandler = this.getBridgeHandler();
            return bridgeHandler.getState(deviceId, stateName, classOfT);
        } catch (TimeoutException | ExecutionException | BoschSHCException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error when trying to refresh state from service %s: %s", stateName, e.getMessage()));
            return null;
        } catch (InterruptedException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Interrupted refresh state from service %s: %s", stateName, e.getMessage()));
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Creates and registers a new service for this device.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of service state.
     * @param newService Supplier function to create a new instance of the service.
     * @param stateUpdateListener Function to call when a state update was received
     *            from the device.
     * @param affectedChannels Channels which are affected by the state of this
     *            service.
     * @return Instance of registered service.
     * @throws BoschSHCException
     */
    protected <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> TService createService(
            Supplier<TService> newService, Consumer<TState> stateUpdateListener, Collection<String> affectedChannels)
            throws BoschSHCException {
        return createService(newService, stateUpdateListener, affectedChannels, false);
    }

    /**
     * Creates and registers a new service for this device.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of service state.
     * @param newService Supplier function to create a new instance of the service.
     * @param stateUpdateListener Function to call when a state update was received
     *            from the device.
     * @param affectedChannels Channels which are affected by the state of this
     *            service.
     * @param shouldFetchInitialState indicates whether the initial state should be actively requested from the device
     *            or service. Useful if state updates are not included in long poll results.
     * @return Instance of registered service.
     * @throws BoschSHCException
     */
    protected <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> TService createService(
            Supplier<TService> newService, Consumer<TState> stateUpdateListener, Collection<String> affectedChannels,
            boolean shouldFetchInitialState) throws BoschSHCException {
        TService service = newService.get();
        this.registerService(service, stateUpdateListener, affectedChannels, shouldFetchInitialState);
        return service;
    }

    /**
     * Registers a service for this device.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of service state.
     * @param service Service to register.
     * @param stateUpdateListener Function to call when a state update was received
     *            from the device.
     * @param affectedChannels Channels which are affected by the state of this
     *            service.
     * @throws BoschSHCException If bridge for handler is not set or an invalid bridge is set.
     * @throws BoschSHCException If no device id is set.
     */
    protected <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> void registerService(
            TService service, Consumer<TState> stateUpdateListener, Collection<String> affectedChannels)
            throws BoschSHCException {
        registerService(service, stateUpdateListener, affectedChannels, false);
    }

    /**
     * Registers a service for this device.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of service state.
     * @param service Service to register.
     * @param stateUpdateListener Function to call when a state update was received
     *            from the device.
     * @param affectedChannels Channels which are affected by the state of this
     *            service.
     * @param shouldFetchInitialState indicates whether the initial state should be actively requested from the device
     *            or service. Useful if state updates are not included in long poll results.
     * @throws BoschSHCException If bridge for handler is not set or an invalid bridge is set.
     * @throws BoschSHCException If no device id is set.
     */
    protected <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> void registerService(
            TService service, Consumer<TState> stateUpdateListener, Collection<String> affectedChannels,
            boolean shouldFetchInitialState) throws BoschSHCException {

        String deviceId = verifyBoschID();
        service.initialize(getBridgeHandler(), deviceId, stateUpdateListener);
        this.registerService(service, affectedChannels);

        if (shouldFetchInitialState) {
            fetchInitialState(service, stateUpdateListener);
        }
    }

    /**
     * Actively requests the initial state for the given service. This is required if long poll results do not contain
     * status updates for the given service.
     *
     * @param <TService> Type of the service for which the state should be obtained
     * @param <TState> Type of the objects to serialize and deserialize the service state
     * @param service Service for which the state should be requested
     * @param stateUpdateListener Function to process the obtained state
     */
    private <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> void fetchInitialState(
            TService service, Consumer<TState> stateUpdateListener) {

        try {
            @Nullable
            TState serviceState = service.getState();
            if (serviceState != null) {
                stateUpdateListener.accept(serviceState);
            }
        } catch (TimeoutException | ExecutionException | BoschSHCException e) {
            logger.debug("Could not retrieve the initial state for service {} of device {}", service.getServiceName(),
                    getBoschID());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Could not retrieve the initial state for service {} of device {}", service.getServiceName(),
                    getBoschID());
        }
    }

    /**
     * Registers a write-only service that does not receive states from the bridge.
     * <p>
     * Examples for such services are the actions of the intrusion detection service.
     *
     * @param <TService> Type of service.
     * @param service Service to register.
     * @throws BoschSHCException If no device ID is set.
     */
    protected <TService extends AbstractBoschSHCService> void registerStatelessService(TService service)
            throws BoschSHCException {

        String deviceId = verifyBoschID();
        service.initialize(getBridgeHandler(), deviceId);
        // do not register in service list because the service can not receive state updates
    }

    /**
     * Verifies that a Bosch device or service ID is set and throws an exception if this is not the case.
     *
     * @return the Bosch ID, if present
     * @throws BoschSHCException if no Bosch ID is set
     */
    private String verifyBoschID() throws BoschSHCException {
        String deviceId = this.getBoschID();
        if (deviceId == null) {
            throw new BoschSHCException(
                    String.format("Could not register service for %s, no device id set", this.getThing()));
        }
        return deviceId;
    }

    /**
     * Updates the state of a device service.
     * Sets the status of the device to offline if setting the state fails.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of service state.
     * @param service Service to set state for.
     * @param state State to set.
     */
    protected <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> void updateServiceState(
            TService service, TState state) {
        try {
            service.setState(state);
        } catch (TimeoutException | ExecutionException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, String.format(
                    "Error when trying to update state for service %s: %s", service.getServiceName(), e.getMessage()));
        } catch (InterruptedException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, String
                    .format("Interrupted update state for service %s: %s", service.getServiceName(), e.getMessage()));
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Lets a service handle a received command.
     * Sets the status of the device to offline if handling the command fails.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of service state.
     * @param service Service which should handle command.
     * @param command Command to handle.
     */
    protected <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> void handleServiceCommand(
            TService service, Command command) {
        try {
            if (command instanceof RefreshType) {
                this.refreshServiceState(service);
            } else {
                TState state = service.handleCommand(command);
                this.updateServiceState(service, state);
            }
        } catch (BoschSHCException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error when service %s should handle command %s: %s", service.getServiceName(),
                            command.getClass().getName(), e.getMessage()));
        }
    }

    /**
     * Requests a service to refresh its state.
     * Sets the device offline if request fails.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of service state.
     * @param service Service to refresh state for.
     */
    private <TService extends BoschSHCService<TState>, TState extends BoschSHCServiceState> void refreshServiceState(
            TService service) {
        try {
            service.refreshState();
        } catch (TimeoutException | ExecutionException | BoschSHCException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error when trying to refresh state from service %s: %s", service.getServiceName(),
                            e.getMessage()));
        } catch (InterruptedException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, String
                    .format("Interrupted refresh state from service %s: %s", service.getServiceName(), e.getMessage()));
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Registers a service of this device.
     *
     * @param service Service which belongs to this device
     * @param affectedChannels Channels which are affected by the state of this
     *            service
     */
    private <TState extends BoschSHCServiceState> void registerService(BoschSHCService<TState> service,
            Collection<String> affectedChannels) {
        this.services.add(new DeviceService<TState>(service, affectedChannels));
    }

    /**
     * Sends a HTTP POST request with empty body.
     *
     * @param <TService> Type of service.
     * @param service Service implementing the action
     */
    protected <TService extends AbstractStatelessBoschSHCService> void postAction(TService service) {
        try {
            service.postAction();
        } catch (ExecutionException | TimeoutException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error while triggering action %s", service.getEndpoint()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error while triggering action %s", service.getEndpoint()));
        }
    }

    /**
     * Sends a HTTP POST request with the given request body.
     *
     * @param <TService> Type of service.
     * @param <TState> Type of the request to be sent.
     * @param service Service implementing the action
     * @param request Request object to be serialized to JSON
     */
    protected <TService extends AbstractStatelessBoschSHCServiceWithRequestBody<TState>, TState extends BoschSHCServiceState> void postAction(
            TService service, TState request) {
        try {
            service.postAction(request);
        } catch (ExecutionException | TimeoutException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error while triggering action %s", service.getEndpoint()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error while triggering action %s", service.getEndpoint()));
        }
    }
}
