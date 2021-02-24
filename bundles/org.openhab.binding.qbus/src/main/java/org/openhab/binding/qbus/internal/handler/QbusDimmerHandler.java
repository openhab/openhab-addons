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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;
import static org.openhab.core.types.RefreshType.REFRESH;

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

/**
 * The {@link QbusDimmerHandler} is responsible for handling the dimmable outputs of Qbus
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusDimmerHandler extends QbusGlobalHandler {

    protected @Nullable QbusThingsConfig config;

    private int dimmerId;

    private @Nullable String sn;

    public QbusDimmerHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        setConfig();
        dimmerId = getId();

        QbusCommunication qComm = getCommunication("Dimmer", dimmerId);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Dimmer", dimmerId);
        if (qBridgeHandler == null) {
            return;
        }

        setSN();

        Map<Integer, QbusDimmer> dimmerComm = qComm.getDimmer();
        if (dimmerComm != null) {
            QbusDimmer qDimmer = dimmerComm.get(dimmerId);
            if (qDimmer != null) {
                qDimmer.setThingHandler(this);
                handleStateUpdate(qDimmer);
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
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Dimmer", dimmerId);
        if (qBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        } else {
            this.sn = qBridgeHandler.getSn();
        }
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Dimmer", dimmerId);

        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for dimmer " + dimmerId);
            return;
        }

        Map<Integer, QbusDimmer> dimmerComm = qComm.getDimmer();
        if (dimmerComm != null) {
            QbusDimmer qDimmer = dimmerComm.get(dimmerId);

            if (qDimmer == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for dimmer " + dimmerId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Dimmer", dimmerId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qDimmer);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_SWITCH:
                                handleSwitchCommand(qDimmer, command);
                                break;

                            case CHANNEL_BRIGHTNESS:
                                handleBrightnessCommand(qDimmer, command);
                                break;
                        }
                    }
                });
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Error while initializing the thing.");
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
     * Executes the switch command
     */
    private void handleSwitchCommand(QbusDimmer qDimmer, Command command) {
        if (command instanceof OnOffType) {
            String snr = getSN();

            if (command == OnOffType.OFF) {
                if (snr != null) {
                    qDimmer.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + dimmerId);
                }
            } else {
                if (snr != null) {
                    qDimmer.execute(1000, snr);
                } else {
                    thingOffline("No serial number configured for  " + dimmerId);
                }
            }
        }
    }

    /**
     * Executes the brightness command
     */
    private void handleBrightnessCommand(QbusDimmer qDimmer, Command command) {
        String snr = getSN();
        if (command instanceof OnOffType) {
            if (command == OnOffType.OFF) {
                if (snr != null) {
                    qDimmer.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + dimmerId);
                }
            } else {
                if (snr != null) {
                    qDimmer.execute(100, snr);
                } else {
                    thingOffline("No serial number configured for  " + dimmerId);
                }
            }
        } else if (command instanceof IncreaseDecreaseType) {
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            Integer currentValue = qDimmer.getState();
            Integer newValue;
            Integer sendvalue;
            if (currentValue != null) {
                if (command == IncreaseDecreaseType.INCREASE) {
                    newValue = currentValue + stepValue;
                    // round down to step multiple
                    newValue = newValue - newValue % stepValue;
                    sendvalue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        qDimmer.execute(sendvalue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + dimmerId);
                    }
                } else {
                    newValue = currentValue - stepValue;
                    // round up to step multiple
                    newValue = newValue + newValue % stepValue;
                    sendvalue = newValue < 0 ? 0 : newValue;
                    if (snr != null) {
                        qDimmer.execute(sendvalue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + dimmerId);
                    }
                }
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            int pp = p.intValue();
            if (command == PercentType.ZERO) {
                if (snr != null) {
                    qDimmer.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + dimmerId);
                }
            } else {
                if (snr != null) {
                    qDimmer.execute(pp, snr);
                } else {
                    thingOffline("No serial number configured for  " + dimmerId);
                }
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Dimmer.
     */
    public void handleStateUpdate(QbusDimmer qDimmer) {
        Integer dimmerState = qDimmer.getState();
        if (dimmerState != null) {
            updateState(CHANNEL_BRIGHTNESS, new PercentType(dimmerState));
            updateStatus(ThingStatus.ONLINE);
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
     * @return dimmerId
     */
    public int getId() {
        if (this.config != null) {
            return this.config.dimmerId;
        } else {
            return 0;
        }
    }
}
