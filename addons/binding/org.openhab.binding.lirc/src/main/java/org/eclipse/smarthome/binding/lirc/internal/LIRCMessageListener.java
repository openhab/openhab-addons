/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.lirc.internal;

import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCButtonEvent;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCResponse;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * Interface for listeners to receive messages from LIRC server
 *
 * @author Andrew Nagle
 */
public interface LIRCMessageListener {

    /**
     * This method is called whenever the message is received from the bridge.
     *
     * @param bridge
     *            The LIRC bridge where message is received.
     * @param message
     *            The message which received.
     */
    void onMessageReceived(ThingUID bridge, LIRCResponse message);

    /**
     * This method is called whenever a button is pressed on a remote.
     *
     * @param bridge
     *            The LIRC bridge where message is received.
     * @param buttonEvent
     *            Button event details
     */
    void onButtonPressed(ThingUID bridge, LIRCButtonEvent buttonEvent);
}
