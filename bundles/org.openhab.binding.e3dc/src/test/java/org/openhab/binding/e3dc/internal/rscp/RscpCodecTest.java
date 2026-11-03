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
package org.openhab.binding.e3dc.internal.rscp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests the RSCP frame and item codec against the documented wire layout.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class RscpCodecTest {

    /** Item header is tag (4 bytes), type (1 byte) and length (2 bytes). */
    private static final int ITEM_HEADER = 7;

    private static byte[] item(int tag, int type, byte[] data) {
        ByteBuffer header = ByteBuffer.allocate(ITEM_HEADER).order(ByteOrder.LITTLE_ENDIAN).putInt(tag).put((byte) type)
                .putShort((short) data.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(header.array(), 0, ITEM_HEADER);
        out.write(data, 0, data.length);
        return out.toByteArray();
    }

    /** Builds a frame by hand so the codec is checked against an independent construction. */
    private static byte[] frame(byte[] payload, boolean withCrc) {
        int extra = withCrc ? 4 : 0;
        ByteBuffer buffer = ByteBuffer.allocate(RscpCodec.HEADER_LENGTH + payload.length + extra)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(RscpCodec.MAGIC_BYTES[0]);
        buffer.put(RscpCodec.MAGIC_BYTES[1]);
        buffer.put((byte) 0);
        buffer.put((byte) (withCrc ? RscpCodec.VERSION : 0));
        buffer.putLong(1_600_000_000L);
        buffer.putInt(123_000_000);
        buffer.putShort((short) payload.length);
        buffer.put(payload);
        byte[] bytes = buffer.array();
        if (withCrc) {
            CRC32 crc = new CRC32();
            crc.update(bytes, 0, bytes.length - 4);
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(bytes.length - 4, (int) crc.getValue());
        }
        return bytes;
    }

    // ------------------------------------------------------------------ assertions

    /**
     * Reads a number that must be present, turning the nullness of the lookup into a test failure
     * instead of a null pointer access.
     */
    private static double numberAt(RscpValue tree, int tag) {
        Number number = Objects.requireNonNull(tree.findNumber(tag), "no numeric value for " + RscpTag.nameOf(tag));
        return number.doubleValue();
    }

    private static RscpValue firstOf(List<RscpValue> values) {
        assertEquals(1, values.size(), "expected exactly one item");
        return Objects.requireNonNull(values.get(0));
    }

    // ------------------------------------------------------------------ encoding

    @Test
    void encodedFrameFollowsTheDocumentedLayout() throws Exception {
        byte[] encoded = RscpCodec.encodeFrame(List.of(RscpValue.none(RscpTag.EMS_REQ_POWER_PV)));

        // the control field is checked as raw bytes: reading it as a little endian short hides a wrong
        // byte order, which is exactly how the version byte was lost before
        assertEquals((byte) 0xE3, encoded[0], "frame magic, first byte");
        assertEquals((byte) 0xDC, encoded[1], "frame magic, second byte");
        assertEquals((byte) 0x00, encoded[2], "control byte 0 is zero");
        assertEquals((byte) 0x11, encoded[3], "control byte 1 carries version and CRC flag");
        assertTrue((encoded[3] & RscpCodec.CTRL_CRC) != 0, "the CRC flag must be set");
        assertEquals(RscpCodec.VERSION, encoded[3] & 0xFF, "the announced control value");

        ByteBuffer buffer = ByteBuffer.wrap(encoded).order(ByteOrder.LITTLE_ENDIAN);
        buffer.getShort();
        buffer.getShort();
        buffer.getLong();
        buffer.getInt();
        int payloadLength = buffer.getShort() & 0xFFFF;

        assertEquals(ITEM_HEADER, payloadLength, "one tag only item is 7 bytes");
        assertEquals(RscpCodec.HEADER_LENGTH + payloadLength + 4, encoded.length,
                "the frame is header plus payload plus the CRC trailer");
        assertEquals(RscpTag.EMS_REQ_POWER_PV, buffer.getInt(), "the requested tag");
        assertEquals(RscpType.NONE.getCode(), buffer.get() & 0xFF, "a request carries the NONE type");
        assertEquals(0, buffer.getShort() & 0xFFFF, "and no value");
    }

    /**
     * A frame carries a list of elements side by side. Wrapping them in a container would announce an
     * outer tag the interface does not have, which is what the device answered with NOT_HANDLED before.
     */
    @Test
    void frameElementsAreSiblingsWithoutAWrappingContainer() throws Exception {
        byte[] encoded = RscpCodec.encodeFrame(
                List.of(RscpValue.none(RscpTag.EMS_REQ_POWER_PV), RscpValue.none(RscpTag.EMS_REQ_BAT_SOC)));

        List<RscpValue> decoded = RscpCodec.decodeFrame(encoded);

        assertEquals(2, decoded.size(), "both elements are top level items");
        assertEquals(RscpTag.EMS_REQ_POWER_PV, decoded.get(0).getTag());
        assertEquals(RscpTag.EMS_REQ_BAT_SOC, decoded.get(1).getTag());
        assertEquals(RscpType.NONE, decoded.get(0).getType(), "a request element carries no value");
        // the payload is header plus the two items and the CRC, with no room for a container in between
        assertEquals(RscpCodec.HEADER_LENGTH + 2 * ITEM_HEADER + 4, encoded.length);
    }

    @Test
    void everyEncodedFrameCarriesAValidCrc() throws Exception {
        byte[] encoded = RscpCodec.encodeFrame(List.of(RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_USER, "someone")));

        CRC32 crc = new CRC32();
        crc.update(encoded, 0, encoded.length - 4);
        int stored = ByteBuffer.wrap(encoded, encoded.length - 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();

        assertEquals((int) crc.getValue(), stored, "the CRC must cover the whole frame but itself");
    }

    @Test
    void aStringIsEncodedWithoutATerminator() throws Exception {
        byte[] encoded = RscpCodec.encodeFrame(List.of(RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_USER, "abc")));

        ByteBuffer buffer = ByteBuffer.wrap(encoded).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(RscpCodec.HEADER_LENGTH + 4);
        assertEquals(RscpType.CSTRING.getCode(), buffer.get() & 0xFF);
        assertEquals(3, buffer.getShort() & 0xFFFF, "the three characters and nothing else");

        byte[] text = new byte[3];
        buffer.get(text);
        assertArrayEquals(new byte[] { 'a', 'b', 'c' }, text,
                "the device expects the bare text, a trailing NUL is not sent");
    }

    @Test
    void aContainerLengthCoversItsChildren() throws Exception {
        RscpValue container = RscpValue.container(RscpTag.RSCP_REQ_AUTHENTICATION,
                List.of(RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_USER, "u"),
                        RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_PASSWORD, "p")));
        byte[] encoded = RscpCodec.encodeFrame(List.of(container));

        ByteBuffer buffer = ByteBuffer.wrap(encoded).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(RscpCodec.HEADER_LENGTH);
        buffer.getInt();
        assertEquals(RscpType.CONTAINER.getCode(), buffer.get() & 0xFF);
        int containerLength = buffer.getShort() & 0xFFFF;

        // each child is a 7 byte header plus one byte of value, because strings carry no terminator
        assertEquals(2 * (ITEM_HEADER + 1), containerLength);
        assertEquals(RscpCodec.HEADER_LENGTH + ITEM_HEADER + containerLength + 4, encoded.length,
                "the frame payload is the container header plus its children");
    }

    // ------------------------------------------------------------------ decoding

    @Test
    void decodingRecoversTagTypeAndValue() {
        byte[] payload = item(RscpTag.EMS_POWER_PV, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4711).array());

        List<RscpValue> values = RscpCodec.decodeFrame(frame(payload, true));

        assertEquals(1, values.size());
        RscpValue value = values.get(0);
        assertEquals(RscpTag.EMS_POWER_PV, value.getTag());
        assertEquals(RscpType.INT32, value.getType());
        assertEquals(4711, numberAt(value, RscpTag.EMS_POWER_PV));
    }

    @Test
    void decodedContainersExposeTheirChildren() {
        byte[] child = item(RscpTag.EMS_MAX_CHARGE_POWER, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(9000).array());
        byte[] payload = item(RscpTag.EMS_GET_POWER_SETTINGS, RscpType.CONTAINER.getCode(), child);

        RscpValue root = firstOf(RscpCodec.decodeFrame(frame(payload, true)));

        assertEquals(RscpType.CONTAINER, root.getType());
        assertEquals(1, root.getChildren().size());
        assertEquals(9000, numberAt(root, RscpTag.EMS_MAX_CHARGE_POWER),
                "a nested value is reachable from the container");
    }

    @Test
    void anErrorItemBecomesTheCorrespondingErrorCode() {
        // RSCP_ERR_UNKNOWN_TAG is what a device answers for a data point it does not implement
        byte[] payload = item(RscpTag.EMS_REQ_BAT_SOC, RscpType.ERROR.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(7).array());

        RscpValue root = firstOf(RscpCodec.decodeFrame(frame(payload, true)));

        assertTrue(root.isError());
        assertEquals(RscpError.UNKNOWN_TAG, root.getError());
        assertTrue(root.containsError());
        assertEquals(RscpError.UNKNOWN_TAG, root.findError());
    }

    @Test
    void aFrameWithoutTheCrcBitIsDecodedWithoutATrailer() {
        byte[] payload = item(RscpTag.EMS_BAT_SOC, RscpType.DOUBLE64.getCode(),
                ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(42.5).array());

        RscpValue root = firstOf(RscpCodec.decodeFrame(frame(payload, false)));

        assertEquals(42.5, numberAt(root, RscpTag.EMS_BAT_SOC), 0.0001);
    }

    @Test
    void everyScalarTypeRoundTrips() throws Exception {
        record Case(int type, Object value, double expected) {
        }
        List<Case> cases = List.of(//
                new Case(RscpType.BOOL.getCode(), Boolean.TRUE, 1), //
                new Case(RscpType.UCHAR8.getCode(), 200, 200), //
                new Case(RscpType.INT16.getCode(), -3000, -3000), //
                new Case(RscpType.UINT16.getCode(), 60000, 60000), //
                new Case(RscpType.INT32.getCode(), -70000, -70000), //
                new Case(RscpType.FLOAT32.getCode(), 1.5f, 1.5), //
                new Case(RscpType.DOUBLE64.getCode(), -2.25d, -2.25));

        for (Case testCase : cases) {
            RscpValue encoded = RscpValue.of(1000, RscpType.fromCode(testCase.type()), testCase.value());
            RscpValue decoded = firstOf(RscpCodec.decodeFrame(RscpCodec.encodeFrame(List.of(encoded))));
            RscpValue item = decoded.find(1000);
            assertNotNull(item, "the item must survive the round trip, but the frame only held " + decoded);

            // a BOOL decodes to a boolean, everything else to a number
            Object value = item.getValue();
            assertNotNull(value, "the decoded item must carry a value");
            double actual = value instanceof Boolean flag ? (flag ? 1 : 0) : ((Number) value).doubleValue();
            assertEquals(testCase.expected(), actual, 0.0001,
                    "type " + decoded.getType() + " did not survive the round trip");
        }
    }

    // ------------------------------------------------------------------ stream reading

    @Test
    void aFrameIsReadCompletelyFromAStream() throws Exception {
        byte[] payload = item(RscpTag.EMS_POWER_PV, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array());
        byte[] encoded = frame(payload, true);

        byte[] read = RscpCodec.readFrame(new ByteArrayInputStream(encoded));

        assertArrayEquals(encoded, read);
    }

    @Test
    void aStreamThatDeliversOneByteAtATimeIsStillReadCompletely() throws Exception {
        byte[] payload = item(RscpTag.EMS_POWER_PV, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(2).array());
        byte[] encoded = frame(payload, true);

        // an InputStream that returns a single byte per call, which must not confuse the reader
        var trickle = new ByteArrayInputStream(encoded) {
            @Override
            public int read(byte @Nullable [] bytes, int offset, int length) {
                return super.read(Objects.requireNonNull(bytes), offset, Math.min(1, length));
            }
        };
        assertArrayEquals(encoded, RscpCodec.readFrame(trickle));
    }

    @Test
    void aTruncatedFrameIsRejected() {
        byte[] payload = item(RscpTag.EMS_POWER_PV, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(3).array());
        byte[] encoded = frame(payload, true);
        byte[] truncated = java.util.Arrays.copyOf(encoded, encoded.length - 4);

        assertThrows(java.io.EOFException.class, () -> RscpCodec.readFrame(new ByteArrayInputStream(truncated)),
                "a short stream must not be reported as a complete frame");
    }

    @Test
    void aWrongMagicIsRejected() {
        byte[] payload = item(RscpTag.EMS_POWER_PV, RscpType.INT32.getCode(), new byte[4]);
        byte[] encoded = frame(payload, true);
        encoded[0] = 0x00;
        encoded[1] = 0x00;

        java.io.IOException exception = assertThrows(java.io.IOException.class,
                () -> RscpCodec.readFrame(new ByteArrayInputStream(encoded)));
        String message = String.valueOf(exception.getMessage());
        assertTrue(message.contains("magic"), "the failure must name the magic: " + exception);
    }

    @Test
    void aCorruptedPayloadIsDetectedByTheCrc() {
        byte[] payload = item(RscpTag.EMS_POWER_PV, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4).array());
        byte[] encoded = frame(payload, true);
        encoded[RscpCodec.HEADER_LENGTH] ^= 0x01;

        java.io.IOException exception = assertThrows(java.io.IOException.class,
                () -> RscpCodec.readFrame(new ByteArrayInputStream(encoded)));
        String message = String.valueOf(exception.getMessage());
        assertTrue(message.contains("CRC"), "the failure must name the CRC: " + exception);
    }

    @Test
    void aPayloadTooLargeForTheLengthFieldIsRejected() {
        byte[] huge = new byte[0x10000];
        RscpValue oversized = RscpValue.of(1, RscpType.BYTEARRAY, huge);

        assertThrows(java.io.IOException.class, () -> RscpCodec.encodeFrame(List.of(oversized)));
    }

    @Test
    void utf8TextSurvivesTheRoundTrip() throws Exception {
        // written as escapes so the file stays ASCII and cannot be mangled by an editor
        String text = "Gr\u00fc\u00dfe aus M\u00fcnchen";
        RscpValue decoded = firstOf(RscpCodec
                .decodeFrame(RscpCodec.encodeFrame(List.of(RscpValue.cstring(RscpTag.INFO_SW_RELEASE, text)))));

        byte[] raw = decoded.asBytes();
        assertNotNull(raw, "the decoded value must be a byte array");
        assertEquals(text, new String(raw, StandardCharsets.UTF_8),
                "the whole value is the text, without a terminator");
    }
}
