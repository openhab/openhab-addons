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
package org.openhab.binding.bticinosmarther.internal.handler;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bticinosmarther.internal.account.SmartherAccountHandler;
import org.openhab.binding.bticinosmarther.internal.account.SmartherNotificationHandler;
import org.openhab.binding.bticinosmarther.internal.api.SmartherApi;
import org.openhab.binding.bticinosmarther.internal.api.dto.Location;
import org.openhab.binding.bticinosmarther.internal.api.dto.Module;
import org.openhab.binding.bticinosmarther.internal.api.dto.ModuleStatus;
import org.openhab.binding.bticinosmarther.internal.api.dto.Notification;
import org.openhab.binding.bticinosmarther.internal.api.dto.Plant;
import org.openhab.binding.bticinosmarther.internal.api.dto.Program;
import org.openhab.binding.bticinosmarther.internal.api.dto.Sender;
import org.openhab.binding.bticinosmarther.internal.api.dto.Subscription;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.bticinosmarther.internal.config.SmartherBridgeConfiguration;
import org.openhab.binding.bticinosmarther.internal.discovery.SmartherModuleDiscoveryService;
import org.openhab.binding.bticinosmarther.internal.model.BridgeStatus;
import org.openhab.binding.bticinosmarther.internal.model.ModuleSettings;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code SmartherBridgeHandler} class is responsible of the handling of a Smarther Bridge thing.
 * The Smarther Bridge is used to manage a set of Smarther Chronothermostat Modules registered under the same
 * Legrand/Bticino account credentials.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherBridgeHandler extends BaseBridgeHandler
        implements SmartherAccountHandler, SmartherNotificationHandler, AccessTokenRefreshListener {

    private static final long POLL_INITIAL_DELAY = 5;

    private final Logger logger = LoggerFactory.getLogger(SmartherBridgeHandler.class);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    // Bridge configuration
    private SmartherBridgeConfiguration config;

    // Field members assigned in initialize method
    private @Nullable Future<?> pollFuture;
    private @Nullable OAuthClientService oAuthService;
    private @Nullable SmartherApi smartherApi;
    private @Nullable ExpiringCache<List<Location>> locationCache;
    private @Nullable BridgeStatus bridgeStatus;

    /**
     * Constructs a {@code SmartherBridgeHandler} for the given Bridge thing, authorization factory and http client.
     *
     * @param bridge
     *            the {@link Bridge} thing to be used
     * @param oAuthFactory
     *            the OAuth2 authorization factory to be used
     * @param httpClient
     *            the http client to be used
     */
    public SmartherBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        this.config = new SmartherBridgeConfiguration();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SmartherModuleDiscoveryService.class);
    }

    // ===========================================================================
    //
    // Bridge thing lifecycle management methods
    //
    // ===========================================================================

    @Override
    public void initialize() {
        logger.debug("Bridge[{}] Initialize handler", thing.getUID());

        this.config = getConfigAs(SmartherBridgeConfiguration.class);
        if (StringUtil.isBlank(config.getSubscriptionKey())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'Subscription Key' property is not set or empty. If you have an older thing please recreate it.");
            return;
        }
        if (StringUtil.isBlank(config.getClientId())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'Client Id' property is not set or empty. If you have an older thing please recreate it.");
            return;
        }
        if (StringUtil.isBlank(config.getClientSecret())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'Client Secret' property is not set or empty. If you have an older thing please recreate it.");
            return;
        }

        // Initialize OAuth2 authentication support
        final OAuthClientService localOAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                SMARTHER_API_TOKEN_URL, SMARTHER_AUTHORIZE_URL, config.getClientId(), config.getClientSecret(),
                SMARTHER_API_SCOPES, false);
        localOAuthService.addAccessTokenRefreshListener(SmartherBridgeHandler.this);
        this.oAuthService = localOAuthService;

        // Initialize Smarther Api
        final SmartherApi localSmartherApi = new SmartherApi(localOAuthService, config.getSubscriptionKey(), scheduler,
                httpClient);
        this.smartherApi = localSmartherApi;

        // Initialize locations (plant Ids) local cache
        final ExpiringCache<List<Location>> localLocationCache = new ExpiringCache<>(
                Duration.ofMinutes(config.getStatusRefreshPeriod()), this::locationCacheAction);
        this.locationCache = localLocationCache;

        // Initialize bridge local status
        final BridgeStatus localBridgeStatus = new BridgeStatus();
        this.bridgeStatus = localBridgeStatus;

        updateStatus(ThingStatus.UNKNOWN);

        schedulePoll();

        logger.debug("Bridge[{}] Finished initializing!", thing.getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_CONFIG_FETCH_LOCATIONS:
                if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        logger.debug(
                                "Bridge[{}] Manually triggered channel to remotely fetch the updated client locations list",
                                thing.getUID());
                        expireCache();
                        getLocations();
                        updateChannelState(CHANNEL_CONFIG_FETCH_LOCATIONS, OnOffType.OFF);
                    }
                    return;
                }
                break;
        }

        if (command instanceof RefreshType) {
            // Avoid logging wrong command when refresh command is sent
            return;
        }

        logger.debug("Bridge[{}] Received command {} of wrong type {} on channel {}", thing.getUID(), command,
                command.getClass().getTypeName(), channelUID.getId());
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll(true);
    }

    @Override
    public void dispose() {
        logger.debug("Bridge[{}] Dispose handler", thing.getUID());
        final OAuthClientService localOAuthService = this.oAuthService;
        if (localOAuthService != null) {
            localOAuthService.removeAccessTokenRefreshListener(this);
        }
        this.oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
        stopPoll(true);
        logger.debug("Bridge[{}] Finished disposing!", thing.getUID());
    }

    // ===========================================================================
    //
    // Bridge data cache management methods
    //
    // ===========================================================================

    /**
     * Returns the available locations to be cached for this Bridge.
     *
     * @return the available locations to be cached for this Bridge, or {@code null} if the list of available locations
     *         cannot be retrieved
     */
    private @Nullable List<Location> locationCacheAction() {
        try {
            // Retrieve the plants list from the API Gateway
            final List<Plant> plants = getPlants();

            List<Location> locations;
            if (config.isUseNotifications()) {
                // Retrieve the subscriptions list from the API Gateway
                final List<Subscription> subscriptions = getSubscriptions();

                // Enrich the notifications list with externally registered subscriptions
                updateNotifications(subscriptions);

                // Get the notifications list from bridge config
                final List<String> notifications = config.getNotifications();

                locations = plants.stream().map(p -> Location.fromPlant(p, subscriptions.stream()
                        .filter(s -> s.getPlantId().equals(p.getId()) && notifications.contains(s.getSubscriptionId()))
                        .findFirst())).collect(Collectors.toList());
            } else {
                locations = plants.stream().map(p -> Location.fromPlant(p)).collect(Collectors.toList());
            }
            logger.debug("Bridge[{}] Available locations: {}", thing.getUID(), locations);

            return locations;

        } catch (SmartherGatewayException e) {
            logger.warn("Bridge[{}] Cannot retrieve available locations: {}", thing.getUID(), e.getMessage());
            return null;
        }
    }

    /**
     * Updates this Bridge local notifications list with externally registered subscriptions.
     *
     * @param subscriptions
     *            the externally registered subscriptions to be added to the local notifications list
     */
    private void updateNotifications(List<Subscription> subscriptions) {
        // Get the notifications list from bridge config
        List<String> notifications = config.getNotifications();

        for (Subscription s : subscriptions) {
            if (s.getEndpointUrl().equalsIgnoreCase(config.getNotificationUrl())
                    && !notifications.contains(s.getSubscriptionId())) {
                // Add the external subscription to notifications list
                notifications = config.addNotification(s.getSubscriptionId());

                // Save the updated notifications list back to bridge config
                Configuration configuration = editConfiguration();
                configuration.put(PROPERTY_NOTIFICATIONS, notifications);
                updateConfiguration(configuration);
            }
        }
    }

    /**
     * Sets all the cache to "expired" for this Bridge.
     */
    private void expireCache() {
        logger.debug("Bridge[{}] Invalidating location cache", thing.getUID());
        final ExpiringCache<List<Location>> localLocationCache = this.locationCache;
        if (localLocationCache != null) {
            localLocationCache.invalidateValue();
        }
    }

    // ===========================================================================
    //
    // Bridge status polling mechanism methods
    //
    // ===========================================================================

    /**
     * Starts a new scheduler to periodically poll and update this Bridge status.
     */
    private void schedulePoll() {
        stopPoll(false);

        // Schedule poll to start after POLL_INITIAL_DELAY sec and run periodically based on status refresh period
        final Future<?> localPollFuture = scheduler.scheduleWithFixedDelay(this::poll, POLL_INITIAL_DELAY,
                config.getStatusRefreshPeriod() * 60, TimeUnit.SECONDS);
        this.pollFuture = localPollFuture;

        logger.debug("Bridge[{}] Scheduled poll for {} sec out, then every {} min", thing.getUID(), POLL_INITIAL_DELAY,
                config.getStatusRefreshPeriod());
    }

    /**
     * Cancels all running poll schedulers.
     *
     * @param mayInterruptIfRunning
     *            {@code true} if the thread executing this task should be interrupted, {@code false} if the in-progress
     *            tasks are allowed to complete
     */
    private synchronized void stopPoll(boolean mayInterruptIfRunning) {
        final Future<?> localPollFuture = this.pollFuture;
        if (localPollFuture != null) {
            if (!localPollFuture.isCancelled()) {
                localPollFuture.cancel(mayInterruptIfRunning);
            }
            this.pollFuture = null;
        }
    }

    /**
     * Polls to update this Bridge status, calling the Smarther API to refresh its plants list.
     *
     * @return {@code true} if the method completes without errors, {@code false} otherwise
     */
    private synchronized boolean poll() {
        try {
            onAccessTokenResponse(getAccessTokenResponse());

            expireCache();
            getLocations();

            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (SmartherAuthorizationException e) {
            logger.warn("Bridge[{}] Authorization error during polling: {}", thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            // All other exceptions apart from Authorization and Gateway issues
            logger.warn("Bridge[{}] Unexpected error during polling, please report if this keeps occurring: ",
                    thing.getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
        schedulePoll();
        return false;
    }

    @Override
    public void onAccessTokenResponse(@Nullable AccessTokenResponse tokenResponse) {
        logger.trace("Bridge[{}] Got access token: {}", thing.getUID(),
                (tokenResponse != null) ? tokenResponse.getAccessToken() : "none");
    }

    // ===========================================================================
    //
    // Bridge convenience methods
    //
    // ===========================================================================

    /**
     * Convenience method to get this Bridge configuration.
     *
     * @return a {@link SmartherBridgeConfiguration} object containing the Bridge configuration
     */
    public SmartherBridgeConfiguration getSmartherBridgeConfig() {
        return config;
    }

    /**
     * Convenience method to get the access token from Smarther API authorization layer.
     *
     * @return the autorization access token, may be {@code null}
     *
     * @throws {@link SmartherAuthorizationException}
     *             in case of authorization issues with the Smarther API
     */
    private @Nullable AccessTokenResponse getAccessTokenResponse() throws SmartherAuthorizationException {
        try {
            final OAuthClientService localOAuthService = this.oAuthService;
            if (localOAuthService != null) {
                return localOAuthService.getAccessTokenResponse();
            }
            return null;
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            throw new SmartherAuthorizationException(e.getMessage());
        }
    }

    /**
     * Convenience method to update the given Channel state "only" if the Channel is linked.
     *
     * @param channelId
     *            the identifier of the Channel to be updated
     * @param state
     *            the new state to be applied to the given Channel
     */
    private void updateChannelState(String channelId, State state) {
        final Channel channel = thing.getChannel(channelId);

        if (channel != null && isLinked(channel.getUID())) {
            updateState(channel.getUID(), state);
        }
    }

    /**
     * Convenience method to update the Smarther API calls counter for this Bridge.
     */
    private void updateApiCallsCounter() {
        final BridgeStatus localBridgeStatus = this.bridgeStatus;
        if (localBridgeStatus != null) {
            updateChannelState(CHANNEL_STATUS_API_CALLS_HANDLED,
                    new DecimalType(localBridgeStatus.incrementApiCallsHandled()));
        }
    }

    /**
     * Convenience method to check and get the Smarther API instance for this Bridge.
     *
     * @return the Smarther API instance
     *
     * @throws {@link SmartherGatewayException}
     *             in case the Smarther API instance is {@code null}
     */
    private SmartherApi getSmartherApi() throws SmartherGatewayException {
        final SmartherApi localSmartherApi = this.smartherApi;
        if (localSmartherApi == null) {
            throw new SmartherGatewayException("Smarther API instance is null");
        }
        return localSmartherApi;
    }

    // ===========================================================================
    //
    // Implementation of the SmartherAccountHandler interface
    //
    // ===========================================================================

    @Override
    public ThingUID getUID() {
        return thing.getUID();
    }

    @Override
    public String getLabel() {
        return StringUtil.defaultString(thing.getLabel());
    }

    @Override
    public List<Location> getLocations() {
        final ExpiringCache<List<Location>> localLocationCache = this.locationCache;
        final List<Location> locations = (localLocationCache != null) ? localLocationCache.getValue() : null;
        return (locations != null) ? locations : Collections.emptyList();
    }

    @Override
    public boolean hasLocation(String plantId) {
        final ExpiringCache<List<Location>> localLocationCache = this.locationCache;
        final List<Location> locations = (localLocationCache != null) ? localLocationCache.getValue() : null;
        return (locations != null) ? locations.stream().anyMatch(l -> l.getPlantId().equals(plantId)) : false;
    }

    @Override
    public List<Plant> getPlants() throws SmartherGatewayException {
        updateApiCallsCounter();
        return getSmartherApi().getPlants();
    }

    @Override
    public List<Subscription> getSubscriptions() throws SmartherGatewayException {
        updateApiCallsCounter();
        return getSmartherApi().getSubscriptions();
    }

    @Override
    public String subscribePlant(String plantId, String notificationUrl) throws SmartherGatewayException {
        updateApiCallsCounter();
        return getSmartherApi().subscribePlant(plantId, notificationUrl);
    }

    @Override
    public void unsubscribePlant(String plantId, String subscriptionId) throws SmartherGatewayException {
        updateApiCallsCounter();
        getSmartherApi().unsubscribePlant(plantId, subscriptionId);
    }

    @Override
    public List<Module> getLocationModules(Location location) {
        try {
            updateApiCallsCounter();
            return getSmartherApi().getPlantModules(location.getPlantId());
        } catch (SmartherGatewayException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public ModuleStatus getModuleStatus(String plantId, String moduleId) throws SmartherGatewayException {
        updateApiCallsCounter();
        return getSmartherApi().getModuleStatus(plantId, moduleId);
    }

    @Override
    public boolean setModuleStatus(ModuleSettings moduleSettings) throws SmartherGatewayException {
        updateApiCallsCounter();
        return getSmartherApi().setModuleStatus(moduleSettings);
    }

    @Override
    public List<Program> getModulePrograms(String plantId, String moduleId) throws SmartherGatewayException {
        updateApiCallsCounter();
        return getSmartherApi().getModulePrograms(plantId, moduleId);
    }

    @Override
    public boolean isAuthorized() {
        try {
            final AccessTokenResponse tokenResponse = getAccessTokenResponse();
            onAccessTokenResponse(tokenResponse);

            return (tokenResponse != null && tokenResponse.getAccessToken() != null
                    && tokenResponse.getRefreshToken() != null);
        } catch (SmartherAuthorizationException e) {
            return false;
        }
    }

    @Override
    public boolean isOnline() {
        return (thing.getStatus() == ThingStatus.ONLINE);
    }

    @Override
    public String authorize(String redirectUrl, String reqCode, String notificationUrl)
            throws SmartherGatewayException {
        try {
            logger.debug("Bridge[{}] Call API gateway to get access token. RedirectUri: {}", thing.getUID(),
                    redirectUrl);

            final OAuthClientService localOAuthService = this.oAuthService;
            if (localOAuthService == null) {
                throw new SmartherAuthorizationException("Authorization service is null");
            }

            // OAuth2 call to get access token from received authorization code
            localOAuthService.getAccessTokenResponseByAuthorizationCode(reqCode, redirectUrl);

            // Store the notification URL in bridge configuration
            Configuration configuration = editConfiguration();
            configuration.put(PROPERTY_NOTIFICATION_URL, notificationUrl);
            updateConfiguration(configuration);
            config.setNotificationUrl(notificationUrl);
            logger.debug("Bridge[{}] Store notification URL: {}", thing.getUID(), notificationUrl);

            // Reschedule the polling thread
            schedulePoll();

            return config.getClientId();
        } catch (OAuthResponseException e) {
            throw new SmartherAuthorizationException(e.toString(), e);
        } catch (OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new SmartherGatewayException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equalsThingUID(String thingUID) {
        return thing.getUID().getAsString().equals(thingUID);
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        try {
            final OAuthClientService localOAuthService = this.oAuthService;
            if (localOAuthService != null) {
                return localOAuthService.getAuthorizationUrl(redirectUri, null, thing.getUID().getAsString());
            }
        } catch (OAuthException e) {
            logger.warn("Bridge[{}] Error constructing AuthorizationUrl: {}", thing.getUID(), e.getMessage());
        }
        return "";
    }

    // ===========================================================================
    //
    // Implementation of the SmartherNotificationHandler interface
    //
    // ===========================================================================

    @Override
    public boolean useNotifications() {
        return config.isUseNotifications();
    }

    @Override
    public synchronized void registerNotification(String plantId) throws SmartherGatewayException {
        if (!config.isUseNotifications()) {
            return;
        }

        final ExpiringCache<List<Location>> localLocationCache = this.locationCache;
        if (localLocationCache != null) {
            List<Location> locations = localLocationCache.getValue();
            if (locations != null) {
                final Optional<Location> maybeLocation = locations.stream().filter(l -> l.getPlantId().equals(plantId))
                        .findFirst();
                if (maybeLocation.isPresent()) {
                    Location location = maybeLocation.get();
                    if (!location.hasSubscription()) {
                        // Validate notification Url (must be non-null and https)
                        final String notificationUrl = config.getNotificationUrl();
                        if (isValidNotificationUrl(notificationUrl)) {
                            // Call gateway to register plant subscription
                            String subscriptionId = subscribePlant(plantId, config.getNotificationUrl());
                            logger.debug("Bridge[{}] Notification registered: [plantId={}, subscriptionId={}]",
                                    thing.getUID(), plantId, subscriptionId);

                            // Add the new subscription to notifications list
                            List<String> notifications = config.addNotification(subscriptionId);

                            // Save the updated notifications list back to bridge config
                            Configuration configuration = editConfiguration();
                            configuration.put(PROPERTY_NOTIFICATIONS, notifications);
                            updateConfiguration(configuration);

                            // Update the local locationCache with the added data
                            locations.stream().forEach(l -> {
                                if (l.getPlantId().equals(plantId)) {
                                    l.setSubscription(subscriptionId, config.getNotificationUrl());
                                }
                            });
                            localLocationCache.putValue(locations);
                        } else {
                            logger.warn(
                                    "Bridge[{}] Invalid notification Url [{}]: must be non-null, public https address",
                                    thing.getUID(), notificationUrl);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleNotification(Notification notification) {
        final Sender sender = notification.getSender();
        if (sender != null) {
            final BridgeStatus localBridgeStatus = this.bridgeStatus;
            if (localBridgeStatus != null) {
                logger.debug("Bridge[{}] Notification received: [id={}]", thing.getUID(), notification.getId());
                updateChannelState(CHANNEL_STATUS_NOTIFS_RECEIVED,
                        new DecimalType(localBridgeStatus.incrementNotificationsReceived()));

                final String plantId = sender.getPlant().getId();
                final String moduleId = sender.getPlant().getModule().getId();
                Optional<SmartherModuleHandler> maybeModuleHandler = getThing().getThings().stream()
                        .map(t -> (SmartherModuleHandler) t.getHandler()).filter(h -> h.isLinkedTo(plantId, moduleId))
                        .findFirst();

                if (config.isUseNotifications() && maybeModuleHandler.isPresent()) {
                    maybeModuleHandler.get().handleNotification(notification);
                } else {
                    logger.debug("Bridge[{}] Notification rejected: no module handler available", thing.getUID());
                    updateChannelState(CHANNEL_STATUS_NOTIFS_REJECTED,
                            new DecimalType(localBridgeStatus.incrementNotificationsRejected()));
                }
            }
        }
    }

    @Override
    public synchronized void unregisterNotification(String plantId) throws SmartherGatewayException {
        if (!config.isUseNotifications()) {
            return;
        }

        final ExpiringCache<List<Location>> localLocationCache = this.locationCache;
        if (localLocationCache != null) {
            List<Location> locations = localLocationCache.getValue();

            final long remainingModules = getThing().getThings().stream()
                    .map(t -> (SmartherModuleHandler) t.getHandler()).filter(h -> h.getPlantId().equals(plantId))
                    .count();

            if (locations != null && remainingModules == 0) {
                final Optional<Location> maybeLocation = locations.stream().filter(l -> l.getPlantId().equals(plantId))
                        .findFirst();
                if (maybeLocation.isPresent()) {
                    Location location = maybeLocation.get();
                    final String subscriptionId = location.getSubscriptionId();
                    if (location.hasSubscription() && (subscriptionId != null)) {
                        // Call gateway to unregister plant subscription
                        unsubscribePlant(plantId, subscriptionId);
                        logger.debug("Bridge[{}] Notification unregistered: [plantId={}, subscriptionId={}]",
                                thing.getUID(), plantId, subscriptionId);

                        // Remove the subscription from notifications list
                        List<String> notifications = config.removeNotification(subscriptionId);

                        // Save the updated notifications list back to bridge config
                        Configuration configuration = editConfiguration();
                        configuration.put(PROPERTY_NOTIFICATIONS, notifications);
                        updateConfiguration(configuration);

                        // Update the local locationCache with the removed data
                        locations.stream().forEach(l -> {
                            if (l.getPlantId().equals(plantId)) {
                                l.unsetSubscription();
                            }
                        });
                        localLocationCache.putValue(locations);
                    }
                }
            }
        }
    }

    /**
     * Checks if the passed string is a formally valid Notification Url (non-null, public https address).
     *
     * @param str
     *            the string to check
     *
     * @return {@code true} if the given string is a formally valid Notification Url, {@code false} otherwise
     */
    private boolean isValidNotificationUrl(@Nullable String str) {
        try {
            if (str != null) {
                URI maybeValidNotificationUrl = new URI(str);
                if (HTTPS_SCHEMA.equals(maybeValidNotificationUrl.getScheme())) {
                    InetAddress address = InetAddress.getByName(maybeValidNotificationUrl.getHost());
                    if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (URISyntaxException | UnknownHostException e) {
            return false;
        }
    }
}
