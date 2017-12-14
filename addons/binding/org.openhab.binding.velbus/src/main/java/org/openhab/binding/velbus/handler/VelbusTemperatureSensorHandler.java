/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.handler;

import static org.openhab.binding.velbus.VelbusBindingConstants.COMMAND_SENSOR_TEMPERATURE;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
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
    ScheduledFuture<?> refreshJob;

    ChannelUID temperatureChannel;

    public VelbusTemperatureSensorHandler(Thing thing, int numberOfChannels, int numberOfSubAddresses,
            ChannelUID temperatureChannel) {
        super(thing, numberOfChannels, numberOfSubAddresses);

        this.temperatureChannel = temperatureChannel;
    }

    @Override
    public void initialize() {
        super.initialize();

        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    @SuppressWarnings("null")
    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                sendSensorTemperatureRequest();
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, 300, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            logger.warn("Velbus bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        if (command instanceof RefreshType) {
            if (channelUID.equals(temperatureChannel)) {
                sendSensorTemperatureRequest();
            }
        }
    }

    protected void sendSensorTemperatureRequest() {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            logger.warn("Velbus bridge handler not found. Cannot send request without bridge.");
            return;
        }

        VelbusSensorTemperatureRequestPacket packet = new VelbusSensorTemperatureRequestPacket(
                getModuleAddress().getAddress());

        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    @SuppressWarnings("null")
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
                updateState(temperatureChannel, new DecimalType(temperature));
            }
        }
    }
}
