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
package org.openhab.binding.mycroft.internal.channels;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mycroft.internal.MycroftHandler;
import org.openhab.binding.mycroft.internal.api.MessageType;
import org.openhab.binding.mycroft.internal.api.MycroftMessageListener;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * A helper method for channel handling
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public abstract class MycroftChannel<T extends State>
        implements ChannelCommandHandler, MycroftMessageListener<BaseMessage> {

    private ChannelUID channelUID;
    protected MycroftHandler handler;

    public MycroftChannel(MycroftHandler handler, String channelUIDPart) {
        this.handler = handler;
        this.channelUID = new ChannelUID(handler.getThing().getUID(), channelUIDPart);
    }

    public final ChannelUID getChannelUID() {
        return channelUID;
    }

    protected final void updateMyState(T state) {
        handler.updateMyChannel(this, state);
    }

    public final void registerListeners() {
        for (MessageType messageType : getMessageToListenTo()) {
            handler.registerMessageListener(messageType, this);
        }
    }

    protected List<MessageType> getMessageToListenTo() {
        return new ArrayList<>();
    }

    public final void unregisterListeners() {
        for (MessageType messageType : getMessageToListenTo()) {
            handler.unregisterMessageListener(messageType, this);
        }
    }

    @Override
    public void messageReceived(BaseMessage message) {
    }
}
