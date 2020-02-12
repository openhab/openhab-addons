/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lightwaverf.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lightwaverf.internal.api.AccessToken;
import org.openhab.binding.lightwaverf.internal.api.FeatureStatus;
import org.openhab.binding.lightwaverf.internal.api.discovery.Devices;
import org.openhab.binding.lightwaverf.internal.api.discovery.Root;
import org.openhab.binding.lightwaverf.internal.api.discovery.StructureList;
import org.openhab.binding.lightwaverf.internal.api.login.Login;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class UpdateListener {
    private final Logger logger = LoggerFactory.getLogger(UpdateListener.class);
    private List<FeatureStatus> featureStatus = new ArrayList<FeatureStatus>();
    private Map<String, Long> featureMap = new HashMap<String,Long>();
    private List<String> channelList = new ArrayList<String>();
    private List<String> cLinked = new ArrayList<String>();
    private List<List<String>> partitions = new ArrayList<>();
    private String jsonBody = "";
    private String jsonEnd = "";
    private String jsonMain = "";
    private boolean isConnected = false;
    private String sessionKey = "";
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public void updateListener(int partitionSize) throws IOException {
        jsonBody = "";
        jsonEnd = "";
        jsonMain = "";
        partitions.clear();
        for (int i = 0; i < channelList.size(); i += partitionSize) {
            partitions.add(channelList.subList(i, Math.min(i + partitionSize, channelList.size())));
        }
        for (int l = 0; l < partitions.size(); l++) {
            jsonBody = "{\"features\": [";
            jsonEnd = "";
            for (int m = 0; m < partitions.get(l).size(); m++) {
                if (m < (partitions.get(l).size() - 1)) {
                    jsonEnd = ",";
                } else {
                    jsonEnd = "]}";
                }
                jsonMain = "{\"featureId\": \"" + partitions.get(l).get(m).toString() + "\"}";
                jsonBody = jsonBody + jsonMain + jsonEnd;
            }

            InputStream data = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8));
            String response = Http.httpClient("features", data, "application/json", "");
            logger.debug("response:{}", response);
            HashMap<String, Integer> featureStatuses = gson.fromJson(response,
                    new TypeToken<HashMap<String, Integer>>() {
                    }.getType());
            for (Map.Entry<String, Integer> myMap : featureStatuses.entrySet()) {
                String key = myMap.getKey().toString();
                int value = myMap.getValue();
                featureStatus.stream().filter(i -> key.equals(i.getFeatureId())).forEach(
                u -> {
                    if(!featureMap.containsKey(key)) {
                        u.setValue(value);
                    }
                    else {
                        logger.debug("feature {} not updated as lock is present", key);
                    }
                }
                );
            }
        }
    }

    public Map<String, Long> getLocked() {
        return featureMap;
    }

    public boolean addLocked( String featureId, Long time ) {
        featureMap.put(featureId,time);
        return true;
    }

    public boolean removeLocked( String featureId, Long time ) {
        featureMap.remove(featureId,time);
        return true;
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    private synchronized void setConnected(boolean state) {
        isConnected = state;
    }

    public List<String> channelList() {
        return channelList;
    }

    public boolean addChannelList( String newLink ) {
        channelList.add(newLink);
        return true;
    }

    public boolean removeChannelList( String newLink ) {
        channelList.remove(newLink );
        return true;
     }

    public List<FeatureStatus> featureStatus() {
        return featureStatus;
    }

    public boolean addFeatureStatus( FeatureStatus newFeatureStatus ) {
        featureStatus.add(newFeatureStatus);
        return true;
    }

    public boolean removeFeatureStatus( FeatureStatus newFeatureStatus ) {
        featureStatus.remove(newFeatureStatus );
        return true;
     }

    public synchronized List<String> cLinked() {
        return cLinked;
    }

    public boolean addcLinked( String newLink ) {
        cLinked.add( newLink );
        logger.debug("Channel added to update list");
        return true;
     }

    public boolean removecLinked( String newLink ) {
        cLinked.remove( newLink );
        logger.debug("Channel removed from update list");
        return true;
     }

    public void login(String username, String password) throws IOException {
        logger.warn("Start Lightwave Login Process");
        setConnected(false);
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("email", username);
        jsonReq.addProperty("password", password);
        InputStream body = new ByteArrayInputStream(jsonReq.toString().getBytes(StandardCharsets.UTF_8));
        String response = Http.httpClient("login", body, "application/json", null);
        logger.debug("Returned Login Http Response {}", response);
        if (response.contains("Not found")) {
            logger.warn("Lightwave Rf Servers Currently Down");
            setConnected(false);
        }
        else{
        Login login = gson.fromJson(response, Login.class);
        logger.debug("Parsed Login response");
        sessionKey = login.getTokens().getAccessToken().toString();
        AccessToken.setToken(sessionKey);
        logger.debug("token: {}", sessionKey);
        setConnected(true);
        logger.warn("Connected to lightwave");
        }
    }

        public StructureList getStructureList() throws IOException {
            String response = Http.httpClient("structures", null, null, null);
            StructureList structureList = gson.fromJson(response, StructureList.class);
            return structureList;
        }

        public Root getStructure(String structureId) throws IOException {
            String response = Http.httpClient("structure", null, null, structureId);
            Root structure = gson.fromJson(response, Root.class);
            return structure;
        }

        public List<Devices> getDevices(String structureId) throws IOException {
            List<Devices> devices = getStructure(structureId).getDevices();
            return devices;
        }
}
