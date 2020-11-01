/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal.handler;

import static org.openhab.binding.vwweconnect.internal.VWWeConnectBindingConstants.CHANNEL_STATUS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.vwweconnect.internal.DeviceStatusListener;
import org.openhab.binding.vwweconnect.internal.VWWeConnectBridgeConfiguration;
import org.openhab.binding.vwweconnect.internal.VWWeConnectSession;
import org.openhab.binding.vwweconnect.internal.discovery.VWWeConnectDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VWWeConnectBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class VWWeConnectBridgeHandler extends BaseBridgeHandler {

    private static final int REFRESH_DELAY_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(VWWeConnectBridgeHandler.class);
    private final ReentrantLock refreshJobLock = new ReentrantLock();
    private final ReentrantLock immediateRefreshJobLock = new ReentrantLock();
    private final List<ScheduledFuture<?>> pendingActions = new Stack<>();

    private @Nullable String securePIN;
    private int refresh = 600;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> immediateRefreshJob;
    private @Nullable VWWeConnectSession session;
    private HttpClient httpClient;

    public VWWeConnectBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        session = new VWWeConnectSession(this.httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("VWWeConnectBridgeHandler Handle command {} on channelUID: {}", command, channelUID);
        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(CHANNEL_STATUS) && channelUID.getThingUID().equals(getThing().getUID())) {
                logger.debug("Refresh command on status channel {} will trigger a vehicle status request", channelUID);
                scheduler.execute(() -> {
                    if (session != null) {
                        session.requestVehicleStatus();
                    } else {
                        logger.debug("Failed to handle refresh on status channel since session is null!");
                    }
                });
            }
            logger.debug("Refresh command on channel {} will trigger refresh in {} seconds", channelUID,
                    REFRESH_DELAY_SECONDS);
            scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
        } else {
            logger.warn("unknown command! {}", command);
        }
    }

    @Override
    protected void updateThing(Thing thing) {
        super.updateThing(thing);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        logger.debug("Configuration updated {}", configuration);
        stopAutomaticRefresh();
        stopImmediateRefresh();
        super.updateConfiguration(configuration);
        initialize();
    }

    public @Nullable VWWeConnectSession getSession() {
        return session;
    }

    public void addPendingAction(ScheduledFuture<?> pendingJob) {
        pendingActions.add(pendingJob);
    }

    public void removeFinishedJobs() {
        pendingActions.removeIf(ScheduledFuture::isDone);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing VWWeConnect Bridge");
        VWWeConnectBridgeConfiguration config = getConfigAs(VWWeConnectBridgeConfiguration.class);
        this.securePIN = config.spin;
        this.refresh = config.refresh;
        if (config.username == null || config.password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration of username and password is mandatory");
        } else {
            updateStatus(ThingStatus.UNKNOWN);

            scheduler.execute(() -> {
                if (session == null) {
                    logger.debug("Session is null, config change probably, then let's create a new one");
                    session = new VWWeConnectSession(this.httpClient);
                }
                if (session != null) {
                    session.initialize(config.username, config.password, securePIN);
                }
            });

            startAutomaticRefresh();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler is disposed.");
        stopAutomaticRefresh();
        stopImmediateRefresh();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
        }
        if (session != null) {
            session = null;
        }
        pendingActions.stream().filter(f -> !f.isCancelled()).forEach(f -> f.cancel(true));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // Do not do a refresh since that will refresh all things as well
        logger.debug("Channel linked {}.", channelUID);
    }

    public boolean registerObjectStatusListener(DeviceStatusListener deviceStatusListener) {
        VWWeConnectSession localSession = session;
        if (localSession != null) {
            logger.debug("registerObjectStatusListener for listener {}", deviceStatusListener);
            return localSession.registerDeviceStatusListener(deviceStatusListener);
        }
        return false;
    }

    public boolean unregisterObjectStatusListener(DeviceStatusListener deviceStatusListener) {
        VWWeConnectSession localSession = session;
        if (localSession != null) {
            logger.debug("unregisterObjectStatusListener for listener {}", deviceStatusListener);
            return localSession.unregisterDeviceStatusListener(deviceStatusListener);
        }
        return false;
    }

    @Override
    public void handleRemoval() {
        logger.debug("handleRemoval");
        stopAutomaticRefresh();
        stopImmediateRefresh();
        if (session != null) {
            session = null;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(VWWeConnectDiscoveryService.class);
    }

    public @Nullable String getSecurePIN() {
        return securePIN;
    }

    private void refreshAndUpdateStatus() {
        logger.debug("VWWeConnectBridgeHandler - Refresh thread is up'n running! job: {}", refreshJob);
        try {
            if (session != null) {
                boolean success = session.refresh();
                if (success) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Refresh success!");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    logger.warn("Refresh failed!");
                    session = null;
                }
            } else {
                logger.debug("VWWeConnectBridgeHandler - Refresh thread session is null, let's re-initialize!");
                dispose();
                SslContextFactory sslFactory = new SslContextFactory(true);
                this.httpClient = new HttpClient(sslFactory);
                this.httpClient.setFollowRedirects(false);
                try {
                    this.httpClient.start();
                } catch (Exception e) {
                    logger.error("Exception: {}", e.getMessage());
                }
                initialize();
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    void scheduleImmediateRefresh(int refreshDelay) {
        logger.debug("VWWeConnectBridgeHandler - scheduleImmediateRefresh");
        immediateRefreshJobLock.lock();
        logger.trace("immediateRefreshJobLock is locked");
        try {
            ScheduledFuture<?> localRefreshJob = refreshJob;
            ScheduledFuture<?> localImmediateRefreshJob = immediateRefreshJob;
            if (localRefreshJob != null) {
                if (localImmediateRefreshJob != null) {
                    logger.debug("Current remaining delay {} for immediate refresh job {}",
                            localImmediateRefreshJob.getDelay(TimeUnit.SECONDS), localImmediateRefreshJob);
                }
                if (localRefreshJob.getDelay(TimeUnit.SECONDS) > refreshDelay) {
                    if (localImmediateRefreshJob == null || localImmediateRefreshJob.getDelay(TimeUnit.SECONDS) <= 0) {
                        logger.debug("Current remaining delay {} for refresh job {}",
                                localRefreshJob.getDelay(TimeUnit.SECONDS), localRefreshJob);
                        // Note we are using getDelay() instead of isDone() as we want to allow Things to schedule a
                        // refresh if their status is pending. As the status update happens inside the
                        // refreshAndUpdateStatus execution the isDone() will return false and would not
                        // allow the rescheduling of the task.
                        immediateRefreshJob = scheduler.schedule(this::refreshAndUpdateStatus, refreshDelay,
                                TimeUnit.SECONDS);
                        logger.debug("Scheduling new immediate refresh job {}", localImmediateRefreshJob);
                    }
                } else {
                    logger.debug("No immediate refresh scheduled since resfresh job will run in {} s.",
                            localRefreshJob.getDelay(TimeUnit.SECONDS));
                }
            }
        } catch (RejectedExecutionException e) {
            logger.warn("Immediate refresh job cannot be scheduled!");
        } finally {
            immediateRefreshJobLock.unlock();
            logger.trace("immediateRefreshJobLock is unlocked");
        }
    }

    private void stopImmediateRefresh() {
        immediateRefreshJobLock.lock();
        try {
            ScheduledFuture<?> localImmediateRefreshJob = immediateRefreshJob;
            logger.debug("Stop immediate refresh for job {}", localImmediateRefreshJob);
            if (localImmediateRefreshJob != null && !localImmediateRefreshJob.isCancelled()) {
                localImmediateRefreshJob.cancel(true);
                immediateRefreshJob = null;
            }
        } catch (RejectedExecutionException e) {
            logger.warn("Immediate refresh job cannot be stopped!");
        } finally {
            immediateRefreshJobLock.unlock();
        }
    }

    private void startAutomaticRefresh() {
        logger.debug("Start automatic refresh {}", refreshJob);
        refreshJobLock.lock();
        logger.trace("refreshJobLock is locked");
        try {
            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob == null || localRefreshJob.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::refreshAndUpdateStatus, 0, refresh,
                        TimeUnit.SECONDS);
                logger.debug("Scheduling at fixed delay refreshjob {}", refreshJob);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Refresh time value is invalid! Please change the refresh time configuration!", e);
        } catch (RejectedExecutionException e) {
            logger.warn("Automatic refresh job cannot be started!");
        } finally {
            refreshJobLock.unlock();
            logger.trace("refreshJobLock is unlocked");
        }
    }

    private void stopAutomaticRefresh() {
        logger.debug("Stop automatic refresh for job {}", refreshJob);
        refreshJobLock.lock();
        logger.trace("refreshJobLock is locked");
        try {
            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
                localRefreshJob.cancel(true);
                refreshJob = null;
            }
        } catch (RejectedExecutionException e) {
            logger.warn("Automatic refresh job cannot be stopped!");
        } finally {
            refreshJobLock.unlock();
            logger.trace("refreshJobLock is unlocked");
        }
    }
}
