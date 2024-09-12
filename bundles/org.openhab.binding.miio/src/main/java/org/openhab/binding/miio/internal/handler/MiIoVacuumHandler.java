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
package org.openhab.binding.miio.internal.handler;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.CloudUtil;
import org.openhab.binding.miio.internal.cloud.HomeRoomDTO;
import org.openhab.binding.miio.internal.cloud.MiCloudException;
import org.openhab.binding.miio.internal.robot.ConsumablesType;
import org.openhab.binding.miio.internal.robot.DockStatusType;
import org.openhab.binding.miio.internal.robot.FanModeType;
import org.openhab.binding.miio.internal.robot.HistoryRecordDTO;
import org.openhab.binding.miio.internal.robot.RRMapDraw;
import org.openhab.binding.miio.internal.robot.RRMapDrawOptions;
import org.openhab.binding.miio.internal.robot.RobotCababilities;
import org.openhab.binding.miio.internal.robot.StatusDTO;
import org.openhab.binding.miio.internal.robot.StatusType;
import org.openhab.binding.miio.internal.robot.VacuumErrorType;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link MiIoVacuumHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoVacuumHandler extends MiIoAbstractHandler {
    private final Logger logger = LoggerFactory.getLogger(MiIoVacuumHandler.class);
    private static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter PARSER_TZ = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final ChannelUID mapChannelUid;

    private static final Set<RobotCababilities> FEATURES_CHANNELS = Collections.unmodifiableSet(Stream.of(
            RobotCababilities.SEGMENT_STATUS, RobotCababilities.MAP_STATUS, RobotCababilities.LED_STATUS,
            RobotCababilities.CARPET_MODE, RobotCababilities.FW_FEATURES, RobotCababilities.ROOM_MAPPING,
            RobotCababilities.MULTI_MAP_LIST, RobotCababilities.CUSTOMIZE_CLEAN_MODE, RobotCababilities.COLLECT_DUST,
            RobotCababilities.CLEAN_MOP_START, RobotCababilities.CLEAN_MOP_STOP, RobotCababilities.MOP_DRYING,
            RobotCababilities.MOP_DRYING_REMAINING_TIME, RobotCababilities.DOCK_STATE_ID).collect(Collectors.toSet()));

    private ExpiringCache<String> status;
    private ExpiringCache<String> consumables;
    private ExpiringCache<String> dnd;
    private ExpiringCache<String> history;
    private int stateId;
    private ExpiringCache<String> map;
    private String lastHistoryId = "";
    private String lastMap = "";
    private boolean hasChannelStructure;
    private ConcurrentHashMap<RobotCababilities, Boolean> deviceCapabilities = new ConcurrentHashMap<>();
    private ChannelTypeRegistry channelTypeRegistry;
    private RRMapDrawOptions mapDrawOptions = new RRMapDrawOptions();

    public MiIoVacuumHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
            CloudConnector cloudConnector, ChannelTypeRegistry channelTypeRegistry, TranslationProvider i18nProvider,
            LocaleProvider localeProvider) {
        super(thing, miIoDatabaseWatchService, cloudConnector, i18nProvider, localeProvider);
        this.channelTypeRegistry = channelTypeRegistry;
        mapChannelUid = new ChannelUID(thing.getUID(), CHANNEL_VACUUM_MAP);
        status = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.GET_STATUS);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during status refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        consumables = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.CONSUMABLES_GET);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during consumables refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        dnd = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.DND_GET);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during dnd refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        history = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.CLEAN_SUMMARY_GET);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during cleaning data refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        map = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.GET_MAP);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during dnd refresh: {}", e.getMessage(), e);
            }
            return null;
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getConnection() == null) {
            logger.debug("Vacuum {} not online. Command {} ignored", getThing().getUID(), command.toString());
            return;
        }
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
            lastMap = "";
            if (channelUID.getId().equals(CHANNEL_VACUUM_MAP)) {
                sendCommand(MiIoCommand.GET_MAP);
            }
            return;
        }
        if (handleCommandsChannels(channelUID, command)) {
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_VACUUM)) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    sendCommand(MiIoCommand.START_VACUUM);
                    forceStatusUpdate();
                    return;
                } else {
                    sendCommand(MiIoCommand.STOP_VACUUM);
                    miIoScheduler.schedule(() -> {
                        sendCommand(MiIoCommand.CHARGE);
                        forceStatusUpdate();
                    }, 2000, TimeUnit.MILLISECONDS);
                    return;
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_CONTROL)) {
            if ("vacuum".equals(command.toString())) {
                sendCommand(MiIoCommand.START_VACUUM);
            } else if ("spot".equals(command.toString())) {
                sendCommand(MiIoCommand.START_SPOT);
            } else if ("pause".equals(command.toString())) {
                sendCommand(MiIoCommand.PAUSE);
            } else if ("dock".equals(command.toString())) {
                sendCommand(MiIoCommand.STOP_VACUUM);
                miIoScheduler.schedule(() -> {
                    sendCommand(MiIoCommand.CHARGE);
                    forceStatusUpdate();
                }, 2000, TimeUnit.MILLISECONDS);
                return;
            } else {
                logger.info("Command {} not recognised", command.toString());
            }
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
            sendCommand(MiIoCommand.SET_MODE, "[" + command.toString() + "]");
            forceStatusUpdate();
            return;
        }

        if (channelUID.getId().equals(RobotCababilities.WATERBOX_MODE.getChannel())) {
            sendCommand(MiIoCommand.SET_WATERBOX_MODE, "[" + command.toString() + "]");
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(RobotCababilities.MOP_MODE.getChannel())) {
            sendCommand(MiIoCommand.SET_MOP_MODE, "[" + command.toString() + "]");
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(RobotCababilities.SEGMENT_CLEAN.getChannel()) && !command.toString().isEmpty()
                && !command.toString().contentEquals("-")) {
            sendCommand(MiIoCommand.START_SEGMENT, "[" + command.toString() + "]");
            updateState(RobotCababilities.SEGMENT_CLEAN.getChannel(), new StringType("-"));
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_CONTROL)) {
            if (Integer.valueOf(command.toString()) > 0) {
                sendCommand(MiIoCommand.SET_MODE, "[" + command.toString() + "]");
            }
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_CONSUMABLE_RESET)) {
            sendCommand(MiIoCommand.CONSUMABLES_RESET, "[" + command.toString() + "]");
            updateState(CHANNEL_CONSUMABLE_RESET, new StringType("none"));
        }

        if (channelUID.getId().equals(RobotCababilities.COLLECT_DUST.getChannel()) && !command.toString().isEmpty()
                && !command.toString().contentEquals("-")) {
            sendCommand(MiIoCommand.SET_COLLECT_DUST);
            forceStatusUpdate();
            return;
        }

        if (channelUID.getId().equals(RobotCababilities.CLEAN_MOP_START.getChannel()) && !command.toString().isEmpty()
                && !command.toString().contentEquals("-")) {
            sendCommand(MiIoCommand.SET_CLEAN_MOP_START);
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(RobotCababilities.CLEAN_MOP_STOP.getChannel()) && !command.toString().isEmpty()
                && !command.toString().contentEquals("-")) {
            sendCommand(MiIoCommand.SET_CLEAN_MOP_STOP);
            forceStatusUpdate();
            return;
        }
    }

    private void forceStatusUpdate() {
        status.invalidateValue();
        miIoScheduler.schedule(() -> {
            status.getValue();
        }, 3000, TimeUnit.MILLISECONDS);
    }

    private void safeUpdateState(String channelID, @Nullable Integer state) {
        if (state != null) {
            updateState(channelID, new DecimalType(state));
        } else {
            logger.debug("Channel {} not update. value not available.", channelID);
        }
    }

    private boolean updateVacuumStatus(JsonObject statusData) {
        StatusDTO statusInfo = GSON.fromJson(statusData, StatusDTO.class);
        if (statusInfo == null) {
            return false;
        }
        safeUpdateState(CHANNEL_BATTERY, statusInfo.getBattery());
        if (statusInfo.getCleanArea() != null) {
            updateState(CHANNEL_CLEAN_AREA,
                    new QuantityType<>(statusInfo.getCleanArea() / 1000000.0, SIUnits.SQUARE_METRE));
        }
        if (statusInfo.getCleanTime() != null) {
            updateState(CHANNEL_CLEAN_TIME,
                    new QuantityType<>(TimeUnit.SECONDS.toMinutes(statusInfo.getCleanTime()), Units.MINUTE));
        }
        safeUpdateState(CHANNEL_DND_ENABLED, statusInfo.getDndEnabled());

        if (statusInfo.getErrorCode() != null) {
            updateState(CHANNEL_ERROR_CODE,
                    new StringType(VacuumErrorType.getType(statusInfo.getErrorCode()).getDescription()));
            safeUpdateState(CHANNEL_ERROR_ID, statusInfo.getErrorCode());
        }

        if (statusInfo.getFanPower() != null) {
            updateState(CHANNEL_FAN_POWER, new DecimalType(statusInfo.getFanPower()));
            updateState(CHANNEL_FAN_CONTROL, new DecimalType(FanModeType.getType(statusInfo.getFanPower()).getId()));
        }
        safeUpdateState(CHANNEL_IN_CLEANING, statusInfo.getInCleaning());
        safeUpdateState(CHANNEL_MAP_PRESENT, statusInfo.getMapPresent());
        if (statusInfo.getState() != null) {
            stateId = statusInfo.getState();
            StatusType state = StatusType.getType(statusInfo.getState());
            updateState(CHANNEL_STATE, new StringType(state.getDescription()));
            updateState(CHANNEL_STATE_ID, new DecimalType(statusInfo.getState()));

            State vacuum = OnOffType.OFF;
            String control;
            switch (state) {
                case ZONE:
                case ROOM:
                case CLEANING:
                case RETURNING:
                    control = "vacuum";
                    vacuum = OnOffType.ON;
                    break;
                case CHARGING:
                case CHARGING_ERROR:
                case DOCKING:
                case FULL:
                    control = "dock";
                    break;
                case SLEEPING:
                case PAUSED:
                case IDLE:
                    control = "pause";
                    break;
                case SPOTCLEAN:
                    control = "spot";
                    vacuum = OnOffType.ON;
                    break;
                default:
                    control = "undef";
                    break;
            }
            if ("undef".equals(control)) {
                updateState(CHANNEL_CONTROL, UnDefType.UNDEF);
            } else {
                updateState(CHANNEL_CONTROL, new StringType(control));
            }
            updateState(CHANNEL_VACUUM, vacuum);
        }
        if (this.deviceCapabilities.containsKey(RobotCababilities.DOCK_STATE_ID)) {
            DockStatusType state = DockStatusType.getType(statusInfo.getDockErrorStatus().intValue());
            updateState(CHANNEL_DOCK_STATE, new StringType(state.getDescription()));
            updateState(CHANNEL_DOCK_STATE_ID, new DecimalType(state.getId()));
        }
        if (deviceCapabilities.containsKey(RobotCababilities.WATERBOX_MODE)) {
            safeUpdateState(RobotCababilities.WATERBOX_MODE.getChannel(), statusInfo.getWaterBoxMode());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.MOP_MODE)) {
            safeUpdateState(RobotCababilities.MOP_MODE.getChannel(), statusInfo.getMopMode());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.WATERBOX_STATUS)) {
            safeUpdateState(RobotCababilities.WATERBOX_STATUS.getChannel(), statusInfo.getWaterBoxStatus());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.WATERBOX_CARRIAGE)) {
            safeUpdateState(RobotCababilities.WATERBOX_CARRIAGE.getChannel(), statusInfo.getWaterBoxCarriageStatus());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.LOCKSTATUS)) {
            safeUpdateState(RobotCababilities.LOCKSTATUS.getChannel(), statusInfo.getLockStatus());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.MOP_FORBIDDEN)) {
            safeUpdateState(RobotCababilities.MOP_FORBIDDEN.getChannel(), statusInfo.getMopForbiddenEnable());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.LOCATING)) {
            safeUpdateState(RobotCababilities.LOCATING.getChannel(), statusInfo.getIsLocating());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.CLEAN_MOP_START)) {
            safeUpdateState(RobotCababilities.CLEAN_MOP_START.getChannel(), 0);
        }
        if (deviceCapabilities.containsKey(RobotCababilities.CLEAN_MOP_STOP)) {
            safeUpdateState(RobotCababilities.CLEAN_MOP_STOP.getChannel(), 0);
        }
        if (deviceCapabilities.containsKey(RobotCababilities.COLLECT_DUST)) {
            safeUpdateState(RobotCababilities.COLLECT_DUST.getChannel(), 0);
        }
        if (deviceCapabilities.containsKey(RobotCababilities.MOP_DRYING)) {
            safeUpdateState(RobotCababilities.MOP_DRYING.getChannel(), statusInfo.getIsMopDryingActive());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.MOP_DRYING_REMAINING_TIME)) {
            updateState(CHANNEL_MOP_TOTALDRYTIME,
                    new QuantityType<>(TimeUnit.SECONDS.toMinutes(statusInfo.getMopDryTime()), Units.MINUTE));
        }
        return true;
    }

    private boolean updateConsumables(JsonObject consumablesData) {
        int mainBrush = consumablesData.get("main_brush_work_time").getAsInt();
        int sideBrush = consumablesData.get("side_brush_work_time").getAsInt();
        int filter = consumablesData.get("filter_work_time").getAsInt();
        int sensor = consumablesData.get("sensor_dirty_time").getAsInt();
        updateState(CHANNEL_CONSUMABLE_MAIN_TIME,
                new QuantityType<>(ConsumablesType.remainingHours(mainBrush, ConsumablesType.MAIN_BRUSH), Units.HOUR));
        updateState(CHANNEL_CONSUMABLE_MAIN_PERC,
                new DecimalType(ConsumablesType.remainingPercent(mainBrush, ConsumablesType.MAIN_BRUSH)));
        updateState(CHANNEL_CONSUMABLE_SIDE_TIME,
                new QuantityType<>(ConsumablesType.remainingHours(sideBrush, ConsumablesType.SIDE_BRUSH), Units.HOUR));
        updateState(CHANNEL_CONSUMABLE_SIDE_PERC,
                new DecimalType(ConsumablesType.remainingPercent(sideBrush, ConsumablesType.SIDE_BRUSH)));
        updateState(CHANNEL_CONSUMABLE_FILTER_TIME,
                new QuantityType<>(ConsumablesType.remainingHours(filter, ConsumablesType.FILTER), Units.HOUR));
        updateState(CHANNEL_CONSUMABLE_FILTER_PERC,
                new DecimalType(ConsumablesType.remainingPercent(filter, ConsumablesType.FILTER)));
        updateState(CHANNEL_CONSUMABLE_SENSOR_TIME,
                new QuantityType<>(ConsumablesType.remainingHours(sensor, ConsumablesType.SENSOR), Units.HOUR));
        updateState(CHANNEL_CONSUMABLE_SENSOR_PERC,
                new DecimalType(ConsumablesType.remainingPercent(sensor, ConsumablesType.SENSOR)));
        return true;
    }

    private boolean updateDnD(JsonObject dndData) {
        logger.trace("Do not disturb data: {}", dndData.toString());
        updateState(CHANNEL_DND_FUNCTION, new DecimalType(dndData.get("enabled").getAsBigDecimal()));
        updateState(CHANNEL_DND_START, new StringType(String.format("%02d:%02d", dndData.get("start_hour").getAsInt(),
                dndData.get("start_minute").getAsInt())));
        updateState(CHANNEL_DND_END, new StringType(
                String.format("%02d:%02d", dndData.get("end_hour").getAsInt(), dndData.get("end_minute").getAsInt())));
        return true;
    }

    private boolean updateHistoryLegacy(JsonArray historyData) {
        logger.trace("Cleaning history data: {}", historyData.toString());
        updateState(CHANNEL_HISTORY_TOTALTIME,
                new QuantityType<>(TimeUnit.SECONDS.toMinutes(historyData.get(0).getAsLong()), Units.MINUTE));
        updateState(CHANNEL_HISTORY_TOTALAREA,
                new QuantityType<>(historyData.get(1).getAsDouble() / 1000000D, SIUnits.SQUARE_METRE));
        updateState(CHANNEL_HISTORY_COUNT, new DecimalType(historyData.get(2).toString()));
        if (historyData.get(3).getAsJsonArray().size() > 0) {
            String lastClean = historyData.get(3).getAsJsonArray().get(0).getAsString();
            if (!lastClean.equals(lastHistoryId)) {
                lastHistoryId = lastClean;
                sendCommand(MiIoCommand.CLEAN_RECORD_GET, "[" + lastClean + "]");
            }
        }
        return true;
    }

    private boolean updateHistory(JsonObject historyData) {
        logger.trace("Cleaning history data: {}", historyData);
        if (historyData.has("clean_time")) {
            updateState(CHANNEL_HISTORY_TOTALTIME, new QuantityType<>(
                    TimeUnit.SECONDS.toMinutes(historyData.get("clean_time").getAsLong()), Units.MINUTE));
        }
        if (historyData.has("clean_area")) {
            updateState(CHANNEL_HISTORY_TOTALAREA,
                    new QuantityType<>(historyData.get("clean_area").getAsDouble() / 1000000D, SIUnits.SQUARE_METRE));
        }
        if (historyData.has("clean_count")) {
            updateState(CHANNEL_HISTORY_COUNT, new DecimalType(historyData.get("clean_count").getAsLong()));
        }
        if (historyData.has("records") & historyData.get("records").isJsonArray()) {
            JsonArray historyRecords = historyData.get("records").getAsJsonArray();
            if (!historyRecords.isEmpty()) {
                String lastClean = historyRecords.get(0).getAsString();
                if (!lastClean.equals(lastHistoryId)) {
                    lastHistoryId = lastClean;
                    sendCommand(MiIoCommand.CLEAN_RECORD_GET, "[" + lastClean + "]");
                }
            }
        }
        return true;
    }

    private void updateHistoryRecordLegacy(JsonArray historyData) {
        HistoryRecordDTO historyRecord = new HistoryRecordDTO();

        for (int i = 0; i < historyData.size(); ++i) {
            try {
                BigInteger value = historyData.get(i).getAsBigInteger();
                switch (i) {
                    case 0:
                        historyRecord.setStart(ZonedDateTime
                                .ofInstant(Instant.ofEpochSecond(value.longValue()), ZoneId.systemDefault())
                                .format(PARSER_TZ));
                        break;
                    case 1:
                        historyRecord.setEnd(ZonedDateTime
                                .ofInstant(Instant.ofEpochSecond(value.longValue()), ZoneId.systemDefault())
                                .format(PARSER_TZ));
                        break;
                    case 2:
                        historyRecord.setDuration(value.intValue());
                        break;
                    case 3:
                        historyRecord.setArea(new BigDecimal(value).divide(BigDecimal.valueOf(1000000)));
                        break;
                    case 4:
                        historyRecord.setError(value.intValue());
                        break;
                    case 5:
                        historyRecord.setFinished(value.intValue());
                        break;
                    case 6:
                        historyRecord.setStartType(value.intValue());
                        break;
                    case 7:
                        historyRecord.setCleanType(value.intValue());
                        break;
                    case 8:
                        historyRecord.setFinishReason(value.intValue());
                        break;
                }
            } catch (ClassCastException | NumberFormatException | IllegalStateException e) {
            }
        }
        updateHistoryRecord(historyRecord);
    }

    private void updateHistoryRecord(HistoryRecordDTO historyRecordDTO) {
        JsonObject historyRecord = GSON.toJsonTree(historyRecordDTO).getAsJsonObject();
        if (historyRecordDTO.getStart() != null) {
            DateTimeType start = new DateTimeType(historyRecordDTO.getStart());
            historyRecord.addProperty("start", start.toLocaleZone().format(null));
            updateState(CHANNEL_HISTORY_START_TIME, start);
        }
        if (historyRecordDTO.getEnd() != null) {
            DateTimeType end = new DateTimeType(historyRecordDTO.getEnd());
            historyRecord.addProperty("end", end.toLocaleZone().format(null));
            updateState(CHANNEL_HISTORY_END_TIME, end);
        }
        if (historyRecordDTO.getDuration() != null) {
            long duration = TimeUnit.SECONDS.toMinutes(historyRecordDTO.getDuration().longValue());
            historyRecord.addProperty("duration", duration);
            updateState(CHANNEL_HISTORY_DURATION, new QuantityType<>(duration, Units.MINUTE));
        }
        if (historyRecordDTO.getArea() != null) {
            historyRecord.addProperty("area", historyRecordDTO.getArea());
            updateState(CHANNEL_HISTORY_AREA, new QuantityType<>(historyRecordDTO.getArea(), SIUnits.SQUARE_METRE));
        }
        if (historyRecordDTO.getError() != null) {
            historyRecord.addProperty("error", historyRecordDTO.getError());
            updateState(CHANNEL_HISTORY_ERROR, new DecimalType(historyRecordDTO.getError()));
        }
        if (historyRecordDTO.getFinished() != null) {
            historyRecord.addProperty("finished", historyRecordDTO.getFinished());
            updateState(CHANNEL_HISTORY_FINISH, new DecimalType(historyRecordDTO.getFinished()));
        }
        if (historyRecordDTO.getFinishReason() != null) {
            historyRecord.addProperty("finish_reason", historyRecordDTO.getFinishReason());
            updateState(CHANNEL_HISTORY_FINISHREASON, new DecimalType(historyRecordDTO.getFinishReason()));
        }
        if (historyRecordDTO.getDustCollectionStatus() != null) {
            historyRecord.addProperty("dust_collection_status", historyRecordDTO.getDustCollectionStatus());
            updateState(CHANNEL_HISTORY_DUSTCOLLECTION, new DecimalType(historyRecordDTO.getDustCollectionStatus()));
        }
        updateState(CHANNEL_HISTORY_RECORD, new StringType(historyRecord.toString()));
    }

    private void updateRoomMapping(MiIoSendCommand response) {
        for (RobotCababilities cmd : FEATURES_CHANNELS) {
            if (response.getCommand().getCommand().contentEquals(cmd.getCommand())) {
                if (response.getResult().isJsonArray()) {
                    JsonArray rooms = response.getResult().getAsJsonArray();
                    JsonArray mappedRoom = new JsonArray();
                    for (JsonElement roomE : rooms) {
                        JsonArray room = roomE.getAsJsonArray();
                        HomeRoomDTO name = cloudConnector.getRoom(room.get(1).getAsString());
                        if (name != null && name.getName() != null) {
                            room.add(name.getName());
                        } else {
                            room.add("not found");
                        }
                        mappedRoom.add(room);
                    }
                    updateState(cmd.getChannel(), new StringType(mappedRoom.toString()));
                } else {
                    updateState(cmd.getChannel(), new StringType(response.getResult().toString()));
                }
                break;
            }
        }
    }

    @Override
    protected boolean skipUpdate() {
        if (!hasConnection()) {
            logger.debug("Skipping periodic update for '{}'. No Connection", getThing().getUID().toString());
            return true;
        }
        if (ThingStatusDetail.CONFIGURATION_ERROR.equals(getThing().getStatusInfo().getStatusDetail())) {
            logger.debug("Skipping periodic update for '{}' UID '{}'. Thing Status", getThing().getUID().toString(),
                    getThing().getStatusInfo().getStatusDetail());
            refreshNetwork();
            return true;
        }
        final MiIoAsyncCommunication mc = miioCom;
        if (mc != null && mc.getQueueLength() > MAX_QUEUE) {
            logger.debug("Skipping periodic update for '{}'. {} elements in queue.", getThing().getUID().toString(),
                    mc.getQueueLength());
            return true;
        }
        return false;
    }

    @Override
    protected synchronized void updateData() {
        if (!hasConnection() || skipUpdate()) {
            return;
        }
        logger.debug("Periodic update for '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID());
        try {
            dnd.getValue();
            history.getValue();
            status.getValue();
            refreshNetwork();
            consumables.getValue();
            if (lastMap.isEmpty() || stateId != 8) {
                if (isLinked(mapChannelUid)) {
                    map.getValue();
                }
            }
            for (RobotCababilities cmd : FEATURES_CHANNELS) {
                if (isLinked(cmd.getChannel()) && !cmd.getCommand().isBlank()) {
                    sendCommand(cmd.getCommand());
                }
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': '{}", getThing().getUID().toString(), e.getLocalizedMessage());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        hasChannelStructure = false;
        this.mapDrawOptions = RRMapDrawOptions
                .getOptionsFromFile(BINDING_USERDATA_PATH + File.separator + "mapConfig.json", logger);
        updateState(RobotCababilities.SEGMENT_CLEAN.getChannel(), new StringType("-"));
        cloudConnector.getHomeLists();
    }

    @Override
    protected boolean initializeData() {
        updateState(CHANNEL_CONSUMABLE_RESET, new StringType("none"));
        return super.initializeData();
    }

    @Override
    public void onMessageReceived(MiIoSendCommand response) {
        super.onMessageReceived(response);
        if (response.isError()) {
            return;
        }
        switch (response.getCommand()) {
            case GET_STATUS:
                if (response.getResult().isJsonArray()) {
                    JsonObject statusResponse = response.getResult().getAsJsonArray().get(0).getAsJsonObject();
                    if (!hasChannelStructure) {
                        setCapabilities(statusResponse);
                        createCapabilityChannels();
                    }
                    updateVacuumStatus(statusResponse);
                }
                break;
            case CONSUMABLES_GET:
                if (response.getResult().isJsonArray()) {
                    updateConsumables(response.getResult().getAsJsonArray().get(0).getAsJsonObject());
                }
                break;
            case DND_GET:
                if (response.getResult().isJsonArray()) {
                    updateDnD(response.getResult().getAsJsonArray().get(0).getAsJsonObject());
                }
                break;
            case CLEAN_SUMMARY_GET:
                if (response.getResult().isJsonArray()) {
                    updateHistoryLegacy(response.getResult().getAsJsonArray());
                } else if (response.getResult().isJsonObject()) {
                    updateHistory(response.getResult().getAsJsonObject());
                }
                break;
            case CLEAN_RECORD_GET:
                if (response.getResult().isJsonArray() && response.getResult().getAsJsonArray().size() > 0
                        && response.getResult().getAsJsonArray().get(0).isJsonArray()) {
                    updateHistoryRecordLegacy(response.getResult().getAsJsonArray().get(0).getAsJsonArray());
                } else if (response.getResult().isJsonArray() && response.getResult().getAsJsonArray().size() > 0
                        && response.getResult().getAsJsonArray().get(0).isJsonObject()) {
                    final HistoryRecordDTO historyRecordDTO = GSON.fromJson(
                            response.getResult().getAsJsonArray().get(0).getAsJsonObject(), HistoryRecordDTO.class);
                    if (historyRecordDTO != null) {
                        updateHistoryRecord(historyRecordDTO);
                    }
                } else if (response.getResult().isJsonObject()) {
                    final HistoryRecordDTO historyRecordDTO = GSON.fromJson(response.getResult().getAsJsonObject(),
                            HistoryRecordDTO.class);
                    if (historyRecordDTO != null) {
                        updateHistoryRecord(historyRecordDTO);
                    }
                } else {
                    logger.debug("Could not extract cleaning history record from: {}", response.getResult());
                }
                break;
            case GET_MAP:
                if (response.getResult().isJsonArray()) {
                    String mapresponse = response.getResult().getAsJsonArray().get(0).getAsString();
                    if (!mapresponse.contentEquals("retry") && !mapresponse.contentEquals(lastMap)) {
                        lastMap = mapresponse;
                        miIoScheduler.submit(() -> updateState(CHANNEL_VACUUM_MAP, getMap(mapresponse)));
                    }
                }
                break;
            case GET_MAP_STATUS:
            case GET_SEGMENT_STATUS:
            case GET_LED_STATUS:
                updateNumericChannel(response);
                break;
            case GET_ROOM_MAPPING:
                updateRoomMapping(response);
                break;
            case GET_CARPET_MODE:
            case GET_FW_FEATURES:
            case GET_CUSTOMIZED_CLEAN_MODE:
            case GET_MULTI_MAP_LIST:
            case SET_COLLECT_DUST:
            case SET_CLEAN_MOP_START:
            case SET_CLEAN_MOP_STOP:
                for (RobotCababilities cmd : FEATURES_CHANNELS) {
                    if (response.getCommand().getCommand().contentEquals(cmd.getCommand())) {
                        updateState(cmd.getChannel(), new StringType(response.getResult().toString()));
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void updateNumericChannel(MiIoSendCommand response) {
        RobotCababilities capabilityChannel = null;
        for (RobotCababilities cmd : FEATURES_CHANNELS) {
            if (response.getCommand().getCommand().contentEquals(cmd.getCommand())) {
                capabilityChannel = cmd;
                break;
            }
        }
        if (capabilityChannel != null) {
            if (response.getResult().isJsonArray() && response.getResult().getAsJsonArray().get(0).isJsonPrimitive()) {
                try {
                    Integer stat = response.getResult().getAsJsonArray().get(0).getAsInt();
                    updateState(capabilityChannel.getChannel(), new DecimalType(stat));
                    return;
                } catch (ClassCastException | IllegalStateException e) {
                    logger.debug("Could not update numeric channel {} with '{}': {}", capabilityChannel.getChannel(),
                            response.getResult(), e.getMessage());
                }
            } else {
                logger.debug("Could not update numeric channel {} with '{}': Not in expected format",
                        capabilityChannel.getChannel(), response.getResult());
            }
            updateState(capabilityChannel.getChannel(), UnDefType.UNDEF);
        }
    }

    private void setCapabilities(JsonObject statusResponse) {
        for (RobotCababilities capability : RobotCababilities.values()) {
            if (statusResponse.has(capability.getStatusFieldName())) {
                deviceCapabilities.putIfAbsent(capability, false);
                logger.debug("Setting additional vacuum {}", capability);
            }
        }
    }

    private void createCapabilityChannels() {
        ThingBuilder thingBuilder = editThing();
        int cnt = 0;

        for (Entry<RobotCababilities, Boolean> robotCapability : deviceCapabilities.entrySet()) {
            RobotCababilities capability = robotCapability.getKey();
            Boolean channelCreated = robotCapability.getValue();
            if (!channelCreated) {
                if (thing.getChannels().stream()
                        .anyMatch(ch -> ch.getUID().getId().equalsIgnoreCase(capability.getChannel()))) {
                    logger.debug("Channel already available...skip creation of channel '{}'.", capability.getChannel());
                    deviceCapabilities.replace(capability, true);
                    continue;
                }
                logger.debug("Creating dynamic channel for capability {}", capability);
                ChannelType channelType = channelTypeRegistry.getChannelType(capability.getChannelType());
                if (channelType != null) {
                    logger.debug("Found channelType '{}' for capability {}", channelType, capability.name());
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(), capability.getChannel());
                    Channel channel = ChannelBuilder.create(channelUID, channelType.getItemType())
                            .withType(capability.getChannelType()).withLabel(channelType.getLabel()).build();
                    thingBuilder.withChannel(channel);
                    cnt++;
                } else {
                    logger.debug("ChannelType {} not found (Unexpected). Available types:",
                            capability.getChannelType());
                    for (ChannelType ct : channelTypeRegistry.getChannelTypes()) {
                        logger.debug("Available channelType: '{}' '{}' '{}'", ct.getUID(), ct.toString(),
                                ct.getConfigDescriptionURI());
                    }
                }
            }
        }
        if (cnt > 0) {
            updateThing(thingBuilder.build());
        }
        hasChannelStructure = true;
    }

    private State getMap(String map) {
        final MiIoBindingConfiguration configuration = this.configuration;
        if (configuration != null && cloudConnector.isConnected()) {
            try {
                final @Nullable RawType mapDl = cloudConnector.getMap(map, configuration.cloudServer);
                if (mapDl != null) {
                    byte[] mapData = mapDl.getBytes();
                    RRMapDraw rrMap = RRMapDraw.loadImage(new ByteArrayInputStream(mapData));
                    rrMap.setDrawOptions(mapDrawOptions);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (logger.isDebugEnabled()) {
                        final String mapPath = BINDING_USERDATA_PATH + File.separator + map
                                + LocalDateTime.now().format(DATEFORMATTER) + ".rrmap";
                        CloudUtil.writeBytesToFileNio(mapData, mapPath);
                        logger.debug("Mapdata saved to {}", mapPath);
                    }
                    ImageIO.write(rrMap.getImage(), "jpg", baos);
                    byte[] byteArray = baos.toByteArray();
                    if (byteArray != null && byteArray.length > 0) {
                        return new RawType(byteArray, "image/jpeg");
                    } else {
                        logger.debug("Mapdata empty removing image");
                        return UnDefType.UNDEF;
                    }
                }
            } catch (MiCloudException e) {
                logger.debug("Error getting data from Xiaomi cloud. Mapdata could not be updated: {}", e.getMessage());
            } catch (IOException e) {
                logger.debug("Mapdata could not be updated: {}", e.getMessage());
            }
        } else {
            logger.debug("Not connected to Xiaomi cloud. Cannot retreive new map: {}", map);
        }
        return UnDefType.UNDEF;
    }
}
