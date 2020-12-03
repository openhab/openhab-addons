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
package org.openhab.binding.panasonictv.internal.handler;

import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.POWER;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panasonictv.internal.api.PanasonicEventListener;
import org.openhab.binding.panasonictv.internal.api.PanasonicTvService;
import org.openhab.binding.panasonictv.internal.config.PanasonicTvConfiguration;
import org.openhab.binding.panasonictv.internal.service.MediaRendererService;
import org.openhab.binding.panasonictv.internal.service.RemoteControllerService;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicTvHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class PanasonicTvHandler extends BaseThingHandler implements PanasonicEventListener {
    private Logger logger = LoggerFactory.getLogger(PanasonicTvHandler.class);

    /* Global configuration for Panasonic TV Thing */
    private PanasonicTvConfiguration configuration = new PanasonicTvConfiguration();

    private final UpnpIOService upnpIOService;

    /* Panasonic TV services */
    private final List<PanasonicTvService> services = new CopyOnWriteArrayList<>();
    private boolean powerState = false;

    public PanasonicTvHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing);

        this.upnpIOService = upnpIOService;
        logger.debug("Create a Panasonic TV Handler for thing '{}'", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        String channel = channelUID.getId();
        // Delegate command to correct service
        services.stream().filter(service -> service.getSupportedChannelNames().contains(channel)).findAny()
                .ifPresent(service -> service.handleCommand(channel, command));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);

        updateState(POWER, OnOffType.from(powerState));
        services.forEach(PanasonicTvService::clearCache);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(PanasonicTvConfiguration.class);

        logger.debug("Initializing Panasonic TV handler for uid '{}' with configuration `{}`", getThing().getUID(),
                configuration);
        if (configuration.mediaRendererUdn.isEmpty() || configuration.remoteControllerUdn.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "UDNs must not be empty.");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        try {
            services.add(new MediaRendererService(scheduler, upnpIOService, configuration.mediaRendererUdn,
                    configuration.refreshInterval, this));
            services.add(new RemoteControllerService(scheduler, upnpIOService, configuration.remoteControllerUdn,
                    configuration.refreshInterval, this));
            services.forEach(PanasonicTvService::start);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Could not initialize services.");
            services.forEach(PanasonicTvService::stop);
        }
    }

    @Override
    public void dispose() {
        shutdown();
        services.clear();
    }

    private void shutdown() {
        logger.debug("Shutdown all Panasonic TV services");
        services.forEach(PanasonicTvService::stop);
    }

    private void setThingAndPowerState(boolean powerState) {
        if (this.powerState != powerState) {
            this.powerState = powerState;
            updateState(POWER, OnOffType.from(powerState));
            updateStatus(powerState ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        }
    }

    @Override
    public void valueReceived(String variable, State value) {
        logger.debug("Received value '{}':'{}' for thing '{}'", variable, value, this.getThing().getUID());
        updateState(variable, value);
        setThingAndPowerState(true);
    }

    @Override
    public void reportError(ThingStatusDetail statusDetail, String message, @Nullable Throwable e) {
        logger.debug("Error was reported: {}", message, e);
        updateStatus(ThingStatus.OFFLINE, statusDetail, message);
    }
}
