/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.globalcache.internal.command;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.globalcache.GlobalCacheBindingConstants.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandSendir} class implements the GlobalCache sendir command, which sends
 * an infrared command to the device.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class CommandSendir extends AbstractCommand {

    private final Logger logger = LoggerFactory.getLogger(CommandSendir.class);

    private String rcvCounter;
    Command command;

    public CommandSendir(Thing thing, Command command, LinkedBlockingQueue<RequestMessage> queue, String mod,
            String con, String code, int sendCounter) {
        super(thing, queue, "sendir", CommandType.COMMAND);

        this.command = command;
        deviceCommand = "sendir," + mod + ":" + con + "," + String.valueOf(sendCounter) + "," + code;
    }

    @Override
    public void parseSuccessfulReply() {
        if (deviceReply == null) {
            return;
        }

        // decode response of form completeir,1:1,<ID>
        if (deviceReply.startsWith("completeir")) {
            setModule(deviceReply.substring(11, 12));
            setConnector(deviceReply.substring(13, 14));
            setRcvCounter(deviceReply.substring(15));
        }
    }

    private void setRcvCounter(String c) {
        rcvCounter = c;
    }

    public String getRcvCounter() {
        return rcvCounter;
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
