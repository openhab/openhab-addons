/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.openhab.binding.xmppclient.internal.action.XMPPActions;
import org.openhab.binding.xmppclient.internal.client.XMPPClient;
import org.openhab.binding.xmppclient.internal.client.XMPPClientConfigException;
import org.openhab.binding.xmppclient.internal.client.XMPPClientEventlistener;
import org.openhab.binding.xmppclient.internal.client.XMPPClientException;
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
@NonNullByDefault
public class XMPPClientHandler extends BaseBridgeHandler implements XMPPClientEventlistener {
    private final Logger logger = LoggerFactory.getLogger(XMPPClientHandler.class);
    private XMPPClient xmppClient;
    private final Map<ChannelUID, PublishTriggerChannel> channelStateByChannelUID = new HashMap<>();

    public XMPPClientHandler(Bridge thing) {
        super(thing);
        xmppClient = new XMPPClient(this);
    }

    public XMPPClient getXMPPClient() {
        return xmppClient;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(XMPPActions.class);
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
        XMPPClientConfiguration config = getConfigAs(XMPPClientConfiguration.class);
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Please check configuration");
            return;
        }

        try {
            xmppClient.connect(Objects.requireNonNullElse(config.host, ""), config.port, config.username, config.domain,
                    config.password, SecurityMode.valueOf(config.securityMode));
            updateStatus(ThingStatus.ONLINE);
        } catch (XMPPClientConfigException e) {
            logger.debug("XMPP connection error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        } catch (XMPPClientException e) {
            logger.debug("XMPP connection error", e);
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
    }

    @Override
    public void onErrorEvent(String errorMessage) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
    }

    @Override
    public void onAllOk() {
        updateStatus(ThingStatus.ONLINE);
    }
}
