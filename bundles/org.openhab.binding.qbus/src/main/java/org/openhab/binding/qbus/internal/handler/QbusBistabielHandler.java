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
package org.openhab.binding.qbus.internal.handler;

import static org.openhab.binding.qbus.internal.QbusBindingConstants.CHANNEL_SWITCH;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusBistabiel;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusBistabielHandler} is responsible for handling the Bistable outputs of Qbus
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusBistabielHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusBistabielHandler.class);

    protected @Nullable QbusThingsConfig bistabielConfig = new QbusThingsConfig();

    private @Nullable Integer bistabielId;

    private @Nullable String sn;

    public QbusBistabielHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        readConfig();

        this.bistabielId = getId();

        setSN();

        scheduler.submit(() -> {
            QbusCommunication controllerComm;

            if (this.bistabielId != null) {
                controllerComm = getCommunication("Bistabiel", this.bistabielId);
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for BISTABIEL no set! " + this.bistabielId);
                return;
            }

            if (controllerComm == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for BISTABIEL not known in controller " + this.bistabielId);
                return;
            }

            Map<Integer, QbusBistabiel> bistabielCommLocal = controllerComm.getBistabiel();

            QbusBistabiel outputLocal = bistabielCommLocal.get(this.bistabielId);

            if (outputLocal == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge could not initialize BISTABIEL ID " + this.bistabielId);
                return;
            }

            outputLocal.setThingHandler(this);
            handleStateUpdate(outputLocal);

            QbusBridgeHandler qBridgeHandler = getBridgeHandler("Bistabiel", this.bistabielId);

            if (qBridgeHandler != null) {
                if (qBridgeHandler.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Bridge offline for BISTABIEL ID " + this.bistabielId);
                }
            }
        });
    }

    /**
     * Handle the status update from the bistabiel
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Bistabiel", this.bistabielId);

        if (qComm == null) {
            thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                    "ID for BISTABIEL not known in controller " + this.bistabielId);
            return;
        } else {
            Map<Integer, QbusBistabiel> bistabielComm = qComm.getBistabiel();

            QbusBistabiel qBistabiel = bistabielComm.get(this.bistabielId);

            if (qBistabiel == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for BISTABIEL not known in controller " + this.bistabielId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Bistabiel", this.bistabielId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qBistabiel);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_SWITCH:
                                try {
                                    handleSwitchCommand(qBistabiel, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Switch for bistabiel ID {}. IOException: {}",
                                            this.bistabielId, message);
                                } catch (InterruptedException e) {
                                    String message = e.getMessage();
                                    logger.warn(
                                            "Error on executing Switch for bistabiel ID {}. Interruptedexception {}",
                                            this.bistabielId, message);
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
     * Executes the switch command
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleSwitchCommand(QbusBistabiel qBistabiel, Command command)
            throws InterruptedException, IOException {
        String snr = getSN();
        if (snr != null) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.OFF) {
                    qBistabiel.execute(0, snr);
                } else {
                    qBistabiel.execute(100, snr);
                }
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "No serial number configured for BISTABIEL " + this.bistabielId);
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Bistabiel.
     *
     * @param qBistabiel
     */
    public void handleStateUpdate(QbusBistabiel qBistabiel) {
        Integer bistabielState = qBistabiel.getState();
        if (bistabielState != null) {
            updateState(CHANNEL_SWITCH, (bistabielState == 0) ? OnOffType.OFF : OnOffType.ON);
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
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Bistabiel", this.bistabielId);
        if (qBridgeHandler == null) {
            thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication with Qbus Bridge for BISTABIEL " + this.bistabielId);
            return;
        }
        sn = qBridgeHandler.getSn();
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        bistabielConfig = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return outputId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig localConfig = bistabielConfig;
        if (localConfig != null) {
            return localConfig.bistabielId;
        } else {
            return null;
        }
    }
}
