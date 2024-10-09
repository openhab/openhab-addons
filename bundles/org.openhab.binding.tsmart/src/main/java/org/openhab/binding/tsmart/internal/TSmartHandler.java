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
package org.openhab.binding.tsmart.internal;

import static org.openhab.binding.tsmart.internal.TSmartBindingConstants.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TSmartHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class TSmartHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TSmartHandler.class);
    private @Nullable TSmartConfiguration config;

    private volatile int unRespondedRequests = 0;
    private @Nullable InetAddress destAddr;

    private @Nullable ScheduledFuture<?> statusRefreshJob;
    private boolean listenerInitializedSuccessfully = false;

    public TSmartHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            requestStatusRefresh();
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    if (command instanceof OnOffType) {
                        setPower(OnOffType.ON.equals(command));
                    }
                    break;
                case CHANNEL_SETPOINT:
                    if (command instanceof QuantityType<?> quantityCommand) {
                        setSetpoint(quantityCommand);
                    }
                    break;
                case CHANNEL_MODE:
                    if (command instanceof StringType stringCommand) {
                        setMode(stringCommand);
                    }
                    break;
            }
        }
    }

    /**
     * Initialise ThingHandler - start listening for UDP responses,
     * and sending status requests on the configured frequency.
     */
    @Override
    public void initialize() {
        TSmartConfiguration config = getConfigAs(TSmartConfiguration.class);
        this.config = config;
        if (config.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.hostname-required");
        } else if (config.refreshInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.refresh-gt-zero");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            statusRefreshJob = scheduler.scheduleWithFixedDelay(this::requestStatusRefresh, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private synchronized boolean initializeListener() {
        if (listenerInitializedSuccessfully) {
            return true;
        }

        TSmartConfiguration thisConfig = config;
        if (thisConfig != null) {
            try {
                InetAddress destination = InetAddress.getByName(thisConfig.hostname);

                destAddr = destination;

                TSmartUDPListener.addHandler(destination, this);

                listenerInitializedSuccessfully = true;
            } catch (UnknownHostException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } else {
            logger.debug("Unexpected missing config");
        }

        return listenerInitializedSuccessfully;
    }

    /**
     * Close down ThingHandler - cease sending UDP requests
     * and listening for UDP responses for this device.
     */
    @Override
    public void dispose() {
        InetAddress destination = destAddr;
        if (destination != null) {
            TSmartUDPListener.removeHandler(destination);
        }
        ScheduledFuture<?> statusRefreshJob = this.statusRefreshJob;
        if (statusRefreshJob != null) {
            statusRefreshJob.cancel(true);
            this.statusRefreshJob = null;
        }
    }

    /**
     * Handle the UDP packet for the T-Smart device Status response.
     * Update status and map data points to Channels on the Thing.
     *
     * @param buffer UDP Packet
     */
    public void updateStatusHandler(byte[] buffer) {
        unRespondedRequests = 0;
        updateStatus(ThingStatus.ONLINE);
        QuantityType<Temperature> high = getTemperature(buffer[7], buffer[8]);
        QuantityType<Temperature> low = getTemperature(buffer[11], buffer[12]);
        QuantityType<Temperature> avg = new QuantityType<Temperature>(
                high.toBigDecimal().add(low.toBigDecimal()).divide(BigDecimal.valueOf(2)), SIUnits.CELSIUS);

        updateState(CHANNEL_POWER, OnOffType.from(buffer[3] == 0x1));
        updateState(CHANNEL_SETPOINT, getTemperature(buffer[4], buffer[5]));
        updateState(CHANNEL_TEMPERATURE_HIGH, high);
        updateState(CHANNEL_TEMPERATURE_LOW, low);
        updateState(CHANNEL_TEMPERATURE, avg);
        updateState(CHANNEL_RELAY, OnOffType.from(buffer[9] == 0x1));
        updateState(CHANNEL_MODE, getMode(buffer[6]));
        updateState(CHANNEL_SMART_STATE, getSmartState(buffer[10]));
    }

    private void requestStatusRefresh() {
        if (!initializeListener()) {
            return;
        }

        sendUDPPacket(new byte[] { (byte) 0xF1, (byte) 0x00, (byte) 0x00 });

        unRespondedRequests++;
        if (unRespondedRequests > 5) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.five-missing-responses");
        }
    }

    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String message) {
        super.updateStatus(status, statusDetail, message);
    }

    private QuantityType<Temperature> getTemperature(byte firstByte, byte secondByte) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(firstByte);
        buffer.put(secondByte);
        short shortVal = buffer.getShort(0);

        return new QuantityType<Temperature>(((float) shortVal) / 10, SIUnits.CELSIUS);
    }

    private State getMode(byte b) {
        int i = b;

        if (MODES.size() > i) {
            return new StringType(MODES.get(i));
        } else if (b == 0x21) {
            return new StringType("Limited");
        } else if (b == 0x22) {
            return new StringType("Critical");
        } else {
            return UnDefType.UNDEF;
        }
    }

    private State getSmartState(byte b) {
        int i = b;

        if (SMART_MODES.length > i) {
            return new StringType(SMART_MODES[i]);
        } else {
            return UnDefType.UNDEF;
        }
    }

    private void setPower(boolean power) {
        byte zero = 0x00;
        byte[] controlWrite = new byte[] { (byte) 0xF2, zero, zero, power ? 0x01 : zero, zero, zero, zero };

        sendUDPPacket(controlWrite);
    }

    private void setSetpoint(QuantityType<?> command) {
        if (command.getDimension().equals(SIUnits.CELSIUS.getDimension())) {
            QuantityType<?> temp = command.toUnit(SIUnits.CELSIUS);
            if (temp != null) {
                short temp10 = (short) (temp.floatValue() * 10);

                ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.putShort(temp10);

                byte zero = 0x00;
                byte[] controlWrite = new byte[] { (byte) 0xF2, zero, zero, (byte) 0x01, buffer.array()[0],
                        buffer.array()[1], zero };

                sendUDPPacket(controlWrite);
            }
        }
    }

    private void setMode(StringType command) {
        int i = MODES.indexOf(command.toString());
        byte zero = 0x00;
        byte[] controlWrite = new byte[] { (byte) 0xF2, zero, zero, (byte) 0x01, zero, zero, (byte) i };

        sendUDPPacket(controlWrite);
    }

    private void sendUDPPacket(byte[] payload) {
        InetAddress destination = destAddr;
        if (destination != null) {
            new TSmartUDPUtils().sendUDPPacket(destination, payload);
        }
    }
}
