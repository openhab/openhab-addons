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
package org.openhab.binding.eyeonwater.internal.handler;

import static org.openhab.binding.eyeonwater.internal.EyeOnWaterBindingConstants.BINDING_ID;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;
import org.openhab.binding.eyeonwater.internal.config.EyeOnWaterBridgeConfiguration;
import org.openhab.binding.eyeonwater.internal.discovery.EyeOnWaterDiscoveryService;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EyeOnWaterBridgeHandler} manages connections and authenticates with the EyeOnWater API.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
public class EyeOnWaterBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EyeOnWaterBridgeHandler.class);

    private final BundleContext bundleContext;
    private final ScheduledExecutorService pollingScheduler = ThreadPoolManager.getScheduledPool(BINDING_ID);
    private final Set<EyeOnWaterMeterHandler> registeredMeters = ConcurrentHashMap.newKeySet();

    private volatile @Nullable EyeOnWaterClient client;

    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable ServiceRegistration<?> discoveryServiceReg;

    public EyeOnWaterBridgeHandler(Bridge bridge, BundleContext bundleContext) {
        super(bridge);
        this.bundleContext = bundleContext;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing EyeOnWater Bridge: {}", getThing().getUID());

        EyeOnWaterBridgeConfiguration config = getConfigAs(EyeOnWaterBridgeConfiguration.class);

        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.bridge-config-missing");
            return;
        }

        EyeOnWaterClient activeClient = new EyeOnWaterClient(config.hostname, config.username, config.password);
        client = activeClient;

        // Perform async login verification
        pollingScheduler.execute(() -> {
            try {
                activeClient.authenticate();
                if (activeClient.equals(client)) {
                    updateStatus(ThingStatus.ONLINE);
                    startPolling();
                }
            } catch (IOException e) {
                if (activeClient.equals(client)) {
                    logger.debug("Communication error connecting to EyeOnWater API during initialization: {}",
                            e.getMessage(), e);
                    String msg = e.getMessage();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            msg != null ? msg : "@text/offline.communication-error");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (activeClient.equals(client)) {
                    logger.debug("Interrupted during EyeOnWater API initialization", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.initialization-interrupted");
                }
            } catch (Exception e) {
                if (activeClient.equals(client)) {
                    logger.error("Unexpected error during EyeOnWater API initialization", e);
                    String msg = e.getMessage();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            msg != null ? msg : "@text/offline.unexpected-error");
                }
            }
        });

        // Register the Discovery Service dynamically for this bridge instance
        EyeOnWaterDiscoveryService discoveryService = new EyeOnWaterDiscoveryService(this);
        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<>());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing EyeOnWater Bridge: {}", getThing().getUID());

        stopPolling();

        ServiceRegistration<?> reg = discoveryServiceReg;
        if (reg != null) {
            reg.unregister();
            discoveryServiceReg = null;
        }

        client = null;
        super.dispose();
    }

    private synchronized void startPolling() {
        stopPolling();

        EyeOnWaterBridgeConfiguration config = getConfigAs(EyeOnWaterBridgeConfiguration.class);
        int interval = Math.max(5, config.refreshInterval);

        logger.debug("Starting EyeOnWater polling job with interval of {} minutes", interval);
        pollingJob = pollingScheduler.scheduleWithFixedDelay(this::pollAllMeters, 0, interval, TimeUnit.MINUTES);
    }

    private synchronized void stopPolling() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    private void pollAllMeters() {
        logger.debug("Executing EyeOnWater polling cycle...");
        EyeOnWaterClient activeClient = client;
        if (activeClient == null) {
            return;
        }

        for (EyeOnWaterMeterHandler meterHandler : registeredMeters) {
            try {
                logger.debug("Polling meter: {}", meterHandler.getMeterId());
                EyeOnWaterMeterData data = activeClient.pollMeter(meterHandler.getMeterUuid(),
                        meterHandler.getMeterId());
                if (registeredMeters.contains(meterHandler)) {
                    meterHandler.updateState(data);
                }
            } catch (IOException e) {
                logger.debug("Communication error polling EyeOnWater meter {}: {}", meterHandler.getMeterId(),
                        e.getMessage(), e);
                String msg = e.getMessage();
                meterHandler.updateStatusOffline(msg != null ? msg : "@text/offline.communication-error");
            } catch (InterruptedException e) {
                logger.debug("Interrupted while polling EyeOnWater meter {}", meterHandler.getMeterId(), e);
                Thread.currentThread().interrupt();
                meterHandler.updateStatusOffline("@text/offline.interrupted-polling");
            } catch (Exception e) {
                logger.error("Unexpected error polling EyeOnWater meter {}", meterHandler.getMeterId(), e);
                String msg = e.getMessage();
                meterHandler.updateStatusOffline(msg != null ? msg : "@text/offline.unexpected-error");
            }
        }
    }

    /**
     * Retrieve discovered physical meters for scanning.
     */
    public List<EyeOnWaterMeterData> discoverMeters() throws IOException, InterruptedException, IllegalStateException {
        EyeOnWaterClient activeClient = client;
        if (activeClient == null) {
            throw new IllegalStateException("API client is not initialized.");
        }
        EyeOnWaterBridgeConfiguration config = getConfigAs(EyeOnWaterBridgeConfiguration.class);
        return activeClient.discoverMeters(config.preferNewSearch);
    }

    public void registerMeterHandler(EyeOnWaterMeterHandler meterHandler) {
        registeredMeters.add(meterHandler);
        // Trigger an immediate poll only if the bridge is already ONLINE (i.e. added manually after startup)
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            EyeOnWaterClient activeClient = client;
            if (activeClient != null) {
                pollingScheduler.execute(() -> {
                    try {
                        EyeOnWaterMeterData data = activeClient.pollMeter(meterHandler.getMeterUuid(),
                                meterHandler.getMeterId());
                        if (registeredMeters.contains(meterHandler)) {
                            meterHandler.updateState(data);
                        }
                    } catch (IOException e) {
                        if (registeredMeters.contains(meterHandler)) {
                            logger.debug("Failed to perform initial poll for meter {}: {}", meterHandler.getMeterId(),
                                    e.getMessage(), e);
                            String msg = e.getMessage();
                            meterHandler.updateStatusOffline(msg != null ? msg : "@text/offline.failed-initial-poll");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (registeredMeters.contains(meterHandler)) {
                            logger.debug("Interrupted during initial poll for meter {}", meterHandler.getMeterId(), e);
                            meterHandler.updateStatusOffline("@text/offline.poll-interrupted");
                        }
                    }
                });
            }
        }
    }

    public void unregisterMeterHandler(EyeOnWaterMeterHandler meterHandler) {
        registeredMeters.remove(meterHandler);
    }

    @Override
    public void handleCommand(org.openhab.core.thing.ChannelUID channelUID, org.openhab.core.types.Command command) {
        // Bridge does not handle commands directly
    }
}
