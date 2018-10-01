/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model.type;

/**
* The {@link ActionType} is the enumeration of supported Domintell actions
*
* @author Gabor Bicskei - Initial contribution
*/
public enum ActionType {
    /**
     * '%Dxxx' decimal dimmer/volume value assignment
     * '%DB' and '%DE' : execute a Start/Stop dim on a dimmer output
     * '%I%Dxxx' and '%O%Dxxx' Increase and Decrease dimmer/volume value by step of decimal 'xxx' percent
     * '%Txx.x' decimal T° value (set Heating setpoint)
     * '%Uxx.x' decimal T° value (set Cooling setpoint)
     * '%Ax' Sound Auxiliary selection 1=>4, Tuner = 5
     * '%Fxxx,xxxx' decimal Tuner Frequency in Mhz
     * '%I' set the output
     * '%O' reset the output
     * '%Mx' set Temperature mode (1=absence, 2=auto, 5=confort, 6=gel)
     * '%Rx' set Regulation mode (0=off, 1=heating, 2=cooling, 3=mixed)
     * '%H' shutter goes High
     * '%L' shutter goes Low
     * '%S' ask status of module (does not work with MEMO)
     * '%Px' simulate a push on an input (1=Begin short push, 2=End short push, 3=Begin long push, 4=End long push)
     */
    SET_DOMMER_OR_VOLUME(true, false, false, false, "%D"),
    BEGIN_DIM(false, false, false, false, "%DB"),
    END_DIM(false, false, false, false, "%DE"),
    INCREASE_BY(true, false, false, false, "%I%D"),
    DECREASE_BY(true, false, false, false, "%O%D"),
    HEATING(false, true, false, false, "%T"),
    COOLING(false, true, false, false, "%U"),
    SOUND_AUXILIARY(false, false, false, true, "%A"),
    TUNER_FREQUENCY(false, false, true, false, "%F"),
    SET_OUTPUT(false, false, false, false, "%I"),
    RESET_OUTPUT(false, false, false, false, "%O"),
    TEMPERATURE_MODE(false, false, false, true, "%M"),
    REGULATION_MODE(false, false, false, true, "%R"),
    SHORT_PUSH(false, false, false, true, "%P1", "%P2"),
    LONG_PUSH(false, false, false, true, "%P3", "%P4"),
    SHUTTER_HIGH(false, false, false, false, "%H"),
    SHUTTER_LOW(false, false, false, false, "%L"),
    STATUS(false, false, false, false, "%S");

    /**
     * The action needs a decimal value to execute
     */
    private boolean decimalValueNeeded;

    /**
     * The action needs a temperature value to execute
     */
    private boolean temperatureValueNeeded;

    /**
     * The action needs a frequency value to execute
     */
    private boolean frequencyValueNeeded;

    /**
     * The action needs a selection value to execute
     */
    private boolean selectionValueNeeded;

    /**
     * Sequence of actions to execute
     */
    private String[] actionString;

    ActionType(boolean decimalValueNeeded, boolean temperatureValueNeeded, boolean frequencyValueNeeded, boolean selectionValueNeeded) {
        this(decimalValueNeeded, temperatureValueNeeded, frequencyValueNeeded, selectionValueNeeded, null);
    }

    ActionType(boolean decimalValueNeeded, boolean temperatureValueNeeded, boolean frequencyValueNeeded, boolean selectionValueNeeded, String... actionString) {
        this.decimalValueNeeded = decimalValueNeeded;
        this.temperatureValueNeeded = temperatureValueNeeded;
        this.frequencyValueNeeded = frequencyValueNeeded;
        this.selectionValueNeeded = selectionValueNeeded;
        this.actionString = actionString;
    }

    public boolean isDecimalValueNeeded() {
        return decimalValueNeeded;
    }

    public boolean isTemperatureValueNeeded() {
        return temperatureValueNeeded;
    }

    public boolean isFrequencyValueNeeded() {
        return frequencyValueNeeded;
    }

    public boolean isSelectionValueNeeded() {
        return selectionValueNeeded;
    }

    public String[] getActionString() {
        return actionString;
    }
}
