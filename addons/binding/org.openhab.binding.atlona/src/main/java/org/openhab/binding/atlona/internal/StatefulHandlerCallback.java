/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

/**
 * Defines an implementation of {@link AtlonaHandlerCallback} that will remember the last state
 * for an channelId and suppress the callback if the state hasn't changed
 *
 * @author Tim Roberts
 *
 */
public class StatefulHandlerCallback implements AtlonaHandlerCallback {

    /** The wrapped callback */
    private final AtlonaHandlerCallback _wrappedCallback;

    /** The state by channel id */
    private final Map<String, State> _state = new ConcurrentHashMap<String, State>();

    private final Lock _statusLock = new ReentrantLock();
    private ThingStatus _lastThingStatus = null;
    private ThingStatusDetail _lastThingStatusDetail = null;

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

        _wrappedCallback = wrappedCallback;
    }

    /**
     * Overrides the status changed to simply call the {@link #_wrappedCallback}
     *
     * @param status the new status
     * @param detail the new detail
     * @param msg the new message
     */
    @Override
    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
        _statusLock.lock();
        try {
            // Simply return we match the last status change (prevents loops if changing to the same status)
            if (status == _lastThingStatus && detail == _lastThingStatusDetail) {
                return;
            }

            _lastThingStatus = status;
            _lastThingStatusDetail = detail;
        } finally {
            _statusLock.unlock();
        }
        // If we got this far - call the underlying one
        _wrappedCallback.statusChanged(status, detail, msg);

    }

    /**
     * Overrides the state changed to determine if the state is new or changed and then
     * to call the {@link #_wrappedCallback} if it has
     *
     * @param channelId the channel id that changed
     * @param state the new state
     */
    @Override
    public void stateChanged(String channelId, State state) {
        if (StringUtils.isEmpty(channelId)) {
            return;
        }

        final State oldState = _state.get(channelId);

        // If both null OR the same value (enums), nothing changed
        if (oldState == state) {
            return;
        }

        // If they are equal - nothing changed
        if (oldState != null && oldState.equals(state)) {
            return;
        }

        // Something changed - save the new state and call the underlying wrapped
        _state.put(channelId, state);
        _wrappedCallback.stateChanged(channelId, state);

    }

    /**
     * Removes the state associated with the channel id. If the channelid
     * doesn't exist (or is null or is empty), this method will do nothing.
     *
     * @param channelId the channel id to remove state
     */
    public void removeState(String channelId) {
        if (StringUtils.isEmpty(channelId)) {
            return;
        }
        _state.remove(channelId);
    }

    /**
     * Overrides the set property to simply call the {@link #_wrappedCallback}
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    @Override
    public void setProperty(String propertyName, String propertyValue) {
        _wrappedCallback.setProperty(propertyName, propertyValue);

    }

    /**
     * Callback to get the {@link State} for a given property name
     *
     * @param propertyName a possibly null, possibly empty property name
     * @return the {@link State} for the propertyName or null if not found
     */
    public State getState(String propertyName) {
        // TODO Auto-generated method stub
        return _state.get(propertyName);
    }
}
