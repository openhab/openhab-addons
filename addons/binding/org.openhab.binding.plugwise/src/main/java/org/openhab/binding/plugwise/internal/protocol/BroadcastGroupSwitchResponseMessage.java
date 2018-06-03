/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.BROADCAST_GROUP_SWITCH_RESPONSE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * A sleeping end device (SED: Scan, Sense, Switch) sends this message to switch groups on/off when the configured
 * switching conditions have been met.
 *
 * @author Wouter Born - Initial contribution
 */
public class BroadcastGroupSwitchResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{2})(\\w{2})");

    private int portMask;
    private boolean powerState;

    public BroadcastGroupSwitchResponseMessage(int sequenceNumber, String payload) {
        super(BROADCAST_GROUP_SWITCH_RESPONSE, sequenceNumber, payload);
    }

    public int getPortMask() {
        return portMask;
    }

    public boolean getPowerState() {
        return powerState;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            portMask = Integer.parseInt(matcher.group(2));
            powerState = (matcher.group(3).equals("01"));
        } else {
            throw new PlugwisePayloadMismatchException(BROADCAST_GROUP_SWITCH_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }

}
