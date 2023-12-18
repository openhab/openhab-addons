/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.thekeys.internal.gateway;

import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.PROPERTY_VERSION;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.thekeys.internal.api.GatewayService;
import org.openhab.binding.thekeys.internal.api.TheKeysHttpClient;
import org.openhab.binding.thekeys.internal.api.model.GatewayInfosDTO;
import org.openhab.binding.thekeys.internal.api.model.LockerDTO;
import org.openhab.binding.thekeys.internal.provider.TheKeyTranslationProvider;
import org.openhab.binding.thekeys.internal.smartlock.TheKeysDiscoveryService;
import org.openhab.binding.thekeys.internal.smartlock.TheKeysSmartlockHandler;
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
 * The {@link TheKeysGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class TheKeysGatewayHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TheKeysGatewayHandler.class);
    private final TheKeyTranslationProvider translationProvider;
    private @Nullable GatewayService api;

    private @Nullable ScheduledFuture<?> gwPollingJob;

    public TheKeysGatewayHandler(Bridge bridge, TheKeyTranslationProvider translationProvider) {
        super(bridge);
        this.translationProvider = translationProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // do nothing
    }

    @Override
    public void initialize() {
        TheKeysGatewayConfiguration config = getConfigAs(TheKeysGatewayConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY,
                "@text/message.status.wait-data-from-gateway");
        api = new GatewayService(config, new TheKeysHttpClient());
        gwPollingJob = scheduler.scheduleWithFixedDelay(this::pollGateway, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    /**
     * Fetch the state of locks and notify the corresponding thing handler
     */
    private void pollGateway() {
        try {
            GatewayInfosDTO gwInfos = api.getGatewayInfos();
            updateProperty(PROPERTY_VERSION, String.valueOf(gwInfos.getVersion()));

            // Here the api answer without errors => back online
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            // No need to fetch locks data when none is linked
            if (noLockThingIsEnabled()) {
                return;
            }

            // Try to match and update lock handler from api response
            List<TheKeysSmartlockHandler> locksThing = getThing().getThings().stream().map(Thing::getHandler)
                    .map(TheKeysSmartlockHandler.class::cast).toList();

            Map<Integer, LockerDTO> locksResponse = api.getLocks().stream()
                    .collect(Collectors.toMap(LockerDTO::getIdentifier, Function.identity()));

            // Update each handler with corresponding data
            for (TheKeysSmartlockHandler lockHandler : locksThing) {
                LockerDTO lockData = locksResponse.get(lockHandler.getLockIdentifier());
                if (lockData == null) {
                    lockHandler.onLockNotFoundOnGateway();
                } else {
                    lockHandler.updateLockState(lockData);
                }
            }

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            logger.debug("Failed to fetch data from gateway", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Return true if at least one thing associated to the gateway is enabled
     */
    private boolean noLockThingIsEnabled() {
        return getThing().getThings().stream().noneMatch(Thing::isEnabled);
    }

    @Override
    public void dispose() {
        if (gwPollingJob != null && !gwPollingJob.isCancelled()) {
            gwPollingJob.cancel(true);
            gwPollingJob = null;
        }
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(TheKeysDiscoveryService.class);
    }

    public @Nullable GatewayService getApi() {
        return api;
    }

    public TheKeyTranslationProvider getTranslationProvider() {
        return translationProvider;
    }
}
