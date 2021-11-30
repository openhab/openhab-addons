/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FreeAtHomeTestDeviceList} is responsible for providing test device lists from file source.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeTestDeviceList implements FreeAtHomeDeviceList {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeTestDeviceList.class);

    private @Nullable List<String> listOfComponentId;

    private String sysApUID;
    private String configFileLocation;
    private String devicelistFileLocation;

    int numberOfComponents;

    public FreeAtHomeTestDeviceList(String devicelistFile, String configFile, String forSysAP) {

        sysApUID = forSysAP;

        configFileLocation = configFile;
        devicelistFileLocation = devicelistFile;

        // Create list of component IDs
        listOfComponentId = new ArrayList<String>();
    }

    @Override
    @SuppressWarnings("null")
    public boolean buildComponentList() {

        listOfComponentId.clear();

        // Perform a simple GET and wait for the response.
        try {
            JsonParser parser = new JsonParser();

            JsonElement jsonTree;

            jsonTree = parser.parse(new FileReader(devicelistFileLocation));

            // check the output
            if (jsonTree.isJsonObject()) {
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                // Get the main object
                JsonElement listOfComponents = jsonObject.get(sysApUID);

                if (null != listOfComponents) {
                    JsonArray array = listOfComponents.getAsJsonArray();

                    this.numberOfComponents = array.size();

                    for (int i = 0; i < array.size(); i++) {
                        JsonElement basicElement = array.get(i);

                        listOfComponentId.add(basicElement.getAsString());
                    }
                }
            }
        } catch (JsonIOException e) {
            logger.error("Error to build up the Component list [ {} ]", e.getMessage());
        } catch (JsonSyntaxException e) {
            // TODO Auto-generated catch block
            logger.error("Error to build up the Component list [ {} ]", e.getMessage());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            logger.error("Error to build up the Component list [ {} ]", e.getMessage());
        }

        // Scan finished
        return true;
    }

    @Override
    public FreeAtHomeDeviceDescription getDeviceDescription(String id) {
        FreeAtHomeDeviceDescription device = new FreeAtHomeDeviceDescription();

        try {
            // Get component List
            JsonParser parser = new JsonParser();
            JsonElement jsonTree = parser.parse(new FileReader(configFileLocation));

            // check the output
            if (null != jsonTree) {
                if (jsonTree.isJsonObject()) {
                    JsonObject jsonObject = jsonTree.getAsJsonObject();

                    jsonObject = jsonObject.getAsJsonObject(sysApUID);
                    jsonObject = jsonObject.getAsJsonObject("devices");

                    device = new FreeAtHomeDeviceDescription(jsonObject, id);
                }
            }
        } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            logger.info("Invalid Json file [ {} ]", e.getMessage());
        }

        return device;
    }

    @Override
    @SuppressWarnings("null")
    public String getDeviceIdByIndex(int index) {
        return listOfComponentId.get(index);
    }

    @Override
    public int getNumberOfDevices() {
        return this.numberOfComponents;
    }
}
