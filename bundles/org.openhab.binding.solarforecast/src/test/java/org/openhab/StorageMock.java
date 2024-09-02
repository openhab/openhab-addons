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
package org.openhab;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;

/**
 * The {@link StorageMock} simulates json Storage in openHAB.
 *
 * @author Bernd Weymann - Initial contribution
 */
public class StorageMock implements Storage {
    private HashMap<String, Object> ramStorage = new HashMap();

    @Override
    public boolean containsKey(String key) {
        System.out.println("STORAGE: containsKey " + key);
        return ramStorage.containsKey(key);
    }

    @Override
    public @Nullable Object get(String key) {
        System.out.println("STORAGE: get " + key);
        return ramStorage.get(key);
    }

    @Override
    public Collection getKeys() {
        return ramStorage.keySet();
    }

    @Override
    public Collection getValues() {
        return ramStorage.values();
    }

    @Override
    public @Nullable Object put(String key, @Nullable Object value) {
        System.out.println("STORAGE: put " + key + " : " + value);
        Object ret = ramStorage.remove(key);
        ramStorage.put(key, value);
        return ret;
    }

    @Override
    public @Nullable Object remove(String key) {
        System.out.println("STORAGE: remove " + key);
        return ramStorage.remove(key);
    }
}
