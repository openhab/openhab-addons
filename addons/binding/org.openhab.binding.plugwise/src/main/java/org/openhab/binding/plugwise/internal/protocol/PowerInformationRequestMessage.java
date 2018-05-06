/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_INFORMATION_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Request real-time energy consumption from a relay device (Circle, Circle+, Stealth). This
 * message is answered by a {@link PowerInformationResponseMessage}.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PowerInformationRequestMessage extends Message {

    public PowerInformationRequestMessage(MACAddress macAddress) {
        super(POWER_INFORMATION_REQUEST, macAddress);
    }

}
