package org.openhab.binding.ivtheatpump.internal.protocol;

abstract class AbstractResponseParser<T> implements ResponseParser<T> {
    @Override
    public abstract int responseLength();

    protected abstract T convert(byte[] responseBytes);

    @Override
    public T parse(byte[] buffer) {
        if (buffer == null) {
            throw new NullPointerException();
        }

        if (buffer.length != responseLength()) {
            throw new IllegalStateException("Expected size does not match: " + buffer.length + "!=" + responseLength());
        }

        if (buffer[0] != ComputerAddress) {
            throw new IllegalStateException("Invalid header received " + buffer[0]);
        }

        if (Checksum.calculate(buffer, 1, responseLength() - 2) != buffer[responseLength() - 1]) {
            throw new IllegalStateException("Invalid crc received.");
        }

        return convert(buffer);
    }
}
