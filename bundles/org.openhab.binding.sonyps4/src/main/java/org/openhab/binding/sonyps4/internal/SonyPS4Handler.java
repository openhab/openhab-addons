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
package org.openhab.binding.sonyps4.internal;

import static org.openhab.binding.sonyps4.internal.SonyPS4BindingConstants.*;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPS4Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class SonyPS4Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SonyPS4Handler.class);
    private final SonyPS4Crypto ps4Crypto = new SonyPS4Crypto();
    private final SonyPS4ArtworkHandler ps4ArtworkHandler = new SonyPS4ArtworkHandler();
    private static final int SOCKET_TIMEOUT_SECONDS = 4;
    private static final int POST_CONNECT_SENDKEY_DELAY = 1500;
    private static final int MIN_SENDKEY_DELAY = 250; // min delay between sendKey sends
    private static final int MIN_HOLDKEY_DELAY = 300; // min delay after Key set

    private SonyPS4Configuration config = getConfigAs(SonyPS4Configuration.class);

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

    public SonyPS4Handler(Thing thing, @Nullable LocaleProvider localeProvider) {
        super(thing);
        this.localeProvider = localeProvider;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        SocketChannelHandler scHandler = socketChannelHandler;
        if (scHandler == null || !scHandler.loggedIn) {
            boolean loggedIn = login();
            if (loggedIn && !config.pairingCode.isEmpty()) {
                // If we are paired, remove pairing code as it's one use only.
                Configuration editedConfig = editConfiguration();
                editedConfig.put(SonyPS4Configuration.PAIRING_CODE, "");
                updateConfiguration(editedConfig);
            }
        }
        setupConnectionTimeout(config.connectionTimeout);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
        } else {
            if (CHANNEL_POWER.equals(channelUID.getId()) && command instanceof OnOffType) {
                currentPower = (OnOffType) command;
                if (currentPower.equals(OnOffType.ON)) {
                    turnOnPS4();
                } else if (currentPower.equals(OnOffType.OFF)) {
                    sendStandby();
                }
            }
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
                if (CHANNEL_KEY_UP.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_UP);
                }
                if (CHANNEL_KEY_DOWN.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_DOWN);
                }
                if (CHANNEL_KEY_RIGHT.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_RIGHT);
                }
                if (CHANNEL_KEY_LEFT.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_LEFT);
                }
                if (CHANNEL_KEY_ENTER.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_ENTER);
                }
                if (CHANNEL_KEY_BACK.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_BACK);
                }
                if (CHANNEL_KEY_OPTION.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_OPTION);
                }
                if (CHANNEL_KEY_PS.equals(channelUID.getId())) {
                    sendRemoteKey(PS4_KEY_PS);
                }
                if (CHANNEL_DISCONNECT.equals(channelUID.getId())) {
                    sendByeBye();
                }
                if (CHANNEL_LOG_OUT.equals(channelUID.getId())) {
                    logOut();
                }
                if (CHANNEL_SCREEN_SHOT.equals(channelUID.getId())) {
                    takeScreenShot();
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(SonyPS4Configuration.class);

        updateStatus(ThingStatus.UNKNOWN);
        setupRefreshTimer(1);

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
            refreshTimer = null;
        }
    }

    /**
     * Sets up a timer for querying the PS4 (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh in seconds. Maybe 0 to immediately initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(this::updateAllChannels, initialWaitTime, 10, TimeUnit.SECONDS);
    }

    /**
     * Sets up a timer for stopping the connection to the PS4 (using the scheduler) with the given time.
     *
     * @param waitTime The time in seconds before the connection is stopped.
     */
    private void setupConnectionTimeout(int waitTime) {
        if (timeoutTimer != null) {
            timeoutTimer.cancel(false);
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
            byte[] discover = SonyPS4PacketHandler.makeSearchPacket();
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
        if (handler != null && handler.getChannel() != null) {
            sendByeBye();
        }
    }

    private void wakeUpPS4() {
        logger.debug("Waking up PS4...");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(false);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // send wake-up
            byte[] wakeup = SonyPS4PacketHandler.makeWakeupPacket(config.userCredential);
            DatagramPacket packet = new DatagramPacket(wakeup, wakeup.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.info("Wake up PS4 exception: {}", e.getMessage());
        }
    }

    private boolean openComs() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(false);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // send launch
            byte[] launch = SonyPS4PacketHandler.makeLaunchPacket(config.userCredential);
            DatagramPacket packet = new DatagramPacket(launch, launch.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
            return true;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.info("Open coms exception: {}", e.getMessage());
        }
        return false;
    }

    private boolean setupConnection(SocketChannel channel) throws IOException {
        logger.debug("TCP connecting");

        channel.socket().setSoTimeout(2000);
        channel.configureBlocking(true);
        channel.connect(new InetSocketAddress(config.ipAddress, currentComPort));

        ByteBuffer outPacket = SonyPS4PacketHandler.makeHelloPacket();
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
        private @Nullable SocketChannel socketChannel;
        private @Nullable SonyPS4Command lastCommand;
        boolean loggedIn = false;
        boolean oskOpen = false;

        public SocketChannelHandler(SocketChannel channel) {
            socketChannel = channel;
        }

        public @Nullable SocketChannel getChannel() {
            return socketChannel;
        }

        @Override
        public void run() {
            SocketChannel channel = socketChannel;
            if (channel == null) {
                return;
            }
            final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
            try {
                while (channel.read(readBuffer) > 0) {
                    ByteBuffer messBuffer = ps4Crypto.decryptPacket(readBuffer);
                    parseResponsePacket(messBuffer);
                    readBuffer.position(0);

                    if (lastCommand == SonyPS4Command.SERVER_STATUS_RSP) {
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
            socketChannel = null;
            ps4Crypto.clearCiphers();
            loggedIn = false;
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
            SonyPS4ErrorStatus status = SonyPS4ErrorStatus.valueOfTag(statValue);
            SonyPS4Command command = SonyPS4Command.valueOfTag(cmdValue);
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
                        if (status != null && status != SonyPS4ErrorStatus.STATUS_OK) {
                            logger.info("App start response: {}", status.message);
                        }
                        break;
                    case STANDBY_RSP:
                        if (status != null && status != SonyPS4ErrorStatus.STATUS_OK) {
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
                        String httpdStat = SonyPS4PacketHandler.parseHTTPdPacket(rBuffer);
                        logger.debug("HTTPd Response; {}", httpdStat);
                        break;
                    case OSK_CHANGE_STRING_REQ:
                        String oskText = SonyPS4PacketHandler.parseOSKStringChangePacket(rBuffer);
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
        if (handler == null || handler.getChannel() == null) {
            try {
                channel = SocketChannel.open();
                if (!openComs()) {
                    throw new IOException("Open coms failed");
                }
                if (!setupConnection(channel)) {
                    throw new IOException("Setup connection failed");
                }
                handler = new SocketChannelHandler(channel);
                socketChannelHandler = handler;
                handler.start();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                throw e;
            }
        }
        channel = handler.getChannel();
        if (channel != null) {
            if (!handler.loggedIn && requiresLogin) {
                login(channel);

                if (handler.loggedIn) {
                    try {
                        Thread.sleep(1000);
                        ByteBuffer outPacket = SonyPS4PacketHandler
                                .makeClientIDPacket("com.playstation.mobile2ndscreen", "18.9.3");
                        sendPacketEncrypted(outPacket, channel);

                        Thread.sleep(POST_CONNECT_SENDKEY_DELAY);
                    } catch (InterruptedException e) {
                        // Ignore, sleep just to make keyEvents behave better.
                    }
                }
            }
            return channel;
        }
        throw new IOException("No channel allocated");
    }

    private void sendPacketToPS4(ByteBuffer packet, SocketChannel channel, boolean encrypted, boolean restartTimeout) {
        SonyPS4Command cmd = SonyPS4Command.valueOfTag(packet.getInt(4));
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

    /**
     * This is used as a heart beat to let the PS4 know that we are still listening.
     */
    private void sendStatus() {
        ByteBuffer outPacket = SonyPS4PacketHandler.makeStatusPacket(1);
        sendPacketEncrypted(outPacket, false);
    }

    private boolean login(SocketChannel channel) {
        // Send login request
        ByteBuffer outPacket = SonyPS4PacketHandler.makeLoginPacket(config.userCredential, config.passCode,
                config.pairingCode);
        sendPacketEncrypted(outPacket, channel);

        return true;
    }

    private boolean login() {
        try {
            SocketChannel channel = getConnection(false);
            return login(channel);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.info("Send packet exception: {}", e.getMessage());
        }
        return false;
    }

    private void logOut() {
        ByteBuffer outPacket = SonyPS4PacketHandler.makeLogoutPacket();
        sendPacketEncrypted(outPacket);
    }

    private void takeScreenShot() {
        ByteBuffer outPacket = SonyPS4PacketHandler.makeScreenShotPacket();
        sendPacketEncrypted(outPacket);
    }

    /**
     * This closes the connection with the PS4.
     */
    private void sendByeBye() {
        ByteBuffer outPacket = SonyPS4PacketHandler.makeByebyePacket();
        sendPacketEncrypted(outPacket, false);
    }

    private void turnOnPS4() {
        wakeUpPS4();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            logger.debug("Trun on PS4 interrupted: {}", e.getMessage());
        }
        try {
            getConnection();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Turn on PS4 exception: {}", e.getMessage());
        }
    }

    private void sendStandby() {
        ByteBuffer outPacket = SonyPS4PacketHandler.makeStandbyPacket();
        sendPacketEncrypted(outPacket);
    }

    /**
     * Ask PS4 if the OSK is open so we can get and set text.
     */
    private void sendOSKStart() {
        ByteBuffer outPacket = SonyPS4PacketHandler.makeOSKStartPacket();
        sendPacketEncrypted(outPacket);
    }

    /**
     * Sets the entire OSK string on the PS4.
     *
     * @param text The text to set in the OSK.
     */
    private void setOSKText(String text) {
        logger.debug("Sending osk text packet,\"{}\"", text);
        ByteBuffer outPacket = SonyPS4PacketHandler.makeOSKStringChangePacket(text);
        sendPacketEncrypted(outPacket);
    }

    /**
     * Tries to start an application on the PS4.
     *
     * @param applicationId The unique id for the application (CUSAxxxxx).
     */
    private void startApplication(String applicationId) {
        ByteBuffer outPacket = SonyPS4PacketHandler.makeApplicationPacket(applicationId);
        sendPacketEncrypted(outPacket);
    }

    private void sendRemoteKey(int pushedKey) {
        try {
            SocketChannel channel = getConnection();

            ByteBuffer outPacket = SonyPS4PacketHandler.makeRemoteControlPacket(PS4_KEY_OPEN_RC);
            sendPacketEncrypted(outPacket, channel);
            Thread.sleep(MIN_SENDKEY_DELAY);

            // Send remote key
            outPacket = SonyPS4PacketHandler.makeRemoteControlPacket(pushedKey);
            sendPacketEncrypted(outPacket, channel);
            Thread.sleep(MIN_HOLDKEY_DELAY);

            outPacket = SonyPS4PacketHandler.makeRemoteControlPacket(PS4_KEY_OFF);
            sendPacketEncrypted(outPacket, channel);
            Thread.sleep(MIN_SENDKEY_DELAY);

            outPacket = SonyPS4PacketHandler.makeRemoteControlPacket(PS4_KEY_CLOSE_RC);
            sendPacketEncrypted(outPacket, channel);

        } catch (InterruptedException e) {
            logger.debug("RemoteKey interrupted: {}", e.getMessage());
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

        String[] ss = message.trim().split("\n");
        for (String row : ss) {
            int index = row.indexOf(':');
            if (index == -1) {
                OnOffType power = null;
                if (row.contains("200")) {
                    power = OnOffType.ON;
                } else if (row.contains("620")) {
                    power = OnOffType.OFF;
                }
                if (power != null) {
                    if (!currentPower.equals(power)) {
                        currentPower = power;
                        updateState(CHANNEL_POWER, currentPower);
                        if (power.equals(OnOffType.ON) && config.autoConnect
                                && (socketChannelHandler == null || !socketChannelHandler.loggedIn)) {
                            logger.debug("Trying to login after power on.");
                            scheduler.schedule(() -> login(), 20, TimeUnit.SECONDS);
                        }
                    }
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
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
                    updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, value);
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
        Locale locale = Locale.US;
        if (localeProvider != null) {
            locale = localeProvider.getLocale();
        }
        RawType artWork = ps4ArtworkHandler.fetchArtworkForTitleid(titleId, config.artworkSize, locale);
        if (artWork != null) {
            currentArtwork = artWork;
            updateState(CHANNEL_APPLICATION_IMAGE, artWork);
        } else if (!titleId.isEmpty()) {
            logger.info("Couldn't fetch artwork for title id: {}", titleId);
        }
    }
}
