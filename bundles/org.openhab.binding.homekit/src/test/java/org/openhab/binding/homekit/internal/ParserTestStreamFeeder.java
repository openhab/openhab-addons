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
package org.openhab.binding.homekit.internal;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Test helper class for {@link org.openhab.binding.homekit.internal.session.HttpPayloadParser}
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ParserTestStreamFeeder {

    final PipedInputStream in;
    final PipedOutputStream out;

    ParserTestStreamFeeder() throws IOException {
        this.in = new PipedInputStream(8192);
        this.out = new PipedOutputStream(in);
    }

    void feed(byte[] frame) throws IOException {
        out.write(frame);
        out.flush();
    }

    void feedAll(Iterable<byte[]> frames) throws IOException {
        for (byte[] frame : frames) {
            feed(frame);
        }
    }

    void close() throws IOException {
        out.close();
        in.close();
    }
}
