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
package org.openhab.automation.jsscripting.internal.scope;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.ScriptExtensionProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Shared Cache implementation for JS scripting.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(immediate = true)
@NonNullByDefault
public class SharedCache implements ScriptExtensionProvider {

    private static final String PRESET_NAME = "cache";
    private static final String OBJECT_NAME = "sharedcache";

    private JSCache cache = new JSCache();

    @Override
    public Collection<String> getDefaultPresets() {
        return Set.of(PRESET_NAME);
    }

    @Override
    public Collection<String> getPresets() {
        return Set.of(PRESET_NAME);
    }

    @Override
    public Collection<String> getTypes() {
        return Set.of(OBJECT_NAME);
    }

    @Override
    public @Nullable Object get(String scriptIdentifier, String type) throws IllegalArgumentException {
        if (OBJECT_NAME.equals(type)) {
            return cache;
        }

        return null;
    }

    @Override
    public Map<String, Object> importPreset(String scriptIdentifier, String preset) {
        if (PRESET_NAME.equals(preset)) {
            final Object requestedType = get(scriptIdentifier, OBJECT_NAME);
            if (requestedType != null) {
                return Map.of(OBJECT_NAME, requestedType);
            }
        }

        return Collections.emptyMap();
    }

    @Override
    public void unload(String scriptIdentifier) {
        // ignore for now
    }

    public static class JSCache {
        private Map<String, Object> backingMap = new HashMap<>();

        public void put(String k, Object v) {
            backingMap.put(k, v);
        }

        public @Nullable Object remove(String k) {
            return backingMap.remove(k);
        }

        public @Nullable Object get(String k) {
            return backingMap.get(k);
        }

        public @Nullable Object get(String k, Supplier<Object> supplier) {
            return backingMap.computeIfAbsent(k, (unused_key) -> supplier.get());
        }
    }
}
