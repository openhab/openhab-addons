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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.NODE_AVAILABLE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Node available messages are broadcasted by nodes that are not yet part of a network. They are currently unused
 * because typically the network is configured using the Plugwise Source software, and never changed after.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class NodeAvailableMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})");

    public NodeAvailableMessage(int sequenceNumber, String payload) {
        super(NODE_AVAILABLE, sequenceNumber, payload);
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
        } else {
            throw new PlugwisePayloadMismatchException(NODE_AVAILABLE, PAYLOAD_PATTERN, payload);
        }
    }
}
