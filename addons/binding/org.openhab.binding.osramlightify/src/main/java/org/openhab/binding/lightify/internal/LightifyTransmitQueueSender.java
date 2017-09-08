/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal;

/**
 * @author Mike Jagdis - Initial contribution
 */
interface LightifyTransmitQueueSender<T extends Object> {

    /** Perform the actual sending of a message.
     *
     * @return true if the send was successful, false if the message cannot
     *     be sent and should be discarded.
     */
    boolean transmitQueueSender(T msg);
}
