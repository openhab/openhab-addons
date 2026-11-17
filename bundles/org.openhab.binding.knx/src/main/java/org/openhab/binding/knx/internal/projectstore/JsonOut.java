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
package org.openhab.binding.knx.internal.projectstore;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Writes JSON without spaces or line breaks. The caller writes names and values in a valid order.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class JsonOut {

    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final char LINE_SEPARATOR = 0x2028;
    private static final char PARAGRAPH_SEPARATOR = 0x2029;

    private final Writer out;
    private boolean comma;

    JsonOut(Writer out) {
        this.out = out;
    }

    JsonOut beginObject() throws IOException {
        separate();
        out.write('{');
        comma = false;
        return this;
    }

    JsonOut endObject() throws IOException {
        out.write('}');
        comma = true;
        return this;
    }

    JsonOut beginArray() throws IOException {
        separate();
        out.write('[');
        comma = false;
        return this;
    }

    JsonOut endArray() throws IOException {
        out.write(']');
        comma = true;
        return this;
    }

    JsonOut name(String name) throws IOException {
        separate();
        quoted(name);
        out.write(':');
        comma = false;
        return this;
    }

    JsonOut value(String value) throws IOException {
        separate();
        quoted(value);
        comma = true;
        return this;
    }

    JsonOut value(long value) throws IOException {
        separate();
        out.write(Long.toString(value));
        comma = true;
        return this;
    }

    JsonOut value(boolean value) throws IOException {
        separate();
        out.write(value ? "true" : "false");
        comma = true;
        return this;
    }

    /** Writes the name and the value, or nothing for a null value. */
    JsonOut optional(String name, @Nullable String value) throws IOException {
        return value == null ? this : name(name).value(value);
    }

    /** Writes the name and the value, or nothing for a null value. */
    JsonOut optional(String name, @Nullable Integer value) throws IOException {
        return value == null ? this : name(name).value(value.longValue());
    }

    private void separate() throws IOException {
        if (comma) {
            out.write(',');
        }
    }

    // a lone surrogate is written as an escape: the UTF-8 encoder would replace it
    private void quoted(String text) throws IOException {
        out.write('"');
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') {
                out.write('\\');
                out.write(c);
            } else if (c < 0x20 || c == LINE_SEPARATOR || c == PARAGRAPH_SEPARATOR) {
                escape(c);
            } else if (Character.isHighSurrogate(c) && i + 1 < length && Character.isLowSurrogate(text.charAt(i + 1))) {
                out.write(c);
                out.write(text.charAt(++i));
            } else if (Character.isSurrogate(c)) {
                escape(c);
            } else {
                out.write(c);
            }
        }
        out.write('"');
    }

    private void escape(char c) throws IOException {
        out.write("\\u");
        out.write(HEX[(c >> 12) & 0xF]);
        out.write(HEX[(c >> 8) & 0xF]);
        out.write(HEX[(c >> 4) & 0xF]);
        out.write(HEX[c & 0xF]);
    }
}
