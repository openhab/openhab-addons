package ch.obermuhlner.scriptengine.java.construct;

import javax.script.ScriptException;

/**
 * The strategy used to construct an instance of a {@link Class}.
 */
public interface ConstructorStrategy {
    /**
     * Constructs an instance of a {@link Class}.
     *
     * @param clazz the {@link Class}
     * @return the constructed instance or {@code null}
     * @throws ScriptException if the instance could not be constructed
     */
    Object construct(Class<?> clazz) throws ScriptException;
}
