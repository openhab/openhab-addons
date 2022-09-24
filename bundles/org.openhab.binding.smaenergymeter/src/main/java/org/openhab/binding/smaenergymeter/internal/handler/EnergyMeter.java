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
package org.openhab.binding.smaenergymeter.internal.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.*;

import org.openhab.binding.smaenergymeter.internal.SMAEnergyMeterBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergyMeter} class is responsible for communication with the SMA device
 * and extracting the data fields out of the received telegrams.
 *
 * @author Osman Basha - Initial contribution
 */
public class EnergyMeter {
    public static final String DEFAULT_MCAST_GRP = "239.12.255.254";
    public static final int DEFAULT_MCAST_PORT = 9522;
    private final Logger logger = LoggerFactory.getLogger(EnergyMeter.class);
    private final String multicastGroup;
    private final int port;
    private String serialNumber;
    private Date lastUpdate;

    public EnergyMeter(String multicastGroup, int port) {
        this.multicastGroup = multicastGroup;
        this.port = port;
    }

    private static int getIntegerFromByteArray(byte[] bytes, int from, int to) {
        return ByteBuffer.wrap(Arrays.copyOfRange(bytes, from, to)).getInt();
    }

    public List<EnergyMeterData> update() throws IOException {
        byte[] bytes = new byte[608];
        List<EnergyMeterData> result = new ArrayList<>();
        try (MulticastSocket socket = new MulticastSocket(port)) {
            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(multicastGroup);
            socket.joinGroup(address);

            DatagramPacket msgPacket = new DatagramPacket(bytes, bytes.length);
            socket.receive(msgPacket);

            String sma = new String(Arrays.copyOfRange(bytes, 0x00, 0x03));
            if (!sma.equals("SMA")) {
                throw new IOException("Not a SMA telegram: " + sma);
            }

            int dataLength = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 12, 14)).getShort() + 16;

            if (dataLength != 54) {
                ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 20, 24));
                serialNumber = String.valueOf(buffer.getInt() & 0xFFFFFFFFL);

                logger.debug("Telegram received with date: ");
                int dataPosition = 28;
                while (dataPosition < dataLength) {
                    int headerStartPosition = dataPosition;
                    int headerEndPosition = headerStartPosition + SMAEnergyMeterBindingConstants.DATA_HEADER_SIZE;

                    byte[] valueHeader = Arrays.copyOfRange(bytes, headerStartPosition, headerEndPosition);
                    EnergyMeterData energyMeterData = decodeData(valueHeader);
                    if (energyMeterData.datatype == ValueType.UNKNOWN) {
                        logger.debug("No valid header at {} will stop reading data", dataPosition);
                        break;
                    }
                    int dataEndPosition = headerEndPosition + energyMeterData.datatype.getDataSize();
                    energyMeterData.rawValue = getIntegerFromByteArray(bytes, headerEndPosition, dataEndPosition);
                    dataPosition = dataEndPosition;
                    logger.debug("Data read {}", energyMeterData);
                    result.add(energyMeterData);
                }
            }
            lastUpdate = new Date(System.currentTimeMillis());
        } catch (Exception e) {
            throw new IOException(e);
        }
        return result;
    }

    private EnergyMeterData decodeData(byte[] valueHeader) {
        EnergyMeterData energyMeterData = new EnergyMeterData();
        int rawType = ByteBuffer.wrap(Arrays.copyOfRange(valueHeader, 2, 3)).get();
        short channelNumberShort = ByteBuffer.wrap(Arrays.copyOfRange(valueHeader, 0, 2)).getShort();
        int channelNumber = Short.toUnsignedInt(channelNumberShort);
        EnergyMeterChannel measuredUnit = SMAEnergyMeterBindingConstants.getEnergyMeterValueForChannel(channelNumber);
        if (measuredUnit == null) {
            logger.debug("Not able to identify an energy meter value for {}", channelNumber);
        } else if (rawType == 0 && measuredUnit.getChannel() == 36864) {
            energyMeterData.datatype = ValueType.VERSION;
        } else if (rawType == 4) {
            energyMeterData.datatype = ValueType.CURRENT;
        } else if (rawType == 8) {
            energyMeterData.datatype = ValueType.TOTAL;
        } else {
            energyMeterData.datatype = ValueType.UNKNOWN;
            logger.debug("unknown datatype: measurement {} datatype {} raw_type {}", energyMeterData.channelNo,
                    energyMeterData.datatype, rawType);
        }
        energyMeterData.energyMeterChannel = measuredUnit;
        return energyMeterData;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
}
