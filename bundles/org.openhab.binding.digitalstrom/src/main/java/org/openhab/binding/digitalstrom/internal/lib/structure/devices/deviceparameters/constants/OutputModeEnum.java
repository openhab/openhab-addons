/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link OutputModeEnum} lists all available digitalSTROM-device output modes.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @see <a href="http://developer.digitalstrom.org/Architecture/ds-basics.pdf">Table 36: Output Mode Register, page
 *      51</a>
 */
public enum OutputModeEnum {
    /*
     * | Output Mode | Description |
     * ------------------------------------------------------------------------------
     * | 0 | No output or output disabled |
     * | 16 | Switched |
     * | 17 | RMS (root mean square) dimmer |
     * | 18 | RMS dimmer with characteristic curve |
     * | 19 | Phase control dimmer |
     * | 20 | Phase control dimmer with characteristic curve |
     * | 21 | Reverse phase control dimmer |
     * | 22 | Reverse phase control dimmer with characteristic curve |
     * | 23 | PWM (pulse width modulation) |
     * | 24 | PWM with characteristic curve |
     * | 30 | PWM to control heating control valve | (from dS web configurator, it dosn't stand in the ds-basic.pdf from
     * 19.08.2015)
     * | 33 | Positioning control |
     * | 34 | combined 2 stage switch [Both relais switch combined in tow steps depending on the output value. Output >
     * 33% - relais 1 is on. Output > 66% - relais 1 and 2 are on.] | (from ds web configurator, it dosn't stand in the
     * ds-basic.pdf from 19.08.2015)
     * | 35 | single switch | (from ds web configurator, it dosn't stand in the ds-basic.pdf from 19.08.2015)
     * | 38 | combined 3 stage switch [Both relais switch combined in tow steps depending on the output value. Output >
     * 25% - relais 1 is on. Output > 50% - relais 1 is off and relais 2 is on. Output > 75% - relais 1 and 2 are on.] |
     * (from ds web configurator, it dosn't stand in the
     * ds-basic.pdf from 19.08.2015)
     * | 39 | Relay with switched mode scene table configuration |
     * | 40 | Relay with wiped mode scene table configuration |
     * | 41 | Relay with saving mode scene table configuration |
     * | 42 | Positioning control for uncalibrated shutter |
     * | 43 | combined switch | (from dS web configurator, it dosn't stand in the ds-basic.pdf from 19.08.2015)
     * | 49 | dimmed 0-10V [dimming with 0-10V control power] | (from dS web configurator, it dosn't stand in the
     * ds-basic.pdf from 02.06.2015)
     * | 51 | dimmed 1-10V [dimming with 1-10V control power] | (from dS web configurator, it dosn't stand in the
     * ds-basic.pdf from 02.06.2015)
     * | 64 | temperature controlled switch for heating though the dSS | (from dS web configurator, it dosn't stand in
     * the ds-basic.pdf from 19.08.2015)
     * | 65 | temperature controlled pwm for heating though the dSS | (from dS web configurator, it dosn't stand in the
     * ds-basic.pdf from 19.08.2015)
     *
     */
    DISABLED(0),
    SWITCHED(16),
    RMS_DIMMER(17),
    RMS_DIMMER_CC(18),
    PC_DIMMER(19),
    PC_DIMMER_CC(20),
    RPC_DIMMER(21),
    RPC_DIMMER_CC(22),
    PWM(23),
    PWM_CC(24),
    HEATING_PWM(30),
    POSITION_CON(33),
    COMBINED_2_STAGE_SWITCH(34),
    SINGLE_SWITCH(35),
    COMBINED_3_STAGE_SWITCH(38),
    SWITCH(39),
    WIPE(40),
    POWERSAVE(41),
    POSITION_CON_US(42),
    COMBINED_SWITCH(43),
    DIMMED_0_10V(49),
    DIMMED_1_10V(51),
    TEMPRETURE_SWITCHED(54),
    TEMPRETURE_PWM(54);

    private final int mode;

    static final Map<Integer, OutputModeEnum> OUTPUT_MODES = new HashMap<>();

    static {
        for (OutputModeEnum out : OutputModeEnum.values()) {
            OUTPUT_MODES.put(out.getMode(), out);
        }
    }

    /**
     * Returns true, if the output mode id contains in digitalSTROM, otherwise false.
     *
     * @param modeID to be checked
     * @return true, if contains, otherwise false
     */
    public static boolean containsMode(Integer modeID) {
        return OUTPUT_MODES.keySet().contains(modeID);
    }

    /**
     * Returns true, if the output mode is a dimmable output mode, otherwise false.
     *
     * @param outputMode to check
     * @return true, if outputMode is dimmable, otherwise false
     */
    public static boolean outputModeIsDimmable(OutputModeEnum outputMode) {
        if (outputMode == null) {
            return false;
        }
        switch (outputMode) {
            case RMS_DIMMER:
            case RMS_DIMMER_CC:
            case PC_DIMMER:
            case PC_DIMMER_CC:
            case RPC_DIMMER:
            case RPC_DIMMER_CC:
            case DIMMED_0_10V:
            case DIMMED_1_10V:
            case HEATING_PWM:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true, if the output mode is a switchable output mode, otherwise false.
     *
     * @param outputMode to check
     * @return true, if outputMode is switchable, otherwise false
     */
    public static boolean outputModeIsSwitch(OutputModeEnum outputMode) {
        if (outputMode == null) {
            return false;
        }
        switch (outputMode) {
            case SWITCHED:
            case SWITCH:
            case COMBINED_SWITCH:
            case SINGLE_SWITCH:
            case WIPE:
            case POWERSAVE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true, if the output mode is a shade control output mode, otherwise false.
     *
     * @param outputMode to check
     * @return true, if outputMode is for shade control, otherwise false
     */
    public static boolean outputModeIsShade(OutputModeEnum outputMode) {
        if (outputMode == null) {
            return false;
        }
        switch (outputMode) {
            case POSITION_CON:
            case POSITION_CON_US:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true, if the output mode is a temperature controlled output mode, otherwise false.<br>
     * <br>
     * <b>Note:</b>
     * This output mode will be automatically controlled through the digitalSTROM-Server and can't be set manually.
     *
     * @param outputMode to check
     * @return true, if outputMode is temperature controlled, otherwise false
     */
    public static boolean outputModeIsTemperationControlled(OutputModeEnum outputMode) {
        if (outputMode == null) {
            return false;
        }
        switch (outputMode) {
            case TEMPRETURE_PWM:
            case TEMPRETURE_SWITCHED:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the {@link OutputModeEnum} for the given modeID, otherwise null.
     *
     * @param modeID of the {@link OutputModeEnum}
     * @return OutputModeEnum or null
     */
    public static OutputModeEnum getMode(Integer modeID) {
        return OUTPUT_MODES.get(modeID);
    }

    private OutputModeEnum(int outputMode) {
        this.mode = outputMode;
    }

    /**
     * Returns the id of this {@link OutputModeEnum} object.
     *
     * @return mode id
     */
    public int getMode() {
        return mode;
    }
}
