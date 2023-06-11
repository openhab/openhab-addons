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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants.CommandType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandSetserial} class implements the GlobalCache set_SERIAL command, which sets the serial
 * port parameters (baud, flow control, and parity) on the device.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class CommandSetserial extends AbstractCommand {

    private final Logger logger = LoggerFactory.getLogger(CommandSetserial.class);

    private String baud;
    private String flow;
    private String parity;

    public CommandSetserial(Thing thing, LinkedBlockingQueue<RequestMessage> requestQueue, String mod, String con,
            String baud, String flowcontrol, String parity) {
        super(thing, requestQueue, "set_SERIAL", CommandType.COMMAND);

        deviceCommand = "set_SERIAL," + mod + ":" + con + "," + baud + "," + flowcontrol + "," + parity;
    }

    @Override
    public void parseSuccessfulReply() {
        if (deviceReply == null) {
            return;
        }

        // decode response of form SERIAL,1:1,<baudrate>,<flowcontrol>,<parity
        Pattern p = Pattern.compile("SERIAL,\\d:\\d,\\S+,\\S+,\\S+");
        Matcher m = p.matcher(deviceReply);
        if (!m.matches()) {
            return;
        }

        String fields[] = deviceReply.split(",");
        if (fields.length != 5) {
            return;
        }

        setModule(fields[1].substring(0, 1));
        setConnector(fields[1].substring(2));
        baud = fields[2];
        flow = fields[3];
        parity = fields[4];
    }

    public String getBaud() {
        return baud;
    }

    public String getFlowcontrol() {
        return flow;
    }

    public String getParity() {
        return parity;
    }

    @Override
    public void logSuccess() {
        logger.debug("Execute '{}' succeeded on thing {} at {}", commandName, thing.getUID().getId(), ipAddress);
    }

    @Override
    public void logFailure() {
        logger.error("Execute '{}' failed on thing {} at: ipAdress={}, errorCode={}, errorMessage={}", commandName,
                thing.getUID().getId(), ipAddress, errorCode, errorMessage);
    }
}
