/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.listener.NestThingDataListener;

/**
 * Handles the updates of one type of data by notifying listeners of changes and storing the update value.
 *
 * @author Wouter Born - Initial contribution
 *
 * @param <T> the type of update data
 */
@NonNullByDefault
public class NestUpdateHandler<T> {

    /**
     * The ID used for listeners that subscribe to any Nest update.
     */
    private static final String ANY_ID = "*";

    private final Map<String, @Nullable T> lastUpdates = new ConcurrentHashMap<>();
    private final Map<String, @Nullable Set<NestThingDataListener<T>>> listenersMap = new ConcurrentHashMap<>();

    public boolean addListener(NestThingDataListener<T> listener) {
        return addListener(ANY_ID, listener);
    }

    public boolean addListener(String nestId, NestThingDataListener<T> listener) {
        return getOrCreateListeners(nestId).add(listener);
    }

    public @Nullable T getLastUpdate(String nestId) {
        return lastUpdates.get(nestId);
    }

    public List<T> getLastUpdates() {
        return new ArrayList<>(lastUpdates.values());
    }

    private Set<NestThingDataListener<T>> getListeners(String nestId) {
        Set<NestThingDataListener<T>> listeners = new HashSet<>();
        if (listenersMap.get(nestId) != null) {
            listeners.addAll(listenersMap.get(nestId));
        }
        if (listenersMap.get(ANY_ID) != null) {
            listeners.addAll(listenersMap.get(ANY_ID));
        }
        return listeners;
    }

    private Set<NestThingDataListener<T>> getOrCreateListeners(String nestId) {
        Set<NestThingDataListener<T>> listeners = listenersMap.get(nestId);
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
        T lastUpdate = getLastUpdate(nestId);
        lastUpdates.put(nestId, update);
        notifyListeners(nestId, lastUpdate, update);
    }

    private void notifyListeners(String nestId, @Nullable T lastUpdate, T update) {
        Set<NestThingDataListener<T>> listeners = getListeners(nestId);
        if (lastUpdate == null) {
            listeners.forEach(l -> l.onNewData(update));
        } else if (!lastUpdate.equals(update)) {
            listeners.forEach(l -> l.onUpdatedData(lastUpdate, update));
        }
    }

    public boolean removeListener(NestThingDataListener<T> listener) {
        return removeListener(ANY_ID, listener);
    }

    public boolean removeListener(String nestId, NestThingDataListener<T> listener) {
        return getOrCreateListeners(nestId).remove(listener);
    }

    public void resendLastUpdates() {
        lastUpdates.forEach((nestId, update) -> notifyListeners(nestId, null, update));
    }
}
