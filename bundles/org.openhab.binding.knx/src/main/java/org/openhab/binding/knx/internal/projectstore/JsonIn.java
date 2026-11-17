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
import java.io.Reader;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reads JSON one token at a time. Every deviation from the JSON grammar, a value of another kind than asked for,
 * nesting deeper than the limit and more characters than the limit end in a {@link StoredFileException}, whose
 * text never holds anything read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class JsonIn {

    private static final int NONE = -2;
    private static final int MAX_NUMBER_LENGTH = 11;

    private final Reader in;
    private final long maxChars;
    private final boolean[] isObject;
    private final boolean[] isFirst;
    private long chars;
    private int peeked = NONE;
    private int depth;
    private boolean afterName;
    private boolean topLevelRead;

    JsonIn(Reader in, long maxChars, int maxDepth) {
        this.in = in;
        this.maxChars = maxChars;
        this.isObject = new boolean[maxDepth];
        this.isFirst = new boolean[maxDepth];
    }

    void beginObject() throws IOException {
        beforeValue();
        open('{', true);
    }

    void endObject() throws IOException {
        close('}', true);
    }

    void beginArray() throws IOException {
        beforeValue();
        open('[', false);
    }

    void endArray() throws IOException {
        close(']', false);
    }

    /** {@return true if the object or array that is open holds a further name or value} */
    boolean hasNext() throws IOException {
        int c = peekNonSpace();
        return c != '}' && c != ']';
    }

    String nextName() throws IOException {
        if (depth == 0 || !isObject[depth - 1] || afterName) {
            throw new StoredFileException();
        }
        item();
        String name = string();
        expect(':');
        afterName = true;
        return name;
    }

    String nextString() throws IOException {
        beforeValue();
        if (peekNonSpace() != '"') {
            throw new StoredFileException();
        }
        return string();
    }

    int nextInt() throws IOException {
        beforeValue();
        peekNonSpace();
        StringBuilder text = new StringBuilder();
        if (peek() == '-') {
            text.append((char) read());
        }
        while (peek() >= '0' && peek() <= '9') {
            text.append((char) read());
            if (text.length() > MAX_NUMBER_LENGTH) {
                throw new StoredFileException();
            }
        }
        int c = peek();
        if (text.isEmpty() || "-".contentEquals(text) || c == '.' || c == 'e' || c == 'E') {
            throw new StoredFileException();
        }
        long value = Long.parseLong(text.toString());
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new StoredFileException();
        }
        return (int) value;
    }

    boolean nextBoolean() throws IOException {
        beforeValue();
        int c = peekNonSpace();
        if (c == 't') {
            literal("true");
            return true;
        } else if (c == 'f') {
            literal("false");
            return false;
        }
        throw new StoredFileException();
    }

    /** Reads over the next value, whatever its kind. */
    void skipValue() throws IOException {
        beforeValue();
        int c = peekNonSpace();
        if (c == '{') {
            open('{', true);
            while (hasNext()) {
                nextName();
                skipValue();
            }
            endObject();
        } else if (c == '[') {
            open('[', false);
            while (hasNext()) {
                skipValue();
            }
            endArray();
        } else if (c == '"') {
            string();
        } else if (c == 't') {
            literal("true");
        } else if (c == 'f') {
            literal("false");
        } else if (c == 'n') {
            literal("null");
        } else if (c == '-' || (c >= '0' && c <= '9')) {
            int length = 0;
            while ((peek() >= '0' && peek() <= '9') || "+-.eE".indexOf(peek()) >= 0) {
                read();
                if (++length > 64) {
                    throw new StoredFileException();
                }
            }
        } else {
            throw new StoredFileException();
        }
    }

    /** Checks that nothing but white space follows the value read. */
    void end() throws IOException {
        if (depth != 0 || !topLevelRead || peekNonSpace() != -1) {
            throw new StoredFileException();
        }
    }

    private void beforeValue() throws IOException {
        if (depth == 0) {
            if (topLevelRead) {
                throw new StoredFileException();
            }
            topLevelRead = true;
        } else if (isObject[depth - 1]) {
            if (!afterName) {
                throw new StoredFileException();
            }
            afterName = false;
        } else {
            item();
        }
    }

    private void item() throws IOException {
        if (isFirst[depth - 1]) {
            isFirst[depth - 1] = false;
        } else {
            expect(',');
        }
    }

    private void open(char bracket, boolean object) throws IOException {
        expect(bracket);
        if (depth == isObject.length) {
            throw new StoredFileException();
        }
        isObject[depth] = object;
        isFirst[depth] = true;
        depth++;
    }

    private void close(char bracket, boolean object) throws IOException {
        if (depth == 0 || isObject[depth - 1] != object || afterName) {
            throw new StoredFileException();
        }
        expect(bracket);
        depth--;
    }

    private String string() throws IOException {
        expect('"');
        StringBuilder text = new StringBuilder();
        while (true) {
            int c = read();
            if (c == '"') {
                return text.toString();
            } else if (c == '\\') {
                text.append(escaped());
            } else if (c < 0x20) {
                throw new StoredFileException();
            } else {
                text.append((char) c);
            }
        }
    }

    private char escaped() throws IOException {
        int c = read();
        return switch (c) {
            case '"', '\\', '/' -> (char) c;
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> {
                int value = 0;
                for (int i = 0; i < 4; i++) {
                    int digit = Character.digit(read(), 16);
                    if (digit < 0) {
                        throw new StoredFileException();
                    }
                    value = value * 16 + digit;
                }
                yield (char) value;
            }
            default -> throw new StoredFileException();
        };
    }

    private void literal(String word) throws IOException {
        for (int i = 0; i < word.length(); i++) {
            if (read() != word.charAt(i)) {
                throw new StoredFileException();
            }
        }
    }

    private void expect(char c) throws IOException {
        if (peekNonSpace() != c) {
            throw new StoredFileException();
        }
        read();
    }

    private int peekNonSpace() throws IOException {
        int c = peek();
        while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            read();
            c = peek();
        }
        return c;
    }

    private int peek() throws IOException {
        if (peeked == NONE) {
            peeked = in.read();
            if (peeked >= 0 && ++chars > maxChars) {
                throw new StoredFileException();
            }
        }
        return peeked;
    }

    private int read() throws IOException {
        int c = peek();
        if (c < 0) {
            throw new StoredFileException();
        }
        peeked = NONE;
        return c;
    }
}
