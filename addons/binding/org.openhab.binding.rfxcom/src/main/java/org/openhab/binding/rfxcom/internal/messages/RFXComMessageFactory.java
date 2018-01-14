/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;

/**
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComMessageFactory {

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
                    // put(PacketType.FAN, RFXComFanMessage.class);
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
                    // put(PacketType.THERMOSTAT3, RFXComThermostat3Message.class);
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
                    // put(PacketType.RFXSENSOR, RFXComRFXSensorMessage.class);
                    // put(PacketType.RFXMETER, RFXComRFXMeterMessage.class);
                    // put(PacketType.FS20, RFXComFS20Message.class);
                    // put(PacketType.IO_LINES, RFXComIOLinesMessage.class);
                }
            });

    /**
     * Command to reset RFXCOM controller.
     *
     */
    public static final byte[] CMD_RESET = new byte[] { 0x0D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00 };

    /**
     * Command to get RFXCOM controller status.
     *
     */
    public static final byte[] CMD_GET_STATUS = new byte[] { 0x0D, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00 };

    /**
     * Command to save RFXCOM controller configuration.
     *
     */
    public static final byte[] CMD_SAVE = new byte[] { 0x0D, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00 };

    /**
     * Command to start RFXCOM receiver.
     *
     */
    public static final byte[] CMD_START_RECEIVER = new byte[] { 0x0D, 0x00, 0x00, 0x03, 0x07, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00 };

    public static RFXComMessage createMessage(PacketType packetType) throws RFXComException {

        try {
            Class<? extends RFXComMessage> cl = MESSAGE_CLASSES.get(packetType);
            if (cl == null) {
                throw new RFXComMessageNotImplementedException("Message " + packetType + " not implemented");
            }
            return cl.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RFXComException(e);
        }
    }

    public static RFXComMessage createMessage(byte[] packet) throws RFXComException {
        PacketType packetType = ByteEnumUtil.fromByte(PacketType.class, (int) packet[1]);

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
