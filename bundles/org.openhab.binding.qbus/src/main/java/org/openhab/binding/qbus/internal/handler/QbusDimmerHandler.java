/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusDimmer;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusDimmerHandler} is responsible for handling the dimmable outputs of Qbus
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusDimmerHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusDimmerHandler.class);

    protected @Nullable QbusThingsConfig dimmerConfig = new QbusThingsConfig();

    private @Nullable Integer dimmerId;

    private @Nullable String sn;

    public QbusDimmerHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        readConfig();

        this.dimmerId = getId();

        setSN();

        scheduler.submit(() -> {
            QbusCommunication controllerComm;

            if (this.dimmerId != null) {
                controllerComm = getCommunication("Dimmer", this.dimmerId);
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for DIMMER no set!  " + this.dimmerId);
                return;
            }

            if (controllerComm == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for DIMMER not known in controller " + this.dimmerId);
                return;
            }

            Map<Integer, QbusDimmer> dimmerCommLocal = controllerComm.getDimmer();

            QbusDimmer outputLocal = dimmerCommLocal.get(this.dimmerId);

            if (outputLocal == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge could not initialize DIMMER ID " + this.dimmerId);
                return;
            }

            outputLocal.setThingHandler(this);
            handleStateUpdate(outputLocal);

            QbusBridgeHandler qBridgeHandler = getBridgeHandler("Dimmer", this.dimmerId);

            if (qBridgeHandler != null) {
                if (qBridgeHandler.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Bridge offline for DIMMER ID " + this.dimmerId);
                }
            }
        });
    }

    /**
     * Handle the status update from the dimmer
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Dimmer", this.dimmerId);

        if (qComm == null) {
            thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                    "ID for DIMMER not known in controller " + this.dimmerId);
            return;
        } else {
            Map<Integer, QbusDimmer> dimmerComm = qComm.getDimmer();

            QbusDimmer qDimmer = dimmerComm.get(this.dimmerId);

            if (qDimmer == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for DIMMER not known in controller " + this.dimmerId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Dimmer", this.dimmerId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qDimmer);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_SWITCH:
                                try {
                                    handleSwitchCommand(qDimmer, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Switch for dimmer ID {}. IOException: {}",
                                            this.dimmerId, message);
                                } catch (InterruptedException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Switch for dimmer ID {}. Interruptedexception {}",
                                            this.dimmerId, message);
                                }
                                break;

                            case CHANNEL_BRIGHTNESS:
                                try {
                                    handleBrightnessCommand(qDimmer, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Brightness for dimmer ID {}. IOException: {}",
                                            this.dimmerId, message);
                                } catch (InterruptedException e) {
                                    String message = e.getMessage();
                                    logger.warn(
                                            "Error on executing Brightness for dimmer ID {}. Interruptedexception {}",
                                            this.dimmerId, message);
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
    private void handleSwitchCommand(QbusDimmer qDimmer, Command command) throws InterruptedException, IOException {
        if (command instanceof OnOffType) {
            String snr = getSN();
            if (snr != null) {
                if (command == OnOffType.OFF) {
                    qDimmer.execute(0, snr);
                } else {
                    qDimmer.execute(1000, snr);
                }
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "No serial number configured for DIMMER " + this.dimmerId);
            }
        }
    }

    /**
     * Executes the brightness command
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleBrightnessCommand(QbusDimmer qDimmer, Command command) throws InterruptedException, IOException {
        String snr = getSN();

        if (snr == null) {
            thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                    "No serial number configured for DIMMER " + this.dimmerId);
            return;
        } else {
            if (command instanceof OnOffType) {
                if (command == OnOffType.OFF) {
                    qDimmer.execute(0, snr);
                } else {
                    qDimmer.execute(100, snr);
                }
            } else if (command instanceof IncreaseDecreaseType) {
                int stepValue = ((Number) getConfig().get(CONFIG_STEP_VALUE)).intValue();
                Integer currentValue = qDimmer.getState();
                Integer newValue;
                Integer sendvalue;
                if (currentValue != null) {
                    if (command == IncreaseDecreaseType.INCREASE) {
                        newValue = currentValue + stepValue;
                        // round down to step multiple
                        newValue = newValue - newValue % stepValue;
                        sendvalue = newValue > 100 ? 100 : newValue;
                        qDimmer.execute(sendvalue, snr);
                    } else {
                        newValue = currentValue - stepValue;
                        // round up to step multiple
                        newValue = newValue + newValue % stepValue;
                        sendvalue = newValue < 0 ? 0 : newValue;
                        qDimmer.execute(sendvalue, snr);
                    }
                }
            } else if (command instanceof PercentType percentCommand) {
                int percentToInt = percentCommand.intValue();
                if (PercentType.ZERO.equals(command)) {
                    qDimmer.execute(0, snr);
                } else {
                    qDimmer.execute(percentToInt, snr);
                }
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Dimmer.
     *
     * @param qDimmer
     */
    public void handleStateUpdate(QbusDimmer qDimmer) {
        Integer dimmerState = qDimmer.getState();
        if (dimmerState != null) {
            updateState(CHANNEL_BRIGHTNESS, new PercentType(dimmerState));
        }
    }

    /**
     * Returns the serial number of the controller
     *
     * @return the serial number
     */
    public @Nullable String getSN() {
        return sn;
    }

    /**
     * Sets the serial number of the controller
     */
    public void setSN() {
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Dimmer", this.dimmerId);
        if (qBridgeHandler == null) {
            thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication with Qbus Bridge for DIMMER " + this.dimmerId);
            return;
        }
        this.sn = qBridgeHandler.getSn();
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        dimmerConfig = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return outputId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig localConfig = dimmerConfig;
        if (localConfig != null) {
            return localConfig.dimmerId;
        } else {
            return null;
        }
    }
}
