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
package org.openhab.binding.herzborg.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.herzborg.internal.dto.HerzborgProtocol.Function;
import org.openhab.binding.herzborg.internal.dto.HerzborgProtocol.Packet;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Bus} is a handy base class, implementing data communication with Herzborg devices.
 *
 * @author Pavel Fedin - Initial contribution
 */
@NonNullByDefault
public class Bus {
    private final Logger logger = LoggerFactory.getLogger(Bus.class);

    protected @Nullable InputStream dataIn;
    protected @Nullable OutputStream dataOut;

    public static class Result {
        ThingStatusDetail code;
        @Nullable
        String message;

        Result(ThingStatusDetail code, String msg) {
            this.code = code;
            this.message = msg;
        }

        Result(ThingStatusDetail code) {
            this.code = code;
        }
    }

    public Bus() {
        // Nothing to do here
    }

    private void safeClose(@Nullable Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                logger.debug("Error closing I/O stream: {}", e.getMessage());
            }
        }
    }

    public void dispose() {
        safeClose(dataOut);
        safeClose(dataIn);

        dataOut = null;
        dataIn = null;
    }

    public synchronized @Nullable Packet doPacket(Packet pkt) throws IOException {
        OutputStream dataOut = this.dataOut;
        InputStream dataIn = this.dataIn;

        if (dataOut == null || dataIn == null) {
            return null;
        }

        int readLength = Packet.MIN_LENGTH;

        switch (pkt.getFunction()) {
            case Function.READ:
                // The reply will include data itself
                readLength += pkt.getDataLength();
                break;
            case Function.WRITE:
                // The reply is number of bytes written
                readLength += 1;
                break;
            case Function.CONTROL:
                // The whole packet will be echoed back
                readLength = pkt.getBuffer().length;
                break;
            default:
                // We must not have anything else here
                throw new IllegalStateException("Unknown function code");
        }

        dataOut.write(pkt.getBuffer());

        int readOffset = 0;
        byte[] replyBuffer = new byte[readLength];

        while (readLength > 0) {
            int n = dataIn.read(replyBuffer, readOffset, readLength);

            if (n < 0) {
                throw new IOException("EOF from serial port");
            } else if (n == 0) {
                throw new IOException("Serial read timeout");
            }

            readOffset += n;
            readLength -= n;
        }

        return new Packet(replyBuffer);
    }

    public void flush() throws IOException {
        InputStream dataIn = this.dataIn;

        if (dataIn != null) {
            // Unfortunately Java streams can't be flushed. Just read and drop all the characters
            while (dataIn.available() > 0) {
                dataIn.read();
            }
        }
    }
}
