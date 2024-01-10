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
package org.openhab.binding.qbus.internal.handler;

import static org.openhab.binding.qbus.internal.QbusBindingConstants.CHANNEL_SCENE;

import java.io.IOException;
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

    protected @Nullable QbusThingsConfig sceneConfig = new QbusThingsConfig();

    private @Nullable Integer sceneId;

    private @Nullable String sn;

    public QbusSceneHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        readConfig();

        this.sceneId = getId();

        setSN();

        scheduler.submit(() -> {
            QbusCommunication controllerComm;

            if (this.sceneId != null) {
                controllerComm = getCommunication("Scene", this.sceneId);
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for SCENE no set! " + this.sceneId);
                return;
            }

            if (controllerComm == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for SCENE not known in controller " + this.sceneId);
                return;
            }

            Map<Integer, QbusScene> sceneCommLocal = controllerComm.getScene();

            QbusScene outputLocal = sceneCommLocal.get(this.sceneId);

            if (outputLocal == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge could not initialize SCENE ID " + this.sceneId);
                return;
            }

            outputLocal.setThingHandler(this);

            QbusBridgeHandler qBridgeHandler = getBridgeHandler("Scene", this.sceneId);

            if ((qBridgeHandler != null) && (qBridgeHandler.getStatus() == ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                thingOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Bridge offline for SCENE ID " + this.sceneId);
            }
        });
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Scene", this.sceneId);

        if (qComm == null) {
            thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for SCENE not known in controller " + this.sceneId);
            return;
        } else {
            Map<Integer, QbusScene> sceneComm = qComm.getScene();
            QbusScene qScene = sceneComm.get(this.sceneId);

            if (qScene == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for SCENE not known in controller " + this.sceneId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Scene", this.sceneId);
                    }

                    if (qComm.communicationActive()) {
                        switch (channelUID.getId()) {
                            case CHANNEL_SCENE:
                                try {
                                    handleSwitchCommand(qScene, channelUID, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Scene for scene ID {}. IOException: {}",
                                            this.sceneId, message);
                                } catch (InterruptedException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Scene for scene ID {}. Interruptedexception {}",
                                            this.sceneId, message);
                                }
                                break;

                            default:
                                thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Unknown Channel " + channelUID.getId());
                        }
                    }
                });
            }
        }
    }

    /**
     * Executes the scene command
     *
     * @throws IOException
     * @throws InterruptedException
     */
    void handleSwitchCommand(QbusScene qScene, ChannelUID channelUID, Command command)
            throws InterruptedException, IOException {
        String snr = getSN();
        if (snr != null) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.OFF) {
                    qScene.execute(0, snr);
                } else {
                    qScene.execute(100, snr);
                }
            }
        }
    }

    /**
     * Returns the serial number of the controller
     *
     * @return the serial nr
     */
    public @Nullable String getSN() {
        return sn;
    }

    /**
     * Sets the serial number of the controller
     */
    public void setSN() {
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Scene", this.sceneId);
        if (qBridgeHandler == null) {
            thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication with Qbus Bridge for SCENE " + this.sceneId);
            return;
        }
        sn = qBridgeHandler.getSn();
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        sceneConfig = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return outputId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig localConfig = sceneConfig;
        if (localConfig != null) {
            return localConfig.sceneId;
        } else {
            return null;
        }
    }
}
