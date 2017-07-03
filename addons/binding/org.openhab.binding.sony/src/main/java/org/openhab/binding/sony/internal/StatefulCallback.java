/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

// TODO: Auto-generated Javadoc
/**
 * The Class StatefulCallback.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
public class StatefulCallback<T> implements ThingCallback<T> {

    /** The state by channel. */
    private Map<T, State> stateByChannel = new HashMap<T, State>();

    /** The wrapped callback. */
    private final ThingCallback<T> _wrappedCallback;

    /**
     * Instantiates a new stateful callback.
     *
     * @param wrappedCallback the wrapped callback
     */
    public StatefulCallback(ThingCallback<T> wrappedCallback) {
        _wrappedCallback = wrappedCallback;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.ThingCallback#statusChanged(org.eclipse.smarthome.core.thing.ThingStatus,
     * org.eclipse.smarthome.core.thing.ThingStatusDetail, java.lang.String)
     */
    @Override
    public synchronized void statusChanged(ThingStatus state, ThingStatusDetail detail, String msg) {
        _wrappedCallback.statusChanged(state, detail, msg);
        if (state == ThingStatus.OFFLINE) {
            stateByChannel.clear();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.ThingCallback#stateChanged(java.lang.Object,
     * org.eclipse.smarthome.core.types.State)
     */
    @Override
    public synchronized void stateChanged(T channelId, State newState) {
        final State oldState = stateByChannel.get(channelId);

        // If both are null or they are the same (ie enums), ignore update
        if (oldState == newState) {
            return; // null to null or struct compare
        }

        // If they are not null and are equal - ignore update
        if (oldState != null && newState != null && oldState.equals(newState)) {
            return;
        }

        // Nothing's equal - update our state and call callback
        stateByChannel.put(channelId, newState);
        _wrappedCallback.stateChanged(channelId, newState);
    }

    /**
     * Removes the state.
     *
     * @param channelId the channel id
     */
    public synchronized void removeState(T channelId) {
        stateByChannel.remove(channelId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.ThingCallback#setProperty(java.lang.String, java.lang.String)
     */
    @Override
    public void setProperty(String propertyName, String propertyValue) {
        _wrappedCallback.setProperty(propertyName, propertyValue);
    }
}
