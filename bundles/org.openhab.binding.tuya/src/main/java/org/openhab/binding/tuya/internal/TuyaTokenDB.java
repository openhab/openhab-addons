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
package org.openhab.binding.tuya.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.cloud.dto.Token;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;

import com.google.gson.Gson;

/**
 * The {@link TuyaTokenDB} implements storage of tokens.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public class TuyaTokenDB {
    public static final Token noToken = new Token();

    private static @Nullable Storage<String> storage = null;

    private static final Gson gson = new Gson();

    public static void setStorage(StorageService storageService, String name) {
        storage = storageService.getStorage(name);
    }

    public static Token get(String key) {
        Token token = noToken;
        Storage<String> persistent = storage;

        if (persistent != null) {
            token = Objects.requireNonNullElse(gson.fromJson(persistent.get(key), Token.class), noToken);
        }

        return token;
    }

    public static void remove(String key) {
        Storage<String> persistent = storage;

        if (persistent != null) {
            persistent.remove(key);
        }
    }

    public static Token put(String key, Token token) {
        Storage<String> persistent = storage;

        if (persistent != null) {
            persistent.put(key, gson.toJson(token));
        }

        return token;
    }
}
