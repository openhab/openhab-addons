package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;

/**
 * The strategy used to execute a method on an object instance.
 */
public interface ExecutionStrategy {
    /**
     * Executes a method on an object instance, or a static method if the specified instance is {@code null}.
     *
     * @param instance the object instance to be executed or {@code null} to execute a static method
     * @return the return value of the method, or {@code null}
     * @throws ScriptException if no method to execute was found
     */
    Object execute(Object instance) throws ScriptException;
}
