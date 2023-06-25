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
package org.openhab.binding.lgthinq.internal.handler;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQBridgeConfiguration;
import org.openhab.binding.lgthinq.internal.api.TokenManager;
import org.openhab.binding.lgthinq.internal.discovery.LGThinqDiscoveryService;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.internal.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgservices.LGThinQACApiV1ClientServiceImpl;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQBridgeHandler}
 *
 * @author Nemer Daud - Initial contribution
 */
public class LGThinQBridgeHandler extends ConfigStatusBridgeHandler implements LGThinQBridge {

    private Map<String, LGThinQAbstractDeviceHandler> lGDeviceRegister = new ConcurrentHashMap<>();
    private Map<String, LGDevice> lastDevicesDiscovered = new ConcurrentHashMap<>();

    static {
        var logger = LoggerFactory.getLogger(LGThinQBridgeHandler.class);
        try {
            File directory = new File(THINQ_USER_DATA_FOLDER);
            if (!directory.exists()) {
                directory.mkdir();
            }
        } catch (Exception e) {
            logger.warn("Unable to setup thinq userdata directory: {}", e.getMessage());
        }
    }
    private final Logger logger = LoggerFactory.getLogger(LGThinQBridgeHandler.class);
    private LGThinQBridgeConfiguration lgthinqConfig;
    private TokenManager tokenManager;
    private LGThinqDiscoveryService discoveryService;
    private LGThinQApiClientService lgApiClient;
    private @Nullable Future<?> initJob;
    private @Nullable ScheduledFuture<?> devicePollingJob;

    public LGThinQBridgeHandler(Bridge bridge) {
        super(bridge);
        tokenManager = TokenManager.getInstance();
        lgApiClient = LGThinQACApiV1ClientServiceImpl.getInstance();
        lgDevicePollingRunnable = new LGDevicePollingRunnable(bridge.getUID().getId());
    }

    final ReentrantLock pollingLock = new ReentrantLock();

    /**
     * Abstract Runnable Polling Class to schedule synchronization status of the Bridge Thing Kinds !
     */
    abstract class PollingRunnable implements Runnable {
        protected final String bridgeName;
        protected LGThinQBridgeConfiguration lgthinqConfig;

        PollingRunnable(String bridgeName) {
            this.bridgeName = bridgeName;
        }

        @Override
        public void run() {
            try {
                pollingLock.lock();
                // check if configuration file already exists
                if (tokenManager.isOauthTokenRegistered(bridgeName)) {
                    logger.debug(
                            "Token authentication process has been already done. Skip first authentication process.");
                    try {
                        tokenManager.getValidRegisteredToken(bridgeName);
                    } catch (IOException e) {
                        logger.error("Error reading LGThinq TokenFile", e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                "@text/error.toke-file-corrupted");
                        return;
                    } catch (RefreshTokenException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                "@text/error.toke-refresh");
                        return;
                    }
                } else {
                    try {
                        tokenManager.oauthFirstRegistration(bridgeName, lgthinqConfig.getLanguage(),
                                lgthinqConfig.getCountry(), lgthinqConfig.getUsername(), lgthinqConfig.getPassword(),
                                lgthinqConfig.getAlternativeServer());
                        tokenManager.getValidRegisteredToken(bridgeName);
                        logger.debug("Successful getting token from LG API");
                    } catch (IOException e) {
                        logger.debug(
                                "I/O error accessing json token configuration file. Updating Bridge Status to OFFLINE.",
                                e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "@text/error.toke-file-access-error");
                        return;
                    } catch (LGThinqException e) {
                        logger.debug("Error accessing LG API. Updating Bridge Status to OFFLINE.", e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/error.lgapi-communication-error");
                        return;
                    }
                }
                if (thing.getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }

                try {
                    doConnectedRun();
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/error.lgapi-getting-devices");
                }

            } finally {
                pollingLock.unlock();
            }
        }

        protected abstract void doConnectedRun() throws IOException, LGThinqException;
    }

    @Override
    public void registerDiscoveryListener(LGThinqDiscoveryService listener) {
        if (discoveryService == null) {
            discoveryService = listener;
        }
    }

    /**
     * Registry the OSGi services used by this Bridge.
     * Eventually, the Discovery Service will be activated with this bridge as argument.
     * 
     * @return Services to be registered to OSGi.
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LGThinqDiscoveryService.class);
    }

    @Override
    public void registryListenerThing(LGThinQAbstractDeviceHandler thing) {
        if (lGDeviceRegister.get(thing.getDeviceId()) == null) {
            lGDeviceRegister.put(thing.getDeviceId(), thing);
            // remove device from discovery list, if exists.
            LGDevice device = lastDevicesDiscovered.get(thing.getDeviceId());
            if (device != null) {
                discoveryService.removeLgDeviceDiscovery(device);
            }
        }
    }

    @Override
    public void unRegistryListenerThing(LGThinQAbstractDeviceHandler thing) {
        lGDeviceRegister.remove(thing.getDeviceId());
    }

    @Override
    public LGThinQAbstractDeviceHandler getThingByDeviceId(String deviceId) {
        return lGDeviceRegister.get(deviceId);
    }

    private LGDevicePollingRunnable lgDevicePollingRunnable;

    class LGDevicePollingRunnable extends PollingRunnable {
        public LGDevicePollingRunnable(String bridgeName) {
            super(bridgeName);
        }

        @Override
        protected void doConnectedRun() throws LGThinqException {
            Map<String, LGDevice> lastDevicesDiscoveredCopy = new HashMap<>(lastDevicesDiscovered);
            List<LGDevice> devices = lgApiClient.listAccountDevices(bridgeName);
            // if not registered yet, and not discovered before, then add to discovery list.
            devices.forEach(device -> {
                String deviceId = device.getDeviceId();
                if (lGDeviceRegister.get(deviceId) == null && !lastDevicesDiscovered.containsKey(deviceId)) {
                    logger.debug("Adding new LG Device to things registry with id:{}", deviceId);
                    if (discoveryService != null) {
                        discoveryService.addLgDeviceDiscovery(device);
                    }
                }
                lastDevicesDiscovered.put(deviceId, device);
                lastDevicesDiscoveredCopy.remove(deviceId);
            });
            // the rest in lastDevicesDiscoveredCopy is not more registered in LG API. Remove from discovery
            lastDevicesDiscoveredCopy.forEach((deviceId, device) -> {
                logger.trace("LG Device '{}' removed.", deviceId);
                lastDevicesDiscovered.remove(deviceId);

                LGThinQAbstractDeviceHandler deviceThing = lGDeviceRegister.get(deviceId);
                if (deviceThing != null) {
                    deviceThing.onDeviceRemoved();
                }
                if (discoveryService != null && deviceThing != null) {
                    discoveryService.removeLgDeviceDiscovery(device);
                }
            });
        }
    };

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        List<ConfigStatusMessage> resultList = new ArrayList<>();
        if (lgthinqConfig.username.isEmpty()) {
            resultList.add(ConfigStatusMessage.Builder.error("USERNAME").withMessageKeySuffix("missing field")
                    .withArguments("username").build());
        }
        if (lgthinqConfig.password.isEmpty()) {
            resultList.add(ConfigStatusMessage.Builder.error("PASSWORD").withMessageKeySuffix("missing field")
                    .withArguments("password").build());
        }
        if (lgthinqConfig.language.isEmpty()) {
            resultList.add(ConfigStatusMessage.Builder.error("LANGUAGE").withMessageKeySuffix("missing field")
                    .withArguments("language").build());
        }
        if (lgthinqConfig.country.isEmpty()) {
            resultList.add(ConfigStatusMessage.Builder.error("COUNTRY").withMessageKeySuffix("missing field")
                    .withArguments("country").build());

        }
        return resultList;
    }

    @Override
    public void handleRemoval() {
        if (devicePollingJob != null)
            devicePollingJob.cancel(true);
        tokenManager.cleanupTokenRegistry(getBridge().getUID().getId());
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        if (devicePollingJob != null) {
            devicePollingJob.cancel(true);
            devicePollingJob = null;
        }
    }

    @Override
    public <T> T getConfigAs(Class<T> configurationClass) {
        return super.getConfigAs(configurationClass);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing LGThinq bridge handler.");
        lgthinqConfig = getConfigAs(LGThinQBridgeConfiguration.class);
        lgDevicePollingRunnable.lgthinqConfig = lgthinqConfig;
        if (lgthinqConfig.username.isEmpty() || lgthinqConfig.password.isEmpty() || lgthinqConfig.language.isEmpty()
                || lgthinqConfig.country.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.mandotory-fields-missing");
        } else {
            // updateStatus(ThingStatus.UNKNOWN);
            startLGThinqDevicePolling();
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Bridge Configuration was updated. Cleaning the token registry file");
        File f = new File(String.format(THINQ_CONNECTION_DATA_FILE, getThing().getUID().getId()));
        if (f.isFile()) {
            // file exists. Delete it
            if (!f.delete()) {
                logger.error("Error deleting file:{}", f.getAbsolutePath());
            }
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    private void startLGThinqDevicePolling() {
        // stop current scheduler, if any
        if (devicePollingJob != null && !devicePollingJob.isDone()) {
            devicePollingJob.cancel(true);
        }
        long poolingInterval;
        int configPollingInterval = lgthinqConfig.getPoolingIntervalSec();
        // It's not recommended to polling for resources in LG API short intervals to do not enter in BlackList
        if (configPollingInterval < 300 && configPollingInterval != 0) {
            poolingInterval = TimeUnit.SECONDS.toSeconds(300);
            logger.info("Wrong configuration value for pooling interval. Using default value: {}s", poolingInterval);
        } else {
            if (configPollingInterval == 0) {
                logger.info("LG's discovery pooling disabled (configured as zero)");
                return;
            }
            poolingInterval = configPollingInterval;
        }
        // submit instantlly and schedule for the next polling interval.
        scheduler.submit(lgDevicePollingRunnable);
        devicePollingJob = scheduler.scheduleWithFixedDelay(lgDevicePollingRunnable, 2, poolingInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
