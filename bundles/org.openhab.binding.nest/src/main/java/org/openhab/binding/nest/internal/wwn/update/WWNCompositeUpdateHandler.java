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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.dto.WWNIdentifiable;
import org.openhab.binding.nest.internal.wwn.dto.WWNTopLevelData;
import org.openhab.binding.nest.internal.wwn.listener.WWNThingDataListener;

/**
 * Handles all Nest data updates through delegation to the {@link WWNUpdateHandler} for the respective data type.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WWNCompositeUpdateHandler {

    private final Supplier<Set<String>> presentNestIdsSupplier;
    private final Map<Class<?>, WWNUpdateHandler<?>> updateHandlersMap = new ConcurrentHashMap<>();

    public WWNCompositeUpdateHandler(Supplier<Set<String>> presentNestIdsSupplier) {
        this.presentNestIdsSupplier = presentNestIdsSupplier;
    }

    public <T> boolean addListener(Class<T> dataClass, WWNThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).addListener(listener);
    }

    public <T> boolean addListener(Class<T> dataClass, String nestId, WWNThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).addListener(nestId, listener);
    }

    private Set<String> findMissingNestIds(Set<WWNIdentifiable> updates) {
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

    private Set<WWNIdentifiable> getNestUpdates(WWNTopLevelData data) {
        Set<WWNIdentifiable> updates = new HashSet<>();
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
    private <@NonNull T> WWNUpdateHandler<T> getOrCreateUpdateHandler(Class<T> dataClass) {
        WWNUpdateHandler<T> handler = (WWNUpdateHandler<T>) updateHandlersMap.get(dataClass);
        if (handler == null) {
            handler = new WWNUpdateHandler<>();
            updateHandlersMap.put(dataClass, handler);
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    public void handleUpdate(WWNTopLevelData data) {
        Set<WWNIdentifiable> updates = getNestUpdates(data);
        updates.forEach(update -> {
            Class<WWNIdentifiable> updateClass = (Class<WWNIdentifiable>) update.getClass();
            getOrCreateUpdateHandler(updateClass).handleUpdate(updateClass, update.getId(), update);
        });

        Set<String> missingNestIds = findMissingNestIds(updates);
        if (!missingNestIds.isEmpty()) {
            updateHandlersMap.values().forEach(handler -> {
                handler.handleMissingNestIds(missingNestIds);
            });
        }
    }

    public <T> boolean removeListener(Class<T> dataClass, WWNThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).removeListener(listener);
    }

    public <T> boolean removeListener(Class<T> dataClass, String nestId, WWNThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).removeListener(nestId, listener);
    }

    public void resendLastUpdates() {
        updateHandlersMap.values().forEach(handler -> {
            handler.resendLastUpdates();
        });
    }
}
