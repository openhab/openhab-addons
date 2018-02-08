/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.listener.GatewayListener;
import org.openhab.binding.openwebnet.internal.parser.Parser;

/**
 * Abstract class defining main functions of a Bticino gateway
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
abstract class InternalGateway implements AutoCloseable {

    private final List<GatewayListener> listeners;
    private final Parser parser;

    public InternalGateway(Parser parser) {
        this.listeners = new CopyOnWriteArrayList<GatewayListener>();
        this.parser = parser;
    }

    /**
     * Get parser
     *
     * @return parser
     */
    protected Parser getParser() {
        return this.parser;
    }

    /**
     * connect to the gateway
     */
    public abstract void connect();

    /**
     * Transmit data to the gateway
     *
     * @param data the data to send
     */
    public abstract void write(@Nullable String data) throws IOException;

    /**
     * Disconnect from the gateway
     */
    @Override
    public abstract void close();

    /**
     * Add a new Listener to the list of object that are informed of Gateway status
     *
     * @param listener to add
     */
    public void addGatewayListener(GatewayListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a new Listener to the list of object that are informed of Gateway status
     *
     * @param listener listener to remove
     */
    public void removeGatewayListener(GatewayListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that the gateway is connected
     */
    protected void notifyConnected() {
        for (GatewayListener listener : listeners) {
            listener.onConnect();
        }
    }

    /**
     * Notify all listeners that the gateway is disconnected
     */
    protected void notifyDisconnected() {
        for (GatewayListener listener : listeners) {
            listener.onDisconnect();
        }
    }

}
