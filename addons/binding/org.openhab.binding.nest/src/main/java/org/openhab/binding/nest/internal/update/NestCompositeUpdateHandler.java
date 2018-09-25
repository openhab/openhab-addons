/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.update;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.data.NestIdentifiable;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.openhab.binding.nest.internal.listener.NestThingDataListener;

/**
 * Handles all Nest data updates through delegation to the {@link NestUpdateHandler} for the respective data type.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class NestCompositeUpdateHandler {

    private final Supplier<Set<String>> presentNestIdsSupplier;
    private final Map<Class<?>, @Nullable NestUpdateHandler<?>> updateHandlersMap = new ConcurrentHashMap<>();

    public NestCompositeUpdateHandler(Supplier<Set<String>> presentNestIdsSupplier) {
        this.presentNestIdsSupplier = presentNestIdsSupplier;
    }

    public <T> boolean addListener(Class<T> dataClass, NestThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).addListener(listener);
    }

    public <T> boolean addListener(Class<T> dataClass, String nestId, NestThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).addListener(nestId, listener);
    }

    private Set<String> findMissingNestIds(Set<NestIdentifiable> updates) {
        Set<String> nestIds = updates.stream().map(u -> u.getId()).collect(Collectors.toSet());
        Set<String> missingNestIds = presentNestIdsSupplier.get();
        missingNestIds.removeAll(nestIds);
        return missingNestIds;
    }

    public @Nullable <T> T getLastUpdate(Class<T> dataClass, String nestId) {
        return getOrCreateUpdateHandler(dataClass).getLastUpdate(nestId);
    }

    public <T> List<T> getLastUpdates(Class<T> dataClass) {
        return getOrCreateUpdateHandler(dataClass).getLastUpdates();
    }

    private Set<NestIdentifiable> getNestUpdates(TopLevelData data) {
        Set<NestIdentifiable> updates = new HashSet<>();
        if (data.getDevices() != null) {
            if (data.getDevices().getCameras() != null) {
                updates.addAll(data.getDevices().getCameras().values());
            }
            if (data.getDevices().getSmokeCoAlarms() != null) {
                updates.addAll(data.getDevices().getSmokeCoAlarms().values());
            }
            if (data.getDevices().getThermostats() != null) {
                updates.addAll(data.getDevices().getThermostats().values());
            }
        }
        if (data.getStructures() != null) {
            updates.addAll(data.getStructures().values());
        }
        return updates;
    }

    @SuppressWarnings("unchecked")
    private <T> NestUpdateHandler<T> getOrCreateUpdateHandler(Class<T> dataClass) {
        NestUpdateHandler<T> handler = (NestUpdateHandler<T>) updateHandlersMap.get(dataClass);
        if (handler == null) {
            handler = new NestUpdateHandler<>();
            updateHandlersMap.put(dataClass, handler);
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    public void handleUpdate(TopLevelData data) {
        Set<NestIdentifiable> updates = getNestUpdates(data);
        updates.forEach(update -> {
            Class<NestIdentifiable> updateClass = (Class<NestIdentifiable>) update.getClass();
            getOrCreateUpdateHandler(updateClass).handleUpdate(updateClass, update.getId(), update);
        });

        Set<String> missingNestIds = findMissingNestIds(updates);
        if (!missingNestIds.isEmpty()) {
            updateHandlersMap.values().forEach(handler -> {
                if (handler != null) {
                    handler.handleMissingNestIds(missingNestIds);
                }
            });
        }
    }

    public <T> boolean removeListener(Class<T> dataClass, NestThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).removeListener(listener);
    }

    public <T> boolean removeListener(Class<T> dataClass, String nestId, NestThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).removeListener(nestId, listener);
    }

    public void resendLastUpdates() {
        updateHandlersMap.values().forEach(handler -> {
            if (handler != null) {
                handler.resendLastUpdates();
            }
        });
    }

}
