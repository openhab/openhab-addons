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
import org.openhab.binding.globalcache.GlobalCacheBindingConstants.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandGetversion} class implements the GlobalCache getversion command, which retrieves software
 * version of the device.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class CommandGetversion extends AbstractCommand {

    private final Logger logger = LoggerFactory.getLogger(CommandGetversion.class);

    private String version;

    public CommandGetversion(Thing thing, LinkedBlockingQueue<RequestMessage> requestQueue) {
        super(thing, requestQueue, "getversion", CommandType.COMMAND);
        deviceCommand = "getversion";
    }

    @Override
    public void parseSuccessfulReply() {
        if (deviceReply == null) {
            return;
        }
        // decode response of form <textversionstring>
        version = deviceReply;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void logSuccess() {
        logger.debug("Execute '{}' succeeded on thing {} at {}, version={}", commandName, thing.getUID().getId(),
                ipAddress, version);
    }

    @Override
    public void logFailure() {
        logger.error("Execute '{}' failed for thing {} at {}, errorCode={}, errorMessage={}", commandName,
                thing.getUID().getId(), ipAddress, errorCode, errorMessage);
    }
}
