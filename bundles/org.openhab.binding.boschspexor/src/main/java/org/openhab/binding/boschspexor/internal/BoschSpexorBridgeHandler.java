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
package org.openhab.binding.boschspexor.internal;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.SPEXOR_OPENHAB_URL;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.boschspexor.internal.api.model.Spexor;
import org.openhab.binding.boschspexor.internal.api.service.SpexorAPIService;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationProcessListener;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService.SpexorAuthGrantState;
import org.openhab.binding.boschspexor.internal.discovery.BoschSpexorDiscoveryService;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spexor Bridge Handler
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class BoschSpexorBridgeHandler extends BaseBridgeHandler implements SpexorAuthorizationProcessListener {
    private final Logger logger = LoggerFactory.getLogger(BoschSpexorBridgeHandler.class);

    private BoschSpexorBridgeConfig bridgeConfig;
    private SpexorAuthorizationService authService;
    private SpexorAPIService apiService;

    private Optional<BoschSpexorDiscoveryService> discoveryService = Optional.empty();

    public BoschSpexorBridgeHandler(Bridge bridge, HttpClient httpClient, StorageService storageService) {
        super(bridge);
        bridgeConfig = getConfigAs(BoschSpexorBridgeConfig.class);
        this.authService = new SpexorAuthorizationService(httpClient, storageService, this);
        this.apiService = new SpexorAPIService(authService);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Bosch spexor BridgeHandler... using {}", bridgeConfig.getHost());
        authService.setConfig(bridgeConfig);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.INITIALIZING.NONE);
        if (authService.isRegistered()) {
            authService.authorize();
            updateStatus();
        } else if (authService.isRequestPending()) {
            authService.authorize();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please register your openHAB and visit this website: http(s)://<YOUROPENHAB>:<YOURPORT>"
                            + SPEXOR_OPENHAB_URL);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please register your openHAB and visit this website: http(s)://<YOUROPENHAB>:<YOURPORT>"
                            + SPEXOR_OPENHAB_URL);
        }
    }

    private void updateStatus() {
        if (isAuthorized()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @SuppressWarnings("null") // instanceof is also checking null - warning can be ignored
    public void discoverChannels() {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof BoschSpexorThingHandler) {
                BoschSpexorThingHandler thingHandler = (BoschSpexorThingHandler) thing.getHandler();
                thingHandler.discoverChannels();
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command '{}' received for channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            discoveryService.ifPresent(service -> service.startScan(null));
        }
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
            result = apiService.getSpexors();
        }
        return result;
    }

    public ThingUID getUID() {
        return this.thing.getUID();
    }

    public SpexorAPIService getApiService() {
        return apiService;
    }

    public SpexorAuthorizationService getAuthService() {
        return authService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(BoschSpexorDiscoveryService.class);
    }

    /**
     * Called by the discovery service to let this handler have a reference.
     */
    public void setDiscoveryService(@Nullable BoschSpexorDiscoveryService discoveryService) {
        this.discoveryService = Optional.ofNullable(discoveryService);
    }

    @Override
    public void changedState(SpexorAuthGrantState oldState, SpexorAuthGrantState newState) {
        updateStatus();
    }
}
