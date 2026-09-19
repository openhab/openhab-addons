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
package org.openhab.io.eebus.internal.cert;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;

/**
 * A trivial in-memory {@link Storage} used to unit-test {@link EEBusCertificateStorage} without an
 * OSGi runtime.
 */
@NonNullByDefault
public class FakeStorage implements Storage<String> {

    private final Map<String, String> values = new LinkedHashMap<>();

    @Override
    public @Nullable String put(String key, @Nullable String value) {
        return value == null ? values.remove(key) : values.put(key, value);
    }

    @Override
    public @Nullable String remove(String key) {
        return values.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    @Override
    public @Nullable String get(String key) {
        return values.get(key);
    }

    @Override
    public Collection<String> getKeys() {
        return values.keySet();
    }

    @Override
    public Collection<String> getValues() {
        return values.values();
    }
}
