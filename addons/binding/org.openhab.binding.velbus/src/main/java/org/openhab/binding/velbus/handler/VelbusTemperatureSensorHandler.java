/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.handler;

import static org.openhab.binding.velbus.VelbusBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSensorTemperatureRequestPacket;

/**
 * The {@link VelbusTemperatureSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
public abstract class VelbusTemperatureSensorHandler extends VelbusSensorHandler {
    private ScheduledFuture<?> refreshJob;
    private ChannelUID temperatureChannel;

    public VelbusTemperatureSensorHandler(Thing thing, int numberOfSubAddresses, ChannelUID temperatureChannel) {
        super(thing, numberOfSubAddresses);

        this.temperatureChannel = temperatureChannel;
    }

    @Override
    public void initialize() {
        super.initialize();

        initializeAutomaticRefresh();
    }

    private void initializeAutomaticRefresh() {
        Object refreshIntervalObject = getConfig().get(REFRESH_INTERVAL);
        if (refreshIntervalObject != null) {
            int refreshInterval = ((BigDecimal) refreshIntervalObject).intValue();

            if (refreshInterval > 0) {
                startAutomaticRefresh(refreshInterval);
            }
        }
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    private void startAutomaticRefresh(int refreshInterval) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            sendSensorTemperatureRequest(velbusBridgeHandler);
        }, 0, refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (command instanceof RefreshType) {
            if (channelUID.equals(temperatureChannel)) {
                sendSensorTemperatureRequest(velbusBridgeHandler);
        }
    }
        }

    protected void sendSensorTemperatureRequest(VelbusBridgeHandler velbusBridgeHandler) {
        VelbusSensorTemperatureRequestPacket packet = new VelbusSensorTemperatureRequestPacket(
                getModuleAddress().getAddress());

        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        super.onPacketReceived(packet);

        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_SENSOR_TEMPERATURE && packet.length >= 6) {
                byte highByteCurrentSensorTemperature = packet[5];
                byte lowByteCurrentSensorTemperature = packet[6];

                double temperature = ((highByteCurrentSensorTemperature << 3) + (lowByteCurrentSensorTemperature >> 5))
                        * 0.0625 * ((lowByteCurrentSensorTemperature & 0x1F) == 0x1F ? -1 : 1);
                QuantityType<Temperature> state = new QuantityType<>(temperature, SIUnits.CELSIUS);
                updateState(temperatureChannel, state);
            }
        }
    }
}
