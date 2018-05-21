/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.zoneminder.internal.state;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GenericThingState} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public abstract class GenericThingState implements ChannelStateChangePublisher {

    private Logger logger = LoggerFactory.getLogger(GenericThingState.class);

    final AtomicBoolean allowRefresh = new AtomicBoolean(true);

    private ChannelStateChangeSubscriber subscriber = null;

    // Keeps track of current channel refresh status of subscribed channels
    private Map<String, GenericChannelState> subscriptions = new ConcurrentHashMap<String, GenericChannelState>();

    public GenericThingState(ChannelStateChangeSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    protected abstract void recalculate();

    public void onChannelChanged(ChannelUID channelUID) {
    }

    @Override
    public void addChannel(ChannelUID channelUID) {
        try {
            if (!subscriptions.containsKey(channelUID.getId())) {
                subscriptions.put(channelUID.getId(), createChannelSubscription(channelUID));
            }
        } catch (

        Exception ex) {
            logger.error("{}: context='subscribe' - Exception occurred when subscribing to channel '{}'", "<UNKNOWN>",
                    channelUID.getId());
        }
    }

    protected GenericChannelState createSubscriptionStringType(ChannelUID channelUID) {
        return new ChannelStringType(channelUID, this, subscriber);

    }

    protected GenericChannelState createSubscriptionOnOffType(ChannelUID channelUID) {
        return new ChannelOnOffType(channelUID, this, subscriber);
    }

    protected GenericChannelState getChannelStateHandler(String channelId) {
        return subscriptions.get(channelId);

    }

    public GenericChannelState getChannelStateHandler(ChannelUID channelUID) {
        return getChannelStateHandler(channelUID.getId());

    }

    protected GenericChannelState createSubscriptionRawType(ChannelUID channelUID) {
        return new ChannelRawType(channelUID, this, subscriber);
    }

    @Override
    public void subscribe(ChannelUID channelUID/* , ChannelStateChangeSubscriber subscriber */) {
        try {
            if (getChannelStateHandler(channelUID) == null) {
                addChannel(channelUID/* , subscriber */);
            }
            getChannelStateHandler(channelUID).subscribe();

        } catch (

        Exception ex) {
            logger.error("{}: context='subscribe' - Exception occurred when subscribing to channel '{}'", "<UNKNOWN>",
                    channelUID.getId(), ex);
        }
    }

    @Override
    public void unsubscribe(ChannelUID channelUID) {
        try {
            if (getChannelStateHandler(channelUID) != null) {
                getChannelStateHandler(channelUID).subscribe();
            }
        } catch (Exception ex) {
            logger.error("{}: context='unsubscribe' - Exception occurred when subscribing to channel '{}'", "<UNKNOWN>",
                    channelUID.getId());
        }
    }

    @Override
    public void disableRefresh() {
        allowRefresh.compareAndSet(true, false);
    }

    @Override
    public void enableRefresh() {
        if (allowRefresh.compareAndSet(false, true)) {
            for (Map.Entry<String, GenericChannelState> entry : subscriptions.entrySet()) {
                String key = entry.getKey().toString();
                GenericChannelState channel = entry.getValue();
                channel.flushChanges();
            }
        }
    }

    @Override
    public boolean allowRefresh() {
        return allowRefresh.get();
    }

    public void forceUpdate(boolean _recalculate) {
        if (_recalculate) {
            recalculate();
        }

        for (Map.Entry<String, GenericChannelState> entry : subscriptions.entrySet()) {
            String key = entry.getKey().toString();
            GenericChannelState channel = entry.getValue();
            channel.flushChanges(true);
        }
    }

    /**
     *
     * OLD STUFF Needs cleaning
     *
     */

    protected State getStringAsStringState(String value) {
        State state = UnDefType.UNDEF;

        if (value != null) {
            state = new StringType(value);
        }

        return state;

    }

    protected State getBooleanAsOnOffState(boolean value) {
        State state = UnDefType.UNDEF;
        state = value ? OnOffType.ON : OnOffType.OFF;
        return state;
    }

    protected State getImageByteArrayAsRawType(ByteArrayOutputStream baos) {
        State state = UnDefType.UNDEF;
        if (baos != null) {
            state = new RawType(baos.toByteArray(), "image/jpeg");
        }

        return state;
    }

}
