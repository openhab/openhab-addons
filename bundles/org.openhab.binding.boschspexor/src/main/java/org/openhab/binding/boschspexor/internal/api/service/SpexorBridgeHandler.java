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
package org.openhab.binding.boschspexor.internal.api.service;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.boschspexor.internal.api.model.Spexor;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService.SpexorAuthGrantState;
import org.openhab.binding.boschspexor.internal.discovery.BoschSpexorDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpexorBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SpexorBridgeHandler.class);

    // private @NonNullByDefault Future<?> pollingFuture;
    private BoschSpexorBridgeConfig bridgeConfig;
    private final @NonNull SpexorAuthorizationService authService;
    private final @NonNull SpexorAPIService apiService;

    private BoschSpexorDiscoveryService discoveryService;

    private @NonNull ChannelUID spexorsChannelUID;

    public SpexorBridgeHandler(Bridge bridge, @NonNull SpexorAuthorizationService authService,
            @NonNull SpexorAPIService apiService) {
        super(bridge);
        this.authService = authService;
        this.apiService = apiService;
        spexorsChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_SPEXORS);
        initialize();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Initializing Bosch spexor BridgeHandler...");
        bridgeConfig = getConfigAs(BoschSpexorBridgeConfig.class);
        authService.setConfig(bridgeConfig);
        updateStatus(ThingStatus.UNINITIALIZED);
        if (authService.isRegistered()) {
            updateStatus(ThingStatus.INITIALIZING);
            authService.authorize();
            if (isAuthorized()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please register your openHAB and visit this website: http(s)://<YOUROPENHAB>:<YOURPORT>"
                            + SPEXOR_OPENHAB_URL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            discoveryService.startScan(null);
        }
    }

    @Override
    public void dispose() {
        super.scheduler.shutdown();
    }

    public BoschSpexorBridgeConfig getBridgeConfig() {
        return bridgeConfig;
    }

    public boolean isAuthorized() {
        return SpexorAuthGrantState.AUTHORIZED.equals(this.authService.getStatus().getState());
    }

    public List<Spexor> listSpexors() {
        List<Spexor> result = Collections.emptyList();
        if (isAuthorized()) {
            List<Spexor> spexors = apiService.getSpexors();
            if (spexors != null) {
                result = spexors;
            }
        }
        return result;
    }

    public ThingUID getUID() {
        return this.thing.getUID();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(BoschSpexorDiscoveryService.class);
    }

    /**
     * Called by the discovery service to let this handler have a reference.
     */
    public void setDiscoveryService(BoschSpexorDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }
}
