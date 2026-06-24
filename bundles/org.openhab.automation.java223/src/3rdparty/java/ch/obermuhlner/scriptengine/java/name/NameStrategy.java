package ch.obermuhlner.scriptengine.java.name;

import javax.script.ScriptException;

/**
 * The strategy used to determine the name of a Java class in a script.
 */
public interface NameStrategy {
    /**
     * Returns the fully qualified name of the Java class in the specified script.
     *
     * @param script the Java script
     * @return the fully qualified class name
     * @throws ScriptException if no class name could be determined
     */
    String getFullName(String script) throws ScriptException;

    /**
     * Extracts the simple name from a fully qualified class name.
     *
     * @param fullName the fully qualified class name
     * @return the simple class name
     */
    static String extractSimpleName(String fullName) {
        int lastDotIndex = fullName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return fullName;
        }
        return fullName.substring(lastDotIndex + 1);
    }

    /**
     * Extracts the package name from a fully qualified class name.
     *
     * @param fullName the fully qualified class name
     * @return the package name, {@code ""} if it is the default package
     */
    static String extractPackageName(String fullName) {
        int lastDotIndex = fullName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return "";
        }
        return fullName.substring(0, lastDotIndex);
    }
}
