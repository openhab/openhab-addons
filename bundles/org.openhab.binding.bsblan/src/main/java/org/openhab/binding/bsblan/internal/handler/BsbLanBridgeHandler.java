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
package org.openhab.binding.bsblan.internal.handler;

import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bsblan.internal.api.BsbLanApiCaller;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterQueryResponseDTO;
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge for BSB-LAN devices.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BsbLanBridgeHandler.class);
    private final Set<BsbLanBaseThingHandler> things = new HashSet<>();
    private BsbLanBridgeConfiguration bridgeConfig = new BsbLanBridgeConfiguration();
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> debouncedInit;
    private @Nullable BsbLanApiParameterQueryResponseDTO cachedParameterQueryResponse;

    public BsbLanBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void registerThing(final BsbLanBaseThingHandler parameter) {
        this.things.add(parameter);

        // To avoid having to wait up to refreshInterval seconds until values are updated
        // for the added thing, we trigger a debounced refresh to shorten the delay.
        // Alternatively the thing itself could make an additional REST call
        // on initialization but this would flood the device when lots of parameters are setup.

        // use a local variable to avoid the build warning "Potential null pointer access"
        ScheduledFuture<?> localDebouncedInit = debouncedInit;
        if (localDebouncedInit == null || localDebouncedInit.isCancelled() || localDebouncedInit.isDone()) {
            debouncedInit = scheduler.schedule(this::doRefresh, 2, TimeUnit.SECONDS);
        }
    }

    @Override
    public void initialize() {
        bridgeConfig = getConfigAs(BsbLanBridgeConfiguration.class);

        // validate 'host' configuration
        String host = bridgeConfig.host;
        if (host == null || host.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'host' is mandatory and must be configured");
            return;
        }

        // validate 'refreshInterval' configuration
        if (bridgeConfig.refreshInterval != null && bridgeConfig.refreshInterval < MIN_REFRESH_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("Parameter 'refreshInterval' must be at least %d seconds", MIN_REFRESH_INTERVAL));
            return;
        }

        if (bridgeConfig.port == null) {
            bridgeConfig.port = DEFAULT_API_PORT;
        }

        // all checks succeeded, start refreshing
        startAutomaticRefresh(bridgeConfig);
    }

    @Override
    public void dispose() {
        // use a local variable to avoid the build warning "Potential null pointer access"
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
        // use a local variable to avoid the build warning "Potential null pointer access"
        ScheduledFuture<?> localDebouncedInit = debouncedInit;
        if (localDebouncedInit != null) {
            localDebouncedInit.cancel(true);
        }
        things.clear();
    }

    public @Nullable BsbLanApiParameterQueryResponseDTO getCachedParameterQueryResponse() {
        return cachedParameterQueryResponse;
    }

    public BsbLanBridgeConfiguration getBridgeConfiguration() {
        return bridgeConfig;
    }

    private void doRefresh() {
        logger.trace("Refreshing parameter values");

        BsbLanApiCaller apiCaller = new BsbLanApiCaller(bridgeConfig);

        // refresh all parameters
        Set<Integer> parameterIds = things.stream() //
                .filter(thing -> thing instanceof BsbLanParameterHandler) //
                .map(thing -> (BsbLanParameterHandler) thing) //
                .map(thing -> thing.getParameterId()) //
                .collect(Collectors.toSet());

        cachedParameterQueryResponse = apiCaller.queryParameters(parameterIds);

        // InetAddress.isReachable(...) check returned false on RPi although the device is reachable (worked on
        // Windows).
        // Therefore we check status depending on the response.
        if (cachedParameterQueryResponse == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Did not receive a response from BSB-LAN device. Check your configuration and if device is online.");
            // continue processing, so things can go to OFFLINE too
        } else {
            // response received, tread device as reachable, refresh state now
            updateStatus(ThingStatus.ONLINE);
        }

        for (BsbLanBaseThingHandler parameter : things) {
            parameter.refresh(bridgeConfig);
        }
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh(BsbLanBridgeConfiguration config) {
        // use a local variable to avoid the build warning "Potential null pointer access"
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            int interval = (config.refreshInterval != null) ? config.refreshInterval.intValue()
                    : DEFAULT_REFRESH_INTERVAL;
            refreshJob = scheduler.scheduleWithFixedDelay(this::doRefresh, 0, interval, TimeUnit.SECONDS);
        }
    }
}
