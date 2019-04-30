/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
interface LightifyTransmitQueueSender<T extends Object> {

    /** Perform the actual sending of a message.
     *
     * @return true if the send was successful, false if the message cannot
     *     be sent and should be discarded.
     */
    boolean transmitQueueSender(T msg);
}
