package org.openhab.binding.ivtheatpump.internal.protocol;

public interface ResponseParser<T> {
    public final static byte ComputerAddress = (byte) 0x01;

    public int responseLength();

    public T parse(byte[] buffer);
}
