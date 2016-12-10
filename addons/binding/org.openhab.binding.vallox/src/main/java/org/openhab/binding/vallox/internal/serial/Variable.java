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
 * Telegram identifiers. Use the key-variable for each binary
 * value.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public enum Variable {

    POLL((byte) 0x00), // poll request for variable in value.

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0 Speed 1 - 0=0ff 1=on - readonly
    // | | | | | | +--- 1 Speed 2 - 0=0ff 1=on - readonly
    // | | | | | +----- 2 Speed 3 - 0=0ff 1=on - readonly
    // | | | | +------- 3 Speed 4 - 0=0ff 1=on - readonly
    // | | | +--------- 4 Speed 5 - 0=0ff 1=on - readonly
    // | | +----------- 5 Speed 6 - 0=0ff 1=on - readonly
    // | +------------- 6 Speed 7 - 0=0ff 1=on - readonly
    // +--------------- 7 Speed 8 - 0=0ff 1=on - readonly
    IOPORT_FANSPEED_RELAYS((byte) 0x06),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4
    // | | +----------- 5 post-heating on - 0=0ff 1=on - readonly
    // | +------------- 6
    // +--------------- 7
    IOPORT_MULTI_PURPOSE_1((byte) 0x07),

    // 1 1 1 1 1 1 1 1 0=0ff 1=on
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1 damper motor position - 0=wbyteer 1=season - readonly
    // | | | | | +----- 2 fault signal relay - 0=open 1=closed - readonly
    // | | | | +------- 3 supply fan - 0=on 1=off
    // | | | +--------- 4 pre-heating - 0=off 1=on - readonly
    // | | +----------- 5 exhaust-fan - 0=on 1=off
    // | +------------- 6 fireplace-booster - 0=open 1=closed - readonly
    // +--------------- 7
    IOPORT_MULTI_PURPOSE_2((byte) 0x08),

    // 01H( (byte)speed 1
    // 03H( (byte)speed 2
    // 07H( (byte)speed 3
    // 0FH( (byte)speed 4
    // 1FH( (byte)speed 5
    // 3FH( (byte)speed 6
    // 7FH( (byte)speed 7
    // FFH( (byte)speed 8
    FAN_SPEED((byte) 0x29),

    // 33H( (byte)0% FFH( (byte)100%
    HUMIDITY((byte) 0x2A), // higher measured relative humidity from 2F and 30. Translating Formula (x-51)/2.04
    CO2_HIGH((byte) 0x2B),
    CO2_LOW((byte) 0x2C),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1 Sensor1 - 0=not installed 1=installed - readonly
    // | | | | | +----- 2 Sensor2 - 0=not installed 1=installed - readonly
    // | | | | +------- 3 Sensor3 - 0=not installed 1=installed - readonly
    // | | | +--------- 4 Sensor4 - 0=not installed 1=installed - readonly
    // | | +----------- 5 Sensor5 - 0=not installed 1=installed - readonly
    // | +------------- 6
    // +--------------- 7
    INSTALLED_CO2_SENSORS((byte) 0x2D),

    CURRENT_INCOMMING((byte) 0x2E), // Current/Voltage in mA incomming on machine - readonly

    HUMIDITY_SENSOR1((byte) 0x2F), // sensor value: (x-51)/2.04
    HUMIDITY_SENSOR2((byte) 0x30), // sensor value: (x-51)/2.04

    TEMP_OUTSIDE((byte) 0x32),
    TEMP_EXHAUST((byte) 0x33),
    TEMP_INSIDE((byte) 0x34),
    TEMP_INCOMMING((byte) 0x35),

    // 05H( (byte)Supply air temperature sensor fault
    // 06H( (byte)Carbon dioxide alarm
    // 07h( (byte)Outdoor air sensor fault
    // 08H( (byte)Extract air sensor fault
    // 09h( (byte)Water radiator danger of freezing
    // 0AH( (byte)Exhaust air sensor fault
    LAST_ERROR_NUMBER((byte) 0x36),

    // Post-heating power-on seconds counter. Percentage of X / 2.5
    POST_HEATING_ON_COUNTER((byte) 0x55),

    // Post-heating off time, in seconds, the counter. Percentage of X / 2.5
    POST_HEATING_OFF_TIME((byte) 0x56),

    // The ventilation zone of air blown to the desired temperature NTC sensor scale
    POST_HEATING_TARGET_VALUE((byte) 0x57),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4
    // | | +----------- 5
    // | +------------- 6
    // +--------------- 7
    FLAGS_1((byte) 0x6C),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0 CO2 higher speed-request 0=no 1=Speed​​. up
    // | | | | | | +--- 1 CO2 lower rate public invitation 0=no 1=Speed​​. down
    // | | | | | +----- 2 %RH lower rate public invitation 0=no 1=Speed​​. down
    // | | | | +------- 3 switch low. Spd.-request 0=no 1=Speed​. down
    // | | | +--------- 4
    // | | +----------- 5
    // | +------------- 6 CO2 alarm 0=no 1=CO2 alarm
    // +--------------- 7 sensor Frost alarm 0=no 1=a risk of freezing
    FLAGS_2((byte) 0x6D),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4
    // | | +----------- 5
    // | +------------- 6
    // +--------------- 7
    FLAGS_3((byte) 0x6E),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4 water radiator danger of freezing 0=no risk 1( (byte)risk
    // | | +----------- 5
    // | +------------- 6
    // +--------------- 7 slave/master selection 0=slave 1=master
    FLAGS_4((byte) 0x6F),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4
    // | | +----------- 5
    // | +------------- 6
    // +--------------- 7 preheating status flag 0=on 1=off
    FLAGS_5((byte) 0x70),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4 remote monitoring control 0=no 1=Operation - readonly
    // | | +----------- 5 Activation of the fireplace switch read the variable and set this number one <-- bit can be
    // set to activate fire place switch
    // | +------------- 6 fireplace/booster status 0=off 1=on - read only
    // +--------------- 7
    FLAGS_6((byte) 0x71),

    // Function time in minutes remaining , descending - readonly
    FIRE_PLACE_BOOSTER_COUNTER((byte) 0x79),

    // Suspend Resume Traffic for CO2 sensor byteeraction: is sent twice as broadcast
    SUSPEND((byte) 0x91),
    RESUME((byte) 0x8F),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0 Power state
    // | | | | | | +--- 1 CO2 Adjust state
    // | | | | | +----- 2 %RH adjust state
    // | | | | +------- 3 Heating state
    // | | | +--------- 4 Filterguard indicator
    // | | +----------- 5 Heating indicator
    // | +------------- 6 Fault indicator
    // +--------------- 7 service reminder
    SELECT((byte) 0xA3),
    HEATING_SET_POINT((byte) 0xA4),

    // 01H( (byte)Speed 1
    // 03H( (byte)Speed 2
    // 07H( (byte)Speed 3
    // 0FH( (byte)Speed 4
    // 1FH( (byte)Speed 5
    // 3FH( (byte)Speed 6
    // 7FH( (byte)Speed 7
    // FFH( (byte)Speed 8
    FAN_SPEED_MAX((byte) 0xA5),
    SERVICE_REMINDER((byte) 0xA6), // months
    PRE_HEATING_SET_POINT((byte) 0xA7),
    INPUT_FAN_STOP((byte) 0xA8), // Temp threshold: fan stops if input temp falls below this temp.

    // 01H( (byte)Speed 1
    // 03H( (byte)Speed 2
    // 07H( (byte)Speed 3
    // 0FH( (byte)Speed 4
    // 1FH( (byte)Speed 5
    // 3FH( (byte)Speed 6
    // 7FH( (byte)Speed 7
    // FFH( (byte)Speed 8
    FAN_SPEED_MIN((byte) 0xA9),

    // 1 1 1 1 1 1 1 1
    // | | | | _______
    // | | | | |
    // | | | | +--- 0-3 set adjustment byteerval of CO2 and %RH in minutes (Regelbyteerval)
    // | | | |
    // | | | |
    // | | | |
    // | | | +--------- 4 automatic RH basic level seeker state
    // | | +----------- 5 boost switch modde (1=boost, 0( (byte)fireplace)
    // | +------------- 6 radiator type 0( (byte)electric, 1( (byte)water
    // +--------------- 7 cascade adjust 0( (byte)off, 1( (byte)on
    PROGRAM((byte) 0xAA),

    // The maintenance counter informs about the next maintenance alarm time: remaining months, descending.
    MAINTENANCE_MONTH_COUNTER((byte) 0xAB),

    BASIC_HUMIDITY_LEVEL((byte) 0xAE),
    HRC_BYPASS((byte) 0xAF), // Heat recovery cell bypass setpoint temp
    DC_FAN_INPUT_ADJUSTMENT((byte) 0xB0), // 0-100%
    DC_FAN_OUTPUT_ADJUSTMENT((byte) 0xB1), // 0-100%
    CELL_DEFROSTING((byte) 0xB2), // Defrosting starts when exhaust air drops below this setpoint temp (Hysteresis 4)
    CO2_SET_POINT_UPPER((byte) 0xB3),
    CO2_SET_POINT_LOWER((byte) 0xB4),

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0 Function of max speed limit 0( (byte)with adjustment, 1( (byte)always
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4
    // | | +----------- 5
    // | +------------- 6
    // +--------------- 7
    PROGRAM2((byte) 0xB5),

    // This one is queried at startup and answered with 3 but not described in the protocol
    UNKNOWN((byte) 0xC0),

    // dummy for any key we don't know
    UNDEFINED((byte) 0x00);

    private final byte key;

    public byte getKey() {
        return key;
    }

    Variable(byte key) {
        this.key = key;
    }

    static Variable get(byte key) {
        for (Variable v : Variable.values()) {
            if (v.key == key) {
                return v;
            }
        }
        return UNDEFINED;
    }

}
