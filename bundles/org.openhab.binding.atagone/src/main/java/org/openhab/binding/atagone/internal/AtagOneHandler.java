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
package org.openhab.binding.atagone.internal;

import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.atagone.internal.action.AtagOneActions;
import org.openhab.binding.atagone.internal.api.AtagEpoch;
import org.openhab.binding.atagone.internal.api.AtagOneApiClient;
import org.openhab.binding.atagone.internal.api.AtagOneCommunicationException;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the ATAG ONE thermostat Thing: pairing, polling, channel updates, and command dispatch.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneHandler extends BaseThingHandler {

    private static final int PAIRING_RETRY_S = 5;
    private static final int POST_COMMAND_DELAY_S = 2;
    private static final SecureRandom CLIENT_ID_RANDOM = new SecureRandom();

    public static final long SECONDS_PER_HOUR = 3600L;
    public static final long SECONDS_PER_DAY = 86400L;
    public static final long SECONDS_PER_15_MINUTES = 900L;
    private static final long SECONDS_PER_MINUTE = 60L;

    private final Logger logger = LoggerFactory.getLogger(AtagOneHandler.class);
    private final HttpClient httpClient;

    private AtagOneConfiguration config = new AtagOneConfiguration();
    private @Nullable AtagOneApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollJob;
    private @Nullable ScheduledFuture<?> pairingJob;
    private @Nullable Future<?> connectJob;
    private volatile boolean disposing = false;

    private volatile long generation = 0L;
    private final Object commandLock = new Object();

    private final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    // Timed-preset writes trigger boiler API reinit; suppress COMMUNICATION_ERROR during that window.
    private volatile long suppressCommErrorUntil = 0L;
    private volatile long defaultVacationDurationSeconds = 7 * 86400L;
    private volatile long defaultExtendDurationSeconds = 3600L;
    // Pending vacation reports ch_mode=auto, not holiday; tracked to detect armed-but-not-yet-active schedules.
    private volatile long armedStartVacation = 0L;
    private volatile double @Nullable [][][] lastChScheduleEntries;
    private volatile double @Nullable [][][] lastDhwScheduleEntries;
    private volatile @Nullable DeviceConfigDTO lastConfiguration;

    private final AtagOneStateDescriptionProvider stateDescriptionProvider;

    public AtagOneHandler(Thing thing, HttpClient httpClient,
            AtagOneStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(AtagOneActions.class);
    }

    @Override
    public void initialize() {
        disposing = false;
        long myGeneration = ++generation;
        config = getConfigAs(AtagOneConfiguration.class);
        if (config.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.hostname-missing");
            return;
        }
        stateMap.clear();
        updateStatus(ThingStatus.UNKNOWN);
        connectJob = scheduler.submit(() -> connect(myGeneration));
    }

    @Override
    public void dispose() {
        disposing = true;
        stopPollJob();
        Future<?> connecting = connectJob;
        if (connecting != null) {
            connecting.cancel(true);
            connectJob = null;
        }
        ScheduledFuture<?> pairing = pairingJob;
        if (pairing != null) {
            pairing.cancel(true);
            pairingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (disposing) {
            return;
        }
        AtagOneApiClient client = apiClient;
        if (client == null) {
            return;
        }

        if (command instanceof RefreshType) {
            stateMap.clear();
            scheduler.execute(this::poll);
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Ignoring command {} for channel {} — Thing is not ONLINE", command, channelUID.getId());
            return;
        }

        String channelId = channelUID.getId();

        if (CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE.equals(channelId)) {
            ScheduleDTO schedule = composeChScheduleUpdate(command);
            if (schedule == null) {
                logger.debug("Unhandled command {} for channel {}", command, channelId);
                revertToLastKnownState(channelId);
                return;
            }
            scheduler.execute(() -> sendChScheduleUpdate(client, schedule));
            return;
        }
        if (CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE.equals(channelId)) {
            ScheduleDTO schedule = composeDhwScheduleUpdate(command);
            if (schedule == null) {
                logger.debug("Unhandled command {} for channel {}", command, channelId);
                revertToLastKnownState(channelId);
                return;
            }
            scheduler.execute(() -> sendDhwScheduleUpdate(client, schedule));
            return;
        }

        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        if (!buildControlUpdate(channelId, command, control, configUpdate)) {
            logger.debug("Unhandled command {} for channel {}", command, channelId);
            revertToLastKnownState(channelId);
            return;
        }

        scheduler.execute(() -> sendControlUpdate(client, channelId, control, configUpdate));
    }

    public void sendComposedUpdate(String label, ControlUpdateDTO control, DeviceConfigUpdateDTO configUpdate) {
        if (disposing) {
            return;
        }
        AtagOneApiClient client = apiClient;
        if (client == null) {
            logger.warn("Cannot send {} — not connected", label);
            return;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Ignoring {} — Thing is not ONLINE", label);
            return;
        }
        scheduler.execute(() -> sendControlUpdate(client, label, control, configUpdate));
    }

    private void sendControlUpdate(AtagOneApiClient client, String channelId, ControlUpdateDTO control,
            DeviceConfigUpdateDTO configUpdate) {
        if (disposing) {
            return;
        }
        synchronized (commandLock) {
            boolean hasConfig = configUpdate.hasChanges();
            stopPollJob();
            try {
                client.updateControl(control, hasConfig ? configUpdate : null);
                if (control.ch_mode != null
                        && (control.ch_mode == CH_MODE_HOLIDAY || control.ch_mode == CH_MODE_FIREPLACE)) {
                    suppressCommErrorUntil = System.currentTimeMillis() + 5 * 60 * 1000L;
                    logger.debug("Timed preset (ch_mode={}) sent — suppressing COMMUNICATION_ERROR for 5 min",
                            control.ch_mode);
                }
            } catch (AtagOneCommunicationException e) {
                logger.warn("Command failed for {}: {}", channelId, e.getMessage());
            } catch (RuntimeException e) {
                logger.warn("Unexpected error sending command for {}: {}", channelId, e.getMessage(), e);
            } finally {
                startPollJob(POST_COMMAND_DELAY_S);
            }
        }
    }

    private void revertToLastKnownState(String channelId) {
        State currentState = stateMap.get(channelId);
        if (currentState != null) {
            updateState(channelId, currentState);
        }
    }

    @Nullable
    ScheduleDTO composeChScheduleUpdate(Command command) {
        if (!(command instanceof QuantityType<?> qt)) {
            return null;
        }
        QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
        double[][][] entries = lastChScheduleEntries;
        if (celsius == null || entries == null) {
            return null;
        }
        ScheduleDTO schedule = new ScheduleDTO();
        schedule.base_temp = celsius.doubleValue();
        schedule.entries = entries;
        return schedule;
    }

    private void sendChScheduleUpdate(AtagOneApiClient client, ScheduleDTO schedule) {
        if (disposing) {
            return;
        }
        synchronized (commandLock) {
            stopPollJob();
            try {
                client.updateChSchedule(schedule);
                lastChScheduleEntries = schedule.entries;
                updateIfChanged(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE,
                        new QuantityType<>(schedule.base_temp, SIUnits.CELSIUS));
                publishSchedule(CHANNEL_CH_SCHEDULE, schedule.base_temp, schedule.entries);
            } catch (AtagOneCommunicationException e) {
                logger.warn("CH schedule update failed: {}", e.getMessage());
            } catch (RuntimeException e) {
                logger.warn("Unexpected error updating CH schedule: {}", e.getMessage(), e);
            } finally {
                startPollJob(POST_COMMAND_DELAY_S);
            }
        }
    }

    @Nullable
    ScheduleDTO composeDhwScheduleUpdate(Command command) {
        if (!(command instanceof QuantityType<?> qt)) {
            return null;
        }
        QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
        double[][][] entries = lastDhwScheduleEntries;
        if (celsius == null || entries == null) {
            return null;
        }
        ScheduleDTO schedule = new ScheduleDTO();
        schedule.base_temp = celsius.doubleValue();
        schedule.entries = entries;
        return schedule;
    }

    private void sendDhwScheduleUpdate(AtagOneApiClient client, ScheduleDTO schedule) {
        if (disposing) {
            return;
        }
        synchronized (commandLock) {
            stopPollJob();
            try {
                client.updateDhwSchedule(schedule);
                lastDhwScheduleEntries = schedule.entries;
                updateIfChanged(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE,
                        new QuantityType<>(schedule.base_temp, SIUnits.CELSIUS));
                publishSchedule(CHANNEL_DHW_SCHEDULE, schedule.base_temp, schedule.entries);
            } catch (AtagOneCommunicationException e) {
                logger.warn("DHW schedule update failed: {}", e.getMessage());
            } catch (RuntimeException e) {
                logger.warn("Unexpected error updating DHW schedule: {}", e.getMessage(), e);
            } finally {
                startPollJob(POST_COMMAND_DELAY_S);
            }
        }
    }

    public void sendComposedChSchedule(String label, ScheduleDTO schedule) {
        if (disposing) {
            return;
        }
        AtagOneApiClient client = apiClient;
        if (client == null) {
            logger.warn("Cannot send {} — not connected", label);
            return;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Ignoring {} — Thing is not ONLINE", label);
            return;
        }
        scheduler.execute(() -> sendChScheduleUpdate(client, schedule));
    }

    public void sendComposedDhwSchedule(String label, ScheduleDTO schedule) {
        if (disposing) {
            return;
        }
        AtagOneApiClient client = apiClient;
        if (client == null) {
            logger.warn("Cannot send {} — not connected", label);
            return;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Ignoring {} — Thing is not ONLINE", label);
            return;
        }
        scheduler.execute(() -> sendDhwScheduleUpdate(client, schedule));
    }

    @Nullable
    public ScheduleDTO composeChSchedulePeriodSet(String weekday, int periodIndex, int startMinutes, int endMinutes,
            double temperatureCelsius) {
        return composeSchedulePeriodChange(lastChScheduleEntries, CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, weekday,
                periodIndex, new double[] { startMinutes, endMinutes, temperatureCelsius });
    }

    @Nullable
    public ScheduleDTO composeChSchedulePeriodClear(String weekday, int periodIndex) {
        return composeSchedulePeriodChange(lastChScheduleEntries, CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, weekday,
                periodIndex, null);
    }

    @Nullable
    public ScheduleDTO composeDhwSchedulePeriodSet(String weekday, int periodIndex, int startMinutes, int endMinutes,
            double temperatureCelsius) {
        return composeSchedulePeriodChange(lastDhwScheduleEntries, CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, weekday,
                periodIndex, new double[] { startMinutes, endMinutes, temperatureCelsius });
    }

    @Nullable
    public ScheduleDTO composeDhwSchedulePeriodClear(String weekday, int periodIndex) {
        return composeSchedulePeriodChange(lastDhwScheduleEntries, CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, weekday,
                periodIndex, null);
    }

    @Nullable
    public ScheduleDTO composeChScheduleFromJson(String json) {
        return composeScheduleFromJson(json, lastChScheduleEntries, CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE);
    }

    @Nullable
    public ScheduleDTO composeDhwScheduleFromJson(String json) {
        return composeScheduleFromJson(json, lastDhwScheduleEntries, CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE);
    }

    @Nullable
    private ScheduleDTO composeScheduleFromJson(String json, double @Nullable [][][] lastEntries,
            String baseTempChannel) {
        if (lastEntries == null) {
            return null;
        }
        State storedBaseTemp = stateMap.get(baseTempChannel);
        if (!(storedBaseTemp instanceof QuantityType<?> qt)) {
            return null;
        }
        QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
        if (celsius == null) {
            return null;
        }
        return ScheduleJson.parse(json, celsius.doubleValue(), lastEntries);
    }

    private static boolean overlapsAnyOtherPeriod(double[][] dayEntries, int excludeIndex, double[] candidate) {
        for (int i = 0; i < dayEntries.length; i++) {
            if (i == excludeIndex) {
                continue;
            }
            double[] other = dayEntries[i];
            if (ScheduleJson.periodsOverlap(candidate[0], candidate[1], other[0], other[1])) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private ScheduleDTO composeSchedulePeriodChange(double @Nullable [][][] lastEntries, String baseTempChannel,
            String weekday, int periodIndex, double @Nullable [] newPeriod) {
        Integer weekdayNumber = WEEKDAY_BY_NAME.get(weekday.toLowerCase());
        if (weekdayNumber == null || lastEntries == null || periodIndex < 0) {
            return null;
        }
        int dayIndex = weekdayNumber - 1;
        if (dayIndex < 0 || dayIndex >= lastEntries.length) {
            return null;
        }
        double @Nullable [][] dayEntries = lastEntries[dayIndex];
        if (dayEntries == null) {
            return null;
        }
        double[][] updatedDay;
        if (newPeriod != null) {
            if (periodIndex > dayEntries.length || !ScheduleJson.isValidPeriod(newPeriod[0], newPeriod[1])
                    || overlapsAnyOtherPeriod(dayEntries, periodIndex, newPeriod)) {
                return null;
            }
            updatedDay = periodIndex < dayEntries.length ? dayEntries.clone()
                    : Arrays.copyOf(dayEntries, dayEntries.length + 1);
            updatedDay[periodIndex] = newPeriod;
        } else {
            if (periodIndex >= dayEntries.length) {
                return null;
            }
            updatedDay = new double[dayEntries.length - 1][];
            System.arraycopy(dayEntries, 0, updatedDay, 0, periodIndex);
            System.arraycopy(dayEntries, periodIndex + 1, updatedDay, periodIndex, dayEntries.length - periodIndex - 1);
        }
        State storedBaseTemp = stateMap.get(baseTempChannel);
        if (!(storedBaseTemp instanceof QuantityType<?> qt)) {
            return null;
        }
        QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
        if (celsius == null) {
            return null;
        }
        double[][][] updatedEntries = lastEntries.clone();
        updatedEntries[dayIndex] = updatedDay;
        ScheduleDTO schedule = new ScheduleDTO();
        schedule.base_temp = celsius.doubleValue();
        schedule.entries = updatedEntries;
        return schedule;
    }

    boolean buildControlUpdate(String channelId, Command command, ControlUpdateDTO dto,
            DeviceConfigUpdateDTO configDto) {
        switch (channelId) {
            case CHANNEL_TARGET_TEMPERATURE:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null) {
                        return false;
                    }
                    dto.ch_mode_temp = celsius.doubleValue();
                    return true;
                }
                return false;

            case CHANNEL_CH_CONTROL_MODE:
                if (command instanceof StringType s) {
                    Integer mode = CH_CONTROL_MODE_BY_NAME.get(s.toString().toLowerCase());
                    if (mode == null) {
                        logger.warn(
                                "Unknown ch-control-mode value '{}'; valid write values: thermostat, weather-dependent",
                                s);
                        return false;
                    }
                    return composeChControlModeUpdate(mode, dto, configDto);
                }
                return false;

            case CHANNEL_VACATION_DURATION:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> seconds = qt.toUnit(Units.SECOND);
                    if (seconds == null || seconds.longValue() <= 0) {
                        return false;
                    }
                    if (!isWholeUnits(seconds.longValue(), SECONDS_PER_DAY)) {
                        logger.warn("vacation-duration must be a whole number of days, got {} s", seconds.longValue());
                        return false;
                    }
                    dto.vacation_duration = seconds.longValue();
                    return true;
                }
                return false;

            case CHANNEL_PRESET_MODE:
                if (command instanceof StringType s) {
                    String modeName = s.toString().toLowerCase();
                    Integer mode = CH_MODE_BY_NAME.get(modeName);
                    if (mode == null) {
                        logger.warn(
                                "Unknown preset-mode value '{}'; valid write values: manual, auto, holiday, extend, fireplace",
                                modeName);
                        return false;
                    }
                    if (mode == CH_MODE_MANUAL) {
                        composeManualActivation(dto);
                        return true;
                    }
                    if (mode == CH_MODE_EXTEND) {
                        composeExtendActivation(dto, null);
                        return true;
                    }
                    if (mode == CH_MODE_HOLIDAY) {
                        composeVacationActivation(dto, configDto, null);
                        return true;
                    }
                    if (mode == CH_MODE_FIREPLACE) {
                        composeFireplaceActivation(dto, null);
                        return true;
                    }
                    composeCancel(dto, configDto);
                    return true;
                }
                return false;

            case CHANNEL_VACATION_TEMPERATURE:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.ch_vacation_temp = celsius.doubleValue();
                    // ch_mode_temp only applies if ch_mode=3 is sent in the same write
                    State currentPreset = stateMap.get(CHANNEL_PRESET_MODE);
                    if (currentPreset instanceof StringType st && "holiday".equals(st.toString())) {
                        dto.ch_mode = CH_MODE_HOLIDAY;
                        dto.ch_mode_duration = 0L;
                        dto.ch_mode_temp = celsius.doubleValue();
                    }
                    return true;
                }
                return false;

            case CHANNEL_EXTEND_DURATION:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> seconds = qt.toUnit(Units.SECOND);
                    if (seconds == null || seconds.longValue() <= 0) {
                        return false;
                    }
                    if (!isWholeUnits(seconds.longValue(), SECONDS_PER_15_MINUTES)) {
                        logger.warn("extend-duration must be a whole number of 15-minute increments, got {} s",
                                seconds.longValue());
                        return false;
                    }
                    // extend_duration is additive to time until the next schedule boundary, not an absolute duration
                    dto.extend_duration = seconds.longValue();
                    return true;
                }
                return false;

            case CHANNEL_FIREPLACE_DURATION:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> seconds = qt.toUnit(Units.SECOND);
                    if (seconds == null || seconds.longValue() <= 0) {
                        return false;
                    }
                    if (!isWholeUnits(seconds.longValue(), SECONDS_PER_HOUR)) {
                        logger.warn("fireplace-duration must be a whole number of hours, got {} s",
                                seconds.longValue());
                        return false;
                    }
                    dto.fireplace_duration = seconds.longValue();
                    return true;
                }
                return false;

            case CHANNEL_FROST_PROTECTION:
                if (command instanceof StringType s) {
                    Integer mode = FROST_PROTECTION_BY_NAME.get(s.toString().toLowerCase());
                    if (mode == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.frost_prot_enabled = mode;
                    return true;
                }
                return false;

            case CHANNEL_FROST_PROTECTION_TEMPERATURE_ROOM:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.frost_prot_temp_room = celsius.doubleValue();
                    return true;
                }
                return false;

            case CHANNEL_FROST_PROTECTION_TEMPERATURE_OUTSIDE:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.frost_prot_temp_outs = celsius.doubleValue();
                    return true;
                }
                return false;

            case CHANNEL_SUMMER_ECO_MODE:
                if (command instanceof OnOffType onOff) {
                    if (!fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.summer_eco_mode = onOff == OnOffType.ON ? 1 : 0;
                    return true;
                }
                return false;

            case CHANNEL_SUMMER_ECO_TEMPERATURE:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.summer_eco_temp = celsius.doubleValue();
                    return true;
                }
                return false;

            case CHANNEL_HEATING_TYPE:
                if (command instanceof StringType s) {
                    Integer type = HEATING_TYPE_BY_NAME.get(s.toString().toLowerCase());
                    if (type == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.ch_heating_type = type;
                    return true;
                }
                return false;

            case CHANNEL_INSULATION:
                if (command instanceof StringType s) {
                    Integer insulation = INSULATION_BY_NAME.get(s.toString().toLowerCase());
                    if (insulation == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.ch_isolation = insulation;
                    return true;
                }
                return false;

            case CHANNEL_TIME_ZONE:
                if (command instanceof StringType s) {
                    // Only "berlin" is device-confirmed; others inferred from app dropdown order only
                    Integer timeZone = TIME_ZONE_BY_NAME.get(s.toString().toLowerCase());
                    if (timeZone == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.time_zone = timeZone;
                    return true;
                }
                return false;

            case CHANNEL_LANGUAGE:
                if (command instanceof StringType s) {
                    Integer language = LANGUAGE_BY_NAME.get(s.toString().toLowerCase());
                    if (language == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.language = language;
                    return true;
                }
                return false;

            case CHANNEL_BUILDING_SIZE:
                if (command instanceof StringType s) {
                    Integer size = BUILDING_SIZE_BY_NAME.get(s.toString().toLowerCase());
                    if (size == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.ch_building_size = size;
                    return true;
                }
                return false;

            case CHANNEL_WDR_TEMPERATURE_INFLUENCE:
                if (command instanceof StringType s) {
                    Integer influence = WDR_TEMPERATURE_INFLUENCE_BY_NAME.get(s.toString().toLowerCase());
                    if (influence == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.wdr_temps_influence = influence;
                    return true;
                }
                return false;

            case CHANNEL_CLIMATE_ZONE:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.climate_zone = celsius.doubleValue();
                    return true;
                }
                return false;

            case CHANNEL_MAX_PREHEAT:
                if (command instanceof StringType s) {
                    Integer minutes = MAX_PREHEAT_BY_NAME.get(s.toString().toLowerCase());
                    if (minutes == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.max_preheat = minutes;
                    return true;
                }
                return false;

            case CHANNEL_LEGIONELLA_PROTECTION:
                if (command instanceof OnOffType onOff) {
                    if (!fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.dhw_legion_enabled = onOff == OnOffType.ON ? 1 : 0;
                    return true;
                }
                return false;

            case CHANNEL_LEGIONELLA_PROTECTION_DAY:
                if (command instanceof StringType s) {
                    Integer day = WEEKDAY_BY_NAME.get(s.toString().toLowerCase());
                    if (day == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.dhw_legion_day = day;
                    return true;
                }
                return false;

            case CHANNEL_LEGIONELLA_PROTECTION_TIME:
                if (command instanceof StringType s) {
                    Integer minutes = parseTimeOfDay(s.toString());
                    if (minutes == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.dhw_legion_time = minutes;
                    return true;
                }
                return false;

            case CHANNEL_DISPLAY_BRIGHTNESS:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> percent = qt.toUnit(Units.PERCENT);
                    if (percent == null || !fillConfigBundle(configDto)) {
                        return false;
                    }
                    configDto.disp_brightness = percent.intValue();
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    boolean composeChControlModeUpdate(int controlMode, ControlUpdateDTO dto, DeviceConfigUpdateDTO configDto) {
        if (!fillConfigBundle(configDto)) {
            return false;
        }
        dto.ch_control_mode = controlMode;
        return true;
    }

    private boolean fillConfigBundle(DeviceConfigUpdateDTO configDto) {
        DeviceConfigDTO config = lastConfiguration;
        if (config == null) {
            return false;
        }
        configDto.ch_heating_type = config.ch_heating_type;
        configDto.ch_isolation = config.ch_isolation;
        configDto.ch_building_size = config.ch_building_size;
        configDto.wdr_temps_influence = config.wdr_temps_influence;
        configDto.climate_zone = config.climate_zone;
        configDto.wd_temp_offs = config.wd_temp_offs;
        configDto.summer_eco_mode = config.summer_eco_mode;
        configDto.summer_eco_temp = config.summer_eco_temp;
        configDto.frost_prot_enabled = config.frost_prot_enabled;
        configDto.frost_prot_temp_room = config.frost_prot_temp_room;
        configDto.frost_prot_temp_outs = config.frost_prot_temp_outs;
        configDto.max_preheat = config.max_preheat;
        configDto.ch_vacation_temp = config.ch_vacation_temp;
        configDto.ch_mode_vacation = config.ch_mode_vacation;
        configDto.ch_mode_extend = config.ch_mode_extend;
        configDto.time_zone = config.time_zone;
        configDto.language = config.language;
        configDto.dhw_legion_enabled = config.dhw_legion_enabled;
        configDto.dhw_legion_day = config.dhw_legion_day;
        configDto.dhw_legion_time = config.dhw_legion_time;
        return true;
    }

    void composeManualActivation(ControlUpdateDTO dto) {
        dto.ch_mode = CH_MODE_MANUAL;
        State stored = stateMap.get(CHANNEL_TARGET_TEMPERATURE);
        if (stored instanceof QuantityType<?> sq) {
            QuantityType<?> celsius = sq.toUnit(SIUnits.CELSIUS);
            if (celsius != null) {
                dto.ch_mode_temp = celsius.doubleValue();
            }
        }
    }

    public void composeExtendActivation(ControlUpdateDTO dto, @Nullable Long explicitDurationSeconds) {
        long durationSeconds;
        if (explicitDurationSeconds != null) {
            durationSeconds = explicitDurationSeconds;
        } else {
            durationSeconds = defaultExtendDurationSeconds;
            State stored = stateMap.get(CHANNEL_EXTEND_DURATION);
            if (stored instanceof QuantityType<?> sq) {
                QuantityType<?> inSeconds = sq.toUnit(Units.SECOND);
                if (inSeconds != null && inSeconds.longValue() > 0) {
                    durationSeconds = inSeconds.longValue();
                }
            }
        }
        dto.ch_mode = CH_MODE_EXTEND;
        dto.extend_duration = durationSeconds;
    }

    // ch_mode alone does not activate holiday mode; start_vacation must be sent in the same write
    public void composeVacationActivation(ControlUpdateDTO dto, DeviceConfigUpdateDTO configDto,
            @Nullable Long explicitDurationSeconds) {
        long durationSeconds;
        if (explicitDurationSeconds != null) {
            durationSeconds = explicitDurationSeconds;
        } else {
            durationSeconds = defaultVacationDurationSeconds;
            State stored = stateMap.get(CHANNEL_VACATION_DURATION);
            if (stored instanceof QuantityType<?> sq) {
                QuantityType<?> inSeconds = sq.toUnit(Units.SECOND);
                if (inSeconds != null && inSeconds.longValue() > 0) {
                    durationSeconds = inSeconds.longValue();
                } else {
                    logger.info("No vacation-duration currently stored; using device default ({} s)", durationSeconds);
                }
            } else {
                logger.info("No vacation-duration currently stored; using device default ({} s)", durationSeconds);
            }
        }
        dto.ch_mode = CH_MODE_HOLIDAY;
        dto.ch_mode_duration = durationSeconds;
        dto.vacation_duration = durationSeconds;
        configDto.start_vacation = AtagEpoch.fromZonedDateTime(ZonedDateTime.now());
    }

    // ch_mode_duration must be present; omitting it causes the boiler API subsystem to restart
    public void composeFireplaceActivation(ControlUpdateDTO dto, @Nullable Long explicitDurationSeconds) {
        long durationSeconds;
        if (explicitDurationSeconds != null) {
            durationSeconds = explicitDurationSeconds;
        } else {
            durationSeconds = 3600L;
            State stored = stateMap.get(CHANNEL_FIREPLACE_DURATION);
            if (stored instanceof QuantityType<?> sq) {
                QuantityType<?> inSeconds = sq.toUnit(Units.SECOND);
                if (inSeconds != null && inSeconds.longValue() > 0) {
                    durationSeconds = inSeconds.longValue();
                } else {
                    logger.info("No fireplace-duration available yet; defaulting to 1 hour");
                }
            } else {
                logger.info("No fireplace-duration available yet; defaulting to 1 hour");
            }
        }
        dto.ch_mode = CH_MODE_FIREPLACE;
        dto.ch_mode_duration = durationSeconds;
        dto.fireplace_duration = durationSeconds;
    }

    // Fireplace cancel requires a physical button press regardless of the API write
    public boolean composeCancel(ControlUpdateDTO dto, DeviceConfigUpdateDTO configDto) {
        dto.ch_mode = CH_MODE_AUTO;
        dto.ch_mode_duration = 0L;
        State currentPreset = stateMap.get(CHANNEL_PRESET_MODE);
        boolean reportedHoliday = currentPreset instanceof StringType st && "holiday".equals(st.toString());
        if (reportedHoliday || armedStartVacation > 0) {
            dto.vacation_duration = 0L;
            configDto.start_vacation = 0L;
        }
        if (currentPreset instanceof StringType st && "fireplace".equals(st.toString())) {
            logger.warn(
                    "Cancelling fireplace mode requires a physical button press on the thermostat display; the API write alone will not take effect");
            return true;
        }
        return false;
    }

    public static boolean isWholeUnits(long durationSeconds, long unitSeconds) {
        return durationSeconds >= unitSeconds && durationSeconds % unitSeconds == 0;
    }

    private void connect(long myGeneration) {
        if (disposing || generation != myGeneration) {
            return;
        }
        String clientId = resolveClientId();
        boolean needsPairing = clientId.isEmpty();
        if (needsPairing) {
            clientId = generateClientId();
            logger.info("Generated new client ID {}", clientId);
        }
        AtagOneApiClient client = new AtagOneApiClient(httpClient, config.hostname, config.port, clientId);
        if (disposing || generation != myGeneration) {
            return;
        }
        apiClient = client;
        if (needsPairing) {
            doPair(client, clientId, myGeneration);
        } else {
            startPollJob(0);
        }
    }

    private void doPair(AtagOneApiClient client, String clientId, long myGeneration) {
        if (disposing || generation != myGeneration) {
            return;
        }
        try {
            int accStatus = client.pair();
            if (disposing || generation != myGeneration) {
                return;
            }
            switch (accStatus) {
                case 2:
                case 0:
                    logger.info("ATAG ONE paired (acc_status={}), persisting clientId", accStatus);
                    persistClientId(clientId);
                    startPollJob(0);
                    break;
                case 1:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "@text/offline.conf-pending.press-accept");
                    if (!disposing) {
                        pairingJob = scheduler.schedule(() -> doPair(client, clientId, myGeneration), PAIRING_RETRY_S,
                                TimeUnit.SECONDS);
                    }
                    break;
                case 3:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.conf-error.pairing-denied");
                    break;
                default:
                    logger.warn("Unexpected acc_status={} during pairing", accStatus);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unexpected pairing response (acc_status=" + accStatus + ")");
            }
        } catch (AtagOneCommunicationException e) {
            if (disposing || generation != myGeneration) {
                return;
            }
            logger.debug("Pairing error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            if (!disposing) {
                pairingJob = scheduler.schedule(() -> doPair(client, clientId, myGeneration), PAIRING_RETRY_S,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void poll() {
        if (disposing) {
            return;
        }
        AtagOneApiClient client = apiClient;
        if (client == null) {
            return;
        }
        try {
            RetrieveReplyDTO r = client.retrieve();
            updateChannels(r);
            goOnline();
        } catch (AtagOneCommunicationException e) {
            logger.debug("Poll failed: {}", e.getMessage());
            goOffline(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error while processing poll response: {}", e.getMessage(), e);
            goOffline(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized void startPollJob(int initialDelaySeconds) {
        stopPollJob();
        if (disposing) {
            return;
        }
        pollJob = scheduler.scheduleWithFixedDelay(this::poll, initialDelaySeconds, config.refreshInterval,
                TimeUnit.SECONDS);
    }

    private synchronized void stopPollJob() {
        ScheduledFuture<?> job = pollJob;
        if (job != null) {
            job.cancel(false);
            pollJob = null;
        }
    }

    private void updateChannels(RetrieveReplyDTO r) {
        if (r.configuration.ch_mode_vacation > 0) {
            defaultVacationDurationSeconds = r.configuration.ch_mode_vacation;
        }
        if (r.configuration.ch_mode_extend > 0) {
            defaultExtendDurationSeconds = r.configuration.ch_mode_extend;
        }
        armedStartVacation = r.configuration.start_vacation;
        lastConfiguration = r.configuration;
        updateDeviceProperties(r);

        updateIfChanged(CHANNEL_ROOM_TEMPERATURE, new QuantityType<>(r.report.room_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_OUTSIDE_TEMPERATURE, new QuantityType<>(r.report.outside_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_CH_WATER_TEMPERATURE, new QuantityType<>(r.report.ch_water_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_CH_RETURN_TEMPERATURE, new QuantityType<>(r.report.ch_return_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_DELTA_TEMPERATURE,
                new QuantityType<>(r.report.ch_water_temp - r.report.ch_return_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_CH_WATER_PRESSURE, new QuantityType<>(r.report.ch_water_pres, Units.BAR));
        updateIfChanged(CHANNEL_CH_SETPOINT, new QuantityType<>(r.report.ch_setpoint, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_DHW_TEMPERATURE, new QuantityType<>(r.report.dhw_water_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_AVERAGE_OUTSIDE_TEMPERATURE, new QuantityType<>(r.report.tout_avg, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_PCB_TEMPERATURE, new QuantityType<>(r.report.pcb_temp, SIUnits.CELSIUS));

        boolean flame = (r.report.boiler_status & BOILER_STATUS_FLAME) != 0;
        boolean chActive = (r.report.boiler_status & BOILER_STATUS_CH_ACTIVE) != 0;
        boolean dhwActive = (r.report.boiler_status & BOILER_STATUS_DHW_ACTIVE) != 0;
        updateIfChanged(CHANNEL_FLAME, OnOffType.from(flame));
        updateIfChanged(CHANNEL_CH_ACTIVE, OnOffType.from(chActive));
        updateIfChanged(CHANNEL_DHW_ACTIVE, OnOffType.from(dhwActive));
        updateIfChanged(CHANNEL_MODULATION_LEVEL, new QuantityType<>(r.report.details.rel_mod_level, Units.PERCENT));
        updateIfChanged(CHANNEL_BURNING_HOURS, new QuantityType<>(r.report.burning_hours, Units.HOUR));
        updateIfChanged(CHANNEL_TIME_TO_TARGET,
                new QuantityType<>(r.report.ch_time_to_temp / (double) SECONDS_PER_MINUTE, Units.MINUTE));
        // Strip RSS:…; tokens the device embeds in device_errors; Gson may null the default "" if JSON carries null
        String deviceErrors = r.report.device_errors;
        updateIfChanged(CHANNEL_DEVICE_ERRORS,
                new StringType(deviceErrors == null ? "" : deviceErrors.replaceAll("RSS:[^;]*;", "").trim()));
        String boilerErrors = r.report.boiler_errors;
        updateIfChanged(CHANNEL_BOILER_ERRORS, new StringType(boilerErrors == null ? "" : boilerErrors));

        updateIfChanged(CHANNEL_WIFI_SIGNAL, new DecimalType(classifyWifiSignal(-r.report.rssi)));
        // voltage is reported in mV when > 1000, otherwise already in V (observed device inconsistency).
        double voltage = r.report.voltage > 1000 ? r.report.voltage / 1000.0 : r.report.voltage;
        updateIfChanged(CHANNEL_VOLTAGE, new QuantityType<>(voltage, Units.VOLT));
        updateIfChanged(CHANNEL_DHW_FLOW_RATE, new QuantityType<>(r.report.dhw_flow_rate, Units.LITRE_PER_MINUTE));
        updateIfChanged(CHANNEL_RESETS, new DecimalType(r.report.resets));
        updateIfChanged(CHANNEL_BOILER_TEMPERATURE, new QuantityType<>(r.report.details.boiler_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_BOILER_RETURN_TEMPERATURE,
                new QuantityType<>(r.report.details.boiler_return_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_REPORT_TIME, new DateTimeType(AtagEpoch.toZonedDateTime(r.report.report_time)));

        updateIfChanged(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE,
                new QuantityType<>(r.schedules.ch_schedule.base_temp, SIUnits.CELSIUS));
        lastChScheduleEntries = r.schedules.ch_schedule.entries;
        publishSchedule(CHANNEL_CH_SCHEDULE, r.schedules.ch_schedule.base_temp, r.schedules.ch_schedule.entries);
        updateIfChanged(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE,
                new QuantityType<>(r.schedules.dhw_schedule.base_temp, SIUnits.CELSIUS));
        lastDhwScheduleEntries = r.schedules.dhw_schedule.entries;
        publishSchedule(CHANNEL_DHW_SCHEDULE, r.schedules.dhw_schedule.base_temp, r.schedules.dhw_schedule.entries);
        updateNextScheduleChannels(r.schedules.ch_schedule.entries, ZonedDateTime.now());

        updateIfChanged(CHANNEL_TARGET_TEMPERATURE, new QuantityType<>(r.control.ch_mode_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_CH_CONTROL_MODE,
                new StringType(CH_CONTROL_MODE_NAMES.getOrDefault(r.control.ch_control_mode, "thermostat")));
        updateIfChanged(CHANNEL_PRESET_MODE, new StringType(CH_MODE_NAMES.getOrDefault(r.control.ch_mode, "manual")));
        updateIfChanged(CHANNEL_DHW_TARGET_TEMPERATURE, new QuantityType<>(r.control.dhw_temp_setp, SIUnits.CELSIUS));
        updateDhwTargetTemperatureBounds(r.configuration.dhw_min_set, r.configuration.dhw_max_set);
        updateIfChanged(CHANNEL_EXTEND_DURATION,
                new QuantityType<>(r.control.extend_duration / (double) SECONDS_PER_MINUTE, Units.MINUTE));
        updateIfChanged(CHANNEL_FIREPLACE_DURATION,
                new QuantityType<>(r.control.fireplace_duration / (double) SECONDS_PER_HOUR, Units.HOUR));
        updateIfChanged(CHANNEL_VACATION_DURATION,
                new QuantityType<>(r.control.vacation_duration / (double) SECONDS_PER_DAY, Units.DAY));
        updateIfChanged(CHANNEL_WEATHER_STATUS,
                new StringType(WEATHER_STATUS_NAMES.getOrDefault(r.control.weather_status, "unknown")));
        updateIfChanged(CHANNEL_WEATHER_TEMPERATURE, new QuantityType<>(r.control.weather_temp, SIUnits.CELSIUS));

        int mode = r.control.ch_mode;
        if (mode == CH_MODE_HOLIDAY && r.control.vacation_duration > 0 && r.configuration.start_vacation > 0) {
            ZonedDateTime vacStart = AtagEpoch.toZonedDateTime(r.configuration.start_vacation);
            ZonedDateTime vacEnd = vacStart.plusSeconds(r.control.vacation_duration);
            long remainingSeconds = Math.max(0, Duration.between(ZonedDateTime.now(), vacEnd).getSeconds());
            updateIfChanged(CHANNEL_VACATION_START, new DateTimeType(vacStart));
            updateIfChanged(CHANNEL_VACATION_END, new DateTimeType(vacEnd));
            updateIfChanged(CHANNEL_VACATION_REMAINING, new QuantityType<>(remainingSeconds, Units.SECOND));
            updateIfChanged(CHANNEL_VACATION_TEMPERATURE, new QuantityType<>(r.control.ch_mode_temp, SIUnits.CELSIUS));
            updateIfChanged(CHANNEL_EXTEND_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, UnDefType.UNDEF);
        } else if (mode == CH_MODE_EXTEND) {
            updateIfChanged(CHANNEL_VACATION_START, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_END, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_TEMPERATURE,
                    new QuantityType<>(r.configuration.ch_vacation_temp, SIUnits.CELSIUS));
            updateIfChanged(CHANNEL_EXTEND_REMAINING, new QuantityType<>(r.control.ch_mode_duration, Units.SECOND));
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, UnDefType.UNDEF);
        } else if (mode == CH_MODE_FIREPLACE) {
            updateIfChanged(CHANNEL_VACATION_START, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_END, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_TEMPERATURE,
                    new QuantityType<>(r.configuration.ch_vacation_temp, SIUnits.CELSIUS));
            updateIfChanged(CHANNEL_EXTEND_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, new QuantityType<>(r.control.ch_mode_duration, Units.SECOND));
        } else {
            updateIfChanged(CHANNEL_VACATION_START, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_END, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_TEMPERATURE,
                    new QuantityType<>(r.configuration.ch_vacation_temp, SIUnits.CELSIUS));
            updateIfChanged(CHANNEL_EXTEND_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, UnDefType.UNDEF);
        }

        DeviceConfigDTO config = r.configuration;
        updateIfChanged(CHANNEL_FROST_PROTECTION,
                new StringType(FROST_PROTECTION_NAMES.getOrDefault(config.frost_prot_enabled, "unknown")));
        updateIfChanged(CHANNEL_FROST_PROTECTION_TEMPERATURE_ROOM,
                new QuantityType<>(config.frost_prot_temp_room, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_FROST_PROTECTION_TEMPERATURE_OUTSIDE,
                new QuantityType<>(config.frost_prot_temp_outs, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_SUMMER_ECO_MODE, OnOffType.from(config.summer_eco_mode == 1));
        updateIfChanged(CHANNEL_SUMMER_ECO_TEMPERATURE, new QuantityType<>(config.summer_eco_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_HEATING_TYPE,
                new StringType(HEATING_TYPE_NAMES.getOrDefault(config.ch_heating_type, "unknown")));
        updateIfChanged(CHANNEL_INSULATION,
                new StringType(INSULATION_NAMES.getOrDefault(config.ch_isolation, "unknown")));
        updateIfChanged(CHANNEL_BUILDING_SIZE,
                new StringType(BUILDING_SIZE_NAMES.getOrDefault(config.ch_building_size, "unknown")));
        updateIfChanged(CHANNEL_WDR_TEMPERATURE_INFLUENCE,
                new StringType(WDR_TEMPERATURE_INFLUENCE_NAMES.getOrDefault(config.wdr_temps_influence, "unknown")));
        updateIfChanged(CHANNEL_CLIMATE_ZONE, new QuantityType<>(config.climate_zone, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_MAX_PREHEAT,
                new StringType(MAX_PREHEAT_NAMES.getOrDefault(config.max_preheat, "unknown")));
        updateIfChanged(CHANNEL_LEGIONELLA_PROTECTION, OnOffType.from(config.dhw_legion_enabled == 1));
        updateIfChanged(CHANNEL_LEGIONELLA_PROTECTION_DAY,
                new StringType(WEEKDAY_NAMES.getOrDefault(config.dhw_legion_day, "unknown")));
        updateIfChanged(CHANNEL_LEGIONELLA_PROTECTION_TIME, new StringType(formatTimeOfDay(config.dhw_legion_time)));
        updateIfChanged(CHANNEL_DISPLAY_BRIGHTNESS, new QuantityType<>(config.disp_brightness, Units.PERCENT));
        updateIfChanged(CHANNEL_TIME_ZONE, new StringType(TIME_ZONE_NAMES.getOrDefault(config.time_zone, "unknown")));
        updateIfChanged(CHANNEL_LANGUAGE, new StringType(LANGUAGE_NAMES.getOrDefault(config.language, "unknown")));
    }

    private void updateIfChanged(String channelId, State state) {
        State previous = stateMap.put(channelId, state);
        if (!state.equals(previous)) {
            updateState(channelId, state);
        }
    }

    private void publishSchedule(String channelId, double baseTemp, double @Nullable [][][] entries) {
        if (entries == null) {
            updateIfChanged(channelId, UnDefType.UNDEF);
            return;
        }
        updateIfChanged(channelId, new StringType(ScheduleJson.toJson(baseTemp, entries)));
    }

    private void goOnline() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void goOffline(ThingStatusDetail detail, @Nullable String reason) {
        if (detail == ThingStatusDetail.COMMUNICATION_ERROR && System.currentTimeMillis() < suppressCommErrorUntil) {
            if (getThing().getStatus() != ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.UNKNOWN);
            }
            return;
        }
        updateStatus(ThingStatus.OFFLINE, detail, reason);
    }

    void updateNextScheduleChannels(double[][][] entries, ZonedDateTime now) {
        int minutesNow = now.getHour() * 60 + now.getMinute();
        for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
            ZonedDateTime day = now.plusDays(dayOffset);
            int weekdayIndex = day.getDayOfWeek().getValue() - 1; // entries[0] = Monday
            if (weekdayIndex >= entries.length) {
                continue;
            }
            double @Nullable [] nextEntry = null;
            for (double[] entry : entries[weekdayIndex]) {
                if (dayOffset == 0 && entry[0] <= minutesNow) {
                    continue;
                }
                if (nextEntry == null || entry[0] < nextEntry[0]) {
                    nextEntry = entry;
                }
            }
            if (nextEntry != null) {
                ZonedDateTime time = day.truncatedTo(ChronoUnit.DAYS).plusMinutes((long) nextEntry[0]);
                updateIfChanged(CHANNEL_NEXT_SCHEDULE_TIME, new DateTimeType(time));
                updateIfChanged(CHANNEL_NEXT_SCHEDULE_TEMPERATURE, new QuantityType<>(nextEntry[2], SIUnits.CELSIUS));
                return;
            }
        }
        updateIfChanged(CHANNEL_NEXT_SCHEDULE_TIME, UnDefType.UNDEF);
        updateIfChanged(CHANNEL_NEXT_SCHEDULE_TEMPERATURE, UnDefType.UNDEF);
    }

    private void updateDeviceProperties(RetrieveReplyDTO r) {
        if (!r.status.device_id.isEmpty()) {
            updateProperty(PROPERTY_DEVICE_ID, r.status.device_id);
        }
        if (!r.configuration.boiler_id.isEmpty()) {
            updateProperty(Thing.PROPERTY_SERIAL_NUMBER, r.configuration.boiler_id);
        }
        if (!r.configuration.installer_id.isEmpty()) {
            updateProperty(PROPERTY_INSTALLER_ID, r.configuration.installer_id);
        }
        updateProperty(Thing.PROPERTY_VENDOR, "ATAG");
        String firmwareVersion = parseFirmwareVersion(r.configuration.download_url);
        if (firmwareVersion != null) {
            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        }
    }

    private void updateDhwTargetTemperatureBounds(double min, double max) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_DHW_TARGET_TEMPERATURE);
        StateDescription description = StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.valueOf(min))
                .withMaximum(BigDecimal.valueOf(max)).withStep(BigDecimal.valueOf(0.5)).withPattern("%.1f %unit%")
                .build().toStateDescription();
        if (description != null) {
            stateDescriptionProvider.setDescription(channelUID, description);
        }
    }

    private static @Nullable String parseFirmwareVersion(String downloadUrl) {
        int lastSlash = downloadUrl.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == downloadUrl.length() - 1) {
            return null;
        }
        return downloadUrl.substring(lastSlash + 1);
    }

    // Buckets dBm into 0-4; raw dBm via Number:Power renders as watts in OH widgets
    private static int classifyWifiSignal(int dbm) {
        if (dbm >= -50) {
            return 4;
        } else if (dbm >= -60) {
            return 3;
        } else if (dbm >= -70) {
            return 2;
        } else if (dbm >= -80) {
            return 1;
        } else {
            return 0;
        }
    }

    private static String formatTimeOfDay(int minutesSinceMidnight) {
        int clamped = Math.max(0, Math.min(1439, minutesSinceMidnight));
        return String.format("%02d:%02d", clamped / 60, clamped % 60);
    }

    @Nullable
    static Integer parseTimeOfDay(String hhMm) {
        int colon = hhMm.indexOf(':');
        if (colon < 0) {
            return null;
        }
        try {
            int hours = Integer.parseInt(hhMm.substring(0, colon));
            int minutes = Integer.parseInt(hhMm.substring(colon + 1));
            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                return null;
            }
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveClientId() {
        if (!config.clientId.isBlank()) {
            return config.clientId;
        }
        String prop = getThing().getProperties().get(PROPERTY_CLIENT_ID);
        return prop != null ? prop : "";
    }

    private void persistClientId(String clientId) {
        // updateConfiguration() would trigger dispose()+initialize() on a managed Thing — use property instead
        updateProperty(PROPERTY_CLIENT_ID, clientId);
    }

    private static String generateClientId() {
        byte[] bytes = new byte[6];
        CLIENT_ID_RANDOM.nextBytes(bytes);
        bytes[0] = (byte) ((bytes[0] | 0x02) & 0xFE);
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X", bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
                bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }
}
