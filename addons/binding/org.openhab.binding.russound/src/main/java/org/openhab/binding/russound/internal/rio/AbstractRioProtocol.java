/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.system.RioSystemHandler;

/**
 * Defines the abstract base for a protocol handler. This base provides managment of the {@link SocketSession} and
 * provides helper methods that will callback {@link RioHandlerCallback}
 *
 * @author Tim Roberts - Initial contribution
 */
public abstract class AbstractRioProtocol implements SocketSessionListener {
    /**
     * The {@link SocketSession} used by this protocol handler
     */
    private final SocketSession session;

    /**
     * The {@link RioSystemHandler} to call back to update status and state
     */
    private final RioHandlerCallback callback;

    /**
     * Constructs the protocol handler from given parameters and will add this handler as a
     * {@link SocketSessionListener} to the specified {@link SocketSession} via
     * {@link SocketSession#addListener(SocketSessionListener)}
     *
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to update state and status
     */
    protected AbstractRioProtocol(SocketSession session, RioHandlerCallback callback) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }

        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        this.session = session;
        this.session.addListener(this);
        this.callback = callback;
    }

    /**
     * Sends the command and puts the thing into {@link ThingStatus#OFFLINE} if an IOException occurs
     *
     * @param command a non-null, non-empty command to send
     */
    protected void sendCommand(String command) {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }
        try {
            session.sendCommand(command);
        } catch (IOException e) {
            getCallback().statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred sending command: " + e);
        }
    }

    /**
     * Updates the state via the {@link RioHandlerCallback#stateChanged(String, State)}
     *
     * @param channelId the channel id to update state
     * @param newState the new state
     */
    protected void stateChanged(String channelId, State newState) {
        getCallback().stateChanged(channelId, newState);
    }

    /**
     * Updates a property via the {@link RioHandlerCallback#setProperty(String, String)}
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    protected void setProperty(String propertyName, String propertyValue) {
        getCallback().setProperty(propertyName, propertyValue);
    }

    /**
     * Updates the status via {@link RioHandlerCallback#statusChanged(ThingStatus, ThingStatusDetail, String)}
     *
     * @param status the new status
     * @param statusDetail the status detail
     * @param msg the status detail message
     */
    protected void statusChanged(ThingStatus status, ThingStatusDetail statusDetail, String msg) {
        getCallback().statusChanged(status, statusDetail, msg);
    }

    /**
     * Disposes of the protocol by removing ourselves from listening to the socket via
     * {@link SocketSession#removeListener(SocketSessionListener)}
     */
    public void dispose() {
        session.removeListener(this);
    }

    /**
     * Implements the {@link SocketSessionListener#responseException(Exception)} to automatically take the thing offline
     * via {@link RioHandlerCallback#statusChanged(ThingStatus, ThingStatusDetail, String)}
     *
     * @param e the exception
     */
    @Override
    public void responseException(IOException e) {
        getCallback().statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Exception occurred reading from the socket: " + e);
    }

    /**
     * Returns the {@link RioHandlerCallback} used by this protocol
     *
     * @return a non-null {@link RioHandlerCallback}
     */
    public RioHandlerCallback getCallback() {
        return callback;
    }
}
