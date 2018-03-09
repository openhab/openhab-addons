/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store and load the state in and from a file
 *
 * @author Michael Geramb - Initial Contribution
 */
public class StateStorage {

    private final Logger logger = LoggerFactory.getLogger(StateStorage.class);

    File propertyFile;
    Thing thing;
    Properties properties;

    public StateStorage(Thing thing) {
        this.thing = thing;
        propertyFile = new File(
                ConfigConstants.getUserDataFolder() + File.separator + AmazonEchoControlBindingConstants.BINDING_ID
                        + File.separator + thing.getUID().getAsString().replace(':', '_') + ".properties");
    }

    public void storeState(String key, String value) {
        synchronized (this) {
            if (key == null) {
                return;
            }
            initProperties();
            if (value == null || value.isEmpty()) {
                properties.remove(key);
            } else {
                properties.setProperty(key, value);
            }
            // store the property also in OH to see it in the GUI
            thing.setProperty(key, value);
            saveProperties();
        }
    }

    @SuppressWarnings("null")
    public String findState(String key) {
        synchronized (this) {
            initProperties();
            Object value = properties.get(key);
            if (value == null) {
                // upgrade from BETA 9 configuration
                String oldValue = thing.getProperties().get(key);
                if (oldValue != null && !oldValue.isEmpty()) {
                    value = oldValue;
                    storeState(key, oldValue);
                }
            }
            if (value != null) {
                return value.toString();
            }
            return null;
        }
    }

    private void saveProperties() {

        try {
            logger.debug("Create file {}.", propertyFile);
            String directoryName = propertyFile.getParent();
            File directory = new File(directoryName);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            FileWriter fileWriter = new FileWriter(propertyFile);
            properties.store(fileWriter, "Save properties");
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Saving properties failed {}", e);
        }

    }

    private void initProperties() {
        if (properties == null) {
            Properties p = new Properties();

            if (propertyFile.exists()) {
                try {
                    FileReader fileReader = new FileReader(propertyFile);
                    p.load(fileReader);
                    fileReader.close();
                } catch (IOException e) {
                    logger.error("Error occured on writing the property file.", e);
                }
            }
            properties = p;
        }
    }
}
