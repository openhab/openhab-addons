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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.DEVICE_ROLE_CALL_RESPONSE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * <p>
 * The Circle+ sends this message as response to a {@link RoleCallRequestMessage}. It contains the MAC address for the
 * the node identified by the node ID in the request message. When no node is known with given ID, the MAC
 * address will be empty.
 * </p>
 * <p>
 * The MAC address can belong to a relay device (Circle, Stealth) as well as a sleeping end device (SED: Scan, Sense,
 * Switch). An {@link InformationRequestMessage} can be used to determine the actual device type (when it is online).
 * </p>
 * <p>
 * The Circle+ MAC address can not be retrieved from the node list. The Circle+ MAC address can be retrieved with a
 * {@link NetworkStatusRequestMessage}.
 * </p>
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class RoleCallResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{16})(\\w{2})");
    private static final String EMPTY_MAC_ADDRESS = "FFFFFFFFFFFFFFFF";

    private int nodeID;
    private MACAddress nodeMAC;

    public RoleCallResponseMessage(int sequenceNumber, String payload) {
        super(DEVICE_ROLE_CALL_RESPONSE, sequenceNumber, payload);
    }

    public int getNodeID() {
        return nodeID;
    }

    public MACAddress getNodeMAC() {
        return nodeMAC;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            nodeMAC = matcher.group(2).equals(EMPTY_MAC_ADDRESS) ? null : new MACAddress(matcher.group(2));
            nodeID = (Integer.parseInt(matcher.group(3), 16));
        } else {
            throw new PlugwisePayloadMismatchException(DEVICE_ROLE_CALL_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }
}
