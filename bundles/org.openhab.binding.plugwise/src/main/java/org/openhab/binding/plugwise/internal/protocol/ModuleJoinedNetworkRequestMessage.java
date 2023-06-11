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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.MODULE_JOINED_NETWORK_REQUEST;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Module joined network request. Sent when a SED (re)joins the network. E.g. when you reinsert the battery of a Scan.
 *
 * @author Wouter Born - Initial contribution
 */
public class ModuleJoinedNetworkRequestMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})");

    public ModuleJoinedNetworkRequestMessage(int sequenceNumber, String payload) {
        super(MODULE_JOINED_NETWORK_REQUEST, sequenceNumber, payload);
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
        } else {
            throw new PlugwisePayloadMismatchException(MODULE_JOINED_NETWORK_REQUEST, PAYLOAD_PATTERN, payload);
        }
    }
}
