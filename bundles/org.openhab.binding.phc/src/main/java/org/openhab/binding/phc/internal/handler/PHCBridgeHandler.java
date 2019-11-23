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
package org.openhab.binding.phc.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.phc.internal.PHCBindingConstants;
import org.openhab.binding.phc.internal.PHCHelper;
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
    private static final int SEND_RETRY_COUNT = 15; // max count to send the same message
    private static final int SEND_RETRY_TIME_MILLIS = 80; // time to wait for an acknowledge before send the message
                                                          // again in milliseconds

    private @Nullable InputStream serialIn;
    private @Nullable OutputStream serialOut;
    short lastReceivedCrc;
    SerialPortManager serialPortManager;

    private final Map<String, Boolean> toggleMap = new HashMap<>();
    private final InternalBuffer buffer = new InternalBuffer();
    private final Queue<QueueObject> receiveQueue = new ConcurrentLinkedQueue<QueueObject>();
    private final ConcurrentNavigableMap<Long, QueueObject> queue = new ConcurrentSkipListMap<Long, QueueObject>();

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
            serialPort.setRTS(true);

            // get the output stream
            serialOut = serialPort.getOutputStream();

            sendPorBroadcast((byte) 0xFF);

            byte[] b = { 0x01 };
            for (int j = 0; j <= 0x1F; j++) {
                serialWrite(buildMessage((byte) j, 0, b, false));
            }
            updateStatus(ThingStatus.ONLINE);

            // receive messages
            new Thread("phc-reseive-serial") {

                @Override
                public void run() {
                    processReceivedBytes();
                }

            }.start();

            // process received messages
            new Thread("phc-process-messages") {

                @Override
                public void run() {
                    processReceiveQueue();
                }

            }.start();
            new Thread("phc-send-messages") {

                @Override
                public void run() {
                    processQueueLoop();
                }
            }.start();
        } catch (PortInUseException | TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not open serial port " + port + ": " + e.getMessage());
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not configure serial port " + port + ": " + e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to get input or output stream for serialPort. (See debugging log.)");
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
                    StringBuilder hex = new StringBuilder();
                    for (byte b : bytes) {
                        hex.append(PHCHelper.byteToHexString(b));
                    }
                    logger.trace("buffer offered {}", hex);
                }
            } catch (IOException e) {
                logger.error("error on reading input stream to internal buffer", e);
            }
        }
    }

    /**
     * process internal incoming buffer (recognize on read messages)
     */
    private void processReceivedBytes() {
        int loopCounter = 0;
        int faultCounter = 0;

        while (true) {
            try {
                // Recognition of messages from byte buffer.

                if (loopCounter > 25) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            loopCounter++;

            byte module = -1;
            while (buffer.size() >= 5) {
                loopCounter = 0;

                if (module == -1) {
                    module = buffer.get();
                }

                // not a known module address
                if (!modules.contains(module)) {
                    module = -1;
                    continue;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("get module: {}", PHCHelper.byteToHexString(module));
                }

                byte sizeToggle = buffer.get();

                if (logger.isDebugEnabled()) {
                    logger.debug("get sizeToggle: {}", PHCHelper.byteToHexString(sizeToggle));
                }

                // read length of command and check if makes sense
                if ((sizeToggle < 1 || sizeToggle > 9) && ((sizeToggle & 0xFF) < 0x81 || (sizeToggle & 0xFF) > 0x89)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("sizeToggle incorrect");
                    }

                    module = sizeToggle;
                    continue;
                }

                // read toggle, size and command
                int size = (sizeToggle & 0x7F);
                boolean toggle = (sizeToggle & 0x80) == 0x80;

                byte[] command = new byte[size];

                try {
                    for (int i = 0; i < size; i++) {
                        command[i] = buffer.get();
                    }

                    // log command
                    if (logger.isDebugEnabled()) {
                        String bin = "";
                        for (byte b : command) {
                            bin += PHCHelper.byteToBinaryString(b);
                        }
                        logger.debug("command read: {}", bin);
                    }

                    // read crc
                    byte crcByte1 = buffer.get();
                    byte crcByte2 = buffer.get();

                    short crc = (short) (crcByte1 & 0xFF);
                    crc |= (crcByte2 << 8);

                    if (logger.isTraceEnabled()) {
                        logger.trace("get crc: {}", (crcByte1 << 8) | crcByte2);
                    }

                    // calculate checkCrc
                    short checkCrc = (short) 0xFFFF;
                    checkCrc = crc16Update(checkCrc, module);
                    checkCrc = crc16Update(checkCrc, sizeToggle);

                    for (byte commandByte : command) {
                        checkCrc = crc16Update(checkCrc, commandByte);
                    }
                    checkCrc ^= 0xFFFF;

                    if (logger.isDebugEnabled()) {
                        logger.debug("crc {}, checkCrc {}", crc, checkCrc);
                    }

                    // check crc
                    if (crc != checkCrc) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("CRC not correct(module/sizeToggle/command): {} {} {}", module, sizeToggle,
                                    command);
                        }

                        faultCounter++;

                        if (faultCounter >= 5) {
                            faultCounter = 0;

                            // Normally recognizing shifted -> move one byte
                            if (buffer.hasNext()) {
                                buffer.get();
                            }
                        }

                        module = -1;
                        continue;
                    }
                } catch (IllegalStateException e) {
                    // In exceptional cases the message is longer than the buffer
                    break;
                }

                faultCounter = 0;

                if (logger.isDebugEnabled()) {
                    logger.debug("crc correct");
                }

                // Acknowledgement received (command first byte 0)
                if (command[0] == 0) {
                    String moduleType;
                    if ((module & 0xE0) == 0x40) {
                        moduleType = PHCBindingConstants.CHANNELS_AM;
                    } else if ((module & 0xE0) == 0xA0) {
                        moduleType = PHCBindingConstants.CHANNELS_DIM;
                    } else {
                        moduleType = PHCBindingConstants.CHANNELS_EM_LED;
                    }

                    setModuleOutputState(moduleType, (byte) (module & 0x1F), command[1]);

                    // initialization (first byte FF)
                } else if (command[0] == (byte) 0xFF) {
                    if ((module & 0xE0) == 0x00) { // EM
                        sendEmConfig(module);
                    } else if ((module & 0xE0) == 0x40 || (module & 0xE0) == 0xA0) { // AM, JRM and DIM
                        sendAmConfig(module);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("initialization: {}", module);
                    }

                    // ignored - ping (first byte 01)
                } else if (command[0] == 0x01) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("first byte 0x01 -> ignored");
                    }

                    // EM command / update
                } else {
                    if (((module & 0xE0) == 0x00)) {
                        sendEmAcknowledge(module, toggle);
                        if (logger.isDebugEnabled()) {
                            logger.debug("send acknowledge (modul, toggle) {} {}", module, toggle);
                        }

                        byte channel = (byte) ((command[0] >>> 4) & 0x0F);

                        OnOffType onOff = OnOffType.OFF;

                        if ((command[0] & 0x0F) == 2) {
                            onOff = OnOffType.ON;
                        }

                        QueueObject qo = new QueueObject(PHCBindingConstants.CHANNELS_EM, module, channel, onOff);

                        if (logger.isDebugEnabled()) {
                            logger.debug("QueueObject found: {}", qo);
                        }

                        // put recognized message into queue
                        if (!receiveQueue.contains(qo)) {
                            receiveQueue.offer(qo);
                        }

                        // ignore if message not from EM module
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Incoming message not from EM module: {} {} {}", module, sizeToggle, command);
                        }
                    }
                    module = -1;
                }
            }
        }
    }

    /**
     * process receive queue
     */
    private void processReceiveQueue() {
        while (true) {
            if (!receiveQueue.isEmpty()) {
                QueueObject qo = receiveQueue.poll();
                if (logger.isDebugEnabled()) {
                    logger.debug("Consume Receive QueueObject: {}", qo);
                }
                handleIncomingCommand(qo.getModuleAddress(), qo.getChannel(), (OnOffType) qo.getCommand());
            }
        }
    }

    private void processQueueLoop() {
        while (true) {
            long limit = System.currentTimeMillis();

            Map<Long, QueueObject> subQueue = queue.subMap(0L, true, limit, true);

            for (Long key : subQueue.keySet()) {
                QueueObject qo = subQueue.get(key);
                queue.remove(key);
                if (qo.getCounter() < SEND_RETRY_COUNT && !qo.getCommand()
                        .equals(isChannelOutputStateSet(qo.getModuleType(), qo.getModuleAddress(), qo.getChannel()))) {
                    qo.increaseCounter();

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

                    queue.put(System.currentTimeMillis() + SEND_RETRY_TIME_MILLIS, qo);
                } else if (qo.getCounter() >= SEND_RETRY_COUNT
                        && !qo.getModuleType().equals(PHCBindingConstants.CHANNELS_JRM)
                        && !qo.getModuleType().equals(PHCBindingConstants.CHANNELS_DIM)) {
                    // Can´t process the acknowledgement of JRM/DIM yet.
                    logger.debug("No acknowlgdge from the module {} received.", qo.getModuleAddress());
                }
            }
        }
    }

    private void setModuleOutputState(String moduleType, byte moduleAddress, byte state) {
        if (moduleType == PHCBindingConstants.CHANNELS_EM_LED) {
            emLedOutputState[moduleAddress] = state;
        } else if (moduleType == PHCBindingConstants.CHANNELS_AM) {
            amOutputState[moduleAddress & 0x1F] = state;
        } else if (moduleType == PHCBindingConstants.CHANNELS_DIM) {
            dmOutputState[moduleAddress & 0x1F] = state;
        }
    }

    @Nullable
    private Command isChannelOutputStateSet(String moduleType, byte moduleAddress, byte channel) {
        Command set = null;

        if (moduleType.equals(PHCBindingConstants.CHANNELS_EM_LED)) {
            set = ((emLedOutputState[moduleAddress] >> channel) & 0x01) == 1 ? OnOffType.ON : OnOffType.OFF;
        } else if (moduleType.equals(PHCBindingConstants.CHANNELS_AM)) {
            set = ((amOutputState[moduleAddress & 0x1F] >> channel) & 0x01) == 1 ? OnOffType.ON : OnOffType.OFF;
        } else if (moduleType.equals(PHCBindingConstants.CHANNELS_JRM)) {
            set = ((amOutputState[moduleAddress & 0x1F] >> channel) & 0x01) == 1 ? OnOffType.ON : OnOffType.OFF;
        } else if (moduleType.equals(PHCBindingConstants.CHANNELS_DIM)) {
            set = ((dmOutputState[moduleAddress & 0x1F] >> channel) & 0x01) == 1 ? OnOffType.ON : OnOffType.OFF;
        }

        return set;
    }

    private boolean toggleChannel(byte moduleAddress, byte channel) {
        String key = new String(new byte[] { moduleAddress, channel });
        boolean toggle = false;
        if (toggleMap.containsKey(key)) {
            toggle = toggleMap.get(key);
        }
        toggleMap.put(key, !toggle);

        return toggle;
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
        if (command instanceof OnOffType || command instanceof UpDownType || command.equals(StopMoveType.STOP)
                || command instanceof PercentType) {
            if (PHCBindingConstants.CHANNELS_JRM.equals(moduleType)
                    || PHCBindingConstants.CHANNELS_DIM.equals(moduleType)) { // can´t process acknowledge yet
                queue.put(System.currentTimeMillis(),
                        new QueueObject(moduleType, moduleAddress, channel, command, 9, upDownTime));
            } else {
                queue.put(System.currentTimeMillis(), new QueueObject(moduleType, moduleAddress, channel, command));
            }
        }
    }

    private void sendAm(byte moduleAddress, byte channel, Command command) {
        byte module = (byte) (moduleAddress | 0x40);

        byte[] cmd = { (byte) (channel << 5) };

        if (command.equals(OnOffType.ON)) {
            cmd[0] |= 2;
        } else {
            cmd[0] |= 3;
        }
        serialWrite(buildMessage(module, channel, cmd, toggleChannel(moduleAddress, channel)));
    }

    private void sendEm(byte moduleAddress, byte channel, Command command) {
        byte[] cmd = { (byte) (channel << 4) };

        if (command.equals(OnOffType.ON)) {
            cmd[0] |= 2;
        } else {
            cmd[0] |= 3;
        }
        serialWrite(buildMessage(moduleAddress, channel, cmd, toggleChannel(moduleAddress, channel)));
    }

    private void sendJrm(byte moduleAddress, byte channel, Command command, short upDownTime) {
        // The up and the down message needs two additional bytes for the time.
        int size = (command == StopMoveType.STOP) ? 2 : 4;
        byte[] cmd = new byte[size];

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

        serialWrite(buildMessage(module, channel, cmd, toggleChannel(module, channel)));
    }

    private void sendDim(byte moduleAddress, byte channel, Command command, short dimTime) {
        byte module = (byte) (moduleAddress | 0xA0);
        byte[] cmd = new byte[(command instanceof PercentType && !(((PercentType) command).byteValue() == 0)) ? 3 : 1];
        cmd[0] = (byte) (channel << 5);

        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.ON)) {
                cmd[0] |= 3;
            } else if (command.equals(OnOffType.OFF)) {
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
        serialWrite(buildMessage(module, channel, cmd, toggleChannel(module, channel)));
    }

    private void sendPorBroadcast(byte b) {
        byte[] cmd = { 0 };
        writeMsg(buildMessage((byte) 0xFF, 0, cmd, false), 20);
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
        byte[] cmd = { 0 };
        writeMsg(buildMessage(module, 0, cmd, toggle), 2); // 2 times needed?
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

        short crc = (short) 0xFFFF;

        for (int i = 0; i < (2 + len); i++) {
            crc = crc16Update(crc, buffer[i]);
        }
        crc ^= 0xFFFF;

        buffer[2 + len] = (byte) (crc & 0xFF);
        buffer[3 + len] = (byte) ((crc >> 8) & 0xFF);

        return buffer;
    }

    /**
     * Calculate/update the 16 bit crc of the message.
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
        Thing thing = getThingByUID(uid);
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

    /**
     * Send message to to serial port x times.
     *
     * @param msg
     * @param sendCnt
     */
    private void writeMsg(byte[] msg, int sendCnt) {
        int count = sendCnt;
        while (0 < count) {
            serialWrite(msg);
            count--;
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
                StringBuilder log = new StringBuilder();
                for (byte b : msg) {
                    log.append(PHCHelper.byteToBinaryString(b));
                    log.append(' ');
                }
                logger.trace("send: {}", log);
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
}
