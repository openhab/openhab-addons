package org.openhab.binding.supla.internal.server.mappers;


import java.lang.reflect.Type;

public interface Mapper {
    String map(Object o);

    <T> T to(Class<T> clazz, String string);

    /**
     * I know that using TypeToken binds us to Gson implementation but I don't have time to think about it...
     *
     * @param clazz
     * @param string
     * @param <T>
     * @return
     */
    <T> T to(Type clazz, String string);
}
