package org.openhab.binding.regoheatpump.internal.protocol;

abstract class AbstractLongResponseParser<T> extends AbstractResponseParser<T> {
    @Override
    public int responseLength() {
        return 42;
    }
}
