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
package org.openhab.binding.hasslink.internal.handler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.HassLinkBindingConstants;
import org.openhab.binding.hasslink.internal.action.HassLinkActions;
import org.openhab.binding.hasslink.internal.api.HomeAssistantConnectionListener;
import org.openhab.binding.hasslink.internal.api.HomeAssistantWebSocketClient;
import org.openhab.binding.hasslink.internal.api.dto.CompressedEntityDiff;
import org.openhab.binding.hasslink.internal.api.dto.CompressedEntityState;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.EntityStateCompressedEvent;
import org.openhab.binding.hasslink.internal.api.dto.HomeAssistantConfig;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.api.exception.CommunicationException;
import org.openhab.binding.hasslink.internal.api.util.HomeAssistantWebSocketUriResolver;
import org.openhab.binding.hasslink.internal.config.HassLinkBridgeConfiguration;
import org.openhab.binding.hasslink.internal.discovery.HassLinkDiscoveryService;
import org.openhab.binding.hasslink.internal.entity.EntityId;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.EntityTypeRegistry;
import org.openhab.binding.hasslink.internal.entity.util.ImageUtils;
import org.openhab.binding.hasslink.internal.registry.HomeAssistantRegistry;
import org.openhab.binding.hasslink.internal.util.EndpointUtils;
import org.openhab.binding.hasslink.internal.util.EntityUtils;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HassLinkBridgeHandler} maintains the WebSocket connection to a Home Assistant server,
 * requests initial entity/registry snapshots, subscribes to live state and registry events,
 * performs periodic 15-minute background registry polling, and dispatches updates.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HassLinkBridgeHandler extends BaseBridgeHandler implements HomeAssistantConnectionListener {

    private static final long REGISTRY_POLLING_INTERVAL_MINUTES = 15;
    private static final long IMAGE_CACHE_EXPIRATION = Duration.ofMinutes(15).toMillis();

    private static final Set<String> BUTTON_DOMAINS = Set.of("button", "input_button");

    private final Logger logger = LoggerFactory.getLogger(HassLinkBridgeHandler.class);
    private final WebSocketFactory webSocketFactory;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Set<HassLinkDeviceHandler> registeredDeviceHandlers = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<HassLinkDeviceHandler>> entityHandlers = new ConcurrentHashMap<>();
    private final Map<String, EntityState> lastKnownStates = new ConcurrentHashMap<>();

    private final HomeAssistantRegistry registry = new HomeAssistantRegistry();
    private volatile @Nullable HomeAssistantConfig haConfig;
    private volatile @Nullable JsonObject rawHaConfig;

    private volatile String restBaseUri = "";
    private volatile String accessToken = "";

    private @Nullable HomeAssistantWebSocketClient client;
    private @Nullable ScheduledFuture<?> registryPollingFuture;

    private @Nullable HassLinkDiscoveryService discoveryService;
    private @Nullable ServiceRegistration<?> discoveryServiceRegistration;
    private final EntityTypeRegistry entityTypeRegistry = new EntityTypeRegistry();

    private static class CachedImage {
        private final @Nullable String lastUpdated;
        private final RawType rawType;

        CachedImage(@Nullable String lastUpdated, RawType rawType) {
            this.lastUpdated = lastUpdated;
            this.rawType = rawType;
        }

        public @Nullable String getLastUpdated() {
            return lastUpdated;
        }

        public RawType getRawType() {
            return rawType;
        }
    }

    private final ExpiringCacheMap<String, CachedImage> imageCache = new ExpiringCacheMap<>(IMAGE_CACHE_EXPIRATION);

    public HassLinkBridgeHandler(Bridge bridge, WebSocketFactory webSocketFactory) {
        super(bridge);
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public void initialize() {
        stopConnection();

        HassLinkBridgeConfiguration config = getConfigAs(HassLinkBridgeConfiguration.class);

        if (config.host.isBlank()) {
            Map<String, String> properties = editProperties();
            if (properties.containsKey(HassLinkBindingConstants.ENDPOINT_PROPERTY)) {
                properties.remove(HassLinkBindingConstants.ENDPOINT_PROPERTY);
                updateProperties(properties);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Home Assistant host must be configured");
            return;
        }

        if (config.token.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Home Assistant access token must be configured");
            return;
        }

        String serverEndpoint = EndpointUtils.normalizeEndpoint(config.host, config.port);
        Map<String, String> properties = editProperties();
        if (!serverEndpoint.equals(properties.get(HassLinkBindingConstants.ENDPOINT_PROPERTY))) {
            properties.put(HassLinkBindingConstants.ENDPOINT_PROPERTY, serverEndpoint);
            updateProperties(properties);
        }

        URI wsUri;
        try {
            wsUri = HomeAssistantWebSocketUriResolver.resolveWebSocketUri(config.host, config.port, config.secure);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        restBaseUri = (config.secure ? "https" : "http") + "://" + config.host + ":" + config.port;
        accessToken = config.token;

        HomeAssistantWebSocketClient newClient = new HomeAssistantWebSocketClient(
                webSocketFactory.getCommonWebSocketClient(), config.token, this, scheduler);
        client = newClient;
        try {
            newClient.start(wsUri.toString());
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        registerDiscoveryService();
    }

    @Override
    public void dispose() {
        stopConnection();
        unregisterDiscoveryService();
        registeredDeviceHandlers.clear();
        entityHandlers.clear();
        super.dispose();
    }

    private void stopConnection() {
        stopRegistryPolling();
        registry.clear();
        haConfig = null;
        HomeAssistantWebSocketClient localClient = client;
        if (localClient != null) {
            localClient.stop();
            client = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The bridge itself does not expose any channels
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HassLinkActions.class);
    }

    public HassLinkBridgeConfiguration getBridgeConfiguration() {
        return getConfigAs(HassLinkBridgeConfiguration.class);
    }

    public HomeAssistantRegistry getRegistry() {
        return registry;
    }

    public @Nullable HomeAssistantConfig getHaConfig() {
        return haConfig;
    }

    public @Nullable JsonObject getRawHaConfig() {
        return rawHaConfig;
    }

    public String getRestBaseUri() {
        return restBaseUri;
    }

    public void registerDeviceHandler(HassLinkDeviceHandler handler) {
        registeredDeviceHandlers.add(handler);
        notifyEntityMappingsChanged();
    }

    public void unregisterDeviceHandler(HassLinkDeviceHandler handler) {
        registeredDeviceHandlers.remove(handler);
        notifyEntityMappingsChanged();
    }

    public void notifyEntityMappingsChanged() {
        reconcileSubscriptions();
    }

    public synchronized void reconcileSubscriptions() {
        Map<String, Set<HassLinkDeviceHandler>> newMap = new HashMap<>();
        for (HassLinkDeviceHandler handler : registeredDeviceHandlers) {
            for (String entityId : handler.getRequiredEntityIds()) {
                Objects.requireNonNull(newMap.computeIfAbsent(entityId, k -> ConcurrentHashMap.newKeySet()))
                        .add(handler);
            }
        }

        entityHandlers.keySet().removeIf(entityId -> !newMap.containsKey(entityId));
        newMap.forEach((entityId, handlers) -> {
            Set<HassLinkDeviceHandler> current = Objects
                    .requireNonNull(entityHandlers.computeIfAbsent(entityId, k -> ConcurrentHashMap.newKeySet()));
            current.retainAll(handlers);
            current.addAll(handlers);
        });

        syncEntitySubscriptions();
    }

    public void syncEntitySubscriptions() {
        HomeAssistantWebSocketClient localClient = this.client;
        if (localClient != null && localClient.isConnected()) {
            localClient.updateEntitySubscriptions(Set.copyOf(entityHandlers.keySet()));
        }
    }

    public void sendRegistryListRequest() {
        HomeAssistantWebSocketClient localClient = this.client;
        if (localClient != null && localClient.isConnected()) {
            logger.debug("Requesting Home Assistant device and entity registries");
            localClient.sendRegistryListRequest();
        }
    }

    public void callService(ServiceCall serviceCall) {
        callService(serviceCall.domain(), serviceCall.service(), serviceCall.entityId(), serviceCall.serviceData());
    }

    public void callService(String domain, String service, @Nullable String entityId,
            @Nullable Map<String, Object> serviceData) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Ignoring service call {}.{} for entity {}: Bridge is offline", domain, service, entityId);
            return;
        }

        HomeAssistantWebSocketClient localClient = this.client;
        if (localClient == null || !localClient.isConnected()) {
            logger.warn("Ignoring service call {}.{} for entity {}: WebSocket disconnected", domain, service, entityId);
            return;
        }

        logger.debug("Executing HA service call -> Domain: {}, Service: {}, entityId: {}, Payload: {}", domain, service,
                entityId, serviceData);

        localClient.callService(domain, service, entityId, serviceData).whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to execute HA service call {}.{} for {}: {}", domain, service, entityId,
                        throwable.getMessage(), throwable);
            }
        });
    }

    public void refreshEntity(String entityId) {
        EntityState cachedState = lastKnownStates.get(entityId);
        if (cachedState != null) {
            String imageSource = ImageUtils.extractImageSource(cachedState);
            if (imageSource != null) {
                imageCache.invalidate(imageSource);
            }
            dispatchState(cachedState);
        }
    }

    public void fetchImage(String pathOrUrl, @Nullable String imageLastUpdated, Consumer<RawType> consumer) {
        if (pathOrUrl.isBlank()) {
            return;
        }

        boolean isCacheable = imageLastUpdated != null && !imageLastUpdated.isBlank();

        if (isCacheable) {
            CachedImage cached = imageCache.refresh(pathOrUrl);
            if (cached != null && Objects.equals(cached.getLastUpdated(), imageLastUpdated)) {
                consumer.accept(cached.getRawType());
                return;
            }
        }

        URI uri;
        boolean requiresAuth = false;
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            uri = URI.create(pathOrUrl);
            if (!restBaseUri.isBlank() && uri.toString().startsWith(restBaseUri)) {
                requiresAuth = true;
            }
        } else if (pathOrUrl.startsWith("/")) {
            uri = URI.create(restBaseUri + pathOrUrl);
            requiresAuth = true;
        } else {
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).GET();
        if (requiresAuth && !accessToken.isBlank()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }

        logger.debug("Fetching Home Assistant image from {} (requiresAuth={})", uri, requiresAuth);

        httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofByteArray()).thenAccept(response -> {
            if (response.statusCode() / 100 == 2) {
                byte[] body = response.body();
                if (body != null && body.length > 0) {
                    String headerValue = response.headers().firstValue("Content-Type").orElse(null);
                    String contentType = "image/jpeg";
                    if (headerValue != null && !headerValue.isBlank()) {
                        String mime = headerValue.split(";")[0].trim();
                        if (!mime.isBlank()) {
                            contentType = mime;
                        }
                    }

                    RawType rawType = new RawType(body, contentType);

                    if (isCacheable) {
                        imageCache.put(pathOrUrl, () -> new CachedImage(imageLastUpdated, rawType));
                    }

                    logger.trace("Fetched Home Assistant image from {} ({} bytes, contentType={})", uri, body.length,
                            contentType);

                    consumer.accept(rawType);
                }
            }
        }).whenComplete((response, error) -> {
            if (error != null) {
                logger.debug("Failed to fetch Home Assistant image from {}: {}", pathOrUrl, error.getMessage());
            }
        });
    }

    public void fetchImage(String pathOrUrl, Consumer<RawType> consumer) {
        fetchImage(pathOrUrl, null, consumer);
    }

    public void pressButton(String buttonIdOrEntityId) {
        EntityId parsedEntityId = EntityUtils.parseEntityId(buttonIdOrEntityId, "button");
        if (parsedEntityId == null) {
            return;
        }

        String domain = parsedEntityId.domain();

        if (!BUTTON_DOMAINS.contains(domain)) {
            logger.warn("pressButton called with unexpected domain '{}' for entity '{}'. Expected one of {}.", domain,
                    buttonIdOrEntityId, BUTTON_DOMAINS);
        }

        callService(domain, "press", parsedEntityId.getFullId(), null);
    }

    public void runScript(String scriptIdOrEntityId, @Nullable Map<String, Object> variables) {
        EntityId parsedEntityId = EntityUtils.parseEntityId(scriptIdOrEntityId, "script");
        if (parsedEntityId == null) {
            return;
        }

        if (!"script".equals(parsedEntityId.domain())) {
            logger.warn(
                    "runScript called with non-script entity ID '{}'. Expected domain 'script' or a simple script ID.",
                    scriptIdOrEntityId);
        }

        String scriptId = parsedEntityId.objectId();
        callService("script", scriptId, null, variables);
    }

    public void activateScene(String sceneIdOrEntityId) {
        EntityId parsedEntityId = EntityUtils.parseEntityId(sceneIdOrEntityId, "scene");
        if (parsedEntityId == null) {
            return;
        }

        if (!"scene".equals(parsedEntityId.domain())) {
            logger.warn(
                    "activateScene called with non-scene entity ID '{}'. Expected domain 'scene' or a simple scene ID.",
                    sceneIdOrEntityId);
        }

        callService("scene", "turn_on", parsedEntityId.getFullId(), null);
    }

    public @Nullable EntityState getCachedEntityState(String entityId) {
        return lastKnownStates.get(entityId);
    }

    @Override
    public void onAuthenticated(String haVersion) {
        registry.clear();
        haConfig = null;

        Map<String, String> properties = editProperties();
        boolean changed = false;

        if (!"Home Assistant".equals(properties.get(Thing.PROPERTY_VENDOR))) {
            properties.put(Thing.PROPERTY_VENDOR, "Home Assistant");
            changed = true;
        }
        if (!"Home Assistant Core".equals(properties.get(Thing.PROPERTY_MODEL_ID))) {
            properties.put(Thing.PROPERTY_MODEL_ID, "Home Assistant Core");
            changed = true;
        }
        if (!Objects.equals(haVersion, properties.get(Thing.PROPERTY_FIRMWARE_VERSION))) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, haVersion);
            changed = true;
        }

        if (changed) {
            updateProperties(properties);
        }

        updateStatus(ThingStatus.ONLINE);
        reconcileSubscriptions();
        startRegistryPolling();
    }

    @Override
    public void onAuthenticationFailed(String message) {
        stopRegistryPolling();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
    }

    @Override
    public void onConnectionClosed(String reason) {
        stopRegistryPolling();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
    }

    @Override
    public void onConnectionError(String message) {
        stopRegistryPolling();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    @Override
    public void onConfigSnapshot(HomeAssistantConfig config, JsonObject rawConfig) {
        this.haConfig = config;
        this.rawHaConfig = rawConfig;
    }

    @Override
    public void onEntitiesSnapshot(List<EntityState> entities) {
        for (EntityState state : entities) {
            dispatchState(state);
        }
    }

    @Override
    public void onDeviceRegistrySnapshot(List<JsonObject> devices) {
        registry.updateDeviceSnapshot(devices);
        HassLinkDiscoveryService localDiscoveryService = discoveryService;
        if (localDiscoveryService != null) {
            localDiscoveryService.onRegistryUpdated();
        }
        notifyChildHandlersRegistryUpdated();
    }

    @Override
    public void onEntityRegistrySnapshot(List<JsonObject> entities) {
        registry.updateEntitySnapshot(entities);
        HassLinkDiscoveryService localDiscoveryService = discoveryService;
        if (localDiscoveryService != null) {
            localDiscoveryService.onRegistryUpdated();
        }
        notifyChildHandlersRegistryUpdated();
    }

    private void notifyChildHandlersRegistryUpdated() {
        for (Thing childThing : getThing().getThings()) {
            if (childThing.getHandler() instanceof HassLinkDeviceHandler deviceHandler) {
                deviceHandler.onRegistryUpdated();
            }
        }
    }

    public boolean isRegistryLoaded() {
        return registry.isLoaded();
    }

    public Set<String> getEntitiesForDevice(String deviceId) {
        if (deviceId.isBlank()) {
            return Set.of();
        }
        return registry.getEntityIdsForDevice(deviceId);
    }

    public EntityType getEntityType(EntityState state) {
        return getEntityTypeByDomain(state.getDomain());
    }

    public EntityType getEntityTypeByDomain(String domain) {
        return entityTypeRegistry.getByType(domain);
    }

    @Override
    public void onEntityStateChanged(EntityState state) {
        dispatchState(state);
    }

    @Override
    public void onCompressedEvent(EntityStateCompressedEvent compressedEvent) {
        Map<String, CompressedEntityState> addedEntities = compressedEvent.a;
        if (addedEntities != null) {
            for (Map.Entry<String, CompressedEntityState> entry : addedEntities.entrySet()) {
                String entityId = entry.getKey();
                CompressedEntityState compState = entry.getValue();

                if (entityId.isBlank()) {
                    continue;
                }
                String newState = Objects.requireNonNullElse(compState.state, "");
                Map<String, JsonElement> newAttributes = Objects.requireNonNullElse(compState.attributes, Map.of());
                EntityState state = new EntityState(entityId, newState, newAttributes);
                dispatchState(state);
            }
        }

        Map<String, CompressedEntityDiff> diffEntities = compressedEvent.c;
        if (diffEntities != null) {
            for (Map.Entry<String, CompressedEntityDiff> entry : diffEntities.entrySet()) {
                String entityId = entry.getKey();
                if (entityId.isBlank()) {
                    continue;
                }
                CompressedEntityDiff diff = entry.getValue();

                EntityState cachedState = lastKnownStates.get(entityId);
                String cachedStateValue = cachedState == null ? "" : cachedState.state();
                Map<String, JsonElement> cachedAttributes = cachedState == null ? new HashMap<>()
                        : new HashMap<>(cachedState.attributes());

                CompressedEntityState add = diff.add;
                if (add != null) {
                    String addState = add.state;
                    if (addState != null) {
                        cachedStateValue = addState;
                    }
                    Map<String, JsonElement> attributesToAdd = add.attributes;
                    if (attributesToAdd != null) {
                        cachedAttributes.putAll(attributesToAdd);
                    }
                }

                Map<String, JsonElement> attributesToRemove = diff.remove;
                if (attributesToRemove != null && !cachedAttributes.isEmpty()) {
                    for (String keyToRemove : attributesToRemove.keySet()) {
                        cachedAttributes.remove(keyToRemove);
                    }
                }

                dispatchState(new EntityState(entityId, cachedStateValue, cachedAttributes));
            }
        }

        List<String> removedEntities = compressedEvent.r;
        if (removedEntities != null) {
            for (String removedEntityId : removedEntities) {
                if (!removedEntityId.isBlank()) {
                    lastKnownStates.remove(removedEntityId);
                }
            }
        }
    }

    private synchronized void startRegistryPolling() {
        stopRegistryPolling();
        logger.debug("Scheduling background registry polling every {} minutes", REGISTRY_POLLING_INTERVAL_MINUTES);
        registryPollingFuture = scheduler.scheduleWithFixedDelay(this::sendRegistryListRequest,
                REGISTRY_POLLING_INTERVAL_MINUTES, REGISTRY_POLLING_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private synchronized void stopRegistryPolling() {
        ScheduledFuture<?> future = registryPollingFuture;
        if (future != null) {
            future.cancel(false);
            registryPollingFuture = null;
        }
    }

    private void dispatchState(EntityState state) {
        String entityId = state.entityId();
        if (!EntityUtils.isValidEntityId(entityId)) {
            return;
        }
        lastKnownStates.put(entityId, state);
        Set<HassLinkDeviceHandler> handlers = entityHandlers.get(entityId);
        if (handlers != null) {
            handlers.forEach(handler -> handler.onEntityStateUpdate(state));
        }
    }

    private void registerDiscoveryService() {
        if (discoveryServiceRegistration == null) {
            BundleContext bundleContext = FrameworkUtil.getBundle(HassLinkBridgeHandler.class).getBundleContext();
            if (bundleContext != null) {
                HassLinkDiscoveryService newDiscoveryService = new HassLinkDiscoveryService();
                newDiscoveryService.setBridgeHandler(this);
                this.discoveryService = newDiscoveryService;

                discoveryServiceRegistration = bundleContext.registerService(
                        new String[] { DiscoveryService.class.getName(), HassLinkDiscoveryService.class.getName() },
                        newDiscoveryService, null);
            }
        }
    }

    private void unregisterDiscoveryService() {
        HassLinkDiscoveryService localDiscoveryService = discoveryService;
        if (localDiscoveryService != null) {
            localDiscoveryService.removeDiscoveredEntities(); // Notify Inbox FIRST while listener is active
            discoveryService = null;
        }
        ServiceRegistration<?> localRegistration = discoveryServiceRegistration;
        if (localRegistration != null) {
            localRegistration.unregister();
            discoveryServiceRegistration = null;
        }
    }
}
