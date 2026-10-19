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
package org.openhab.binding.e3dc.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.cloud.E3DCAi360Listener;
import org.openhab.binding.e3dc.internal.cloud.E3DCAi360Payloads;
import org.openhab.binding.e3dc.internal.cloud.E3DCAi360State;
import org.openhab.binding.e3dc.internal.cloud.E3DCCloudClient;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

import com.google.gson.JsonObject;

/**
 * The home power plant itself, publishing power, battery, inverter, energy and system channels.
 *
 * <p>
 * The power mode channel is not a plain value: the device only holds a forced mode for a short time, so the
 * handler sends the mode again with every poll cycle until the requested number of cycles is over.
 *
 * <p>
 * The {@code ai360} group is the exception among the channels: its values live in the E3/DC cloud and are
 * neither requested over RSCP nor reported by the device. They arrive through the bridge's cloud poll and
 * are written back over HTTPS, which is why this handler keeps the two paths apart.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCPowerPlantHandler extends E3DCThingHandler implements E3DCAi360Listener {

    /** The write only channel that starts a power mode. */
    static final String CHANNEL_SET_POWER_MODE = "set-power-mode";

    /** The switch that blocks charging of the battery around the clock. */
    static final String CHANNEL_BLOCK_CHARGING = "block-charging";
    /** The switch that blocks discharging of the battery around the clock. */
    static final String CHANNEL_BLOCK_DISCHARGING = "block-discharging";
    private static final int VERIFY_ATTEMPTS = 6;
    private static final int VERIFY_DELAY_SECONDS = 1;
    static final String CHANNEL_IDLE_PERIODS = "idle-periods";
    static final String CHANNEL_IDLE_PERIOD_SET = "idle-period-set";
    static final String CHANNEL_IDLE_PERIOD_DELETE = "idle-period-delete";

    /** Switches the cloud optimisation of the battery on and off. */
    static final String CHANNEL_AI360_ENABLED = "enabled";
    /** Whether the battery is charged when the price is below the limit. */
    static final String CHANNEL_AI360_BATTERY_PRICE_CHARGING = "battery-price-charging";
    /** The price limit of the battery in ct/kWh. */
    static final String CHANNEL_AI360_PRICE_LIMIT = "price-limit";
    /** The state of charge up to which the battery is charged from the grid. */
    static final String CHANNEL_AI360_CHARGE_UNTIL_SOC = "charge-until-soc";
    /** The months the price limit applies to. */
    static final String CHANNEL_AI360_ACTIVE_MONTHS = "active-months";
    /** Whether the battery follows an SG Ready signal. */
    static final String CHANNEL_AI360_SG_READY = "sg-ready";
    /** How charging reacts to the feed in tariff. */
    static final String CHANNEL_AI360_FEED_IN_TARIFF_TYPE = "feed-in-tariff-type";

    private final E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();
    private final E3DCCommandRegistry commandRegistry = E3DCCommandSpec.powerPlant();

    /**
     * The device replaces its whole list of idle periods on every write, so a change reads the list, alters it
     * and writes it back. Two changes at the same time would drop each other's part.
     */
    private final Object idleLock = new Object();

    private @Nullable E3DCPowerMode heldMode;
    private long heldUntilMillis;

    /** The number of trackers the channels are adapted to, or 0 while the device has not reported it. */
    private int trackerCount;

    /** The trackers beyond the fixed ones, which are read like the other channels of the inverter. */
    private volatile Map<String, E3DCChannelSpec> trackerSpecs = Map.of();

    /** What the last log of the trackers said, so that the log repeats only when it changes. */
    private E3DCTrackers.@Nullable Discovery loggedTrackers;
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
    public void initialize() {
        super.initialize();
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge != null) {
            bridge.registerAi360Listener(this);
        }
    }

    @Override
    public void dispose() {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge != null) {
            bridge.unregisterAi360Listener(this);
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (E3DCBindingConstants.GROUP_BATTERY_CONTROL.equals(channelUID.getGroupId())
                && CHANNEL_SET_POWER_MODE.equals(channelUID.getIdWithoutGroup())) {
            if (!(command instanceof RefreshType)) {
                setPowerMode(channelUID, command.toString());
            }
            return;
        }
        if (E3DCBindingConstants.GROUP_BATTERY_CONTROL.equals(channelUID.getGroupId())
                && (CHANNEL_IDLE_PERIODS.equals(channelUID.getIdWithoutGroup())
                        || CHANNEL_IDLE_PERIOD_SET.equals(channelUID.getIdWithoutGroup())
                        || CHANNEL_IDLE_PERIOD_DELETE.equals(channelUID.getIdWithoutGroup()))) {
            if (command instanceof RefreshType) {
                if (CHANNEL_IDLE_PERIODS.equals(channelUID.getIdWithoutGroup())) {
                    super.handleCommand(channelUID, command);
                }
            } else {
                writeIdleChannel(channelUID.getIdWithoutGroup(), command.toString());
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
        if (E3DCBindingConstants.GROUP_AI360.equals(channelUID.getGroupId())) {
            if (command instanceof RefreshType) {
                E3DCBridgeHandler bridge = getBridgeHandler();
                if (bridge != null) {
                    bridge.requestAi360Refresh();
                }
            } else {
                writeAi360(channelUID.getIdWithoutGroup(), command);
            }
            return;
        }
        super.handleCommand(channelUID, command);
    }

    private void setPowerMode(ChannelUID channelUID, String text) {
        E3DCPowerMode mode = E3DCPowerMode.parse(text);
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (mode == null) {
            logger.warn(
                    "'{}' is not a power mode, use auto, idle:<cycles> or charge|discharge|grid_charge:<watts>:<cycles>",
                    text);
            return;
        }
        if (bridge == null) {
            logger.debug("Cannot set the power mode without a bridge");
            return;
        }
        // the state is only updated once the device acknowledged the write
        if (commandHandler.send(bridge, mode.toRequest())) {
            synchronized (this) {
                if (mode.isHeld()) {
                    heldMode = mode;
                    heldUntilMillis = System.currentTimeMillis()
                            + mode.cycles() * bridge.getRefreshIntervalSeconds() * 1000L;
                } else {
                    heldMode = null;
                }
            }
            updateState(channelUID, new StringType(text.trim()));
        }
    }

    /**
     * Writes one AI 360 setting back to the cloud.
     *
     * <p>
     * The settings are read first because the cloud replaces the whole object on a write: sending only the
     * changed field would drop the remaining ones. The channels are updated by the poll that follows, so a
     * rejected write leaves them untouched.
     */
    private void writeAi360(String channelId, Command command) {
        E3DCBridgeHandler bridge = getBridgeHandler();
        E3DCCloudClient client = bridge != null ? bridge.getCloudClient() : null;
        if (bridge == null || client == null) {
            logger.debug("Channel {} needs the cloud token, installation and storage of the bridge", channelId);
            return;
        }
        JsonObject prices = client.readPrices();
        JsonObject ems = client.readEms();
        if (prices == null || ems == null) {
            logger.warn("The AI 360 settings were not returned by the cloud, so the command was not sent");
            return;
        }
        E3DCAi360State current = E3DCAi360Payloads.state(ems, prices);

        boolean accepted = switch (channelId) {
            case CHANNEL_AI360_ENABLED -> writeEnabled(client, prices, ems, current, OnOffType.ON.equals(command));
            case CHANNEL_AI360_BATTERY_PRICE_CHARGING ->
                writeBatteryPriceCharging(client, prices, ems, current, OnOffType.ON.equals(command));
            case CHANNEL_AI360_PRICE_LIMIT ->
                command instanceof DecimalType limit && write(client, E3DCAi360Payloads.prices(prices,
                        current.batteryPriceCharging(), current.enabled(), limit.doubleValue(), null, null), null);
            case CHANNEL_AI360_CHARGE_UNTIL_SOC -> command instanceof DecimalType soc
                    && write(client, E3DCAi360Payloads.prices(prices, current.batteryPriceCharging(), current.enabled(),
                            null, (int) Math.round(soc.doubleValue()), null), null);
            case CHANNEL_AI360_ACTIVE_MONTHS -> writeActiveMonths(client, prices, current, command);
            case CHANNEL_AI360_SG_READY -> write(client, null, E3DCAi360Payloads.ems(ems,
                    current.batteryPriceCharging(), OnOffType.ON.equals(command), current.feedInTariffType()));
            case CHANNEL_AI360_FEED_IN_TARIFF_TYPE -> write(client, null,
                    E3DCAi360Payloads.ems(ems, current.batteryPriceCharging(), current.sgReady(), command.toString()));
            default -> {
                logger.debug("Channel {} is not writable", channelId);
                yield false;
            }
        };
        if (accepted) {
            bridge.requestAi360Refresh();
        }
    }

    /** AI 360 means the battery takes part, so the installation and the storage flag are both written. */
    private boolean writeEnabled(E3DCCloudClient client, JsonObject prices, JsonObject ems, E3DCAi360State current,
            boolean enabled) {
        if (enabled) {
            return write(client, E3DCAi360Payloads.prices(prices, true, true, null, null, null),
                    E3DCAi360Payloads.ems(ems, true, current.sgReady(), current.feedInTariffType()));
        }
        // switching AI 360 off also clears the SG Ready and feed in tariff settings, as the portal does
        return write(client, E3DCAi360Payloads.prices(prices, false, false, null, null, null),
                E3DCAi360Payloads.ems(ems, false, false, null));
    }

    private boolean writeBatteryPriceCharging(E3DCCloudClient client, JsonObject prices, JsonObject ems,
            E3DCAi360State current, boolean active) {
        return write(client, E3DCAi360Payloads.prices(prices, active, active, null, null, null),
                E3DCAi360Payloads.ems(ems, active, current.sgReady(), current.feedInTariffType()));
    }

    private boolean writeActiveMonths(E3DCCloudClient client, JsonObject prices, E3DCAi360State current,
            Command command) {
        if (!(command instanceof StringType text)) {
            return false;
        }
        int months;
        try {
            months = E3DCAi360Payloads.monthsMask(text.toString());
        } catch (IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
            return false;
        }
        return write(client,
                E3DCAi360Payloads.prices(prices, current.batteryPriceCharging(), current.enabled(), null, null, months),
                null);
    }

    /**
     * Writes the given bodies, skipping the ones that are {@code null}.
     *
     * <p>
     * A command either covers the installation settings, the storage settings or both; both have to be
     * accepted for the command to count as accepted.
     */
    private boolean write(E3DCCloudClient client, @Nullable JsonObject pricesBody, @Nullable JsonObject emsBody) {
        boolean accepted = pricesBody == null || client.writePrices(pricesBody);
        if (accepted && emsBody != null) {
            accepted = client.writeEms(emsBody);
        }
        return accepted;
    }

    @Override
    public void onAi360(E3DCAi360State state) {
        updateState(channel(CHANNEL_AI360_ENABLED), state.enabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(channel(CHANNEL_AI360_BATTERY_PRICE_CHARGING),
                state.batteryPriceCharging() ? OnOffType.ON : OnOffType.OFF);
        updateState(channel(CHANNEL_AI360_SG_READY), state.sgReady() ? OnOffType.ON : OnOffType.OFF);
        updateState(channel(CHANNEL_AI360_PRICE_LIMIT), new DecimalType(state.priceLimit()));
        updateState(channel(CHANNEL_AI360_CHARGE_UNTIL_SOC), new DecimalType(state.chargeUntilSoc()));
        updateState(channel(CHANNEL_AI360_ACTIVE_MONTHS),
                new StringType(E3DCAi360Payloads.monthsText(state.activeMonths())));
        updateState(channel(CHANNEL_AI360_FEED_IN_TARIFF_TYPE), new StringType(state.feedInTariffType()));
    }

    private ChannelUID channel(String channelId) {
        return new ChannelUID(getThing().getUID(), E3DCBindingConstants.GROUP_AI360 + "#" + channelId);
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
     * Writes what an idle period channel received: the whole list, one period to add or replace, or a name to remove.
     */
    private void writeIdleChannel(String channelId, String text) {
        switch (channelId) {
            case CHANNEL_IDLE_PERIODS -> setIdlePeriods(text.trim());
            case CHANNEL_IDLE_PERIOD_SET -> {
                E3DCIdlePeriod period = E3DCIdlePeriod.parse(text.trim());
                if (period == null) {
                    logger.warn("The idle period was not written: it must read name:days:mode:active:hh:mm-hh:mm");
                    return;
                }
                changeIdlePeriods(periods -> E3DCIdlePeriod.replaceByName(periods, period));
            }
            default -> {
                String name = text.trim();
                boolean[] found = new boolean[1];
                changeIdlePeriods(periods -> {
                    List<E3DCIdlePeriod> remaining = E3DCIdlePeriod.removeByName(periods, name);
                    found[0] = remaining.size() != periods.size();
                    return remaining;
                });
                if (!found[0]) {
                    logger.warn("No idle period is named {}", name);
                }
            }
        }
    }

    /** Reads the periods of the device, changes them and writes the list back, as one step. */
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
        for (int year = LocalDate.now(ZoneOffset.UTC).getYear() - 1; year >= FIRST_HISTORY_YEAR
                && emptyInARow < MAX_EMPTY_YEARS; year--) {
            RscpValue response;
            try {
                response = bridge.sendRequest(List.of(E3DCBridgeHandler.yearHistoryRequest(year)));
            } catch (RscpException e) {
                logger.warn("History of {} not fetched: {}", year, e.getMessage());
                emptyInARow++;
                continue;
            }
            Map<String, State> states = new LinkedHashMap<>();
            for (Map.Entry<String, E3DCChannelSpec> channel : channels.entrySet()) {
                E3DCChannelSpec spec = channel.getValue();
                if (spec.container() != RscpTag.DB_HISTORY_DATA_YEAR) {
                    continue;
                }
                RscpValue value = findValue(response, spec, null);
                State state = value != null ? valueState(value, spec) : null;
                if (state != null) {
                    states.put(channel.getKey(), state);
                }
            }
            if (states.values().stream().anyMatch(E3DCPowerPlantHandler::isNonZero)) {
                Instant stamp = LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                states.forEach((key, state) -> series
                        .computeIfAbsent(key, k -> new TimeSeries(TimeSeries.Policy.REPLACE)).add(stamp, state));
                delivered++;
                emptyInARow = 0;
            } else {
                logger.debug("The device holds no history for {}", year);
                emptyInARow++;
            }
        }
        for (Map.Entry<String, TimeSeries> entry : series.entrySet()) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(),
                    E3DCBindingConstants.GROUP_HISTORY + "#" + entry.getKey());
            sendTimeSeries(channelUID, entry.getValue());
        }
        logger.info("History fetched for {} years", delivered);
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
     * Adapts the tracker channels to the number the device has and publishes their values.
     *
     * <p>
     * The first two trackers are channels of the thing description. Further ones are added once the answer
     * of the inverter shows them, so an installation with one, three or more trackers gets what it has.
     */
    private void updateTrackers(RscpValue response) {
        RscpValue inverter = response.find(RscpTag.PVI_DATA);
        if (inverter == null) {
            return;
        }
        E3DCTrackers.Discovery found = E3DCTrackers.discover(inverter);
        logTrackers(inverter, found);
        if (found.count() > 0) {
            adaptTrackerChannels(found.count());
        }
        for (Map.Entry<String, E3DCChannelSpec> tracker : trackerSpecs.entrySet()) {
            RscpValue value = findValue(response, tracker.getValue(), getDeviceIndex());
            if (value != null) {
                updateChannel(E3DCBindingConstants.GROUP_INVERTER, tracker.getKey(), tracker.getValue(), value);
            }
        }
    }

    /**
     * Adds and removes the tracker channels so that they match the number of trackers.
     *
     * <p>
     * This runs with every poll, because a thing that is defined in a file gets its channels back from the
     * definition when the file is reloaded, which removes the ones added here. Comparing the channels is
     * cheap; the thing is only updated when they differ.
     */
    private void adaptTrackerChannels(int count) {
        List<Channel> channels = E3DCTrackers.reconcile(getThing().getChannels(), getThing().getUID(), count);
        if (channels != null) {
            logger.debug("The inverter has {} PV trackers, the tracker channels are adapted", count);
            updateThing(editThing().withChannels(channels).build());
        }
        if (count != trackerCount) {
            trackerSpecs = E3DCTrackers.specs(count);
            trackerCount = count;
        }
    }

    /**
     * Logs what the device answered about its trackers.
     *
     * <p>
     * At debug level the whole answer of the inverter is logged once, and again when the number of trackers
     * changes or the level is switched on, followed by the values of the trackers every 20th poll. At trace
     * level the values are logged with every poll. Nothing is built while the level is off.
     */
    private void logTrackers(RscpValue inverter, E3DCTrackers.Discovery found) {
        if (!logger.isDebugEnabled()) {
            loggedTrackers = null;
            return;
        }
        // the texts below are long, so they are only built when they are logged
        if (!found.equals(loggedTrackers)) {
            loggedTrackers = found;
            trackerPolls = 0;
            logger.debug("PV trackers: {}", found.describe());
            logger.debug("Answer of the inverter:{}{}", System.lineSeparator(), inverter.describeTree());
        }
        if (logger.isTraceEnabled() || trackerPolls % 20 == 0) {
            logger.debug("PV tracker values: {}", E3DCTrackers.describeValues(inverter));
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
