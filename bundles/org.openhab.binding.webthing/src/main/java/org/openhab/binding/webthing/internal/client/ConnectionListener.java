/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;


/**
 * Connection listener that will be notified, if the connection state is changed
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public interface ConnectionListener {

    /**
     * "empty" instance
     */
    ConnectionListener EMPTY = new ConnectionListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected(String reason) {

        }
    };

    /**
     * callback that will be called, if the WebThing is connected
     *
     */
    void onConnected();

    /**
     * callback that will be called, if the stream is disconnected
     *
     * @param reason the reason
     */
    void onDisconnected(String reason);
}
