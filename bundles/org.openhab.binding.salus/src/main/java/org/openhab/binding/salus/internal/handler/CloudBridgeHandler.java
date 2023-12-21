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
package org.openhab.binding.salus.internal.handler;

import static java.util.Collections.emptySortedSet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.salus.internal.rest.*;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public final class CloudBridgeHandler extends BaseBridgeHandler implements CloudApi {
    private Logger logger = LoggerFactory.getLogger(CloudBridgeHandler.class.getName());
    private final HttpClientFactory httpClientFactory;
    private LoadingCache<String, SortedSet<DeviceProperty<?>>> devicePropertiesCache;
    private String username;
    private char[] password;
    private String url;
    private long refreshInterval;
    private long propertiesRefreshInterval;
    private SalusApi salusApi;
    private ScheduledFuture<?> scheduledFuture;

    public CloudBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = requireNonNull(httpClientFactory, "httpClientFactory");
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
            updateStatus(ONLINE);
        } catch (Exception ex) {
            logger.error("Cannot start server!", ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Cannot start server! " + ex.getMessage());
        }
    }

    private void internalInitialize() {
        loadConfigs();
        var missingUsername = StringUtils.isEmpty(username);
        var missingPassword = password == null || password.length == 0;
        var missingUrl = StringUtils.isEmpty(url);
        if (missingUsername || missingPassword || missingUrl) {
            var sb = new StringBuilder();
            sb.append("Missing configuration!\n");
            sb.append(missingUsername ? "❌" : "✅").append(" username\n");
            sb.append(missingPassword ? "❌" : "✅").append(" password\n");
            sb.append(missingUrl ? "❌" : "✅").append(" url\n");
            sb.append("Please check your configuration!\n");
            logger.error("{}", sb);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, sb.toString());
            return;
        }
        var httpClient = new JettyHttpClient(httpClientFactory.getCommonHttpClient());
        salusApi = new SalusApi(username, password, url, httpClient, GsonMapper.INSTANCE);
        logger = LoggerFactory.getLogger(CloudBridgeHandler.class.getName() + "[" + username.replace(".", "_") + "]");
        try {
            var devices = salusApi.findDevices();
        } catch (Exception ex) {
            var msg = "Cannot connect to Salus Cloud! Probably username/password mismatch!";
            logger.error(msg, ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, msg + " " + ex.getMessage());
            return;
        }
        this.devicePropertiesCache = Caffeine.newBuilder().maximumSize(10_000)
                .expireAfterWrite(Duration.ofSeconds(propertiesRefreshInterval))
                .refreshAfterWrite(Duration.ofSeconds(propertiesRefreshInterval)).build(this::loadPropertiesForDevice);
        var scheduledPool = ThreadPoolManager.getScheduledPool("Salus");
        this.scheduledFuture = scheduledPool.scheduleWithFixedDelay(this::refreshCloudDevices, refreshInterval * 2,
                refreshInterval, SECONDS);

        // done
        updateStatus(ONLINE);
    }

    private void loadConfigs() {
        var config = this.getConfig();
        username = (String) config.get("username");
        password = ((String) config.get("password")).toCharArray();
        url = (String) config.get("url");
        if (StringUtils.isEmpty(url)) {
            url = "https://eu.salusconnect.io";
        }
        refreshInterval = ((BigDecimal) config.get("refreshInterval")).longValue();
        if (refreshInterval <= 0) {
            this.refreshInterval = 30;
        }
        propertiesRefreshInterval = ((BigDecimal) config.get("propertiesRefreshInterval")).longValue();
        if (propertiesRefreshInterval <= 0) {
            this.propertiesRefreshInterval = 5;
        }
    }

    private void refreshCloudDevices() {
        logger.debug("Refreshing devices from CloudBridgeHandler");

        if (!(thing instanceof Bridge bridge)) {
            logger.debug("No bridge, refresh cancelled");
            return;
        }
        var things = bridge.getThings();
        for (var thing : things) {
            if (!thing.isEnabled()) {
                logger.debug("Thing {} is disabled, refresh cancelled", thing.getUID());
                continue;
            }
            var handler = thing.getHandler();
            if (handler == null) {
                logger.debug("No handler for thing {} refresh cancelled", thing.getUID());
                continue;
            }
            var channels = thing.getChannels();
            for (var channel : channels) {
                handler.handleCommand(channel.getUID(), REFRESH);
            }
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
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        super.dispose();
    }

    @Override
    public SortedSet<DeviceProperty<?>> findPropertiesForDevice(String dsn) {
        return requireNonNullElse(devicePropertiesCache.get(dsn), emptySortedSet());
    }

    private SortedSet<DeviceProperty<?>> loadPropertiesForDevice(String dsn) {
        if (salusApi == null) {
            logger.error("Cannot find properties for device {} because salusClient is null", dsn);
            return null;
        }
        logger.debug("Finding properties for device {} using salusClient", dsn);
        var response = salusApi.findDeviceProperties(dsn);
        if (response.failed()) {
            logger.error("Cannot find properties for device {} using salusClient\n{}", dsn, response.error());
            return null;
        }
        return response.body();
    }

    @Override
    public void setValueForProperty(String dsn, String propertyName, Object value) {
        if (salusApi == null) {
            logger.error("Cannot set value for property {} on device {} because salusClient is null", propertyName,
                    dsn);
            return;
        }
        logger.debug("Setting property {} on device {} to value {} using salusClient", propertyName, dsn, value);
        var response = salusApi.setValueForProperty(dsn, propertyName, value);
        if (response.failed()) {
            logger.error("Cannot set property {} on device {} to value {} using salusClient\n{}", propertyName, dsn,
                    value, response.error());
            return;
        }
        var setValue = response.body();
        if (!(setValue instanceof Boolean) && !(setValue instanceof String) && !(setValue instanceof Number)) {
            logger.warn(
                    "Cannot set value {} ({}) for property {} on device {} because it is not a Boolean, String, Long or Integer",
                    setValue, setValue.getClass().getSimpleName(), propertyName, dsn);
            return;
        }
        var property = devicePropertiesCache.get(dsn).stream().filter(prop -> prop.getName().equals(propertyName))
                .findFirst();
        if (property.isEmpty()) {
            logger.warn(
                    "Cannot set value {} ({}) for property {} on device {} because it is not found in the cache. Invalidating cache",
                    setValue, setValue.getClass().getSimpleName(), propertyName, dsn);
            devicePropertiesCache.invalidate(dsn);
            return;
        }
        var prop = property.get();
        if (setValue instanceof Boolean b && prop instanceof DeviceProperty.BooleanDeviceProperty boolProp) {
            boolProp.setValue(b);
        } else if (setValue instanceof String s && prop instanceof DeviceProperty.StringDeviceProperty stringProp) {
            stringProp.setValue(s);
        } else if (setValue instanceof Number l && prop instanceof DeviceProperty.LongDeviceProperty longProp) {
            longProp.setValue(l.longValue());
        } else {
            logger.warn(
                    "Cannot set value {} ({}) for property {} ({}) on device {} because value class does not match property class",
                    setValue, setValue.getClass().getSimpleName(), propertyName, prop.getClass().getSimpleName(), dsn);
        }
    }

    @Override
    public SortedSet<Device> findDevices() {
        var salusApi = this.salusApi;
        if (salusApi == null) {
            logger.error("Cannot find devices because salusClient is null");
            return emptySortedSet();
        }
        logger.debug("Finding devices using salusClient");
        var response = salusApi.findDevices();
        if (response.failed()) {
            logger.error("Cannot find devices using salusClient\n{}", response.error());
            return emptySortedSet();
        }
        return response.body();
    }

    @Override
    public Optional<Device> findDevice(String dsn) {
        return findDevices().stream().filter(device -> device.dsn().equals(dsn)).findFirst();
    }
}
