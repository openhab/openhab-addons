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
package org.openhab.binding.smaenergymeter.internal.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

import org.openhab.core.library.types.DecimalType;

/**
 * The {@link EnergyMeter} class is responsible for communication with the SMA device
 * and extracting the data fields out of the received telegrams.
 *
 * @author Osman Basha - Initial contribution
 */
public class EnergyMeter {

    private String multicastGroup;
    private int port;

    private String serialNumber;
    private Date lastUpdate;

    private final FieldDTO powerIn;
    private final FieldDTO energyIn;
    private final FieldDTO powerOut;
    private final FieldDTO energyOut;
    private final FieldDTO powerInL1;
    private final FieldDTO energyInL1;
    private final FieldDTO powerOutL1;
    private final FieldDTO energyOutL1;
    private final FieldDTO powerInL2;
    private final FieldDTO energyInL2;
    private final FieldDTO powerOutL2;
    private final FieldDTO energyOutL2;
    private final FieldDTO powerInL3;
    private final FieldDTO energyInL3;
    private final FieldDTO powerOutL3;
    private final FieldDTO energyOutL3;

    public static final String DEFAULT_MCAST_GRP = "239.12.255.254";
    public static final int DEFAULT_MCAST_PORT = 9522;

    public EnergyMeter(String multicastGroup, int port) {
        this.multicastGroup = multicastGroup;
        this.port = port;

        powerIn = new FieldDTO(0x20, 4, 10);
        energyIn = new FieldDTO(0x28, 8, 3600000);
        powerOut = new FieldDTO(0x34, 4, 10);
        energyOut = new FieldDTO(0x3C, 8, 3600000);

        powerInL1 = new FieldDTO(0xA8, 4, 10);
        energyInL1 = new FieldDTO(0xB0, 8, 3600000); // +8
        powerOutL1 = new FieldDTO(0xBC, 4, 10); // + C
        energyOutL1 = new FieldDTO(0xC4, 8, 3600000); // +8

        powerInL2 = new FieldDTO(0x138, 4, 10);
        energyInL2 = new FieldDTO(0x140, 8, 3600000); // +8
        powerOutL2 = new FieldDTO(0x14C, 4, 10); // + C
        energyOutL2 = new FieldDTO(0x154, 8, 3600000); // +8

        powerInL3 = new FieldDTO(0x1C8, 4, 10);
        energyInL3 = new FieldDTO(0x1D0, 8, 3600000); // +8
        powerOutL3 = new FieldDTO(0x1DC, 4, 10); // + C
        energyOutL3 = new FieldDTO(0x1E4, 8, 3600000); // +8
    }

    public void update() throws IOException {
        byte[] bytes = new byte[608];
        try (MulticastSocket socket = new MulticastSocket(port)) {
            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(multicastGroup);
            socket.joinGroup(address);

            DatagramPacket msgPacket = new DatagramPacket(bytes, bytes.length);
            socket.receive(msgPacket);

            String sma = new String(Arrays.copyOfRange(bytes, 0x00, 0x03));
            if (!"SMA".equals(sma)) {
                throw new IOException("Not a SMA telegram." + sma);
            }

            ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0x14, 0x18));
            serialNumber = String.valueOf(buffer.getInt());

            powerIn.updateValue(bytes);
            energyIn.updateValue(bytes);
            powerOut.updateValue(bytes);
            energyOut.updateValue(bytes);

            powerInL1.updateValue(bytes);
            energyInL1.updateValue(bytes);
            powerOutL1.updateValue(bytes);
            energyOutL1.updateValue(bytes);

            powerInL2.updateValue(bytes);
            energyInL2.updateValue(bytes);
            powerOutL2.updateValue(bytes);
            energyOutL2.updateValue(bytes);

            powerInL3.updateValue(bytes);
            energyInL3.updateValue(bytes);
            powerOutL3.updateValue(bytes);
            energyOutL3.updateValue(bytes);

            lastUpdate = new Date(System.currentTimeMillis());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public DecimalType getPowerIn() {
        return new DecimalType(powerIn.getValue());
    }

    public DecimalType getPowerOut() {
        return new DecimalType(powerOut.getValue());
    }

    public DecimalType getEnergyIn() {
        return new DecimalType(energyIn.getValue());
    }

    public DecimalType getEnergyOut() {
        return new DecimalType(energyOut.getValue());
    }

    public DecimalType getPowerInL1() {
        return new DecimalType(powerInL1.getValue());
    }

    public DecimalType getPowerOutL1() {
        return new DecimalType(powerOutL1.getValue());
    }

    public DecimalType getEnergyInL1() {
        return new DecimalType(energyInL1.getValue());
    }

    public DecimalType getEnergyOutL1() {
        return new DecimalType(energyOutL1.getValue());
    }

    public DecimalType getPowerInL2() {
        return new DecimalType(powerInL2.getValue());
    }

    public DecimalType getPowerOutL2() {
        return new DecimalType(powerOutL2.getValue());
    }

    public DecimalType getEnergyInL2() {
        return new DecimalType(energyInL2.getValue());
    }

    public DecimalType getEnergyOutL2() {
        return new DecimalType(energyOutL2.getValue());
    }

    public DecimalType getPowerInL3() {
        return new DecimalType(powerInL3.getValue());
    }

    public DecimalType getPowerOutL3() {
        return new DecimalType(powerOutL3.getValue());
    }

    public DecimalType getEnergyInL3() {
        return new DecimalType(energyInL3.getValue());
    }

    public DecimalType getEnergyOutL3() {
        return new DecimalType(energyOutL3.getValue());
    }
}
