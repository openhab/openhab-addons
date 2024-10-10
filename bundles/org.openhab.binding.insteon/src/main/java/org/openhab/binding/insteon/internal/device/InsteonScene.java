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
package org.openhab.binding.insteon.internal.device;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.database.LinkDBRecord;
import org.openhab.binding.insteon.internal.device.feature.FeatureListener;
import org.openhab.binding.insteon.internal.handler.InsteonSceneHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonScene} represents an Insteon scene
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonScene implements Scene {
    public static final int GROUP_MIN = 2;
    public static final int GROUP_MAX = 254;
    // limit new scene group minimum to 25 matching the current Insteon app behavior
    public static final int GROUP_NEW_MIN = 25;
    public static final int GROUP_NEW_MAX = 254;

    private final Logger logger = LoggerFactory.getLogger(InsteonScene.class);

    private int group;
    private @Nullable InsteonModem modem;
    private @Nullable InsteonSceneHandler handler;
    private List<SceneEntry> entries = new ArrayList<>();
    private boolean modemDBEntry = false;

    public InsteonScene(int group) {
        this.group = group;
    }

    @Override
    public int getGroup() {
        return group;
    }

    public @Nullable InsteonModem getModem() {
        return modem;
    }

    public @Nullable InsteonSceneHandler getHandler() {
        return handler;
    }

    public List<SceneEntry> getEntries() {
        synchronized (entries) {
            return entries.stream().toList();
        }
    }

    public List<SceneEntry> getEntries(InsteonAddress address) {
        return getEntries().stream().filter(entry -> entry.getAddress().equals(address)).toList();
    }

    public List<InsteonAddress> getDevices() {
        return getEntries().stream().map(SceneEntry::getAddress).distinct().toList();
    }

    public List<DeviceFeature> getFeatures() {
        return getEntries().stream().map(SceneEntry::getFeature).toList();
    }

    public List<DeviceFeature> getFeatures(InsteonAddress address) {
        return getEntries(address).stream().map(SceneEntry::getFeature).toList();
    }

    public State getState() {
        return getEntries().stream().noneMatch(SceneEntry::isStateDefined) ? UnDefType.NULL
                : OnOffType
                        .from(getEntries().stream().filter(SceneEntry::isStateDefined).allMatch(SceneEntry::isStateOn));
    }

    public boolean hasEntry(InsteonAddress address) {
        return getEntries().stream().anyMatch(entry -> entry.getAddress().equals(address));
    }

    public boolean hasEntry(InsteonAddress address, String featureName) {
        return getEntries().stream().anyMatch(
                entry -> entry.getAddress().equals(address) && entry.getFeature().getName().equals(featureName));
    }

    public boolean hasModemDBEntry() {
        return modemDBEntry;
    }

    public boolean isComplete() {
        InsteonModem modem = getModem();
        return modem != null && modem.getDB().getRelatedDevices(group).stream().allMatch(this::hasEntry);
    }

    public void setModem(@Nullable InsteonModem modem) {
        this.modem = modem;
    }

    public void setHandler(InsteonSceneHandler handler) {
        this.handler = handler;
    }

    public void setHasModemDBEntry(boolean modemDBEntry) {
        this.modemDBEntry = modemDBEntry;
    }

    @Override
    public String toString() {
        return "group:" + group + "|entries:" + entries.size();
    }

    /**
     * Adds an entry to this scene
     *
     * @param entry the scene entry to add
     */
    private void addEntry(SceneEntry entry) {
        logger.trace("adding entry to scene {}: {}", group, entry);

        synchronized (entries) {
            if (entries.add(entry)) {
                entry.register();
            }
        }
    }

    /**
     * Deletes an entry from this scene
     *
     * @param entry the scene entry to delete
     */
    private void deleteEntry(SceneEntry entry) {
        synchronized (entries) {
            if (entries.remove(entry)) {
                entry.unregister();
            }
        }
    }

    /**
     * Deletes all entries from this scene
     */
    public void deleteEntries() {
        getEntries().forEach(this::deleteEntry);
    }

    /**
     * Deletes entries for a given device from this scene
     *
     * @param address the device address
     */
    public void deleteEntries(InsteonAddress address) {
        logger.trace("removing entries from scene {} for device {}", group, address);

        getEntries(address).forEach(this::deleteEntry);
    }

    /**
     * Updates all entries for this scene
     */
    public void updateEntries() {
        synchronized (entries) {
            entries.clear();
        }

        InsteonModem modem = getModem();
        if (modem != null) {
            for (InsteonAddress address : modem.getDB().getRelatedDevices(group)) {
                InsteonDevice device = modem.getInsteonDevice(address);
                if (device == null) {
                    logger.debug("device {} part of scene {} not enabled or configured, ignoring.", address, group);
                } else {
                    updateEntries(device);
                }
            }
        }
    }

    /**
     * Updates entries related to a given device for this scene
     *
     * @param device the device
     */
    public void updateEntries(InsteonDevice device) {
        InsteonAddress address = device.getAddress();

        logger.trace("updating entries for scene {} device {}", group, address);

        getEntries(address).forEach(this::deleteEntry);

        InsteonModem modem = getModem();
        if (modem != null) {
            for (LinkDBRecord record : device.getLinkDB().getResponderRecords(modem.getAddress(), group)) {
                device.getResponderFeatures().stream()
                        .filter(feature -> feature.getComponentId() == record.getComponentId()).findFirst()
                        .ifPresent(feature -> addEntry(new SceneEntry(address, feature, record.getData())));
            }
        }
    }

    /**
     * Resets state for this scene
     */
    public void resetState() {
        logger.trace("resetting state for scene {}", group);

        getEntries().forEach(entry -> entry.setState(UnDefType.NULL));
    }

    /**
     * Updates state for this scene
     */
    private void updateState() {
        State state = getState();
        InsteonSceneHandler handler = getHandler();
        if (handler != null && state instanceof OnOffType) {
            handler.updateState(state);
        }
    }

    /**
     * Adds a device feature to this scene
     *
     * @param device the device
     * @param onLevel the feature on level
     * @param rampRate the feature ramp rate
     * @param componentId the feature component id
     */
    public void addDeviceFeature(InsteonDevice device, int onLevel, @Nullable RampRate rampRate, int componentId) {
        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete() || !device.getLinkDB().isComplete()) {
            return;
        }

        modem.getDB().clearChanges();
        modem.getDB().markRecordForAddOrModify(device.getAddress(), group, true);
        modem.getDB().update();

        device.getLinkDB().clearChanges();
        device.getLinkDB().markRecordForAddOrModify(modem.getAddress(), group, false, new byte[] { (byte) onLevel,
                (byte) (rampRate != null ? rampRate.getValue() : 0x00), (byte) componentId });
        device.getLinkDB().update();
    }

    /**
     * Removes a device feature from this scene
     *
     * @param device the device
     * @param componentId the feature component id
     */
    public void removeDeviceFeature(InsteonDevice device, int componentId) {
        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete() || !device.getLinkDB().isComplete()) {
            return;
        }

        modem.getDB().clearChanges();
        modem.getDB().markRecordForDelete(device.getAddress(), group);
        modem.getDB().update();

        device.getLinkDB().clearChanges();
        device.getLinkDB().markRecordForDelete(modem.getAddress(), group, false, componentId);
        device.getLinkDB().update();
    }

    /**
     * Initializes this scene
     */
    public void initialize() {
        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete()) {
            return;
        }

        if (!modem.getDB().hasBroadcastGroup(group)) {
            logger.warn("scene {} not found in the modem database.", group);
            setHasModemDBEntry(false);
            return;
        }

        if (!hasModemDBEntry()) {
            logger.debug("scene {} found in the modem database.", group);
            setHasModemDBEntry(true);
        }

        updateEntries();
    }

    /**
     * Refreshes this scene
     */
    @Override
    public void refresh() {
        logger.trace("refreshing scene {}", group);

        initialize();

        InsteonSceneHandler handler = getHandler();
        if (handler != null) {
            handler.refresh();
        }
    }

    /**
     * Class that represents a scene entry
     */
    public class SceneEntry implements FeatureListener {
        private InsteonAddress address;
        private DeviceFeature feature;
        private byte[] data;
        private State state = UnDefType.NULL;

        public SceneEntry(InsteonAddress address, DeviceFeature feature, byte[] data) {
            this.address = address;
            this.feature = feature;
            this.data = data;
        }

        public InsteonAddress getAddress() {
            return address;
        }

        public DeviceFeature getFeature() {
            return feature;
        }

        public State getOnState() {
            return OnLevel.getState(Byte.toUnsignedInt(data[0]), feature.getType());
        }

        public RampRate getRampRate() {
            return RampRate.valueOf(Byte.toUnsignedInt(data[1]));
        }

        public State getState() {
            return state;
        }

        public boolean isStateDefined() {
            return !UnDefType.NULL.equals(state);
        }

        public boolean isStateOn() {
            return getOnState().equals(state);
        }

        public void setState(State state) {
            this.state = state;
        }

        public void register() {
            feature.registerListener(this);

            stateUpdated(feature.getState());
        }

        public void unregister() {
            feature.unregisterListener(this);
        }

        @Override
        public String toString() {
            String s = address + " " + feature.getName() + " currentState: " + state + " onState: " + getOnState();
            if (RampRate.supportsFeatureType(feature.getType())) {
                s += " rampRate: " + getRampRate();
            }
            return s;
        }

        @Override
        public void stateUpdated(State state) {
            setState(state);
            updateState();
        }

        @Override
        public void eventTriggered(String event) {
            // do nothing
        }
    }

    /**
     * Returns if scene group is valid
     *
     * @param group the scene group
     * @return true if group is an integer within supported range
     */
    public static boolean isValidGroup(String group) {
        try {
            return isValidGroup(Integer.parseInt(group));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns if scene group is valid
     *
     * @param group the scene group
     * @return true if group within supported range
     */
    public static boolean isValidGroup(int group) {
        return group >= GROUP_MIN && group <= GROUP_MAX;
    }

    /**
     * Factory method for creating a InsteonScene from a scene group and modem
     *
     * @param group the scene group
     * @param modem the scene modem
     * @return the newly created InsteonScene
     */
    public static InsteonScene makeScene(int group, @Nullable InsteonModem modem) {
        InsteonScene scene = new InsteonScene(group);
        scene.setModem(modem);
        return scene;
    }
}
