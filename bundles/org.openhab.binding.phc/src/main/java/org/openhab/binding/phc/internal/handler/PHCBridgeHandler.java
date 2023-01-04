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
package org.openhab.binding.phc.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.phc.internal.PHCBindingConstants;
import org.openhab.binding.phc.internal.PHCHelper;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PHCBridgeHandler} is responsible for handling the serial Communication to and from the PHC Modules.
 *
 * @author Jonas Hohaus - Initial contribution
 */
@NonNullByDefault
public class PHCBridgeHandler extends BaseBridgeHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(PHCBridgeHandler.class);

    private static final int BAUD = 19200;
    private static final int SEND_RETRY_COUNT = 20; // max count to send the same message
    private static final int SEND_RETRY_TIME_MILLIS = 60; // time to wait for an acknowledge before send the message
                                                          // again in milliseconds

    private @Nullable InputStream serialIn;
    private @Nullable OutputStream serialOut;
    private @Nullable SerialPort commPort;
    private final SerialPortManager serialPortManager;

    private final Map<Byte, Boolean> toggleMap = new HashMap<>();
    private final InternalBuffer buffer = new InternalBuffer();
    private final BlockingQueue<QueueObject> receiveQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<QueueObject> sendQueue = new LinkedBlockingQueue<>();
    private final ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(3);

    private final byte emLedOutputState[] = new byte[32];
    private final byte amOutputState[] = new byte[32];
    private final byte dmOutputState[] = new byte[32];

    private final List<Byte> modules = new ArrayList<>();

    public PHCBridgeHandler(Bridge phcBridge, SerialPortManager serialPortManager) {
        super(phcBridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        String port = ((String) getConfig().get(PHCBindingConstants.PORT));

        // find the given port
        SerialPortIdentifier portId = serialPortManager.getIdentifier(port);

        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Serial port '" + port + "' could not be found.");
            return;
        }

        try {
            // initialize serial port
            SerialPort serialPort = portId.open(this.getClass().getName(), 2000); // owner, timeout
            serialIn = serialPort.getInputStream();
            // set port parameters
            serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            serialPort.addEventListener(this);
            // activate the DATA_AVAILABLE notifier
            serialPort.notifyOnDataAvailable(true);

            // get the output stream
            serialOut = serialPort.getOutputStream();

            commPort = serialPort;

            sendPorBroadcast();

            byte[] b = { 0x01 };
            for (int j = 0; j <= 0x1F; j++) {
                serialWrite(buildMessage((byte) j, 0, b, false));
            }
            updateStatus(ThingStatus.ONLINE);

            // receive messages
            threadPoolExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    processReceivedBytes();
                }
            });

            // process received messages
            threadPoolExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    processReceiveQueue();
                }
            });

            // sendig commands to the modules
            threadPoolExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    processSendQueue();
                }
            });
        } catch (PortInUseException | TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not open serial port " + port + ": " + e.getMessage());
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not configure serial port " + port + ": " + e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to get input or output stream for serialPort: " + e.getMessage());
            logger.debug("Failed to get inputstream for serialPort", e);
        }
    }

    /**
     * Reads the data on serial port and puts it into the internal buffer.
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE && serialIn != null) {
            try {
                byte[] bytes = new byte[serialIn.available()];
                serialIn.read(bytes);

                buffer.offer(bytes);

                if (logger.isTraceEnabled()) {
                    logger.trace("buffer offered {}", HexUtils.bytesToHex(bytes, " "));
                }
            } catch (IOException e) {
                logger.warn("Error on reading input stream to internal buffer", e);
            }
        }
    }

    /**
     * process internal incoming buffer (recognize on read messages)
     */
    private void processReceivedBytes() {
        int faultCounter = 0;

        try {
            byte module = buffer.get();

            while (true) {
                // Recognition of messages from byte buffer.
                // not a known module address
                if (!modules.contains(module)) {
                    module = buffer.get();
                    continue;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("get module: {}", new String(HexUtils.byteToHex(module)));
                }

                byte sizeToggle = buffer.get();

                // read length of command and check if makes sense
                int size = (sizeToggle & 0x7F);

                if (!isSizeToggleValid(sizeToggle, module)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("get invalid sizeToggle: {}", new String(HexUtils.byteToHex(sizeToggle)));
                    }

                    module = sizeToggle;
                    continue;
                }

                // read toggle, size and command
                boolean toggle = (sizeToggle & 0x80) == 0x80;

                logger.debug("get toggle: {}", toggle);

                byte[] command = new byte[size];

                for (int i = 0; i < size; i++) {
                    command[i] = buffer.get();
                }

                // log command
                if (logger.isTraceEnabled()) {
                    logger.trace("command read: {}", PHCHelper.bytesToBinaryString(command));
                }

                // read crc
                byte crcByte1 = buffer.get();
                byte crcByte2 = buffer.get();

                short crc = (short) (crcByte1 & 0xFF);
                crc |= (crcByte2 << 8);

                // calculate checkCrc
                short checkCrc = calcCrc(module, sizeToggle, command);

                // check crc
                if (crc != checkCrc) {
                    logger.debug("CRC not correct (crc from message, calculated crc): {}, {}", crc, checkCrc);

                    faultCounter = handleCrcFault(faultCounter);

                    module = buffer.get();
                    continue;
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("get crc: {}", HexUtils.bytesToHex(new byte[] { crcByte1, crcByte2 }, " "));
                }

                faultCounter = 0;

                processReceivedMsg(module, toggle, command);
                module = buffer.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isSizeToggleValid(byte sizeToggle, byte module) {
        int unsigned = sizeToggle & 0xFF;

        if (unsigned > 0 && unsigned < 4) {
            return true;
        } else if (unsigned > 0x80 && unsigned < 0x84) {
            return true;
        } else if ((module & 0xE0) == 0x00) {
            if (unsigned > 0 && unsigned < 16) {
                return true;
            } else if (unsigned > 0x80 && unsigned < 0x90) {
                return true;
            }
        }

        return false;
    }

    private int handleCrcFault(int faultCounter) throws InterruptedException {
        if (faultCounter > 0) {
            // Normally in this case we read the message repeatedly offset to the real -> skip one to 6 bytes
            for (int i = 0; i < faultCounter; i++) {
                if (buffer.hasNext()) {
                    buffer.get();
                }
            }
        }

        int resCounter = faultCounter + 1;
        if (resCounter > 6) {
            resCounter = 0;
        }
        return resCounter;
    }

    private void processReceivedMsg(byte module, boolean toggle, byte[] command) {
        // Acknowledgement received (command first byte 0)
        if (command[0] == 0) {
            String moduleType;
            byte channel = 0; // only needed for dim
            if ((module & 0xE0) == 0x40) {
                moduleType = PHCBindingConstants.CHANNELS_AM;
            } else if ((module & 0xE0) == 0xA0) {
                moduleType = PHCBindingConstants.CHANNELS_DIM;
                channel = (byte) ((command[0] >>> 5) & 0x0F);
            } else {
                moduleType = PHCBindingConstants.CHANNELS_EM_LED;
            }

            setModuleOutputState(moduleType, (byte) (module & 0x1F), command[1], channel);
            toggleMap.put(module, !toggle);

            // initialization (first byte FF)
        } else if (command[0] == (byte) 0xFF) {
            if ((module & 0xE0) == 0x00) { // EM
                sendEmConfig(module);
            } else if ((module & 0xE0) == 0x40 || (module & 0xE0) == 0xA0) { // AM, JRM and DIM
                sendAmConfig(module);
            }

            logger.debug("initialization: {}", module);

            // ignored - ping (first byte 01)
        } else if (command[0] == 0x01) {
            logger.debug("first byte 0x01 -> ignored");

            // EM command / update
        } else {
            if ((module & 0xE0) == 0x00) {
                sendEmAcknowledge(module, toggle);
                logger.debug("send acknowledge (modul, toggle) {} {}", module, toggle);

                for (byte cmdByte : command) {
                    byte channel = (byte) ((cmdByte >>> 4) & 0x0F);

                    OnOffType onOff = OnOffType.OFF;

                    byte cmd = (byte) (cmdByte & 0x0F);
                    if (cmd % 2 == 0) {
                        if (cmd == 2) {
                            onOff = OnOffType.ON;
                        } else {
                            logger.debug("Command {} isn't implemented for EM", cmd);
                            continue;
                        }
                    }

                    QueueObject qo = new QueueObject(PHCBindingConstants.CHANNELS_EM, module, channel, onOff);

                    // put recognized message into queue
                    if (!receiveQueue.contains(qo)) {
                        receiveQueue.offer(qo);
                    }
                }

                // ignore if message not from EM module
            } else if (logger.isDebugEnabled()) {
                logger.debug("Incoming message (module, toggle, command) not from EM module: {} {} {}",
                        new String(HexUtils.byteToHex(module)), toggle, PHCHelper.bytesToBinaryString(command));
            }
        }
    }

    /**
     * process receive queue
     */
    private void processReceiveQueue() {
        while (true) {
            try {
                QueueObject qo = receiveQueue.take();

                logger.debug("Consume Receive QueueObject: {}", qo);
                handleIncomingCommand(qo.getModuleAddress(), qo.getChannel(), (OnOffType) qo.getCommand());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * process send queue
     */
    private void processSendQueue() {
        while (true) {
            try {
                QueueObject qo = sendQueue.take();

                sendQueueObject(qo);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendQueueObject(QueueObject qo) {
        int sendCount = 0;
        // Send the command to the module until a response is received. Max. SEND_RETRY_COUNT repeats.
        do {
            switch (qo.getModuleType()) {
                case PHCBindingConstants.CHANNELS_AM:
                    sendAm(qo.getModuleAddress(), qo.getChannel(), qo.getCommand());
                    break;
                case PHCBindingConstants.CHANNELS_EM_LED:
                    sendEm(qo.getModuleAddress(), qo.getChannel(), qo.getCommand());
                    break;
                case PHCBindingConstants.CHANNELS_JRM:
                    sendJrm(qo.getModuleAddress(), qo.getChannel(), qo.getCommand(), qo.getTime());
                    break;
                case PHCBindingConstants.CHANNELS_DIM:
                    sendDim(qo.getModuleAddress(), qo.getChannel(), qo.getCommand(), qo.getTime());
                    break;
            }

            sendCount++;
            try {
                Thread.sleep(SEND_RETRY_TIME_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } while (!isChannelOutputState(qo.getModuleType(), qo.getModuleAddress(), qo.getChannel(), qo.getCommand())
                && sendCount < SEND_RETRY_COUNT);

        if (PHCBindingConstants.CHANNELS_JRM.equals(qo.getModuleType())) {
            // there aren't state per channel for JRM modules
            amOutputState[qo.getModuleAddress() & 0x1F] = -1;
        } else if (PHCBindingConstants.CHANNELS_DIM.equals(qo.getModuleType())) {
            // state ist the same for every dim level except zero/off -> inizialize state
            // with 0x0F after sending a command.
            dmOutputState[qo.getModuleAddress() & 0x1F] |= (0x0F << (qo.getChannel() * 4));
        }

        if (sendCount >= SEND_RETRY_COUNT) {
            // change the toggle: if no acknowledge received it may be wrong.
            byte module = qo.getModuleAddress();
            if (PHCBindingConstants.CHANNELS_AM.equals(qo.getModuleType())
                    || PHCBindingConstants.CHANNELS_JRM.equals(qo.getModuleType())) {
                module |= 0x40;
            } else if (PHCBindingConstants.CHANNELS_DIM.equals(qo.getModuleType())) {
                module |= 0xA0;
            }
            toggleMap.put(module, !getToggle(module));

            if (logger.isDebugEnabled()) {
                logger.debug("No acknowledge from the module {} received.", qo.getModuleAddress());
            }
        }
    }

    private void setModuleOutputState(String moduleType, byte moduleAddress, byte state, byte channel) {
        if (PHCBindingConstants.CHANNELS_EM_LED.equals(moduleType)) {
            emLedOutputState[moduleAddress] = state;
        } else if (PHCBindingConstants.CHANNELS_AM.equals(moduleType)) {
            amOutputState[moduleAddress & 0x1F] = state;
        } else if (PHCBindingConstants.CHANNELS_DIM.equals(moduleType)) {
            dmOutputState[moduleAddress & 0x1F] = (byte) (state << channel * 4);
        }
    }

    private boolean isChannelOutputState(String moduleType, byte moduleAddress, byte channel, Command cmd) {
        int state = OnOffType.OFF.equals(cmd) ? 0 : 1;

        if (PHCBindingConstants.CHANNELS_EM_LED.equals(moduleType)) {
            return ((emLedOutputState[moduleAddress & 0x1F] >>> channel) & 0x01) == state;
        } else if (PHCBindingConstants.CHANNELS_AM.equals(moduleType)) {
            return ((amOutputState[moduleAddress & 0x1F] >>> channel) & 0x01) == state;
        } else if (PHCBindingConstants.CHANNELS_JRM.equals(moduleType)) {
            return (amOutputState[moduleAddress & 0x1F] != -1);
        } else if (PHCBindingConstants.CHANNELS_DIM.equals(moduleType)) {
            return ((dmOutputState[moduleAddress & 0x1F] >>> channel * 4) & 0x0F) != 0x0F;
        } else {
            return false;
        }
    }

    private boolean getToggle(byte moduleAddress) {
        if (!toggleMap.containsKey(moduleAddress)) {
            toggleMap.put(moduleAddress, false);
        }

        return toggleMap.get(moduleAddress);
    }

    /**
     * Put the given command into the queue to send.
     *
     * @param moduleType
     * @param moduleAddress
     * @param channel
     * @param command
     * @param upDownTime
     */
    public void send(@Nullable String moduleType, int moduleAddress, String channel, Command command,
            short upDownTime) {
        if (PHCBindingConstants.CHANNELS_JRM.equals(moduleType)
                || PHCBindingConstants.CHANNELS_DIM.equals(moduleType)) {
            sendQueue.offer(new QueueObject(moduleType, moduleAddress, channel, command, upDownTime));
        } else {
            sendQueue.offer(new QueueObject(moduleType, moduleAddress, channel, command));
        }
    }

    private void sendAm(byte moduleAddress, byte channel, Command command) {
        byte module = (byte) (moduleAddress | 0x40);

        byte[] cmd = { (byte) (channel << 5) };

        if (OnOffType.ON.equals(command)) {
            cmd[0] |= 2;
        } else {
            cmd[0] |= 3;
        }
        serialWrite(buildMessage(module, channel, cmd, getToggle(module)));
    }

    private void sendEm(byte moduleAddress, byte channel, Command command) {
        byte[] cmd = { (byte) (channel << 4) };

        if (OnOffType.ON.equals(command)) {
            cmd[0] |= 2;
        } else {
            cmd[0] |= 3;
        }
        serialWrite(buildMessage(moduleAddress, channel, cmd, getToggle(moduleAddress)));
    }

    private void sendJrm(byte moduleAddress, byte channel, Command command, short upDownTime) {
        // The up and the down message needs two additional bytes for the time.
        int size = (command == StopMoveType.STOP) ? 2 : 4;
        byte[] cmd = new byte[size];
        if (channel == 0) {
            channel = 4;
        }

        byte module = (byte) (moduleAddress | 0x40);

        cmd[0] = (byte) (channel << 5);
        cmd[1] = 0x3F;

        switch (command.toString()) {
            case "UP":
                cmd[0] |= 5;
                cmd[2] = (byte) (upDownTime & 0xFF);// Time 1/10 sec. LSB
                cmd[3] = (byte) ((upDownTime >> 8) & 0xFF); // 1/10 sec. MSB
                break;
            case "DOWN":
                cmd[0] |= 6;
                cmd[2] = (byte) (upDownTime & 0xFF);// Time 1/10 sec. LSB
                cmd[3] = (byte) ((upDownTime >> 8) & 0xFF); // 1/10 sec. MSB
                break;
            case "STOP":
                cmd[0] |= 2;
                break;
        }

        serialWrite(buildMessage(module, channel, cmd, getToggle(module)));
    }

    private void sendDim(byte moduleAddress, byte channel, Command command, short dimTime) {
        byte module = (byte) (moduleAddress | 0xA0);
        byte[] cmd = new byte[(command instanceof PercentType && !(((PercentType) command).byteValue() == 0)) ? 3 : 1];

        cmd[0] = (byte) (channel << 5);

        if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                cmd[0] |= 3;
            } else if (OnOffType.OFF.equals(command)) {
                cmd[0] |= 4;
            }
        } else {
            if (((PercentType) command).byteValue() == 0) {
                cmd[0] |= 4;
            } else {
                cmd[0] |= 22;
                cmd[1] = (byte) (((PercentType) command).byteValue() * 2.55);
                cmd[2] = (byte) dimTime;
            }
        }
        serialWrite(buildMessage(module, channel, cmd, getToggle(module)));
    }

    private void sendPorBroadcast() {
        byte[] msg = buildMessage((byte) 0xFF, 0, new byte[] { 0 }, false);
        for (int i = 0; i < 20; i++) {
            serialWrite(msg);

        }
    }

    private void sendAmConfig(byte moduleAddress) {
        byte[] cmd = new byte[3];

        cmd[0] = (byte) 0xFE;
        cmd[1] = 0;
        cmd[2] = (byte) 0xFF;

        serialWrite(buildMessage(moduleAddress, 0, cmd, false));
    }

    private void sendEmConfig(byte moduleAddress) {
        byte[] cmd = new byte[52];
        int pos = 0;

        cmd[pos++] = (byte) 0xFE;
        cmd[pos++] = (byte) 0x00; // POR

        cmd[pos++] = 0x00;
        cmd[pos++] = 0x00;

        for (int i = 0; i < 16; i++) { // 16 inputs
            cmd[pos++] = (byte) ((i << 4) | 0x02);
            cmd[pos++] = (byte) ((i << 4) | 0x03);
            cmd[pos++] = (byte) ((i << 4) | 0x05);
        }

        serialWrite(buildMessage(moduleAddress, 0, cmd, false));
    }

    private void sendEmAcknowledge(byte module, boolean toggle) {
        byte[] msg = buildMessage(module, 0, new byte[] { 0 }, toggle);
        for (int i = 0; i < 3; i++) { // send three times stops the module faster from sending messages if the first
                                      // response is not recognized.
            serialWrite(msg);
        }
    }

    /**
     * Build a serial message from the given parameters.
     *
     * @param modulAddr
     * @param channel
     * @param cmd
     * @param toggle
     * @return
     */
    private byte[] buildMessage(byte modulAddr, int channel, byte[] cmd, boolean toggle) {
        int len = cmd.length;
        byte[] buffer = new byte[len + 4];

        buffer[0] = modulAddr;
        buffer[1] = (byte) (toggle ? (len | 0x80) : len); // 0x80: 1000 0000

        System.arraycopy(cmd, 0, buffer, 2, len);

        short crc = calcCrc(modulAddr, buffer[1], cmd);

        buffer[2 + len] = (byte) (crc & 0xFF);
        buffer[3 + len] = (byte) ((crc >> 8) & 0xFF);

        return buffer;
    }

    /**
     * Calculate the 16 bit crc of the message.
     *
     * @param module
     * @param sizeToggle
     * @param cmd
     * @return
     */
    private short calcCrc(byte module, byte sizeToggle, byte[] cmd) {
        short crc = (short) 0xFFFF;

        crc = crc16Update(crc, module);
        crc = crc16Update(crc, sizeToggle);

        for (byte b : cmd) {
            crc = crc16Update(crc, b);
        }

        crc ^= 0xFFFF;
        return crc;
    }

    /**
     * Update the 16 bit crc of the message.
     *
     * @param crc
     * @param data
     * @return
     */
    private short crc16Update(short crc, byte messagePart) {
        byte data = (byte) (messagePart ^ (crc & 0xFF));
        data ^= data << 4;
        short data16 = data;

        return (short) (((data16 << 8) | (((crc >> 8) & 0xFF) & 0xFF)) ^ ((data >> 4) & 0xF)
                ^ ((data16 << 3) & 0b11111111111));
    }

    /**
     * Send the incoming command to the appropriate handler and channel.
     *
     * @param moduleAddress
     * @param channel
     * @param cmd
     * @param rcvCrc
     */
    private void handleIncomingCommand(byte moduleAddress, int channel, OnOffType onOff) {
        ThingUID uid = PHCHelper.getThingUIDreverse(PHCBindingConstants.THING_TYPE_EM, moduleAddress);
        Thing thing = getThing().getThing(uid);
        String channelId = "em#" + StringUtils.leftPad(Integer.toString(channel), 2, '0');

        if (thing != null && thing.getHandler() != null) {
            logger.debug("Input: {}, {}, {}", thing.getUID(), channelId, onOff);

            PHCHandler handler = (PHCHandler) thing.getHandler();
            if (handler != null) {
                handler.handleIncoming(channelId, onOff);
            } else {
                logger.debug("No Handler for Thing {} available.", thing.getUID());
            }

        } else {
            logger.debug("No Thing with UID {} available.", uid.getAsString());
        }
    }

    private void serialWrite(byte[] msg) {
        if (serialOut != null) {
            try {
                // write to serial port
                serialOut.write(msg);
                serialOut.flush();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error writing '" + msg + "' to serial port : " + e.getMessage());
            }

            if (logger.isTraceEnabled()) {
                logger.trace("send: {}", PHCHelper.bytesToBinaryString(msg));
            }
        }
    }

    /**
     * Adds the given address to the module list.
     *
     * @param module
     */
    public void addModule(byte module) {
        modules.add(module);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // unnecessary
    }

    @Override
    public void dispose() {
        threadPoolExecutor.shutdownNow();
        if (commPort != null) {
            commPort.close();
            commPort = null;
        }
    }
}
