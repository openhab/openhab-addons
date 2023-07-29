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
package org.openhab.binding.openuv.internal.handler;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private static final State ALERT_GREEN = DecimalType.ZERO;
    private static final State ALERT_YELLOW = new DecimalType(1);
    private static final State ALERT_ORANGE = new DecimalType(2);
    private static final State ALERT_RED = new DecimalType(3);
    private static final State ALERT_PURPLE = new DecimalType(4);
    private static final State ALERT_UNDEF = HSBType.fromRGB(179, 179, 179);

    private static final Map<State, State> ALERT_COLORS = Map.of(ALERT_GREEN, HSBType.fromRGB(85, 139, 47),
            ALERT_YELLOW, HSBType.fromRGB(249, 168, 37), ALERT_ORANGE, HSBType.fromRGB(239, 108, 0), ALERT_RED,
            HSBType.fromRGB(183, 28, 28), ALERT_PURPLE, HSBType.fromRGB(106, 27, 154));

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
            if (uvMaxTime != UnDefType.NULL) {
                ZonedDateTime uvMaxZdt = ((DateTimeType) uvMaxTime).getZonedDateTime();
                long timeDiff = ChronoUnit.MINUTES.between(ZonedDateTime.now(ZoneId.systemDefault()), uvMaxZdt);
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
                    updateChannels(config);
                }
            }, 0, config.refresh, TimeUnit.MINUTES);
        }
    }

    private void updateChannels(ReportConfiguration config) {
        ThingStatusInfo bridgeStatusInfo = bridgeHandler.getThing().getStatusInfo();
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            PointType location = new PointType(config.location);
            OpenUVResult openUVData = bridgeHandler.getUVData(location.getLatitude().toString(),
                    location.getLongitude().toString(), location.getAltitude().toString());
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
        ScheduledFuture<?> refresh = refreshJob;
        if (refresh != null && !refresh.isCancelled()) {
            refresh.cancel(true);
        }
        refreshJob = null;

        ScheduledFuture<?> uxMax = uvMaxJob;
        if (uxMax != null && !uxMax.isCancelled()) {
            uxMax.cancel(true);
        }
        uvMaxJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                ReportConfiguration config = getConfigAs(ReportConfiguration.class);
                updateChannels(config);
            });
        } else if (ELEVATION.equals(channelUID.getId()) && command instanceof QuantityType) {
            QuantityType<?> qtty = (QuantityType<?>) command;
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
                return asAlertLevel(openUVData.getUv());
            case UV_COLOR:
                return ALERT_COLORS.getOrDefault(asAlertLevel(openUVData.getUv()), ALERT_UNDEF);
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
            return openUVData.getSafeExposureTime(configuration.index);
        }
        return UnDefType.NULL;
    }

    private State asAlertLevel(double uv) {
        if (uv >= 11) {
            return ALERT_PURPLE;
        } else if (uv >= 8) {
            return ALERT_RED;
        } else if (uv >= 6) {
            return ALERT_ORANGE;
        } else if (uv >= 3) {
            return ALERT_YELLOW;
        } else if (uv > 0) {
            return ALERT_GREEN;
        }
        return UnDefType.NULL;
    }
}
