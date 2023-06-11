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
package org.openhab.binding.lifx.internal.util;

import static org.openhab.binding.lifx.internal.util.LifxNetworkUtil.isRemoteAddress;
import static org.openhab.binding.lifx.internal.util.LifxSelectorUtil.CastType.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.LifxSelectorContext;
import org.openhab.binding.lifx.internal.dto.Packet;
import org.openhab.binding.lifx.internal.dto.PacketFactory;
import org.openhab.binding.lifx.internal.dto.PacketHandler;
import org.openhab.binding.lifx.internal.fields.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for sharing {@link Selector} logic between objects.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxSelectorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifxSelectorUtil.class);
    private static final int MAX_SEND_SELECT_RETRIES = 10;
    private static final int SEND_SELECT_TIMEOUT = 200;

    enum CastType {
        BROADCAST,
        UNICAST
    }

    @SuppressWarnings("resource")
    public static @Nullable SelectionKey openBroadcastChannel(@Nullable Selector selector, String logId,
            int broadcastPort) throws IOException {
        if (selector == null) {
            return null;
        }
        DatagramChannel broadcastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.SO_BROADCAST, true);
        broadcastChannel.configureBlocking(false);
        LOGGER.debug("{} : Binding the broadcast channel on port {}", logId, broadcastPort);
        broadcastChannel.bind(new InetSocketAddress(broadcastPort));
        return broadcastChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    @SuppressWarnings("resource")
    public static @Nullable SelectionKey openUnicastChannel(@Nullable Selector selector, String logId,
            @Nullable InetSocketAddress address) throws IOException {
        if (selector == null || address == null) {
            return null;
        }
        DatagramChannel unicastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true);
        unicastChannel.configureBlocking(false);
        unicastChannel.connect(address);
        LOGGER.trace("{} : Connected to light via {}", logId, unicastChannel.getLocalAddress().toString());
        return unicastChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public static void closeSelector(@Nullable Selector selector, String logId) {
        if (selector == null) {
            return;
        }

        try {
            selector.wakeup();

            boolean done = false;
            while (!done) {
                try {
                    selector.keys().stream().forEach(key -> cancelKey(key, logId));
                    done = true; // continue until all keys are cancelled
                } catch (ConcurrentModificationException e) {
                    LOGGER.debug("{} while closing selection keys of the light ({}): {}", e.getClass().getSimpleName(),
                            logId, e.getMessage());
                }
            }

            selector.close();
        } catch (IOException e) {
            LOGGER.warn("{} while closing the selector of the light ({}): {}", e.getClass().getSimpleName(), logId,
                    e.getMessage());
        }
    }

    public static void cancelKey(@Nullable SelectionKey key, String logId) {
        if (key == null) {
            return;
        }

        try {
            key.channel().close();
        } catch (IOException e) {
            LOGGER.error("{} while closing a channel of the light ({}): {}", e.getClass().getSimpleName(), logId,
                    e.getMessage());
        }
        key.cancel();
    }

    @SuppressWarnings("resource")
    public static void receiveAndHandlePackets(Selector selector, String logId,
            BiConsumer<Packet, InetSocketAddress> packetConsumer) {
        try {
            selector.selectNow();
        } catch (IOException e) {
            LOGGER.error("{} while selecting keys for the light ({}) : {}", e.getClass().getSimpleName(), logId,
                    e.getMessage());
        }

        ByteBuffer readBuffer = ByteBuffer.allocate(LifxNetworkUtil.getBufferSize());
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key;

            try {
                key = keyIterator.next();
            } catch (ConcurrentModificationException e) {
                // when a StateServiceResponse packet is handled a new unicastChannel may be registered
                // in the selector which causes this exception, recover from it by restarting the iteration
                LOGGER.debug("{} : Restarting iteration after ConcurrentModificationException", logId);
                keyIterator = selector.selectedKeys().iterator();
                continue;
            }

            if (key.isValid() && key.isReadable()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("{} : Channel is ready for reading", logId);
                }

                SelectableChannel channel = key.channel();
                readBuffer.rewind();

                try {
                    if (channel instanceof DatagramChannel) {
                        InetSocketAddress address = (InetSocketAddress) ((DatagramChannel) channel).receive(readBuffer);
                        if (address == null) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("{} : No datagram is available", logId);
                            }
                        } else if (isRemoteAddress(address.getAddress())) {
                            supplyParsedPacketToConsumer(readBuffer, address, packetConsumer, logId);
                        }
                    } else if (channel instanceof SocketChannel) {
                        ((SocketChannel) channel).read(readBuffer);
                        InetSocketAddress address = (InetSocketAddress) ((SocketChannel) channel).getRemoteAddress();
                        if (address == null) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("{} : Channel socket is not connected", logId);
                            }
                        } else if (isRemoteAddress(address.getAddress())) {
                            supplyParsedPacketToConsumer(readBuffer, address, packetConsumer, logId);
                        }

                    }
                } catch (Exception e) {
                    LOGGER.debug("{} while reading data for the light ({}) : {}", e.getClass().getSimpleName(), logId,
                            e.getMessage());
                }
            }
        }
    }

    private static void supplyParsedPacketToConsumer(ByteBuffer readBuffer, InetSocketAddress address,
            BiConsumer<Packet, InetSocketAddress> packetConsumer, String logId) {
        int messageLength = readBuffer.position();
        readBuffer.rewind();

        ByteBuffer packetSize = readBuffer.slice();
        packetSize.position(0);
        packetSize.limit(2);
        int size = Packet.FIELD_SIZE.value(packetSize);

        if (messageLength == size) {
            ByteBuffer packetType = readBuffer.slice();
            packetType.position(32);
            packetType.limit(34);
            int type = Packet.FIELD_PACKET_TYPE.value(packetType);

            PacketHandler<?> handler = PacketFactory.createHandler(type);

            if (handler == null) {
                LOGGER.trace("{} : Unknown packet type: {} (source: {})", logId, String.format("0x%02X", type),
                        address.toString());
            } else {
                Packet packet = handler.handle(readBuffer);
                packetConsumer.accept(packet, address);
            }
        }
    }

    public static boolean broadcastPacket(@Nullable LifxSelectorContext context, Packet packet) {
        if (context == null) {
            return false;
        }

        packet.setSource(context.getSourceId());
        packet.setSequence(context.getSequenceNumberSupplier().get());

        boolean success = true;
        for (InetSocketAddress address : LifxNetworkUtil.getBroadcastAddresses()) {
            success = success && sendPacket(context, packet, address, BROADCAST);
        }
        return success;
    }

    public static String getLogId(@Nullable MACAddress macAddress, @Nullable InetSocketAddress host) {
        return (macAddress != null ? macAddress.getHex() : (host != null ? host.getHostString() : "Unknown"));
    }

    public static boolean sendPacket(@Nullable LifxSelectorContext context, Packet packet) {
        if (context == null) {
            return false;
        }

        InetSocketAddress host = context.getHost();
        if (host == null) {
            return false;
        }

        packet.setSource(context.getSourceId());
        packet.setTarget(context.getMACAddress());
        packet.setSequence(context.getSequenceNumberSupplier().get());
        return sendPacket(context, packet, host, UNICAST);
    }

    public static boolean resendPacket(@Nullable LifxSelectorContext context, Packet packet) {
        if (context == null) {
            return false;
        }

        InetSocketAddress host = context.getHost();
        if (host == null) {
            return false;
        }

        packet.setSource(context.getSourceId());
        packet.setTarget(context.getMACAddress());
        return sendPacket(context, packet, host, UNICAST);
    }

    @SuppressWarnings("resource")
    private static boolean sendPacket(@Nullable LifxSelectorContext context, Packet packet, InetSocketAddress address,
            CastType castType) {
        if (context == null) {
            return false;
        }

        try {
            if (castType == UNICAST) {
                LifxThrottlingUtil.lock(packet.getTarget());
            } else {
                LifxThrottlingUtil.lock();
            }

            for (int i = 0; i <= MAX_SEND_SELECT_RETRIES; i++) {
                context.getSelector().select(SEND_SELECT_TIMEOUT);

                for (Iterator<SelectionKey> it = context.getSelector().selectedKeys().iterator(); it.hasNext();) {
                    SelectionKey key = it.next();
                    SelectionKey castKey = castType == UNICAST ? context.getUnicastKey() : context.getBroadcastKey();

                    if (key.isValid() && key.isWritable() && key.equals(castKey)) {
                        SelectableChannel channel = key.channel();
                        if (channel instanceof DatagramChannel) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "{} : Sending packet type '{}' from '{}' to '{}' for '{}' with sequence '{}' and source '{}'",
                                        new Object[] { context.getLogId(), packet.getClass().getSimpleName(),
                                                ((InetSocketAddress) ((DatagramChannel) channel).getLocalAddress())
                                                        .toString(),
                                                address.toString(), packet.getTarget().getHex(), packet.getSequence(),
                                                Long.toString(packet.getSource(), 16) });
                            }
                            ((DatagramChannel) channel).send(packet.bytes(), address);
                            return true;
                        } else if (channel instanceof SocketChannel) {
                            ((SocketChannel) channel).write(packet.bytes());
                            return true;
                        }
                    }
                }

                if (i == MAX_SEND_SELECT_RETRIES) {
                    LOGGER.debug("Failed to send packet after {} select retries to the light ({})", i,
                            context.getLogId());
                }
            }
        } catch (Exception e) {
            LOGGER.debug("{} while sending a packet to the light ({}): {}", e.getClass().getSimpleName(),
                    context.getLogId(), e.getMessage());
        } finally {
            if (castType == UNICAST) {
                LifxThrottlingUtil.unlock(packet.getTarget());
            } else {
                LifxThrottlingUtil.unlock();
            }
        }
        return false;
    }
}
