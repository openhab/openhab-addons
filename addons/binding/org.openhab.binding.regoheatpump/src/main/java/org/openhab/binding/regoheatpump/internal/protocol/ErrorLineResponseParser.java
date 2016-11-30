package org.openhab.binding.regoheatpump.internal.protocol;

class ErrorLineResponseParser extends AbstractLongResponseParser<ErrorLine> {

    @Override
    protected ErrorLine convert(byte[] responseBytes) {
        // 255 marks no error.
        if (responseBytes[1] == (byte) 255) {
            return null;
        }

        return new ErrorLine(responseBytes[1], ValueConverter.stringFromBytes(responseBytes, 2, 15));
    }
}
