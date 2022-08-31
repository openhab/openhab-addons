/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.hdpowerview.internal.api.Color;
import org.openhab.binding.hdpowerview.internal.api.HubFirmware;
import org.openhab.binding.hdpowerview.internal.api.ShadeData;
import org.openhab.binding.hdpowerview.internal.api.ShadeDataV3;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.ShadePositionV3;
import org.openhab.binding.hdpowerview.internal.api.SurveyData;
import org.openhab.binding.hdpowerview.internal.api.UserData;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeCalibrate;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeJog;
import org.openhab.binding.hdpowerview.internal.api.responses.GatewayInfo;
import org.openhab.binding.hdpowerview.internal.api.responses.RepeaterData;
import org.openhab.binding.hdpowerview.internal.api.responses.Repeaters;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEventV3;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubShadeTimeoutException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * JAX-RS targets for communicating with an HD PowerView hub Generation 3
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewWebTargetsV3 extends HDPowerViewWebTargets {

    private static final String IDS = "ids";

    private final String shades;
    private final String scenes;
    private final String sceneActivate;
    private final String shadeMotion;
    private final String shadeStop;
    private final String shadePositions;
    private final String firmware;
    private final String automations;

    /**
     * Initialize the web targets
     *
     * @param httpClient the HTTP client (the binding)
     * @param ipAddress the IP address of the server (the hub)
     */
    public HDPowerViewWebTargetsV3(HttpClient httpClient, String ipAddress) {
        super(httpClient, ipAddress);

        String base = "http://" + ipAddress + "/";
        shades = base + "home/shades/";
        scenes = base + "home/scenes/";
        sceneActivate = base + "home/scenes/%d/activate";
        shadeMotion = base + "home/shades/%d/motion";
        shadeStop = base + "home/shades/stop";
        shadePositions = base + "home/shades/positions";
        automations = base + "home/automations";
        firmware = base + "gateway/info";
    }

    @Override
    public HubFirmware getFirmwareVersions()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String jsonResponse = invoke(HttpMethod.GET, firmware, null, null);
        GatewayInfo gatewayInfo = gson.fromJson(jsonResponse, GatewayInfo.class);
        if (gatewayInfo == null) {
            throw new HubProcessingException("getFirmwareVersions(): missing gatewayInfo");
        }
        return gatewayInfo.getHubFirmware();
    }

    @Override
    public UserData getUserData() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("getUserData(): method not implemented");
    }

    @Override
    public Shades getShades() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, shades, null, null);
        try {
            Shades shades = gson.fromJson(json, Shades.class);
            if (shades == null) {
                throw new HubInvalidResponseException("Missing shades response");
            }
            List<ShadeData> shadeData = shades.shadeData;
            if (shadeData == null) {
                throw new HubInvalidResponseException("Missing 'shades.shadeData' element");
            }
            return shades;
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing shades response", e);
        }
    }

    @Override
    public ShadeData moveShade(int shadeId, ShadePosition position) throws HubInvalidResponseException,
            HubProcessingException, HubMaintenanceException, HubShadeTimeoutException {
        invoke(HttpMethod.PUT, shadePositions, Query.of(IDS, Integer.valueOf(shadeId).toString()),
                gson.toJson(position));
        return getShade(shadeId);
    }

    @Override
    public ShadeData stopShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException {
        invoke(HttpMethod.PUT, shadeStop, Query.of(IDS, Integer.valueOf(shadeId).toString()), null);
        return getShade(shadeId);
    }

    @Override
    public ShadeData jogShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException {
        String jsonRequest = gson.toJson(new ShadeJog());
        invoke(HttpMethod.PUT, String.format(shadeMotion, shadeId), null, jsonRequest);
        return getShade(shadeId);
    }

    @Override
    public ShadeData calibrateShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException {
        String jsonRequest = gson.toJson(new ShadeCalibrate());
        invoke(HttpMethod.PUT, String.format(shadeMotion, shadeId), null, jsonRequest);
        return getShade(shadeId);
    }

    @Override
    public Scenes getScenes() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, scenes, null, null);
        try {
            Scenes scenes = gson.fromJson(json, Scenes.class);
            if (scenes == null) {
                throw new HubInvalidResponseException("Missing scenes response");
            }
            List<Scene> sceneData = scenes.sceneData;
            if (sceneData == null) {
                throw new HubInvalidResponseException("Missing 'scenes.sceneData' element");
            }
            return scenes;
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing scenes response", e);
        }
    }

    @Override
    public void activateScene(int sceneId) throws HubProcessingException, HubMaintenanceException {
        invoke(HttpMethod.PUT, String.format(sceneActivate, sceneId), null, null);
    }

    @Override
    public SceneCollections getSceneCollections()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("getSceneCollections(): method not implemented");
    }

    @Override
    public void activateSceneCollection(int sceneCollectionId) throws HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("activateSceneCollection(): method not implemented");
    }

    @Override
    public ScheduledEvents getScheduledEvents()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        ScheduledEvents result = new ScheduledEvents();
        String jsonResponse = invoke(HttpMethod.GET, automations, null, null);
        // TODO we really need to check this funky code..
        Type typeOfListOfScheduledEvent = new TypeToken<ArrayList<ScheduledEvent>>() {
        }.getType();
        result.scheduledEventData = gson.fromJson(jsonResponse, typeOfListOfScheduledEvent);
        return result;
    }

    @Override
    public void enableScheduledEvent(int scheduledEventId, boolean enable)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("enableScheduledEvent(): method not implemented");
    }

    @Override
    public Repeaters getRepeaters()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("getRepeaters(): method not implemented");
    }

    @Override
    public RepeaterData getRepeater(int repeaterId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("getRepeater(): method not implemented");
    }

    @Override
    public RepeaterData identifyRepeater(int repeaterId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("identifyRepeater(): method not implemented");
    }

    @Override
    public RepeaterData enableRepeaterBlinking(int repeaterId, boolean enable)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("enableRepeaterBlinking(): method not implemented");
    }

    @Override
    public RepeaterData setRepeaterColor(int repeaterId, Color color)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("setRepeaterColor(): method not implemented");
    }

    @Override
    public ShadeData getShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException {
        String jsonResponse = invoke(HttpMethod.GET, shades + Integer.toString(shadeId), null, null);
        return shadeDataFromJson(jsonResponse);
    }

    @Override
    public ShadeData refreshShadePosition(int shadeId)
            throws JsonParseException, HubProcessingException, HubMaintenanceException, HubShadeTimeoutException {
        return getShade(shadeId);
    }

    @Override
    public List<SurveyData> getShadeSurvey(int shadeId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("getShadeSurvey(): method not implemented");
    }

    @Override
    public ShadeData refreshShadeBatteryLevel(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException {
        return getShade(shadeId);
    }

    private static class ShadeDataDeserializer implements JsonDeserializer<ShadeData> {
        @Override
        public @Nullable ShadeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, ShadeDataV3.class);
        }
    }

    private static class ShadePositionDeserializer implements JsonDeserializer<ShadePosition> {
        @Override
        public @Nullable ShadePosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, ShadePositionV3.class);
        }
    }

    private static class ScheduledEventDeserializer implements JsonDeserializer<ScheduledEvent> {
        @Override
        public @Nullable ScheduledEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, ScheduledEventV3.class);
        }
    }

    @Override
    protected Gson getGsonObject() {
        return getGson();
    }

    /**
     * Public static method to get Gson object. e.g. used for JUnit testing.
     *
     * @return an instance of the Gson class.
     */
    public static Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(ShadeData.class, new ShadeDataDeserializer())
                .registerTypeAdapter(ShadePosition.class, new ShadePositionDeserializer())
                .registerTypeAdapter(ScheduledEvent.class, new ScheduledEventDeserializer()).create();
    }
}
