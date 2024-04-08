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
                deviceDiscoveryService.discoverDevices();
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
        config = getConfigAs(OnectaConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                String refreshToken = thing.getConfiguration().get(CHANNEL_REFRESH_TOKEN) == null ? ""
                        : thing.getConfiguration().get(CHANNEL_REFRESH_TOKEN).toString();
                OnectaConnectionClient.startConnecton(thing.getConfiguration().get(CHANNEL_USERID).toString(),
                        thing.getConfiguration().get(CHANNEL_PASSWORD).toString(), refreshToken);

                if (OnectaConnectionClient.isOnline()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            } catch (DaikinCommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevices, 10,
                Integer.parseInt(thing.getConfiguration().get(CHANNEL_REFRESHINTERVAL).toString()), TimeUnit.SECONDS);

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
        if (OnectaConnectionClient.isOnline()) {
            updateStatus(ThingStatus.ONLINE);

            getThing().getConfiguration().put(CHANNEL_REFRESH_TOKEN, OnectaConnectionClient.getRefreshToken());

        } else {
            if (getThing().getStatus() != ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
        try {
            OnectaConnectionClient.refreshUnitsData(getThing());
        } catch (DaikinCommunicationException e) {
            logger.debug("DaikinCommunicationException: " + e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            // BaseThingHandler handler;
            if (t.getStatus() == ThingStatus.ONLINE) {

                if (t.getThingTypeUID().equals(DEVICE_THING_TYPE)) {
                    OnectaDeviceHandler onectaDeviceHandler = (OnectaDeviceHandler) t.getHandler();
                    onectaDeviceHandler.refreshDevice();
                } else if (t.getThingTypeUID().equals(GATEWAY_THING_TYPE)) {
                    OnectaGatewayHandler onectaGatewayHandler = (OnectaGatewayHandler) t.getHandler();
                    onectaGatewayHandler.refreshDevice();
                } else if (t.getThingTypeUID().equals(WATERTANK_THING_TYPE)) {
                    OnectaWaterTankHandler onectaWaterTankHandler = (OnectaWaterTankHandler) t.getHandler();
                    onectaWaterTankHandler.refreshDevice();
                } else if (t.getThingTypeUID().equals(INDOORUNIT_THING_TYPE)) {
                    OnectaIndoorUnitHandler onectaIndoorUnitHandler = (OnectaIndoorUnitHandler) t.getHandler();
                    onectaIndoorUnitHandler.refreshDevice();
                } else
                    continue;
            }
        }
    }

    public void setDiscovery(DeviceDiscoveryService deviceDiscoveryService) {
        this.deviceDiscoveryService = deviceDiscoveryService;
    }
}
