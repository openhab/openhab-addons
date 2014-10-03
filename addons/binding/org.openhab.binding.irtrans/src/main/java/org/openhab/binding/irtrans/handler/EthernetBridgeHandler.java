/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.irtrans.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.irtrans.IRcommand;
import org.openhab.binding.irtrans.IRtransBindingConstants;
import org.openhab.binding.irtrans.IRtransBindingConstants.Led;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EthernetBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @since 2.1.0
 *
 */
public class EthernetBridgeHandler extends BaseBridgeHandler implements TransceiverStatusListener {

    // List of Configuration constants
    public static final String BUFFER_SIZE = "bufferSize";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String IS_LISTENER = "isListener";
    public static final String FIRMWARE_VERSION = "firmwareVersion";
    public static final String LISTENER_PORT = "listenerPort";
    public static final String MODE = "mode";
    public static final String PING_TIME_OUT = "pingTimeOut";
    public static final String PORT_NUMBER = "portNumber";
    public static final String RECONNECT_INTERVAL = "reconnectInterval";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String RESPONSE_TIME_OUT = "responseTimeOut";
    public static final String COMMAND = "command";
    public static final String LED = "led";
    public static final String REMOTE = "remote";

    private Logger logger = LoggerFactory.getLogger(EthernetBridgeHandler.class);

    private Selector selector;
    private SocketChannel socketChannel;
    protected SelectionKey socketChannelKey;
    protected ServerSocketChannel listenerChannel;
    protected SelectionKey listenerKey;
    protected boolean previousConnectionState;
    private final Lock lock = new ReentrantLock();

    private List<TransceiverStatusListener> transceiverStatusListeners = new CopyOnWriteArrayList<>();

    private ScheduledFuture<?> pollingJob;

    /**
     * Data structure to store the infrared commands that are 'loaded' from the
     * configuration files. Command loading from pre-defined configuration files is not supported
     * (anymore), but the code is maintained in case this functionality is re-added in the future
     **/
    protected final Collection<IRcommand> irCommands = new HashSet<IRcommand>();

    public EthernetBridgeHandler(Bridge bridge) {
        super(bridge);
        // Nothing to do here
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (!(command instanceof RefreshType)) {
            if (channelUID != null) {
                Channel channel = this.getThing().getChannel(channelUID.getId());
                if (channel != null) {
                    Configuration channelConfiguration = channel.getConfiguration();
                    if (channel.getAcceptedItemType().equals(IRtransBindingConstants.BLASTER_CHANNEL_TYPE)) {
                        if (command instanceof StringType) {
                            String remoteName = StringUtils.substringBefore(command.toString(), ",");
                            String irCommandName = StringUtils.substringAfter(command.toString(), ",");

                            IRcommand ircommand = new IRcommand();
                            ircommand.remote = remoteName;
                            ircommand.command = irCommandName;

                            IRcommand thingCompatibleCommand = new IRcommand();
                            thingCompatibleCommand.remote = (String) channelConfiguration.get(REMOTE);
                            thingCompatibleCommand.command = (String) channelConfiguration.get(COMMAND);

                            if (ircommand.matches(thingCompatibleCommand)) {
                                if (sendIRcommand(ircommand, Led.get((String) channelConfiguration.get(LED)))) {
                                    logger.debug("Sent a matching infrared command '{}' for channel '{}'", command,
                                            channelUID);
                                } else {
                                    logger.warn(
                                            "An error occured whilst sending the infrared command '{}' for Channel '{}'",
                                            command, channelUID);
                                }
                            }

                        }
                    }

                    if (channel.getAcceptedItemType().equals(IRtransBindingConstants.RECEIVER_CHANNEL_TYPE)) {
                        logger.warn("Receivers can only receive infrared commands, not send them");
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing IRtrans Ethernet handler.");

        // register ourselves as a Transceiver Status Listener
        registerTransceiverStatusListener(this);

        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error("An exception occurred while registering the selector: '{}'", e.getMessage());
        }

        if (selector != null) {
            if (getConfig().get(IP_ADDRESS) != null && getConfig().get(PORT_NUMBER) != null) {
                onUpdate();
            } else {
                logger.warn("Cannot connect to IRtrans Ethernet device. IP address or port number not set.");
            }

            if (getConfig().get(IS_LISTENER) != null) {
                configureListener((String) getConfig().get(LISTENER_PORT));
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        unregisterTransceiverStatusListener(this);

        disconnect(socketChannel);

        try {
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            logger.error("An exception occurred while closing the selector: '{}'", e.getMessage());
        }

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void establishConnection() {
        lock.lock();
        try {
            if (getConfig().get(IP_ADDRESS) != null && getConfig().get(PORT_NUMBER) != null) {
                socketChannel = connect((String) getConfig().get(IP_ADDRESS),
                        ((BigDecimal) getConfig().get(PORT_NUMBER)).intValue());
                try {
                    Thread.sleep(((BigDecimal) getConfig().get(RESPONSE_TIME_OUT)).intValue());
                } catch (NumberFormatException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("An exception occurred while putting a thread to sleep: '{}'", e.getMessage());
                }
                onConnectable(socketChannel);
            }
        } finally {
            lock.unlock();
        }
    }

    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 0,
                    ((BigDecimal) getConfig().get(REFRESH_INTERVAL)).intValue(), TimeUnit.MILLISECONDS);
        }
    }

    public void onConnectionLost() {
        logger.debug("Updating thing status to OFFLINE.");
        updateStatus(ThingStatus.OFFLINE);
        for (TransceiverStatusListener listener : transceiverStatusListeners) {
            listener.onBridgeDisconnected(this);
        }
        establishConnection();
    }

    public void onConnectionResumed() {
        logger.debug("Updating thing status to ONLINE.");
        configureTransceiver(socketChannel);
        updateStatus(ThingStatus.ONLINE);
        for (TransceiverStatusListener listener : transceiverStatusListeners) {
            listener.onBridgeConnected(this);
        }
    }

    private void configureListener(String listenerPort) {
        try {
            listenerChannel = ServerSocketChannel.open();
            listenerChannel.socket().bind(new InetSocketAddress(Integer.parseInt(listenerPort)));
            listenerChannel.configureBlocking(false);

            logger.info("Listening for incoming connections on {}", listenerChannel.getLocalAddress());

            synchronized (selector) {
                selector.wakeup();
                try {
                    listenerKey = listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
                } catch (ClosedChannelException e1) {
                    logger.error("An exception occurred while registering a selector: '{}'", e1.getMessage());
                }
            }
        } catch (IOException e3) {
            logger.error(
                    "An exception occurred while creating configuring the listener channel on port number {}: '{}'",
                    Integer.parseInt(listenerPort), e3.getMessage());
        }
    }

    protected void configureTransceiver(SocketChannel socketChannel) {
        lock.lock();
        try {

            String putInASCIImode = "ASCI";
            ByteBuffer response = sendCommand(putInASCIImode);

            String getFirmwareVersion = "Aver" + (char) 13;
            response = sendCommand(getFirmwareVersion);

            if (response != null) {
                String message = stripByteCount(response).split("\0")[0];
                if (message != null) {
                    if (message.contains("VERSION")) {
                        logger.info("'{}' matches an IRtrans device with firmware {}", getThing().getUID(), message);
                        getConfig().put(FIRMWARE_VERSION, message);
                    } else {
                        logger.warn("Received some non-compliant garbage ({})", message);
                        disconnect(socketChannel);
                    }
                }
            } else {
                try {
                    logger.warn("Did not receive an answer from the IRtrans transceiver '{}' - Parsing is skipped",
                            socketChannel.getRemoteAddress());
                    disconnect(socketChannel);
                } catch (IOException e1) {
                    logger.debug("An exception occurred while getting a remote address: '{}'", e1.getMessage());
                }
            }

            int numberOfRemotes = 0;
            int numberOfRemotesProcessed = 0;
            int numberOfRemotesInBatch = 0;
            String[] remoteList = getRemoteList(0);

            if (remoteList.length > 0) {
                logger.info("The IRtrans device for '{}' supports '{}' remotes", getThing().getUID(), remoteList[1]);
                numberOfRemotes = Integer.valueOf(remoteList[1]);
                numberOfRemotesInBatch = Integer.valueOf(remoteList[2]);
            }

            while (numberOfRemotesProcessed < numberOfRemotes) {
                for (int i = 1; i <= numberOfRemotesInBatch; i++) {
                    String remote = remoteList[2 + i];

                    // get remote commands
                    String[] commands = getRemoteCommands(remote, 0);
                    String resultString = new String();
                    int numberOfCommands = 0;
                    int numberOfCommandsInBatch = 0;
                    int numberOfCommandsProcessed = 0;

                    if (commands.length > 0) {
                        numberOfCommands = Integer.valueOf(commands[1]);
                        numberOfCommandsInBatch = Integer.valueOf(commands[2]);
                        numberOfCommandsProcessed = 0;
                    }

                    while (numberOfCommandsProcessed < numberOfCommands) {
                        for (int j = 1; j <= numberOfCommandsInBatch; j++) {
                            String command = commands[2 + j];
                            resultString = resultString + command;
                            numberOfCommandsProcessed++;
                            if (numberOfCommandsProcessed < numberOfCommands) {
                                resultString = resultString + ", ";
                            }
                        }

                        if (numberOfCommandsProcessed < numberOfCommands) {
                            commands = getRemoteCommands(remote, numberOfCommandsProcessed);
                            if (commands.length == 0) {
                                break;
                            }
                            numberOfCommandsInBatch = Integer.valueOf(commands[2]);
                        } else {
                            numberOfCommandsInBatch = 0;
                        }

                    }

                    logger.info("The remote '{}' on '{}' supports '{}' commands: {}", remote, getThing().getUID(),
                            numberOfCommands, resultString);

                    numberOfRemotesProcessed++;
                }

                // get next batch
                if (numberOfRemotesProcessed < numberOfRemotes) {
                    remoteList = getRemoteList(numberOfRemotesProcessed);
                    if (remoteList.length == 0) {
                        break;
                    }
                    numberOfRemotesInBatch = Integer.valueOf(remoteList[2]);
                } else {
                    numberOfRemotesInBatch = 0;
                }

            }

        } finally {
            lock.unlock();
        }
    }

    private String[] getRemoteCommands(String remote, int index) {

        String getCommands = "Agetcommands " + remote + "," + index + (char) 13;
        ByteBuffer response = sendCommand(getCommands);
        String[] commandList = new String[0];

        if (response != null) {
            String message = stripByteCount(response).split("\0")[0];
            logger.trace("commands returned {}", message);
            if (message != null) {
                if (message.contains("COMMANDLIST")) {
                    commandList = message.split(",");
                } else {
                    logger.warn("Received some non-compliant command ({})", message);
                    disconnect(socketChannel);
                }
            }
        } else {
            logger.warn("Did not receive an answer from the IRtrans transceiver for '{}' - Parsing is skipped",
                    getThing().getUID());
            disconnect(socketChannel);
        }

        return commandList;
    }

    private String[] getRemoteList(int index) {

        String getRemotes = "Agetremotes " + index + (char) 13;
        ByteBuffer response = sendCommand(getRemotes);
        String[] remoteList = new String[0];

        if (response != null) {
            String message = stripByteCount(response).split("\0")[0];
            logger.trace("remotes returned {}", message);
            if (message != null) {
                if (message.contains("REMOTELIST")) {
                    remoteList = message.split(",");
                } else {
                    logger.warn("Received some non-compliant command ({})", message);
                    disconnect(socketChannel);
                }
            }
        } else {
            logger.warn("Did not receive an answer from the IRtrans transceiver for '{}' - Parsing is skipped",
                    getThing().getUID());
            disconnect(socketChannel);
        }

        return remoteList;
    }

    private ByteBuffer sendCommand(String command) {

        if (command != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(command.getBytes().length);
            try {
                byteBuffer.put(command.getBytes("ASCII"));
                onWritable(byteBuffer, socketChannel);
                Thread.sleep(((BigDecimal) getConfig().get(RESPONSE_TIME_OUT)).intValue());
                return onReadable(socketChannel, ((BigDecimal) getConfig().get(BUFFER_SIZE)).intValue());
            } catch (UnsupportedEncodingException | NumberFormatException | InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("An exception occurred while configurting the IRtrans transceiver for '{}': {}",
                        getThing().getUID(), e.getMessage());
            }
        }

        return null;
    }

    @SuppressWarnings("null")
    private SocketChannel connect(String ipAddress, int portNumber) {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.socket().setKeepAlive(true);
            socketChannel.configureBlocking(false);

            while (selector == null) {
                Thread.sleep(100);
            }

            synchronized (selector) {
                selector.wakeup();
                int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT;
                socketChannelKey = socketChannel.register(selector, interestSet);
            }

            InetSocketAddress remoteAddress = new InetSocketAddress(ipAddress, portNumber);
            logger.trace("Connecting the channel for {} ", remoteAddress);
            socketChannel.connect(remoteAddress);
        } catch (IOException e) {
            logger.error("An exception occurred while connecting connecting to '{}:{}' : {}", ipAddress, portNumber,
                    e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("An exception occurred while putting the Thread to sleep: {}", e.getMessage(), e);
        }

        return socketChannel;
    }

    private void disconnect(SocketChannel socketChannel) {
        logger.trace("Disconnecting the socket channel '{}'", socketChannel);
        try {
            socketChannel.close();
        } catch (IOException e) {
            logger.warn("An exception occurred while closing the channel '{}': {}", socketChannel, e.getMessage());
        }
    }

    protected void onAcceptable(ServerSocketChannel listenerChannel) {
        lock.lock();
        try {
            synchronized (selector) {
                try {
                    selector.selectNow();
                } catch (IOException e) {
                    logger.error("An exception occurred while selecting: {}", e.getMessage());
                }
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selKey = it.next();
                it.remove();
                if (selKey.isValid()) {
                    if (selKey.isAcceptable() && selKey == listenerChannel.keyFor(selector)) {
                        try {
                            SocketChannel newChannel = listenerChannel.accept();
                            newChannel.configureBlocking(false);
                            logger.trace("Received a connection request from '{}'", newChannel.getRemoteAddress());

                            synchronized (selector) {
                                selector.wakeup();
                                newChannel.register(selector, newChannel.validOps());
                            }
                        } catch (IOException e) {
                            logger.error("An exception occurred while accepting a connection on channel '{}': {}",
                                    listenerChannel, e.getMessage());
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected ByteBuffer onReadable(SocketChannel theChannel, int bufferSize) {
        lock.lock();
        try {

            synchronized (selector) {
                try {
                    selector.selectNow();
                } catch (IOException e) {
                    logger.error("An exception occurred while selecting: {}", e.getMessage());
                }
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selKey = it.next();
                it.remove();
                if (selKey.isValid() && selKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selKey.channel();

                    if (socketChannel.equals(theChannel) || theChannel == null) {
                        ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
                        int numberBytesRead = 0;
                        boolean error = false;

                        try {
                            numberBytesRead = socketChannel.read(readBuffer);
                        } catch (NotYetConnectedException e) {
                            logger.warn("The channel '{}' is not yet connected: {}", socketChannel, e.getMessage());
                            if (!socketChannel.isConnectionPending()) {
                                error = true;
                            }
                        } catch (IOException e) {
                            // If some other I/O error occurs
                            logger.warn("An IO exception occured on chanel '{}': {}", socketChannel, e.getMessage());
                            error = true;
                        }

                        if (numberBytesRead == -1) {
                            error = true;
                        }

                        if (error) {
                            logger.debug("Disconnecting '{}' because of a socket error", getThing().getUID());
                            disconnect(socketChannel);
                        } else {
                            readBuffer.flip();
                            return readBuffer;
                        }
                    }
                }
            }

            return null;
        } finally {
            lock.unlock();
        }
    }

    protected void onWritable(ByteBuffer buffer, SocketChannel theChannel) {
        lock.lock();
        try {
            synchronized (selector) {
                try {
                    selector.selectNow();
                } catch (IOException e) {
                    logger.error("An exception occurred while selecting: {}", e.getMessage());
                }
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selKey = it.next();
                it.remove();
                if (selKey.isValid() && selKey.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) selKey.channel();

                    if (socketChannel.equals(theChannel) || theChannel == null) {
                        boolean error = false;

                        buffer.rewind();
                        try {
                            logger.trace("Sending '{}' on the channel '{}'->'{}'", new String(buffer.array()),
                                    socketChannel.getLocalAddress(), socketChannel.getRemoteAddress());
                            socketChannel.write(buffer);
                        } catch (NotYetConnectedException e) {
                            logger.warn("The channel '{}' is not yet connected: {}", socketChannel, e.getMessage());
                            if (!socketChannel.isConnectionPending()) {
                                error = true;
                            }
                        } catch (ClosedChannelException e) {
                            // If some other I/O error occurs
                            logger.warn("The channel for '{}' is closed: {}", socketChannel, e.getMessage());
                            error = true;
                        } catch (IOException e) {
                            // If some other I/O error occurs
                            logger.warn("An IO exception occured on chanel '{}': {}", socketChannel, e.getMessage());
                            error = true;
                        }

                        if (error) {
                            disconnect(socketChannel);
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected void onConnectable(SocketChannel theChannel) {
        lock.lock();
        SocketChannel socketChannel = null;
        try {
            synchronized (selector) {
                selector.selectNow();
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selKey = it.next();
                it.remove();
                if (selKey.isValid() && selKey.isConnectable()) {
                    socketChannel = (SocketChannel) selKey.channel();

                    if (socketChannel.equals(theChannel) || theChannel == null) {
                        socketChannel.finishConnect();
                        logger.trace("The channel for '{}' is connected", socketChannel.getRemoteAddress());
                    }
                }
            }
        } catch (IOException | NoConnectionPendingException e) {
            if (socketChannel != null) {
                logger.debug("Disconnecting '{}' because of a socket error : '{}'", getThing().getUID(), e.getMessage(),
                        e);
                disconnect(socketChannel);
            }
        } finally {
            lock.unlock();
        }
    }

    protected int getByteCount(ByteBuffer byteBuffer) {
        Pattern reponsePattern = Pattern.compile("..(\\d{5}) (.*)", Pattern.DOTALL);

        String response = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        response = StringUtils.chomp(response);

        Matcher matcher = reponsePattern.matcher(response);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 0;
    }

    protected String stripByteCount(ByteBuffer byteBuffer) {
        Pattern responsePattern = Pattern.compile("..(\\d{5}) (.*)", Pattern.DOTALL);
        String message = null;

        String response = new String(byteBuffer.array(), 0, byteBuffer.limit());
        response = StringUtils.chomp(response);

        Matcher matcher = responsePattern.matcher(response);
        if (matcher.matches()) {
            String byteCountAsString = matcher.group(1);
            int byteCount = Integer.parseInt(byteCountAsString);
            message = matcher.group(2);
        }

        return message;
    }

    public boolean sendIRcommand(IRcommand command, Led led) {
        // construct the string we need to send to the IRtrans device
        String output = packIRDBCommand(led, command);

        lock.lock();
        try {
            ByteBuffer response = sendCommand(output);

            if (response != null) {
                String message = stripByteCount(response).split("\0")[0];
                if (message != null && message.contains("RESULT OK")) {
                    return true;
                } else {
                    logger.debug("Received an unexpected response from the IRtrans transceiver: '{}'", message);
                    return false;
                }
            }
        } finally {
            lock.unlock();
        }

        return false;
    }

    protected void parseOKMessage(String message) {
        // Nothing to do here
    }

    protected void parseHexMessage(String message) {
        Pattern hexPattern = Pattern.compile("RCV_HEX (.*)");
        Matcher matcher = hexPattern.matcher(message);

        if (matcher.matches()) {
            String command = matcher.group(1);

            IRcommand theCommand = null;
            for (IRcommand aCommand : irCommands) {
                if (aCommand.sequenceToHEXString().equals(command)) {
                    theCommand = aCommand;
                    break;
                }
            }

            if (theCommand != null) {
                for (TransceiverStatusListener listener : transceiverStatusListeners) {
                    listener.onCommandReceived(this, theCommand);
                }
            } else {
                logger.error("{} does not match any know infrared command", command);
            }

        } else {
            logger.error("{} does not match the infrared message format '{}'", message, matcher.pattern());
        }
    }

    protected void parseIRDBMessage(String message) {
        Pattern irdbPattern = Pattern.compile("RCV_COM (.*),(.*),(.*),(.*)");
        Matcher matcher = irdbPattern.matcher(message);

        if (matcher.matches()) {
            IRcommand command = new IRcommand();
            command.remote = matcher.group(1);
            command.command = matcher.group(2);

            for (TransceiverStatusListener listener : transceiverStatusListeners) {
                listener.onCommandReceived(this, command);
            }

        } else {
            logger.error("{} does not match the IRDB infrared message format '{}'", message, matcher.pattern());
        }
    }

    /**
     * "Pack" the infrared command so that it can be sent to the IRTrans device
     *
     * @param led
     *            the led
     * @param command
     *            the the command
     * @return a string which is the full command to be sent to the device
     */
    protected String packIRDBCommand(Led led, IRcommand command) {
        String output = new String();

        output = "Asnd ";
        output += command.remote;
        output += ",";
        output += command.command;
        output += ",l";
        output += led.toString();

        output += "\r\n";

        return output;
    }

    /**
     * "Pack" the infrared command so that it can be sent to the IRTrans device
     *
     * @param led
     *            the led
     * @param command
     *            the the command
     * @return a string which is the full command to be sent to the device
     */
    protected String packHexCommand(Led led, IRcommand command) {
        String output = new String();

        output = "Asndhex ";
        output += "L";
        output += led.toString();

        output += ",";
        output += "H" + command.toHEXString();

        output += (char) 13;

        return output;

    }

    protected void onWrite(ByteBuffer buffer, SocketChannel socketChannel) {
        onWritable(buffer, socketChannel);
    }

    protected void onRead(ByteBuffer byteBuffer, SocketChannel socketChannel) {
        try {
            logger.trace("Received bytebuffer : '{}'", DatatypeConverter.printHexBinary(byteBuffer.array()));
            int byteCount = getByteCount(byteBuffer);

            while (byteCount > 0) {
                byte[] message = new byte[byteCount];
                byteBuffer.get(message, 0, byteCount);
                logger.trace("Received message : '{}'", DatatypeConverter.printHexBinary(message));

                String strippedBuffer = stripByteCount(ByteBuffer.wrap(message));

                if (strippedBuffer != null) {

                    String strippedMessage = strippedBuffer.split("\0")[0];

                    // IRTrans devices return "RESULT OK" when it succeeds to emit an
                    // infrared sequence
                    if (strippedMessage.contains("RESULT OK")) {
                        parseOKMessage(strippedMessage);
                    }

                    // IRTrans devices return a string starting with RCV_HEX each time
                    // it captures an infrared sequence from a remote control
                    if (strippedMessage.contains("RCV_HEX")) {
                        parseHexMessage(strippedMessage);
                    }

                    // IRTrans devices return a string starting with RCV_COM each time
                    // it captures an infrared sequence from a remote control that is stored in the device's internal dB
                    if (strippedMessage.contains("RCV_COM")) {
                        parseIRDBMessage(strippedMessage);
                    }

                    byteCount = getByteCount(byteBuffer);

                } else {
                    logger.warn("Received some non-compliant garbage '{}' - Parsing is skipped",
                            new String(byteBuffer.array()));
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while reading bytebuffer '{}' : {}",
                    DatatypeConverter.printHexBinary(byteBuffer.array()), e.getMessage(), e);
        }
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (socketChannel == null) {
                    previousConnectionState = false;
                    onConnectionLost();
                } else {
                    if (!previousConnectionState && socketChannel.isConnected()) {
                        previousConnectionState = true;
                        onConnectionResumed();
                    }

                    if (previousConnectionState && !socketChannel.isConnected()
                            && !socketChannel.isConnectionPending()) {
                        previousConnectionState = false;
                        onConnectionLost();
                    }

                    if (!socketChannel.isConnectionPending() && !socketChannel.isConnected()) {
                        previousConnectionState = false;
                        logger.debug("Disconnecting '{}' because of a network error", getThing().getUID());
                        disconnect(socketChannel);
                        Thread.sleep(1000 * ((BigDecimal) getConfig().get(RECONNECT_INTERVAL)).intValue());
                        establishConnection();
                    }

                    long stamp = System.currentTimeMillis();
                    if (!InetAddress.getByName(((String) getConfig().get(IP_ADDRESS)))
                            .isReachable(((BigDecimal) getConfig().get(PING_TIME_OUT)).intValue())) {
                        logger.debug(
                                "Ping timed out after '{}' milliseconds. Disconnecting '{}' because of a ping timeout",
                                System.currentTimeMillis() - stamp, getThing().getUID());
                        disconnect(socketChannel);
                    }

                    onConnectable(socketChannel);
                    ByteBuffer buffer = onReadable(socketChannel,
                            ((BigDecimal) getConfig().get(BUFFER_SIZE)).intValue());
                    if (buffer != null && buffer.remaining() > 0) {
                        onRead(buffer, socketChannel);
                    }
                }

                onAcceptable(listenerChannel);

            } catch (IOException e) {
                logger.trace("An exception occurred while polling the transceiver : '{}'", e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    };

    public boolean registerTransceiverStatusListener(TransceiverStatusListener transceiverStatusListener) {
        Objects.requireNonNull(transceiverStatusListener, "It's not allowed to pass a null BlasterStatusListener.");

        boolean result = transceiverStatusListeners.add(transceiverStatusListener);
        if (result) {
            onUpdate();
        }
        return result;
    }

    public boolean unregisterTransceiverStatusListener(TransceiverStatusListener transceiverStatusListener) {
        boolean result = transceiverStatusListeners.remove(transceiverStatusListener);
        if (result) {
            onUpdate();
        }
        return result;
    }

    @Override
    public void onBridgeDisconnected(EthernetBridgeHandler bridge) {
        // Nothing to do here
    }

    @Override
    public void onBridgeConnected(EthernetBridgeHandler bridge) {
        // Nothing to do here
    }

    @Override
    public void onCommandReceived(EthernetBridgeHandler bridge, IRcommand command) {

        logger.debug("Received infrared command '{},{}' for thing '{}'", command.remote, command.command,
                this.getThing().getUID());

        for (Channel channel : getThing().getChannels()) {
            Configuration channelConfiguration = channel.getConfiguration();

            if (channel.getAcceptedItemType().equals(IRtransBindingConstants.RECEIVER_CHANNEL_TYPE)) {
                IRcommand thingCompatibleCommand = new IRcommand();
                thingCompatibleCommand.remote = (String) channelConfiguration.get(REMOTE);
                thingCompatibleCommand.command = (String) channelConfiguration.get(COMMAND);

                if (command.matches(thingCompatibleCommand)) {
                    StringType stringType = new StringType(command.remote + "," + command.command);
                    logger.debug("Received a matching infrared command '{}' for channel '{}'", stringType,
                            channel.getUID());
                    updateState(channel.getUID(), stringType);
                }
            }
        }
    }
}
