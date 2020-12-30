/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.luxtronik.internal;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * With the heatpump connector the internal state of a Novelan (Siemens) Heatpump can be read.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 * @author John Cocula - made port configurable
 * @author Hilbrand Bouwkamp - Migrated to openHAB 3
 * @author Christoph Scholz - Finished migration to openHAB 3
 */
@NonNullByDefault
class HeatpumpConnector implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(HeatpumpConnector.class);

    private final DataInputStream datain;
    private final DataOutputStream dataout;
    private final Socket sock;

    /**
     * connects to the heatpump via network
     *
     * @throws UnknownHostException indicate that the IP address of a host could not be determined.
     * @throws IOException indicate that no data can be read from the heatpump
     */
    public HeatpumpConnector(String serverIp, int serverPort, int connectionTimeout)
            throws UnknownHostException, IOException {
        sock = new Socket();
        sock.connect(new InetSocketAddress(serverIp, serverPort), connectionTimeout);

        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream();
        datain = new DataInputStream(in);
        dataout = new DataOutputStream(out);
        logger.debug("Heatpump connect");
    }

    /**
     * read the parameters of the heatpump
     *
     * @return
     * @throws IOException
     */
    public int[] getParams() throws IOException {
        int[] heatpump_values = null;
        while (datain.available() > 0) {
            datain.readByte();
        }
        dataout.writeInt(3003);
        dataout.writeInt(0);
        dataout.flush();
        if (datain.readInt() != 3003) {
            return new int[0];
        }
        // int stat = datain.readInt();
        int arraylength = datain.readInt();
        heatpump_values = new int[arraylength];

        for (int i = 0; i < arraylength; i++) {
            heatpump_values[i] = datain.readInt();
        }
        return heatpump_values;
    }

    /**
     * set a parameter of the heatpump
     *
     * @param param
     * @param value
     * @return
     * @throws IOException
     */
    public boolean setParam(int param, int value) throws IOException {
        while (datain.available() > 0) {
            datain.readByte();
        }
        dataout.writeInt(3002);
        dataout.writeInt(param);
        dataout.writeInt(value);
        dataout.flush();

        int cmd = datain.readInt();
        datain.readInt();
        if (cmd != 3002) {
            logger.warn("Can't write parameter {} with value {} to heatpump.", param, value);
            return false;
        } else {
            logger.debug("Successful parameter {} with value {} to heatpump written.", param, value);
            return true;
        }
    }

    /**
     * read the internal state of the heatpump
     *
     * @return a array with all internal data of the heatpump
     * @throws IOException indicate that no data can be read from the heatpump
     */
    public int[] getValues() throws IOException {
        int[] heatpump_values = null;
        while (datain.available() > 0) {
            datain.readByte();
        }
        dataout.writeInt(3004);
        dataout.writeInt(0);
        dataout.flush();
        if (datain.readInt() != 3004) {
            return new int[0];
        }
        datain.readInt();
        int arraylength = datain.readInt();
        heatpump_values = new int[arraylength];

        for (int i = 0; i < arraylength; i++) {
            heatpump_values[i] = datain.readInt();
        }
        return heatpump_values;
    }

    /**
     * disconnect from heatpump
     */
    @Override
    public synchronized void close() {
        try {
            datain.close();
        } catch (IOException e) {
            // Eat close exception
        }
        try {
            dataout.close();
        } catch (IOException e) {
            // Eat close exception }
        }
        try {
            sock.close();
        } catch (IOException e) {
            // Eat close exception
        }
    }
}
