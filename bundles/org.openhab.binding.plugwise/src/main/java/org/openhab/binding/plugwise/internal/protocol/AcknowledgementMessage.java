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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.MessageType;

/**
 * Acknowledgement message class - ACKs are used in the Plugwise protocol to serve different means, from acknowledging a
 * message sent to the Stick by the host, as well as confirmation messages from nodes in the network for various
 * purposes. Not all purposes are yet reverse-engineered.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class AcknowledgementMessage extends Message {

    public enum ExtensionCode {
        NOT_EXTENDED(0),
        SENSE_INTERVAL_SET_ACK(179),
        SENSE_INTERVAL_SET_NACK(180),
        SENSE_BOUNDARIES_SET_ACK(181),
        SENSE_BOUNDARIES_SET_NACK(182),
        LIGHT_CALIBRATION_ACK(189),
        SCAN_PARAMETERS_SET_ACK(190),
        SCAN_PARAMETERS_SET_NACK(191),
        SUCCESS(193),
        ERROR(194),
        CIRCLE_PLUS(221),
        CLOCK_SET_ACK(215),
        ON_ACK(216),
        POWER_CALIBRATION_ACK(218),
        OFF_ACK(222),
        REAL_TIME_CLOCK_SET_ACK(223),
        TIMEOUT(225),
        ON_OFF_NACK(226),
        REAL_TIME_CLOCK_SET_NACK(231),
        SLEEP_SET_ACK(246),
        POWER_LOG_INTERVAL_SET_ACK(248),
        UNKNOWN(999);

        private static final Map<Integer, ExtensionCode> TYPES_BY_VALUE = new HashMap<>();

        static {
            for (ExtensionCode type : ExtensionCode.values()) {
                TYPES_BY_VALUE.put(type.identifier, type);
            }
        }

        public static ExtensionCode forValue(int value) {
            return TYPES_BY_VALUE.get(value);
        }

        private int identifier;

        private ExtensionCode(int value) {
            identifier = value;
        }

        public int toInt() {
            return identifier;
        }
    }

    private static final Pattern V1_SHORT_PAYLOAD_PATTERN = Pattern.compile("(\\w{4})");
    private static final Pattern V1_EXTENDED_PAYLOAD_PATTERN = Pattern.compile("(\\w{4})(\\w{16})");
    private static final Pattern V2_EXTENDED_PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{4})");

    private ExtensionCode code;

    public AcknowledgementMessage(MessageType messageType, int sequenceNumber, String payload) {
        super(messageType, sequenceNumber, payload);
    }

    public ExtensionCode getExtensionCode() {
        if (isExtended()) {
            return code;
        } else {
            return ExtensionCode.NOT_EXTENDED;
        }
    }

    @Override
    public String getPayload() {
        return payloadToHexString();
    }

    public boolean isError() {
        return code == ExtensionCode.ERROR;
    }

    public boolean isExtended() {
        return code != ExtensionCode.NOT_EXTENDED && code != ExtensionCode.SUCCESS && code != ExtensionCode.ERROR;
    }

    public boolean isSuccess() {
        return code == ExtensionCode.SUCCESS;
    }

    public boolean isTimeOut() {
        return code == ExtensionCode.TIMEOUT;
    }

    @Override
    protected void parsePayload() {
        if (getType() == ACKNOWLEDGEMENT_V1) {
            parseV1Payload();
        } else if (getType() == ACKNOWLEDGEMENT_V2) {
            parseV2Payload();
        }
    }

    private void parseV1Payload() {
        Matcher shortMatcher = V1_SHORT_PAYLOAD_PATTERN.matcher(payload);
        Matcher extendedMatcher = V1_EXTENDED_PAYLOAD_PATTERN.matcher(payload);

        if (extendedMatcher.matches()) {
            code = ExtensionCode.forValue(Integer.parseInt(extendedMatcher.group(1), 16));
            if (code == null) {
                code = ExtensionCode.UNKNOWN;
            }
            macAddress = new MACAddress(extendedMatcher.group(2));
        } else if (shortMatcher.matches()) {
            code = ExtensionCode.forValue(Integer.parseInt(shortMatcher.group(1), 16));
            if (code == null) {
                code = ExtensionCode.UNKNOWN;
            }
        } else {
            code = ExtensionCode.UNKNOWN;
            throw new PlugwisePayloadMismatchException(ACKNOWLEDGEMENT_V1, V1_SHORT_PAYLOAD_PATTERN,
                    V1_EXTENDED_PAYLOAD_PATTERN, payload);
        }
    }

    private void parseV2Payload() {
        Matcher matcher = V2_EXTENDED_PAYLOAD_PATTERN.matcher(payload);

        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            code = ExtensionCode.forValue(Integer.parseInt(matcher.group(2), 16));
            if (code == null) {
                code = ExtensionCode.UNKNOWN;
            }
        } else {
            code = ExtensionCode.UNKNOWN;
            throw new PlugwisePayloadMismatchException(ACKNOWLEDGEMENT_V2, V2_EXTENDED_PAYLOAD_PATTERN, payload);
        }
    }
}
