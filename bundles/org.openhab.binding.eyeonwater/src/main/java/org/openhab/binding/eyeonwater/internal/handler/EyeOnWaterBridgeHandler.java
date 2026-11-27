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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;
import org.openhab.binding.eyeonwater.internal.config.EyeOnWaterBridgeConfiguration;
import org.openhab.binding.eyeonwater.internal.discovery.EyeOnWaterDiscoveryService;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.util.ThingWebClientUtil;
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
    private final Map<EyeOnWaterMeterHandler, Long> registeredMeters = new ConcurrentHashMap<>();
    private final AtomicLong registrationSequence = new AtomicLong();

    private volatile @Nullable EyeOnWaterClient client;

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable EyeOnWaterDiscoveryService discoveryService;
    private @Nullable ServiceRegistration<?> discoveryServiceReg;

    private final @Nullable HttpClientFactory httpClientFactory;

    private @Nullable HttpClient httpClient;

    public EyeOnWaterBridgeHandler(Bridge bridge, BundleContext bundleContext,
            @Nullable HttpClientFactory httpClientFactory) {
        super(bridge);
        this.bundleContext = bundleContext;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing EyeOnWater Bridge: {}", getThing().getUID());

        HttpClient oldClient = httpClient;
        if (oldClient != null) {
            try {
                oldClient.stop();
            } catch (Exception e) {
                logger.debug("Failed to stop existing EyeOnWater HTTP client: {}", e.getMessage());
            }
            this.httpClient = null;
        }

        HttpClientFactory factory = httpClientFactory;
        if (factory == null) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.unexpected-error");
            return;
        }

        // A dedicated HttpClient per bridge instance is required for session cookie isolation
        // so multiple EyeOnWater account bridges do not overwrite each other's cookies in the shared cookie store.
        String clientName = ThingWebClientUtil.buildWebClientConsumerName(getThing().getUID(), null);
        HttpClient clientInstance = factory.createHttpClient(clientName);
        try {
            clientInstance.start();
            this.httpClient = clientInstance;
        } catch (Exception e) {
            logger.error("Failed to start EyeOnWater HTTP client: {}", e.getMessage(), e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.unexpected-error");
            return;
        }

        EyeOnWaterBridgeConfiguration config = getConfigAs(EyeOnWaterBridgeConfiguration.class);

        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.bridge-config-missing");
            return;
        }

        EyeOnWaterClient activeClient = new EyeOnWaterClient(config.getNormalizedHostname(), config.username,
                config.password, clientInstance);
        client = activeClient;

        initializeAsync();

        // Register the Discovery Service dynamically for this bridge instance
        EyeOnWaterDiscoveryService service = new EyeOnWaterDiscoveryService(this);
        this.discoveryService = service;
        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), service,
                new Hashtable<>());
    }

    private synchronized void initializeAsync() {
        cancelReconnect();

        final EyeOnWaterClient activeClient = client;
        if (activeClient == null) {
            return;
        }

        // Perform async login verification
        pollingScheduler.execute(() -> {
            try {
                activeClient.authenticate();
                synchronized (EyeOnWaterBridgeHandler.this) {
                    if (activeClient.equals(client)) {
                        updateStatus(ThingStatus.ONLINE);
                        startPolling();

                        // Automatically trigger an initial scan when the bridge becomes ONLINE
                        EyeOnWaterDiscoveryService discovery = discoveryService;
                        if (discovery != null) {
                            pollingScheduler.execute(() -> discovery.startScan());
                        }
                    }
                }
            } catch (EyeOnWaterClient.EyeOnWaterAuthenticationException e) {
                synchronized (EyeOnWaterBridgeHandler.this) {
                    if (activeClient.equals(client)) {
                        logger.debug("Authentication rejected for EyeOnWater API: {}", e.getMessage(), e);
                        updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.bridge-config-rejected");
                    }
                }
            } catch (IOException e) {
                synchronized (EyeOnWaterBridgeHandler.this) {
                    if (activeClient.equals(client)) {
                        String msg = e.getMessage();
                        logger.debug("Communication error connecting to EyeOnWater API during initialization: {}", msg,
                                e);
                        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                                msg != null ? msg : "@text/offline.communication-error");
                        scheduleReconnect();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                synchronized (EyeOnWaterBridgeHandler.this) {
                    if (activeClient.equals(client)) {
                        logger.debug("Interrupted during EyeOnWater API initialization", e);
                        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.initialization-interrupted");
                    }
                }
            } catch (RuntimeException e) {
                synchronized (EyeOnWaterBridgeHandler.this) {
                    if (activeClient.equals(client)) {
                        logger.error("Unexpected error during EyeOnWater API initialization", e);
                        String msg = e.getMessage();
                        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                                msg != null ? msg : "@text/offline.unexpected-error");
                    }
                }
            }
        });
    }

    private synchronized void scheduleReconnect() {
        cancelReconnect();
        if (client != null) {
            logger.debug("Scheduling EyeOnWater API reconnection attempt in 1 minute...");
            reconnectJob = pollingScheduler.schedule(this::initializeAsync, 1, TimeUnit.MINUTES);
        }
    }

    private synchronized void cancelReconnect() {
        ScheduledFuture<?> job = reconnectJob;
        if (job != null) {
            job.cancel(true);
            reconnectJob = null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing EyeOnWater Bridge: {}", getThing().getUID());

        synchronized (this) {
            stopPolling();
            cancelReconnect();
            client = null;
        }

        ServiceRegistration<?> reg = discoveryServiceReg;
        if (reg != null) {
            reg.unregister();
            discoveryServiceReg = null;
        }
        this.discoveryService = null;

        HttpClient clientInstance = httpClient;
        if (clientInstance != null) {
            try {
                clientInstance.stop();
            } catch (Exception e) {
                logger.debug("Failed to stop EyeOnWater HTTP client: {}", e.getMessage());
            }
            this.httpClient = null;
        }

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

    void setClient(@Nullable EyeOnWaterClient client) {
        this.client = client;
    }

    void pollAllMeters() {
        logger.debug("Executing EyeOnWater polling cycle...");
        EyeOnWaterClient activeClient = client;
        if (activeClient == null) {
            return;
        }

        for (Map.Entry<EyeOnWaterMeterHandler, Long> entry : registeredMeters.entrySet()) {
            EyeOnWaterMeterHandler meterHandler = entry.getKey();
            Long regId = entry.getValue();
            String meterUuid = meterHandler.getMeterUuid();
            String meterId = meterHandler.getMeterId();
            try {
                logger.debug("Polling meter: {}", meterId);
                EyeOnWaterMeterData data = activeClient.pollMeter(meterUuid, meterId);
                synchronized (this) {
                    if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                        meterHandler.updateState(data);
                    }
                }
            } catch (IOException e) {
                synchronized (this) {
                    if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                        logger.debug("Communication error polling EyeOnWater meter {}: {}", meterId, e.getMessage(), e);
                        String msg = e.getMessage();
                        meterHandler.updateStatusOffline(msg != null ? msg : "@text/offline.communication-error");
                    }
                }
            } catch (InterruptedException e) {
                logger.debug("Interrupted while polling EyeOnWater meter {}", meterId, e);
                Thread.currentThread().interrupt();
                synchronized (this) {
                    if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                        meterHandler.updateStatusOffline("@text/offline.interrupted-polling");
                    }
                }
                break;
            } catch (RuntimeException e) {
                synchronized (this) {
                    if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                        logger.error("Unexpected error polling EyeOnWater meter {}", meterId, e);
                        String msg = e.getMessage();
                        meterHandler.updateStatusOffline(msg != null ? msg : "@text/offline.unexpected-error");
                    }
                }
            }
        }
    }

    private boolean isMeterStillValid(EyeOnWaterClient activeClient, EyeOnWaterMeterHandler meterHandler,
            @Nullable Long regId, String expectedUuid, String expectedId) {
        Long currentRegId = registeredMeters.get(meterHandler);
        if (!activeClient.equals(client) || currentRegId == null || !currentRegId.equals(regId)) {
            return false;
        }
        return expectedUuid.equals(meterHandler.getMeterUuid()) && expectedId.equals(meterHandler.getMeterId());
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
        long regId = registrationSequence.incrementAndGet();
        registeredMeters.put(meterHandler, regId);
        String meterUuid = meterHandler.getMeterUuid();
        String meterId = meterHandler.getMeterId();
        // Trigger an immediate poll only if the bridge is already ONLINE (i.e. added manually after startup)
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            synchronized (this) {
                EyeOnWaterClient activeClient = client;
                if (activeClient != null) {
                    pollingScheduler.execute(() -> {
                        try {
                            EyeOnWaterMeterData data = activeClient.pollMeter(meterUuid, meterId);
                            synchronized (EyeOnWaterBridgeHandler.this) {
                                if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                                    meterHandler.updateState(data);
                                }
                            }
                        } catch (IOException e) {
                            synchronized (EyeOnWaterBridgeHandler.this) {
                                if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                                    logger.debug("Failed to perform initial poll for meter {}: {}", meterId,
                                            e.getMessage(), e);
                                    String msg = e.getMessage();
                                    meterHandler.updateStatusOffline(
                                            msg != null ? msg : "@text/offline.failed-initial-poll");
                                }
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            synchronized (EyeOnWaterBridgeHandler.this) {
                                if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                                    logger.debug("Interrupted during initial poll for meter {}", meterId, e);
                                    meterHandler.updateStatusOffline("@text/offline.poll-interrupted");
                                }
                            }
                        } catch (RuntimeException e) {
                            synchronized (EyeOnWaterBridgeHandler.this) {
                                if (isMeterStillValid(activeClient, meterHandler, regId, meterUuid, meterId)) {
                                    logger.error("Unexpected error during initial poll for meter {}", meterId, e);
                                    String msg = e.getMessage();
                                    meterHandler
                                            .updateStatusOffline(msg != null ? msg : "@text/offline.unexpected-error");
                                }
                            }
                        }
                    });
                }
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
