/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.CLOCK_GET_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the current clock value of a device. This message is answered by a {@link ClockGetResponseMessage} which
 * contains the clock value.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class ClockGetRequestMessage extends Message {

    public ClockGetRequestMessage(MACAddress macAddress) {
        super(CLOCK_GET_REQUEST, macAddress);
    }

}
