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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.evohome.internal.RunnableWithTimeout;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Gateway;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Location;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.LocationStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Locations;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.LocationsStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TemperatureControlSystemStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Zone;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneStatus;
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
@NonNullByDefault
public class EvohomeAccountBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvohomeAccountBridgeHandler.class);
    private final HttpClient httpClient;
    private EvohomeAccountConfiguration configuration = new EvohomeAccountConfiguration();
    private @Nullable EvohomeApiClient apiClient;
    private List<AccountStatusListener> listeners = new CopyOnWriteArrayList<>();

    protected @Nullable ScheduledFuture<?> refreshTask;

    public EvohomeAccountBridgeHandler(Bridge thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(EvohomeAccountConfiguration.class);

        if (checkConfig(configuration)) {
            apiClient = new EvohomeApiClient(configuration, this.httpClient);

            // Initialization can take a while, so kick it off on a separate thread
            scheduler.schedule(() -> {
                EvohomeApiClient localApiCLient = apiClient;
                if (localApiCLient != null && localApiCLient.login()) {
                    if (checkInstallationInfoHasDuplicateIds(localApiCLient.getInstallationInfo())) {
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

    public @Nullable Locations getEvohomeConfig() {
        EvohomeApiClient localApiCLient = apiClient;
        if (localApiCLient != null) {
            return localApiCLient.getInstallationInfo();
        }
        return null;
    }

    public @Nullable LocationsStatus getEvohomeStatus() {
        EvohomeApiClient localApiCLient = apiClient;
        if (localApiCLient != null) {
            return localApiCLient.getInstallationStatus();
        }
        return null;
    }

    public void setTcsMode(String tcsId, String mode) {
        EvohomeApiClient localApiCLient = apiClient;
        if (localApiCLient != null) {
            tryToCall(() -> localApiCLient.setTcsMode(tcsId, mode));
        }
    }

    public void setPermanentSetPoint(String zoneId, double doubleValue) {
        EvohomeApiClient localApiCLient = apiClient;
        if (localApiCLient != null) {
            tryToCall(() -> localApiCLient.setHeatingZoneOverride(zoneId, doubleValue));
        }
    }

    public void cancelSetPointOverride(String zoneId) {
        EvohomeApiClient localApiCLient = apiClient;
        if (localApiCLient != null) {
            tryToCall(() -> localApiCLient.cancelHeatingZoneOverride(zoneId));
        }
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
        EvohomeApiClient localApiClient = apiClient;
        if (localApiClient != null) {
            localApiClient.logout();
            this.apiClient = null;
        }
    }

    private void disposeRefreshTask() {
        ScheduledFuture<?> localRefreshTask = refreshTask;
        if (localRefreshTask != null) {
            localRefreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    private boolean checkConfig(EvohomeAccountConfiguration configuration) {
        String errorMessage = "";

        if (configuration.username.isBlank()) {
            errorMessage = "Username not configured";
        } else if (configuration.password.isBlank()) {
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
            EvohomeApiClient localApiCLient = apiClient;
            if (localApiCLient != null) {
                localApiCLient.update();
            }
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

    private void updateAccountStatus(ThingStatus newStatus, ThingStatusDetail detail, @Nullable String message) {
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

        EvohomeApiClient localApiClient = apiClient;
        if (localApiClient != null) {
            // First, create a lookup table
            LocationsStatus localLocationsStatus = localApiClient.getInstallationStatus();
            if (localLocationsStatus != null) {
                for (LocationStatus location : localLocationsStatus) {
                    for (GatewayStatus gateway : location.getGateways()) {
                        if (gateway == null) {
                            continue;
                        }
                        for (TemperatureControlSystemStatus tcs : gateway.getTemperatureControlSystems()) {
                            String systemId = tcs.getSystemId();
                            if (systemId != null) {
                                idToTcsMap.put(systemId, tcs);
                                tcsIdToGatewayMap.put(systemId, gateway);
                            }
                            for (ZoneStatus zone : tcs.getZones()) {
                                String zoneId = zone.getZoneId();
                                if (zoneId != null) {
                                    idToZoneMap.put(zoneId, zone);
                                    if (systemId != null) {
                                        zoneIdToTcsIdMap.put(zoneId, systemId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Then update the things by type, with pre-filtered info
        for (Thing handler : getThing().getThings()) {
            ThingHandler thingHandler = handler.getHandler();

            if (thingHandler instanceof EvohomeTemperatureControlSystemHandler tcsHandler) {
                tcsHandler.update(tcsIdToGatewayMap.get(tcsHandler.getId()), idToTcsMap.get(tcsHandler.getId()));
                idToTcsThingsStatusMap.put(tcsHandler.getId(), tcsHandler.getThing().getStatus());
            }
            if (thingHandler instanceof EvohomeHeatingZoneHandler zoneHandler) {
                zoneHandler.update(idToTcsThingsStatusMap.get(zoneIdToTcsIdMap.get(zoneHandler.getId())),
                        idToZoneMap.get(zoneHandler.getId()));
            }
        }
    }
}
