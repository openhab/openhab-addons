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
package org.openhab.binding.xmppclient.internal.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.openhab.binding.xmppclient.internal.handler.XMPPClientMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XMPPClient} is lib for handling XMPP connection and messaging
 *
 * @author Pavel Gololobov - Initial contribution
 * @author Leo Siepel - Add reconnection logic
 */
@NonNullByDefault
public class XMPPClient implements IncomingChatMessageListener, ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(XMPPClient.class);
    private @Nullable AbstractXMPPConnection connection;
    private @Nullable ChatManager chatManager;
    private @Nullable HttpFileUploadManager httpFileUploadManager;
    private Set<XMPPClientMessageSubscriber> subscribers = new HashSet<>();
    private final XMPPClientEventlistener eventListener;

    public XMPPClient(XMPPClientEventlistener eventListener) {
        this.eventListener = eventListener;
    }

    public void subscribe(XMPPClientMessageSubscriber channel) {
        logger.debug("Channel {} subscribed", channel.getName());
        subscribers.add(channel);
    }

    public void unsubscribe(XMPPClientMessageSubscriber channel) {
        logger.debug("Channel {} unsubscribed", channel.getName());
        subscribers.remove(channel);
    }

    public void connect(String host, Integer port, String login, String domain, String password,
            SecurityMode securityMode) throws XMPPClientConfigException, XMPPClientException {
        disconnect();
        String serverHost = domain;
        if (!host.isBlank()) {
            serverHost = host;
        }

        XMPPTCPConnectionConfiguration config;
        try {
            config = XMPPTCPConnectionConfiguration.builder() //
                    .setHost(serverHost) //
                    .setPort(port) //
                    .setUsernameAndPassword(login, password) //
                    .setXmppDomain(domain) //
                    .setSecurityMode(securityMode)//
                    .build();
        } catch (XmppStringprepException e) {
            throw new XMPPClientConfigException(Objects.requireNonNullElse(e.getMessage(), "Unknown error message"));
        }

        AbstractXMPPConnection connectionLocal = new XMPPTCPConnection(config);
        connection = connectionLocal;
        connectionLocal.addConnectionListener(this);

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connectionLocal);
        reconnectionManager.enableAutomaticReconnection();

        Identity identity = new Identity("client", "openHAB", "bot");
        ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
        sdm.setIdentity(identity);

        try {
            connectionLocal.connect().login();
        } catch (InterruptedException | XMPPException | SmackException | IOException e) {
            throw new XMPPClientException(Objects.requireNonNullElse(e.getMessage(), "Unknown error message"),
                    e.getCause());
        }

        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(this);
        this.chatManager = chatManager;
        httpFileUploadManager = HttpFileUploadManager.getInstanceFor(connection);
    }

    public void disconnect() {
        AbstractXMPPConnection connection = this.connection;
        if (connection != null) {
            connection.disconnect();
        }
    }

    public void sendMessage(String to, String message) {
        if (connection == null) {
            eventListener.onErrorEvent("XMPP connection is null");
            return;
        }

        ChatManager chatManager = this.chatManager;
        if (chatManager == null) {
            eventListener.onErrorEvent("XMPP chatManager is null");
            return;
        }
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(to);
            Chat chat = chatManager.chatWith(jid);
            chat.send(message);
        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
            logger.warn("XMPP message sending error", e);
        }
    }

    public void sendImageByHTTP(String to, String filename) {
        if (connection == null) {
            logger.warn("XMPP connection is null");
            return;
        }
        HttpFileUploadManager httpFileUploadManagerLocal = httpFileUploadManager;
        if (httpFileUploadManagerLocal == null) {
            logger.warn("XMPP httpFileUploadManager is null");
            return;
        }
        try {
            URL u = httpFileUploadManagerLocal.uploadFile(new File(filename));
            // Use Stanza oob
            this.sendMessage(to, u.toString());
        } catch (XMPPException.XMPPErrorException | SmackException | InterruptedException | IOException e) {
            logger.warn("XMPP HTTP image sending error", e);
        }
    }

    @Override
    public void newIncomingMessage(@Nullable EntityBareJid from, @Nullable Message message, @Nullable Chat chat) {
        if (from == null || message == null || chat == null) {
            logger.debug("newIncomingMessage with atleast one null argument, should not happen");
            return;
        }

        logger.debug("XMPP {} says {}", from.asBareJid().toString(), message.getBody());
        for (XMPPClientMessageSubscriber subscriber : subscribers) {
            logger.debug("Push to subscriber {}", subscriber.getName());
            subscriber.processMessage(from.asBareJid().toString(), message.getBody());
        }
    }

    @Override
    public void connected(@Nullable XMPPConnection connection) {
        logger.debug("Connected to XMPP server.");
        eventListener.onAllOk();
    }

    @Override
    public void authenticated(@Nullable XMPPConnection connection, boolean resumed) {
        logger.debug("Authenticated to XMPP server.");
        eventListener.onAllOk();
    }

    @Override
    public void connectionClosed() {
        logger.debug("XMPP connection was closed.");
        eventListener.onErrorEvent("XMPP connection was closed.");
    }

    @Override
    public void connectionClosedOnError(@Nullable Exception e) {
        logger.debug("Connection to XMPP server was lost.");
        eventListener.onErrorEvent("XMPP connection was closed.");
    }
}
