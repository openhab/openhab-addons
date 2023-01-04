/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.evohome.internal.RunnableWithTimeout;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.openhab.binding.evohome.internal.api.models.v2.response.Gateway;
import org.openhab.binding.evohome.internal.api.models.v2.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Location;
import org.openhab.binding.evohome.internal.api.models.v2.response.LocationStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Locations;
import org.openhab.binding.evohome.internal.api.models.v2.response.LocationsStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystemStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Zone;
import org.openhab.binding.evohome.internal.api.models.v2.response.ZoneStatus;
import org.openhab.binding.evohome.internal.configuration.EvohomeAccountConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the bridge for this binding. Controls the authentication sequence.
 * Manages the scheduler for getting updates from the API and updates the Things it contains.
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class EvohomeAccountBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvohomeAccountBridgeHandler.class);
    private final HttpClient httpClient;
    private EvohomeAccountConfiguration configuration;
    private EvohomeApiClient apiClient;
    private List<AccountStatusListener> listeners = new CopyOnWriteArrayList<>();

    protected ScheduledFuture<?> refreshTask;

    public EvohomeAccountBridgeHandler(Bridge thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(EvohomeAccountConfiguration.class);

        if (checkConfig()) {
            apiClient = new EvohomeApiClient(configuration, this.httpClient);

            // Initialization can take a while, so kick it off on a separate thread
            scheduler.schedule(() -> {
                if (apiClient.login()) {
                    if (checkInstallationInfoHasDuplicateIds(apiClient.getInstallationInfo())) {
                        startRefreshTask();
                    } else {
                        updateAccountStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "System Information Sanity Check failed");
                    }
                } else {
                    updateAccountStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Authentication failed");
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        disposeRefreshTask();
        disposeApiClient();
        listeners.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public Locations getEvohomeConfig() {
        return apiClient.getInstallationInfo();
    }

    public LocationsStatus getEvohomeStatus() {
        return apiClient.getInstallationStatus();
    }

    public void setTcsMode(String tcsId, String mode) {
        tryToCall(() -> apiClient.setTcsMode(tcsId, mode));
    }

    public void setPermanentSetPoint(String zoneId, double doubleValue) {
        tryToCall(() -> apiClient.setHeatingZoneOverride(zoneId, doubleValue));
    }

    public void cancelSetPointOverride(String zoneId) {
        tryToCall(() -> apiClient.cancelHeatingZoneOverride(zoneId));
    }

    public void addAccountStatusListener(AccountStatusListener listener) {
        listeners.add(listener);
        listener.accountStatusChanged(getThing().getStatus());
    }

    public void removeAccountStatusListener(AccountStatusListener listener) {
        listeners.remove(listener);
    }

    private void tryToCall(RunnableWithTimeout action) {
        try {
            action.run();
        } catch (TimeoutException e) {
            updateAccountStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Timeout on executing request");
        }
    }

    private boolean checkInstallationInfoHasDuplicateIds(Locations locations) {
        boolean result = true;

        // Make sure that there are no duplicate IDs
        Set<String> ids = new HashSet<>();

        for (Location location : locations) {
            result &= ids.add(location.getLocationInfo().getLocationId());
            for (Gateway gateway : location.getGateways()) {
                result &= ids.add(gateway.getGatewayInfo().getGatewayId());
                for (TemperatureControlSystem tcs : gateway.getTemperatureControlSystems()) {
                    result &= ids.add(tcs.getSystemId());
                    for (Zone zone : tcs.getZones()) {
                        result &= ids.add(zone.getZoneId());
                    }
                }
            }
        }
        return result;
    }

    private void disposeApiClient() {
        if (apiClient != null) {
            apiClient.logout();
        }
        apiClient = null;
    }

    private void disposeRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
    }

    private boolean checkConfig() {
        String errorMessage = "";

        if (configuration == null) {
            errorMessage = "Configuration is missing or corrupted";
        } else if (configuration.username == null || configuration.username.isEmpty()) {
            errorMessage = "Username not configured";
        } else if (configuration.password == null || configuration.password.isEmpty()) {
            errorMessage = "Password not configured";
        } else {
            return true;
        }

        updateAccountStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
        return false;
    }

    private void startRefreshTask() {
        disposeRefreshTask();

        refreshTask = scheduler.scheduleWithFixedDelay(this::update, 0, configuration.refreshInterval,
                TimeUnit.SECONDS);
    }

    private void update() {
        try {
            apiClient.update();
            updateAccountStatus(ThingStatus.ONLINE);
            updateThings();
        } catch (Exception e) {
            updateAccountStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Failed to update installation status", e);
        }
    }

    private void updateAccountStatus(ThingStatus newStatus) {
        updateAccountStatus(newStatus, ThingStatusDetail.NONE, null);
    }

    private void updateAccountStatus(ThingStatus newStatus, ThingStatusDetail detail, String message) {
        // Prevent spamming the log file
        if (!newStatus.equals(getThing().getStatus())) {
            updateStatus(newStatus, detail, message);
            updateListeners(newStatus);
        }
    }

    private void updateListeners(ThingStatus status) {
        for (AccountStatusListener listener : listeners) {
            listener.accountStatusChanged(status);
        }
    }

    private void updateThings() {
        Map<String, TemperatureControlSystemStatus> idToTcsMap = new HashMap<>();
        Map<String, ZoneStatus> idToZoneMap = new HashMap<>();
        Map<String, GatewayStatus> tcsIdToGatewayMap = new HashMap<>();
        Map<String, String> zoneIdToTcsIdMap = new HashMap<>();
        Map<String, ThingStatus> idToTcsThingsStatusMap = new HashMap<>();

        // First, create a lookup table
        for (LocationStatus location : apiClient.getInstallationStatus()) {
            for (GatewayStatus gateway : location.getGateways()) {
                for (TemperatureControlSystemStatus tcs : gateway.getTemperatureControlSystems()) {
                    idToTcsMap.put(tcs.getSystemId(), tcs);
                    tcsIdToGatewayMap.put(tcs.getSystemId(), gateway);
                    for (ZoneStatus zone : tcs.getZones()) {
                        idToZoneMap.put(zone.getZoneId(), zone);
                        zoneIdToTcsIdMap.put(zone.getZoneId(), tcs.getSystemId());
                    }
                }
            }
        }

        // Then update the things by type, with pre-filtered info
        for (Thing handler : getThing().getThings()) {
            ThingHandler thingHandler = handler.getHandler();

            if (thingHandler instanceof EvohomeTemperatureControlSystemHandler) {
                EvohomeTemperatureControlSystemHandler tcsHandler = (EvohomeTemperatureControlSystemHandler) thingHandler;
                tcsHandler.update(tcsIdToGatewayMap.get(tcsHandler.getId()), idToTcsMap.get(tcsHandler.getId()));
                idToTcsThingsStatusMap.put(tcsHandler.getId(), tcsHandler.getThing().getStatus());
            }
            if (thingHandler instanceof EvohomeHeatingZoneHandler) {
                EvohomeHeatingZoneHandler zoneHandler = (EvohomeHeatingZoneHandler) thingHandler;
                zoneHandler.update(idToTcsThingsStatusMap.get(zoneIdToTcsIdMap.get(zoneHandler.getId())),
                        idToZoneMap.get(zoneHandler.getId()));
            }
        }
    }
}
