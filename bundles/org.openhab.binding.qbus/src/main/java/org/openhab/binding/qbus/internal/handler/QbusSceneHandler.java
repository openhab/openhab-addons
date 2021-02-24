/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.CHANNEL_SCENE;

import java.util.Map;

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

/**
 * The {@link QbusSceneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusSceneHandler extends QbusGlobalHandler {

    protected @Nullable QbusThingsConfig config;

    private int sceneId;

    private @Nullable String sn;

    public QbusSceneHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        setConfig();
        sceneId = getId();

        QbusCommunication qComm = getCommunication("Scene", sceneId);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Scene", sceneId);
        if (qBridgeHandler == null) {
            return;
        }

        setSN();

        Map<Integer, QbusScene> sceneComm = qComm.getScene();

        if (sceneComm != null) {
            QbusScene qScene = sceneComm.get(sceneId);
            if (qScene != null) {
                qScene.setThingHandler(this);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Error while initializing the thing.");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Error while initializing the thing.");
        }
    }

    /**
     * Returns the serial number of the controller
     *
     * @return the serial nr
     */
    public @Nullable String getSN() {
        return this.sn;
    }

    /**
     * Sets the serial number of the controller
     */
    public void setSN() {
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Scene", sceneId);
        if (qBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }
        this.sn = qBridgeHandler.getSn();
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Scene", sceneId);

        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for Scene " + sceneId);
            return;
        }

        Map<Integer, QbusScene> sceneComm = qComm.getScene();

        if (sceneComm != null) {
            QbusScene qScene = sceneComm.get(sceneId);
            if (qScene == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for Scene " + sceneId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Scene", sceneId);
                    }

                    if (qComm.communicationActive()) {
                        switch (channelUID.getId()) {
                            case CHANNEL_SCENE:
                                handleSwitchCommand(qScene, channelUID, command);
                                break;
                        }
                    }
                });
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Scene.
     */
    public void handleStateUpdate(QbusScene qScene) {
        Integer sceneState = qScene.getState();
        if (sceneState != null) {
            updateState(CHANNEL_SCENE, (sceneState == 0) ? OnOffType.OFF : OnOffType.ON);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     *
     * @param message
     */
    public void thingOffline(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
    }

    /**
     * Executes the scene command
     */
    private void handleSwitchCommand(QbusScene qScene, ChannelUID channelUID, Command command) {
        String snr = getSN();
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                if (snr != null) {
                    qScene.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + sceneId);
                }
            } else {
                if (snr != null) {
                    qScene.execute(100, snr);
                } else {
                    thingOffline("No serial number configured for  " + sceneId);
                }
            }
        }
    }

    /**
     * Read the configuration
     */
    protected synchronized void setConfig() {
        this.config = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return sceneId
     */
    public int getId() {
        if (this.config != null) {
            return this.config.sceneId;
        } else {
            return 0;
        }
    }
}
