/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.protocol;

import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * The {@link NibeHeatPumpProtocolStates} implements Nibe heat pump protocol state machine states.
 *
 * @author Pauli Anttila - Initial contribution
 */
public enum NibeHeatPumpProtocolStates implements NibeHeatPumpProtocolState {

    WAIT_START {
        @Override
        public boolean process(NibeHeatPumpProtocolContext context) {
            if (context.buffer().hasRemaining()) {
                byte b = context.buffer().get();
                context.log("Received byte: {}", String.format("%02X", b));
                if (b == NibeHeatPumpProtocol.FRAME_START_CHAR_FROM_NIBE) {
                    context.log("Frame start found");
                    context.msg().clear();
                    context.msg().put(b);
                    context.state(WAIT_DATA);
                }
                return true;
            }
            return false;
        }
    },
    WAIT_DATA {
        @Override
        public boolean process(NibeHeatPumpProtocolContext context) {
            if (context.buffer().hasRemaining()) {
                if (context.msg().position() >= 100) {
                    context.log("Too long message received, rewait start char");
                    context.state(WAIT_START);
                } else {
                    byte b = context.buffer().get();
                    context.log("Received byte: {}", String.format("%02X", b));
                    context.msg().put(b);

                    try {
                        msgStatus status = checkNibeMessage(context.msg().asReadOnlyBuffer());
                        switch (status) {
                            case INVALID:
                                context.state(WAIT_START);
                                break;
                            case VALID:
                                context.state(OK_MESSAGE_RECEIVED);
                                break;
                            case VALID_BUT_NOT_READY:
                                break;
                        }
                    } catch (NibeHeatPumpException e) {
                        context.log(e.getMessage());
                        context.state(CHECKSUM_FAILURE);
                    }
                }
                return true;
            }
            return false;
        }

    },
    OK_MESSAGE_RECEIVED {
        @Override
        public boolean process(NibeHeatPumpProtocolContext context) {
            context.msg().flip();
            byte[] data = new byte[context.msg().remaining()];
            context.msg().get(data, 0, data.length);
            context.log("Received data (len={}): {}", data.length, DatatypeConverter.printHexBinary(data));
            if (NibeHeatPumpProtocol.isModbus40ReadTokenPdu(data)) {
                context.state(READ_TOKEN_RECEIVED);
            } else if (NibeHeatPumpProtocol.isModbus40WriteTokenPdu(data)) {
                context.state(WRITE_TOKEN_RECEIVED);
            } else {
                context.sendAck();
                context.msgReceived(data);
                context.state(WAIT_START);
            }
            return true;
        }

    },
    WRITE_TOKEN_RECEIVED {
        @Override
        public boolean process(NibeHeatPumpProtocolContext context) {
            context.log("Write token received");
            context.sendWriteMsg();
            context.state(WAIT_START);
            return true;
        }

    },
    READ_TOKEN_RECEIVED {
        @Override
        public boolean process(NibeHeatPumpProtocolContext context) {
            context.log("Read token received");
            context.sendReadMsg();
            context.state(WAIT_START);
            return true;
        }

    },
    CHECKSUM_FAILURE {
        @Override
        public boolean process(NibeHeatPumpProtocolContext context) {
            context.log("CRC failure");
            context.sendNak();
            context.state(WAIT_START);
            return true;
        }
    };

    private static enum msgStatus {
        VALID,
        VALID_BUT_NOT_READY,
        INVALID
    }

    /*
     * Throws NibeHeatPumpException when checksum fails
     */
    private static msgStatus checkNibeMessage(ByteBuffer byteBuffer) throws NibeHeatPumpException {
        byteBuffer.flip();
        int len = byteBuffer.remaining();

        if (len >= 1) {
            if (byteBuffer.get(0) != NibeHeatPumpProtocol.FRAME_START_CHAR_FROM_NIBE) {
                return msgStatus.INVALID;
            }

            if (len >= 2) {
                if (!(byteBuffer.get(1) == 0x00)) {
                    return msgStatus.INVALID;
                }
            }

            if (len >= 6) {
                int datalen = byteBuffer.get(NibeHeatPumpProtocol.OFFSET_LEN);

                // check if all bytes received
                if (len < datalen + 6) {
                    return msgStatus.VALID_BUT_NOT_READY;
                }

                // calculate XOR checksum
                byte calcChecksum = 0;
                for (int i = 2; i < (datalen + 5); i++) {
                    calcChecksum ^= byteBuffer.get(i);
                }

                byte msgChecksum = byteBuffer.get(datalen + 5);

                if (calcChecksum != msgChecksum) {

                    // if checksum is 0x5C (start character), heat pump seems to
                    // send 0xC5 checksum
                    if (calcChecksum != 0x5C && msgChecksum != 0xC5) {
                        throw new NibeHeatPumpException(String.format(
                                "Checksum failure, expected checksum 0x%02X was 0x%02X", msgChecksum, calcChecksum));
                    }
                }

                return msgStatus.VALID;
            }
        }

        return msgStatus.VALID_BUT_NOT_READY;
    }
};
