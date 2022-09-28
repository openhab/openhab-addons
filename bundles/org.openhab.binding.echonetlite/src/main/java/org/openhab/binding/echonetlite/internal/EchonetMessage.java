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

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetMessage {
    public static final int TID_OFFSET = 2;
    public static final int GROUP_OFFSET = 4;
    public static final int CLASS_OFFSET = 5;
    public static final int INSTANCE_OFFSET = 6;
    public static final int ESV_OFFSET = 10;
    public static final int OPC_OFFSET = 11;
    public static final int PROPERTY_OFFSET = 12;

    private final ByteBuffer messageData = ByteBuffer.allocateDirect(65536);
    private final ByteBuffer propertyData = messageData.duplicate();
    private int propertyCursor = 0;
    private int currentProperty = -1;

    @Nullable
    private SocketAddress address;

    public ByteBuffer bufferForRead() {
        reset();
        return messageData;
    }

    private void reset() {
        messageData.clear();
        messageData.order(ByteOrder.BIG_ENDIAN);
        propertyCursor = 0;
        currentProperty = -1;
    }

    public void sourceAddress(final SocketAddress address) {
        this.address = address;
    }

    public @Nullable SocketAddress sourceAddress() {
        return address;
    }

    public @Nullable EchonetClass sourceClass() {
        return EchonetClassIndex.INSTANCE.lookup(messageData.get(GROUP_OFFSET), messageData.get(CLASS_OFFSET));
    }

    public byte instance() {
        return messageData.get(INSTANCE_OFFSET);
    }

    public Esv esv() {
        return Esv.forCode(messageData.get(ESV_OFFSET));
    }

    public int numProperties() {
        return 0xFF & messageData.get(OPC_OFFSET);
    }

    public boolean moveNext() {
        if (propertyCursor < numProperties()) {
            propertyCursor++;
            if (-1 == currentProperty) {
                currentProperty = PROPERTY_OFFSET;
            } else {
                int pdc = 0xFF & messageData.get(currentProperty + 1);
                currentProperty = currentProperty + 2 + pdc;
            }
            return true;
        }

        return false;
    }

    public int currentEpc() {
        return messageData.get(currentProperty) & 0xFF;
    }

    public int currentPdc() {
        return messageData.get(currentProperty + 1) & 0xFF;
    }

    public ByteBuffer currentEdt() {
        propertyData.clear();
        propertyData.position(currentProperty + 2).limit(currentProperty + 2 + currentPdc());
        return propertyData;
    }

    public short tid() {
        return messageData.getShort(TID_OFFSET);
    }

    public String toDebug() {
        return "EchonetMessage{" + "sourceAddress=" + sourceAddress() + ", class=" + sourceClass() + ", instance="
                + instance() + ", num properties=" + numProperties() + ", data=" + dumpData() + '}';
    }

    private String dumpData() {
        final byte[] bs = new byte[messageData.limit()];
        final ByteBuffer duplicate = messageData.duplicate();
        duplicate.position(0).limit(messageData.limit());
        duplicate.get(bs);

        final StringBuilder sb = new StringBuilder();

        sb.append('[');
        for (byte b : bs) {
            sb.append("0x").append(Integer.toHexString(0xFF & b)).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(']');

        return sb.toString();
    }
}
