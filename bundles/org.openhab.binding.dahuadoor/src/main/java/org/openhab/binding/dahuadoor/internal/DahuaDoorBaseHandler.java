/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dahuadoor.internal;

import static org.openhab.binding.dahuadoor.internal.DahuaDoorBindingConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.sip.InvalidArgumentException;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.TransportNotSupportedException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dahuadoor.internal.dahuaeventhandler.DHIPEventListener;
import org.openhab.binding.dahuadoor.internal.dahuaeventhandler.DahuaEventClient;
import org.openhab.binding.dahuadoor.internal.media.Go2RtcManager;
import org.openhab.binding.dahuadoor.internal.media.PlayStreamServlet;
import org.openhab.binding.dahuadoor.internal.media.SipBackchannelRtpRelay;
import org.openhab.binding.dahuadoor.internal.media.SipBackchannelSession;
import org.openhab.binding.dahuadoor.internal.sip.SipClient;
import org.openhab.binding.dahuadoor.internal.sip.SipEventListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link DahuaDoorBaseHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public abstract class DahuaDoorBaseHandler extends BaseThingHandler implements DHIPEventListener, SipEventListener {

    protected final Logger logger = LoggerFactory.getLogger(DahuaDoorBaseHandler.class);
    protected @Nullable DahuaDoorConfiguration config;

    protected Gson gson = new Gson();

    protected @Nullable DahuaEventClient client = null;

    private final PlayStreamServlet playStreamServlet;
    private @Nullable Go2RtcManager go2rtcManager;
    private @Nullable SipBackchannelRtpRelay sipBackchannelRelay;
    private @Nullable SipClient sipClient;
    private @Nullable Future<?> webRtcStartupJob;
    private @Nullable Future<?> sipStartupJob;
    private @Nullable ScheduledFuture<?> sipReRegisterJob;
    private final Map<String, String> sessionToClientId = new ConcurrentHashMap<>();
    private final Map<String, SipClient> sipClients = new ConcurrentHashMap<>();
    private final Map<String, SipBackchannelSession> backchannelSessionsByHttpSession = new ConcurrentHashMap<>();
    private static final String[] AVAILABLE_CLIENT_IDS = { "client-1", "client-2", "client-3" };
    private static final int SIP_BACKCHANNEL_LISTEN_PORT_OFFSET = 20000;
    private static final long SIP_INVITE_DEDUP_MS = 1500;
    private static final long SESSION_TTL_MS = TimeUnit.MINUTES.toMillis(30);
    private static final int MAX_SESSION_MAPPINGS = 256;
    private volatile boolean disposed;
    private volatile long lastSipInviteTs = 0L;

    public DahuaDoorBaseHandler(Thing thing, PlayStreamServlet playStreamServlet) {
        super(thing);
        this.playStreamServlet = playStreamServlet;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        DahuaEventClient localClient = client;
        if (localClient == null) {
            logger.warn("Client not initialized, cannot handle command");
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_OPEN_DOOR_1:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        localClient.openDoor(1);
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
                break;
            case CHANNEL_OPEN_DOOR_2:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        localClient.openDoor(2);
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
                break;
        }
    }

    public void errorInformer(String msgError) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msgError);
    }

    @Override
    public void initialize() {
        disposed = false;

        DahuaDoorConfiguration localConfig = getConfigAs(DahuaDoorConfiguration.class);
        config = localConfig;

        // Validate required configuration
        if (localConfig.hostname.isBlank() || localConfig.username.isBlank() || localConfig.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-credentials");
            return;
        }

        if (localConfig.snapshotPath.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-snapshot-path");
            return;
        }

        if (localConfig.enableWebRTC && localConfig.go2rtcPath.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-go2rtc-path");
            return;
        }

        client = new DahuaEventClient(localConfig.hostname, localConfig.username, localConfig.password,
                localConfig.useHttps, this, this::errorInformer);

        if (localConfig.enableWebRTC && localConfig.enableSip) {
            startSipBackchannelRelay(localConfig);
        }

        if (localConfig.enableWebRTC) {
            startWebRtc(localConfig);
        }

        if (localConfig.enableSip) {
            startSip(localConfig);
            updateState(CHANNEL_SIP_CALL_STATE, new StringType(SipClient.SipCallState.IDLE.name()));
        }

        // Set status to UNKNOWN - will be set to ONLINE when first DHIP event is received
        updateStatus(ThingStatus.UNKNOWN);
    }

    private void startSipBackchannelRelay(DahuaDoorConfiguration cfg) {
        int listenPort = cfg.go2rtcApiPort + SIP_BACKCHANNEL_LISTEN_PORT_OFFSET;
        if (!isValidPort(listenPort)) {
            logger.warn(
                    "SIP backchannel relay port {} is outside valid range (go2rtcApiPort={}, offset={}) - relay disabled",
                    listenPort, cfg.go2rtcApiPort, SIP_BACKCHANNEL_LISTEN_PORT_OFFSET);
            return;
        }
        SipBackchannelRtpRelay relay = new SipBackchannelRtpRelay(listenPort, 0);
        try {
            relay.start();
            int sourcePort = relay.getSourcePort();
            if (!isValidPort(sourcePort)) {
                relay.stop();
                logger.warn("SIP backchannel relay source port {} is outside valid range - relay disabled", sourcePort);
                return;
            }
            sipBackchannelRelay = relay;
        } catch (IOException e) {
            sipBackchannelRelay = null;
            logger.warn("Failed to start SIP backchannel RTP relay on 127.0.0.1:{}: {}", listenPort, e.getMessage(), e);
        }
    }

    private void stopSipBackchannelRelay() {
        SipBackchannelRtpRelay relay = sipBackchannelRelay;
        sipBackchannelRelay = null;
        if (relay != null) {
            relay.stop();
        }
    }

    /**
     * Starts the go2rtc sidecar and registers the SDP proxy servlet for this thing.
     * Runs in a background thread to avoid blocking the openHAB initialize() call.
     *
     * @param cfg current configuration snapshot
     */
    private void startWebRtc(DahuaDoorConfiguration cfg) {
        // Derive a URL-safe stream name from the thing UID (replace special chars with _)
        String thingUidSafe = getThing().getUID().toString().replace(":", "_").replace("-", "_").replace(".", "_");
        String streamName = GO2RTC_STREAM_PREFIX + thingUidSafe;

        Go2RtcManager manager = new Go2RtcManager(cfg.go2rtcPath, cfg.go2rtcApiPort, cfg.webRtcPort, cfg.stunServer,
                streamName, cfg.hostname, cfg.username, cfg.password, cfg.rtspChannel, cfg.rtspSubtype);
        go2rtcManager = manager;

        // Publish the proxy URL immediately so the UI shows the path even before go2rtc is ready
        String proxyPath = WEBRTC_SERVLET_PATH + "/" + streamName;
        updateState(CHANNEL_WEBRTC_URL, new StringType(proxyPath));

        webRtcStartupJob = scheduler.submit(() -> {
            try {
                if (disposed || !Objects.equals(go2rtcManager, manager)) {
                    return;
                }

                // 2. Start go2rtc (includes blocking health-check polling)
                manager.start();

                if (disposed || !Objects.equals(go2rtcManager, manager)) {
                    manager.stop();
                    return;
                }

                // 3. Register stream with the SDP proxy servlet
                playStreamServlet.registerStream(streamName, cfg.go2rtcApiPort);
                logger.info("WebRTC streaming active for {} at {}", streamName, proxyPath);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                logger.warn("Failed to start WebRTC streaming for {}: {}", streamName, e.getMessage(), e);
                if (!disposed && Objects.equals(go2rtcManager, manager)) {
                    updateState(CHANNEL_WEBRTC_URL, UnDefType.UNDEF);
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "WebRTC startup failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void dispose() {
        disposed = true;
        stopSip();
        stopWebRtc();
        DahuaEventClient localClient = client;
        if (localClient != null) {
            localClient.dispose();
            client = null;
        }
    }

    /**
     * Stops the go2rtc sidecar and de-registers the SDP proxy servlet entry for this thing.
     */
    private void stopWebRtc() {
        Future<?> localStartupJob = webRtcStartupJob;
        webRtcStartupJob = null;
        if (localStartupJob != null) {
            localStartupJob.cancel(true);
        }

        Go2RtcManager localManager = go2rtcManager;
        go2rtcManager = null;
        if (localManager != null) {
            playStreamServlet.unregisterStream(localManager.getStreamName());
            localManager.stop();
        }
        stopSipBackchannelRelay();
    }

    public void saveSnapshot(byte @Nullable [] buffer) {
        final DahuaDoorConfiguration localConfig = config;
        if (localConfig == null) {
            logger.warn("Configuration not initialized");
            return;
        }
        if (localConfig.snapshotPath.isEmpty()) {
            logger.warn("Path for Snapshots is invalid");
            return;
        }
        if (buffer == null) {
            logger.warn("cannot save empty buffer");
            return;
        }

        // Ensure snapshot directory exists
        File snapshotDir = new File(localConfig.snapshotPath);
        if (!snapshotDir.exists() && !snapshotDir.mkdirs()) {
            logger.warn("Could not create snapshot directory '{}', check permissions", localConfig.snapshotPath);
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT).format(new Date());
        String filename = localConfig.snapshotPath + "/DoorBell_" + timestamp + ".jpg";

        try (FileOutputStream fos = new FileOutputStream(new File(filename))) {
            fos.write(buffer);
        } catch (IOException e) {
            logger.warn("Could not write image to file '{}', check permissions and path", filename, e);
        }

        // Write buffer directly to latest snapshot file (avoids copy-from-source failures)
        String latestSnapshotFilename = localConfig.snapshotPath + "/Doorbell.jpg";
        try (FileOutputStream fos = new FileOutputStream(new File(latestSnapshotFilename))) {
            fos.write(buffer);
        } catch (IOException e) {
            logger.warn("Could not write latest snapshot to '{}', check permissions and path", latestSnapshotFilename,
                    e);
        }
    }

    private void updateChannelImage(byte @Nullable [] buffer) {
        if (buffer == null || buffer.length == 0) {
            updateState(CHANNEL_DOOR_IMAGE, UnDefType.UNDEF);
            return;
        }
        RawType image = new RawType(buffer, "image/jpeg");
        updateState(CHANNEL_DOOR_IMAGE, image);
    }

    protected void handleButtonPressed() {
        Channel channel = this.getThing().getChannel(CHANNEL_BELL_BUTTON);
        if (channel == null) {
            logger.warn("Bell button channel not found");
            return;
        }
        triggerChannel(channel.getUID(), "PRESSED");

        DahuaEventClient localClient = client;
        if (localClient == null) {
            logger.warn("Client not initialized, cannot retrieve doorbell image");
            return;
        }
        byte[] buffer = localClient.requestImage();
        updateChannelImage(buffer);
        saveSnapshot(buffer);
    }

    @Override
    public void onEvent(JsonObject eventData) {
        // Set thing ONLINE when first event is received (confirms successful
        // connection)
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        try {
            logger.trace("JSON{}", eventData);
            JsonObject jsonObj = eventData.getAsJsonObject("params");
            if (jsonObj == null) {
                logger.debug("Missing 'params' object in DahuaDoor event. Raw payload: {}", eventData.toString());
                return;
            }

            JsonArray firstLevel = jsonObj.getAsJsonArray("eventList");
            if (firstLevel == null || firstLevel.isEmpty()) {
                logger.debug("Missing or empty 'eventList' array in DahuaDoor event. Raw payload: {}",
                        eventData.toString());
                return;
            }

            JsonObject eventList = firstLevel.get(0).getAsJsonObject();
            if (!eventList.has("Code")) {
                logger.debug("Missing 'Code' in event data. Raw payload: {}", eventData.toString());
                return;
            }

            String eventCodeString = eventList.get("Code").getAsString();
            DahuaEventCode eventCode = DahuaEventCode.fromString(eventCodeString);
            JsonObject eventPayload = eventList.getAsJsonObject("Data");
            if (firstLevel.size() > 1) {
                logger.debug("Event Manager subscription reply: {}", eventCodeString);
            } else {
                switch (eventCode) {
                    case CALL_NO_ANSWERED:
                        handleVTOCall();
                        break;
                    case IGNORE_INVITE:
                        handleVTHAnswer();
                        break;
                    case VIDEO_MOTION:
                        handleMotionEvent();
                        break;
                    case RTSP_SESSION_DISCONNECT:
                        handleRTSPDisconnect(eventList, eventPayload);
                        break;
                    case BACK_KEY_LIGHT:
                        handleBackKeyLight(eventPayload);
                        break;
                    case TIME_CHANGE:
                        handleTimeChange(eventPayload);
                        break;
                    case NTP_ADJUST_TIME:
                        handleNTPAdjust(eventPayload);
                        break;
                    case KEEP_LIGHT_ON:
                        handleKeepLightOn(eventPayload);
                        break;
                    case VIDEO_BLIND:
                        handleVideoBlind(eventList);
                        break;
                    case FINGER_PRINT_CHECK:
                        handleFingerPrintCheck(eventPayload);
                        break;
                    case DOOR_CARD:
                        handleDoorCard(eventList, eventPayload);
                        break;
                    case SIP_REGISTER_RESULT:
                        handleSIPRegisterResult(eventList, eventPayload);
                        break;
                    case ACCESS_CONTROL:
                        handleAccessControl(eventPayload);
                        break;
                    case CALL_SNAP:
                        handleCallSnap(eventPayload);
                        break;
                    case HUNGUP_PHONE:
                        handleHungupPhone(eventList, eventPayload);
                        break;
                    case HANGUP_PHONE:
                        handleHangupPhone(eventList, eventPayload);
                        break;
                    case HANGUP:
                        handleHangup(eventList, eventPayload);
                        break;
                    case INVITE:
                        handleInvite(eventList, eventPayload);
                        break;
                    case ALARM_LOCAL:
                        handleAlarmLocal(eventList, eventPayload);
                        break;
                    case ACCESS_SNAP:
                        handleAccessSnap(eventPayload);
                        break;
                    case REQUEST_CALL_STATE:
                        handleRequestCallState(eventList, eventPayload);
                        break;
                    case PASSIVE_HANGUP:
                        handlePassiveHangup(eventList, eventPayload);
                        break;
                    case PROFILE_ALARM_TRANSMIT:
                        handleProfileAlarmTransit(eventList, eventPayload);
                        break;
                    case NEW_FILE:
                        handleNewFile(eventList, eventPayload);
                        break;
                    case UPDATE_FILE:
                        handleUpdateFile(eventList, eventPayload);
                        break;
                    case REBOOT:
                        handleReboot(eventList, eventPayload);
                        break;
                    case SECURITY_IM_EXPORT:
                        handleSecurityImport(eventList, eventPayload);
                        break;
                    case DGS_ERROR_REPORT:
                        handleDGSErrorReport(eventList, eventPayload);
                        break;
                    case UPGRADE:
                        handleUpgrade(eventList, eventPayload);
                        break;
                    case SEND_CARD:
                        handleSendCard(eventList, eventPayload);
                        break;
                    case ADD_CARD:
                        handleAddCard(eventList, eventPayload);
                        break;
                    case DOOR_STATUS:
                        handleDoorStatus(eventList, eventPayload);
                        break;
                    case DOOR_CONTROL:
                        handleDoorControl(eventList, eventPayload);
                        break;
                    case DOOR_NOT_CLOSED:
                        handleDoorNotClosed(eventList, eventPayload);
                        break;
                    case NETWORK_CHANGE:
                        handleNetworkChanged(eventList, eventPayload);
                        break;
                    case UNKNOWN:
                    default:
                        logger.debug("Unknown event received. JSON{}", gson.toJson(eventData));
                }
            }
        } catch (IllegalStateException e) {
            logger.debug("Invalid JSON structure while handling DahuaDoor event. Raw payload: {}", eventData.toString(),
                    e);
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Missing expected array elements in DahuaDoor event. Raw payload: {}", eventData.toString(),
                    e);
        }
    }

    protected void handleNetworkChanged(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: NetworkChange, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleDoorNotClosed(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DoorNotClosed, Action {}, Name{}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("Name").getAsString(), eventData.get("LocaleTime").getAsString());
    }

    protected void handleDoorControl(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DoorControl, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleDoorStatus(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DoorStatus, Action {}, Status: {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("Status").getAsString(), eventData.get("LocaleTime").getAsString());
    }

    protected void handleAddCard(JsonObject eventList, JsonObject eventData) {
        JsonObject cardData = eventData.getAsJsonArray("Data").get(0).getAsJsonObject();
        logger.debug(
                "Event: AddCard, Action {}: CardNo {}, UserID {}, UserName {}, CardStatus {}, CardType {}, Doors: Door 0={}, Door1={}",
                eventList.get("Action").getAsString(), cardData.get("CardNo").getAsString(),
                cardData.get("UserID").getAsString(), cardData.get("UserName").getAsString(),
                cardData.get("CardStatus").getAsString(), cardData.get("CardType").getAsString(),
                cardData.getAsJsonArray("Doors").get(0).getAsString(),
                cardData.getAsJsonArray("Doors").get(1).getAsString());
    }

    protected void handleSendCard(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: SendCard, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleUpgrade(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Upgrade, Action {}, with State{}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("State").getAsString(), eventData.get("LocaleTime").getAsString());
    }

    protected void handleDGSErrorReport(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DGSErrorReport, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleSecurityImport(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: SecurityImExport, Action {}, LocaleTime {}, Status {}",
                eventList.get("Action").getAsString(), eventData.get("LocaleTime").getAsString(),
                eventData.get("Status").getAsString());
    }

    protected void handleReboot(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Reboot, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleUpdateFile(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: UpdateFile, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleNewFile(JsonObject eventList, JsonObject eventData) {
        String action = eventList.has("Action") ? eventList.get("Action").getAsString() : "unknown";
        String file = eventData.has("File") ? eventData.get("File").getAsString() : "unknown";
        String folder = eventData.has("Filter") ? eventData.get("Filter").getAsString() : "unknown";
        // not a typo: Filter works for folder, seems to be a naming error in the Dahua firmware
        String localeTime = eventData.has("LocaleTime") ? eventData.get("LocaleTime").getAsString() : "unknown";
        String index = eventList.has("Index") ? eventList.get("Index").getAsString() : "unknown";

        logger.debug("Event: NewFile, Action {}, File {}, Folder {}, LocaleTime {}, Index {}", action, file, folder,
                localeTime, index);
    }

    protected void handleProfileAlarmTransit(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: ProfileAlarmTransmit, Action {}, AlarmType {}, DevSrcType {}, SenseMethod {}",
                eventList.get("Action").getAsString(), eventData.get("AlarmType").getAsString(),
                eventData.get("DevSrcType").getAsString(), eventData.get("SenseMethod").getAsString());
    }

    protected void handlePassiveHangup(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: PassiveHangup, Action {}, LocaleTime {}, Index {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString(), eventData.get("Index").getAsString());
    }

    protected void handleRequestCallState(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: RequestCallState, Action {}, LocaleTime {}, Index {}",
                eventList.get("Action").getAsString(), eventData.get("LocaleTime").getAsString(),
                eventData.get("Index").getAsString());
    }

    protected void handleAccessSnap(JsonObject eventData) {
        logger.debug("Event: AccessSnap, FTP upload to {}", eventData.get("FtpUrl").getAsString());
    }

    protected void handleAlarmLocal(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: AlarmLocal, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleInvite(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Invite, Action {}, CallID {}, Lock Number {}", eventList.get("Action").getAsString(),
                eventData.get("CallID").getAsString(), eventData.get("LockNum").getAsString());
    }

    protected void handleHangup(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Hangup, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleHungupPhone(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: HungupPhone, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleHangupPhone(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: HangupPhone, Action {}, LocaleTime {}", eventList.get("Action").getAsString(),
                eventData.get("LocaleTime").getAsString());
    }

    protected void handleCallSnap(JsonObject eventData) {
        logger.debug("Event: CallSnap, DeviceType {}, RemoteID {}, RemoteIP {}, CallStatus {}",
                eventData.get("DeviceType").getAsString(), eventData.get("RemoteID").getAsString(),
                eventData.get("RemoteIP").getAsString(),
                eventData.getAsJsonArray("ChannelStates").get(0).getAsString());
    }

    protected void handleAccessControl(JsonObject eventData) {
        logger.debug("Event: AccessControl, Name {}, Method {}, ReaderID {}, UserID {}",
                eventData.get("Name").getAsString(), eventData.get("Method").getAsString(),
                eventData.get("ReaderID").getAsString(), eventData.get("UserID").getAsString());
    }

    protected void handleSIPRegisterResult(JsonObject eventList, JsonObject eventData) {
        if ("Pulse".equals(eventList.get("Action").getAsString())) {
            boolean success = eventData.get("Success").getAsBoolean();
            updateState(CHANNEL_SIP_REGISTERED, OnOffType.from(success));
            if (success) {
                logger.debug("Event SIPRegisterResult, Success");
                DahuaDoorConfiguration localConfig = config;
                DahuaEventClient localClient = client;
                if (localConfig != null && localConfig.enableWebRTC && localClient != null) {
                    scheduler.submit(localClient::fixAudioCodec);
                }
            } else {
                logger.debug("Event SIPRegisterResult, Failed");
            }
        }
    }

    protected void handleDoorCard(JsonObject eventList, JsonObject eventData) {
        if ("Pulse".equals(eventList.get("Action").getAsString())) {
            logger.debug("DoorCard {} was used at door", eventData.get("Number").getAsString());
        }
    }

    protected void handleFingerPrintCheck(JsonObject eventData) {
        if (eventData.get("FingerPrintID").getAsInt() > -1) {
            int finger = eventData.get("FingerPrintID").getAsInt();
            logger.debug("Event FingerPrintCheck success, Finger number {}, User {}", finger, "User" + finger);
        } else {
            logger.debug("Event FingerPrintCheck failed, unknown Finger");
        }
    }

    protected void handleVideoBlind(JsonObject eventList) {
        if ("Start".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event VideoBlind started");
        } else if ("Stop".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event VideoBlind stopped");
        }
    }

    protected void handleKeepLightOn(JsonObject eventData) {
        if ("On".equals(eventData.get("Status").getAsString())) {
            logger.debug("Event KeepLightOn");
        } else if ("Off".equals(eventData.get("Status").getAsString())) {
            logger.debug("Event KeepLightOff");
        }
    }

    protected void handleNTPAdjust(JsonObject eventData) {
        if (eventData.get("result").getAsBoolean()) {
            logger.debug("Event NTPAdjustTime with {} success", eventData.get("Address").getAsString());
        } else {
            logger.debug("Event NTPAdjustTime failed");
        }
    }

    protected void handleTimeChange(JsonObject eventData) {
        logger.debug("Event TimeChange, BeforeModifyTime: {}, ModifiedTime: {}",
                eventData.get("BeforeModifyTime").getAsString(), eventData.get("ModifiedTime").getAsString());
    }

    protected void handleBackKeyLight(JsonObject eventData) {
        logger.debug("Event BackKeyLight with State {} ", eventData.get("State").getAsString());
    }

    protected void handleRTSPDisconnect(JsonObject eventList, JsonObject eventData) {
        if ("Start".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event Rtsp-Session from {} disconnected",
                    eventData.get("Device").getAsString().replace("::ffff:", ""));
        } else if ("Stop".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event Rtsp-Session from {} connected",
                    eventData.get("Device").getAsString().replace("::ffff:", ""));
        }
    }

    protected void handleMotionEvent() {
        logger.debug("Event VideoMotion");
    }

    protected void handleVTHAnswer() {
        logger.debug("Event VTH answered call from VTO");
    }

    protected void handleVTOCall() {
        logger.debug("Event Call from VTO");
    }

    /**
     * Abstract method to handle button press events with lock number identification.
     * Subclasses must implement this to handle single or multi-button devices.
     *
     * @param lockNumber The lock number from the Invite event (1 or 2)
     */
    protected abstract void onButtonPressed(int lockNumber);

    /**
     * Routes a resolved doorbell event from DHIP to the concrete device handler.
     *
     * DHIP remains lossless and authoritative for LockNum-based button mapping, so no time-based dedup
     * or SIP gating is applied here.
     *
     * @param source event source label for logging (for example "DHIP")
     * @param lockNumber resolved doorbell/button number
     */
    protected void handleResolvedDoorbellEvent(String source, int lockNumber) {
        onButtonPressed(lockNumber);
    }

    /**
     * Deduplicates SIP INVITE callbacks so UDP retransmits do not spam state updates and logs.
     *
     * @param source event source label for logging (for example "SIP")
     * @return true when caller should continue processing, false when event should be ignored as duplicate
     */
    protected boolean shouldProcessSipInvite(String source) {
        long now = System.currentTimeMillis();
        long previous = lastSipInviteTs;
        if (previous > 0 && (now - previous) < SIP_INVITE_DEDUP_MS) {
            logger.debug("Ignoring duplicate {} INVITE ({} ms after previous event)", source, now - previous);
            return false;
        }
        lastSipInviteTs = now;
        return true;
    }

    // ============================================================================
    // SIP Client Integration (Phase 1: Minimal Signaling)
    // ============================================================================

    /**
     * Starts the SIP client and registers with the VTO SIP server.
     * Runs in a background thread to avoid blocking the initialize() call.
     *
     * @param cfg current configuration snapshot
     */
    private void startSip(DahuaDoorConfiguration cfg) {
        if (cfg.sipExtension.isBlank()) {
            logger.warn("SIP enabled but sipExtension not configured - skipping SIP registration");
            updateState(CHANNEL_SIP_REGISTERED, UnDefType.UNDEF);
            updateState(CHANNEL_SIP_CALL_STATE, UnDefType.UNDEF);
            return;
        }

        sipStartupJob = scheduler.submit(() -> {
            try {
                String localIp = detectLocalIp(cfg.hostname);
                logger.debug("Detected local IP: {}", localIp);

                if (disposed) {
                    return;
                }

                // Use sipExtension as SIP username (extension == username in Dahua VTO)
                // Use separate SIP password if provided, otherwise fall back to API password
                String sipPass = !cfg.sipPassword.isEmpty() ? cfg.sipPassword : cfg.password;
                int localAudioPort = 0;
                SipBackchannelRtpRelay relay = sipBackchannelRelay;
                if (relay != null) {
                    localAudioPort = relay.getSourcePort();
                    if (!isValidPort(localAudioPort)) {
                        logger.warn("SIP backchannel relay source port {} is outside valid range - disabling SDP audio",
                                localAudioPort);
                        localAudioPort = 0;
                    }
                }

                SipClient newSipClient = new SipClient(cfg.hostname, cfg.sipExtension, cfg.sipExtension, sipPass,
                        cfg.localSipPort, localIp, cfg.sipRealm, localAudioPort, sipBackchannelRelay, this,
                        this::errorInformer);

                if (disposed) {
                    newSipClient.dispose();
                    return;
                }

                sipClient = newSipClient;
                sipClients.put("client-1", newSipClient);

                // initializeSipStack() already called in constructor
                newSipClient.sendRegister();

                if (disposed || !Objects.equals(sipClient, newSipClient)) {
                    newSipClient.dispose();
                    return;
                }

                // Schedule re-REGISTER every 50 seconds (VTO expires after 60s)
                sipReRegisterJob = scheduler.scheduleWithFixedDelay(() -> {
                    SipClient localClient = sipClient;
                    if (disposed || localClient == null || !Objects.equals(localClient, newSipClient)) {
                        return;
                    }
                    try {
                        localClient.sendRegister();
                    } catch (RuntimeException e) {
                        logger.warn("Failed to re-register SIP client: {}", e.getMessage());
                    }
                }, 50, 50, TimeUnit.SECONDS);

                logger.info("SIP client started for extension {} at {}:{}", cfg.sipExtension, localIp,
                        cfg.localSipPort);
            } catch (IOException e) {
                logger.warn("Failed to start SIP client: {}", e.getMessage(), e);
                updateState(CHANNEL_SIP_REGISTERED, OnOffType.OFF);
            } catch (PeerUnavailableException | TransportNotSupportedException | InvalidArgumentException
                    | ObjectInUseException | TooManyListenersException e) {
                logger.warn("Failed to start SIP client: {}", e.getMessage(), e);
                updateState(CHANNEL_SIP_REGISTERED, OnOffType.OFF);
            } catch (RuntimeException e) {
                logger.warn("Failed to start SIP client: {}", e.getMessage(), e);
                updateState(CHANNEL_SIP_REGISTERED, OnOffType.OFF);
            }
        });
    }

    /**
     * Stops the SIP client and cancels the re-REGISTER job.
     */
    private void stopSip() {
        Future<?> localStartupJob = sipStartupJob;
        sipStartupJob = null;
        if (localStartupJob != null) {
            localStartupJob.cancel(true);
        }

        ScheduledFuture<?> localJob = sipReRegisterJob;
        sipReRegisterJob = null;
        if (localJob != null) {
            localJob.cancel(true);
        }

        SipClient localClient = sipClient;
        sipClient = null;
        if (localClient != null) {
            localClient.dispose();
            logger.debug("SIP client stopped");
        }
        stopSipBackchannelRelay();
        sessionToClientId.clear();
        backchannelSessionsByHttpSession.clear();
        sipClients.clear();
        updateState(CHANNEL_SIP_REGISTERED, UnDefType.UNDEF);
        updateState(CHANNEL_SIP_CALL_STATE, UnDefType.UNDEF);
    }

    /**
     * Detects the local IP address that can reach the VTO.
     * Uses UDP socket connection (no actual data sent) to determine correct outbound interface.
     *
     * @param vtoHostname VTO hostname or IP address
     * @return Local IP address as string
     * @throws IOException if detection fails
     */
    private String detectLocalIp(String vtoHostname) throws IOException {
        // Create a UDP socket and "connect" to VTO (doesn't actually send data)
        // This makes the OS select the correct network interface
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(vtoHostname), 5060);
            String localIp = socket.getLocalAddress().getHostAddress();
            logger.debug("Detected outbound IP for VTO {}: {}", vtoHostname, localIp);
            return localIp;
        }
    }

    private static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }

    // ============================================================================
    // SipEventListener Implementation
    // ============================================================================

    @Override
    public void onRegistrationSuccess() {
        logger.debug("SIP registration successful");
        updateState(CHANNEL_SIP_REGISTERED, OnOffType.ON);
    }

    @Override
    public void onRegistrationFailed(String reason) {
        logger.warn("SIP registration failed: {}", reason);
        updateState(CHANNEL_SIP_REGISTERED, OnOffType.OFF);
    }

    @Override
    public void onInviteReceived(String callerId) {
        logger.info("SIP INVITE received from {}", callerId);
        if (!shouldProcessSipInvite("SIP")) {
            return;
        }
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(SipClient.SipCallState.RINGING.name()));
    }

    @Override
    public void onCallCancelled() {
        logger.debug("SIP CANCEL received - transitioning call state to IDLE");
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(SipClient.SipCallState.IDLE.name()));
    }

    @Override
    public void onCallActive() {
        logger.debug("SIP call is now ACTIVE (ACK received)");
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(SipClient.SipCallState.ACTIVE.name()));
    }

    @Override
    public void onCallTerminating() {
        logger.debug("SIP call is TERMINATING");
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(SipClient.SipCallState.TERMINATING.name()));
    }

    @Override
    public void onCallEnded() {
        logger.debug("SIP call ended, setting state to IDLE");
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(SipClient.SipCallState.IDLE.name()));
    }

    public synchronized String assignClientForSession(String sessionId) {
        cleanupExpiredSessions();

        Set<String> availableClientIds = !sipClients.isEmpty() ? new TreeSet<>(sipClients.keySet())
                : Set.of(AVAILABLE_CLIENT_IDS[0]);

        @Nullable
        String existingClientId = sessionToClientId.get(sessionId);
        if (existingClientId != null && availableClientIds.contains(existingClientId)) {
            updateBackchannelSession(sessionId, existingClientId, getSipClientForClientId(existingClientId));
            return existingClientId;
        }

        Set<String> usedClientIds = new HashSet<>(sessionToClientId.values());
        for (String clientId : availableClientIds) {
            if (!usedClientIds.contains(clientId)) {
                sessionToClientId.put(sessionId, clientId);
                updateBackchannelSession(sessionId, clientId, getSipClientForClientId(clientId));
                return clientId;
            }
        }

        String fallbackClientId = availableClientIds.iterator().next();
        sessionToClientId.put(sessionId, fallbackClientId);
        updateBackchannelSession(sessionId, fallbackClientId, getSipClientForClientId(fallbackClientId));
        return fallbackClientId;
    }

    private @Nullable SipClient getSipClientForClientId(String clientId) {
        SipClient client = sipClients.get(clientId);
        if (client != null) {
            return client;
        }
        return sipClients.isEmpty() ? sipClient : null;
    }

    public synchronized String getSipCallStateForSession(String sessionId) {
        String clientId = assignClientForSession(sessionId);
        @Nullable
        SipClient client = getSipClientForClientId(clientId);
        String state = client != null ? client.getCallState() : SipClient.SipCallState.IDLE.name();
        updateBackchannelSession(sessionId, clientId, client);
        logger.debug("SIP state request: sessionId={}, clientId={}, state={}", sessionId, clientId, state);
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(state));
        return state;
    }

    public synchronized @Nullable String getSipCallerForSession(String sessionId) {
        String clientId = assignClientForSession(sessionId);
        @Nullable
        SipClient client = getSipClientForClientId(clientId);
        updateBackchannelSession(sessionId, clientId, client);
        logger.debug("SIP caller request: sessionId={}, clientId={}, hasClient={}", sessionId, clientId,
                client != null);
        return client != null ? client.getCurrentCallerId() : null;
    }

    public synchronized boolean answerSipCallForSession(String sessionId) {
        String clientId = assignClientForSession(sessionId);
        @Nullable
        SipClient client = getSipClientForClientId(clientId);
        if (client == null) {
            logger.debug("SIP answer request: sessionId={}, clientId={} has no assigned client", sessionId, clientId);
            return false;
        }
        boolean success = client.sendOkResponse();
        updateBackchannelSession(sessionId, clientId, client);
        logger.debug("SIP answer request: sessionId={}, clientId={}, success={}, state={}", sessionId, clientId,
                success, client.getCallState());
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(client.getCallState()));
        return success;
    }

    public synchronized boolean hangupSipCallForSession(String sessionId) {
        String clientId = assignClientForSession(sessionId);
        @Nullable
        SipClient client = getSipClientForClientId(clientId);
        if (client == null) {
            logger.debug("SIP hangup request: sessionId={}, clientId={} has no assigned client", sessionId, clientId);
            return false;
        }
        boolean success = client.sendBye("manual-ui");
        updateBackchannelSession(sessionId, clientId, client);
        logger.debug("SIP hangup request: sessionId={}, clientId={}, success={}, state={}", sessionId, clientId,
                success, client.getCallState());
        updateState(CHANNEL_SIP_CALL_STATE, new StringType(client.getCallState()));
        return success;
    }

    public synchronized @Nullable SipBackchannelSession getSipBackchannelSessionForSession(String sessionId) {
        cleanupExpiredSessions();
        return backchannelSessionsByHttpSession.get(sessionId);
    }

    private synchronized void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();

        backchannelSessionsByHttpSession.entrySet()
                .removeIf(entry -> now - entry.getValue().getUpdatedAtMs() > SESSION_TTL_MS);

        sessionToClientId.keySet().removeIf(sessionId -> !backchannelSessionsByHttpSession.containsKey(sessionId));

        while (backchannelSessionsByHttpSession.size() > MAX_SESSION_MAPPINGS) {
            @Nullable
            String oldestSessionId = null;
            long oldestUpdatedAt = Long.MAX_VALUE;

            for (Map.Entry<String, SipBackchannelSession> entry : backchannelSessionsByHttpSession.entrySet()) {
                long updatedAt = entry.getValue().getUpdatedAtMs();
                if (updatedAt < oldestUpdatedAt) {
                    oldestUpdatedAt = updatedAt;
                    oldestSessionId = entry.getKey();
                }
            }

            if (oldestSessionId == null) {
                break;
            }

            backchannelSessionsByHttpSession.remove(oldestSessionId);
            sessionToClientId.remove(oldestSessionId);
        }
    }

    private void updateBackchannelSession(String sessionId, String clientId, @Nullable SipClient client) {
        SipBackchannelSession current = backchannelSessionsByHttpSession.get(sessionId);
        long now = System.currentTimeMillis();

        String callerId = current != null ? current.getCallerId() : "";
        String callState = current != null ? current.getCallState() : SipClient.SipCallState.IDLE.name();
        @Nullable
        String inviteSdp = current != null ? current.getInviteSdp() : null;
        long createdAtMs = current != null ? current.getCreatedAtMs() : now;

        if (client != null) {
            callState = client.getCallState();

            @Nullable
            String currentCallerId = client.getCurrentCallerId();
            if (currentCallerId != null) {
                callerId = currentCallerId;
            }

            inviteSdp = client.getCurrentInviteSdp();
        }

        backchannelSessionsByHttpSession.put(sessionId, new SipBackchannelSession(sessionId, clientId,
                getThing().getUID().toString(), callerId, callState, inviteSdp, createdAtMs, now));

        cleanupExpiredSessions();
    }
}
