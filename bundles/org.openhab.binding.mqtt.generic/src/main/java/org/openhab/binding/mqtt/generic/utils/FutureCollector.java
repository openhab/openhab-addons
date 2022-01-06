/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collector to combine a stream of CompletableFutures.
 *
 * @author Jochen Klein - Initial contribution
 *
 */
public class FutureCollector {

    public static <X> Collector<CompletableFuture<X>, Set<CompletableFuture<X>>, CompletableFuture<Void>> allOf() {
        return Collector.<CompletableFuture<X>, Set<CompletableFuture<X>>, CompletableFuture<Void>> of(
                (Supplier<Set<CompletableFuture<X>>>) HashSet::new, Set::add, (left, right) -> {
                    left.addAll(right);
                    return left;
                }, a -> {
                    return CompletableFuture.allOf(a.toArray(new CompletableFuture[a.size()]));
                }, Collector.Characteristics.UNORDERED);
    }
}
