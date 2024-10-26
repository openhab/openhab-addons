package ch.obermuhlner.scriptengine.java.compilation;

import java.util.Collections;
import java.util.List;

import javax.tools.JavaFileObject;

import ch.obermuhlner.scriptengine.java.MemoryFileManager;

public class DefaultCompilationStrategy implements CompilationStrategy {

    @Override
    public List<JavaFileObject> getJavaFileObjectsToCompile(String simpleClassName, String currentSource) {
        return Collections
                .singletonList(MemoryFileManager.createSourceFileObject(null, simpleClassName, currentSource));
    }

}
