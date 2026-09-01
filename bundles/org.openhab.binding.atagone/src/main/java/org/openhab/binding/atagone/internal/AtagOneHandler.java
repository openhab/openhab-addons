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
import org.openhab.core.thing.binding.ThingHandlerService;
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

    // A non-whole-unit duration doesn't fail safely on the device — it triggers the same
    // physical-confirmation/reboot pathway as a real cancel. Public so AtagOneActions can share the limit.
    public static final long SECONDS_PER_HOUR = 3600L;
    public static final long SECONDS_PER_DAY = 86400L;

    private final Logger logger = LoggerFactory.getLogger(AtagOneHandler.class);
    private final HttpClient httpClient;

    private AtagOneConfiguration config = new AtagOneConfiguration();
    private @Nullable AtagOneApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollJob;
    private @Nullable ScheduledFuture<?> pairingJob;
    private @Nullable Future<?> connectJob;
    private volatile boolean disposing = false;

    // Bumped on every initialize(); connect()/doPair() re-check it after each blocking call so a task
    // from a superseded generation (e.g. a rapid dispose+reinitialize) can't mutate stale state.
    private volatile long generation = 0L;

    // Guards only sendControlUpdate()'s stop/write/start sequence — deliberately not the lifecycle lock
    // (startPollJob/stopPollJob stay synchronized on `this`), since sendControlUpdate() holds this
    // across a blocking HTTP call and sharing the lifecycle lock would make dispose() block on it.
    private final Object commandLock = new Object();

    private final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    // A timed-preset write reinitializes the boiler's API for several minutes; communication errors are
    // suppressed until this deadline so the Thing stays UNKNOWN rather than OFFLINE.
    private volatile long suppressCommErrorUntil = 0L;

    // Device's persisted default vacation/extend durations, tracked unconditionally on every poll (see
    // updateChannels()) so "reuse the last duration" survives the mode ending.
    private volatile long defaultVacationDurationSeconds = 7 * 86400L;
    private volatile long defaultExtendDurationSeconds = 3600L;

    // A pending (future-scheduled) vacation reports preset-mode=auto, not holiday, so composeCancel()
    // can't rely on reported mode alone to detect an armed schedule — tracked separately, every poll.
    private volatile long armedStartVacation = 0L;

    public AtagOneHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
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

        // The write is a blocking HTTP call (rate-limited + retried, up to ~12 s) — never on the caller's thread.
        scheduler.execute(() -> sendControlUpdate(client, channelId, control, configUpdate));
    }

    /**
     * Entry point for {@link AtagOneActions}: dispatches an already-composed update with the same
     * checks as {@link #handleCommand}, without its {@code ChannelUID}/{@code Command} plumbing.
     * {@code label} is used only for logging.
     */
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

    /**
     * Sends a control/configuration update and restarts polling afterwards. Serialized on
     * {@link #commandLock} so concurrent commands can't interleave the stop/write/start sequence;
     * {@code startPollJob} runs in a {@code finally} so no write exception leaves polling disabled.
     */
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
                /*
                 * Timed-preset writes (vacation, fireplace) trigger a boiler API reinitialization
                 * lasting several minutes. Suppress COMMUNICATION_ERROR during that window so the
                 * Thing stays UNKNOWN rather than OFFLINE.
                 */
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

            case CHANNEL_CH_CONTROL_MODE:
                /*
                 * Room-vs-weather control is a system/installer-level setting, not a routine runtime
                 * toggle, and writing it via the local API has not been verified as safe. Kept
                 * read-only until that changes.
                 */
                logger.warn("ch-control-mode is read-only; change room/weather control on the thermostat itself");
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
                    /*
                     * Value-setter only — does not activate holiday mode. The device treats
                     * vacation_duration written alone as updating the stored value without changing
                     * ch_mode. preset-mode=holiday is the sole activation trigger (see its case
                     * below), or use the activateVacation action for a single-write custom-duration
                     * activation.
                     */
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
                                "Unknown preset-mode value '{}'; valid write values: auto, holiday, extend, fireplace",
                                modeName);
                        return false;
                    }
                    /*
                     * ch_mode=1 (manual) is not writable via the local API — writing it is believed to
                     * make the boiler restart its API subsystem. Manual is treated as a read-only
                     * state, set by the device itself when the user adjusts the temperature on the
                     * display. This is a conservative safety choice: the risk has not been thoroughly
                     * re-verified, so rejection stays in place unless that changes.
                     */
                    if (mode == CH_MODE_MANUAL) {
                        logger.warn(
                                "preset-mode=manual cannot be written via the API; send auto to cancel timed modes");
                        return false;
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
                    // mode == CH_MODE_AUTO — cancel whatever timed preset (if any) is currently active.
                    composeCancel(dto, configDto);
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
                // control.dhw_temp_setp is read-only/derived — confirmed live: writing it is silently
                // accepted but never changes the value, which tracks the active schedule instead.
                logger.warn("dhw-target-temperature is read-only; the device has no direct control field for it");
                return false;

            case CHANNEL_EXTEND_DURATION:
                if (command instanceof QuantityType<?> qt) {
                    QuantityType<?> seconds = qt.toUnit(Units.SECOND);
                    if (seconds == null || seconds.longValue() <= 0) {
                        return false;
                    }
                    if (!isWholeUnits(seconds.longValue(), SECONDS_PER_HOUR)) {
                        logger.warn("extend-duration must be a whole number of hours, got {} s", seconds.longValue());
                        return false;
                    }
                    /*
                     * Value-setter only — does not activate extend mode. extend_duration is additive
                     * to the time remaining until the next schedule boundary, not an absolute session
                     * length (see composeExtendActivation()). preset-mode=extend is the sole
                     * activation trigger, or use the activateExtend action for a single-write custom
                     * activation.
                     */
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
                    // Value-setter only — does not activate fireplace mode. See CHANNEL_EXTEND_DURATION.
                    dto.fireplace_duration = seconds.longValue();
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    /**
     * Composes an extend-mode activation. Shared by {@link #buildControlUpdate}'s preset-mode case
     * ({@code explicitDurationSeconds == null}: fall back to whatever's currently stored) and
     * {@link AtagOneActions} ({@code explicitDurationSeconds != null}: use the caller's value
     * directly), so the channel path and the action path can never drift apart on what they send.
     * <p>
     * {@code control.extend_duration} is additive to the time remaining until the device's next
     * scheduled temperature change, not an absolute session length — extending by one hour shortly
     * before a scheduled change behaves very differently than extending by one hour shortly after
     * one. {@code control.ch_mode_duration} is deliberately not set here: for extend mode, only
     * {@code ch_mode} and {@code extend_duration} affect the device; {@code ch_mode_duration} has no
     * effect on it.
     */
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

    /**
     * Composes a holiday/vacation activation.
     * <p>
     * Unlike extend and fireplace, {@code ch_mode} alone never activates holiday mode on this device
     * — {@code ch_mode} and {@code configuration.start_vacation} must be sent together in the same
     * write, regardless of whether {@code vacation_duration} is already stored.
     * {@code vacation_duration} itself follows the same stored-or-explicit pattern as the other two
     * modes: use the caller's value if given, otherwise whatever is currently stored, otherwise the
     * device's own default.
     */
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

    /**
     * Composes a fireplace activation.
     * <p>
     * Unlike extend, {@code ch_mode_duration} must be present in this write — omitting it causes the
     * boiler's API subsystem to restart, making the device unreachable for several minutes.
     */
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

    /**
     * Composes a cancel-to-auto write.
     * <p>
     * {@code ch_mode_duration} is the field that must be zeroed to cancel any timed preset — the
     * mode-specific duration field ({@code extend_duration}, {@code fireplace_duration}, or
     * {@code vacation_duration}) is not enough on its own and leaves the countdown stale. Leaving
     * holiday mode additionally clears the vacation schedule ({@code vacation_duration} and
     * {@code start_vacation}) — required to fully cancel a pending, not-yet-active scheduled
     * vacation, and harmless-but-redundant for an active one, which self-clears both fields anyway.
     * <p>
     * The schedule is cleared whenever preset-mode currently reports holiday OR a vacation is armed
     * ({@code armedStartVacation > 0}). Both checks are needed: a pending, not-yet-active vacation
     * still reports preset-mode=auto, so relying on reported mode alone would silently leave such a
     * schedule fully armed while this method reports there was nothing to cancel.
     *
     * @return {@code true} if the mode being left is fireplace, meaning this write is accepted by the
     *         device but has no effect until a button is pressed on the thermostat display; no
     *         payload avoids this requirement
     */
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

    /**
     * True when durationSeconds is a positive whole multiple of unitSeconds — see the field comment
     * on SECONDS_PER_HOUR/SECONDS_PER_DAY for why this is enforced.
     */
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
            // Superseded by a dispose+reinitialize while the client was being constructed — don't let
            // a stale generation's client become this Thing's apiClient.
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
                // Superseded while the (blocking) pairing request was in flight — a newer generation
                // may already have its own apiClient/clientId; don't let this one persist or poll.
                return;
            }
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
                        pairingJob = scheduler.schedule(() -> doPair(client, clientId, myGeneration), PAIRING_RETRY_S,
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

    private void updateChannels(RetrieveReplyDTO r) {
        // Tracked unconditionally (not mode-gated) so it survives holiday mode ending — see the
        // defaultVacationDurationSeconds field comment.
        if (r.configuration.ch_mode_vacation > 0) {
            defaultVacationDurationSeconds = r.configuration.ch_mode_vacation;
        }
        if (r.configuration.ch_mode_extend > 0) {
            defaultExtendDurationSeconds = r.configuration.ch_mode_extend;
        }
        // Tracked unconditionally, including 0 — see armedStartVacation's field comment.
        armedStartVacation = r.configuration.start_vacation;

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
        /*
         * Strip RSS:…; tokens: the device embeds RSSI as a pseudo-error entry in device_errors, but
         * the dedicated wifi-signal channel already exposes the same value from the proper rssi
         * field. deviceErrors can be null here even though the DTO field defaults to "" — Gson
         * overwrites that default with null when the JSON explicitly carries a null value.
         */
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
        // report.current and report.power_cons are deliberately not exposed as channels — their units
        // and meaning are not confirmed against this device.
        updateIfChanged(CHANNEL_DHW_FLOW_RATE, new QuantityType<>(r.report.dhw_flow_rate, Units.LITRE_PER_MINUTE));
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
        updateIfChanged(CHANNEL_CH_CONTROL_MODE,
                new StringType(CH_CONTROL_MODE_NAMES.getOrDefault(r.control.ch_control_mode, "room")));
        updateIfChanged(CHANNEL_PRESET_MODE, new StringType(CH_MODE_NAMES.getOrDefault(r.control.ch_mode, "manual")));
        int modeForDuration = r.control.ch_mode;
        if (modeForDuration == CH_MODE_EXTEND || modeForDuration == CH_MODE_FIREPLACE
                || modeForDuration == CH_MODE_HOLIDAY) {
            updateIfChanged(CHANNEL_PRESET_MODE_DURATION, new QuantityType<>(r.control.ch_mode_duration, Units.SECOND));
        } else {
            updateIfChanged(CHANNEL_PRESET_MODE_DURATION, UnDefType.UNDEF);
        }
        updateIfChanged(CHANNEL_DHW_TARGET_TEMPERATURE, new QuantityType<>(r.control.dhw_temp_setp, SIUnits.CELSIUS));
        // control.dhw_mode is deliberately not exposed as a channel — no source documents its value
        // meanings, and neither the app nor the cloud portal expose a setting for it.
        updateIfChanged(CHANNEL_EXTEND_DURATION, new QuantityType<>(r.control.extend_duration, Units.SECOND));
        updateIfChanged(CHANNEL_FIREPLACE_DURATION, new QuantityType<>(r.control.fireplace_duration, Units.SECOND));
        /*
         * Read unconditionally, same as extend/fireplace above — not masked to UNDEF outside active
         * holiday mode. composeVacationActivation()'s stored-value fallback reads this same channel,
         * so masking it here would hide a value the user just wrote before the next holiday
         * activation ever picks it up. control.vacation_duration resets to 0 on cancel, so reading it
         * raw already conveys "nothing pending" without needing a separate UNDEF state.
         */
        updateIfChanged(CHANNEL_VACATION_DURATION, new QuantityType<>(r.control.vacation_duration, Units.SECOND));
        updateIfChanged(CHANNEL_WEATHER_STATUS,
                new StringType(WEATHER_STATUS_NAMES.getOrDefault(r.control.weather_status, "unknown")));

        // Vacation / extend / fireplace remaining duration
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
            updateIfChanged(CHANNEL_EXTEND_REMAINING, new QuantityType<>(r.control.ch_mode_duration, Units.SECOND));
            updateIfChanged(CHANNEL_FIREPLACE_REMAINING, UnDefType.UNDEF);
        } else if (mode == CH_MODE_FIREPLACE) {
            updateIfChanged(CHANNEL_VACATION_START, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_END, UnDefType.UNDEF);
            updateIfChanged(CHANNEL_VACATION_REMAINING, UnDefType.UNDEF);
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
    }

    private void updateIfChanged(String channelId, State state) {
        State previous = stateMap.put(channelId, state);
        if (!state.equals(previous)) {
            updateState(channelId, state);
        }
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

    private String resolveClientId() {
        if (!config.clientId.isBlank()) {
            return config.clientId;
        }
        String prop = getThing().getProperties().get(PROPERTY_CLIENT_ID);
        return prop != null ? prop : "";
    }

    private void persistClientId(String clientId) {
        /*
         * Thing properties persist for both managed and textually configured Things, and
         * resolveClientId() already checks them as a fallback. Deliberately not also written via
         * updateConfiguration()/editConfiguration(): on a managed Thing, that call round-trips
         * through dispose()+initialize(), tearing this handler down again right after pairing just
         * succeeded.
         */
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
