/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.globalcache.handler;

import static org.openhab.binding.globalcache.GlobalCacheBindingConstants.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.globalcache.GlobalCacheBindingConstants.CommandType;
import org.openhab.binding.globalcache.internal.command.CommandGetstate;
import org.openhab.binding.globalcache.internal.command.CommandGetversion;
import org.openhab.binding.globalcache.internal.command.CommandSendir;
import org.openhab.binding.globalcache.internal.command.CommandSendserial;
import org.openhab.binding.globalcache.internal.command.CommandSetstate;
import org.openhab.binding.globalcache.internal.command.RequestMessage;
import org.openhab.binding.globalcache.internal.command.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GlobalCacheHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class GlobalCacheHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(GlobalCacheHandler.class);

    private static final String GLOBALCACHE_THREAD_POOL = "globalCacheHandler";

    private InetAddress ifAddress;
    private CommandProcessor commandProcessor;
    private ScheduledExecutorService scheduledExecutorService = ThreadPoolManager
            .getScheduledPool(GLOBALCACHE_THREAD_POOL + "-" + thingID());
    private ScheduledFuture<?> scheduledFuture;

    private LinkedBlockingQueue<RequestMessage> sendQueue = null;

    private String ipv4Address;

    // IR transaction counter
    private AtomicInteger irCounter;

    // Character set to use for URL encoding & decoding
    private String CHARSET = "ISO-8859-1";

    public GlobalCacheHandler(@NonNull Thing gcDevice, String ipv4Address) {
        super(gcDevice);
        irCounter = new AtomicInteger(1);
        commandProcessor = new CommandProcessor();
        scheduledFuture = null;
        this.ipv4Address = ipv4Address;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", thingID());
        try {
            ifAddress = InetAddress.getByName(ipv4Address);
            logger.debug("Handler using address {} on network interface {}", ifAddress.getHostAddress(),
                    NetworkInterface.getByInetAddress(ifAddress).getName());
        } catch (SocketException e) {
            logger.error("Handler got Socket exception creating multicast socket: {}", e.getMessage());
            markThingOfflineWithError(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "No suitable network interface");
            return;
        } catch (UnknownHostException e) {
            logger.error("Handler got UnknownHostException getting local IPv4 network interface: {}", e.getMessage());
            markThingOfflineWithError(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "No suitable network interface");
            return;
        }
        scheduledFuture = scheduledExecutorService.schedule(commandProcessor, 2, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", thingID());
        commandProcessor.terminate();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == null) {
            logger.warn("Command passed to handler for thing {} is null");
            return;
        }

        // Don't try to send command if the device is not online
        if (!isOnline()) {
            logger.debug("Can't handle command {} because handler for thing {} is not ONLINE", command, thingID());
            return;
        }

        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Unknown channel {} for thing {}; is item defined correctly", channelUID.getId(), thingID());
            return;
        }

        // Get module and connector properties for this channel
        String modNum = channel.getProperties().get(CHANNEL_PROPERTY_MODULE);
        String conNum = channel.getProperties().get(CHANNEL_PROPERTY_CONNECTOR);
        if (modNum == null || conNum == null) {
            logger.error("Channel {} of thing {} has no module/connector property", channelUID.getId(), thingID());
            return;
        }

        if (command instanceof RefreshType) {
            handleRefresh(modNum, conNum, channel);
            return;
        }

        switch (channel.getChannelTypeUID().getId()) {
            case CHANNEL_TYPE_CC:
                handleContactClosure(modNum, conNum, command, channelUID);
                break;

            case CHANNEL_TYPE_IR:
                handleInfrared(modNum, conNum, command, channelUID);
                break;

            case CHANNEL_TYPE_SL:
                handleSerial(modNum, conNum, command, channelUID);
                break;

            case CHANNEL_TYPE_SL_DIRECT:
                handleSerialDirect(modNum, conNum, command, channelUID);
                break;

            default:
                logger.warn("Thing {} has unknown channel type {}", thingID(), channel.getChannelTypeUID().getId());
                break;
        }
    }

    private void handleContactClosure(String modNum, String conNum, Command command, ChannelUID channelUID) {
        logger.debug("Handling CC command {} on channel {} of thing {}", command, channelUID.getId(), thingID());

        if (command instanceof OnOffType) {
            CommandSetstate setstate = new CommandSetstate(thing, command, sendQueue, modNum, conNum);
            setstate.execute();
        }
    }

    private void handleInfrared(String modNum, String conNum, Command command, ChannelUID channelUID) {
        logger.debug("Handling infrared command {} on channel {} of thing {}", command, channelUID.getId(), thingID());

        String irCode = lookupCode(command);
        if (irCode != null) {
            CommandSendir sendir = new CommandSendir(thing, command, sendQueue, modNum, conNum, irCode, getCounter());
            sendir.execute();
        }
    }

    private void handleSerial(String modNum, String conNum, Command command, ChannelUID channelUID) {
        logger.debug("Handle serial command {} on channel {} of thing {}", command, channelUID.getId(), thingID());

        String slCode = lookupCode(command);
        if (slCode != null) {
            CommandSendserial sendserial = new CommandSendserial(thing, command, sendQueue, modNum, conNum, slCode);
            sendserial.execute();
        }
    }

    private void handleSerialDirect(String modNum, String conNum, Command command, ChannelUID channelUID) {
        logger.debug("Handle serial command {} on channel {} of thing {}", command, channelUID.getId(), thingID());

        CommandSendserial sendserial = new CommandSendserial(thing, command, sendQueue, modNum, conNum,
                command.toString());
        sendserial.execute();
    }

    private void handleRefresh(String modNum, String conNum, Channel channel) {
        // REFRESH makes sense only for CC channels because we can query the device for the relay state
        if (channel.getChannelTypeUID().getId().equals(CHANNEL_TYPE_CC)) {
            logger.debug("Handle REFRESH command on channel {} for thing {}", channel.getUID().getId(), thingID());

            CommandGetstate getstate = new CommandGetstate(thing, sendQueue, modNum, conNum);
            getstate.execute();
            if (getstate.isSuccessful()) {
                updateState(channel.getUID(), getstate.state());
            }
        }
    }

    private int getCounter() {
        return irCounter.getAndIncrement();
    }

    /*
     * Look up the IR or serial command code in the MAP file.
     *
     */
    private String lookupCode(Command command) {
        if (command.toString() == null) {
            logger.warn("Unable to perform transform on null command string");
            return null;
        }

        String mapFile = (String) thing.getConfiguration().get(THING_CONFIG_MAP_FILENAME);
        if (StringUtils.isEmpty(mapFile)) {
            logger.warn("MAP file is not defined in configuration of thing {}", thingID());
            return null;
        }

        TransformationService transformService = TransformationHelper.getTransformationService(bundleContext, "MAP");
        if (transformService == null) {
            logger.error("Failed to get MAP transformation service for thing {}; is bundle installed?", thingID());
            return null;
        }

        String code;
        try {
            code = transformService.transform(mapFile, command.toString());

        } catch (TransformationException e) {
            logger.error("Failed to transform {} for thing {} using map file '{}', exception={}", command, thingID(),
                    mapFile, e.getMessage());
            return null;
        }

        if (StringUtils.isEmpty(code)) {
            logger.warn("No entry for {} in map file '{}' for thing {}", command, mapFile, thingID());
            return null;
        }

        logger.debug("Transformed {} for thing {} with map file '{}'", command, thingID(), mapFile);

        // Determine if the code is hex format. If so, convert to GC format
        if (isHexCode(code)) {
            logger.debug("Code is in hex format, convert to GC format");
            try {
                code = convertHexToGC(code);
                logger.debug("Converted hex code is: {}", code);
            } catch (HexCodeConversionException e) {
                logger.info("Failed to convert hex code to globalcache format: {}", e.getMessage());
                return null;
            }
        }
        return code;
    }

    /*
     * Check if the string looks like a hex code; if not then assume it's GC format
     */
    private boolean isHexCode(String code) {
        Pattern pattern = Pattern.compile("0000( +[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f])+");
        return pattern.matcher(code).find();
    }

    /*
     * Convert a hex code IR string to a Global Cache formatted IR string
     */
    private String convertHexToGC(String hexCode) throws HexCodeConversionException {
        // Magic number for converting frequency to GC format
        final int freqConversionFactor = 4145146;
        final int repeat = 1;
        int frequency;
        int sequence1Length;
        int offset;

        String[] hexCodeArray = hexCode.trim().split(" ");

        if (hexCodeArray.length < 5) {
            throw new HexCodeConversionException("Hex code is too short");
        }

        if (!hexCodeArray[0].equals("0000")) {
            throw new HexCodeConversionException("Illegal hex code element 0, should be 0000");
        }

        try {
            // Use magic number to get frequency
            frequency = Math.round(freqConversionFactor / Integer.parseInt(hexCodeArray[1], 16));
        } catch (Exception e) {
            throw new HexCodeConversionException("Unable to convert frequency from element 1");
        }

        try {
            // Offset is derived from sequenceLength1
            sequence1Length = Integer.parseInt(hexCodeArray[2], 16);
            offset = (sequence1Length * 2) + 1;
        } catch (Exception e) {
            throw new HexCodeConversionException("Unable to convert offset from element 2");
        }

        // sequenceLength2 (hexCodeArray[3]) is not used

        StringBuilder gcCode = new StringBuilder();
        gcCode.append(frequency);
        gcCode.append(",");
        gcCode.append(repeat);
        gcCode.append(",");
        gcCode.append(offset);

        try {
            // The remaining fields are just converted to decimal
            for (int i = 4; i < hexCodeArray.length; i++) {
                gcCode.append(",");
                gcCode.append(Integer.parseInt(hexCodeArray[i], 16));
            }
        } catch (Exception e) {
            throw new HexCodeConversionException("Unable to convert remaining hex code string");
        }

        return gcCode.toString();
    }

    public static String getAsHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < b.length; j++) {
            String s = String.format("%02x ", b[j] & 0xff);
            sb.append(s);
        }
        return sb.toString();
    }

    public String getIP() {
        return thing.getConfiguration().get(THING_PROPERTY_IP).toString();
    }

    public String getFlexActiveCable() {
        return thing.getConfiguration().get(THING_CONFIG_ACTIVECABLE).toString();
    }

    private String thingID() {
        // Return segments 2 & 3 only
        String s = thing.getUID().getAsString();
        return s.substring(s.indexOf(':') + 1);
    }

    /*
     * Manage the ONLINE/OFFLINE status of the thing
     */
    private void markThingOnline() {
        if (!isOnline()) {
            logger.debug("Changing status of {} from {}({}) to ONLINE", thingID(), getStatus(), getDetail());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void markThingOffline() {
        if (isOnline()) {
            logger.debug("Changing status of {} from {}({}) to OFFLINE", thingID(), getStatus(), getDetail());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void markThingOfflineWithError(ThingStatusDetail statusDetail, String statusMessage) {
        // If it's offline with no detail or if it's not offline, mark it offline with detailed status
        if ((isOffline() && getDetail().equals(ThingStatusDetail.NONE)) || !isOffline()) {
            logger.debug("Changing status of {} from {}({}) to OFFLINE({})", thingID(), getStatus(), getDetail(),
                    statusDetail);
            updateStatus(ThingStatus.OFFLINE, statusDetail, statusMessage);
            return;
        }
    }

    private boolean isOnline() {
        return thing.getStatus().equals(ThingStatus.ONLINE);
    }

    private boolean isOffline() {
        return thing.getStatus().equals(ThingStatus.OFFLINE);
    }

    private ThingStatus getStatus() {
        return thing.getStatus();
    }

    private ThingStatusDetail getDetail() {
        return thing.getStatusInfo().getStatusDetail();
    }

    /**
     * The {@link HexCodeConversionException} class is responsible for
     *
     * @author Mark Hilbush - Initial contribution
     */
    private class HexCodeConversionException extends Exception {
        private static final long serialVersionUID = -4422352677677729196L;

        public HexCodeConversionException(String message) {
            super(message);
        }
    }

    /**
     * The {@link CommandProcessor} class is responsible for handling communication with the GlobalCache
     * device. It waits for requests to arrive on a queue. When a request arrives, it sends the command to the
     * GlobalCache device, waits for a response from the device, parses the response, then responds to the caller by
     * placing a message in a response queue. Device response time is typically well below 100 ms, hence the reason
     * fgor a relatively low timeout when reading the response queue.
     *
     * @author Mark Hilbush - Initial contribution
     */
    private class CommandProcessor extends Thread {
        private Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

        private boolean terminate = false;
        private final String TERMINATE_COMMAND = "terminate";

        private final int SEND_QUEUE_MAX_DEPTH = 10;
        private final int SEND_QUEUE_TIMEOUT = 2000;

        private ConnectionManager connectionManager;

        public CommandProcessor() {
            super("GlobalCache Command Processor");
            sendQueue = new LinkedBlockingQueue<RequestMessage>(SEND_QUEUE_MAX_DEPTH);
            logger.debug("Processor for thing {} created request queue, depth={}", thingID(), SEND_QUEUE_MAX_DEPTH);
        }

        public void terminate() {
            logger.debug("Processor for thing {} is being marked ready to terminate.", thingID());

            try {
                // Send the command processor a terminate message
                sendQueue.put(new RequestMessage(TERMINATE_COMMAND, null, null, null));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                terminate = true;
            }
        }

        @Override
        public void run() {
            logger.debug("Command processor STARTING for thing {} at IP {}", thingID(), getIP());
            connectionManager = new ConnectionManager();
            connectionManager.connect();
            connectionManager.scheduleConnectionMonitorJob();
            sendQueue.clear();
            terminate = false;

            try {
                RequestMessage requestMessage;
                while (!terminate) {
                    requestMessage = sendQueue.poll(SEND_QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (requestMessage != null) {
                        if (requestMessage.getCommandName().equals(TERMINATE_COMMAND)) {
                            logger.debug("Processor for thing {} received terminate message", thingID());
                            break;
                        }

                        String deviceReply;
                        connectionManager.connect();
                        if (connectionManager.isConnected()) {
                            try {
                                long startTime = System.currentTimeMillis();
                                if (requestMessage.isCommand()) {
                                    writeCommandToDevice(requestMessage);
                                    deviceReply = readReplyFromDevice(requestMessage);
                                } else {
                                    writeSerialToDevice(requestMessage);
                                    deviceReply = "successful";
                                }
                                long endTime = System.currentTimeMillis();
                                logger.debug("Transaction '{}' for thing {} at {} took {} ms",
                                        requestMessage.getCommandName(), thingID(), getIP(), endTime - startTime);

                            } catch (IOException e) {
                                logger.error("Comm error for thing {} at {}: {}", thingID(), getIP(), e.getMessage());
                                deviceReply = "ERROR: " + e.getMessage();
                                connectionManager.setCommError(deviceReply);
                                connectionManager.disconnect();
                            }
                        } else {
                            deviceReply = "ERROR: " + "No connection to device";
                        }

                        logger.trace("Processor for thing {} queuing response message: {}", thingID(), deviceReply);
                        requestMessage.getReceiveQueue().put(new ResponseMessage(deviceReply));
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Processor for thing {} was interrupted: {}", thingID(), e.getMessage());
                Thread.currentThread().interrupt();
            }

            connectionManager.cancelConnectionMonitorJob();
            connectionManager.disconnect();
            connectionManager = null;
            logger.debug("Command processor TERMINATING for thing {} at IP {}", thingID(), getIP());
        }

        /*
         * Write the command to the device.
         */
        private void writeCommandToDevice(RequestMessage requestMessage) throws IOException {
            logger.trace("Processor for thing {} writing command to device", thingID());

            if (connectionManager.getCommandOut() == null) {
                logger.debug("Error writing to device because output stream object is null");
                return;
            }

            byte[] deviceCommand = (requestMessage.getDeviceCommand() + '\r').getBytes();
            connectionManager.getCommandOut().write(deviceCommand);
            connectionManager.getCommandOut().flush();
        }

        /*
         * Read command reply from the device, then remove the CR at the end of the line.
         */
        private String readReplyFromDevice(RequestMessage requestMessage) throws IOException {
            logger.trace("Processor for thing {} reading reply from device", thingID());

            if (connectionManager.getCommandIn() == null) {
                logger.debug("Error reading from device because input stream object is null");
                return "ERROR: BufferedReader is null!";
            }

            logger.trace("Processor for thing {} reading response from device", thingID());
            return connectionManager.getCommandIn().readLine().trim();
        }

        /*
         * Write a serial command to the device
         */
        private void writeSerialToDevice(RequestMessage requestMessage) throws IOException {
            DataOutputStream out = connectionManager.getSerialOut(requestMessage.getCommandType());
            if (out == null) {
                logger.warn("Can't send serial command; output stream is null!");
                return;
            }

            byte[] deviceCommand;
            deviceCommand = URLDecoder.decode(requestMessage.getDeviceCommand(), CHARSET).getBytes(CHARSET);

            logger.debug("Writing decoded deviceCommand byte array: {}", getAsHexString(deviceCommand));
            out.write(deviceCommand);
        }
    }

    /*
     * The {@link ConnectionManager} class is responsible for managing the state of the connections to the
     * command port and the serial port(s) of the device.
     *
     * @author Mark Hilbush - Initial contribution
     */
    private class ConnectionManager {
        private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

        private DeviceConnection commandConnection;
        private DeviceConnection serialPort1Connection;
        private DeviceConnection serialPort2Connection;

        private SerialPortReader serialReaderPort1;
        private SerialPortReader serialReaderPort2;

        private boolean deviceIsConnected;

        private final String COMMAND_NAME = "command";
        private final String SERIAL1_NAME = "serial-1";
        private final String SERIAL2_NAME = "serial-2";

        private final int COMMAND_PORT = 4998;
        private final int SERIAL1_PORT = 4999;
        private final int SERIAL2_PORT = 5000;

        private final int SOCKET_CONNECT_TIMEOUT = 1500;

        private ScheduledFuture<?> connectionMonitorJob;
        private final int CONNECTION_MONITOR_FREQUENCY = 60;
        private final int CONNECTION_MONITOR_START_DELAY = 15;

        private Runnable connectionMonitorRunnable = new Runnable() {
            @Override
            public void run() {
                logger.trace("Performing connection check for thing {} at IP {}", thingID(), commandConnection.getIP());
                checkConnection();
            }
        };

        public ConnectionManager() {
            commandConnection = new DeviceConnection(COMMAND_NAME, COMMAND_PORT);
            serialPort1Connection = new DeviceConnection(SERIAL1_NAME, SERIAL1_PORT);
            serialPort2Connection = new DeviceConnection(SERIAL2_NAME, SERIAL2_PORT);

            commandConnection.setIP(getIPAddress());
            serialPort1Connection.setIP(getIPAddress());
            serialPort2Connection.setIP(getIPAddress());

            deviceIsConnected = false;
        }

        private String getIPAddress() {
            String ipAddress = ((GlobalCacheHandler) thing.getHandler()).getIP();
            if (StringUtils.isEmpty(ipAddress)) {
                logger.debug("Handler for thing {} could not get IP address from config", thingID());
                markThingOfflineWithError(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "IP address not set");
            }
            return ipAddress;
        }

        /*
         * Connect to the command and serial port(s) on the device. The serial connections are established only for
         * devices that support serial.
         */
        protected void connect() {
            if (isConnected()) {
                return;
            }

            // Get a connection to the command port
            if (!commandConnect(commandConnection)) {
                return;
            }

            // Get a connection to serial port 1
            if (deviceSupportsSerialPort1()) {
                if (!serialConnect(serialPort1Connection)) {
                    commandDisconnect(commandConnection);
                    return;
                }
            }

            // Get a connection to serial port 2
            if (deviceSupportsSerialPort2()) {
                if (!serialConnect(serialPort2Connection)) {
                    commandDisconnect(commandConnection);
                    serialDisconnect(serialPort1Connection);
                    return;
                }
            }

            /*
             * All connections opened successfully, so we can mark the thing online
             * and start the serial port readers
             */
            markThingOnline();
            deviceIsConnected = true;
            startSerialPortReaders();
        }

        private boolean commandConnect(DeviceConnection conn) {
            logger.debug("Connecting to {} port for thing {} at IP {}", conn.getName(), thingID(), conn.getIP());
            if (!openSocket(conn)) {
                return false;
            }
            // create streams
            try {
                conn.setCommandIn(new BufferedReader(new InputStreamReader(conn.getSocket().getInputStream())));
                conn.setCommandOut(new DataOutputStream(conn.getSocket().getOutputStream()));
            } catch (IOException e) {
                logger.debug("Error getting streams to {} port for thing {} at {}, exception={}", conn.getName(),
                        thingID(), conn.getIP(), e.getMessage());
                markThingOfflineWithError(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                closeSocket(conn);
                return false;
            }
            logger.info("Got a connection to {} port for thing {} at {}", conn.getName(), thingID(), conn.getIP());

            return true;
        }

        private boolean serialConnect(DeviceConnection conn) {
            logger.debug("Connecting to {} port for thing {} at {}", conn.getName(), thingID(), conn.getIP());
            if (!openSocket(conn)) {
                return false;
            }
            // create streams
            try {
                conn.setSerialIn(new BufferedInputStream(conn.getSocket().getInputStream()));
                conn.setSerialOut(new DataOutputStream(conn.getSocket().getOutputStream()));
            } catch (IOException e) {
                logger.debug("Failed to get streams on {} port for thing {} at {}", conn.getName(), thingID(),
                        conn.getIP());
                closeSocket(conn);
                return false;
            }
            logger.info("Got a connection to {} port for thing {} at {}", conn.getName(), thingID(), conn.getIP());

            return true;
        }

        private boolean openSocket(DeviceConnection conn) {
            try {
                conn.setSocket(new Socket());
                conn.getSocket().bind(new InetSocketAddress(ifAddress, 0));
                conn.getSocket().connect(new InetSocketAddress(conn.getIP(), conn.getPort()), SOCKET_CONNECT_TIMEOUT);
            } catch (IOException e) {
                logger.debug("Failed to get socket on {} port for thing {} at {}", conn.getName(), thingID(),
                        conn.getIP());
                return false;
            }
            return true;
        }

        private void closeSocket(DeviceConnection conn) {
            if (conn.getSocket() != null) {
                try {
                    conn.getSocket().close();
                } catch (IOException e) {
                    logger.debug("Failed to close socket on {} port for thing {} at {}", conn.getName(), thingID(),
                            conn.getIP());
                }
            }
        }

        /*
         * Disconnect from the command and serial port(s) on the device. Only disconnect the serial port
         * connections if the devices have serial ports.
         */
        protected void disconnect() {
            if (!isConnected()) {
                return;
            }
            commandDisconnect(commandConnection);

            stopSerialPortReaders();
            if (deviceSupportsSerialPort1()) {
                serialDisconnect(serialPort1Connection);
            }
            if (deviceSupportsSerialPort2()) {
                serialDisconnect(serialPort2Connection);
            }

            markThingOffline();
            deviceIsConnected = false;
        }

        private void commandDisconnect(DeviceConnection conn) {
            deviceDisconnect(conn);
        }

        private void serialDisconnect(DeviceConnection conn) {
            deviceDisconnect(conn);
        }

        private void deviceDisconnect(DeviceConnection conn) {
            logger.debug("Disconnecting from {} port for thing {} at IP {}", conn.getName(), thingID(), conn.getIP());

            try {
                if (conn.getSerialOut() != null) {
                    conn.getSerialOut().close();
                }
                if (conn.getSerialIn() != null) {
                    conn.getSerialIn().close();
                }
                if (conn.getSocket() != null) {
                    conn.getSocket().close();
                }
            } catch (IOException e) {
                logger.debug("Error closing {} port for thing {} at IP {}: exception={}", conn.getName(), thingID(),
                        conn.getIP(), e.getMessage());
            }
            conn.reset();
        }

        private boolean isConnected() {
            return deviceIsConnected;
        }

        public void setCommError(String errorMessage) {
            markThingOfflineWithError(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMessage);
        }

        /*
         * Retrieve the input/output streams for command and serial connections.
         */
        protected BufferedReader getCommandIn() {
            return commandConnection.getCommandIn();
        }

        protected DataOutputStream getCommandOut() {
            return commandConnection.getCommandOut();
        }

        protected BufferedInputStream getSerialIn(CommandType commandType) {
            if (commandType != CommandType.SERIAL1 && commandType != CommandType.SERIAL2) {
                return null;
            }
            if (commandType == CommandType.SERIAL1) {
                return serialPort1Connection.getSerialIn();
            } else {
                return serialPort2Connection.getSerialIn();
            }
        }

        protected DataOutputStream getSerialOut(CommandType commandType) {
            if (commandType != CommandType.SERIAL1 && commandType != CommandType.SERIAL2) {
                return null;
            }
            if (commandType == CommandType.SERIAL1) {
                return serialPort1Connection.getSerialOut();
            } else {
                return serialPort2Connection.getSerialOut();
            }
        }

        private boolean deviceSupportsSerialPort1() {
            ThingTypeUID typeUID = thing.getThingTypeUID();

            if (typeUID.equals(THING_TYPE_ITACH_SL)) {
                return true;
            } else if (typeUID.equals(THING_TYPE_GC_100_06) || typeUID.equals(THING_TYPE_GC_100_12)) {
                return true;
            } else if (typeUID.equals(THING_TYPE_ITACH_FLEX) && getFlexActiveCable().equals(ACTIVE_CABLE_SERIAL)) {
                return true;
            }
            return false;
        }

        private boolean deviceSupportsSerialPort2() {
            if (thing.getThingTypeUID().equals(THING_TYPE_GC_100_12)) {
                return true;
            }
            return false;
        }

        /*
         * Periodically validate the command connection to the device by executing a getversion command.
         */
        private void scheduleConnectionMonitorJob() {
            logger.debug("Starting connection monitor job for thing {} at IP {}", thingID(), commandConnection.getIP());
            connectionMonitorJob = scheduler.scheduleWithFixedDelay(connectionMonitorRunnable,
                    CONNECTION_MONITOR_START_DELAY, CONNECTION_MONITOR_FREQUENCY, TimeUnit.SECONDS);
        }

        private void cancelConnectionMonitorJob() {
            if (connectionMonitorJob != null) {
                logger.debug("Canceling connection monitor job for thing {} at IP {}", thingID(),
                        commandConnection.getIP());
                connectionMonitorJob.cancel(true);
                connectionMonitorJob = null;
            }
        }

        private void checkConnection() {
            CommandGetversion getversion = new CommandGetversion(thing, sendQueue);
            getversion.executeQuiet();

            if (getversion.isSuccessful()) {
                logger.trace("Connection check successful for thing {} at IP {}", thingID(), commandConnection.getIP());
                markThingOnline();
                deviceIsConnected = true;
            } else {
                logger.debug("Connection check failed for thing {} at IP {}", thingID(), commandConnection.getIP());
                disconnect();
            }
        }

        private void startSerialPortReaders() {
            if (deviceSupportsSerialPort1()) {
                serialReaderPort1 = startSerialPortReader(CommandType.SERIAL1, CONFIG_ENABLE_TWO_WAY_PORT_1,
                        CONFIG_END_OF_MESSAGE_DELIMITER_PORT_1);
            }
            if (deviceSupportsSerialPort2()) {
                serialReaderPort2 = startSerialPortReader(CommandType.SERIAL2, CONFIG_ENABLE_TWO_WAY_PORT_2,
                        CONFIG_END_OF_MESSAGE_DELIMITER_PORT_2);
            }
        }

        private SerialPortReader startSerialPortReader(CommandType serialDevice, String enableTwoWayConfig,
                String endOfMessageDelimiterConfig) {
            Boolean enableTwoWay = (Boolean) thing.getConfiguration().get(enableTwoWayConfig);
            logger.debug("Enable two-way is {} for thing {} {}", enableTwoWay, thingID(), serialDevice);

            if (Boolean.TRUE.equals(enableTwoWay)) {
                // Get the end of message delimiter from the config, URL decode it, and convert it to a byte array
                String endOfMessageString = (String) thing.getConfiguration().get(endOfMessageDelimiterConfig);
                if (StringUtils.isNotEmpty(endOfMessageString)) {
                    logger.debug("End of message is {} for thing {} {}", endOfMessageString, thingID(), serialDevice);
                    byte[] endOfMessage;
                    try {
                        endOfMessage = URLDecoder.decode(endOfMessageString, CHARSET).getBytes(CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        logger.info("Unable to decode end of message delimiter {} for thing {} {}", endOfMessageString,
                                thingID(), serialDevice);
                        return null;
                    }

                    // Start the serial reader using the above end-of-message delimiter
                    SerialPortReader serialPortReader = new SerialPortReader(serialDevice, getSerialIn(serialDevice),
                            endOfMessage);
                    serialPortReader.start();
                    return serialPortReader;
                } else {
                    logger.warn("End of message delimiter is not defined in configuration of thing {}", thingID());
                }
            }
            return null;
        }

        private void stopSerialPortReaders() {
            if (deviceSupportsSerialPort1() && serialReaderPort1 != null) {
                logger.debug("Stopping serial port 1 reader for thing {} at IP {}", thingID(),
                        commandConnection.getIP());
                serialReaderPort1.stop();
                serialReaderPort1 = null;
            }
            if (deviceSupportsSerialPort2() && serialReaderPort2 != null) {
                logger.debug("Stopping serial port 2 reader for thing {} at IP {}", thingID(),
                        commandConnection.getIP());
                serialReaderPort2.stop();
                serialReaderPort2 = null;
            }
        }
    }

    /*
     * The {@link SerialReader} class reads data from the serial connection. When data is
     * received, the receive channel is updated with the data. Data is read up to the
     * end-of-message delimiter defined in the Thing configuration.
     *
     * @author Mark Hilbush - Initial contribution
     */
    private class SerialPortReader {
        private Logger logger = LoggerFactory.getLogger(SerialPortReader.class);

        private CommandType serialPort;
        private BufferedInputStream serialPortIn;
        private ScheduledFuture<?> serialPortReaderJob;
        private boolean terminateSerialPortReader;

        private byte[] endOfMessage;

        private Runnable serialPortReaderRunnable = new Runnable() {
            @Override
            public void run() {
                serialPortReader();
            }
        };

        SerialPortReader(CommandType serialPort, BufferedInputStream serialIn, byte[] endOfMessage) {
            if (serialIn == null) {
                throw new IllegalArgumentException("Serial input stream is not set");
            }
            this.serialPort = serialPort;
            this.serialPortIn = serialIn;
            this.endOfMessage = endOfMessage;
            serialPortReaderJob = null;
            terminateSerialPortReader = false;
        }

        public void start() {
            serialPortReaderJob = scheduledExecutorService.schedule(serialPortReaderRunnable, 0, TimeUnit.SECONDS);
        }

        public void stop() {
            if (serialPortReaderJob != null) {
                terminateSerialPortReader = true;
                serialPortReaderJob.cancel(true);
                serialPortReaderJob = null;
            }
        }

        private void serialPortReader() {
            logger.info("Serial reader RUNNING for {} on {}:{}", thingID(), getIP(), serialPort);

            while (!terminateSerialPortReader) {
                byte[] buffer;
                try {
                    buffer = readUntilEndOfMessage(endOfMessage);
                    if (buffer == null) {
                        logger.debug("Received end-of-stream from {} on {}", getIP(), serialPort);
                        continue;
                    }
                    logger.debug("Rcv data from {} at {}:{}: {}", thingID(), getIP(), serialPort,
                            getAsHexString(buffer));
                    updateFeedbackChannel(buffer);
                } catch (UnsupportedEncodingException e) {
                    logger.info("Unsupported encoding exception: {}", e.getMessage(), e);
                    continue;
                } catch (IOException e) {
                    logger.debug("Serial Reader got IOException: {}", e.getMessage());
                    break;
                } catch (InterruptedException e) {
                    logger.debug("Serial Reader got InterruptedException: {}", e.getMessage());
                    break;
                }
            }
            logger.debug("Serial reader STOPPING for {} on {}:{}", thingID(), getIP(), serialPort);
        }

        private byte[] readUntilEndOfMessage(byte[] endOfMessageDelimiter) throws IOException, InterruptedException {
            logger.debug("Serial reader waiting for available data");

            int val;
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            // Read from the serial input stream until the endOfMessage delimiter is found
            while (true) {
                val = serialPortIn.read();
                if (val == -1) {
                    logger.debug("Serial reader got unexpected end of input stream");
                    throw new IOException("Unexpected end of stream");
                }

                buf.write(val);
                if (findEndOfMessage(buf.toByteArray(), endOfMessageDelimiter)) {
                    // Found the end-of-message delimiter in the serial input stream
                    break;
                }
            }
            logger.debug("Serial reader returning a message");
            return buf.toByteArray();
        }

        private boolean findEndOfMessage(byte[] buf, byte[] endOfMessage) {
            int lengthEOM = endOfMessage.length;
            int lengthBuf = buf.length;

            // Look for the end-of-message delimiter at the end of the buffer
            while (lengthEOM > 0) {
                lengthEOM--;
                lengthBuf--;
                if (lengthBuf < 0 || endOfMessage[lengthEOM] != buf[lengthBuf]) {
                    // No match on end of message
                    return false;
                }
            }
            logger.debug("Serial reader found the end-of-message delimiter in the input buffer");
            return true;
        }

        private void updateFeedbackChannel(byte[] buffer) {
            String channelId;
            if (serialPort.equals(CommandType.SERIAL1)) {
                channelId = CHANNEL_SL_M1_RECEIVE;
            } else if (serialPort.equals(CommandType.SERIAL2)) {
                channelId = CHANNEL_SL_M2_RECEIVE;
            } else {
                logger.warn("Unknown serial port; can't update feedback channel: {}", serialPort);
                return;
            }
            Channel channel = getThing().getChannel(channelId);
            if (channel != null && isLinked(channelId)) {
                logger.debug("Updating feedback channel for port {}", serialPort);
                try {
                    String encodedReply = URLEncoder.encode(new String(buffer, CHARSET), CHARSET);
                    logger.debug("encodedReply='{}'", encodedReply);
                    updateState(channel.getUID(), new StringType(encodedReply));
                } catch (UnsupportedEncodingException e) {
                    logger.warn("Exception while encoding data read from serial device: {}", e.getMessage());
                }
            }
        }
    }

    /*
     * The {@link DeviceConnection} class stores information about the connection to a globalcache device.
     * There can be two types of connections, command and serial. The command connection is used to
     * send all but the serial strings to the device. The serial connection is used exclusively to
     * send serial messages. These serial connections are applicable only to iTach SL and GC-100 devices.
     *
     * @author Mark Hilbush - Initial contribution
     */
    private class DeviceConnection {
        private String connectionName;
        private int port;
        private String ipAddress;
        private Socket socket;
        private BufferedReader commandIn;
        private DataOutputStream commandOut;
        private BufferedInputStream serialIn;
        private DataOutputStream serialOut;

        DeviceConnection(String connectionName, int port) {
            setName(connectionName);
            setPort(port);
            setIP(null);
            setSocket(null);
            setCommandIn(null);
            setCommandOut(null);
            setSerialIn(null);
            setSerialOut(null);
        }

        public void reset() {
            setSocket(null);
            setCommandIn(null);
            setCommandOut(null);
            setSerialIn(null);
            setSerialOut(null);
        }

        public String getName() {
            return connectionName;
        }

        public void setName(String connectionName) {
            this.connectionName = connectionName;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getIP() {
            return ipAddress;
        }

        public void setIP(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public Socket getSocket() {
            return socket;
        }

        public void setSocket(Socket socket) {
            this.socket = socket;
        }

        public BufferedReader getCommandIn() {
            return commandIn;
        }

        public void setCommandIn(BufferedReader commandIn) {
            this.commandIn = commandIn;
        }

        public DataOutputStream getCommandOut() {
            return commandOut;
        }

        public void setCommandOut(DataOutputStream commandOut) {
            this.commandOut = commandOut;
        }

        public BufferedInputStream getSerialIn() {
            return serialIn;
        }

        public void setSerialIn(BufferedInputStream serialIn) {
            this.serialIn = serialIn;
        }

        public DataOutputStream getSerialOut() {
            return serialOut;
        }

        public void setSerialOut(DataOutputStream serialOut) {
            this.serialOut = serialOut;
        }
    }
}
