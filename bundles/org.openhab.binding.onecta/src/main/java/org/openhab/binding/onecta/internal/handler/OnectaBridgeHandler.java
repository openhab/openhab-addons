/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.onecta.internal.constants.OnectaBridgeConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
import org.openhab.core.thing.binding.ThingHandlerService;
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

    private @Nullable ScheduledFuture<?> pollingJob;

    private Units units = new Units();
    private OnectaConnectionClient onectaConnectionClient;

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
        onectaConnectionClient = OnectaConfiguration.getOnectaConnectionClient();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("initialize.");
        updateStatus(ThingStatus.UNKNOWN);
        if (onectaConnectionClient.isOnline()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.communication-error");
        }

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevices, 0,
                Integer.parseInt(thing.getConfiguration().get(CONFIG_PAR_REFRESHINTERVAL).toString()),
                TimeUnit.SECONDS);
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

        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {

            try {
                onectaConnectionClient.refreshUnitsData();
                updateStatus(ThingStatus.ONLINE);

                List<Thing> things = getThing().getThings();
                for (Thing t : things) {
                    if (t.isEnabled()) {
                        ((AbstractOnectaHandler) Objects.requireNonNull(t.getHandler())).refreshDevice();
                    }
                }
            } catch (DaikinCommunicationException e) {
                logger.debug("DaikinCommunicationException: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (NullPointerException e) {
                logger.debug("NullPointerException: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, e.getMessage());
            }
        }
    }

    public void setDiscoveryService(DeviceDiscoveryService deviceDiscoveryService) {
        this.deviceDiscoveryService = deviceDiscoveryService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(DeviceDiscoveryService.class);
    }
}
