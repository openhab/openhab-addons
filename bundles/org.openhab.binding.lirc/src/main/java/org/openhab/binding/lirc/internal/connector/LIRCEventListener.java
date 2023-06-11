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
package org.openhab.binding.lirc.internal.connector;

import org.openhab.binding.lirc.internal.messages.LIRCButtonEvent;
import org.openhab.binding.lirc.internal.messages.LIRCResponse;

/**
 * Defines an interface to receive messages from the LIRC server
 *
 * @author Andrew Nagle
 */
public interface LIRCEventListener {

    /**
     * Procedure to receive messages from the LIRC server
     *
     * @param reponse
     *            Message received
     */
    void messageReceived(LIRCResponse message);

    /**
     * Procedure for receiving notification of button presses
     *
     * @param buttonEvent
     *            Button press event details
     */
    void buttonPressed(LIRCButtonEvent buttonEvent);

    /**
     * Procedure for receiving information about fatal errors.
     *
     * @param error
     *            Error occured.
     */
    void errorOccured(String error);
}
