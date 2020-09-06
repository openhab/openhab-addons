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
package org.openhab.binding.boschshc.internal.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.boschshc.internal.devices.bridge.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * The {@link BoschSHCHandler} represents Bosch Things. Each type of device
 * inherits from this abstract thing handler.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - refactorings of e.g. server registration
 */
@NonNullByDefault
public abstract class BoschSHCHandler extends BaseThingHandler {

    /**
     * Service State for a Bosch device.
     */
    @NonNullByDefault
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
        public BoschSHCService<TState> service;

        /**
         * Channels which are affected by the state of this service.
         */
        public Collection<String> affectedChannels;
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Reusable gson instance to convert a class to json string and back in derived classes.
     */
    protected Gson gson = new Gson();

    /**
     * Bosch SHC configuration loaded from openHAB configuration.
     */
    private @Nullable BoschSHCConfiguration config;

    /**
     * Services of the device.
     */
    private List<DeviceService<? extends BoschSHCServiceState>> services = new ArrayList<>();

    public BoschSHCHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns the unique id of the Bosch device.
     * 
     * @return Unique id of the Bosch device.
     */
    public @Nullable String getBoschID() {
        BoschSHCConfiguration config = this.config;
        if (config != null) {
            return config.id;
        } else {
            return null;
        }
    }

    /**
     * Initializes this handler. Use this method to register all services of the device with
     * {@link #registerService(BoschSHCService)}.
     */
    @Override
    public void initialize() {
        this.config = getConfigAs(BoschSHCConfiguration.class);

        try {
            this.initializeServices();

            // Mark immediately as online - if the bridge is online, the thing is too.
            this.updateStatus(ThingStatus.ONLINE);
        } catch (BoschSHCException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
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
                    deviceService.service.refreshState();
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
    public void processUpdate(String serviceName, JsonElement stateData) {
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
    protected BoschSHCBridgeHandler getBridgeHandler() throws BoschSHCException {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            throw new BoschSHCException(String.format("No valid bridge set for {}", this.getThing()));
        }
        BoschSHCBridgeHandler bridgeHandler = (BoschSHCBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            throw new BoschSHCException(String.format("Bridge of {} has no valid bridge handler", this.getThing()));
        }
        return bridgeHandler;
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
        BoschSHCBridgeHandler bridgeHandler = this.getBridgeHandler();

        String deviceId = this.getBoschID();
        if (deviceId == null) {
            throw new Error(String.format("Could not create service for {}, no device id set", this.getThing()));
        }

        TService service = newService.get();
        service.initialize(bridgeHandler, deviceId, stateUpdateListener);
        this.registerService(service, affectedChannels);

        return service;
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
}
