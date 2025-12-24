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
package org.openhab.binding.network.internal.handler;

import static org.openhab.binding.network.internal.NetworkBindingConstants.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.SpeedTestConfiguration;
import org.openhab.core.library.dimension.DataTransferRate;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

/**
 * The {@link SpeedTestHandler } is responsible for launching bandwidth
 * measurements at a given interval and for given file / size
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ravi Nadahar - Made class thread-safe
 */
@NonNullByDefault
public class SpeedTestHandler extends BaseThingHandler implements ISpeedTestListener {
    private final Logger logger = LoggerFactory.getLogger(SpeedTestHandler.class);

    /* All access must be guarded by "this" */
    private @Nullable SpeedTestSocket speedTestSocket;

    /* All access must be guarded by "this" */
    private @Nullable ScheduledFuture<?> refreshTask;

    /* All access must be guarded by "this" */
    private State bufferedProgress = UnDefType.UNDEF;

    private final AtomicInteger timeouts = new AtomicInteger();

    public SpeedTestHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        startRefreshTask();
    }

    private void startSpeedTest() {
        SpeedTestConfiguration config = getConfigAs(SpeedTestConfiguration.class);
        String url = config.getDownloadURL();
        if (url == null || url.isBlank()) {
            logger.warn("Failed to start speedtest because the URL is blank");
            return;
        }
        synchronized (this) {
            if (speedTestSocket == null) {
                logger.debug("Network speedtest started");
                final SpeedTestSocket socket = new SpeedTestSocket(1500);
                speedTestSocket = socket;
                socket.addSpeedTestListener(this);
                updateState(CHANNEL_TEST_ISRUNNING, OnOffType.ON);
                updateState(CHANNEL_TEST_START, new DateTimeType());
                updateState(CHANNEL_TEST_END, UnDefType.NULL);
                updateProgress(new QuantityType<>(0, Units.PERCENT));
                socket.startDownload(url);
            } else {
                logger.info("A speedtest is already in progress, will retry on next refresh");
            }
        }
    }

    private void stopSpeedTest() {
        updateState(CHANNEL_TEST_ISRUNNING, OnOffType.OFF);
        updateProgress(UnDefType.NULL);
        updateState(CHANNEL_TEST_END, new DateTimeType());
        synchronized (this) {
            if (speedTestSocket != null) {
                SpeedTestSocket socket = speedTestSocket;
                socket.closeSocket();
                socket.removeSpeedTestListener(this);
                speedTestSocket = null;
                logger.debug("Network speedtest finished");
            }
        }
    }

    @Override
    public void onCompletion(final @Nullable SpeedTestReport testReport) {
        SpeedTestConfiguration config = getConfigAs(SpeedTestConfiguration.class);
        timeouts.set(config.maxTimeout);
        if (testReport != null) {
            BigDecimal rate = testReport.getTransferRateBit();
            QuantityType<DataTransferRate> quantity = new QuantityType<>(rate, BIT_PER_SECOND)
                    .toUnit(MEGABIT_PER_SECOND);
            if (quantity != null) {
                switch (testReport.getSpeedTestMode()) {
                    case DOWNLOAD:
                        updateState(CHANNEL_RATE_DOWN, quantity);
                        String url = config.getUploadURL();
                        synchronized (this) {
                            if (speedTestSocket != null && url != null) {
                                speedTestSocket.startUpload(config.getUploadURL(), config.uploadSize);
                            }
                        }
                        break;
                    case UPLOAD:
                        updateState(CHANNEL_RATE_UP, quantity);
                        stopSpeedTest();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onError(final @Nullable SpeedTestError testError, final @Nullable String errorMessage) {
        if (SpeedTestError.UNSUPPORTED_PROTOCOL.equals(testError) || SpeedTestError.MALFORMED_URI.equals(testError)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
            freeRefreshTask();
        } else if (SpeedTestError.SOCKET_TIMEOUT.equals(testError)) {
            int count = timeouts.decrementAndGet();
            if (count <= 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Max timeout count reached");
                freeRefreshTask();
            } else {
                logger.warn("Speedtest timed out, {} attempts left. Message '{}'", count, errorMessage);
                stopSpeedTest();
            }
        } else if (SpeedTestError.SOCKET_ERROR.equals(testError)
                || SpeedTestError.INVALID_HTTP_RESPONSE.equals(testError)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            freeRefreshTask();
        } else {
            stopSpeedTest();
            logger.warn("Speedtest failed: {}", errorMessage);
        }
    }

    @Override
    public void onProgress(float percent, @Nullable SpeedTestReport testReport) {
        updateProgress(new QuantityType<>(Math.round(percent), Units.PERCENT));
    }

    private void updateProgress(State state) {
        boolean isNew = false;
        synchronized (this) {
            if (!state.toString().equals(bufferedProgress.toString())) {
                bufferedProgress = state;
                isNew = true;
            }
        }
        if (isNew) {
            updateState(CHANNEL_TEST_PROGRESS, state);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == OnOffType.ON
                && ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR == getThing().getStatusInfo().getStatusDetail()) {
            logger.debug("Speedtest was offline, restarting it upon command to do so");
            startRefreshTask();
        } else {
            if (CHANNEL_TEST_ISRUNNING.equals(channelUID.getId())) {
                if (command == OnOffType.ON) {
                    startSpeedTest();
                } else if (command == OnOffType.OFF) {
                    stopSpeedTest();
                }
            } else {
                logger.debug("Command {} is not supported for channel: {}.", command, channelUID.getId());
            }
        }
    }

    @Override
    public void dispose() {
        freeRefreshTask();
    }

    private void freeRefreshTask() {
        synchronized (this) {
            ScheduledFuture<?> task = refreshTask;
            if (task != null) {
                task.cancel(true);
                refreshTask = null;
            }
        }
        stopSpeedTest();
    }

    private void startRefreshTask() {
        SpeedTestConfiguration config = getConfigAs(SpeedTestConfiguration.class);
        logger.info("Speedtests starts in {} minutes, then refreshes every {} minutes", config.initialDelay,
                config.refreshInterval);
        synchronized (this) {
            refreshTask = scheduler.scheduleWithFixedDelay(this::startSpeedTest, config.initialDelay,
                    config.refreshInterval, TimeUnit.MINUTES);
        }
        timeouts.set(config.maxTimeout);
        updateStatus(ThingStatus.ONLINE);
    }
}
