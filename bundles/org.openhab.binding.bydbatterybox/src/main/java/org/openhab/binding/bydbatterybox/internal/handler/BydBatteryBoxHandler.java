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
package org.openhab.binding.bydbatterybox.internal.handler;

import static org.openhab.binding.bydbatterybox.internal.BydBatteryBoxBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bydbatterybox.internal.BydBatteryBoxConfiguration;
import org.openhab.binding.bydbatterybox.internal.dto.BatteryType;
import org.openhab.binding.bydbatterybox.internal.dto.CellStatistics;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.dto.SystemInfo;
import org.openhab.binding.bydbatterybox.internal.dto.SystemStatus;
import org.openhab.binding.bydbatterybox.internal.dto.TowerData;
import org.openhab.binding.bydbatterybox.internal.protocol.BydClient;
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException;
import org.openhab.binding.bydbatterybox.internal.protocol.BydConnection;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polls one BYD Battery-Box over a short-lived TCP session per cycle.
 * <p>
 * The battery serves a single client only. Every cycle therefore opens one connection, reads identity and status,
 * optionally the cell details and the event logs, and closes the connection again. The job runs on a binding-specific
 * thread pool because the session blocks for up to a few seconds (socket reads, waiting for a measurement).
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BydBatteryBoxHandler extends BaseThingHandler {

    private static final Unit<?> MILLIVOLT = MetricPrefix.MILLI(Units.VOLT);
    private static final long MAX_BACKOFF_SECONDS = 15 * 60;
    private static final long INITIAL_DELAY_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(BydBatteryBoxHandler.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool(BINDING_ID);
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;
    private final BydClient.Sleeper sleeper;
    private final BydTexts texts;

    /** Incremented on every (re)initialization and on dispose; a job of an older generation must not publish. */
    private final AtomicInteger generation = new AtomicInteger();
    private @Nullable ScheduledFuture<?> pollJob;
    /** Last published state per channel, to answer REFRESH commands without contacting the battery. */
    private final Map<ChannelUID, State> lastStates = new ConcurrentHashMap<>();
    /** Connection of the running cycle, closed by {@link #dispose()} to abort blocking reads. */
    private final AtomicReference<@Nullable BydConnection> activeConnection = new AtomicReference<>();

    /** State of one initialization; only accessed by its polling job. */
    private static final class PollContext {
        final int generation;
        final BydBatteryBoxConfiguration config;
        final EventTracker eventTracker = new EventTracker();
        @Nullable
        Instant lastDetail;
        @Nullable
        Instant lastEventLog;
        @Nullable
        String topology;
        Duration clockOffset = Duration.ZERO;
        int failures;
        long skipCycles;
        boolean unsupportedTypeLogged;

        PollContext(int generation, BydBatteryBoxConfiguration config) {
            this.generation = generation;
            this.config = config;
        }
    }

    public BydBatteryBoxHandler(Thing thing, TranslationProvider i18nProvider, LocaleProvider localeProvider,
            Bundle bundle) {
        this(thing, i18nProvider, localeProvider, bundle, Thread::sleep);
    }

    BydBatteryBoxHandler(Thing thing, TranslationProvider i18nProvider, LocaleProvider localeProvider, Bundle bundle,
            BydClient.Sleeper sleeper) {
        super(thing);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.bundle = bundle;
        this.sleeper = sleeper;
        this.texts = new BydTexts(this::text);
    }

    @Override
    public void initialize() {
        BydBatteryBoxConfiguration config = getConfigAs(BydBatteryBoxConfiguration.class);
        if (config.hostname.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.no-hostname");
            return;
        }
        if (config.refreshInterval < BydBatteryBoxConfiguration.MIN_REFRESH_INTERVAL || config.detailInterval < 0
                || config.eventLogInterval < 0 || config.connectionTimeout < 1) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.invalid-interval");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        lastStates.clear();
        PollContext context = new PollContext(generation.incrementAndGet(), config);
        pollJob = executor.scheduleWithFixedDelay(() -> poll(context), INITIAL_DELAY_SECONDS, config.refreshInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        generation.incrementAndGet();
        ScheduledFuture<?> job = pollJob;
        pollJob = null;
        if (job != null) {
            // interrupt a running cycle, e.g. while it waits for a measurement to complete
            job.cancel(true);
        }
        BydConnection connection = activeConnection.get();
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // all channels are read-only; a REFRESH (e.g. for a newly linked item) gets the last published state, the
        // battery itself is only read by the polling job
        if (command instanceof RefreshType) {
            State state = lastStates.get(channelUID);
            if (state != null) {
                updateState(channelUID, state);
            }
        }
    }

    private boolean isCurrent(PollContext context) {
        return context.generation == generation.get();
    }

    private void poll(PollContext context) {
        if (context.skipCycles > 0) {
            context.skipCycles--;
            return;
        }
        Instant now = Instant.now();
        BydBatteryBoxConfiguration config = context.config;
        boolean detailDue = isDue(context.lastDetail, config.detailInterval, config.refreshInterval, now);
        boolean eventsDue = isDue(context.lastEventLog, config.eventLogInterval, config.refreshInterval, now);
        BydConnection connection;
        try {
            connection = BydConnection.open(config.hostname, config.port, config.connectionTimeout * 1000);
        } catch (BydCommunicationException e) {
            handleFailure(context, e);
            return;
        }
        activeConnection.set(connection);
        try (connection) {
            if (!isCurrent(context)) {
                return;
            }
            BydClient client = new BydClient(connection, sleeper);
            SystemInfo info = client.readIdentity();
            SystemStatus status = client.readStatus();
            if (!isCurrent(context)) {
                return;
            }
            applyIdentity(context, info);
            publishStatus(status);
            context.failures = 0;
            updateStatus(ThingStatus.ONLINE);

            if (detailDue && info.batteryType().hasCellDetails() && readDetails(context, client, info)) {
                context.lastDetail = now;
            }
            if (eventsDue && readEventLogs(context, client, info)) {
                context.lastEventLog = now;
            }
        } catch (BydCommunicationException e) {
            handleFailure(context, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            // keep the scheduled job alive; an unexpected error must not stop polling
            logger.warn("Unexpected error while polling {}", getThing().getUID(), e);
        } finally {
            // a newer cycle may already have registered its own connection
            activeConnection.compareAndSet(connection, null);
        }
    }

    /**
     * Interval steps are aligned to the polling cycle: a step is due if at least the interval minus half a cycle has
     * passed, so that an interval of n cycles really runs every n cycles.
     */
    private static boolean isDue(@Nullable Instant last, int intervalSeconds, int refreshSeconds, Instant now) {
        if (intervalSeconds <= 0) {
            return false;
        }
        return last == null
                || Duration.between(last, now).toMillis() >= intervalSeconds * 1000L - refreshSeconds * 500L;
    }

    private void handleFailure(PollContext context, BydCommunicationException e) {
        if (!isCurrent(context)) {
            return;
        }
        context.failures++;
        long maxSkip = Math.max(0, MAX_BACKOFF_SECONDS / context.config.refreshInterval - 1);
        context.skipCycles = Math.min(maxSkip, (1L << Math.min(context.failures - 1, 16)) - 1);
        logger.debug("Polling {} failed ({}): {}; next attempt in {} cycle(s)", getThing().getUID(), e.getReason(),
                e.getMessage(), context.skipCycles + 1);
        String key = switch (e.getReason()) {
            case UNREACHABLE -> "offline.unreachable";
            case BUSY -> "offline.busy";
            case TIMEOUT -> "offline.timeout";
            case NOT_READY -> "offline.not-ready";
            case PROTOCOL -> "offline.protocol";
        };
        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/" + key);
    }

    // ------------------------------------------------------------------------------------------ cell details

    /**
     * @return {@code true} if all towers were read
     */
    private boolean readDetails(PollContext context, BydClient client, SystemInfo info)
            throws BydCommunicationException, InterruptedException {
        for (int tower = 1; tower <= info.towers(); tower++) {
            TowerData data;
            try {
                data = client.readTower(tower, info, context.config.readExtendedCells);
            } catch (BydCommunicationException e) {
                if (e.getReason() != BydCommunicationException.Reason.NOT_READY) {
                    throw e;
                }
                logger.debug("Cell measurement of tower {} not ready: {}", tower, e.getMessage());
                return false;
            }
            if (!isCurrent(context)) {
                return false;
            }
            publishTower(data, info);
        }
        update(GROUP_DIAGNOSTICS, CHANNEL_LAST_DETAIL_UPDATE, new DateTimeType(Instant.now()));
        return true;
    }

    private void publishTower(TowerData data, SystemInfo info) {
        String group = ChannelLayout.towerGroup(data.tower());
        update(group, CHANNEL_STATE_OF_CHARGE, new QuantityType<>(data.stateOfCharge(), Units.PERCENT));
        update(group, CHANNEL_STATE_OF_HEALTH, new QuantityType<>(data.stateOfHealth(), Units.PERCENT));
        update(group, CHANNEL_VOLTAGE, new QuantityType<>(data.voltage(), Units.VOLT));
        update(group, CHANNEL_OUTPUT_VOLTAGE, new QuantityType<>(data.outputVoltage(), Units.VOLT));
        update(group, CHANNEL_CURRENT, new QuantityType<>(data.current(), Units.AMPERE));
        update(group, CHANNEL_CHARGED_ENERGY,
                new QuantityType<>(BigDecimal.valueOf(data.chargedEnergyWh(), 3), Units.KILOWATT_HOUR));
        update(group, CHANNEL_DISCHARGED_ENERGY,
                new QuantityType<>(BigDecimal.valueOf(data.dischargedEnergyWh(), 3), Units.KILOWATT_HOUR));
        update(group, CHANNEL_BALANCING_CELLS, new DecimalType(data.balancingCells()));
        update(group, CHANNEL_WARNING, OnOffType.from(data.stateBits() != 0));
        update(group, CHANNEL_WARNINGS, new StringType(texts.towerWarnings(data.stateBits())));

        List<Integer> cells = data.cellVoltages();
        CellStatistics cellStats = CellStatistics.of(cells, 0, cells.size());
        publishCellStatistics(group, cellStats);
        update(group, CHANNEL_MAX_CELL_VOLTAGE_CELL,
                cellStats == null ? UnDefType.UNDEF : new DecimalType(cellStats.maxPosition()));
        update(group, CHANNEL_MIN_CELL_VOLTAGE_CELL,
                cellStats == null ? UnDefType.UNDEF : new DecimalType(cellStats.minPosition()));

        List<Integer> temperatures = data.temperatures();
        CellStatistics tempStats = CellStatistics.of(temperatures, 0, temperatures.size());
        update(group, CHANNEL_MAX_CELL_TEMPERATURE,
                tempStats == null ? UnDefType.UNDEF : new QuantityType<>(tempStats.max(), SIUnits.CELSIUS));
        update(group, CHANNEL_MAX_CELL_TEMPERATURE_SENSOR,
                tempStats == null ? UnDefType.UNDEF : new DecimalType(tempStats.maxPosition()));
        update(group, CHANNEL_MIN_CELL_TEMPERATURE,
                tempStats == null ? UnDefType.UNDEF : new QuantityType<>(tempStats.min(), SIUnits.CELSIUS));
        update(group, CHANNEL_MIN_CELL_TEMPERATURE_SENSOR,
                tempStats == null ? UnDefType.UNDEF : new DecimalType(tempStats.minPosition()));

        int cellsPerModule = info.batteryType().getCellsPerModule();
        int sensorsPerModule = info.batteryType().getTemperatureSensorsPerModule();
        for (int module = 1; module <= info.modulesPerTower(); module++) {
            String moduleGroup = ChannelLayout.moduleGroup(data.tower(), module);
            int firstCell = (module - 1) * cellsPerModule;
            CellStatistics moduleStats = CellStatistics.of(cells, firstCell, firstCell + cellsPerModule);
            publishCellStatistics(moduleGroup, moduleStats);
            update(moduleGroup, CHANNEL_VOLTAGE, moduleStats == null ? UnDefType.UNDEF
                    : new QuantityType<>(BigDecimal.valueOf(moduleStats.sum(), 3), Units.VOLT));
            for (int i = 0; i < cellsPerModule; i++) {
                int index = firstCell + i;
                update(moduleGroup, CHANNEL_CELL_VOLTAGE_PREFIX + twoDigits(i + 1),
                        index < cells.size() ? new QuantityType<>(cells.get(index), MILLIVOLT) : UnDefType.UNDEF);
            }
            int firstSensor = (module - 1) * sensorsPerModule;
            for (int i = 0; i < sensorsPerModule; i++) {
                int index = firstSensor + i;
                update(moduleGroup, CHANNEL_CELL_TEMPERATURE_PREFIX + twoDigits(i + 1),
                        index < temperatures.size() ? new QuantityType<>(temperatures.get(index), SIUnits.CELSIUS)
                                : UnDefType.UNDEF);
            }
        }

        updateProperty(String.format(PROPERTY_TOWER_SERIAL_NUMBER, data.tower()), data.serialNumber());
        updateProperty(String.format(PROPERTY_TOWER_FIRMWARE, data.tower()),
                data.bmsFirmwareA() + " / " + data.bmsFirmwareB());
    }

    private void publishCellStatistics(String group, @Nullable CellStatistics stats) {
        if (stats == null) {
            update(group, CHANNEL_MAX_CELL_VOLTAGE, UnDefType.UNDEF);
            update(group, CHANNEL_MIN_CELL_VOLTAGE, UnDefType.UNDEF);
            update(group, CHANNEL_AVERAGE_CELL_VOLTAGE, UnDefType.UNDEF);
            update(group, CHANNEL_CELL_VOLTAGE_SPREAD, UnDefType.UNDEF);
            return;
        }
        update(group, CHANNEL_MAX_CELL_VOLTAGE, new QuantityType<>(stats.max(), MILLIVOLT));
        update(group, CHANNEL_MIN_CELL_VOLTAGE, new QuantityType<>(stats.min(), MILLIVOLT));
        update(group, CHANNEL_AVERAGE_CELL_VOLTAGE, new QuantityType<>(stats.average(), MILLIVOLT));
        update(group, CHANNEL_CELL_VOLTAGE_SPREAD, new QuantityType<>(stats.spread(), MILLIVOLT));
    }

    // ------------------------------------------------------------------------------------------ event logs

    /**
     * @return {@code true} if all units were read
     */
    private boolean readEventLogs(PollContext context, BydClient client, SystemInfo info)
            throws BydCommunicationException, InterruptedException {
        EventRecord newest = null;
        for (int unit = EventRecord.UNIT_BMU; unit <= info.towers(); unit++) {
            List<EventRecord> records;
            try {
                records = client.readEventLog(unit);
            } catch (BydCommunicationException e) {
                if (e.getReason() != BydCommunicationException.Reason.NOT_READY) {
                    throw e;
                }
                logger.debug("Event log of unit {} not ready: {}", unit, e.getMessage());
                return false;
            }
            if (!isCurrent(context)) {
                return false;
            }
            for (EventRecord record : context.eventTracker.update(unit, records)) {
                triggerChannel(new ChannelUID(thing.getUID(), GROUP_EVENTS, CHANNEL_EVENT),
                        (record.isBmu() ? "BMU" : "BMS" + record.getUnit()) + ":" + record.getCode());
            }
            if (!records.isEmpty()
                    && (newest == null || records.getFirst().getTimestamp().isAfter(newest.getTimestamp()))) {
                newest = records.getFirst();
            }
        }
        if (newest != null) {
            update(GROUP_EVENTS, CHANNEL_LAST_EVENT, new StringType(texts.event(newest)));
            Instant time = newest.getTimestamp().toInstant(ZoneOffset.UTC).minus(context.clockOffset);
            update(GROUP_EVENTS, CHANNEL_LAST_EVENT_TIME, new DateTimeType(time));
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------ identity

    private void applyIdentity(PollContext context, SystemInfo info) {
        Map<String, String> properties = new HashMap<>(editProperties());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, info.serialNumber());
        properties.put(Thing.PROPERTY_MODEL_ID,
                info.batteryType() == BatteryType.UNSUPPORTED ? "unknown (" + info.batteryTypeCode() + ")"
                        : info.batteryType().name());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, info.activeBmuFirmware());
        properties.put(PROPERTY_BMU_FIRMWARE_A, info.bmuFirmwareA());
        properties.put(PROPERTY_BMU_FIRMWARE_B, info.bmuFirmwareB());
        properties.put(PROPERTY_BMS_FIRMWARE, info.bmsFirmware() + "-" + info.activeBmsSection());
        properties.put(PROPERTY_TOWERS, String.valueOf(info.towers()));
        properties.put(PROPERTY_MODULES_PER_TOWER, String.valueOf(info.modulesPerTower()));
        properties.put(PROPERTY_CELLS_PER_MODULE, String.valueOf(info.batteryType().getCellsPerModule()));
        properties.put(PROPERTY_TEMPERATURE_SENSORS_PER_MODULE,
                String.valueOf(info.batteryType().getTemperatureSensorsPerModule()));
        properties.put(PROPERTY_INVERTER_TYPE, String.valueOf(info.inverterType()));
        properties.put(PROPERTY_APPLICATION, String.valueOf(info.application()));
        if (!properties.equals(editProperties())) {
            updateProperties(properties);
        }

        if (!info.batteryType().hasCellDetails() && !context.unsupportedTypeLogged) {
            context.unsupportedTypeLogged = true;
            logger.info("Battery type code {} of {} has no known cell layout; cell details are not available",
                    info.batteryTypeCode(), getThing().getUID());
        }
        String topology = info.batteryType() + "/" + info.towers() + "/" + info.modulesPerTower();
        if (!topology.equals(context.topology)) {
            context.topology = topology;
            reconcileChannels(info);
        }

        LocalDateTime clock = info.clock();
        if (clock != null) {
            context.clockOffset = Duration.between(Instant.now(), clock.toInstant(ZoneOffset.UTC));
            update(GROUP_DIAGNOSTICS, CHANNEL_BMU_CLOCK_OFFSET,
                    new QuantityType<>(context.clockOffset.toSeconds(), Units.SECOND));
        } else {
            update(GROUP_DIAGNOSTICS, CHANNEL_BMU_CLOCK_OFFSET, UnDefType.UNDEF);
        }
    }

    /**
     * Removes the channels of towers, modules, cells and sensors that do not exist, and adds back missing channels of
     * the static groups and of the reported topology (e.g. after a module was added). Idempotent: the Thing is only
     * updated if channels need to be removed or added.
     */
    private void reconcileChannels(SystemInfo info) {
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        List<Channel> obsolete = thing.getChannels().stream().filter(c -> !exists(c.getUID(), info)).toList();
        Set<ChannelUID> present = new HashSet<>();
        thing.getChannels().forEach(c -> present.add(c.getUID()));
        List<Channel> missing = new ArrayList<>();
        Map<String, ChannelGroupTypeUID> groups = new LinkedHashMap<>();
        groups.put(GROUP_STATUS, GROUP_TYPE_STATUS);
        groups.put(GROUP_EVENTS, GROUP_TYPE_EVENTS);
        groups.put(GROUP_DIAGNOSTICS, GROUP_TYPE_DIAGNOSTICS);
        for (String group : ChannelLayout.groups(info)) {
            groups.put(group, group.contains(GROUP_MODULE_INFIX) ? GROUP_TYPE_MODULE : GROUP_TYPE_TOWER);
        }
        for (Map.Entry<String, ChannelGroupTypeUID> entry : groups.entrySet()) {
            String group = entry.getKey();
            ChannelGroupTypeUID groupType = entry.getValue();
            for (ChannelBuilder builder : callback.createChannelBuilders(new ChannelGroupUID(thing.getUID(), group),
                    groupType)) {
                Channel channel = builder.build();
                if (!present.contains(channel.getUID()) && exists(channel.getUID(), info)) {
                    missing.add(channel);
                }
            }
        }
        if (!obsolete.isEmpty() || !missing.isEmpty()) {
            logger.debug("Adapting channels of {} to {} tower(s) with {} {} module(s): removing {}, adding {}",
                    thing.getUID(), info.towers(), info.modulesPerTower(), info.batteryType(), obsolete.size(),
                    missing.size());
            // ThingBuilder.withChannels() would replace all channels; add the missing ones individually
            ThingBuilder builder = editThing().withoutChannels(obsolete);
            missing.forEach(builder::withChannel);
            updateThing(builder.build());
        }
    }

    private static boolean exists(ChannelUID uid, SystemInfo info) {
        String group = uid.getGroupId();
        return group == null || ChannelLayout.exists(group, uid.getIdWithoutGroup(), info);
    }

    private static String twoDigits(int number) {
        return String.format("%02d", number);
    }

    // ------------------------------------------------------------------------------------------ status

    private void publishStatus(SystemStatus status) {
        update(GROUP_STATUS, CHANNEL_STATE_OF_CHARGE, new QuantityType<>(status.stateOfCharge(), Units.PERCENT));
        update(GROUP_STATUS, CHANNEL_STATE_OF_HEALTH, new QuantityType<>(status.stateOfHealth(), Units.PERCENT));
        update(GROUP_STATUS, CHANNEL_VOLTAGE, new QuantityType<>(status.voltage(), Units.VOLT));
        update(GROUP_STATUS, CHANNEL_OUTPUT_VOLTAGE, new QuantityType<>(status.outputVoltage(), Units.VOLT));
        update(GROUP_STATUS, CHANNEL_CURRENT, new QuantityType<>(status.current(), Units.AMPERE));
        update(GROUP_STATUS, CHANNEL_POWER, new QuantityType<>(
                status.current().multiply(status.outputVoltage()).setScale(0, RoundingMode.HALF_UP), Units.WATT));
        update(GROUP_STATUS, CHANNEL_MAX_CELL_VOLTAGE, new QuantityType<>(status.maxCellVoltage(), Units.VOLT));
        update(GROUP_STATUS, CHANNEL_MIN_CELL_VOLTAGE, new QuantityType<>(status.minCellVoltage(), Units.VOLT));
        update(GROUP_STATUS, CHANNEL_CELL_VOLTAGE_SPREAD, new QuantityType<>(status.maxCellVoltage()
                .subtract(status.minCellVoltage()).movePointRight(3).setScale(0, RoundingMode.HALF_UP), MILLIVOLT));
        update(GROUP_STATUS, CHANNEL_MAX_CELL_TEMPERATURE,
                new QuantityType<>(status.maxCellTemperature(), SIUnits.CELSIUS));
        update(GROUP_STATUS, CHANNEL_MIN_CELL_TEMPERATURE,
                new QuantityType<>(status.minCellTemperature(), SIUnits.CELSIUS));
        update(GROUP_STATUS, CHANNEL_BMU_TEMPERATURE, new QuantityType<>(status.bmuTemperature(), SIUnits.CELSIUS));
        update(GROUP_STATUS, CHANNEL_CHARGED_ENERGY, new QuantityType<>(status.chargedEnergy(), Units.KILOWATT_HOUR));
        update(GROUP_STATUS, CHANNEL_DISCHARGED_ENERGY,
                new QuantityType<>(status.dischargedEnergy(), Units.KILOWATT_HOUR));
        update(GROUP_STATUS, CHANNEL_EFFICIENCY,
                status.chargedEnergy().signum() > 0
                        ? new QuantityType<>(status.dischargedEnergy().multiply(BigDecimal.valueOf(100))
                                .divide(status.chargedEnergy(), 1, RoundingMode.HALF_UP), Units.PERCENT)
                        : UnDefType.UNDEF);
        update(GROUP_STATUS, CHANNEL_ERROR, OnOffType.from(status.errorBits() != 0));
        update(GROUP_STATUS, CHANNEL_ERRORS, new StringType(texts.systemErrors(status.errorBits())));
        updateProperty(PROPERTY_PARAMETER_TABLE, status.parameterTable());
    }

    // ------------------------------------------------------------------------------------------ helpers

    private void update(String group, String channel, State state) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), group, channel);
        lastStates.put(channelUID, state);
        updateState(channelUID, state);
    }

    private String text(String key, String defaultText, Object... arguments) {
        String text = i18nProvider.getText(bundle, key, null, localeProvider.getLocale(), arguments);
        return text != null ? text : MessageFormat.format(defaultText, arguments);
    }
}
