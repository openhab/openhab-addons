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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.SENSE_REPORT_REQUEST;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.Humidity;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.Temperature;

/**
 * A Sense periodically sends this message for updating the current temperature and humidity.
 *
 * @author Wouter Born - Initial contribution
 */
public class SenseReportRequestMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{4})(\\w{4})");

    private Humidity humidity;
    private Temperature temperature;

    public SenseReportRequestMessage(int sequenceNumber, String payload) {
        super(SENSE_REPORT_REQUEST, sequenceNumber, payload);
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            humidity = new Humidity(matcher.group(2));
            temperature = new Temperature(matcher.group(3));
        } else {
            throw new PlugwisePayloadMismatchException(SENSE_REPORT_REQUEST, PAYLOAD_PATTERN, payload);
        }
    }
}
