/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.devices.a1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.Timer;
import org.openhab.binding.mideaac.internal.devices.Timer.TimeParser;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1FanSpeed;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1OperationalMode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link A1CommandHelper} is a static class that is able to translate {@link Command} to {@link A1CommandSet}
 * for Midea Dehumidifier devices.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class A1CommandHelper {
    private static Logger logger = LoggerFactory.getLogger(A1CommandHelper.class);

    private static final StringType HUMIDIFIER_MODE_MANUAL = new StringType("MANUAL");
    private static final StringType HUMIDIFIER_MODE_CONTINUOUS = new StringType("CONTINUOUS");
    private static final StringType HUMIDIFIER_MODE_AUTO = new StringType("AUTO");
    private static final StringType HUMIDIFIER_MODE_CLOTHES_DRY = new StringType("CLOTHES_DRY");
    private static final StringType HUMIDIFIER_MODE_SHOES_DRY = new StringType("SHOES_DRY");

    private static final StringType FAN_SPEED_OFF = new StringType("OFF");
    private static final StringType FAN_SPEED_LOWEST = new StringType("LOWEST");
    private static final StringType FAN_SPEED_LOW = new StringType("LOW");
    private static final StringType FAN_SPEED_MEDIUM = new StringType("MEDIUM");
    private static final StringType FAN_SPEED_HIGH = new StringType("HIGH");
    private static final StringType FAN_SPEED_AUTO = new StringType("AUTO");

    /**
     * Device Power ON OFF for A1 Dehumidifier
     * 
     * @param command On or Off
     */
    public static A1CommandSet handlePower(Command command, A1Response lastResponse)
            throws UnsupportedOperationException {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setPowerState(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setPowerState(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown power command: {}", command));
        }
        return commandSet;
    }

    /**
     * Fan Speeds for A1 Dehumidifier
     * This command can also turn the power 7F
     * 
     * @param command Fan Speed Auto, Low, High, etc.
     */
    public static A1CommandSet handleA1FanSpeed(Command command, A1Response lastResponse)
            throws UnsupportedOperationException {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command instanceof StringType) {
            commandSet.setPowerState(true);
            if (command.equals(FAN_SPEED_OFF)) {
                commandSet.setPowerState(false);
            } else if (command.equals(FAN_SPEED_LOWEST)) {
                commandSet.setA1FanSpeed(A1FanSpeed.LOWEST);
            } else if (command.equals(FAN_SPEED_LOW)) {
                commandSet.setA1FanSpeed(A1FanSpeed.LOW);
            } else if (command.equals(FAN_SPEED_MEDIUM)) {
                commandSet.setA1FanSpeed(A1FanSpeed.MEDIUM);
            } else if (command.equals(FAN_SPEED_HIGH)) {
                commandSet.setA1FanSpeed(A1FanSpeed.HIGH);
            } else if (command.equals(FAN_SPEED_AUTO)) {
                commandSet.setA1FanSpeed(A1FanSpeed.AUTO);
            } else {
                throw new UnsupportedOperationException(String.format("Unknown fan speed command: {}", command));
            }
        }
        return commandSet;
    }

    /**
     * Supported Midea Humidifier modes
     * 
     * @param command Modes MANUAL, CONTINUOUS, AUTO, CLOTHES_DRY, SHOES_DRY
     */
    public static A1CommandSet handleA1OperationalMode(Command command, A1Response lastResponse)
            throws UnsupportedOperationException {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command instanceof StringType) {
            if (command.equals(HUMIDIFIER_MODE_MANUAL)) {
                commandSet.setA1OperationalMode(A1OperationalMode.MANUAL);
            } else if (command.equals(HUMIDIFIER_MODE_CONTINUOUS)) {
                commandSet.setA1OperationalMode(A1OperationalMode.CONTINUOUS);
            } else if (command.equals(HUMIDIFIER_MODE_AUTO)) {
                commandSet.setA1OperationalMode(A1OperationalMode.AUTO);
            } else if (command.equals(HUMIDIFIER_MODE_CLOTHES_DRY)) {
                commandSet.setA1OperationalMode(A1OperationalMode.CLOTHES_DRY);
            } else if (command.equals(HUMIDIFIER_MODE_SHOES_DRY)) {
                commandSet.setA1OperationalMode(A1OperationalMode.SHOES_DRY);
            } else {
                throw new UnsupportedOperationException(String.format("Unknown operational mode command: {}", command));
            }
        }
        return commandSet;
    }

    /**
     * Handle A1 Dehumidifier Swing Mode
     * 
     * @param command Swing Mode
     */
    public static A1CommandSet handleA1Swing(Command command, A1Response lastResponse)
            throws UnsupportedOperationException {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setA1SwingMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setA1SwingMode(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown Swing mode command: {}", command));
        }

        return commandSet;
    }

    /**
     * Handle A1 Dehumidifier Child Lock
     * 
     * @param command Child Lock
     */
    public static A1CommandSet handleA1ChildLock(Command command, A1Response lastResponse)
            throws UnsupportedOperationException {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setA1ChildLock(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setA1ChildLock(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown Child Lock command: {}", command));
        }

        return commandSet;
    }

    /**
     * Handle A1 Dehumidifier Anion
     * 
     * @param command Anoin
     */
    public static A1CommandSet handleA1Anion(Command command, A1Response lastResponse)
            throws UnsupportedOperationException {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setA1Anion(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setA1Anion(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown Anion command: {}", command));
        }

        return commandSet;
    }

    /**
     * Sets the time (from now) that the device will turn on at it's current settings
     * 
     * @param command Sets On Timer
     */
    public static A1CommandSet handleOnTimer(Command command, A1Response lastResponse) {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        TimeParser timeParser = timer.new TimeParser();
        if (command instanceof StringType) {
            String timeString = ((StringType) command).toString();
            if (!timeString.matches("\\d{2}:\\d{2}")) {
                logger.debug("Invalid time format. Expected HH:MM.");
                commandSet.setOnTimer(false, hours, minutes);
            } else {
                int[] timeParts = timeParser.parseTime(timeString);
                boolean on = true;
                hours = timeParts[0];
                minutes = timeParts[1];
                // Validate minutes and hours
                if (minutes < 0 || minutes > 59 || hours > 24 || hours < 0) {
                    logger.debug("Invalid hours (24 max) and or minutes (59 max)");
                    hours = 0;
                    minutes = 0;
                }
                if (hours == 0 && minutes == 0) {
                    commandSet.setOnTimer(false, hours, minutes);
                } else {
                    commandSet.setOnTimer(on, hours, minutes);
                }
            }
        } else {
            logger.debug("Command must be of type StringType: {}", command);
            commandSet.setOnTimer(false, hours, minutes);
        }

        return commandSet;
    }

    /**
     * Sets the time (from now) that the device will turn off
     * 
     * @param command Sets Off Timer
     */
    public static A1CommandSet handleOffTimer(Command command, A1Response lastResponse) {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        TimeParser timeParser = timer.new TimeParser();
        if (command instanceof StringType) {
            String timeString = ((StringType) command).toString();
            if (!timeString.matches("\\d{2}:\\d{2}")) {
                logger.debug("Invalid time format. Expected HH:MM.");
                commandSet.setOffTimer(false, hours, minutes);
            } else {
                int[] timeParts = timeParser.parseTime(timeString);
                boolean on = true;
                hours = timeParts[0];
                minutes = timeParts[1];
                // Validate minutes and hours
                if (minutes < 0 || minutes > 59 || hours > 24 || hours < 0) {
                    logger.debug("Invalid hours (24 max) and or minutes (59 max)");
                    hours = 0;
                    minutes = 0;
                }
                if (hours == 0 && minutes == 0) {
                    commandSet.setOffTimer(false, hours, minutes);
                } else {
                    commandSet.setOffTimer(on, hours, minutes);
                }
            }
        } else {
            logger.debug("Command must be of type StringType: {}", command);
            commandSet.setOffTimer(false, hours, minutes);
        }

        return commandSet;
    }

    /**
     * Sets the Target Humidity for Dehumidifier
     * 
     * @param command Target Humidity
     */
    public static A1CommandSet handleA1MaximumHumidity(Command command, A1Response lastResponse) {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command instanceof DecimalType decimalCommand) {
            int humidity = decimalCommand.intValue();
            commandSet.setA1MaximumHumidity(limitA1HumidityToRange(humidity));
        } else {
            logger.debug("Unknown target humidity command: {}", command);
        }

        return commandSet;
    }

    // Limit Humidity to range
    private static int limitA1HumidityToRange(int humidity) {
        if (humidity < 35) {
            return 35;
        }
        if (humidity > 85) {
            return 85;
        }

        return humidity;
    }

    /**
     * Sets Tank setpoint for the Dehumidifier
     * 
     * @param command Tank Setpoint
     */
    public static A1CommandSet handleA1TankSetpoint(Command command, A1Response lastResponse) {
        A1CommandSet commandSet = A1CommandSet.fromResponse(lastResponse);

        if (command instanceof DecimalType decimalCommand) {
            int setpoint = decimalCommand.intValue();
            commandSet.setTankSetpoint(setpoint);
        } else {
            logger.debug("Unknown target humidity command: {}", command);
        }

        return commandSet;
    }
}
