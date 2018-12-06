/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.storage.Storage;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDataStore.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages users of this emulated HUE bridge.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class UserManagement {
    private final Logger logger = LoggerFactory.getLogger(UserManagement.class);
    private final HueDataStore dataStore;
    private @Nullable Storage<UserAuth> storage;

    public UserManagement(HueDataStore ds) {
        dataStore = ds;
    }

    /**
     * Load users from disk
     */
    public void loadUsersFromFile(Storage<UserAuth> storage) {
        boolean storageChanged = this.storage != null && this.storage != storage;
        this.storage = storage;
        for (String id : storage.getKeys()) {
            UserAuth userAuth = storage.get(id);
            if (userAuth == null) {
                continue;
            }
            dataStore.config.whitelist.put(id, userAuth);
        }
        if (storageChanged) {
            writeToFile();
        }
    }

    /**
     * Checks if the username exists in the whitelist
     */
    @SuppressWarnings("null")
    public boolean authorizeUser(String userName) throws IOException {
        UserAuth userAuth = dataStore.config.whitelist.get(userName);
        if (userAuth != null) {
            userAuth.lastUseDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return userAuth != null;
    }

    /**
     * Adds a user to the whitelist and persist the user file
     */
    public synchronized void addUser(String apiKey, String label) throws IOException {
        if (!dataStore.config.whitelist.containsKey(apiKey)) {
            logger.debug("APIKey {} added", apiKey);
            dataStore.config.whitelist.put(apiKey, new UserAuth(label));
            writeToFile();
        }
    }

    @SuppressWarnings("null")
    public synchronized void removeUser(String apiKey) {
        UserAuth userAuth = dataStore.config.whitelist.remove(apiKey);
        if (userAuth != null) {
            logger.debug("APIKey {} removed", apiKey);
            writeToFile();
        }
    }

    /**
     * Persist users to storage.
     */
    void writeToFile() {
        Storage<UserAuth> storage = this.storage;
        if (storage == null) {
            return;
        }
        dataStore.config.whitelist.forEach((id, userAuth) -> storage.put(id, userAuth));
    }

    public void resetStorage() {
        this.storage = null;
    }
}
