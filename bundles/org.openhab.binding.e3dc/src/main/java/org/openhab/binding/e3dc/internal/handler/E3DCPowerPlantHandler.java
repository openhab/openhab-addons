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
package org.openhab.binding.e3dc.internal.handler;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelRegistry;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCCommandRegistry;
import org.openhab.binding.e3dc.internal.E3DCCommandSpec;
import org.openhab.binding.e3dc.internal.E3DCIdlePeriod;
import org.openhab.binding.e3dc.internal.E3DCInverterChannels;
import org.openhab.binding.e3dc.internal.E3DCPowerMode;
import org.openhab.binding.e3dc.internal.actions.E3DCActions;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * The home power plant itself, publishing power, battery, inverter, energy and system channels.
 *
 * <p>
 * The power mode channel is not a plain value: the device only holds a forced mode for a short time, so the
 * handler sends the mode again with every poll cycle until the requested number of cycles is over.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCPowerPlantHandler extends E3DCThingHandler {

    /** The write only channel that starts a power mode. */
    static final String CHANNEL_SET_POWER_MODE = "set-power-mode";

    /** The command only channel that starts a manual charge, it carries no state. */
    static final String CHANNEL_SET_MANUAL_CHARGE = "set-manual-charge";

    /** The switch that blocks charging of the battery around the clock. */
    static final String CHANNEL_BLOCK_CHARGING = "block-charging";
    /** The switch that blocks discharging of the battery around the clock. */
    static final String CHANNEL_BLOCK_DISCHARGING = "block-discharging";
    private static final int VERIFY_ATTEMPTS = 6;
    private static final int VERIFY_DELAY_SECONDS = 1;
    static final String CHANNEL_IDLE_PERIODS = "idle-periods";

    private final E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();
    private final E3DCCommandRegistry commandRegistry = E3DCCommandSpec.powerPlant();

    /**
     * The device replaces its whole list of idle periods on every write, so a change reads the list, alters it
     * and writes it back. Two changes at the same time would drop each other's part.
     */
    private final Object idleLock = new Object();

    private @Nullable E3DCPowerMode heldMode;
    private long heldUntilMillis;

    /** The trackers and temperature sensors the channels were created for. */
    private E3DCInverterChannels.@Nullable Found knownInverterChannels;

    /** The trackers and temperature sensors that were found, which are read like the other channels of the inverter. */
    private volatile Map<String, E3DCChannelSpec> trackerSpecs = Map.of();

    /** What the last log of the trackers said, so that the log repeats only when it changes. */
    private E3DCInverterChannels.@Nullable Found loggedTrackers;
    private int trackerPolls;

    public E3DCPowerPlantHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected E3DCChannelRegistry getRegistry() {
        return registry;
    }

    @Override
    protected @Nullable E3DCCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(E3DCActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (E3DCBindingConstants.GROUP_BATTERY_CONTROL.equals(channelUID.getGroupId())
                && CHANNEL_SET_POWER_MODE.equals(channelUID.getIdWithoutGroup())) {
            if (!(command instanceof RefreshType)) {
                setPowerMode(command.toString());
            }
            return;
        }
        if (E3DCBindingConstants.GROUP_BATTERY_CONTROL.equals(channelUID.getGroupId())
                && CHANNEL_IDLE_PERIODS.equals(channelUID.getIdWithoutGroup())) {
            if (command instanceof RefreshType) {
                super.handleCommand(channelUID, command);
            } else {
                setIdlePeriods(command.toString().trim());
            }
            return;
        }
        if (E3DCBindingConstants.GROUP_BATTERY_CONTROL.equals(channelUID.getGroupId())
                && (CHANNEL_BLOCK_CHARGING.equals(channelUID.getIdWithoutGroup())
                        || CHANNEL_BLOCK_DISCHARGING.equals(channelUID.getIdWithoutGroup()))) {
            if (command instanceof RefreshType) {
                super.handleCommand(channelUID, command);
            } else if (command instanceof OnOffType) {
                E3DCIdlePeriod.Type type = CHANNEL_BLOCK_CHARGING.equals(channelUID.getIdWithoutGroup())
                        ? E3DCIdlePeriod.Type.CHARGE
                        : E3DCIdlePeriod.Type.DISCHARGE;
                block(channelUID, type, OnOffType.ON.equals(command));
            } else {
                logger.debug("Command {} is not valid for channel {}, use ON or OFF", command, channelUID);
            }
            return;
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    protected boolean publishesWrittenState(ChannelUID channelUID) {
        return !CHANNEL_SET_MANUAL_CHARGE.equals(channelUID.getIdWithoutGroup());
    }

    /**
     * Forces a power mode on the device.
     *
     * <p>
     * The channel is command only, so nothing is published as its state.
     *
     * @param text the mode as {@code auto}, {@code idle:<cycles>} or
     *            {@code charge|discharge|grid_charge:<watts>:<cycles>}
     * @return whether the device accepted the mode
     */
    public boolean setPowerMode(String text) {
        E3DCPowerMode mode = E3DCPowerMode.parse(text);
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (mode == null) {
            logger.warn(
                    "'{}' is not a power mode, use auto, idle:<cycles> or charge|discharge|grid_charge:<watts>:<cycles>",
                    text);
            return false;
        }
        if (bridge == null) {
            logger.debug("Cannot set the power mode without a bridge");
            return false;
        }
        if (!commandHandler.send(bridge, mode.toRequest())) {
            return false;
        }
        synchronized (this) {
            if (mode.isHeld()) {
                heldMode = mode;
                heldUntilMillis = System.currentTimeMillis()
                        + mode.cycles() * bridge.getRefreshIntervalSeconds() * 1000L;
            } else {
                heldMode = null;
            }
        }
        return true;
    }

    /**
     * Reads the idle periods of the second version from the device.
     *
     * @return the periods, one per line as {@code name:days:mode:active:hh:mm-hh:mm}, or {@code null} when the
     *         device could not be asked
     */
    public @Nullable String getIdlePeriods() {
        synchronized (idleLock) {
            List<E3DCIdlePeriod> periods = readIdlePeriods();
            return periods != null ? E3DCIdlePeriod.toText(periods) : null;
        }
    }

    /**
     * Replaces the whole list of idle periods of the second version on the device.
     *
     * @param text the periods, one per line; an empty text removes all of them
     * @return whether the device accepted the new list
     */
    public boolean setIdlePeriods(String text) {
        List<E3DCIdlePeriod> periods = E3DCIdlePeriod.parseAll(text);
        if (periods == null) {
            logger.warn("The idle periods were not written: every line must read name:days:mode:active:hh:mm-hh:mm");
            return false;
        }
        String problem = problemOf(periods);
        if (problem != null) {
            logger.warn("The idle periods were not written: {}", problem);
            return false;
        }
        synchronized (idleLock) {
            return writeIdlePeriods(periods);
        }
    }

    /**
     * Removes the idle period of the given name and keeps all others.
     *
     * @param name the name of the period
     * @return whether a period of that name existed and the device accepted the shortened list
     */
    public boolean deleteIdlePeriod(String name) {
        String wanted = name.trim();
        boolean[] found = new boolean[1];
        boolean written = changeIdlePeriods(periods -> {
            List<E3DCIdlePeriod> remaining = E3DCIdlePeriod.removeByName(periods, wanted);
            found[0] = remaining.size() != periods.size();
            return remaining;
        });
        if (!found[0]) {
            logger.warn("No idle period is named {}", wanted);
            return false;
        }
        return written;
    }

    /** Reads the periods of the device, changes them and writes the list back, as one step. */
    @SuppressWarnings("null") // the JDK functional interfaces carry no null annotations
    private boolean changeIdlePeriods(UnaryOperator<List<E3DCIdlePeriod>> change) {
        synchronized (idleLock) {
            List<E3DCIdlePeriod> current = readIdlePeriods();
            if (current == null) {
                logger.warn("The idle periods were not changed because they could not be read");
                return false;
            }
            List<E3DCIdlePeriod> updated = change.apply(new ArrayList<>(current));
            String problem = problemOf(updated);
            if (problem != null) {
                logger.warn("The idle periods were not written: {}", problem);
                return false;
            }
            if (!writeIdlePeriods(updated)) {
                logger.warn("The device did not accept the idle periods");
                return false;
            }
            return true;
        }
    }

    /** The oldest year the history fetch tries, a safety limit. */
    static final int FIRST_HISTORY_YEAR = 2000;

    /** The number of years in a row without data after which the history fetch stops going back. */
    static final int MAX_EMPTY_YEARS = 2;

    /** The number of days, weeks or months in a row without data after which the fetch stops going back. */
    static final int MAX_EMPTY_PERIODS = 3;

    /** The most days, weeks or months one call fetches, a safety limit as every period is a request. */
    static final int MAX_PERIODS = 400;

    /**
     * Fetches the yearly history of all earlier years once and publishes it on the {@code history#year-*}
     * channels as time series entries, one entry per year stamped with the start of the year.
     *
     * <p>
     * The fetch starts at the last completed year and goes back year by year; the running year is polled
     * anyway. Each year is asked for on its own, so an answer can only belong to the year it was requested
     * for. It stops after {@value #MAX_EMPTY_YEARS} years in a row without data, which tolerates a single gap
     * and ends before the installation of the device. A year already known to persistence is replaced.
     *
     * @return the number of years the device delivered data for, or {@code -1} when the device is not
     *         connected
     */
    public int fetchHistory() {
        List<LocalDate> starts = new ArrayList<>();
        for (int year = LocalDate.now(ZoneOffset.UTC).getYear() - 1; year >= FIRST_HISTORY_YEAR; year--) {
            starts.add(LocalDate.of(year, 1, 1));
        }
        return fetchPeriods(RscpTag.DB_HISTORY_DATA_YEAR, starts, MAX_EMPTY_YEARS);
    }

    /**
     * Fetches the history of finished days, weeks or months and publishes it on the {@code history#day-*},
     * {@code history#week-*} or {@code history#month-*} channels as time series entries stamped with the start of
     * the period.
     *
     * <p>
     * It starts at the last finished period and goes back. A week runs from Monday to Sunday, a month is a
     * calendar month. It stops after {@value #MAX_EMPTY_PERIODS} periods in a row without data or after
     * {@code count} periods, at most {@value #MAX_PERIODS}.
     *
     * @param period {@code day}, {@code week}, {@code month} or {@code year}
     * @param count the number of finished periods to go back
     * @return the number of periods the device delivered data for, or {@code -1} when the device is not
     *         connected or the arguments are not valid
     */
    public int fetchPeriodHistory(String period, int count) {
        String name = period.trim().toLowerCase(Locale.ROOT);
        if (count < 1 || count > MAX_PERIODS) {
            logger.warn("History not fetched: the count has to be between 1 and {}", MAX_PERIODS);
            return -1;
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<LocalDate> starts = new ArrayList<>();
        int container;
        switch (name) {
            case "day" -> {
                container = RscpTag.DB_HISTORY_DATA_DAY;
                for (int i = 1; i <= count; i++) {
                    starts.add(today.minusDays(i));
                }
            }
            case "week" -> {
                container = RscpTag.DB_HISTORY_DATA_WEEK;
                LocalDate monday = today.with(DayOfWeek.MONDAY);
                for (int i = 1; i <= count; i++) {
                    starts.add(monday.minusWeeks(i));
                }
            }
            case "month" -> {
                container = RscpTag.DB_HISTORY_DATA_MONTH;
                LocalDate first = today.withDayOfMonth(1);
                for (int i = 1; i <= count; i++) {
                    starts.add(first.minusMonths(i));
                }
            }
            case "year" -> {
                container = RscpTag.DB_HISTORY_DATA_YEAR;
                LocalDate first = today.withDayOfYear(1);
                for (int i = 1; i <= count; i++) {
                    starts.add(first.minusYears(i));
                }
            }
            default -> {
                logger.warn("History not fetched: the period has to be day, week, month or year, not '{}'", period);
                return -1;
            }
        }
        return fetchPeriods(container, starts,
                container == RscpTag.DB_HISTORY_DATA_YEAR ? MAX_EMPTY_YEARS : MAX_EMPTY_PERIODS);
    }

    /** The number of days a period of the given container covers when it starts on that day. */
    static int daysOf(int container, LocalDate start) {
        return switch (container) {
            case RscpTag.DB_HISTORY_DATA_WEEK -> 7;
            case RscpTag.DB_HISTORY_DATA_MONTH -> start.lengthOfMonth();
            case RscpTag.DB_HISTORY_DATA_YEAR -> start.lengthOfYear();
            default -> 1;
        };
    }

    /**
     * Requests the periods one by one, newest first, and publishes what the device delivers as time series.
     *
     * @param container the history container of the periods
     * @param starts the first day of each period, newest first
     * @param maxEmpty the number of periods in a row without data after which the fetch stops
     * @return the number of periods with data, or {@code -1} when the device is not connected
     */
    private int fetchPeriods(int container, List<LocalDate> starts, int maxEmpty) {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null || !bridge.isConnected()) {
            logger.warn("History not fetched: the device is not connected");
            return -1;
        }
        Map<String, E3DCChannelSpec> channels = registry.groups().getOrDefault(E3DCBindingConstants.GROUP_HISTORY,
                Map.of());
        Map<String, TimeSeries> series = new LinkedHashMap<>();
        int delivered = 0;
        int emptyInARow = 0;
        for (LocalDate start : starts) {
            if (emptyInARow >= maxEmpty) {
                break;
            }
            RscpValue response;
            try {
                response = bridge.sendRequest(
                        List.of(E3DCBridgeHandler.periodHistoryRequest(container, start, daysOf(container, start))));
            } catch (RscpException e) {
                logger.debug("History of {} not fetched: {}", start, e.getMessage());
                emptyInARow++;
                continue;
            }
            Map<String, State> states = new LinkedHashMap<>();
            for (Map.Entry<String, E3DCChannelSpec> channel : channels.entrySet()) {
                E3DCChannelSpec spec = channel.getValue();
                if (spec.container() != container) {
                    continue;
                }
                RscpValue value = findValue(response, spec, null);
                State state = value != null ? valueState(value, spec) : null;
                if (state != null) {
                    states.put(channel.getKey(), state);
                }
            }
            if (states.values().stream().anyMatch(E3DCPowerPlantHandler::isNonZero)) {
                Instant stamp = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
                states.forEach((key, state) -> {
                    TimeSeries timeSeries = series.get(key);
                    if (timeSeries == null) {
                        timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
                        series.put(key, timeSeries);
                    }
                    timeSeries.add(stamp, state);
                });
                delivered++;
                emptyInARow = 0;
            } else {
                logger.debug("The device holds no history for {}", start);
                emptyInARow++;
            }
        }
        for (Map.Entry<String, TimeSeries> entry : series.entrySet()) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(),
                    E3DCBindingConstants.GROUP_HISTORY + "#" + entry.getKey());
            sendTimeSeries(channelUID, entry.getValue());
        }
        logger.debug("History fetched for {} periods", delivered);
        return delivered;
    }

    /** Whether the state is a number other than zero; the device may answer a year without data with zeros. */
    static boolean isNonZero(State state) {
        if (state instanceof QuantityType<?> quantity) {
            return quantity.doubleValue() != 0;
        }
        if (state instanceof DecimalType decimal) {
            return decimal.doubleValue() != 0;
        }
        return true;
    }

    /** What is wrong with a list of periods, or {@code null} when the device can take it. */
    private static @Nullable String problemOf(List<E3DCIdlePeriod> periods) {
        List<String> names = new ArrayList<>();
        for (E3DCIdlePeriod period : periods) {
            if (names.contains(period.name())) {
                return "the name " + period.name() + " is used more than once";
            }
            names.add(period.name());
            if (!period.hasValidWindow()) {
                return "the period " + period.name() + " must end after it starts";
            }
        }
        return null;
    }

    /**
     * Switches the lock of charging or discharging.
     *
     * <p>
     * The device has no such function, so the lock is an idle period of its own over all seven days. Switching
     * it on also turns on the use of idle periods, because the period does nothing otherwise.
     */
    private void block(ChannelUID channelUID, E3DCIdlePeriod.Type type, boolean on) {
        E3DCIdlePeriod blocker = E3DCIdlePeriod.blocker(type);
        synchronized (idleLock) {
            List<E3DCIdlePeriod> current = readIdlePeriods();
            if (current == null) {
                logger.warn("The lock was not changed because the idle periods could not be read");
                return;
            }
            List<E3DCIdlePeriod> updated = new ArrayList<>(current);
            updated.removeIf(period -> period.name().equals(blocker.name()));
            if (on) {
                updated.add(blocker);
            }
            if (!updated.equals(current) && !writeIdlePeriods(updated)) {
                logger.warn("The lock of {} was not changed because the device did not accept the idle periods",
                        type.name().toLowerCase());
                return;
            }
            if (on && !enableIdlePeriods()) {
                logger.warn("The lock of {} is not active because the device did not confirm switching on the idle"
                        + " periods", type.name().toLowerCase());
                return;
            }
        }
        // the device takes a moment to report a list it has just accepted, so the check does not run at once
        scheduler.schedule(() -> verifyBlock(channelUID, type, on, 1), VERIFY_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Checks what the device reports after a lock was written and publishes it on the switch.
     *
     * <p>
     * The write is answered at once, but the list the device reports may still be the old one for a moment.
     * The check therefore repeats until the device reports what was written, and gives up with a warning
     * that names what the device holds.
     */
    private void verifyBlock(ChannelUID channelUID, E3DCIdlePeriod.Type type, boolean on, int attempt) {
        IdleState state;
        synchronized (idleLock) {
            state = readIdleState();
        }
        boolean blocked = state != null && state.blocks(type);
        if (state != null && blocked == on) {
            updateState(channelUID, blocked ? OnOffType.ON : OnOffType.OFF);
            return;
        }
        if (attempt < VERIFY_ATTEMPTS) {
            scheduler.schedule(() -> verifyBlock(channelUID, type, on, attempt + 1), VERIFY_DELAY_SECONDS,
                    TimeUnit.SECONDS);
            return;
        }
        if (state == null) {
            logger.warn("The lock of {} could not be checked because the device did not report its idle periods",
                    type.name().toLowerCase());
            return;
        }
        logger.warn("The device does not report the lock of {} as {} after {} s, idle periods in use: {}, periods: {}",
                type.name().toLowerCase(), on ? "active" : "released", VERIFY_ATTEMPTS * VERIFY_DELAY_SECONDS,
                state.enabled(), E3DCIdlePeriod.toText(state.periods()));
        updateState(channelUID, blocked ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * The periods of the device together with whether it uses them.
     *
     * @param periods the periods the device reported
     * @param enabled whether the device uses idle periods at all
     */
    private record IdleState(List<E3DCIdlePeriod> periods, boolean enabled) {
        /** Whether idle periods are in use and the list holds the active period of the lock switch of the type. */
        boolean blocks(E3DCIdlePeriod.Type type) {
            return enabled && periods.stream().anyMatch(period -> period.blocks(type));
        }
    }

    /** Reads the periods and the use of idle periods, or returns {@code null} when the device did not report both. */
    private @Nullable IdleState readIdleState() {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null) {
            return null;
        }
        try {
            RscpValue response = bridge.sendRequest(List.of(RscpValue.none(RscpTag.EMS_REQ_GET_IDLE_PERIODS_2),
                    RscpValue.none(RscpTag.EMS_REQ_GET_IDLE_PERIODS_ENABLE)));
            RscpValue enabled = response.find(RscpTag.EMS_GET_IDLE_PERIODS_ENABLE);
            if (response.find(RscpTag.EMS_GET_IDLE_PERIODS_2) == null || enabled == null
                    || !(enabled.getValue() instanceof Boolean flag)) {
                logger.debug("The device did not report its idle periods and their use: {}", response.findError());
                return null;
            }
            IdleState state = new IdleState(E3DCIdlePeriod.fromResponse(response), flag);
            logger.debug("The device reports idle periods in use: {}, periods: {}", flag,
                    E3DCIdlePeriod.toText(state.periods()));
            return state;
        } catch (RscpException e) {
            logger.debug("Cannot read the idle state: {}", e.getMessage());
            return null;
        }
    }

    /** Reads the periods, or returns {@code null} when the device did not answer with a list. */
    private @Nullable List<E3DCIdlePeriod> readIdlePeriods() {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null) {
            logger.debug("Cannot read the idle periods without a bridge");
            return null;
        }
        try {
            RscpValue response = bridge.sendRequest(List.of(RscpValue.none(RscpTag.EMS_REQ_GET_IDLE_PERIODS_2)));
            if (response.containsError() || response.find(RscpTag.EMS_GET_IDLE_PERIODS_2) == null) {
                // without a list from the device a write would drop the periods that are there
                logger.debug("The device did not report its idle periods: {}", response.findError());
                return null;
            }
            return E3DCIdlePeriod.fromResponse(response);
        } catch (RscpException e) {
            logger.debug("Cannot read the idle periods: {}", e.getMessage());
            return null;
        }
    }

    private boolean writeIdlePeriods(List<E3DCIdlePeriod> periods) {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null) {
            logger.debug("Cannot write the idle periods without a bridge");
            return false;
        }
        boolean accepted = commandHandler.send(bridge, E3DCIdlePeriod.toRequest(periods));
        if (accepted) {
            bridge.requestRefresh();
        }
        return accepted;
    }

    private boolean enableIdlePeriods() {
        E3DCBridgeHandler bridge = getBridgeHandler();
        return bridge != null
                && commandHandler.send(bridge, List.of(RscpValue.bool(RscpTag.EMS_REQ_SET_IDLE_PERIODS_ENABLE, true)));
    }

    /** Publishes whether charging and discharging are blocked, as far as the poll reported the periods. */
    private void updateBlockSwitches(RscpValue response) {
        if (response.find(RscpTag.EMS_GET_IDLE_PERIODS_2) == null) {
            return;
        }
        List<E3DCIdlePeriod> periods = E3DCIdlePeriod.fromResponse(response);
        RscpValue enabledValue = response.find(RscpTag.EMS_GET_IDLE_PERIODS_ENABLE);
        if (enabledValue == null || !(enabledValue.getValue() instanceof Boolean enabled)) {
            // without a report of the use of idle periods the switches cannot tell, so they stay as they are
            return;
        }
        for (E3DCIdlePeriod.Type type : E3DCIdlePeriod.Type.values()) {
            String channelId = type == E3DCIdlePeriod.Type.CHARGE ? CHANNEL_BLOCK_CHARGING : CHANNEL_BLOCK_DISCHARGING;
            boolean blocked = enabled && periods.stream().anyMatch(period -> period.blocks(type));
            updateState(
                    new ChannelUID(getThing().getUID(), E3DCBindingConstants.GROUP_BATTERY_CONTROL + "#" + channelId),
                    blocked ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    public void onData(RscpValue response) {
        super.onData(response);
        updateTrackers(response);
        updateBlockSwitches(response);
        repeatHeldMode();
    }

    /**
     * Creates the channels of the trackers and temperature sensors the device answers and publishes their values.
     *
     * <p>
     * The device is asked for the indexes 0 to 5 of both. Every index that answers with a value gets its
     * channels, so an installation with one, three or more trackers gets what it has, and a tracker that is
     * connected later is picked up as soon as it answers.
     */
    private void updateTrackers(RscpValue response) {
        RscpValue inverter = response.find(RscpTag.PVI_DATA);
        if (inverter == null) {
            return;
        }
        E3DCInverterChannels.Found answered = E3DCInverterChannels.discover(inverter);
        logTrackers(inverter, answered);
        adaptInverterChannels(answered);
        for (Map.Entry<String, E3DCChannelSpec> tracker : trackerSpecs.entrySet()) {
            RscpValue value = findValue(response, tracker.getValue(), getDeviceIndex());
            if (value != null) {
                updateChannel(E3DCBindingConstants.GROUP_INVERTER, tracker.getKey(), tracker.getValue(), value);
            }
        }
    }

    /**
     * Adds the channels of the trackers and sensors that answered and remembers which ones are read.
     *
     * <p>
     * This runs with every poll, because a thing that is defined in a file gets its channels back from the
     * definition when the file is reloaded, which removes the ones added here. Comparing the channels is
     * cheap; the thing is only updated when they differ. Indexes that answered once stay in the list, so a
     * single failed poll does not drop a channel.
     */
    private void adaptInverterChannels(E3DCInverterChannels.Found answered) {
        E3DCInverterChannels.Found known = knownInverterChannels;
        SortedSet<Integer> trackers = new TreeSet<>(answered.trackers());
        SortedSet<Integer> sensors = new TreeSet<>(answered.sensors());
        if (known != null) {
            trackers.addAll(known.trackers());
            sensors.addAll(known.sensors());
        }
        E3DCInverterChannels.Found found = new E3DCInverterChannels.Found(trackers, sensors);
        if (found.isEmpty()) {
            return;
        }
        List<Channel> channels = E3DCInverterChannels.reconcile(getThing().getChannels(), getThing().getUID(), found);
        if (channels != null) {
            logger.debug("The inverter answers for {}, the channels are adapted", found.describe());
            updateThing(editThing().withChannels(channels).build());
        }
        if (!found.equals(known)) {
            trackerSpecs = E3DCInverterChannels.specs(found);
            knownInverterChannels = found;
        }
    }

    /**
     * Logs what the device answered about its trackers and temperature sensors.
     *
     * <p>
     * At debug level the whole answer of the inverter is logged once, and again when the indexes that answer
     * change or the level is switched on, followed by the values of the trackers every 20th poll. At trace
     * level the values are logged with every poll. Nothing is built while the level is off.
     */
    private void logTrackers(RscpValue inverter, E3DCInverterChannels.Found found) {
        if (!logger.isDebugEnabled()) {
            loggedTrackers = null;
            return;
        }
        // the texts below are long, so they are only built when they are logged
        if (!found.equals(loggedTrackers)) {
            loggedTrackers = found;
            trackerPolls = 0;
            logger.debug("Inverter answers for {}", found.describe());
            logger.debug("Answer of the inverter:{}{}", System.lineSeparator(), inverter.describeTree());
        }
        if (logger.isTraceEnabled() || trackerPolls % 20 == 0) {
            logger.debug("PV tracker values: {}", E3DCInverterChannels.describeValues(inverter));
        }
        trackerPolls++;
    }

    /** Sends the held mode again, or forgets it once its time is over. */
    private void repeatHeldMode() {
        E3DCPowerMode mode;
        synchronized (this) {
            mode = heldMode;
            if (mode != null && System.currentTimeMillis() >= heldUntilMillis) {
                heldMode = null;
                mode = null;
            }
        }
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (mode != null && bridge != null) {
            commandHandler.send(bridge, mode.toRequest());
        }
    }
}
