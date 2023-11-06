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
package org.openhab.binding.onewire.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.owserver.OwserverPacket;
import org.openhab.binding.onewire.internal.owserver.OwserverPacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwserverTestServer} defines a server for testing the OwserverConnection class
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwserverTestServer {
    private final Logger logger = LoggerFactory.getLogger(OwserverTestServer.class);

    private final ServerSocket serverSocket;
    private boolean isRunning = false;

    public OwserverTestServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void startServer(CompletableFuture<Boolean> serverStarted) throws IOException {
        isRunning = true;

        new Thread() {
            @Override
            public void run() {
                OwserverPacket receivedPacket;
                List<OwserverPacket> answerPackets;
                serverStarted.complete(true);
                try {
                    while (isRunning) {
                        final Socket connectionSocket = serverSocket.accept();
                        final DataInputStream inputStream = new DataInputStream(connectionSocket.getInputStream());
                        final DataOutputStream outputStream = new DataOutputStream(connectionSocket.getOutputStream());

                        receivedPacket = new OwserverPacket(inputStream, OwserverPacketType.REQUEST);
                        logger.debug("received {}", receivedPacket);

                        answerPackets = processPacket(receivedPacket);

                        answerPackets.forEach(answerPacket -> {
                            logger.debug("answering {}", answerPacket);
                            try {
                                outputStream.write(answerPacket.toBytes());
                            } catch (IOException e) {
                                logger.error("I/O Error: {}", e.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    logger.error("I/O Error: {}", e.getMessage());
                } catch (OwException e) {
                    fail("caught unexpected OwException");
                }
            }
        }.start();
    }

    public void stopServer() throws IOException {
        isRunning = false;
        serverSocket.close();
    }

    private List<OwserverPacket> processPacket(OwserverPacket inputPacket) {
        List<OwserverPacket> returnPackets = new ArrayList<>();
        OwserverPacket returnPacket = new OwserverPacket(OwserverPacketType.RETURN);
        switch (inputPacket.getMessageType()) {
            case NOP:
                returnPacket.setPayload("");
                returnPackets.add(returnPacket);
                break;
            case DIRALL:
                returnPacket.setPayload("/00.0123456789ab,/00.0123456789ac,/00.0123456789ad,/statistics");
                returnPackets.add(returnPacket);
                returnPacket = new OwserverPacket(OwserverPacketType.RETURN);
                break;
            case PRESENT:
                switch (inputPacket.getPayloadString()) {
                    case "present":
                        break;
                    default:
                        returnPacket.setReturnCode(-1);
                }
                returnPacket.setPayload(inputPacket.getPayloadString());
                returnPackets.add(returnPacket);
                break;
            case READ:
                switch (inputPacket.getPayloadString()) {
                    case "testsensor/pages/page.ALL":
                        OwPageBuffer pageBuffer = new OwPageBuffer(8);
                        pageBuffer.setByte(5, 7, (byte) 31);
                        returnPacket.setPayload(pageBuffer);
                        returnPackets.add(returnPacket);
                        break;
                    case "testsensor/decimal":
                        returnPacket.setPayload("    17.4");
                        returnPackets.add(returnPacket);
                        break;
                    case "testsensor/decimalarray":
                        returnPacket.setPayload("        3834,           0");
                        returnPackets.add(returnPacket);
                        break;
                    default:
                }
                break;
            case WRITE:
                returnPackets.add(returnPacket);
                break;
            default:

        }
        return returnPackets;
    }
}
