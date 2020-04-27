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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderCommand;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IpMessagesConstants;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommunicationState}. This is enum based state machine used mostly for the logon process orchestration.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public enum CommunicationState {
    START {

        @Override
        protected CommunicationState nextState() {
            return STEP2;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            String password = communicator.getPassword();
            logger.debug("Phase {}", this);
            ParadoxIPPacket packet = new ParadoxIPPacket(password, false)
                    .setCommand(HeaderCommand.CONNECT_TO_IP_MODULE);
            sendPacket(communicator, packet);
        }

        @Override
        protected boolean isPhaseSuccess(IResponse response) {
            byte[] loginPacketResponse = response.getPayload();

            byte payloadResponseByte = loginPacketResponse[16];

            byte headerResponseByte = loginPacketResponse[4];
            switch (headerResponseByte) {
                case 0x38:
                case 0x39:
                    if (payloadResponseByte == 0x00) {
                        logger.info("Login - Login to IP150 - OK");
                        return true;
                    }
                    break;
                case 0x30:
                    logger.warn("Login - Login to IP150 failed - Incorrect password");
                    break;
                case 0x78:
                case 0x79:
                    logger.warn("Login - IP module is busy");
                    break;
            }

            switch (payloadResponseByte) {
                case 0x01:
                    logger.warn("Login - Invalid password");
                    break;
                case 0x02:
                case 0x04:
                    logger.warn("Login - User already connected");
                    break;
                default:
                    logger.warn("Login - Connection refused");
            }
            return false;
        }
    },
    STEP2 {

        @Override
        protected CommunicationState nextState() {
            return STEP3;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            logger.debug("Phase {}", this);
            ParadoxIPPacket packet = new ParadoxIPPacket(ParadoxIPPacket.EMPTY_PAYLOAD, false)
                    .setCommand(HeaderCommand.LOGIN_COMMAND1);
            sendPacket(communicator, packet);
        }
    },
    STEP3 {

        @Override
        protected CommunicationState nextState() {
            return STEP4;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            logger.debug("Phase {}", this);
            ParadoxIPPacket packet = new ParadoxIPPacket(ParadoxIPPacket.EMPTY_PAYLOAD, false)
                    .setCommand(HeaderCommand.LOGIN_COMMAND2);
            sendPacket(communicator, packet);
        }
    },
    STEP4 {

        @Override
        protected CommunicationState nextState() {
            return STEP5;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            logger.debug("Phase {}", this);
            byte[] message4 = new byte[37];
            message4[0] = 0x72;
            ParadoxIPPacket packet = new ParadoxIPPacket(message4, true)
                    .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST);
            sendPacket(communicator, packet);
        }

        @Override
        protected void receiveResponse(IParadoxInitialLoginCommunicator communicator, IResponse response) {
            byte[] receivedPacket = response.getPayload();
            if (receivedPacket != null && receivedPacket.length >= 53) {
                byte[] panelInfoBytes = Arrays.copyOfRange(receivedPacket, 16, 53);
                communicator.setPanelInfoBytes(panelInfoBytes);
                logger.debug("Phase {} completed successfully.", this);
                nextState().runPhase(communicator);
            } else {
                logger.warn("Received wrong response in phase {}. Response: {}", this, response);
                LOGOUT.runPhase(communicator);
            }
        }
    },
    STEP5 {

        @Override
        protected CommunicationState nextState() {
            return STEP6;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            logger.debug("Phase {}", this);
            ParadoxIPPacket packet = new ParadoxIPPacket(IpMessagesConstants.UNKNOWN_IP150_REQUEST_MESSAGE01, false)
                    .setCommand(HeaderCommand.SERIAL_CONNECTION_INITIATED);
            sendPacket(communicator, packet);
        }
    },
    STEP6 {

        @Override
        protected CommunicationState nextState() {
            return STEP7;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            logger.debug("Phase {}", this);
            byte[] message6 = new byte[37];
            message6[0] = 0x5F;
            message6[1] = 0x20;
            ParadoxIPPacket packet = new ParadoxIPPacket(message6, true)
                    .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST);
            sendPacket(communicator, packet);
        }

        @Override
        protected void receiveResponse(IParadoxInitialLoginCommunicator communicator, IResponse response) {
            byte[] payload = response.getPayload();
            byte[] initializationMessage = Arrays.copyOfRange(payload, 16, payload.length);
            ParadoxUtil.printPacket("Init communication sub array: ", initializationMessage);
            logger.debug("Phase {} completed successfully.", this);
            nextState().runPhase(communicator, initializationMessage);
        }
    },
    STEP7 {

        @Override
        protected CommunicationState nextState() {
            return INITIALIZE_DATA;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            if (args != null && args.length == 1) {
                byte[] initializationMessage = (byte[]) args[0];
                logger.debug("Phase {}", this);
                byte[] message7 = generateInitializationRequest(initializationMessage,
                        communicator.getPcPasswordBytes());
                ParadoxIPPacket packet = new ParadoxIPPacket(message7, true)
                        .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);
                sendPacket(communicator, packet);
            } else {
                logger.error("Error in step {}. Missing argument {}", this, args);
                throw new IllegalArgumentException(
                        "Initialization message not send in request for phase + " + this + ". Arguments= " + args);
            }
        }

        private byte[] generateInitializationRequest(byte[] initializationMessage, byte[] pcPassword) {
            byte[] message7 = new byte[] {
                    // Initialization command
                    0x00,

                    // Module address
                    initializationMessage[1],

                    // Not used
                    0x00, 0x00,

                    // Product ID
                    initializationMessage[4],

                    // Software version
                    initializationMessage[5],

                    // Software revision
                    initializationMessage[6],

                    // Software ID
                    initializationMessage[7],

                    // Module ID
                    initializationMessage[8], initializationMessage[9],

                    // PC Password
                    pcPassword[0], pcPassword[1],

                    // Modem speed
                    0x0A,

                    // Winload type ID
                    0x30,

                    // User code (for some reason Winload sends user code 021000)
                    0x02, 0x10, 0x00,

                    // Module serial number
                    initializationMessage[17], initializationMessage[18], initializationMessage[19],
                    initializationMessage[20],

                    // EVO section 3030-3038 data
                    initializationMessage[21], initializationMessage[22], initializationMessage[23],
                    initializationMessage[24], initializationMessage[25], initializationMessage[26],
                    initializationMessage[27], initializationMessage[28], initializationMessage[29],

                    // Not used
                    0x00, 0x00, 0x00, 0x00,

                    // Source ID (0x02 = Winload through IP)
                    0x02,

                    // Carrier length
                    0x00,

                    // Checksum
                    0x00 };
            return message7;
        }

        @Override
        protected void receiveResponse(IParadoxInitialLoginCommunicator communicator, IResponse response) {
            // UGLY - this is the handling of ghost packet which appears after the logon sequence
            // Read ghost packet affter 300ms then continue with normal flow
            communicator.getScheduler().schedule(() -> {
                if (communicator instanceof GenericCommunicator) {
                    try {
                        GenericCommunicator genCommunicator = (GenericCommunicator) communicator;
                        byte[] value = new byte[256];
                        int packetLength = genCommunicator.getRx().read(value);
                        logger.debug("Reading ghost packet with length={}", packetLength);
                        ParadoxUtil.printPacket("Reading ghost packet", value);
                    } catch (IOException e) {
                        logger.debug("Error reading ghost packet.", e);
                    }

                    super.receiveResponse(communicator, response);
                }
            }, 300, TimeUnit.MILLISECONDS);
        }
    },
    INITIALIZE_DATA {

        @Override
        protected CommunicationState nextState() {
            return ONLINE;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            if (communicator instanceof IParadoxCommunicator) {
                IParadoxCommunicator comm = (IParadoxCommunicator) communicator;
                comm.initializeData();
            }
            ONLINE.runPhase(communicator);
        }
    },
    ONLINE {

        @Override
        protected CommunicationState nextState() {
            return this;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            logger.debug("Phase {}. Setting communicator to status ONLINE.", this);
            communicator.setOnline(true);
            logger.info("Successfully established communication with the panel.");
        }
    },
    LOGOUT {

        @Override
        protected CommunicationState nextState() {
            return OFFLINE;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            // For some reason after sending logout packet the connection gets reset from the other end
            // currently workaround is to run directly offline phase, i.e. close socket from our end

            // logger.info("Logout packet sent to IP150.");
            // ParadoxIPPacket logoutPacket = new ParadoxIPPacket(IpMessagesConstants.LOGOUT_MESAGE_BYTES, true)
            // .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);
            // sendPacket(communicator, logoutPacket);
            nextState().runPhase(communicator);
        }
    },
    OFFLINE {

        @Override
        protected CommunicationState nextState() {
            return this;
        }

        @Override
        protected void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args) {
            communicator.close();
            communicator.setOnline(false);
        }
    };

    protected final Logger logger = LoggerFactory.getLogger(CommunicationState.class);

    private static CommunicationState currentState = CommunicationState.OFFLINE;

    // This method is the entry of logon procedure.
    public static void login(IParadoxInitialLoginCommunicator communicator) {
        START.runPhase(communicator);
    }

    public static void logout(IParadoxInitialLoginCommunicator communicator) {
        LOGOUT.runPhase(communicator);
    }

    protected abstract CommunicationState nextState();

    protected abstract void runPhase(IParadoxInitialLoginCommunicator communicator, Object... args);

    protected void runPhase(IParadoxInitialLoginCommunicator communicator) {
        setCurrentState(this);
        runPhase(communicator, new Object[0]);
    }

    protected void sendPacket(IParadoxInitialLoginCommunicator communicator, ParadoxIPPacket packet) {
        IRequest request = new LogonRequest(this, packet);
        communicator.submitRequest(request);
    }

    protected void receiveResponse(IParadoxInitialLoginCommunicator communicator, IResponse response) {
        if (isPhaseSuccess(response)) {
            logger.debug("Phase {} completed successfully.", this);
            nextState().runPhase(communicator);
        }
    }

    protected boolean isPhaseSuccess(IResponse response) {
        return true;
    }

    public static CommunicationState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(CommunicationState currentState) {
        CommunicationState.currentState = currentState;
    }
}
