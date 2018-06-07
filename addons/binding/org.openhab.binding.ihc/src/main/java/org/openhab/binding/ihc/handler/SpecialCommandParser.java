/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.openhab.binding.ihc.handler.SpecialCommand.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SpecialCommandParser {
    private final Logger logger = LoggerFactory.getLogger(SpecialCommandParser.class);

    private static final Pattern CONFIG_PATTERN = Pattern.compile("(<|>)\\((.*?)\\)(|$)");

    private String commandline;

    public SpecialCommandParser(String commandline) throws IllegalArgumentException {
        this.commandline = commandline;
        if (!CONFIG_PATTERN.matcher(commandline).matches()) {
            throw new IllegalArgumentException(String.format("Illegal special commmand '%s'", commandline));
        }
    }

    public List<SpecialCommand> getAllInCommands() {
        return getAllCommands().stream().filter(i -> i.getDirection() == Direction.IN).collect(Collectors.toList());
    }

    public List<SpecialCommand> getAllOutCommands() {
        return getAllCommands().stream().filter(i -> i.getDirection() == Direction.OUT).collect(Collectors.toList());
    }

    public List<SpecialCommand> getAllCommands() {
        List<SpecialCommand> list = new ArrayList<SpecialCommand>();
        try {
            Matcher matcher = CONFIG_PATTERN.matcher(commandline);
            while (matcher.find()) {
                String specialCommandStr = matcher.group().trim();
                logger.debug("Match: {}", specialCommandStr);
                SpecialCommand specialCommand = new SpecialCommand(specialCommandStr);
                list.add(specialCommand);
            }
        } catch (PatternSyntaxException ex) {
            // Should not happen as syntax is already checked
        }
        return list;
    }
}
