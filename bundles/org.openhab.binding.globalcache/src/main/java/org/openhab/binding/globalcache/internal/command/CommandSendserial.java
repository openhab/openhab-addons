/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants.CommandType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandSendserial} class sends a serial command string to the device.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class CommandSendserial extends AbstractCommand {
    private final Logger logger = LoggerFactory.getLogger(CommandSendserial.class);

    private Command command;

    public CommandSendserial(Thing thing, Command command, LinkedBlockingQueue<RequestMessage> queue, String mod,
            String con, String code) {
        super(thing, queue, "sendserial", CommandType.SERIAL1);
        // Check to see if this is for the second serial port on a GC-100-12
        if (isGC100Model12() && mod.equals("2")) {
            setCommandType(CommandType.SERIAL2);
        }
        this.command = command;
        this.deviceCommand = code;
    }

    @Override
    public void parseSuccessfulReply() {
        if (deviceReply == null) {
            return;
        }
        // decode response
    }

    @Override
    public void logSuccess() {
        logger.debug("Execute '{}' succeeded for command {} on thing {} at {}", commandName, command.toString(),
                thing.getUID().getId(), ipAddress);
    }

    @Override
    public void logFailure() {
        logger.error("Execute '{}' failed on thing {} at {}: errorCode={}, errorMessage={}", commandName,
                thing.getUID().getId(), ipAddress, errorCode, errorMessage);
    }
}
