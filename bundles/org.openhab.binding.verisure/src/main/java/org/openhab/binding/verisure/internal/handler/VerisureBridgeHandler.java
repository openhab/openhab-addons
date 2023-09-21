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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.verisure.internal.DeviceStatusListener;
import org.openhab.binding.verisure.internal.VerisureBridgeConfiguration;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.discovery.VerisureThingDiscoveryService;
import org.openhab.binding.verisure.internal.dto.VerisureThingDTO;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author l3rum - Initial contribution
 * @author Jan Gustafsson - Furher development
 */
@NonNullByDefault
public class VerisureBridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private static final int REFRESH_DELAY_SECONDS = 30;
    private final Logger logger = LoggerFactory.getLogger(VerisureBridgeHandler.class);
    private final ReentrantLock immediateRefreshJobLock = new ReentrantLock();
    private final HttpClient httpClient;

    private String authstring = "";
    private @Nullable String pinCode;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> immediateRefreshJob;
    private @Nullable VerisureSession session;

    public VerisureBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("VerisureBridgeHandler Handle command {} on channelUID: {}", command, channelUID);
        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(CHANNEL_STATUS) && channelUID.getThingUID().equals(getThing().getUID())) {
                logger.debug("Refresh command on status channel {} will trigger instant refresh", channelUID);
                scheduleImmediateRefresh(0);
            } else {
                logger.debug("Refresh command on channel {} will trigger refresh in {} seconds", channelUID,
                        REFRESH_DELAY_SECONDS);
                scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
            }
        } else {
            logger.warn("unknown command! {}", command);
        }
    }

    public @Nullable VerisureSession getSession() {
        return session;
    }

    public @Nullable ThingUID getUID() {
        return getThing().getUID();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Verisure Binding");
        VerisureBridgeConfiguration config = getConfigAs(VerisureBridgeConfiguration.class);

        this.pinCode = config.pin;
        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration of username and password is mandatory");

        } else if (config.refresh < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Refresh time is lower than min value of 10!");
        } else {
            authstring = "j_username=" + config.username;
            scheduler.execute(() -> {
                if (session == null) {
                    logger.debug("Session is null, let's create a new one");
                    session = new VerisureSession(this.httpClient);
                }
                VerisureSession session = this.session;
                updateStatus(ThingStatus.UNKNOWN);
                if (session != null) {
                    if (!session.initialize(authstring, pinCode, config.username, config.password)) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Failed to login to Verisure, please check your account settings! Is MFA activated?");
                    }
                }
                startAutomaticRefresh(config.refresh);
            });
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopAutomaticRefresh();
        stopImmediateRefresh();
        session = null;
    }

    public <T extends VerisureThingDTO> boolean registerObjectStatusListener(
            DeviceStatusListener<T> deviceStatusListener) {
        VerisureSession mySession = session;
        if (mySession != null) {
            logger.debug("registerObjectStatusListener for listener {}", deviceStatusListener);
            return mySession.registerDeviceStatusListener(deviceStatusListener);
        }
        return false;
    }

    public <T extends VerisureThingDTO> boolean unregisterObjectStatusListener(
            DeviceStatusListener<T> deviceStatusListener) {
        VerisureSession mySession = session;
        if (mySession != null) {
            logger.debug("unregisterObjectStatusListener for listener {}", deviceStatusListener);
            return mySession.unregisterDeviceStatusListener(deviceStatusListener);
        }
        return false;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(VerisureThingDiscoveryService.class);
    }

    private void refreshAndUpdateStatus() {
        logger.debug("Refresh and update status!");
        VerisureSession session = this.session;
        if (session != null) {
            if (session.refresh()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    }

    void scheduleImmediateRefresh(int refreshDelay) {
        logger.debug("VerisureBridgeHandler - scheduleImmediateRefresh");
        immediateRefreshJobLock.lock();
        ScheduledFuture<?> refreshJob = this.refreshJob;
        ScheduledFuture<?> immediateRefreshJob = this.immediateRefreshJob;
        try {
            // We schedule in 10 sec, to avoid multiple updates
            if (refreshJob != null) {
                logger.debug("Current remaining delay {} for refresh job {}", refreshJob.getDelay(TimeUnit.SECONDS),
                        refreshJob);
                if (immediateRefreshJob != null) {
                    logger.debug("Current remaining delay {} for immediate refresh job {}",
                            immediateRefreshJob.getDelay(TimeUnit.SECONDS), immediateRefreshJob);
                }

                if (refreshJob.getDelay(TimeUnit.SECONDS) > refreshDelay) {
                    if (immediateRefreshJob == null || immediateRefreshJob.getDelay(TimeUnit.SECONDS) <= 0) {
                        if (immediateRefreshJob != null) {
                            logger.debug("Current remaining delay {} for immediate refresh job {}",
                                    immediateRefreshJob.getDelay(TimeUnit.SECONDS), immediateRefreshJob);
                        }
                        // Note we are using getDelay() instead of isDone() as we want to allow Things to schedule a
                        // refresh if their status is pending. As the status update happens inside the
                        // refreshAndUpdateStatus
                        // execution the isDone() will return false and would not allow the rescheduling of the task.
                        this.immediateRefreshJob = scheduler.schedule(this::refreshAndUpdateStatus, refreshDelay,
                                TimeUnit.SECONDS);
                        logger.debug("Scheduling new immediate refresh job {}", immediateRefreshJob);
                    }
                }
            }
        } catch (RejectedExecutionException e) {
            logger.warn("Immediate refresh job cannot be scheduled!");
        } finally {
            immediateRefreshJobLock.unlock();
        }
    }

    private void startAutomaticRefresh(int refresh) {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        logger.debug("Start automatic refresh {}", refreshJob);
        if (refreshJob == null || refreshJob.isCancelled()) {
            try {
                this.refreshJob = scheduler.scheduleWithFixedDelay(this::refreshAndUpdateStatus, 0, refresh,
                        TimeUnit.SECONDS);
                logger.debug("Scheduling at fixed delay refreshjob {}", this.refreshJob);
            } catch (RejectedExecutionException e) {
                logger.warn("Automatic refresh job cannot be started!");
            }
        }
    }

    private void stopAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        logger.debug("Stop automatic refresh for job {}", refreshJob);
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    private void stopImmediateRefresh() {
        immediateRefreshJobLock.lock();
        ScheduledFuture<?> immediateRefreshJob = this.immediateRefreshJob;
        try {
            logger.debug("Stop immediate refresh for job {}", immediateRefreshJob);
            if (immediateRefreshJob != null) {
                immediateRefreshJob.cancel(true);
                this.immediateRefreshJob = null;
            }
        } catch (RejectedExecutionException e) {
            logger.warn("Immediate refresh job cannot be scheduled!");
        } finally {
            immediateRefreshJobLock.unlock();
        }
    }
}
