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

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.SalusApi;
import org.openhab.binding.salus.internal.SalusBindingConstants;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.HttpClient;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.binding.salus.internal.rest.RetryHttpClient;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
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
public abstract class AbstractBridgeHandler<ConfigT extends AbstractBridgeConfig> extends BaseBridgeHandler
        implements CloudApi {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpClientFactory httpClientFactory;
    private final Class<ConfigT> configClass;
    @NonNullByDefault({})
    private LoadingCache<String, SortedSet<DeviceProperty<?>>> devicePropertiesCache;
    @Nullable
    private SalusApi salusApi;
    @Nullable
    private ScheduledFuture<?> scheduledFuture;

    public AbstractBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory, Class<ConfigT> configClass) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.configClass = configClass;
    }

    @Override
    public void initialize() {
        var config = this.getConfigAs(configClass);
        if (!config.isValid()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/cloud-bridge-handler.initialize.username-pass-not-valid");
            return;
        }
        RestClient httpClient = new HttpClient(httpClientFactory.getCommonHttpClient());
        if (config.getMaxHttpRetries() > 0) {
            httpClient = new RetryHttpClient(httpClient, config.getMaxHttpRetries());
        }
        var localSalusApi = salusApi = newSalusApi(config, httpClient, GsonMapper.INSTANCE);
        logger = LoggerFactory
                .getLogger(this.getClass().getName() + "[" + config.getUsername().replace(".", "_") + "]");

        ScheduledExecutorService scheduledPool = ThreadPoolManager.getScheduledPool(SalusBindingConstants.BINDING_ID);
        scheduledPool.schedule(() -> tryConnectToCloud(localSalusApi), 1, MICROSECONDS);

        this.devicePropertiesCache = Caffeine.newBuilder().maximumSize(10_000)
                .expireAfterWrite(Duration.ofSeconds(config.getPropertiesRefreshInterval()))
                .refreshAfterWrite(Duration.ofSeconds(config.getPropertiesRefreshInterval()))
                .build(this::findPropertiesForDevice);
        this.scheduledFuture = scheduledPool.scheduleWithFixedDelay(this::refreshCloudDevices,
                config.getRefreshInterval() * 2, config.getRefreshInterval(), SECONDS);

        // Do NOT set state to online to prevent it to flip from online to offline
        // check *tryConnectToCloud(SalusApi)*
    }

    protected abstract SalusApi newSalusApi(ConfigT config, RestClient httpClient, GsonMapper gsonMapper);

    private void tryConnectToCloud(SalusApi localSalusApi) {
        try {
            localSalusApi.findDevices();
            // there is a connection with the cloud
            updateStatus(ONLINE);
        } catch (SalusApiException ex) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR,
                    "@text/cloud-bridge-handler.initialize.cannot-connect-to-cloud [\"" + ex.getMessage() + "\"]");
        } catch (AuthSalusApiException ex) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR,
                    "@text/cloud-bridge-handler.initialize.auth-exception [\"" + ex.getMessage() + "\"]");
        }
    }

    private void refreshCloudDevices() {
        logger.debug("Refreshing devices from CloudBridgeHandler");
        if (!(thing instanceof Bridge bridge)) {
            logger.debug("No bridge, refresh cancelled");
            return;
        }
        List<Thing> things = bridge.getThings();
        for (Thing thing : things) {
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
        }

        var local = salusApi;
        if (local != null) {
            tryConnectToCloud(local);
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
    public SortedSet<DeviceProperty<?>> findPropertiesForDevice(String dsn)
            throws SalusApiException, AuthSalusApiException {
        logger.debug("Finding properties for device {} using salusClient", dsn);
        return requireNonNull(salusApi).findDeviceProperties(dsn);
    }

    @Override
    public boolean setValueForProperty(String dsn, String propertyName, Object value)
            throws SalusApiException, AuthSalusApiException {
        try {
            @Nullable
            SalusApi api = requireNonNull(salusApi);
            logger.debug("Setting property {} on device {} to value {} using salusClient", propertyName, dsn, value);
            Object setValue = api.setValueForProperty(dsn, propertyName, value);
            if ((!(setValue instanceof Boolean) && !(setValue instanceof String) && !(setValue instanceof Number))) {
                logger.warn(
                        "Cannot set value {} ({}) for property {} on device {} because it is not a Boolean, String, Long or Integer",
                        setValue, setValue.getClass().getSimpleName(), propertyName, dsn);
                return false;
            }
            var properties = devicePropertiesCache.get(dsn);
            Optional<DeviceProperty<?>> property = requireNonNull(properties).stream()
                    .filter(prop -> prop.getName().equals(propertyName)).findFirst();
            if (property.isEmpty()) {
                String simpleName = setValue.getClass().getSimpleName();
                logger.warn(
                        "Cannot set value {} ({}) for property {} on device {} because it is not found in the cache. Invalidating cache",
                        setValue, simpleName, propertyName, dsn);
                devicePropertiesCache.invalidate(dsn);
                return false;
            }
            DeviceProperty<?> prop = property.get();
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
        } catch (AuthSalusApiException | SalusApiException ex) {
            devicePropertiesCache.invalidateAll();
            throw ex;
        }
    }

    @Override
    public SortedSet<Device> findDevices() throws SalusApiException, AuthSalusApiException {
        return requireNonNull(this.salusApi).findDevices();
    }

    @Override
    public Optional<Device> findDevice(String dsn) throws SalusApiException, AuthSalusApiException {
        return findDevices().stream().filter(device -> device.dsn().equals(dsn)).findFirst();
    }

    public abstract Set<String> it600RequiredChannels();

    public abstract String channelPrefix();
}
