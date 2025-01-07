/*
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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
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
        context.registerFilter(new RegexFindAllFilter());
        context.registerFilter(new RegexFindAllIndexFilter());
    }

    @NonNullByDefault({})
    private static class SimpleFilter implements Filter {
        private final String name;
        private final Method method;
        private final Class<?> klass;

        public SimpleFilter(String name, Class<?> klass, String methodName, Class<?>... args) {
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

    // https://www.home-assistant.io/docs/configuration/templating/#regular-expressions
    // https://github.com/home-assistant/core/blob/2024.12.2/homeassistant/helpers/template.py#L2453
    @NonNullByDefault({})
    private static class RegexFindAllFilter implements Filter {
        @Override
        public String getName() {
            return "regex_findall";
        }

        @Override
        public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
            if (args.length > 2) {
                throw new TemplateSyntaxException(interpreter, getName(),
                        "requires at most 2 arguments (regex string, ignore case)");
            }

            String find = null;
            if (args.length >= 1) {
                find = args[0];
            }
            String ignoreCase = null;
            if (args.length == 2) {
                ignoreCase = args[1];
            }

            Matcher m = regexFindAll(var, interpreter, find, ignoreCase);

            List<Object> result = new ArrayList<>();
            while (m.find()) {
                result.add(resultForMatcher(m));
            }

            return result;
        }

        protected Object resultForMatcher(Matcher m) {
            if (m.groupCount() == 0) {
                return m.group();
            } else if (m.groupCount() == 1) {
                return m.group(1);
            } else {
                List<String> groups = new ArrayList<>(m.groupCount());
                for (int i = 1; i <= m.groupCount(); ++i) {
                    groups.add(m.group(i));
                }
                return groups;
            }
        }

        protected Matcher regexFindAll(Object var, JinjavaInterpreter interpreter, String find, String ignoreCaseStr) {
            String s;
            if (var == null) {
                s = "None";
            } else {
                s = var.toString();
            }

            boolean ignoreCase = ObjectTruthValue.evaluate(ignoreCaseStr);
            int flags = 0;
            if (ignoreCase) {
                flags = Pattern.CASE_INSENSITIVE;
            }

            Pattern p;
            try {
                if (find instanceof String findString) {
                    p = Pattern.compile(findString, flags);
                } else if (find == null) {
                    p = Pattern.compile("", flags);
                } else {
                    throw new InvalidArgumentException(interpreter, this, InvalidReason.REGEX, 0, find);
                }

                return p.matcher(s);
            } catch (PatternSyntaxException e) {
                throw new InvalidArgumentException(interpreter, this, InvalidReason.REGEX, 0, find);
            }
        }
    }

    // https://www.home-assistant.io/docs/configuration/templating/#regular-expressions
    // https://github.com/home-assistant/core/blob/2024.12.2/homeassistant/helpers/template.py#L2448
    @NonNullByDefault({})
    private static class RegexFindAllIndexFilter extends RegexFindAllFilter {
        @Override
        public String getName() {
            return "regex_findall_index";
        }

        @Override
        public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
            if (args.length > 3) {
                throw new TemplateSyntaxException(interpreter, getName(),
                        "requires at most 3 arguments (regex string, index, ignore case)");
            }

            String find = null;
            if (args.length >= 1) {
                find = args[0];
            }
            int index = 0;
            if (args.length >= 2) {
                index = Integer.valueOf(args[1]);
                if (index < 0) {
                    throw new InvalidArgumentException(interpreter, this, InvalidReason.POSITIVE_NUMBER, 1, args[1]);
                }
            }

            String ignoreCase = null;
            if (args.length == 3) {
                ignoreCase = args[2];
            }

            Matcher m = regexFindAll(var, interpreter, find, ignoreCase);
            int i = 0;
            while (i <= index) {
                if (!m.find()) {
                    break;
                }
                i += 1;
            }

            return resultForMatcher(m);
        }
    }

    private static class Functions {
        // https://www.home-assistant.io/docs/configuration/templating/#immediate-if-iif
        public static @Nullable Object iif(@Nullable Object value, @Nullable Object... results) {
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
