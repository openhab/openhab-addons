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
package org.openhab.binding.hue.internal.handler;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hue.internal.HueBindingConstants;
import org.openhab.binding.hue.internal.config.Clip2BridgeConfig;
import org.openhab.binding.hue.internal.connection.Clip2Bridge;
import org.openhab.binding.hue.internal.connection.HueTlsTrustManagerProvider;
import org.openhab.binding.hue.internal.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.dto.clip2.ProductData;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.dto.clip2.Resources;
import org.openhab.binding.hue.internal.dto.clip2.enums.Archetype;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.AssetNotLoadedException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_CLIP2);

    private static final int FAST_SCHEDULE_MILLI_SECONDS = 500;
    private static final int APPLICATION_KEY_MAX_TRIES = 600; // i.e. 300 seconds, 5 minutes
    private static final int RECONNECT_MAX_TRIES = 5;

    private static final ResourceReference DEVICE = new ResourceReference().setType(ResourceType.DEVICE);

    private final Logger logger = LoggerFactory.getLogger(Clip2BridgeHandler.class);

    private final HttpClient httpClient;

    private @Nullable Clip2Bridge clip2Bridge;
    private @Nullable ScheduledFuture<?> checkConnectionTask;
    private @Nullable ServiceRegistration<?> trustManagerRegistration;

    private boolean assetsLoaded;
    private int applKeyRetriesRemaining;
    private int connectRetriesRemaining;

    public Clip2BridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
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
     * Try to connect and set the online status accordingly. If the connection attempt throws an IllegalAccessException
     * then try to register the existing application key, or create a new one, with the hub. If the connection attempt
     * throws an ApiException then set the thing status to offline. This method is called on a scheduler thread, which
     * reschedules itself repeatedly until the thing is shutdown.
     */
    private void checkConnection() {
        logger.debug("checkConnection() called");

        // check connection to the hub
        ThingStatusDetail thingStatus;
        try {
            checkAssetsLoaded();
            getClip2Bridge().testConnectionState();
            thingStatus = ThingStatusDetail.NONE;
        } catch (IllegalAccessException e) {
            logger.debug("checkConnection() {}", e.getMessage(), e);
            thingStatus = ThingStatusDetail.CONFIGURATION_ERROR;
        } catch (ApiException e) {
            logger.debug("checkConnection() {}", e.getMessage(), e);
            thingStatus = ThingStatusDetail.COMMUNICATION_ERROR;
        } catch (AssetNotLoadedException e) {
            logger.debug("checkConnection() {}", e.getMessage(), e);
            thingStatus = ThingStatusDetail.BRIDGE_UNINITIALIZED;
        }

        // update the thing status
        boolean retryApplicationKey = false;
        boolean retryConnection = false;
        switch (thingStatus) {
            case CONFIGURATION_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.clip2.conf-error-press-pairing-button");
                if (applKeyRetriesRemaining > 0) {
                    try {
                        registerApplicationKey();
                        retryApplicationKey = true;
                    } catch (IllegalAccessException e) {
                        retryApplicationKey = true;
                    } catch (ApiException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/offline.communication-error");
                    } catch (IllegalStateException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "@text/offline.clip2.conf-error-read-only");
                    } catch (AssetNotLoadedException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                                "@text/offline.clip2.conf-error-assets-not-loaded");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.clip2.conf-error-creation-applicationkey");
                }
                break;

            case COMMUNICATION_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error");
                retryConnection = connectRetriesRemaining > 0;
                break;

            case BRIDGE_UNINITIALIZED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "@text/offline.clip2.conf-error-assets-not-loaded");
                break;

            case NONE:
            default:
                updateSelf(); // go online
                break;
        }

        // this method schedules itself to be called again in a loop..
        ScheduledFuture<?> task = checkConnectionTask;
        if (Objects.nonNull(task)) {
            task.cancel(false);
        }
        int milliSeconds;
        if (retryApplicationKey) {
            // short delay used during attempts to create or validate an application key
            milliSeconds = FAST_SCHEDULE_MILLI_SECONDS;
            applKeyRetriesRemaining--;
        } else {
            // default delay, set via configuration parameter, used as heart-beat 'just-in-case'
            Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
            milliSeconds = config.checkSeconds * 1000;
            if (retryConnection) {
                // exponential back off delay used during attempts to reconnect
                int backOffDelay = 60000 * (int) Math.pow(2, RECONNECT_MAX_TRIES - connectRetriesRemaining);
                milliSeconds = Math.min(milliSeconds, backOffDelay);
                connectRetriesRemaining--;
            }
        }
        checkConnectionTask = scheduler.schedule(() -> checkConnection(), milliSeconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (thing.getStatus() == ThingStatus.ONLINE && (childHandler instanceof DeviceThingHandler)) {
            logger.debug("childHandlerInitialized() {}", childThing.getUID());
            try {
                ResourceReference reference = ((DeviceThingHandler) childHandler).getResourceReference();
                getClip2Bridge().getResources(reference).getResources().forEach(r -> onResource(r));
            } catch (ApiException | AssetNotLoadedException e) {
                // exceptions should not occur here; but log anyway (just in case)
                logger.warn("childHandlerInitialized() {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Return the application key for the console app.
     *
     * @return the application key.
     */
    public @Nullable String consoleGetApplicationKey() {
        Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
        return config.applicationKey;
    }

    /**
     * Return the ip address for the console app.
     *
     * @return the ip address.
     */
    public @Nullable String consoleGetIpAddress() {
        Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
        return config.ipAddress;
    }

    /**
     * Return a list of resources for the console app.
     *
     * @param type of resources to return.
     * @return list of resources of the given type.
     */
    public List<Resource> consoleGetResources(ResourceType type) {
        try {
            return getClip2Bridge().getResources(new ResourceReference().setType(type)).getResources();
        } catch (ApiException | AssetNotLoadedException e) {
        }
        return List.of();
    }

    @Override
    public void dispose() {
        logger.debug("dispose() {} called", this);
        if (assetsLoaded) {
            assetsLoaded = false;
            scheduler.submit(() -> disposeAssets());
        }
    }

    /**
     * Dispose the bridge handler's assets.
     */
    private void disposeAssets() {
        logger.debug("disposeAssets() {} called", this);
        synchronized (this) {
            assetsLoaded = false;
            ScheduledFuture<?> task = checkConnectionTask;
            if (Objects.nonNull(task)) {
                task.cancel(false);
                checkConnectionTask = null;
            }
            Clip2Bridge bridge = clip2Bridge;
            if (Objects.nonNull(bridge)) {
                bridge.close();
                clip2Bridge = null;
            }
            ServiceRegistration<?> registration = trustManagerRegistration;
            if (registration != null) {
                registration.unregister();
                trustManagerRegistration = null;
            }
        }
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
     * Execute an HTTP GET for a resources reference object from the server.
     *
     * @param reference containing the resourceType and (optionally) the resourceId of the resource to get. If the
     *            resourceId is null then all resources of the given type are returned.
     * @return the resource, or null if something fails.
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     */
    public Resources getResources(ResourceReference reference) throws ApiException, AssetNotLoadedException {
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            return;
        }
        if (CHANNEL_SCENE.equals(channelUID.getId()) && command instanceof StringType) {
            try {
                putResource(new Resource(ResourceType.SCENE).setId(command.toString()));
            } catch (ApiException | AssetNotLoadedException e) {
                logger.warn("handleCommand() error {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize() {} called", this);
        updateStatus(ThingStatus.UNKNOWN);
        applKeyRetriesRemaining = APPLICATION_KEY_MAX_TRIES;
        connectRetriesRemaining = RECONNECT_MAX_TRIES;
        scheduler.submit(() -> initializeAssets());
    }

    /**
     * Initialize the bridge handler's assets.
     */
    private void initializeAssets() {
        logger.debug("initializeAssets() {} called", this);
        synchronized (this) {
            Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);

            String ipAddress = config.ipAddress;
            if (Objects.isNull(ipAddress) || ipAddress.isEmpty()) {
                logger.debug("initializeAssets() invalid ip address '{}'", config.ipAddress);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-no-ip-address");
                return;
            }

            try {
                Clip2Bridge.testSupportsClip2(ipAddress);
            } catch (IllegalStateException e) {
                logger.debug("initializeAssets() hub does not support clip 2");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.clip2.conf-error-clip2-not-supported");
                return;
            } catch (IllegalArgumentException e) {
                logger.debug("initializeAssets() invalid ip address '{}'", ipAddress);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.clip2.conf-error-invalid-ip-address");
                return;
            } catch (ApiException e) {
                logger.debug("initializeAssets() communication error on '{}'", ipAddress);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error");
                return;
            }

            HueTlsTrustManagerProvider trustManagerProvider = new HueTlsTrustManagerProvider(ipAddress + ":443",
                    config.useSelfSignedCertificate);

            if (trustManagerProvider.getPEMTrustManager() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.clip2.conf-error-certificate-load");
                return;
            }

            trustManagerRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
                    .registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider, null);

            String applicationKey = config.applicationKey;
            applicationKey = Objects.nonNull(applicationKey) ? applicationKey : "";
            clip2Bridge = new Clip2Bridge(httpClient, this, ipAddress, applicationKey);

            assetsLoaded = true;
        }

        scheduler.submit(() -> checkConnection());
    }

    /**
     * Called when the connection goes offline. Schedule a reconnection event.
     */
    public void onConnectionOffline() {
        if (assetsLoaded) {
            logger.debug("onOffline() ThingStatus:OFFLINE");
            updateStatus(ThingStatus.OFFLINE);
            ScheduledFuture<?> task = checkConnectionTask;
            if (Objects.nonNull(task)) {
                task.cancel(false);
            }
            checkConnectionTask = scheduler.schedule(() -> checkConnection(), FAST_SCHEDULE_MILLI_SECONDS,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Called when the connection goes online.
     */
    public void onConnectionOnline() {
        if (assetsLoaded && (thing.getStatus() != ThingStatus.ONLINE)) {
            logger.debug("onOnline() ThingStatus:ONLINE");
            connectRetriesRemaining = RECONNECT_MAX_TRIES;
            updateStatus(ThingStatus.ONLINE);
            try {
                updateDevices();
            } catch (ApiException | AssetNotLoadedException e) {
                // should never happen as we are already online
            }
        }
    }

    /**
     * Inform all child thing handlers about the contents of the given resource.
     *
     * @param resource the given resource.
     */
    private void onResource(Resource resource) {
        logger.debug("onResource() {}", resource);
        getThing().getThings().forEach(thing -> {
            ThingHandler handler = thing.getHandler();
            if (handler instanceof DeviceThingHandler) {
                ((DeviceThingHandler) handler).onResource(resource);
            }
        });
    }

    /**
     * Called when an SSE event message comes in with a valid list of resources.
     *
     * @param resources a list of incoming resource objects.
     */
    public void onSseResources(List<Resource> resources) {
        if (assetsLoaded) {
            logger.debug("onSseResources() called with resource count {}", resources.size());
            resources.forEach(resource -> onResource(resource));
        }
    }

    /**
     * Execute an HTTP PUT to send a Resource object to the server.
     *
     * @param resource the resource to put.
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     */
    public void putResource(Resource resource) throws ApiException, AssetNotLoadedException {
        logger.debug("putResource() {}", resource);
        checkAssetsLoaded();
        getClip2Bridge().putResource(resource);
    }

    /**
     * Register the application key with the hub. If the current application key is empty it will create a new one.
     *
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     * @throws IllegalAccessException if the communication was OK but the registration failed anyway.
     * @throws IllegalStateException if the configuration cannot be changed e.g. read only.
     */
    private void registerApplicationKey()
            throws IllegalAccessException, ApiException, AssetNotLoadedException, IllegalStateException {
        logger.debug("registerApplicationKey() called");
        Clip2BridgeConfig config = getConfigAs(Clip2BridgeConfig.class);
        String newApplicationKey = getClip2Bridge().registerApplicationKey(config.applicationKey);
        Configuration configuration = editConfiguration();
        configuration.put(Clip2BridgeConfig.APPLICATION_KEY, newApplicationKey);
        updateConfiguration(configuration);
    }

    /**
     * Get the data for all devices in the bridge, and inform all child thing handlers.
     *
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     */
    private void updateDevices() throws ApiException, AssetNotLoadedException {
        logger.debug("updateDevices() called");
        getClip2Bridge().getResources(DEVICE).getResources().forEach(resource -> onResource(resource));
    }

    /**
     * Update the bridge thing properties.
     *
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     */
    private void updateProperties() throws ApiException, AssetNotLoadedException {
        logger.debug("updateProperties() called");

        for (Resource device : getClip2Bridge().getResources(DEVICE).getResources()) {
            MetaData metaData = device.getMetaData();

            if (Objects.nonNull(metaData) && metaData.getArchetype() == Archetype.BRIDGE_V2) {
                Map<String, @Nullable String> properties = new HashMap<>(thing.getProperties());

                // set resource properties
                properties.put(HueBindingConstants.PROPERTY_RESOURCE_ID, device.getId());
                properties.put(HueBindingConstants.PROPERTY_RESOURCE_TYPE, device.getType().toString());

                // set metadata properties
                properties.put(HueBindingConstants.PROPERTY_RESOURCE_NAME, metaData.getName());
                properties.put(HueBindingConstants.PROPERTY_RESOURCE_ARCHETYPE, metaData.getArchetype().toString());

                // set product data properties
                ProductData productData = device.getProductData();
                if (Objects.nonNull(productData)) {
                    // set generic thing properties
                    properties.put(Thing.PROPERTY_MODEL_ID, productData.getModelId());
                    properties.put(Thing.PROPERTY_VENDOR, productData.getManufacturerName());
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, productData.getSoftwareVersion().toString());
                    String hardwarePlatformType = productData.getHardwarePlatformType();
                    if (Objects.nonNull(hardwarePlatformType)) {
                        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwarePlatformType);
                    }

                    // set hue specific properties
                    properties.put(HueBindingConstants.PROPERTY_PRODUCT_NAME, productData.getProductName());
                    properties.put(HueBindingConstants.PROPERTY_PRODUCT_ARCHETYPE,
                            productData.getProductArchetype().toString());
                    properties.put(HueBindingConstants.PROPERTY_PRODUCT_CERTIFIED,
                            productData.getCertified().toString());
                }

                thing.setProperties(properties);
                break;
            }
        }
    }

    /**
     * Update the thing's own state. Called sporadically in case any SSE events may have been lost.
     */
    private void updateSelf() {
        logger.debug("updateSelf() called");
        try {
            checkAssetsLoaded();
            updateProperties();
            updateStatus(ThingStatus.UNKNOWN);
            getClip2Bridge().open();
        } catch (ApiException e) {
            logger.debug("updateSelf() {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error");
        } catch (AssetNotLoadedException e) {
            logger.debug("updateSelf() {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.clip2.conf-error-assets-not-loaded");
        } catch (IllegalAccessException e) {
            logger.debug("updateSelf() {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.clip2.conf-error-access_denied");
        }
    }
}
