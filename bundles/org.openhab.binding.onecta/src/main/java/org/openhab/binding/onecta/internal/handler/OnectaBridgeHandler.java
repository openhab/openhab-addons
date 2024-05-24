/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.handler;

import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.Units;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.binding.onecta.internal.service.DeviceDiscoveryService;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnectaBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OnectaBridgeHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private Units units = new Units();
    private final OnectaConnectionClient onectaConnectionClient = new OnectaConnectionClient();

    public Units getUnits() {
        return units;
    }

    private @Nullable DeviceDiscoveryService deviceDiscoveryService;

    /**
     * Defines a runnable for a discovery
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (deviceDiscoveryService != null) {
                deviceDiscoveryService.startScan();
            }
        }
    };

    public OnectaBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("initialize.");
        config = getConfigAs(OnectaConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        try {
            onectaConnectionClient.startConnecton(thing.getConfiguration().get(CONFIG_PAR_USERID).toString(),
                    thing.getConfiguration().get(CONFIG_PAR_PASSWORD).toString());

            if (onectaConnectionClient.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (DaikinCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevices, 10,
                Integer.parseInt(thing.getConfiguration().get(CONFIG_PAR_REFRESHINTERVAL).toString()),
                TimeUnit.SECONDS);

        // Trigger discovery of Devices
        scheduler.submit(runnable);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    private void pollDevices() {
        logger.debug("pollDevices.");

        if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
            try {
                logger.debug("Try to restore connection ");
                onectaConnectionClient.restoreConnecton();

                if (onectaConnectionClient.isOnline()) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (DaikinCommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Try to restore connection. See log for more information. ");
            }
        }

        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {

            try {
                onectaConnectionClient.refreshUnitsData();
            } catch (DaikinCommunicationException e) {
                logger.debug("DaikinCommunicationException: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            List<Thing> things = getThing().getThings();
            for (Thing t : things) {
                // BaseThingHandler handler;
                if (t.getStatus().equals(ThingStatus.ONLINE)) {

                    if (t.getThingTypeUID().equals(THING_TYPE_CLIMATECONTROL)) {
                        OnectaDeviceHandler onectaDeviceHandler = (OnectaDeviceHandler) t.getHandler();
                        onectaDeviceHandler.refreshDevice();
                    } else if (t.getThingTypeUID().equals(THING_TYPE_GATEWAY)) {
                        OnectaGatewayHandler onectaGatewayHandler = (OnectaGatewayHandler) t.getHandler();
                        onectaGatewayHandler.refreshDevice();
                    } else if (t.getThingTypeUID().equals(THING_TYPE_WATERTANK)) {
                        OnectaWaterTankHandler onectaWaterTankHandler = (OnectaWaterTankHandler) t.getHandler();
                        onectaWaterTankHandler.refreshDevice();
                    } else if (t.getThingTypeUID().equals(THING_TYPE_INDOORUNIT)) {
                        OnectaIndoorUnitHandler onectaIndoorUnitHandler = (OnectaIndoorUnitHandler) t.getHandler();
                        onectaIndoorUnitHandler.refreshDevice();
                    } else
                        continue;
                }
            }
        }
    }

    public void setDiscovery(DeviceDiscoveryService deviceDiscoveryService) {
        this.deviceDiscoveryService = deviceDiscoveryService;
    }
}
