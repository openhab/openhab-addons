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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandGetstate} class implements the GlobalCache getstate command, which retrieves the
 * current state of the contact closure on the device.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class CommandGetstate extends AbstractCommand {

    private final Logger logger = LoggerFactory.getLogger(CommandGetstate.class);

    private OnOffType state;

    public CommandGetstate(Thing thing, LinkedBlockingQueue<RequestMessage> requestQueue, String mod, String con) {
        super(thing, requestQueue, "getstate", CommandType.COMMAND);
        deviceCommand = "getstate," + mod + ":" + con;
    }

    @Override
    public void parseSuccessfulReply() {
        if (deviceReply == null) {
            return;
        }

        // decode response of form state,1:3,0
        Pattern p = Pattern.compile("state,\\d:\\d,[01]");
        Matcher m = p.matcher(deviceReply);
        if (!m.matches()) {
            logger.warn("Successful reply from device can't be matched: {}", deviceReply);
            state = OnOffType.OFF;
            return;
        }

        setModule(deviceReply.substring(6, 7));
        setConnector(deviceReply.substring(8, 9));
        setState(OnOffType.from(deviceReply.charAt(10) != '0'));
    }

    private void setState(OnOffType s) {
        state = s;
    }

    public OnOffType state() {
        return state;
    }

    @Override
    public void logSuccess() {
        logger.debug("Execute '{}' succeeded on thing {} at {}, state={}", commandName, thing.getUID().getId(),
                ipAddress, state);
    }

    @Override
    public void logFailure() {
        logger.error("Execute '{}' failed on thing {} at {}: errorCode={}, errorMessage={}", commandName,
                thing.getUID().getId(), ipAddress, errorCode, errorMessage);
    }
}
