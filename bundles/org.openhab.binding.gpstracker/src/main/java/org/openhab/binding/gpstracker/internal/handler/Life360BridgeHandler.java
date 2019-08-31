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
import org.openhab.binding.gpstracker.internal.discovery.TrackerDescription;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.message.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.life360.CircleDetailResponse;
import org.openhab.binding.gpstracker.internal.message.life360.Location;
import org.openhab.binding.gpstracker.internal.message.life360.PlacesItem;
import org.openhab.binding.gpstracker.internal.provider.ProviderType;
import org.openhab.binding.gpstracker.internal.provider.life360.Life360Connection;
import org.openhab.binding.gpstracker.internal.provider.life360.Life360StatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
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

    /**
     * Life360 API connection
     */
    private Life360Connection connection;

    /**
     * Scheduled future for polling the location
     */
    private ScheduledFuture<?> scheduledLocationUpdate;

    /**
     * Scheduled future for polling the configuration
     */
    private ScheduledFuture<?> scheduledPlacesUpdate;

    /**
     * Life360 circle ids
     */
    private Set<String> circleIds;

    /**
     * Tracker device handlers
     */
    private Map<String, TrackerHandler> trackerHandlers;

    /**
     * Discovery service
     */
    private TrackerDiscoveryService discoveryService;

    /**
     * Configured user places
     */
    private Map<String, Map<String, PlacesItem>> userPlaces = new HashMap<>();

    /**
     * Constructor.
     *
     * @param bridge Bridge thing
     * @param httpClient HTTP client
     * @param discoveryService Discovery service
     * @param trackerHandlers Map of tracker handlers
     */
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

        //connecting to cloud service
        Life360Config config = thing.getConfiguration().as(Life360Config.class);
        connection.authenticate(config);

        if (connection.isOnline()) {
            //initial load of the configuration
            loadLife360Config();

            //scheduling the poll
            scheduledLocationUpdate = scheduler.scheduleWithFixedDelay(this::refreshTrackerLocations, 0, config.getRefreshPeriod(), TimeUnit.SECONDS);
            logger.debug("Life360 polling started");

            //scheduling config update
            scheduledPlacesUpdate = scheduler.scheduleWithFixedDelay(this::loadLife360Config, 0, config.getConfigPeriod(), TimeUnit.SECONDS);
        } else {
            //cloud connection failed - setting thing offline
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to connect to Life360");
        }
    }

    /**
     * Load Life360 configuration
     */
    private void loadLife360Config() {
        //loac the configured circle ids
        circleIds = connection.loadCircleIds();
        logger.debug("Found {} Life360 circles.", circleIds.size());

        //load configured places for every circle
        circleIds.forEach(cId -> {
            try {
                CircleDetailResponse circle = connection.loadCircleDetails(cId);
                List<PlacesItem> placesItems = connection.loadPlaces(cId);
                placesItems.forEach(p -> {
                    Map<String, PlacesItem> userMap = userPlaces.computeIfAbsent(p.getOwnerId(), k -> new HashMap<>());
                    userMap.put(p.getId(), p);
                });
                logger.debug("Found {} Life360 configured users in circle {}.", circle.getMembers().size(), cId);
                logger.debug("Found {} Life360 configured places for all users in circle {}.", placesItems.size(), cId);
            } catch (Exception e) {
                logger.error("Failed to load places for circle {}", cId, e);
            }
        });
        //update distance channels based on the configuration
        trackerHandlers.values().forEach(th->
        userPlaces.entrySet().stream().filter(e -> e.getKey().equals(th.getTrackerId())).forEach(e -> {
            e.getValue().values().forEach(th::createCustomDistanceChannel4Places);
        }));
    }

    /**
     * Life360 circle poll
     */
    private void refreshTrackerLocations() {
        logger.debug("Refresh trackers from Life360");
        circleIds.forEach(cId -> {
            if (!connection.isStopped()) {
                try {
                    CircleDetailResponse circle = connection.loadCircleDetails(cId);
                    //check location for all members
                    circle.getMembers().forEach(m -> {
                        Location location = m.getLocation();
                        String trackerId = m.getId();
                        logger.trace("Location received for {} tracker: {}", trackerId, location);

                        TrackerHandler trackerHandler = trackerHandlers.get(trackerId);
                        if (trackerHandler != null) {
                            //update tracker locations
                            trackerHandler.updateLocation(new LocationMessage(trackerId, location));
                            logger.debug("Life360 tracker updated: {}", trackerId);
                        } else {
                            //discover if no tracker is configured for the member
                            discoveryService.addTracker(new TrackerDescription(trackerId, m.getFullName(), ProviderType.Life360));
                        }
                    });
                } catch (Exception e) {
                    logger.error("Failed to update circle {}", cId, e);
                }
            } else {
                //stop the schedulers if the connection is terminated
                stop();
            }
        });
    }

    /**
     * Terminate schedulers
     */
    public void stop() {
        if (scheduledLocationUpdate != null && !scheduledLocationUpdate.isCancelled()) {
            scheduledLocationUpdate.cancel(true);
            logger.debug("Life360 polling stopped");
        }
        if (scheduledPlacesUpdate != null && !scheduledPlacesUpdate.isCancelled()) {
            scheduledPlacesUpdate.cancel(true);
            logger.debug("Life360 configuration polling stopped");
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
