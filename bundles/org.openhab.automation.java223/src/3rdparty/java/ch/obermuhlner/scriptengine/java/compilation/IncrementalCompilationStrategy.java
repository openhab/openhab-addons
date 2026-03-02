package ch.obermuhlner.scriptengine.java.compilation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.tools.JavaFileObject;

import ch.obermuhlner.scriptengine.java.MemoryFileManager;

public class IncrementalCompilationStrategy implements CompilationStrategy {

    Map<String, JavaFileObject> previousFileObject = new HashMap<>();

    JavaFileObject currentJavaFileObject;

    @Override
    public List<JavaFileObject> getJavaFileObjectsToCompile(String simpleClassName, String currentSource) {
        currentJavaFileObject = MemoryFileManager.createSourceFileObject(null, simpleClassName, currentSource);
        Stream<JavaFileObject> previousFileObjects = previousFileObject.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(simpleClassName)) // do no keep the old file
                .map(Entry::getValue);
        return Stream.concat(previousFileObjects, Stream.of(currentJavaFileObject)).toList();
    }

    @Override
    public void compilationResult(Class<?> clazz) {
        previousFileObject.put(clazz.getSimpleName(), currentJavaFileObject);
    }

}
