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
package org.openhab.binding.mqtt.generic.tools;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * JsonReader delegate.
 *
 * This class allows to overwrite parts of the {@link JsonReader} functionality
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public class JsonReaderDelegate extends JsonReader {

    /**
     * Retrieve the 'original' {@link JsonReader} after removing all {@link JsonReaderDelegate}s
     *
     * @param in the current {@link JsonReader}
     * @return the original {@link JsonReader} after removing all {@link JsonReaderDelegate}s
     */
    public static JsonReader getDelegate(final JsonReader in) {
        JsonReader current = in;
        while (current instanceof JsonReaderDelegate) {
            current = ((JsonReaderDelegate) current).delegate;
        }
        return current;
    }

    private final JsonReader delegate;

    public JsonReaderDelegate(JsonReader delegate) {
        /* super class demands a Reader. This will never be used as all requests are forwarded to the delegate */
        super(new StringReader(""));
        this.delegate = delegate;
    }

    @Override
    public void beginArray() throws IOException {
        delegate.beginArray();
    }

    @Override
    public void beginObject() throws IOException {
        delegate.beginObject();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void endArray() throws IOException {
        delegate.endArray();
    }

    @Override
    public void endObject() throws IOException {
        delegate.endObject();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public boolean hasNext() throws IOException {
        return delegate.hasNext();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean nextBoolean() throws IOException {
        return delegate.nextBoolean();
    }

    @Override
    public double nextDouble() throws IOException {
        return delegate.nextDouble();
    }

    @Override
    public int nextInt() throws IOException {
        return delegate.nextInt();
    }

    @Override
    public long nextLong() throws IOException {
        return delegate.nextLong();
    }

    @Override
    public String nextName() throws IOException {
        return delegate.nextName();
    }

    @Override
    public void nextNull() throws IOException {
        delegate.nextNull();
    }

    @Override
    public String nextString() throws IOException {
        return delegate.nextString();
    }

    @Override
    public JsonToken peek() throws IOException {
        return delegate.peek();
    }

    @Override
    public void skipValue() throws IOException {
        delegate.skipValue();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
