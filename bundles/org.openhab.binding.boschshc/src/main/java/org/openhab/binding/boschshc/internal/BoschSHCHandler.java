/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonElement;

class DeviceService<TState extends BoschSHCServiceState> {
    /**
     * Service which belongs to the device.
     */
    public BoschSHCService<TState> service;

    /**
     * Channels which are affected by the state of this service.
     */
    public Collection<String> affectedChannels;
}

/**
 * The {@link BoschSHCHandler} represents Bosch Things. Each type of device
 * inherits from this abstract thing handler.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public abstract class BoschSHCHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(BoschSHCHandler.class);
    private @Nullable BoschSHCConfiguration config;

    /**
     * Services of the device.
     */
    private List<DeviceService<? extends BoschSHCServiceState>> services = new ArrayList<DeviceService<? extends BoschSHCServiceState>>();

    public BoschSHCHandler(Thing thing) {
        super(thing);
        logger.warn("Creating thing: {}", thing.getLabel());
    }

    public @Nullable String getBoschID() {
        if (this.config != null) {
            return this.config.id;
        } else {
            return null;
        }
    }

    public @Nullable BoschSHCConfiguration getBoschConfig() {
        return this.config;
    }

    /**
     * Initializes this handler. Use this method to register all services of the
     * device with {@link #registerService(BoschSHCService)}.
     */
    @Override
    public void initialize() {

        this.config = getConfigAs(BoschSHCConfiguration.class);
        logger.warn("Initializing thing: {}", this.config.id);

        // Mark immediately as online - if the bridge is online, the thing is too.
        updateStatus(ThingStatus.ONLINE);
    }

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
     * @param stateData   Current state of device service. Serialized as JSON.
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

    protected @Nullable BoschSHCBridgeHandler getBridgeHandler() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            return null;
        }
        return (BoschSHCBridgeHandler) bridge.getHandler();
    }

    /**
     * Registers a service of this device.
     * 
     * @param service          Service which belongs to this device
     * @param affectedChannels Channels which are affected by the state of this
     *                         service
     */
    protected <TState extends BoschSHCServiceState> void registerService(BoschSHCService<TState> service,
            Collection<String> affectedChannels) {
        DeviceService<TState> deviceService = new DeviceService<TState>();
        deviceService.service = service;
        deviceService.affectedChannels = affectedChannels;
        this.services.add(deviceService);
    }
}
