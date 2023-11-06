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
package org.openhab.binding.bosesoundtouch.internal.handler;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bosesoundtouch.internal.ContentItem;
import org.openhab.core.storage.Storage;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class InMemmoryContentStorage implements Storage<ContentItem> {
    Map<String, @Nullable ContentItem> items = new TreeMap<>();

    public InMemmoryContentStorage() {
    }

    @Override
    public @Nullable ContentItem put(String key, @Nullable ContentItem value) {
        return items.put(key, value);
    }

    @Override
    public @Nullable ContentItem remove(String key) {
        return items.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return items.containsKey(key);
    }

    @Override
    public @Nullable ContentItem get(String key) {
        return items.get(key);
    }

    @Override
    public Collection<@NonNull String> getKeys() {
        return items.keySet();
    }

    @Override
    public Collection<@Nullable ContentItem> getValues() {
        return items.values();
    }
}
