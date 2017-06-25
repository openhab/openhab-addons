package org.openhab.binding.supla.internal.server.mappers;

public interface Mapper {
    String map(Object o);

    <T> T to(Class<T> clazz, String string);
}
