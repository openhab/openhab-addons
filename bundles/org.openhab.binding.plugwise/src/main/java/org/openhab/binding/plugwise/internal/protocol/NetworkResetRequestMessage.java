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

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.NETWORK_RESET_REQUEST;

/**
 * Requests the Plugwise network to be reset. Currently not used in the binding.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class NetworkResetRequestMessage extends Message {

    public NetworkResetRequestMessage(String payload) {
        super(NETWORK_RESET_REQUEST, payload);
    }
}
