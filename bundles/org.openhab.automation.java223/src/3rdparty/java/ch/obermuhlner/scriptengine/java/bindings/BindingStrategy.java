package ch.obermuhlner.scriptengine.java.bindings;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The strategy used to set/get the Bindings around invoke
 */
public interface BindingStrategy {

    void associateBindings(@NonNull Class<?> compiledClass, @NonNull Object compiledInstance,
            @NonNull Map<String, Object> mergedBindings);

    @NonNull
    Map<String, Object> retrieveBindings(@NonNull Class<?> compiledClass, @NonNull Object compiledInstance);
}
