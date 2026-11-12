package ch.obermuhlner.scriptengine.java;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;


/**
 * The compiled Java script created by a {@link JavaScriptEngine}.
 */
public class JavaCompiledScript extends CompiledScript {
    private final JavaScriptEngine engine;
    private final Class<?> compiledClass;
    private final Object compiledInstance;

    /**
     * Construct a {@link JavaCompiledScript}.
     * JAVA223 modification: public constructor accessor
     *
     * @param engine the {@link JavaScriptEngine} that compiled this script
     * @param compiledClass the compiled {@link Class}
     * @param compiledInstance the instance of the compiled {@link Class} or {@code null}
     *                         if no instance was created and only static methods will be called.
     */
    public JavaCompiledScript(JavaScriptEngine engine, Class<?> compiledClass, Object compiledInstance) {
        this.engine = engine;
        this.compiledClass = compiledClass;
        this.compiledInstance = compiledInstance;
    }

    /**
     * Returns the compiled {@link Class}.
     *
     * @return the compiled {@link Class}.
     */
    public Class<?> getCompiledClass() {
        return compiledClass;
    }

    /**
     * Returns the instance of the compiled {@link Class}.
     *
     * @return the instance of the compiled {@link Class} or {@code null}
     *         if no instance was created and only static methods will be called.
     */
    public Object getCompiledInstance() {
        return compiledInstance;
    }

    @Override
    public ScriptEngine getEngine() {
        return engine;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        // Overridden by child class
        return null;
    }
}
