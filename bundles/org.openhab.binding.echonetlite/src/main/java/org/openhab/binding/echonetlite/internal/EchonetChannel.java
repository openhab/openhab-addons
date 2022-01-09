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
package org.openhab.binding.echonetlite.internal;

import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.DISCOVERY_KEY;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Enumeration;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Barker - Initial contribution
 */
public class EchonetChannel {

    private final Logger logger = LoggerFactory.getLogger(EchonetChannel.class);

    private final DatagramChannel channel;
    private final Selector selector = Selector.open();

    private short tid = 0;

    public EchonetChannel() throws IOException {
        channel = DatagramChannel.open();

        channel.bind(new InetSocketAddress("0.0.0.0", 3610));
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.supportsMulticast()) {
                channel.join(DISCOVERY_KEY.address.getAddress(), networkInterface);
            }
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    public void close() {
        try {
            if (null != selector) {
                logger.info("closing selector");
                selector.close();
            }
            if (null != channel) {
                logger.info("closing channel");
                channel.close();
            }
        } catch (IOException e) {
            logger.error("Failed to close selector/channel", e);
        }
    }

    short nextTid() {
        return tid++;
    }

    public void sendMessage(EchonetMessageBuilder messageBuilder) throws IOException {
        messageBuilder.buffer().flip();
        channel.send(messageBuilder.buffer(), messageBuilder.address());
    }

    public void pollMessages(EchonetMessage echonetMessage, Consumer<EchonetMessage> consumer, final long timeout)
            throws IOException {
        selector.select(selectionKey -> {
            final DatagramChannel channel = (DatagramChannel) selectionKey.channel();
            try {
                final ByteBuffer buffer = echonetMessage.bufferForRead();
                final SocketAddress address = channel.receive(buffer);

                echonetMessage.sourceAddress(address);
                buffer.flip();
                consumer.accept(echonetMessage);
            } catch (IOException e) {
                logger.error("Unable to select", e);
            }
        }, timeout);
    }
}
