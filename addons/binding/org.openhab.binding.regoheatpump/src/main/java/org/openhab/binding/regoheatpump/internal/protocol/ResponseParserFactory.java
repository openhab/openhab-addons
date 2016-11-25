package org.openhab.binding.regoheatpump.internal.protocol;

public class ResponseParserFactory {
    public static final ResponseParser<Short> Short = new ShortResponseParser();
    public static final ResponseParser<String> String = new StringResponseParser();
    public static final ResponseParser<ErrorLine> ErrorLine = new ErrorLineResponseParser();
}
