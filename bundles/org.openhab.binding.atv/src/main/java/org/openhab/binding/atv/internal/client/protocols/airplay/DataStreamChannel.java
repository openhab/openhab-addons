/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapChannel;
import org.openhab.binding.atv.internal.client.protocols.mrp.MrpExtensions;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection used to handle the AirPlay 2 data stream channel.
 *
 * <p>
 * Messages are framed with a 32-byte big-endian header ({@code size:u32},
 * {@code message_type:12s}, {@code command:4s}, {@code seqno:u64}, {@code padding:u32}, in
 * the packed format {@code >I12s4sQI}); the payload is a binary plist of the form
 * {@code {"params": {"data": <protobufs>}}} where the protobuf messages are concatenated
 * with varint length prefixes. Requests use message type {@code "sync"} and are acknowledged
 * with an (empty) {@code "rply"} carrying the same sequence number.
 *
 * <p>
 * The static codec methods are used by both the client side and the fake device in tests.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class DataStreamChannel extends HapChannel {

    /** Length of the binary data frame header ({@code DataHeader.length}). */
    public static final int HEADER_LENGTH = 32;

    /** Padding value used in all sent messages ({@code DATA_HEADER_PADDING}). */
    public static final int DATA_HEADER_PADDING = 0x00000000;

    /**
     * Message type of requests ({@code b"sync" + 8 * b"\x00"}).
     */
    public static final byte[] MESSAGE_TYPE_SYNC = messageType("sync");

    /**
     * Message type of replies ({@code b"rply" + 8 * b"\x00"}).
     */
    public static final byte[] MESSAGE_TYPE_REPLY = messageType("rply");

    /**
     * Command used when sending protobuf messages ({@code b"comm"}).
     */
    public static final byte[] COMMAND_COMM = "comm".getBytes(StandardCharsets.UTF_8);

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStreamChannel.class);

    /** Listener interface for a {@link DataStreamChannel}. */
    public interface Listener {

        /**
         * Handles an incoming protobuf message.
         *
         * @param message the decoded protocol message
         */
        void handleProtobuf(ProtocolMessage message);

        /**
         * Called when the device connection was dropped.
         *
         * @param exception cause, or {@code null} for a clean remote close
         */
        void handleConnectionLost(Exception exception);
    }

    /**
     * A decoded data stream channel message.
     *
     * @param messageType 12-byte message type, e.g. {@code "sync"} padded with zeros
     * @param command 4-byte command, e.g. {@code "comm"}
     * @param seqno sequence number correlating requests and replies
     * @param padding header padding value
     * @param payload message payload (typically a binary plist)
     */
    public record DataStreamMessage(byte[] messageType, byte[] command, long seqno, int padding, byte[] payload) {
    }

    /**
     * Result of {@link #decodeMessage(byte[])}: the decoded message (or {@code null} when
     * more data is needed) and the unconsumed remainder of the buffer.
     *
     * @param message decoded message or {@code null}
     * @param remainder bytes not consumed by this message
     */
    public record DecodeResult(@Nullable DataStreamMessage message, byte[] remainder) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private final long sendSeqno;
    private volatile @Nullable Listener listener;
    private byte[] buffer = new byte[0];

    /**
     * Creates a data stream channel with encryption enabled and a random send sequence
     * number in {@code [0x100000000, 0x1FFFFFFFF)}.
     *
     * @param outputKey key encrypting outgoing data
     * @param inputKey key decrypting incoming data
     */
    public DataStreamChannel(byte[] outputKey, byte[] inputKey) {
        this(outputKey, inputKey, 0x100000000L + (long) (RANDOM.nextDouble() * 0xFFFFFFFFL));
    }

    /**
     * Creates a data stream channel with an explicit send sequence number (for tests).
     *
     * @param outputKey key encrypting outgoing data
     * @param inputKey key decrypting incoming data
     * @param sendSeqno sequence number stamped on outgoing requests
     */
    public DataStreamChannel(byte[] outputKey, byte[] inputKey, long sendSeqno) {
        super(outputKey, inputKey);
        this.sendSeqno = sendSeqno;
        onConnectionLost(cause -> {
            Listener current = listener;
            if (current != null) {
                current.handleConnectionLost(
                        cause instanceof Exception ex ? ex : cause == null ? null : new RuntimeException(cause));
            }
        });
    }

    /**
     * Sets the listener receiving decoded protobuf messages and connection state.
     *
     * @param listener the listener
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * The sequence number stamped on outgoing requests.
     */
    public long sendSeqno() {
        return sendSeqno;
    }

    /**
     * Serializes a protobuf message and sends it to the receiver.
     *
     * @param message the message to send
     */
    public void sendProtobuf(ProtocolMessage message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("params", Map.of("data", encodeProtobufs(List.of(message))));
        send(encodeMessage(new DataStreamMessage(MESSAGE_TYPE_SYNC, COMMAND_COMM, sendSeqno, DATA_HEADER_PADDING,
                encodePayload(payload))));
    }

    @Override
    protected void onReceive(byte[] plaintext) {
        buffer = concat(buffer, plaintext);
        while (buffer.length >= HEADER_LENGTH) {
            DecodeResult result = decodeMessage(buffer);
            buffer = result.remainder();
            DataStreamMessage message = result.message();
            if (message == null) {
                break;
            }

            Object payload = decodePayload(message.payload());
            if (payload != null) {
                processPayload(payload);
            }

            // If this was a request, send a reply to satisfy other end
            if (startsWith(message.messageType(), "sync")) {
                send(encodeReply(message.seqno()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processPayload(Object payload) {
        Object data = null;
        if (payload instanceof Map<?, ?> message) {
            Object params = message.get("params");
            if (params instanceof Map<?, ?> paramsMap) {
                data = paramsMap.get("data");
            }
        }
        if (!(data instanceof byte[] raw)) {
            LOGGER.debug("Got message with unsupported format: {}", payload);
            return;
        }

        Listener current = listener;
        for (ProtocolMessage message : decodeProtobufs(raw)) {
            if (current != null) {
                current.handleProtobuf(message);
            }
        }
    }

    // Static codecs (BaseDataStreamChannel), shared with the fake device in tests

    /**
     * Encodes a data stream channel message.
     *
     * @param message the message to encode
     * @return header plus payload bytes
     */
    public static byte[] encodeMessage(DataStreamMessage message) {
        ByteBuffer output = ByteBuffer.allocate(HEADER_LENGTH + message.payload().length);
        output.putInt(HEADER_LENGTH + message.payload().length);
        output.put(Arrays.copyOf(message.messageType(), 12));
        output.put(Arrays.copyOf(message.command(), 4));
        output.putLong(message.seqno());
        output.putInt(message.padding());
        output.put(message.payload());
        return output.array();
    }

    /**
     * Decodes a data stream channel message.
     *
     * @param data buffered bytes
     * @return the decoded message (or {@code null} when incomplete) plus the remainder
     */
    public static DecodeResult decodeMessage(byte[] data) {
        if (data.length < HEADER_LENGTH) {
            return new DecodeResult(null, data);
        }
        ByteBuffer input = ByteBuffer.wrap(data);
        long size = Integer.toUnsignedLong(input.getInt());
        byte[] messageType = new byte[12];
        input.get(messageType);
        byte[] command = new byte[4];
        input.get(command);
        long seqno = input.getLong();
        int padding = input.getInt();
        if (data.length < size) {
            LOGGER.debug("Not enough data on data channel (has {}, expects {})", data.length, size);
            return new DecodeResult(null, data);
        }
        byte[] payload = Arrays.copyOfRange(data, HEADER_LENGTH, (int) size);
        return new DecodeResult(new DataStreamMessage(messageType, command, seqno, padding, payload),
                Arrays.copyOfRange(data, (int) size, data.length));
    }

    /**
     * Encodes a message payload as a binary plist.
     *
     * @param payload the object graph to encode
     * @return {@code bplist00} bytes
     */
    public static byte[] encodePayload(Object payload) {
        return BinaryPlist.dump(payload);
    }

    /**
     * Decodes a message payload.
     *
     * @param payload binary plist bytes
     * @return the decoded object or {@code null} when decoding fails
     */
    public static @Nullable Object decodePayload(byte[] payload) {
        Object data = AirPlayUtils.decodePlistBody(payload);
        if (data == null) {
            LOGGER.warn("failed to process data frame");
        }
        return data;
    }

    /**
     * Encodes protobuf messages with varint length prefixes.
     *
     * @param messages the messages to serialize
     * @return concatenated varint-prefixed serialized messages
     */
    public static byte[] encodeProtobufs(List<ProtocolMessage> messages) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (ProtocolMessage message : messages) {
            byte[] serialized = message.toByteArray();
            output.writeBytes(Variant.writeVariant(serialized.length));
            output.writeBytes(serialized);
        }
        return output.toByteArray();
    }

    /**
     * Decodes protobuf messages, including the quirk that a
     * {@code ConfigureConnectionMessage} may arrive <em>without</em> a varint length
     * prefix: protobuf fields are encoded in ascending numerical order and every message
     * must include type (field #1), encoded with the tag {@code 0x08}. This is not a
     * valid length since the minimal message length is at least 40 (type and
     * uniqueIdentifier), so a leading {@code 0x08} means the message is not length
     * prefixed.
     *
     * @param data concatenated (normally varint-prefixed) serialized messages
     * @return successfully decoded messages; parsing stops at the first error
     */
    public static List<ProtocolMessage> decodeProtobufs(byte[] data) {
        List<ProtocolMessage> messages = new ArrayList<>();
        try {
            while (data.length > 0) {
                byte[] message;
                if (data[0] == 0x08) {
                    message = data;
                    data = new byte[0];
                } else {
                    Variant.Result length = Variant.readVariant(data);
                    byte[] raw = Arrays.copyOfRange(data, length.consumed(), data.length);
                    if (raw.length < length.value()) {
                        LOGGER.warn("Expected {} bytes, got {}", length.value(), raw.length);
                        break;
                    }
                    message = Arrays.copyOfRange(raw, 0, (int) length.value());
                    data = Arrays.copyOfRange(raw, (int) length.value(), raw.length);
                }
                if (message.length == 0 || message[0] != 0x08) {
                    throw new IllegalStateException("message does not start with type field");
                }
                messages.add(ProtocolMessage.parseFrom(message, MrpExtensions.EXTENSION_REGISTRY));
            }
        } catch (Exception e) {
            LOGGER.warn("failed to process data frame", e);
        }
        return messages;
    }

    /**
     * Encodes an (empty) reply to a request.
     *
     * @param seqno sequence number of the request being acknowledged
     * @return the encoded reply message
     */
    public static byte[] encodeReply(long seqno) {
        return encodeMessage(
                new DataStreamMessage(MESSAGE_TYPE_REPLY, new byte[4], seqno, DATA_HEADER_PADDING, new byte[0]));
    }

    private static boolean startsWith(byte[] data, String prefix) {
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        if (data.length < prefixBytes.length) {
            return false;
        }
        return Arrays.equals(data, 0, prefixBytes.length, prefixBytes, 0, prefixBytes.length);
    }

    private static byte[] messageType(String name) {
        return Arrays.copyOf(name.getBytes(StandardCharsets.UTF_8), 12);
    }

    private static byte[] concat(byte[] first, byte[] second) {
        if (first.length == 0) {
            return second;
        }
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
