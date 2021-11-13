/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.handler;

import static org.openhab.binding.cloudrain.internal.CloudrainBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.CloudrainConfig;
import org.openhab.binding.cloudrain.internal.CloudrainException;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPI;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPIException;
import org.openhab.binding.cloudrain.internal.api.model.AuthParams;
import org.openhab.binding.cloudrain.internal.api.model.Irrigation;
import org.openhab.binding.cloudrain.internal.api.model.Zone;
import org.openhab.binding.cloudrain.internal.discovery.CloudrainDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CloudrainAccountHandler is responsible for handling the user's Cloudrain account. It acts as bridge in
 * the Cloudrain ecosystem and offers access to the user's Controllers, Zones and Irrigations. This handler
 * authenticates with the Cloudrain Developer API using the user-defined authentication parameters and is responsible
 * for polling the overall irrigation status.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(CloudrainAccountHandler.class);

    /**
     * The CloudrainDiscoveryService responsible for automatic zone discovery
     */
    private @Nullable CloudrainDiscoveryService discoveryService;

    /**
     * The Cloudrain API instance used to retrieve updates and send commands
     */
    private CloudrainAPI cloudrainAPI;

    /**
     * Holds the account configuration settings
     */
    private CloudrainConfig config;

    /**
     * The zoneUpdateJob is responsible for polling zone updates using the Cloudrain API
     */
    private @Nullable ScheduledFuture<?> zoneUpdateJob;

    /**
     * The irrigationUpdateJob is responsible for polling irrigation updates using the Cloudrain API
     */
    private @Nullable ScheduledFuture<?> irrigationUpdateJob;

    /**
     * The zoneRegistry contains all zoneIds and active zone things
     */
    private ConcurrentHashMap<String, Thing> zoneRegistry;

    /**
     * The statusUpdateRegistry contains all zoneIds and zone things which register for updates from the statusUpdateJob
     */
    private ConcurrentHashMap<String, Thing> statusUpdateRegistry;

    /**
     * Creates this {@link CloudrainAccountHandler}
     *
     * @param bridge the bridge thing (the Cloudrain account)
     * @param cloudrainAPI the uninitialized Cloudrain API created by the CloudrainHandlerFactory
     */
    public CloudrainAccountHandler(Bridge bridge, CloudrainAPI cloudrainAPI) {
        super(bridge);
        this.cloudrainAPI = cloudrainAPI;
        this.config = getThing().getConfiguration().as(CloudrainConfig.class);
        this.statusUpdateRegistry = new ConcurrentHashMap<String, Thing>();
        this.zoneRegistry = new ConcurrentHashMap<String, Thing>();
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(CloudrainConfig.class);
        int irrigationInterval = config.getIrrigationUpdateInterval().intValue();
        int zoneInterval = config.getZoneUpdateInterval().intValue();
        // execute initialization tasks in background
        scheduler.execute(() -> {
            try {
                // initialize the API and authenticate
                cloudrainAPI.initialize(config);
                cloudrainAPI.authenticate(createAuthParams(config));
                // initialize and start the automatic zone discovery
                final CloudrainDiscoveryService discoveryService = this.discoveryService;
                if (discoveryService != null) {
                    discoveryService.startScan(null);
                    discoveryService.waitForScanFinishing();
                }
                // Schedule the zone status updates according to the defined update interval. Skip the first run as
                // initially the zones update their status themselves
                zoneUpdateJob = scheduler.scheduleWithFixedDelay(this::updateZoneStatusFromAPI, zoneInterval,
                        zoneInterval, TimeUnit.SECONDS);
                // Schedule the irrigation status updates according to the defined update interval
                irrigationUpdateJob = scheduler.scheduleWithFixedDelay(this::updateIrrigationStatusFromAPI, 0,
                        irrigationInterval, TimeUnit.SECONDS);
                // All important tasks done - update the bridge's status to online.
                updateStatus(ThingStatus.ONLINE);
            } catch (CloudrainAPIException ex) {
                // In case of failure log and go into offline status
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format(ERROR_MSG_API_AUTH, ex.getMessage()));
            } catch (CloudrainException ex) {
                // In case of failure log and go into offline status
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ERROR_MSG_CONFIG_PARAMS);
            } finally {
                // In any case update the zone's status so that they reflect any issue with the bridge as well. On first
                // initialization this has no effect as no zone is registered yet, but during changes on the account
                // handler's configuration it is important to force all zones to update their status. For example when
                // changing login information or the test mode.
                forceUpdateAllZones();
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        ScheduledFuture<?> irrigationJob = this.irrigationUpdateJob;
        ScheduledFuture<?> zoneJob = this.zoneUpdateJob;
        // stop the running status update jobs
        if (irrigationJob != null) {
            irrigationJob.cancel(true);
            this.irrigationUpdateJob = null;
        }
        if (zoneJob != null) {
            zoneJob.cancel(true);
            this.zoneUpdateJob = null;
        }
    }

    /**
     * Updates all known Zone Things status (properties and thing status) from the API.
     * This code is intended to be run inside a polling job.
     */
    private synchronized void updateZoneStatusFromAPI() {
        // Only has to be done if at least one Zone has registered
        if (!zoneRegistry.isEmpty()) {
            try {
                // Find all currently known zones from API
                List<Zone> zones = cloudrainAPI.getZones();
                Map<String, Zone> knownZones = new HashMap<String, Zone>();
                for (Zone zone : zones) {
                    String id = zone.getId();
                    if (id != null) {
                        knownZones.put(id, zone);
                    }
                }
                // Iterate all registered zones and check each
                Set<String> registeredZonesIds = zoneRegistry.keySet();
                // copy the set to prevent concurrent updates from registrations while iterating
                Set<String> registeredZonesIdsCopy = new HashSet<String>(registeredZonesIds);

                for (String zoneId : registeredZonesIdsCopy) {
                    Thing zoneThing = zoneRegistry.get(zoneId);
                    if (zoneThing != null) {
                        Zone zone = knownZones.get(zoneId);
                        if (zone != null) {
                            // if the zone is known by the API update its properties
                            updateFromZone(zoneThing, zone);
                        } else {
                            // if the zone is not known by the API force a status update
                            forceUpdate(zoneThing);
                        }
                    }
                }
            } catch (CloudrainAPIException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format(ERROR_MSG_ZONE_STATUS_UPDATE, e.getMessage()));
            }
        }
    }

    /**
     * Updates the Zone Things that have registered for status update from the running irrigations retrieved via the
     * Cloudrain API. This code is intended to be run inside a polling job.
     */
    private synchronized void updateIrrigationStatusFromAPI() {
        // Only has to be done if at least one Zone has registered for updates
        if (!statusUpdateRegistry.isEmpty()) {
            try {
                // retrieve all running irrigations
                List<Irrigation> irrigations = cloudrainAPI.getIrrigations();
                // remember which zones were updated
                Set<String> updatedZones = new HashSet<String>();
                // Update the corresponding zones with running irrigation
                if (!irrigations.isEmpty()) {
                    for (Irrigation irrigation : irrigations) {
                        String zoneId = irrigation.getZoneId();
                        if (zoneId == null || zoneId.isBlank()) {
                            // Skip this irrigation. No processing possible without zoneId.
                            logger.warn(ERROR_MSG_GET_IRRIGATION_ZONE_ID);
                            continue;
                        }
                        Thing zoneThing = statusUpdateRegistry.get(zoneId);
                        if (zoneThing != null) {
                            updateFromIrrigation(zoneThing, irrigation);
                            updatedZones.add(zoneId);
                        }
                    }
                }
                // Process all remaining zones
                for (String zoneId : statusUpdateRegistry.keySet()) {
                    // Clear all zones without active irrigations
                    if (!updatedZones.contains(zoneId)) {
                        Thing zoneThing = statusUpdateRegistry.get(zoneId);
                        if (zoneThing != null) {
                            updateFromIrrigation(zoneThing, null);
                        }
                    }
                }
            } catch (CloudrainAPIException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format(ERROR_MSG_STATUS_UPDATE_IRRIGATION, e.getMessage()));
            }
        }
    }

    /**
     * Updates a zone {@link Thing} with latest {@link Irrigation} data retrieved from the polling job.
     *
     * @param zoneThing the zone thing to be updated
     * @param irrigation the irrigation status retrieved from the Cloudrain API
     */
    private void updateFromZone(Thing zoneThing, Zone zone) {
        if (zoneThing.getHandler() instanceof CloudrainZoneHandler) {
            CloudrainZoneHandler handler = (CloudrainZoneHandler) zoneThing.getHandler();
            if (handler != null) {
                handler.updateZoneProperties(zone);
            } else {
                logger.warn(ERROR_MSG_ZONE_UPDATE_PROPERTIES, zoneThing.getUID());
            }
        }
    }

    /**
     * Updates a zone {@link Thing} with latest {@link Irrigation} data retrieved from the polling job.
     *
     * @param zoneThing the zone thing to be updated
     * @param irrigation the irrigation status retrieved from the Cloudrain API
     */
    private void updateFromIrrigation(Thing zoneThing, @Nullable Irrigation irrigation) {
        if (zoneThing.getHandler() instanceof CloudrainZoneHandler) {
            CloudrainZoneHandler handler = (CloudrainZoneHandler) zoneThing.getHandler();
            if (handler != null) {
                handler.updateIrrigationState(irrigation);
            } else {
                logger.warn(ERROR_MSG_ZONE_UPDATE_IRRIGATION, zoneThing.getUID());
            }
        }
    }

    /**
     * Forces the {@link CloudrainZoneHandler} to update the things status because the bridge thinks it may not be valid
     * anymore, e.g. the zone was deleted in the Cloudrain app.
     *
     * @param zoneThing the zone thing to be updated
     */
    private void forceUpdateAllZones() {
        if (!zoneRegistry.isEmpty()) {
            // Iterate all registered zones and check each
            Collection<Thing> registeredZones = zoneRegistry.values();
            // copy the set to prevent concurrent updates from registrations while iterating
            Collection<Thing> registeredZonesCopy = new HashSet<Thing>(registeredZones);
            for (Thing zoneThing : registeredZonesCopy) {
                forceUpdate(zoneThing);
            }
        }
    }

    /**
     * Forces the {@link CloudrainZoneHandler} to update the things status because the bridge thinks it may not be valid
     * anymore, e.g. the zone was deleted in the Cloudrain app.
     *
     * @param zoneThing the zone thing to be updated
     */
    private void forceUpdate(Thing zoneThing) {
        if (zoneThing.getHandler() instanceof CloudrainZoneHandler) {
            CloudrainZoneHandler handler = (CloudrainZoneHandler) zoneThing.getHandler();
            if (handler != null) {
                handler.thingUpdated(zoneThing);
            } else {
                logger.warn(ERROR_MSG_ZONE_UPDATE_PROPERTIES, zoneThing.getUID());
            }
        }
    }

    /**
     * With this method instances of {@link CloudrainZoneHandler} can register the zone with the account handler.
     * This is required to receive periodic updates of the zone's properties and thing status (online / offline).
     *
     * @param zoneId the id of the {@link Zone} handled by the {@link CloudrainZoneHandler}
     * @param zoneThing the {@link Thing} representing the zone
     */
    public void registerZone(String zoneId, Thing zoneThing) {
        if (!zoneRegistry.containsKey(zoneId)) {
            zoneRegistry.put(zoneId, zoneThing);
        }
    }

    /**
     * With this method instances of {@link CloudrainZoneHandler} can unregister a zone from the account handler
     *
     * @param zoneId the id of the {@link Zone} to be unregistered
     */
    public void unregisterZone(String zoneId) {
        zoneRegistry.remove(zoneId);
    }

    /**
     * With this method instances of {@link CloudrainZoneHandler} can register to receive updates from the
     * central status polling using the Cloudrain Developer API. The API offers a function to retrieve all active
     * irrigations for all zones. This API is called periodically and registered zones will receive updates. If no zone
     * is registered the central polling will be omitted.
     *
     * @param zoneId the id of the {@link Zone} handled by the {@link CloudrainZoneHandler}
     * @param zoneThing the {@link Thing} representing the zone to receive the updates
     */
    public void registerForIrrigationUpdates(String zoneId, Thing zoneThing) {
        if (!statusUpdateRegistry.containsKey(zoneId)) {
            statusUpdateRegistry.put(zoneId, zoneThing);
        }
    }

    /**
     * With this method instances of {@link CloudrainZoneHandler} can unregister from receiving updates from the
     * central status polling using the Cloudrain Developer API.
     *
     * @param zoneId the id of the {@link Zone} to be unregistered
     */
    public void unregisterFromIrrigationUpdates(String zoneId) {
        statusUpdateRegistry.remove(zoneId);
    }

    /**
     * Retrieves all zones known in this Cloudrain account.
     * Exposed to be used by the {link {@link CloudrainDiscoveryService}.
     *
     * @return the list of {@link Zone} objects. Null in case or errors.
     */
    public @Nullable List<Zone> getZones() {
        try {
            return cloudrainAPI.getZones();
        } catch (CloudrainAPIException e) {
            logger.warn(ERROR_MSG_GET_ZONES, e.getMessage());
            return null;
        }
    }

    /**
     * Convenience method to convert {@link CloudrainConfig} into {@link AuthParams} required for the Cloudrain API
     * authentication.
     *
     * @param config the configuration object containing the authentication data
     * @return the authentication object for the Cloudrain API
     */
    private AuthParams createAuthParams(CloudrainConfig config) throws CloudrainException {
        String user = config.getUser();
        String pw = config.getPassword();
        String clientId = config.getClientId();
        String clientSecret = config.getClientSecret();

        if (user != null && pw != null && clientId != null && clientSecret != null) {
            return new AuthParams(user, pw, clientId, clientSecret);
        }
        throw new CloudrainException(ERROR_MSG_CONFIG_PARAMS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Accounts do not need to handle commands
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(CloudrainDiscoveryService.class);
    }

    /**
     * Returns the {@link CloudrainDiscoveryService} associated with this handler
     *
     * @return the {@link CloudrainDiscoveryService} associated with this handler
     */
    public @Nullable CloudrainDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    /**
     * Sets the {@link CloudrainDiscoveryService} associated with this handler
     *
     * @param discoveryService the {@link CloudrainDiscoveryService} to be set
     */
    public void setDiscoveryService(CloudrainDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }
}
