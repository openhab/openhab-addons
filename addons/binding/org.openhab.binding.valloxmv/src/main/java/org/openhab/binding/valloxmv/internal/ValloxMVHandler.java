/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.valloxmv.internal;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
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
public class ValloxMVHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ValloxMVHandler.class);
    private ScheduledFuture<?> updateTasks;
    private ValloxMVWebSocket valloxSendSocket;

    /**
     * Refresh interval in seconds.
     */
    private int updateInterval;
    private long lastUpdate;

    /**
     * IP of vallox ventilation unit web interface.
     */
    public ValloxMVHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (lastUpdate > System.currentTimeMillis() + updateInterval * 1000) {
                valloxSendSocket.request(null, null);
            }
        } else {
            if (ValloxMVBindingConstants.CHANNEL_STATE.equals(channelUID.getId())) {
                try {
                    int cmd = Integer.parseInt(command.toString());
                    if ((cmd == ValloxMVBindingConstants.STATE_FIREPLACE)
                            || (cmd == ValloxMVBindingConstants.STATE_ATHOME)
                            || (cmd == ValloxMVBindingConstants.STATE_AWAY)
                            || (cmd == ValloxMVBindingConstants.STATE_BOOST)) {
                        logger.debug("Changing state to: {}", command);
                        // Open WebSocket
                        valloxSendSocket.request(channelUID, command.toString());
                        valloxSendSocket.request(null, null);
                    }
                } catch (NumberFormatException nfe) {
                    // Other commands like refresh
                    return;
                }
            } else if (ValloxMVBindingConstants.CHANNEL_ONOFF.equals(channelUID.getId())) {
                if (OnOffType.ON.equals(command)) {
                    valloxSendSocket.request(channelUID, "0");
                    valloxSendSocket.request(null, null);
                } else if (OnOffType.OFF.equals(command)) {
                    valloxSendSocket.request(channelUID, "5");
                    valloxSendSocket.request(null, null);
                }
            } else if (ValloxMVBindingConstants.CHANNEL_EXTR_FAN_BALANCE_BASE.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    QuantityType<Dimensionless> quantity = (QuantityType<Dimensionless>) command;
                    valloxSendSocket.request(channelUID, Integer.toString(quantity.intValue()));
                    valloxSendSocket.request(null, null);
                }
            } else if (ValloxMVBindingConstants.CHANNEL_SUPP_FAN_BALANCE_BASE.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    QuantityType<Dimensionless> quantity = (QuantityType<Dimensionless>) command;
                    valloxSendSocket.request(channelUID, Integer.toString(quantity.intValue()));
                    valloxSendSocket.request(null, null);
                }
            } else if (ValloxMVBindingConstants.CHANNEL_HOME_SPEED_SETTING.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    QuantityType<Dimensionless> quantity = (QuantityType<Dimensionless>) command;
                    valloxSendSocket.request(channelUID, Integer.toString(quantity.intValue()));
                    valloxSendSocket.request(null, null);
                }
            } else if (ValloxMVBindingConstants.CHANNEL_HOME_AIR_TEMP_TARGET.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    // Convert temperature to millidegree Kelvin
                    QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command)
                            .toUnit(SmartHomeUnits.KELVIN);
                    if (quantity == null) {
                        return;
                    }
                    int milliKelvin = quantity.multiply(new BigDecimal(100)).intValue();
                    valloxSendSocket.request(channelUID, Integer.toString(milliKelvin));
                    valloxSendSocket.request(null, null);
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);

        String ip = getConfigAs(ValloxMVConfig.class).getIp();
        valloxSendSocket = new ValloxMVWebSocket(ValloxMVHandler.this, ip);

        updateInterval = getConfigAs(ValloxMVConfig.class).getUpdateinterval();
        if (updateInterval < 15) {
            updateInterval = 60;
        }

        scheduleUpdates();
    }

    private void scheduleUpdates() {
        logger.debug("Schedule vallox update every {} sec", updateInterval);

        String ip = getConfigAs(ValloxMVConfig.class).getIp();
        logger.debug("Connecting to ip: {}", ip);
        // Open WebSocket
        ValloxMVWebSocket valloxSocket = new ValloxMVWebSocket(ValloxMVHandler.this, ip);

        updateTasks = scheduler.scheduleWithFixedDelay(() -> {
            // Do a pure read request to websocket interface
            valloxSocket.request(null, null);
        }, 0, updateInterval, TimeUnit.SECONDS);
    }

    public void dataUpdated() {
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void dispose() {
        updateTasks.cancel(true);
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
