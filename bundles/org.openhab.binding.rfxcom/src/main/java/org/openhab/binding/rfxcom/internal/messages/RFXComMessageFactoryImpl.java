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
package org.openhab.binding.rfxcom.internal.messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;

/**
 * Factory to create RFXCom messages from either bytes delivered by the RFXCom device
 * or from openhab state to transmit.
 *
 * @author Pauli Anttila - Initial contribution
 * @author James Hewitt-Thomas - Use the enum singleton pattern to allow dependency injection
 */
public enum RFXComMessageFactoryImpl implements RFXComMessageFactory {
    INSTANCE();

    @SuppressWarnings("serial")
    private static final Map<PacketType, Class<? extends RFXComMessage>> MESSAGE_CLASSES = Collections
            .unmodifiableMap(new HashMap<PacketType, Class<? extends RFXComMessage>>() {
                {
                    put(PacketType.INTERFACE_CONTROL, RFXComInterfaceControlMessage.class);
                    put(PacketType.INTERFACE_MESSAGE, RFXComInterfaceMessage.class);
                    put(PacketType.TRANSMITTER_MESSAGE, RFXComTransmitterMessage.class);
                    put(PacketType.UNDECODED_RF_MESSAGE, RFXComUndecodedRFMessage.class);
                    put(PacketType.LIGHTING1, RFXComLighting1Message.class);
                    put(PacketType.LIGHTING2, RFXComLighting2Message.class);
                    // put(PacketType.LIGHTING3, RFXComLighting3Message.class);
                    put(PacketType.LIGHTING4, RFXComLighting4Message.class);
                    put(PacketType.LIGHTING5, RFXComLighting5Message.class);
                    put(PacketType.LIGHTING6, RFXComLighting6Message.class);
                    put(PacketType.CHIME, RFXComChimeMessage.class);
                    put(PacketType.FAN, RFXComFanMessage.class);
                    // put(PacketType.FAN_SF01, RFXComFanMessage.class);
                    // put(PacketType.FAN_ITHO, RFXComFanMessage.class);
                    // put(PacketType.FAN_SEAV, RFXComFanMessage.class);
                    put(PacketType.FAN_LUCCI_DC, RFXComFanMessage.class);
                    // put(PacketType.FAN_FT1211R, RFXComFanMessage.class);
                    put(PacketType.FAN_FALMEC, RFXComFanMessage.class);
                    put(PacketType.FAN_LUCCI_DC_II, RFXComFanMessage.class);
                    put(PacketType.FAN_NOVY, RFXComFanMessage.class);
                    put(PacketType.CURTAIN1, RFXComCurtain1Message.class);
                    put(PacketType.BLINDS1, RFXComBlinds1Message.class);
                    put(PacketType.RFY, RFXComRfyMessage.class);
                    put(PacketType.HOME_CONFORT, RFXComHomeConfortMessage.class);
                    put(PacketType.SECURITY1, RFXComSecurity1Message.class);
                    put(PacketType.SECURITY2, RFXComSecurity2Message.class);
                    // put(PacketType.CAMERA1, RFXComCamera1Message.class);
                    // put(PacketType.REMOTE_CONTROL, RFXComRemoteControlMessage.class);
                    put(PacketType.THERMOSTAT1, RFXComThermostat1Message.class);
                    // put(PacketType.THERMOSTAT2, RFXComThermostat2Message.class);
                    put(PacketType.THERMOSTAT3, RFXComThermostat3Message.class);
                    // put(PacketType.RADIATOR1, RFXComRadiator1Message.class);
                    put(PacketType.BBQ, RFXComBBQTemperatureMessage.class);
                    put(PacketType.TEMPERATURE_RAIN, RFXComTemperatureRainMessage.class);
                    put(PacketType.TEMPERATURE, RFXComTemperatureMessage.class);
                    put(PacketType.HUMIDITY, RFXComHumidityMessage.class);
                    put(PacketType.TEMPERATURE_HUMIDITY, RFXComTemperatureHumidityMessage.class);
                    // put(PacketType.BAROMETRIC, RFXComBarometricMessage.class);
                    put(PacketType.TEMPERATURE_HUMIDITY_BAROMETRIC, RFXComTemperatureHumidityBarometricMessage.class);
                    put(PacketType.RAIN, RFXComRainMessage.class);
                    put(PacketType.WIND, RFXComWindMessage.class);
                    put(PacketType.UV, RFXComUVMessage.class);
                    put(PacketType.DATE_TIME, RFXComDateTimeMessage.class);
                    put(PacketType.CURRENT, RFXComCurrentMessage.class);
                    put(PacketType.ENERGY, RFXComEnergyMessage.class);
                    put(PacketType.CURRENT_ENERGY, RFXComCurrentEnergyMessage.class);
                    // put(PacketType.POWER, RFXComPowerMessage.class);
                    // put(PacketType.WEIGHT, RFXComWeightMessage.class);
                    // put(PacketType.GAS, RFXComGasMessage.class);
                    // put(PacketType.WATER, RFXComWaterMessage.class);
                    put(PacketType.RFXSENSOR, RFXComRFXSensorMessage.class);
                    // put(PacketType.RFXMETER, RFXComRFXMeterMessage.class);
                    // put(PacketType.FS20, RFXComFS20Message.class);
                    put(PacketType.RAW, RFXComRawMessage.class);
                    // put(PacketType.IO_LINES, RFXComIOLinesMessage.class);
                }
            });

    /**
     * Create message for transmission from the packet type associated with the thing.
     */
    @Override
    public RFXComMessage createMessage(PacketType packetType, RFXComDeviceConfiguration config, ChannelUID channelUID,
            Command command) throws RFXComException {
        try {
            Class<? extends RFXComMessage> cl = MESSAGE_CLASSES.get(packetType);
            if (cl == null) {
                throw new RFXComMessageNotImplementedException("Message " + packetType + " not implemented");
            }
            RFXComMessage msg = cl.getDeclaredConstructor().newInstance();
            msg.setConfig(config);
            msg.convertFromState(channelUID.getId(), command);
            return msg;
        } catch (ReflectiveOperationException e) {
            throw new RFXComException(e);
        }
    }

    /**
     * Create message from received bytes.
     */
    @Override
    public RFXComMessage createMessage(byte[] packet) throws RFXComException {
        PacketType packetType = ByteEnumUtil.fromByte(PacketType.class, packet[1]);

        try {
            Class<? extends RFXComMessage> cl = MESSAGE_CLASSES.get(packetType);
            if (cl == null) {
                throw new RFXComMessageNotImplementedException("Message " + packetType + " not implemented");
            }
            Constructor<?> c = cl.getConstructor(byte[].class);
            return (RFXComMessage) c.newInstance(packet);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RFXComException) {
                throw (RFXComException) e.getCause();
            } else {
                throw new RFXComException(e);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new RFXComException(e);
        }
    }

    public static PacketType convertPacketType(String packetType) throws IllegalArgumentException {
        for (PacketType p : PacketType.values()) {
            if (p.toString().replace("_", "").equals(packetType.replace("_", ""))) {
                return p;
            }
        }

        throw new IllegalArgumentException("Unknown packet type " + packetType);
    }
}
