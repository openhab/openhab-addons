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
package org.openhab.binding.insteon.internal.handler;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonSceneConfiguration;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link InsteonSceneHandler} represents an insteon scene handler.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonSceneHandler extends InsteonBaseThingHandler {
    private @Nullable InsteonScene scene;

    public InsteonSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public @Nullable InsteonModem getDevice() {
        return getModem();
    }

    public @Nullable InsteonScene getScene() {
        return scene;
    }

    @Override
    public void initialize() {
        InsteonSceneConfiguration config = getConfigAs(InsteonSceneConfiguration.class);

        scheduler.execute(() -> {
            if (getBridge() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
                return;
            }

            int group = config.getGroup();
            if (!InsteonScene.isValidGroup(group)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid scene group, it must be between " + InsteonScene.GROUP_MIN + " and "
                                + InsteonScene.GROUP_MAX + ".");
                return;
            }

            InsteonModem modem = getModem();
            if (modem != null && modem.hasScene(group)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Duplicate scene.");
                return;
            }

            InsteonScene scene = createScene(group, modem);
            this.scene = scene;

            if (modem != null) {
                modem.addScene(scene);
            }

            updateChannels(scene);
            refresh();
        });
    }

    private InsteonScene createScene(int group, @Nullable InsteonModem modem) {
        InsteonScene scene = InsteonScene.makeScene(group, modem);
        scene.setHandler(this);
        scene.initialize();
        return scene;
    }

    private void updateChannels(InsteonScene scene) {
        getThing().getChannels().stream().map(Channel::getConfiguration)
                .forEach(config -> config.put(PARAMETER_GROUP, scene.getGroup()));
    }

    @Override
    public void dispose() {
        InsteonScene scene = getScene();
        if (scene != null) {
            scene.deleteEntries();

            InsteonModem modem = getModem();
            if (modem != null) {
                modem.removeScene(scene);
            }
        }
        this.scene = null;

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        InsteonScene scene = getScene();
        if (scene != null) {
            scene.resetState();
        }

        super.handleCommand(channelUID, command);
    }

    @Override
    public void bridgeThingDisposed() {
        InsteonScene scene = getScene();
        if (scene != null) {
            scene.setModem(null);
        }
    }

    @Override
    public void bridgeThingUpdated(InsteonBridgeConfiguration config, InsteonModem modem) {
        InsteonScene scene = getScene();
        if (scene != null) {
            scene.setModem(modem);

            modem.addScene(scene);
        }
    }

    @Override
    protected String getConfigInfo() {
        return getConfigAs(InsteonSceneConfiguration.class).toString();
    }

    @Override
    public void updateStatus() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
            return;
        }

        if (bridge.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Waiting for modem database.");
            return;
        }

        InsteonScene scene = getScene();
        if (scene == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to determine scene.");
            return;
        }

        if (!scene.hasModemDBEntry()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Scene not found in modem database.");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void updateState(State state) {
        getThing().getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> FEATURE_SCENE_ON_OFF.equals(channelUID.getId())).findFirst()
                .ifPresent(channelUID -> updateState(channelUID, state));
    }
}
