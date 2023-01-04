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
package org.openhab.binding.xmppclient.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.AbstractXMPPConnection;
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
 */
public class XMPPClient implements IncomingChatMessageListener, ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(XMPPClient.class);
    private AbstractXMPPConnection connection;
    private ChatManager chatManager;
    private HttpFileUploadManager httpFileUploadManager;
    private Set<XMPPClientMessageSubscriber> subscribers = new HashSet<>();

    public void subscribe(XMPPClientMessageSubscriber channel) {
        logger.debug("Channel {} subscribed", channel.getName());
        subscribers.add(channel);
    }

    public void unsubscribe(XMPPClientMessageSubscriber channel) {
        logger.debug("Channel {} unsubscribed", channel.getName());
        subscribers.remove(channel);
    }

    public void connect(String host, Integer port, String login, String domain, String password)
            throws XMPPException, SmackException, IOException {
        disconnect();
        String serverHost = domain;
        if ((host != null) && !host.isEmpty()) {
            serverHost = host;
        }

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder() //
                .setHost(serverHost) //
                .setPort(port) //
                .setUsernameAndPassword(login, password) //
                .setXmppDomain(domain) //
                .build();

        connection = new XMPPTCPConnection(config);
        connection.addConnectionListener(this);

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.enableAutomaticReconnection();

        Identity identity = new Identity("client", "openHAB", "bot");
        ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
        sdm.setIdentity(identity);

        try {
            connection.connect().login();
        } catch (InterruptedException ex) {
        }

        chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(this);
        httpFileUploadManager = HttpFileUploadManager.getInstanceFor(connection);
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public void sendMessage(String to, String message) {
        if (connection == null) {
            logger.warn("XMPP connection is null");
            return;
        }
        if (chatManager == null) {
            logger.warn("XMPP chatManager is null");
            return;
        }
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(to);
            Chat chat = chatManager.chatWith(jid);
            chat.send(message);
        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
            logger.info("XMPP message sending error", e);
        }
    }

    public void sendImageByHTTP(String to, String filename) {
        if (connection == null) {
            logger.warn("XMPP connection is null");
            return;
        }
        if (httpFileUploadManager == null) {
            logger.warn("XMPP httpFileUploadManager is null");
            return;
        }
        try {
            URL u = httpFileUploadManager.uploadFile(new File(filename));
            // Use Stanza oob
            this.sendMessage(to, u.toString());
        } catch (XMPPException.XMPPErrorException | SmackException | InterruptedException | IOException e) {
            logger.warn("XMPP HTTP image sending error", e);
        }
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        logger.debug("XMPP {} says {}", from.asBareJid().toString(), message.getBody());
        for (XMPPClientMessageSubscriber subscriber : subscribers) {
            logger.debug("Push to subscriber {}", subscriber.getName());
            subscriber.processMessage(from.asBareJid().toString(), message.getBody());
        }
    }

    @Override
    public void connected(XMPPConnection connection) {
        logger.debug("Connected to XMPP server.");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        logger.debug("Authenticated to XMPP server.");
    }

    @Override
    public void connectionClosed() {
        logger.debug("XMPP connection was closed.");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        logger.debug("Connection to XMPP server was lost.");
        if (connection != null) {
            connection.disconnect();
            try {
                connection.connect().login();
            } catch (SmackException | IOException | XMPPException | InterruptedException ex) {
                logger.info("XMPP connection error", ex);
            }
        }
    }
}
