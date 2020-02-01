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
    private final SonyPS4PacketHandler ps4PacketHandler = new SonyPS4PacketHandler();
    private final SonyPS4ArtworkHandler ps4ArtworkHandler = new SonyPS4ArtworkHandler();
    private static final int SOCKET_TIMEOUT_SECONDS = 4;
    private static final int POST_CONNECT_SENDKEY_DELAY = 1500;
    private static final int MIN_SENDKEY_DELAY = 250; // min delay between sendKey sends
    private static final int MIN_HOLDKEY_DELAY = 300; // min delay after Key set

    private SonyPS4Configuration config = getConfigAs(SonyPS4Configuration.class);

    private @Nullable LocaleProvider localeProvider;
    private @Nullable ScheduledFuture<?> refreshTimer;

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
        if (!config.pairingCode.isEmpty()) {
            logger.debug("Pairing to PS4.");
            if (pairDevice()) {
                // If we are paired, remove pairing code as it's one use only.
                Configuration editedConfig = editConfiguration();
                editedConfig.put(SonyPS4Configuration.PAIRING_CODE, "");
                updateConfiguration(editedConfig);
            }
        }
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
                    standby();
                }
            }
            if (CHANNEL_APPLICATION_TITLEID.equals(channelUID.getId()) && command instanceof StringType) {
                if (!currentApplicationId.equals(((StringType) command).toString())) {
                    updateApplicationTitleid(((StringType) command).toString());
                    startApplication(currentApplicationId);
                }
            }
            if (command instanceof OnOffType) {
                if (CHANNEL_LOG_OUT.equals(channelUID.getId())) {
                    logOut();
                }
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
     * Sets up a refresh timer (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *            initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(this::updateAllChannels, initialWaitTime, 10, TimeUnit.SECONDS);
    }

    private void refreshFromState(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                updateState(channelUID, currentPower);
                break;
            case CHANNEL_APPLICATION_NAME:
                updateState(channelUID, StringType.valueOf(currentApplication));
                break;
            case CHANNEL_APPLICATION_TITLEID:
                updateState(channelUID, StringType.valueOf(currentApplicationId));
                break;
            case CHANNEL_APPLICATION_IMAGE:
                updateApplicationTitleid(currentApplicationId);
                updateState(channelUID, currentArtwork);
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
            byte[] discover = ps4PacketHandler.makeSearchPacket();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);

            // wait for response
            byte[] rxbuf = new byte[256];
            packet = new DatagramPacket(rxbuf, rxbuf.length);
            socket.receive(packet);
            parseSearchResponse(packet);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Fetch status exception: {}", e.getMessage());
        }
    }

    private void wakeUpPS4() {
        logger.debug("Waking up PS4.");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(false);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // send wake-up
            byte[] wakeup = ps4PacketHandler.makeWakeupPacket(config.userCredential);
            DatagramPacket packet = new DatagramPacket(wakeup, wakeup.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Wake up PS4 exception: {}", e.getMessage());
        }
    }

    private boolean openComs() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(false);
            InetAddress inetAddress = InetAddress.getByName(config.ipAddress);
            // send launch
            byte[] launch = ps4PacketHandler.makeLaunchPacket(config.userCredential);
            DatagramPacket packet = new DatagramPacket(launch, launch.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
            return true;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Open coms exception: {}", e.getMessage());
        }
        return false;
    }

    private boolean login(SocketChannel channel) throws IOException {
        logger.debug("TCP connecting");
        String hostName = config.ipAddress;
        channel.configureBlocking(true);

        // TODO Loop here a couple of times and check if we are connected.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.info("Login interrupted: {}", e.getMessage());
        }
        channel.connect(new InetSocketAddress(hostName, currentComPort));
        channel.finishConnect();

        ByteBuffer outPacket = ps4PacketHandler.makeHelloPacket();
        logger.debug("Sending hello packet.");
        channel.write(outPacket);

        // Read hello response
        final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);

        int responseLength = channel.read(readBuffer);
        if (responseLength > 0) {
            ps4PacketHandler.parseResponsePacket(readBuffer);
        } else {
            return false;
        }

        outPacket = ps4PacketHandler.makeHandshakePacket();
        logger.debug("Sending handshake packet.");
        channel.write(outPacket);

        // Send login request
        outPacket = ps4PacketHandler.makeLoginPacket(config.userCredential, config.pinCode, config.pairingCode);
        logger.debug("Sending login packet.");
        // logger.debug("Sending login packet: {}", outPacket);
        channel.write(outPacket);

        // Read login response
        readBuffer.clear();
        responseLength = channel.read(readBuffer);
        if (responseLength > 0) {
            byte[] respBuff = new byte[readBuffer.position()];
            readBuffer.position(0);
            readBuffer.get(respBuff, 0, responseLength);
            int result = ps4PacketHandler.parseEncryptedPacket(readBuffer);
            SonyPS4ErrorStatus status = SonyPS4ErrorStatus.valueOfTag(result);
            if (status != null) {
                switch (status) {
                    case STATUS_OK:
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, status.message);
                        return true;
                    case STATUS_NOT_PAIRED:
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, status.message);
                        break;
                    case STATUS_MISSING_PAIRING_CODE:
                    case STATUS_MISSING_PIN_CODE:
                    case STATUS_WRONG_PAIRING_CODE:
                    case STATUS_WRONG_PIN_CODE:
                    case STATUS_WRONG_USER_CREDENTIAL:
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, status.message);
                        break;
                    case STATUS_COULD_NOT_LOG_IN:
                    case STATUS_ERROR_IN_COMMUNICATION:
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR, status.message);
                        break;
                    default:
                        logger.warn("Unhandled response {}", result);
                        break;
                }
                logger.info("Not logged in: {}", status.message);
            } else {
                logger.info("Error code in login response:{}", result);
            }
        } else {
            logger.debug("No login response!");
        }
        return false;
    }

    private void logOut() {
        if (!openComs()) {
            return;
        }
        try (SocketChannel channel = SocketChannel.open()) {
            if (!login(channel)) {
                return;
            }

            // Send logout request
            logger.debug("Sending logout packet");
            ByteBuffer outPacket = ps4PacketHandler.makeLogoutPacket();
            channel.write(outPacket);

            // Read logout response
            final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
            int responseLength = channel.read(readBuffer);
            if (responseLength > 0) {
                byte[] respBuff = new byte[responseLength];
                readBuffer.position(0);
                readBuffer.get(respBuff, 0, responseLength);
                logger.debug("Logout response: {}", respBuff);
                ps4PacketHandler.parseEncryptedPacket(readBuffer);
            } else {
                logger.warn("No logout response!");
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Log out exception: {}", e.getMessage());
        }
    }

    private void turnOnPS4() {
        wakeUpPS4();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            logger.debug("Login interrupted: {}", e.getMessage());
        }
        if (!openComs()) {
            return;
        }
        try (SocketChannel channel = SocketChannel.open()) {
            login(channel);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Turn on PS4 exception: {}", e.getMessage());
        }
    }

    private void standby() {
        if (!openComs()) {
            return;
        }
        try (SocketChannel channel = SocketChannel.open()) {
            if (!login(channel)) {
                return;
            }

            // Send standby request
            logger.debug("Sending standby packet");
            ByteBuffer outPacket = ps4PacketHandler.makeStandbyPacket();
            channel.write(outPacket);

            // Read standby response
            final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
            int responseLength = channel.read(readBuffer);
            if (responseLength > 0) {
                byte[] respBuff = new byte[responseLength];
                readBuffer.position(0);
                readBuffer.get(respBuff, 0, responseLength);
                logger.debug("Standby response: {}", respBuff);
                ps4PacketHandler.parseEncryptedPacket(readBuffer);
            } else {
                logger.warn("No standby response!");
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Standby exception: {}", e.getMessage());
        }
    }

    private boolean pairDevice() {
        if (!openComs()) {
            return false;
        }
        try (SocketChannel channel = SocketChannel.open()) {
            return login(channel);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Pair device exception: {}", e.getMessage());
        }
        return false;
    }

    private void startApplication(String applicationId) {
        if (!openComs()) {
            return;
        }
        try (SocketChannel channel = SocketChannel.open()) {
            if (!login(channel)) {
                return;
            }

            // Send application request
            logger.debug("Sending app start packet");
            ByteBuffer outPacket = ps4PacketHandler.makeApplicationPacket(applicationId);
            channel.write(outPacket);

            // Read application response
            final ByteBuffer readBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
            int responseLength = channel.read(readBuffer);
            if (responseLength > 0) {
                byte[] respBuff = new byte[responseLength];
                readBuffer.position(0);
                readBuffer.get(respBuff, 0, responseLength);
                logger.debug("App start response: {}", respBuff);
                ps4PacketHandler.parseEncryptedPacket(readBuffer);
            } else {
                logger.debug("No app start response!");
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Start application exception: {}", e.getMessage());
        }
    }

    private void sendRemoteKey(int pushedKey) {
        if (!openComs()) {
            return;
        }
        try (SocketChannel channel = SocketChannel.open()) {
            if (!login(channel)) {
                return;
            }

            Thread.sleep(POST_CONNECT_SENDKEY_DELAY);
            logger.debug("Sending remoteKey");
            ByteBuffer outPacket = ps4PacketHandler.makeRemoteControlPacket(PS4_KEY_OPEN_RC);
            channel.write(outPacket);
            Thread.sleep(MIN_SENDKEY_DELAY);

            // Send remote key
            outPacket = ps4PacketHandler.makeRemoteControlPacket(pushedKey);
            channel.write(outPacket);
            Thread.sleep(MIN_HOLDKEY_DELAY);

            outPacket = ps4PacketHandler.makeRemoteControlPacket(PS4_KEY_OFF);
            channel.write(outPacket);
            Thread.sleep(MIN_SENDKEY_DELAY);

            outPacket = ps4PacketHandler.makeRemoteControlPacket(PS4_KEY_CLOSE_RC);
            channel.write(outPacket);

            // Read remoteKey response
            final ByteBuffer readBuffer = ByteBuffer.allocate(512);
            int responseLength = channel.read(readBuffer);
            if (responseLength > 0) {
                byte[] respBuff = new byte[responseLength];
                readBuffer.position(0);
                readBuffer.get(respBuff, 0, responseLength);
                logger.debug("RemoteKey response: {}", respBuff);
                ps4PacketHandler.parseEncryptedPacket(readBuffer);
            } else {
                logger.warn("No remoteKey response!");
            }

        } catch (InterruptedException e) {
            logger.debug("RemoteKey interrupted: {}", e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("RemoteKey exception: {}", e.getMessage());
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
                    if (currentPower != power) {
                        currentPower = power;
                        updateState(CHANNEL_POWER, currentPower);
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
