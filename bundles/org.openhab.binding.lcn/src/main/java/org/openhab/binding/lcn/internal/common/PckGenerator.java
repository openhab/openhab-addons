/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpers to generate LCN-PCK commands.
 * <p>
 * LCN-PCK is the command-syntax used by LCN-PCHK to send and receive LCN commands.
 *
 * @author Tobias Jüttner - Initial Contribution
 * @author Fabian Wolter - Migration to OH2
 */
@NonNullByDefault
public final class PckGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PckGenerator.class);
    /** Termination character after a PCK message */
    public static final String TERMINATION = "\n";

    /**
     * Generates a keep-alive.
     * LCN-PCHK will close the connection if it does not receive any commands from
     * an open {@link Connection} for a specific period (10 minutes by default).
     *
     * @param counter the current ping's id (optional, but "best practice"). Should start with 1
     * @return the PCK command as text
     */
    public static String ping(int counter) {
        return String.format("^ping%d", counter);
    }

    /**
     * Generates a PCK command that will set the LCN-PCHK connection's operation mode.
     * This influences how output-port commands and status are interpreted and must be
     * in sync with the LCN bus.
     *
     * @param dimMode see {@link LcnDefs.OutputPortDimMode}
     * @param statusMode see {@link LcnDefs.OutputPortStatusMode}
     * @return the PCK command as text
     */
    public static String setOperationMode(LcnDefs.OutputPortDimMode dimMode, LcnDefs.OutputPortStatusMode statusMode) {
        return "!OM" + (dimMode == LcnDefs.OutputPortDimMode.NATIVE200 ? "1" : "0")
                + (statusMode == LcnDefs.OutputPortStatusMode.PERCENT ? "P" : "N");
    }

    /**
     * Generates a PCK address header.
     * Used for commands to LCN modules and groups.
     *
     * @param addr the target's address (module or group)
     * @param localSegId the local segment id where the physical bus connection is located
     * @param wantsAck true to claim an acknowledge / receipt from the target
     * @return the PCK address header as text
     */
    public static String generateAddressHeader(LcnAddr addr, int localSegId, boolean wantsAck) {
        return String.format(">%s%03d%03d%s", addr.isGroup() ? "G" : "M", addr.getPhysicalSegmentId(localSegId),
                addr.getId(), wantsAck ? "!" : ".");
    }

    /**
     * Generates a scan-command for LCN segment-couplers.
     * Used to detect the local segment (where the physical bus connection is located).
     *
     * @return the PCK command (without address header) as text
     */
    public static String segmentCouplerScan() {
        return "SK";
    }

    /**
     * Generates a firmware/serial-number request.
     *
     * @return the PCK command (without address header) as text
     */
    public static String requestSn() {
        return "SN";
    }

    /**
     * Generates a command to request a part of a name of a module.
     *
     * @param partNumber 0..1
     * @return the PCK command (without address header) as text
     */
    public static String requestModuleName(int partNumber) {
        return "NMN" + (partNumber + 1);
    }

    /**
     * Generates an output-port status request.
     *
     * @param outputId 0..3
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String requestOutputStatus(int outputId) throws LcnException {
        if (outputId < 0 || outputId > 3) {
            throw new LcnException();
        }
        return String.format("SMA%d", outputId + 1);
    }

    /**
     * Generates a dim command for a single output-port.
     *
     * @param outputId 0..3
     * @param percent 0..100
     * @param rampMs ramp in milliseconds
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String dimOutput(int outputId, double percent, int rampMs) throws LcnException {
        if (outputId < 0 || outputId > 3) {
            throw new LcnException();
        }
        int rampNative = PckGenerator.timeToRampValue(rampMs);
        int n = (int) Math.round(percent * 2);
        if ((n % 2) == 0) { // Use the percent command (supported by all LCN-PCHK versions)
            return String.format("A%dDI%03d%03d", outputId + 1, n / 2, rampNative);
        } else { // We have a ".5" value. Use the native command (supported since LCN-PCHK 2.3)
            return String.format("O%dDI%03d%03d", outputId + 1, n, rampNative);
        }
    }

    /**
     * Generates a dim command for all output-ports.
     *
     * Attention: This command is supported since module firmware version 180501 AND LCN-PCHK 2.61
     *
     * @param firstPercent dimmer value of the first output 0..100
     * @param secondPercent dimmer value of the first output 0..100
     * @param thirdPercent dimmer value of the first output 0..100
     * @param fourthPercent dimmer value of the first output 0..100
     * @param rampMs ramp in milliseconds
     * @return the PCK command (without address header) as text
     */
    public static String dimAllOutputs(double firstPercent, double secondPercent, double thirdPercent,
            double fourthPercent, int rampMs) {
        long n1 = Math.round(firstPercent * 2);
        long n2 = Math.round(secondPercent * 2);
        long n3 = Math.round(thirdPercent * 2);
        long n4 = Math.round(fourthPercent * 2);

        return String.format("OY%03d%03d%03d%03d%03d", n1, n2, n3, n4, timeToRampValue(rampMs));
    }

    /**
     * Generates a control command for switching all outputs ON or OFF with a fixed ramp of 0.5s.
     *
     * @param percent 0..100
     * @returnthe PCK command (without address header) as text
     */
    public static String controlAllOutputs(double percent) {
        return String.format("AH%03d", Math.round(percent));
    }

    /**
     * Generates a control command for switching dimmer output 1 and 2 both ON or OFF with a fixed ramp of 0.5s or
     * without ramp.
     *
     * @param on true, if outputs shall be switched on
     * @param ramp true, if the ramp shall be 0.5s, else 0s
     * @return the PCK command (without address header) as text
     */
    public static String controlOutputs12(boolean on, boolean ramp) {
        int commandByte;
        if (on) {
            commandByte = ramp ? 0xC8 : 0xFD;
        } else {
            commandByte = ramp ? 0x00 : 0xFC;
        }
        return String.format("X2%03d%03d%03d", 1, commandByte, commandByte);
    }

    /**
     * Generates a dim command for setting the brightness of dimmer output 1 and 2 with a fixed ramp of 0.5s.
     *
     * @param percent brightness of both outputs 0..100
     * @return the PCK command (without address header) as text
     */
    public static String dimOutputs12(double percent) {
        long localPercent = Math.round(percent);
        return String.format("AY%03d%03d", localPercent, localPercent);
    }

    /**
     * Let an output flicker.
     *
     * @param outputId output id 0..3
     * @param depth flicker depth, the higher the deeper 0..2
     * @param ramp the flicker speed 0..2
     * @param count number of flashes 1..15
     * @return the PCK command (without address header) as text
     * @throws LcnException when the input values are out of range
     */
    public static String flickerOutput(int outputId, int depth, int ramp, int count) throws LcnException {
        if (outputId < 0 || outputId > 3) {
            throw new LcnException("Output number out of range");
        }
        if (count < 1 || count > 15) {
            throw new LcnException("Number of flashes out of range");
        }
        String depthString;
        switch (depth) {
            case 0:
                depthString = "G";
                break;
            case 1:
                depthString = "M";
                break;
            case 2:
                depthString = "S";
                break;
            default:
                throw new LcnException("Depth out of range");
        }
        String rampString;
        switch (ramp) {
            case 0:
                rampString = "L";
                break;
            case 1:
                rampString = "M";
                break;
            case 2:
                rampString = "S";
                break;
            default:
                throw new LcnException("Ramp out of range");
        }
        return String.format("A%dFL%s%s%02d", outputId + 1, depthString, rampString, count);
    }

    /**
     * Generates a command to change the value of an output-port.
     *
     * @param outputId 0..3
     * @param percent -100..100
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String relOutput(int outputId, double percent) throws LcnException {
        if (outputId < 0 || outputId > 3) {
            throw new LcnException();
        }
        int n = (int) Math.round(percent * 2);
        if ((n % 2) == 0) { // Use the percent command (supported by all LCN-PCHK versions)
            return String.format("A%d%s%03d", outputId + 1, percent >= 0 ? "AD" : "SB", Math.abs(n / 2));
        } else { // We have a ".5" value. Use the native command (supported since LCN-PCHK 2.3)
            return String.format("O%d%s%03d", outputId + 1, percent >= 0 ? "AD" : "SB", Math.abs(n));
        }
    }

    /**
     * Generates a command that toggles a single output-port (on->off, off->on).
     *
     * @param outputId 0..3
     * @param ramp see {@link PckGenerator#timeToRampValue(int)}
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String toggleOutput(int outputId, int ramp) throws LcnException {
        if (outputId < 0 || outputId > 3) {
            throw new LcnException();
        }
        return String.format("A%dTA%03d", outputId + 1, ramp);
    }

    /**
     * Generates a command that toggles all output-ports (on->off, off->on).
     *
     * @param ramp see {@link PckGenerator#timeToRampValue(int)}
     * @return the PCK command (without address header) as text
     */
    public static String toggleAllOutputs(int ramp) {
        return String.format("AU%03d", ramp);
    }

    /**
     * Generates a relays-status request.
     *
     * @return the PCK command (without address header) as text
     */
    public static String requestRelaysStatus() {
        return "SMR";
    }

    /**
     * Generates a command to control relays.
     *
     * @param states the 8 modifiers for the relay states
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String controlRelays(LcnDefs.RelayStateModifier[] states) throws LcnException {
        if (states.length != 8) {
            throw new LcnException();
        }
        StringBuilder ret = new StringBuilder("R8");
        for (int i = 0; i < 8; ++i) {
            switch (states[i]) {
                case ON:
                    ret.append("1");
                    break;
                case OFF:
                    ret.append("0");
                    break;
                case TOGGLE:
                    ret.append("U");
                    break;
                case NOCHANGE:
                    ret.append("-");
                    break;
                default:
                    throw new LcnException();
            }
        }
        return ret.toString();
    }

    /**
     * Generates a binary-sensors status request.
     *
     * @return the PCK command (without address header) as text
     */
    public static String requestBinSensorsStatus() {
        return "SMB";
    }

    /**
     * Generates a command that sets a variable absolute.
     *
     * @param number regulator number 0..1
     * @param value the absolute value to set
     * @return the PCK command (without address header) as text
     * @throws LcnException
     */
    public static String setSetpointAbsolute(int number, int value) {
        int internalValue = value;
        // Set absolute (not in PCK yet)
        int b1 = number << 6; // 01000000
        b1 |= 0x20; // xx10xxxx (set absolute)
        if (value < 1000) {
            internalValue = 1000 - internalValue;
            b1 |= 8;
        } else {
            internalValue -= 1000;
        }
        b1 |= (internalValue >> 8) & 0x0f; // xxxx1111
        int b2 = internalValue & 0xff;
        return String.format("X2%03d%03d%03d", 30, b1, b2);
    }

    /**
     * Generates a command to change the value of a variable.
     *
     * @param variable the target variable to change
     * @param type the reference-point
     * @param value the native LCN value to add/subtract (can be negative)
     * @return the PCK command (without address header) as text
     * @throws LcnException if command is not supported
     */
    public static String setVariableRelative(Variable variable, LcnDefs.RelVarRef type, int value) {
        if (variable.getNumber() == 0) {
            // Old command for variable 1 / T-var (compatible with all modules)
            return String.format("Z%s%d", value >= 0 ? "A" : "S", Math.abs(value));
        } else { // New command for variable 1-12 (compatible with all modules, since LCN-PCHK 2.8)
            return String.format("Z%s%03d%d", value >= 0 ? "+" : "-", variable.getNumber() + 1, Math.abs(value));
        }
    }

    /**
     * Generates a command the change the value of a regulator setpoint relative.
     *
     * @param number 0..1
     * @param type relative to the current or to the programmed value
     * @param value the relative value -4000..+4000
     * @return the PCK command (without address header) as text
     */
    public static String setSetpointRelative(int number, LcnDefs.RelVarRef type, int value) {
        return String.format("RE%sS%s%s%d", number == 0 ? "A" : "B", type == LcnDefs.RelVarRef.CURRENT ? "A" : "P",
                value >= 0 ? "+" : "-", Math.abs(value));
    }

    /**
     * Generates a command the change the value of a threshold relative.
     *
     * @param variable the threshold to change
     * @param type relative to the current or to the programmed value
     * @param value the relative value -4000..+4000
     * @param is2013 true, if the LCN module's firmware is equal to or newer than 2013
     * @return the PCK command (without address header) as text
     */
    public static String setThresholdRelative(Variable variable, LcnDefs.RelVarRef type, int value, boolean is2013)
            throws LcnException {
        if (is2013) { // New command for registers 1-4 (since 170206, LCN-PCHK 2.8)
            return String.format("SS%s%04d%sR%d%d", type == LcnDefs.RelVarRef.CURRENT ? "R" : "E", Math.abs(value),
                    value >= 0 ? "A" : "S", variable.getNumber() + 1, variable.getThresholdNumber().get() + 1);
        } else if (variable.getNumber() == 0) { // Old command for register 1 (before 170206)
            return String.format("SS%s%04d%s%s%s%s%s%s", type == LcnDefs.RelVarRef.CURRENT ? "R" : "E", Math.abs(value),
                    value >= 0 ? "A" : "S", variable.getThresholdNumber().get() == 0 ? "1" : "0",
                    variable.getThresholdNumber().get() == 1 ? "1" : "0",
                    variable.getThresholdNumber().get() == 2 ? "1" : "0",
                    variable.getThresholdNumber().get() == 3 ? "1" : "0",
                    variable.getThresholdNumber().get() == 4 ? "1" : "0");
        } else {
            throw new LcnException(
                    "Module does not have threshold register " + (variable.getThresholdNumber().get() + 1));
        }
    }

    /**
     * Generates a variable value request.
     *
     * @param variable the variable to request
     * @param firmwareVersion the target module's firmware version
     * @return the PCK command (without address header) as text
     * @throws LcnException if command is not supported
     */
    public static String requestVarStatus(Variable variable, int firmwareVersion) throws LcnException {
        if (firmwareVersion >= LcnBindingConstants.FIRMWARE_2013) {
            int id = variable.getNumber();
            switch (variable.getType()) {
                case UNKNOWN:
                    throw new LcnException("Variable unknown");
                case VARIABLE:
                    return String.format("MWT%03d", id + 1);
                case REGULATOR:
                    return String.format("MWS%03d", id + 1);
                case THRESHOLD:
                    return String.format("SE%03d", id + 1); // Whole register
                case S0INPUT:
                    return String.format("MWC%03d", id + 1);
            }
            throw new LcnException("Unsupported variable type: " + variable);
        } else {
            switch (variable) {
                case VARIABLE1:
                    return "MWV";
                case VARIABLE2:
                    return "MWTA";
                case VARIABLE3:
                    return "MWTB";
                case RVARSETPOINT1:
                    return "MWSA";
                case RVARSETPOINT2:
                    return "MWSB";
                case THRESHOLDREGISTER11:
                case THRESHOLDREGISTER12:
                case THRESHOLDREGISTER13:
                case THRESHOLDREGISTER14:
                case THRESHOLDREGISTER15:
                    return "SL1"; // Whole register
                default:
                    throw new LcnException("Unsupported variable type: " + variable);
            }
        }
    }

    /**
     * Generates a request for LED and logic-operations states.
     *
     * @return the PCK command (without address header) as text
     */
    public static String requestLedsAndLogicOpsStatus() {
        return "SMT";
    }

    /**
     * Generates a command to the set the state of a single LED.
     *
     * @param ledId 0..11
     * @param state the state to set
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String controlLed(int ledId, LcnDefs.LedStatus state) throws LcnException {
        if (ledId < 0 || ledId > 11) {
            throw new LcnException();
        }
        return String.format("LA%03d%s", ledId + 1, state == LcnDefs.LedStatus.OFF ? "A"
                : state == LcnDefs.LedStatus.ON ? "E" : state == LcnDefs.LedStatus.BLINK ? "B" : "F");
    }

    /**
     * Generates a command to send LCN keys.
     *
     * @param cmds the 4 concrete commands to send for the tables (A-D)
     * @param keys the tables' 8 key-states (true means "send")
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String sendKeys(LcnDefs.SendKeyCommand[] cmds, boolean[] keys) throws LcnException {
        if (cmds.length != 4 || keys.length != 8) {
            throw new LcnException();
        }
        StringBuilder ret = new StringBuilder("TS");
        for (int i = 0; i < 4; ++i) {
            switch (cmds[i]) {
                case HIT:
                    ret.append("K");
                    break;
                case MAKE:
                    ret.append("L");
                    break;
                case BREAK:
                    ret.append("O");
                    break;
                case DONTSEND:
                    // By skipping table D (if it is not used), we use the old command
                    // for table A-C which is compatible with older LCN modules
                    if (i < 3) {
                        ret.append("-");
                    }
                    break;
                default:
                    throw new LcnException();
            }
        }
        for (int i = 0; i < 8; ++i) {
            ret.append(keys[i] ? "1" : "0");
        }
        return ret.toString();
    }

    /**
     * Generates a command to send LCN keys deferred / delayed.
     *
     * @param tableId 0(A)..3(D)
     * @param time the delay time
     * @param timeUnit the time unit
     * @param keys the key-states (true means "send")
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String sendKeysHitDefered(int tableId, int time, LcnDefs.TimeUnit timeUnit, boolean[] keys)
            throws LcnException {
        if (tableId < 0 || tableId > 3 || keys.length != 8) {
            throw new LcnException();
        }
        StringBuilder ret = new StringBuilder("TV");
        switch (tableId) {
            case 0:
                ret.append("A");
                break;
            case 1:
                ret.append("B");
                break;
            case 2:
                ret.append("C");
                break;
            case 3:
                ret.append("D");
                break;
            default:
                throw new LcnException();
        }
        ret.append(String.format("%03d", time));
        switch (timeUnit) {
            case SECONDS:
                if (time < 1 || time > 60) {
                    throw new LcnException();
                }
                ret.append("S");
                break;
            case MINUTES:
                if (time < 1 || time > 90) {
                    throw new LcnException();
                }
                ret.append("M");
                break;
            case HOURS:
                if (time < 1 || time > 50) {
                    throw new LcnException();
                }
                ret.append("H");
                break;
            case DAYS:
                if (time < 1 || time > 45) {
                    throw new LcnException();
                }
                ret.append("D");
                break;
            default:
                throw new LcnException();
        }
        for (int i = 0; i < 8; ++i) {
            ret.append(keys[i] ? "1" : "0");
        }
        return ret.toString();
    }

    /**
     * Generates a request for key-lock states.
     * Always requests table A-D. Supported since LCN-PCHK 2.8.
     *
     * @return the PCK command (without address header) as text
     */
    public static String requestKeyLocksStatus() {
        return "STX";
    }

    /**
     * Generates a command to lock keys.
     *
     * @param tableId 0(A)..3(D)
     * @param states the 8 key-lock modifiers
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String lockKeys(int tableId, LcnDefs.KeyLockStateModifier[] states) throws LcnException {
        if (tableId < 0 || tableId > 3 || states.length != 8) {
            throw new LcnException();
        }
        StringBuilder ret = new StringBuilder(
                String.format("TX%s", tableId == 0 ? "A" : tableId == 1 ? "B" : tableId == 2 ? "C" : "D"));
        for (int i = 0; i < 8; ++i) {
            switch (states[i]) {
                case ON:
                    ret.append("1");
                    break;
                case OFF:
                    ret.append("0");
                    break;
                case TOGGLE:
                    ret.append("U");
                    break;
                case NOCHANGE:
                    ret.append("-");
                    break;
                default:
                    throw new LcnException();
            }
        }
        return ret.toString();
    }

    /**
     * Generates a command to lock keys for table A temporary.
     * There is no hardware-support for locking tables B-D.
     *
     * @param time the lock time
     * @param timeUnit the time unit
     * @param keys the 8 key-lock states (true means lock)
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String lockKeyTabATemporary(int time, LcnDefs.TimeUnit timeUnit, boolean[] keys) throws LcnException {
        if (keys.length != 8) {
            throw new LcnException();
        }
        StringBuilder ret = new StringBuilder(String.format("TXZA%03d", time));
        switch (timeUnit) {
            case SECONDS:
                if (time < 1 || time > 60) {
                    throw new LcnException();
                }
                ret.append("S");
                break;
            case MINUTES:
                if (time < 1 || time > 90) {
                    throw new LcnException();
                }
                ret.append("M");
                break;
            case HOURS:
                if (time < 1 || time > 50) {
                    throw new LcnException();
                }
                ret.append("H");
                break;
            case DAYS:
                if (time < 1 || time > 45) {
                    throw new LcnException();
                }
                ret.append("D");
                break;
            default:
                throw new LcnException();
        }
        for (int i = 0; i < 8; ++i) {
            ret.append(keys[i] ? "1" : "0");
        }
        return ret.toString();
    }

    /**
     * Generates the command header / start for sending dynamic texts.
     * Used by LCN-GTxD periphery (supports 4 text rows).
     * To complete the command, the text to send must be appended (UTF-8 encoding).
     * Texts are split up into up to 5 parts with 12 "UTF-8 bytes" each.
     *
     * @param row 0..3
     * @param part 0..4
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String dynTextHeader(int row, int part) throws LcnException {
        if (row < 0 || row > 3 || part < 0 || part > 4) {
            throw new LcnException("Row number is out of range: " + (row + 1));
        }
        return String.format("GTDT%d%d", row + 1, part + 1);
    }

    /**
     * Generates a command to lock a regulator.
     *
     * @param regId 0..1
     * @param state the lock state
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String lockRegulator(int regId, boolean state) throws LcnException {
        if (regId < 0 || regId > 1) {
            throw new LcnException();
        }
        return String.format("RE%sX%s", regId == 0 ? "A" : "B", state ? "S" : "A");
    }

    /**
     * Generates a command to start a relay timer
     *
     * @param relayNumber number of relay (1..8)
     * @param duration duration in milliseconds
     * @return the PCK command (without address header) as text
     * @throws LcnException if out of range
     */
    public static String startRelayTimer(int relayNumber, double duration) throws LcnException {
        if (relayNumber < 1 || relayNumber > 8 || duration < 30 || duration > 240960) {
            throw new LcnException();
        }
        StringBuilder command = new StringBuilder("R8T");
        command.append(String.format("%03d", convertMsecToLCNTimer(duration)));
        StringBuilder data = new StringBuilder("--------");
        data.setCharAt(relayNumber - 1, '1');
        command.append(data);
        return command.toString();
    }

    /**
     * Generates a null command, used for broadcast messages.
     *
     * @return the PCK command (without address header) as text
     */
    public static String nullCommand() {
        return "LEER";
    }

    /**
     * Converts the given time into an LCN ramp value.
     *
     * @param timeMSec the time in milliseconds
     * @return the (LCN-internal) ramp value (0..250)
     */
    private static int timeToRampValue(int timeMSec) {
        int ret;
        if (timeMSec < 250) {
            ret = 0;
        } else if (timeMSec < 500) {
            ret = 1;
        } else if (timeMSec < 660) {
            ret = 2;
        } else if (timeMSec < 1000) {
            ret = 3;
        } else if (timeMSec < 1400) {
            ret = 4;
        } else if (timeMSec < 2000) {
            ret = 5;
        } else if (timeMSec < 3000) {
            ret = 6;
        } else if (timeMSec < 4000) {
            ret = 7;
        } else if (timeMSec < 5000) {
            ret = 8;
        } else if (timeMSec < 6000) {
            ret = 9;
        } else {
            ret = (timeMSec / 1000 - 6) / 2 + 10;
            if (ret > 250) {
                ret = 250;
                LOGGER.warn("Ramp value is too high. Limiting value to 486s.");
            }
        }
        return ret;
    }

    /**
     * Converts duration in milliseconds to lcntimer value
     * Source: https://www.symcon.de/forum/threads/38603-LCN-Relais-Kurzzeit-Timer-umrechnen
     *
     * @param ms time in milliseconds
     * @return lcn timer value
     */
    private static int convertMsecToLCNTimer(double ms) {
        Integer lcntimer = -1;
        if (ms >= 0 && ms <= 240960) {
            double a = ms / 1000 / 0.03;
            double b = (a / 32.0) + 1.0;
            double c = Math.log(b) / Math.log(2);
            double mod = Math.floor(c);
            double faktor = 32 * (Math.pow(2, mod) - 1);
            double offset = (a - faktor) / Math.pow(2, mod);
            lcntimer = (int) (offset + mod * 32);
        } else {
            LOGGER.warn("Timer not in [0,240960] ms");
        }
        return lcntimer;
    }
}
