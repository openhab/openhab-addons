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
import static org.openhab.core.library.types.UpDownType.DOWN;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusRol;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * The {@link QbusRolHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusRolHandler extends QbusGlobalHandler {

    protected @Nullable QbusThingsConfig config;

    private int rolId;

    private @Nullable String sn;

    public QbusRolHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {

        setConfig();
        rolId = getId();

        QbusCommunication QComm = getCommunication("Screen/Store", rolId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Screen/Store", rolId);
        if (QBridgeHandler == null) {
            return;
        }

        setSN();

        Map<Integer, QbusRol> rolComm = QComm.getRol();

        if (rolComm != null) {
            QbusRol QRol = rolComm.get(rolId);
            if (QRol != null) {
                QRol.setThingHandler(this);
                handleStateUpdate(QRol);
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
        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Screen/Store", rolId);
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }
        this.sn = QBridgeHandler.getSn();
        ;
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication QComm = getCommunication("Screen/Store", rolId);

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for Screen/Store " + rolId);
            return;
        }

        Map<Integer, QbusRol> rolComm = QComm.getRol();

        if (rolComm != null) {
            QbusRol QRol = rolComm.get(rolId);
            if (QRol == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for ROL " + rolId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!QComm.communicationActive()) {
                        restartCommunication(QComm, "Screen/Store", rolId);
                    }

                    if (QComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(QRol);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_ROLLERSHUTTER:
                                handleScreenposCommand(QRol, command);
                                break;

                            case CHANNEL_SLATS:
                                handleSlatsposCommand(QRol, command);
                                break;
                        }
                    }
                });
            }
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
     * Executes the command for screen up/down position
     */
    private void handleScreenposCommand(QbusRol QRol, Command command) {
        String snr = getSN();
        if (command instanceof UpDownType) {
            UpDownType upDown = (UpDownType) command;
            if (upDown == DOWN) {
                if (snr != null) {
                    QRol.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    QRol.execute(100, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType inc = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            Integer currentValue = QRol.getState();
            int newValue;
            int sendValue;
            if (currentValue != null) {
                if (inc == IncreaseDecreaseType.INCREASE) {
                    newValue = currentValue + stepValue;
                    // round down to step multiple
                    newValue = newValue - newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        QRol.execute(sendValue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + rolId);
                    }
                } else {
                    newValue = currentValue - stepValue;
                    // round up to step multiple
                    newValue = newValue + newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        QRol.execute(sendValue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + rolId);
                    }
                }
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            int pp = p.intValue();
            if (p == PercentType.ZERO) {
                if (snr != null) {
                    QRol.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    QRol.execute(pp, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            }
        }
    }

    /**
     * Executes the command for screen slats position
     */
    private void handleSlatsposCommand(QbusRol QRol, Command command) {
        String snr = getSN();
        if (command instanceof org.openhab.core.library.types.UpDownType) {
            org.openhab.core.library.types.UpDownType s = (org.openhab.core.library.types.UpDownType) command;
            if (s == org.openhab.core.library.types.UpDownType.DOWN) {
                if (snr != null) {
                    QRol.executeSlats(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    QRol.executeSlats(100, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            Integer currentValue = QRol.getState();
            int newValue;
            int sendValue;
            if (currentValue != null) {
                if (s == IncreaseDecreaseType.INCREASE) {
                    newValue = currentValue + stepValue;
                    // round down to step multiple
                    newValue = newValue - newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        QRol.executeSlats(sendValue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + rolId);
                    }
                } else {
                    newValue = currentValue - stepValue;
                    // round up to step multiple
                    newValue = newValue + newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        QRol.executeSlats(sendValue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + rolId);
                    }
                }
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            int pp = p.intValue();
            if (p == PercentType.ZERO) {
                if (snr != null) {
                    QRol.executeSlats(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    QRol.executeSlats(pp, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Screen/Store.
     */
    public void handleStateUpdate(QbusRol qRol) {
        Integer rolState = qRol.getState();
        Integer slatState = qRol.getStateSlats();

        if (rolState != null) {
            updateState(CHANNEL_ROLLERSHUTTER, new PercentType(rolState));
            updateStatus(ThingStatus.ONLINE);
        }
        if (slatState != null) {
            updateState(CHANNEL_SLATS, new PercentType(slatState));
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
     * @return rolId
     */
    public int getId() {
        if (this.config != null) {
            return this.config.rolId;
        } else {
            return 0;
        }
    }
}
