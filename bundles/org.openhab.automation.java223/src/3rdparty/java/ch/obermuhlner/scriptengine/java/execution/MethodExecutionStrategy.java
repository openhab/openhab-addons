package ch.obermuhlner.scriptengine.java.execution;

import ch.obermuhlner.scriptengine.java.util.ReflectionUtil;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link ExecutionStrategy} that executes a specific method.
 *
 * This implementation has three static constructor methods to define the method that should be called:
 * <ul>
 *      <li>{@link #byMethod(Method, Object...)}
 *      to call the specified method and pass it the specified arguments.</li>
 *      <li>{@link #byArgumentTypes(Class, String, Class[], Object...)}
 *      to call the public method with the
 *      specified argument types and pass it the specified arguments.</li>
 *      <li>{@link #byMatchingArguments(Class, String, Object...)}
 *      to call a public method that matches the specified arguments.</li>
 * </ul>
 */
public class MethodExecutionStrategy implements ExecutionStrategy {
    private Method method;
    private Object[] arguments;

    private MethodExecutionStrategy(Method method, Object... arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object execute(Object instance) throws ScriptException {
        try {
            return method.invoke(instance, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * Creates a {@link MethodExecutionStrategy} that will call the specified {@link Method}.
     *
     * @param method the {@link Method} to execute
     * @param arguments the arguments to be passed to the method
     * @return the value returned by the method, or {@code null}
     */
    public static MethodExecutionStrategy byMethod(Method method, Object... arguments) {
        return new MethodExecutionStrategy(method, arguments);
    }

    /**
     * Creates a {@link MethodExecutionStrategy} that will call the {@code public static void main(String[] args)}
     * with the specified arguments.
     *
     * @param clazz the {@link Class}
     * @param arguments the arguments to pass to the main method
     * @return the created {@link MethodExecutionStrategy}
     * @throws ScriptException if no {@code public static void main(String[] args)} method was found
     */
    public static MethodExecutionStrategy byMainMethod(Class<?> clazz, String... arguments) throws ScriptException {
        try {
            Method method = clazz.getMethod("main", String[].class);
            return new MethodExecutionStrategy(method, (Object[]) arguments);
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * Creates a {@link MethodExecutionStrategy} that will call the public method with the
     * specified argument types and passes the specified argument list.
     *
     * @param clazz the {@link Class}
     * @param methodName the method name
     * @param argumentTypes the argument types defining the constructor to call
     * @param arguments the arguments to pass to the constructor (may contain {@code null})
     * @return the created {@link MethodExecutionStrategy}
     * @throws ScriptException if no matching public method was found
     */
    public static MethodExecutionStrategy byArgumentTypes(Class<?> clazz, String methodName, Class<?>[] argumentTypes, Object... arguments) throws ScriptException {
        try {
            Method method = clazz.getMethod(methodName, argumentTypes);
            return byMethod(method, arguments);
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * Creates a {@link MethodExecutionStrategy} that will call a public method that matches the
     * specified arguments.
     *
     * A method must match all specified arguments, except {@code null} values which
     * match any non-primitive type.
     * The conversion from object types into corresponding primitive types
     * (for example {@link Integer} into {@code int}) is handled automatically.
     *
     * @param clazz the {@link Class}
     * @param methodName the method name
     * @param arguments the arguments to be passed to the method
     * @return the created {@link MethodExecutionStrategy}
     * @throws ScriptException if no matching public method was found
     */
    public static MethodExecutionStrategy byMatchingArguments(Class<?> clazz, String methodName, Object... arguments) throws ScriptException {
        List<Method> matchingMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)
                    && (method.getModifiers() & Modifier.PUBLIC) != 0
                    && ReflectionUtil.matchesArguments(method, arguments)) {
                matchingMethods.add(method);
            }
        }

        int count = matchingMethods.size();
        if (count == 0) {
            throw new ScriptException("No method '" + methodName + "' with matching arguments found");
        } else if (count > 1) {
            throw new ScriptException("Ambiguous methods '" + methodName + "' with matching arguments found: " + "\n" +
                    matchingMethods.stream().map(Object::toString).collect(Collectors.joining("\n")));
        }

        return byMethod(matchingMethods.get(0), arguments);
    }
}
