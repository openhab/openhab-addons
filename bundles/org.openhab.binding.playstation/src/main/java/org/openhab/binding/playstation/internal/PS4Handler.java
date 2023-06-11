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
package org.openhab.binding.playstation.internal;

import static org.openhab.binding.playstation.internal.PlayStationBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.playstation.internal.discovery.PlayStationDiscovery;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PS4Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class PS4Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PS4Handler.class);
    private final PS4Crypto ps4Crypto = new PS4Crypto();
    private static final int SOCKET_TIMEOUT_SECONDS = 5;
    /** Time after connect that we can start to send key events, milli seconds */
    private static final int POST_CONNECT_SENDKEY_DELAY_MS = 500;
    /** Minimum delay between sendKey sends, milli seconds */
    private static final int MIN_SENDKEY_DELAY_MS = 210;
    /** Minimum delay after Key set, milli seconds */
    private static final int MIN_HOLDKEY_DELAY_MS = 300;

    private PS4Configuration config = new PS4Configuration();

    private final @Nullable LocaleProvider localeProvider;
    private final @Nullable NetworkAddressService networkAS;
    private List<ScheduledFuture<?>> scheduledFutures = Collections.synchronizedList(new ArrayList<>());
    private @Nullable ScheduledFuture<?> refreshTimer;
    private @Nullable ScheduledFuture<?> timeoutTimer;
    private @Nullable SocketChannelHandler socketChannelHandler;
    private @Nullable InetAddress localAddress;

    // State of PS4
    private String currentApplication = "";
    private String currentApplicationId = "";
    private OnOffType currentPower = OnOffType.OFF;
    private State currentArtwork = UnDefType.UNDEF;
    private int currentComPort = DEFAULT_COMMUNICATION_PORT;

    boolean loggedIn = false;
    boolean oskOpen = false;

    public PS4Handler(Thing thing, LocaleProvider locProvider, NetworkAddressService network) {
        super(thing);
        localeProvider = locProvider;
        networkAS = network;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        figureOutLocalIP();
        SocketChannelHandler scHandler = socketChannelHandler;
        if (!config.pairingCode.isEmpty() && (scHandler == null || !loggedIn)) {
            // Try to log in then remove pairing code as it's one use only.
            scheduler.execute(() -> {
                login();
                Configuration editedConfig = editConfiguration();
                editedConfig.put(PAIRING_CODE, "");
                updateConfiguration(editedConfig);
            });
        }
        setupConnectionTimeout(config.connectionTimeout);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
        } else {
            if (command instanceof StringType) {
                switch (channelUID.getId()) {
                    case CHANNEL_APPLICATION_ID:
                        if (!currentApplicationId.equals(((StringType) command).toString())) {
                            updateApplicationTitleid(((StringType) command).toString());
                            startApplication(currentApplicationId);
                        }
                        break;
                    case CHANNEL_OSK_TEXT:
                        setOSKText(((StringType) command).toString());
                        break;
                    case CHANNEL_SEND_KEY:
                        int ps4Key = 0;
                        switch (((StringType) command).toString()) {
                            case SEND_KEY_UP:
                                ps4Key = PS4_KEY_UP;
                                break;
                            case SEND_KEY_DOWN:
                                ps4Key = PS4_KEY_DOWN;
                                break;
                            case SEND_KEY_RIGHT:
                                ps4Key = PS4_KEY_RIGHT;
                                break;
                            case SEND_KEY_LEFT:
                                ps4Key = PS4_KEY_LEFT;
                                break;
                            case SEND_KEY_ENTER:
                                ps4Key = PS4_KEY_ENTER;
                                break;
                            case SEND_KEY_BACK:
                                ps4Key = PS4_KEY_BACK;
                                break;
                            case SEND_KEY_OPTION:
                                ps4Key = PS4_KEY_OPTION;
                                break;
                            case SEND_KEY_PS:
                                ps4Key = PS4_KEY_PS;
                                break;
                            default:
                                break;
                        }
                        if (ps4Key != 0) {
                            sendRemoteKey(ps4Key);
                        }
                        break;
                    default:
                        break;
                }
            } else if (command instanceof OnOffType) {
                OnOffType onOff = (OnOffType) command;
                switch (channelUID.getId()) {
                    case CHANNEL_POWER:
                        if (currentPower != onOff) {
                            currentPower = onOff;
                            if (currentPower.equals(OnOffType.ON)) {
                                turnOnPS4();
                            } else if (currentPower.equals(OnOffType.OFF)) {
                                sendStandby();
                            }
                        }
                        break;
                    case CHANNEL_CONNECT:
                        boolean connected = socketChannelHandler != null && socketChannelHandler.isChannelOpen();
                        if (connected && onOff.equals(OnOffType.OFF)) {
                            sendByeBye();
                        } else if (!connected && onOff.equals(OnOffType.ON)) {
                            scheduler.execute(() -> login());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(PS4Configuration.class);

        figureOutLocalIP();
        updateStatus(ThingStatus.UNKNOWN);
        setupRefreshTimer();
    }

    @Override
    public void dispose() {
        stopConnection();
        ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(false);
            refreshTimer = null;
        }
        timer = timeoutTimer;
        if (timer != null) {
            timer.cancel(false);
            timeoutTimer = null;
        }
        scheduledFutures.forEach(f -> f.cancel(false));
        scheduledFutures.clear();
    }

    /**
     * Tries to figure out a local IP that can communicate with the PS4.
     */
    private void figureOutLocalIP() {
        if (!config.outboundIP.trim().isEmpty()) {
            try {
                localAddress = InetAddress.getByName(config.outboundIP);
                logger.debug("Outbound local IP.\"{}\"", localAddress);
                return;
            } catch (UnknownHostException e) {
                // This is expected
            }
        }
        NetworkAddressService network = networkAS;
        String adr = (network != null) ? network.getPrimaryIpv4HostAddress() : null;
        if (adr != null) {
            try {
                localAddress = InetAddress.getByName(adr);
            } catch (UnknownHostException e) {
                // Ignore, just let the socket use whatever.
            }
        }
    }

    /**
     * Sets up a timer for querying the PS4 (using the scheduler) every 10 seconds.
     */
    private void setupRefreshTimer() {
        final ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(this::updateAllChannels, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Sets up a timer for stopping the connection to the PS4 (using the scheduler) with the given time.
     *
     * @param waitTime The time in seconds before the connection is stopped.
     */
    private void setupConnectionTimeout(int waitTime) {
        final ScheduledFuture<?> timer = timeoutTimer;
        if (timer != null) {
            timer.cancel(false);
        }
        if (waitTime > 0) {
            timeoutTimer = scheduler.schedule(this::stopConnection, waitTime, TimeUnit.SECONDS);
        }
    }

    private void refreshFromState(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                updateState(channelUID, currentPower);
                break;
            case CHANNEL_APPLICATION_NAME:
                updateState(channelUID, StringType.valueOf(currentApplication));
                break;
            case CHANNEL_APPLICATION_ID:
                updateState(channelUID, StringType.valueOf(currentApplicationId));
                break;
            case CHANNEL_APPLICATION_IMAGE:
                updateApplicationTitleid(currentApplicationId);
                updateState(channelUID, currentArtwork);
                break;
            case CHANNEL_OSK_TEXT:
            case CHANNEL_2ND_SCREEN:
                updateState(channelUID, UnDefType.UNDEF);
                break;
            case CHANNEL_CONNECT:
                boolean connected = socketChannelHandler != null && socketChannelHandler.isChannelOpen();
                updateState(channelUID, OnOffType.from(connected));
                break;
            case CHANNEL_SEND_KEY:
                break;
            default:
                logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    private void updateAllChannels() {
        try (DatagramSocket socket = new DatagramSocket(0, localAddress)) {
            socket.setBroadcast(false);
            socket.setSoTimeout(SOCKET_TIMEOUT_SECONDS * 1000);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);

            // send discover
            byte[] discover = PS4PacketHandler.makeSearchPacket();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);

            // wait for response
            byte[] rxbuf = new byte[256];
            packet = new DatagramPacket(rxbuf, rxbuf.length);
            socket.receive(packet);
            parseSearchResponse(packet);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopConnection() {
        SocketChannelHandler handler = socketChannelHandler;
        if (handler != null && handler.isChannelOpen()) {
            sendByeBye();
        }
    }

    private void wakeUpPS4() {
        logger.debug("Waking up PS4...");
        try (DatagramSocket socket = new DatagramSocket(0, localAddress)) {
            socket.setBroadcast(false);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // send wake-up
            byte[] wakeup = PS4PacketHandler.makeWakeupPacket(config.userCredential);
            DatagramPacket packet = new DatagramPacket(wakeup, wakeup.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean openComs() {
        try (DatagramSocket socket = new DatagramSocket(0, localAddress)) {
            socket.setBroadcast(false);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // send launch
            byte[] launch = PS4PacketHandler.makeLaunchPacket(config.userCredential);
            DatagramPacket packet = new DatagramPacket(launch, launch.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
            Thread.sleep(100);
            return true;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            return true;
        }
        return false;
    }

    private boolean setupConnection(SocketChannel channel) throws IOException {
        logger.debug("TCP connecting");

        channel.socket().setSoTimeout(2000);
        channel.configureBlocking(true);
        channel.connect(new InetSocketAddress(config.ipAddress, currentComPort));

        ByteBuffer outPacket = PS4PacketHandler.makeHelloPacket();
        sendPacketToPS4(outPacket, channel, false, false);

        // Read hello response
        final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);

        int responseLength = channel.read(readBuffer);
        if (responseLength > 0) {
            ps4Crypto.parseHelloResponsePacket(readBuffer);
        } else {
            return false;
        }

        outPacket = ps4Crypto.makeHandshakePacket();
        sendPacketToPS4(outPacket, channel, false, false);
        return true;
    }

    private class SocketChannelHandler extends Thread {
        private SocketChannel socketChannel;

        public SocketChannelHandler() throws IOException {
            socketChannel = setupChannel();
            loggedIn = false;
            oskOpen = false;
            start();
        }

        public SocketChannel getChannel() {
            if (!socketChannel.isOpen()) {
                try {
                    socketChannel = setupChannel();
                } catch (IOException e) {
                    logger.debug("Couldn't open SocketChannel.{}", e.getMessage());
                }
            }
            return socketChannel;
        }

        public boolean isChannelOpen() {
            return socketChannel.isOpen();
        }

        private SocketChannel setupChannel() throws IOException {
            SocketChannel channel = SocketChannel.open();
            if (!openComs()) {
                throw new IOException("Open coms failed");
            }
            if (!setupConnection(channel)) {
                throw new IOException("Setup connection failed");
            }
            updateState(CHANNEL_CONNECT, OnOffType.ON);
            return channel;
        }

        @Override
        public void run() {
            SocketChannel channel = socketChannel;
            final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
            try {
                while (channel.read(readBuffer) > 0) {
                    ByteBuffer messBuffer = ps4Crypto.decryptPacket(readBuffer);
                    readBuffer.position(0);
                    PS4Command lastCommand = parseResponsePacket(messBuffer);

                    if (lastCommand == PS4Command.SERVER_STATUS_RSP) {
                        if (oskOpen && isLinked(CHANNEL_OSK_TEXT)) {
                            sendOSKStart();
                        } else {
                            sendStatus();
                        }
                    }
                }
            } catch (IOException e) {
                logger.debug("Connection read exception: {}", e.getMessage());
            } finally {
                try {
                    channel.close();
                } catch (IOException e) {
                    logger.debug("Connection close exception: {}", e.getMessage());
                }
            }
            updateState(CHANNEL_CONNECT, OnOffType.OFF);
            logger.debug("SocketHandler done.");
            ps4Crypto.clearCiphers();
            loggedIn = false;
        }
    }

    private @Nullable PS4Command parseResponsePacket(ByteBuffer rBuffer) {
        rBuffer.rewind();
        final int buffSize = rBuffer.remaining();
        final int size = rBuffer.getInt();
        if (size > buffSize || size < 12) {
            logger.debug("Response size ({}) not good, buffer size ({}).", size, buffSize);
            return null;
        }
        int cmdValue = rBuffer.getInt();
        int statValue = rBuffer.getInt();
        PS4ErrorStatus status = PS4ErrorStatus.valueOfTag(statValue);
        PS4Command command = PS4Command.valueOfTag(cmdValue);
        byte[] respBuff = new byte[size];
        rBuffer.rewind();
        rBuffer.get(respBuff);
        if (command != null) {
            if (status == null) {
                logger.debug("Resp; size:{}, command:{}, statValue:{}, data:{}.", size, command, statValue, respBuff);
            } else {
                logger.debug("Resp; size:{}, command:{}, status:{}, data:{}.", size, command, status, respBuff);
            }
            switch (command) {
                case LOGIN_RSP:
                    if (status == null) {
                        logger.debug("Unhandled Login status value: {}", statValue);
                        return command;
                    }
                    // Read login response
                    switch (status) {
                        case STATUS_OK:
                            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, status.message);
                            loggedIn = true;
                            if (isLinked(CHANNEL_2ND_SCREEN)) {
                                scheduler.execute(() -> {
                                    ByteBuffer outPacket = PS4PacketHandler
                                            .makeClientIDPacket("com.playstation.mobile2ndscreen", "18.9.3");
                                    sendPacketEncrypted(outPacket, false);
                                });
                            }
                            break;
                        case STATUS_NOT_PAIRED:
                            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, status.message);
                            loggedIn = false;
                            break;
                        case STATUS_MISSING_PAIRING_CODE:
                        case STATUS_MISSING_PASS_CODE:
                        case STATUS_WRONG_PAIRING_CODE:
                        case STATUS_WRONG_PASS_CODE:
                        case STATUS_WRONG_USER_CREDENTIAL:
                            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, status.message);
                            loggedIn = false;
                            logger.debug("Not logged in: {}", status.message);
                            break;
                        case STATUS_CAN_NOT_PLAY_NOW:
                        case STATUS_CLOSE_OTHER_APP:
                        case STATUS_COMMAND_NOT_GOOD:
                        case STATUS_COULD_NOT_LOG_IN:
                        case STATUS_DO_LOGIN:
                        case STATUS_MAX_USERS:
                        case STATUS_REGISTER_DEVICE_OVER:
                        case STATUS_RESTART_APP:
                        case STATUS_SOMEONE_ELSE_USING:
                        case STATUS_UPDATE_APP:
                        case STATUS_UPDATE_PS4:
                            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, status.message);
                            loggedIn = false;
                            logger.debug("Not logged in: {}", status.message);
                            break;
                        default:
                            logger.debug("Unhandled Login response status:{}, message:{}", status, status.message);
                            break;
                    }
                    break;
                case APP_START_RSP:
                    if (status != null && status != PS4ErrorStatus.STATUS_OK) {
                        logger.debug("App start response: {}", status.message);
                    }
                    break;
                case STANDBY_RSP:
                    if (status != null && status != PS4ErrorStatus.STATUS_OK) {
                        logger.debug("Standby response: {}", status.message);
                    }
                    break;
                case SERVER_STATUS_RSP:
                    if ((statValue & 4) != 0) {
                        oskOpen = true;
                    } else {
                        if (oskOpen) {
                            updateState(CHANNEL_OSK_TEXT, StringType.valueOf(""));
                        }
                        oskOpen = false;
                    }
                    logger.debug("Server status value:{}", statValue);
                    break;
                case HTTPD_STATUS_RSP:
                    String httpdStat = PS4PacketHandler.parseHTTPdPacket(rBuffer);
                    logger.debug("HTTPd Response; {}", httpdStat);
                    String secondScrStr = "";
                    int httpStatus = rBuffer.getInt(8);
                    int port = rBuffer.getInt(12);
                    if (httpStatus != 0 && port != 0) {
                        secondScrStr = "http://" + config.ipAddress + ":" + port;
                    }
                    updateState(CHANNEL_2ND_SCREEN, StringType.valueOf(secondScrStr));
                    break;
                case OSK_CHANGE_STRING_REQ:
                    String oskText = PS4PacketHandler.parseOSKStringChangePacket(rBuffer);
                    updateState(CHANNEL_OSK_TEXT, StringType.valueOf(oskText));
                    break;
                case OSK_START_RSP:
                case OSK_CONTROL_REQ:
                case COMMENT_VIEWER_START_RESULT:
                case SCREEN_SHOT_RSP:
                case APP_START2_RSP:
                case LOGOUT_RSP:
                    break;
                default:
                    logger.debug("Unknown response, command:{}. Missing case.", command);
                    break;
            }
        } else {
            logger.debug("Unknown resp-cmd, size:{}, command:{}, status:{}, data:{}.", size, cmdValue, statValue,
                    respBuff);
        }
        return command;
    }

    private SocketChannel getConnection() throws IOException {
        return getConnection(true);
    }

    private SocketChannel getConnection(boolean requiresLogin) throws IOException {
        SocketChannel channel = null;
        SocketChannelHandler handler = socketChannelHandler;
        if (handler == null || !handler.isChannelOpen()) {
            try {
                handler = new SocketChannelHandler();
                socketChannelHandler = handler;
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                throw e;
            }
        }
        channel = handler.getChannel();
        if (!loggedIn && requiresLogin) {
            login(channel);
        }
        return channel;
    }

    private void sendPacketToPS4(ByteBuffer packet, SocketChannel channel, boolean encrypted, boolean restartTimeout) {
        PS4Command cmd = PS4Command.valueOfTag(packet.getInt(4));
        logger.debug("Sending {} packet.", cmd);
        try {
            if (encrypted) {
                ByteBuffer outPacket = ps4Crypto.encryptPacket(packet);
                channel.write(outPacket);
            } else {
                channel.write(packet);
            }
            if (restartTimeout) {
                setupConnectionTimeout(config.connectionTimeout);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void sendPacketEncrypted(ByteBuffer packet, SocketChannel channel) {
        sendPacketToPS4(packet, channel, true, true);
    }

    private void sendPacketEncrypted(ByteBuffer packet) {
        sendPacketEncrypted(packet, true);
    }

    private void sendPacketEncrypted(ByteBuffer packet, boolean requiresLogin) {
        try {
            SocketChannel channel = getConnection(requiresLogin);
            if (requiresLogin && !loggedIn) {
                ScheduledFuture<?> future = scheduler.schedule(
                        () -> sendPacketToPS4(packet, channel, true, requiresLogin), 250, TimeUnit.MILLISECONDS);
                scheduledFutures.add(future);
                scheduledFutures.removeIf(ScheduledFuture::isDone);
            } else {
                sendPacketToPS4(packet, channel, true, requiresLogin);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * This is used as a heart beat to let the PS4 know that we are still listening.
     */
    private void sendStatus() {
        ByteBuffer outPacket = PS4PacketHandler.makeStatusPacket(0);
        sendPacketEncrypted(outPacket, false);
    }

    private void login(SocketChannel channel) {
        // Send login request
        ByteBuffer outPacket = PS4PacketHandler.makeLoginPacket(config.userCredential, config.passCode,
                config.pairingCode);
        sendPacketEncrypted(outPacket, channel);
    }

    private void login() {
        try {
            SocketChannel channel = getConnection(false);
            login(channel);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * This closes the connection with the PS4.
     */
    private void sendByeBye() {
        ByteBuffer outPacket = PS4PacketHandler.makeByebyePacket();
        sendPacketEncrypted(outPacket, false);
    }

    private void turnOnPS4() {
        wakeUpPS4();
        ScheduledFuture<?> future = scheduler.schedule(this::waitAndConnectToPS4, 17, TimeUnit.SECONDS);
        scheduledFutures.add(future);
        scheduledFutures.removeIf(ScheduledFuture::isDone);
    }

    private void waitAndConnectToPS4() {
        try {
            getConnection();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void sendStandby() {
        ByteBuffer outPacket = PS4PacketHandler.makeStandbyPacket();
        sendPacketEncrypted(outPacket);
    }

    /**
     * Ask PS4 if the OSK is open so we can get and set text.
     */
    private void sendOSKStart() {
        ByteBuffer outPacket = PS4PacketHandler.makeOSKStartPacket();
        sendPacketEncrypted(outPacket);
    }

    /**
     * Sets the entire OSK string on the PS4.
     *
     * @param text The text to set in the OSK.
     */
    private void setOSKText(String text) {
        logger.debug("Sending osk text packet,\"{}\"", text);
        ByteBuffer outPacket = PS4PacketHandler.makeOSKStringChangePacket(text);
        sendPacketEncrypted(outPacket);
    }

    /**
     * Tries to start an application on the PS4.
     *
     * @param applicationId The unique id for the application (CUSAxxxxx).
     */
    private void startApplication(String applicationId) {
        ByteBuffer outPacket = PS4PacketHandler.makeApplicationPacket(applicationId);
        sendPacketEncrypted(outPacket);
    }

    private void sendRemoteKey(int pushedKey) {
        try {
            SocketChannelHandler scHandler = socketChannelHandler;
            int preWait = (scHandler == null || !loggedIn) ? POST_CONNECT_SENDKEY_DELAY_MS : 0;
            SocketChannel channel = getConnection();

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                ByteBuffer outPacket = PS4PacketHandler.makeRemoteControlPacket(PS4_KEY_OPEN_RC);
                sendPacketEncrypted(outPacket, channel);
            }, preWait, TimeUnit.MILLISECONDS);
            scheduledFutures.add(future);

            future = scheduler.schedule(() -> {
                // Send remote key
                ByteBuffer keyPacket = PS4PacketHandler.makeRemoteControlPacket(pushedKey);
                sendPacketEncrypted(keyPacket, channel);
            }, preWait + MIN_SENDKEY_DELAY_MS, TimeUnit.MILLISECONDS);
            scheduledFutures.add(future);

            future = scheduler.schedule(() -> {
                ByteBuffer offPacket = PS4PacketHandler.makeRemoteControlPacket(PS4_KEY_OFF);
                sendPacketEncrypted(offPacket, channel);
            }, preWait + MIN_SENDKEY_DELAY_MS + MIN_HOLDKEY_DELAY_MS, TimeUnit.MILLISECONDS);
            scheduledFutures.add(future);

            future = scheduler.schedule(() -> {
                ByteBuffer closePacket = PS4PacketHandler.makeRemoteControlPacket(PS4_KEY_CLOSE_RC);
                sendPacketEncrypted(closePacket, channel);
            }, preWait + MIN_SENDKEY_DELAY_MS * 2 + MIN_HOLDKEY_DELAY_MS, TimeUnit.MILLISECONDS);
            scheduledFutures.add(future);

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        scheduledFutures.removeIf(ScheduledFuture::isDone);
    }

    private void parseSearchResponse(DatagramPacket packet) {
        byte[] data = packet.getData();
        String message = new String(data, StandardCharsets.UTF_8);
        String applicationName = "";
        String applicationId = "";

        String[] rowStrings = message.trim().split("\\r?\\n");
        for (String row : rowStrings) {
            int index = row.indexOf(':');
            if (index == -1) {
                OnOffType power = null;
                if (row.contains("200")) {
                    power = OnOffType.ON;
                } else if (row.contains("620")) {
                    power = OnOffType.OFF;
                }
                if (power != null) {
                    updateState(CHANNEL_POWER, power);
                    if (!currentPower.equals(power)) {
                        currentPower = power;
                        if (power.equals(OnOffType.ON) && config.autoConnect) {
                            SocketChannelHandler scHandler = socketChannelHandler;
                            if (scHandler == null || !loggedIn) {
                                logger.debug("Trying to login after power on.");
                                ScheduledFuture<?> future = scheduler.schedule(() -> login(), 20, TimeUnit.SECONDS);
                                scheduledFutures.add(future);
                                scheduledFutures.removeIf(ScheduledFuture::isDone);
                            }
                        }
                    }
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Could not determine power status.");
                }
                continue;
            }
            String key = row.substring(0, index);
            String value = row.substring(index + 1);
            switch (key) {
                case RESPONSE_RUNNING_APP_NAME:
                    applicationName = value;
                    break;
                case RESPONSE_RUNNING_APP_TITLEID:
                    applicationId = value;
                    break;
                case RESPONSE_HOST_REQUEST_PORT:
                    int port = Integer.parseInt(value);
                    if (currentComPort != port) {
                        currentComPort = port;
                        logger.debug("Host request port: {}", port);
                    }
                    break;
                case RESPONSE_SYSTEM_VERSION:
                    updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, PlayStationDiscovery.formatPS4Version(value));
                    break;

                default:
                    break;
            }
        }
        if (!currentApplication.equals(applicationName)) {
            currentApplication = applicationName;
            updateState(CHANNEL_APPLICATION_NAME, StringType.valueOf(applicationName));
            logger.debug("Current application: {}", applicationName);
        }
        if (!currentApplicationId.equals(applicationId)) {
            updateApplicationTitleid(applicationId);
        }
    }

    /**
     * Sets the cached TitleId and tries to download artwork
     * for application if CHANNEL_APPLICATION_IMAGE is linked.
     *
     * @param titleId Id of application.
     */
    private void updateApplicationTitleid(String titleId) {
        currentApplicationId = titleId;
        updateState(CHANNEL_APPLICATION_ID, StringType.valueOf(titleId));
        logger.debug("Current application title id: {}", titleId);
        if (!isLinked(CHANNEL_APPLICATION_IMAGE)) {
            return;
        }
        LocaleProvider lProvider = localeProvider;
        Locale locale = (lProvider != null) ? lProvider.getLocale() : Locale.US;

        RawType artWork = PS4ArtworkHandler.fetchArtworkForTitleid(titleId, config.artworkSize, locale);
        if (artWork != null) {
            currentArtwork = artWork;
            updateState(CHANNEL_APPLICATION_IMAGE, artWork);
        } else if (!titleId.isEmpty()) {
            logger.debug("Couldn't fetch artwork for title id: {}", titleId);
        }
    }
}
