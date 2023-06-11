/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 * Defines an implementation of {@link RioHandlerCallback} that will remember the last state
 * for a channelId and suppress the callback if the state hasn't changed
 *
 * @author Tim Roberts - Initial contribution
 */
public class StatefulHandlerCallback implements RioHandlerCallback {

    /** The wrapped callback */
    private final RioHandlerCallback wrappedCallback;

    /** The state by channel id */
    private final Map<String, State> state = new ConcurrentHashMap<>();

    private final Lock statusLock = new ReentrantLock();
    private ThingStatus lastThingStatus = null;
    private ThingStatusDetail lastThingStatusDetail = null;

    /**
     * Create the callback from the other {@link RioHandlerCallback}
     *
     * @param wrappedCallback a non-null {@link RioHandlerCallback}
     * @throws IllegalArgumentException if wrappedCallback is null
     */
    public StatefulHandlerCallback(RioHandlerCallback wrappedCallback) {
        if (wrappedCallback == null) {
            throw new IllegalArgumentException("wrappedCallback cannot be null");
        }

        this.wrappedCallback = wrappedCallback;
    }

    /**
     * Overrides the status changed to simply call the {@link #wrappedCallback}
     *
     * @param status the new status
     * @param detail the new detail
     * @param msg the new message
     */
    @Override
    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
        statusLock.lock();
        try {
            // Simply return we match the last status change (prevents loops if changing to the same status)
            if (status == lastThingStatus && detail == lastThingStatusDetail) {
                return;
            }

            lastThingStatus = status;
            lastThingStatusDetail = detail;
        } finally {
            statusLock.unlock();
        }

        // If we got this far - call the underlying one
        wrappedCallback.statusChanged(status, detail, msg);
    }

    /**
     * Overrides the state changed to determine if the state is new or changed and then
     * to call the {@link #wrappedCallback} if it has
     *
     * @param channelId the channel id that changed
     * @param newState the new state
     */
    @Override
    public void stateChanged(String channelId, State newState) {
        if (channelId == null || channelId.isEmpty()) {
            return;
        }

        final State oldState = state.get(channelId);

        // If they are equal - nothing changed
        if (Objects.equals(oldState, newState)) {
            return;
        }

        // Something changed - save the new state and call the underlying wrapped
        state.put(channelId, newState);
        wrappedCallback.stateChanged(channelId, newState);
    }

    /**
     * Removes the state associated with the channel id. If the channelid
     * doesn't exist (or is null or is empty), this method will do nothing.
     *
     * @param channelId the channel id to remove state
     */
    public void removeState(String channelId) {
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        state.remove(channelId);
    }

    /**
     * Overrides the set property to simply call the {@link #wrappedCallback}
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    @Override
    public void setProperty(String propertyName, String propertyValue) {
        wrappedCallback.setProperty(propertyName, propertyValue);
    }

    /**
     * Returns teh current state for the property
     *
     * @param propertyName a possibly null, possibly empty property name
     * @return the {@link State} for the property or null if not found (or property name is null/empty)
     */
    public State getProperty(String propertyName) {
        return state.get(propertyName);
    }

    @Override
    public void addListener(String channelId, RioHandlerCallbackListener listener) {
        wrappedCallback.addListener(channelId, listener);
    }

    @Override
    public void removeListener(String channelId, RioHandlerCallbackListener listener) {
        wrappedCallback.removeListener(channelId, listener);
    }
}
