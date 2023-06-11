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
package org.openhab.binding.nest.internal.wwn.update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.listener.WWNThingDataListener;

/**
 * Handles the updates of one type of data by notifying listeners of changes and storing the update value.
 *
 * @author Wouter Born - Initial contribution
 *
 * @param <T> the type of update data
 */
@NonNullByDefault
public class WWNUpdateHandler<@NonNull T> {

    /**
     * The ID used for listeners that subscribe to any Nest update.
     */
    private static final String ANY_ID = "*";

    private final Map<String, T> lastUpdates = new ConcurrentHashMap<>();
    private final Map<String, Set<WWNThingDataListener<T>>> listenersMap = new ConcurrentHashMap<>();

    public boolean addListener(WWNThingDataListener<T> listener) {
        return addListener(ANY_ID, listener);
    }

    public boolean addListener(String nestId, WWNThingDataListener<T> listener) {
        return getOrCreateListeners(nestId).add(listener);
    }

    public @Nullable T getLastUpdate(String nestId) {
        return lastUpdates.get(nestId);
    }

    public List<T> getLastUpdates() {
        return new ArrayList<>(lastUpdates.values());
    }

    private Set<WWNThingDataListener<T>> getListeners(String nestId) {
        Set<WWNThingDataListener<T>> listeners = new HashSet<>();
        Set<WWNThingDataListener<T>> idListeners = listenersMap.get(nestId);
        if (idListeners != null) {
            listeners.addAll(idListeners);
        }
        Set<WWNThingDataListener<T>> anyListeners = listenersMap.get(ANY_ID);
        if (anyListeners != null) {
            listeners.addAll(anyListeners);
        }
        return listeners;
    }

    private Set<WWNThingDataListener<T>> getOrCreateListeners(String nestId) {
        Set<WWNThingDataListener<T>> listeners = listenersMap.get(nestId);
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
            listenersMap.put(nestId, listeners);
        }
        return listeners;
    }

    public void handleMissingNestIds(Set<String> nestIds) {
        nestIds.forEach(nestId -> {
            lastUpdates.remove(nestId);
            getListeners(nestId).forEach(l -> l.onMissingData(nestId));
        });
    }

    public void handleUpdate(Class<T> dataClass, String nestId, T update) {
        final @Nullable T lastUpdate = getLastUpdate(nestId);
        lastUpdates.put(nestId, update);
        notifyListeners(nestId, lastUpdate, update);
    }

    private void notifyListeners(String nestId, @Nullable T lastUpdate, T update) {
        Set<WWNThingDataListener<T>> listeners = getListeners(nestId);
        if (lastUpdate == null) {
            listeners.forEach(l -> l.onNewData(update));
        } else if (!lastUpdate.equals(update)) {
            listeners.forEach(l -> l.onUpdatedData(lastUpdate, update));
        }
    }

    public boolean removeListener(WWNThingDataListener<T> listener) {
        return removeListener(ANY_ID, listener);
    }

    public boolean removeListener(String nestId, WWNThingDataListener<T> listener) {
        return getOrCreateListeners(nestId).remove(listener);
    }

    public void resendLastUpdates() {
        lastUpdates.forEach((nestId, update) -> notifyListeners(nestId, null, update));
    }
}
