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
package org.openhab.binding.salus.internal.handler;

import static java.util.Collections.emptySortedSet;
import static java.util.Objects.*;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.SalusBindingConstants;
import org.openhab.binding.salus.internal.rest.ApiResponse;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.Error;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.JettyHttpClient;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.binding.salus.internal.rest.RetryHttpClient;
import org.openhab.binding.salus.internal.rest.SalusApi;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public final class CloudBridgeHandler extends BaseBridgeHandler implements CloudApi {
    private Logger logger = LoggerFactory.getLogger(CloudBridgeHandler.class.getName());
    private final HttpClientFactory httpClientFactory;
    @NonNullByDefault({})
    private LoadingCache<String, SortedSet<DeviceProperty<?>>> devicePropertiesCache;
    @Nullable
    private SalusApi salusApi;
    @Nullable
    private ScheduledFuture<?> scheduledFuture;

    public CloudBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void initialize() {
        CloudBridgeConfig config = this.getConfigAs(CloudBridgeConfig.class);
        if (!config.isValid()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/cloud-bridge-handler.initialize.username-pass-not-valid");
            return;
        }
        RestClient httpClient = new JettyHttpClient(httpClientFactory.getCommonHttpClient());
        if (config.getMaxHttpRetries() > 0) {
            httpClient = new RetryHttpClient(httpClient, config.getMaxHttpRetries());
        }
        @Nullable
        SalusApi localSalusApi = salusApi = new SalusApi(config.getUsername(), config.getPassword().toCharArray(),
                config.getUrl(), httpClient, GsonMapper.INSTANCE);
        logger = LoggerFactory
                .getLogger(CloudBridgeHandler.class.getName() + "[" + config.getUsername().replace(".", "_") + "]");

        ScheduledExecutorService scheduledPool = ThreadPoolManager.getScheduledPool(SalusBindingConstants.BINDING_ID);
        scheduledPool.schedule(() -> tryConnectToCloud(localSalusApi), 1, MICROSECONDS);

        this.devicePropertiesCache = Caffeine.newBuilder().maximumSize(10_000)
                .expireAfterWrite(Duration.ofSeconds(config.getPropertiesRefreshInterval()))
                .refreshAfterWrite(Duration.ofSeconds(config.getPropertiesRefreshInterval()))
                .build(this::loadPropertiesForDevice);
        this.scheduledFuture = scheduledPool.scheduleWithFixedDelay(this::refreshCloudDevices,
                config.getRefreshInterval() * 2, config.getRefreshInterval(), SECONDS);

        // Do NOT set state to online to prevent it to flip from online to offline
        // check *tryConnectToCloud(SalusApi)*
    }

    private void tryConnectToCloud(SalusApi localSalusApi) {
        try {
            ApiResponse<SortedSet<Device>> response = localSalusApi.findDevices();
            if (response.failed()) {
                @Nullable
                Error error = response.error();
                if (error == null) {
                    error = new Error(500, "Unknown error");
                }
                updateStatus(OFFLINE, COMMUNICATION_ERROR, error.code() + ": " + error.message());
            } else {
                // there is a connection with the cloud
                updateStatus(ONLINE);
            }
        } catch (Exception ex) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR,
                    "@text/cloud-bridge-handler.initialize.cannot-connect-to-cloud [\"" + ex.getMessage() + "\"]");
        }
    }

    private void refreshCloudDevices() {
        logger.debug("Refreshing devices from CloudBridgeHandler");

        try {
            if (!(thing instanceof Bridge bridge)) {
                logger.debug("No bridge, refresh cancelled");
                return;
            }
            List<Thing> things = bridge.getThings();
            for (Thing thing : things) {
                try {
                    if (!thing.isEnabled()) {
                        logger.debug("Thing {} is disabled, refresh cancelled", thing.getUID());
                        continue;
                    }

                    @Nullable
                    ThingHandler handler = thing.getHandler();
                    if (handler == null) {
                        logger.debug("No handler for thing {} refresh cancelled", thing.getUID());
                        continue;
                    }
                    thing.getChannels().forEach(channel -> handler.handleCommand(channel.getUID(), REFRESH));
                } catch (Exception ex) {
                    logger.warn("Cannot refresh thing {} from CloudBridgeHandler", thing.getUID(), ex);
                }
            }

            // all things were updated,
            updateStatus(ONLINE);
        } catch (Exception ex) {
            logger.warn("Cannot refresh devices from CloudBridgeHandler", ex);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands in this bridge
        logger.debug("Bridge does not support any commands to any channels. channelUID={}, command={}", channelUID,
                command);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localScheduledFuture = scheduledFuture;
        if (localScheduledFuture != null) {
            localScheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        super.dispose();
    }

    @Override
    public SortedSet<@NonNull DeviceProperty<?>> findPropertiesForDevice(String dsn) {
        return requireNonNullElse(devicePropertiesCache.get(dsn), emptySortedSet());
    }

    @Nullable
    private SortedSet<DeviceProperty<?>> loadPropertiesForDevice(String dsn)
            throws ExecutionException, InterruptedException, TimeoutException {
        @Nullable
        SalusApi api = salusApi;
        if (api == null) {
            logger.debug("Cannot find properties for device {} because salusClient is null", dsn);
            return null;
        }
        logger.debug("Finding properties for device {} using salusClient", dsn);
        ApiResponse<SortedSet<DeviceProperty<?>>> response = api.findDeviceProperties(dsn);
        if (response.failed()) {
            var error = response.error();
            if (error == null) {
                error = new Error(500, "Unknown error");
            }
            throw new RuntimeException("Cannot find properties for device " + dsn + " using salusClient\n"
                    + error.code() + ": " + error.message());
        }
        return response.body();
    }

    @Override
    public boolean setValueForProperty(String dsn, String propertyName, Object value) {
        try {
            @Nullable
            SalusApi api = requireNonNull(salusApi);
            logger.debug("Setting property {} on device {} to value {} using salusClient", propertyName, dsn, value);
            ApiResponse<Object> response = api.setValueForProperty(dsn, propertyName, value);
            if (response.failed()) {
                logger.debug("Cannot set property {} on device {} to value {} using salusClient\n{}", propertyName, dsn,
                        value, response.error());
                devicePropertiesCache.invalidate(dsn);
                return false;
            }
            @Nullable
            Object setValue = response.body();
            if (setValue != null && (!(setValue instanceof Boolean) && !(setValue instanceof String)
                    && !(setValue instanceof Number))) {
                logger.warn(
                        "Cannot set value {} ({}) for property {} on device {} because it is not a Boolean, String, Long or Integer",
                        setValue, setValue.getClass().getSimpleName(), propertyName, dsn);
                return false;
            }
            Optional<DeviceProperty<?>> property = devicePropertiesCache.get(dsn).stream()
                    .filter(prop -> prop.getName().equals(propertyName)).findFirst();
            if (property.isEmpty()) {
                String simpleName = setValue != null ? setValue.getClass().getSimpleName() : "<null>";
                logger.warn(
                        "Cannot set value {} ({}) for property {} on device {} because it is not found in the cache. Invalidating cache",
                        setValue, simpleName, propertyName, dsn);
                devicePropertiesCache.invalidate(dsn);
                return false;
            }
            DeviceProperty<?> prop = property.get();
            if (setValue == null) {
                prop.setValue(null);
                return true;
            }
            if (setValue instanceof Boolean b && prop instanceof DeviceProperty.BooleanDeviceProperty boolProp) {
                boolProp.setValue(b);
                return true;
            }
            if (setValue instanceof String s && prop instanceof DeviceProperty.StringDeviceProperty stringProp) {
                stringProp.setValue(s);
                return true;
            }
            if (setValue instanceof Number l && prop instanceof DeviceProperty.LongDeviceProperty longProp) {
                longProp.setValue(l.longValue());
                return true;
            }

            logger.warn(
                    "Cannot set value {} ({}) for property {} ({}) on device {} because value class does not match property class",
                    setValue, setValue.getClass().getSimpleName(), propertyName, prop.getClass().getSimpleName(), dsn);
            return false;
        } catch (Exception ex) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR,
                    "@text/cloud-bridge-handler.errors.http [\"" + ex.getLocalizedMessage() + "\"]");
            devicePropertiesCache.invalidateAll();
            return false;
        }
    }

    @Override
    public SortedSet<Device> findDevices() {
        try {
            @Nullable
            SalusApi api = this.salusApi;
            if (api == null) {
                logger.debug("Cannot find devices because salusClient is null");
                return emptySortedSet();
            }
            logger.debug("Finding devices using salusClient");
            ApiResponse<SortedSet<Device>> response = api.findDevices();
            if (response.failed()) {
                logger.warn("Cannot find devices using salusClient\n{}", response.error());
                return emptySortedSet();
            }

            @Nullable
            SortedSet<Device> body = response.body();
            if (body == null) {
                return emptySortedSet();
            }

            return body;
        } catch (Exception ex) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR,
                    "@text/cloud-bridge-handler.errors.http [\"" + ex.getLocalizedMessage() + "\"]");
            // have to return something because we are not rethrowing exception
            return emptySortedSet();
        }
    }

    @Override
    public Optional<Device> findDevice(String dsn) {
        return findDevices().stream().filter(device -> device.dsn().equals(dsn)).findFirst();
    }
}
