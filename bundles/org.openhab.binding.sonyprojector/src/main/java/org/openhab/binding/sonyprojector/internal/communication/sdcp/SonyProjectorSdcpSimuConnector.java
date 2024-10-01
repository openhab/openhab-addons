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
package org.openhab.binding.sonyprojector.internal.communication.sdcp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.SonyProjectorModel;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with Sony Projectors through an IP connection
 * using Pj Talk service (SDCP protocol)
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Refactoring to consider SonyProjectorConnector and add a full check of responses
 */
@NonNullByDefault
public class SonyProjectorSdcpSimuConnector extends SonyProjectorSdcpConnector {

    private final Logger logger = LoggerFactory.getLogger(SonyProjectorSdcpSimuConnector.class);

    private byte[] lastItemCode = new byte[] { 0x00, 0x00 };

    /**
     * Constructor
     *
     * @param model the projector model in use
     */
    public SonyProjectorSdcpSimuConnector(SonyProjectorModel model) {
        super("127.0.0.1", null, null, model, true);
    }

    @Override
    public synchronized void open() throws ConnectionException {
        if (!connected) {
            connected = true;
            logger.debug("Simulated SDCP connection opened");
        }
    }

    @Override
    public synchronized void close() {
        if (connected) {
            logger.debug("Simulated SDCP connection closed");
            connected = false;
        }
    }

    @Override
    protected byte[] buildMessage(byte[] itemCode, boolean getCommand, byte[] data) {
        lastItemCode = itemCode;
        return super.buildMessage(itemCode, getCommand, data);
    }

    @Override
    protected synchronized byte[] readResponse() throws CommunicationException {
        byte[] message = new byte[34];
        byte[] communityData = getCommunity().getBytes();
        message[0] = HEADER[0];
        message[1] = HEADER[1];
        message[2] = communityData[0];
        message[3] = communityData[1];
        message[4] = communityData[2];
        message[5] = communityData[3];
        message[6] = OK;
        message[7] = lastItemCode[0];
        message[8] = lastItemCode[1];
        message[9] = 2;
        message[10] = 0;
        message[11] = 1;
        logger.debug("readResponse: {}", HexUtils.bytesToHex(message));
        return message;
    }
}
