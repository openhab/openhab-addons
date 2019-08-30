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
package org.openhab.binding.gpstracker.internal.handler;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.gpstracker.internal.config.Life360Config;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.message.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.life360.CircleDetailResponse;
import org.openhab.binding.gpstracker.internal.message.life360.Location;
import org.openhab.binding.gpstracker.internal.provider.life360.Life360Connection;
import org.openhab.binding.gpstracker.internal.provider.life360.Life360StatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The {@link Life360BridgeHandler} class is a bridge handler for Life360.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class Life360BridgeHandler extends BaseBridgeHandler implements Life360StatusListener {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(Life360BridgeHandler.class);

    private Life360Connection connection;
    private ScheduledFuture<?> scheduledFuture;
    private Set<String> circleIds;
    private Map<String, TrackerHandler> trackerHandlers;
    private TrackerDiscoveryService discoveryService;

    public Life360BridgeHandler(Bridge bridge, HttpClient httpClient, TrackerDiscoveryService discoveryService, Map<String, TrackerHandler> trackerHandlers) {
        super(bridge);
        this.trackerHandlers = trackerHandlers;
        this.discoveryService = discoveryService;

        connection = new Life360Connection(httpClient, this);
        discoveryService.setLife360BridgeUID(thing.getUID());
    }

    @Override
    public void initialize() {
        super.initialize();

        Life360Config config = thing.getConfiguration().as(Life360Config.class);
        connection.authenticate(config);
        circleIds = connection.loadCircleIds();
        logger.debug("Found {} Life360 circles.", circleIds.size());
        logger.debug("Life360 polling started");
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::refreshTrackerLocations, 0, config.getRefreshPeriod(), TimeUnit.SECONDS);
    }

    private void refreshTrackerLocations() {
        logger.debug("Refresh trackers from Life360");
        circleIds.forEach(cId -> {
            try {
                CircleDetailResponse circle = connection.loadCircleDetails(cId);
                circle.getMembers().forEach(m -> {
                    Location location = m.getLocation();
                    String loginEmail = m.getLoginEmail();
                    logger.trace("Location received for {} tracker: {}", loginEmail, location);

                    TrackerHandler trackerHandler = trackerHandlers.get(loginEmail);
                    if (trackerHandler != null) {
                        trackerHandler.updateLocation(new LocationMessage(loginEmail, location));
                        logger.debug("Life360 tracker updated: {}", loginEmail);
                    } else {
                        discoveryService.addTracker(loginEmail);
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to update circle {}", cId, e);
            }
        });
    }

    public void stop() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            logger.debug("Life360 polling stopped");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshTrackerLocations();
        }
    }

    @Override
    public void online() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void error(String msg) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
    }
}
