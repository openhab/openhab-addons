/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.internal.profiles;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile for working with single bit from a 16-bit register.
 *
 * The profile accepts also commands from item. Using its internal cache of
 * the register value, an updated register value is constructed and passed to the binding.
 *
 * Designed to be used with modbus data things, having value type int16.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusBitProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(ModbusBitProfile.class);
    private static final String BIT_INDEX_PARAM = "bit-index";
    private static final String INVERTED_PARAM = "inverted";

    private final ProfileCallback callback;
    private final ProfileContext context;

    private OptionalInt bitIndex;
    private Optional<byte[]> lastState = Optional.empty();
    private boolean inverted;

    public ModbusBitProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.context = context;

        {
            Object paramValue = this.context.getConfiguration().get(BIT_INDEX_PARAM);
            logger.debug("Configuring profile with {} parameter '{}'", BIT_INDEX_PARAM, paramValue);
            bitIndex = OptionalInt.empty();
            if (paramValue instanceof String) {
                try {
                    bitIndex = OptionalInt.of(Integer.parseInt((String) paramValue));
                } catch (IllegalArgumentException e) {
                    logger.error("Cannot convert value '{}' of parameter '{}' into a integer.", paramValue,
                            BIT_INDEX_PARAM);
                }
            } else if (paramValue instanceof BigDecimal) {
                BigDecimal bd = (BigDecimal) paramValue;
                bitIndex = OptionalInt.of(bd.toBigInteger().intValue());
            } else {
                logger.error("Parameter '{}' is not of type String or BigDecimal", BIT_INDEX_PARAM);
            }
            bitIndex.ifPresent(i -> {
                if (i < 0) {
                    logger.error("Parameter '{}' is negative", BIT_INDEX_PARAM);
                }
            });
        }

        {
            Object paramValue = this.context.getConfiguration().get(INVERTED_PARAM);
            if (paramValue instanceof Boolean) {
                inverted = (boolean) paramValue;
            } else if (paramValue == null || paramValue instanceof String) {
                inverted = Boolean.parseBoolean((String) paramValue);
            } else {
                logger.warn("Unexpected parameter type for '{}': {} {}. Assuming inverted=false", INVERTED_PARAM,
                        paramValue.getClass().getClass().getSimpleName(), paramValue);
                inverted = false;
            }
            logger.debug("Configuring profile with {} parameter '{}' (=> inverted={})", INVERTED_PARAM, paramValue,
                    inverted);
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return ModbusProfiles.BIT;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // We do not process state updates from item
        //
        // Updating the internal state of this instance would not update
        // other channels using ModbusBitProfile referring to same register, having their own copy of the state,
        // resulting
        // in inconsistent states
    }

    @Override
    public void onCommandFromItem(Command command) {
        logger.trace("Command '{}' from item...", command);
        combineCommandWithState(command);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        final Optional<byte[]> localState = updateLastState(command);
        Command result = (Command) extractBit(localState);
        logger.trace(
                "Command '{}' update from handler, updating internal state to {}. Result {} after extracting bit {} with inverted={}.",
                command, localState.map(b -> HexUtils.bytesToHex(b)).orElse("<null>"), result, bitIndex.orElse(-1),
                inverted);
        callback.sendCommand(result);
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        final Optional<byte[]> localState = updateLastState(state);
        State result = (State) extractBit(localState);
        logger.trace(
                "State '{}' update from handler, updating internal state to {}. Result {} after extracting bit {} with inverted={}.",
                state, localState.map(b -> HexUtils.bytesToHex(b)).orElse("<null>"), result, bitIndex.orElse(-1),
                inverted);
        callback.sendUpdate(result);
    }

    /**
     * Combine boolean-like command with internal state. Combined state is passed to the binding via
     * callback.handleCommand.
     *
     * @param command boolean-like command
     */
    private void combineCommandWithState(Command command) {
        lastState.ifPresentOrElse(bytes -> {
            OptionalInt localBitIndexOptional = bitIndex;
            if (localBitIndexOptional.isEmpty()) {
                logger.warn(
                        "BitIndex not configured correctly, please make sure it is of type integer, e.g. \"3\". Using bitIndex 0 now.");
            }
            int bitIndex = localBitIndexOptional.orElse(0);

            Optional<Boolean> commandBool = ModbusBitUtilities.translateCommand2Boolean(command)
                    .map(b -> inverted ? (b ? false : true) : b);
            commandBool.ifPresentOrElse(b -> {
                BigInteger val = new BigInteger(bytes);
                val = b ? val.setBit(bitIndex) : val.clearBit(bitIndex);
                DecimalType combinedCommand = new DecimalType(new BigDecimal(val));
                Optional<byte[]> newState = updateLastState(combinedCommand);
                logger.trace(
                        "Update '{}' from item, combining command with internal state ({}) with bitIndex={}, inverted={}, resulting state {} and combined {}",
                        command, HexUtils.bytesToHex(bytes), bitIndex, inverted,
                        newState.map(stateBytes -> HexUtils.bytesToHex(stateBytes)).orElse("<null>"), combinedCommand);
                updateLastState(combinedCommand);
                callback.handleCommand(combinedCommand);
            }, () -> {
                logger.warn("Profile received command that is not convertible to 0/1 bit. Ignoring.");
            });
        }, () -> {
            logger.warn("Profile received bit-command but full state is unknown. Ignoring.");
        });
    }

    private Optional<byte[]> updateLastState(Type state) {
        Optional<byte[]> localState;
        try {
            if (state instanceof DecimalType) {
                DecimalType decimal = (DecimalType) state;
                short low16bits = decimal.toBigDecimal().toBigIntegerExact().shortValue();
                byte[] low2Bytes = new byte[] { (byte) ((low16bits & 0xFF00) >> 8), (byte) (low16bits & 0xFF) };

                localState = updateLastState(low2Bytes);
            } else {
                logger.warn(
                        "Profile received non-DecimalType state update ('{}' of type {}) from handler. Not updating state.",
                        state, state.getClass().getSimpleName());
                localState = Optional.empty();
            }
        } catch (ArithmeticException e) {
            // Non-integer
            logger.warn(
                    "Profile received non-integer DecimalType state update from handler. Ignoring. Use this profile only with 16-bit integer value types.");
            localState = Optional.empty();
        }
        this.lastState = localState;
        return localState;
    }

    private Optional<byte[]> updateLastState(byte[] bytes) {
        Optional<byte[]> localState = this.lastState = Optional.of(bytes);
        return localState;
    }

    private Type extractBit(Optional<byte[]> localState) {
        if (localState.isEmpty()) {
            return UnDefType.UNDEF;
        }

        OptionalInt localBitIndexOptional = bitIndex;
        if (localBitIndexOptional.isEmpty()) {
            logger.warn(
                    "BitIndex not configured correctly, please make sure it is of type integer, e.g. \"3\". Using bitIndex 0 now.");
        }
        int bitIndex = localBitIndexOptional.orElse(0);
        int extractedBit = ModbusBitUtilities.extractBit(localState.get(), bitIndex);
        return new DecimalType(inverted ? (extractedBit != 0 ? 0 : 1) : extractedBit);
    }

    public Optional<byte[]> getLastState() {
        return lastState.map(bytes -> Arrays.copyOf(bytes, bytes.length));
    }
}
