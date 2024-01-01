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
package org.openhab.binding.foobot.internal.handler;

import static org.openhab.binding.foobot.internal.FoobotBindingConstants.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.foobot.internal.FoobotApiConnector;
import org.openhab.binding.foobot.internal.FoobotApiException;
import org.openhab.binding.foobot.internal.FoobotBindingConstants;
import org.openhab.binding.foobot.internal.config.FoobotAccountConfiguration;
import org.openhab.binding.foobot.internal.discovery.FoobotAccountDiscoveryService;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler to manage Foobot Account
 *
 * @author George Katsis - Initial contribution
 * @author Hilbrand Bouwkamp - Completed implementation
 */
@NonNullByDefault
public class FoobotAccountHandler extends BaseBridgeHandler {

    /*
     * Set the exact interval a little lower to compensate for the time it takes to get the new data.
     */
    private static final long DEVICES_INTERVAL_MINUTES = Duration.ofDays(1).minus(Duration.ofMinutes(1)).toMinutes();
    private static final Duration SENSOR_INTERVAL_OFFSET_SECONDS = Duration.ofSeconds(15);

    private final Logger logger = LoggerFactory.getLogger(FoobotAccountHandler.class);

    private final FoobotApiConnector connector;

    private String username = "";
    private int refreshInterval;
    private @Nullable ScheduledFuture<?> refreshDeviceListJob;
    private @Nullable ScheduledFuture<?> refreshSensorsJob;
    private @NonNullByDefault({}) ExpiringCache<List<FoobotDeviceHandler>> dataCache;

    public FoobotAccountHandler(Bridge bridge, FoobotApiConnector connector) {
        super(bridge);
        this.connector = connector;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(FoobotAccountDiscoveryService.class);
    }

    public List<FoobotDevice> getDeviceList() throws FoobotApiException {
        return connector.getAssociatedDevices(username);
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public void initialize() {
        final FoobotAccountConfiguration accountConfig = getConfigAs(FoobotAccountConfiguration.class);
        final List<String> missingParams = new ArrayList<>();

        String apiKey = accountConfig.apiKey;
        if (apiKey.isBlank()) {
            missingParams.add("'apikey'");
        }
        String username = accountConfig.username;
        if (username.isBlank()) {
            missingParams.add("'username'");
        }

        if (!missingParams.isEmpty()) {
            final boolean oneParam = missingParams.size() == 1;
            final String errorMsg = String.format(
                    "Parameter%s [%s] %s mandatory and must be configured and not be empty", oneParam ? "" : "s",
                    String.join(", ", missingParams), oneParam ? "is" : "are");

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            return;
        }
        this.username = username;
        connector.setApiKey(apiKey);
        refreshInterval = accountConfig.refreshInterval;
        if (this.refreshInterval < MINIMUM_REFRESH_PERIOD_MINUTES) {
            logger.warn(
                    "Refresh interval time [{}] is not valid. Refresh interval time must be at least {} minutes. Setting to {} minutes",
                    accountConfig.refreshInterval, MINIMUM_REFRESH_PERIOD_MINUTES, DEFAULT_REFRESH_PERIOD_MINUTES);
            refreshInterval = DEFAULT_REFRESH_PERIOD_MINUTES;
        }
        logger.debug("Foobot Account bridge starting... user: {}, refreshInterval: {}", username, refreshInterval);

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Wait to get associated devices");

        dataCache = new ExpiringCache<>(Duration.ofMinutes(refreshInterval), this::retrieveDeviceList);
        this.refreshDeviceListJob = scheduler.scheduleWithFixedDelay(this::refreshDeviceList, 0,
                DEVICES_INTERVAL_MINUTES, TimeUnit.MINUTES);
        this.refreshSensorsJob = scheduler.scheduleWithFixedDelay(this::refreshSensors, 0,
                Duration.ofMinutes(refreshInterval).minus(SENSOR_INTERVAL_OFFSET_SECONDS).getSeconds(),
                TimeUnit.SECONDS);

        logger.debug("Foobot account bridge handler started.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            refreshDeviceList();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose {}", getThing().getUID());

        final ScheduledFuture<?> refreshDeviceListJob = this.refreshDeviceListJob;
        if (refreshDeviceListJob != null) {
            refreshDeviceListJob.cancel(true);
            this.refreshDeviceListJob = null;
        }
        final ScheduledFuture<?> refreshSensorsJob = this.refreshSensorsJob;
        if (refreshSensorsJob != null) {
            refreshSensorsJob.cancel(true);
            this.refreshSensorsJob = null;
        }
    }

    /**
     * Retrieves the list of devices and updates the properties of the devices. This method is called by the cache to
     * update the cache data.
     *
     * @return List of retrieved devices
     */
    private List<FoobotDeviceHandler> retrieveDeviceList() {
        logger.debug("Refreshing sensors for {}", getThing().getUID());
        final List<FoobotDeviceHandler> footbotHandlers = getFootbotHandlers();

        try {
            getDeviceList().stream().forEach(d -> {
                footbotHandlers.stream().filter(h -> h.getUuid().equals(d.getUuid())).findAny()
                        .ifPresent(fh -> fh.handleUpdateProperties(d));
            });
        } catch (FoobotApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return footbotHandlers;
    }

    /**
     * Refreshes the devices list
     */
    private void refreshDeviceList() {
        // This getValue() return value not used here. But if the cache is expired it refreshes the cache.
        dataCache.getValue();
        updateRemainingLimitStatus();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof FoobotDeviceHandler handler) {
            final String uuid = handler.getUuid();

            try {
                getDeviceList().stream().filter(d -> d.getUuid().equals(uuid)).findAny()
                        .ifPresent(fd -> handler.handleUpdateProperties(fd));
            } catch (FoobotApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @return Returns the list of associated footbot devices with this bridge.
     */
    public List<FoobotDeviceHandler> getFootbotHandlers() {
        return getThing().getThings().stream().map(Thing::getHandler).filter(FoobotDeviceHandler.class::isInstance)
                .map(FoobotDeviceHandler.class::cast).collect(Collectors.toList());
    }

    private void refreshSensors() {
        logger.debug("Refreshing sensors for {}", getThing().getUID());
        logger.debug("handlers: {}", getFootbotHandlers().size());
        try {
            for (FoobotDeviceHandler handler : getFootbotHandlers()) {
                logger.debug("handler: {}", handler.getUuid());
                handler.refreshSensors();
            }
            if (connector.getApiKeyLimitRemaining() == FoobotApiConnector.API_RATE_LIMIT_EXCEEDED) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        FoobotApiConnector.API_RATE_LIMIT_EXCEEDED_MESSAGE);
            } else if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (RuntimeException e) {
            logger.debug("Error updating sensor data ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    public void updateRemainingLimitStatus() {
        final int remaining = connector.getApiKeyLimitRemaining();

        updateState(FoobotBindingConstants.CHANNEL_APIKEY_LIMIT_REMAINING,
                remaining < 0 ? UnDefType.UNDEF : new DecimalType(remaining));
    }
}
