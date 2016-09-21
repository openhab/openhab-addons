/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rio;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

/**
 * Defines an implementation of {@link RioHandlerCallback} that will remember the last state
 * for an channelId and suppress the callback if the state hasn't changed
 *
 * @author Tim Roberts
 *
 */
public class StatefulHandlerCallback implements RioHandlerCallback {

    /** The wrapped callback */
    private final RioHandlerCallback _wrappedCallback;

    /** The state by channel id */
    private final Map<String, State> _state = new HashMap<String, State>();

    /**
     * Create the callback from the other {@link RioHandlerCallback}
     *
     * @param wrappedCallback a non-null {@link RioHandlerCallback}
     * @throws NullPointerException if wrappedCallback is null
     */
    public StatefulHandlerCallback(RioHandlerCallback wrappedCallback) {
        if (wrappedCallback == null) {
            throw new NullPointerException("wrappedCallback cannot be null");
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
}
