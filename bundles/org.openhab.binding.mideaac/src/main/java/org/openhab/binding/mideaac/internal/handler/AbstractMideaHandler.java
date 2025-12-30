/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.handler;

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mideaac.internal.MideaACConfiguration;
import org.openhab.binding.mideaac.internal.cloud.Cloud;
import org.openhab.binding.mideaac.internal.cloud.CloudProvider;
import org.openhab.binding.mideaac.internal.connection.ConnectionManager;
import org.openhab.binding.mideaac.internal.discovery.DiscoveryHandler;
import org.openhab.binding.mideaac.internal.discovery.MideaACDiscoveryService;
import org.openhab.binding.mideaac.internal.security.TokenKey;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractMideaHandler} is responsible for initializing the devices
 * and passing commands to specific device handlers.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMideaHandler extends BaseThingHandler implements DiscoveryHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractMideaHandler.class);
    private final HttpClient httpClient;

    protected MideaACConfiguration config = new MideaACConfiguration();
    protected Map<String, String> properties = new HashMap<>();
    // Default parameters are the same as in the MideaACConfiguration class
    protected ConnectionManager connectionManager = new ConnectionManager("", 6444, 4, "", "", "", "", "", "", 0, false,
            "");
    protected ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private @Nullable ScheduledFuture<?> scheduledTask;
    private @Nullable ScheduledFuture<?> scheduledKeyTokenUpdate;

    public AbstractMideaHandler(Thing thing, UnitProvider unitProvider, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    /**
     * Initializes the handler by performing the following steps:
     * <ol>
     * <li>Retrieves the configuration for the handler.</li>
     * <li>Ensures the discovery or configuration is valid. If not, starts the discovery process and exits early.</li>
     * <li>Ensures the token and key for V3 devices are available. If not, starts the retrieval process and exits
     * early.</li>
     * <li>Updates the thing's status to {@link ThingStatus#UNKNOWN}.</li>
     * <li>Initializes the connection manager using the configuration.</li>
     * <li>Requests device capabilities if they are missing.</li>
     * <li>Starts any necessary schedulers for the handler.</li>
     * </ol>
     */
    @Override
    public void initialize() {
        config = getConfigAs(MideaACConfiguration.class);
        properties = editProperties();

        // 1) Ensure discovery/config is valid or start discovery and exit early
        if (!ensureConfigOrStartDiscovery()) {
            return;
        }

        // 2) Ensure token/key for V3 devices or start retrieval and exit early
        if (!ensureTokenKeyOrStartRetrieval()) {
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        initConnectionManagerFromConfig();

        requestCapabilitiesIfMissing(); // abstract hook, uses protected properties and config

        startSchedulers();
    }

    /**
     * Ensure we have all configuration needed to reach the device. If incomplete
     * but discoverable,
     * trigger discovery asynchronously and return false to stop current
     * initialization.
     */
    private boolean ensureConfigOrStartDiscovery() {
        if (config.isValid()) {
            logger.debug("Discovery parameters are valid for {}", thing.getUID());
            return true;
        }

        if (!config.isDiscoveryPossible()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration_error_invalid_discovery");
            return false;
        }

        MideaACDiscoveryService discoveryService = new MideaACDiscoveryService();

        // Kick off discovery asynchronously and end this initialization thread.

        scheduler.execute(() -> {
            try {
                // Keep thing OFFLINE with message about attempting discovery.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration_pending_discovery");
                discoveryService.discoverThing(config.ipAddress, this);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication_error_discovery");
            }
        });

        return false;
    }

    /**
     * Ensure token/key are available for V3 devices. If retrievable from cloud,
     * trigger async
     * retrieval and return false to stop current initialization.
     */
    private boolean ensureTokenKeyOrStartRetrieval() {
        if (config.version != 3 || config.isV3ConfigValid()) {
            logger.debug("Valid token and key for V.3 device {}", thing.getUID());
            return true;
        }

        if (!config.isTokenKeyObtainable()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration_error_invalid_token");
            return false;
        }

        scheduler.execute(() -> {
            try {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration_pending_token");
                CloudProvider cloudProvider = CloudProvider.getCloudProvider(config.cloud);
                getTokenKeyCloud(cloudProvider);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication_error_token");
            }
        });

        return false;
    }

    /** Initialize the connection manager from the current configuration. */
    private void initConnectionManagerFromConfig() {
        connectionManager = new org.openhab.binding.mideaac.internal.connection.ConnectionManager(config.ipAddress,
                config.ipPort, config.timeout, config.key, config.token, config.cloud, config.email, config.password,
                config.deviceId, config.version, config.promptTone, config.deviceType);
    }

    /** Subclasses decide what to do */
    protected abstract void requestCapabilitiesIfMissing();

    /**
     * Start routine and token refresh schedulers as needed.
     * 
     */
    private void startSchedulers() {
        // Routine polling
        if (scheduledTask == null) {
            scheduledTask = scheduler.scheduleWithFixedDelay(this::pollJob, 2, config.pollingTime, TimeUnit.SECONDS);
            logger.debug("Scheduled task started, Poll Time {} seconds", config.pollingTime);
        } else {
            logger.debug("Scheduler already running");
        }

        // Token/key update
        if (config.keyTokenUpdate != 0 && scheduledKeyTokenUpdate == null) {
            scheduledKeyTokenUpdate = scheduler.scheduleWithFixedDelay(
                    () -> getTokenKeyCloud(CloudProvider.getCloudProvider(config.cloud)), config.keyTokenUpdate,
                    config.keyTokenUpdate, TimeUnit.HOURS);
            logger.debug("Token Key Update Scheduler started, update interval {} hours", config.keyTokenUpdate);
        } else {
            logger.debug("Token Key Scheduler already running or disabled");
        }
    }

    private void pollJob() {
        try {
            refreshDeviceState(); // Delegate to subclass
            // If we reach here, the device is online.
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // delegate refresh to subclass
            refreshDeviceStateAll();
            return;
        }
        try {
            handleDeviceCommand(channelUID, command);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /** Subclasses implement device command handling */
    protected abstract void handleDeviceCommand(ChannelUID channelUID, Command command);

    /** Subclasses implement device state refresh */
    protected abstract void refreshDeviceState();

    /** Subclasses implement full device state refresh for RefreshType */
    protected abstract void refreshDeviceStateAll();

    /** Utility for updating a single channel state */
    protected void updateChannel(String channelId, State state) {
        updateState(new ChannelUID(getThing().getUID(), channelId), state);
    }

    @Override
    public void discovered(DiscoveryResult discoveryResult) {
        logger.debug("Discovered {}", thing.getUID());
        Map<String, Object> discoveryProps = discoveryResult.getProperties();
        Configuration configuration = editConfiguration();

        Object propertyDeviceId = Objects.requireNonNull(discoveryProps.get(CONFIG_DEVICEID));
        configuration.put(CONFIG_DEVICEID, propertyDeviceId.toString());

        Object propertyIpPort = Objects.requireNonNull(discoveryProps.get(CONFIG_IP_PORT));
        configuration.put(CONFIG_IP_PORT, propertyIpPort.toString());

        Object propertyVersion = Objects.requireNonNull(discoveryProps.get(CONFIG_VERSION));
        BigDecimal bigDecimalVersion = new BigDecimal((String) propertyVersion);
        logger.trace("Property Version in Handler {}", bigDecimalVersion.intValue());
        configuration.put(CONFIG_VERSION, bigDecimalVersion.intValue());

        updateConfiguration(configuration);

        properties = editProperties();

        Object propertySN = Objects.requireNonNull(discoveryProps.get(PROPERTY_SN));
        properties.put(PROPERTY_SN, propertySN.toString());

        Object propertySSID = Objects.requireNonNull(discoveryProps.get(PROPERTY_SSID));
        properties.put(PROPERTY_SSID, propertySSID.toString());

        Object propertyType = Objects.requireNonNull(discoveryProps.get(CONFIG_DEVICE_TYPE));
        properties.put(CONFIG_DEVICE_TYPE, propertyType.toString());

        updateProperties(properties);
        initialize();
    }

    /**
     * Gets the token and key from the Cloud
     * 
     * @param cloudProvider Cloud Provider account
     */
    public void getTokenKeyCloud(CloudProvider cloudProvider) {
        if (scheduledTask != null) {
            stopScheduler();
        }
        logger.debug("Retrieving Token and/or Key from cloud");
        Cloud cloud = new Cloud(config.email, config.password, cloudProvider, httpClient);
        if (cloud.login()) {
            TokenKey tk = cloud.getToken(config.deviceId);
            Configuration configuration = editConfiguration();

            configuration.put(CONFIG_TOKEN, tk.token());
            configuration.put(CONFIG_KEY, tk.key());
            updateConfiguration(configuration);

            logger.trace("Token: {}", tk.token());
            logger.trace("Key: {}", tk.key());
            logger.debug("Token and Key obtained from cloud, saving, back to initialize");
            initialize();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration_error_invalid_token ");
        }
    }

    private void stopScheduler() {
        ScheduledFuture<?> localScheduledTask = this.scheduledTask;

        if (localScheduledTask != null && !localScheduledTask.isCancelled()) {
            localScheduledTask.cancel(true);
            logger.debug("Scheduled task cancelled.");
            scheduledTask = null;
        }
    }

    private void stopTokenKeyUpdate() {
        ScheduledFuture<?> localScheduledTask = this.scheduledKeyTokenUpdate;

        if (localScheduledTask != null && !localScheduledTask.isCancelled()) {
            localScheduledTask.cancel(true);
            logger.debug("Scheduled Key Token Update cancelled.");
            scheduledKeyTokenUpdate = null;
        }
    }

    @Override
    public void dispose() {
        stopScheduler();
        stopTokenKeyUpdate();
        connectionManager.dispose(true);
    }
}
