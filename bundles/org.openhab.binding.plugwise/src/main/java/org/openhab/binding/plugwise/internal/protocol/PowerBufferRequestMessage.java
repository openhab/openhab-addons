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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_BUFFER_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the historical pulse measurements at a certain log address from a device (Circle, Circle+, Stealth). This
 * message is answered by a {@link PowerBufferResponseMessage}.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class PowerBufferRequestMessage extends Message {

    private int logAddress;

    public PowerBufferRequestMessage(MACAddress macAddress, int logAddress) {
        super(POWER_BUFFER_REQUEST, macAddress);
        this.logAddress = logAddress;
    }

    @Override
    protected String payloadToHexString() {
        return String.format("%08X", (logAddress * 32 + 278528));
    }
}
