package org.openhab.binding.ivtheatpump.internal.protocol;

class ShortResponseParser extends AbstractResponseParser<Short> {

    @Override
    public int responseLength() {
        return 5;
    }

    @Override
    protected Short convert(byte[] responseBytes) {
        return ValueConverter.sevenBitFormatToShort(responseBytes, 1);
    }
}
