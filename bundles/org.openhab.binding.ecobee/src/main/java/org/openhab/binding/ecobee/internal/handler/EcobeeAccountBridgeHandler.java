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
package org.openhab.binding.ecobee.internal.handler;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.CONFIG_THERMOSTAT_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ecobee.internal.api.EcobeeApi;
import org.openhab.binding.ecobee.internal.config.EcobeeAccountConfiguration;
import org.openhab.binding.ecobee.internal.discovery.EcobeeDiscoveryService;
import org.openhab.binding.ecobee.internal.dto.SelectionDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatUpdateRequestDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.summary.SummaryResponseDTO;
import org.openhab.binding.ecobee.internal.function.FunctionRequest;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcobeeAccountBridgeHandler} is responsible for managing
 * communication with the Ecobee API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeAccountBridgeHandler extends BaseBridgeHandler {

    private static final int REFRESH_STARTUP_DELAY_SECONDS = 3;
    private static final int REFRESH_INTERVAL_SECONDS = 1;
    private static final int DEFAULT_REFRESH_INTERVAL_NORMAL_SECONDS = 20;
    private static final int DEFAULT_REFRESH_INTERVAL_QUICK_SECONDS = 5;
    private static final int DEFAULT_API_TIMEOUT_SECONDS = 20;

    private final Logger logger = LoggerFactory.getLogger(EcobeeAccountBridgeHandler.class);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    private @NonNullByDefault({}) EcobeeApi api;
    private @NonNullByDefault({}) String apiKey;
    private int refreshIntervalNormal;
    private int refreshIntervalQuick;
    private int apiTimeout;
    private boolean discoveryEnabled;

    private final Map<String, EcobeeThermostatBridgeHandler> thermostatHandlers = new ConcurrentHashMap<>();
    private final Set<String> thermostatIds = new CopyOnWriteArraySet<>();

    private @Nullable Future<?> refreshThermostatsJob;
    private final AtomicInteger refreshThermostatsCounter = new AtomicInteger(REFRESH_STARTUP_DELAY_SECONDS);

    private @Nullable SummaryResponseDTO previousSummary;

    public EcobeeAccountBridgeHandler(final Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("AccountBridge: Initializing");

        EcobeeAccountConfiguration config = getConfigAs(EcobeeAccountConfiguration.class);
        apiKey = config.apiKey;

        Integer value;
        value = config.refreshIntervalNormal;
        refreshIntervalNormal = value == null ? DEFAULT_REFRESH_INTERVAL_NORMAL_SECONDS : value;

        value = config.refreshIntervalQuick;
        refreshIntervalQuick = value == null ? DEFAULT_REFRESH_INTERVAL_QUICK_SECONDS : value;

        value = config.apiTimeout;
        apiTimeout = (value == null ? DEFAULT_API_TIMEOUT_SECONDS : value) * 1000;

        Boolean booleanValue = config.discoveryEnabled;
        discoveryEnabled = booleanValue == null ? false : booleanValue.booleanValue();
        logger.debug("AccountBridge: Thermostat and sensor discovery is {}", discoveryEnabled ? "enabled" : "disabled");

        api = new EcobeeApi(this, apiKey, apiTimeout, oAuthFactory, httpClient);

        scheduleRefreshJob();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Checking authorization");
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        api.closeOAuthClientService();
        logger.debug("AccountBridge: Disposing");
    }

    @Override
    public void handleRemoval() {
        oAuthFactory.deleteServiceAndAccessToken(thing.getUID().getAsString());
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EcobeeDiscoveryService.class);
    }

    @Override
    public void childHandlerInitialized(ThingHandler thermostatHandler, Thing thermostatThing) {
        String thermostatId = (String) thermostatThing.getConfiguration().get(CONFIG_THERMOSTAT_ID);
        thermostatHandlers.put(thermostatId, (EcobeeThermostatBridgeHandler) thermostatHandler);
        thermostatIds.add(thermostatId);
        scheduleQuickPoll();
        logger.debug("AccountBridge: Adding thermostat handler for {} with id {}", thermostatThing.getUID(),
                thermostatId);
    }

    @Override
    public void childHandlerDisposed(ThingHandler thermostatHandler, Thing thermostatThing) {
        String thermostatId = (String) thermostatThing.getConfiguration().get(CONFIG_THERMOSTAT_ID);
        thermostatHandlers.remove(thermostatId);
        thermostatIds.remove(thermostatId);
        logger.debug("AccountBridge: Removing thermostat handler for {} with id {}", thermostatThing.getUID(),
                thermostatId);
    }

    public boolean isBackgroundDiscoveryEnabled() {
        return discoveryEnabled;
    }

    public void updateBridgeStatus(ThingStatus status) {
        updateStatus(status);
    }

    public void updateBridgeStatus(ThingStatus status, ThingStatusDetail statusDetail, String statusMessage) {
        updateStatus(status, statusDetail, statusMessage);
    }

    public boolean performThermostatFunction(FunctionRequest request) {
        boolean success = api.performThermostatFunction(request);
        if (success) {
            scheduleQuickPoll();
        }
        return success;
    }

    public boolean performThermostatUpdate(ThermostatUpdateRequestDTO request) {
        boolean success = api.performThermostatUpdate(request);
        if (success) {
            scheduleQuickPoll();
        }
        return success;
    }

    public SelectionDTO getSelection() {
        SelectionDTO mergedSelection = new SelectionDTO();
        for (EcobeeThermostatBridgeHandler handler : new ArrayList<>(thermostatHandlers.values())) {
            SelectionDTO selection = handler.getSelection();
            logger.trace("AccountBridge: Thermostat {} selection: {}", handler.getThing().getUID(), selection);
            mergedSelection.mergeSelection(selection);
        }
        return mergedSelection;
    }

    public void markOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public List<ThermostatDTO> getRegisteredThermostats() {
        return api.queryRegisteredThermostats();
    }

    /*
     * The refresh job updates the thermostat channels on the refresh interval set in the thermostat thing config.
     * The thermostat update process involves first running a thermostat summary transaction to
     * determine if any thermostat data has changed since the last summary. If any change is detected,
     * a full query of the thermostats is performed.
     */
    private void refresh() {
        refreshThermostats();
    }

    @SuppressWarnings("null")
    private void refreshThermostats() {
        if (refreshThermostatsCounter.getAndDecrement() == 0) {
            refreshThermostatsCounter.set(refreshIntervalNormal);
            SummaryResponseDTO summary = api.performThermostatSummaryQuery();
            if (summary != null && summary.hasChanged(previousSummary) && !thermostatIds.isEmpty()) {
                for (ThermostatDTO thermostat : api.performThermostatQuery(thermostatIds)) {
                    EcobeeThermostatBridgeHandler handler = thermostatHandlers.get(thermostat.identifier);
                    if (handler != null) {
                        handler.updateChannels(thermostat);
                    }
                }
            }
            previousSummary = summary;
        }
    }

    private void scheduleQuickPoll() {
        if (refreshThermostatsCounter.get() > refreshIntervalQuick) {
            logger.debug("AccountBridge: Scheduling quick poll");
            refreshThermostatsCounter.set(refreshIntervalQuick);
            forceFullNextPoll();
        }
    }

    private void scheduleRefreshJob() {
        logger.debug("AccountBridge: Scheduling thermostat refresh job");
        cancelRefreshJob();
        refreshThermostatsCounter.set(0);
        refreshThermostatsJob = scheduler.scheduleWithFixedDelay(this::refresh, REFRESH_STARTUP_DELAY_SECONDS,
                REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void cancelRefreshJob() {
        Future<?> localRefreshThermostatsJob = refreshThermostatsJob;
        if (localRefreshThermostatsJob != null) {
            forceFullNextPoll();
            localRefreshThermostatsJob.cancel(true);
            logger.debug("AccountBridge: Canceling thermostat refresh job");
        }
    }

    private void forceFullNextPoll() {
        previousSummary = null;
    }
}
