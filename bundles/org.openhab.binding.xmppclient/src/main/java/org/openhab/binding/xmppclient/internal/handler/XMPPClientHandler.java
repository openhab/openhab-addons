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
package org.openhab.binding.xmppclient.internal.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.openhab.binding.xmppclient.internal.XMPPClient;
import org.openhab.binding.xmppclient.internal.action.XMPPActions;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XMPPClientHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pavel Gololobov - Initial contribution
 */

public class XMPPClientHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(XMPPClientHandler.class);
    private XMPPClient xmppClient;
    private XMPPClientConfiguration config;
    private final Map<ChannelUID, PublishTriggerChannel> channelStateByChannelUID = new HashMap<>();

    public XMPPClientHandler(Bridge thing) {
        super(thing);
    }

    public XMPPClient getXMPPClient() {
        return xmppClient;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(XMPPActions.class);
    }

    @Override
    protected void triggerChannel(ChannelUID channelUID, String event) {
        super.triggerChannel(channelUID, event);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not supported
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.schedule(this::doConnect, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        channelStateByChannelUID.values().forEach(c -> c.stop());
        channelStateByChannelUID.clear();
        xmppClient.disconnect();
        super.dispose();
    }

    private void doConnect() {
        config = getConfigAs(XMPPClientConfiguration.class);
        xmppClient = new XMPPClient();
        try {
            xmppClient.connect(config.host, config.port, config.username, config.domain, config.password);
        } catch (SmackException | IOException | XMPPException e) {
            logger.info("XMPP connection error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        for (Channel channel : thing.getChannels()) {
            final PublishTriggerChannelConfig channelConfig = channel.getConfiguration()
                    .as(PublishTriggerChannelConfig.class);
            PublishTriggerChannel c = new PublishTriggerChannel(channelConfig, channel.getUID(), xmppClient, this);
            channelStateByChannelUID.put(channel.getUID(), c);
            logger.info("XMPP added channel {} payload {}", channel.getUID().toString(), channelConfig.payload);
        }
        channelStateByChannelUID.values().forEach(c -> c.start());

        updateStatus(ThingStatus.ONLINE);
    }
}
