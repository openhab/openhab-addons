/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.handler;

import static org.openhab.binding.smappee.SmappeeBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
//import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smappee.internal.ReadingsUpdate;
import org.openhab.binding.smappee.internal.SmappeeDeviceReading;
import org.openhab.binding.smappee.internal.SmappeeService;
import org.openhab.binding.smappee.internal.discovery.SmappeeDiscoveryService;
import org.osgi.framework.ServiceRegistration;
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
    public SmappeeService smappeeService;

    private SmappeeDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    public SmappeeHandler(Bridge bridge) {
        super(bridge);
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
        updateState(CHANNEL_CONSUMPTION, new DecimalType(readings.getLatestConsumption()));
        updateState(CHANNEL_SOLAR, new DecimalType(readings.getLatestSolar()));
        updateState(CHANNEL_ALWAYSON, new DecimalType(readings.getLatestAlwaysOn()));
    }

    @Override
    public void initialize() {
        Configuration conf = this.getConfig();

        String clientId = String.valueOf(conf.get(PARAMETER_CLIENT_ID));
        String clientSecret = String.valueOf(conf.get(PARAMETER_CLIENT_SECRET));
        String username = String.valueOf(conf.get(PARAMETER_USERNAME));
        String password = String.valueOf(conf.get(PARAMETER_PASSWORD));
        String serviceLocationName = String.valueOf(conf.get(PARAMETER_SERVICE_LOCATION_NAME));
        String pollTime = String.valueOf(conf.get(PARAMETER_POLLTIME));

        if (clientId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Client Id must be provided");
            return;
        }
        if (clientSecret.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Client secret must be provided");
            return;
        }
        if (username.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Username must be provided");
            return;
        }
        if (password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Password must be provided");
            return;
        }
        if (serviceLocationName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Service location name must be provided");
            return;
        }
        if (pollTime.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, polling time must be provided");
            return;
        }

        int pollTimeMSec = 300000; // 5 minutes
        try {
            pollTimeMSec = Integer.parseInt(pollTime) * 60000;
        } catch (NumberFormatException e) {
            logger.warn("Invalid polling time : '{}', taking default of 5 min", pollTime);
        }

        logger.debug("Initialize Network handler.");

        smappeeService = new SmappeeService(clientId, clientSecret, username, password, serviceLocationName,
                pollTimeMSec);

        super.initialize();

        if (smappeeService.initialize()) {

            smappeeService.startAutomaticRefresh(scheduler, this);

            updateStatus(ThingStatus.ONLINE);

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find a smappee with configured service location name '" + serviceLocationName + "'");
        }

        logger.debug("Initialize discovery service.");
        registerDeviceDiscoveryService();
        discoveryService.startScan(null);
    }

    @Override
    public void dispose() {
        smappeeService.stopAutomaticRefresh();

        if (discoveryService != null) {
            discoveryService.stopScan();
            unregisterDeviceDiscoveryService();
        }
    }

    private void registerDeviceDiscoveryService() {
        discoveryService = new SmappeeDiscoveryService(this, this.thing.getUID());
        discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
        discoveryService.activate();
    }

    private void unregisterDeviceDiscoveryService() {
        if (discoveryServiceRegistration != null) {
            if (bundleContext != null) {
                SmappeeDiscoveryService service = (SmappeeDiscoveryService) bundleContext
                        .getService(discoveryServiceRegistration.getReference());
                service.deactivate();
            }
            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryService = null;
        }
    }
}
