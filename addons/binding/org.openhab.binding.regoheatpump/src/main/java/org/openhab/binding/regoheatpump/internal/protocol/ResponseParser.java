package org.openhab.binding.regoheatpump.internal.protocol;

public interface ResponseParser<T> {
    public final static byte ComputerAddress = (byte) 0x01;

    public int responseLength();

    public T parse(byte[] buffer);
}
