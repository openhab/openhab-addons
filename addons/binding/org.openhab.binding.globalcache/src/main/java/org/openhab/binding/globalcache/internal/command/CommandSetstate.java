/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.globalcache.internal.command;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.globalcache.GlobalCacheBindingConstants.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * The {@link CommandSetstate} class implements the GlobalCache setstate command for devices that support contact closure.
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

        // decode response of form setstate,1:3,0
        Pattern p = Pattern.compile("setstate,\\d:\\d,[01]");
        Matcher m = p.matcher(deviceReply);
        if (!m.matches()) {
            logger.warn("Successful reply from device can't be matched: {}", deviceReply);
            setState(OnOffType.OFF);
            return;
        }

        setModule(deviceReply.substring(9, 10));
        setConnector(deviceReply.substring(11, 12));
        setState((deviceReply.charAt(13) == '0' ? OnOffType.OFF : OnOffType.ON));
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
