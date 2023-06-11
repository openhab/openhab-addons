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
package org.openhab.binding.mielecloud.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.I18NKeys;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefreshListener;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.mielecloud.internal.util.LocaleValidator;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionStatusListener;
import org.openhab.binding.mielecloud.internal.webservice.DeviceStateListener;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.UnavailableMieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceInitializationException;
import org.openhab.binding.mielecloud.internal.webservice.language.CombiningLanguageProvider;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;
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
 * BridgeHandler implementation for the Miele cloud account.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Introduced CombiningLanguageProvider field and interactions, added LanguageProvider super
 *         interface, switched from polling to SSE, added support for multiple bridges, removed e-mail validation
 */
@NonNullByDefault
public class MieleBridgeHandler extends BaseBridgeHandler
        implements OAuthTokenRefreshListener, LanguageProvider, ConnectionStatusListener, DeviceStateListener {
    private static final int NUMBER_OF_SSE_RECONNECTION_ATTEMPTS_BEFORE_STATUS_IS_UPDATED = 6;

    private final Supplier<MieleWebservice> webserviceFactory;

    private final OAuthTokenRefresher tokenRefresher;
    private final CombiningLanguageProvider languageProvider;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private @Nullable CompletableFuture<@Nullable Void> logoutFuture;
    private @Nullable MieleWebservice webService;
    private @Nullable ThingDiscoveryService discoveryService;

    /**
     * Creates a new {@link MieleBridgeHandler}.
     *
     * @param bridge The bridge to handle.
     * @param webserviceFactory Factory for creating {@link MieleWebservice} instances.
     * @param tokenRefresher Token refresher.
     * @param languageProvider Language provider.
     */
    public MieleBridgeHandler(Bridge bridge, Function<ScheduledExecutorService, MieleWebservice> webserviceFactory,
            OAuthTokenRefresher tokenRefresher, CombiningLanguageProvider languageProvider) {
        super(bridge);
        this.webserviceFactory = () -> webserviceFactory.apply(scheduler);
        this.tokenRefresher = tokenRefresher;
        this.languageProvider = languageProvider;
    }

    public void setDiscoveryService(@Nullable ThingDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Gets the current webservice instance for communication with the Miele service.
     *
     * This function may return an {@link UnavailableMieleWebservice} in case no webservice is available at the moment.
     */
    public MieleWebservice getWebservice() {
        MieleWebservice webservice = webService;
        if (webservice != null) {
            return webservice;
        } else {
            return UnavailableMieleWebservice.INSTANCE;
        }
    }

    private String getOAuthServiceHandle() {
        return getConfig().get(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL).toString();
    }

    @Override
    public void initialize() {
        // It is required to set a status in this method as stated in the Javadoc of ThingHandler.initialize
        updateStatus(ThingStatus.UNKNOWN);

        initializeWebservice();
    }

    public void initializeWebservice() {
        try {
            webService = webserviceFactory.get();
        } catch (MieleWebserviceInitializationException e) {
            logger.warn("Failed to initialize webservice.", e);
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        try {
            tokenRefresher.setRefreshListener(this, getOAuthServiceHandle());
        } catch (OAuthException e) {
            logger.debug("Could not initialize Miele Cloud bridge.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    I18NKeys.BRIDGE_STATUS_DESCRIPTION_ACCOUNT_NOT_AUTHORIZED);
            // When the authorization takes place a new initialization will be triggered. Therefore, we can leave the
            // bridge in this state.
            return;
        }
        languageProvider.setPrioritizedLanguageProvider(this);
        tryInitializeWebservice();

        MieleWebservice webservice = getWebservice();
        webservice.addConnectionStatusListener(this);
        webservice.addDeviceStateListener(this);
        if (webservice.hasAccessToken()) {
            webservice.connectSse();
        }
    }

    @Override
    public void handleRemoval() {
        performLogout();
        tokenRefresher.removeTokensFromStorage(getOAuthServiceHandle());
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing {}", this.getClass().getName());
        disposeWebservice();
    }

    public void disposeWebservice() {
        getWebservice().removeConnectionStatusListener(this);
        getWebservice().removeDeviceStateListener(this);
        getWebservice().disconnectSse();
        languageProvider.unsetPrioritizedLanguageProvider();
        tokenRefresher.unsetRefreshListener(getOAuthServiceHandle());

        stopWebservice();
    }

    private void stopWebservice() {
        final MieleWebservice webService = this.webService;
        this.webService = null;
        if (webService == null) {
            return;
        }

        scheduler.submit(() -> {
            CompletableFuture<@Nullable Void> logoutFuture = this.logoutFuture;
            if (logoutFuture != null) {
                try {
                    logoutFuture.get();
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for logout!");
                } catch (ExecutionException e) {
                    logger.warn("Failed to wait for logout.", e);
                }
            }

            try {
                webService.close();
            } catch (Exception e) {
                logger.warn("Failed to close webservice.", e);
            }
        });
    }

    @Override
    public void onNewAccessToken(String accessToken) {
        logger.debug("Setting new access token for webservice access.");
        updateProperty(MieleCloudBindingConstants.PROPERTY_ACCESS_TOKEN, accessToken);

        // Without this the retry would fail causing the thing to go OFFLINE
        getWebservice().setAccessToken(accessToken);

        // If there was no access token during initialization then the SSE connection was not established.
        getWebservice().connectSse();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void performLogout() {
        logoutFuture = new CompletableFuture<>();
        scheduler.execute(() -> {
            try {
                getWebservice().logout();
            } catch (Exception e) {
                logger.warn("Failed to logout from Miele cloud.", e);
            }
            Optional.ofNullable(logoutFuture).map(future -> future.complete(null));
        });
    }

    private void tryInitializeWebservice() {
        Optional<String> accessToken = tokenRefresher.getAccessTokenFromStorage(getOAuthServiceHandle());
        if (!accessToken.isPresent()) {
            logger.debug("No OAuth2 access token available. Retrying later.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    I18NKeys.BRIDGE_STATUS_DESCRIPTION_ACCESS_TOKEN_NOT_CONFIGURED);
            return;
        }
        getWebservice().setAccessToken(accessToken.get());
        updateProperty(MieleCloudBindingConstants.PROPERTY_ACCESS_TOKEN, accessToken.get());
    }

    @Override
    public void onConnectionAlive() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onConnectionError(ConnectionError connectionError, int failedReconnectionAttempts) {
        if (connectionError == ConnectionError.AUTHORIZATION_FAILED) {
            tryToRefreshAccessToken();
            return;
        }

        if (failedReconnectionAttempts <= NUMBER_OF_SSE_RECONNECTION_ATTEMPTS_BEFORE_STATUS_IS_UPDATED
                && getThing().getStatus() != ThingStatus.UNKNOWN) {
            return;
        }

        if (getThing().getStatus() == ThingStatus.UNKNOWN && connectionError == ConnectionError.REQUEST_INTERRUPTED
                && failedReconnectionAttempts <= NUMBER_OF_SSE_RECONNECTION_ATTEMPTS_BEFORE_STATUS_IS_UPDATED) {
            return;
        }

        switch (connectionError) {
            case AUTHORIZATION_FAILED:
                // Handled above.
                break;

            case REQUEST_EXECUTION_FAILED:
            case SERVICE_UNAVAILABLE:
            case RESPONSE_MALFORMED:
            case TIMEOUT:
            case TOO_MANY_RERQUESTS:
            case SSE_STREAM_ENDED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;

            case SERVER_ERROR:
            case REQUEST_INTERRUPTED:
            case OTHER_HTTP_ERROR:
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        I18NKeys.BRIDGE_STATUS_DESCRIPTION_TRANSIENT_HTTP_ERROR);
                break;
        }
    }

    private void tryToRefreshAccessToken() {
        try {
            tokenRefresher.refreshToken(getOAuthServiceHandle());
            getWebservice().connectSse();
        } catch (OAuthException e) {
            logger.debug("Failed to refresh OAuth token!", e);
            getWebservice().disconnectSse();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    I18NKeys.BRIDGE_STATUS_DESCRIPTION_ACCESS_TOKEN_REFRESH_FAILED);
        }
    }

    @Override
    public Optional<String> getLanguage() {
        Object languageObject = thing.getConfiguration().get(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE);
        if (languageObject instanceof String) {
            String language = (String) languageObject;
            if (language.isEmpty() || !LocaleValidator.isValidLanguage(language)) {
                return Optional.empty();
            } else {
                return Optional.of(language);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void onDeviceStateUpdated(DeviceState deviceState) {
        ThingDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.onDeviceStateUpdated(deviceState);
        }

        invokeOnThingHandlers(deviceState.getDeviceIdentifier(), handler -> handler.onDeviceStateUpdated(deviceState));
    }

    @Override
    public void onProcessActionUpdated(ActionsState actionState) {
        invokeOnThingHandlers(actionState.getDeviceIdentifier(),
                handler -> handler.onProcessActionUpdated(actionState));
    }

    @Override
    public void onDeviceRemoved(String deviceIdentifier) {
        ThingDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.onDeviceRemoved(deviceIdentifier);
        }

        invokeOnThingHandlers(deviceIdentifier, handler -> handler.onDeviceRemoved());
    }

    private void invokeOnThingHandlers(String deviceIdentifier, Consumer<AbstractMieleThingHandler> action) {
        getThing().getThings().stream().map(Thing::getHandler)
                .filter(handler -> handler instanceof AbstractMieleThingHandler)
                .map(handler -> (AbstractMieleThingHandler) handler)
                .filter(handler -> deviceIdentifier.equals(handler.getDeviceId())).forEach(action);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ThingDiscoveryService.class);
    }
}
