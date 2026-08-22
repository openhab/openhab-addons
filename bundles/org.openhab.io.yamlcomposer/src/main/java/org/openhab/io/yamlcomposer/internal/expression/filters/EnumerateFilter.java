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
package org.openhab.io.yamlcomposer.internal.expression.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

/**
 * Custom Jinjava filter to enumerate elements of a collection, iterable, or array.
 * It returns a list of pairs, where each pair consists of the index and the corresponding element
 *
 * Usage: {@code variable|enumerate} to get a list of {@code [index, element]} pairs.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class EnumerateFilter implements Filter {

    @Override
    public String getName() {
        return "enumerate";
    }

    @NonNullByDefault({})
    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
        if (var == null) {
            return new ArrayList<>();
        }

        List<Object> enumeratedList = new ArrayList<>();
        int index = 0;

        if (var instanceof Collection) {
            for (Object item : (Collection<?>) var) {
                addPair(enumeratedList, index++, item);
            }
        } else if (var instanceof Iterable) {
            for (Object item : (Iterable<?>) var) {
                addPair(enumeratedList, index++, item);
            }
        } else if (var instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) var).entrySet()) {
                addPair(enumeratedList, index++, entry);
            }
        } else if (var.getClass().isArray()) {
            Object[] array = (Object[]) var;
            for (Object item : array) {
                addPair(enumeratedList, index++, item);
            }
        } else {
            addPair(enumeratedList, 0, var);
        }

        return enumeratedList;
    }

    private void addPair(List<Object> list, int index, @Nullable Object item) {
        List<@Nullable Object> pair = new ArrayList<>(2);
        pair.add(index);
        pair.add(item);
        list.add(pair);
    }

    public static Object staticEnumerate(Object target) {
        return new EnumerateFilter().filter(target, null);
    }
}
