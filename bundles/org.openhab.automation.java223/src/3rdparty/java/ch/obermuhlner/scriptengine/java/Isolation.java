package ch.obermuhlner.scriptengine.java;

/**
 * The isolation levels of the script at execution time.
 */
public enum Isolation {
    /**
     * The caller {@link ClassLoader} is visible to the script during execution.
     *
     * This allows to see all classes from the script that are visible in the calling application.
     */
    CallerClassLoader,

    /**
     * The script executes in an isolated {@link ClassLoader}.
     *
     * This hides all classes of the calling application.
     */
    IsolatedClassLoader
}
