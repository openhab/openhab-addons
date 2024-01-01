/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.manager.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.lib.event.EventListener;
import org.openhab.binding.digitalstrom.internal.lib.event.constants.EventNames;
import org.openhab.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.openhab.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.openhab.binding.digitalstrom.internal.lib.listener.ManagerStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.stateenums.ManagerStates;
import org.openhab.binding.digitalstrom.internal.lib.listener.stateenums.ManagerTypes;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.SceneDiscovery;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneManagerImpl} is the implementation of the {@link SceneManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SceneManagerImpl implements SceneManager {

    /**
     * Contains all supported event-types.
     */
    public static final List<String> SUPPORTED_EVENTS = Arrays.asList(EventNames.CALL_SCENE, EventNames.UNDO_SCENE);

    private final Logger logger = LoggerFactory.getLogger(SceneManagerImpl.class);

    private final List<String> echoBox = Collections.synchronizedList(new LinkedList<>());
    private final Map<String, InternalScene> internalSceneMap = Collections.synchronizedMap(new HashMap<>());

    private EventListener eventListener;
    private final StructureManager structureManager;
    private final ConnectionManager connectionManager;
    private final SceneDiscovery discovery;
    private ManagerStatusListener statusListener;

    private ManagerStates state = ManagerStates.STOPPED;
    private boolean scenesGenerated = false;

    /**
     * Creates a new {@link SceneManagerImpl} through the given managers.
     *
     * @param connectionManager (must not be null)
     * @param structureManager (must not be null)
     */
    public SceneManagerImpl(ConnectionManager connectionManager, StructureManager structureManager) {
        this.structureManager = structureManager;
        this.connectionManager = connectionManager;
        this.discovery = new SceneDiscovery(this);
    }

    /**
     * Same constructor like {@link #SceneManagerImpl(ConnectionManager, StructureManager)}, but a
     * {@link ManagerStatusListener} can be set, too.
     *
     * @param connectionManager (must not be null)
     * @param structureManager (must not be null)
     * @param statusListener (can be null)
     * @see #SceneManagerImpl(ConnectionManager, StructureManager)
     */
    public SceneManagerImpl(ConnectionManager connectionManager, StructureManager structureManager,
            ManagerStatusListener statusListener) {
        this.structureManager = structureManager;
        this.connectionManager = connectionManager;
        this.discovery = new SceneDiscovery(this);
        this.statusListener = statusListener;
    }

    /**
     * Same constructor like {@link #SceneManagerImpl(ConnectionManager, StructureManager, ManagerStatusListener)}, but
     * an {@link EventListener} can be set, too.
     *
     * @param connectionManager (must not be null)
     * @param structureManager (must not be null)
     * @param statusListener (can be null)
     * @param eventListener (can be null)
     * @see #SceneManagerImpl(ConnectionManager, StructureManager, ManagerStatusListener)
     */
    public SceneManagerImpl(ConnectionManager connectionManager, StructureManager structureManager,
            ManagerStatusListener statusListener, EventListener eventListener) {
        this.structureManager = structureManager;
        this.connectionManager = connectionManager;
        this.discovery = new SceneDiscovery(this);
        this.statusListener = statusListener;
        this.eventListener = eventListener;
    }

    @Override
    public void start() {
        logger.debug("start SceneManager");
        if (eventListener == null) {
            logger.debug("no EventListener is set, create a new EventListener");
            eventListener = new EventListener(connectionManager, this);
        } else {
            logger.debug("EventListener is set, add this SceneManager as EventHandler");
            eventListener.addEventHandler(this);
        }
        eventListener.start();
        logger.debug("start SceneManager");
        stateChanged(ManagerStates.RUNNING);
    }

    @Override
    public void stop() {
        logger.debug("stop SceneManager");
        if (eventListener != null) {
            eventListener.removeEventHandler(this);
        }
        this.discovery.stop();
        this.stateChanged(ManagerStates.STOPPED);
    }

    @Override
    public void handleEvent(EventItem eventItem) {
        if (eventItem == null) {
            return;
        }
        boolean isCallScene = true;
        String isCallStr = eventItem.getName();
        if (isCallStr != null) {
            isCallScene = isCallStr.equals(EventNames.CALL_SCENE);
        }

        boolean isDeviceCall = false;
        String deviceCallStr = eventItem.getSource().get(EventResponseEnum.IS_DEVICE);
        if (deviceCallStr != null) {
            isDeviceCall = Boolean.parseBoolean(deviceCallStr);
        }

        if (isDeviceCall) {
            String dsidStr = null;
            dsidStr = eventItem.getSource().get(EventResponseEnum.DSID);
            short sceneId = -1;
            String sceneStr = eventItem.getProperties().get(EventResponseEnum.SCENEID);
            if (sceneStr != null) {
                try {
                    sceneId = Short.parseShort(sceneStr);
                } catch (java.lang.NumberFormatException e) {
                    logger.error("An exception occurred, while handling event at parsing sceneID: {}", sceneStr, e);
                }
            }

            if (!isEcho(dsidStr, sceneId)) {
                logger.debug("{} event for device: {}", eventItem.getName(), dsidStr);
                if (isCallScene) {
                    this.callDeviceScene(dsidStr, sceneId);
                } else {
                    this.undoDeviceScene(dsidStr);
                }
            }
        } else {
            String intSceneID = null;
            String zoneIDStr = eventItem.getSource().get(EventResponseEnum.ZONEID);
            String groupIDStr = eventItem.getSource().get(EventResponseEnum.GROUPID);
            String sceneIDStr = eventItem.getProperties().get(EventResponseEnum.SCENEID);

            if (zoneIDStr != null && sceneIDStr != null && groupIDStr != null) {
                intSceneID = zoneIDStr + "-" + groupIDStr + "-" + sceneIDStr;
                if (!isEcho(intSceneID)) {
                    logger.debug("{} event for scene: {}-{}-{}", eventItem.getName(), zoneIDStr, groupIDStr,
                            sceneIDStr);
                    if (isCallScene) {
                        this.callInternalScene(intSceneID);
                    } else {
                        this.undoInternalScene(intSceneID);
                    }
                }
            }
        }
    }

    private boolean isEcho(String dsid, short sceneId) {
        // sometimes the dS-event has a dSUID saved in the dSID
        String echo = (structureManager.getDeviceByDSUID(dsid) != null
                ? structureManager.getDeviceByDSUID(dsid).getDSID().getValue()
                : dsid) + "-" + sceneId;
        logger.debug("An echo scene event was detected: {}", echo);
        return isEcho(echo);
    }

    private boolean isEcho(String echoID) {
        if (echoBox.contains(echoID)) {
            echoBox.remove(echoID);
            return true;
        }
        return false;
    }

    // ... we want to ignore own 'command-echos'
    @Override
    public void addEcho(String dsid, short sceneId) {
        addEcho(dsid + "-" + sceneId);
    }

    // ... we want to ignore own 'command-echos'
    @Override
    public void addEcho(String internalSceneID) {
        echoBox.add(internalSceneID);
    }

    @Override
    public void callInternalScene(InternalScene scene) {
        InternalScene intScene = this.internalSceneMap.get(scene.getID());
        if (intScene != null) {
            intScene.activateScene();
        } else {
            if (SceneEnum.getScene(scene.getSceneID()) != null
                    && structureManager.checkZoneGroupID(scene.getZoneID(), scene.getGroupID())) {
                scene.addReferenceDevices(this.structureManager.getReferenceDeviceListFromZoneXGroupX(scene.getZoneID(),
                        scene.getGroupID()));
                this.internalSceneMap.put(scene.getID(), scene);
                scene.activateScene();
            }
        }
    }

    @Override
    public void callInternalSceneWithoutDiscovery(Integer zoneID, Short groupID, Short sceneID) {
        InternalScene intScene = this.internalSceneMap.get(zoneID + "-" + groupID + "-" + sceneID);
        if (intScene != null) {
            intScene.activateScene();
        } else {
            InternalScene scene = new InternalScene(zoneID, groupID, sceneID, null);
            if (structureManager.checkZoneGroupID(scene.getZoneID(), scene.getGroupID())) {
                scene.addReferenceDevices(this.structureManager.getReferenceDeviceListFromZoneXGroupX(scene.getZoneID(),
                        scene.getGroupID()));
                scene.activateScene();
            }
        }
    }

    @Override
    public void callInternalScene(String sceneID) {
        InternalScene intScene = this.internalSceneMap.get(sceneID);
        if (intScene != null) {
            logger.debug("activating existing scene {}", intScene.getSceneName());
            intScene.activateScene();
        } else {
            intScene = createNewScene(sceneID);
            if (intScene != null) {
                logger.debug("created new scene, activating it: {}", intScene.getSceneName());
                discovery.sceneDiscoverd(intScene);
                intScene.activateScene();
            }
        }
    }

    @Override
    public void addInternalScene(InternalScene intScene) {
        if (!this.internalSceneMap.containsKey(intScene.getID())) {
            if (SceneEnum.getScene(intScene.getSceneID()) != null
                    && structureManager.checkZoneGroupID(intScene.getZoneID(), intScene.getGroupID())) {
                intScene.addReferenceDevices(this.structureManager
                        .getReferenceDeviceListFromZoneXGroupX(intScene.getZoneID(), intScene.getGroupID()));
                this.internalSceneMap.put(intScene.getID(), intScene);

            }
        } else {
            InternalScene oldScene = this.internalSceneMap.get(intScene.getID());
            if (oldScene != null) {
                String oldSceneName = oldScene.getSceneName();
                String newSceneName = intScene.getSceneName();
                if ((oldSceneName.contains("Zone:") && oldSceneName.contains("Group:")
                        && oldSceneName.contains("Scene:"))
                        && !(newSceneName.contains("Zone:") && newSceneName.contains("Group:")
                                && newSceneName.contains("Scene:"))) {
                    oldScene.setSceneName(newSceneName);
                    this.discovery.sceneDiscoverd(oldScene);
                }
            }
        }
    }

    @Override
    public void removeInternalScene(String sceneID) {
        this.internalSceneMap.remove(sceneID);
    }

    @Override
    public InternalScene getInternalScene(String sceneID) {
        return this.internalSceneMap.get(sceneID);
    }

    private InternalScene createNewScene(String sceneID) {
        String[] sceneData = sceneID.split("-");
        if (sceneData.length == 3) {
            int zoneID = Integer.parseInt(sceneData[0]);
            short groupID = Short.parseShort(sceneData[1]);
            short sceneNumber = Short.parseShort(sceneData[2]);
            String sceneName = null;
            sceneName = connectionManager.getDigitalSTROMAPI().getSceneName(connectionManager.getSessionToken(), zoneID,
                    null, groupID, sceneNumber);
            InternalScene intScene = null;
            if (SceneEnum.getScene(sceneNumber) != null && structureManager.checkZoneGroupID(zoneID, groupID)) {
                if (sceneName == null) {
                    if (structureManager.getZoneName(zoneID) != null) {
                        sceneName = "Zone: " + structureManager.getZoneName(zoneID);
                        if (structureManager.getZoneGroupName(zoneID, groupID) != null) {
                            sceneName = sceneName + " Group: " + structureManager.getZoneGroupName(zoneID, groupID);
                        } else {
                            sceneName = sceneName + " Group: " + groupID;
                        }
                    } else {
                        if (structureManager.getZoneGroupName(zoneID, groupID) != null) {
                            sceneName = "Zone: " + zoneID + " Group: "
                                    + structureManager.getZoneGroupName(zoneID, groupID);
                        } else {
                            sceneName = "Zone: " + zoneID + " Group: " + groupID;
                        }
                    }
                    sceneName = sceneName + " Scene: "
                            + SceneEnum.getScene(sceneNumber).toString().toLowerCase().replace("_", " ");
                }
                intScene = new InternalScene(zoneID, groupID, sceneNumber, sceneName);
            }
            return intScene;
        }
        return null;
    }

    @Override
    public void callDeviceScene(String dSID, Short sceneID) {
        Device device = this.structureManager.getDeviceByDSID(new DSID(dSID));
        if (device != null) {
            device.internalCallScene(sceneID);
        } else {
            device = this.structureManager.getDeviceByDSUID(dSID);
            if (device != null) {
                device.internalCallScene(sceneID);
            }
        }
    }

    @Override
    public void callDeviceScene(Device device, Short sceneID) {
        if (device != null) {
            callDeviceScene(device.getDSID().toString(), sceneID);
        }
    }

    @Override
    public void undoInternalScene(InternalScene scene) {
        if (scene != null) {
            undoInternalScene(scene.getID());
        }
    }

    @Override
    public void undoInternalScene(String sceneID) {
        InternalScene intScene = this.internalSceneMap.get(sceneID);
        if (intScene != null) {
            logger.debug("deactivating existing scene {}", intScene.getSceneName());
            intScene.deactivateScene();
        } else {
            intScene = createNewScene(sceneID);
            if (intScene != null) {
                logger.debug("created new scene, deactivating it: {}", intScene.getSceneName());
                intScene.deactivateScene();
            }
        }
    }

    @Override
    public void undoDeviceScene(String dSID) {
        Device device = this.structureManager.getDeviceByDSID(new DSID(dSID));
        if (device != null) {
            device.internalUndoScene();
        } else {
            device = this.structureManager.getDeviceByDSUID(dSID);
            if (device != null) {
                device.internalUndoScene();
            }
        }
    }

    @Override
    public void undoDeviceScene(Device device) {
        if (device != null) {
            undoDeviceScene(device.getDSID().toString());
        }
    }

    @Override
    public void registerSceneListener(SceneStatusListener sceneListener) {
        if (sceneListener != null) {
            String id = sceneListener.getSceneStatusListenerID();
            if (id.equals(SceneStatusListener.SCENE_DISCOVERY)) {
                discovery.registerSceneDiscovery(sceneListener);
                logger.debug("Scene-Discovery registrated");
                for (InternalScene scene : internalSceneMap.values()) {
                    discovery.sceneDiscoverd(scene);
                }
            } else {
                InternalScene intScene = internalSceneMap.get(sceneListener.getSceneStatusListenerID());
                if (intScene != null) {
                    intScene.registerSceneListener(sceneListener);
                } else {
                    addInternalScene(createNewScene(id));
                    registerSceneListener(sceneListener);
                }
                logger.debug("SceneStatusListener with id {} is registrated", sceneListener.getSceneStatusListenerID());
            }
        }
    }

    @Override
    public void unregisterSceneListener(SceneStatusListener sceneListener) {
        if (sceneListener != null) {
            String id = sceneListener.getSceneStatusListenerID();
            if (id.equals(SceneStatusListener.SCENE_DISCOVERY)) {
                this.discovery.unRegisterDiscovery();
                logger.debug("Scene-Discovery unregistrated");
            } else {
                InternalScene intScene = this.internalSceneMap.get(sceneListener.getSceneStatusListenerID());
                if (intScene != null) {
                    intScene.unregisterSceneListener();
                }
                logger.debug("SceneStatusListener with id {} is unregistrated",
                        sceneListener.getSceneStatusListenerID());
            }
        }
    }

    @Override
    public synchronized boolean scenesGenerated() {
        return scenesGenerated;
    }

    @Override
    public void generateScenes() {
        stateChanged(ManagerStates.GENERATING_SCENES);
        logger.debug("start generating scenes");
        discovery.generateAllScenes(connectionManager, structureManager);
    }

    @Override
    public void scenesGenerated(char[] scenesGenerated) {
        if ("1111".equals(String.valueOf(scenesGenerated))) {
            this.scenesGenerated = true;
            stateChanged(ManagerStates.RUNNING);
        }
        if (String.valueOf(scenesGenerated).contains("2")) {
            String type = "nan";
            switch (String.valueOf(scenesGenerated).indexOf("2")) {
                case 0:
                    type = "namedScens";
                    break;
                case 1:
                    type = "appScenes";
                    break;
                case 2:
                    type = "zoneScenes";
                    break;
                case 3:
                    type = "reachableScenes";
                    break;
            }
            logger.debug("Not all scenes are generated, try it again. Scene type {} is not generated.", type);
            stateChanged(ManagerStates.RUNNING);
        }
    }

    @Override
    public boolean isDiscoveryRegistrated() {
        return this.discovery != null;
    }

    private void stateChanged(ManagerStates state) {
        this.state = state;
        if (statusListener != null) {
            statusListener.onStatusChanged(ManagerTypes.SCENE_MANAGER, state);
        }
    }

    @Override
    public ManagerTypes getManagerType() {
        return ManagerTypes.SCENE_MANAGER;
    }

    @Override
    public synchronized ManagerStates getManagerState() {
        return state;
    }

    @Override
    public List<InternalScene> getScenes() {
        return this.internalSceneMap != null ? new LinkedList<>(this.internalSceneMap.values()) : null;
    }

    @Override
    public void registerStatusListener(ManagerStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    @Override
    public void unregisterStatusListener() {
        this.statusListener = null;
    }

    @Override
    public String getUID() {
        return this.getClass().getSimpleName() + "-" + SUPPORTED_EVENTS.toString();
    }

    @Override
    public List<String> getSupportedEvents() {
        return SUPPORTED_EVENTS;
    }

    @Override
    public boolean supportsEvent(String eventName) {
        return SUPPORTED_EVENTS.contains(eventName);
    }

    @Override
    public void setEventListener(EventListener eventListener) {
        if (this.eventListener != null) {
            this.eventListener.removeEventHandler(this);
        }
        this.eventListener = eventListener;
    }

    @Override
    public void unsetEventListener(EventListener eventListener) {
        if (this.eventListener != null) {
            this.eventListener.removeEventHandler(this);
        }
        this.eventListener = null;
    }
}
