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
package org.openhab.binding.openuv.internal.handler;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openuv.internal.AlertLevel;
import org.openhab.binding.openuv.internal.OpenUVException;
import org.openhab.binding.openuv.internal.config.ReportConfiguration;
import org.openhab.binding.openuv.internal.config.SafeExposureConfiguration;
import org.openhab.binding.openuv.internal.json.OpenUVResult;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenUVReportHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVReportHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenUVReportHandler.class);

    private @NonNullByDefault({}) OpenUVBridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> uvMaxJob;
    private boolean suspendUpdates = false;

    public OpenUVReportHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenUV handler.");

        ReportConfiguration config = getConfigAs(ReportConfiguration.class);

        if (config.refresh < 3) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-refresh");
        } else {
            Bridge bridge = getBridge();
            if (bridge == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            } else {
                bridgeHandler = (OpenUVBridgeHandler) bridge.getHandler();
                updateStatus(ThingStatus.UNKNOWN);
                startAutomaticRefresh();
            }
        }
    }

    /**
     * Start the job screening UV Max reached
     *
     * @param openUVData
     */
    private void scheduleUVMaxEvent(OpenUVResult openUVData) {
        ScheduledFuture<?> job = this.uvMaxJob;
        if ((job == null || job.isCancelled())) {
            State uvMaxTime = openUVData.getUVMaxTime();
            if (uvMaxTime instanceof DateTimeType uvMaxDateTime) {
                long timeDiff = ChronoUnit.MINUTES.between(ZonedDateTime.now(ZoneId.systemDefault()),
                        uvMaxDateTime.getZonedDateTime());
                if (timeDiff > 0) {
                    logger.debug("Scheduling {} in {} minutes", UV_MAX_EVENT, timeDiff);
                    uvMaxJob = scheduler.schedule(() -> {
                        triggerChannel(UV_MAX_EVENT);
                        uvMaxJob = null;
                    }, timeDiff, TimeUnit.MINUTES);
                }
            }
        }
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> job = this.refreshJob;
        if (job == null || job.isCancelled()) {
            ReportConfiguration config = getConfigAs(ReportConfiguration.class);
            refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                if (!suspendUpdates) {
                    updateChannels(new PointType(config.location));
                }
            }, 0, config.refresh, TimeUnit.MINUTES);
        }
    }

    private void updateChannels(PointType location) {
        ThingStatusInfo bridgeStatusInfo = bridgeHandler.getThing().getStatusInfo();
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            OpenUVResult openUVData = bridgeHandler.getUVData(location.getLatitude().toBigDecimal(),
                    location.getLongitude().toBigDecimal(), location.getAltitude().toBigDecimal());
            if (openUVData != null) {
                scheduleUVMaxEvent(openUVData);
                getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID().getId()))
                        .forEach(channel -> updateState(channel.getUID(), getState(channel, openUVData)));
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, bridgeStatusInfo.getStatusDetail(),
                        bridgeStatusInfo.getDescription());
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the OpenUV handler.");

        cancelFuture(refreshJob);
        refreshJob = null;

        cancelFuture(uvMaxJob);
        uvMaxJob = null;
    }

    private void cancelFuture(@Nullable ScheduledFuture<?> job) {
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                ReportConfiguration config = getConfigAs(ReportConfiguration.class);
                updateChannels(new PointType(config.location));
            });
        } else if (ELEVATION.equals(channelUID.getId()) && command instanceof QuantityType<?> qtty) {
            if (Units.DEGREE_ANGLE.equals(qtty.getUnit())) {
                suspendUpdates = qtty.doubleValue() < 0;
            } else {
                logger.info("The OpenUV Report handles Sun Elevation of Number:Angle type, {} does not fit.", command);
            }
        } else {
            logger.info("The OpenUV Report Thing handles Refresh or Sun Elevation command and not '{}'", command);
        }
    }

    private State getState(Channel channel, OpenUVResult openUVData) {
        ChannelUID uid = channel.getUID();
        switch (uid.getId()) {
            case UV_INDEX:
                return new DecimalType(openUVData.getUv());
            case ALERT_LEVEL:
                return AlertLevel.fromUVIndex(openUVData.getUv()).state;
            case UV_COLOR:
                return hexToHSB(AlertLevel.fromUVIndex(openUVData.getUv()).color);
            case UV_MAX:
                return new DecimalType(openUVData.getUvMax());
            case OZONE:
                return new QuantityType<>(openUVData.getOzone(), Units.DOBSON_UNIT);
            case OZONE_TIME:
                return openUVData.getOzoneTime();
            case UV_MAX_TIME:
                return openUVData.getUVMaxTime();
            case UV_TIME:
                return openUVData.getUVTime();
        }

        ChannelTypeUID channelType = channel.getChannelTypeUID();
        if (channelType != null && SAFE_EXPOSURE.equals(channelType.getId())) {
            SafeExposureConfiguration configuration = channel.getConfiguration().as(SafeExposureConfiguration.class);
            try {
                return openUVData.getSafeExposureTime(configuration.index);
            } catch (OpenUVException e) {
                logger.warn("Error getting safe exposure value : {}", e.getMessage());
            }
        }

        return UnDefType.NULL;
    }

    private State hexToHSB(String hexValue) {
        int resultRed = Integer.valueOf(hexValue.substring(0, 2), 16);
        int resultGreen = Integer.valueOf(hexValue.substring(2, 4), 16);
        int resultBlue = Integer.valueOf(hexValue.substring(4, 6), 16);
        return HSBType.fromRGB(resultRed, resultGreen, resultBlue);
    }
}
