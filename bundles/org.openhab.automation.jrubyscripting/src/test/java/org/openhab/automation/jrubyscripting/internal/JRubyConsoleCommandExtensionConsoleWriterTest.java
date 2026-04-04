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
package org.openhab.automation.jrubyscripting.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Writer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JRuby console output writer behavior.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class JRubyConsoleCommandExtensionConsoleWriterTest {

    @Test
    void preservesAnsiAndNormalizesLfToCrLf() throws Exception {
        RecordingWriter delegate = new RecordingWriter();
        Writer writer = new JRubyConsoleCommandExtension.ConsoleWriter(delegate);

        String output = "\u001B[32mgreen\u001B[0m\nplain";
        writer.write(output.toCharArray(), 0, output.length());
        writer.flush();

        assertEquals("\u001B[32mgreen\u001B[0m\r\nplain", delegate.output());
    }

    @Test
    void preservesExistingCrLfAndHandlesChunkBoundary() throws Exception {
        RecordingWriter delegate = new RecordingWriter();
        Writer writer = new JRubyConsoleCommandExtension.ConsoleWriter(delegate);

        writer.write("left\r".toCharArray(), 0, 5);
        writer.write("\nright\n".toCharArray(), 0, 7);
        writer.flush();

        assertEquals("left\r\nright\r\n", delegate.output());
    }

    private static final class RecordingWriter extends Writer {
        private final StringBuilder output = new StringBuilder();

        @Override
        public void write(char @Nullable [] cbuf, int off, int len) {
            if (cbuf != null) {
                output.append(cbuf, off, len);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        String output() {
            return output.toString();
        }
    }
}
