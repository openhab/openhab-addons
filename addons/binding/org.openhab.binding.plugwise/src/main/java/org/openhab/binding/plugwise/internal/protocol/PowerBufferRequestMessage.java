/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_BUFFER_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the historical pulse measurements at a certain log address from a device (Circle, Circle+, Stealth). This
 * message is answered by a {@link PowerBufferResponseMessage}.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
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
