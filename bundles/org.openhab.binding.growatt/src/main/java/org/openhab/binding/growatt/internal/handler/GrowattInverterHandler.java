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
package org.openhab.binding.growatt.internal.handler;

import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.growatt.internal.action.GrowattActions;
import org.openhab.binding.growatt.internal.cloud.GrowattApiException;
import org.openhab.binding.growatt.internal.cloud.GrowattCloud;
import org.openhab.binding.growatt.internal.config.GrowattInverterConfiguration;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.binding.growatt.internal.dto.GrottValues;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GrowattInverterHandler} is a thing handler for Growatt inverters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattInverterHandler extends BaseThingHandler {

    // data-logger sends packets each 5 minutes; timeout means 2 packets missed
    private static final int AWAITING_DATA_TIMEOUT_MINUTES = 11;

    private final Logger logger = LoggerFactory.getLogger(GrowattInverterHandler.class);
    private final HttpClientFactory httpClientFactory;

    private String deviceId = "unknown";

    private @Nullable ScheduledFuture<?> awaitingDataTimeoutTask;
    private @Nullable GrowattCloud growattCloud;

    public GrowattInverterHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void dispose() {
        growattCloud = null;
        ScheduledFuture<?> task = awaitingDataTimeoutTask;
        if (task != null) {
            task.cancel(true);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(GrowattActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // everything is read only so do nothing
    }

    @Override
    public void initialize() {
        GrowattInverterConfiguration config = getConfigAs(GrowattInverterConfiguration.class);
        deviceId = config.deviceId;
        thing.setProperty(GrowattInverterConfiguration.DEVICE_ID, deviceId);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.awaiting-data");
        scheduleAwaitingDataTimeoutTask();
        logger.debug("initialize() thing has {} channels", thing.getChannels().size());
    }

    private void scheduleAwaitingDataTimeoutTask() {
        ScheduledFuture<?> task = awaitingDataTimeoutTask;
        if (task != null) {
            task.cancel(true);
        }
        awaitingDataTimeoutTask = scheduler.schedule(() -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.awaiting-data-timeout");
        }, AWAITING_DATA_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Receives a collection of GrottDevice inverter objects containing potential data for this thing. If the collection
     * contains an entry matching the things's deviceId, and it contains GrottValues, then process it further. Otherwise
     * go offline with a configuration error.
     *
     * @param inverters collection of GrottDevice objects.
     */
    public void updateInverters(Collection<GrottDevice> inverters) {
        inverters.stream().filter(inverter -> deviceId.equals(inverter.getDeviceId()))
                .map(inverter -> inverter.getValues()).filter(values -> values != null).findAny()
                .ifPresentOrElse(values -> {
                    updateStatus(ThingStatus.ONLINE);
                    scheduleAwaitingDataTimeoutTask();
                    updateInverterValues(values);
                }, () -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                });
    }

    /**
     * Receives a GrottValues object containing state values for this thing. Process the respective values and update
     * the channels accordingly.
     *
     * @param inverter a GrottDevice object containing the new status values.
     */
    public void updateInverterValues(GrottValues inverterValues) {
        // get channel states
        Map<String, QuantityType<?>> channelStates;
        try {
            channelStates = inverterValues.getChannelStates();
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
            logger.warn("updateInverterValues() unexpected exception:{}, message:{}", e.getClass().getName(),
                    e.getMessage(), e);
            return;
        }

        // find unused channels
        List<Channel> actualChannels = thing.getChannels();
        List<Channel> unusedChannels = actualChannels.stream()
                .filter(channel -> !channelStates.containsKey(channel.getUID().getId())).collect(Collectors.toList());

        // remove unused channels
        if (!unusedChannels.isEmpty()) {
            updateThing(editThing().withoutChannels(unusedChannels).build());
            logger.debug("updateInverterValues() channel count {} reduced by {} to {}", actualChannels.size(),
                    unusedChannels.size(), thing.getChannels().size());
        }

        List<String> thingChannelIds = thing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toList());

        // update channel states
        channelStates.forEach((channelId, state) -> {
            if (thingChannelIds.contains(channelId)) {
                updateState(channelId, state);
            } else {
                logger.warn("updateInverterValues() channel '{}' not found; try re-creating the thing", channelId);
            }
        });
    }

    private GrowattCloud getGrowattCloud() throws IllegalStateException {
        GrowattCloud growattCloud = this.growattCloud;
        if (growattCloud == null) {
            try {
                growattCloud = new GrowattCloud(getConfigAs(GrowattInverterConfiguration.class), httpClientFactory);
            } catch (Exception e) {
                throw new IllegalStateException("GrowattCloud not created", e);
            }
            this.growattCloud = growattCloud;
        }
        return growattCloud;
    }

    /**
     * This method is called from a Rule Action to setup the battery charging program.
     *
     * @param chargingPower the rate of charging 0%..100%
     * @param targetSOC the SOC at which to stop charging 0%..100%
     * @param allowAcCharging allow the battery to be charged from AC power
     * @param startTime the start time of the charging program; a time formatted string e.g. "12:34"
     * @param stopTime the stop time of the charging program; a time formatted string e.g. "12:34"
     * @param programEnable charge program shall be enabled
     */
    public void setupChargingProgram(Number chargingPower, Number targetSOC, boolean allowAcCharging, String startTime,
            String stopTime, boolean programEnable) {
        try {
            getGrowattCloud().setupChargingProgram(chargingPower.intValue(), targetSOC.intValue(), allowAcCharging,
                    GrowattCloud.localTimeOf(startTime), GrowattCloud.localTimeOf(stopTime), programEnable);
        } catch (IllegalStateException | DateTimeParseException | GrowattApiException e) {
            logger.warn("setupChargingProgram() error", e);
            this.growattCloud = null;
        }
    }

    /**
     * This method is called from a Rule Action to setup the battery discharging program.
     *
     * @param dischargingPower the rate of discharging 1%..100%
     * @param targetSOC the SOC at which to stop charging 1%..100%
     * @param startTime the start time of the discharging program; a time formatted string e.g. "12:34"
     * @param stopTime the stop time of the discharging program; a time formatted string e.g. "12:34"
     * @param programEnable the discharge program shall be enabled
     */
    public void setupDischargingProgram(Number dischargingPower, Number targetSOC, String startTime, String stopTime,
            boolean programEnable) {
        try {
            getGrowattCloud().setupDischargingProgram(dischargingPower.intValue(), targetSOC.intValue(),
                    GrowattCloud.localTimeOf(startTime), GrowattCloud.localTimeOf(stopTime), programEnable);
        } catch (IllegalStateException | DateTimeParseException | GrowattApiException e) {
            logger.warn("setupDischargingProgram() error", e);
            this.growattCloud = null;
        }
    }
}
