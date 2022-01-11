/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.atlona.internal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 * Defines an implementation of {@link AtlonaHandlerCallback} that will remember the last state
 * for an channelId and suppress the callback if the state hasn't changed
 *
 * @author Tim Roberts - Initial contribution
 */
public class StatefulHandlerCallback implements AtlonaHandlerCallback {

    /** The wrapped callback */
    private final AtlonaHandlerCallback wrappedCallback;

    /** The state by channel id */
    private final Map<String, State> state = new ConcurrentHashMap<>();

    private final Lock statusLock = new ReentrantLock();
    private ThingStatus lastThingStatus;
    private ThingStatusDetail lastThingStatusDetail;

    /**
     * Create the callback from the other {@link AtlonaHandlerCallback}
     *
     * @param wrappedCallback a non-null {@link AtlonaHandlerCallback}
     * @throws IllegalArgumentException if wrappedCallback is null
     */
    public StatefulHandlerCallback(AtlonaHandlerCallback wrappedCallback) {
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
     * @param state the new state
     */
    @Override
    public void stateChanged(String channelId, State state) {
        if (channelId == null || "".equals(channelId)) {
            return;
        }

        final State oldState = this.state.get(channelId);

        // If they are equal - nothing changed
        if (Objects.equals(oldState, state)) {
            return;
        }

        // Something changed - save the new state and call the underlying wrapped
        this.state.put(channelId, state);
        wrappedCallback.stateChanged(channelId, state);
    }

    /**
     * Removes the state associated with the channel id. If the channelid
     * doesn't exist (or is null or is empty), this method will do nothing.
     *
     * @param channelId the channel id to remove state
     */
    public void removeState(String channelId) {
        if (channelId == null || "".equals(channelId)) {
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
     * Callback to get the {@link State} for a given property name
     *
     * @param propertyName a possibly null, possibly empty property name
     * @return the {@link State} for the propertyName or null if not found
     */
    public State getState(String propertyName) {
        return state.get(propertyName);
    }
}
