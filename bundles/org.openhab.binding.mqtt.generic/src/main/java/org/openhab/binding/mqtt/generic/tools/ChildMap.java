/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.tools;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * <p>
 * In some MQTT conventions there are topics dedicated to list further subtopics.
 * We need to watch those topics and maintain observable lists for those children.
 * </p>
 *
 * <p>
 * This class consists of a mapping ID->subtopic. Apply an array of subtopics
 * via the {@link #apply(String[], Function, Function, Consumer)} method.
 * </p>
 *
 * <p>
 * Restore children from configuration by using {@link #put(String, Object)}.
 * </p>
 *
 * For example in homie 3.x these topics are meant to be watched:
 *
 * <pre>
 * * homie/mydevice/$nodes
 * * homie/mydevice/mynode/$properties
 * </pre>
 *
 * <p>
 * An example value of "homie/mydevice/$nodes" could be "lamp1,lamp2,switch", which means there are
 * "homie/mydevice/lamp1","homie/mydevice/lamp2" and "homie/mydevice/switch" existing and this map
 * would contain 3 entries [lamp1->Node, lamp2->Node, switch->Node].
 * </p>
 *
 * @author David Graeff - Initial contribution
 *
 * @param <T> Any object
 */
@NonNullByDefault
public class ChildMap<T> {
    protected Map<String, T> map = new TreeMap<>();

    public Stream<T> stream() {
        return map.values().stream();
    }

    /**
     * Modifies the map in way that it matches the entries of the given childIDs.
     *
     * @param childIDs The list of IDs that should be in the map. Everything else currently in the map will be removed.
     * @param addedAction A function where the newly added child is given as an argument to perform any actions on it.
     *            A future is expected as a return value that completes as soon as said action is performed.
     * @param supplyNewChild A function where the ID of a new child is given and the created child is
     *            expected as a
     *            result.
     * @param removedCallback A callback, that is called whenever a child got removed by the
     *            {@link #apply(String[], Function, Function, Consumer)} method.
     * @return Complete successfully if all "addedAction" complete successfully, otherwise complete exceptionally.
     */
    public CompletableFuture<@Nullable Void> apply(String[] childIDs,
            final Function<T, CompletableFuture<Void>> addedAction, final Function<String, T> supplyNewChild,
            final Consumer<T> removedCallback) {
        Set<String> arrayValues = Stream.of(childIDs).collect(Collectors.toSet());

        // Add all entries to the map, that are not in there yet.
        final Map<String, T> newSubnodes = arrayValues.stream().filter(entry -> !this.map.containsKey(entry))
                .collect(Collectors.toMap(k -> k, supplyNewChild));
        this.map.putAll(newSubnodes);

        // Remove any entries that are not listed in the 'childIDs'.
        this.map.entrySet().removeIf(entry -> {
            if (!arrayValues.contains(entry.getKey())) {
                removedCallback.accept(entry.getValue());
                return true;
            }
            return false;
        });

        // Apply the 'addedAction' function for all new entries.
        return CompletableFuture
                .allOf(newSubnodes.values().stream().map(addedAction).toArray(CompletableFuture[]::new));
    }

    /**
     * Return the size of this map.
     */
    public int size() {
        return map.size();
    }

    /**
     * Get the item with the given id
     *
     * @param key The id
     * @return The item
     */
    public T get(@Nullable String key) {
        return map.get(key);
    }

    /**
     * Clear the map
     */
    public void clear() {
        map.clear();
    }

    /**
     * Use this method only to restore a child from configuration.
     *
     * @param key The ID
     * @param value The subnode object
     */
    public void put(String key, T value) {
        map.put(key, value);
    }
}
