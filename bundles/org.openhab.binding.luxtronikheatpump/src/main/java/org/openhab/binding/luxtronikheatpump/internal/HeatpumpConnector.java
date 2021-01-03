/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luxtronikheatpump.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeatpumpConnector} reads / writes internal states of a Heat pump with Luxtronik control.
 *
 * Based on HeatpumpConnector class of novelanheatpump binding
 * 
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
public class HeatpumpConnector {

    static final Logger logger = LoggerFactory.getLogger(HeatpumpConnector.class);

    private String serverIp;
    private int serverPort;
    private Integer[] heatpumpValues = new Integer[0];
    private Integer[] heatpumpParams = new Integer[0];
    private Integer[] heatpumpVisibilities = new Integer[0];

    public HeatpumpConnector(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * reads all values from the heatpump via network
     * 
     * @throws UnknownHostException indicate that the IP address of a host could not be determined.
     * @throws IOException indicate that no data can be read from the heat pump
     */
    public void read() throws UnknownHostException, IOException {
        try (Socket sock = new Socket(serverIp, serverPort)) {
            InputStream in = sock.getInputStream(); // @SuppressWarnings
            OutputStream out = sock.getOutputStream();
            DataInputStream datain = new DataInputStream(in);
            DataOutputStream dataout = new DataOutputStream(out);

            heatpumpValues = readInt(datain, dataout, 3004);

            // workaround for thermal energies
            // the thermal energies can be unreasonably high in some cases, probably due to a sign bug in the firmware
            // trying to correct this issue here
            if (heatpumpValues[151] >= 214748364)
                heatpumpValues[151] -= 214748364;
            if (heatpumpValues[152] >= 214748364)
                heatpumpValues[152] -= 214748364;
            if (heatpumpValues[153] >= 214748364)
                heatpumpValues[153] -= 214748364;
            if (heatpumpValues[154] >= 214748364)
                heatpumpValues[154] -= 214748364;

            heatpumpParams = readInt(datain, dataout, 3003);
            heatpumpVisibilities = readInt(datain, dataout, 3005);

            datain.close();
            dataout.close();
        }
    }

    /**
     * read the parameters of the heat pump
     */
    public Integer[] getParams() {
        return heatpumpParams;
    }

    /**
     * set a parameter of the heat pump
     * 
     * @param param
     * @param value
     * @throws IOException indicate that no data can be sent to the heat pump
     */
    public Boolean setParam(int param, int value) throws IOException {
        try (Socket sock = new Socket(serverIp, serverPort)) {
            InputStream in = sock.getInputStream(); // @SuppressWarnings
            OutputStream out = sock.getOutputStream();
            DataInputStream datain = new DataInputStream(in);
            DataOutputStream dataout = new DataOutputStream(out);

            while (datain.available() > 0) {
                datain.readByte();
            }
            dataout.writeInt(3002);
            dataout.writeInt(param);
            dataout.writeInt(value);
            dataout.flush();

            int cmd = datain.readInt();
            datain.readInt();

            datain.close();
            dataout.close();

            if (cmd != 3002) {
                logger.warn("Can't write parameter {} with value {} to heat pump.", param, value);
                return false;
            } else {
                logger.debug("Successful parameter {} with value {} to heatpump written.", param, value);
                return true;
            }
        }
    }

    /**
     * Returns the internal states of the heat pump
     * 
     * @return a array with all internal data of the heat pump
     */
    public Integer[] getValues() {
        return heatpumpValues;
    }

    /**
     * Returns the internal visibilities of the heat pump
     *
     * @return a array with all internal visibilities of the heat pump
     */
    public Integer[] getVisibilities() {
        return heatpumpVisibilities;
    }

    /**
     * Reads all available parameters for the given value from socket
     *
     * @param value int value to read from socket
     * @return an array with all values returned from heat pump socket
     * @throws IOException indicate that no data can be read from the heat pump
     */
    private Integer[] readInt(DataInputStream datain, DataOutputStream dataout, int value) throws IOException {
        Integer[] result = null;
        while (datain.available() > 0) {
            datain.readByte();
        }
        dataout.writeInt(value);
        dataout.writeInt(0);
        dataout.flush();
        if (datain.readInt() != value) {
            return new Integer[0];
        }

        if (value == 3004) {
            datain.readInt();
        }

        int arraylength = datain.readInt();

        logger.debug("Found {} values for {}", arraylength, value);

        result = new Integer[arraylength];

        if (value == 3005) {
            byte[] data = datain.readNBytes(arraylength);
            for (int i = 0; i < data.length; i++) {
                result[i] = (int) data[i];
            }
            return result;
        }

        for (int i = 0; i < arraylength; i++) {
            result[i] = datain.readInt();
        }

        return result;
    }
}
