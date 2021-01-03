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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.CHANNEL_SWITCH;
import static org.openhab.core.types.RefreshType.REFRESH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusScene;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
public class QbusSceneHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusSceneHandler.class);

    public QbusSceneHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable QbusThingsConfig config;

    int sceneId = 0;

    String sn = "";

    /**
     * Main initialization
     */
    @Override
    public void initialize() {

        setConfig();
        sceneId = getId();

        QbusCommunication QComm = getCommunication("Scene", sceneId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Scene", sceneId);
        if (QBridgeHandler == null) {
            return;
        }

        QbusScene QScene = QComm.getScenes().get(sceneId);

        sn = QBridgeHandler.getSn();

        if (QScene != null) {
            QScene.setThingHandler(this);
            handleStateUpdate(QScene);
            logger.info("Scene intialized {}", sceneId);
        } else {
            logger.warn("Scene not intialized {}", sceneId);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication QComm = getCommunication("Scene", sceneId);

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for Scene " + sceneId);
            return;
        }

        QbusScene QScene = QComm.getScenes().get(sceneId);

        if (QScene == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for Scene " + sceneId);
            return;
        }

        scheduler.submit(() -> {
            if (!QComm.communicationActive()) {
                restartCommunication(QComm, "Scene", sceneId);
            }

            if (QComm.communicationActive()) {

                if (command == REFRESH) {
                    handleStateUpdate(QScene);
                    return;
                }

                switch (channelUID.getId()) {
                    case CHANNEL_SWITCH:
                        handleSwitchCommand(QScene, command);
                        updateStatus(ThingStatus.ONLINE);
                        break;

                    default:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Channel unknown " + channelUID.getId());
                }
            }
        });
    }

    /**
     * Executes the scene command
     */
    private void handleSwitchCommand(QbusScene QScene, Command command) {

        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                QScene.execute(0, sn);
            } else {
                QScene.execute(100, sn);
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Scene.
     */
    public void handleStateUpdate(QbusScene QScene) {

        int sceneState = QScene.getState();

        updateState(CHANNEL_SWITCH, (sceneState == 0) ? OnOffType.OFF : OnOffType.ON);
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Read the configuration
     */
    protected synchronized void setConfig() {
        config = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return sceneId
     */
    @SuppressWarnings("null")
    public int getId() {
        return config.sceneId;
    }
}
