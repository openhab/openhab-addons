/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.yamlcomposer.internal.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A mutable lexical scope with an optional parent scope.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class Scope {
    private final Map<String, @Nullable Object> bindings = new LinkedHashMap<>();
    private final @Nullable Scope parent;

    public Scope() {
        this(null);
    }

    public Scope(@Nullable Scope parent) {
        this.parent = parent;
    }

    public @Nullable Object get(String name) {
        if (bindings.containsKey(name)) {
            return bindings.get(name);
        }
        Scope currentParent = parent;
        return currentParent != null ? currentParent.get(name) : null;
    }

    public boolean containsKey(String name) {
        if (bindings.containsKey(name)) {
            return true;
        }
        Scope currentParent = parent;
        return currentParent != null && currentParent.containsKey(name);
    }

    public void put(String name, @Nullable Object value) {
        bindings.put(name, value);
    }

    public void putAll(Map<String, @Nullable Object> map) {
        bindings.putAll(map);
    }

    public Map<String, @Nullable Object> flatten() {
        Scope currentParent = parent;
        Map<String, @Nullable Object> result = currentParent != null ? currentParent.flatten() : new LinkedHashMap<>();
        result.putAll(bindings);
        return result;
    }

    public Scope createChild() {
        return new Scope(this);
    }
}
