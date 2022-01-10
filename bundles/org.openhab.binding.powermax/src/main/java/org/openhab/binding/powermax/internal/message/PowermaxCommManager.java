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
package org.openhab.binding.powermax.internal.message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.connector.PowermaxConnector;
import org.openhab.binding.powermax.internal.connector.PowermaxSerialConnector;
import org.openhab.binding.powermax.internal.connector.PowermaxTcpConnector;
import org.openhab.binding.powermax.internal.state.PowermaxArmMode;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxPanelType;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.binding.powermax.internal.state.PowermaxStateEvent;
import org.openhab.binding.powermax.internal.state.PowermaxStateEventListener;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that manages the communication with the Visonic alarm system
 *
 * Visonic does not provide a specification of the RS232 protocol and, thus,
 * the binding uses the available protocol specification given at the ​domoticaforum
 * http://www.domoticaforum.eu/viewtopic.php?f=68&t=6581
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxCommManager implements PowermaxMessageEventListener {

    private static final int DEFAULT_TCP_PORT = 80;
    private static final int TCP_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int WAITING_DELAY_FOR_RESPONSE = 750;
    private static final long DELAY_BETWEEN_SETUP_DOWNLOADS = TimeUnit.SECONDS.toMillis(45);

    private final Logger logger = LoggerFactory.getLogger(PowermaxCommManager.class);

    private final ScheduledExecutorService scheduler;

    private final TimeZoneProvider timeZoneProvider;

    /** The object to store the current settings of the Powermax alarm system */
    private final PowermaxPanelSettings panelSettings;

    /** Panel type used when in standard mode */
    private final PowermaxPanelType panelType;

    private final boolean forceStandardMode;
    private final boolean autoSyncTime;

    private final List<PowermaxStateEventListener> listeners = new ArrayList<>();

    /** The serial or TCP connecter used to communicate with the Powermax alarm system */
    private final PowermaxConnector connector;

    /** The last message sent to the the Powermax alarm system */
    private @Nullable PowermaxBaseMessage lastSendMsg;

    /** The message queue of messages to be sent to the the Powermax alarm system */
    private ConcurrentLinkedQueue<PowermaxBaseMessage> msgQueue = new ConcurrentLinkedQueue<>();

    /** The time in milliseconds the last download of the panel setup was requested */
    private long lastTimeDownloadRequested;

    /** The boolean indicating if the download of the panel setup is in progress or not */
    private boolean downloadRunning;

    /** The time in milliseconds used to set time and date */
    private long syncTimeCheck;

    /**
     * Constructor for Serial Connection
     *
     * @param sPort the serial port name
     * @param panelType the panel type to be used when in standard mode
     * @param forceStandardMode true to force the standard mode rather than trying using the Powerlink mode
     * @param autoSyncTime true for automatic sync time
     * @param serialPortManager the serial port manager
     * @param threadName the prefix name of threads to be created
     */
    public PowermaxCommManager(String sPort, PowermaxPanelType panelType, boolean forceStandardMode,
            boolean autoSyncTime, SerialPortManager serialPortManager, String threadName,
            TimeZoneProvider timeZoneProvider) {
        this.panelType = panelType;
        this.forceStandardMode = forceStandardMode;
        this.autoSyncTime = autoSyncTime;
        this.timeZoneProvider = timeZoneProvider;
        this.panelSettings = new PowermaxPanelSettings(panelType);
        this.scheduler = ThreadPoolManager.getScheduledPool(threadName + "-sender");
        this.connector = new PowermaxSerialConnector(serialPortManager, sPort.trim(), DEFAULT_BAUD_RATE,
                threadName + "-reader");
    }

    /**
     * Constructor for TCP connection
     *
     * @param ip the IP address
     * @param port TCP port number; default port is used if value <= 0
     * @param panelType the panel type to be used when in standard mode
     * @param forceStandardMode true to force the standard mode rather than trying using the Powerlink mode
     * @param autoSyncTime true for automatic sync time
     * @param serialPortManager
     * @param threadName the prefix name of threads to be created
     */
    public PowermaxCommManager(String ip, int port, PowermaxPanelType panelType, boolean forceStandardMode,
            boolean autoSyncTime, String threadName, TimeZoneProvider timeZoneProvider) {
        this.panelType = panelType;
        this.forceStandardMode = forceStandardMode;
        this.autoSyncTime = autoSyncTime;
        this.timeZoneProvider = timeZoneProvider;
        this.panelSettings = new PowermaxPanelSettings(panelType);
        this.scheduler = ThreadPoolManager.getScheduledPool(threadName + "-sender");
        this.connector = new PowermaxTcpConnector(ip.trim(), port > 0 ? port : DEFAULT_TCP_PORT, TCP_CONNECTION_TIMEOUT,
                threadName + "-reader");
    }

    /**
     * Add event listener
     *
     * @param listener the listener to be added
     */
    public synchronized void addEventListener(PowermaxStateEventListener listener) {
        listeners.add(listener);
        connector.addEventListener(this);
    }

    /**
     * Remove event listener
     *
     * @param listener the listener to be removed
     */
    public synchronized void removeEventListener(PowermaxStateEventListener listener) {
        connector.removeEventListener(this);
        listeners.remove(listener);
    }

    /**
     * Connect to the Powermax alarm system
     *
     * @return true if connected or false if not
     */
    public void open() throws Exception {
        connector.open();
        lastSendMsg = null;
        msgQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Close the connection to the Powermax alarm system.
     *
     * @return true if connected or false if not
     */
    public boolean close() {
        connector.close();
        lastTimeDownloadRequested = 0;
        downloadRunning = false;
        return isConnected();
    }

    /**
     * @return true if connected to the Powermax alarm system or false if not
     */
    public boolean isConnected() {
        return connector.isConnected();
    }

    /**
     * @return the current settings of the Powermax alarm system
     */
    public PowermaxPanelSettings getPanelSettings() {
        return panelSettings;
    }

    /**
     * Process and store all the panel settings from the raw buffers
     *
     * @param PowerlinkMode true if in Powerlink mode or false if in standard mode
     *
     * @return true if no problem encountered to get all the settings; false if not
     */
    public boolean processPanelSettings(boolean powerlinkMode) {
        return panelSettings.process(powerlinkMode, panelType, powerlinkMode ? syncTimeCheck : 0);
    }

    /**
     * @return a new instance of PowermaxState
     */
    public PowermaxState createNewState() {
        return new PowermaxState(panelSettings, timeZoneProvider);
    }

    /**
     * @return the last message sent to the Powermax alarm system
     */
    public synchronized @Nullable PowermaxBaseMessage getLastSendMsg() {
        return lastSendMsg;
    }

    @Override
    public void onNewMessageEvent(EventObject event) {
        PowermaxMessageEvent messageEvent = (PowermaxMessageEvent) event;
        PowermaxBaseMessage message = messageEvent.getMessage();

        if (logger.isDebugEnabled()) {
            logger.debug("onNewMessageReceived(): received message 0x{} ({})",
                    HexUtils.bytesToHex(message.getRawData()),
                    (message.getReceiveType() != null) ? message.getReceiveType()
                            : String.format("%02X", message.getCode()));
        }

        if (forceStandardMode && message instanceof PowermaxPowerlinkMessage) {
            message = new PowermaxBaseMessage(message.getRawData());
        }

        PowermaxState updateState = message.handleMessage(this);

        if (updateState == null) {
            updateState = createNewState();
        }

        updateState.lastMessageTime.setValue(System.currentTimeMillis());

        byte[] buffer = updateState.getUpdateSettings();
        if (buffer != null) {
            panelSettings.updateRawSettings(buffer);
        }
        if (!updateState.getUpdatedZoneNames().isEmpty()) {
            for (Integer zoneIdx : updateState.getUpdatedZoneNames().keySet()) {
                panelSettings.updateZoneName(zoneIdx, updateState.getUpdatedZoneNames().get(zoneIdx));
            }
        }
        if (!updateState.getUpdatedZoneInfos().isEmpty()) {
            for (Integer zoneIdx : updateState.getUpdatedZoneInfos().keySet()) {
                panelSettings.updateZoneInfo(zoneIdx, updateState.getUpdatedZoneInfos().get(zoneIdx));
            }
        }

        PowermaxStateEvent newEvent = new PowermaxStateEvent(this, updateState);

        // send message to event listeners
        listeners.forEach(listener -> listener.onNewStateEvent(newEvent));
    }

    @Override
    public void onCommunicationFailure(String message) {
        close();
        listeners.forEach(listener -> listener.onCommunicationFailure(message));
    }

    /**
     * Compute the CRC of a message
     *
     * @param data the buffer containing the message
     * @param len the size of the message in the buffer
     *
     * @return the computed CRC
     */
    public static byte computeCRC(byte[] data, int len) {
        long checksum = 0;
        for (int i = 1; i < (len - 2); i++) {
            checksum = checksum + (data[i] & 0x000000FF);
        }
        checksum = 0xFF - (checksum % 0xFF);
        if (checksum == 0xFF) {
            checksum = 0;
        }
        return (byte) checksum;
    }

    /**
     * Send an ACK for a received message
     *
     * @param msg the received message object
     * @param ackType the type of ACK to be sent
     *
     * @return true if the ACK was sent or false if not
     */
    public synchronized boolean sendAck(PowermaxBaseMessage msg, byte ackType) {
        int code = msg.getCode();
        byte[] rawData = msg.getRawData();
        byte[] ackData;
        if ((code >= 0x80) || ((code < 0x10) && (rawData[rawData.length - 3] == 0x43))) {
            ackData = new byte[] { 0x0D, ackType, 0x43, 0x00, 0x0A };
        } else {
            ackData = new byte[] { 0x0D, ackType, 0x00, 0x0A };
        }

        if (logger.isDebugEnabled()) {
            logger.debug("sendAck(): sending message {}", HexUtils.bytesToHex(ackData));
        }
        boolean done = sendMessage(ackData);
        if (!done) {
            logger.debug("sendAck(): failed");
        }
        return done;
    }

    /**
     * Send a message to the Powermax alarm panel to change arm mode
     *
     * @param armMode the arm mode
     * @param pinCode the PIN code. A string of 4 characters is expected
     *
     * @return true if the message was sent or false if not
     */
    public boolean requestArmMode(PowermaxArmMode armMode, String pinCode) {
        logger.debug("requestArmMode(): armMode = {}", armMode.getShortName());

        boolean done = false;
        if (!armMode.isAllowedCommand()) {
            logger.debug("Powermax alarm binding: requested arm mode {} rejected", armMode.getShortName());
        } else if (pinCode.length() != 4) {
            logger.debug("Powermax alarm binding: requested arm mode {} rejected due to invalid PIN code",
                    armMode.getShortName());
        } else {
            try {
                byte[] dynPart = new byte[3];
                dynPart[0] = armMode.getCommandCode();
                dynPart[1] = (byte) Integer.parseInt(pinCode.substring(0, 2), 16);
                dynPart[2] = (byte) Integer.parseInt(pinCode.substring(2, 4), 16);

                done = sendMessage(new PowermaxBaseMessage(PowermaxSendType.ARM, dynPart), false, 0, true);
            } catch (NumberFormatException e) {
                logger.debug("Powermax alarm binding: requested arm mode {} rejected due to invalid PIN code",
                        armMode.getShortName());
            }
        }
        return done;
    }

    /**
     * Send a message to the Powermax alarm panel to change PGM or X10 zone state
     *
     * @param action the requested action. Allowed values are: OFF, ON, DIM, BRIGHT
     * @param device the X10 device number. null is expected for PGM
     *
     * @return true if the message was sent or false if not
     */
    public boolean sendPGMX10(Command action, @Nullable Byte device) {
        logger.debug("sendPGMX10(): action = {}, device = {}", action, device);

        boolean done = false;

        Map<String, Byte> codes = new HashMap<>();
        codes.put("OFF", (byte) 0x00);
        codes.put("ON", (byte) 0x01);
        codes.put("DIM", (byte) 0x0A);
        codes.put("BRIGHT", (byte) 0x0B);

        Byte code = codes.get(action.toString());
        if (code == null) {
            logger.debug("Powermax alarm binding: invalid PGM/X10 command: {}", action);
        } else if ((device != null) && ((device < 1) || (device >= panelSettings.getNbPGMX10Devices()))) {
            logger.debug("Powermax alarm binding: invalid X10 device id: {}", device);
        } else {
            int val = (device == null) ? 1 : (1 << device);
            byte[] dynPart = new byte[3];
            dynPart[0] = code;
            dynPart[1] = (byte) (val & 0x000000FF);
            dynPart[2] = (byte) (val >> 8);

            done = sendMessage(new PowermaxBaseMessage(PowermaxSendType.X10PGM, dynPart), false, 0);
        }
        return done;
    }

    /**
     * Send a message to the Powermax alarm panel to bypass a zone or to not bypass a zone
     *
     * @param bypass true to bypass the zone; false to not bypass the zone
     * @param zone the zone number (first zone is number 1)
     * @param pinCode the PIN code. A string of 4 characters is expected
     *
     * @return true if the message was sent or false if not
     */
    public boolean sendZoneBypass(boolean bypass, byte zone, String pinCode) {
        logger.debug("sendZoneBypass(): bypass = {}, zone = {}", bypass ? "true" : "false", zone);

        boolean done = false;

        if (pinCode.length() != 4) {
            logger.debug("Powermax alarm binding: zone bypass rejected due to invalid PIN code");
        } else if ((zone < 1) || (zone > panelSettings.getNbZones())) {
            logger.debug("Powermax alarm binding: invalid zone number: {}", zone);
        } else {
            try {
                int val = (1 << (zone - 1));

                byte[] dynPart = new byte[10];
                dynPart[0] = (byte) Integer.parseInt(pinCode.substring(0, 2), 16);
                dynPart[1] = (byte) Integer.parseInt(pinCode.substring(2, 4), 16);
                int i;
                for (i = 2; i < 10; i++) {
                    dynPart[i] = 0;
                }
                i = bypass ? 2 : 6;
                dynPart[i++] = (byte) (val & 0x000000FF);
                dynPart[i++] = (byte) ((val >> 8) & 0x000000FF);
                dynPart[i++] = (byte) ((val >> 16) & 0x000000FF);
                dynPart[i++] = (byte) ((val >> 24) & 0x000000FF);

                done = sendMessage(new PowermaxBaseMessage(PowermaxSendType.BYPASS, dynPart), false, 0, true);
                if (done) {
                    done = sendMessage(new PowermaxBaseMessage(PowermaxSendType.BYPASSTAT), false, 0);
                }
            } catch (NumberFormatException e) {
                logger.debug("Powermax alarm binding: zone bypass rejected due to invalid PIN code");
            }
        }
        return done;
    }

    /**
     * Send a message to set the alarm time and date using the system time and date
     *
     * @return true if the message was sent or false if not
     */
    public boolean sendSetTime() {
        logger.debug("sendSetTime()");

        boolean done = false;

        if (autoSyncTime) {
            GregorianCalendar cal = new GregorianCalendar();
            if (cal.get(Calendar.YEAR) >= 2000) {
                logger.debug("sendSetTime(): sync time {}",
                        String.format("%02d/%02d/%04d %02d:%02d:%02d", cal.get(Calendar.DAY_OF_MONTH),
                                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));

                byte[] dynPart = new byte[6];
                dynPart[0] = (byte) cal.get(Calendar.SECOND);
                dynPart[1] = (byte) cal.get(Calendar.MINUTE);
                dynPart[2] = (byte) cal.get(Calendar.HOUR_OF_DAY);
                dynPart[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
                dynPart[4] = (byte) (cal.get(Calendar.MONTH) + 1);
                dynPart[5] = (byte) (cal.get(Calendar.YEAR) - 2000);

                done = sendMessage(new PowermaxBaseMessage(PowermaxSendType.SETTIME, dynPart), false, 0);

                cal.set(Calendar.MILLISECOND, 0);
                syncTimeCheck = cal.getTimeInMillis();
            } else {
                logger.info(
                        "Powermax alarm binding: time not synchronized; please correct the date/time of your openHAB server");
                syncTimeCheck = 0;
            }
        } else {
            syncTimeCheck = 0;
        }
        return done;
    }

    /**
     * Send a message to the Powermax alarm panel to get all the event logs
     *
     * @param pinCode the PIN code. A string of 4 characters is expected
     *
     * @return true if the message was sent or false if not
     */
    public boolean requestEventLog(String pinCode) {
        logger.debug("requestEventLog()");

        boolean done = false;

        if (pinCode.length() != 4) {
            logger.debug("Powermax alarm binding: requested event log rejected due to invalid PIN code");
        } else {
            try {
                byte[] dynPart = new byte[3];
                dynPart[0] = (byte) Integer.parseInt(pinCode.substring(0, 2), 16);
                dynPart[1] = (byte) Integer.parseInt(pinCode.substring(2, 4), 16);

                done = sendMessage(new PowermaxBaseMessage(PowermaxSendType.EVENTLOG, dynPart), false, 0, true);
            } catch (NumberFormatException e) {
                logger.debug("Powermax alarm binding: requested event log rejected due to invalid PIN code");
            }
        }
        return done;
    }

    /**
     * Start downloading panel setup
     *
     * @return true if the message was sent or the sending is delayed; false in other cases
     */
    public synchronized boolean startDownload() {
        if (downloadRunning) {
            return false;
        } else {
            lastTimeDownloadRequested = System.currentTimeMillis();
            downloadRunning = true;
            return sendMessage(PowermaxSendType.DOWNLOAD);
        }
    }

    /**
     * Act the exit of the panel setup
     */
    public synchronized void exitDownload() {
        downloadRunning = false;
    }

    public void retryDownloadSetup(int remainingAttempts) {
        long now = System.currentTimeMillis();
        if ((remainingAttempts > 0) && !isDownloadRunning() && ((lastTimeDownloadRequested == 0)
                || ((now - lastTimeDownloadRequested) >= DELAY_BETWEEN_SETUP_DOWNLOADS))) {
            // We wait at least 45 seconds before each retry to download the panel setup
            logger.debug("Powermax alarm binding: try again downloading setup");
            startDownload();
        }
    }

    public void getInfosWhenInStandardMode() {
        sendMessage(PowermaxSendType.ZONESNAME);
        sendMessage(PowermaxSendType.ZONESTYPE);
        sendMessage(PowermaxSendType.STATUS);
    }

    public void sendRestoreMessage() {
        sendMessage(PowermaxSendType.RESTORE);
    }

    /**
     * @return true if a download of the panel setup is in progress
     */
    public boolean isDownloadRunning() {
        return downloadRunning;
    }

    /**
     * @return the time in milliseconds the last download of the panel setup was requested or 0 if not yet requested
     */
    public long getLastTimeDownloadRequested() {
        return lastTimeDownloadRequested;
    }

    /**
     * Send a ENROLL message
     *
     * @return true if the message was sent or the sending is delayed; false in other cases
     */
    public boolean enrollPowerlink() {
        return sendMessage(new PowermaxBaseMessage(PowermaxSendType.ENROLL), true, 0);
    }

    /**
     * Send a message or delay the sending if time frame for receiving response is not ended
     *
     * @param msgType the message type to be sent
     *
     * @return true if the message was sent or the sending is delayed; false in other cases
     */
    public boolean sendMessage(PowermaxSendType msgType) {
        return sendMessage(new PowermaxBaseMessage(msgType), false, 0);
    }

    /**
     * Delay the sending of a message
     *
     * @param msgType the message type to be sent
     * @param waitTime the delay in seconds to wait
     *
     * @return true if the sending is delayed; false in other cases
     */
    public boolean sendMessageLater(PowermaxSendType msgType, int waitTime) {
        return sendMessage(new PowermaxBaseMessage(msgType), false, waitTime);
    }

    private synchronized boolean sendMessage(@Nullable PowermaxBaseMessage msg, boolean immediate, int waitTime) {
        return sendMessage(msg, immediate, waitTime, false);
    }

    /**
     * Send a message or delay the sending if time frame for receiving response is not ended
     *
     * @param msg the message to be sent
     * @param immediate true if the message has to be send without considering timing
     * @param waitTime the delay in seconds to wait
     * @param doNotLog true if the message contains data that must not be logged
     *
     * @return true if the message was sent or the sending is delayed; false in other cases
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private synchronized boolean sendMessage(@Nullable PowermaxBaseMessage msg, boolean immediate, int waitTime,
            boolean doNotLog) {
        if ((waitTime > 0) && (msg != null)) {
            logger.debug("sendMessage(): delay ({} s) sending message (type {})", waitTime, msg.getSendType());
            // Don't queue the message
            PowermaxBaseMessage msgToSendLater = new PowermaxBaseMessage(msg.getRawData());
            msgToSendLater.setSendType(msg.getSendType());
            scheduler.schedule(() -> {
                sendMessage(msgToSendLater, false, 0);
            }, waitTime, TimeUnit.SECONDS);
            return true;
        }

        if (msg == null) {
            msg = msgQueue.peek();
            if (msg == null) {
                logger.debug("sendMessage(): nothing to send");
                return false;
            }
        }

        // Delay sending if time frame for receiving response is not ended
        long delay = WAITING_DELAY_FOR_RESPONSE - (System.currentTimeMillis() - connector.getWaitingForResponse());

        PowermaxBaseMessage msgToSend = msg;

        if (!immediate) {
            msgToSend = msgQueue.peek();
            if (msgToSend != msg) {
                logger.debug("sendMessage(): add message in queue (type {})", msg.getSendType());
                msgQueue.offer(msg);
                msgToSend = msgQueue.peek();
            }
            if ((msgToSend != msg) && (delay > 0)) {
                return true;
            } else if ((msgToSend == msg) && (delay > 0)) {
                if (delay < 100) {
                    delay = 100;
                }
                logger.debug("sendMessage(): delay ({} ms) sending message (type {})", delay, msgToSend.getSendType());
                scheduler.schedule(() -> {
                    sendMessage(null, false, 0);
                }, delay, TimeUnit.MILLISECONDS);
                return true;
            } else {
                msgToSend = msgQueue.poll();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("sendMessage(): sending {} message {}", msgToSend.getSendType(),
                    doNotLog ? "***" : HexUtils.bytesToHex(msgToSend.getRawData()));
        }
        boolean done = sendMessage(msgToSend.getRawData());
        if (done) {
            lastSendMsg = msgToSend;
            connector.setWaitingForResponse(System.currentTimeMillis());

            if (!immediate && (msgQueue.peek() != null)) {
                logger.debug("sendMessage(): delay sending next message (type {})", msgQueue.peek().getSendType());
                scheduler.schedule(() -> {
                    sendMessage(null, false, 0);
                }, WAITING_DELAY_FOR_RESPONSE, TimeUnit.MILLISECONDS);
            }
        } else {
            logger.debug("sendMessage(): failed");
        }

        return done;
    }

    /**
     * Send a message to the Powermax alarm panel
     *
     * @param data the data buffer containing the message to be sent
     *
     * @return true if the message was sent or false if not
     */
    private boolean sendMessage(byte[] data) {
        boolean done = false;
        if (isConnected()) {
            data[data.length - 2] = computeCRC(data, data.length);
            connector.sendMessage(data);
            done = connector.isConnected();
        } else {
            logger.debug("sendMessage(): aborted (not connected)");
        }
        return done;
    }
}
