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
package org.openhab.automation.java223.internal.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.core.automation.Action;

import helper.rules.Java223Rule;
import helper.rules.annotations.Debounce;

/**
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class DebouncerTest {

    @Mock
    Action emptyAction = Mockito.mock(Action.class);

    @Mock
    Debounce debounce = Mockito.mock(Debounce.class);

    @Test
    public void debouncerFirstTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        AtomicInteger value = new AtomicInteger(0);
        AtomicInteger count = new AtomicInteger(0);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

        when(debounce.type()).thenReturn(Debounce.Type.FIRST_ONLY);
        when(debounce.value()).thenReturn(500L);

        // The rule will set a value
        Java223Rule rule = new Java223Rule((action, bindings) -> {
            Integer input = (Integer) bindings.get("input");
            input = input == null ? -1 : input;
            value.set(input);
            count.incrementAndGet();
            return null;
        }, debounce);
        setExecutorService(rule, service);

        // we will call it two times
        exec(rule, 1);
        Thread.sleep(100);
        exec(rule, 2);

        service.shutdown();

        // but only one call, and the value set is the first one
        assertThat(service.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        assertThat(count.get()).isEqualTo(1);
        assertThat(value.get()).isEqualTo(1);
    }

    @Test
    public void debouncerLastTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        AtomicInteger value = new AtomicInteger(0);
        AtomicInteger count = new AtomicInteger(0);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

        when(debounce.type()).thenReturn(Debounce.Type.LAST_ONLY);
        when(debounce.value()).thenReturn(500L);

        // The rule will set a value
        Java223Rule rule = new Java223Rule((action, bindings) -> {
            Integer input = (Integer) bindings.get("input");
            input = input == null ? -1 : input;
            value.set(input);
            count.incrementAndGet();
            return null;
        }, debounce);
        setExecutorService(rule, service);

        // we will call it two times
        exec(rule, 1);
        Thread.sleep(100);
        exec(rule, 2);

        service.shutdown();

        // but only one call, and the value set is the last one
        assertThat(service.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        assertThat(count.get()).isEqualTo(1);
        assertThat(value.get()).isEqualTo(2);
    }

    private void exec(Java223Rule rule, int value) {
        Map<String, Integer> bindings = new HashMap<>();
        bindings.put("input", value);
        rule.execute(emptyAction, bindings);
    }

    @Test
    public void debouncerStableTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        AtomicInteger value = new AtomicInteger(0);
        AtomicInteger count = new AtomicInteger(0);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

        when(debounce.type()).thenReturn(Debounce.Type.STABLE);
        when(debounce.value()).thenReturn(500L);

        // The rule will set a value
        Java223Rule rule = new Java223Rule((action, bindings) -> {
            Integer input = (Integer) bindings.get("input");
            input = input == null ? -1 : input;
            value.set(input);
            count.incrementAndGet();
            return null;
        }, debounce);
        setExecutorService(rule, service);

        // we will call it three times
        exec(rule, 1);
        Thread.sleep(300);
        exec(rule, 2);
        Thread.sleep(300);
        exec(rule, 3);

        service.shutdown();

        // but only one call, and the value set is the last one
        assertThat(service.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
        assertThat(count.get()).isEqualTo(1);
        assertThat(value.get()).isEqualTo(3);
    }

    /**
     * Sets a specific field on a target object to the given value using reflection.
     *
     * @param target the object whose field is to be set
     * @param value the new value to assign to the specified field
     */
    private void setExecutorService(Object target, Object value) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField("execService");
        field.setAccessible(true);
        field.set(target, value);
    }
}
