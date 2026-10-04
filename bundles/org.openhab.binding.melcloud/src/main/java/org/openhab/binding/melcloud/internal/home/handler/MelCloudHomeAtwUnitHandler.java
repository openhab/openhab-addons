/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.home.handler;

import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwCapabilities;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwScheduleEntry;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwScheduleWriteRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwUnit;
import org.openhab.binding.melcloud.internal.home.config.MelCloudHomeUnitConfig;
import org.openhab.binding.melcloud.internal.logging.SensitiveDataMasker;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MelCloudHomeAtwUnitHandler} handles a single MELCloud Home Air-to-Water (heat pump) unit.
 *
 * <p>
 * Its main state comes from the bridge's centralized {@code /context} poll via
 * {@link MelCloudHomeAtwUnitListener#onAtwUnitUpdated(MelCloudHomeAtwUnit)}, including outdoor temperature, which
 * (unlike the ATA unit) the Home API reports directly in {@code settings} rather than via a separate telemetry
 * call. Only energy consumed/produced is polled directly by this handler, at a much longer interval.
 *
 * <p>
 * Outbound control commands are deduplicated against the last known unit state (skipping a call whose single target
 * field already matches) and routed through the bridge's shared {@code MelCloudHomeRequestPacer}, per ADR-008.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtwUnitHandler extends BaseThingHandler implements MelCloudHomeAtwUnitListener {

    private static final long TELEMETRY_POLL_INTERVAL_SECONDS = 1800;
    private static final String ENERGY_CONSUMED_MEASURE = "interval_energy_consumed";
    private static final String ENERGY_PRODUCED_MEASURE = "interval_energy_produced";

    /**
     * Maps a schedule entry's {@code operationModeZone1}/{@code operationModeZone2} word (matched
     * case-insensitively, to absorb the unconfirmed casing difference between the read shape's lowercase words and
     * the live control API's PascalCase words — see ADR-012) onto the integer code the write endpoint expects.
     * Deliberately covers only the three heating modes: cooling modes have no confirmed integer code at all, so
     * {@link #createSchedule} / {@link #updateSchedule} reject them (per {@code spec.md}'s "cooling-mode schedule
     * entry is rejected" scenario) rather than guess.
     */
    private static final Map<String, Integer> SCHEDULE_OPERATION_MODE_TO_CODE = Map.of("heatroomtemperature", 0,
            "heatflowtemperature", 1, "heatcurve", 2);

    /**
     * Maps the API's zone operation mode word onto the numeric code exposed by the {@code zoneOperationMode-channel}
     * Number channel. The codes are the values of the MELCloud Home app's own {@code AtwOperationModesZone} enum. A
     * word the API starts sending that is not listed here needs a new entry (and a channel option) before it is shown.
     */
    private static final Map<String, Integer> ZONE_MODE_WORD_TO_CODE = Map.of("HeatRoomTemperature", 0,
            "HeatFlowTemperature", 1, "HeatCurve", 2, "CoolRoomTemperature", 3, "CoolFlowTemperature", 4, "DryFloor",
            5);
    private static final Map<Integer, String> ZONE_MODE_CODE_TO_WORD = ZONE_MODE_WORD_TO_CODE.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * Maps the API's read-only operation status word onto the numeric code exposed by the
     * {@code operationStatus-channel} Number channel. The codes are the values of the MELCloud Home app's own
     * {@code AtwOperationMode} enum ({@code 0} = Unknown is intentionally not mapped).
     */
    private static final Map<String, Integer> OPERATION_STATUS_WORD_TO_CODE = Map.of("Stop", 1, "HotWater", 2,
            "Heating", 3, "Cooling", 4, "FreezeStat", 5, "LegionellaPrevention", 6);

    /**
     * Day-name-to-number mapping for schedule writes, per the {@code melcloudhome} project's documented convention
     * (0=Sunday..6=Saturday) — unconfirmed against real ATW traffic, see ADR-012.
     */
    private static final Map<String, Integer> DAY_NAME_TO_NUMBER = Map.of("sunday", 0, "monday", 1, "tuesday", 2,
            "wednesday", 3, "thursday", 4, "friday", 5, "saturday", 6);

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeAtwUnitHandler.class);

    private MelCloudHomeUnitConfig config = new MelCloudHomeUnitConfig();
    private @Nullable MelCloudHomeAccountHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> telemetryFuture;
    private volatile @Nullable MelCloudHomeAtwUnit lastKnownUnit;
    private volatile boolean capabilitiesPropertiesSet;

    public MelCloudHomeAtwUnitHandler(Thing thing) {
        super(thing);
    }

    /**
     * Registers {@link MelCloudHomeAtwScheduleActions} so its {@code @RuleAction} methods are available on this
     * Thing, per ADR-011 (schedule management is exposed via {@code ThingActions}, not Channels/Items).
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MelCloudHomeAtwScheduleActions.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing {} handler.", getThing().getThingTypeUID());
        config = getConfigAs(MelCloudHomeUnitConfig.class);
        if (config.unitId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "unitId is required");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof MelCloudHomeAccountHandler accountHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not set");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        initializeBridge(accountHandler, bridge.getStatus());
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler != null) {
            handler.unregisterAtwUnitListener(config.unitId);
        }
        cancelTelemetryPoll();
        bridgeHandler = null;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof MelCloudHomeAccountHandler accountHandler) {
            initializeBridge(accountHandler, bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(MelCloudHomeAccountHandler accountHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());
        this.bridgeHandler = accountHandler;
        if (bridgeStatus == ThingStatus.ONLINE) {
            accountHandler.registerAtwUnitListener(config.unitId, this);
            startTelemetryPollIfNeeded();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command '{}' to channel {}", command, channelUID);

        if (command instanceof RefreshType) {
            logger.debug("Refresh command not supported");
            return;
        }

        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            logger.warn("No connection to MELCloud Home available, ignoring command");
            return;
        }

        MelCloudHomeAtwUnit lastUnit = lastKnownUnit;
        MelCloudHomeAtwControlRequest request = new MelCloudHomeAtwControlRequest();
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                boolean powerValue = command == OnOffType.ON;
                if (lastUnit != null && lastUnit.isPower() == powerValue) {
                    logger.debug("Skipping power command, unit already reports power={}", powerValue);
                    return;
                }
                request.power = powerValue;
                break;
            case CHANNEL_HOME_SET_TEMPERATURE_ZONE1:
                Double zone1Temperature = toCelsius(command);
                if (zone1Temperature == null) {
                    return;
                }
                if (lastUnit != null
                        && lastUnit.getSetTemperatureZone1().filter(zone1Temperature::equals).isPresent()) {
                    logger.debug("Skipping zone1 set temperature command, unit already reports {}", zone1Temperature);
                    return;
                }
                request.setTemperatureZone1 = zone1Temperature;
                break;
            case CHANNEL_HOME_SET_TEMPERATURE_ZONE2:
                Double zone2Temperature = toCelsius(command);
                if (zone2Temperature == null) {
                    return;
                }
                if (lastUnit != null
                        && lastUnit.getSetTemperatureZone2().filter(zone2Temperature::equals).isPresent()) {
                    logger.debug("Skipping zone2 set temperature command, unit already reports {}", zone2Temperature);
                    return;
                }
                request.setTemperatureZone2 = zone2Temperature;
                break;
            case CHANNEL_ZONE1_OPERATION_MODE:
                String operationModeZone1 = toZoneModeWord(command);
                if (operationModeZone1 == null) {
                    return;
                }
                if (lastUnit != null && operationModeZone1.equals(lastUnit.getOperationModeZone1())) {
                    logger.debug("Skipping zone1 operation mode command, unit already reports {}", operationModeZone1);
                    return;
                }
                request.operationModeZone1 = operationModeZone1;
                break;
            case CHANNEL_ZONE2_OPERATION_MODE:
                String operationModeZone2 = toZoneModeWord(command);
                if (operationModeZone2 == null) {
                    return;
                }
                if (lastUnit != null
                        && lastUnit.getOperationModeZone2().filter(operationModeZone2::equals).isPresent()) {
                    logger.debug("Skipping zone2 operation mode command, unit already reports {}", operationModeZone2);
                    return;
                }
                request.operationModeZone2 = operationModeZone2;
                break;
            case CHANNEL_HOME_TANK_TARGET_WATER_TEMPERATURE:
                Double tankTemperature = toCelsius(command);
                if (tankTemperature == null) {
                    return;
                }
                if (lastUnit != null
                        && lastUnit.getSetTankWaterTemperature().filter(tankTemperature::equals).isPresent()) {
                    logger.debug("Skipping tank target temperature command, unit already reports {}", tankTemperature);
                    return;
                }
                request.setTankWaterTemperature = tankTemperature;
                break;
            case CHANNEL_HOME_FORCED_HOTWATERMODE:
                boolean forcedHotWaterModeValue = command == OnOffType.ON;
                if (lastUnit != null && lastUnit.isForcedHotWaterMode() == forcedHotWaterModeValue) {
                    logger.debug("Skipping forced hot water mode command, unit already reports {}",
                            forcedHotWaterModeValue);
                    return;
                }
                request.forcedHotWaterMode = forcedHotWaterModeValue;
                break;
            default:
                logger.debug("Read-only or unknown channel {}, skipping command", channelUID);
                return;
        }

        handler.getRequestPacer().schedule(() -> {
            try {
                handler.getApiClient().controlAtwUnit(handler.getAccessToken(), config.unitId, request);
            } catch (MelCloudCommException e) {
                logger.warn("Command '{}' to channel '{}' failed, reason {}. ", command, channelUID, e.getMessage());
            }
        });
    }

    @Override
    public void onAtwUnitUpdated(MelCloudHomeAtwUnit unit) {
        lastKnownUnit = unit;
        updateCapabilityProperties(unit.capabilities);
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_POWER, OnOffType.from(unit.isPower()));
        updateCodeState(CHANNEL_OPERATION_STATUS, OPERATION_STATUS_WORD_TO_CODE, unit.getOperationStatus());
        updateCodeState(CHANNEL_ZONE1_OPERATION_MODE, ZONE_MODE_WORD_TO_CODE, unit.getOperationModeZone1());
        unit.getSetTemperatureZone1().ifPresentOrElse(
                value -> updateState(CHANNEL_HOME_SET_TEMPERATURE_ZONE1, new QuantityType<>(value, SIUnits.CELSIUS)),
                () -> updateState(CHANNEL_HOME_SET_TEMPERATURE_ZONE1, UnDefType.UNDEF));
        unit.getRoomTemperatureZone1().ifPresentOrElse(
                value -> updateState(CHANNEL_HOME_ROOM_TEMPERATURE_ZONE1, new QuantityType<>(value, SIUnits.CELSIUS)),
                () -> updateState(CHANNEL_HOME_ROOM_TEMPERATURE_ZONE1, UnDefType.UNDEF));
        if (unit.hasZone2()) {
            unit.getOperationModeZone2().ifPresentOrElse(
                    value -> updateCodeState(CHANNEL_ZONE2_OPERATION_MODE, ZONE_MODE_WORD_TO_CODE, value),
                    () -> updateState(CHANNEL_ZONE2_OPERATION_MODE, UnDefType.UNDEF));
            unit.getSetTemperatureZone2().ifPresentOrElse(
                    value -> updateState(CHANNEL_HOME_SET_TEMPERATURE_ZONE2,
                            new QuantityType<>(value, SIUnits.CELSIUS)),
                    () -> updateState(CHANNEL_HOME_SET_TEMPERATURE_ZONE2, UnDefType.UNDEF));
            unit.getRoomTemperatureZone2().ifPresentOrElse(
                    value -> updateState(CHANNEL_HOME_ROOM_TEMPERATURE_ZONE2,
                            new QuantityType<>(value, SIUnits.CELSIUS)),
                    () -> updateState(CHANNEL_HOME_ROOM_TEMPERATURE_ZONE2, UnDefType.UNDEF));
        }
        unit.getSetTankWaterTemperature().ifPresentOrElse(
                value -> updateState(CHANNEL_HOME_TANK_TARGET_WATER_TEMPERATURE,
                        new QuantityType<>(value, SIUnits.CELSIUS)),
                () -> updateState(CHANNEL_HOME_TANK_TARGET_WATER_TEMPERATURE, UnDefType.UNDEF));
        unit.getTankWaterTemperature().ifPresentOrElse(
                value -> updateState(CHANNEL_HOME_TANK_WATER_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)),
                () -> updateState(CHANNEL_HOME_TANK_WATER_TEMPERATURE, UnDefType.UNDEF));
        updateState(CHANNEL_HOME_FORCED_HOTWATERMODE, OnOffType.from(unit.isForcedHotWaterMode()));
        unit.getOutdoorTemperature().ifPresentOrElse(
                value -> updateState(CHANNEL_OUTDOOR_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)),
                () -> updateState(CHANNEL_OUTDOOR_TEMPERATURE, UnDefType.UNDEF));
        updateState(CHANNEL_IN_STANDBY_MODE, OnOffType.from(unit.isInStandbyMode()));
        updateState(CHANNEL_IS_IN_ERROR, OnOffType.from(unit.isInError()));
        unit.getErrorCode().ifPresentOrElse(value -> updateState(CHANNEL_ERROR_CODE, new StringType(value)),
                () -> updateState(CHANNEL_ERROR_CODE, UnDefType.UNDEF));
        updateState(CHANNEL_HOLIDAY_MODE, OnOffType.from(unit.isHolidayModeEnabled()));
        updateState(CHANNEL_FROST_PROTECTION, OnOffType.from(unit.isFrostProtectionEnabled()));
        Integer rssi = unit.rssi;
        if (rssi != null) {
            updateState(CHANNEL_RSSI, new DecimalType(mapRssiToSignalStrength(rssi)));
        } else {
            updateState(CHANNEL_RSSI, UnDefType.UNDEF);
        }
    }

    // --- Schedule management (ADR-011/ADR-012, called from MelCloudHomeAtwScheduleActions) ---
    //
    // Every method here is PROVISIONAL: field shapes, endpoint paths, and the integer/day-name mappings used below
    // are not independently confirmed against real ATW traffic (see docs/changes/add-melcloud-home-schedule-
    // management/proposal.md's Open Questions and ADR-012's Status section). None of these methods throw
    // MelCloudCommException to their ThingActions caller — like handleCommand, failures are logged and reported
    // back as an empty/false result, since openHAB ThingActions are not expected to propagate checked exceptions
    // to rule scripts.

    /**
     * Lists this unit's currently known cloud schedule entries, as last reported by the bridge's {@code /context}
     * poll. Returns an empty list (not an error) if no unit state has been received yet.
     *
     * <p>
     * <b>Provisional:</b> assumes {@code schedule} arrives embedded in the regular unit state — see
     * {@link MelCloudHomeAtwUnit#schedule}'s Javadoc.
     */
    public List<Map<String, Object>> listSchedules() {
        MelCloudHomeAtwUnit unit = lastKnownUnit;
        if (unit == null) {
            return List.of();
        }
        return unit.schedule.stream().map(MelCloudHomeAtwUnitHandler::scheduleEntryToMap).collect(Collectors.toList());
    }

    /**
     * Creates a new cloud schedule entry. See the class-level note above on provisional field mappings and error
     * handling.
     *
     * @return the newly generated entry id, or {@code ""} if the call was rejected (unconfirmed cooling mode, no
     *         bridge connection, invalid day name) or failed (communication error)
     */
    public String createSchedule(String days, String time, @Nullable Boolean power, @Nullable String operationModeZone1,
            @Nullable Double setTemperatureZone1, @Nullable Double setTemperatureZone2,
            @Nullable Double setTankWaterTemperature, @Nullable Boolean forcedHotWaterMode) {
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            logger.warn("No connection to MELCloud Home available, ignoring createSchedule");
            return "";
        }
        List<Integer> dayNumbers = parseDays(days);
        if (dayNumbers == null) {
            return "";
        }
        Integer modeCode = toScheduleOperationModeCode(operationModeZone1);
        if (operationModeZone1 != null && modeCode == null) {
            logger.warn(
                    "Rejecting createSchedule: '{}' has no confirmed schedule integer code (cooling modes are not yet confirmed against the real API, see ADR-012)",
                    operationModeZone1);
            return "";
        }
        MelCloudHomeAtwScheduleWriteRequest request = new MelCloudHomeAtwScheduleWriteRequest();
        String id = UUID.randomUUID().toString();
        request.id = id;
        request.days = dayNumbers;
        request.time = time;
        request.power = power;
        request.operationModeZone1 = modeCode;
        request.setTemperatureZone1 = setTemperatureZone1;
        request.setTemperatureZone2 = setTemperatureZone2;
        request.setTankWaterTemperature = setTankWaterTemperature;
        request.forcedHotWaterMode = forcedHotWaterMode;
        return submitScheduleWrite(handler, request) ? id : "";
    }

    /**
     * Updates an existing cloud schedule entry by id. Fields left {@code null} are meant to be left unchanged — see
     * {@link MelCloudHomeAtwScheduleWriteRequest#days}'s Javadoc for why this is unconfirmed, not a settled
     * contract.
     *
     * @return {@code true} if the update request was submitted, {@code false} if it was rejected or failed
     */
    public boolean updateSchedule(String id, @Nullable String days, @Nullable String time, @Nullable Boolean power,
            @Nullable String operationModeZone1, @Nullable Double setTemperatureZone1,
            @Nullable Double setTemperatureZone2, @Nullable Double setTankWaterTemperature,
            @Nullable Boolean forcedHotWaterMode) {
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            logger.warn("No connection to MELCloud Home available, ignoring updateSchedule");
            return false;
        }
        @Nullable
        List<Integer> dayNumbers = null;
        if (days != null) {
            dayNumbers = parseDays(days);
            if (dayNumbers == null) {
                return false;
            }
        }
        Integer modeCode = toScheduleOperationModeCode(operationModeZone1);
        if (operationModeZone1 != null && modeCode == null) {
            logger.warn(
                    "Rejecting updateSchedule: '{}' has no confirmed schedule integer code (cooling modes are not yet confirmed against the real API, see ADR-012)",
                    operationModeZone1);
            return false;
        }
        MelCloudHomeAtwScheduleWriteRequest request = new MelCloudHomeAtwScheduleWriteRequest();
        request.id = id;
        request.days = dayNumbers;
        request.time = time;
        request.power = power;
        request.operationModeZone1 = modeCode;
        request.setTemperatureZone1 = setTemperatureZone1;
        request.setTemperatureZone2 = setTemperatureZone2;
        request.setTankWaterTemperature = setTankWaterTemperature;
        request.forcedHotWaterMode = forcedHotWaterMode;
        return submitScheduleWrite(handler, request);
    }

    /**
     * Deletes a cloud schedule entry by id.
     *
     * @return {@code true} if the delete request was submitted, {@code false} if it failed or no bridge is
     *         connected
     */
    public boolean deleteSchedule(String id) {
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            logger.warn("No connection to MELCloud Home available, ignoring deleteSchedule");
            return false;
        }
        try {
            handler.getRequestPacer().<Boolean> scheduleBlocking(() -> {
                handler.getApiClient().deleteAtwSchedule(handler.getAccessToken(), config.unitId, id);
                return Boolean.TRUE;
            });
            return true;
        } catch (MelCloudCommException e) {
            logger.warn("deleteSchedule for id '{}' failed, reason {}. ", id, e.getMessage());
            return false;
        }
    }

    /**
     * Enables or disables all of this unit's cloud schedules at once.
     *
     * @return {@code true} if the request was submitted, {@code false} if it failed or no bridge is connected
     */
    public boolean setSchedulesEnabled(boolean enabled) {
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            logger.warn("No connection to MELCloud Home available, ignoring setSchedulesEnabled");
            return false;
        }
        try {
            handler.getRequestPacer().<Boolean> scheduleBlocking(() -> {
                handler.getApiClient().setAtwScheduleEnabled(handler.getAccessToken(), config.unitId, enabled);
                return Boolean.TRUE;
            });
            return true;
        } catch (MelCloudCommException e) {
            logger.warn("setSchedulesEnabled({}) failed, reason {}. ", enabled, e.getMessage());
            return false;
        }
    }

    private boolean submitScheduleWrite(MelCloudHomeAccountHandler handler,
            MelCloudHomeAtwScheduleWriteRequest request) {
        try {
            handler.getRequestPacer().<Boolean> scheduleBlocking(() -> {
                handler.getApiClient().createOrUpdateAtwSchedule(handler.getAccessToken(), config.unitId, request);
                return Boolean.TRUE;
            });
            return true;
        } catch (MelCloudCommException e) {
            logger.warn("Schedule write for id '{}' failed, reason {}. ", request.id, e.getMessage());
            return false;
        }
    }

    /**
     * @param word a schedule entry's {@code operationModeZone1}/{@code operationModeZone2} word, matched
     *            case-insensitively (see {@link #SCHEDULE_OPERATION_MODE_TO_CODE}'s Javadoc)
     * @return the confirmed integer code, or {@code null} if {@code word} is {@code null} or has no confirmed code
     *         (in particular, any cooling mode)
     */
    private static @Nullable Integer toScheduleOperationModeCode(@Nullable String word) {
        if (word == null) {
            return null;
        }
        return SCHEDULE_OPERATION_MODE_TO_CODE.get(word.toLowerCase(Locale.ROOT));
    }

    /**
     * @param days a comma-separated list of day names (e.g. {@code "monday,wednesday,friday"}), matched
     *            case-insensitively
     * @return the corresponding day numbers per {@link #DAY_NAME_TO_NUMBER}, or {@code null} (logging a warning) if
     *         any name is unrecognized
     */
    private @Nullable List<Integer> parseDays(String days) {
        List<Integer> result = new ArrayList<>();
        for (String rawDay : days.split(",")) {
            String day = rawDay.trim().toLowerCase(Locale.ROOT);
            Integer number = DAY_NAME_TO_NUMBER.get(day);
            if (number == null) {
                logger.warn("Unknown day name '{}' in '{}', rejecting schedule call", rawDay, days);
                return null;
            }
            result.add(number);
        }
        return result;
    }

    private static Map<String, Object> scheduleEntryToMap(MelCloudHomeAtwScheduleEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entry.id);
        map.put("days", entry.days);
        map.put("time", entry.time);
        map.put("power", entry.power);
        putIfPresent(map, "setTankWaterTemperature", entry.setTankWaterTemperature);
        map.put("forcedHotWaterMode", entry.forcedHotWaterMode);
        map.put("zone1Active", entry.zone1Active);
        map.put("zone2Active", entry.zone2Active);
        map.put("hotWaterActive", entry.hotWaterActive);
        putIfPresent(map, "setTemperatureZone1", entry.setTemperatureZone1);
        putIfPresent(map, "setTemperatureZone2", entry.setTemperatureZone2);
        putIfPresent(map, "operationModeZone1", entry.operationModeZone1);
        putIfPresent(map, "operationModeZone2", entry.operationModeZone2);
        return map;
    }

    /**
     * Puts {@code key}/{@code value} into {@code map} only if {@code value} is non-null, since
     * {@code Map<String, Object>} does not accept null values — unset optional schedule fields are simply omitted
     * from the resulting map rather than represented as a null entry.
     *
     * @param map the target map
     * @param key the key to put
     * @param value the value to put, or {@code null} to skip this entry
     */
    private static void putIfPresent(Map<String, Object> map, String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Writes the unit's capability flags as Thing properties, once. Capabilities are static for the lifetime of a
     * unit (they don't change between {@code /context} polls), so there is no need to re-write them on every update.
     *
     * @param capabilities the unit's capability flags, as last reported by the API
     */
    private void updateCapabilityProperties(MelCloudHomeAtwCapabilities capabilities) {
        if (capabilitiesPropertiesSet) {
            return;
        }
        updateProperties(Map.of(PROPERTY_ATW_HAS_HOT_WATER, String.valueOf(capabilities.hasHotWater),
                PROPERTY_ATW_HAS_ZONE2, String.valueOf(capabilities.hasZone2), PROPERTY_ATW_HAS_HALF_DEGREES,
                String.valueOf(capabilities.hasHalfDegrees), PROPERTY_ATW_HAS_COOLING_MODE,
                String.valueOf(capabilities.hasCoolingMode), PROPERTY_ATW_HAS_MEASURED_ENERGY_CONSUMPTION,
                String.valueOf(capabilities.hasMeasuredEnergyConsumption), PROPERTY_ATW_HAS_MEASURED_ENERGY_PRODUCTION,
                String.valueOf(capabilities.hasMeasuredEnergyProduction), PROPERTY_ATW_HAS_ESTIMATED_ENERGY_CONSUMPTION,
                String.valueOf(capabilities.hasEstimatedEnergyConsumption),
                PROPERTY_ATW_HAS_ESTIMATED_ENERGY_PRODUCTION, String.valueOf(capabilities.hasEstimatedEnergyProduction),
                PROPERTY_ATW_FTC_MODEL, String.valueOf(capabilities.ftcModel)));
        capabilitiesPropertiesSet = true;
    }

    /**
     * Maps a Wi-Fi RSSI value in dBm to the 0 (no signal) .. 4 (excellent) scale expected by the
     * {@code system.signal-strength} channel.
     *
     * @param rssi received signal strength indicator, in dBm
     * @return signal quality on a 0-4 scale
     */
    private static int mapRssiToSignalStrength(int rssi) {
        if (rssi >= -60) {
            return 4;
        } else if (rssi >= -70) {
            return 3;
        } else if (rssi >= -80) {
            return 2;
        } else if (rssi >= -90) {
            return 1;
        }
        return 0;
    }

    private void startTelemetryPollIfNeeded() {
        if (telemetryFuture == null) {
            telemetryFuture = scheduler.scheduleWithFixedDelay(this::pollTelemetry, 0, TELEMETRY_POLL_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    private void pollTelemetry() {
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            return;
        }
        Instant to = Instant.now();
        Instant from = to.minus(1, ChronoUnit.DAYS);
        try {
            String accessToken = handler.getAccessToken();
            Double consumedWh = handler.getApiClient()
                    .fetchLatestEnergyWh(accessToken, config.unitId, from, to, ENERGY_CONSUMED_MEASURE).orElse(null);
            Double producedWh = handler.getApiClient()
                    .fetchLatestEnergyWh(accessToken, config.unitId, from, to, ENERGY_PRODUCED_MEASURE).orElse(null);
            if (consumedWh != null) {
                updateState(CHANNEL_ENERGY_CONSUMED, new QuantityType<>(consumedWh, Units.WATT_HOUR));
            } else {
                updateState(CHANNEL_ENERGY_CONSUMED, UnDefType.UNDEF);
            }
            if (producedWh != null) {
                updateState(CHANNEL_ENERGY_PRODUCED, new QuantityType<>(producedWh, Units.WATT_HOUR));
            } else {
                updateState(CHANNEL_ENERGY_PRODUCED, UnDefType.UNDEF);
            }
            if (consumedWh != null && producedWh != null && consumedWh > 0) {
                updateState(CHANNEL_COP, new DecimalType(producedWh / consumedWh));
            } else {
                // Also covers consumedWh == 0: COP is not meaningfully computable, not just "missing".
                updateState(CHANNEL_COP, UnDefType.UNDEF);
            }
        } catch (MelCloudCommException e) {
            logger.debug("Telemetry poll failed for ATW unit {}, reason {}. ",
                    SensitiveDataMasker.maskId(config.unitId), e.getMessage());
        }
    }

    private void updateCodeState(String channelId, Map<String, Integer> wordToCode, String word) {
        Integer code = wordToCode.get(word);
        if (code == null) {
            logger.debug("Unknown value '{}' for channel {}, no mapping defined", word, channelId);
            updateState(channelId, UnDefType.UNDEF);
            return;
        }
        updateState(channelId, new DecimalType(code));
    }

    private @Nullable String toZoneModeWord(Command command) {
        if (command instanceof DecimalType decimalCommand) {
            String word = ZONE_MODE_CODE_TO_WORD.get(decimalCommand.intValue());
            if (word != null) {
                return word;
            }
        }
        logger.debug("Can't convert '{}' to a zone operation mode", command);
        return null;
    }

    private @Nullable Double toCelsius(Command command) {
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> celsius = quantityCommand.toUnit(SIUnits.CELSIUS);
            if (celsius == null) {
                logger.debug("Can't convert '{}' to unit Celsius", quantityCommand);
                return null;
            }
            return celsius.doubleValue();
        }
        if (command instanceof DecimalType decimalCommand) {
            return decimalCommand.doubleValue();
        }
        logger.debug("Can't convert '{}' to set temperature", command);
        return null;
    }

    private void cancelTelemetryPoll() {
        ScheduledFuture<?> future = telemetryFuture;
        if (future != null) {
            future.cancel(true);
            telemetryFuture = null;
        }
    }
}
