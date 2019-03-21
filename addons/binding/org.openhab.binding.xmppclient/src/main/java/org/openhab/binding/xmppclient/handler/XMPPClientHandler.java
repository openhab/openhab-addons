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

import static org.openhab.binding.xmppclient.XMPPClientBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.xmppclient.action.XMPPActions;
import org.openhab.binding.xmppclient.internal.XMPPClient;
import org.openhab.binding.xmppclient.handler.XMPPClientConfiguration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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

    final Map<ChannelUID, PublishTriggerChannel> channelStateByChannelUID = new HashMap<>();

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

    /**
     * Does nothing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands to handle
    }

    @Override
    protected void triggerChannel(ChannelUID channelUID, String event) {
        super.triggerChannel(channelUID, event);
    }

    @Override
    public void initialize() {
        logger.info("XMPP Client Start initializing");
        config = getConfigAs(XMPPClientConfiguration.class);

        xmppClient = new XMPPClient();

        updateStatus(ThingStatus.UNKNOWN);

        xmppClient.connect(config.host, config.port, config.username, config.password, "openHAB");
        updateStatus(ThingStatus.ONLINE);

        for(Channel channel : thing.getChannels()) {
            final PublishTriggerChannelConfig channelConfig = channel.getConfiguration().as(PublishTriggerChannelConfig.class);
            PublishTriggerChannel c = new PublishTriggerChannel(channelConfig, channel.getUID(), xmppClient, this);
            channelStateByChannelUID.put(channel.getUID(), c);
            logger.info("XMPP added channel {} payload {}", channel.getUID().toString(), channelConfig.payload);
        }

        channelStateByChannelUID.values().forEach(c -> c.start());
    }

    @Override
    public void dispose() {
        channelStateByChannelUID.values().forEach(c -> c.stop());
        channelStateByChannelUID.clear();
        xmppClient.disconnect();
        super.dispose();
    }

}
