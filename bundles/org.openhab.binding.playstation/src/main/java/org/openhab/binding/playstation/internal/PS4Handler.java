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
package org.openhab.binding.playstation.internal;

import static org.openhab.binding.playstation.internal.PlayStationBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.playstation.internal.discovery.PlayStationDiscovery;
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
    private static final int POST_CONNECT_SENDKEY_DELAY = 1500;
    private static final int MIN_SENDKEY_DELAY = 250; // min delay between sendKey sends
    private static final int MIN_HOLDKEY_DELAY = 300; // min delay after Key set

    private PS4Configuration config = new PS4Configuration();

    private @Nullable LocaleProvider localeProvider;
    private @Nullable ScheduledFuture<?> refreshTimer;
    private @Nullable ScheduledFuture<?> timeoutTimer;
    private @Nullable SocketChannelHandler socketChannelHandler;

    // State of PS4
    private String currentApplication = "";
    private String currentApplicationId = "";
    private OnOffType currentPower = OnOffType.OFF;
    private State currentArtwork = UnDefType.UNDEF;
    private int currentComPort = DEFAULT_COMMUNICATION_PORT;

    public PS4Handler(Thing thing, @Nullable LocaleProvider localeProvider) {
        super(thing);
        this.localeProvider = localeProvider;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        SocketChannelHandler scHandler = socketChannelHandler;
        if (!config.pairingCode.isEmpty() && (scHandler == null || !scHandler.loggedIn)) {
            // Try to log in then remove pairing code as it's one use only.
            login();
            Configuration editedConfig = editConfiguration();
            editedConfig.put(PAIRING_CODE, "");
            updateConfiguration(editedConfig);
        }
        setupConnectionTimeout(config.connectionTimeout);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
        } else {
            if (CHANNEL_APPLICATION_ID.equals(channelUID.getId()) && command instanceof StringType) {
                if (!currentApplicationId.equals(((StringType) command).toString())) {
                    updateApplicationTitleid(((StringType) command).toString());
                    startApplication(currentApplicationId);
                }
            }
            if (CHANNEL_OSK_TEXT.equals(channelUID.getId()) && command instanceof StringType) {
                setOSKText(((StringType) command).toString());
            }
            if (command instanceof OnOffType) {
                switch (channelUID.getId()) {
                    case CHANNEL_POWER:
                        currentPower = (OnOffType) command;
                        if (currentPower.equals(OnOffType.ON)) {
                            turnOnPS4();
                        } else if (currentPower.equals(OnOffType.OFF)) {
                            sendStandby();
                        }
                        break;
                    case CHANNEL_KEY_UP:
                        sendRemoteKey(PS4_KEY_UP);
                        break;
                    case CHANNEL_KEY_DOWN:
                        sendRemoteKey(PS4_KEY_DOWN);
                        break;
                    case CHANNEL_KEY_RIGHT:
                        sendRemoteKey(PS4_KEY_RIGHT);
                        break;
                    case CHANNEL_KEY_LEFT:
                        sendRemoteKey(PS4_KEY_LEFT);
                        break;
                    case CHANNEL_KEY_ENTER:
                        sendRemoteKey(PS4_KEY_ENTER);
                        break;
                    case CHANNEL_KEY_BACK:
                        sendRemoteKey(PS4_KEY_BACK);
                        break;
                    case CHANNEL_KEY_OPTION:
                        sendRemoteKey(PS4_KEY_OPTION);
                        break;
                    case CHANNEL_KEY_PS:
                        sendRemoteKey(PS4_KEY_PS);
                        break;
                    case CHANNEL_DISCONNECT:
                        sendByeBye();
                        break;
                    case CHANNEL_LOG_OUT:
                        logOut();
                        break;
                    case CHANNEL_SCREEN_SHOT:
                        takeScreenShot();
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

        updateStatus(ThingStatus.UNKNOWN);
        setupRefreshTimer();
    }

    @Override
    public void dispose() {
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
        stopConnection();
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
                updateState(channelUID, StringType.valueOf(""));
                break;
            case CHANNEL_KEY_UP:
            case CHANNEL_KEY_DOWN:
            case CHANNEL_KEY_RIGHT:
            case CHANNEL_KEY_LEFT:
            case CHANNEL_KEY_ENTER:
            case CHANNEL_KEY_BACK:
            case CHANNEL_KEY_OPTION:
            case CHANNEL_KEY_PS:
            case CHANNEL_DISCONNECT:
                updateState(channelUID, OnOffType.OFF);
                break;
            default:
                logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    private void updateAllChannels() {
        try (DatagramSocket socket = new DatagramSocket()) {
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
            logger.info("Fetch status exception: {}", e.getMessage());
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
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(false);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // send wake-up
            byte[] wakeup = PS4PacketHandler.makeWakeupPacket(config.userCredential);
            DatagramPacket packet = new DatagramPacket(wakeup, wakeup.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.info("Wake up PS4 exception: {}", e.getMessage());
        }
    }

    private class SocketChannelHandler extends Thread {
        private SocketChannel socketChannel;
        private @Nullable PS4Command lastCommand;
        boolean loggedIn = false;
        boolean oskOpen = false;

        public SocketChannelHandler() throws IOException {
            socketChannel = setupChannel();
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
            return channel;
        }

        private boolean openComs() {
            try (DatagramSocket socket = new DatagramSocket()) {
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
                logger.info("Open coms exception: {}", e.getMessage());
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

        @Override
        public void run() {
            SocketChannel channel = socketChannel;
            final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
            try {
                while (channel.read(readBuffer) > 0) {
                    ByteBuffer messBuffer = ps4Crypto.decryptPacket(readBuffer);
                    parseResponsePacket(messBuffer);
                    readBuffer.position(0);

                    if (lastCommand == PS4Command.SERVER_STATUS_RSP) {
                        if (oskOpen && isLinked(CHANNEL_OSK_TEXT)) {
                            sendOSKStart();
                        } else {
                            sendStatus();
                        }
                    }
                }
            } catch (IOException e) {
                logger.info("Connection read exception: {}", e.getMessage());
            } finally {
                try {
                    channel.close();
                } catch (IOException e) {
                    logger.info("Connection close exception: {}", e.getMessage());
                }
            }
            logger.debug("SocketHandler done.");
            ps4Crypto.clearCiphers();
            loggedIn = false;
        }

        /**
         * This is used as a heart beat to let the PS4 know that we are still listening.
         */
        private void sendStatus() {
            ByteBuffer outPacket = PS4PacketHandler.makeStatusPacket(0);
            sendPacketEncrypted(outPacket, false);
        }

        void parseResponsePacket(ByteBuffer rBuffer) {
            rBuffer.rewind();
            final int buffSize = rBuffer.remaining();
            final int size = rBuffer.getInt();
            if (size > buffSize || size < 12) {
                logger.info("Response size ({}) not good, buffer size ({}).", size, buffSize);
                return;
            }
            int cmdValue = rBuffer.getInt();
            int statValue = rBuffer.getInt();
            PS4ErrorStatus status = PS4ErrorStatus.valueOfTag(statValue);
            PS4Command command = PS4Command.valueOfTag(cmdValue);
            byte[] respBuff = new byte[size];
            rBuffer.rewind();
            rBuffer.get(respBuff);
            if (command != null) {
                lastCommand = command;
                if (status == null) {
                    logger.debug("Resp; size:{}, command:{}, statValue:{}, data:{}.", size, command, statValue,
                            respBuff);
                } else {
                    logger.debug("Resp; size:{}, command:{}, status:{}, data:{}.", size, command, status, respBuff);
                }
                switch (command) {
                    case LOGIN_RSP:
                        if (status == null) {
                            logger.info("Unhandled Login status value: {}", statValue);
                            return;
                        }
                        // Read login response
                        switch (status) {
                            case STATUS_OK:
                                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, status.message);
                                loggedIn = true;
                                if (isLinked(CHANNEL_2ND_SCREEN)) {
                                    scheduler.schedule(() -> {
                                        ByteBuffer outPacket = PS4PacketHandler
                                                .makeClientIDPacket("com.playstation.mobile2ndscreen", "18.9.3");
                                        sendPacketEncrypted(outPacket, socketChannel);
                                    }, 10, TimeUnit.MILLISECONDS);
                                }
                                break;
                            case STATUS_NOT_PAIRED:
                                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                        status.message);
                                loggedIn = false;
                                break;
                            case STATUS_MISSING_PAIRING_CODE:
                            case STATUS_MISSING_PASS_CODE:
                            case STATUS_WRONG_PAIRING_CODE:
                            case STATUS_WRONG_PASS_CODE:
                            case STATUS_WRONG_USER_CREDENTIAL:
                                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, status.message);
                                loggedIn = false;
                                logger.info("Not logged in: {}", status.message);
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
                                logger.info("Not logged in: {}", status.message);
                                break;
                            default:
                                logger.info("Unhandled Login response status:{}, message:{}", status, status.message);
                                break;
                        }
                        break;
                    case APP_START_RSP:
                        if (status != null && status != PS4ErrorStatus.STATUS_OK) {
                            logger.info("App start response: {}", status.message);
                        }
                        break;
                    case STANDBY_RSP:
                        if (status != null && status != PS4ErrorStatus.STATUS_OK) {
                            logger.info("Standby response: {}", status.message);
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
                            secondScrStr = "http://" + config.ipAddress + ":" + Integer.toString(port);
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
                        logger.info("Unknown response, command:{}. Missing case.", command);
                        break;
                }
            } else {
                logger.info("Unknown resp-cmd, size:{}, command:{}, status:{}, data:{}.", size, cmdValue, statValue,
                        respBuff);
            }
        }

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
        if (!handler.loggedIn && requiresLogin) {
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
            logger.info("Send packet {} exception: {}", cmd, e.getMessage());
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
            sendPacketToPS4(packet, channel, true, requiresLogin);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.info("Send packet exception: {}", e.getMessage());
        }
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
            logger.info("Send packet exception: {}", e.getMessage());
        }
    }

    private void logOut() {
        ByteBuffer outPacket = PS4PacketHandler.makeLogoutPacket();
        sendPacketEncrypted(outPacket);
    }

    private void takeScreenShot() {
        ByteBuffer outPacket = PS4PacketHandler.makeScreenShotPacket();
        sendPacketEncrypted(outPacket);
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
        scheduler.schedule(this::waitAndConnectToPS4, 13, TimeUnit.SECONDS);
    }

    private void waitAndConnectToPS4() {
        try {
            getConnection();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Turn on PS4 exception: {}", e.getMessage());
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
            int preWait = (scHandler == null || !scHandler.loggedIn) ? POST_CONNECT_SENDKEY_DELAY : 0;
            SocketChannel channel = getConnection();

            scheduler.schedule(() -> {
                ByteBuffer outPacket = PS4PacketHandler.makeRemoteControlPacket(PS4_KEY_OPEN_RC);
                sendPacketEncrypted(outPacket, channel);

                scheduler.schedule(() -> {
                    // Send remote key
                    ByteBuffer keyPacket = PS4PacketHandler.makeRemoteControlPacket(pushedKey);
                    sendPacketEncrypted(keyPacket, channel);

                    scheduler.schedule(() -> {
                        ByteBuffer offPacket = PS4PacketHandler.makeRemoteControlPacket(PS4_KEY_OFF);
                        sendPacketEncrypted(offPacket, channel);

                        scheduler.schedule(() -> {
                            ByteBuffer closePacket = PS4PacketHandler.makeRemoteControlPacket(PS4_KEY_CLOSE_RC);
                            sendPacketEncrypted(closePacket, channel);
                        }, MIN_SENDKEY_DELAY, TimeUnit.MILLISECONDS);

                    }, MIN_HOLDKEY_DELAY, TimeUnit.MILLISECONDS);

                }, MIN_SENDKEY_DELAY, TimeUnit.MILLISECONDS);

            }, preWait, TimeUnit.MILLISECONDS);

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.info("RemoteKey exception: {}", e.getMessage());
        }
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
                            if (scHandler == null || !scHandler.loggedIn) {
                                logger.debug("Trying to login after power on.");
                                scheduler.schedule(() -> login(), 20, TimeUnit.SECONDS);
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
                        logger.info("Host request port: {}", port);
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

    private void updateApplicationTitleid(String titleId) {
        currentApplicationId = titleId;
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
            logger.info("Couldn't fetch artwork for title id: {}", titleId);
        }
    }
}
