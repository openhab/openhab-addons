package org.openhab.binding.regoheatpump.internal.protocol;

class ErrorLineResponseParser extends StringResponseParser {

    @Override
    protected String convert(byte[] responseBytes) {
        // TODO: when testing with real device implement support
        // since currently I don't have any real data/packet.
        return ValueConverter.stringFromBytes(responseBytes, 1);
    }
}
