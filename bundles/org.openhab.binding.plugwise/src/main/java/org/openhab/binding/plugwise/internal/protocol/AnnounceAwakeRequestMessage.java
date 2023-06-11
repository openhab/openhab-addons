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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.ANNOUNCE_AWAKE_REQUEST;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * A sleeping end device (SED: Scan, Sense, Switch) sends this message to announce that is awake.
 *
 * @author Wouter Born - Initial contribution
 */
public class AnnounceAwakeRequestMessage extends Message {

    public enum AwakeReason {

        /** The SED joins the network for maintenance */
        MAINTENANCE(0),

        /** The SED joins a network for the first time */
        JOIN_NETWORK(1),

        /** The SED joins a network it has already joined, e.g. after reinserting a battery */
        REJOIN_NETWORK(2),

        /** When a SED switches a device group or when reporting values such as temperature/humidity */
        NORMAL(3),

        /** A human pressed the button on a SED to wake it up */
        WAKEUP_BUTTON(5);

        public static AwakeReason forValue(int value) {
            return Arrays.stream(values()).filter(awakeReason -> awakeReason.id == value).findFirst().get();
        }

        private final int id;

        AwakeReason(int id) {
            this.id = id;
        }
    }

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{2})");

    private AwakeReason awakeReason;

    public AnnounceAwakeRequestMessage(int sequenceNumber, String payload) {
        super(ANNOUNCE_AWAKE_REQUEST, sequenceNumber, payload);
    }

    public AwakeReason getAwakeReason() {
        return awakeReason;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            awakeReason = AwakeReason.forValue(Integer.parseInt(matcher.group(2)));
        } else {
            throw new PlugwisePayloadMismatchException(ANNOUNCE_AWAKE_REQUEST, PAYLOAD_PATTERN, payload);
        }
    }
}
