/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv.internal.handler;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.openuv.internal.OpenUVConfiguration;
import org.openhab.binding.openuv.internal.json.OpenUVJsonResult;
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

    @NonNullByDefault({})
    private OpenUVBridgeHandler bridgeHandler;

    @NonNullByDefault({})
    private ScheduledFuture<?> refreshJob;
    @NonNullByDefault({})
    private ScheduledFuture<?> uvMaxJob;

    public OpenUVReportHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenUV handler.");

        OpenUVConfiguration config = getConfigAs(OpenUVConfiguration.class);

        String errorMsg = null;

        if (config.refresh != null && config.refresh < 3) {
            errorMsg = "Parameter 'refresh' must be higher than 3 minutes to stay in free API plan";
        }

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeHandler = (OpenUVBridgeHandler) bridge.getHandler();
        } else {
            errorMsg = "Invalid bridge";
        }

        if (errorMsg == null) {
            updateStatus(ThingStatus.UNKNOWN);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job screening UV Max reached
     *
     * @param openUVData
     */
    private void scheduleUVMaxEvent(OpenUVJsonResult openUVData) {
        if ((uvMaxJob == null || uvMaxJob.isCancelled())) {
            long timeDiff = ChronoUnit.MINUTES.between(ZonedDateTime.now(ZoneId.systemDefault()),
                    openUVData.getUVMaxTimeAsZDT());

            if (timeDiff > 0) {
                logger.debug("Scheduling {} in {} minutes", UVMAXEVENT, timeDiff);
                uvMaxJob = scheduler.schedule(() -> {
                    triggerChannel(UVMAXEVENT);
                    uvMaxJob = null;
                }, timeDiff, TimeUnit.MINUTES);
            }
        }
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            OpenUVConfiguration config = getConfigAs(OpenUVConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                updateChannels();
            }, 0, delay, TimeUnit.MINUTES);
        }
    }

    private void updateChannels() {
        try {
            OpenUVJsonResult openUVData = getOpenUVData();
            scheduleUVMaxEvent(openUVData);
            for (Channel channel : getThing().getChannels()) {
                updateChannel(channel.getUID(), openUVData);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.error("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
                updateChannels();
            });
        } else {
            logger.debug("The OpenUV binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Update the channel from the last OpenUV data retrieved
     *
     * @param channelUID the id identifying the channel to be updated
     * @param openUVData
     *
     */
    private void updateChannel(ChannelUID channelUID, OpenUVJsonResult openUVData) {
        switch (channelUID.getId()) {
            case UVINDEX:
                updateState(channelUID, openUVData.getUv());
                break;
            case UVCOLOR:
                updateState(channelUID, getAsHSB(openUVData.getUv().floatValue()));
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
                Channel channel = getThing().getChannel(channelUID.getId());
                if (channel != null) {
                    Configuration configuration = channel.getConfiguration();
                    int index = 1;
                    Object skinIndex = configuration.get(PROPERTY_INDEX);
                    if (skinIndex != null) {
                        try {
                            index = Integer.parseInt(skinIndex.toString());
                        } finally {
                            updateState(channelUID, openUVData.getSafeExposureTime().getSafeExposure(index));
                        }
                    }
                }
                break;
        }
    }

    private State getAsHSB(float uv) {
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

    /**
     * Request new UV Index data to the service
     *
     * @throws IOException
     */
    private OpenUVJsonResult getOpenUVData() throws IOException {
        OpenUVConfiguration config = getConfigAs(OpenUVConfiguration.class);
        return bridgeHandler.getUVData(config.getLatitude(), config.getLongitude(), config.getAltitude()).getResult();
    }

}
