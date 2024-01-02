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
package org.openhab.binding.echonetlite.internal;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Enumeration;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a Datagram channel for sending/receiving data to/from echonet lite devices.
 *
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetChannel {

    private final Logger logger = LoggerFactory.getLogger(EchonetChannel.class);

    private final DatagramChannel channel;
    private final Selector selector = Selector.open();

    private short tid = 0;

    public EchonetChannel(InetSocketAddress discoveryAddress) throws IOException {
        channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.bind(new InetSocketAddress("0.0.0.0", discoveryAddress.getPort()));
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
            if (networkInterface.supportsMulticast() && hasIpV4Address(networkInterface)) {
                channel.join(discoveryAddress.getAddress(), networkInterface);
            }
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    private boolean hasIpV4Address(final NetworkInterface networkInterface) {
        return networkInterface.inetAddresses().anyMatch(ia -> ia instanceof Inet4Address);
    }

    public void close() {
        try {
            logger.debug("closing selector");
            selector.close();
            logger.debug("closing channel");
            channel.close();
        } catch (IOException ignore) {
        }
    }

    short nextTid() {
        return tid++;
    }

    public void sendMessage(EchonetMessageBuilder messageBuilder) throws IOException {
        messageBuilder.buffer().flip();
        channel.send(messageBuilder.buffer(), messageBuilder.address());
    }

    public void pollMessages(EchonetMessage echonetMessage, BiConsumer<EchonetMessage, SocketAddress> consumer,
            final long timeout) throws IOException {
        selector.select(selectionKey -> {
            final DatagramChannel channel = (DatagramChannel) selectionKey.channel();
            try {
                final ByteBuffer buffer = echonetMessage.bufferForRead();
                final SocketAddress address = channel.receive(buffer);

                echonetMessage.sourceAddress(address);
                buffer.flip();
                long t0 = System.currentTimeMillis();
                consumer.accept(echonetMessage, address);
                long t1 = System.currentTimeMillis();
                final long processingTimeMs = t1 - t0;
                if (500 < processingTimeMs) {
                    logger.debug("Message took {}ms to process", processingTimeMs);
                }
            } catch (IOException e) {
                logger.warn("Failed to receive on channel", e);
            }
        }, timeout);
    }
}
