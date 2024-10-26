package ch.obermuhlner.scriptengine.java.construct;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import ch.obermuhlner.scriptengine.java.util.ReflectionUtil;

/**
 * The default {@link ConstructorStrategy} implementation.
 *
 * This implementation has three static constructor methods to define the constructor that should be called:
 * <ul>
 * <li>{@link #byDefaultConstructor()} to call the public default no-argument constructor.</li>
 * <li>{@link #byArgumentTypes(Class[], Object...)} to call the public constructor with the
 * specified argument types and pass it the specified arguments.</li>
 * <li>{@link #byMatchingArguments(Object...)} to call a public constructor that matches the
 * specified arguments.</li>
 * </ul>
 */
public class DefaultConstructorStrategy implements ConstructorStrategy {

    private Class<?>[] argumentTypes;
    private final Object[] arguments;

    private DefaultConstructorStrategy(Class<?>[] argumentTypes, Object[] arguments) {
        this.argumentTypes = argumentTypes;
        this.arguments = arguments;
    }

    @Override
    public Object construct(Class<?> clazz) throws ScriptException {
        try {
            Constructor<?> constructor = findConstructor(clazz, argumentTypes, arguments);
            return constructor.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * Creates a {@link DefaultConstructorStrategy} that will call the public default no-argument constructor.
     *
     * @return the created {@link DefaultConstructorStrategy}
     */
    public static DefaultConstructorStrategy byDefaultConstructor() {
        return new DefaultConstructorStrategy(new Class<?>[0], new Object[0]);
    }

    /**
     * Creates a {@link DefaultConstructorStrategy} that will call the public constructor with the
     * specified argument types and passes the specified argument list.
     *
     * @param argumentTypes the argument types defining the constructor to call
     * @param arguments the arguments to pass to the constructor (may contain {@code null})
     * @return the created {@link DefaultConstructorStrategy}
     */
    public static DefaultConstructorStrategy byArgumentTypes(Class<?>[] argumentTypes, Object... arguments) {
        return new DefaultConstructorStrategy(argumentTypes, arguments);
    }

    /**
     * Creates a {@link DefaultConstructorStrategy} that will call a public constructor that matches the
     * specified arguments.
     *
     * A constructor must match all specified arguments, except {@code null} values which
     * match any non-primitive type.
     * The conversion from object types into corresponding primitive types
     * (for example {@link Integer} into {@code int}) is handled automatically.
     *
     * If multiple public constructors match the specified arguments the
     * {@link #construct(Class)} method will throw a {@link ScriptException}.
     *
     * @param arguments the arguments to pass to the constructor (may contain {@code null})
     * @return the created {@link DefaultConstructorStrategy}
     */
    public static DefaultConstructorStrategy byMatchingArguments(Object... arguments) {
        return new DefaultConstructorStrategy(null, arguments);
    }

    private Constructor<?> findConstructor(Class<?> clazz, Class<?>[] argumentTypes, Object[] arguments)
            throws NoSuchMethodException, ScriptException {
        if (argumentTypes != null) {
            return clazz.getConstructor(argumentTypes);
        }

        List<Constructor<?>> matchingConstructors = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())
                    && ReflectionUtil.matchesArguments(constructor, arguments)) {
                matchingConstructors.add(constructor);
            }
        }

        int count = matchingConstructors.size();
        if (count == 0) {
            throw new ScriptException("No constructor with matching arguments found");
        } else if (count > 1) {
            throw new ScriptException("Ambiguous constructors with matching arguments found:\n"
                    + matchingConstructors.stream().map(Object::toString).collect(Collectors.joining("\n")));
        }

        return matchingConstructors.get(0);
    }
}
