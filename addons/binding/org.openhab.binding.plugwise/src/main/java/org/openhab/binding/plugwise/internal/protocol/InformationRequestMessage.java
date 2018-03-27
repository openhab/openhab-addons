/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.DEVICE_INFORMATION_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests generic device information. This message is answered by an {@link InformationResponseMessage} which contains
 * the device information.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class InformationRequestMessage extends Message {

    public InformationRequestMessage(MACAddress macAddress) {
        super(DEVICE_INFORMATION_REQUEST, macAddress);
    }

}
