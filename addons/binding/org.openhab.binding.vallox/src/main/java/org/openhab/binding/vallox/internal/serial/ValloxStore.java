/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal.serial;

/**
 * Simple data container class that stores values for
 * all vallox unit properties. This is required to cache
 * values received from the vallox serial interface as
 * the ESH runtime and vallox serial interface exchange
 * the data very asynchronously.
 *
 * @author Hauke Fuhrmann - Initial contribution
 */
public class ValloxStore {

    // sensor data
    public int fanSpeed; // VALLOX_VARIABLE_FAN_SPEED
    public int tempInside; // VALLOX_VARIABLE_TEMP_INSIDE
    public int tempOutside; // VALLOX_VARIABLE_TEMP_OUTSIDE
    public int tempExhaust; // VALLOX_VARIABLE_TEMP_EXHAUST
    public int tempIncomming; // VALLOX_VARIABLE_TEMP_INCOMMING

    // status bits
    public boolean powerState; // VALLOX_VARIABLE_SELECT
    public boolean cO2AdjustState; // VALLOX_VARIABLE_SELECT
    public boolean humidityAdjustState; // VALLOX_VARIABLE_SELECT
    public boolean heatingState; // VALLOX_VARIABLE_SELECT
    public boolean filterGuardIndicator; // VALLOX_VARIABLE_SELECT
    public boolean heatingIndicator; // VALLOX_VARIABLE_SELECT
    public boolean faultIndicator; // VALLOX_VARIABLE_SELECT
    public boolean serviceReminderIndicator; // VALLOX_VARIABLE_SELECT

    public int humidity; // VALLOX_VARIABLE_HUMIDITY
    public int basicHumidityLevel; // VALLOX_VARIABLE_BASIC_HUMIDITY_LEVEL
    public int humiditySensor1; // VALLOX_VARIABLE_HUMIDITY_SENSOR1
    public int humiditySensor2; // VALLOX_VARIABLE_HUMIDITY_SENSOR2

    public int cO2High; // VALLOX_VARIABLE_CO2_HIGH
    public int cO2Low; // VALLOX_VARIABLE_CO2_LOW
    public int cO2SetPointHigh; // VALLOX_VARIABLE_CO2_SET_POINT_UPPER
    public int cO2SetPointLow; // VALLOX_VARIABLE_CO2_SET_POINT_LOWER

    public int fanSpeedMax; // VALLOX_VARIABLE_FAN_SPEED_MAX
    public int fanSpeedMin; // VALLOX_VARIABLE_FAN_SPEED_MIN
    public int dCFanInputAdjustment; // VALLOX_VARIABLE_DC_FAN_INPUT_ADJUSTMENT
    public int dCFanOutputAdjustment; // VALLOX_VARIABLE_DC_FAN_OUTPUT_ADJUSTMENT
    public int inputFanStopThreshold; // VALLOX_VARIABLE_INPUT_FAN_STOP
    public int heatingSetPoint; // VALLOX_VARIABLE_HEATING_SET_POINT
    public int preHeatingSetPoint; // VALLOX_VARIABLE_PRE_HEATING_SET_POINT
    public int hrcBypassThreshold; // VALLOX_VARIABLE_HRC_BYPASS
    public int cellDefrostingThreshold; // VALLOX_VARIABLE_CELL_DEFROSTING

    // program
    public int adjustmentIntervalMinutes; // VALLOX_VARIABLE_PROGRAM
    public boolean automaticHumidityLevelSeekerState; // VALLOX_VARIABLE_PROGRAM
    public boolean boostSwitchMode; // VALLOX_VARIABLE_PROGRAM
    public boolean radiatorType; // VALLOX_VARIABLE_PROGRAM
    public boolean cascadeAdjust; // VALLOX_VARIABLE_PROGRAM

    // program2
    public boolean maxSpeedLimitMode; // VALLOX_VARIABLE_PROGRAM2

    public int serviceReminder; // VALLOX_VARIABLE_SERVICE_REMINDER

    // ioport multi purpose 1
    public boolean postHeatingOn; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_1

    // ioport multi purpose 2
    public boolean damperMotorPosition; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    public boolean faultSignalRelayClosed; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    public boolean supplyFanOff; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    public boolean preHeatingOn; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    public boolean exhaustFanOff; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    public boolean firePlaceBoosterClosed; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2

    public int incommingCurrent; // VALLOX_VARIABLE_CURRENT_INCOMMING
    public int lastErrorNumber; // VALLOX_VARIABLE_LAST_ERROR_NUMBER

    // those variables might be implemented in future
    // VALLOX_VARIABLE_IOPORT_FANSPEED_RELAYS
    // VALLOX_VARIABLE_INSTALLED_CO2_SENSORS
    // VALLOX_VARIABLE_POST_HEATING_ON_COUNTER
    // VALLOX_VARIABLE_POST_HEATING_OFF_TIME
    // VALLOX_VARIABLE_POST_HEATING_TARGET_VALUE
    // VALLOX_VARIABLE_FLAGS_1
    // VALLOX_VARIABLE_FLAGS_2
    // VALLOX_VARIABLE_FLAGS_3
    // VALLOX_VARIABLE_FLAGS_4
    // VALLOX_VARIABLE_FLAGS_5
    // VALLOX_VARIABLE_FLAGS_6
    // VALLOX_VARIABLE_FIRE_PLACE_BOOSTER_COUNTER
    // VALLOX_VARIABLE_MAINTENANCE_MONTH_COUNTER

    // calculated properties
    public int inEfficiency;
    public int outEfficiency;
    public int averageEfficiency;

    // virtual properties to be able to poll for this variable
    public int selectStatus;
    public int program; // VALLOX_VARIABLE_PROGRAM
    public int program2; // VALLOX_VARIABLE_PROGRAM2
    public int ioPortMultiPurpose1; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_1
    public int ioPortMultiPurpose2; // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2

    public boolean suspended = false;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ValloxStore [fanSpeed=");
        builder.append(fanSpeed);
        builder.append(", tempInside=");
        builder.append(tempInside);
        builder.append(", tempOutside=");
        builder.append(tempOutside);
        builder.append(", tempExhaust=");
        builder.append(tempExhaust);
        builder.append(", tempIncomming=");
        builder.append(tempIncomming);
        builder.append(", powerState=");
        builder.append(powerState);
        builder.append(", cO2AdjustState=");
        builder.append(cO2AdjustState);
        builder.append(", humidityAdjustState=");
        builder.append(humidityAdjustState);
        builder.append(", heatingState=");
        builder.append(heatingState);
        builder.append(", filterGuardIndicator=");
        builder.append(filterGuardIndicator);
        builder.append(", heatingIndicator=");
        builder.append(heatingIndicator);
        builder.append(", faultIndicator=");
        builder.append(faultIndicator);
        builder.append(", serviceReminderIndicator=");
        builder.append(serviceReminderIndicator);
        builder.append(", humidity=");
        builder.append(humidity);
        builder.append(", basicHumidityLevel=");
        builder.append(basicHumidityLevel);
        builder.append(", humiditySensor1=");
        builder.append(humiditySensor1);
        builder.append(", humiditySensor2=");
        builder.append(humiditySensor2);
        builder.append(", cO2High=");
        builder.append(cO2High);
        builder.append(", cO2Low=");
        builder.append(cO2Low);
        builder.append(", cO2SetPointHigh=");
        builder.append(cO2SetPointHigh);
        builder.append(", cO2SetPointLow=");
        builder.append(cO2SetPointLow);
        builder.append(", fanSpeedMax=");
        builder.append(fanSpeedMax);
        builder.append(", fanSpeedMin=");
        builder.append(fanSpeedMin);
        builder.append(", dCFanInputAdjustment=");
        builder.append(dCFanInputAdjustment);
        builder.append(", dCFanOutputAdjustment=");
        builder.append(dCFanOutputAdjustment);
        builder.append(", inputFanStopThreshold=");
        builder.append(inputFanStopThreshold);
        builder.append(", heatingSetPoint=");
        builder.append(heatingSetPoint);
        builder.append(", preHeatingSetPoint=");
        builder.append(preHeatingSetPoint);
        builder.append(", hrcBypassThreshold=");
        builder.append(hrcBypassThreshold);
        builder.append(", cellDefrostingThreshold=");
        builder.append(cellDefrostingThreshold);
        builder.append(", adjustmentIntervalMinutes=");
        builder.append(adjustmentIntervalMinutes);
        builder.append(", automaticHumidityLevelSeekerState=");
        builder.append(automaticHumidityLevelSeekerState);
        builder.append(", boostSwitchMode=");
        builder.append(boostSwitchMode);
        builder.append(", radiatorType=");
        builder.append(radiatorType);
        builder.append(", cascadeAdjust=");
        builder.append(cascadeAdjust);
        builder.append(", maxSpeedLimitMode=");
        builder.append(maxSpeedLimitMode);
        builder.append(", serviceReminder=");
        builder.append(serviceReminder);
        builder.append(", postHeatingOn=");
        builder.append(postHeatingOn);
        builder.append(", damperMotorPosition=");
        builder.append(damperMotorPosition);
        builder.append(", faultSignalRelayClosed=");
        builder.append(faultSignalRelayClosed);
        builder.append(", supplyFanOff=");
        builder.append(supplyFanOff);
        builder.append(", preHeatingOn=");
        builder.append(preHeatingOn);
        builder.append(", exhaustFanOff=");
        builder.append(exhaustFanOff);
        builder.append(", firePlaceBoosterClosed=");
        builder.append(firePlaceBoosterClosed);
        builder.append(", incommingCurrent=");
        builder.append(incommingCurrent);
        builder.append(", lastErrorNumber=");
        builder.append(lastErrorNumber);
        builder.append(", inEfficiency=");
        builder.append(inEfficiency);
        builder.append(", outEfficiency=");
        builder.append(outEfficiency);
        builder.append(", averageEfficiency=");
        builder.append(averageEfficiency);
        builder.append(", selectStatus=");
        builder.append(selectStatus);
        builder.append(", program=");
        builder.append(program);
        builder.append(", program2=");
        builder.append(program2);
        builder.append(", ioPortMultiPurpose1=");
        builder.append(ioPortMultiPurpose1);
        builder.append(", ioPortMultiPurpose2=");
        builder.append(ioPortMultiPurpose2);
        builder.append(", suspended=");
        builder.append(suspended);
        builder.append("]");
        return builder.toString();
    }

}
