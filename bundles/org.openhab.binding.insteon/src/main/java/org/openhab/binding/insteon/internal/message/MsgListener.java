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
package org.openhab.binding.insteon.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface to receive Insteon messages from the modem.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public interface MsgListener {
    /**
     * Invoked whenever a valid message comes in from the modem
     *
     * @param msg the message received
     */
    void msg(Msg msg);
}
