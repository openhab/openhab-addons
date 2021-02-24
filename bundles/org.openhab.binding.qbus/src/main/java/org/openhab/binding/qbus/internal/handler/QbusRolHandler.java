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
import java.util.concurrent.ScheduledFuture;

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

    private @Nullable Integer rolId;

    private @Nullable String sn;

    private @Nullable ScheduledFuture<?> pollingJob;

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

        QbusCommunication qComm = getCommunication("Screen/Store", rolId);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Screen/Store", rolId);
        if (qBridgeHandler == null) {
            return;
        }

        setSN();

        Map<Integer, QbusRol> rolComm = qComm.getRol();

        if (rolComm != null) {
            QbusRol qRol = rolComm.get(rolId);
            if (qRol != null) {
                qRol.setThingHandler(this);
                handleStateUpdate(qRol);
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
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Screen/Store", rolId);
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
        QbusCommunication qComm = getCommunication("Screen/Store", rolId);

        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for Screen/Store " + rolId);
            return;
        }

        Map<Integer, QbusRol> rolComm = qComm.getRol();

        if (rolComm != null) {
            QbusRol qRol = rolComm.get(rolId);
            if (qRol == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for ROL " + rolId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Screen/Store", rolId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qRol);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_ROLLERSHUTTER:
                                handleScreenposCommand(qRol, command);
                                break;

                            case CHANNEL_SLATS:
                                handleSlatsposCommand(qRol, command);
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
    private void handleScreenposCommand(QbusRol qRol, Command command) {
        String snr = getSN();
        if (command instanceof UpDownType) {
            UpDownType upDown = (UpDownType) command;
            if (upDown == DOWN) {
                if (snr != null) {
                    qRol.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    qRol.execute(100, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType inc = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            Integer currentValue = qRol.getState();
            int newValue;
            int sendValue;
            if (currentValue != null) {
                if (inc == IncreaseDecreaseType.INCREASE) {
                    newValue = currentValue + stepValue;
                    // round down to step multiple
                    newValue = newValue - newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        qRol.execute(sendValue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + rolId);
                    }
                } else {
                    newValue = currentValue - stepValue;
                    // round up to step multiple
                    newValue = newValue + newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        qRol.execute(sendValue, snr);
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
                    qRol.execute(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    qRol.execute(pp, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            }
        }
    }

    /**
     * Executes the command for screen slats position
     */
    private void handleSlatsposCommand(QbusRol qRol, Command command) {
        String snr = getSN();
        if (command instanceof UpDownType) {
            if (command == DOWN) {
                if (snr != null) {
                    qRol.executeSlats(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    qRol.executeSlats(100, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            }
        } else if (command instanceof IncreaseDecreaseType) {
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            Integer currentValue = qRol.getState();
            int newValue;
            int sendValue;
            if (currentValue != null) {
                if (command == IncreaseDecreaseType.INCREASE) {
                    newValue = currentValue + stepValue;
                    // round down to step multiple
                    newValue = newValue - newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        qRol.executeSlats(sendValue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + rolId);
                    }
                } else {
                    newValue = currentValue - stepValue;
                    // round up to step multiple
                    newValue = newValue + newValue % stepValue;
                    sendValue = newValue > 100 ? 100 : newValue;
                    if (snr != null) {
                        qRol.executeSlats(sendValue, snr);
                    } else {
                        thingOffline("No serial number configured for  " + rolId);
                    }
                }
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            int pp = p.intValue();
            if (command == PercentType.ZERO) {
                if (snr != null) {
                    qRol.executeSlats(0, snr);
                } else {
                    thingOffline("No serial number configured for  " + rolId);
                }
            } else {
                if (snr != null) {
                    qRol.executeSlats(pp, snr);
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
        final ScheduledFuture<?> localPollingJob = this.pollingJob;

        if (localPollingJob != null) {
            localPollingJob.cancel(true);
        }

        if (localPollingJob == null || localPollingJob.isCancelled()) {
            this.config = getConfig().as(QbusThingsConfig.class);
        }
        this.config = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return rolId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig rolConfig = this.config;
        if (rolConfig != null) {
            if (rolConfig.rolId != null) {
                return rolConfig.rolId;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
