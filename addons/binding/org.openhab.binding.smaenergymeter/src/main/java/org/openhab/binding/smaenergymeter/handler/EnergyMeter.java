/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smaenergymeter.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DecimalType;

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

    public static final String DEFAULT_MCAST_GRP = "239.12.255.254";
    public static final int DEFAULT_MCAST_PORT = 9522;

    public EnergyMeter(String multicastGroup, int port) {
        this.multicastGroup = multicastGroup;
        this.port = port;

        powerIn = new FieldDTO(0x20, 4, 10);
        energyIn = new FieldDTO(0x28, 8, 3600000);
        powerOut = new FieldDTO(0x34, 4, 10);
        energyOut = new FieldDTO(0x3C, 8, 3600000);
    }

    public void update() throws IOException {
        byte[] bytes = new byte[600];
        try (MulticastSocket socket = new MulticastSocket(port)) {
            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(multicastGroup);
            socket.joinGroup(address);

            DatagramPacket msgPacket = new DatagramPacket(bytes, bytes.length);
            socket.receive(msgPacket);

            String sma = new String(Arrays.copyOfRange(bytes, 0x00, 0x03));
            if (!sma.equals("SMA")) {
                throw new IOException("Not a SMA telegram." + sma);
            }

            ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0x14, 0x18));
            serialNumber = String.valueOf(buffer.getInt());

            powerIn.updateValue(bytes);
            energyIn.updateValue(bytes);
            powerOut.updateValue(bytes);
            energyOut.updateValue(bytes);

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

}
