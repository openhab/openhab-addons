/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.NETWORK_RESET_REQUEST;

/**
 * Requests the Plugwise network to be reset. Currently not used in the binding.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class NetworkResetRequestMessage extends Message {

    public NetworkResetRequestMessage(String payload) {
        super(NETWORK_RESET_REQUEST, payload);
    }

}
