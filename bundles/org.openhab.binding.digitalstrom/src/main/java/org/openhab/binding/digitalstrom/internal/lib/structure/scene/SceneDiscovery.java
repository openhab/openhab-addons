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
package org.openhab.binding.digitalstrom.internal.lib.structure.scene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl.JSONResponseHandler;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.ApartmentSceneEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.ZoneSceneEnum;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link SceneDiscovery} can read out various digitalSTROM-Scene types and generates a list of theirs or manages it
 * by the {@link SceneManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneDiscovery {

    private final Logger logger = LoggerFactory.getLogger(SceneDiscovery.class);
    // fields: 0 = namedScenes, 1 = apartmentScenes, 2 = zoneScenes, 3 = reachableScenes
    private final char[] scenesGenerated = "0000".toCharArray();

    private final List<InternalScene> namedScenes = new LinkedList<>();
    private boolean genList = false;
    ScheduledFuture<?> generateReachableScenesScheduledFuture;

    private SceneManager sceneManager;
    private SceneStatusListener discovery;

    public static final String NAMEND_SCENE_QUERY = "/apartment/zones/*(ZoneID)/groups/*(group)/scenes/*(scene,name)";
    public static final String REACHABLE_SCENE_QUERY = "/json/zone/getReachableScenes?id=";
    public static final String REACHABLE_GROUPS_QUERY = "/json/apartment/getReachableGroups?token=";

    /**
     * Creates a new {@link SceneDiscovery} with managed scene by the {@link SceneManager}
     *
     * @param sceneManager must not be null
     */
    public SceneDiscovery(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Creates a new {@link SceneDiscovery} and generates only a list of all scenes, if genList is true.
     *
     * @param genList yes/no (true/false)
     */
    public SceneDiscovery(boolean genList) {
        this.genList = genList;
    }

    /**
     * Generates all named, reachable group, apartment and zone scenes.
     *
     * @param connectionManager must not be null
     * @param structureManager must not be null
     */
    public void generateAllScenes(ConnectionManager connectionManager, StructureManager structureManager) {
        generateNamedScenes(connectionManager);
        generateApartmentScence();
        generateZoneScenes(connectionManager, structureManager);
        generateReachableScenes(connectionManager, structureManager);
    }

    /**
     * Generates all named scenes.
     *
     * @param connectionManager must not be null
     * @return true, if successful otherwise false
     */
    public boolean generateNamedScenes(ConnectionManager connectionManager) {
        JsonObject responsJsonObj = connectionManager.getDigitalSTROMAPI().query(connectionManager.getSessionToken(),
                NAMEND_SCENE_QUERY);
        if (responsJsonObj == null) {
            scenesGenerated[0] = '2';
            sceneManager.scenesGenerated(scenesGenerated);
            return false;
        } else {
            addScenesToList(responsJsonObj);
            scenesGenerated[0] = '1';
            sceneManager.scenesGenerated(scenesGenerated);
            return true;
        }
    }

    /**
     * Generates all apartment scenes.
     */
    public void generateApartmentScence() {
        for (ApartmentSceneEnum apartmentScene : ApartmentSceneEnum.values()) {

            InternalScene scene = new InternalScene(null, null, apartmentScene.getSceneNumber(),
                    "Apartment-Scene: " + apartmentScene.toString().toLowerCase().replace("_", " "));
            if (genList) {
                this.namedScenes.add(scene);
            } else {
                sceneDiscoverd(scene);
            }
        }
        scenesGenerated[1] = '1';
        sceneManager.scenesGenerated(scenesGenerated);
    }

    private void addScenesToList(JsonObject resultJsonObj) {
        if (resultJsonObj.get(JSONApiResponseKeysEnum.ZONES.getKey()) != null
                && resultJsonObj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
            JsonArray zones = resultJsonObj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
            for (int i = 0; i < zones.size(); i++) {

                if (((JsonObject) zones.get(i)).get(JSONApiResponseKeysEnum.GROUPS.getKey()).isJsonArray()) {
                    JsonArray groups = ((JsonObject) zones.get(i)).get(JSONApiResponseKeysEnum.GROUPS.getKey())
                            .getAsJsonArray();

                    for (int j = 0; j < groups.size(); j++) {

                        if (((JsonObject) groups.get(j)).get("scenes") != null
                                && ((JsonObject) groups.get(j)).get("scenes").isJsonArray()) {
                            JsonArray scenes = ((JsonObject) groups.get(j)).get("scenes").getAsJsonArray();
                            for (int k = 0; k < scenes.size(); k++) {
                                if (scenes.get(k).isJsonObject()) {
                                    JsonObject sceneJsonObject = ((JsonObject) scenes.get(k));
                                    int zoneID = ((JsonObject) zones.get(i)).get("ZoneID").getAsInt();
                                    short groupID = ((JsonObject) groups.get(j)).get("group").getAsShort();
                                    InternalScene scene = new InternalScene(zoneID, groupID,
                                            sceneJsonObject.get("scene").getAsShort(),
                                            sceneJsonObject.get("name").getAsString());
                                    if (genList) {
                                        this.namedScenes.add(scene);
                                    } else {
                                        sceneDiscoverd(scene);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates all zone scenes.
     *
     * @param connectionManager must not be null
     * @param structureManager must not be null
     * @return success true otherwise false
     */
    public boolean generateZoneScenes(ConnectionManager connectionManager, StructureManager structureManager) {
        HashMap<Integer, List<Short>> reachableGroups = getReachableGroups(connectionManager);

        if (reachableGroups != null) {
            for (Integer zoneID : reachableGroups.keySet()) {
                if (!reachableGroups.get(zoneID).isEmpty()) {
                    for (ZoneSceneEnum zoneScene : ZoneSceneEnum.values()) {
                        String sceneName = "Zone-Scene: Zone: ";
                        if (structureManager.getZoneName(zoneID) != null
                                && !structureManager.getZoneName(zoneID).isEmpty()) {
                            sceneName = sceneName + structureManager.getZoneName(zoneID);

                        } else {
                            sceneName = sceneName + zoneID;
                        }
                        sceneName = sceneName + " Scene: " + zoneScene.toString().toLowerCase().replace("_", " ");
                        InternalScene scene = new InternalScene(zoneID, null, zoneScene.getSceneNumber(), sceneName);
                        if (genList) {
                            this.namedScenes.add(scene);
                        } else {
                            sceneDiscoverd(scene);
                        }
                    }
                }
            }

            scenesGenerated[2] = '1';
            sceneManager.scenesGenerated(scenesGenerated);
            return true;
        }
        scenesGenerated[2] = '2';
        sceneManager.scenesGenerated(scenesGenerated);
        return false;
    }

    /**
     *
     */
    public void stop() {
        if (generateReachableScenesScheduledFuture != null) {
            generateReachableScenesScheduledFuture.cancel(true);
            generateReachableScenesScheduledFuture = null;
        }
    }

    /**
     * Generates all reachable scenes.
     *
     * @param connectionManager must not be null
     * @param structureManager must not be null
     */
    public void generateReachableScenes(final ConnectionManager connectionManager,
            final StructureManager structureManager) {
        if (generateReachableScenesScheduledFuture == null || generateReachableScenesScheduledFuture.isCancelled()) {
            generateReachableScenesScheduledFuture = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME)
                    .scheduleWithFixedDelay(new Runnable() {

                        HashMap<Integer, List<Short>> reachableGroups = getReachableGroups(connectionManager);
                        Iterator<Integer> zoneIdInter = null;
                        Iterator<Short> groupIdInter = null;
                        Integer zoneID = null;

                        @Override
                        public void run() {
                            if (reachableGroups != null) {
                                if (zoneIdInter == null) {
                                    zoneIdInter = reachableGroups.keySet().iterator();
                                }
                                if (groupIdInter == null) {
                                    if (zoneIdInter.hasNext()) {
                                        zoneID = zoneIdInter.next();
                                        groupIdInter = reachableGroups.get(zoneID).iterator();
                                    } else {
                                        zoneID = null;
                                        scenesGenerated[3] = '1';
                                        sceneManager.scenesGenerated(scenesGenerated);
                                        generateReachableScenesScheduledFuture.cancel(true);
                                    }
                                }
                                if (zoneID != null) {
                                    if (groupIdInter != null) {
                                        Short groupID = null;
                                        if (groupIdInter.hasNext()) {
                                            groupID = groupIdInter.next();
                                        } else {
                                            groupIdInter = null;
                                        }
                                        if (groupID != null) {

                                            if (ApplicationGroup.Color.YELLOW
                                                    .equals(ApplicationGroup.getGroup(groupID).getColor())) {
                                                discoverScene(SceneEnum.AUTO_OFF.getSceneNumber(), groupID);
                                            }
                                            String response = connectionManager.getHttpTransport()
                                                    .execute(REACHABLE_SCENE_QUERY + zoneID + "&groupID=" + groupID
                                                            + "&token=" + connectionManager.getSessionToken());
                                            if (response == null) {
                                                scenesGenerated[3] = '2';
                                                sceneManager.scenesGenerated(scenesGenerated);
                                                logger.debug(
                                                        "Reachable scenes for zone {} and group {} cant be generated, because the dSS does not answer",
                                                        zoneID, groupID);
                                                generateReachableScenesScheduledFuture.cancel(true);
                                                return;
                                            } else {
                                                JsonObject responsJsonObj = JSONResponseHandler.toJsonObject(response);
                                                if (JSONResponseHandler.checkResponse(responsJsonObj)) {
                                                    JsonObject resultJsonObj = JSONResponseHandler
                                                            .getResultJsonObject(responsJsonObj);
                                                    if (resultJsonObj
                                                            .get(JSONApiResponseKeysEnum.REACHABLE_SCENES.getKey())
                                                            .isJsonArray()) {
                                                        JsonArray scenes = resultJsonObj
                                                                .get(JSONApiResponseKeysEnum.REACHABLE_SCENES.getKey())
                                                                .getAsJsonArray();
                                                        if (scenes != null) {
                                                            for (int i = 0; i < scenes.size(); i++) {
                                                                discoverScene(scenes.get(i).getAsShort(), groupID);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        private void discoverScene(short sceneNumber, short groupID) {
                            String sceneName = null;
                            if (SceneEnum.getScene(sceneNumber) != null) {
                                if (structureManager.getZoneName(zoneID) != null
                                        && !structureManager.getZoneName(zoneID).isEmpty()) {
                                    sceneName = "Zone: " + structureManager.getZoneName(zoneID);

                                } else {
                                    sceneName = "Zone: " + zoneID;
                                }
                                if (structureManager.getZoneGroupName(zoneID, groupID) != null
                                        && !structureManager.getZoneGroupName(zoneID, groupID).isEmpty()) {
                                    sceneName = sceneName + " Group: "
                                            + structureManager.getZoneGroupName(zoneID, groupID);
                                } else {
                                    sceneName = sceneName + " Group: " + groupID;
                                }
                                sceneName = sceneName + " Scene: "
                                        + SceneEnum.getScene(sceneNumber).toString().toLowerCase().replace("_", " ");
                            }
                            InternalScene scene = new InternalScene(zoneID, groupID, sceneNumber, sceneName);

                            if (genList) {
                                namedScenes.add(scene);
                            } else {
                                sceneDiscoverd(scene);
                            }
                        }
                    }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    private HashMap<Integer, List<Short>> getReachableGroups(ConnectionManager connectionManager) {
        HashMap<Integer, List<Short>> reachableGroupsMap = null;
        String response = connectionManager.getHttpTransport()
                .execute(SceneDiscovery.REACHABLE_GROUPS_QUERY + connectionManager.getSessionToken());
        if (response == null) {
            return null;
        } else {
            JsonObject responsJsonObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responsJsonObj)) {
                JsonObject resultJsonObj = JSONResponseHandler.getResultJsonObject(responsJsonObj);
                if (resultJsonObj.get(JSONApiResponseKeysEnum.ZONES.getKey()) instanceof JsonArray) {
                    JsonArray zones = (JsonArray) resultJsonObj.get(JSONApiResponseKeysEnum.ZONES.getKey());
                    reachableGroupsMap = new HashMap<>(zones.size());
                    List<Short> groupList;
                    for (int i = 0; i < zones.size(); i++) {
                        if (((JsonObject) zones.get(i))
                                .get(JSONApiResponseKeysEnum.GROUPS.getKey()) instanceof JsonArray) {
                            JsonArray groups = (JsonArray) ((JsonObject) zones.get(i))
                                    .get(JSONApiResponseKeysEnum.GROUPS.getKey());
                            groupList = new LinkedList<>();
                            for (int k = 0; k < groups.size(); k++) {
                                groupList.add(groups.get(k).getAsShort());
                            }
                            reachableGroupsMap.put(((JsonObject) zones.get(i)).get("zoneID").getAsInt(), groupList);
                        }
                    }
                }
            }
        }
        return reachableGroupsMap;
    }

    /**
     * Informs the registered {@link SceneStatusListener} as scene discovery about a new scene.
     *
     * @param scene that was discoverd
     */
    public void sceneDiscoverd(InternalScene scene) {
        if (scene != null) {
            if (SceneEnum.containsScene(scene.getSceneID())) {
                if (!isStandardScene(scene.getSceneID())) {
                    if (this.discovery != null) {
                        this.discovery.onSceneAdded(scene);
                        logger.debug("Inform scene discovery about added scene with id: {}", scene.getID());
                    } else {
                        logger.debug(
                                "Can't inform scene discovery about added scene with id: {} because scene discovery is disabled",
                                scene.getID());
                    }
                }
                this.sceneManager.addInternalScene(scene);
            } else {
                logger.error("Added scene with id: {} is a not usage scene!", scene.getID());
            }
        }
    }

    private boolean isStandardScene(short sceneID) {
        switch (SceneEnum.getScene(sceneID)) {
            case INCREMENT:
            case DECREMENT:
            case STOP:
            case MINIMUM:
            case MAXIMUM:
            case AUTO_OFF:
            case DEVICE_ON:
            case DEVICE_OFF:
            case DEVICE_STOP:
                return true;
            default:
                return false;
        }
    }

    /**
     * Registers the given {@link SceneStatusListener} as scene discovery.
     *
     * @param listener to register
     */
    public void registerSceneDiscovery(SceneStatusListener listener) {
        this.discovery = listener;
    }

    /**
     * Unregisters the {@link SceneStatusListener} as scene discovery from this {@link InternalScene}.
     */
    public void unRegisterDiscovery() {
        this.discovery = null;
    }

    /**
     * Returns the list of all generated {@link InternalScene}'s, if the list shall generated.
     *
     * @return List of all {@link InternalScene} or null
     */
    public List<InternalScene> getNamedSceneList() {
        if (genList) {
            return this.namedScenes;
        } else {
            if (sceneManager != null) {
                sceneManager.getScenes();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.namedScenes.toString();
    }
}
