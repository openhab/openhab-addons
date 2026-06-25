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
package org.openhab.binding.modbus.ankersolix.internal;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.BaseModbusThingHandler;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base handler for all Anker SOLIX device profiles. Provides the shared Modbus
 * polling, register decoding, write, shadow-state and channel-update
 * infrastructure. Concrete handlers implement the device-specific poll ranges,
 * state mapping and command handling.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractAnkerSolixHandler extends BaseModbusThingHandler {

    protected record PollRange(ModbusReadFunctionCode functionCode, int startAddress, int length) {
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<Integer, Integer> registerCache = new ConcurrentHashMap<>();
    private final Map<String, State> shadowStates = new ConcurrentHashMap<>();
    private final Map<String, Instant> shadowStateExpiry = new ConcurrentHashMap<>();

    protected @Nullable AnkerSolixConfiguration config;

    protected AbstractAnkerSolixHandler(Thing thing) {
        super(thing);
    }

    /**
     * Provides the Modbus register ranges to poll for this device profile.
     */
    protected abstract List<PollRange> getPollRanges();

    /**
     * Maps the cached register values to channel states for this device profile.
     */
    protected abstract void applyStateFromCache();

    /**
     * Handles a non-refresh command for a channel of this device profile.
     */
    protected abstract void handleDeviceCommand(String channelId, Command command);

    /**
     * Hook invoked after the thing transitions to {@link ThingStatus#ONLINE} on a
     * successful read. Subclasses may override to perform one-time setup.
     */
    protected void onConnected() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            triggerImmediateRefresh();
            return;
        }
        handleDeviceCommand(channelUID.getIdWithoutGroup(), command);
    }

    @Override
    public void modbusInitialize() {
        AnkerSolixConfiguration localConfig = getConfigAs(AnkerSolixConfiguration.class);
        if (localConfig.pollInterval < 500) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid poll interval (must be >= 500 ms)");
            return;
        }
        if (localConfig.maxTries < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid maxTries (must be >= 1)");
            return;
        }

        config = localConfig;

        updateStatus(ThingStatus.UNKNOWN);
        for (PollRange range : getPollRanges()) {
            ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(getSlaveId(), range.functionCode,
                    range.startAddress, range.length, localConfig.maxTries);
            registerRegularPoll(request, localConfig.pollInterval, 0, result -> handleReadSuccess(range, result),
                    this::handleReadFailure);
        }
    }

    private void triggerImmediateRefresh() {
        AnkerSolixConfiguration localConfig = config;
        if (localConfig == null) {
            return;
        }
        for (PollRange range : getPollRanges()) {
            ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(getSlaveId(), range.functionCode,
                    range.startAddress, range.length, localConfig.maxTries);
            submitOneTimePoll(request, result -> handleReadSuccess(range, result), this::handleReadFailure);
        }
    }

    private void handleReadSuccess(PollRange range, AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            byte[] bytes = registers.getBytes();
            for (int index = 0; index < registers.size(); index++) {
                int value = ModbusBitUtilities.extractUInt16(bytes, index * 2);
                registerCache.put(range.startAddress + index, value);
            }

            cleanupExpiredShadows();
            applyStateFromCache();
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            onConnected();
        });
    }

    private void handleReadFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        String message = String.valueOf(failure.getCause().getMessage());
        logger.debug("Failed to read Anker Solix registers: {}", message);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    protected void writeInt16Holding(int registerAddress, int value) {
        AnkerSolixConfiguration localConfig = config;
        if (localConfig == null) {
            return;
        }

        byte[] payload = new byte[] { (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF) };
        ModbusRegisterArray registerArray = new ModbusRegisterArray(payload);
        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(getSlaveId(),
                registerAddress, registerArray, false, localConfig.maxTries);

        submitOneTimeWrite(request, result -> updateStatus(ThingStatus.ONLINE),
                failure -> handleWriteFailure(failure, "writeInt16"));
    }

    protected void writeInt32Holding(int registerAddress, int value) {
        AnkerSolixConfiguration localConfig = config;
        if (localConfig == null) {
            return;
        }

        byte[] payload = new byte[] { (byte) ((value >> 24) & 0xFF), (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF) };
        ModbusRegisterArray registerArray = new ModbusRegisterArray(payload);
        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(getSlaveId(),
                registerAddress, registerArray, true, localConfig.maxTries);

        submitOneTimeWrite(request, result -> updateStatus(ThingStatus.ONLINE),
                failure -> handleWriteFailure(failure, "writeInt32"));
    }

    private void handleWriteFailure(AsyncModbusFailure<?> failure, String operation) {
        String message = String.valueOf(failure.getCause().getMessage());
        logger.warn("{} failed: {}", operation, message);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    protected void setShadowState(String channelId, State state) {
        AnkerSolixConfiguration localConfig = config;
        int durationSeconds = localConfig != null ? Math.max(1, localConfig.writeProtectionDurationSeconds) : 15;
        shadowStates.put(channelId, state);
        shadowStateExpiry.put(channelId, Instant.now().plusSeconds(durationSeconds));
    }

    protected @Nullable State getShadowState(String channelId) {
        Instant expiry = shadowStateExpiry.get(channelId);
        if (expiry == null) {
            return null;
        }
        if (Instant.now().isAfter(expiry)) {
            shadowStateExpiry.remove(channelId);
            shadowStates.remove(channelId);
            return null;
        }
        return shadowStates.get(channelId);
    }

    private void cleanupExpiredShadows() {
        Instant now = Instant.now();
        shadowStateExpiry.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
        shadowStates.keySet().removeIf(channel -> !shadowStateExpiry.containsKey(channel));
    }

    protected @Nullable Integer readUInt16(int registerAddress) {
        return registerCache.get(registerAddress);
    }

    protected @Nullable Integer readInt16(int registerAddress) {
        Integer value = readUInt16(registerAddress);
        if (value == null) {
            return null;
        }
        return value >= 0x8000 ? value - 0x10000 : value;
    }

    protected @Nullable Integer readInt32(int registerAddress) {
        Integer highWord = readUInt16(registerAddress);
        Integer lowWord = readUInt16(registerAddress + 1);
        if (highWord == null || lowWord == null) {
            return null;
        }

        long unsigned = ((long) (highWord & 0xFFFF) << 16) | (lowWord & 0xFFFFL);
        if (unsigned >= 0x80000000L) {
            return (int) (unsigned - 0x1_0000_0000L);
        }
        return (int) unsigned;
    }

    protected @Nullable Long readUInt32(int registerAddress) {
        Integer highWord = readUInt16(registerAddress);
        Integer lowWord = readUInt16(registerAddress + 1);
        if (highWord == null || lowWord == null) {
            return null;
        }
        return ((long) (highWord & 0xFFFF) << 16) | (lowWord & 0xFFFFL);
    }

    protected @Nullable String readVersion(int registerAddress) {
        Integer first = readUInt16(registerAddress);
        Integer second = readUInt16(registerAddress + 1);
        if (first == null || second == null) {
            return null;
        }

        int major = (first >> 8) & 0xFF;
        int minor = first & 0xFF;
        int patch = (second >> 8) & 0xFF;
        int build = second & 0xFF;
        return major + "." + minor + "." + patch + "." + build;
    }

    protected @Nullable BigDecimal readScaledInt16(int registerAddress, int gain) {
        Integer value = readInt16(registerAddress);
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(gain));
    }

    protected @Nullable BigDecimal readScaledUInt16(int registerAddress, int gain) {
        Integer value = readUInt16(registerAddress);
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(gain));
    }

    protected @Nullable String readString(int registerAddress, int registerCount) {
        byte[] payload = new byte[registerCount * 2];
        for (int index = 0; index < registerCount; index++) {
            Integer value = readUInt16(registerAddress + index);
            if (value == null) {
                return null;
            }
            payload[index * 2] = (byte) ((value >> 8) & 0xFF);
            payload[index * 2 + 1] = (byte) (value & 0xFF);
        }

        String decoded = new String(payload, StandardCharsets.UTF_8).replace("\u0000", "").trim();
        return decoded.isEmpty() ? null : decoded;
    }

    protected @Nullable String resolveModelName(@Nullable String rawModel, @Nullable String serialNumber) {
        String mappedModel = AnkerSolixBindingConstants.resolveModelFromSerial(serialNumber);
        if (mappedModel != null) {
            return mappedModel;
        }
        return rawModel;
    }

    protected void updateStringChannel(String channelId, @Nullable String value) {
        if (value != null) {
            updateChannelState(channelId, new StringType(value));
        }
    }

    protected void updatePowerChannel(String channelId, @Nullable Integer valueInWatt) {
        if (valueInWatt != null) {
            updateChannelState(channelId, new QuantityType<>(BigDecimal.valueOf(valueInWatt), Units.WATT));
        }
    }

    protected void updateScaledPowerChannel(String channelId, @Nullable BigDecimal valueInWatt) {
        if (valueInWatt != null) {
            updateChannelState(channelId, new QuantityType<>(valueInWatt, Units.WATT));
        }
    }

    protected void updateCurrentChannel(String channelId, @Nullable BigDecimal valueInAmpere) {
        if (valueInAmpere != null) {
            updateChannelState(channelId, new QuantityType<>(valueInAmpere, Units.AMPERE));
        }
    }

    protected void updateVoltageChannel(String channelId, @Nullable BigDecimal valueInVolt) {
        if (valueInVolt != null) {
            updateChannelState(channelId, new QuantityType<>(valueInVolt, Units.VOLT));
        }
    }

    protected void updateChannelState(String channelId, State state) {
        updateState(new ChannelUID(getThing().getUID(), channelId), state);
    }
}
