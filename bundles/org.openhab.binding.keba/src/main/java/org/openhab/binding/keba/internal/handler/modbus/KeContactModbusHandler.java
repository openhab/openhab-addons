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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.core.thing.binding.BaseThingHandler;
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
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class KeContactModbusHandler extends BaseThingHandler {

    private static final int READ_REGISTER_LENGTH = 2;
    private static final int MAX_TRIES = 3;
    private static final int MIN_FAST_REFRESH_INTERVAL_SECONDS = 10;
    private static final int MAX_CONSECUTIVE_READ_FAILURES = 3;
    private static final long WRITE_INTERVAL_MILLIS = 5000;

    private final Logger logger = LoggerFactory.getLogger(KeContactModbusHandler.class);
    private final ModbusManager modbusManager;

    private KeContactModbusConfiguration config = new KeContactModbusConfiguration();
    private volatile @Nullable ModbusCommunicationInterface comms;
    private final List<PollTask> pollTasks = new ArrayList<>();
    private final List<ScheduledFuture<?>> writeTasks = new ArrayList<>();
    private final Object writeLock = new Object();
    private final AtomicInteger consecutiveReadFailures = new AtomicInteger();
    private long nextWriteNanos;
    private int slaveId;

    public KeContactModbusHandler(Thing thing, ModbusManager modbusManager) {
        super(thing);
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

        // opening the Modbus TCP connection and registering the polls can involve blocking I/O, so run it in the
        // background instead of blocking the calling thread
        scheduler.execute(() -> {
            ModbusTCPSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint(host, config.port, false);
            EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
            // KEBA recommends at least 0.5s between reads and 5s between writes to the same station
            poolConfiguration.setInterTransactionDelayMillis(500);
            ModbusCommunicationInterface localComms = comms = modbusManager.newModbusCommunicationInterface(endpoint,
                    poolConfiguration);

            long initialDelay = 0;
            for (KebaModbusReadRegister register : KebaModbusReadRegister.values()) {
                long refreshMillis = (register.isFast() ? config.refreshInterval : config.refreshIntervalSlow) * 1000L;
                ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, register.getAddress(), READ_REGISTER_LENGTH,
                        MAX_TRIES);
                pollTasks.add(localComms.registerRegularPoll(request, refreshMillis, initialDelay,
                        result -> handleReadResult(register, result), this::handleReadError));
                // stagger the individual polls a bit to avoid bursting the single register-at-a-time interface
                initialDelay += 200;
            }
        });
    }

    @Override
    public void dispose() {
        disposeCommunication();
        super.dispose();
    }

    private void disposeCommunication() {
        synchronized (writeLock) {
            writeTasks.forEach(task -> task.cancel(false));
            writeTasks.clear();
            nextWriteNanos = 0;
        }
        ModbusCommunicationInterface localComms = comms;
        pollTasks.forEach(task -> {
            if (localComms != null) {
                localComms.unregisterRegularPoll(task);
            }
        });
        pollTasks.clear();
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
            ModbusBitUtilities.extractStateFromRegisters(registers, 0, ValueType.UINT32)
                    .ifPresent(value -> updateState(register.getChannelId(), toState(register, value)));
        });
    }

    private void handleReadError(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        if (consecutiveReadFailures.incrementAndGet() >= MAX_CONSECUTIVE_READ_FAILURES) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-modbus-read [\"" + getFailureMessage(failure) + "\"]");
        }
    }

    private static String getFailureMessage(AsyncModbusFailure<?> failure) {
        Throwable cause = failure.getCause();
        if (cause == null) {
            return "Unknown Modbus error";
        }
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
            long now = System.nanoTime();
            long scheduledAt = Math.max(now, nextWriteNanos);
            nextWriteNanos = scheduledAt + TimeUnit.MILLISECONDS.toNanos(WRITE_INTERVAL_MILLIS);
            long delay = TimeUnit.NANOSECONDS.toMillis(scheduledAt - now);
            ScheduledFuture<?> task = scheduler.schedule(() -> {
                if (localComms.equals(comms)) {
                    localComms.submitOneTimeWrite(request, result -> {
                        logger.debug("Modbus write to register {} successful", register.getAddress());
                        if (register == KebaModbusWriteRegister.UNLOCK_PLUG) {
                            updateState(register.getChannelId(), OnOffType.OFF);
                        }
                    }, failure -> logger.warn("Modbus write to register {} failed: {}", register.getAddress(),
                            getFailureMessage(failure)));
                }
            }, delay, TimeUnit.MILLISECONDS);
            writeTasks.add(task);
        }
    }

    private void refreshChannel(ModbusCommunicationInterface comms, String channelId) {
        for (KebaModbusReadRegister register : KebaModbusReadRegister.values()) {
            if (register.getChannelId().equals(channelId)) {
                ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, register.getAddress(), READ_REGISTER_LENGTH,
                        MAX_TRIES);
                comms.submitOneTimePoll(request, result -> handleReadResult(register, result), this::handleReadError);
                return;
            }
        }
    }

    static @Nullable Integer toRawValue(KebaModbusWriteRegister register, Command command) {
        return switch (register.getKind()) {
            case SWITCH -> toRawValue(register, command == OnOffType.ON ? 1 : 0);
            case SWITCH_TRIGGER -> command == OnOffType.ON ? toRawValue(register, 0) : null;
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
