/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.util.ObjectTruthValue;

/**
 * Contains extensions methods exposed in Jinja transformations
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantJinjaFunctionLibrary {
    public static void register(Context context) {
        context.registerFunction(
                new ELFunctionDefinition("", "iif", Functions.class, "iif", Object.class, Object[].class));
        context.registerFilter(new SimpleFilter("iif", Functions.class, "iif", Object.class, Object[].class));
        context.registerFilter(new IsDefinedFilter());
    }

    @NonNullByDefault({})
    private static class SimpleFilter implements Filter {
        private final String name;
        private final Method method;
        private final Class klass;

        public SimpleFilter(String name, Class klass, String methodName, Class... args) {
            this.name = name;
            this.klass = klass;
            try {
                this.method = klass.getDeclaredMethod(methodName, args);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
            Object[] allArgs = Stream.of(Arrays.stream(args), kwargs.values().stream()).flatMap(s -> s)
                    .toArray(Object[]::new);

            try {
                return method.invoke(klass, var, allArgs);
            } catch (IllegalAccessException e) {
                // Not possible
                return null;
            } catch (InvocationTargetException e) {
                throw new InterpretException(e.getMessage(), e, interpreter.getLineNumber(), interpreter.getPosition());
            }
        }

        @Override
        public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
            // Object[] allArgs = Stream.concat(List.of(var).stream(), Arrays.stream(args)).toArray(Object[]::new);

            try {
                return method.invoke(klass, var, args);
            } catch (IllegalAccessException e) {
                // Not possible
                return null;
            } catch (InvocationTargetException e) {
                throw new InterpretException(e.getMessage(), e, interpreter.getLineNumber(), interpreter.getPosition());
            }
        }
    }

    // https://www.home-assistant.io/docs/configuration/templating/#is-defined
    @NonNullByDefault({})
    private static class IsDefinedFilter implements Filter {
        @Override
        public String getName() {
            return "is_defined";
        }

        @Override
        public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
            if (var == null) {
                throw new HomeAssistantChannelTransformation.UndefinedException(interpreter);
            }

            return var;
        }
    }

    private static class Functions {
        // https://www.home-assistant.io/docs/configuration/templating/#immediate-if-iif
        public static Object iif(Object value, Object... results) {
            if (results.length > 3) {
                throw new IllegalArgumentException("Parameters for function 'iff' do not match");
            }
            if (value == null && results.length >= 3) {
                return results[2];
            }
            if (ObjectTruthValue.evaluate(value)) {
                if (results.length >= 1) {
                    return results[0];
                }
                return true;
            }
            if (results.length >= 2) {
                return results[1];
            }
            return false;
        }
    }
}
