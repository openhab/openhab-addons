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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.config.NibeHeatPumpConfiguration;
import org.openhab.binding.nibeheatpump.internal.message.MessageFactory;
import org.openhab.binding.nibeheatpump.internal.message.ModbusDataReadOutMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusReadRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusReadResponseMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusValue;
import org.openhab.binding.nibeheatpump.internal.message.ModbusWriteRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusWriteResponseMessage;
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector for testing purposes.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SimulatorConnector extends NibeHeatPumpBaseConnector {

    private final Logger logger = LoggerFactory.getLogger(SimulatorConnector.class);

    private Thread readerThread = null;

    private final List<byte[]> readQueue = new ArrayList<>();
    private final List<byte[]> writeQueue = new ArrayList<>();

    private static final Random RANDOM = new Random();

    private final ArrayList<ModbusValue> dataReadoutValues = new ArrayList<ModbusValue>() {
        {
            add(new ModbusValue(43009, 287));
            add(new ModbusValue(43008, 100));
            add(new ModbusValue(43005, 976));
            add(new ModbusValue(40004, 30));
            add(new ModbusValue(40015, 160));
            add(new ModbusValue(40016, 120));
            add(new ModbusValue(40017, 259));
            add(new ModbusValue(40018, 283));
            add(new ModbusValue(40071, 276));
            add(new ModbusValue(40014, 454));
            add(new ModbusValue(40007, 257));
            add(new ModbusValue(47381, -80));
            add(new ModbusValue(47418, 75));
            add(new ModbusValue(45001, 0));
            add(new ModbusValue(40008, 269));
            add(new ModbusValue(40012, 231));
            add(new ModbusValue(40011, 0));
            add(new ModbusValue(0xFFFF, 0));
            add(new ModbusValue(0xFFFF, 0));
            add(new ModbusValue(0xFFFF, 0));
        }
    };

    private final Map<Integer, Integer> cache = Collections.synchronizedMap(new HashMap<>());

    public SimulatorConnector() {
        logger.debug("Nibe heatpump Test message listener created");
    }

    @Override
    public void connect(NibeHeatPumpConfiguration configuration) {
        if (isConnected()) {
            return;
        }
        readerThread = new Reader();
        readerThread.start();
        connected = true;
    }

    @Override
    public void disconnect() {
        if (readerThread != null) {
            logger.debug("Interrupt message listener");
            readerThread.interrupt();
            try {
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }

        readerThread = null;
        connected = false;
        logger.debug("Closed");
    }

    @Override
    public void sendDatagram(NibeHeatPumpMessage msg) {
        logger.debug("Sending request: {}", msg.toHexString());

        if (msg instanceof ModbusWriteRequestMessage) {
            writeQueue.add(msg.decodeMessage());
        } else if (msg instanceof ModbusReadRequestMessage) {
            readQueue.add(msg.decodeMessage());
        } else {
            logger.debug("Ignore PDU: {}", msg.getClass().toString());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Read queue: {}, Write queue: {}", readQueue.size(), writeQueue.size());
        }
    }

    private class Reader extends Thread {
        boolean interrupted = false;

        @Override
        public void interrupt() {
            logger.debug("Data listener interupt request received");
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.debug("Data listener simulator started");

            int i = 1;

            while (!interrupted) {
                try {
                    if (i++ % 60 == 0) {
                        // simulate CRC error ones a while
                        ModbusDataReadOutMessage dataReadOut = new ModbusDataReadOutMessage.MessageBuilder()
                                .values(dataReadoutValues).build();
                        byte[] data = dataReadOut.decodeMessage();
                        // create CRC error
                        data[data.length - 1] = (byte) (data[data.length - 1] + 1);
                        sendMsgToListeners(data);
                        Thread.sleep(1000);
                        continue;
                    } else if (i % 100 == 0) {
                        sendErrorToListeners("Simulated error");
                        Thread.sleep(1000);
                        continue;
                    } else if (i % 10 == 0) {
                        // ok data
                        ModbusDataReadOutMessage dataReadOut = new ModbusDataReadOutMessage.MessageBuilder()
                                .values(dataReadoutValues).build();
                        updateData();
                        updateCache();
                        sendMsgToListeners(dataReadOut.decodeMessage());
                    }

                    if (!writeQueue.isEmpty()) {
                        byte[] data = writeQueue.remove(0);
                        try {
                            ModbusWriteRequestMessage writeReq = (ModbusWriteRequestMessage) MessageFactory
                                    .getMessage(data);
                            setCacheValue(writeReq.getCoilAddress(), writeReq.getValue());
                            ModbusWriteResponseMessage writeResp = new ModbusWriteResponseMessage.MessageBuilder()
                                    .result(true).build();
                            Thread.sleep(300);
                            sendMsgToListeners(writeResp.decodeMessage());
                        } catch (NibeHeatPumpException e) {
                            logger.debug("Simulation error, cause {}", e.getMessage());
                        }
                    } else if (!readQueue.isEmpty()) {
                        byte[] data = readQueue.remove(0);
                        try {
                            ModbusReadRequestMessage readReq = (ModbusReadRequestMessage) MessageFactory
                                    .getMessage(data);
                            ModbusReadResponseMessage readResp = new ModbusReadResponseMessage.MessageBuilder()
                                    .coilAddress(readReq.getCoilAddress())
                                    .value(getCacheValue(readReq.getCoilAddress())).build();
                            Thread.sleep(200);
                            sendMsgToListeners(readResp.decodeMessage());
                        } catch (NibeHeatPumpException e) {
                            logger.debug("Simulation error, cause {}", e.getMessage());
                        }
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Read queue: {}, Write queue: {}", readQueue.size(), writeQueue.size());
                    }
                    Thread.sleep(800);

                } catch (InterruptedException e) {
                }
            }

            logger.debug("Data listener stopped");
        }
    }

    private void updateCache() {
        for (ModbusValue val : dataReadoutValues) {
            cache.put(val.getCoilAddress(), val.getValue());
        }
    }

    private int getCacheValue(int coilAddress) {
        return cache.getOrDefault(coilAddress, 0);
    }

    private void setCacheValue(int coilAddress, int value) {
        cache.put(coilAddress, value);
    }

    private void updateData() {
        for (ModbusValue val : dataReadoutValues) {
            switch (val.getCoilAddress()) {
                case 43005: // Degree Minutes
                    val.setValue(val.getValue() + 5);
                    break;
                case 40004: // BT1 Outdoor temp
                    val.setValue((int) random(-100, 100));
                    break;
                case 40015: // EB100-EP14-BT10 Brine in temp
                    val.setValue((int) random(200, 600));
                    break;
                case 40016: // EB100-EP14-BT11 Brine out temp
                    val.setValue((int) random(200, 600));
                    break;
                case 40017: // EB100-EP14-BT12 Cond. out
                    val.setValue((int) random(200, 600));
                    break;
                case 40018: // EB100-EP14-BT14 Hot gas temp
                    val.setValue((int) random(200, 600));
                    break;
                case 40071: // BT25 external supply temp
                    val.setValue((int) random(200, 600));
                    break;
            }
        }
    }

    private static double random(double min, double max) {
        return min + (RANDOM.nextDouble() * (max - min));
    }
}
