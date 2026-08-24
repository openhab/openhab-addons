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

import java.security.SecureRandom;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.atagone.internal.api.AtagEpoch;
import org.openhab.binding.atagone.internal.api.AtagOneApiClient;
import org.openhab.binding.atagone.internal.api.AtagOneCommunicationException;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
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
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

    private final Logger logger = LoggerFactory.getLogger(AtagOneHandler.class);
    private final HttpClient httpClient;

    private AtagOneConfiguration config = new AtagOneConfiguration();
    private @Nullable AtagOneApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollJob;
    private @Nullable ScheduledFuture<?> pairingJob;
    private volatile boolean disposing = false;

    private final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    // After a timed-preset write (ch_mode 3=holiday or 5=fireplace) the boiler API reinitializes for
    // several minutes. During this window, communication errors are suppressed so the Thing stays
    // UNKNOWN rather than OFFLINE.
    private volatile long suppressCommErrorUntil = 0L;

    public AtagOneHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void initialize() {
        disposing = false;
        config = getConfigAs(AtagOneConfiguration.class);
        if (config.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.hostname-missing");
            return;
        }
        stateMap.clear();
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::connect);
    }

    @Override
    public void dispose() {
        disposing = true;
        stopPollJob();
        ScheduledFuture<?> pairing = pairingJob;
        if (pairing != null) {
            pairing.cancel(true);
            pairingJob = null;
        }
    }

    // ── Command handling ──────────────────────────────────────────────────────

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
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        if (!buildControlUpdate(channelId, command, control, configUpdate)) {
            logger.debug("Unhandled command {} for channel {}", command, channelId);
            // Push the last known good state back so the item doesn't stick at the rejected value.
            State currentState = stateMap.get(channelId);
            if (currentState != null) {
                updateState(channelId, currentState);
            }
            return;
        }

        // The actual write is a blocking HTTP call (rate-limited + retried, up to ~12 s) — never run
        // it on the calling thread, which may be a shared openHAB event-bus thread.
        scheduler.execute(() -> sendControlUpdate(client, channelId, control, configUpdate));
    }

    /**
     * Sends a control/configuration update and restarts polling afterwards.
     * Synchronized against {@link #startPollJob} / {@link #stopPollJob} (via the same intrinsic lock)
     * so two commands handled concurrently cannot interleave the stop/write/start sequence.
     */
    private synchronized void sendControlUpdate(AtagOneApiClient client, String channelId, ControlUpdateDTO control,
            DeviceConfigUpdateDTO configUpdate) {
        boolean hasConfig = configUpdate.hasChanges();
        stopPollJob();
        try {
            client.updateControl(control, hasConfig ? configUpdate : null);
            // Timed-preset writes (vacation, fireplace) trigger a boiler API reinitialization lasting
            // several minutes. Suppress COMMUNICATION_ERROR during that window so the Thing stays
            // UNKNOWN rather than OFFLINE.
            if (control.ch_mode != null
                    && (control.ch_mode == CH_MODE_HOLIDAY || control.ch_mode == CH_MODE_FIREPLACE)) {
                suppressCommErrorUntil = System.currentTimeMillis() + 5 * 60 * 1000L;
                logger.debug("Timed preset (ch_mode={}) sent — suppressing COMMUNICATION_ERROR for 5 min",
                        control.ch_mode);
            }
        } catch (AtagOneCommunicationException e) {
            logger.warn("Command failed for {}: {}", channelId, e.getMessage());
        }
        startPollJob(POST_COMMAND_DELAY_S);
    }

    /**
     * Translates a channel command into the {@code control}/{@code configuration} DTO fields to send.
     * Encodes the binding's write-path business rules: which preset-mode values are accepted, and how
     * the device's "mode and its duration must be sent together" protocol quirk is composed.
     * <p>
     * Package-private (not private) so {@code AtagOneHandlerTest} can exercise it directly without
     * going through a live device or a full openHAB command dispatch.
     *
     * @param channelId the channel the command was sent to
     * @param command the command to translate
     * @param dto control fields to populate; left untouched if the command is rejected
     * @param configDto configuration fields to populate; left untouched if the command is rejected
     * @return {@code true} if the command was understood and {@code dto}/{@code configDto} were
     *         populated; {@code false} if the command should be rejected and the channel state
     *         reverted
     */
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

            case CHANNEL_HVAC_MODE:
                if (command instanceof StringType s) {
                    dto.ch_control_mode = "auto".equalsIgnoreCase(s.toString()) ? CH_CONTROL_MODE_AUTO
                            : CH_CONTROL_MODE_HEAT;
                    return true;
                }
                return false;

            case CHANNEL_VACATION_DURATION:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> seconds = qt.toUnit(Units.SECOND);
                    if (seconds == null || seconds.longValue() <= 0) {
                        return false;
                    }
                    dto.ch_mode = CH_MODE_HOLIDAY;
                    dto.ch_mode_duration = seconds.longValue();
                    dto.vacation_duration = seconds.longValue();
                    configDto.start_vacation = AtagEpoch.fromZonedDateTime(ZonedDateTime.now());
                    return true;
                }
                return false;

            case CHANNEL_PRESET_MODE:
                if (command instanceof StringType s) {
                    String modeName = s.toString().toLowerCase();
                    Integer mode = CH_MODE_BY_NAME.get(modeName);
                    if (mode == null) {
                        logger.warn("Unknown preset-mode value '{}'; valid write values: auto, holiday, fireplace",
                                modeName);
                        return false;
                    }
                    // ch_mode=1 (manual) is not writable via the local API — the boiler rejects it
                    // and restarts its API subsystem. Manual is a read-only state set by the device.
                    if (mode == CH_MODE_MANUAL) {
                        logger.warn(
                                "preset-mode=manual cannot be written via the API; send auto to cancel timed modes");
                        return false;
                    }
                    // extend activates automatically via target-temperature write in auto mode.
                    if (mode == CH_MODE_EXTEND) {
                        logger.warn(
                                "preset-mode=extend cannot be set directly; write target-temperature in auto mode to activate extend");
                        return false;
                    }
                    if (mode == CH_MODE_HOLIDAY) {
                        // Use stored vacation-duration or default to 7 days.
                        long durationSeconds = 7 * 86400L;
                        boolean usedStoredDuration = false;
                        State stored = stateMap.get(CHANNEL_VACATION_DURATION);
                        if (stored instanceof QuantityType<?> sq) {
                            QuantityType<?> inSeconds = sq.toUnit(Units.SECOND);
                            if (inSeconds != null && inSeconds.longValue() > 0) {
                                durationSeconds = inSeconds.longValue();
                                usedStoredDuration = true;
                            }
                        }
                        if (!usedStoredDuration) {
                            logger.info("No vacation-duration set, defaulting to 7 days");
                        }
                        dto.ch_mode = CH_MODE_HOLIDAY;
                        dto.ch_mode_duration = durationSeconds;
                        dto.vacation_duration = durationSeconds;
                        configDto.start_vacation = AtagEpoch.fromZonedDateTime(ZonedDateTime.now());
                        return true;
                    }
                    dto.ch_mode = mode;
                    dto.ch_mode_duration = 0L;
                    // Leaving vacation mode — clear the vacation schedule on the device.
                    State currentPreset = stateMap.get(CHANNEL_PRESET_MODE);
                    if (currentPreset instanceof StringType st && "holiday".equals(st.toString())) {
                        dto.vacation_duration = 0L;
                        configDto.start_vacation = 0L;
                    }
                    return true;
                }
                return false;

            case CHANNEL_VACATION_TEMPERATURE:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null) {
                        return false;
                    }
                    configDto.ch_vacation_temp = celsius.doubleValue();
                    // When currently in holiday mode, also update the active setpoint.
                    // Device ignores ch_mode_temp unless ch_mode=3 is sent in the same request.
                    State currentPreset = stateMap.get(CHANNEL_PRESET_MODE);
                    if (currentPreset instanceof StringType st && "holiday".equals(st.toString())) {
                        dto.ch_mode = CH_MODE_HOLIDAY;
                        dto.ch_mode_duration = 0L;
                        dto.ch_mode_temp = celsius.doubleValue();
                    }
                    return true;
                }
                return false;

            case CHANNEL_DHW_TARGET_TEMPERATURE:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> celsius = qt.toUnit(SIUnits.CELSIUS);
                    if (celsius == null) {
                        return false;
                    }
                    dto.dhw_temp_setp = celsius.doubleValue();
                    return true;
                }
                return false;

            case CHANNEL_EXTEND_DURATION:
                // Extend mode activates automatically when target-temperature is written while
                // the device is in auto mode. There is no API write path to activate it explicitly.
                logger.warn("extend-duration is read-only; to activate extend, write target-temperature in auto mode");
                return false;

            case CHANNEL_FIREPLACE_DURATION:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> seconds = qt.toUnit(Units.SECOND);
                    if (seconds == null) {
                        return false;
                    }
                    // Activate for the written duration and update the stored default simultaneously.
                    // ch_mode_duration=<value> avoids the API restart that a missing field causes.
                    dto.fireplace_duration = seconds.longValue();
                    dto.ch_mode = CH_MODE_FIREPLACE;
                    dto.ch_mode_duration = seconds.longValue();
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    // ── Connection / pairing ──────────────────────────────────────────────────

    private void connect() {
        if (disposing) {
            return;
        }
        String clientId = resolveClientId();
        boolean needsPairing = clientId.isEmpty();
        if (needsPairing) {
            clientId = generateClientId();
            logger.info("Generated new client ID {}", clientId);
        }
        AtagOneApiClient client = new AtagOneApiClient(httpClient, config.hostname, config.port, clientId);
        apiClient = client;
        if (needsPairing) {
            doPair(client, clientId);
        } else {
            startPollJob(0);
        }
    }

    private void doPair(AtagOneApiClient client, String clientId) {
        if (disposing) {
            return;
        }
        try {
            int accStatus = client.pair();
            switch (accStatus) {
                case 2: // explicitly granted
                case 0: // open-LAN firmware — auto-accepted without user prompt
                    logger.info("ATAG ONE paired (acc_status={}), persisting clientId", accStatus);
                    persistClientId(clientId);
                    startPollJob(0);
                    break;
                case 1: // pending — user must press Accept on the thermostat display
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "@text/offline.conf-pending.press-accept");
                    if (!disposing) {
                        pairingJob = scheduler.schedule(() -> doPair(client, clientId), PAIRING_RETRY_S,
                                TimeUnit.SECONDS);
                    }
                    break;
                case 3: // denied — terminal, no retry
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.conf-error.pairing-denied");
                    break;
                default:
                    logger.warn("Unexpected acc_status={} during pairing", accStatus);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unexpected pairing response (acc_status=" + accStatus + ")");
            }
        } catch (AtagOneCommunicationException e) {
            logger.debug("Pairing error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            if (!disposing) {
                pairingJob = scheduler.schedule(() -> doPair(client, clientId), PAIRING_RETRY_S, TimeUnit.SECONDS);
            }
        }
    }

    // ── Polling ───────────────────────────────────────────────────────────────

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
            // Never let an unexpected defect (e.g. a malformed reply) kill scheduleWithFixedDelay —
            // an uncaught exception here would silently and permanently stop all future polls.
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

    // ── Channel updates ───────────────────────────────────────────────────────

    private void updateChannels(RetrieveReplyDTO r) {
        // Report — temperatures
        updateIfChanged(CHANNEL_ROOM_TEMPERATURE, new QuantityType<>(r.report.room_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_OUTSIDE_TEMPERATURE, new QuantityType<>(r.report.outside_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_CH_WATER_TEMPERATURE, new QuantityType<>(r.report.ch_water_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_CH_RETURN_TEMPERATURE, new QuantityType<>(r.report.ch_return_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_CH_WATER_PRESSURE, new QuantityType<>(r.report.ch_water_pres, Units.BAR));
        updateIfChanged(CHANNEL_CH_SETPOINT, new QuantityType<>(r.report.ch_setpoint, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_DHW_TEMPERATURE, new QuantityType<>(r.report.dhw_water_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_SHOWN_SET_TEMPERATURE, new QuantityType<>(r.report.shown_set_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_AVERAGE_OUTSIDE_TEMPERATURE, new QuantityType<>(r.report.tout_avg, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_PCB_TEMPERATURE, new QuantityType<>(r.report.pcb_temp, SIUnits.CELSIUS));

        // Report — boiler state
        boolean flame = (r.report.boiler_status & BOILER_STATUS_FLAME) != 0;
        boolean chActive = (r.report.boiler_status & BOILER_STATUS_CH_ACTIVE) != 0;
        boolean dhwActive = (r.report.boiler_status & BOILER_STATUS_DHW_ACTIVE) != 0;
        updateIfChanged(CHANNEL_FLAME, OnOffType.from(flame));
        updateIfChanged(CHANNEL_BURNER_TARGET, new StringType(dhwActive ? "dhw" : chActive ? "ch" : "none"));
        updateIfChanged(CHANNEL_MODULATION_LEVEL, new QuantityType<>(r.report.details.rel_mod_level, Units.PERCENT));
        updateIfChanged(CHANNEL_BURNING_HOURS, new QuantityType<>(r.report.burning_hours, Units.HOUR));
        updateIfChanged(CHANNEL_TIME_TO_TARGET, new QuantityType<>(r.report.ch_time_to_temp, Units.SECOND));
        // Strip RSS:…; tokens — the device embeds RSSI as a pseudo-error entry; the dedicated
        // wifi-signal channel already exposes the same value from the proper rssi field.
        // Gson overwrites the "" field initializer with null when the JSON carries an explicit null.
        String deviceErrors = r.report.device_errors;
        updateIfChanged(CHANNEL_DEVICE_ERRORS,
                new StringType(deviceErrors == null ? "" : deviceErrors.replaceAll("RSS:[^;]*;", "").trim()));
        String boilerErrors = r.report.boiler_errors;
        updateIfChanged(CHANNEL_BOILER_ERRORS, new StringType(boilerErrors == null ? "" : boilerErrors));

        // Report — advanced diagnostics
        updateIfChanged(CHANNEL_WIFI_SIGNAL, new QuantityType<>(-r.report.rssi, Units.DECIBEL_MILLIWATTS));
        // voltage is reported in mV when > 1000, otherwise already in V (observed device inconsistency).
        double voltage = r.report.voltage > 1000 ? r.report.voltage / 1000.0 : r.report.voltage;
        updateIfChanged(CHANNEL_VOLTAGE, new QuantityType<>(voltage, Units.VOLT));
        updateIfChanged(CHANNEL_CURRENT, new DecimalType(r.report.current));
        updateIfChanged(CHANNEL_POWER_CONSUMPTION, new DecimalType(r.report.power_cons));
        updateIfChanged(CHANNEL_DHW_FLOW_RATE, new DecimalType(r.report.dhw_flow_rate));
        updateIfChanged(CHANNEL_RESETS, new DecimalType(r.report.resets));
        updateIfChanged(CHANNEL_MEMORY_ALLOCATION, new DecimalType(r.report.memory_allocation));
        updateIfChanged(CHANNEL_BOILER_TEMPERATURE, new QuantityType<>(r.report.details.boiler_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_BOILER_RETURN_TEMPERATURE,
                new QuantityType<>(r.report.details.boiler_return_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_MODULATION_MIN, new QuantityType<>(r.report.details.min_mod_level, Units.PERCENT));
        updateIfChanged(CHANNEL_MAX_BOILER_TEMPERATURE,
                new QuantityType<>(r.report.details.max_boiler_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_REPORT_TIME, new DateTimeType(AtagEpoch.toZonedDateTime(r.report.report_time)));

        // Control — setpoints and modes
        updateIfChanged(CHANNEL_TARGET_TEMPERATURE, new QuantityType<>(r.control.ch_mode_temp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_HVAC_MODE,
                new StringType(CH_CONTROL_MODE_NAMES.getOrDefault(r.control.ch_control_mode, "heat")));
        updateIfChanged(CHANNEL_PRESET_MODE, new StringType(CH_MODE_NAMES.getOrDefault(r.control.ch_mode, "manual")));
        int modeForDuration = r.control.ch_mode;
        if (modeForDuration == CH_MODE_EXTEND || modeForDuration == CH_MODE_FIREPLACE
                || modeForDuration == CH_MODE_HOLIDAY) {
            updateIfChanged(CHANNEL_PRESET_MODE_DURATION, new QuantityType<>(r.control.ch_mode_duration, Units.SECOND));
        } else {
            updateIfChanged(CHANNEL_PRESET_MODE_DURATION, UnDefType.UNDEF);
        }
        updateIfChanged(CHANNEL_DHW_TARGET_TEMPERATURE, new QuantityType<>(r.control.dhw_temp_setp, SIUnits.CELSIUS));
        updateIfChanged(CHANNEL_DHW_MODE, new DecimalType(r.control.dhw_mode));
        updateIfChanged(CHANNEL_EXTEND_DURATION, new QuantityType<>(r.control.extend_duration, Units.SECOND));
        updateIfChanged(CHANNEL_FIREPLACE_DURATION, new QuantityType<>(r.control.fireplace_duration, Units.SECOND));
        updateIfChanged(CHANNEL_WEATHER_STATUS,
                new StringType(WEATHER_STATUS_NAMES.getOrDefault(r.control.weather_status, "unknown")));

        // Vacation / extend / fireplace remaining duration
        int mode = r.control.ch_mode;
        if (mode == CH_MODE_HOLIDAY && r.control.vacation_duration > 0 && r.configuration.start_vacation > 0) {
            ZonedDateTime vacStart = AtagEpoch.toZonedDateTime(r.configuration.start_vacation);
            ZonedDateTime vacEnd = vacStart.plusSeconds(r.control.vacation_duration);
            long remainingSeconds = Math.max(0, Duration.between(ZonedDateTime.now(), vacEnd).getSeconds());
            updateIfChanged(CHANNEL_VACATION_DURATION, new QuantityType<>(r.control.vacation_duration, Units.SECOND));
            updateIfChanged(CHANNEL_VACATION_START, new DateTimeType(vacStart));
            updateIfChanged(CHANNEL_VACATION_END, new DateTimeType(vacEnd));
            updateIfChanged(CHANNEL_VACATION_REMAINING, new QuantityType<>(remainingSeconds, Units.SECOND));
            updateIfChanged(CHANNEL_VACATION_TEMPERATURE, new QuantityType<>(r.control.ch_mode_temp, SIUnits.CELSIUS));
            updateIfChanged(CHANNEL_EXTEND_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, UnDefType.UNDEF);
        } else if (mode == CH_MODE_EXTEND) {
            updateIfChanged(CHANNEL_VACATION_DURATION, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_START, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_END, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_EXTEND_REMAINING, new QuantityType<>(r.control.ch_mode_duration, Units.SECOND));
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, UnDefType.UNDEF);
        } else if (mode == CH_MODE_FIREPLACE) {
            updateIfChanged(CHANNEL_VACATION_DURATION, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_START, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_END, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_EXTEND_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, new QuantityType<>(r.control.ch_mode_duration, Units.SECOND));
        } else {
            updateIfChanged(CHANNEL_VACATION_DURATION, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_START, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_END, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_TEMPERATURE,
                    new QuantityType<>(r.configuration.ch_vacation_temp, SIUnits.CELSIUS));
            updateIfChanged(CHANNEL_EXTEND_REMAINING, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, UnDefType.UNDEF);
        }
    }

    private void updateIfChanged(String channelId, State state) {
        State previous = stateMap.put(channelId, state);
        if (!state.equals(previous)) {
            updateState(channelId, state);
        }
    }

    // ── Status helpers ────────────────────────────────────────────────────────

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

    // ── Client ID lifecycle ───────────────────────────────────────────────────

    private String resolveClientId() {
        if (!config.clientId.isBlank()) {
            return config.clientId;
        }
        String prop = getThing().getProperties().get(PROPERTY_CLIENT_ID);
        return prop != null ? prop : "";
    }

    private void persistClientId(String clientId) {
        // Thing properties persist for both managed and textually configured Things, and
        // resolveClientId() already checks them as a fallback. Deliberately NOT also written via
        // updateConfiguration()/editConfiguration(): on a managed Thing that round-trips through
        // dispose()+initialize(), tearing this handler down again right after pairing succeeds.
        updateProperty(PROPERTY_CLIENT_ID, clientId);
    }

    private static String generateClientId() {
        byte[] bytes = new byte[6];
        CLIENT_ID_RANDOM.nextBytes(bytes);
        // Locally-administered, unicast MAC-style identifier.
        bytes[0] = (byte) ((bytes[0] | 0x02) & 0xFE);
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X", bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
                bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }
}
