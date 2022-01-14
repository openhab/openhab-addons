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
package org.openhab.binding.valloxmv.internal;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
    private @Nullable ScheduledFuture<?> readDataJob;
    private @Nullable ValloxMVWebSocket valloxSocket;
    private @Nullable WebSocketClient webSocketClient;

    /**
     * Refresh interval in seconds.
     */
    private int readDataInterval;

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
            // We don't have Vallox device values in memory - we cannot update channel with value
            // We can re-schedule readDataJob in case next read is more than 5 seconds away
            if (readDataJob != null) {
                if ((!readDataJob.isDone()) && (readDataJob.getDelay(TimeUnit.MILLISECONDS) > 5000)) {
                    // Next read is more than 5 seconds away, re-schedule
                    // Cancel read data job
                    cancelReadDataJob();
                    // Schedule read data job with 2 seconds initial delay
                    scheduleReadDataJob(2);
                }
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
                        // logger.debug("Changing state to: {}", command);
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
                    QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command).toUnit(Units.KELVIN);
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
            if (!strUpdateValue.isEmpty()) {
                if (readDataJob != null) {
                    // Re-schedule readDataJob to read device values after data write
                    // Avoid re-scheduling job several times in case of subsequent data writes
                    long timeToRead = readDataJob.getDelay(TimeUnit.MILLISECONDS);
                    if ((!readDataJob.isDone()) && ((timeToRead < 2000) || (timeToRead > 5000))) {
                        // Next read is not within the next 2 to 5 seconds, cancel read data job
                        cancelReadDataJob();
                        // Schedule read data job with 5 seconds initial delay
                        scheduleReadDataJob(5);
                    }
                }
                // Send command and process response
                valloxSocket.request(channelUID, strUpdateValue);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());

        updateStatus(ThingStatus.UNKNOWN);

        String ip = getConfigAs(ValloxMVConfig.class).getIp();
        valloxSocket = new ValloxMVWebSocket(webSocketClient, ValloxMVHandler.this, ip);

        logger.debug("Vallox MV IP address : {}", ip);
        // Schedule read data job with 2 seconds initial delay
        scheduleReadDataJob(2);

        logger.debug("Thing {} initialized", getThing().getUID());
    }

    private void scheduleReadDataJob(int initialDelay) {
        if (initialDelay < 0) {
            initialDelay = 0;
        }

        readDataInterval = getConfigAs(ValloxMVConfig.class).getUpdateinterval();
        if (readDataInterval < 15) {
            readDataInterval = 60;
        }

        logger.debug("Data table request interval {} seconds, Request in {} seconds", readDataInterval, initialDelay);

        readDataJob = scheduler.scheduleWithFixedDelay(() -> {
            // Read all device values
            valloxSocket.request(null, null);
        }, initialDelay, readDataInterval, TimeUnit.SECONDS);
    }

    private void cancelReadDataJob() {
        if (readDataJob != null) {
            if (!readDataJob.isDone()) {
                readDataJob.cancel(true);
                logger.debug("Scheduled data table requests cancelled");
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        cancelReadDataJob();
        logger.debug("Thing {} disposed", getThing().getUID());
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
