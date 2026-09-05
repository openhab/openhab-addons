package ch.obermuhlner.scriptengine.java.compilation;

/**
 * This strategy allows to modify a script before compiling it.
 */
public interface ScriptInterceptorStrategy {

    public String intercept(String script);
    
}
