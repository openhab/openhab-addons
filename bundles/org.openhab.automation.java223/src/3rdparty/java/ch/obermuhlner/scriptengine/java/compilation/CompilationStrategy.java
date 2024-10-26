package ch.obermuhlner.scriptengine.java.compilation;

import java.util.List;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This strategy is used to decide what to compile
 */
public interface CompilationStrategy {

    /**
     * Generate a list of JavaFileObject to compile
     *
     * @param simpleClassName the class name of the script
     * @param currentSource The current source script we want to execute
     * @return A list of java file object to compile alongside the main script
     */
    @NonNull
    List<JavaFileObject> getJavaFileObjectsToCompile(@NonNull String simpleClassName, @NonNull String currentSource);

    /**
     * As the script is compiled, this is an opportunity to see if we still want to
     * keep it.
     *
     * @param clazz
     */
    default void compilationResult(Class<?> clazz) {
    }

    /**
     * Get a file manager to use, during compilation, as a parent of the in-memory file manager
     *
     * @param parentJavaFileManager The parent javaFileManager of the returned file manager
     * @return a file manager. It will be the parent of the in-memory file manager managing the script.
     */
    default JavaFileManager getJavaFileManager(JavaFileManager parentJavaFileManager) {
        return null;
    }

}
