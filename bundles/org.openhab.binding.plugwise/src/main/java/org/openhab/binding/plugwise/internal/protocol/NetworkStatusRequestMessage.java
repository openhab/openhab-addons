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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.NETWORK_STATUS_REQUEST;

/**
 * Requests the network status from the Stick. This message is answered by a {@link NetworkStatusResponseMessage}.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class NetworkStatusRequestMessage extends Message {

    public NetworkStatusRequestMessage() {
        super(NETWORK_STATUS_REQUEST);
    }
}
