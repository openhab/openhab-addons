/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.automation.java223.internal.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.automation.java223.common.Java223Exception;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule;

import helper.rules.Java223Rule;
import helper.rules.RuleParserException;

/**
 * Unit tests for {@link Java223Rule}, focused on the constructor variants and the
 * synchronous (non-debounced) execution paths of {@link Java223Rule#execute(Action, Map)}.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class Java223RuleTest {

    private final Action emptyAction = mock(Action.class);

    @Test
    public void fieldConstructorReturnsValueAndForwardsBindingsToBiFunction() throws Exception {
        AtomicReference<@org.eclipse.jdt.annotation.Nullable Integer> received = new AtomicReference<>();
        ScriptWithBiFunction script = new ScriptWithBiFunction(
                (BiFunction<Action, Map<String, Object>, @org.eclipse.jdt.annotation.Nullable Object>) (action,
                        bindings) -> {
                    received.set((Integer) bindings.get("input"));
                    return 42;
                });
        Field field = ScriptWithBiFunction.class.getDeclaredField("biFunction");

        Java223Rule rule = new Java223Rule(script, field);
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("input", 7);
        Object result = rule.execute(emptyAction, bindings);

        assertEquals(7, received.get(), "BiFunction should receive the bindings map");
        assertEquals(42, result, "Returned value should be propagated to caller");
    }

    @Test
    public void executeReturnsEmptyStringWhenCodeReturnsNull() throws Exception {
        ScriptWithRunnable script = new ScriptWithRunnable(() -> {
        });
        Field field = ScriptWithRunnable.class.getDeclaredField("runnable");

        Java223Rule rule = new Java223Rule(script, field);
        Object result = rule.execute(emptyAction, new HashMap<>());

        assertEquals("", result, "Null returns from the code must be normalized to empty string");
    }

    @Test
    public void executeRethrowsJava223ExceptionWhenCodeThrows() throws Exception {
        ScriptWithRunnable script = new ScriptWithRunnable(() -> {
            throw new Java223Exception("boom");
        });
        Field field = ScriptWithRunnable.class.getDeclaredField("runnable");

        Java223Rule rule = new Java223Rule(script, field);
        assertThrows(Java223Exception.class, () -> rule.execute(emptyAction, new HashMap<>()));
    }

    @Test
    public void fieldConstructorExecutesRunnableField() throws Exception {
        AtomicInteger count = new AtomicInteger();
        ScriptWithRunnable script = new ScriptWithRunnable(count::incrementAndGet);
        Field field = ScriptWithRunnable.class.getDeclaredField("runnable");

        Java223Rule rule = new Java223Rule(script, field);
        Object result = rule.execute(emptyAction, new HashMap<>());

        assertEquals(1, count.get(), "Runnable.run() must be called once per execute()");
        assertEquals("", result, "Runnable has no return value, must normalize to empty string");
    }

    @Test
    public void fieldConstructorExecutesCallableField() throws Exception {
        ScriptWithCallable script = new ScriptWithCallable(() -> "callable-result");
        Field field = ScriptWithCallable.class.getDeclaredField("callable");

        Java223Rule rule = new Java223Rule(script, field);
        Object result = rule.execute(emptyAction, new HashMap<>());

        assertEquals("callable-result", result, "Callable.call() result must be returned to caller");
    }

    @Test
    public void fieldConstructorExecutesCallableAndWrapsCheckedException() throws Exception {
        ScriptWithCallable script = new ScriptWithCallable(() -> {
            throw new Java223Exception("checked");
        });
        Field field = ScriptWithCallable.class.getDeclaredField("callable");

        Java223Rule rule = new Java223Rule(script, field);

        assertThrows(Java223Exception.class, () -> rule.execute(emptyAction, new HashMap<>()));
    }

    @Test
    public void fieldConstructorExecutesFunctionField() throws Exception {
        ScriptWithFunction script = new ScriptWithFunction(inputs -> inputs.get("input") + "-done");
        Field field = ScriptWithFunction.class.getDeclaredField("function");

        Java223Rule rule = new Java223Rule(script, field);
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("input", "x");

        Object result = rule.execute(emptyAction, bindings);

        assertEquals("x-done", result, "Function.apply() result must be returned to caller");
    }

    @Test
    public void fieldConstructorExecutesBiConsumerField() throws Exception {
        AtomicReference<@org.eclipse.jdt.annotation.Nullable Map<String, Object>> seen = new AtomicReference<>();
        AtomicReference<@org.eclipse.jdt.annotation.Nullable Action> seenAction = new AtomicReference<>();
        ScriptWithBiConsumer script = new ScriptWithBiConsumer((action, inputs) -> {
            seenAction.set(action);
            seen.set(inputs);
        });
        Field field = ScriptWithBiConsumer.class.getDeclaredField("biConsumer");

        Java223Rule rule = new Java223Rule(script, field);
        Map<String, Object> bindings = new HashMap<>();
        rule.execute(emptyAction, bindings);

        assertSame(emptyAction, seenAction.get(), "BiConsumer must receive the action module");
        assertSame(bindings, seen.get(), "BiConsumer must receive the bindings map");
    }

    @Test
    public void fieldConstructorExecutesBiFunctionField() throws Exception {
        ScriptWithBiFunction script = new ScriptWithBiFunction(
                (BiFunction<Action, Map<String, Object>, @org.eclipse.jdt.annotation.Nullable Object>) (action,
                        inputs) -> inputs.get("input"));
        Field field = ScriptWithBiFunction.class.getDeclaredField("biFunction");

        Java223Rule rule = new Java223Rule(script, field);
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("input", "value");

        Object result = rule.execute(emptyAction, bindings);

        assertEquals("value", result, "BiFunction.apply() result must be returned to caller");
    }

    @Test
    public void fieldConstructorExecutesConsumerField() throws Exception {
        AtomicReference<@org.eclipse.jdt.annotation.Nullable Map<String, Object>> seen = new AtomicReference<>();
        ScriptWithConsumer script = new ScriptWithConsumer(seen::set);
        Field field = ScriptWithConsumer.class.getDeclaredField("consumer");

        Java223Rule rule = new Java223Rule(script, field);
        Map<String, Object> bindings = new HashMap<>();
        rule.execute(emptyAction, bindings);

        assertSame(bindings, seen.get(), "Consumer.accept() must receive the bindings map");
    }

    @Test
    public void fieldConstructorExecutesSimpleRuleField() throws Exception {
        AtomicInteger delegated = new AtomicInteger();
        SimpleRule inner = new SimpleRule() {
            @Override
            public Object execute(Action module, Map<String, ?> bindings) {
                delegated.incrementAndGet();
                return "inner-result";
            }
        };
        ScriptWithSimpleRule script = new ScriptWithSimpleRule(inner);
        Field field = ScriptWithSimpleRule.class.getDeclaredField("simpleRule");

        Java223Rule rule = new Java223Rule(script, field);
        Object result = rule.execute(emptyAction, new HashMap<>());

        assertEquals(1, delegated.get(), "SimpleRule.execute() must be called once per execute()");
        assertEquals("inner-result", result, "SimpleRule.execute() result must be returned to caller");
    }

    @Test
    public void fieldConstructorThrowsWhenFieldIsNull() throws Exception {
        ScriptWithRunnable script = new ScriptWithRunnable(null);
        Field field = ScriptWithRunnable.class.getDeclaredField("runnable");

        Java223Rule rule = new Java223Rule(script, field);

        assertThrows(Java223Exception.class, () -> rule.execute(emptyAction, new HashMap<>()));
    }

    @Test
    public void fieldConstructorRejectsUnsupportedFieldType() throws NoSuchFieldException {
        ScriptWithString script = new ScriptWithString("hello");
        Field field = ScriptWithString.class.getDeclaredField("string");
        assertThrows(RuleParserException.class, () -> new Java223Rule(script, field));
    }

    @Test
    public void methodConstructorExecutesZeroArgMethod() throws Exception {
        ScriptWithMethods script = new ScriptWithMethods();
        Method method = ScriptWithMethods.class.getDeclaredMethod("zeroArgMethod");

        Java223Rule rule = new Java223Rule(script, method);
        Object result = rule.execute(emptyAction, new HashMap<>());

        assertEquals("zero", result, "Zero-arg method must be invoked and its return value forwarded");
        assertEquals(1, script.zeroArgCalls.get(), "Zero-arg method must be called once");
    }

    @Test
    public void methodConstructorExecutesMethodWithActionParameter() throws Exception {
        ScriptWithMethods script = new ScriptWithMethods();
        Method method = ScriptWithMethods.class.getDeclaredMethod("methodWithAction", Action.class);

        Java223Rule rule = new Java223Rule(script, method);
        rule.execute(emptyAction, new HashMap<>());

        assertEquals(1, script.actionCalls.get(), "Method with Action parameter must be invoked once");
        assertSame(emptyAction, script.lastSeenAction.get(),
                "The Action module passed to execute() must be forwarded to the method");
    }

    @Test
    public void methodConstructorWrapsInvocationException() throws NoSuchMethodException {
        ScriptWithMethods script = new ScriptWithMethods();
        Method method = ScriptWithMethods.class.getDeclaredMethod("throwingMethod");
        Java223Rule rule = new Java223Rule(script, method);
        assertThrows(Java223Exception.class, () -> rule.execute(emptyAction, new HashMap<>()));
    }

    @Test
    public void fieldConstructorCanBeCalledMultipleTimes() throws Exception {
        AtomicInteger count = new AtomicInteger();
        ScriptWithRunnable script = new ScriptWithRunnable(count::incrementAndGet);
        Field field = ScriptWithRunnable.class.getDeclaredField("runnable");

        Java223Rule rule = new Java223Rule(script, field);
        rule.execute(emptyAction, new HashMap<>());
        rule.execute(emptyAction, new HashMap<>());
        rule.execute(emptyAction, new HashMap<>());

        assertEquals(3, count.get(), "Field-based rules must be reusable across multiple execute() calls");
    }

    @Test
    public void fieldConstructorExecutesCallableFieldWithDerivedClass() throws Exception {
        Runnablederived runnableDerived = new Runnablederived();
        ScriptWithRunnableDerived script = new ScriptWithRunnableDerived(runnableDerived);
        Field field = ScriptWithRunnableDerived.class.getDeclaredField("runnable");

        Java223Rule rule = new Java223Rule(script, field);
        rule.execute(emptyAction, new HashMap<>());

        assertEquals(1, runnableDerived.getCount(), "runnable.run() result must be executed");
    }

    @NonNullByDefault({})
    public static class ScriptWithRunnableDerived {
        @SuppressWarnings("unused")
        public Runnablederived runnable;

        ScriptWithRunnableDerived(@org.eclipse.jdt.annotation.Nullable Runnablederived runnable) {
            this.runnable = runnable;
        }
    }

    public static class Runnablederived implements Runnable {
        AtomicInteger count = new AtomicInteger();

        @Override
        public void run() {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithRunnable {
        @SuppressWarnings("unused")
        public Runnable runnable;

        ScriptWithRunnable(@org.eclipse.jdt.annotation.Nullable Runnable runnable) {
            this.runnable = runnable;
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithCallable {
        @SuppressWarnings("unused")
        public Callable<String> callable;

        ScriptWithCallable(Callable<String> callable) {
            this.callable = callable;
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithFunction {
        @SuppressWarnings("unused")
        public Function<Map<String, Object>, String> function;

        ScriptWithFunction(Function<Map<String, Object>, String> function) {
            this.function = function;
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithBiFunction {
        @SuppressWarnings("unused")
        public BiFunction<Action, Map<String, Object>, @org.eclipse.jdt.annotation.Nullable Object> biFunction;

        ScriptWithBiFunction(
                BiFunction<Action, Map<String, Object>, @org.eclipse.jdt.annotation.Nullable Object> biFunction) {
            this.biFunction = biFunction;
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithConsumer {
        @SuppressWarnings("unused")
        public Consumer<Map<String, Object>> consumer;

        ScriptWithConsumer(Consumer<Map<String, Object>> consumer) {
            this.consumer = consumer;
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithBiConsumer {
        @SuppressWarnings("unused")
        public BiConsumer<Action, Map<String, Object>> biConsumer;

        ScriptWithBiConsumer(BiConsumer<Action, Map<String, Object>> biConsumer) {
            this.biConsumer = biConsumer;
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithSimpleRule {
        @SuppressWarnings("unused")
        public SimpleRule simpleRule;

        ScriptWithSimpleRule(SimpleRule simpleRule) {
            this.simpleRule = simpleRule;
        }
    }

    @NonNullByDefault({})
    public static class ScriptWithString {
        @SuppressWarnings("unused")
        public String string;

        ScriptWithString(String string) {
            this.string = string;
        }
    }

    public static class ScriptWithMethods {
        AtomicInteger zeroArgCalls = new AtomicInteger();
        AtomicInteger actionCalls = new AtomicInteger();
        AtomicReference<@org.eclipse.jdt.annotation.Nullable Action> lastSeenAction = new AtomicReference<>();

        public String zeroArgMethod() {
            zeroArgCalls.incrementAndGet();
            return "zero";
        }

        public String methodWithAction(Action action) {
            actionCalls.incrementAndGet();
            lastSeenAction.set(action);
            return "with-action";
        }

        public String throwingMethod() {
            throw new Java223Exception("kaboom");
        }
    }
}
