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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.NETWORK_STATUS_RESPONSE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Contains the current network status as well as the MAC address of the Circle+ that coordinates the network. The Stick
 * sends this message as response of a {@link NetworkStatusRequestMessage}.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class NetworkStatusResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern
            .compile("(\\w{16})(\\w{2})(\\w{2})(\\w{16})(\\w{4})(\\w{2})");

    private boolean online;
    private String networkID;
    private String unknown1;
    private String unknown2;
    private String shortNetworkID;

    private MACAddress circlePlusMAC;

    public NetworkStatusResponseMessage(int sequenceNumber, String payload) {
        super(NETWORK_STATUS_RESPONSE, sequenceNumber, payload);
    }

    public NetworkStatusResponseMessage(String payload) {
        super(NETWORK_STATUS_RESPONSE, payload);
    }

    public MACAddress getCirclePlusMAC() {
        return circlePlusMAC;
    }

    public String getNetworkID() {
        return networkID;
    }

    public String getShortNetworkID() {
        return shortNetworkID;
    }

    public String getUnknown1() {
        return unknown1;
    }

    public String getUnknown2() {
        return unknown2;
    }

    public boolean isOnline() {
        return online;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            unknown1 = matcher.group(2);
            online = (Integer.parseInt(matcher.group(3), 16) == 1);
            networkID = matcher.group(4);
            shortNetworkID = matcher.group(5);
            unknown2 = matcher.group(6);

            // now some serious protocol reverse-engineering assumption. Circle+ MAC = networkID with first two bytes
            // replaced by 00
            circlePlusMAC = new MACAddress("00" + networkID.substring(2));
        } else {
            throw new PlugwisePayloadMismatchException(NETWORK_STATUS_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }

    @Override
    protected String payloadToHexString() {
        return unknown1 + String.format("%02X", online ? 1 : 0) + networkID + shortNetworkID + unknown2;
    }
}
