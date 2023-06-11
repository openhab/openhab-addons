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
package org.openhab.binding.irtrans.internal.handler;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.irtrans.internal.IRtransBindingConstants;
import org.openhab.binding.irtrans.internal.IRtransBindingConstants.Led;
import org.openhab.binding.irtrans.internal.IrCommand;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EthernetBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
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
    public static final int LISTENING_INTERVAL = 100;

    private static final Pattern RESPONSE_PATTERN = Pattern.compile("..(\\d{5}) (.*)", Pattern.DOTALL);
    private static final Pattern HEX_PATTERN = Pattern.compile("RCV_HEX (.*)");
    private static final Pattern IRDB_PATTERN = Pattern.compile("RCV_COM (.*),(.*),(.*),(.*)");

    private Logger logger = LoggerFactory.getLogger(EthernetBridgeHandler.class);

    private Selector selector;
    private Thread pollingThread;
    private SocketChannel socketChannel;
    protected SelectionKey socketChannelKey;
    protected ServerSocketChannel listenerChannel;
    protected SelectionKey listenerKey;
    protected boolean previousConnectionState;
    private final Lock lock = new ReentrantLock();

    private List<TransceiverStatusListener> transceiverStatusListeners = new CopyOnWriteArrayList<>();

    /**
     * Data structure to store the infrared commands that are 'loaded' from the
     * configuration files. Command loading from pre-defined configuration files is not supported
     * (anymore), but the code is maintained in case this functionality is re-added in the future
     **/
    protected final Collection<IrCommand> irCommands = new HashSet<>();

    public EthernetBridgeHandler(Bridge bridge) {
        super(bridge);
        // Nothing to do here
    }

    @Override
    public void initialize() {
        // register ourselves as a Transceiver Status Listener
        registerTransceiverStatusListener(this);

        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.debug("An exception occurred while registering the selector: '{}'", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
        }

        if (selector != null) {
            if (getConfig().get(IP_ADDRESS) != null && getConfig().get(PORT_NUMBER) != null) {
                if (pollingThread == null) {
                    pollingThread = new Thread(pollingRunnable, "OH-binding-" + getThing().getUID() + "-polling");
                    pollingThread.start();
                }
            } else {
                logger.debug("Cannot connect to IRtrans Ethernet device. IP address or port number not set.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "IP address or port number not set.");
            }

            if (getConfig().get(IS_LISTENER) != null) {
                configureListener((String) getConfig().get(LISTENER_PORT));
            }
        }
    }

    @Override
    public void dispose() {
        unregisterTransceiverStatusListener(this);

        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
        } catch (IOException e) {
            logger.warn("An exception occurred while closing the channel '{}': {}", socketChannel, e.getMessage());
        }

        try {
            if (listenerChannel != null) {
                listenerChannel.close();
            }
        } catch (IOException e) {
            logger.warn("An exception occurred while closing the channel '{}': {}", listenerChannel, e.getMessage());
        }

        try {
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            logger.debug("An exception occurred while closing the selector: '{}'", e.getMessage());
        }

        logger.debug("Stopping the IRtrans polling Thread for {}", getThing().getUID());
        if (pollingThread != null) {
            pollingThread.interrupt();
            try {
                pollingThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            pollingThread = null;
        }
    }

    public boolean registerTransceiverStatusListener(@NonNull TransceiverStatusListener transceiverStatusListener) {
        return transceiverStatusListeners.add(transceiverStatusListener);
    }

    public boolean unregisterTransceiverStatusListener(@NonNull TransceiverStatusListener transceiverStatusListener) {
        return transceiverStatusListeners.remove(transceiverStatusListener);
    }

    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
        } catch (IOException e) {
            logger.warn("An exception occurred while closing the channel '{}': {}", socketChannel, e.getMessage());
        }

        establishConnection();
    }

    public void onConnectionResumed() {
        configureTransceiver();
        updateStatus(ThingStatus.ONLINE);
    }

    private void establishConnection() {
        lock.lock();
        try {
            if (getConfig().get(IP_ADDRESS) != null && getConfig().get(PORT_NUMBER) != null) {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.socket().setKeepAlive(true);
                    socketChannel.configureBlocking(false);

                    synchronized (selector) {
                        selector.wakeup();
                        int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT;
                        socketChannelKey = socketChannel.register(selector, interestSet);
                    }

                    InetSocketAddress remoteAddress = new InetSocketAddress((String) getConfig().get(IP_ADDRESS),
                            ((BigDecimal) getConfig().get(PORT_NUMBER)).intValue());
                    socketChannel.connect(remoteAddress);
                } catch (IOException e) {
                    logger.debug("An exception occurred while connecting to '{}:{}' : {}", getConfig().get(IP_ADDRESS),
                            ((BigDecimal) getConfig().get(PORT_NUMBER)).intValue(), e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }

                try {
                    Thread.sleep(((BigDecimal) getConfig().get(RESPONSE_TIME_OUT)).intValue());
                } catch (NumberFormatException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("An exception occurred while putting a thread to sleep: '{}'", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
                onConnectable();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onCommandReceived(EthernetBridgeHandler bridge, IrCommand command) {
        logger.debug("Received infrared command '{},{}' for thing '{}'", command.getRemote(), command.getCommand(),
                this.getThing().getUID());

        for (Channel channel : getThing().getChannels()) {
            Configuration channelConfiguration = channel.getConfiguration();

            if (channel.getChannelTypeUID() != null
                    && channel.getChannelTypeUID().getId().equals(IRtransBindingConstants.RECEIVER_CHANNEL_TYPE)) {
                IrCommand thingCompatibleCommand = new IrCommand();
                thingCompatibleCommand.setRemote((String) channelConfiguration.get(REMOTE));
                thingCompatibleCommand.setCommand((String) channelConfiguration.get(COMMAND));

                if (command.matches(thingCompatibleCommand)) {
                    StringType stringType = new StringType(command.getRemote() + "," + command.getCommand());
                    logger.debug("Received a matching infrared command '{}' for channel '{}'", stringType,
                            channel.getUID());
                    updateState(channel.getUID(), stringType);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            Channel channel = this.getThing().getChannel(channelUID.getId());
            if (channel != null) {
                Configuration channelConfiguration = channel.getConfiguration();
                if (channel.getChannelTypeUID() != null
                        && channel.getChannelTypeUID().getId().equals(IRtransBindingConstants.BLASTER_CHANNEL_TYPE)) {
                    if (command instanceof StringType) {
                        String remoteName = StringUtils.substringBefore(command.toString(), ",");
                        String irCommandName = StringUtils.substringAfter(command.toString(), ",");

                        IrCommand ircommand = new IrCommand();
                        ircommand.setRemote(remoteName);
                        ircommand.setCommand(irCommandName);

                        IrCommand thingCompatibleCommand = new IrCommand();
                        thingCompatibleCommand.setRemote((String) channelConfiguration.get(REMOTE));
                        thingCompatibleCommand.setCommand((String) channelConfiguration.get(COMMAND));

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
                if (channel.getAcceptedItemType() != null
                        && channel.getAcceptedItemType().equals(IRtransBindingConstants.RECEIVER_CHANNEL_TYPE)) {
                    logger.warn("Receivers can only receive infrared commands, not send them");
                }
            }
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
                    logger.debug("An exception occurred while registering a selector: '{}'", e1.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.getMessage());
                }
            }
        } catch (IOException e3) {
            logger.error(
                    "An exception occurred while creating configuring the listener channel on port number {}: '{}'",
                    Integer.parseInt(listenerPort), e3.getMessage());
        }
    }

    protected void configureTransceiver() {
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
                        logger.debug("Received some non-compliant garbage ({})", message);
                        onConnectionLost();
                    }
                }
            } else {
                try {
                    logger.debug("Did not receive an answer from the IRtrans transceiver '{}' - Parsing is skipped",
                            socketChannel.getRemoteAddress());
                    onConnectionLost();
                } catch (IOException e1) {
                    logger.debug("An exception occurred while getting a remote address: '{}'", e1.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.getMessage());
                }
            }

            int numberOfRemotes = 0;
            int numberOfRemotesProcessed = 0;
            int numberOfRemotesInBatch = 0;
            String[] remoteList = getRemoteList(0);

            if (remoteList.length > 0) {
                logger.debug("The IRtrans device for '{}' supports '{}' remotes", getThing().getUID(), remoteList[1]);
                numberOfRemotes = Integer.valueOf(remoteList[1]);
                numberOfRemotesInBatch = Integer.valueOf(remoteList[2]);
            }

            while (numberOfRemotesProcessed < numberOfRemotes) {
                for (int i = 1; i <= numberOfRemotesInBatch; i++) {
                    String remote = remoteList[2 + i];

                    // get remote commands
                    String[] commands = getRemoteCommands(remote, 0);
                    StringBuilder result = new StringBuilder();
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
                            result.append(command);
                            numberOfCommandsProcessed++;
                            if (numberOfCommandsProcessed < numberOfCommands) {
                                result.append(", ");
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

                    logger.debug("The remote '{}' on '{}' supports '{}' commands: {}", remote, getThing().getUID(),
                            numberOfCommands, result.toString());

                    numberOfRemotesProcessed++;
                }

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
                    logger.debug("Received some non-compliant command ({})", message);
                    onConnectionLost();
                }
            }
        } else {
            logger.debug("Did not receive an answer from the IRtrans transceiver for '{}' - Parsing is skipped",
                    getThing().getUID());
            onConnectionLost();
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
                    logger.debug("Received some non-compliant command ({})", message);
                    onConnectionLost();
                }
            }
        } else {
            logger.debug("Did not receive an answer from the IRtrans transceiver for '{}' - Parsing is skipped",
                    getThing().getUID());
            onConnectionLost();
        }

        return remoteList;
    }

    public boolean sendIRcommand(IrCommand command, Led led) {
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

    private ByteBuffer sendCommand(String command) {
        if (command != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(command.getBytes().length);
            try {
                byteBuffer.put(command.getBytes("ASCII"));
                onWritable(byteBuffer);
                Thread.sleep(((BigDecimal) getConfig().get(RESPONSE_TIME_OUT)).intValue());
                return onReadable(((BigDecimal) getConfig().get(BUFFER_SIZE)).intValue(), true);
            } catch (UnsupportedEncodingException | NumberFormatException | InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("An exception occurred while sending a command to the IRtrans transceiver for '{}': {}",
                        getThing().getUID(), e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        return null;
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            while (true) {
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
                            socketChannel.close();
                            Thread.sleep(1000 * ((BigDecimal) getConfig().get(RECONNECT_INTERVAL)).intValue());
                            establishConnection();
                        }

                        long stamp = System.currentTimeMillis();
                        if (!InetAddress.getByName(((String) getConfig().get(IP_ADDRESS)))
                                .isReachable(((BigDecimal) getConfig().get(PING_TIME_OUT)).intValue())) {
                            logger.debug(
                                    "Ping timed out after '{}' milliseconds. Disconnecting '{}' because of a ping timeout",
                                    System.currentTimeMillis() - stamp, getThing().getUID());
                            socketChannel.close();
                        }

                        onConnectable();
                        ByteBuffer buffer = onReadable(((BigDecimal) getConfig().get(BUFFER_SIZE)).intValue(), false);
                        if (buffer != null && buffer.remaining() > 0) {
                            onRead(buffer);
                        }
                    }

                    onAcceptable();

                    if (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(LISTENING_INTERVAL);
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    logger.trace("An exception occurred while polling the transceiver : '{}'", e.getMessage(), e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    };

    protected void onAcceptable() {
        lock.lock();
        try {
            try {
                selector.selectNow();
            } catch (IOException e) {
                logger.debug("An exception occurred while selecting: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selKey = it.next();
                it.remove();
                if (selKey.isValid()) {
                    if (selKey.isAcceptable() && selKey.equals(listenerKey)) {
                        try {
                            SocketChannel newChannel = listenerChannel.accept();
                            newChannel.configureBlocking(false);
                            logger.trace("Received a connection request from '{}'", newChannel.getRemoteAddress());

                            synchronized (selector) {
                                selector.wakeup();
                                newChannel.register(selector, newChannel.validOps());
                            }
                        } catch (IOException e) {
                            logger.debug("An exception occurred while accepting a connection on channel '{}': {}",
                                    listenerChannel, e.getMessage());
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected void onConnectable() {
        lock.lock();
        SocketChannel aSocketChannel = null;
        try {
            synchronized (selector) {
                selector.selectNow();
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selKey = it.next();
                it.remove();
                if (selKey.isValid() && selKey.isConnectable()) {
                    aSocketChannel = (SocketChannel) selKey.channel();
                    aSocketChannel.finishConnect();
                    logger.trace("The channel for '{}' is connected", aSocketChannel.getRemoteAddress());
                }
            }
        } catch (IOException | NoConnectionPendingException e) {
            if (aSocketChannel != null) {
                logger.debug("Disconnecting '{}' because of a socket error : '{}'", getThing().getUID(), e.getMessage(),
                        e);
                try {
                    aSocketChannel.close();
                } catch (IOException e1) {
                    logger.debug("An exception occurred while closing the channel '{}': {}", socketChannel,
                            e1.getMessage());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected ByteBuffer onReadable(int bufferSize, boolean isSelective) {
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
                    SocketChannel aSocketChannel = (SocketChannel) selKey.channel();

                    if ((aSocketChannel.equals(socketChannel) && isSelective) || !isSelective) {
                        ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
                        int numberBytesRead = 0;
                        boolean error = false;

                        try {
                            numberBytesRead = aSocketChannel.read(readBuffer);
                        } catch (NotYetConnectedException e) {
                            logger.warn("The channel '{}' is not yet connected: {}", aSocketChannel, e.getMessage());
                            if (!aSocketChannel.isConnectionPending()) {
                                error = true;
                            }
                        } catch (IOException e) {
                            // If some other I/O error occurs
                            logger.warn("An IO exception occured on channel '{}': {}", aSocketChannel, e.getMessage());
                            error = true;
                        }

                        if (numberBytesRead == -1) {
                            error = true;
                        }

                        if (error) {
                            logger.debug("Disconnecting '{}' because of a socket error", getThing().getUID());
                            try {
                                aSocketChannel.close();
                            } catch (IOException e1) {
                                logger.debug("An exception occurred while closing the channel '{}': {}", socketChannel,
                                        e1.getMessage());
                            }
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

    protected void onWritable(ByteBuffer buffer) {
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
                    SocketChannel aSocketChannel = (SocketChannel) selKey.channel();

                    if (aSocketChannel.equals(socketChannel)) {
                        boolean error = false;

                        buffer.rewind();
                        try {
                            logger.trace("Sending '{}' on the channel '{}'->'{}'", new String(buffer.array()),
                                    aSocketChannel.getLocalAddress(), aSocketChannel.getRemoteAddress());
                            aSocketChannel.write(buffer);
                        } catch (NotYetConnectedException e) {
                            logger.warn("The channel '{}' is not yet connected: {}", aSocketChannel, e.getMessage());
                            if (!aSocketChannel.isConnectionPending()) {
                                error = true;
                            }
                        } catch (ClosedChannelException e) {
                            // If some other I/O error occurs
                            logger.warn("The channel for '{}' is closed: {}", aSocketChannel, e.getMessage());
                            error = true;
                        } catch (IOException e) {
                            // If some other I/O error occurs
                            logger.warn("An IO exception occured on channel '{}': {}", aSocketChannel, e.getMessage());
                            error = true;
                        }

                        if (error) {
                            try {
                                aSocketChannel.close();
                            } catch (IOException e) {
                                logger.warn("An exception occurred while closing the channel '{}': {}", aSocketChannel,
                                        e.getMessage());
                            }
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected void onRead(ByteBuffer byteBuffer) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Received bytebuffer : '{}'", HexUtils.bytesToHex(byteBuffer.array()));
            }
            int byteCount = getByteCount(byteBuffer);

            while (byteCount > 0) {
                byte[] message = new byte[byteCount];
                byteBuffer.get(message, 0, byteCount);

                if (logger.isTraceEnabled()) {
                    logger.trace("Received message : '{}'", HexUtils.bytesToHex(message));
                }

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
                    HexUtils.bytesToHex(byteBuffer.array()), e.getMessage(), e);
        }
    }

    protected int getByteCount(ByteBuffer byteBuffer) {
        String response = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        response = StringUtils.chomp(response);

        Matcher matcher = RESPONSE_PATTERN.matcher(response);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 0;
    }

    protected String stripByteCount(ByteBuffer byteBuffer) {
        String message = null;

        String response = new String(byteBuffer.array(), 0, byteBuffer.limit());
        response = StringUtils.chomp(response);

        Matcher matcher = RESPONSE_PATTERN.matcher(response);
        if (matcher.matches()) {
            message = matcher.group(2);
        }

        return message;
    }

    protected void parseOKMessage(String message) {
        // Nothing to do here
    }

    protected void parseHexMessage(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);

        if (matcher.matches()) {
            String command = matcher.group(1);

            IrCommand theCommand = null;
            for (IrCommand aCommand : irCommands) {
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
        Matcher matcher = IRDB_PATTERN.matcher(message);

        if (matcher.matches()) {
            IrCommand command = new IrCommand();
            command.setRemote(matcher.group(1));
            command.setCommand(matcher.group(2));

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
     * @param led the led
     * @param command the the command
     * @return a string which is the full command to be sent to the device
     */
    protected String packIRDBCommand(Led led, IrCommand command) {
        String output = new String();

        output = "Asnd ";
        output += command.getRemote();
        output += ",";
        output += command.getCommand();
        output += ",l";
        output += led.toString();

        output += "\r\n";

        return output;
    }

    /**
     * "Pack" the infrared command so that it can be sent to the IRTrans device
     *
     * @param led the led
     * @param command the the command
     * @return a string which is the full command to be sent to the device
     */
    protected String packHexCommand(Led led, IrCommand command) {
        String output = new String();

        output = "Asndhex ";
        output += "L";
        output += led.toString();

        output += ",";
        output += "H" + command.toHEXString();

        output += (char) 13;

        return output;
    }
}
