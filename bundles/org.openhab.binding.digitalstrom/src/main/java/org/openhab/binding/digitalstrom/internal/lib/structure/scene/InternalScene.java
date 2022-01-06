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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.SceneTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InternalScene} represents a digitalSTROM-Scene for the internal model.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class InternalScene {
    private final Logger logger = LoggerFactory.getLogger(InternalScene.class);

    private final Short sceneID;
    private final Short groupID;
    private final Integer zoneID;
    private String sceneName;
    private final String internalSceneID;
    private boolean active = false;
    private boolean deviceHasChanged = false;
    private String sceneType = SceneTypes.GROUP_SCENE;

    private List<Device> devices = Collections.synchronizedList(new LinkedList<>());
    private SceneStatusListener listener;

    /**
     * Creates a new {@link InternalScene} with the given parameters. Only the <i>sceneID</i> must not be null. If the
     * <i>sceneName</i> is null, the internal scene id will be set as name in format "[zoneID]-[groupID]-[sceneID]". If
     * the
     * <i>zoneID</i> and/or the <i> groupID</i> is null, the broadcast address 0 will be set.
     *
     * @param zoneID can be null
     * @param groupID can be null
     * @param sceneID must not be null
     * @param sceneName can be null
     */
    public InternalScene(Integer zoneID, Short groupID, Short sceneID, String sceneName) {
        if (sceneID == null) {
            throw new IllegalArgumentException("The parameter sceneID can't be null!");
        }
        this.sceneID = sceneID;
        if (groupID == null) {
            this.groupID = 0;
        } else {
            this.groupID = groupID;
        }
        if (zoneID == null) {
            this.zoneID = 0;
        } else {
            this.zoneID = zoneID;
        }
        this.internalSceneID = this.zoneID + "-" + this.groupID + "-" + this.sceneID;
        if (sceneName == null || sceneName.isBlank()) {
            this.sceneName = this.internalSceneID;
        } else {
            this.sceneName = sceneName;
        }
        setSceneType();
    }

    private void setSceneType() {
        if ((sceneName != null) && !sceneName.contains("Apartment-Scene: ") && !sceneName.contains("Zone-Scene: Zone:")
                && !(sceneName.contains("Zone: ") && sceneName.contains("Group: ") && sceneName.contains("Scene: "))) {
            sceneType = SceneTypes.NAMED_SCENE;
        } else if (this.zoneID == 0) {
            sceneType = SceneTypes.APARTMENT_SCENE;
        } else if (this.groupID == 0) {
            sceneType = SceneTypes.ZONE_SCENE;
        }
    }

    /**
     * Activates this Scene.
     */
    public void activateScene() {
        logger.debug("activate scene: {}", this.getSceneName());
        this.active = true;
        deviceHasChanged = false;
        informListener();
        if (this.devices != null) {
            for (Device device : this.devices) {
                device.callInternalScene(this);
            }
        }
    }

    /**
     * Deactivates this Scene.
     */
    public void deactivateScene() {
        logger.debug("deactivate scene: {}", this.getSceneName());
        if (active) {
            this.active = false;
            deviceHasChanged = false;
            informListener();
            if (this.devices != null) {
                for (Device device : this.devices) {
                    device.undoInternalScene(this);
                }
            }
        }
    }

    /**
     * Will be called by a device, if an undo call of an other scene activated this scene.
     */
    public void activateSceneByDevice() {
        logger.debug("activate scene by device: {}", this.getSceneName());
        if (!active && !deviceHasChanged) {
            this.active = true;
            deviceHasChanged = false;
            informListener();
        }
    }

    /**
     * Will be called by a device, if an call of an other scene deactivated this scene.
     */
    public void deactivateSceneByDevice() {
        logger.debug("deactivate scene by device: {}", this.getSceneName());
        if (active) {
            this.active = false;
            deviceHasChanged = false;
            informListener();
        }
    }

    /**
     * This method has a device to call, if this scene was activated and the device state has changed.
     *
     * @param sceneNumber new scene number
     */
    public void deviceSceneChanged(short sceneNumber) {
        if (this.sceneID != sceneNumber) {
            if (active) {
                deviceHasChanged = true;
                active = false;
                informListener();
            }
        }
    }

    private void informListener() {
        logger.debug("inform listener: {}", this.getSceneName());
        if (this.listener != null) {
            listener.onSceneStateChanged(this.active);
        } else {
            logger.debug("no listener found for scene: {}", this.getSceneName());
        }
    }

    /**
     * Returns true, if this scene is active, otherwise false.
     *
     * @return Scene is active? (true = yes | false = no)
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Adds an affected {@link Device} to this {@link InternalScene} device list.
     *
     * @param device to add
     */
    public void addDevice(Device device) {
        if (!this.devices.contains(device)) {
            this.devices.add(device);
        }
        short prio = 0;
        if (this.listener != null) {
            prio = 1;
        } else {
            prio = 2;
        }
        device.checkSceneConfig(sceneID, prio);
    }

    /**
     * Overrides the existing device list of this {@link InternalScene} with a new reference to a {@link List} of
     * affected {@link Device}'s.
     *
     * @param deviceList to add
     */
    public void addReferenceDevices(List<Device> deviceList) {
        this.devices = deviceList;
        checkDeviceSceneConfig();
    }

    /**
     * Proves, if the scene configuration is saved to all {@link Device}'s. If not, the device initials the reading out
     * of the missing configuration in the following priority steps:
     * <ul>
     * <li>low priority, if no listener is added.</li>
     * <li>medium priority, if a listener is added.</li>
     * <li>high priority, if this scene has been activated.</li>
     * </ul>
     */
    public void checkDeviceSceneConfig() {
        short prio = 0;
        if (this.listener != null) {
            prio = 1;
        } else {
            prio = 2;
        }
        if (devices != null) {
            for (Device device : devices) {
                device.checkSceneConfig(sceneID, prio);
            }
        }
    }

    /**
     * Returns the list of the affected {@link Device}'s.
     *
     * @return device list
     */
    public List<Device> getDeviceList() {
        return this.devices;
    }

    /**
     * Adds a {@link List} of affected {@link Device}'s.
     *
     * @param deviceList to add
     */
    public void addDevices(List<Device> deviceList) {
        for (Device device : deviceList) {
            addDevice(device);
        }
    }

    /**
     * Removes a not anymore affected {@link Device} from the device list.
     *
     * @param device to remove
     */
    public void removeDevice(Device device) {
        this.devices.remove(device);
    }

    /**
     * Updates the affected {@link Device}'s with the given deviceList.
     *
     * @param deviceList to update
     */
    public void updateDeviceList(List<Device> deviceList) {
        if (!this.devices.equals(deviceList)) {
            this.devices.clear();
            addDevices(deviceList);
        }
    }

    /**
     * Returns the Scene name.
     *
     * @return scene name
     */
    public String getSceneName() {
        return sceneName;
    }

    /**
     * Sets the scene name.
     *
     * @param sceneName to set
     */
    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
        setSceneType();
    }

    /**
     * Returns the Scene id of this scene call.
     *
     * @return scene id
     */
    public Short getSceneID() {
        return sceneID;
    }

    /**
     * Returns the group id of this scene call.
     *
     * @return group id
     */
    public Short getGroupID() {
        return groupID;
    }

    /**
     * Returns the zone id of this scene call.
     *
     * @return zone id
     */
    public Integer getZoneID() {
        return zoneID;
    }

    /**
     * Returns the id of this scene call.
     *
     * @return scene call id
     */
    public String getID() {
        return internalSceneID;
    }

    /**
     * Registers a {@link SceneStatusListener} to this {@link InternalScene}.
     *
     * @param listener to register
     */
    public synchronized void registerSceneListener(SceneStatusListener listener) {
        this.listener = listener;
        this.listener.onSceneAdded(this);
        checkDeviceSceneConfig();
    }

    /**
     * Unregisters the {@link SceneStatusListener} from this {@link InternalScene}.
     */
    public synchronized void unregisterSceneListener() {
        if (listener != null) {
            this.listener = null;
        }
    }

    /**
     * Returns the scene type.
     * <br>
     * <b>Note:</b>
     * The valid Scene types can be found at {@link SceneTypes}.
     *
     * @return sceneType
     */
    public String getSceneType() {
        return this.sceneType;
    }

    @Override
    public String toString() {
        return "NamedScene [SceneName=" + sceneName + ", NAMED_SCENE_ID=" + internalSceneID + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((internalSceneID == null) ? 0 : internalSceneID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof InternalScene)) {
            return false;
        }
        InternalScene other = (InternalScene) obj;
        if (internalSceneID == null) {
            if (other.getID() != null) {
                return false;
            }
        } else if (!internalSceneID.equals(other.getID())) {
            return false;
        }
        return true;
    }
}
