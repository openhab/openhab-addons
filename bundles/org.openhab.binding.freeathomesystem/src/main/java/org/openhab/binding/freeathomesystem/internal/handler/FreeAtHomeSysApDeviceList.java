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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link FreeAtHomeSysApDeviceList} is responsible for device lists from free@home bridge.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeSysApDeviceList implements FreeAtHomeDeviceList {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeSysApDeviceList.class);

    private @Nullable HttpClient httpClient;
    private @Nullable String sysApUID;
    private String baseUrl = "";
    private String componentListString = "";

    private List<String> listOfComponentId = new ArrayList<String>();;

    int numberOfComponents;

    public FreeAtHomeSysApDeviceList(HttpClient client, String forIpAddress, String forSysAP) {
        httpClient = client;
        baseUrl = "http://" + forIpAddress + "/fhapi/v1/api/rest";
        sysApUID = forSysAP;

        // Create list of component IDs
        // listOfComponentId = new ArrayList<String>();
    }

    @Override
    public boolean buildComponentList() {
        boolean ret = false;

        listOfComponentId.clear();

        String url = baseUrl + "/devicelist";

        // Perform a simple GET and wait for the response.
        try {
            HttpClient client = httpClient;

            if (null != client) {
                Request req = client.newRequest(url);
                ContentResponse response = req.send();

                // Get component List
                componentListString = new String(response.getContent());

                JsonParser parser = new JsonParser();

                JsonElement jsonTree = parser.parse(this.componentListString);

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

                        ret = true;
                    }
                }
            } else {
                ret = false;
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error to build up the Component list [ {} ]", e.getMessage());

            ret = false;
        }

        // Scan finished
        return ret;
    }

    @Override
    public FreeAtHomeDeviceDescription getDeviceDescription(String id) {
        FreeAtHomeDeviceDescription device = new FreeAtHomeDeviceDescription();

        String url = baseUrl + "/device/" + sysApUID + "/" + id;
        try {
            HttpClient client = httpClient;

            if (null != client) {
                Request req = client.newRequest(url);
                ContentResponse response;
                response = req.send();

                // Get component List
                String deviceString = new String(response.getContent());

                JsonParser parser = new JsonParser();

                JsonElement jsonTree = parser.parse(deviceString);

                // check the output
                if (null != jsonTree) {
                    if (jsonTree.isJsonObject()) {

                        JsonObject jsonObject = jsonTree.getAsJsonObject();

                        jsonObject = jsonObject.getAsJsonObject(sysApUID);

                        if (null != jsonObject) {

                            jsonObject = jsonObject.getAsJsonObject("devices");

                            if (null != jsonObject) {

                                device = new FreeAtHomeDeviceDescription(jsonObject, id);

                            }
                        }
                    }
                }
            }

        } catch (InterruptedException e) {
            logger.info("No communication possible to get device list - Communication interrupt [ {} ]",
                    e.getMessage());
        } catch (TimeoutException e) {
            logger.info("No communication possible to get device list - Communication timeout [ {} ]", e.getMessage());
        } catch (ExecutionException e) {
            logger.info("No communication possible to get device list - exception [ {} ]", e.getMessage());
        }

        return device;
    }

    @Override
    public String getDeviceIdByIndex(int index) {
        return listOfComponentId.get(index);
    }

    @Override
    public int getNumberOfDevices() {
        return this.numberOfComponents;
    }
}
