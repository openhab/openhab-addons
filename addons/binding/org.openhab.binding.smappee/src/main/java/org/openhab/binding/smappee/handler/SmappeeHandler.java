/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.handler;

import static org.openhab.binding.smappee.SmappeeBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smappee.internal.CommunicationException;
import org.openhab.binding.smappee.internal.InvalidConfigurationException;
import org.openhab.binding.smappee.internal.ReadingsUpdate;
import org.openhab.binding.smappee.internal.SmappeeConfigurationParameters;
import org.openhab.binding.smappee.internal.SmappeeDeviceReading;
import org.openhab.binding.smappee.internal.SmappeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmappeeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeHandler extends BaseBridgeHandler implements ReadingsUpdate {

    private final Logger logger = LoggerFactory.getLogger(SmappeeHandler.class);
    private SmappeeService smappeeService;

    // private SmappeeDiscoveryService discoveryService;
    // private ServiceRegistration<?> discoveryServiceRegistration;

    public SmappeeHandler(Bridge bridge) {
        super(bridge);
    }

    public SmappeeService getSmappeeService() {
        return smappeeService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (smappeeService == null || !smappeeService.isInitialized()) {
            return;
        }

        if (command instanceof RefreshType) {
            SmappeeDeviceReading readings = smappeeService.getDeviceReadings();
            newState(readings);
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void newState(SmappeeDeviceReading readings) {
        if (readings != null) {
            updateState(CHANNEL_CONSUMPTION, new QuantityType<>(readings.getLatestConsumption(), SmartHomeUnits.WATT));
            updateState(CHANNEL_SOLAR, new QuantityType<>(readings.getLatestSolar(), SmartHomeUnits.WATT));
            updateState(CHANNEL_ALWAYSON, new QuantityType<>(readings.getLatestAlwaysOn(), SmartHomeUnits.WATT));
        }
    }

    @Override
    public void initialize() {
        SmappeeConfigurationParameters config = getConfigAs(SmappeeConfigurationParameters.class);

        if (config.clientId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Client Id must be provided");
            return;
        }
        if (config.clientSecret.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Client secret must be provided");
            return;
        }
        if (config.username.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Username must be provided");
            return;
        }
        if (config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Password must be provided");
            return;
        }
        if (config.serviceLocationName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Service location name must be provided");
            return;
        }

        logger.debug("Initialize Network handler.");

        smappeeService = new SmappeeService(config);

        // contact Smappee API
        scheduler.submit(() -> initializeSmappeeService());
    }

    private void initializeSmappeeService() {
        try {
            smappeeService.initialize();
        } catch (CommunicationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not contact Smappee, retrying");

            // try again in 30 seconds
            scheduler.schedule(() -> initializeSmappeeService(), 30, TimeUnit.SECONDS);

            return;
        } catch (InvalidConfigurationException ex) {
            // bad configuration, stay offline until user corrects the configuration
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());

            return;
        }

        // ok, initialization succeeded
        smappeeService.startAutomaticRefresh(scheduler, this);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        if (smappeeService != null) {
            smappeeService.stopAutomaticRefresh();
        }

        // if (discoveryService != null) {
        // discoveryService.stopScan();
        // unregisterDeviceDiscoveryService();
        // }
    }
    /*
     * private void registerDeviceDiscoveryService() {
     * discoveryService = new SmappeeDiscoveryService(this.smappeeService, this.thing.getUID());
     * discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
     * new Hashtable<String, Object>());
     * discoveryService.activate();
     * }
     *
     * private void unregisterDeviceDiscoveryService() {
     * if (discoveryServiceRegistration != null) {
     * discoveryServiceRegistration.unregister();
     * discoveryServiceRegistration = null;
     * discoveryService.deactivate();
     * discoveryService = null;
     * }
     * }
     */
}
