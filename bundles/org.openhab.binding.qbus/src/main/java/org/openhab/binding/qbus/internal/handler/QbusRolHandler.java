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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;
import static org.openhab.core.library.types.UpDownType.DOWN;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusRolHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusRolHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusRolHandler.class);

    protected @Nullable QbusThingsConfig rolConfig = new QbusThingsConfig();

    private @Nullable Integer rolId;

    private @Nullable String sn;

    public QbusRolHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        readConfig();

        this.rolId = getId();

        setSN();

        scheduler.submit(() -> {
            QbusCommunication controllerComm;

            if (this.rolId != null) {
                controllerComm = getCommunication("Screen/Store", this.rolId);
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for Screen/Store no set! " + this.rolId);
                return;
            }

            if (controllerComm == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for Screen/Store not known in controller " + this.rolId);
                return;
            }

            Map<Integer, QbusRol> rolCommLocal = controllerComm.getRol();

            QbusRol outputLocal = rolCommLocal.get(this.rolId);

            if (outputLocal == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge could not initialize Screen/Store ID " + this.rolId);
                return;
            }

            outputLocal.setThingHandler(this);
            handleStateUpdate(outputLocal);

            QbusBridgeHandler qBridgeHandler = getBridgeHandler("Screen/Store", this.rolId);

            if (qBridgeHandler != null) {
                if (qBridgeHandler.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Bridge offline for SCREEN/STORE ID " + this.rolId);
                }
            }
        });
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Screen/Store", this.rolId);

        if (qComm == null) {
            thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                    "ID for ROLLERSHUTTER/SCREEN not known in controller " + this.rolId);
            return;
        } else {
            Map<Integer, QbusRol> rolComm = qComm.getRol();

            QbusRol qRol = rolComm.get(this.rolId);

            if (qRol == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for ROLLERSHUTTER/SCREEN not known in controller " + this.rolId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Screen/Store", this.rolId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qRol);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_ROLLERSHUTTER:
                                try {
                                    handleScreenposCommand(qRol, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Rollershutter for screen ID {}. IOException: {}",
                                            this.rolId, message);
                                } catch (InterruptedException e) {
                                    String message = e.toString();
                                    logger.warn(
                                            "Error on executing Rollershutter for screen ID {}. Interruptedexception {}",
                                            this.rolId, message);
                                }
                                break;

                            case CHANNEL_SLATS:
                                try {
                                    handleSlatsposCommand(qRol, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Slats for screen ID {}. IOException: {}",
                                            this.rolId, message);
                                } catch (InterruptedException e) {
                                    String message = e.toString();
                                    logger.warn("Error on executing Slats for screen ID {}. Interruptedexception {}",
                                            this.rolId, message);
                                }
                                break;
                        }
                    }
                });
            }
        }
    }

    /**
     * Executes the command for screen up/down position
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleScreenposCommand(QbusRol qRol, Command command) throws InterruptedException, IOException {
        String snr = getSN();
        if (snr != null) {
            if (command instanceof UpDownType) {
                UpDownType upDown = (UpDownType) command;
                if (upDown == DOWN) {
                    qRol.execute(0, snr);
                } else {
                    qRol.execute(100, snr);
                }
            } else if (command instanceof IncreaseDecreaseType) {
                IncreaseDecreaseType inc = (IncreaseDecreaseType) command;
                int stepValue = ((Number) getConfig().get(CONFIG_STEP_VALUE)).intValue();
                Integer currentValue = qRol.getState();
                int newValue;
                int sendValue;
                if (currentValue != null) {
                    if (inc == IncreaseDecreaseType.INCREASE) {
                        newValue = currentValue + stepValue;
                        // round down to step multiple
                        newValue = newValue - newValue % stepValue;
                        sendValue = newValue > 100 ? 100 : newValue;
                        qRol.execute(sendValue, snr);
                    } else {
                        newValue = currentValue - stepValue;
                        // round up to step multiple
                        newValue = newValue + newValue % stepValue;
                        sendValue = newValue > 100 ? 100 : newValue;
                        qRol.execute(sendValue, snr);
                    }
                }
            } else if (command instanceof PercentType) {
                PercentType p = (PercentType) command;
                int pp = p.intValue();
                if (PercentType.ZERO.equals(p)) {
                    qRol.execute(0, snr);
                } else {
                    qRol.execute(pp, snr);
                }
            }
        }
    }

    /**
     * Executes the command for screen slats position
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleSlatsposCommand(QbusRol qRol, Command command) throws InterruptedException, IOException {
        String snr = getSN();
        if (snr != null) {
            if (command instanceof UpDownType) {
                if (command == DOWN) {
                    qRol.executeSlats(0, snr);
                } else {
                    qRol.executeSlats(100, snr);
                }
            } else if (command instanceof IncreaseDecreaseType) {
                int stepValue = ((Number) getConfig().get(CONFIG_STEP_VALUE)).intValue();
                Integer currentValue = qRol.getState();
                int newValue;
                int sendValue;
                if (currentValue != null) {
                    if (command == IncreaseDecreaseType.INCREASE) {
                        newValue = currentValue + stepValue;
                        // round down to step multiple
                        newValue = newValue - newValue % stepValue;
                        sendValue = newValue > 100 ? 100 : newValue;
                        qRol.executeSlats(sendValue, snr);
                    } else {
                        newValue = currentValue - stepValue;
                        // round up to step multiple
                        newValue = newValue + newValue % stepValue;
                        sendValue = newValue > 100 ? 100 : newValue;
                        qRol.executeSlats(sendValue, snr);
                    }
                }
            } else if (command instanceof PercentType) {
                int percentToInt = ((PercentType) command).intValue();
                if (PercentType.ZERO.equals(command)) {
                    qRol.executeSlats(0, snr);
                } else {
                    qRol.executeSlats(percentToInt, snr);
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
        }
        if (slatState != null) {
            updateState(CHANNEL_SLATS, new PercentType(slatState));
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
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Screen/Store", this.rolId);
        if (qBridgeHandler == null) {
            thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication with Qbus Bridge for ROLLERSHUTTER/SCREEN " + this.rolId);
            return;
        }
        sn = qBridgeHandler.getSn();
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        rolConfig = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return outputId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig localConfig = rolConfig;
        if (localConfig != null) {
            return localConfig.rolId;
        } else {
            return null;
        }
    }
}
