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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatus.UNKNOWN;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.internal.UniFiControllerThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiCommunicationException;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidCredentialsException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidHostException;
import org.openhab.binding.unifi.internal.api.UniFiSSLException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiControllerThingHandler} is responsible for handling commands and status
 * updates for the UniFi Controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiControllerThingHandler extends BaseBridgeHandler {

    private static final String STATUS_DESCRIPTION_COMMUNICATION_ERROR = "@text/error.bridge.offline.communication_error";
    private static final String STATUS_DESCRIPTION_SSL_ERROR = "@text/error.bridge.offline.ssl_error";
    private static final String STATUS_DESCRIPTION_INVALID_CREDENTIALS = "@text/error.bridge.offline.invalid_credentials";
    private static final String STATUS_DESCRIPTION_INVALID_HOSTNAME = "@text/error.bridge.offline.invalid_hostname";
    private static final String I18N_STATUS_WITH_ARGUMENTS = "%s [\"%s\"]";

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerThingHandler.class);

    private UniFiControllerThingConfig config = new UniFiControllerThingConfig();

    private @Nullable volatile UniFiController controller; /* mgb: volatile because accessed from multiple threads */

    private @Nullable ScheduledFuture<?> refreshJob;

    private final HttpClient httpClient;

    public UniFiControllerThingHandler(final Bridge bridge, final HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    // Public API

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(UniFiThingDiscoveryService.class);
    }

    @Override
    public void initialize() {
        config = getConfigAs(UniFiControllerThingConfig.class);
        logger.debug("Initializing the UniFi Controller Handler with config = {}", config);
        final UniFiController uc = new UniFiController(httpClient, config.getHost(), config.getPort(),
                config.getUsername(), config.getPassword(), config.isUniFiOS(), config.getTimeoutSeconds());

        controller = uc;
        updateStatus(UNKNOWN);
        scheduler.schedule(() -> start(uc), 10, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void updateStatus(final ThingStatus status, final ThingStatusDetail statusDetail,
            @Nullable final String description) {
        // mgb: update the status only if it's changed
        final ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(status, statusDetail)
                .withDescription(description).build();
        if (!statusInfo.equals(getThing().getStatusInfo())) {
            super.updateStatus(status, statusDetail, description);
        }
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        final UniFiController controller = this.controller;

        if (controller != null) {
            try {
                controller.stop();
            } catch (final UniFiException e) {
                // mgb: nop as we're in dispose
            }
            this.controller = null;
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // nop - read-only binding
        logger.warn("Ignoring command = {} for channel = {} - the UniFi binding is read-only!", command, channelUID);
    }

    public @Nullable UniFiController getController() {
        return controller;
    }

    // Private API

    private void start(final UniFiController uc) {
        boolean startRefresh = false;
        try {
            uc.start();
            startRefresh = true;
        } catch (final UniFiCommunicationException e) {
            updateStatusOffline(COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR, e.getMessage());
            startRefresh = true;
        } catch (final UniFiInvalidHostException e) {
            updateStatusOffline(CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_HOSTNAME, e.getMessage());
        } catch (final UniFiSSLException e) {
            updateStatusOffline(CONFIGURATION_ERROR, STATUS_DESCRIPTION_SSL_ERROR, e.getMessage());
        } catch (final UniFiInvalidCredentialsException e) {
            updateStatusOffline(CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_CREDENTIALS, e.getMessage());
        } catch (final UniFiException e) {
            logger.debug("Unknown error while configuring the UniFi Controller", e);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, e.getMessage());
        }
        if (startRefresh) {
            logger.debug("Scheduling refresh job every {}s", config.getRefresh());
            refreshJob = scheduler.scheduleWithFixedDelay(this::run, 0, config.getRefresh(), TimeUnit.SECONDS);
        }
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            final ScheduledFuture<?> rj = refreshJob;

            if (rj != null) {
                logger.debug("Cancelling refresh job");
                rj.cancel(true);
                refreshJob = null;
            }
        }
    }

    private void run() {
        try {
            logger.trace("Executing refresh job");
            refresh();
            updateStatus(ONLINE);
        } catch (final UniFiCommunicationException e) {
            updateStatusOffline(COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR, e.getMessage());
        } catch (final UniFiInvalidCredentialsException e) {
            updateStatusOffline(CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_CREDENTIALS, e.getMessage());
        } catch (final RuntimeException | UniFiException e) {
            logger.debug("Unhandled exception while refreshing the UniFi Controller {}", getThing().getUID(), e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateStatusOffline(final ThingStatusDetail thingStatusDetail, final String i18nKey,
            final @Nullable String argument) {
        updateStatus(OFFLINE, thingStatusDetail, String.format(I18N_STATUS_WITH_ARGUMENTS, i18nKey, argument));
    }

    private void refresh() throws UniFiException {
        final UniFiController uc = controller;

        if (uc != null) {
            logger.debug("Refreshing the UniFi Controller {}", getThing().getUID());
            uc.refresh();
            // mgb: then refresh all the client things
            getThing().getThings().forEach((thing) -> {
                final ThingHandler handler = thing.getHandler();

                if (handler instanceof UniFiBaseThingHandler baseThingHandler) {
                    baseThingHandler.refresh();
                }
            });
        }
    }
}
