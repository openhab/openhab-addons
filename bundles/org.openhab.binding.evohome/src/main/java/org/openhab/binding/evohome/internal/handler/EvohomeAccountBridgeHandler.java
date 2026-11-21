/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.RunnableWithTimeout;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient.AcceptedSetPoint;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient.CachedSchedule;
import org.openhab.binding.evohome.internal.api.EvohomeApiClientException;
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
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the bridge for this binding. Controls the authentication sequence.
 * Manages the scheduler for getting updates from the API and updates the Things it contains.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Retry scheduling and reliable state publication
 * @author Kovacs Istvan - Validated override commands and system reset
 * @author Kovacs Istvan - Cached weekly schedule polling
 * @author Kovacs Istvan - Discovery lifecycle and partial reset recovery
 * @author Kovacs Istvan - Fair schedule refresh and accepted command propagation
 * @author Kovacs Istvan - Lifecycle-bound zone status delivery
 *
 */
@NonNullByDefault
public class EvohomeAccountBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvohomeAccountBridgeHandler.class);
    private final HttpClient httpClient;
    private final ScheduledExecutorService refreshScheduler;
    private EvohomeAccountConfiguration configuration = new EvohomeAccountConfiguration();
    private volatile @Nullable EvohomeApiClient apiClient;
    private List<AccountStatusListener> listeners = new CopyOnWriteArrayList<>();

    protected @Nullable ScheduledFuture<?> refreshTask;
    private long refreshGeneration;
    private long retryDelay;
    private final Set<String> pendingScheduleRefresh = new LinkedHashSet<>();

    public EvohomeAccountBridgeHandler(Bridge thing, HttpClient httpClient) {
        this(thing, httpClient, null);
    }

    EvohomeAccountBridgeHandler(Bridge thing, HttpClient httpClient,
            @Nullable ScheduledExecutorService refreshScheduler) {
        super(thing);
        this.httpClient = httpClient;
        this.refreshScheduler = refreshScheduler == null ? scheduler : refreshScheduler;
    }

    @Override
    public synchronized void initialize() {
        disposeConnection();
        configuration = getConfigAs(EvohomeAccountConfiguration.class);

        if (checkConfig(configuration)) {
            EvohomeApiClient client = new EvohomeApiClient(configuration, this.httpClient);
            apiClient = client;
            retryDelay = 0;
            updateAccountStatus(ThingStatus.UNKNOWN);
            scheduleRefresh(client, 0);
        }
    }

    @Override
    public synchronized void dispose() {
        disposeConnection();
        listeners.clear();
    }

    private void disposeConnection() {
        refreshGeneration++;
        disposeRefreshTask();
        disposeApiClient();
        pendingScheduleRefresh.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            requestRefresh();
        }
    }

    public synchronized void requestRefresh() {
        EvohomeApiClient client = apiClient;
        // Explicit refreshes must not bypass failure backoff or a server's retry delay.
        if (client != null && retryDelay == 0) {
            logger.trace("Account {}: explicit refresh requested", getThing().getUID());
            scheduleRefresh(client, 0);
        } else {
            logger.trace("Account {}: explicit refresh deferred; clientAvailable={}, retryDelay={} s",
                    getThing().getUID(), client != null, retryDelay);
        }
    }

    public synchronized boolean requestScheduleRefresh(String zoneId) {
        if (apiClient == null || zoneId.isBlank()) {
            return false;
        }
        pendingScheduleRefresh.add(zoneId);
        requestRefresh();
        return true;
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

    public boolean setTcsMode(String tcsId, String mode) {
        logger.trace("Account {}: system mode command for controller {}: {}", getThing().getUID(), tcsId, mode);
        EvohomeApiClient localApiCLient = apiClient;
        if (localApiCLient != null) {
            return tryToCall(localApiCLient, "set system mode", tcsId, () -> localApiCLient.setTcsMode(tcsId, mode));
        }
        return false;
    }

    public boolean setTemporaryTcsMode(String tcsId, String mode, Instant until) {
        logger.trace("Account {}: temporary system mode command for controller {}: {} until {}", getThing().getUID(),
                tcsId, mode, until);
        EvohomeApiClient client = apiClient;
        return client != null && tryToCall(client, "set temporary system mode", tcsId,
                () -> client.setTemporaryTcsMode(tcsId, mode, until));
    }

    public boolean resetSystem(String tcsId) {
        EvohomeApiClient client = apiClient;
        List<Consumer<String>> resetCallbacks = getThing().getThings().stream().map(Thing::getHandler)
                .filter(EvohomeHeatingZoneHandler.class::isInstance).map(EvohomeHeatingZoneHandler.class::cast)
                .map(EvohomeHeatingZoneHandler::resetCommandContextCallback).toList();
        return client != null
                && tryToCall(client, "reset system to schedule", tcsId, () -> client.resetTcs(tcsId, zoneId -> {
                    synchronized (this) {
                        if (client.equals(apiClient)) {
                            resetCallbacks.forEach(callback -> callback.accept(zoneId));
                        }
                    }
                }));
    }

    public boolean setPermanentSetPoint(String zoneId, double temperature) {
        return setPermanentSetPoint(zoneId, temperature, accepted -> {
        });
    }

    boolean setPermanentSetPoint(String zoneId, double temperature, Consumer<AcceptedSetPoint> onAccepted) {
        logger.trace("Account {}: permanent setpoint command for zone {}: {} Celsius", getThing().getUID(), zoneId,
                temperature);
        EvohomeApiClient client = apiClient;
        AtomicReference<@Nullable AcceptedSetPoint> accepted = new AtomicReference<>();
        return client != null && tryToCall(client, "set permanent setpoint", zoneId,
                () -> accepted.set(client.setHeatingZoneOverride(zoneId, temperature)),
                () -> onAccepted.accept(Objects.requireNonNull(accepted.get())));
    }

    public boolean setTemporarySetPoint(String zoneId, double temperature, Instant until) {
        return setTemporarySetPoint(zoneId, temperature, until, accepted -> {
        });
    }

    boolean setTemporarySetPoint(String zoneId, double temperature, Instant until,
            Consumer<AcceptedSetPoint> onAccepted) {
        logger.trace("Account {}: temporary setpoint command for zone {}: {} Celsius until {}", getThing().getUID(),
                zoneId, temperature, until);
        EvohomeApiClient client = apiClient;
        AtomicReference<@Nullable AcceptedSetPoint> accepted = new AtomicReference<>();
        return client != null && tryToCall(client, "set temporary setpoint", zoneId,
                () -> accepted.set(client.setHeatingZoneTemporaryOverride(zoneId, temperature, until)),
                () -> onAccepted.accept(Objects.requireNonNull(accepted.get())));
    }

    public @Nullable Instant setTemporarySetPointUntilNextSwitchpoint(String zoneId, double temperature) {
        return setTemporarySetPointUntilNextSwitchpoint(zoneId, temperature, accepted -> {
        });
    }

    @Nullable
    Instant setTemporarySetPointUntilNextSwitchpoint(String zoneId, double temperature,
            Consumer<AcceptedSetPoint> onAccepted) {
        EvohomeApiClient client = apiClient;
        if (client == null) {
            return null;
        }
        AtomicReference<@Nullable AcceptedSetPoint> accepted = new AtomicReference<>();
        boolean success = tryToCall(client, "set temporary setpoint until next switchpoint", zoneId,
                () -> accepted.set(client.setHeatingZoneTemporaryOverrideUntilNextSwitchpoint(zoneId, temperature)),
                () -> onAccepted.accept(Objects.requireNonNull(accepted.get())));
        return success ? Objects.requireNonNull(accepted.get()).until() : null;
    }

    public boolean cancelSetPointOverride(String zoneId) {
        return cancelSetPointOverride(zoneId, () -> {
        });
    }

    boolean cancelSetPointOverride(String zoneId, Runnable onAccepted) {
        logger.trace("Account {}: cancel setpoint override command for zone {}", getThing().getUID(), zoneId);
        EvohomeApiClient localApiCLient = apiClient;
        if (localApiCLient != null) {
            return tryToCall(localApiCLient, "cancel setpoint override", zoneId,
                    () -> localApiCLient.cancelHeatingZoneOverride(zoneId), onAccepted);
        }
        return false;
    }

    void executeZoneCommand(Runnable command) {
        EvohomeApiClient client = apiClient;
        if (client != null) {
            synchronized (client) {
                if (client.equals(apiClient)) {
                    // Select the mode and expiry after earlier accepted commands have updated their context.
                    command.run();
                }
            }
        }
    }

    public void addAccountStatusListener(AccountStatusListener listener) {
        listeners.add(listener);
        listener.accountStatusChanged(getThing().getStatus());
    }

    public void removeAccountStatusListener(AccountStatusListener listener) {
        listeners.remove(listener);
    }

    private boolean tryToCall(EvohomeApiClient client, String operation, String targetId, RunnableWithTimeout action) {
        return tryToCall(client, operation, targetId, action, () -> {
        });
    }

    private boolean tryToCall(EvohomeApiClient client, String operation, String targetId, RunnableWithTimeout action,
            Runnable onAccepted) {
        synchronized (client) {
            if (!client.equals(apiClient)) {
                return false;
            }
            try {
                action.run();
                synchronized (this) {
                    if (client.equals(apiClient)) {
                        // Keep accepted command context in the same order as the serialized API requests.
                        onAccepted.run();
                        logger.trace("Account {}: command '{}' accepted for {}; status readback scheduled in 2 s",
                                getThing().getUID(), operation, targetId);
                        scheduleRefresh(client, 2);
                        return true;
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Command '{}' rejected for {}: {}", operation, targetId, e.getMessage());
            } catch (TimeoutException | EvohomeApiClientException e) {
                synchronized (this) {
                    if (client.equals(apiClient)) {
                        logger.trace("Account {}: command '{}' failed for {}", getThing().getUID(), operation,
                                targetId);
                        if (e instanceof EvohomeApiClientException apiFailure
                                && (apiFailure.getStatusCode() == 400 || apiFailure.getStatusCode() == 404
                                        || apiFailure.getStatusCode() == 409 || apiFailure.getStatusCode() == 422)) {
                            logger.debug("Command '{}' rejected by API for {} (HTTP {})", operation, targetId,
                                    apiFailure.getStatusCode());
                            // A multi-request reset may have partially succeeded. Read back the actual state.
                            if (retryDelay == 0) {
                                scheduleRefresh(client, 2);
                            }
                        } else {
                            handleFailure(client, e);
                        }
                    }
                }
            }
        }
        return false;
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
        } else if (configuration.refreshInterval < 15 || configuration.refreshInterval > 3000) {
            errorMessage = "Refresh interval must be between 15 and 3000 seconds";
        } else if (configuration.scheduleRefreshInterval < 1 || configuration.scheduleRefreshInterval > 1440) {
            errorMessage = "Schedule refresh interval must be between 1 and 1440 minutes";
        } else {
            return true;
        }

        updateAccountStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
        return false;
    }

    private void scheduleRefresh(EvohomeApiClient client, long delay) {
        ScheduledFuture<?> task = refreshTask;
        if (task != null) {
            task.cancel(false);
        }
        long generation = ++refreshGeneration;
        logger.trace("Account {}: next status request scheduled in {} s", getThing().getUID(), delay);
        refreshTask = refreshScheduler.schedule(() -> update(client, generation), delay, TimeUnit.SECONDS);
    }

    private void update(EvohomeApiClient client, long generation) {
        synchronized (client) {
            synchronized (this) {
                if (!client.equals(apiClient) || generation != refreshGeneration) {
                    return;
                }
            }
            long started = System.nanoTime();
            try {
                if (!client.isLoggedIn()) {
                    logger.trace("Account {}: authenticating and loading installation information",
                            getThing().getUID());
                    client.login();
                    logger.trace("Account {}: installation loaded, locations={}", getThing().getUID(),
                            client.getInstallationInfo().size());
                }
                if (!checkInstallationInfoHasDuplicateIds(client.getInstallationInfo())) {
                    throw new EvohomeApiClientException("Duplicate installation IDs");
                }
                client.update();
                synchronized (this) {
                    if (!client.equals(apiClient) || generation != refreshGeneration) {
                        return;
                    }
                    retryDelay = 0;
                }
                long scheduleRetryDelay = refreshOneSchedule(client, generation);
                synchronized (this) {
                    if (!client.equals(apiClient) || generation != refreshGeneration) {
                        return;
                    }
                    retryDelay = scheduleRetryDelay;
                    updateAccountStatus(ThingStatus.ONLINE);
                    updateThings();
                    updateState(EvohomeBindingConstants.ACCOUNT_LAST_SUCCESSFUL_UPDATE_CHANNEL,
                            new DateTimeType(ZonedDateTime.now(ZoneOffset.UTC)));
                    logger.trace("Account {}: poll completed in {} ms; published lastSuccessfulUpdate",
                            getThing().getUID(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
                    scheduleRefresh(client, retryDelay > 0 ? retryDelay : configuration.refreshInterval);
                }
            } catch (TimeoutException | EvohomeApiClientException e) {
                synchronized (this) {
                    if (client.equals(apiClient) && generation == refreshGeneration) {
                        handleFailure(client, e);
                    }
                }
            } catch (RuntimeException e) {
                synchronized (this) {
                    if (client.equals(apiClient) && generation == refreshGeneration) {
                        logger.debug("Failed to process installation status", e);
                        handleFailure(client, new EvohomeApiClientException("Invalid installation status"));
                    }
                }
            }
        }
    }

    private long refreshOneSchedule(EvohomeApiClient client, long generation) {
        String zoneId = null;
        synchronized (this) {
            if (!client.equals(apiClient) || generation != refreshGeneration) {
                return 0;
            }
            var forced = pendingScheduleRefresh.iterator();
            if (forced.hasNext()) {
                zoneId = forced.next();
                forced.remove();
            } else {
                Instant now = Instant.now();
                Instant oldest = Instant.MAX;
                for (Thing thing : getThing().getThings()) {
                    if (thing.getHandler() instanceof EvohomeHeatingZoneHandler zoneHandler
                            && zoneHandler.hasScheduleLinks() && client.isZoneScheduleDue(zoneHandler.getId(), now,
                                    configuration.scheduleRefreshInterval)) {
                        CachedSchedule cached = client.getCachedZoneSchedule(zoneHandler.getId());
                        if (cached == null) {
                            zoneId = zoneHandler.getId();
                            break;
                        }
                        if (zoneId == null || cached.updatedAt().isBefore(oldest)) {
                            zoneId = zoneHandler.getId();
                            oldest = cached.updatedAt();
                        }
                    }
                }
            }
        }
        if (zoneId == null) {
            return 0;
        }
        try {
            client.readZoneSchedule(zoneId);
            return 0;
        } catch (TimeoutException e) {
            return deferScheduleFailure(client, generation, zoneId, "request timed out", 0);
        } catch (EvohomeApiClientException e) {
            long serverDelay = e.getStatusCode() == 429 || e.getStatusCode() == 503
                    ? e.getRetryAfterSeconds() > 0 ? e.getRetryAfterSeconds() : 60
                    : 0;
            return deferScheduleFailure(client, generation, zoneId, e.getMessage(), serverDelay);
        } catch (IllegalArgumentException | DateTimeException e) {
            return deferScheduleFailure(client, generation, zoneId, e.getMessage(), 0);
        }
    }

    private long deferScheduleFailure(EvohomeApiClient client, long generation, String zoneId, @Nullable String message,
            long serverDelay) {
        synchronized (this) {
            if (!client.equals(apiClient)) {
                return 0;
            }
            client.deferZoneScheduleRefresh(zoneId, Math.max(300, serverDelay));
            logger.debug("Schedule read failed for zone {}: {}; retaining the last known schedule", zoneId, message);
            if (serverDelay > 0) {
                retryDelay = serverDelay;
                // A refresh queued during this request must also honor the provider's cooldown.
                if (generation != refreshGeneration) {
                    scheduleRefresh(client, serverDelay);
                }
            }
        }
        return serverDelay;
    }

    private void handleFailure(EvohomeApiClient client, Exception failure) {
        long serverDelay = failure instanceof EvohomeApiClientException apiFailure ? apiFailure.getRetryAfterSeconds()
                : 0;
        retryDelay = Math.max(serverDelay, retryDelay == 0 ? Math.max(15, configuration.refreshInterval)
                : Math.min(Math.max(900, configuration.refreshInterval), Math.min(3000, retryDelay) * 2));
        logger.trace(
                "Account {}: request failed ({}); retry in {} s, server retry delay={} s; retaining last known values",
                getThing().getUID(), failure instanceof TimeoutException ? "request timed out" : failure.getMessage(),
                retryDelay, serverDelay);
        updateAccountStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                failure instanceof TimeoutException ? "API request timed out" : failure.getMessage());
        scheduleRefresh(client, retryDelay);
    }

    private void updateAccountStatus(ThingStatus newStatus) {
        updateAccountStatus(newStatus, ThingStatusDetail.NONE, null);
    }

    private void updateAccountStatus(ThingStatus newStatus, ThingStatusDetail detail, @Nullable String message) {
        // Prevent spamming the log file
        if (!newStatus.equals(getThing().getStatus()) || detail != getThing().getStatusInfo().getStatusDetail()
                || !Objects.equals(message, getThing().getStatusInfo().getDescription())) {
            logger.trace("Account {}: status {} -> {}, detail={}, reason={}", getThing().getUID(),
                    getThing().getStatus(), newStatus, detail, message);
            if (detail == ThingStatusDetail.NONE) {
                updateStatus(newStatus, message);
            } else {
                updateStatus(detail, message);
            }
            updateListeners(newStatus);
        }
    }

    private void updateListeners(ThingStatus status) {
        for (AccountStatusListener listener : listeners) {
            listener.accountStatusChanged(status);
        }
    }

    private void updateThings() {
        Instant scheduleNow = Instant.now();
        Map<String, TemperatureControlSystemStatus> idToTcsMap = new HashMap<>();
        Map<String, ZoneStatus> idToZoneMap = new HashMap<>();
        Map<String, GatewayStatus> tcsIdToGatewayMap = new HashMap<>();
        Map<String, String> zoneIdToTcsIdMap = new HashMap<>();
        Map<String, ThingStatus> idToTcsThingsStatusMap = new HashMap<>();
        int locationCount = 0;
        int controllerThings = 0;
        int zoneThings = 0;

        EvohomeApiClient localApiClient = apiClient;
        if (localApiClient != null) {
            // First, create a lookup table
            LocationsStatus localLocationsStatus = localApiClient.getInstallationStatus();
            if (localLocationsStatus != null) {
                locationCount = localLocationsStatus.size();
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
                controllerThings++;
                logger.trace("Account {}: mapping controller {} to Thing {}; statusFound={}", getThing().getUID(),
                        tcsHandler.getId(), handler.getUID(), idToTcsMap.containsKey(tcsHandler.getId()));
                tcsHandler.update(tcsIdToGatewayMap.get(tcsHandler.getId()), idToTcsMap.get(tcsHandler.getId()));
                idToTcsThingsStatusMap.put(tcsHandler.getId(), tcsHandler.getThing().getStatus());
            }
        }
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof EvohomeHeatingZoneHandler zoneHandler) {
                var update = zoneHandler.captureUpdate();
                if (update == null) {
                    continue;
                }
                String zoneId = update.zoneId();
                zoneThings++;
                logger.trace("Account {}: mapping zone {} to Thing {}; statusFound={}, controllerId={}",
                        getThing().getUID(), zoneId, thing.getUID(), idToZoneMap.containsKey(zoneId),
                        zoneIdToTcsIdMap.get(zoneId));
                zoneHandler.update(update, idToTcsThingsStatusMap.get(zoneIdToTcsIdMap.get(zoneId)),
                        idToZoneMap.get(zoneId));
                zoneHandler.updateSchedule(update,
                        localApiClient == null ? null : localApiClient.getCachedZoneSchedule(zoneId), scheduleNow);
            }
        }
        logger.trace(
                "Account {}: status processed, locations={}, controllers={}, zones={}, controllerThings={}, zoneThings={}",
                getThing().getUID(), locationCount, idToTcsMap.size(), idToZoneMap.size(), controllerThings,
                zoneThings);
    }
}
