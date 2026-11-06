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
package org.openhab.binding.solarman.internal;

import static org.openhab.binding.solarman.internal.SolarmanBindingConstants.DYNAMIC_CHANNEL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.channel.BaseChannelConfig;
import org.openhab.binding.solarman.internal.channel.SolarmanChannelManager;
import org.openhab.binding.solarman.internal.defmodel.InverterDefinition;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.defmodel.Request;
import org.openhab.binding.solarman.internal.defmodel.Validation;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnection;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanProtocol;
import org.openhab.binding.solarman.internal.modbus.SolarmanProtocolFactory;
import org.openhab.binding.solarman.internal.modbus.SolarmanRawProtocol;
import org.openhab.binding.solarman.internal.modbus.SolarmanV5Protocol;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanProtocolException;
import org.openhab.binding.solarman.internal.sunsynk.SunsynkConfiguration;
import org.openhab.binding.solarman.internal.sunsynk.SunsynkProfile;
import org.openhab.binding.solarman.internal.sunsynk.SunsynkSensor;
import org.openhab.binding.solarman.internal.updater.SolarmanChannelUpdater;
import org.openhab.binding.solarman.internal.updater.SolarmanProcessResult;
import org.openhab.binding.solarman.internal.updater.SolarmanRegisterUpdater;
import org.openhab.binding.solarman.internal.util.ModbusReadPlan;
import org.openhab.binding.solarman.internal.util.ParserUtils;
import org.openhab.binding.solarman.internal.util.SolarmanWorkQueue;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarmanLoggerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Catalin Sanda - Initial contribution
 * @author Ricardo Costa - Advanced profiles and shared compatibility corrections
 */
@NonNullByDefault
public class SolarmanLoggerHandler extends BaseThingHandler {
    private final Logger logger;

    private final DefinitionParser definitionParser;
    private final SolarmanChannelManager solarmanChannelManager;
    @Nullable
    private volatile SolarmanWorkQueue workQueue;
    @Nullable
    private volatile SolarmanRegisterUpdater solarmanRegisterUpdater;
    private final Object lifecycleLock = new Object();
    private long generation;
    private boolean disposed;
    private @Nullable SolarmanLoggerConnector connector;
    private Map<String, SunsynkSensor> channels = Map.of();
    private @Nullable CommandWriter writer;

    public SolarmanLoggerHandler(Thing thing) {
        this(thing, LoggerFactory.getLogger(SolarmanLoggerHandler.class));
    }

    SolarmanLoggerHandler(Thing thing, Logger logger) {
        super(thing);
        this.logger = logger;
        this.definitionParser = new DefinitionParser();
        this.solarmanChannelManager = new SolarmanChannelManager();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Received command {} in channel {}", command, channelUID);

        SolarmanWorkQueue queue;
        SolarmanRegisterUpdater registerUpdater;
        CommandWriter commandWriter;
        SunsynkSensor sensor;
        synchronized (lifecycleLock) {
            if (disposed) {
                return;
            }
            queue = workQueue;
            registerUpdater = solarmanRegisterUpdater;
            commandWriter = writer;
            sensor = channels.get(channelUID.getId());
        }
        if (queue == null || (registerUpdater == null && commandWriter == null)) {
            return;
        }
        if (command instanceof RefreshType) {
            queue.requestRefresh();
        } else if (commandWriter != null) {
            if (sensor == null || !sensor.accepts(command)) {
                logger.warn("Unsupported command type or read-only channel: {}", channelUID);
                return;
            }
            if (!queue.submitTransaction(commandWriter.create(sensor, command))) {
                logger.warn("Command queue is full or disposed; rejected command for {}", channelUID);
            }
        } else if (registerUpdater != null && (command instanceof DecimalType || command instanceof QuantityType<?>
                || command instanceof OnOffType || command instanceof StringType || command instanceof DateTimeType)) {
            if (!queue.submit(() -> {
                try {
                    registerUpdater.updateLoggerRegisters(channelUID, command);
                } catch (RuntimeException e) {
                    logger.warn("Command {} failed for {}: {}", command, channelUID, e.getMessage());
                    throw e;
                }
            })) {
                logger.warn("Command queue is full or disposed; rejected command for {}", channelUID);
            }
        } else {
            logger.warn("Unsupported command type for {}: {}", channelUID, command.getClass().getSimpleName());
        }
    }

    @Override
    public void initialize() {
        long currentGeneration = resetWork(false);
        SolarmanLoggerConfiguration config = getConfigAs(SolarmanLoggerConfiguration.class);
        String profile = SolarmanBindingConstants.SUNSYNK_PROFILES.get(thing.getThingTypeUID());
        if (profile == null) {
            profile = SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.get(config.inverterType);
        }
        if (profile != null) {
            initializeAdvanced(profile, currentGeneration);
            return;
        }
        synchronized (lifecycleLock) {
            if (!disposed && generation == currentGeneration) {
                updateStatus(ThingStatus.UNKNOWN);
            }
        }

        if (config.refreshInterval < 1) {
            reportConfigurationError("Refresh interval must be positive", currentGeneration);
            return;
        }

        List<Channel> staticChannels = thing.getChannels().stream()
                .filter(channel -> !channel.getProperties().containsKey(DYNAMIC_CHANNEL)).toList();

        InverterDefinition inverterDefinition = definitionParser.parseDefinition(config.inverterType);

        if (inverterDefinition == null) {
            reportConfigurationError("Unable to find a definition for the provided inverter type", currentGeneration);
            return;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Found definition for {}", config.inverterType);
            }
        }

        logger.debug("Raw Type {}", config.solarmanLoggerMode);

        SolarmanProtocol solarmanProtocol = SolarmanProtocolFactory.createSolarmanProtocol(config);

        String additionalRequests = Objects.requireNonNullElse(config.getAdditionalRequests(), "");

        List<Request> configuredRequests = !additionalRequests.isBlank()
                ? mergeRequests(inverterDefinition.getRequests(), extractAdditionalRequests(additionalRequests))
                : inverterDefinition.getRequests();

        List<Request> mergedRequests;
        try {
            mergedRequests = ModbusReadPlan.normalize(configuredRequests);
        } catch (IllegalArgumentException e) {
            reportConfigurationError(e.getMessage(), currentGeneration);
            return;
        }

        Map<ParameterItem, ChannelUID> paramToChannelMapping = mergeMaps(
                extractChannelMappingFromChannels(staticChannels),
                setupChannelsForInverterDefinition(inverterDefinition, currentGeneration));

        SolarmanLoggerConnector solarmanLoggerConnector = new SolarmanLoggerConnector(config);
        SolarmanChannelUpdater solarmanChannelUpdater = new SolarmanChannelUpdater((uid, state) -> {
            synchronized (lifecycleLock) {
                if (!disposed && generation == currentGeneration) {
                    updateState(uid, state);
                }
            }
        });
        synchronized (lifecycleLock) {
            if (disposed || generation != currentGeneration) {
                solarmanLoggerConnector.close();
                return;
            }
            connector = solarmanLoggerConnector;
            solarmanRegisterUpdater = new SolarmanRegisterUpdater(paramToChannelMapping, solarmanLoggerConnector,
                    solarmanProtocol);
            workQueue = createWorkQueue(
                    () -> queryLoggerAndUpdateState(solarmanLoggerConnector, solarmanProtocol, mergedRequests,
                            paramToChannelMapping, solarmanChannelUpdater, currentGeneration),
                    config.refreshInterval, e -> {
                        if (e instanceof IllegalArgumentException || e instanceof ArithmeticException) {
                            logger.warn("Rejected command: {}", e.getMessage());
                        } else {
                            reportCommunicationError(e.getMessage(), currentGeneration);
                        }
                    });
        }
    }

    private void initializeAdvanced(String profileName, long currentGeneration) {
        try {
            SunsynkConfiguration configuration = getConfigAs(SunsynkConfiguration.class);
            long serial = Long.parseUnsignedLong(configuration.serialNumber);
            if (configuration.hostname.isBlank() || configuration.port < 1 || configuration.port > 65535
                    || configuration.refreshInterval < 5 || serial < 1 || serial > 0xFFFFFFFFL
                    || configuration.slaveId < 1 || configuration.slaveId > 247) {
                throw new IllegalArgumentException("Invalid hostname, port, serial number or refresh interval");
            }
            if (SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER.equals(thing.getThingTypeUID())
                    && thing.getChannels().stream()
                            .anyMatch(channel -> !channel.getProperties().containsKey(DYNAMIC_CHANNEL))) {
                throw new IllegalArgumentException("Custom channels require a legacy inverter profile");
            }
            ZoneId zone = ZoneId.of(configuration.timeZone);
            if (!Objects.requireNonNullElse(configuration.additionalRequests, "").isBlank()) {
                throw new IllegalArgumentException(
                        "Additional requests are supported only by legacy inverter profiles");
            }
            SunsynkProfile profile = loadProfile(profileName);
            List<Request> plan = profile.readPlan();
            boolean dynamicProfile = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER
                    .equals(thing.getThingTypeUID());
            Thing updatedThing = dynamicProfile ? buildAdvancedChannels(profileName, profile) : thing;
            Map<String, SunsynkSensor> channelMappings = profile.sensors().stream()
                    .collect(Collectors.toUnmodifiableMap(SunsynkSensor::channelId, s -> s));
            SolarmanProtocol protocol = configuration.getSolarmanLoggerMode() == SolarmanLoggerMode.RAWMODBUS
                    ? new SolarmanRawProtocol(configuration, true)
                    : new SolarmanV5Protocol(configuration, true);
            SolarmanLoggerConnector currentConnector = new SolarmanLoggerConnector(configuration);
            CommandWriter commandWriter = (sensor, command) -> new AdvancedWriteTransaction(currentConnector, protocol,
                    plan, profile, sensor, command, zone, currentGeneration);
            synchronized (lifecycleLock) {
                if (disposed || generation != currentGeneration) {
                    currentConnector.close();
                    return;
                }
                if (dynamicProfile) {
                    updateThing(updatedThing);
                }
                if (disposed || generation != currentGeneration) {
                    currentConnector.close();
                    return;
                }
                connector = currentConnector;
                channels = channelMappings;
                writer = commandWriter;
                workQueue = createWorkQueue(() -> {
                    try (SolarmanLoggerConnection connection = currentConnector.createConnection()) {
                        publish(profile, readSnapshot(connection, protocol, plan), zone, currentGeneration);
                    } catch (SolarmanException e) {
                        reportError(e, currentGeneration);
                    }
                }, configuration.refreshInterval, e -> reportError(e, currentGeneration));
                updateStatus(ThingStatus.UNKNOWN);
            }
        } catch (IllegalArgumentException | java.time.DateTimeException | IOException e) {
            synchronized (lifecycleLock) {
                if (!disposed && generation == currentGeneration) {
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }
            }
        }
    }

    private class AdvancedWriteTransaction implements SolarmanWorkQueue.Transaction {
        private final SolarmanLoggerConnector currentConnector;
        private final SolarmanProtocol protocol;
        private final List<Request> plan;
        private final SunsynkProfile profile;
        private final SunsynkSensor sensor;
        private final Command command;
        private final ZoneId zone;
        private final long currentGeneration;
        private final ChannelUID channelUID;
        private final String addresses;
        private volatile boolean cancelled;
        private @Nullable SolarmanLoggerConnection connection;
        private Map<Integer, byte[]> before = Map.of();
        private State previous = UnDefType.UNDEF;
        private byte[] payload = new byte[0];
        private int attempt;
        private String stage = "pre-read";
        private final List<SolarmanProtocolException> readFailures = new ArrayList<>();

        AdvancedWriteTransaction(SolarmanLoggerConnector currentConnector, SolarmanProtocol protocol,
                List<Request> plan, SunsynkProfile profile, SunsynkSensor sensor, Command command, ZoneId zone,
                long currentGeneration) {
            this.currentConnector = currentConnector;
            this.protocol = protocol;
            this.plan = plan;
            this.profile = profile;
            this.sensor = sensor;
            this.command = command;
            this.zone = zone;
            this.currentGeneration = currentGeneration;
            channelUID = new ChannelUID(thing.getUID(), sensor.channelId());
            addresses = sensor.registers().stream().map(register -> String.format(Locale.ROOT, "0x%04X", register))
                    .collect(Collectors.joining(","));
        }

        @Override
        public SolarmanWorkQueue.Step advance() throws SolarmanException {
            if (cancelled) {
                return SolarmanWorkQueue.Step.REFRESH;
            }
            try {
                SolarmanLoggerConnection current = connection();
                if (attempt == 0) {
                    before = readSnapshot(current, protocol, plan, readFailures);
                    requireControlRegisters(sensor, before);
                    previous = sensor.decode(before, zone);
                    return assign(current);
                }
                stage = "readback";
                Map<Integer, byte[]> after = readSnapshot(current, protocol, plan, readFailures);
                requireControlRegisters(sensor, after);
                if (cancelled) {
                    return SolarmanWorkQueue.Step.REFRESH;
                }
                Map<Integer, byte[]> expected = new HashMap<>(before);
                for (int index = 0; index < sensor.registers().size(); index++) {
                    expected.put(sensor.registers().get(index), Arrays.copyOfRange(payload, index * 2, index * 2 + 2));
                }
                State confirmed = sensor.decode(after, zone);
                State requested = sensor.decode(expected, zone);
                if (requested.equals(confirmed)) {
                    if (!previous.equals(confirmed)) {
                        logger.info(
                                "Confirmed register change: channel={}, registers=[{}], previous={}, requested={}, confirmed={}",
                                channelUID, addresses, previous, command, confirmed);
                    } else {
                        logger.debug(
                                "Confirmed unchanged value: channel={}, registers=[{}], requested={}, confirmed={}",
                                channelUID, addresses, command, confirmed);
                    }
                    publish(profile, after, zone, currentGeneration);
                    return completeSnapshot(plan, after) ? SolarmanWorkQueue.Step.CONFIRMED
                            : SolarmanWorkQueue.Step.REFRESH;
                }
                if ("SystemTimeRWSensor".equals(sensor.kind())) {
                    logger.debug(
                            "Clock write acknowledged without exact confirmation: channel={}, registers=[{}], requested={}, observed={}",
                            channelUID, addresses, command, confirmed);
                    publish(profile, after, zone, currentGeneration);
                    return SolarmanWorkQueue.Step.REFRESH;
                }
                logger.warn(
                        "Write confirmation mismatch: channel={}, registers=[{}], requested={}, observed={}, attempt={}/{}",
                        channelUID, addresses, command, confirmed, attempt, 2);
                publish(profile, after, zone, currentGeneration);
                if (cancelled) {
                    return SolarmanWorkQueue.Step.REFRESH;
                }
                if (attempt < 2) {
                    logger.warn("Repeating write after confirmed mismatch: channel={}, registers=[{}], requested={}",
                            channelUID, addresses, command);
                    stage = "retry-pre-read";
                    before = readSnapshot(current, protocol, plan, readFailures);
                    requireControlRegisters(sensor, before);
                    return assign(current);
                }
                throw new SolarmanProtocolException("Register write did not match device readback");
            } catch (SolarmanException e) {
                if (!cancelled) {
                    logger.warn(
                            "Write or confirmation failed: channel={}, registers=[{}], requested={}, stage={}, reason={}, readFailures={}",
                            channelUID, addresses, command, stage, e.getMessage(),
                            readFailures.stream().map(Throwable::getMessage).toList());
                }
                throw e;
            }
        }

        private SolarmanWorkQueue.Step assign(SolarmanLoggerConnection current) throws SolarmanException {
            // Each retry revalidates dependent limits and rebuilds masks from a fresh snapshot.
            payload = sensor.encode(command, profile.decode(before, zone), before, zone);
            if (cancelled) {
                return SolarmanWorkQueue.Step.REFRESH;
            }
            stage = "write";
            if (!protocol.writeRegisters(current, sensor.registers().getFirst(), payload)) {
                throw new SolarmanProtocolException("Device did not acknowledge the write");
            }
            attempt++;
            return SolarmanWorkQueue.Step.DELAYED;
        }

        private SolarmanLoggerConnection connection() throws SolarmanConnectionException {
            synchronized (this) {
                if (cancelled) {
                    throw new SolarmanConnectionException("Write transaction was cancelled");
                }
                SolarmanLoggerConnection current = connection;
                if (current != null) {
                    return current;
                }
            }
            SolarmanLoggerConnection created = currentConnector.createConnection();
            synchronized (this) {
                if (!cancelled) {
                    connection = created;
                    return created;
                }
            }
            created.close();
            throw new SolarmanConnectionException("Write transaction was cancelled");
        }

        @Override
        public void close() {
            SolarmanLoggerConnection previousConnection;
            synchronized (this) {
                cancelled = true;
                previousConnection = connection;
                connection = null;
            }
            if (previousConnection != null) {
                previousConnection.close();
            }
        }
    }

    private boolean completeSnapshot(List<Request> plan, Map<Integer, byte[]> snapshot) {
        for (Request block : plan) {
            for (int address = block.getStart(); address <= block.getEnd(); address++) {
                byte @Nullable [] word = snapshot.get(address);
                if (word == null || word.length != Short.BYTES) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requireControlRegisters(SunsynkSensor sensor, Map<Integer, byte[]> snapshot)
            throws SolarmanConnectionException {
        for (int address : sensor.registers()) {
            byte @Nullable [] word = snapshot.get(address);
            if (word == null || word.length != Short.BYTES) {
                throw new SolarmanConnectionException(String.format(Locale.ROOT,
                        "Invalid control register %d (0x%04X): expected 2 bytes, received %s", address, address,
                        word == null ? "missing" : word.length + " bytes"));
            }
        }
    }

    protected SunsynkProfile loadProfile(String name) throws IOException {
        return new SunsynkProfile(name);
    }

    protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errors) {
        return new SolarmanWorkQueue(scheduler, poll, interval, errors);
    }

    private Thing buildAdvancedChannels(String profileName, SunsynkProfile profile) {
        ThingBuilder builder = editThing();
        builder.withoutChannels(thing.getChannels().stream()
                .filter(channel -> channel.getProperties().containsKey(DYNAMIC_CHANNEL)).toList());
        for (SunsynkSensor sensor : profile.sensors()) {
            String typeId = Objects.requireNonNull(
                    SolarmanBindingConstants.SUNSYNK_CHANNEL_TYPE_IDS.get(profileName + ":" + sensor.id()));
            builder.withChannel(ChannelBuilder.create(new ChannelUID(thing.getUID(), sensor.channelId()))
                    .withType(new ChannelTypeUID(SolarmanBindingConstants.SOLARMAN_BINDING_ID, typeId))
                    .withAcceptedItemType(sensor.itemType()).withLabel(sensor.definition().path("name").asText())
                    .withKind(ChannelKind.STATE).withProperties(Map.of(DYNAMIC_CHANNEL, Boolean.TRUE.toString()))
                    .build());
        }
        return builder.build();
    }

    private Map<Integer, byte[]> readSnapshot(SolarmanLoggerConnection connection, SolarmanProtocol protocol,
            List<Request> plan) throws SolarmanException {
        return readSnapshot(connection, protocol, plan, new ArrayList<>());
    }

    private Map<Integer, byte[]> readSnapshot(SolarmanLoggerConnection connection, SolarmanProtocol protocol,
            List<Request> plan, List<SolarmanProtocolException> readFailures) throws SolarmanException {
        readFailures.clear();
        if (!connection.isConnected()) {
            throw new SolarmanConnectionException("Unable to connect to the logger");
        }
        Map<Integer, byte[]> snapshot = new HashMap<>();
        for (Request block : plan) {
            if (Thread.currentThread().isInterrupted()) {
                throw new SolarmanConnectionException("Logger task was cancelled");
            }
            try {
                snapshot.putAll(protocol.readRegisters(connection, (byte) 3, block.getStart(), block.getEnd()));
            } catch (SolarmanProtocolException e) {
                SolarmanProtocolException failure = new SolarmanProtocolException(readFailure(block, e), e);
                readFailures.add(failure);
                logger.debug("{}", failure.getMessage());
            } catch (SolarmanConnectionException e) {
                throw new SolarmanConnectionException(readFailure(block, e), e);
            }
        }
        if (snapshot.isEmpty()) {
            String reason = "No valid profile register blocks were received";
            if (!readFailures.isEmpty()) {
                throw new SolarmanConnectionException(
                        reason + ": "
                                + readFailures.stream().map(Throwable::getMessage).collect(Collectors.joining("; ")),
                        readFailures.getFirst());
            }
            throw new SolarmanConnectionException(reason);
        }
        return snapshot;
    }

    private String readFailure(Request block, SolarmanException failure) {
        List<String> causes = new ArrayList<>();
        @Nullable
        Throwable cause = failure;
        while (cause != null) {
            causes.add(cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
        }
        return String.format(Locale.ROOT, "FC03 block %d..%d (0x%04X..0x%04X): %s", block.getStart(), block.getEnd(),
                block.getStart(), block.getEnd(), String.join("; caused by ", causes));
    }

    private void publish(SunsynkProfile profile, Map<Integer, byte[]> snapshot, ZoneId zone, long currentGeneration) {
        synchronized (lifecycleLock) {
            if (disposed || generation != currentGeneration) {
                return;
            }
            Map<String, State> states = profile.decode(snapshot, zone);
            for (SunsynkSensor sensor : profile.sensors()) {
                if (disposed || generation != currentGeneration) {
                    return;
                }
                updateState(new ChannelUID(thing.getUID(), sensor.channelId()),
                        Objects.requireNonNull(states.get(sensor.id())));
            }
            if (disposed || generation != currentGeneration) {
                return;
            }
            if (snapshot.keySet().containsAll(List.of(0, 2, 3, 4, 5, 6, 7))) {
                StringBuilder serial = new StringBuilder();
                for (int i = 3; i <= 7; i++) {
                    int word = Short.toUnsignedInt(ByteBuffer.wrap(Objects.requireNonNull(snapshot.get(i))).getShort());
                    serial.append((char) (word >>> 8)).append((char) (word & 255));
                }
                int version = Short.toUnsignedInt(ByteBuffer.wrap(Objects.requireNonNull(snapshot.get(2))).getShort());
                Map<String, String> properties = new LinkedHashMap<>();
                properties.put("deviceSerialNumber", serial.toString().trim());
                properties.put("deviceProtocol", (version >>> 8) + "." + (version & 255));
                properties.put("deviceTypeCode", Integer.toString(
                        Short.toUnsignedInt(ByteBuffer.wrap(Objects.requireNonNull(snapshot.get(0))).getShort())));
                updateProperties(properties);
            }
            if (!disposed && generation == currentGeneration) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    private void reportError(Exception error, long currentGeneration) {
        if (error instanceof IllegalArgumentException || error instanceof ArithmeticException
                || error instanceof java.time.DateTimeException) {
            logger.warn("Rejected profile command: {}", error.getMessage());
            return;
        }
        synchronized (lifecycleLock) {
            if (!disposed && generation == currentGeneration) {
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
            }
        }
    }

    private void queryLoggerAndUpdateState(SolarmanLoggerConnector solarmanLoggerConnector,
            SolarmanProtocol solarmanProtocol, List<Request> mergedRequests,
            Map<ParameterItem, ChannelUID> paramToChannelMapping, SolarmanChannelUpdater solarmanChannelUpdater,
            long currentGeneration) {
        try {
            SolarmanProcessResult solarmanProcessResult = solarmanChannelUpdater.fetchDataFromLogger(mergedRequests,
                    solarmanLoggerConnector, solarmanProtocol, paramToChannelMapping);

            synchronized (lifecycleLock) {
                if (!disposed && generation == currentGeneration) {
                    if (solarmanProcessResult.hasSuccessfulResponses()) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, solarmanProcessResult.toString());
                    }
                }
            }
        } catch (Exception e) {
            reportCommunicationError(e.getMessage(), currentGeneration);
        }
    }

    private void reportCommunicationError(@Nullable String message, long currentGeneration) {
        synchronized (lifecycleLock) {
            if (!disposed && generation == currentGeneration) {
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, message);
            }
        }
    }

    private void reportConfigurationError(@Nullable String message, long currentGeneration) {
        synchronized (lifecycleLock) {
            if (!disposed && generation == currentGeneration) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, message);
            }
        }
    }

    private <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
    }

    private Map<ParameterItem, ChannelUID> extractChannelMappingFromChannels(List<Channel> channels) {
        return channels.stream().map(channel -> {
            BaseChannelConfig bcc = channel.getConfiguration().as(BaseChannelConfig.class);

            @Nullable
            String label = channel.getLabel();
            if (label == null) {
                throw new IllegalStateException("Channel label should not be null");
            }

            // Static channels use their configured conversion rule without a lookup table.
            return new AbstractMap.SimpleEntry<>(new ParameterItem(label, "N/A", "N/A", bcc.uom, bcc.scale, bcc.rule,
                    ParserUtils.parseRegisters(bcc.registers), "N/A", new Validation(), bcc.offset, Boolean.FALSE,
                    bcc.readOnly, null), channel.getUID());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // The combined requests are normalized before polling.
    private List<Request> mergeRequests(List<Request> requestList1, List<Request> requestList2) {
        return Stream.concat(requestList1.stream(), requestList2.stream()).collect(Collectors.toList());
    }

    private List<Request> extractAdditionalRequests(String channels) {
        String[] tokens = channels.split(",");
        Pattern pattern = Pattern.compile(
                "\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*:\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*-\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*");

        return Stream.of(tokens).map(pattern::matcher).filter(Matcher::matches).map(matcher -> {
            try {
                int functionCode = ParserUtils.parseNumber(matcher.group(1));
                int start = ParserUtils.parseNumber(matcher.group(2));
                int end = ParserUtils.parseNumber(matcher.group(3));
                return new Request(functionCode, start, end);
            } catch (NumberFormatException e) {
                logger.debug("Invalid number format in token: {} , ignoring additional requests", matcher.group(), e);
                return Request.NONE;
            }
        }).filter(request -> !Request.NONE.equals(request)).collect(Collectors.toList());
    }

    private Map<ParameterItem, ChannelUID> setupChannelsForInverterDefinition(InverterDefinition inverterDefinition,
            long currentGeneration) {
        ThingBuilder thingBuilder = editThing();

        List<Channel> oldDynamicChannels = thing.getChannels().stream()
                .filter(channel -> channel.getProperties().containsKey(DYNAMIC_CHANNEL)).toList();

        Map<ParameterItem, Channel> newDynamicItemChannelMap = solarmanChannelManager.generateItemChannelMap(thing,
                inverterDefinition);

        // Remove old dynamic channels
        thingBuilder.withoutChannels(oldDynamicChannels);

        // Add new dynamic channels
        newDynamicItemChannelMap.values().forEach(thingBuilder::withChannel);

        synchronized (lifecycleLock) {
            if (!disposed && generation == currentGeneration) {
                updateThing(thingBuilder.build());
            }
        }

        logger.debug("Updated thing with id {} and {} channels", thing.getThingTypeUID(), thing.getChannels().size());

        return newDynamicItemChannelMap.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getUID()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void dispose() {
        resetWork(true);
        super.dispose();
    }

    private long resetWork(boolean markDisposed) {
        SolarmanWorkQueue queue;
        SolarmanLoggerConnector currentConnector;
        long currentGeneration;
        synchronized (lifecycleLock) {
            disposed = markDisposed;
            currentGeneration = ++generation;
            queue = workQueue;
            workQueue = null;
            currentConnector = connector;
            connector = null;
            solarmanRegisterUpdater = null;
            writer = null;
            channels = Map.of();
        }
        if (queue != null) {
            queue.close();
        }
        if (currentConnector != null) {
            currentConnector.close();
        }
        return currentGeneration;
    }

    @FunctionalInterface
    private interface CommandWriter {
        SolarmanWorkQueue.Transaction create(SunsynkSensor sensor, Command command);
    }
}
