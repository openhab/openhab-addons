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
package org.openhab.binding.max.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;

/**
 * The {@link: FMessage} contains information about the Cube NTP Configuration
 * This is the response to a f: command
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class FMessage extends Message {

    private String ntpServer1 = "";
    private String ntpServer2 = "";

    /**
     * The {@link: FMessage} contains information about the Cube NTP Configuration
     *
     * @param raw String with raw message
     */
    public FMessage(String raw) {
        super(raw);

        final String[] servers = this.getPayload().split(",");
        if (servers.length > 0) {
            ntpServer1 = servers[0];
        }
        if (servers.length > 1) {
            ntpServer2 = servers[1];
        }
    }

    /**
     * @return the NTP Server1 name
     */
    public String getNtpServer1() {
        return ntpServer1;
    }

    /**
     * @return the NTP Server2 name
     */
    public String getNtpServer2() {
        return ntpServer2;
    }

    @Override
    public void debug(Logger logger) {
        logger.debug("=== F Message === ");
        logger.trace("\tRAW : {}", this.getPayload());
        logger.debug("\tNTP Server1    : {}", this.ntpServer1);
        logger.debug("\tNTP Server2    : {}", this.ntpServer2);
    }

    @Override
    public MessageType getType() {
        return MessageType.F;
    }
}
