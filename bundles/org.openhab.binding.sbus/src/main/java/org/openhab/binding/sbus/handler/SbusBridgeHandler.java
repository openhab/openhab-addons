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
package org.openhab.binding.sbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sbus.handler.config.SbusBridgeConfig;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SbusBridgeHandler} is responsible for handling communication with the Sbus bridge.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusBridgeHandler.class);
    private final @Nullable SbusService sbusService;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;

    /**
     * Constructs a new SbusBridgeHandler.
     *
     * @param bridge the bridge
     * @param sbusService the Sbus service
     */
    public SbusBridgeHandler(Bridge bridge, @Nullable SbusService sbusService, TranslationProvider translationProvider,
            LocaleProvider localeProvider) {
        super(bridge);
        this.sbusService = sbusService;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
    }

    /**
     * Initializes the Sbus bridge handler by establishing a connection to the Sbus network.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing Sbus bridge handler for bridge {}", getThing().getUID());

        // Get configuration using the config class
        SbusBridgeConfig config = getConfigAs(SbusBridgeConfig.class);
        if (config.host.isBlank()) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.bridge.host-not-configured", null, localeProvider.getLocale()));
            return;
        }
        try {
            // Initialize Sbus service with the configuration parameters
            final SbusService service = sbusService;
            if (service == null) {
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, translationProvider
                        .getText(bundle, "error.bridge.service-not-available", null, localeProvider.getLocale()));
                return;
            }
            service.initialize(config.host, config.port);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Gets the Sbus service.
     *
     * @return the Sbus service
     */
    public @Nullable SbusService getSbusConnection() {
        return sbusService;
    }

    /**
     * Disposes the handler by closing the Sbus connection.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing Sbus bridge handler");
        final SbusService service = sbusService;
        if (service != null) {
            service.close();
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Bridge doesn't handle commands directly
    }
}
