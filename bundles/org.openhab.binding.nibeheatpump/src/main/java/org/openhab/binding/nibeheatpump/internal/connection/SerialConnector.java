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
package org.openhab.binding.nibeheatpump.internal.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.config.NibeHeatPumpConfiguration;
import org.openhab.binding.nibeheatpump.internal.message.MessageFactory;
import org.openhab.binding.nibeheatpump.internal.message.ModbusReadRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusWriteRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocol;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolContext;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolDefaultContext;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector for serial port communication.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SerialConnector extends NibeHeatPumpBaseConnector {

    private final Logger logger = LoggerFactory.getLogger(SerialConnector.class);

    private InputStream in;
    private OutputStream out;
    private SerialPort serialPort;
    private final SerialPortManager serialPortManager;
    private Thread readerThread;
    private NibeHeatPumpConfiguration conf;

    private final List<byte[]> readQueue = new ArrayList<>();
    private final List<byte[]> writeQueue = new ArrayList<>();

    public SerialConnector(SerialPortManager serialPortManager) {
        logger.debug("Nibe heatpump Serial Port message listener created");
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void connect(NibeHeatPumpConfiguration configuration) throws NibeHeatPumpException {
        if (isConnected()) {
            return;
        }

        conf = configuration;
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(conf.serialPort);
            if (portIdentifier == null) {
                throw new NibeHeatPumpException("Connection failed, no such port: " + conf.serialPort);
            }

            serialPort = portIdentifier.open(this.getClass().getName(), 2000);
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            serialPort.enableReceiveThreshold(1);
            serialPort.disableReceiveTimeout();

            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();

            out.flush();
            if (in.markSupported()) {
                in.reset();
            }
        } catch (PortInUseException e) {
            throw new NibeHeatPumpException("Connection failed, port in use: " + conf.serialPort, e);
        } catch (UnsupportedCommOperationException | IOException e) {
            throw new NibeHeatPumpException("Connection failed, reason: " + e.getMessage(), e);
        }

        readQueue.clear();
        writeQueue.clear();

        readerThread = new SerialReader(in);
        readerThread.start();
        connected = true;
    }

    @Override
    public void disconnect() {
        logger.debug("Disconnecting");
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        if (readerThread != null) {
            logger.debug("Interrupt serial listener");
            readerThread.interrupt();
        }
        if (out != null) {
            logger.debug("Close serial out stream");
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
        }
        if (in != null) {
            logger.debug("Close serial in stream");
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
        }
        if (serialPort != null) {
            logger.debug("Close serial port");
            serialPort.close();
        }
        readerThread = null;
        serialPort = null;
        out = null;
        in = null;
        connected = false;
        logger.debug("Closed");
    }

    @Override
    public void sendDatagram(NibeHeatPumpMessage msg) {
        if (logger.isTraceEnabled()) {
            logger.trace("Add request to queue: {}", msg.toHexString());
        }

        if (msg instanceof ModbusWriteRequestMessage) {
            writeQueue.add(msg.decodeMessage());
        } else if (msg instanceof ModbusReadRequestMessage) {
            readQueue.add(msg.decodeMessage());
        } else {
            logger.trace("Ignore PDU: {}", msg.getClass());
        }

        logger.trace("Read queue: {}, Write queue: {}", readQueue.size(), writeQueue.size());
    }

    public class SerialReader extends Thread implements SerialPortEventListener {
        boolean interrupted = false;
        final InputStream in;

        SerialReader(InputStream in) {
            this.in = in;
        }

        @Override
        public void interrupt() {
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.debug("Data listener started");

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            try {
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
            } catch (TooManyListenersException e) {
                logger.info("RXTX high CPU load workaround failed, reason {}", e.getMessage());
            }

            NibeHeatPumpProtocolContext context = new NibeHeatPumpProtocolDefaultContext() {
                @Override
                public void sendAck() {
                    try {
                        byte addr = msg().get(NibeHeatPumpProtocol.RES_OFFS_ADR);
                        sendAckToNibe(addr);
                    } catch (IOException e) {
                        sendErrorToListeners(e.getMessage());
                    }
                }

                @Override
                public void sendNak() {
                    // do nothing
                }

                @Override
                public void msgReceived(byte[] data) {
                    sendMsgToListeners(data);
                }

                @Override
                public void sendWriteMsg() {
                    try {
                        if (!writeQueue.isEmpty()) {
                            sendDataToNibe(writeQueue.remove(0));
                        } else {
                            // no messages to send, send ack to pump
                            byte addr = msg().get(NibeHeatPumpProtocol.RES_OFFS_ADR);
                            sendAckToNibe(addr);
                        }
                    } catch (IOException e) {
                        sendErrorToListeners(e.getMessage());
                    }
                }

                @Override
                public void sendReadMsg() {
                    try {
                        if (!readQueue.isEmpty()) {
                            sendDataToNibe(readQueue.remove(0));
                        } else {
                            // no messages to send, send ack to pump
                            byte addr = msg().get(NibeHeatPumpProtocol.RES_OFFS_ADR);
                            sendAckToNibe(addr);
                        }
                    } catch (IOException e) {
                        sendErrorToListeners(e.getMessage());
                    }
                }
            };

            while (!interrupted) {
                try {
                    final byte[] data = getAllAvailableBytes(in);
                    if (data != null) {
                        context.buffer().put(data);

                        // flip buffer for reading
                        context.buffer().flip();
                    }
                } catch (InterruptedIOException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("Interrupted via InterruptedIOException");
                } catch (IOException e) {
                    logger.error("Reading from serial port failed", e);
                    sendErrorToListeners(e.getMessage());
                } catch (Exception e) {
                    logger.debug("Error occurred during serial port read", e);
                }

                // run state machine to process all received data
                while (context.state().process(context)) {
                    if (interrupted) {
                        break;
                    }
                }

                // all bytes should be handled, clear buffer for next round
                context.buffer().clear();
            }

            logger.debug("Data listener stopped");
        }

        private byte[] getAllAvailableBytes(InputStream in) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            int b;
            // wait first byte (blocking)
            if ((b = in.read()) > -1) {
                byte d[] = new byte[] { (byte) b };
                os.write(d);

                // read rest of the available bytes
                final int bufferLen = 100;
                byte[] buffer = new byte[bufferLen];
                int available = in.available();
                if (available > 0) {
                    int len = in.read(buffer, 0, bufferLen);
                    if (len > -1) {
                        os.write(buffer, 0, len);
                    }
                }

                os.flush();
                return os.toByteArray();
            }

            return null;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            try {
                /*
                 * See more details from
                 * https://github.com/NeuronRobotics/nrjavaserial/issues/22
                 */
                logger.trace("RXTX library CPU load workaround, sleep forever");
                sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
            }
        }
    }

    @SuppressWarnings("unused")
    private void sendNakToNibe() throws IOException {
        logger.trace("Send data (len=1): 15");
        out.write(0x15);
        out.flush();
    }

    private void sendAckToNibe(byte address) throws IOException {
        boolean sendack = false;

        if (address == NibeHeatPumpProtocol.ADR_MODBUS40 && conf.sendAckToMODBUS40) {
            logger.debug("Send ack to MODBUS40 message");
            sendack = true;
        } else if (address == NibeHeatPumpProtocol.ADR_SMS40 && conf.sendAckToSMS40) {
            logger.debug("Send ack to SMS40 message");
            sendack = true;
        } else if (address == NibeHeatPumpProtocol.ADR_RMU40 && conf.sendAckToRMU40) {
            logger.debug("Send ack to RMU40 message");
            sendack = true;
        }

        if (sendack) {
            sendAckToNibe();
        }
    }

    private void sendAckToNibe() throws IOException {
        logger.trace("Send data (len=1): 06");
        out.write(0x06);
        out.flush();
    }

    private void sendDataToNibe(byte[] data) throws IOException {
        if (logger.isTraceEnabled()) {
            try {
                NibeHeatPumpMessage msg = MessageFactory.getMessage(data);
                logger.trace("Sending msg: {}", msg);
            } catch (NibeHeatPumpException e) {
                // do nothing
            }
            logger.trace("Sending data (len={}): {}", data.length, HexUtils.bytesToHex(data));
        }
        out.write(data);
        out.flush();
    }
}
