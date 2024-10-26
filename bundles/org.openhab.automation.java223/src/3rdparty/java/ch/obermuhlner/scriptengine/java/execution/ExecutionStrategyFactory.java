package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;

/**
 * The factory for the execution strategy used to execute a method of a class instance.
 */
public interface ExecutionStrategyFactory {
    /**
     * Creates an {@link ExecutionStrategy} for the specified {@link Class}.
     *
     * @param clazz the {@link Class}
     * @return the {@link ExecutionStrategy}
     * @throws ScriptException if the {@link ExecutionStrategy} could not be created
     */
    public ExecutionStrategy create(Class<?> clazz) throws ScriptException;
}
