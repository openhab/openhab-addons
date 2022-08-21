package org.openhab.binding.freeboxos.internal.api.home;

public class EndpointGenericValue<T> {

    private T value;

    public EndpointGenericValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
