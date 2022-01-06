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
package org.openhab.binding.xmppclient.internal.handler;

import org.openhab.binding.xmppclient.internal.XMPPClient;
import org.openhab.core.thing.ChannelUID;

/**
 * Subscribes to a chat and calls {@link AbstractBrokerHandler#triggerChannel(ChannelUID, String)} if a value has been
 * received.
 *
 * @author Pavel Gololobov - Initial contribution
 */
public class PublishTriggerChannel implements XMPPClientMessageSubscriber {
    private final XMPPClient connection;
    private final PublishTriggerChannelConfig config;
    private final ChannelUID uid;
    private final XMPPClientHandler handler;

    PublishTriggerChannel(PublishTriggerChannelConfig config, ChannelUID uid, XMPPClient connection,
            XMPPClientHandler handler) {
        this.config = config;
        this.uid = uid;
        this.connection = connection;
        this.handler = handler;
    }

    public void start() {
        connection.subscribe(this);
    }

    public void stop() {
        connection.unsubscribe(this);
    }

    @Override
    public void processMessage(String from, String payload) {
        // Check condition if exists
        String expectedPayload = config.payload;
        if ((expectedPayload != null) && (!expectedPayload.isEmpty()) && !payload.equals(expectedPayload)) {
            return;
        }
        String eventValue = "";
        if (!config.separator.isEmpty()) {
            eventValue = from + config.separator + payload;
        } else {
            eventValue = payload;
        }
        handler.triggerChannel(uid, eventValue);
    }

    @Override
    public String getName() {
        return uid.toString();
    }
}
