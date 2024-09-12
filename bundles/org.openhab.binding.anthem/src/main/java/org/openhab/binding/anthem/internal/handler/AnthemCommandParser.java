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
import org.openhab.core.thing.Thing;
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
    private static final Pattern NUM_AVAILABLE_INPUTS_PATTERN = Pattern.compile("ICN([0-9]{1,2})");
    private static final Pattern INPUT_SHORT_NAME_PATTERN = Pattern.compile("ISN([0-9][0-9])(\\p{ASCII}*)");
    private static final Pattern INPUT_LONG_NAME_PATTERN = Pattern.compile("ILN([0-9][0-9])(\\p{ASCII}*)");
    private static final Pattern POWER_PATTERN = Pattern.compile("Z([0-9])POW([01])");
    private static final Pattern VOLUME_PATTERN = Pattern.compile("Z([0-9])VOL(-?[0-9]*)");
    private static final Pattern MUTE_PATTERN = Pattern.compile("Z([0-9])MUT([01])");
    private static final Pattern ACTIVE_INPUT_PATTERN = Pattern.compile("Z([0-9])INP([1-9])");

    private Logger logger = LoggerFactory.getLogger(AnthemCommandParser.class);

    private Map<String, String> inputShortNamesMap = new HashMap<>();
    private Map<String, String> inputLongNamesMap = new HashMap<>();

    public @Nullable AnthemUpdate parseCommand(String command) {
        if (!isValidCommand(command)) {
            return null;
        }
        // Strip off the termination char and any whitespace
        String cmd = command.substring(0, command.indexOf(COMMAND_TERMINATION_CHAR)).trim();

        // Zone command
        if (cmd.startsWith("Z")) {
            return parseZoneCommand(cmd);
        }
        // Information command
        else if (cmd.startsWith("ID")) {
            return parseInformationCommand(cmd);
        }
        // Number of inputs
        else if (cmd.startsWith("ICN")) {
            return parseNumberOfAvailableInputsCommand(cmd);
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
        return null;
    }

    public @Nullable String getInputShortName(String input) {
        return inputShortNamesMap.get(input);
    }

    public @Nullable String getInputLongName(String input) {
        return inputLongNamesMap.get(input);
    }

    private boolean isValidCommand(String command) {
        if (command.isEmpty() || command.isBlank() || command.length() < 4
                || command.indexOf(COMMAND_TERMINATION_CHAR) == -1) {
            logger.trace("Parser received invalid command: '{}'", command);
            return false;
        }
        return true;
    }

    private @Nullable AnthemUpdate parseZoneCommand(String command) {
        // Power update
        if (command.contains("POW")) {
            return parsePower(command);
        }
        // Volume update
        else if (command.contains("VOL")) {
            return parseVolume(command);
        }
        // Mute update
        else if (command.contains("MUT")) {
            return parseMute(command);
        }
        // Active input
        else if (command.contains("INP")) {
            return parseActiveInput(command);
        }
        return null;
    }

    private @Nullable AnthemUpdate parseInformationCommand(String command) {
        String value = command.substring(3, command.length());
        AnthemUpdate update = null;
        switch (command.substring(2, 3)) {
            case "M":
                update = AnthemUpdate.createPropertyUpdate(Thing.PROPERTY_MODEL_ID, value);
                break;
            case "R":
                update = AnthemUpdate.createPropertyUpdate(PROPERTY_REGION, value);
                break;
            case "S":
                update = AnthemUpdate.createPropertyUpdate(Thing.PROPERTY_FIRMWARE_VERSION, value);
                break;
            case "B":
                update = AnthemUpdate.createPropertyUpdate(PROPERTY_SOFTWARE_BUILD_DATE, value);
                break;
            case "H":
                update = AnthemUpdate.createPropertyUpdate(Thing.PROPERTY_HARDWARE_VERSION, value);
                break;
            case "N":
                update = AnthemUpdate.createPropertyUpdate(Thing.PROPERTY_MAC_ADDRESS, value);
                break;
            case "Q":
                // Ignore
                break;
            default:
                logger.debug("Unknown info type");
                break;
        }
        return update;
    }

    private @Nullable AnthemUpdate parseNumberOfAvailableInputsCommand(String command) {
        Matcher matcher = NUM_AVAILABLE_INPUTS_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                return AnthemUpdate.createPropertyUpdate(PROPERTY_NUM_AVAILABLE_INPUTS, matcher.group(1));
            } catch (IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
        return null;
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

    private void parseInputName(String command, @Nullable Matcher matcher, Map<String, String> map) {
        if (matcher != null) {
            try {
                matcher.find();
                String input = matcher.group(1);
                String inputName = matcher.group(2);
                map.putIfAbsent(input, inputName);
            } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
    }

    private @Nullable AnthemUpdate parsePower(String command) {
        Matcher mmatcher = POWER_PATTERN.matcher(command);
        if (mmatcher != null) {
            try {
                mmatcher.find();
                String zone = mmatcher.group(1);
                String power = mmatcher.group(2);
                return AnthemUpdate.createStateUpdate(zone, CHANNEL_POWER, OnOffType.from("1".equals(power)));
            } catch (IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
        return null;
    }

    private @Nullable AnthemUpdate parseVolume(String command) {
        Matcher matcher = VOLUME_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                String zone = matcher.group(1);
                String volume = matcher.group(2);
                return AnthemUpdate.createStateUpdate(zone, CHANNEL_VOLUME_DB, DecimalType.valueOf(volume));
            } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
        return null;
    }

    private @Nullable AnthemUpdate parseMute(String command) {
        Matcher matcher = MUTE_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                String zone = matcher.group(1);
                String mute = matcher.group(2);
                return AnthemUpdate.createStateUpdate(zone, CHANNEL_MUTE, OnOffType.from("1".equals(mute)));
            } catch (IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
        return null;
    }

    private @Nullable AnthemUpdate parseActiveInput(String command) {
        Matcher matcher = ACTIVE_INPUT_PATTERN.matcher(command);
        if (matcher != null) {
            try {
                matcher.find();
                String zone = matcher.group(1);
                DecimalType activeInput = DecimalType.valueOf(matcher.group(2));
                return AnthemUpdate.createStateUpdate(zone, CHANNEL_ACTIVE_INPUT, activeInput);
            } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException e) {
                logger.debug("Parsing exception on command: {}", command, e);
            }
        }
        return null;
    }
}
