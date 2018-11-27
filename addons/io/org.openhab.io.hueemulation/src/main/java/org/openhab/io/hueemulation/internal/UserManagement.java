/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDataStore.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Manages users of this emulated HUE bridge. Users are persisted to "userdata/hueemulation/usernames.json".
 *
 * @author David Graeff - Initial contribution
 */
public class UserManagement {
    private final Logger logger = LoggerFactory.getLogger(UserManagement.class);
    private static final File USER_FILE = new File(
            ConfigConstants.getUserDataFolder() + File.separator + "hueemulation" + File.separator + "usernames.json");

    private final HueDataStore dataStore;
    private final Gson gson;

    public UserManagement(HueDataStore ds, Gson gson) {
        dataStore = ds;
        this.gson = gson;
    }

    /**
     * Load users from disk
     */
    public void loadUsersFromFile() {
        if (USER_FILE.exists()) {
            try (JsonReader reader = new JsonReader(new FileReader(USER_FILE))) {
                Map<String, UserAuth> tmpMap;
                tmpMap = gson.fromJson(reader, new TypeToken<Map<String, UserAuth>>() {
                }.getType());
                if (tmpMap != null) {
                    dataStore.whitelist.putAll(tmpMap);
                }
            } catch (IOException | IllegalStateException e) {
                logger.warn("File {} error", USER_FILE, e);
            }
        }
    }

    /**
     * Checks if the username exists in the whitelist
     */
    @SuppressWarnings("null")
    public boolean authorizeUser(String userName) throws IOException {
        UserAuth userAuth = dataStore.whitelist.get(userName);
        if (userAuth != null) {
            userAuth.lastUseDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return userAuth != null;
    }

    /**
     * Adds a user to the whitelist and persist the user file
     */
    public synchronized void addUser(String apiKey, String label) throws IOException {
        if (!dataStore.whitelist.containsKey(apiKey)) {
            logger.debug("APIKey {} added", apiKey);
            dataStore.whitelist.put(apiKey, new UserAuth(label));
            writeToFile();
        }
    }

    @SuppressWarnings("null")
    public synchronized void removeUser(String apiKey) {
        UserAuth userAuth = dataStore.whitelist.remove(apiKey);
        if (userAuth != null) {
            logger.debug("APIKey {} removed", apiKey);
            writeToFile();
        }
    }

    /**
     * Persist users to "userdata/hueemulation/usernames.json".
     */
    void writeToFile() {
        USER_FILE.getParentFile().mkdirs();
        try (JsonWriter writer = new JsonWriter(new FileWriter(USER_FILE))) {
            gson.toJson(dataStore.whitelist, new TypeToken<Map<String, UserAuth>>() {
            }.getType(), writer);
        } catch (IOException e) {
            logger.error("Could not persist users", e);
        }
    }

}
