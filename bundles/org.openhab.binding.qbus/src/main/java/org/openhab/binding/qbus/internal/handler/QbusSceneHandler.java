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
package org.openhab.binding.qbus.internal.handler;

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusScene;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusSceneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */
@NonNullByDefault
public class QbusSceneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusSceneHandler.class);

    // private volatile int prevSceneState;

    public QbusSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer SceneId = ((Number) this.getConfig().get(CONFIG_SCENE_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute Scene " + SceneId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute Scene " + SceneId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Qbus: bridge communication not initialized when trying to execute scene " + SceneId);
            return;
        }

        QbusScene QScene = QComm.getScenes().get(SceneId);

        if (QComm.communicationActive()) {
            handleCommandSelection(QScene, channelUID, command);
        } else {
            // We lost connection but the connection object is there, so was correctly started.
            // Try to restart communication.
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                QComm.restartCommunication();
                // If still not active, take thing offline and return.
                if (!QComm.communicationActive()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Qbus: communication socket error");
                    return;
                }
                // Also put the bridge back online
                QBridgeHandler.bridgeOnline();

                // And finally handle the command
                handleCommandSelection(QScene, channelUID, command);
            });
        }
    }

    private void handleCommandSelection(QbusScene QScene, ChannelUID channelUID, Command command) {
        logger.debug("Qbus: handle command {} for {}", command, channelUID);

        if (command == REFRESH) {
            handleStateUpdate(QScene);
            return;
        }

        handleSwitchCommand(QScene, command);
        updateStatus(ThingStatus.ONLINE);
    }

    private void handleSwitchCommand(QbusScene QScene, Command command) {
        @SuppressWarnings("null")
        String sn = getBridge().getConfiguration().get(CONFIG_SN).toString();
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                QScene.execute(0, sn);
            } else {
                QScene.execute(100, sn);
            }
        }
    }

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer SceneId = ((Number) config.get(CONFIG_SCENE_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for scene " + SceneId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for scene " + SceneId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();
        if (QComm == null || !QComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Qbus: no connection with Qbus server, could not initialize scene " + SceneId);
            return;
        }

        QbusScene QScene = QComm.getScenes().get(SceneId);

        // int sceneState = QScene.getState();
        /*
         * this.prevSceneState = sceneState;
         * QScene.setThingHandler(this);
         */
        Map<String, String> properties = new HashMap<>();

        thing.setProperties(properties);

        handleStateUpdate(QScene);

        logger.debug("Qbus: scene intialized {}", SceneId);
    }

    /**
     * Method to update state of channel, called from Qbus Scene.
     */
    public void handleStateUpdate(QbusScene QScene) {

        int sceneState = QScene.getState();

        updateState(CHANNEL_SWITCH, (sceneState == 0) ? OnOffType.OFF : OnOffType.ON);
        updateStatus(ThingStatus.ONLINE);

        // this.prevSceneState = sceneState;
    }
}
