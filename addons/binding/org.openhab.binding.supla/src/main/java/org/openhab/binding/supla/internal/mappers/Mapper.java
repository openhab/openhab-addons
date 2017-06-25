package org.openhab.binding.supla.internal.mappers;


import java.lang.reflect.Type;

public interface Mapper {
    String map(Object o);

    <T> T to(Class<T> clazz, String string);

    <T> T to(Type clazz, String string);
}
