package org.openhab.binding.regoheatpump.internal.protocol;

class ErrorLineResponseParser extends AbstractLongResponseParser<ErrorLine> {

    @Override
    protected ErrorLine convert(byte[] responseBytes) {
        // 255 marks no error.
        if (responseBytes[1] == 255) {
            return null;
        }

        return new ErrorLine(responseBytes[1], ValueConverter.stringFromBytes(responseBytes, 2, 15), responseBytes[32],
                responseBytes[33], responseBytes[34], responseBytes[35]);
    }
}
