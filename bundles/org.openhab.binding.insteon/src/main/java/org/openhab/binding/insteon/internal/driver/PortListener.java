/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.driver;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.message.Msg;

/**
 * Interface for classes that want to listen to notifications from the driver port
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public interface PortListener {
    /**
     * Notifies that the port has disconnected
     */
    public abstract void disconnected();

    /**
     * Notifies that the port has received a message
     *
     * @param msg the message received
     */
    public abstract void messageReceived(Msg msg);

    /**
     * Notifies that the port has sent a message
     *
     * @param msg the message sent
     */
    public abstract void messageSent(Msg msg);
}
