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
package org.openhab.binding.worxlandroid.internal.handler;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.worxlandroid.internal.api.WebApiException;
import org.openhab.binding.worxlandroid.internal.api.WorxApiDeserializer;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.MowerCommand;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.OneTimeCommand;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.ScheduleCommand;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.ScheduleDaysCommand;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.SetRainDelay;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.ZoneMeterAlloc;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.ZoneMeterCommand;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Dat.Axis;
import org.openhab.binding.worxlandroid.internal.api.dto.ProductItemStatus;
import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidActionCodes;
import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidDayCodes;
import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidStatusCodes;
import org.openhab.binding.worxlandroid.internal.config.MowerConfiguration;
import org.openhab.binding.worxlandroid.internal.vo.Mower;
import org.openhab.binding.worxlandroid.internal.vo.ScheduledDay;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The{@link WorxLandroidMowerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nils - Initial contribution
 *
 */
@NonNullByDefault
public class WorxLandroidMowerHandler extends AWSClientThingHandler {
    private static final String EMPTY_PAYLOAD = "{}";

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidMowerHandler.class);
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<ScheduledFuture<?>> pollingJob = Optional.empty();

    private Optional<Mower> mower = Optional.empty();

    public WorxLandroidMowerHandler(Thing thing, WorxApiDeserializer deserializer) {
        super(thing, deserializer);
    }

    @Override
    public void initialize() {
        super.initialize();
        MowerConfiguration config = getConfigAs(MowerConfiguration.class);

        if (config.serialNumber.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-serial");
            return;
        }

        WorxLandroidBridgeHandler bridgeHandler = getBridgeHandler(getBridge(), WorxLandroidBridgeHandler.class);
        if (bridgeHandler != null) {
            initializeData(bridgeHandler);
        }
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();

        pollingJob.ifPresent(job -> job.cancel(true));
        pollingJob = Optional.empty();

        super.dispose();
    }

    private void initializeData(WorxLandroidBridgeHandler bridgeHandler) {
        MowerConfiguration config = getConfigAs(MowerConfiguration.class);
        logger.debug("Initializing WorxLandroidMowerHandler for serial number '{}'", config.serialNumber);
        try {
            ProductItemStatus product = bridgeHandler.retrieveDeviceStatus(config.serialNumber);
            if (product != null) {
                connectAws(product.mqttEndpoint, product.uuid, product.userId, product.mqttTopics.commandOut);
                mower = Optional.of(new Mower(this, product));
                if (firstLaunch()) {
                    setChannelsAndProperties(mower.get());
                }
                processStatusMessage(mower.get());

                updateStatus(product.online ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
                startScheduledJobs(bridgeHandler, mower.get(), config);
            }
        } catch (WebApiException e) {
            logger.error("initialize mower: id {} - {}::{}", config.serialNumber, getThing().getLabel(),
                    getThing().getUID());
        }
    }

    private void setChannelsAndProperties(Mower mower) {
        ThingBuilder thingBuilder = editThing();
        Set<ChannelUID> toRemove = new HashSet<>();

        if (!mower.lockSupported()) { // lock channel only when supported
            toRemove.add(getChannelUID(GROUP_COMMON, CHANNEL_LOCK));
        }

        if (!mower.rainDelaySupported()) { // rainDelay channel only when supported
            toRemove.add(getChannelUID(GROUP_RAIN, CHANNEL_DELAY));
        }

        if (!mower.rainDelayStartSupported()) { // // rainDelayStart channel only when supported
            toRemove.addAll(getChannelUIDs(GROUP_RAIN, Set.of(CHANNEL_RAIN_STATE, CHANNEL_RAIN_COUNTER)));
        }

        if (!mower.multiZoneSupported()) { // multizone channels only when supported
            toRemove.add(getChannelUID(GROUP_MULTI_ZONES, CHANNEL_LAST_ZONE));

            // remove zone meter channels
            IntStream.range(0, mower.getMultiZoneCount())
                    .forEach(index -> toRemove.add(getChannelUID(GROUP_MULTI_ZONES, "zone-%d".formatted(index + 1))));
            // remove allocation channels
            IntStream.range(0, 10).forEach(index -> toRemove
                    .add(getChannelUID(GROUP_MULTI_ZONES, "%s-%d".formatted(CHANNEL_PREFIX_ALLOCATION, index))));
        }

        if (!mower.oneTimeSchedulerSupported()) { // oneTimeScheduler channel only when supported
            toRemove.addAll(getChannelUIDs(GROUP_ONE_TIME, Set.of(CHANNEL_DURATION, CHANNEL_EDGECUT, CHANNEL_MODE)));
        }

        if (!mower.scheduler2Supported()) { // Scheduler 2 channels only when supported version
            EnumSet.allOf(WorxLandroidDayCodes.class).stream()
                    .map(dayCode -> "%s2".formatted(dayCode.getDescription().toLowerCase()))
                    .forEach(groupName -> toRemove.addAll(getChannelUIDs(groupName,
                            Set.of(CHANNEL_ENABLE, CHANNEL_DURATION, CHANNEL_EDGECUT, CHANNEL_TIME))));
        }

        toRemove.stream().forEach(thingBuilder::withoutChannel);
        updateThing(thingBuilder.build());

        updateProperties(Map.of(Thing.PROPERTY_MAC_ADDRESS, mower.getMacAddress(), Thing.PROPERTY_VENDOR, "Worx",
                "productId", mower.getId(), "language", mower.getLanguage(), "mqtt_endpoint", endpoint));
    }

    private void processStatusMessage(Mower mower) {
        updateStateCfg(mower);
        updateStateDat(mower);
        thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, mower.getFirmwareVersion());
    }

    /**
     * Start scheduled jobs.
     * Jobs are only started if interval > 0
     */
    private void startScheduledJobs(WorxLandroidBridgeHandler bridgeHandler, Mower theMower,
            MowerConfiguration config) {
        if (config.refreshStatusInterval > 0) {
            refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(() -> {
                try {
                    ProductItemStatus product = bridgeHandler.retrieveDeviceStatus(config.serialNumber);
                    updateChannelDateTime(GROUP_COMMON, CHANNEL_ONLINE_TIMESTAMP, ZonedDateTime.now());
                    updateChannelOnOff(GROUP_COMMON, CHANNEL_ONLINE, product != null && product.online);
                    updateStatus(product != null ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
                } catch (WebApiException e) {
                    logger.debug("Refreshing Thing {} failed, handler might be OFFLINE", config.serialNumber);
                }
            }, 3, config.refreshStatusInterval, TimeUnit.SECONDS));
        }

        if (config.pollingInterval > 0) {
            pollingJob = Optional.of(scheduler.scheduleWithFixedDelay(() -> sendCommand(theMower, EMPTY_PAYLOAD), 5,
                    config.pollingInterval, TimeUnit.SECONDS));
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        WorxLandroidBridgeHandler bridgeHandler = getBridgeHandler(getBridge(), WorxLandroidBridgeHandler.class);
        if (bridgeHandler != null) {
            initializeData(bridgeHandler);
        }
    }

    @Override
    protected void internalHandleCommand(@Nullable String groupId, String channelId, Command command) {
        mower.ifPresent(theMower -> {
            if (GROUP_MULTI_ZONES.equals(groupId)) {
                handleMultiZonesCommand(theMower, channelId, command);
            } else if (GROUP_AWS.equals(groupId)) {
                handleAWSCommand(theMower, channelId);
            } else if (GROUP_SCHEDULE.equals(groupId)) {
                handleScheduleCommand(theMower, channelId, Integer.parseInt(command.toString()));
            } else if (GROUP_ONE_TIME.equals(groupId)) {
                handleOneTimeSchedule(theMower, channelId, command);
            } else if (GROUP_COMMON.equals(groupId)) {
                handleCommonGroup(theMower, channelId, command);
            } else if (groupId != null && groupId.contains("day")) {
                setScheduledDays(theMower, groupId, channelId, command);
                sendCommand(theMower,
                        theMower.scheduler2Supported()
                                ? new ScheduleDaysCommand(theMower.getTimeExtension(), theMower.getSheduleArray1(),
                                        theMower.getSheduleArray2())
                                : new ScheduleDaysCommand(theMower.getTimeExtension(), theMower.getSheduleArray1()));
            } else if (CHANNEL_DELAY.equals(channelId)) {
                int delaySec = commandToInt(command, Units.SECOND);
                sendCommand(theMower, new SetRainDelay(delaySec));
            } else if (CHANNEL_BLADE_TIME.equals(channelId) || CHANNEL_CHARGE_CYCLES.equals(channelId)) {
                resetStat(channelId, theMower.getSerialNumber());
            } else {
                logger.debug("command for channel {} not supported: {}", channelId, command);
            }
        });
    }

    private void handleAWSCommand(Mower theMower, String channel) {
        if (CHANNEL_POLL.equals(channel)) {
            sendCommand(theMower, EMPTY_PAYLOAD);
            updateState(CHANNEL_POLL, OnOffType.OFF);
        } else {
            logger.warn("No action identified on channel {}", channel);
        }
    }

    private void handleCommonGroup(Mower theMower, String channel, Command command) {
        if (CHANNEL_ACTION.equals(channel)) {
            WorxLandroidActionCodes actionCode = WorxLandroidActionCodes.valueOf(command.toString());
            sendCommand(theMower, new MowerCommand(actionCode));
        } else if (CHANNEL_LOCK.equals(channel)) {
            WorxLandroidActionCodes lockCode = OnOffType.ON.equals(command) ? WorxLandroidActionCodes.LOCK
                    : WorxLandroidActionCodes.UNLOCK;
            sendCommand(theMower, new MowerCommand(lockCode));
        } else if (CHANNEL_ENABLE.equals(channel)) {
            theMower.setEnable(OnOffType.ON.equals(command));
            sendCommand(theMower,
                    theMower.scheduler2Supported()
                            ? new ScheduleDaysCommand(theMower.getTimeExtension(), theMower.getSheduleArray1(),
                                    theMower.getSheduleArray2())
                            : new ScheduleDaysCommand(theMower.getTimeExtension(), theMower.getSheduleArray1()));
        } else {
            logger.warn("No action identified for command {} on channel {}", command, channel);
        }
    }

    private void handleOneTimeSchedule(Mower theMower, String channel, Command command) {
        if (CHANNEL_DURATION.equals(channel)) {
            sendCommand(theMower, new OneTimeCommand(0, Integer.parseInt(command.toString())));
        } else if (CHANNEL_EDGECUT.equals(channel)) {
            sendCommand(theMower, new OneTimeCommand(OnOffType.ON.equals(command) ? 1 : 0, 0));
        } else {
            logger.warn("No action identified for command {} on channel {}", command, channel);
        }
    }

    private void handleScheduleCommand(Mower theMower, String channel, int command) {
        if (CHANNEL_MODE.equals(channel)) {
            sendCommand(theMower, new ScheduleCommand(command));
        } else if (CHANNEL_TIME_EXTENSION.equals(channel)) {
            theMower.setTimeExtension(command);
            sendCommand(theMower,
                    theMower.scheduler2Supported()
                            ? new ScheduleDaysCommand(theMower.getTimeExtension(), theMower.getSheduleArray1(),
                                    theMower.getSheduleArray2())
                            : new ScheduleDaysCommand(theMower.getTimeExtension(), theMower.getSheduleArray1()));
        } else {
            logger.warn("No action identified for command {} on channel {}", command, channel);
        }
    }

    private void handleMultiZonesCommand(Mower theMower, String channel, Command command) {
        if (CHANNEL_ENABLE.equals(channel)) {
            theMower.setMultiZoneEnable(OnOffType.ON.equals(command));
            sendCommand(theMower, new ZoneMeterCommand(theMower.getZoneMeters()));
        } else if (CHANNEL_LAST_ZONE.equals(channel)) {
            if (!WorxLandroidStatusCodes.HOME.equals(theMower.getStatusCode())) {
                logger.warn("Cannot start zone because mower must be at HOME!");
                return;
            }

            theMower.setZoneTo(Integer.parseInt(command.toString()));
            sendCommand(theMower, new ZoneMeterCommand(theMower.getZoneMeters()));
            scheduler.schedule(() -> sendCommand(theMower, new MowerCommand(WorxLandroidActionCodes.START)), 2000,
                    TimeUnit.MILLISECONDS);
        } else {
            String[] names = channel.split("-");
            int index = Integer.valueOf(names[1]);

            if (CHANNEL_PREFIX_ZONE.startsWith(names[0])) {
                int meterValue = commandToInt(command, SIUnits.METRE);
                theMower.setZoneMeter(index - 1, meterValue);
                sendCommand(theMower, new ZoneMeterCommand(theMower.getZoneMeters()));
            } else if (CHANNEL_PREFIX_ALLOCATION.startsWith(names[0])) {
                theMower.setAllocation(index, Integer.parseInt(command.toString()));
                sendCommand(theMower, new ZoneMeterAlloc(theMower.getAllocations()));
            } else {
                logger.warn("No action identified for command {} on channel {}", command, channel);
            }
        }
    }

    private int commandToInt(Command command, @Nullable Unit<?> targetUnit) {
        if (command instanceof QuantityType<?> qtty && targetUnit != null) {
            QuantityType<?> inTarget = qtty.toUnit(targetUnit);
            if (inTarget != null) {
                return inTarget.intValue();
            }
        }
        return Integer.parseInt(command.toString());
    }

    /**
     * Set scheduled days
     *
     * @param theMower
     *
     * @param scDaysIndex 1 or 2
     * @param channelUID
     * @param command
     */
    private void setScheduledDays(Mower theMower, String groupId, String channelId, Command command) {
        int scDaysSlot = groupId.endsWith("2") ? 2 : 1;
        WorxLandroidDayCodes dayCodeUpdated = WorxLandroidDayCodes.valueOf(groupId.replace("2", "").toUpperCase());

        ScheduledDay scheduledDayUpdated = theMower.getScheduledDay(scDaysSlot, dayCodeUpdated);
        if (scheduledDayUpdated == null) {
            return;
        }

        if (CHANNEL_ENABLE.equals(channelId)) {
            scheduledDayUpdated.setEnable(OnOffType.ON.equals(command));
        } else if (CHANNEL_TIME.equals(channelId)) {
            if (command instanceof DateTimeType dateTime) {
                ZonedDateTime zdt = dateTime.getZonedDateTime();
                scheduledDayUpdated.setStartTime(zdt);
            } else if (command instanceof StringType stringType) {
                scheduledDayUpdated.setStartTime(stringType.toString());
            } else {
                logger.warn("Incorrect command {} on channel {}:{} ", command, groupId, channelId);
            }
        } else if (CHANNEL_DURATION.equals(channelId)) {
            scheduledDayUpdated.setDuration(Integer.parseInt(command.toString()));
        } else if (CHANNEL_EDGECUT.equals(channelId)) {
            scheduledDayUpdated.setEdgecut(OnOffType.ON.equals(command));
        }
    }

    private void sendCommand(Mower theMower, Object command) {
        logger.debug("send command: {}", command);
        publishMessage(theMower.getMqttCommandIn(), command);
    }

    /**
     * Update states for data values
     *
     * @param theMower
     *
     * @param dat
     */
    private void updateStateDat(Mower theMower) {
        updateChannelQuantity(GROUP_ORIENTATION, CHANNEL_PITCH, theMower.getAngle(Axis.PITCH), Units.DEGREE_ANGLE);
        updateChannelQuantity(GROUP_ORIENTATION, CHANNEL_ROLL, theMower.getAngle(Axis.ROLL), Units.DEGREE_ANGLE);
        updateChannelQuantity(GROUP_ORIENTATION, CHANNEL_YAW, theMower.getAngle(Axis.YAW), Units.DEGREE_ANGLE);
        updateChannelEnum(GROUP_COMMON, CHANNEL_STATUS_CODE, theMower.getPayloadDat().statusCode);
        updateChannelEnum(GROUP_COMMON, CHANNEL_ERROR_CODE, theMower.getPayloadDat().errorCode);
        updateChannelDecimal(GROUP_MULTI_ZONES, CHANNEL_LAST_ZONE, theMower.getLastZone());

        updateChannelDecimal(GROUP_BATTERY, CHANNEL_CHARGE_CYCLES, theMower.getCurrentChargeCycles());
        updateChannelDecimal(GROUP_BATTERY, CHANNEL_CHARGE_CYCLES_TOTAL, theMower.getTotalChargeCycles());
        updateChannelQuantity(GROUP_METRICS, CHANNEL_BLADE_TIME, theMower.getCurrentBladeTime(), Units.MINUTE);
        updateChannelQuantity(GROUP_METRICS, CHANNEL_BLADE_TIME_TOTAL, theMower.getTotalBladeTime(), Units.MINUTE);

        theMower.getBattery().ifPresent(battery -> {
            updateChannelQuantity(GROUP_BATTERY, CHANNEL_TEMPERATURE, battery.temp != -1 ? battery.temp : null,
                    SIUnits.CELSIUS);
            updateChannelQuantity(GROUP_BATTERY, CHANNEL_VOLTAGE, battery.voltage != -1 ? battery.voltage : null,
                    Units.VOLT);
            updateChannelDecimal(GROUP_BATTERY, CHANNEL_LEVEL, battery.level);
            updateChannelOnOff(GROUP_BATTERY, CHANNEL_CHARGING, battery.charging);
        });

        theMower.getStats().ifPresent(stats -> {
            updateChannelQuantity(GROUP_METRICS, CHANNEL_DISTANCE,
                    stats.distanceCovered != -1 ? stats.distanceCovered : null, SIUnits.METRE);
            updateChannelQuantity(GROUP_METRICS, CHANNEL_TOTAL_TIME,
                    stats.mowerWorkTime != -1 ? stats.mowerWorkTime : null, Units.MINUTE);
        });

        int rssi = theMower.getPayloadDat().wifiQuality;
        updateChannelDecimal(GROUP_WIFI, CHANNEL_WIFI_QUALITY, rssi <= 0 ? toQoS(rssi) : null);
        updateChannelQuantity(GROUP_WIFI, CHANNEL_RSSI,
                rssi <= 0 ? new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS) : null);

        if (theMower.lockSupported()) {
            updateChannelOnOff(GROUP_COMMON, CHANNEL_LOCK, theMower.getPayloadDat().isLocked());
        }

        theMower.getRain().ifPresent(rain -> {
            if (theMower.rainDelayStartSupported()) {
                updateChannelOnOff(GROUP_RAIN, CHANNEL_RAIN_STATE, rain.raining);
                updateChannelQuantity(GROUP_RAIN, CHANNEL_RAIN_COUNTER, rain.counter, Units.MINUTE);
            }
        });
    }

    /**
     * Update states for cfg values
     *
     * @param theMower
     *
     * @param cfg
     * @param zoneId
     */
    private void updateStateCfg(Mower theMower) {
        updateChannelDateTime(GROUP_CONFIG, CHANNEL_TIMESTAMP, theMower.getLastUpdate());

        theMower.getOneTimeSchedule().ifPresent(ots -> {
            updateChannelOnOff(GROUP_ONE_TIME, CHANNEL_EDGECUT, ots.getEdgeCut());
            updateChannelQuantity(GROUP_ONE_TIME, CHANNEL_DURATION, ots.duration != -1 ? ots.duration : null,
                    Units.MINUTE);
        });

        theMower.getSchedule().ifPresent(schedule -> {
            if (theMower.oneTimeSchedulerSupported()) {
                updateChannelEnum(GROUP_SCHEDULE, CHANNEL_MODE, schedule.scheduleMode);
            }

            if (schedule.timeExtension != -1) {
                updateChannelQuantity(GROUP_SCHEDULE, CHANNEL_TIME_EXTENSION, schedule.timeExtension, Units.PERCENT);
                updateChannelOnOff(GROUP_COMMON, CHANNEL_ENABLE, theMower.isEnable());
            }

            if (schedule.d != null) {
                updateStateCfgScDays(theMower, 1, schedule.d);
                if (schedule.dd != null) {
                    updateStateCfgScDays(theMower, 2, schedule.dd);
                }
            }
        });

        // What is this ???
        int command = theMower.getPayloadCfg().cmd;
        updateChannelDecimal(GROUP_CONFIG, CHANNEL_COMMAND, command != -1 ? command : null);

        if (theMower.multiZoneSupported()) {
            for (int zoneIndex = 0; zoneIndex < theMower.getZonesSize(); zoneIndex++) {
                updateChannelQuantity(GROUP_MULTI_ZONES, CHANNEL_PREFIX_ZONE.formatted(zoneIndex + 1),
                        theMower.getZoneMeter(zoneIndex), SIUnits.METRE);
            }

            for (int allocationIndex = 0; allocationIndex < theMower.getAllocationsSize(); allocationIndex++) {
                updateChannelDecimal(GROUP_MULTI_ZONES, CHANNEL_PREFIX_ALLOCATION.formatted(allocationIndex),
                        theMower.getAllocation(allocationIndex));
            }
            updateChannelOnOff(GROUP_MULTI_ZONES, CHANNEL_ENABLE, theMower.isMultiZoneEnable());
        }

        int rainDelay = theMower.getPayloadCfg().rainDelay;
        updateChannelQuantity(GROUP_RAIN, CHANNEL_DELAY,
                theMower.rainDelaySupported() && rainDelay != -1 ? rainDelay : null, Units.MINUTE);
    }

    /**
     * @param theMower
     * @param scDSlot scheduled day slot
     * @param d scheduled day
     */
    private void updateStateCfgScDays(Mower theMower, int scDSlot, List<List<String>> d) {
        List<ZonedDateTime> nextStarts = new ArrayList<>();
        List<ZonedDateTime> nextEnds = new ArrayList<>();

        for (WorxLandroidDayCodes dayCode : WorxLandroidDayCodes.values()) {
            ScheduledDay scheduledDay = theMower.getScheduledDay(scDSlot, dayCode);
            if (scheduledDay == null) {
                return;
            }

            String groupName = "%s%s".formatted(dayCode.getDescription().toLowerCase(),
                    scDSlot == 1 ? "" : String.valueOf(scDSlot));

            updateChannelOnOff(groupName, CHANNEL_ENABLE, scheduledDay.isEnabled());
            updateChannelOnOff(groupName, CHANNEL_EDGECUT, scheduledDay.isEdgecut());
            updateChannelQuantity(groupName, CHANNEL_DURATION, scheduledDay.getDuration(), Units.MINUTE);

            if (scheduledDay.isEnabled()) {
                ZonedDateTime scheduleStart = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                        .with(scheduledDay.getStartTime());
                scheduleStart = ZonedDateTime.from(dayCode.dayOfWeek.adjustInto(scheduleStart));
                updateChannelDateTime(groupName, CHANNEL_TIME, scheduleStart);
                ZonedDateTime scheduleEnd = scheduleStart.plusMinutes(scheduledDay.getDuration());
                if (scheduleStart.isBefore(ZonedDateTime.now())) {
                    scheduleStart = scheduleStart.plusDays(7);
                }
                if (scheduleEnd.isBefore(ZonedDateTime.now())) {
                    scheduleEnd = scheduleEnd.plusDays(7);
                }

                nextStarts.add(scheduleStart);
                nextEnds.add(scheduleEnd);
            }
        }
        if (!nextStarts.isEmpty()) {
            Collections.sort(nextStarts);
            Collections.sort(nextEnds);
            updateChannelDateTime(GROUP_SCHEDULE, CHANNEL_START, nextStarts.get(0));
            updateChannelDateTime(GROUP_SCHEDULE, CHANNEL_STOP, nextEnds.get(0));
        }
    }

    private int toQoS(int rssi) {
        return rssi > -50 ? 4 : rssi > -60 ? 3 : rssi > -70 ? 2 : rssi > -85 ? 1 : 0;
    }

    public void publishMessage(String topic, Object command) {
        publishMessage(topic, deserializer.toJson(command));
    }

    @Override
    protected void internalHandlePayload(Payload payload) {
        mower.ifPresent(theMower -> {
            theMower.setStatus(payload);
            updateStateCfg(theMower);
            updateStateDat(theMower);
        });
    }

    private boolean resetStat(String channelId, String serialNumber) {
        WorxLandroidBridgeHandler bridgeHandler = getBridgeHandler(getBridge(), WorxLandroidBridgeHandler.class);
        if (bridgeHandler != null) {
            logger.debug("Resetting {}", channelId);
            if (CHANNEL_BLADE_TIME.equals(channelId)) {
                bridgeHandler.resetBladeTime(serialNumber);
            } else {
                bridgeHandler.resetBatteryCycles(serialNumber);
            }
        }
        return true;
    }
}
