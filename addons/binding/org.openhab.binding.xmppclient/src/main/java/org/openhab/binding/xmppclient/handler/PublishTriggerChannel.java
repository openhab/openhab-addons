/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.xmppclient.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.openhab.binding.xmppclient.internal.XMPPClient;

/**
 * Subscribes to a chat and calls {@link AbstractBrokerHandler#triggerChannel(ChannelUID, String)} if a value got
 * received.
 *
 * @author Pavel Gololobov - Initial contribution
 */
@NonNullByDefault
public class PublishTriggerChannel implements XMPPClientMessageSubscriber {

    private final XMPPClient connection;
    private final PublishTriggerChannelConfig config;
    private final ChannelUID uid;
    private final XMPPClientHandler handler;

    PublishTriggerChannel(PublishTriggerChannelConfig config, ChannelUID uid, XMPPClient connection, XMPPClientHandler handler) {
        this.config = config;
        this.uid = uid;
        this.connection = connection;
        this.handler = handler;
    }

    void start() {
        connection.subscribe(this);
    }

    public void stop() {
        connection.unsubscribe(this);
    }

    @Override
    public void processMessage(String topic, String payload) {
        // Check condition if exists
        String expectedPayload = config.payload;
        if ((expectedPayload != null) && (!expectedPayload.isEmpty()) && !payload.equals(expectedPayload)) {
            return;
        }
        handler.triggerChannel(uid, payload);
    }

    @Override
    public String getName() {
        return uid.toString();
    }

}
