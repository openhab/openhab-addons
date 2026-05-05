package ch.obermuhlner.scriptengine.java.construct;

import javax.script.ScriptException;

/**
 * A {@link ConstructorStrategy} implementation that always returns {@code null}.
 *
 * Used to indicate that only static methods should be called to evaluate the
 * {@link ch.obermuhlner.scriptengine.java.JavaCompiledScript} holding the {@link Class}.
 */
public class NullConstructorStrategy implements ConstructorStrategy {
    @Override
    public Object construct(Class<?> clazz) throws ScriptException {
        return null;
    }
}
