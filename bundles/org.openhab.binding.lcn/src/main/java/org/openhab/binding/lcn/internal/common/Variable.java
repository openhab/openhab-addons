/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.common;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;

/**
 * LCN variable types.
 *
 * @author Tobias Jüttner - Initial Contribution
 * @author Fabian Wolter - Migration to OH2
 */
@NonNullByDefault
public enum Variable {
    UNKNOWN(0, Type.UNKNOWN, LcnChannelGroup.VARIABLE), // Used if the real type is not known (yet)
    VARIABLE1(0, Type.VARIABLE, LcnChannelGroup.VARIABLE), // or TVar
    VARIABLE2(1, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE3(2, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE4(3, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE5(4, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE6(5, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE7(6, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE8(7, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE9(8, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE10(9, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE11(10, Type.VARIABLE, LcnChannelGroup.VARIABLE),
    VARIABLE12(11, Type.VARIABLE, LcnChannelGroup.VARIABLE), // Since 170206
    RVARSETPOINT1(0, Type.REGULATOR, LcnChannelGroup.RVARSETPOINT),
    RVARSETPOINT2(1, Type.REGULATOR, LcnChannelGroup.RVARSETPOINT), // Set-points for regulators
    THRESHOLDREGISTER11(0, 0, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER1),
    THRESHOLDREGISTER12(0, 1, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER1),
    THRESHOLDREGISTER13(0, 2, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER1),
    THRESHOLDREGISTER14(0, 3, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER1),
    // Register 1 (THRESHOLDREGISTER15 only before 170206)
    THRESHOLDREGISTER15(0, 4, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER1),
    THRESHOLDREGISTER21(1, 0, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER2),
    THRESHOLDREGISTER22(1, 1, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER2),
    THRESHOLDREGISTER23(1, 2, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER2),
    THRESHOLDREGISTER24(1, 3, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER2), // Register 2 (since 2012)
    THRESHOLDREGISTER31(2, 0, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER3),
    THRESHOLDREGISTER32(2, 1, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER3),
    THRESHOLDREGISTER33(2, 2, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER3),
    THRESHOLDREGISTER34(2, 3, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER3), // Register 3 (since 2012)
    THRESHOLDREGISTER41(3, 0, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER4),
    THRESHOLDREGISTER42(3, 1, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER4),
    THRESHOLDREGISTER43(3, 2, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER4),
    THRESHOLDREGISTER44(3, 3, Type.THRESHOLD, LcnChannelGroup.THRESHOLDREGISTER4), // Register 4 (since 2012)
    S0INPUT1(0, Type.S0INPUT, LcnChannelGroup.S0INPUT),
    S0INPUT2(1, Type.S0INPUT, LcnChannelGroup.S0INPUT),
    S0INPUT3(2, Type.S0INPUT, LcnChannelGroup.S0INPUT),
    S0INPUT4(3, Type.S0INPUT, LcnChannelGroup.S0INPUT); // LCN-BU4L

    private final int number;
    private final Optional<Integer> thresholdNumber;
    private final Type type;
    private final LcnChannelGroup channelGroup;

    /**
     * Defines the origin of an LCN variable.
     */
    public enum Type {
        UNKNOWN,
        VARIABLE,
        REGULATOR,
        THRESHOLD,
        S0INPUT
    }

    Variable(int number, Type type, LcnChannelGroup channelGroup) {
        this(number, Optional.empty(), type, channelGroup);
    }

    Variable(int number, int thresholdNumber, Type type, LcnChannelGroup channelGroup) {
        this(number, Optional.of(thresholdNumber), type, channelGroup);
    }

    Variable(int number, Optional<Integer> thresholdNumber, Type type, LcnChannelGroup channelGroup) {
        this.number = number;
        this.type = type;
        this.channelGroup = channelGroup;
        this.thresholdNumber = thresholdNumber;
    }

    /**
     * Gets the type of the variable's origin.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the channel type of the variable.
     *
     * @return the channel type
     */
    public LcnChannelGroup getChannelType() {
        return channelGroup;
    }

    /**
     * Gets the threshold number within a threshold register.
     *
     * @return the threshold number
     */
    public Optional<Integer> getThresholdNumber() {
        return thresholdNumber;
    }

    /**
     * Gets the threshold register number.
     *
     * @return the threshold register number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Translates a given id into a variable type.
     *
     * @param number 0..11
     * @return the translated {@link Variable}
     * @throws LcnException if out of range
     */
    public static Variable varIdToVar(int number) throws LcnException {
        if (number < 0 || number >= LcnChannelGroup.VARIABLE.getCount()) {
            throw new LcnException("Invalid variable number: " + (number + 1));
        }
        return getVariableFromNumberAndType(number, Type.VARIABLE, v -> true);
    }

    /**
     * Translates a given id into a LCN set-point variable type.
     *
     * @param number 0..1
     * @return the translated {@link Variable}
     * @throws LcnException if out of range
     */
    public static Variable setPointIdToVar(int number) throws LcnException {
        if (number < 0 || number >= LcnChannelGroup.RVARSETPOINT.getCount()) {
            throw new LcnException();
        }

        return getVariableFromNumberAndType(number, Type.REGULATOR, v -> true);
    }

    /**
     * Translates given ids into a LCN threshold variable type.
     *
     * @param registerNumber 0..3
     * @param thresholdNumber 0..4 for register 0, 0..3 for registers 1..3
     * @return the translated {@link Variable}
     * @throws LcnException if out of range
     */
    public static Variable thrsIdToVar(int registerNumber, int thresholdNumber) throws LcnException {
        if (registerNumber < 0 || registerNumber >= LcnDefs.THRESHOLD_REGISTER_COUNT) {
            throw new LcnException("Threshold register number out of range: " + (registerNumber + 1));
        }
        if (thresholdNumber < 0 || thresholdNumber >= (registerNumber == 0 ? 5 : 4)) {
            throw new LcnException("Threshold number out of range: " + (thresholdNumber + 1));
        }
        return getVariableFromNumberAndType(registerNumber, Type.THRESHOLD,
                v -> v.thresholdNumber.get() == thresholdNumber);
    }

    /**
     * Translates a given id into a LCN S0-input variable type.
     *
     * @param number 0..3
     * @return the translated {@link Variable}
     * @throws LcnException if out of range
     */
    public static Variable s0IdToVar(int number) throws LcnException {
        if (number < 0 || number >= LcnChannelGroup.S0INPUT.getCount()) {
            throw new LcnException();
        }
        return getVariableFromNumberAndType(number, Type.S0INPUT, v -> true);
    }

    private static Variable getVariableFromNumberAndType(int varId, Type type, Predicate<Variable> filter)
            throws LcnException {
        return Stream.of(values()).filter(v -> v.type == type).filter(v -> v.number == varId).filter(filter).findAny()
                .orElseThrow(LcnException::new);
    }

    /**
     * Checks if this variable type uses special values.
     * Examples for special values: "No value yet", "sensor defective" etc.
     *
     * @return true if special values are in use
     */
    public boolean useLcnSpecialValues() {
        return type != Type.S0INPUT;
    }

    /**
     * Module-generation check.
     * Checks if the given variable type would receive a typed response if
     * its status was requested.
     *
     * @param firmwareVersion the target LCN-modules firmware version
     * @return true if a response would contain the variable's type
     */
    public boolean hasTypeInResponse(int firmwareVersion) {
        return (firmwareVersion >= LcnBindingConstants.FIRMWARE_2013
                || (type != Type.VARIABLE && type != Type.REGULATOR));
    }

    /**
     * Module-generation check.
     * Checks if the given variable type automatically sends status-updates on
     * value-change. It must be polled otherwise.
     *
     * @param firmwareVersion the target LCN-module's firmware version
     * @return true if the LCN module supports automatic status-messages for this {@link Variable}
     */
    public boolean isEventBased(int firmwareVersion) {
        return type == Type.REGULATOR || type == Type.S0INPUT || firmwareVersion >= LcnBindingConstants.FIRMWARE_2013;
    }

    /**
     * Module-generation check.
     * Checks if the target LCN module would automatically send status-updates if
     * the given variable type was changed by command.
     *
     * @param firmwareVersion
     * @return true if a poll is required to get the new status-value
     */
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean shouldPollStatusAfterCommand(int firmwareVersion) {
        // Regulator set-points will send status-messages on every change (all firmware versions)
        if (type == Type.REGULATOR) {
            return false;
        }
        // Thresholds since 170206 will send status-messages on every change
        if (firmwareVersion >= LcnBindingConstants.FIRMWARE_2013 && type == Type.THRESHOLD) {
            return false;
        }
        // Others:
        // - Variables before 170206 will never send any status-messages
        // - Variables since 170206 only send status-messages on "big" changes
        // - Thresholds before 170206 will never send any status-messages
        // - S0-inputs only send status-messages on "big" changes
        // (all "big changes" cases force us to poll the status to get faster updates)
        return true;
    }

    /**
     * Module-generation check.
     * Checks if the target LCN module would automatically send status-updates if
     * the given regulator's lock-state was changed by command.
     *
     * @param firmwareVersion the target LCN-module's firmware version
     * @param lockState the lock-state sent via command
     * @return true if a poll is required to get the new status-value
     */
    public boolean shouldPollStatusAfterRegulatorLock(int firmwareVersion, boolean lockState) {
        // LCN modules before 170206 will send an automatic status-message for "lock", but not for "unlock"
        return !lockState && firmwareVersion < LcnBindingConstants.FIRMWARE_2013;
    }
}
