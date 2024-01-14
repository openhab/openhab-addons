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
package org.openhab.binding.globalcache.internal.command;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants.CommandType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandSetstate} class implements the GlobalCache setstate command for devices that support contact
 * closure.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class CommandSetstate extends AbstractCommand {

    private final Logger logger = LoggerFactory.getLogger(CommandSetstate.class);

    private Command command;
    private OnOffType state;

    public CommandSetstate(Thing thing, Command command, LinkedBlockingQueue<RequestMessage> queue, String mod,
            String con) {
        super(thing, queue, "setstate", CommandType.COMMAND);

        this.command = command;
        if (command instanceof OnOffType) {
            deviceCommand = "setstate," + mod + ":" + con + "," + (command.equals(OnOffType.ON) ? "1" : "0");
        }
    }

    @Override
    public void parseSuccessfulReply() {
        if (deviceReply == null) {
            return;
        }

        // Match on response of form setstate,1:3,0 or state,1:3,0
        if (!matchSetstate()) {
            logger.warn("Successful reply from device can't be matched: {}", deviceReply);
            setState(OnOffType.OFF);
            return;
        }
    }

    private boolean matchSetstate() {
        // Matches both iTach and GC-100 responses
        Pattern p = Pattern.compile("(setstate|state),(\\d):(\\d),([01])");
        Matcher m = p.matcher(deviceReply);
        if (m.matches()) {
            logger.trace("Matched setstate response: g2={}, g3={}, g4={}", m.group(2), m.group(3), m.group(4));
            if (m.groupCount() == 4) {
                setModule(m.group(2));
                setConnector(m.group(3));
                setState(OnOffType.from(!"0".equals(m.group(4))));
                return true;
            }
        }
        return false;
    }

    private void setState(OnOffType s) {
        state = s;
    }

    public OnOffType state() {
        return state;
    }

    @Override
    public void logSuccess() {
        logger.debug("Execute '{}' succeeded for command {} on thing {} at {}", commandName, command,
                thing.getUID().getId(), ipAddress);
    }

    @Override
    public void logFailure() {
        logger.error("Execute '{}' failed on thing {} at {}: errorCode={}, errorMessage={}", commandName,
                thing.getUID().getId(), ipAddress, errorCode, errorMessage);
    }
}
