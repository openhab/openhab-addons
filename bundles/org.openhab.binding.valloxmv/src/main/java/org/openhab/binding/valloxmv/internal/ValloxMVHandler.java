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
package org.openhab.binding.valloxmv.internal;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ValloxMVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Björn Brings - Initial contribution
 */
@NonNullByDefault
public class ValloxMVHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ValloxMVHandler.class);
    private @Nullable ScheduledFuture<?> updateTasks;
    private @Nullable ValloxMVWebSocket valloxSocket;
    private @Nullable WebSocketClient webSocketClient;

    /**
     * Refresh interval in seconds.
     */
    private int updateInterval;
    private long lastUpdate;

    /**
     * IP of vallox ventilation unit web interface.
     */
    public ValloxMVHandler(Thing thing, @Nullable WebSocketClient webSocketClient) {
        super(thing);
        this.webSocketClient = webSocketClient;
    }

    @SuppressWarnings({ "null", "unchecked" })
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (valloxSocket == null) {
            return;
        }
        if (command instanceof RefreshType) {
            if (lastUpdate > System.currentTimeMillis() + updateInterval * 1000) {
                valloxSocket.request(null, null);
            }
        } else {
            String strUpdateValue = "";
            if (ValloxMVBindingConstants.CHANNEL_STATE.equals(channelUID.getId())) {
                try {
                    int cmd = Integer.parseInt(command.toString());
                    if ((cmd == ValloxMVBindingConstants.STATE_FIREPLACE)
                            || (cmd == ValloxMVBindingConstants.STATE_ATHOME)
                            || (cmd == ValloxMVBindingConstants.STATE_AWAY)
                            || (cmd == ValloxMVBindingConstants.STATE_BOOST)) {
                        //logger.debug("Changing state to: {}", command);
                        strUpdateValue = command.toString();
                    }
                } catch (NumberFormatException nfe) {
                    // Other commands like refresh
                    return;
                }
            } else if (ValloxMVBindingConstants.WRITABLE_CHANNELS_SWITCHES.contains(channelUID.getId())) {
                if (ValloxMVBindingConstants.CHANNEL_ONOFF.equals(channelUID.getId())) {
                    // Vallox MV MODE: Normal mode = 0, Switch off = 5
                    strUpdateValue = (OnOffType.ON.equals(command)) ? "0" : "5";
                } else {
                    // Switches with ON = 1, OFF = 0
                    strUpdateValue = (OnOffType.ON.equals(command)) ? "1" : "0";
                }
            } else if (ValloxMVBindingConstants.WRITABLE_CHANNELS_DIMENSIONLESS.contains(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    QuantityType<Dimensionless> quantity = (QuantityType<Dimensionless>) command;
                    strUpdateValue = Integer.toString(quantity.intValue());
                }
            } else if (ValloxMVBindingConstants.WRITABLE_CHANNELS_TEMPERATURE.contains(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    // Convert temperature to centiKelvin (= (Celsius * 100) + 27315 )
                    QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command)
                            .toUnit(SmartHomeUnits.KELVIN);
                    if (quantity == null) {
                        return;
                    }
                    int centiKelvin = quantity.multiply(new BigDecimal(100)).intValue();
                    strUpdateValue = Integer.toString(centiKelvin);
                }
            } else {
                // Not writable channel
                return;
            }
            if (strUpdateValue != "") {
                // Stop scheduler while sending command to device
                dispose();
                logger.debug("Scheduled data table requests stopped");
                // Send command and process response
                valloxSocket.request(channelUID, strUpdateValue);
                // Start scheduler with 5 seconds delay; first data request in 5 seconds
                scheduleUpdatesWithDelay(5);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);

        String ip = getConfigAs(ValloxMVConfig.class).getIp();
        valloxSocket = new ValloxMVWebSocket(webSocketClient, ValloxMVHandler.this, ip);

        logger.debug("Vallox MV IP address : {}", ip);

        scheduleUpdatesWithDelay(2);
    }

    private void scheduleUpdatesWithDelay(int delay) {
        if (delay < 0) delay = 0;

        updateInterval = getConfigAs(ValloxMVConfig.class).getUpdateinterval();
        if (updateInterval < 15) updateInterval = 60;

        logger.debug("Data table request interval {} seconds, Request in {} seconds", updateInterval, delay);

        updateTasks = scheduler.scheduleWithFixedDelay(() -> {
            // Read all device values
            valloxSocket.request(null, null);
        }, delay, updateInterval, TimeUnit.SECONDS);
    }

    public void dataUpdated() {
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void dispose() {
        if (updateTasks != null) {
            updateTasks.cancel(true);
        }
        updateTasks = null;
    }

    @Override
    protected void updateState(String strChannelName, State dt) {
        super.updateState(strChannelName, dt);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail) {
        super.updateStatus(ts, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus ts) {
        super.updateStatus(ts);
    }
}
