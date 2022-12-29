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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.ScriptExtensionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;

/**
 * Base class to offer support for script extension providers
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public abstract class ScriptDisposalAwareScriptExtensionProvider
        implements ScriptExtensionProvider, ScriptDisposalAware {
    private Map<String, Function<String, Object>> types;
    private Map<String, Map<String, Object>> idToTypes = new ConcurrentHashMap<>();

    protected abstract String getPresetName();

    protected abstract void initializeTypes(final BundleContext context);

    protected void addType(String name, Function<String, Object> value) {
        types.put(name, value);
    }

    @Activate
    public void activate(final BundleContext context) {
        types = new HashMap<>();
        initializeTypes(context);
    }

    @Override
    public Collection<String> getDefaultPresets() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getPresets() {
        return Collections.singleton(getPresetName());
    }

    @Override
    public Collection<String> getTypes() {
        return types.keySet();
    }

    @Override
    public @Nullable Object get(String scriptIdentifier, String type) throws IllegalArgumentException {

        Map<String, Object> forScript = idToTypes.computeIfAbsent(scriptIdentifier, k -> new HashMap<>());
        return forScript.computeIfAbsent(type,
                k -> Objects.nonNull(types.get(k)) ? types.get(k).apply(scriptIdentifier) : null);
    }

    @Override
    public Map<String, Object> importPreset(String scriptIdentifier, String preset) {
        if (getPresetName().equals(preset)) {
            Map<String, Object> results = new HashMap<>(types.size());
            for (String type : types.keySet()) {
                results.put(type, get(scriptIdentifier, type));
            }
            return results;
        }

        return Collections.emptyMap();
    }

    @Override
    public void unload(String scriptIdentifier) {
        Map<String, Object> forScript = idToTypes.remove(scriptIdentifier);

        if (forScript != null) {
            for (Object o : forScript.values()) {
                if (o instanceof ScriptDisposalAware) {
                    ((ScriptDisposalAware) o).unload(scriptIdentifier);
                }
            }
        }
    }
}
