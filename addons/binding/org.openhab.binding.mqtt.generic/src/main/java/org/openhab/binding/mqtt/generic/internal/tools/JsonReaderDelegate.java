package org.openhab.binding.mqtt.generic.internal.tools;

import java.io.IOException;
import java.io.StringReader;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class JsonReaderDelegate extends JsonReader {

    public static JsonReader getDelegate(final JsonReader in) {
        JsonReader current = in;
        while (current instanceof JsonReaderDelegate) {
            current = ((JsonReaderDelegate) current).delegate;
        }
        return current;
    }

    private final JsonReader delegate;

    public JsonReaderDelegate(JsonReader delegate) {
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
    public boolean equals(Object obj) {
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