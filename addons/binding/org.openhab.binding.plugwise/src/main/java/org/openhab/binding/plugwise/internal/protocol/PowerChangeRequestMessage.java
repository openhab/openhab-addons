/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_CHANGE_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the power state of a relay device (Circle, Circle+, Stealth) to be switched on/off. The current power state
 * of a device is retrieved by sending a {@link InformationRequestMessage} and reading the
 * {@link InformationResponseMessage#getPowerState()} value.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PowerChangeRequestMessage extends Message {

    public PowerChangeRequestMessage(MACAddress macAddress, boolean powerState) {
        super(POWER_CHANGE_REQUEST, macAddress, powerState ? "01" : "00");
    }

}
