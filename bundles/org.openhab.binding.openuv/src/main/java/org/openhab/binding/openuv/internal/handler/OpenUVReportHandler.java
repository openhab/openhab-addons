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
package org.openhab.binding.openuv.internal.handler;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.openuv.internal.ReportConfiguration;
import org.openhab.binding.openuv.internal.SafeExposureConfiguration;
import org.openhab.binding.openuv.internal.json.OpenUVResult;
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
    private static final int DEFAULT_REFRESH_PERIOD = 30;

    private final Logger logger = LoggerFactory.getLogger(OpenUVReportHandler.class);

    private @NonNullByDefault({}) OpenUVBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) ScheduledFuture<?> uvMaxJob;

    public OpenUVReportHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenUV handler.");

        ReportConfiguration config = getConfigAs(ReportConfiguration.class);

        if (config.refresh != null && config.refresh < 3) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'refresh' must be higher than 3 minutes to stay in free API plan");
        } else {
            Bridge bridge = getBridge();
            if (bridge == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid bridge");
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
        if ((uvMaxJob == null || uvMaxJob.isCancelled())) {
            State uvMaxTime = openUVData.getUVMaxTime();
            if (uvMaxTime != UnDefType.NULL) {
                ZonedDateTime uvMaxZdt = ((DateTimeType) uvMaxTime).getZonedDateTime();
                long timeDiff = ChronoUnit.MINUTES.between(ZonedDateTime.now(ZoneId.systemDefault()), uvMaxZdt);
                if (timeDiff > 0) {
                    logger.debug("Scheduling {} in {} minutes", UVMAXEVENT, timeDiff);
                    uvMaxJob = scheduler.schedule(() -> {
                        triggerChannel(UVMAXEVENT);
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
        if (refreshJob == null || refreshJob.isCancelled()) {
            ReportConfiguration config = getConfigAs(ReportConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                updateChannels(config);
            }, 0, delay, TimeUnit.MINUTES);
        }
    }

    private void updateChannels(ReportConfiguration config) {
        ThingStatusInfo bridgeStatusInfo = bridgeHandler.getThing().getStatusInfo();
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            OpenUVResult openUVData = bridgeHandler.getUVData(config.getLatitude(), config.getLongitude(),
                    config.getAltitude());
            if (openUVData != null) {
                scheduleUVMaxEvent(openUVData);
                getThing().getChannels().forEach(channel -> {
                    updateChannel(channel.getUID(), openUVData);
                });
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

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }

        if (uvMaxJob != null && !uvMaxJob.isCancelled()) {
            uvMaxJob.cancel(true);
            uvMaxJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                ReportConfiguration config = getConfigAs(ReportConfiguration.class);
                updateChannels(config);
            });
        } else {
            logger.debug("The OpenUV Report Thing only handles Refresh command and not '{}'", command);
        }
    }

    /**
     * Update the channel from the last OpenUV data retrieved
     *
     * @param channelUID the id identifying the channel to be updated
     * @param openUVData
     *
     */
    private void updateChannel(ChannelUID channelUID, OpenUVResult openUVData) {
        Channel channel = getThing().getChannel(channelUID.getId());
        switch (channel.getChannelTypeUID().getId()) {
            case UVINDEX:
                updateState(channelUID, openUVData.getUv());
                break;
            case UVCOLOR:
                updateState(channelUID, getAsHSB(openUVData.getUv().intValue()));
                break;
            case UVMAX:
                updateState(channelUID, openUVData.getUvMax());
                break;
            case OZONE:
                updateState(channelUID, new QuantityType<>(openUVData.getOzone(), SmartHomeUnits.DOBSON_UNIT));
                break;
            case OZONETIME:
                updateState(channelUID, openUVData.getOzoneTime());
                break;
            case UVMAXTIME:
                updateState(channelUID, openUVData.getUVMaxTime());
                break;
            case UVTIME:
                updateState(channelUID, openUVData.getUVTime());
                break;
            case SAFEEXPOSURE:
                SafeExposureConfiguration configuration = channel.getConfiguration()
                        .as(SafeExposureConfiguration.class);
                if (configuration.index != null) {
                    updateState(channelUID, openUVData.getSafeExposureTime().getSafeExposure(configuration.index));
                }
                break;
        }
    }

    private State getAsHSB(int uv) {
        if (uv >= 11) {
            return HSBType.fromRGB(106, 27, 154);
        } else if (uv >= 8) {
            return HSBType.fromRGB(183, 28, 28);
        } else if (uv >= 6) {
            return HSBType.fromRGB(239, 108, 0);
        } else if (uv >= 3) {
            return HSBType.fromRGB(249, 168, 37);
        } else {
            return HSBType.fromRGB(85, 139, 47);
        }
    }
}
