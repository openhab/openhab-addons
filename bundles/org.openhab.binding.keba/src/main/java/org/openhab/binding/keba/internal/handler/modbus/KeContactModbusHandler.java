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
package org.openhab.binding.keba.internal.handler.modbus;

import static org.openhab.binding.keba.internal.KebaBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.keba.internal.handler.KeContactProtocolHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.core.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KeContactModbusHandler} connects to a KEBA KeContact P30/P40 charging station via its Modbus TCP
 * interface. Unlike the UDP based {@code kecontact} Thing type it opens its own Modbus TCP connection directly
 * through the core {@link ModbusManager}, so it does not require a separate Modbus bridge Thing to be configured.
 *
 * The KEBA Modbus TCP interface only supports reading a single, two-word ({@code UINT32}) register per request, so
 * every readable register is polled independently (see {@link KebaModbusReadRegister}).
 *
 * @author Michael Weger - Initial contribution
 * @author Michael Weger - Combined Thing selective polling
 */
@NonNullByDefault
public class KeContactModbusHandler extends KeContactProtocolHandler {

    private static final int READ_REGISTER_LENGTH = 2;
    private static final int MAX_TRIES = 3;
    private static final int MIN_FAST_REFRESH_INTERVAL_SECONDS = 10;
    private static final int MAX_CONSECUTIVE_READ_FAILURES = 3;
    private static final long WRITE_INTERVAL_MILLIS = 5000;
    private static final String PROPERTY_MODBUS_MODEL = "modbusModel";
    private static final String MODEL_P30 = "P30";
    private static final String MODEL_P40 = "P40";
    private static final List<String> P40_ONLY_CHANNELS = Objects
            .requireNonNull(List.of(CHANNEL_FAST_CHARGING_STATUS, CHANNEL_ACTIVATE_FAST_CHARGING));

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(KeContactModbusHandler.class));
    private final ModbusManager modbusManager;

    private KeContactModbusConfiguration config = new KeContactModbusConfiguration();
    private volatile @Nullable ModbusCommunicationInterface comms;
    private final List<PollTask> pollTasks = new ArrayList<>();
    private final List<ScheduledFuture<?>> writeTasks = new ArrayList<>();
    private final Object writeLock = new Object();
    private final Object connectionLock = new Object();
    private final AtomicInteger consecutiveReadFailures = new AtomicInteger();
    private final AtomicInteger connectionGeneration = new AtomicInteger();
    private long lastWriteSubmissionNanos;
    private int slaveId;
    private @Nullable ScheduledFuture<?> identificationJob;
    private boolean pollsRegistered;
    private boolean p40;
    private boolean identityRead;

    public KeContactModbusHandler(Thing thing, ModbusManager modbusManager) {
        this(thing, modbusManager, null, null);
    }

    public KeContactModbusHandler(Thing thing, ModbusManager modbusManager, @Nullable Configuration configuration,
            @Nullable Listener listener) {
        super(thing, configuration, listener);
        this.modbusManager = modbusManager;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        disposeCommunication();
        initialize();
    }

    @Override
    public void initialize() {
        config = getConfigAs(KeContactModbusConfiguration.class);
        String host = config.ipAddress;
        if (host == null || host.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-no-address");
            return;
        }
        if (config.refreshInterval < MIN_FAST_REFRESH_INTERVAL_SECONDS || config.refreshIntervalSlow <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-refresh [\"fast=" + config.refreshInterval + ", slow="
                            + config.refreshIntervalSlow + "\"]");
            return;
        }

        slaveId = config.unitId;
        updateStatus(ThingStatus.UNKNOWN);
        int generation = connectionGeneration.incrementAndGet();

        // opening the Modbus TCP connection and registering the polls can involve blocking I/O, so run it in the
        // background instead of blocking the calling thread
        scheduler.execute(() -> {
            if (isStaleGeneration(generation, connectionGeneration.get())) {
                return;
            }
            ModbusTCPSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint(host, config.port, false);
            EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
            // KEBA recommends at least 0.5s between reads and 5s between writes to the same station
            poolConfiguration.setInterTransactionDelayMillis(500);
            ModbusCommunicationInterface localComms = modbusManager.newModbusCommunicationInterface(endpoint,
                    poolConfiguration);
            synchronized (connectionLock) {
                if (isStaleGeneration(generation, connectionGeneration.get())) {
                    closeStaleComms(localComms);
                    return;
                }
                comms = localComms;
                String model = getThing().getProperties().get(PROPERTY_MODBUS_MODEL);
                if (MODEL_P30.equals(model) || MODEL_P40.equals(model)) {
                    registerPolls(localComms, generation, MODEL_P40.equals(model));
                } else {
                    identifyProduct(localComms, generation);
                }
            }
        });
    }

    private void identifyProduct(ModbusCommunicationInterface localComms, int generation) {
        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, KebaModbusReadRegister.PRODUCT_INFO.getAddress(),
                READ_REGISTER_LENGTH, MAX_TRIES);
        localComms.submitOneTimePoll(request, result -> {
            if (isStaleGeneration(generation, connectionGeneration.get())) {
                return;
            }
            result.getRegisters().ifPresentOrElse(registers -> {
                ModbusBitUtilities.extractStateFromRegisters(registers, 0, ValueType.UINT32).ifPresentOrElse(value -> {
                    consecutiveReadFailures.set(0);
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    long productInfo = value.longValue();
                    if (isP30Product(productInfo)) {
                        var thingBuilder = editThing().withProperty(PROPERTY_MODBUS_MODEL, MODEL_P30)
                                .withProperty(PROPERTY_MODEL, MODEL_P30).withProperty(PROPERTY_PRODUCT_TYPE,
                                        Objects.requireNonNull(Long.toString(productInfo)));
                        P40_ONLY_CHANNELS.forEach(
                                channel -> thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), channel)));
                        updateThing(thingBuilder.build());
                        if (isCombined()) {
                            registerPolls(localComms, generation, false);
                        }
                    } else {
                        boolean isP40 = isP40Product(productInfo);
                        if (isP40) {
                            updateProperties(Map.of(PROPERTY_MODBUS_MODEL, MODEL_P40, PROPERTY_MODEL, MODEL_P40,
                                    PROPERTY_PRODUCT_TYPE, Objects.requireNonNull(Long.toString(productInfo))));
                        } else {
                            updateProperties(
                                    Map.of(PROPERTY_PRODUCT_TYPE, Objects.requireNonNull(Long.toString(productInfo))));
                        }
                        registerPolls(localComms, generation, isP40);
                    }
                }, () -> retryIdentification(localComms, generation));
            }, () -> retryIdentification(localComms, generation));
        }, failure -> {
            if (!isStaleGeneration(generation, connectionGeneration.get())) {
                handleReadError(KebaModbusReadRegister.PRODUCT_INFO, failure);
                retryIdentification(localComms, generation);
            }
        });
    }

    private void retryIdentification(ModbusCommunicationInterface localComms, int generation) {
        if (!isCombined()) {
            registerPolls(localComms, generation, true);
            return;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Modbus identification failed");
        synchronized (connectionLock) {
            if (generation == connectionGeneration.get()) {
                identificationJob = scheduler.schedule(() -> identifyProduct(localComms, generation), 300,
                        TimeUnit.SECONDS);
            }
        }
    }

    static boolean isP30Product(long productInfo) {
        return Long.toString(productInfo).startsWith("3");
    }

    static boolean isP40Product(long productInfo) {
        return Long.toString(productInfo).startsWith("4");
    }

    private void registerPolls(ModbusCommunicationInterface localComms, int generation, boolean isP40) {
        if (generation != connectionGeneration.get()) {
            return;
        }
        pollsRegistered = true;
        p40 = isP40;
        long initialDelay = 0;
        for (KebaModbusReadRegister register : KebaModbusReadRegister.values()) {
            if (register.isP40Only() && !isP40) {
                continue;
            }
            if (isCombined() && register != KebaModbusReadRegister.STATE && register != KebaModbusReadRegister.SERIAL
                    && propertyName(register) == null && !isLinked(register.getChannelId())) {
                continue;
            }
            long refreshMillis = (register.isFast() ? config.refreshInterval : config.refreshIntervalSlow) * 1000L;
            ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, register.getAddress(), READ_REGISTER_LENGTH,
                    MAX_TRIES);
            if (isCombined() && (register == KebaModbusReadRegister.SERIAL || propertyName(register) != null)) {
                if (!identityRead && register != KebaModbusReadRegister.PRODUCT_INFO) {
                    localComms.submitOneTimePoll(request, result -> {
                        if (generation == connectionGeneration.get()) {
                            handleReadResult(register, result);
                        }
                    }, failure -> {
                        if (generation == connectionGeneration.get()) {
                            handleReadError(register, failure);
                        }
                    });
                }
                continue;
            }
            PollTask pollTask = localComms.registerRegularPoll(request, refreshMillis, initialDelay, result -> {
                if (generation == connectionGeneration.get()) {
                    handleReadResult(register, result);
                }
            }, failure -> {
                if (generation == connectionGeneration.get()) {
                    handleReadError(register, failure);
                }
            });
            synchronized (pollTasks) {
                if (!isStaleGeneration(generation, connectionGeneration.get())) {
                    pollTasks.add(pollTask);
                } else {
                    localComms.unregisterRegularPoll(pollTask);
                    break;
                }
            }
            initialDelay += 200;
        }
        identityRead = true;
    }

    private void closeStaleComms(ModbusCommunicationInterface localComms) {
        try {
            localComms.close();
        } catch (Exception e) {
            logger.debug("Error closing stale Modbus communication interface: {}", e.getMessage());
        }
    }

    public void refreshLinkedPolls() {
        synchronized (connectionLock) {
            ModbusCommunicationInterface localComms = comms;
            if (!isCombined() || localComms == null || !pollsRegistered) {
                return;
            }
            synchronized (pollTasks) {
                pollTasks.forEach(localComms::unregisterRegularPoll);
                pollTasks.clear();
            }
            registerPolls(localComms, connectionGeneration.get(), p40);
        }
    }

    static boolean isStaleGeneration(int expected, int current) {
        return expected != current;
    }

    @Override
    public void dispose() {
        disposeCommunication();
        super.dispose();
    }

    private void disposeCommunication() {
        synchronized (connectionLock) {
            connectionGeneration.incrementAndGet();
            disposeCommunicationLocked();
        }
    }

    private void disposeCommunicationLocked() {
        pollsRegistered = false;
        identityRead = false;
        ScheduledFuture<?> localIdentificationJob = identificationJob;
        if (localIdentificationJob != null) {
            localIdentificationJob.cancel(false);
            identificationJob = null;
        }
        synchronized (writeLock) {
            writeTasks.forEach(task -> task.cancel(false));
            writeTasks.clear();
            lastWriteSubmissionNanos = 0;
        }
        ModbusCommunicationInterface localComms = comms;
        synchronized (pollTasks) {
            pollTasks.forEach(task -> {
                if (localComms != null) {
                    localComms.unregisterRegularPoll(task);
                }
            });
            pollTasks.clear();
        }
        if (localComms != null) {
            try {
                localComms.close();
            } catch (Exception e) {
                logger.warn("Error closing Modbus communication interface: {}", e.getMessage());
            }
        }
        comms = null;
        consecutiveReadFailures.set(0);
    }

    private void handleReadResult(KebaModbusReadRegister register, AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            consecutiveReadFailures.set(0);
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            ModbusBitUtilities.extractStateFromRegisters(registers, 0, ValueType.UINT32).ifPresent(value -> {
                if (register == KebaModbusReadRegister.SERIAL) {
                    updateProperties(
                            Map.of(PROPERTY_SERIAL, Objects.requireNonNull(String.valueOf(value.longValue()))));
                } else {
                    String propertyName = propertyName(register);
                    if (propertyName != null) {
                        updateProperties(
                                Map.of(propertyName, Objects.requireNonNull(toState(register, value).toString())));
                    } else {
                        updateState(register.getChannelId(), toState(register, value));
                    }
                }
            });
        });
    }

    static @Nullable String propertyName(KebaModbusReadRegister register) {
        return switch (register) {
            case PRODUCT_INFO -> PROPERTY_PRODUCT_TYPE;
            case SOFTWARE_VERSION -> PROPERTY_FIRMWARE;
            case HARDWARE_REVISION_DEVICE, HARDWARE_REVISION_KC_MS10 -> register.getChannelId();
            default -> null;
        };
    }

    private void handleReadError(KebaModbusReadRegister register,
            AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        if (register.isOptional()) {
            logger.debug("Optional Modbus register {} is unavailable: {}", register.getAddress(),
                    getFailureMessage(failure));
            return;
        }
        if (consecutiveReadFailures.incrementAndGet() >= MAX_CONSECUTIVE_READ_FAILURES) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-modbus-read [\"" + getFailureMessage(failure) + "\"]");
        }
    }

    private static String getFailureMessage(AsyncModbusFailure<?> failure) {
        Throwable cause = failure.getCause();
        String message = cause.getMessage();
        return message != null ? message : "Unknown Modbus error";
    }

    static org.openhab.core.types.State toState(KebaModbusReadRegister register, DecimalType value) {
        return switch (register.getKind()) {
            case CURRENT_MA -> new QuantityType<>(value.doubleValue() / 1000.0, Units.AMPERE);
            case VOLTAGE_V -> new QuantityType<>(value.doubleValue(), Units.VOLT);
            case POWER_MW -> new QuantityType<>(value.doubleValue() / 1000.0, Units.WATT);
            case ENERGY_01WH -> new QuantityType<>(value.doubleValue() / 10.0, Units.WATT_HOUR);
            case PERMILLE_PERCENT -> new QuantityType<>(value.doubleValue() / 10.0, Units.PERCENT);
            case TIME_S -> new QuantityType<>(value.doubleValue(), Units.SECOND);
            case DECIMAL_STRING -> new StringType(String.valueOf(value.longValue()));
            case HEX_STRING -> new StringType(String.format("%08X", value.longValue()));
            case NUMBER -> value;
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ModbusCommunicationInterface localComms = comms;
        if (localComms == null) {
            return;
        }
        String channelId = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            refreshChannel(localComms, channelId);
            return;
        }

        KebaModbusWriteRegister register;
        try {
            register = KebaModbusWriteRegister.fromChannelId(channelId);
        } catch (IllegalArgumentException e) {
            return;
        }

        Integer rawValue = toRawValue(register, command);
        if (rawValue == null) {
            logger.debug("Unsupported command '{}' for channel '{}'", command, channelId);
            return;
        }

        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(slaveId,
                register.getAddress(), new ModbusRegisterArray(rawValue), false, MAX_TRIES);
        scheduleWrite(localComms, request, register);
    }

    private void scheduleWrite(ModbusCommunicationInterface localComms, ModbusWriteRegisterRequestBlueprint request,
            KebaModbusWriteRegister register) {
        synchronized (writeLock) {
            writeTasks.removeIf(ScheduledFuture::isDone);
            scheduleWriteTask(() -> submitWriteWhenAllowed(localComms, request, register), 1);
        }
    }

    private void submitWriteWhenAllowed(ModbusCommunicationInterface localComms,
            ModbusWriteRegisterRequestBlueprint request, KebaModbusWriteRegister register) {
        synchronized (writeLock) {
            if (!localComms.equals(comms)) {
                return;
            }
            long now = System.nanoTime();
            long elapsed = now - lastWriteSubmissionNanos;
            long intervalNanos = TimeUnit.MILLISECONDS.toNanos(WRITE_INTERVAL_MILLIS);
            if (lastWriteSubmissionNanos != 0 && !isWriteIntervalElapsed(elapsed, intervalNanos)) {
                scheduleWriteTask(() -> submitWriteWhenAllowed(localComms, request, register),
                        TimeUnit.NANOSECONDS.toMillis(intervalNanos - elapsed) + 1);
                return;
            }
            lastWriteSubmissionNanos = now;
            localComms.submitOneTimeWrite(request, result -> {
                logger.debug("Modbus write to register {} successful", register.getAddress());
                if (register.getKind() == KebaModbusWriteRegister.Kind.SWITCH_TRIGGER) {
                    updateState(register.getChannelId(), OnOffType.OFF);
                }
            }, failure -> logger.warn("Modbus write to register {} failed: {}", register.getAddress(),
                    getFailureMessage(failure)));
        }
    }

    static boolean isWriteIntervalElapsed(long elapsedNanos, long intervalNanos) {
        return elapsedNanos >= intervalNanos;
    }

    private void scheduleWriteTask(Runnable action, long delayMillis) {
        final ScheduledFuture<?>[] taskReference = new ScheduledFuture<?>[1];
        taskReference[0] = scheduler.schedule(() -> {
            try {
                action.run();
            } finally {
                synchronized (writeLock) {
                    writeTasks.remove(taskReference[0]);
                }
            }
        }, Math.max(1, delayMillis), TimeUnit.MILLISECONDS);
        writeTasks.add(taskReference[0]);
    }

    private void refreshChannel(ModbusCommunicationInterface comms, String channelId) {
        for (KebaModbusReadRegister register : KebaModbusReadRegister.values()) {
            if (register.getChannelId().equals(channelId)) {
                ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, register.getAddress(), READ_REGISTER_LENGTH,
                        MAX_TRIES);
                comms.submitOneTimePoll(request, result -> handleReadResult(register, result),
                        failure -> handleReadError(register, failure));
                return;
            }
        }
    }

    static @Nullable Integer toRawValue(KebaModbusWriteRegister register, Command command) {
        return switch (register.getKind()) {
            case SWITCH -> command == OnOffType.ON ? toRawValue(register, 1)
                    : command == OnOffType.OFF ? toRawValue(register, 0) : null;
            case SWITCH_TRIGGER -> command == OnOffType.ON ? toRawValue(register, register.getMaxRawValue()) : null;
            case NUMBER -> command instanceof DecimalType decimal ? toRawValue(register, decimal.longValue()) : null;
            case CURRENT_MA -> {
                if (command instanceof QuantityType<?> quantity) {
                    QuantityType<?> ampere = Objects.requireNonNull(quantity.toUnit(Units.AMPERE));
                    yield toRawValue(register, Math.round(ampere.doubleValue() * 1000.0));
                } else if (command instanceof DecimalType decimal) {
                    yield toRawValue(register, decimal.longValue());
                }
                yield null;
            }
            case ENERGY_10WH -> {
                if (command instanceof QuantityType<?> quantity) {
                    QuantityType<?> wattHour = Objects.requireNonNull(quantity.toUnit(Units.WATT_HOUR));
                    yield toRawValue(register, Math.round(wattHour.doubleValue() / 10.0));
                } else if (command instanceof DecimalType decimal) {
                    yield toRawValue(register, decimal.longValue());
                }
                yield null;
            }
            case TIME_S -> {
                if (command instanceof QuantityType<?> quantity) {
                    QuantityType<?> seconds = Objects.requireNonNull(quantity.toUnit(Units.SECOND));
                    yield toRawValue(register, Math.round(seconds.doubleValue()));
                } else if (command instanceof DecimalType decimal) {
                    yield toRawValue(register, decimal.longValue());
                }
                yield null;
            }
        };
    }

    private static @Nullable Integer toRawValue(KebaModbusWriteRegister register, long value) {
        return value >= 0 && value <= register.getMaxRawValue() && value <= 0xFFFFL ? (int) value : null;
    }
}
