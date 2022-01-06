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
package org.openhab.binding.plclogo.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7;
import Moka7.S7Client;

/**
 * The {@link PLCLogoClient} is thread safe LOGO! client.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCLogoClient extends S7Client {

    private static final int MAX_RETRY_NUMBER = 10;
    private final Logger logger = LoggerFactory.getLogger(PLCLogoClient.class);

    private String plcIPAddress = "INVALID_IP";

    /**
     * Connects a client to a PLC
     */
    @Override
    public synchronized int Connect() {
        return super.Connect();
    }

    /**
     * Connects a client to a PLC with specified parameters
     *
     * @param Address IP address of PLC
     * @param LocalTSAP Local TSAP for the connection
     * @param RemoteTSAP Remote TSAP for the connection
     * @return Zero on success, error code otherwise
     */
    public synchronized int Connect(String Address, int LocalTSAP, int RemoteTSAP) {
        SetConnectionParams(Address, LocalTSAP, RemoteTSAP);
        return super.Connect();
    }

    /**
     * Set connection parameters
     *
     * @param Address IP address of PLC
     * @param LocalTSAP Local TSAP for the connection
     * @param RemoteTSAP Remote TSAP for the connection
     */
    @Override
    public void SetConnectionParams(String Address, int LocalTSAP, int RemoteTSAP) {
        plcIPAddress = Address; // Store ip address for logging
        super.SetConnectionParams(Address, LocalTSAP, RemoteTSAP);
    }

    /**
     * Disconnects a client from a PLC
     */
    @Override
    public synchronized void Disconnect() {
        super.Disconnect();
    }

    /**
     * Reads a data area from a PLC
     *
     * @param Area S7 Area ID. Can be S7AreaPE, S7AreaPA, S7AreaMK, S7AreaDB, S7AreaCT or S7AreaTM
     * @param DBNumber S7 data block number
     * @param Start First position within data block read from
     * @param Amount Number of words to read
     * @param WordLength Length of single word. Can be S7WLBit, S7WLByte, S7WLCounter or S7WLTimer
     * @param Data Buffer to read into
     * @return Zero on success, error code otherwise
     */
    @Override
    public synchronized int ReadArea(int Area, int DBNumber, int Start, int Amount, int WordLength, byte[] Data) {
        if (LastError != 0) {
            logger.debug("Reconnect during read from {}: {}", plcIPAddress, ErrorText(LastError));
            Disconnect();
        }
        if (!Connected) {
            Connect();
        }

        final int packet = Math.min(Amount, 1024);
        int offset = packet;

        int retry = 0;
        int result = -1;
        do {
            // read first portion directly to data
            result = super.ReadArea(Area, DBNumber, Start, packet, WordLength, Data);
            while ((result == 0) && (offset < Amount)) {
                byte buffer[] = new byte[Math.min(Amount - offset, packet)];
                result = super.ReadArea(Area, DBNumber, offset, buffer.length, WordLength, buffer);
                System.arraycopy(buffer, 0, Data, offset, buffer.length);
                offset = offset + buffer.length;
            }

            if (retry == MAX_RETRY_NUMBER) {
                logger.info("Giving up reading from {} after {} retries.", plcIPAddress, MAX_RETRY_NUMBER);
                break;
            }

            if (result != 0) {
                logger.info("Reconnect during read from {}: {}", plcIPAddress, ErrorText(result));
                retry = retry + 1;
                Disconnect();
                Connect();
            }
        } while (result != 0);

        return result;
    }

    /**
     * Reads a data block area from a PLC
     *
     * @param DBNumber S7 data block number
     * @param Start First position within data block read from
     * @param Amount Number of words to read
     * @param WordLength Length of single word. Can be S7WLBit, S7WLByte, S7WLCounter or S7WLTimer
     * @param Data Buffer to read into
     * @return Zero on success, error code otherwise
     */
    public int readDBArea(int DBNumber, int Start, int Amount, int WordLength, byte[] Data) {
        return ReadArea(S7.S7AreaDB, DBNumber, Start, Amount, WordLength, Data);
    }

    /**
     * Writes a data area into a PLC
     *
     * @param Area S7 Area ID. Can be S7AreaPE, S7AreaPA, S7AreaMK, S7AreaDB, S7AreaCT or S7AreaTM
     * @param DBNumber S7 data block number
     * @param Start First position within data block write into
     * @param Amount Number of words to write
     * @param WordLength Length of single word. Can be S7WLBit, S7WLByte, S7WLCounter or S7WLTimer
     * @param Data Buffer to write from
     * @return Zero on success, error code otherwise
     */
    @Override
    public synchronized int WriteArea(int Area, int DBNumber, int Start, int Amount, int WordLength, byte[] Data) {
        if (LastError != 0) {
            logger.debug("Reconnect during write to {}: {}", plcIPAddress, ErrorText(LastError));
            Disconnect();
        }
        if (!Connected) {
            Connect();
        }

        int retry = 0;
        int result = -1;
        do {
            result = super.WriteArea(Area, DBNumber, Start, Amount, WordLength, Data);

            if (retry == MAX_RETRY_NUMBER) {
                logger.info("Giving up writing to {} after {} retries.", plcIPAddress, MAX_RETRY_NUMBER);
                break;
            }

            if (result != 0) {
                logger.info("Reconnect during write to {}: {}", plcIPAddress, ErrorText(result));
                retry = retry + 1;
                Disconnect();
                Connect();
            }
        } while (result != 0);

        return result;
    }

    /**
     * Writes a data block area into a PLC
     *
     * @param DBNumber S7 data block number
     * @param Start First position within data block write into
     * @param Amount Number of words to write
     * @param WordLength Length of single word. Can be S7WLBit, S7WLByte, S7WLCounter or S7WLTimer
     * @param Data Buffer to write from
     * @return Zero on success, error code otherwise
     */
    public int writeDBArea(int DBNumber, int Start, int Amount, int WordLength, byte[] Data) {
        return WriteArea(S7.S7AreaDB, DBNumber, Start, Amount, WordLength, Data);
    }

    /**
     * Returns, if client is already connected or not
     *
     * @return True, if client is connected and false otherwise
     */
    public synchronized boolean isConnected() {
        return Connected;
    }
}
