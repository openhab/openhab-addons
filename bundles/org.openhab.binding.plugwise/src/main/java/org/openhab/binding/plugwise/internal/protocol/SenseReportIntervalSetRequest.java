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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.SENSE_REPORT_INTERVAL_SET_REQUEST;

import java.time.Duration;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Sets the Sense temperature and humidity measurement report interval. Based on this interval, periodically a
 * {@link SenseReportRequestMessage} is sent.
 *
 * @author Wouter Born - Initial contribution
 */
public class SenseReportIntervalSetRequest extends Message {

    private Duration reportInterval;

    public SenseReportIntervalSetRequest(MACAddress macAddress, Duration reportInterval) {
        super(SENSE_REPORT_INTERVAL_SET_REQUEST, macAddress);
        this.reportInterval = reportInterval;
    }

    @Override
    protected String payloadToHexString() {
        return String.format("%02X", reportInterval.toMinutes());
    }
}
