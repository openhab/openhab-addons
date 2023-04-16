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
package org.openhab.binding.anthem.internal.handler;

import static org.openhab.binding.anthem.internal.AnthemBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AnthemCommandParser} is responsible for parsing and handling
 * commands received from the Anthem processor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemCommandParser {
    private static final Pattern NUM_AVAILABLE_INPUTS_PATTERN = Pattern.compile("ICN([0-9])");
    private static final Pattern INPUT_SHORT_NAME_PATTERN = Pattern.compile("ISN([0-9][0-9])(\\p{ASCII}*)");
    private static final Pattern INPUT_LONG_NAME_PATTERN = Pattern.compile("ILN([0-9][0-9])(\\p{ASCII}*)");
    private static final Pattern POWER_PATTERN = Pattern.compile("Z([0-9])POW([01])");
    private static final Pattern VOLUME_PATTERN = Pattern.compile("Z([0-9])VOL(-?[0-9]*)");
    private static final Pattern MUTE_PATTERN = Pattern.compile("Z([0-9])MUT([01])");
    private static final Pattern ACTIVE_INPUT_PATTERN = Pattern.compile("Z([0-9])INP([1-9])");

    private Logger logger = LoggerFactory.getLogger(AnthemCommandParser.class);

    private AnthemHandler handler;

    private Map<Integer, String> inputShortNamesMap = new HashMap<>();
    private Map<Integer, String> inputLongNamesMap = new HashMap<>();

    private int numAvailableInputs;

    public AnthemCommandParser(AnthemHandler anthemHandler) {
        this.handler = anthemHandler;
    }

    public int getNumAvailableInputs() {
        return numAvailableInputs;
    }

    public void parseMessage(String command) {
        if (!isValidCommand(command)) {
            return;
        }
        // Strip off the termination char and any whitespace
        String cmd = command.substring(0, command.indexOf(COMMAND_TERMINATION_CHAR)).trim();

        // Zone command
        if (cmd.startsWith("Z")) {
            parseZoneCommand(cmd);
        }
        // Information command
        else if (cmd.startsWith("ID")) {
            parseInformationCommand(cmd);
        }
        // Number of inputs
        else if (cmd.startsWith("ICN")) {
            parseNumberOfAvailableInputsCommand(cmd);
        }
        // Input short name
        else if (cmd.startsWith("ISN")) {
            parseInputShortNameCommand(cmd);
        }
        // Input long name
        else if (cmd.startsWith("ILN")) {
            parseInputLongNameCommand(cmd);
        }
        // Error response to command
        else if (cmd.startsWith("!")) {
            parseErrorCommand(cmd);
        }
        // Unknown/unhandled command
        else {
            logger.trace("Command parser doesn't know how to handle command: '{}'", cmd);
        }
    }

    private boolean isValidCommand(String command) {
        if (command.isEmpty() || command.isBlank() || command.length() < 4
                || command.indexOf(COMMAND_TERMINATION_CHAR) == -1) {
            logger.trace("Parser received invalid command: '{}'", command);
            return false;
        }
        return true;
    }

    private void parseZoneCommand(String command) {
        // Power update
        if (command.contains("POW")) {
            parsePower(command);
        }
        // Volume update
        else if (command.contains("VOL")) {
            parseVolume(command);
        }
        // Mute update
        else if (command.contains("MUT")) {
            parseMute(command);
        }
        // Active input
        else if (command.contains("INP")) {
            parseActiveInput(command);
        }
    }

    private void parseInformationCommand(String command) {
        String value = command.substring(3, command.length());
        switch (command.substring(2, 3)) {
            case "M":
                handler.setModel(value);
                break;
            case "R":
                handler.setRegion(value);
                break;
            case "S":
                handler.setSoftwareVersion(value);
                break;
            case "B":
                handler.setSoftwareBuildDate(value);
                break;
            case "H":
                handler.setHardwareVersion(value);
                break;
            case "N":
                handler.setMacAddress(value);
                break;
            case "Q":
                // Ignore
                break;
            default:
                logger.debug("Unknown info type");
                break;
        }
    }

    private void parseNumberOfAvailableInputsCommand(String command) {
        Matcher matcher = NUM_AVAILABLE_INPUTS_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                String numAvailableInputsStr = matcher.group(1);
                DecimalType numAvailableInputs = DecimalType.valueOf(numAvailableInputsStr);
                handler.setNumAvailableInputs(numAvailableInputs.intValue());
                this.numAvailableInputs = numAvailableInputs.intValue();
            } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
    }

    private void parseInputShortNameCommand(String command) {
        parseInputName(command, INPUT_SHORT_NAME_PATTERN.matcher(command), inputShortNamesMap);
    }

    private void parseInputLongNameCommand(String command) {
        parseInputName(command, INPUT_LONG_NAME_PATTERN.matcher(command), inputLongNamesMap);
    }

    private void parseErrorCommand(String command) {
        logger.info("Command was not processed successfully by the device: '{}'", command);
    }

    private void parseInputName(String command, @Nullable Matcher matcher, Map<Integer, String> map) {
        if (matcher != null) {
            try {
                matcher.find();
                int input = Integer.parseInt(matcher.group(1));
                String inputName = matcher.group(2);
                map.putIfAbsent(input, inputName);
            } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
    }

    private void parsePower(String command) {
        Matcher mmatcher = POWER_PATTERN.matcher(command);
        if (mmatcher != null) {
            try {
                mmatcher.find();
                String zone = mmatcher.group(1);
                String power = mmatcher.group(2);
                handler.updateChannelState(zone, CHANNEL_POWER, "1".equals(power) ? OnOffType.ON : OnOffType.OFF);
                handler.checkPowerStatusChange(zone, power);
            } catch (IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
    }

    private void parseVolume(String command) {
        Matcher matcher = VOLUME_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                String zone = matcher.group(1);
                String volume = matcher.group(2);
                handler.updateChannelState(zone, CHANNEL_VOLUME_DB, DecimalType.valueOf(volume));
            } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
    }

    private void parseMute(String command) {
        Matcher matcher = MUTE_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                String zone = matcher.group(1);
                String mute = matcher.group(2);
                handler.updateChannelState(zone, CHANNEL_MUTE, "1".equals(mute) ? OnOffType.ON : OnOffType.OFF);
            } catch (IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
    }

    private void parseActiveInput(String command) {
        Matcher matcher = ACTIVE_INPUT_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                String zone = matcher.group(1);
                DecimalType activeInput = DecimalType.valueOf(matcher.group(2));
                handler.updateChannelState(zone, CHANNEL_ACTIVE_INPUT, activeInput);
                String name;
                name = inputShortNamesMap.get(activeInput.intValue());
                if (name != null) {
                    handler.updateChannelState(zone, CHANNEL_ACTIVE_INPUT_SHORT_NAME, new StringType(name));
                }
                name = inputShortNamesMap.get(activeInput.intValue());
                if (name != null) {
                    handler.updateChannelState(zone, CHANNEL_ACTIVE_INPUT_LONG_NAME, new StringType(name));
                }
            } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
    }
}
