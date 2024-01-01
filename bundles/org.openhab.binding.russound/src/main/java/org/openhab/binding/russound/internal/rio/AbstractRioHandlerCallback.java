/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.russound.internal.rio;

import java.util.concurrent.CopyOnWriteArrayList;

import org.openhab.core.types.State;

/**
 * Abstract implementation of {@link RioHandlerCallback} that will provide listener services (adding/removing and firing
 * of state)
 *
 * @author Tim Roberts - Initial contribution
 */
public abstract class AbstractRioHandlerCallback implements RioHandlerCallback {
    /** Listener array */
    private final CopyOnWriteArrayList<ListenerState> listeners = new CopyOnWriteArrayList<>();

    /**
     * Adds a listener to {@link #listeners} wrapping the listener in a {@link ListenerState}
     */
    @Override
    public void addListener(String channelId, RioHandlerCallbackListener listener) {
        listeners.add(new ListenerState(channelId, listener));
    }

    /**
     * Remove a listener from {@link #listeners} if the channelID matches
     */
    @Override
    public void removeListener(String channelId, RioHandlerCallbackListener listener) {
        for (ListenerState listenerState : listeners) {
            if (listenerState.channelId.equals(channelId) && listenerState.listener == listener) {
                listeners.remove(listenerState);
            }
        }
    }

    /**
     * Fires a stateUpdate message to all listeners for the channelId and state
     *
     * @param channelId a non-null, non-empty channelId
     * @param state a non-null state
     * @throws IllegalArgumentException if channelId is null or empty.
     * @throws IllegalArgumentException if state is null
     */
    protected void fireStateUpdated(String channelId, State state) {
        if (channelId == null || channelId.isEmpty()) {
            throw new IllegalArgumentException("channelId cannot be null or empty)");
        }
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }
        for (ListenerState listenerState : listeners) {
            if (listenerState.channelId.equals(channelId)) {
                listenerState.listener.stateUpdate(channelId, state);
            }
        }
    }

    /**
     * Internal class used to associate a listener with a channel id
     *
     * @author Tim Roberts
     */
    private class ListenerState {
        /** The channelID */
        private final String channelId;
        /** The listener associated with it */
        private final RioHandlerCallbackListener listener;

        /**
         * Create the listener state from the channelID and listener
         *
         * @param channelId the channelID
         * @param listener the listener
         */
        ListenerState(String channelId, RioHandlerCallbackListener listener) {
            this.channelId = channelId;
            this.listener = listener;
        }
    }
}
