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

import static org.openhab.binding.echonetlite.internal.LangUtil.b;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetMessageBuilder {
    private static final byte EHD_1 = 0x10;
    private static final byte EHD_2 = (byte) (0x81 & 0xFF);

    private final ByteBuffer buffer;
    private final ByteBuffer edtBuffer = ByteBuffer.allocate(4096);
    private int opcPosition = 0;
    @Nullable
    private InetSocketAddress destAddress;

    public EchonetMessageBuilder() {
        buffer = ByteBuffer.allocateDirect(4096).order(ByteOrder.BIG_ENDIAN);
    }

    public void start(short tid, InstanceKey source, InstanceKey dest, Esv service) {
        // 1081000005ff010ef0006201d60100
        // 1081000105ff010ef0006201d600
        // 0000 10 81 00 00 05 ff 01 0e f0 00 62 01 d6 01 00
        // 0000 10 81 00 01 05 ff 01 0e f0 00 62 01 d6 00

        destAddress = dest.address;

        buffer.clear();
        buffer.put(EHD_1);
        buffer.put(EHD_2);
        buffer.putShort(tid);
        buffer.put(b(source.klass.groupCode()));
        buffer.put(b(source.klass.classCode()));
        buffer.put(b(source.instance));
        buffer.put(b(dest.klass.groupCode()));
        buffer.put(b(dest.klass.classCode()));
        buffer.put(b(dest.instance));
        buffer.put(service.code());

        opcPosition = buffer.position();
        buffer.put((byte) 0);
    }

    private void incrementOpc() {
        buffer.put(opcPosition, (byte) (buffer.get(opcPosition) + 1));
    }

    public void append(final byte edt, final byte length, final byte value) {
        buffer.put(edt).put(length).put(value);
        incrementOpc();
    }

    public void appendEpcRequest(final int epc) {
        buffer.put(b(epc)).put((byte) 0);
        incrementOpc();
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    @Nullable
    public SocketAddress address() {
        return destAddress;
    }

    public ByteBuffer edtBuffer() {
        edtBuffer.clear();
        return edtBuffer;
    }

    public void appendEpcUpdate(final int epc, ByteBuffer edtBuffer) {
        if (edtBuffer.remaining() < 0 || 255 < edtBuffer.remaining()) {
            throw new IllegalArgumentException("Invalid update value, length: " + edtBuffer.remaining());
        }

        buffer.put(b(epc)).put(b(edtBuffer.remaining())).put(edtBuffer);
        incrementOpc();
    }
}
