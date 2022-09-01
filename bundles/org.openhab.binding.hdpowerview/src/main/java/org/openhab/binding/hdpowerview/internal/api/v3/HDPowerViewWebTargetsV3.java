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
package org.openhab.binding.hdpowerview.internal.api.v3;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.Color;
import org.openhab.binding.hdpowerview.internal.api.HubFirmware;
import org.openhab.binding.hdpowerview.internal.api.ShadeData;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.SurveyData;
import org.openhab.binding.hdpowerview.internal.api.UserData;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeCalibrate;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeJog;
import org.openhab.binding.hdpowerview.internal.api.responses.RepeaterData;
import org.openhab.binding.hdpowerview.internal.api.responses.Repeaters;
import org.openhab.binding.hdpowerview.internal.api.responses.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.Shade;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubShadeTimeoutException;

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

    // @formatter:off
    private final Type shadeType = new TypeToken<ArrayList<ShadeData>>() {}.getType();
    // @formatter:on

    // @formatter:off
    private final Type sceneType = new TypeToken<ArrayList<Scene>>() {}.getType();
    // @formatter:on

    // @formatter:off
    private final Type scheduledEventType =
            new TypeToken<ArrayList<ScheduledEvent>>() {}.getType();
    // @formatter:on

    /**
     * Initialize the web targets
     *
     * @param httpClient the HTTP client (the binding)
     * @param ipAddress the IP address of the server (the hub)
     */
    public HDPowerViewWebTargetsV3(HttpClient httpClient, String ipAddress) {
        super(httpClient, ipAddress, SceneV3.class, ShadeDataV3.class, ShadePositionV3.class, ScheduledEventV3.class);

        // initialize the urls
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

    /**
     * Protected method to create ShadeData instances from a JSON payload.
     *
     * @param json the json payload
     * @return a ShadeData instance
     * @throws HubInvalidResponseException in case od missing or invalid response
     * @throws HubShadeTimeoutException in case of connection time out (in V1 implementation)
     */
    protected ShadeData shadeDataFromJson(String json) throws HubInvalidResponseException, HubShadeTimeoutException {
        try {
            Shade shade = gson.fromJson(json, Shade.class);
            if (shade == null) {
                throw new HubInvalidResponseException("Missing shade response");
            }
            ShadeData shadeData = shade.shade;
            if (shadeData == null) {
                throw new HubInvalidResponseException("Missing 'shade.shade' element");
            }
            return shadeData;
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing shade response", e);
        }
    }

    @Override
    public HubFirmware getFirmwareVersions()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, firmware, null, null);
        try {
            GatewayInfoV3 gatewayInfo = gson.fromJson(json, GatewayInfoV3.class);
            if (gatewayInfo == null) {
                throw new HubProcessingException("getFirmwareVersions(): missing gatewayInfo");
            }
            return gatewayInfo.toHubFirmware();
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing gateway info response", e);
        }
    }

    @Override
    public UserData getUserData() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("getUserData(): method not implemented");
    }

    @Override
    public Shades getShades() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, shades, null, null);
        try {
            Shades shades = new Shades();
            shades.shadeData = gson.fromJson(json, shadeType);
            if (shades.shadeData == null) {
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
        String json = gson.toJson(new ShadeJog());
        invoke(HttpMethod.PUT, String.format(shadeMotion, shadeId), null, json);
        return getShade(shadeId);
    }

    @Override
    public ShadeData calibrateShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException {
        String json = gson.toJson(new ShadeCalibrate());
        invoke(HttpMethod.PUT, String.format(shadeMotion, shadeId), null, json);
        return getShade(shadeId);
    }

    @Override
    public Scenes getScenes() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, this.scenes, null, null);
        try {
            Scenes scenes = new Scenes();
            scenes.sceneData = gson.fromJson(json, sceneType);
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
        String json = invoke(HttpMethod.GET, automations, null, null);
        try {
            ScheduledEvents scheduledEvents = new ScheduledEvents();
            scheduledEvents.scheduledEventData = gson.fromJson(json, scheduledEventType);
            return scheduledEvents;
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing automation response", e);
        }
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
        String json = invoke(HttpMethod.GET, shades + Integer.toString(shadeId), null, null);
        return shadeDataFromJson(json);
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
}
