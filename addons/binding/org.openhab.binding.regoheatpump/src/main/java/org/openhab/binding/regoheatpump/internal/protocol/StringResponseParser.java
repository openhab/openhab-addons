package org.openhab.binding.regoheatpump.internal.protocol;

class StringResponseParser extends AbstractLongResponseParser<String> {

    @Override
    protected String convert(byte[] responseBytes) {
        return ValueConverter.stringFromBytes(responseBytes, 1, 20);
    }
}
