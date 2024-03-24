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
package org.openhab.binding.mercedesme.internal.handler;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;

/**
 * {@link StorageServiceMock} mocking StorageServce and Storage
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class StorageServiceMock<T> implements StorageService, Storage<T> {
    @Nullable
    String storedObject;

    public StorageServiceMock(@Nullable String store) {
        storedObject = store;
    }

    @Override
    public <T> Storage<T> getStorage(String name) {
        return (Storage<T>) this;
    }

    @Override
    public <T> Storage<T> getStorage(String name, @Nullable ClassLoader classLoader) {
        return (Storage<T>) this;
    }

    @Override
    public @Nullable T put(String key, @Nullable T value) {
        return null;
    }

    @Override
    public @Nullable T remove(String key) {
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public @Nullable T get(String key) {
        return (T) storedObject;
    }

    @Override
    public Collection<@NonNull String> getKeys() {
        return List.of();
    }

    @Override
    public Collection<@Nullable T> getValues() {
        return List.of();
    }
}
