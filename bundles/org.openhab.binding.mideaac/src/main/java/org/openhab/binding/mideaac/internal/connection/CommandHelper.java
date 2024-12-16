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
package org.openhab.binding.mideaac.internal.connection;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.openhab.binding.mideaac.internal.handler.CommandSet;
import org.openhab.binding.mideaac.internal.handler.Response;
import org.openhab.binding.mideaac.internal.handler.Timer;
import org.openhab.binding.mideaac.internal.handler.Timer.TimeParser;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandHelper} is a static class that is able to translate {@link Command} to {@link CommandSet}
 *
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public class CommandHelper {
    private static Logger logger = LoggerFactory.getLogger(CommandHelper.class);

    private static final StringType OPERATIONAL_MODE_OFF = new StringType("OFF");
    private static final StringType OPERATIONAL_MODE_AUTO = new StringType("AUTO");
    private static final StringType OPERATIONAL_MODE_COOL = new StringType("COOL");
    private static final StringType OPERATIONAL_MODE_DRY = new StringType("DRY");
    private static final StringType OPERATIONAL_MODE_HEAT = new StringType("HEAT");
    private static final StringType OPERATIONAL_MODE_FAN_ONLY = new StringType("FAN_ONLY");

    private static final StringType FAN_SPEED_OFF = new StringType("OFF");
    private static final StringType FAN_SPEED_SILENT = new StringType("SILENT");
    private static final StringType FAN_SPEED_LOW = new StringType("LOW");
    private static final StringType FAN_SPEED_MEDIUM = new StringType("MEDIUM");
    private static final StringType FAN_SPEED_HIGH = new StringType("HIGH");
    private static final StringType FAN_SPEED_FULL = new StringType("FULL");
    private static final StringType FAN_SPEED_AUTO = new StringType("AUTO");

    private static final StringType SWING_MODE_OFF = new StringType("OFF");
    private static final StringType SWING_MODE_VERTICAL = new StringType("VERTICAL");
    private static final StringType SWING_MODE_HORIZONTAL = new StringType("HORIZONTAL");
    private static final StringType SWING_MODE_BOTH = new StringType("BOTH");

    /**
     * Device Power ON OFF
     * 
     * @param command On or Off
     */
    public static CommandSet handlePower(Command command, Response lastResponse) throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

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
     * Supported AC - Heat Pump modes
     * 
     * @param command Operational Mode Cool, Heat, etc.
     */
    public static CommandSet handleOperationalMode(Command command, Response lastResponse)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        if (command instanceof StringType) {
            if (command.equals(OPERATIONAL_MODE_OFF)) {
                commandSet.setPowerState(false);
            } else if (command.equals(OPERATIONAL_MODE_AUTO)) {
                commandSet.setOperationalMode(OperationalMode.AUTO);
            } else if (command.equals(OPERATIONAL_MODE_COOL)) {
                commandSet.setOperationalMode(OperationalMode.COOL);
            } else if (command.equals(OPERATIONAL_MODE_DRY)) {
                commandSet.setOperationalMode(OperationalMode.DRY);
            } else if (command.equals(OPERATIONAL_MODE_HEAT)) {
                commandSet.setOperationalMode(OperationalMode.HEAT);
            } else if (command.equals(OPERATIONAL_MODE_FAN_ONLY)) {
                commandSet.setOperationalMode(OperationalMode.FAN_ONLY);
            } else {
                throw new UnsupportedOperationException(String.format("Unknown operational mode command: {}", command));
            }
        }
        return commandSet;
    }

    private static float limitTargetTemperatureToRange(float temperatureInCelsius) {
        if (temperatureInCelsius < 17.0f) {
            return 17.0f;
        }
        if (temperatureInCelsius > 30.0f) {
            return 30.0f;
        }

        return temperatureInCelsius;
    }

    /**
     * Device only uses Celsius in 0.5 degree increments
     * Fahrenheit is rounded to fit (example
     * setting to 64 F is 18 C but will result in 64.4 F display in OH)
     * The evaporator only displays 2 digits, so will show 64.
     * 
     * @param command Target Temperature
     */
    public static CommandSet handleTargetTemperature(Command command, Response lastResponse)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        if (command instanceof DecimalType decimalCommand) {
            logger.debug("Handle Target Temperature as DecimalType in degrees C");
            commandSet.setTargetTemperature(limitTargetTemperatureToRange(decimalCommand.floatValue()));
        } else if (command instanceof QuantityType<?> quantityCommand) {
            if (quantityCommand.getUnit().equals(ImperialUnits.FAHRENHEIT)) {
                quantityCommand = Objects.requireNonNull(quantityCommand.toUnit(SIUnits.CELSIUS));
            }
            commandSet.setTargetTemperature(limitTargetTemperatureToRange(quantityCommand.floatValue()));
        } else {
            throw new UnsupportedOperationException(String.format("Unknown target temperature command: {}", command));
        }
        return commandSet;
    }

    /**
     * Fan Speeds vary by V2 or V3 and device. This command also turns the power ON
     * 
     * @param command Fan Speed Auto, Low, High, etc.
     */
    public static CommandSet handleFanSpeed(Command command, Response lastResponse, int version)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        if (command instanceof StringType) {
            commandSet.setPowerState(true);
            if (command.equals(FAN_SPEED_OFF)) {
                commandSet.setPowerState(false);
            } else if (command.equals(FAN_SPEED_SILENT)) {
                if (version == 2) {
                    commandSet.setFanSpeed(FanSpeed.SILENT2);
                } else if (version == 3) {
                    commandSet.setFanSpeed(FanSpeed.SILENT3);
                }
            } else if (command.equals(FAN_SPEED_LOW)) {
                if (version == 2) {
                    commandSet.setFanSpeed(FanSpeed.LOW2);
                } else if (version == 3) {
                    commandSet.setFanSpeed(FanSpeed.LOW3);
                }
            } else if (command.equals(FAN_SPEED_MEDIUM)) {
                if (version == 2) {
                    commandSet.setFanSpeed(FanSpeed.MEDIUM2);
                } else if (version == 3) {
                    commandSet.setFanSpeed(FanSpeed.MEDIUM3);
                }
            } else if (command.equals(FAN_SPEED_HIGH)) {
                if (version == 2) {
                    commandSet.setFanSpeed(FanSpeed.HIGH2);
                } else if (version == 3) {
                    commandSet.setFanSpeed(FanSpeed.HIGH3);
                }
            } else if (command.equals(FAN_SPEED_FULL)) {
                if (version == 2) {
                    commandSet.setFanSpeed(FanSpeed.FULL2);
                } else if (version == 3) {
                    commandSet.setFanSpeed(FanSpeed.FULL3);
                }
            } else if (command.equals(FAN_SPEED_AUTO)) {
                if (version == 2) {
                    commandSet.setFanSpeed(FanSpeed.AUTO2);
                } else if (version == 3) {
                    commandSet.setFanSpeed(FanSpeed.AUTO3);
                }
            } else {
                throw new UnsupportedOperationException(String.format("Unknown fan speed command: {}", command));
            }
        }
        return commandSet;
    }

    /**
     * Must be set in Cool mode. Fan will switch to Auto
     * and temp will be 24 C or 75 F on unit (75.2 F in OH)
     * 
     * @param command Eco Mode
     */
    public static CommandSet handleEcoMode(Command command, Response lastResponse)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setEcoMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setEcoMode(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown eco mode command: {}", command));
        }

        return commandSet;
    }

    /**
     * Modes supported depends on the device
     * Power is turned on when swing mode is changed
     * 
     * @param command Swing Mode
     */
    public static CommandSet handleSwingMode(Command command, Response lastResponse, int version)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        commandSet.setPowerState(true);

        if (command instanceof StringType) {
            if (command.equals(SWING_MODE_OFF)) {
                if (version == 2) {
                    commandSet.setSwingMode(SwingMode.OFF2);
                } else if (version == 3) {
                    commandSet.setSwingMode(SwingMode.OFF3);
                }
            } else if (command.equals(SWING_MODE_VERTICAL)) {
                if (version == 2) {
                    commandSet.setSwingMode(SwingMode.VERTICAL2);
                } else if (version == 3) {
                    commandSet.setSwingMode(SwingMode.VERTICAL3);
                }
            } else if (command.equals(SWING_MODE_HORIZONTAL)) {
                if (version == 2) {
                    commandSet.setSwingMode(SwingMode.HORIZONTAL2);
                } else if (version == 3) {
                    commandSet.setSwingMode(SwingMode.HORIZONTAL3);
                }
            } else if (command.equals(SWING_MODE_BOTH)) {
                if (version == 2) {
                    commandSet.setSwingMode(SwingMode.BOTH2);
                } else if (version == 3) {
                    commandSet.setSwingMode(SwingMode.BOTH3);
                }
            } else {
                throw new UnsupportedOperationException(String.format("Unknown swing mode command: {}", command));
            }
        }

        return commandSet;
    }

    /**
     * Turbo mode is only with Heat or Cool to quickly change
     * Room temperature. Power is turned on.
     * 
     * @param command Turbo mode - Fast cooling or Heating
     */
    public static CommandSet handleTurboMode(Command command, Response lastResponse)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setTurboMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setTurboMode(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown turbo mode command: {}", command));
        }

        return commandSet;
    }

    /**
     * May not be supported via LAN in all models - IR only
     * 
     * @param command Screen Display Toggle to ON or Off - One command
     */
    public static CommandSet handleScreenDisplay(Command command, Response lastResponse)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setScreenDisplay(true);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setScreenDisplay(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown screen display command: {}", command));
        }

        return commandSet;
    }

    /**
     * This is only for the AC LED device display units, calcs always in Celsius
     * 
     * @param command Temp unit on the indoor evaporator
     */
    public static CommandSet handleTempUnit(Command command, Response lastResponse)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setFahrenheit(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setFahrenheit(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown temperature unit command: {}", command));
        }

        return commandSet;
    }

    /**
     * Power turned on with Sleep Mode Change
     * Sleep mode increases temp slightly in first 2 hours of sleep
     * 
     * @param command Sleep function
     */
    public static CommandSet handleSleepFunction(Command command, Response lastResponse)
            throws UnsupportedOperationException {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setSleepMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setSleepMode(true);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown sleep mode command: {}", command));
        }

        return commandSet;
    }

    /**
     * Sets the time (from now) that the device will turn on at it's current settings
     * 
     * @param command Sets On Timer
     */
    public static CommandSet handleOnTimer(Command command, Response lastResponse) {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);
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
    public static CommandSet handleOffTimer(Command command, Response lastResponse) {
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);
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
}
