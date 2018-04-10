/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.smarthome.core.util.HexUtils;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.config.NibeHeatPumpConfiguration;
import org.openhab.binding.nibeheatpump.internal.message.MessageFactory;
import org.openhab.binding.nibeheatpump.internal.message.ModbusReadRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusWriteRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocol;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolContext;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolDefaultContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

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
    private Thread readerThread;
    private NibeHeatPumpConfiguration conf;

    private final List<byte[]> readQueue = new ArrayList<>();
    private final List<byte[]> writeQueue = new ArrayList<>();

    SerialConnector() {
        logger.debug("Nibe heatpump Serial Port message listener created");
    }

    @Override
    public void connect(NibeHeatPumpConfiguration configuration) throws NibeHeatPumpException {
        if (isConnected()) {
            return;
        }

        conf = configuration;
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(conf.serialPort);

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
        } catch (NoSuchPortException e) {
            throw new NibeHeatPumpException("Connection failed, no such a port", e);
        } catch (PortInUseException e) {
            throw new NibeHeatPumpException("Connection failed, port in use", e);
        } catch (UnsupportedCommOperationException | IOException e) {
            throw new NibeHeatPumpException("Connection failed, reason: {}" + e.getMessage(), e);
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
        serialPort.removeEventListener();
        if (readerThread != null) {
            logger.debug("Interrupt serial listener");
            readerThread.interrupt();
        }
        if (out != null) {
            logger.debug("Close serial out stream");
            IOUtils.closeQuietly(out);
        }
        if (in != null) {
            logger.debug("Close serial in stream");
            IOUtils.closeQuietly(in);
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
            IOUtils.closeQuietly(in);
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
                        byte addr = msg().get(NibeHeatPumpProtocol.OFFSET_ADR);
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
                            byte addr = msg().get(NibeHeatPumpProtocol.OFFSET_ADR);
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
                            byte addr = msg().get(NibeHeatPumpProtocol.OFFSET_ADR);
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
                    logger.debug("Error occurred during serial port read, reason: {}", e);
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
