package org.openhab.binding.ivtheatpump.internal.protocol;

public class ResponseParserFactory {
    public static final ResponseParser<Short> Short = new ShortResponseParser();
    public static final ResponseParser<String> String = new StringResponseParser();
    public static final ResponseParser<String> ErrorLine = new ErrorLineResponseParser();
}
