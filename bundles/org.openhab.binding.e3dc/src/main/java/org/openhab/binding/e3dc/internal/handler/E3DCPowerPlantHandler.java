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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelRegistry;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCCommandOptions;
import org.openhab.binding.e3dc.internal.E3DCCommandOptionsProvider;
import org.openhab.binding.e3dc.internal.E3DCCommandRegistry;
import org.openhab.binding.e3dc.internal.E3DCCommandSpec;
import org.openhab.binding.e3dc.internal.E3DCIdlePeriod;
import org.openhab.binding.e3dc.internal.E3DCInverterChannels;
import org.openhab.binding.e3dc.internal.E3DCPowerMode;
import org.openhab.binding.e3dc.internal.actions.E3DCActions;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * The home power plant itself, publishing power, battery, inverter, energy and system channels. The power
 * mode is held by the device for a short time only, so the handler sends it again with every poll until the
 * requested number of cycles is over.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCPowerPlantHandler extends E3DCThingHandler {

    /** The write only channel that starts a power mode. */
    static final String CHANNEL_SET_POWER_MODE = "set-power-mode";

    /** The command only channel that starts a manual charge, it carries no state. */
    static final String CHANNEL_SET_MANUAL_CHARGE = "set-manual-charge";

    /** The energy channels of the manual charge, which the device reports in Wh and which are published in Wh. */
    private static final Set<String> MANUAL_CHARGE_ENERGY_CHANNELS = Set.of("manual-charge-energy",
            "manual-charge-target");

    /** The channel that shows the energy the running manual charge still has to charge. */
    static final String CHANNEL_MANUAL_CHARGE_REMAINING = "manual-charge-remaining";

    static final String CHANNEL_BLOCK_CHARGING = "block-charging";
    static final String CHANNEL_BLOCK_DISCHARGING = "block-discharging";
    static final String CHANNEL_CHARGE_PERIODS = "charge-periods";
    static final String CHANNEL_DISCHARGE_PERIODS = "discharge-periods";
    private static final int VERIFY_ATTEMPTS = 6;
    private static final int VERIFY_DELAY_SECONDS = 1;

    /** The properties the device reports about itself rather than as a measurement. */
    static final String PROPERTY_PRODUCTION_DATE = "productionDate";
    static final String PROPERTY_TIME_ZONE = "timeZone";
    static final String PROPERTY_INSTALLED_PEAK_POWER = "installedPeakPower";
    static final String PROPERTY_INVERTER_COUNT = "inverterCount";
    static final String PROPERTY_BATTERY_COUNT = "batteryCount";
    static final String PROPERTY_COUPLING_MODE = "couplingMode";

    /** The values read once per connection to publish them as thing properties. */
    private static final List<Integer> PROPERTY_VALUE_TAGS = List.of(RscpTag.INFO_SW_RELEASE,
            RscpTag.INFO_SERIAL_NUMBER, RscpTag.INFO_PRODUCTION_DATE, RscpTag.INFO_TIME_ZONE,
            RscpTag.EMS_INSTALLED_PEAK_POWER, RscpTag.PVI_INVERTER_COUNT, RscpTag.BAT_AVAILABLE_BATTERIES,
            RscpTag.EMS_COUPLING_MODE);

    private final E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();
    private final E3DCCommandRegistry commandRegistry = E3DCCommandSpec.powerPlant();

    /**
     * The device replaces its whole list of idle periods on every write, so a change reads the list, alters it
     * and writes it back. Two changes at the same time would drop each other's part.
     */
    private final Object idleLock = new Object();

    /** Guards the held power mode, which a command sets and the poll reads. */
    private final Object heldLock = new Object();
    private @Nullable E3DCPowerMode heldMode;
    private long heldUntilMillis;

    /** Serializes access to the pending lock switch check, which runs on a scheduler thread. */
    private final Object verifyLock = new Object();
    private @Nullable ScheduledFuture<?> verifyJob;

    /** Reads the values that describe the device whenever the connection comes up. */
    private final Runnable propertiesListener = () -> scheduler.execute(this::readProperties);

    /** The trackers and temperature sensors the channels were created for. */
    private E3DCInverterChannels.@Nullable Found knownInverterChannels;

    /** The trackers and temperature sensors that were found, which are read like the other channels of the inverter. */
    private volatile Map<String, E3DCChannelSpec> trackerSpecs = Map.of();

    /** What the last log of the trackers said, so that the log repeats only when it changes. */
    private E3DCInverterChannels.@Nullable Found loggedTrackers;

    /** What the last log of the manual charge said, so that the log repeats only when the values change. */
    private @Nullable String loggedManualCharge;
    private int trackerPolls;

    /** Offers the energies the user may write, which follow the battery the device reports. */
    private final E3DCCommandOptionsProvider commandOptions;

    /**
     * Creates the handler of the power plant thing.
     */
    public E3DCPowerPlantHandler(Thing thing, E3DCCommandOptionsProvider commandOptions) {
        super(thing);
        this.commandOptions = commandOptions;
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
    public void initialize() {
        super.initialize();
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge != null) {
            bridge.addOnlineListener(propertiesListener);
            if (bridge.isConnected()) {
                propertiesListener.run();
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String groupId = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();
        if (E3DCBindingConstants.GROUP_BATTERY_MODE.equals(groupId) && CHANNEL_SET_POWER_MODE.equals(channelId)) {
            if (!(command instanceof RefreshType)) {
                String mode = command.toString();
                commandQueue.submit(() -> setPowerMode(mode));
            }
            return;
        }
        if (E3DCBindingConstants.GROUP_IDLE_PERIODS.equals(groupId)
                && (CHANNEL_CHARGE_PERIODS.equals(channelId) || CHANNEL_DISCHARGE_PERIODS.equals(channelId))) {
            if (command instanceof RefreshType) {
                super.handleCommand(channelUID, command);
            } else {
                String text = command.toString().trim();
                commandQueue.submit(() -> setPeriods(channelId, text));
            }
            return;
        }
        if (E3DCBindingConstants.GROUP_IDLE_PERIODS.equals(groupId)
                && (CHANNEL_BLOCK_CHARGING.equals(channelId) || CHANNEL_BLOCK_DISCHARGING.equals(channelId))) {
            if (command instanceof RefreshType) {
                super.handleCommand(channelUID, command);
            } else if (command instanceof OnOffType) {
                E3DCIdlePeriod.Type type = CHANNEL_BLOCK_CHARGING.equals(channelId) ? E3DCIdlePeriod.Type.CHARGE
                        : E3DCIdlePeriod.Type.DISCHARGE;
                boolean on = OnOffType.ON.equals(command);
                commandQueue.submit(() -> block(channelUID, type, on));
            } else {
                logger.debug("Command {} is not valid for channel {}, use ON or OFF", command, channelUID);
            }
            return;
        }
        if (E3DCBindingConstants.GROUP_MANUAL_BATTERY_CHARGE.equals(groupId)
                && CHANNEL_SET_MANUAL_CHARGE.equals(channelId) && !(command instanceof RefreshType)) {
            logger.debug("Starting a manual battery charge with {}", command);
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    protected boolean publishesWrittenState(ChannelUID channelUID) {
        return !CHANNEL_SET_MANUAL_CHARGE.equals(channelUID.getIdWithoutGroup());
    }

    /** Forces a power mode on the device; the channel is command only, so no state is published. */
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
        synchronized (heldLock) {
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

    /** Reads the idle periods of the second version from the device, one per line, or {@code null} on failure. */
    /**
     * The idle periods in their written form.
     */
    public @Nullable String getIdlePeriods() {
        synchronized (idleLock) {
            List<E3DCIdlePeriod> periods = readIdlePeriods();
            return periods != null ? E3DCIdlePeriod.toText(periods) : null;
        }
    }

    /** Replaces the whole list of idle periods of the second version on the device; an empty text removes all. */
    /**
     * Sets the idle periods from the written form.
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

    /** Replaces the periods of one direction and keeps the periods of the other direction. */
    private boolean setPeriods(String channelId, String text) {
        E3DCIdlePeriod.Type type = CHANNEL_CHARGE_PERIODS.equals(channelId) ? E3DCIdlePeriod.Type.CHARGE
                : E3DCIdlePeriod.Type.DISCHARGE;
        List<E3DCIdlePeriod> periods = E3DCIdlePeriod.parseAll(text, type);
        if (periods == null) {
            logger.warn("The idle periods were not written: every line must read name:days:active:hh:mm-hh:mm");
            return false;
        }
        return changeIdlePeriods(current -> {
            List<E3DCIdlePeriod> updated = new ArrayList<>();
            for (E3DCIdlePeriod period : current) {
                if (period.type() != type) {
                    updated.add(period);
                }
            }
            updated.addAll(periods);
            return updated;
        });
    }

    /** Removes the idle period of the given name and keeps all others. */
    /**
     * Removes the idle period of the given name.
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
     * channels as one time series entry per year. It starts at the last completed year, goes back year by year
     * and stops after {@value #MAX_EMPTY_YEARS} years in a row without data.
     */
    public int fetchHistory() {
        List<LocalDate> starts = new ArrayList<>();
        for (int year = LocalDate.now(getZone()).getYear() - 1; year >= FIRST_HISTORY_YEAR; year--) {
            starts.add(LocalDate.of(year, 1, 1));
        }
        return fetchPeriods(RscpTag.DB_HISTORY_DATA_YEAR, starts, MAX_EMPTY_YEARS);
    }

    /**
     * Fetches the history of finished days, weeks or months and publishes it on the {@code history#day-*},
     * {@code history#week-*} or {@code history#month-*} channels, one time series entry per period. A week runs
     * from Monday to Sunday, a month is a calendar month, and the fetch stops after {@value #MAX_EMPTY_PERIODS}
     * periods in a row without data or after {@code count} periods.
     */
    public int fetchPeriodHistory(String period, int count) {
        String name = period.trim().toLowerCase(Locale.ROOT);
        if (count < 1 || count > MAX_PERIODS) {
            logger.warn("History not fetched: the count has to be between 1 and {}", MAX_PERIODS);
            return -1;
        }
        LocalDate today = LocalDate.now(getZone());
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

    /** Requests the periods one by one, newest first, and publishes what the device delivers as time series. */
    private int fetchPeriods(int container, List<LocalDate> starts, int maxEmpty) {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null || !bridge.isConnected()) {
            logger.warn("History not fetched: the device is not connected");
            return -1;
        }
        Map<String, E3DCChannelSpec> channels = registry.groups().getOrDefault(E3DCBindingConstants.GROUP_HISTORY,
                Map.of());
        ZoneId zone = bridge.getZone();
        Map<String, TimeSeries> series = new LinkedHashMap<>();
        int delivered = 0;
        EmptyPeriods emptyPeriods = new EmptyPeriods(maxEmpty);
        for (LocalDate start : starts) {
            if (emptyPeriods.exhausted()) {
                break;
            }
            RscpValue response;
            try {
                response = bridge.sendRequest(List
                        .of(E3DCBridgeHandler.periodHistoryRequest(container, start, daysOf(container, start), zone)));
            } catch (RscpException e) {
                logger.debug("History of {} not fetched: {}", start, e.getMessage());
                emptyPeriods.record(false);
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
                    State published = withChannelUnit(channel.getKey(), state, spec);
                    if (published != null) {
                        states.put(channel.getKey(), published);
                    }
                }
            }
            if (states.values().stream().anyMatch(E3DCPowerPlantHandler::isNonZero)) {
                Instant stamp = start.atStartOfDay(zone).toInstant();
                states.forEach((key, state) -> {
                    TimeSeries timeSeries = series.get(key);
                    if (timeSeries == null) {
                        timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
                        series.put(key, timeSeries);
                    }
                    timeSeries.add(stamp, state);
                });
                delivered++;
                emptyPeriods.record(true);
            } else {
                logger.debug("The device holds no history for {}", start);
                emptyPeriods.record(false);
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

    /**
     * The state of a fetched history value with the unit of its channel, as the poll publishes it. The values of a
     * time series are stored by persistence as they are, so the unit has to be part of them.
     */
    private @Nullable State withChannelUnit(String channelId, State state, E3DCChannelSpec spec) {
        Channel channel = getThing()
                .getChannel(new ChannelUID(getThing().getUID(), E3DCBindingConstants.GROUP_HISTORY + "#" + channelId));
        return withUnit(state, channel != null ? channel.getAcceptedItemType() : null, spec);
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

    /**
     * The rule that ends a history fetch: it stops after a given number of periods in a row without data. A
     * single gap is tolerated, and the years before the installation must not be requested one by one.
     */
    static final class EmptyPeriods {

        private final int limit;
        private int inARow;

        EmptyPeriods(int limit) {
            this.limit = limit;
        }

        /** Whether no further period may be requested. */
        boolean exhausted() {
            return inARow >= limit;
        }

        /** Records whether the period that was just requested had data. */
        void record(boolean hasData) {
            inARow = hasData ? 0 : inARow + 1;
        }
    }

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
     * Switches the lock of charging or discharging. The device has no such function, so the lock is an idle
     * period of its own over all seven days; switching it on also turns on the use of idle periods.
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
                        type.name().toLowerCase(Locale.ROOT));
                return;
            }
            if (on && !enableIdlePeriods()) {
                logger.warn("The lock of {} is not active because the device did not confirm switching on the idle"
                        + " periods", type.name().toLowerCase(Locale.ROOT));
                return;
            }
        }
        // the device takes a moment to report a list it has just accepted, so the check does not run at once
        scheduleVerify(channelUID, type, on, 1);
    }

    /**
     * Queues the next check of a lock switch, replacing a check that is still pending. The chain is cancelled
     * in {@link #dispose()}, otherwise a check that was waiting would keep running after the thing is gone.
     */
    private void scheduleVerify(ChannelUID channelUID, E3DCIdlePeriod.Type type, boolean on, int attempt) {
        synchronized (verifyLock) {
            cancelVerifyJob();
            verifyJob = scheduler.schedule(() -> verifyBlock(channelUID, type, on, attempt), VERIFY_DELAY_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    private void cancelVerifyJob() {
        ScheduledFuture<?> job = verifyJob;
        verifyJob = null;
        if (job != null) {
            job.cancel(false);
        }
    }

    @Override
    public void dispose() {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge != null) {
            bridge.removeOnlineListener(propertiesListener);
        }
        synchronized (verifyLock) {
            cancelVerifyJob();
        }
        commandOptions.removeOptions(getThing().getUID());
        super.dispose();
    }

    /**
     * Checks what the device reports after a lock was written and publishes it on the switch. The write is
     * answered at once, but the reported list may still be the old one, so the check repeats until it matches.
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
            scheduleVerify(channelUID, type, on, attempt + 1);
            return;
        }
        if (state == null) {
            logger.warn("The lock of {} could not be checked because the device did not report its idle periods",
                    type.name().toLowerCase(Locale.ROOT));
            return;
        }
        logger.warn("The device does not report the lock of {} as {} after {} s, idle periods in use: {}, periods: {}",
                type.name().toLowerCase(Locale.ROOT), on ? "active" : "released",
                VERIFY_ATTEMPTS * VERIFY_DELAY_SECONDS, state.enabled(), E3DCIdlePeriod.toText(state.periods()));
        updateState(channelUID, blocked ? OnOffType.ON : OnOffType.OFF);
    }

    /** The periods of the device together with whether it uses them. */
    private record IdleState(List<E3DCIdlePeriod> periods, boolean enabled) {
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
            updateState(new ChannelUID(getThing().getUID(), E3DCBindingConstants.GROUP_IDLE_PERIODS + "#" + channelId),
                    blocked ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    public void onData(RscpValue response) {
        super.onData(response);
        updateTrackers(response);
        updateBlockSwitches(response);
        updateManualChargeRemaining(response);
        updateCommandOptions(response);
        logManualCharge(response);
        repeatHeldMode();
    }

    /**
     * Offers the energies the user may write. The device states the highest reserve it accepts and the battery
     * states how much energy it holds, so both choices follow the installed battery rather than fixed values.
     */
    private void updateCommandOptions(RscpValue response) {
        ThingUID thingUID = getThing().getUID();
        Double reserveMax = response.findDouble(RscpTag.SE_PARAM_EP_RESERVE_MAX_W);
        if (reserveMax != null) {
            offerOptions(
                    new ChannelUID(thingUID,
                            E3DCBindingConstants.GROUP_EMERGENCY + "#" + E3DCBindingConstants.CHANNEL_ENERGY),
                    E3DCCommandOptions.reserve(reserveMax));
        }
        double usableWh = usableBatteryWh(response);
        if (usableWh > 0) {
            offerOptions(
                    new ChannelUID(thingUID,
                            E3DCBindingConstants.GROUP_MANUAL_BATTERY_CHARGE + "#" + CHANNEL_SET_MANUAL_CHARGE),
                    E3DCCommandOptions.manualCharge(usableWh));
        }
    }

    /** Adds a choice list, unless there was nothing to derive it from. */
    private void offerOptions(ChannelUID channelUID, List<CommandOption> options) {
        if (!options.isEmpty()) {
            commandOptions.setCommandOptions(channelUID, options);
        }
    }

    /** The energy the batteries hold when they are full, their usable capacity in Ah times their voltage in V. */
    private static double usableBatteryWh(RscpValue response) {
        double wattHours = 0;
        for (RscpValue battery : response.findAll(RscpTag.BAT_DATA)) {
            Double ampereHours = battery.findDouble(RscpTag.BAT_USABLE_CAPACITY);
            Double volts = battery.findDouble(RscpTag.BAT_MODULE_VOLTAGE);
            if (ampereHours != null && volts != null) {
                wattHours += ampereHours * volts;
            }
        }
        return wattHours;
    }

    /** The device reports one list of idle periods, so each channel renders only its own direction. */
    @Override
    protected void updateListChannel(ChannelUID channelUID, String channelId, E3DCChannelSpec spec,
            RscpValue response) {
        if (spec.tag() == RscpTag.EMS_GET_IDLE_PERIODS_2) {
            E3DCIdlePeriod.Type type = CHANNEL_CHARGE_PERIODS.equals(channelId) ? E3DCIdlePeriod.Type.CHARGE
                    : E3DCIdlePeriod.Type.DISCHARGE;
            updateState(channelUID, new StringType(E3DCIdlePeriod.toTextFromResponse(response, type)));
        } else {
            super.updateListChannel(channelUID, channelId, spec, response);
        }
    }

    @Override
    void updateChannel(String groupId, String channelId, E3DCChannelSpec spec, RscpValue value) {
        if (E3DCBindingConstants.GROUP_MANUAL_BATTERY_CHARGE.equals(groupId)
                && MANUAL_CHARGE_ENERGY_CHANNELS.contains(channelId) && value.getValue() instanceof Number number) {
            // a plain number on an energy item would be read in the unit of the item, so the unit is stated
            updateState(new ChannelUID(getThing().getUID(), groupId + "#" + channelId),
                    new QuantityType<>(spec.scale(number.doubleValue()), Units.WATT_HOUR));
            return;
        }
        super.updateChannel(groupId, channelId, spec, value);
    }

    /**
     * Reads the values that describe the device and publishes them as thing properties. They are no
     * measurements, so they are read once per connection instead of being polled on every cycle.
     */
    private void readProperties() {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null || !bridge.isConnected()) {
            return;
        }
        List<RscpValue> request = new ArrayList<>();
        for (int valueTag : PROPERTY_VALUE_TAGS) {
            request.add(RscpValue.none(RscpTag.pollTagFor(valueTag)));
        }
        RscpValue response;
        try {
            response = bridge.sendRequest(request);
        } catch (RscpException e) {
            logger.debug("The properties of the device could not be read: {}", e.getMessage());
            return;
        }
        setProperty(Thing.PROPERTY_FIRMWARE_VERSION, response.findString(RscpTag.INFO_SW_RELEASE));
        setProperty(Thing.PROPERTY_SERIAL_NUMBER, response.findString(RscpTag.INFO_SERIAL_NUMBER));
        setProperty(PROPERTY_PRODUCTION_DATE, response.findString(RscpTag.INFO_PRODUCTION_DATE));
        setProperty(PROPERTY_TIME_ZONE, response.findString(RscpTag.INFO_TIME_ZONE));
        setProperty(PROPERTY_INSTALLED_PEAK_POWER, kilowatts(response, RscpTag.EMS_INSTALLED_PEAK_POWER));
        setProperty(PROPERTY_INVERTER_COUNT, number(response, RscpTag.PVI_INVERTER_COUNT));
        setProperty(PROPERTY_BATTERY_COUNT, batteryCount(response));
        setProperty(PROPERTY_COUPLING_MODE, number(response, RscpTag.EMS_COUPLING_MODE));
    }

    /** The number of batteries: one container per battery on some devices, a plain value on others. */
    private static @Nullable String batteryCount(RscpValue response) {
        RscpValue value = response.find(RscpTag.BAT_AVAILABLE_BATTERIES);
        if (value != null && value.getType() == RscpType.CONTAINER) {
            return Integer.toString(value.findAll(RscpTag.BAT_INDEX).size());
        }
        return number(response, RscpTag.BAT_AVAILABLE_BATTERIES);
    }

    private static @Nullable String number(RscpValue response, int tag) {
        Number value = response.findNumber(tag);
        return value != null ? Long.toString(value.longValue()) : null;
    }

    private static @Nullable String kilowatts(RscpValue response, int tag) {
        Number value = response.findNumber(tag);
        return value != null && value.doubleValue() > 0
                ? String.format(Locale.ROOT, "%.1f kW", value.doubleValue() / 1000.0)
                : null;
    }

    /** Publishes the energy the running manual charge still has to charge; the device reports neither value. */
    private void updateManualChargeRemaining(RscpValue response) {
        RscpValue container = response.find(RscpTag.EMS_GET_MANUAL_CHARGE);
        if (container == null) {
            return;
        }
        RscpValue active = container.find(RscpTag.EMS_MANUAL_CHARGE_ACTIVE);
        Number target = container.findNumber(RscpTag.EMS_UNDEFINED_POWER_VALUE);
        Number charged = container.findNumber(RscpTag.EMS_MANUAL_CHARGE_ENERGY_COUNTER);
        if (active == null || target == null || charged == null) {
            return;
        }
        updateState(
                new ChannelUID(getThing().getUID(),
                        E3DCBindingConstants.GROUP_MANUAL_BATTERY_CHARGE + "#" + CHANNEL_MANUAL_CHARGE_REMAINING),
                new QuantityType<>(remainingWh(Boolean.TRUE.equals(active.getValue()), target.doubleValue(),
                        charged.doubleValue()), Units.WATT_HOUR));
    }

    /**
     * The energy a manual charge still has to charge: nothing while no charge runs. The charged energy can start
     * slightly below zero, so the result never exceeds the energy the charge was started with.
     */
    static double remainingWh(boolean active, double targetWh, double chargedWh) {
        if (!active) {
            return 0;
        }
        return Math.min(targetWh, Math.max(0, targetWh - chargedWh));
    }

    /**
     * Logs what the device answers about the manual battery charge: a summary whenever it changes at debug
     * level, the whole answer with every poll at trace level.
     */
    private void logManualCharge(RscpValue response) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        RscpValue container = response.find(RscpTag.EMS_GET_MANUAL_CHARGE);
        if (container == null) {
            String missing = RscpTag.nameOf(RscpTag.EMS_GET_MANUAL_CHARGE) + " was not part of the answer";
            if (!missing.equals(loggedManualCharge)) {
                loggedManualCharge = missing;
                logger.debug("Manual battery charge: {}", missing);
            }
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Manual battery charge: {}", container.describeTree());
        }
        // the nanoseconds of the start time vary with every answer, so the summary shows whole seconds only
        RscpValue lastStart = container.find(RscpTag.EMS_MANUAL_CHARGE_LASTSTART);
        String summary = "active=" + describe(container.find(RscpTag.EMS_MANUAL_CHARGE_ACTIVE)) + ", started with="
                + describe(container.find(RscpTag.EMS_UNDEFINED_POWER_VALUE)) + " Wh, charged="
                + describe(container.find(RscpTag.EMS_MANUAL_CHARGE_ENERGY_COUNTER)) + " Wh, last start="
                + (lastStart != null ? timestampState(lastStart) : "-") + ", start counter="
                + describe(container.find(RscpTag.EMS_MANUAL_CHARGE_START_COUNTER));
        if (!summary.equals(loggedManualCharge)) {
            loggedManualCharge = summary;
            logger.debug("Manual battery charge: {}", summary);
        }
    }

    private static String describe(@Nullable RscpValue value) {
        return value != null ? value.describeValue() : "-";
    }

    /**
     * Creates the channels of the trackers and temperature sensors the device answers and publishes their
     * values. Every index that answers gets its channels, and a tracker connected later is picked up.
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
     * Adds the channels of the trackers and sensors that answered and remembers which ones are read. This runs
     * with every poll, because a thing defined in a file gets its channels back from the definition when the
     * file is reloaded; indexes that answered once stay, so a single failed poll does not drop a channel.
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
     * Logs what the device answered about its trackers and temperature sensors: once at debug level, and the
     * values every 20th poll, or with every poll at trace level.
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
        synchronized (heldLock) {
            mode = heldMode;
            if (mode != null && System.currentTimeMillis() >= heldUntilMillis) {
                heldMode = null;
                mode = null;
            }
        }
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (mode == null || bridge == null) {
            return;
        }
        // the write must not hold up the poll dispatch, and it runs in turn with the commands of the user
        E3DCPowerMode repeat = mode;
        commandQueue.submit(() -> {
            if (!commandHandler.send(bridge, repeat.toRequest())) {
                logger.warn("The held power mode could not be repeated, the device may no longer follow it");
            }
        });
    }
}
