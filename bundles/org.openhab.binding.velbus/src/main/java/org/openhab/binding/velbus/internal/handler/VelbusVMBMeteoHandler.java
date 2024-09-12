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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSensorReadoutRequestPacket;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMBMeteoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusVMBMeteoHandler extends VelbusTemperatureSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMBMETEO));

    private static final byte RAIN_SENSOR_CHANNEL = 0x02;
    private static final byte LIGHT_SENSOR_CHANNEL = 0x04;
    private static final byte WIND_SENSOR_CHANNEL = 0x08;
    private static final byte ALL_SENSOR_CHANNELS = RAIN_SENSOR_CHANNEL | LIGHT_SENSOR_CHANNEL | WIND_SENSOR_CHANNEL;

    private ChannelUID rainfallChannel;
    private ChannelUID illuminanceChannel;
    private ChannelUID windspeedChannel;

    public VelbusVMBMeteoHandler(Thing thing) {
        super(thing, 0, new ChannelUID(thing.getUID(), "weatherStation", "CH10"));

        this.rainfallChannel = new ChannelUID(thing.getUID(), "weatherStation", "CH11");
        this.illuminanceChannel = new ChannelUID(thing.getUID(), "weatherStation", "CH12");
        this.windspeedChannel = new ChannelUID(thing.getUID(), "weatherStation", "CH13");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            logger.warn("Velbus bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        if (command instanceof RefreshType) {
            if (channelUID.equals(rainfallChannel)) {
                sendSensorReadoutRequest(velbusBridgeHandler, RAIN_SENSOR_CHANNEL);
            } else if (channelUID.equals(illuminanceChannel)) {
                sendSensorReadoutRequest(velbusBridgeHandler, LIGHT_SENSOR_CHANNEL);
            } else if (channelUID.equals(windspeedChannel)) {
                sendSensorReadoutRequest(velbusBridgeHandler, WIND_SENSOR_CHANNEL);
            }
        }
    }

    @Override
    protected void sendSensorReadoutRequest(VelbusBridgeHandler velbusBridgeHandler) {
        super.sendSensorReadoutRequest(velbusBridgeHandler);

        sendSensorReadoutRequest(velbusBridgeHandler, ALL_SENSOR_CHANNELS);
    }

    protected void sendSensorReadoutRequest(VelbusBridgeHandler velbusBridgeHandler, byte channel) {
        VelbusSensorReadoutRequestPacket packet = new VelbusSensorReadoutRequestPacket(getModuleAddress().getAddress(),
                channel);

        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    @Override
    protected int getClockAlarmAndProgramSelectionIndexInModuleStatus() {
        return 8;
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_SENSOR_RAW_DATA && packet.length >= 10) {
                byte highByteCurrentRainValue = packet[5];
                byte lowByteCurrentRainValue = packet[6];
                byte highByteCurrentLightValue = packet[7];
                byte lowByteCurrentLightValue = packet[8];
                byte highByteCurrentWindValue = packet[9];
                byte lowByteCurrentWindValue = packet[10];

                double rainValue = (((highByteCurrentRainValue & 0xff) << 8) + (lowByteCurrentRainValue & 0xff)) / 10;
                double lightValue = (((highByteCurrentLightValue & 0xff) << 8) + (lowByteCurrentLightValue & 0xff));
                double windValue = (((highByteCurrentWindValue & 0xff) << 8) + (lowByteCurrentWindValue & 0xff)) / 10;

                QuantityType<Length> rainValueState = new QuantityType<>(rainValue, MetricPrefix.MILLI(SIUnits.METRE));
                QuantityType<Illuminance> lightValueState = new QuantityType<>(lightValue, Units.LUX);
                QuantityType<Speed> windValueState = new QuantityType<>(windValue, SIUnits.KILOMETRE_PER_HOUR);

                updateState(rainfallChannel, rainValueState);
                updateState(illuminanceChannel, lightValueState);
                updateState(windspeedChannel, windValueState);
            }
        }

        return true;
    }
}
