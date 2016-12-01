package org.openhab.binding.regoheatpump.internal.protocol;

public interface ResponseParser<T> {
    public int responseLength();

    public T parse(byte[] buffer);
}
