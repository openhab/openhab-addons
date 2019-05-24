/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.network.internal.SpeedTestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import fr.bmartel.speedtest.model.SpeedTestMode;

/**
 * The {@link SpeedTestHandler } is responsible for launching bandwidth
 * measurements at a given interval and for given file / size
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SpeedTestHandler extends BaseThingHandler implements ISpeedTestListener {
    private final Logger logger = LoggerFactory.getLogger(SpeedTestHandler.class);
    private static final BigDecimal TO_MBIT = new BigDecimal(1048576);
    private @Nullable SpeedTestSocket speedTestSocket;
    private @NonNullByDefault({}) ScheduledFuture<?> refreshTask;
    private @NonNullByDefault({}) SpeedTestConfiguration configuration;
    private State bufferedProgress = UnDefType.UNDEF;

    public SpeedTestHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = this.getConfigAs(SpeedTestConfiguration.class);
        startAutomaticRefresh();
        updateStatus(ThingStatus.ONLINE);
    }

    private void startAutomaticRefresh() {
        final int initialDelay = 2;
        refreshTask = scheduler.scheduleWithFixedDelay(this::launchSpeedTest, initialDelay,
                configuration.refreshInterval, TimeUnit.MINUTES);
        logger.info("Start automatic in {} minutes, refreshed every {} minutes", initialDelay,
                configuration.refreshInterval);
    }

    private void launchSpeedTest() {
        updateState(CHANNEL_TEST_ISRUNNING, OnOffType.ON);
        updateState(CHANNEL_TEST_START, new DateTimeType());
        updateState(CHANNEL_TEST_END, UnDefType.NULL);
        if (speedTestSocket == null) {
            speedTestSocket = new SpeedTestSocket(1500);
            speedTestSocket.addSpeedTestListener(this);
            startTest(SpeedTestMode.DOWNLOAD);
        } else {
            logger.warn("A speedtest is still in progress, will retry on next refresh");
        }

    }

    private void startTest(final SpeedTestMode mode) {
        updateProgress(new QuantityType<>(0, SmartHomeUnits.PERCENT));
        String fullURL = configuration.url;
        fullURL += fullURL.endsWith("/") ? "" : "/";
        if (mode == SpeedTestMode.DOWNLOAD) {
            fullURL += configuration.fileName;
            speedTestSocket.startDownload(fullURL);
        } else {
            speedTestSocket.startUpload(fullURL, configuration.uploadSize);
        }
    }

    @Override
    public void onCompletion(final @Nullable SpeedTestReport testReport) {
        if (testReport != null) {
            switch (testReport.getSpeedTestMode()) {
                case DOWNLOAD:
                    updateState(CHANNEL_RATE_DOWN, new DecimalType(testReport.getTransferRateBit().divide(TO_MBIT)));
                    startTest(SpeedTestMode.UPLOAD);
                    break;
                case UPLOAD:
                    updateStatus(ThingStatus.ONLINE);
                    updateState(CHANNEL_RATE_UP, new DecimalType(testReport.getTransferRateBit().divide(TO_MBIT)));
                    updateState(CHANNEL_TEST_ISRUNNING, OnOffType.OFF);
                    updateProgress(UnDefType.NULL);
                    updateState(CHANNEL_TEST_END, new DateTimeType());
                    speedTestSocket.removeSpeedTestListener(this);
                    speedTestSocket = null;
                    break;
            }
        }

    }

    @Override
    public void onError(@Nullable SpeedTestError testError, @Nullable String errorMessage) {
        if (testError != null) {
            switch (testError) {
                case UNSUPPORTED_PROTOCOL:
                case MALFORMED_URI:
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
                    refreshTask.cancel(true);
                    break;
                case CONNECTION_ERROR:
                    logger.warn(errorMessage);
                    return;
                case SOCKET_TIMEOUT:
                case SOCKET_ERROR:
                case INVALID_HTTP_RESPONSE:
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                    break;
            }
        }
        updateState(CHANNEL_TEST_ISRUNNING, OnOffType.OFF);
        updateProgress(UnDefType.NULL);
        updateState(CHANNEL_TEST_END, new DateTimeType());
        logger.warn(errorMessage);
    }

    @Override
    public void onProgress(float percent, @Nullable SpeedTestReport testReport) {
        updateProgress(new QuantityType<>(Math.round(percent), SmartHomeUnits.PERCENT));
    }

    private void updateProgress(State state) {
        if (!state.toString().equals(bufferedProgress.toString())) {
            bufferedProgress = state;
            updateState(CHANNEL_TEST_PROGRESS, bufferedProgress);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            launchSpeedTest();
        } else {
            logger.debug("Command {} is not supported for channel: {}. Supported command: REFRESH", command,
                    channelUID.getId());
        }
    }

    @Override
    public void dispose() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
    }
}
