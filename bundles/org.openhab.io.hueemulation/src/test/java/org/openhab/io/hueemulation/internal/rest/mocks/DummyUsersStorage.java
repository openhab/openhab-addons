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
package org.openhab.io.hueemulation.internal.rest.mocks;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.openhab.io.hueemulation.internal.dto.HueUserAuthWithSecrets;

/**
 * @author David Graeff - Initial contribution
 */
public class DummyUsersStorage implements Storage<HueUserAuthWithSecrets> {
    Map<String, HueUserAuthWithSecrets> users = new TreeMap<>();

    public DummyUsersStorage() {
        users.put("testuser", new HueUserAuthWithSecrets("appname", "devicename", "testuser", "clientkey"));
    }

    @Override
    public @Nullable HueUserAuthWithSecrets put(String key, @Nullable HueUserAuthWithSecrets value) {
        return users.put(key, value);
    }

    @Override
    public @Nullable HueUserAuthWithSecrets remove(String key) {
        return users.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return users.containsKey(key);
    }

    @Override
    public @Nullable HueUserAuthWithSecrets get(String key) {
        return users.get(key);
    }

    @Override
    public Collection<@NonNull String> getKeys() {
        return users.keySet();
    }

    @Override
    public Collection<@Nullable HueUserAuthWithSecrets> getValues() {
        return users.values();
    }
}
