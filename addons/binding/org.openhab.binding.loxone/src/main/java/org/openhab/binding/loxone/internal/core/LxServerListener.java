/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

/**
 * Interface to get notifications about {@link LxServer} asynchronous events.
 * These events are triggered by messages received from Miniserver over websocket connection or the state of the
 * connection to the Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public interface LxServerListener {
    /**
     * Called by {@link LxServer} thread when new configuration is received from Loxone Miniserver and stored in
     * {@link LxServer} object.
     *
     * @param server
     *            server object that can be queried for new configuration of the Miniserver
     *
     */
    void onNewConfig(LxServer server);

    /**
     * Called by {@link LxServer} thread when Loxone Miniserver goes online and communication channel is established and
     * ready
     * to send commands and
     * receive state updates.
     */
    void onServerGoesOnline();

    /**
     * Called by {@link LxServer} thread when Loxone Miniserver goes offline and communication channel is broken.
     *
     * @param reason
     *            reason for going offline
     * @param details
     *            details describing the disconnection reason
     */
    void onServerGoesOffline(LxOfflineReason reason, String details);

    /**
     * Called by {@link LxServer} thread when a state of a control is updated on the Loxone Miniserver
     *
     * @param control
     *            control object, which state changed
     * @param stateName
     *            name of the state that was updated
     */
    void onControlStateUpdate(LxControl control, String stateName);

}
