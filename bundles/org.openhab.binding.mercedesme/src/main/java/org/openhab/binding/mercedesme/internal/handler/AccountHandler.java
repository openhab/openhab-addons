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
package org.openhab.binding.mercedesme.internal.handler;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.api.Websocket;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.discovery.MercedesMeDiscoveryService;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeApiException;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeAuthException;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeBindingException;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.Protos.AcknowledgeAssignedVehicles;
import com.daimler.mbcarkit.proto.VehicleEvents.AcknowledgeVEPUpdatesByVIN;
import com.daimler.mbcarkit.proto.VehicleEvents.AcknowledgeVehicleStatusUpdates;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdates;
import com.daimler.mbcarkit.proto.Vehicleapi.AcknowledgeAppTwinCommandStatusUpdatesByVIN;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatusUpdatesByPID;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatusUpdatesByVIN;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinPendingCommandsRequest;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinPendingCommandsResponse;

/**
 * The {@link AccountHandler} acts as Bridge between MercedesMe Account and the associated vehicles
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements AccessTokenRefreshListener {
    private static final int VARIANCE_PERCENT = 15; // 15% variance for refresh interval

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final Map<String, Map<String, Object>> vinCapabilitiesMap = new HashMap<>();
    private final Map<String, VehicleHandler> activeVehicleHandlerMap = new HashMap<>();
    private final Map<String, VEPUpdate> vepUpdateMap = new HashMap<>();
    private final List<String> keepAliveList = new ArrayList<>();
    private final MercedesMeDiscoveryService discoveryService;
    private final LocaleProvider localeProvider;
    private final Storage<String> storage;
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> refreshScheduler;
    private List<PushMessage> eventQueue = new ArrayList<>();
    private boolean updateRunning = false;

    private boolean disposed = true;

    Websocket api;
    AccountConfiguration config = new AccountConfiguration();

    public AccountHandler(Bridge bridge, MercedesMeDiscoveryService mmds, HttpClient hc, LocaleProvider lp,
            StorageService store) {
        super(bridge);
        discoveryService = mmds;
        httpClient = hc;
        localeProvider = lp;
        storage = store.getStorage(Constants.BINDING_ID);
        api = new Websocket(this, httpClient, config, localeProvider, storage);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        disposed = false;
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(AccountConfiguration.class);
        String configValidReason = validateConfig();
        if (!configValidReason.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configValidReason);
        } else {
            api = new Websocket(this, httpClient, config, localeProvider, storage);
            api.websocketDispose(false);
            scheduler.schedule(this::refresh, 2, TimeUnit.SECONDS);
        }
    }

    /**
     * Refresh checking valid authorization and recovery plus selecting update strategy
     */
    public void refresh() {
        if (disposed) {
            logger.debug("AccountHandler is disposed, skipping refresh");
            return;
        }
        if (api.authTokenIsValid()) {
            /**
             * Pattern of the update strategy
             * - every refresh briefly opens (or keeps open) the WebSocket to get a real vehicle status
             * - the REST "vehicleattributes" endpoint is NOT used here: it only ever returns the small
             * Widget-tile attribute set (SOC, ranges, tank levels, lock status, sunroof, park brake, hood,
             * decklid, charging error, timestamp - no ignition/charging state), so it can never trigger
             * keepAlive() and would leave every other channel stale forever
             * - each vehicle still decides on the new attributes if it needs to be kept alive (driving or
             * charging); without that, the socket self-closes again after its randomized 1-3 minute
             * runtime (see Websocket.WS_RUNTIME_MIN/MAX_MS)
             */
            api.websocketUpdate();
        } else {
            // token is not valid - try to resume login
            authorize();
        }
        scheduleRefresh(nextRefreshSeconds());
    }

    private void scheduleRefresh(long delayInSeconds) {
        if (disposed) {
            logger.debug("AccountHandler is disposed, skipping scheduleRefresh");
            return;
        }
        ScheduledFuture<?> localRefreshScheduler = refreshScheduler;
        if (localRefreshScheduler != null) {
            localRefreshScheduler.cancel(false);
        }
        Instant nextSchedule = Instant.now().plus(delayInSeconds, ChronoUnit.SECONDS);
        logger.trace("Next schedule at {}", nextSchedule);
        refreshScheduler = scheduler.schedule(this::refresh, delayInSeconds, TimeUnit.SECONDS);
    }

    public void authorize() {
        try {
            api.login();
        } catch (MercedesMeAuthException e) {
            handleAuthError(e);
        } catch (MercedesMeApiException e) {
            handleApiError(e);
        } catch (MercedesMeBindingException e) {
            handleBindingError(e);
        }
    }

    /**
     * Don't act like a bot!
     * Calls to Mercedes server can be easily identified as bots if they are performed with a constant refresh interval.
     * Introduce a VARIANCE in calling API with plus/minus 15% off from configured refresh interval.
     *
     * @return next refresh offset in seconds
     */
    private long nextRefreshSeconds() {
        // bring in 15% time variance
        int variance = config.refreshInterval * 60 / VARIANCE_PERCENT;
        long leftLimit = config.refreshInterval * 60 - variance;
        long rightLimit = config.refreshInterval * 60 + variance;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }

    private String validateConfig() {
        config = getConfigAs(AccountConfiguration.class);
        if (Constants.NOT_SET.equals(config.email)) {
            return STATUS_CONFIG_EMAIL_MISSING;
        } else if (Constants.NOT_SET.equals(config.password)) {
            return STATUS_CONFIG_PASSWORD_MISSING;
        } else if (Constants.NOT_SET.equals(config.region)) {
            return STATUS_CONFIG_REGION_MISSING;
        } else if (config.refreshInterval < 5) {
            return STATUS_CONFIG_REFRESH_LOW + "[\"" + config.refreshInterval + "\"]";
        } else {
            return Constants.EMPTY;
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        ScheduledFuture<?> localRefreshScheduler = refreshScheduler;
        if (localRefreshScheduler != null) {
            localRefreshScheduler.cancel(false);
        }
        refreshScheduler = null;
        eventQueue.clear();
        api.websocketDispose(true);
    }

    @Override
    public void handleRemoval() {
        storage.remove(config.email);
        super.handleRemoval();
    }

    /**
     * https://next.openhab.org/javadoc/latest/org/openhab/core/auth/client/oauth2/package-summary.html
     */
    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        if (!api.authTokenIsValid()) {
            handleAuthError(new MercedesMeAuthException("Invalid Token"));
        }
    }

    public void registerVin(String vin, VehicleHandler handler) {
        discoveryService.vehicleRemove(this, vin, handler.getThing().getThingTypeUID().getId());
        activeVehicleHandlerMap.put(vin, handler);
        discovery(vin); // update properties for added vehicle
        VEPUpdate updateForVin = vepUpdateMap.get(vin);
        if (updateForVin != null) {
            handler.enqueueUpdate(updateForVin);
        } else {
            scheduleRefresh(1);
        }
    }

    public void unregisterVin(String vin) {
        activeVehicleHandlerMap.remove(vin);
    }

    /**
     * functions for websocket handling
     */

    public void enqueueMessage(PushMessage pm) {
        synchronized (eventQueue) {
            eventQueue.add(pm);
            scheduler.execute(this::scheduleMessage);
        }
    }

    private void scheduleMessage() {
        PushMessage pm;
        synchronized (eventQueue) {
            while (updateRunning) {
                try {
                    eventQueue.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    eventQueue.clear();
                    return;
                }
            }
            if (!eventQueue.isEmpty()) {
                pm = eventQueue.remove(0);
            } else {
                return;
            }
            updateRunning = true;
        }
        try {
            handleMessage(pm);
        } finally {
            synchronized (eventQueue) {
                updateRunning = false;
                eventQueue.notifyAll();
            }
        }
    }

    private void handleMessage(PushMessage pm) {
        // DEBUG (not trace) on purpose - if trace logging isn't enabled for this component this is the one
        // line that still tells us which message types are actually reaching AccountHandler vs. only being
        // seen at the Websocket layer.
        logger.debug("AccountHandler handling message type {}", pm.getMsgCase());
        if (pm.hasVepUpdates()) {
            boolean distributed = distributeVepUpdates(pm.getVepUpdates().getUpdatesMap());
            if (distributed) {
                AcknowledgeVEPUpdatesByVIN ack = AcknowledgeVEPUpdatesByVIN.newBuilder()
                        .setSequenceNumber(pm.getVepUpdates().getSequenceNumber()).build();
                ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeVepUpdatesByVin(ack).build();
                api.sendAcknowledgeMessage(cm);
            }
        } else if (pm.hasVehicleStatusUpdates()) {
            // vehicle-events.proto: typed alternative to VEPUpdate, added in app version 165-1 (PushMessage
            // field 24 / ClientMessage field 28). The generated Java classes already support it, but nothing
            // consumed it yet, so it silently fell into the "not handled" branch below and was never
            // acknowledged. Mapping its ~275 typed fields onto channels is a separate, bigger follow-up -
            // for now we only acknowledge it (required, same MUST-be-acknowledged pattern as the other
            // message types here) and log it so we can confirm the server is actually sending this format.
            VehicleStatusUpdates vsu = pm.getVehicleStatusUpdates();
            logger.debug("Received VehicleStatusUpdates seq {} for {} VIN(s) - not yet mapped to channels",
                    vsu.getSequenceNumber(), vsu.getVehicleStatusUpdatesMap().size());
            AcknowledgeVehicleStatusUpdates ack = AcknowledgeVehicleStatusUpdates.newBuilder()
                    .setSequenceNumber(vsu.getSequenceNumber()).build();
            ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeVehicleStatusUpdates(ack).build();
            api.sendAcknowledgeMessage(cm);
        } else if (pm.hasAssignedVehicles()) {
            for (int i = 0; i < pm.getAssignedVehicles().getVinsCount(); i++) {
                String vin = pm.getAssignedVehicles().getVins(i);
                discovery(vin);
            }
            AcknowledgeAssignedVehicles ack = AcknowledgeAssignedVehicles.newBuilder().build();
            ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeAssignedVehicles(ack).build();
            api.sendAcknowledgeMessage(cm);
        } else if (pm.hasApptwinCommandStatusUpdatesByVin()) {
            AppTwinCommandStatusUpdatesByVIN csubv = pm.getApptwinCommandStatusUpdatesByVin();
            commandStatusUpdate(csubv.getUpdatesByVinMap());
            AcknowledgeAppTwinCommandStatusUpdatesByVIN ack = AcknowledgeAppTwinCommandStatusUpdatesByVIN.newBuilder()
                    .setSequenceNumber(csubv.getSequenceNumber()).build();
            ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeApptwinCommandStatusUpdateByVin(ack).build();
            api.sendAcknowledgeMessage(cm);
        } else if (pm.hasApptwinPendingCommandRequest()) {
            AppTwinPendingCommandsRequest pending = pm.getApptwinPendingCommandRequest();
            if (!pending.getAllFields().isEmpty()) {
                logger.trace("Pending Command {}", pending.getAllFields());
            }
            // vehicleapi.proto: "This request MUST eventually be answered with AppTwinPendingCommandsResponse."
            // We don't track commands across restarts, so we always report an empty pending list. Without
            // this reply the AppTwin actor on the server side appears to never proceed past this handshake
            // step to start pushing regular VEPUpdatesByVIN.
            AppTwinPendingCommandsResponse response = AppTwinPendingCommandsResponse.newBuilder().build();
            ClientMessage cm = ClientMessage.newBuilder().setApptwinPendingCommandsResponse(response).build();
            api.sendAcknowledgeMessage(cm);
            logger.debug("Answered AppTwinPendingCommandsRequest with an empty AppTwinPendingCommandsResponse");
        } else if (pm.hasDebugMessage()) {
            logger.trace("MB Debug Message: {}", pm.getDebugMessage().getMessage());
        } else {
            logger.trace("MB Message: {} not handled", pm.getAllFields());
        }
    }

    public boolean distributeVepUpdates(Map<String, VEPUpdate> map) {
        List<String> notFoundList = new ArrayList<>();
        map.forEach((key, value) -> {
            VehicleHandler h = activeVehicleHandlerMap.get(key);
            if (h != null) {
                h.enqueueUpdate(value);
            } else {
                if (value.getFullUpdate()) {
                    vepUpdateMap.put(key, value);
                }
                notFoundList.add(key);
            }
        });
        notFoundList.forEach(vin -> {
            discovery(vin); // add vehicle to discovery
            logger.trace("No VehicleHandler available for VIN {}", vin);
        });
        return notFoundList.isEmpty();
    }

    public void commandStatusUpdate(Map<String, AppTwinCommandStatusUpdatesByPID> updatesByVinMap) {
        updatesByVinMap.forEach((key, value) -> {
            VehicleHandler h = activeVehicleHandlerMap.get(key);
            if (h != null) {
                h.distributeCommandStatus(value);
            } else {
                logger.trace("No VehicleHandler available for VIN {}", key);
            }
        });
    }

    /**
     * Updates properties for existing handlers or delivers discovery result
     *
     * @param vin of discovered vehicle
     */
    public void discovery(String vin) {
        Map<String, Object> capabilities = vinCapabilitiesMap.get(vin);
        if (capabilities == null) {
            // no capabilities found - retrieve and store them
            capabilities = api.restGetCapabilities(vin);
            vinCapabilitiesMap.put(vin, capabilities);
            VehicleHandler vh = activeVehicleHandlerMap.get(vin);
            if (vh != null) {
                Map<String, String> properties = getStringCapabilities(capabilities);
                properties.putAll(vh.getThing().getProperties());
                vh.getThing().setProperties(properties);
            } else {
                Map<String, Object> discoveryProperties = new HashMap<>(capabilities);
                discoveryProperties.put("vin", vin);
                discoveryService.vehicleDiscovered(this, vin, discoveryProperties);
            }
        }
    }

    private Map<String, String> getStringCapabilities(Map<String, Object> props) {
        Map<String, String> stringProps = new HashMap<>();
        props.forEach((key, value) -> {
            stringProps.put(key, value.toString());
        });
        return stringProps;
    }

    public void sendCommand(@Nullable ClientMessage cm) {
        if (cm != null) {
            api.websocketAddCommand(cm);
        }
    }

    public void keepAlive(String vin, boolean b) {
        if (b) {
            if (!keepAliveList.contains(vin)) {
                keepAliveList.add(vin);
                api.websocketKeepAlive(true);
                scheduleRefresh(1);
            }
        } else {
            keepAliveList.remove(vin);
            if (keepAliveList.isEmpty()) {
                api.websocketKeepAlive(false);
            }
        }
    }

    private void handleAuthError(MercedesMeAuthException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                STATUS_AUTH_FAILURE + " [\"" + e.getMessage() + "\"]");
    }

    private void handleApiError(MercedesMeApiException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                STATUS_API_FAILURE + " [\"" + e.getMessage() + "\"]");
    }

    private void handleBindingError(MercedesMeBindingException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                STATUS_BIDNING_ERROR + " [\"" + e.getMessage() + "\"]");
    }

    public void handleConnected() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void handleWebsocketError(Throwable t) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                STATUS_WEBSOCKET_FAILURE + " [\"" + t.getMessage() + "\"]");
    }

    /**
     * Vehicle Actions
     *
     * @param poi
     */
    public void sendPoi(String vin, JSONObject poi) {
        api.restSendPoi(vin, poi);
    }
}
