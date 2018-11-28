/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.netutils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Informs about the websocket connection.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface WebSocketConnectionListener {
    /**
     * An error occurred during connection or while connecting.
     *
     * @param e The error
     */
    void connectionError(Throwable e);

    /**
     * Connection successfully established.
     */
    void connectionEstablished();

    /**
     * Connection lost. A reconnect timer has been started.
     *
     * @param reason A reason for the disconnection
     */
    void connectionLost(String reason);
}
