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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.PING_RESPONSE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Contains network diagnostic information. This message is the response of a {@link PingRequestMessage}.
 *
 * @author Wouter Born - Initial contribution
 */
public class PingResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{2})(\\w{2})(\\w{4})");

    private int inRSSI;
    private int outRSSI;
    private int pingMillis;

    public PingResponseMessage(int sequenceNumber, String payload) {
        super(PING_RESPONSE, sequenceNumber, payload);
    }

    public int getInRSSI() {
        return inRSSI;
    }

    public int getOutRSSI() {
        return outRSSI;
    }

    public int getPingMillis() {
        return pingMillis;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            inRSSI = (Integer.parseInt(matcher.group(2), 16));
            outRSSI = (Integer.parseInt(matcher.group(3), 16));
            pingMillis = (Integer.parseInt(matcher.group(4), 16));
        } else {
            throw new PlugwisePayloadMismatchException(PING_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }
}
