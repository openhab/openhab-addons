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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.SCAN_PARAMETERS_SET_REQUEST;

import java.time.Duration;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.Sensitivity;

/**
 * Sets the Scan motion detection parameters. These parameters control when the Scan sends on/off commands.
 *
 * @author Wouter Born - Initial contribution
 */
public class ScanParametersSetRequestMessage extends Message {

    private Sensitivity sensitivity;
    private boolean daylightOverride;
    private Duration switchOffDelay;

    public ScanParametersSetRequestMessage(MACAddress macAddress, Sensitivity sensitivity, boolean daylightOverride,
            Duration switchOffDelay) {
        super(SCAN_PARAMETERS_SET_REQUEST, macAddress);
        this.sensitivity = sensitivity;
        this.daylightOverride = daylightOverride;
        this.switchOffDelay = switchOffDelay;
    }

    @Override
    protected String payloadToHexString() {
        String sensitivityHex = String.format("%02X", sensitivity.toInt());
        String daylightOverrideHex = (daylightOverride ? "01" : "00");
        String switchOffDelayHex = String.format("%02X", switchOffDelay.toMinutes());
        return sensitivityHex + daylightOverrideHex + switchOffDelayHex;
    }
}
