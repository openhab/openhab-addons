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
package org.openhab.binding.hue.internal.handler;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.api.dto.clip2.ProductData;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.Archetype;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.CategoryType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;
import org.openhab.binding.hue.internal.config.Clip2BridgeConfig;
import org.openhab.binding.hue.internal.connection.Clip2Bridge;
import org.openhab.binding.hue.internal.connection.HueTlsTrustManagerProvider;
import org.openhab.binding.hue.internal.discovery.Clip2ThingDiscoveryService;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.AssetNotLoadedException;
import org.openhab.binding.hue.internal.exceptions.HttpUnauthorizedException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler for a CLIP 2 bridge. It communicates with the bridge via CLIP 2 end points, and reads and writes API
 * V2 resource objects. It also subscribes to the server's SSE event stream, and receives SSE events from it.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class Clip2BridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE_API2);

    private static final int FAST_SCHEDULE_MILLI_SECONDS = 500;
    private static final int APPLICATION_KEY_MAX_TRIES = 600; // i.e. 300 seconds, 5 minutes
    private static final int RECONNECT_DELAY_SECONDS = 10;
    private static final int RECONNECT_MAX_TRIES = 5;

    private static final ResourceReference DEVICE = new ResourceReference().setType(ResourceType.DEVICE);
    private static final ResourceReference ROOM = new ResourceReference().setType(ResourceType.ROOM);
    private static final ResourceReference ZONE = new ResourceReference().setType(ResourceType.ZONE);
    private static final ResourceReference BRIDGE = new ResourceReference().setType(ResourceType.BRIDGE);
    private static final ResourceReference BRIDGE_HOME = new ResourceReference().setType(ResourceType.BRIDGE_HOME);
    private static final ResourceReference SCENE = new ResourceReference().setType(ResourceType.SCENE);
    private static final ResourceReference SMART_SCENE = new ResourceReference().setType(ResourceType.SMART_SCENE);
    private static final ResourceReference SCRIPT = new ResourceReference().setType(ResourceType.BEHAVIOR_SCRIPT);
    private static final ResourceReference BEHAVIOR = new ResourceReference().setType(ResourceType.BEHAVIOR_INSTANCE);

    private static final String AUTOMATION_CHANNEL_LABEL_KEY = "dynamic-channel.automation-enable.label";
    private static final String AUTOMATION_CHANNEL_DESCRIPTION_KEY = "dynamic-channel.automation-enable.description";

    /**
     * List of resource references that need to be mass down loaded.
     * NOTE: the SCENE resources must be mass down loaded first!
     */
    private static final List<ResourceReference> MASS_DOWNLOAD_RESOURCE_REFERENCES = List.of(SCENE, DEVICE, ROOM, ZONE);

    private final Logger logger = LoggerFactory.getLogger(Clip2BridgeHandler.class);

    private final HttpClientFactory httpClientFactory;
    private final ThingRegistry thingRegistry;
    private final Bundle bundle;
    private final LocaleProvider localeProvider;
    private final TranslationProvider translationProvider;
    private final Map<String, Resource> automationsCache = new ConcurrentHashMap<>();
    private final Set<String> automationScriptIds = ConcurrentHashMap.newKeySet();
    private final ChannelGroupUID automationChannelGroupUID;

    private @Nullable Clip2Bridge clip2Bridge;
    private @Nullable ServiceRegistration<?> trustManagerRegistration;
    private @Nullable Clip2ThingDiscoveryService discoveryService;

    private @Nullable Future<?> updateAutomationChannelsTask;
    private @Nullable Future<?> checkConnectionTask;
    private @Nullable Future<?> updateOnlineStateTask;
    private @Nullable ScheduledFuture<?> scheduledUpdateTask;
    private Map<Integer, Future<?>> resourcesEventTasks = new ConcurrentHashMap<>();

    private boolean assetsLoaded;
    private int applKeyRetriesRemaining;
    private int connectRetriesRemaining;

    public Clip2BridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory, ThingRegistry thingRegistry,
            LocaleProvider localeProvider, TranslationProvider translationProvider) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.thingRegistry = thingRegistry;
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
        this.automationChannelGroupUID = new ChannelGroupUID(thing.getUID(), CHANNEL_GROUP_AUTOMATION);
    }

    /**
     * Cancel the given task.
     *
     * @param cancelTask the task to be cancelled (may be null)
     * @param mayInterrupt allows cancel() to interrupt the thread.
     */
    private void cancelTask(@Nullable Future<?> cancelTask, boolean mayInterrupt) {
        if (Objects.nonNull(cancelTask)) {
            cancelTask.cancel(mayInterrupt);
        }
    }

    /**
     * Check if assets are loaded.
     *
     * @throws AssetNotLoadedException if assets not loaded.
     */
    private void checkAssetsLoaded() throws AssetNotLoadedException {
        if (!assetsLoaded) {
            throw new AssetNotLoadedException("Assets not loaded");
        }
    }

    /**
     * Try to connect and set the online status accordingly. If the connection attempt throws an
     * HttpUnAuthorizedException then try to register the existing application key, or create a new one, with the hub.
     * If the connection attempt throws an ApiException then set the thing status to offline. This method is called on a
     * scheduler thread, which reschedules itself repeatedly until the thing is shutdown.
     */
    private synchronized void checkConnection() {
        logger.debug("checkConnection()");

        boolean retryApplicationKey = false;
        boolean retryConnection = false;

        try {
            checkAssetsLoaded();
            getClip2Bridge().testConnectionState();
            updateSelf(); // go online
        } catch (HttpUnauthorizedException unauthorizedException) {
            logger.debug("checkConnection() {}", unauthorizedException.getMessage(), unauthorizedException);
            if (applKeyRetriesRemaining > 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.api2.conf-error.press-pairing-button");
                try {
                    registerApplicationKey();
                    retryApplicationKey = true;
                } catch (HttpUnauthorizedException e) {
                    retryApplicationKey = true;
                } catch (ApiException e) {
                    setStatusOfflineWithCommunicationError(e);
                } catch (IllegalStateException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.api2.conf-error.read-only");
                } catch (AssetNotLoadedException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.api2.conf-error.assets-not-loaded");
                } catch (InterruptedException e) {
                    return;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.api2.conf-error.not-authorized");
            }
        } catch (ApiException e) {
            logger.debug("checkConnection() {}", e.getMessage(), e);
            setStatusOfflineWithCommunicationError(e);
            retryConnection = connectRetriesRemaining > 0;
        } catch (AssetNotLoadedException e) {
            logger.debug("checkConnection() {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.api2.conf-error.assets-not-loaded");
        } catch (InterruptedException e) {
            return;
        }

        int milliSeconds;
        if (retryApplicationKey) {
            // short delay used during attempts to create or validate an application key
            milliSeconds = FAST_SCHEDULE_MILLI_SECONDS;
            applKeyRetriesRemaining--;
        } else {
            // default delay, set via configuration parameter, used as heart-beat 'just-in-case'
            Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
            milliSeconds = config.checkMinutes * 60000;
            if (retryConnection) {
                // exponential back off delay used during attempts to reconnect
                int backOffDelay = 60000 * (int) Math.pow(2, RECONNECT_MAX_TRIES - connectRetriesRemaining);
                milliSeconds = Math.min(milliSeconds, backOffDelay);
                connectRetriesRemaining--;
            }
        }

        // this method schedules itself to be called again in a loop..
        cancelTask(checkConnectionTask, false);
        checkConnectionTask = scheduler.schedule(() -> checkConnection(), milliSeconds, TimeUnit.MILLISECONDS);
    }

    private void setStatusOfflineWithCommunicationError(Exception e) {
        Throwable cause = e.getCause();
        String causeMessage = cause == null ? null : cause.getMessage();
        if (causeMessage == null || causeMessage.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.api2.comm-error.exception [\"" + e.getMessage() + "\"]");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.api2.comm-error.exception [\"" + e.getMessage() + " -> " + causeMessage + "\"]");
        }
    }

    /**
     * If a child thing has been added, and the bridge is online, update the child's data.
     */
    public void childInitialized() {
        if (thing.getStatus() == ThingStatus.ONLINE) {
            updateThingsScheduled(5000);
        }
    }

    @Override
    public void dispose() {
        if (assetsLoaded) {
            disposeAssets();
        }
    }

    /**
     * Dispose the bridge handler's assets. Called from dispose() on a thread, so that dispose() itself can complete
     * faster.
     */
    private void disposeAssets() {
        logger.debug("disposeAssets() {}", this);
        synchronized (this) {
            assetsLoaded = false;
            cancelTask(updateAutomationChannelsTask, true);
            cancelTask(checkConnectionTask, true);
            cancelTask(updateOnlineStateTask, true);
            cancelTask(scheduledUpdateTask, true);
            updateAutomationChannelsTask = null;
            checkConnectionTask = null;
            updateOnlineStateTask = null;
            scheduledUpdateTask = null;
            synchronized (resourcesEventTasks) {
                resourcesEventTasks.values().forEach(task -> cancelTask(task, true));
                resourcesEventTasks.clear();
            }
            ServiceRegistration<?> registration = trustManagerRegistration;
            if (Objects.nonNull(registration)) {
                registration.unregister();
                trustManagerRegistration = null;
            }
            Clip2Bridge bridge = clip2Bridge;
            if (Objects.nonNull(bridge)) {
                bridge.close();
                clip2Bridge = null;
            }
            Clip2ThingDiscoveryService disco = discoveryService;
            if (Objects.nonNull(disco)) {
                disco.abortScan();
            }
        }
    }

    /**
     * Return the application key for the console app.
     *
     * @return the application key.
     */
    public String getApplicationKey() {
        Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
        return config.applicationKey;
    }

    /**
     * Get the Clip2Bridge connection and throw an exception if it is null.
     *
     * @return the Clip2Bridge.
     * @throws AssetNotLoadedException if the Clip2Bridge is null.
     */
    private Clip2Bridge getClip2Bridge() throws AssetNotLoadedException {
        Clip2Bridge clip2Bridge = this.clip2Bridge;
        if (Objects.nonNull(clip2Bridge)) {
            return clip2Bridge;
        }
        throw new AssetNotLoadedException("Clip2Bridge is null");
    }

    /**
     * Return the IP address for the console app.
     *
     * @return the IP address.
     */
    public String getIpAddress() {
        Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
        return config.ipAddress;
    }

    /**
     * Get the v1 legacy Hue bridge (if any) which has the same IP address as this.
     *
     * @return Optional result containing the legacy bridge (if any found).
     */
    public Optional<Thing> getLegacyBridge() {
        String ipAddress = getIpAddress();
        return Objects.nonNull(ipAddress)
                ? thingRegistry.getAll().stream()
                        .filter(thing -> thing.getThingTypeUID().equals(THING_TYPE_BRIDGE)
                                && ipAddress.equals(thing.getConfiguration().get("ipAddress")))
                        .findFirst()
                : Optional.empty();
    }

    /**
     * Get the v1 legacy Hue thing (if any) which has a Bridge having the same IP address as this, and an ID that
     * matches the given parameter.
     *
     * @param targetIdV1 the idV1 attribute to match.
     * @return Optional result containing the legacy thing (if found).
     */
    public Optional<Thing> getLegacyThing(String targetIdV1) {
        Optional<Thing> legacyBridge = getLegacyBridge();
        if (legacyBridge.isEmpty()) {
            return Optional.empty();
        }

        String config;
        if (targetIdV1.startsWith("/lights/")) {
            config = LIGHT_ID;
        } else if (targetIdV1.startsWith("/sensors/")) {
            config = SENSOR_ID;
        } else if (targetIdV1.startsWith("/groups/")) {
            config = GROUP_ID;
        } else {
            return Optional.empty();
        }

        ThingUID legacyBridgeUID = legacyBridge.get().getUID();
        return thingRegistry.getAll().stream()
                .filter(thing -> legacyBridgeUID.equals(thing.getBridgeUID())
                        && V1_THING_TYPE_UIDS.contains(thing.getThingTypeUID())
                        && thing.getConfiguration().get(config) instanceof String id && targetIdV1.endsWith("/" + id))
                .findFirst();
    }

    /**
     * Return a localized text.
     *
     * @param key the i18n text key.
     * @param arguments for parameterized translation.
     * @return the localized text.
     */
    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    /**
     * Execute an HTTP GET for a resources reference object from the server.
     *
     * @param reference containing the resourceType and (optionally) the resourceId of the resource to get. If the
     *            resourceId is null then all resources of the given type are returned.
     * @return the resource, or null if something fails.
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     * @throws InterruptedException
     */
    public Resources getResources(ResourceReference reference)
            throws ApiException, AssetNotLoadedException, InterruptedException {
        logger.debug("getResources() {}", reference);
        checkAssetsLoaded();
        return getClip2Bridge().getResources(reference);
    }

    /**
     * Getter for the scheduler.
     *
     * @return the scheduler.
     */
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(Clip2ThingDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_GROUP_AUTOMATION.equals(channelUID.getGroupId())) {
            try {
                if (RefreshType.REFRESH.equals(command)) {
                    updateAutomationChannelsNow();
                    return;
                } else {
                    Resources resources = getClip2Bridge().putResource(new Resource(ResourceType.BEHAVIOR_INSTANCE)
                            .setId(channelUID.getIdWithoutGroup()).setEnabled(command));
                    if (resources.hasErrors()) {
                        logger.warn("handleCommand({}, {}) succeeded with errors: {}", channelUID, command,
                                String.join("; ", resources.getErrors()));
                    }
                }
            } catch (ApiException | AssetNotLoadedException e) {
                logger.warn("handleCommand({}, {}) error {}", channelUID, command, e.getMessage(),
                        logger.isDebugEnabled() ? e : null);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void initialize() {
        updateThingFromLegacy();
        updateStatus(ThingStatus.UNKNOWN);
        applKeyRetriesRemaining = APPLICATION_KEY_MAX_TRIES;
        connectRetriesRemaining = RECONNECT_MAX_TRIES;
        initializeAssets();
    }

    /**
     * Initialize the bridge handler's assets.
     */
    private void initializeAssets() {
        logger.debug("initializeAssets() {}", this);
        synchronized (this) {
            Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);

            String ipAddress = config.ipAddress;
            if (ipAddress.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-no-ip-address");
                return;
            }

            try {
                if (!Clip2Bridge.isClip2Supported(ipAddress)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.api2.conf-error.clip2-not-supported");
                    return;
                }
            } catch (IOException e) {
                logger.trace("initializeAssets() communication error on '{}'", ipAddress, e);
                setStatusOfflineWithCommunicationError(e);
                return;
            }

            HueTlsTrustManagerProvider trustManagerProvider = new HueTlsTrustManagerProvider(ipAddress + ":443",
                    config.useSelfSignedCertificate);

            if (Objects.isNull(trustManagerProvider.getPEMTrustManager())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.api2.conf-error.certificate-load");
                return;
            }

            trustManagerRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
                    .registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider, null);

            String applicationKey = config.applicationKey;
            applicationKey = Objects.nonNull(applicationKey) ? applicationKey : "";

            try {
                clip2Bridge = new Clip2Bridge(httpClientFactory, this, ipAddress, applicationKey);
            } catch (ApiException e) {
                logger.trace("initializeAssets() communication error on '{}'", ipAddress, e);
                setStatusOfflineWithCommunicationError(e);
                return;
            }

            assetsLoaded = true;
        }
        cancelTask(checkConnectionTask, false);
        checkConnectionTask = scheduler.submit(() -> checkConnection());
    }

    /**
     * Called when the connection goes offline. Schedule a reconnection.
     */
    public void onConnectionOffline() {
        if (assetsLoaded) {
            cancelTask(checkConnectionTask, false);
            checkConnectionTask = scheduler.schedule(() -> checkConnection(), RECONNECT_DELAY_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Called when the connection goes online. Schedule a general state update.
     */
    public void onConnectionOnline() {
        cancelTask(updateOnlineStateTask, false);
        updateOnlineStateTask = scheduler.schedule(() -> updateOnlineState(), 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Called when an SSE event message comes in with a valid list of resources. For each resource received, inform all
     * child thing handlers with the respective resource.
     *
     * @param resources a list of incoming resource objects.
     */
    public void onResourcesEvent(List<Resource> resources) {
        if (assetsLoaded) {
            synchronized (resourcesEventTasks) {
                int index = resourcesEventTasks.size();
                resourcesEventTasks.put(index, scheduler.submit(() -> {
                    onResourcesEventTask(resources);
                    resourcesEventTasks.remove(index);
                }));
            }
        }
    }

    private void onResourcesEventTask(List<Resource> resources) {
        int numberOfResources = resources.size();
        logger.debug("onResourcesEventTask() resource count {}", numberOfResources);
        Setters.mergeLightResources(resources);
        if (numberOfResources != resources.size()) {
            logger.debug("onResourcesEventTask() merged to {} resources", resources.size());
        }
        if (onResources(resources)) {
            updateAutomationChannelsNow();
        }
        getThing().getThings().forEach(thing -> {
            if (thing.getHandler() instanceof Clip2ThingHandler clip2ThingHandler) {
                clip2ThingHandler.onResources(resources);
            }
        });
    }

    /**
     * Execute an HTTP PUT to send a Resource object to the server.
     *
     * @param resource the resource to put.
     * @return the resource, which may contain errors.
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     * @throws InterruptedException
     */
    public Resources putResource(Resource resource) throws ApiException, AssetNotLoadedException, InterruptedException {
        logger.debug("putResource() {}", resource);
        checkAssetsLoaded();
        return getClip2Bridge().putResource(resource);
    }

    /**
     * Register the application key with the hub. If the current application key is empty it will create a new one.
     *
     * @throws HttpUnauthorizedException if the communication was OK but the registration failed anyway.
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     * @throws IllegalStateException if the configuration cannot be changed e.g. read only.
     * @throws InterruptedException
     */
    private void registerApplicationKey() throws HttpUnauthorizedException, ApiException, AssetNotLoadedException,
            IllegalStateException, InterruptedException {
        logger.debug("registerApplicationKey()");
        Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
        String newApplicationKey = getClip2Bridge().registerApplicationKey(config.applicationKey);
        Configuration configuration = editConfiguration();
        configuration.put(Clip2BridgeConfig.APPLICATION_KEY, newApplicationKey);
        updateConfiguration(configuration);
    }

    /**
     * Register the discovery service.
     *
     * @param discoveryService new discoveryService.
     */
    public void registerDiscoveryService(Clip2ThingDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Unregister the discovery service.
     */
    public void unregisterDiscoveryService() {
        discoveryService = null;
    }

    /**
     * Update the bridge's online state and update its dependent things. Called when the connection goes online.
     */
    private void updateOnlineState() {
        if (assetsLoaded && (thing.getStatus() != ThingStatus.ONLINE)) {
            logger.debug("updateOnlineState()");
            connectRetriesRemaining = RECONNECT_MAX_TRIES;
            updateStatus(ThingStatus.ONLINE);
            loadAutomationScriptIds();
            updateAutomationChannelsNow();
            updateThingsScheduled(500);
            Clip2ThingDiscoveryService discoveryService = this.discoveryService;
            if (Objects.nonNull(discoveryService)) {
                discoveryService.startScan(null);
            }
        }
    }

    /**
     * Update the bridge thing properties.
     *
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     * @throws InterruptedException
     */
    private void updateProperties() throws ApiException, AssetNotLoadedException, InterruptedException {
        logger.debug("updateProperties()");
        Map<String, String> properties = new HashMap<>(thing.getProperties());

        for (Resource device : getClip2Bridge().getResources(BRIDGE).getResources()) {
            // set the serial number
            String bridgeId = device.getBridgeId();
            if (Objects.nonNull(bridgeId)) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, bridgeId);
            }
            break;
        }

        for (Resource device : getClip2Bridge().getResources(DEVICE).getResources()) {
            MetaData metaData = device.getMetaData();
            if (Objects.nonNull(metaData) && metaData.getArchetype() == Archetype.BRIDGE_V2) {
                // set resource properties
                properties.put(PROPERTY_RESOURCE_ID, device.getId());
                properties.put(PROPERTY_RESOURCE_TYPE, device.getType().toString());

                // set metadata properties
                String metaDataName = metaData.getName();
                if (Objects.nonNull(metaDataName)) {
                    properties.put(PROPERTY_RESOURCE_NAME, metaDataName);
                }
                properties.put(PROPERTY_RESOURCE_ARCHETYPE, metaData.getArchetype().toString());

                // set product data properties
                ProductData productData = device.getProductData();
                if (Objects.nonNull(productData)) {
                    // set generic thing properties
                    properties.put(Thing.PROPERTY_MODEL_ID, productData.getModelId());
                    properties.put(Thing.PROPERTY_VENDOR, productData.getManufacturerName());
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, productData.getSoftwareVersion());
                    String hardwarePlatformType = productData.getHardwarePlatformType();
                    if (Objects.nonNull(hardwarePlatformType)) {
                        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwarePlatformType);
                    }

                    // set hue specific properties
                    properties.put(PROPERTY_PRODUCT_NAME, productData.getProductName());
                    properties.put(PROPERTY_PRODUCT_ARCHETYPE, productData.getProductArchetype().toString());
                    properties.put(PROPERTY_PRODUCT_CERTIFIED, productData.getCertified().toString());
                }
                break; // we only needed the BRIDGE_V2 resource
            }
        }
        thing.setProperties(properties);
    }

    /**
     * Update the thing's own state. Called sporadically in case any SSE events may have been lost.
     */
    private void updateSelf() {
        logger.debug("updateSelf()");
        try {
            checkAssetsLoaded();
            updateProperties();
            getClip2Bridge().open();
        } catch (ApiException e) {
            logger.trace("updateSelf() {}", e.getMessage(), e);
            setStatusOfflineWithCommunicationError(e);
            onConnectionOffline();
        } catch (AssetNotLoadedException e) {
            logger.trace("updateSelf() {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.api2.conf-error.assets-not-loaded");
        } catch (InterruptedException e) {
        }
    }

    /**
     * Check if a PROPERTY_LEGACY_THING_UID value was set by the discovery process, and if so, clone the legacy thing's
     * settings into this thing.
     */
    private void updateThingFromLegacy() {
        if (isInitialized()) {
            logger.warn("Cannot update bridge thing '{}' from legacy since handler already initialized.",
                    thing.getUID());
            return;
        }
        Map<String, String> properties = thing.getProperties();
        String legacyThingUID = properties.get(PROPERTY_LEGACY_THING_UID);
        if (Objects.nonNull(legacyThingUID)) {
            Thing legacyThing = thingRegistry.get(new ThingUID(legacyThingUID));
            if (Objects.nonNull(legacyThing)) {
                BridgeBuilder editBuilder = editThing();

                String location = legacyThing.getLocation();
                if (Objects.nonNull(location) && !location.isBlank()) {
                    editBuilder = editBuilder.withLocation(location);
                }

                Object userName = legacyThing.getConfiguration().get(USER_NAME);
                if (userName instanceof String) {
                    Configuration configuration = thing.getConfiguration();
                    configuration.put(Clip2BridgeConfig.APPLICATION_KEY, userName);
                    editBuilder = editBuilder.withConfiguration(configuration);
                }

                Map<String, String> newProperties = new HashMap<>(properties);
                newProperties.remove(PROPERTY_LEGACY_THING_UID);

                updateThing(editBuilder.withProperties(newProperties).build());
            }
        }
    }

    /**
     * Execute the mass download of all relevant resource types, and inform all child thing handlers.
     */
    private void updateThingsNow() {
        logger.debug("updateThingsNow()");
        try {
            Clip2Bridge bridge = getClip2Bridge();
            for (ResourceReference reference : MASS_DOWNLOAD_RESOURCE_REFERENCES) {
                ResourceType resourceType = reference.getType();
                List<Resource> resourceList = bridge.getResources(reference).getResources();
                switch (resourceType) {
                    case ZONE:
                        // add special 'All Lights' zone to the zone resource list
                        resourceList.addAll(bridge.getResources(BRIDGE_HOME).getResources());
                        break;

                    case SCENE:
                        // add 'smart scenes' to the scene resource list
                        resourceList.addAll(bridge.getResources(SMART_SCENE).getResources());
                        break;

                    default:
                        break;
                }
                getThing().getThings().forEach(thing -> {
                    ThingHandler handler = thing.getHandler();
                    if (handler instanceof Clip2ThingHandler) {
                        ((Clip2ThingHandler) handler).onResourcesList(resourceType, resourceList);
                    }
                });
            }
        } catch (ApiException | AssetNotLoadedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("updateThingsNow() unexpected exception", e);
            } else {
                logger.warn("Unexpected exception '{}' while updating things.", e.getMessage());
            }
        } catch (InterruptedException e) {
        }
    }

    /**
     * Schedule a task to call updateThings(). It prevents floods of GET calls when multiple child things are added at
     * the same time.
     *
     * @param delayMilliSeconds the delay before running the next task.
     */
    private void updateThingsScheduled(int delayMilliSeconds) {
        ScheduledFuture<?> task = this.scheduledUpdateTask;
        if (Objects.isNull(task) || task.getDelay(TimeUnit.MILLISECONDS) < 100) {
            cancelTask(scheduledUpdateTask, false);
            scheduledUpdateTask = scheduler.schedule(() -> updateThingsNow(), delayMilliSeconds, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Load the set of automation script ids.
     */
    private void loadAutomationScriptIds() {
        try {
            synchronized (automationScriptIds) {
                automationScriptIds.clear();
                automationScriptIds.addAll(getClip2Bridge().getResources(SCRIPT).getResources().stream()
                        .filter(r -> CategoryType.AUTOMATION == r.getCategory()).map(r -> r.getId())
                        .collect(Collectors.toSet()));
            }
        } catch (ApiException | AssetNotLoadedException e) {
            logger.warn("loadAutomationScriptIds() unexpected exception {}", e.getMessage(),
                    logger.isDebugEnabled() ? e : null);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Create resp. update the automation channels
     */
    private void updateAutomationChannels() {
        List<Resource> automations;
        try {
            automations = getClip2Bridge().getResources(BEHAVIOR).getResources().stream()
                    .filter(r -> automationScriptIds.contains(r.getScriptId())).toList();
        } catch (ApiException | AssetNotLoadedException e) {
            logger.warn("Unexpected exception '{}' while updating channels.", e.getMessage(),
                    logger.isDebugEnabled() ? e : null);
            return;
        } catch (InterruptedException e) {
            return;
        }

        if (automations.size() != automationsCache.size() || automations.stream().anyMatch(automation -> {
            Resource cachedAutomation = automationsCache.get(automation.getId());
            return Objects.isNull(cachedAutomation) || !automation.getName().equals(cachedAutomation.getName());
        })) {
            synchronized (automationsCache) {
                automationsCache.clear();
                automationsCache.putAll(automations.stream().collect(Collectors.toMap(a -> a.getId(), a -> a)));
            }

            Stream<Channel> newChannels = automations.stream().map(a -> createAutomationChannel(a));
            Stream<Channel> oldchannels = thing.getChannels().stream()
                    .filter(c -> !CHANNEL_TYPE_AUTOMATION.equals(c.getChannelTypeUID()));

            updateThing(editThing().withChannels(Stream.concat(oldchannels, newChannels).toList()).build());
            onResources(automations);

            logger.debug("Bridge created {} automation channels", automations.size());
        }
    }

    /**
     * Start a task to update the automation channels
     */
    private void updateAutomationChannelsNow() {
        cancelTask(updateAutomationChannelsTask, false);
        updateAutomationChannelsTask = scheduler.submit(() -> updateAutomationChannels());
    }

    /**
     * Create an automation channel from an automation resource
     */
    private Channel createAutomationChannel(Resource automation) {
        String label = Objects.requireNonNullElse(translationProvider.getText(bundle, AUTOMATION_CHANNEL_LABEL_KEY,
                AUTOMATION_CHANNEL_LABEL_KEY, localeProvider.getLocale(), automation.getName()),
                AUTOMATION_CHANNEL_LABEL_KEY);

        String description = Objects.requireNonNullElse(
                translationProvider.getText(bundle, AUTOMATION_CHANNEL_DESCRIPTION_KEY,
                        AUTOMATION_CHANNEL_DESCRIPTION_KEY, localeProvider.getLocale(), automation.getName()),
                AUTOMATION_CHANNEL_DESCRIPTION_KEY);

        return ChannelBuilder
                .create(new ChannelUID(automationChannelGroupUID, automation.getId()), CoreItemFactory.SWITCH)
                .withLabel(label).withDescription(description).withType(CHANNEL_TYPE_AUTOMATION).build();
    }

    /**
     * Process event resources list
     *
     * @return true if the automation channels require updating
     */
    public boolean onResources(List<Resource> resources) {
        boolean requireUpdateChannels = false;
        for (Resource resource : resources) {
            if (ResourceType.BEHAVIOR_INSTANCE != resource.getType()) {
                continue;
            }
            String resourceId = resource.getId();
            switch (resource.getContentType()) {
                case ADD:
                    requireUpdateChannels |= automationScriptIds.contains(resource.getScriptId());
                    break;
                case DELETE:
                    requireUpdateChannels |= automationsCache.containsKey(resourceId);
                    break;
                case UPDATE:
                case FULL_STATE:
                    Resource cachedAutomation = automationsCache.get(resourceId);
                    if (Objects.isNull(cachedAutomation)) {
                        requireUpdateChannels |= automationScriptIds.contains(resource.getScriptId());
                    } else {
                        if (resource.hasName() && !resource.getName().equals(cachedAutomation.getName())) {
                            requireUpdateChannels = true;
                        } else if (Objects.nonNull(resource.getEnabled())) {
                            updateState(new ChannelUID(automationChannelGroupUID, resourceId),
                                    resource.getEnabledState());
                        }
                    }
                    break;
                default:
            }
        }
        return requireUpdateChannels;
    }
}
