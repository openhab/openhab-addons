/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package helper.rules;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import helper.rules.annotations.Debounce;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.BindingInjector;
import org.openhab.automation.java223.common.Java223Constants;
import org.openhab.automation.java223.common.Java223Exception;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract code to execute, from diverse runnable field or from a method
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
@NonNullByDefault
public class Java223Rule extends SimpleRule {

    Logger logger = LoggerFactory.getLogger(Java223Rule.class);

    // Dummy reference date for "never executed" debouncer
    private static final ZonedDateTime OLD = ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 0, ZoneId.systemDefault());

    // Field type acceptable as container for rule code to execute
    private static final Set<Class<?>> ACCEPTABLE_FIELD_MEMBER_CLASSES = Set.of(SimpleRule.class, Function.class,
            BiFunction.class, Callable.class, Runnable.class, Consumer.class, BiConsumer.class);

    // Store the code to execute when the rule is triggered
    private final BiFunction<Action, Map<String, Object>, @Nullable Object> codeToExecute;

    // Params used for the debounce function
    @Nullable
    private final Debounce debounce;
    @Nullable
    private ZonedDateTime executionTimeStamp = null;
    @Nullable
    private ScheduledFuture<@Nullable Object> futureAction;
    private final ScheduledExecutorService execService = Executors.newScheduledThreadPool(2);

    public void setUid(String uid) {
        if (!uid.isBlank()) {
            this.uid = uid;
        }
    }

    private Object fieldExecution(SimpleRule simpleRule, Action module, Map<String, Object> inputs) {
        return simpleRule.execute(module, inputs);
    }

    private Object fieldExecution(Function<Map<String, Object>, Object> function, Map<String, Object> inputs) {
        return function.apply(inputs);
    }

    private Object fieldExecution(BiFunction<Action, Map<String, Object>, Object> function, Action module,
                                  Map<String, Object> inputs) {
        return function.apply(module, inputs);
    }

    private Object fieldExecution(Callable<Object> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new Java223Exception("Cannot execute callable", e);
        }
    }

    @Nullable
    private Object fieldExecution(Runnable runnable) {
        runnable.run();
        return null;
    }

    @Nullable
    private Object fieldExecution(Consumer<Map<String, Object>> consumer, Map<String, Object> inputs) {
        consumer.accept(inputs);
        return null;
    }

    @Nullable
    private Object fieldExecution(BiConsumer<Action, Map<String, Object>> consumer, Action module, Map<String, Object> inputs) {
        consumer.accept(module, inputs);
        return null;
    }

    public Java223Rule(BiFunction<Action, Map<String, Object>, @Nullable Object> code, Debounce debounce) {
        this.codeToExecute = code;
        this.debounce = debounce;
    }

    /**
     * Prepare some executable code from a method
     *
     * @param script The instance to execute the method on
     * @param method The method to execute
     */
    public Java223Rule(Object script, Method method) {
        Parameter[] parameters = method.getParameters();
        this.debounce = method.getAnnotation(Debounce.class);
        this.codeToExecute = (module, inputs) -> {
            try {
                if (method.getParameters().length == 0) {
                    return method.invoke(script);
                } else {
                    @Nullable Object [] parameterValues = new Object [parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        // special case for injecting the module
                        if (parameters[i].getType().equals(Action.class)) {
                            parameterValues[i] = module;
                        } else {
                            ClassLoader classLoader = script.getClass().getClassLoader();
                            if (classLoader == null) { // should not happen
                                throw new Java223Exception("Cannot get class loader for " + script.getClass());
                            }
                            parameterValues[i] = BindingInjector.extractBindingValueForElement(classLoader,
                                    inputs, parameters[i]);
                        }
                    }
                    return method.invoke(script, parameterValues);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | SecurityException e) {
                throw new Java223Exception("Cannot execute method named " + method.getName(), e);
            }
        };
    }

    /**
     * Prepare some executable code from a field
     *
     * @param script The instance to execute the method on
     * @param fieldMember The field member containing some code to execute
     */
    @SuppressWarnings({"unchecked"})
    public Java223Rule(Object script, Field fieldMember) throws RuleParserException {
        Class<?> fieldType = fieldMember.getType();
        this.debounce = fieldMember.getAnnotation(Debounce.class);
        if (ACCEPTABLE_FIELD_MEMBER_CLASSES.stream().noneMatch(fieldType::isAssignableFrom)) {
            throw new RuleParserException("Field member " + fieldMember.getName() + " cannot be of class " + fieldType
                    + ". Must be " + ACCEPTABLE_FIELD_MEMBER_CLASSES.stream().map(Class::getSimpleName)
                    .collect(Collectors.joining(" or ")));
        }

        this.codeToExecute = (module, inputs) -> {
            Object objectToExecute;
            try {
                objectToExecute = fieldMember.get(script);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new Java223Exception("Cannot get field member " + fieldMember.getName() + " on object of class "
                        + script.getClass().getName(), e);
            }
            return switch (objectToExecute) {
                case null ->
                        throw new Java223Exception("Field " + fieldMember.getName() + " is null. Cannot execute anything");
                case SimpleRule simpleRule -> fieldExecution(simpleRule, module, inputs);
                case Function<?, ?> function ->
                        fieldExecution((Function<Map<String, Object>, Object>) function, inputs);
                case BiFunction<?, ?, ?> bifunction ->
                        fieldExecution((BiFunction<Action, Map<String, Object>, Object>) bifunction, module, inputs);
                case Callable<?> callable -> fieldExecution((Callable<Object>) callable);
                case Runnable runnable -> fieldExecution(runnable);
                case Consumer<?> consumer -> fieldExecution((Consumer<Map<String, Object>>) consumer, inputs);
                case BiConsumer<?, ?> biconsumer ->
                        fieldExecution((BiConsumer<Action, Map<String, Object>>) biconsumer, module, inputs);
                default -> throw new Java223Exception("Wrong type of field " + fieldType + ". Should not happen");
            };
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Action module, Map<String, ?> bindings) {
        // special self reference :
        ((Map<String, Object>) bindings).put(Java223Constants.BINDINGS, bindings);
        // actual call :
        try {
            Debounce debounceLocal = debounce;
            if (debounceLocal != null) {
                debounceExecution(debounceLocal, module, (Map<String, Object>) bindings);
                // as execution is in another thread, we cannot have the return value
                return "";
            } else {
                var value = codeToExecute.apply(module, (Map<String, Object>) bindings);
                return value != null ? value : "";
            }
        } catch (Java223Exception e) {
            // why do we do this? Because openHAB sometimes doesn't log the full stack trace exception.
            // so we took care of it before rethrowing it.
            logger.error("Cannot execute action", e);
            throw new Java223Exception("Cannot execute action");
        }
    }

    private void debounceExecution(Debounce debounce, Action module, Map<String, Object> bindings) {
        ZonedDateTime now = ZonedDateTime.now();

        switch (debounce.type()) {
            case FIRST_ONLY -> {
                // in this case, we will cancel all executions after the first, during the debouncing period.
                ZonedDateTime lastExec = executionTimeStamp;
                lastExec = lastExec != null ? lastExec : OLD;
                if (lastExec.plus(debounce.value(), ChronoUnit.MILLIS).isBefore(now)) {
                    executionTimeStamp = now;
                    // execute immediately in a separate thread to avoid blocking the rule execution. The
                    // purpose of this is to debounce, so we assume the user doesn't want blocking behavior.
                    execService.execute(() -> codeToExecute.apply(module, bindings));
                } else {
                    logger.debug("Debounced action (first only");
                }
            }
            case LAST_ONLY -> {
                // in this case, we will only execute the last action during the debouncing period.
                // first compute the remaining delay. executionTimeStamp is the starting triggering event time.
                long remainingDelay = debounce.value();
                @Nullable ZonedDateTime startingExecTimeStamp = executionTimeStamp;
                if (startingExecTimeStamp != null) {
                    remainingDelay = debounce.value() - ChronoUnit.MILLIS.between(startingExecTimeStamp, now);
                } else {
                    executionTimeStamp = now;
                }

                // Second, cancel the previous scheduled action (if any)
                @Nullable ScheduledFuture<@Nullable Object> futureActionLocal = futureAction;
                if (futureActionLocal != null) {
                    futureActionLocal.cancel(false);
                }

                // Third, schedule
                futureAction = execService.schedule(() -> {
                    futureAction = null;
                    return codeToExecute.apply(module, bindings);
                }, remainingDelay, TimeUnit.MILLISECONDS);
            }
            case STABLE -> {
                // in this case, we will wait for a stable state (rule not triggered during the wanted delay)
                // and execute the last one
                @Nullable ScheduledFuture<@Nullable Object> futureActionLocal = futureAction;
                if (futureActionLocal != null) {
                    futureActionLocal.cancel(false);
                }
                futureAction = execService.schedule(() -> {
                    futureAction = null;
                    return codeToExecute.apply(module, bindings);
                }, debounce.value(), TimeUnit.MILLISECONDS);
            }
        }
    }
}
