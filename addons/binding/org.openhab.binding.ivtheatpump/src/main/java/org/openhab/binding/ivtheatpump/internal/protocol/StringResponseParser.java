package org.openhab.binding.ivtheatpump.internal.protocol;

class StringResponseParser extends AbstractResponseParser<String> {

    @Override
    public int responseLength() {
        return 42;
    }

    @Override
    protected String convert(byte[] responseBytes) {
        return ValueConverter.stringFromBytes(responseBytes, 1);
    }

}
