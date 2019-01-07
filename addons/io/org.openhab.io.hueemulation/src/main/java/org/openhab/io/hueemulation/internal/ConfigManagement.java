/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.storage.Storage;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages config values of this emulated HUE bridge.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ConfigManagement {
    private final Logger logger = LoggerFactory.getLogger(ConfigManagement.class);
    private final HueDataStore dataStore;
    private @Nullable Storage<String> storage;
    private @Nullable Thread pairingTimeoutThread;
    private @NonNullByDefault({}) ConfigurationAdmin configAdmin;

    public ConfigManagement(HueDataStore ds) {
        dataStore = ds;
    }

    public void setConfigAdmin(@Nullable ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    /**
     * Load modified config from disk
     */
    public void loadConfigFromFile(Storage<String> storage) {
        boolean storageChanged = this.storage != null && this.storage != storage;
        this.storage = storage;
        String devicename = storage.get("devicename");
        if (devicename == null) {
            devicename = dataStore.config.devicename;
        }
        dataStore.config.devicename = devicename;

        String udnString = storage.get("udn");
        if (udnString == null) {
            udnString = UUID.randomUUID().toString();
            storage.put("udn", udnString);
        }

        dataStore.config.uuid = udnString;
        dataStore.config.bridgeid = dataStore.config.uuid.replace("-", "").substring(0, 12).toUpperCase();

        if (storageChanged) {
            writeToFile();
        }
    }

    /**
     * Persist to storage.
     */
    void writeToFile() {
        Storage<String> storage = this.storage;
        if (storage == null) {
            return;
        }
        storage.put("devicename", dataStore.config.devicename);
        storage.put("udn", dataStore.config.uuid);
    }

    public void resetStorage() {
        this.storage = null;
    }

    /**
     * Starts a pairing timeout thread if dataStore.config.linkbutton is set to true.
     * Stops any already setup timer.
     */
    void checkPairingTimeout() {
        stopPairingTimeoutThread();
        if (dataStore.config.linkbutton) {
            logger.info("Hue Emulation pairing enabled for {}s at {}", dataStore.config.networkopenduration,
                    RESTApi.PATH);
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(dataStore.config.networkopenduration * 1000);
                    org.osgi.service.cm.Configuration configuration = configAdmin
                            .getConfiguration("org.openhab.hueemulation");
                    Dictionary<String, Object> dictionary = configuration.getProperties();
                    dictionary.put(HueEmulationConfig.CONFIG_PAIRING_ENABLED, false);
                    dictionary.put(HueEmulationConfig.CONFIG_CREATE_NEW_USER_ON_THE_FLY, false);
                    configuration.update(dictionary);
                } catch (IOException | InterruptedException ignore) {
                }
            });
            pairingTimeoutThread = thread;
            thread.start();
        } else {
            logger.info("Hue Emulation pairing disabled. Service available under {}", RESTApi.PATH);
        }
    }

    void stopPairingTimeoutThread() {
        Thread thread = pairingTimeoutThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(2000);
            } catch (InterruptedException e) {
            }
            pairingTimeoutThread = null;
        }
    }
}
