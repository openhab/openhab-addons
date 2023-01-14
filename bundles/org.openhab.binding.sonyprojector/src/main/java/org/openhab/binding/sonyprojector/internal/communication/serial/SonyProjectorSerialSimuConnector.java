/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sonyprojector.internal.communication.serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.SonyProjectorModel;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with Sony Projectors through a serial connection
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonyProjectorSerialSimuConnector extends SonyProjectorSerialConnector {

    private final Logger logger = LoggerFactory.getLogger(SonyProjectorSerialSimuConnector.class);

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param model the projector model in use
     */
    public SonyProjectorSerialSimuConnector(SerialPortManager serialPortManager, SonyProjectorModel model) {
        super(serialPortManager, "dummyPort", model, true);
    }

    @Override
    public synchronized void open() throws ConnectionException {
        if (!connected) {
            connected = true;
            logger.debug("Simulated serial connection opened");
        }
    }

    @Override
    public synchronized void close() {
        if (connected) {
            logger.debug("Simulated serial connection closed");
            connected = false;
        }
    }

    @Override
    protected synchronized byte[] readResponse() throws CommunicationException {
        byte[] message = new byte[8];
        message[0] = START_CODE;
        message[1] = SonyProjectorSerialError.COMPLETE.getDataCode()[0];
        message[2] = SonyProjectorSerialError.COMPLETE.getDataCode()[1];
        message[3] = TYPE_ACK;
        message[4] = 0x00;
        message[5] = 0x01;
        message[6] = computeCheckSum(message);
        message[7] = END_CODE;
        logger.debug("readResponse: {}", HexUtils.bytesToHex(message));
        return message;
    }

    private byte computeCheckSum(byte[] message) {
        byte result = 0;
        for (int i = 1; i <= 5; i++) {
            result |= (message[i] & 0x000000FF);
        }
        return result;
    }
}
