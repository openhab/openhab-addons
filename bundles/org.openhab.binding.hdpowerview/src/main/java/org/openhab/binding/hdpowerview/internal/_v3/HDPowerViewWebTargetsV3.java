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
package org.openhab.binding.hdpowerview.internal._v3;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.Color;
import org.openhab.binding.hdpowerview.internal.api.HubFirmware;
import org.openhab.binding.hdpowerview.internal.api.ShadeData;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.SurveyData;
import org.openhab.binding.hdpowerview.internal.api.UserData;
import org.openhab.binding.hdpowerview.internal.api._v3.ShadeDataV3;
import org.openhab.binding.hdpowerview.internal.api._v3.ShadePositionV3;
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
import org.openhab.binding.hdpowerview.internal.api.responses._v3.InfoV3;
import org.openhab.binding.hdpowerview.internal.api.responses._v3.SceneV3;
import org.openhab.binding.hdpowerview.internal.api.responses._v3.ScheduledEventV3;
import org.openhab.binding.hdpowerview.internal.api.responses._v3.SseSceneV3;
import org.openhab.binding.hdpowerview.internal.api.responses._v3.SseShadeV3;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubShadeTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * JAX-RS targets for communicating with an HD PowerView hub Generation 3
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewWebTargetsV3 extends HDPowerViewWebTargets {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargetsV3.class);

    private static final String IDS = "ids";

    /**
     * Simple DTO for registering the binding with the hub.
     *
     * @author AndrewFG - Initial contribution
     */
    @SuppressWarnings("unused")
    private static class GatewayRegistration {
        public String todo = "org.openhab.binding.hdpowerview"; // TODO
    }

    private final String shades;
    private final String scenes;
    private final String sceneActivate;
    private final String shadeMotion;
    private final String shadeStop;
    private final String shadePositions;
    private final String firmware;
    private final String automations;
    private final String register;
    private final String shadeEvents;
    private final String sceneEvents;

    // @formatter:off
    private final Type shadeListType = new TypeToken<ArrayList<ShadeData>>() {}.getType();
    // @formatter:on

    // @formatter:off
    private final Type sceneListType = new TypeToken<ArrayList<Scene>>() {}.getType();
    // @formatter:on

    // @formatter:off
    private final Type scheduledEventListType =
            new TypeToken<ArrayList<ScheduledEvent>>() {}.getType();
    // @formatter:on

    private boolean isRegistered;
    private @Nullable SseSinkV3 sseSink;
    private final Client sseClient = ClientBuilder.newClient();
    private @Nullable SseEventSource shadeEventSource;
    private @Nullable SseEventSource sceneEventSource;

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

        String home = base + "home/";
        shades = home + "shades";
        scenes = home + "scenes";
        sceneActivate = home + "scenes/%d/activate";
        shadeMotion = home + "shades/%d/motion";
        shadeStop = home + "shades/stop";
        shadePositions = home + "shades/positions";
        automations = home + "automations";
        shadeEvents = home + "shades/events";
        sceneEvents = home + "scenes/events";

        String gateway = base + "gateway/";
        firmware = gateway + "info";
        register = gateway + "TBD"; // TODO
    }

    @Override
    public HubFirmware getFirmwareVersions()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, firmware, null, null);
        return toFirmware(json);
    }

    @Override
    public UserData getUserData() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        throw new HubProcessingException("getUserData(): method not implemented");
    }

    @Override
    public Shades getShades() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, shades, null, null);
        return toShades(json);
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
        return toScenes(json);
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
        return toScheduledEvents(json);
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
        return toShadeData(json);
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

    /**
     * Register the binding with the hub (if not already registered).
     *
     * @throws HubProcessingException
     * @throws HubMaintenanceException
     */
    private void gatewayRegister() throws HubMaintenanceException, HubProcessingException {
        if (!isRegistered) {
            String json = gson.toJson(new GatewayRegistration());
            invoke(HttpMethod.PUT, register, null, json);
            isRegistered = true;
        }
    }

    /**
     * Set the sink for SSE events.
     *
     * @param sseSink a class that implements the SseSinkV3 interface.
     * @return true if registered for SSE events.
     * @throws HubProcessingException
     * @throws HubMaintenanceException
     */
    public boolean sseSubscribe(@Nullable SseSinkV3 sseSink) throws HubMaintenanceException, HubProcessingException {
        SseEventSource source;

        this.sseSink = sseSink;

        if (sseSink != null) {
            // register ourself with the gateway (if necessary)
            gatewayRegister();

            // open SSE channel for shades
            SseEventSource shadeEventSource = this.shadeEventSource;
            if (shadeEventSource == null || !shadeEventSource.isOpen()) {
                source = SseEventSource.target(sseClient.target(shadeEvents)).build();
                source.register((event) -> onShadeEvent(event));
                source.open();
                if (!source.isOpen()) {
                    throw new HubProcessingException("setEventSink(): failed to open SSE channel for shades");
                }
                this.shadeEventSource = source;
            }

            // open SSE channel for scenes
            SseEventSource sceneEventSource = this.sceneEventSource;
            if (sceneEventSource == null || !sceneEventSource.isOpen()) {
                source = SseEventSource.target(sseClient.target(sceneEvents)).build();
                source.register((event) -> onSceneEvent(event));
                source.open();
                if (!source.isOpen()) {
                    throw new HubProcessingException("setEventSink(): failed to open SSE channel for scenes");
                }
                this.sceneEventSource = source;
            }
            return true;
        }

        // close SSE channel for shades
        source = shadeEventSource;
        if (source != null) {
            source.close();
            shadeEventSource = null;
        }

        // close SSE channel for scenes
        source = sceneEventSource;
        if (source != null) {
            source.close();
            sceneEventSource = null;
        }
        return false;
    }

    /**
     * Handle inbound SSE events for a shade.
     *
     * @param sseEvent the inbound event
     */
    private void onShadeEvent(InboundSseEvent sseEvent) {
        SseSinkV3 eventSink = this.sseSink;
        if (eventSink != null) {
            String json = sseEvent.readData();
            logger.trace("onShadeEvent(): {}", json);
            try {
                ShadeData shadeData = toShadeData2(json);
                ShadePosition shadePosition = shadeData.positions;
                if (shadePosition != null) {
                    String evt = toEvt(json);
                    eventSink.sseShade(evt, shadeData.id, shadePosition);
                }
            } catch (HubInvalidResponseException e) {
                // swallow the exception; don't pass back to caller
            }
        }
    }

    /**
     * Handle inbound SSE events for a scene.
     *
     * @param sseEvent the inbound event
     */
    private void onSceneEvent(InboundSseEvent sseEvent) {
        SseSinkV3 eventSink = this.sseSink;
        if (eventSink != null) {
            String json = sseEvent.readData();
            logger.trace("onSceneEvent(): {}", json);
            try {
                Scene scene = toScene(json);
                String evt = toEvt(json);
                eventSink.sseScene(evt, scene.id);
            } catch (HubInvalidResponseException e) {
                // swallow the exception; don't pass back to caller
            }
        }
    }

    /**
     * Create ShadeData instance from a JSON payload.
     *
     * @param json the json payload
     * @return a ShadeData instance
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private ShadeData toShadeData(String json) throws HubInvalidResponseException {
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

    /**
     * Create Shades instance from a JSON payload.
     *
     * @param json the json payload
     * @return a Shades instance
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private Shades toShades(String json) throws HubInvalidResponseException {
        try {
            Shades shades = new Shades();
            shades.shadeData = gson.fromJson(json, shadeListType);
            if (shades.shadeData == null) {
                throw new HubInvalidResponseException("Missing 'shades.shadeData' element");
            }
            return shades;
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing shades response", e);
        }
    }

    /**
     * Create HubFirmware instance from a JSON payload.
     *
     * @param json the json payload
     * @return a HubFirmware instance
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private HubFirmware toFirmware(String json) throws HubProcessingException {
        try {
            InfoV3 gatewayInfo = gson.fromJson(json, InfoV3.class);
            if (gatewayInfo == null) {
                throw new HubProcessingException("getFirmwareVersions(): missing gatewayInfo");
            }
            return gatewayInfo.toHubFirmware();
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing gateway info response", e);
        }
    }

    /**
     * Create Scenes instance from a JSON payload.
     *
     * @param json the json payload
     * @return a Scenes instance
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private Scenes toScenes(String json) throws HubInvalidResponseException {
        try {
            Scenes scenes = new Scenes();
            scenes.sceneData = gson.fromJson(json, sceneListType);
            return scenes;
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing scenes response", e);
        }
    }

    /**
     * Create ScheduledEvents instance from a JSON payload.
     *
     * @param json the json payload
     * @return a ScheduledEvents instance
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private ScheduledEvents toScheduledEvents(String json) throws HubInvalidResponseException {
        try {
            ScheduledEvents scheduledEvents = new ScheduledEvents();
            scheduledEvents.scheduledEventData = gson.fromJson(json, scheduledEventListType);
            return scheduledEvents;
        } catch (JsonParseException e) {
            throw new HubInvalidResponseException("Error parsing automation response", e);
        }
    }

    /**
     * Get the 'evt' message from an event JSON payload.
     *
     * @param json the json payload
     * @return the message
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private String toEvt(String json) throws HubInvalidResponseException {
        SseShadeV3 sseShade = gson.fromJson(json, SseShadeV3.class);
        if (sseShade != null) {
            String evt = sseShade.evt;
            if (evt != null) {
                return evt;
            }
        }
        throw new HubInvalidResponseException("Error parsing event");
    }

    /**
     * Create ShadeData instance from a JSON shade event payload.
     *
     * @param json the json payload
     * @return a ShadeData instance
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private ShadeData toShadeData2(String json) throws HubInvalidResponseException {
        SseShadeV3 sseShade = gson.fromJson(json, SseShadeV3.class);
        if (sseShade != null) {
            ShadePosition shadePosition = sseShade.currentPositions;
            if (shadePosition != null) {
                ShadeData shadeData = new ShadeDataV3();
                shadeData.id = sseShade.id;
                shadeData.positions = sseShade.currentPositions;
                return shadeData;
            }
        }
        throw new HubInvalidResponseException("Error parsing shade event");
    }

    /**
     * Create Scene instance from a JSON scene event payload.
     *
     * @param json the json payload
     * @return a Scene instance
     * @throws HubInvalidResponseException in case of missing or invalid response
     */
    private Scene toScene(String json) throws HubInvalidResponseException {
        SseSceneV3 sceneEvent = gson.fromJson(json, SseSceneV3.class);
        if (sceneEvent != null) {
            Scene scene = sceneEvent.scene;
            if (scene != null) {
                return scene;
            }
        }
        throw new HubInvalidResponseException("Error parsing scene event");
    }
}
