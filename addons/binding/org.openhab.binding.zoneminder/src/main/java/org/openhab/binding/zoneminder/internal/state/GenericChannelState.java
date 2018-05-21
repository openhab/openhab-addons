/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.state;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.activation.UnsupportedDataTypeException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link GenericThingState} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public abstract class GenericChannelState implements ChannelState {

    private AtomicBoolean isDirty = new AtomicBoolean(false);

    private ChannelUID channelUID;
    private GenericThingState thing = null;
    private ChannelStateChangeSubscriber subscriber = null;
    private State state = UnDefType.NULL;
    private int countSubscription = 0;

    protected GenericChannelState(ChannelUID channelUID, GenericThingState thing,
            ChannelStateChangeSubscriber subscriber) {
        this.channelUID = channelUID;
        this.thing = thing;
        this.subscriber = subscriber;
    }

    @Override
    public void subscribe() {
        countSubscription++;
    }

    @Override
    public void unsubscribe() {
        if (countSubscription > 0) {
            countSubscription--;
        }

    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(Object objState) throws UnsupportedDataTypeException {
        setState(objState, true);
    }

    @Override
    public void setState(Object objState, boolean update) throws UnsupportedDataTypeException {
        State newState = convert(objState);
        boolean changed = false;

        if (!(state.toString().equals(newState.toString()))) {
            changed = true;
            state = newState;
            // Set Dirty flag
            setDirtyFlag();

            thing.onChannelChanged(channelUID);
        }

        if (update && changed) {
            // Try to udpate
            flushChanges();
        }
    }

    public void flushChanges() {
        flushChanges(false);
    }

    public void flushChanges(boolean force) {
        if (!isDirty.get() && !force) {
            return;
        }

        if (thing.allowRefresh() || force) {
            subscriber.onStateChanged(channelUID, state);
            // Clear dirtyflag
            clearDirtyFlag();
        }
    }

    public void setDirtyFlag() {
        isDirty.lazySet(true);
    }

    public void clearDirtyFlag() {
        isDirty.set(false);
    }

    protected abstract State convert(Object state) throws UnsupportedDataTypeException;

}
