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
package org.openhab.binding.tuya.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link JoiningMapCollector} allows joining all entries of a {@link java.util.stream.Stream<Map.Entry>} with or
 * without delimiters
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class JoiningMapCollector implements Collector<Map.Entry<String, String>, List<String>, String> {
    private final String valueDelimiter;
    private final String entryDelimiter;

    private JoiningMapCollector(String valueDelimiter, String entryDelimiter) {
        this.valueDelimiter = valueDelimiter;
        this.entryDelimiter = entryDelimiter;
    }

    @Override
    public Supplier<List<String>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<String>, Map.Entry<String, String>> accumulator() {
        return (list, entry) -> list.add(entry.getKey() + valueDelimiter + entry.getValue());
    }

    @Override
    public BinaryOperator<List<String>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }

    @Override
    public Function<List<String>, String> finisher() {
        return (list) -> String.join(entryDelimiter, list);
    }

    @Override
    public @NonNullByDefault({}) Set<Characteristics> characteristics() {
        return Set.of();
    }

    /**
     * Create a collector for joining all @link Map.Entry} with the given delimiters
     *
     * @param valueDelimiter the delimiter used to join key and value of each entry
     * @param entryDelimiter the delimiter used to join entries
     * @return the joined {@link java.util.stream.Stream<Map.Entry>} as {@link String}
     */
    public static JoiningMapCollector joining(String valueDelimiter, String entryDelimiter) {
        return new JoiningMapCollector(valueDelimiter, entryDelimiter);
    }

    /**
     * Create a collector for joining all {@link Map.Entry} without delimiters at all
     *
     * @return the joined {@link java.util.stream.Stream<Map.Entry>} as {@link String}
     */
    public static JoiningMapCollector joining() {
        return new JoiningMapCollector("", "");
    }
}
