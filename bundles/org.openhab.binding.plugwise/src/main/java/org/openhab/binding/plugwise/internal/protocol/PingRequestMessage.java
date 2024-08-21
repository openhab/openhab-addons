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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.PING_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests a {@link PingResponseMessage} from a device.
 *
 * @author Wouter Born - Initial contribution
 */
public class PingRequestMessage extends Message {

    public PingRequestMessage(MACAddress macAddress) {
        super(PING_REQUEST, macAddress);
    }
}
