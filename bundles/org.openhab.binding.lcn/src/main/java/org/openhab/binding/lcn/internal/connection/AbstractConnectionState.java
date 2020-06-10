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
package org.openhab.binding.lcn.internal.connection;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnDefs;

/**
 * Base class for representing LCN-PCK gateway connection states
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractConnectionState extends AbstractState {
    /** The PCK gateway's Connection */
    protected final Connection connection;
    /** An openHAB scheduler */
    protected final ScheduledExecutorService scheduler;

    public AbstractConnectionState(StateContext context, ScheduledExecutorService scheduler) {
        super(context);
        this.connection = context.getConnection();
        this.scheduler = scheduler;
    }

    /**
     * Callback method when a PCK message has been received.
     *
     * @param data the received PCK message without line termination character
     */
    public abstract void onPckMessageReceived(String data);

    /**
     * Enqueues a PCK message to be sent. When the connection is offline, the message will be buffered and sent when the
     * connection is established. When the enqueued PCK message is too old, it will be discarded before a new connection
     * is established.
     *
     * @param addr the module's address to which is message shall be sent
     * @param wantsAck true, if the module shall respond with an Ack upon successful processing
     * @param data the PCK message to be sent
     */
    public void queue(LcnAddr addr, boolean wantsAck, byte[] data) {
        connection.queueOffline(addr, wantsAck, data);
    }

    /**
     * Shuts the Connection down finally. A shut-down connection cannot re-used.
     */
    public void shutdownFinally() {
        nextState(ConnectionStateShutdown.class);
    }

    /**
     * Checks if the given PCK message is an LCN bus disconnect message. If so, openHAB will be informed and the
     * Connection's State Machine waits for a re-connect.
     *
     * @param pck the PCK message to check
     */
    protected void parseLcnBusDiconnectMessage(String pck) {
        if (pck.equals(LcnDefs.LCNCONNSTATE_DISCONNECTED)) {
            connection.getCallback().onOffline("LCN bus not connected to LCN-PCHK/PKE");
            nextState(ConnectionStateWaitForLcnBusConnectedAfterDisconnected.class);
        }
    }

    /**
     * Closes the Connection SocketChannel.
     */
    protected void closeSocketChannel() {
        try {
            Channel socketChannel = connection.getSocketChannel();
            if (socketChannel != null) {
                socketChannel.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
