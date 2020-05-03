/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.handler;

import static org.openhab.binding.smarther.internal.SmartherBindingConstants.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.smarther.internal.account.SmartherAccountHandler;
import org.openhab.binding.smarther.internal.account.SmartherNotificationHandler;
import org.openhab.binding.smarther.internal.api.SmartherApi;
import org.openhab.binding.smarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.smarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.smarther.internal.api.model.Location;
import org.openhab.binding.smarther.internal.api.model.Module;
import org.openhab.binding.smarther.internal.api.model.ModuleSettings;
import org.openhab.binding.smarther.internal.api.model.ModuleStatus;
import org.openhab.binding.smarther.internal.api.model.Notification;
import org.openhab.binding.smarther.internal.api.model.Plant;
import org.openhab.binding.smarther.internal.api.model.Program;
import org.openhab.binding.smarther.internal.api.model.Subscription;
import org.openhab.binding.smarther.internal.config.SmartherBridgeConfiguration;
import org.openhab.binding.smarther.internal.discovery.SmartherModuleDiscoveryService;
import org.openhab.binding.smarther.internal.model.BridgeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartherBridgeHandler} is responsible for accessing the BTicino/Legrand Smarther API gateway.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherBridgeHandler extends BaseBridgeHandler
        implements SmartherAccountHandler, SmartherNotificationHandler, AccessTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    // Field members assigned in initialize method
    private @NonNullByDefault({}) Future<?> pollFuture;
    private @NonNullByDefault({}) OAuthClientService oAuthService;
    private @NonNullByDefault({}) SmartherApi smartherApi;
    private @NonNullByDefault({}) SmartherBridgeConfiguration config;

    // Bridge local status
    private @NonNullByDefault({}) ExpiringCache<@NonNull List<Location>> locationCache;
    private @NonNullByDefault({}) BridgeStatus bridgeStatus;

    public SmartherBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SmartherModuleDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_ACCESS_TOKEN:
                if (command instanceof RefreshType) {
                    onAccessTokenResponse(getAccessTokenResponse());
                    return;
                }
                break;
            case CHANNEL_FETCH_CONFIG:
                if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        logger.debug("Bridge[{}] Manually triggered channel to refresh the Bridge config",
                                thing.getUID());
                        schedulePoll();
                        updateChannelState(CHANNEL_FETCH_CONFIG, OnOffType.OFF);
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
    public void initialize() {
        logger.debug("Bridge[{}] Initialize handler", thing.getUID());

        config = getConfigAs(SmartherBridgeConfiguration.class);
        if (StringUtils.isBlank(config.getSubscriptionKey())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'Subscription Key' property is not set or empty. If you have an older thing please recreate it.");
            return;
        }
        if (StringUtils.isBlank(config.getClientId())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'Client Id' property is not set or empty. If you have an older thing please recreate it.");
            return;
        }
        if (StringUtils.isBlank(config.getClientSecret())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'Client Secret' property is not set or empty. If you have an older thing please recreate it.");
            return;
        }
        if (config.getStatusRefreshPeriod() <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'Bridge Status Refresh Period' must be > 0. If you have an older thing please recreate it.");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        // Initialize OAuth2 authentication support
        oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), SMARTHER_API_TOKEN_URL,
                SMARTHER_AUTHORIZE_URL, config.getClientId(), config.getClientSecret(), SMARTHER_API_SCOPES, false);
        oAuthService.addAccessTokenRefreshListener(SmartherBridgeHandler.this);
        smartherApi = new SmartherApi(config.getSubscriptionKey(), oAuthService, scheduler, httpClient);

        // Setup locations (plant Ids) local cache
        locationCache = new ExpiringCache<>(Duration.ofMinutes(config.getStatusRefreshPeriod()), this::getLocationList);
        bridgeStatus = new BridgeStatus();

        schedulePoll();

        logger.debug("Bridge[{}] Finished initializing!", thing.getUID());
    }

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

    private List<Location> getLocationList() {
        // Retrieve the plants list from the API Gateway
        final List<Plant> plants = listPlants();

        List<Location> locations;
        if (config.isUseNotifications()) {
            // Retrieve the subscriptions list from the API Gateway
            final List<Subscription> subscriptions = getSubscriptionList();

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
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll(true);
    }

    @Override
    public void dispose() {
        logger.debug("Bridge[{}] Dispose handler", thing.getUID());
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(this);
        }
        oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
        stopPoll(true);
        logger.debug("Bridge[{}] Finished disposing!", thing.getUID());
    }

    /**
     * This method initiates a new thread for polling the available Smarther plants and update the plants
     * information.
     */
    private void schedulePoll() {
        stopPoll(false);
        // Schedule poll to start after 1 sec and run periodically based on status refresh period
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, config.getStatusRefreshPeriod() * 60,
                TimeUnit.SECONDS);
        logger.debug("Bridge[{}] Scheduled poll for 1 sec out, then every {} min", thing.getUID(),
                config.getStatusRefreshPeriod());
    }

    /**
     * Cancels all running schedulers.
     *
     * @param mayInterruptIfRunning true if the thread executing this task should be interrupted; otherwise, in-progress
     *            tasks are allowed to complete.
     */
    private synchronized void stopPoll(boolean mayInterruptIfRunning) {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(mayInterruptIfRunning);
            pollFuture = null;
        }
    }

    /**
     * Calls the Smarther API and collects plant data. Returns true if method completed without errors.
     *
     * @return true if method completed without errors.
     */
    private synchronized boolean poll() {
        try {
            onAccessTokenResponse(getAccessTokenResponse());

            expireCache();
            listLocations();

            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (SmartherAuthorizationException e) {
            logger.warn("Bridge[{}] Authorization error during polling: {}", thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return false;
        } catch (SmartherGatewayException e) {
            logger.warn("Bridge[{}] API Gateway error during polling: {}", thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return false;
        } catch (RuntimeException e) {
            // All other exceptions apart from Authorization and Gateway issues
            logger.warn("Bridge[{}] Unexpected error during polling, please report if this keeps occurring: ",
                    thing.getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            return false;
        }
    }

    private void expireCache() {
        logger.debug("Bridge[{}] Invalidating location cache", thing.getUID());
        locationCache.invalidateValue();
    }

    public SmartherBridgeConfiguration getSmartherBridgeConfig() {
        return config;
    }

    private @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            return (oAuthService == null) ? null : oAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            throw new SmartherAuthorizationException(e.getMessage());
        }
    }

    @Override
    public void onAccessTokenResponse(@Nullable AccessTokenResponse tokenResponse) {
        updateChannelState(CHANNEL_ACCESS_TOKEN,
                new StringType((tokenResponse == null) ? null : tokenResponse.getAccessToken()));
    }

    /**
     * Convenience method to update the channel state but only if the channel is linked.
     *
     * @param channelId id of the channel to update
     * @param state State to set on the channel
     */
    private void updateChannelState(String channelId, State state) {
        final Channel channel = thing.getChannel(channelId);

        if (channel != null && isLinked(channel.getUID())) {
            updateState(channel.getUID(), state);
        }
    }

    /**
     * Convenience method to update the api calls counter of local bridge status.
     */
    private void updateApiCallsCounter() {
        updateChannelState(CHANNEL_API_CALLS_HANDLED, new DecimalType(bridgeStatus.incrementApiCallsHandled()));
    }

    // ===========================================================================
    //
    // Implementation of SmartherAccountHandler interface
    //
    // ===========================================================================

    @Override
    public ThingUID getUID() {
        return thing.getUID();
    }

    @Override
    public String getLabel() {
        return StringUtils.defaultString(thing.getLabel());
    }

    @Override
    public List<Location> listLocations() {
        final List<Location> locations = locationCache.getValue();
        return (locations == null) ? Collections.emptyList() : locations;
    }

    @Override
    public boolean hasLocation(String plantId) {
        final List<Location> locations = locationCache.getValue();
        return (locations == null) ? false : locations.stream().anyMatch(l -> l.getPlantId().equals(plantId));
    }

    @Override
    public List<Plant> listPlants() {
        updateApiCallsCounter();
        return smartherApi.getPlantList();
    }

    @Override
    public List<Subscription> getSubscriptionList() {
        updateApiCallsCounter();
        return smartherApi.getSubscriptionList();
    }

    @Override
    public String addSubscription(String plantId, String notificationUrl) {
        updateApiCallsCounter();
        return smartherApi.subscribe(plantId, notificationUrl);
    }

    @Override
    public void removeSubscription(String plantId, String subscriptionId) {
        updateApiCallsCounter();
        smartherApi.unsubscribe(plantId, subscriptionId);
    }

    @Override
    public List<Module> listModules(Location location) {
        updateApiCallsCounter();
        return smartherApi.getTopology(location.getPlantId());
    }

    @Override
    public ModuleStatus getModuleStatus(String plantId, String moduleId) {
        updateApiCallsCounter();
        return smartherApi.getModuleStatus(plantId, moduleId);
    }

    @Override
    public boolean setModuleStatus(ModuleSettings moduleSettings) {
        updateApiCallsCounter();
        return smartherApi.setModuleStatus(moduleSettings);
    }

    @Override
    public List<Program> getModuleProgramList(String plantId, String moduleId) {
        updateApiCallsCounter();
        return smartherApi.getProgramList(plantId, moduleId);
    }

    @Override
    public boolean isAuthorized() {
        try {
            final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

            return (accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                    && accessTokenResponse.getRefreshToken() != null);
        } catch (SmartherAuthorizationException e) {
            return false;
        }
    }

    @Override
    public boolean isOnline() {
        return (thing.getStatus() == ThingStatus.ONLINE);
    }

    @Override
    public String authorize(String redirectUrl, String reqCode, String notificationUrl) {
        try {
            logger.debug("Bridge[{}] Call API gateway to get access token. RedirectUri: {}", thing.getUID(),
                    redirectUrl);

            // OAuth2 call to get access token from received authorization code
            oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode, redirectUrl);

            // Store the notification URL in bridge configuration
            Configuration configuration = editConfiguration();
            configuration.put(PROPERTY_NOTIFICATION_URL, notificationUrl);
            updateConfiguration(configuration);
            config.setNotificationUrl(notificationUrl);
            logger.debug("Bridge[{}] Store notification URL: {}", thing.getUID(), notificationUrl);

            // Reschedule the polling thread
            schedulePoll();

            return config.getClientId();
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new SmartherGatewayException(e.getMessage(), e);
        } catch (OAuthResponseException e) {
            throw new SmartherAuthorizationException(e.toString(), e);
        }
    }

    @Override
    public boolean equalsThingUID(String thingUID) {
        return thing.getUID().getAsString().equals(thingUID);
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        try {
            return oAuthService.getAuthorizationUrl(redirectUri, null, thing.getUID().getAsString());
        } catch (OAuthException e) {
            logger.warn("Bridge[{}] Error constructing AuthorizationUrl: ", thing.getUID(), e);
            return "";
        }
    }

    // ===========================================================================
    //
    // Implementation of SmartherNotificationHandler interface
    //
    // ===========================================================================

    @Override
    public boolean useNotifications() {
        return config.isUseNotifications();
    }

    @Override
    public synchronized void registerNotification(String plantId) {
        if (!config.isUseNotifications()) {
            return;
        }

        List<Location> locations = locationCache.getValue();
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
                        String subscriptionId = addSubscription(plantId, config.getNotificationUrl());
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
                        locationCache.putValue(locations);
                    } else {
                        logger.warn("Bridge[{}] Invalid notification Url [{}]: must be non-null, public https address",
                                thing.getUID(), notificationUrl);
                    }
                }
            }
        }
    }

    @Override
    public void handleNotification(Notification notification) {
        logger.debug("Bridge[{}] Notification received: [id={}]", thing.getUID(), notification.getId());

        updateChannelState(CHANNEL_NOTIFS_RECEIVED, new DecimalType(bridgeStatus.incrementNotificationsReceived()));

        final String plantId = notification.getData().toChronothermostat().getSender().getPlant().getId();
        final String moduleId = notification.getData().toChronothermostat().getSender().getPlant().getModule().getId();

        Optional<SmartherModuleHandler> maybeModuleHandler = getThing().getThings().stream()
                .map(t -> (SmartherModuleHandler) t.getHandler()).filter(h -> h.isLinkedTo(plantId, moduleId))
                .findFirst();

        if (config.isUseNotifications() && maybeModuleHandler.isPresent()) {
            maybeModuleHandler.get().handleNotification(notification);
        } else {
            updateChannelState(CHANNEL_NOTIFS_REJECTED, new DecimalType(bridgeStatus.incrementNotificationsRejected()));
        }
    }

    @Override
    public synchronized void unregisterNotification(String plantId) {
        if (!config.isUseNotifications()) {
            return;
        }

        List<Location> locations = locationCache.getValue();

        final long remainingModules = getThing().getThings().stream().map(t -> (SmartherModuleHandler) t.getHandler())
                .filter(h -> h.getPlantId().equals(plantId)).count();

        if (locations != null && remainingModules == 0) {
            final Optional<Location> maybeLocation = locations.stream().filter(l -> l.getPlantId().equals(plantId))
                    .findFirst();
            if (maybeLocation.isPresent()) {
                Location location = maybeLocation.get();
                final String subscriptionId = location.getSubscriptionId();
                if (location.hasSubscription() && (subscriptionId != null)) {
                    // Call gateway to unregister plant subscription
                    removeSubscription(plantId, subscriptionId);
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
                    locationCache.putValue(locations);
                }
            }
        }
    }

    /**
     * Check whether the passed string is a formally valid Notification Url (non-null, public https address).
     *
     * @param notificationUrl The string to be checked.
     * @return true if the string is a valid Notification Url, false otherwise.
     */
    private boolean isValidNotificationUrl(String notificationUrl) {
        try {
            URI maybeValidUrl = new URI(notificationUrl);
            if (HTTPS_SCHEMA.equals(maybeValidUrl.getScheme())) {
                InetAddress address = InetAddress.getByName(maybeValidUrl.getHost());
                if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()) {
                    return true;
                }
            }
            return false;
        } catch (URISyntaxException | UnknownHostException e) {
            return false;
        }
    }

}
