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
package org.openhab.binding.boschindego.internal.handler;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.boschindego.internal.AuthorizationController;
import org.openhab.binding.boschindego.internal.AuthorizationListener;
import org.openhab.binding.boschindego.internal.AuthorizationProvider;
import org.openhab.binding.boschindego.internal.IndegoController;
import org.openhab.binding.boschindego.internal.discovery.IndegoDiscoveryService;
import org.openhab.binding.boschindego.internal.dto.response.DevicePropertiesResponse;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoschAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class BoschAccountHandler extends BaseBridgeHandler implements AuthorizationListener {

    private final Logger logger = LoggerFactory.getLogger(BoschAccountHandler.class);
    private final OAuthFactory oAuthFactory;
    private final Set<AuthorizationListener> authorizationListeners = ConcurrentHashMap.newKeySet();

    private OAuthClientService oAuthClientService;
    private AuthorizationController authorizationController;
    private IndegoController controller;

    public BoschAccountHandler(Bridge bridge, HttpClient httpClient, OAuthFactory oAuthFactory) {
        super(bridge);

        this.oAuthFactory = oAuthFactory;

        oAuthClientService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), BSK_TOKEN_URI,
                BSK_AUTH_URI, BSK_CLIENT_ID, null, BSK_SCOPE, false);
        authorizationController = new AuthorizationController(oAuthClientService, this);
        controller = new IndegoController(httpClient, authorizationController);
    }

    @Override
    public void initialize() {
        OAuthClientService oAuthClientService = oAuthFactory.getOAuthClientService(thing.getUID().getAsString());
        if (oAuthClientService == null) {
            throw new IllegalStateException("OAuth handle doesn't exist");
        }
        authorizationController.setOAuthClientService(oAuthClientService);
        this.oAuthClientService = oAuthClientService;

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                authorizationController.getAccessToken();
                updateStatus(ThingStatus.ONLINE);
            } catch (OAuthException | OAuthResponseException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.conf-error.oauth2-unauthorized");
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "@text/offline.comm-error.oauth2-authorization-failed");
            }
        });
    }

    @Override
    public void dispose() {
        oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
        authorizationListeners.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void handleRemoval() {
        oAuthFactory.deleteServiceAndAccessToken(thing.getUID().getAsString());
        super.handleRemoval();
    }

    public AuthorizationProvider getAuthorizationProvider() {
        return authorizationController;
    }

    public void registerAuthorizationListener(AuthorizationListener listener) {
        if (!authorizationListeners.add(listener)) {
            throw new IllegalStateException("Attempt to register already registered authorization listener");
        }
    }

    public void unregisterAuthorizationListener(AuthorizationListener listener) {
        if (!authorizationListeners.remove(listener)) {
            throw new IllegalStateException("Attempt to unregister authorization listener which is not registered");
        }
    }

    public void onSuccessfulAuthorization() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void onFailedAuthorization(Throwable throwable) {
        logger.debug("Authorization failure", throwable);
        if (throwable instanceof IndegoAuthenticationException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, throwable.getMessage());
        }
    }

    public void onAuthorizationFlowCompleted() {
        // Ignore
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(IndegoDiscoveryService.class);
    }

    public void authorize(String authCode) throws IndegoAuthenticationException {
        logger.info("Attempting to authorize using authorization code");

        try {
            oAuthClientService.getAccessTokenResponseByAuthorizationCode(authCode, BSK_REDIRECT_URI);
        } catch (OAuthException | OAuthResponseException | IOException e) {
            throw new IndegoAuthenticationException("Failed to authorize by authorization code " + authCode, e);
        }

        logger.info("Authorization completed successfully");

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.authorization-completed");

        authorizationListeners.forEach(l -> l.onAuthorizationFlowCompleted());
    }

    public Collection<DevicePropertiesResponse> getDevices() throws IndegoException {
        Collection<String> serialNumbers = controller.getSerialNumbers();
        List<DevicePropertiesResponse> devices = new ArrayList<DevicePropertiesResponse>(serialNumbers.size());

        for (String serialNumber : serialNumbers) {
            DevicePropertiesResponse properties = controller.getDeviceProperties(serialNumber);
            if (properties.serialNumber == null) {
                properties.serialNumber = serialNumber;
            }
            devices.add(properties);
        }

        return devices;
    }
}
