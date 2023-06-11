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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.CLOCK_GET_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the current clock value of a device. This message is answered by a {@link ClockGetResponseMessage} which
 * contains the clock value.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class ClockGetRequestMessage extends Message {

    public ClockGetRequestMessage(MACAddress macAddress) {
        super(CLOCK_GET_REQUEST, macAddress);
    }
}
