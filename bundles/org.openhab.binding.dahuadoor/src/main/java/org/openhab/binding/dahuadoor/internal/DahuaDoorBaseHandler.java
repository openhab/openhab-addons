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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.dahuadoor.internal.dahuaeventhandler.DHIPEventListener;
import org.openhab.binding.dahuadoor.internal.dahuaeventhandler.DahuaEventClient;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
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
 * are
 * sent to one of the channels.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public abstract class DahuaDoorBaseHandler extends BaseThingHandler implements DHIPEventListener {

    protected final Logger logger = LoggerFactory.getLogger(DahuaDoorBaseHandler.class);
    protected @Nullable DahuaDoorConfiguration config;

    protected Gson gson = new Gson();

    protected @Nullable DahuaEventClient client = null;
    protected @Nullable DahuaDoorHttpQueries queries = null;
    private @Nullable HttpClient httpClient = null;

    public DahuaDoorBaseHandler(Thing thing) {
        super(thing);
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public @Nullable HttpClient getHttpClient() {
        return this.httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        DahuaDoorHttpQueries localQueries = queries;
        if (localQueries == null) {
            logger.warn("HTTP queries not initialized, cannot handle command");
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_OPEN_DOOR_1:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        localQueries.openDoor(1);
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
                break;
            case CHANNEL_OPEN_DOOR_2:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        localQueries.openDoor(2);
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
        DahuaDoorConfiguration localConfig = getConfigAs(DahuaDoorConfiguration.class);
        config = localConfig;

        // Validate required configuration
        if (localConfig.hostname.isBlank() || localConfig.username.isBlank() || localConfig.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Hostname, username and password must be configured.");
            return;
        }

        if (localConfig.snapshotpath.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Snapshot path must be configured.");
            return;
        }

        client = new DahuaEventClient(localConfig.hostname, localConfig.username, localConfig.password, this, scheduler,
                this::errorInformer);
        queries = new DahuaDoorHttpQueries(httpClient, localConfig);

        // Mark thing as online; errorInformer will switch to OFFLINE on failures
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Connected to device");
    }

    @Override
    public void dispose() {
        DahuaEventClient localClient = client;
        if (localClient != null) {
            localClient.dispose();
            client = null;
        }
        DahuaDoorHttpQueries localQueries = queries;
        if (localQueries != null) {
            localQueries.dispose();
            queries = null;
        }
    }

    public void saveSnapshot(byte @Nullable [] buffer) {
        final DahuaDoorConfiguration localConfig = config;
        if (localConfig == null) {
            logger.warn("Configuration not initialized");
            return;
        }
        if (localConfig.snapshotpath.isEmpty()) {
            logger.warn("Path for Snapshots is invalid");
            return;
        }
        if (buffer == null) {
            logger.warn("cannot save empty buffer");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = localConfig.snapshotpath + "/DoorBell_" + timestamp + ".jpg";

        try (FileOutputStream fos = new FileOutputStream(new File(filename))) {
            fos.write(buffer);
        } catch (Exception e) {
            logger.warn("Could not write image to file '{}', check permissions and path", filename, e);
        }

        String latestSnapshotFilename = localConfig.snapshotpath + "/Doorbell.jpg";
        try {
            Files.copy(Paths.get(filename), Paths.get(latestSnapshotFilename), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.warn("Could not copy file from '{}' to '{}', check permissions and path", filename,
                    latestSnapshotFilename, e);
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

        DahuaDoorHttpQueries localQueries = queries;
        if (localQueries == null) {
            logger.warn("HTTP queries not initialized, cannot retrieve doorbell image");
            return;
        }
        byte[] buffer = localQueries.requestImage();
        updateChannelImage(buffer);
        saveSnapshot(buffer);
    }

    @Override
    public void eventHandler(JsonObject data) {
        // Set thing ONLINE when first event is received (confirms successful
        // connection)
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        try {
            logger.trace("JSON{}", data);
            JsonObject jsonObj = data.getAsJsonObject("params");
            JsonArray firstLevel = jsonObj.getAsJsonArray("eventList");
            JsonObject eventList = firstLevel.get(0).getAsJsonObject();
            String eventCode = eventList.get("Code").getAsString();
            JsonObject eventData = eventList.getAsJsonObject("Data");
            if (firstLevel.size() > 1) {
                logger.debug("Event Manager subscription reply: {}", eventCode);
            } else {
                switch (eventCode) {
                    case "CallNoAnswered":
                        handleVTOCall();
                        break;
                    case "IgnoreInvite":
                        handleVTHAnswer();
                        break;
                    case "VideoMotion":
                        handleMotionEvent();
                        break;
                    case "RtspSessionDisconnect":
                        handleRTSPDisconnect(eventList, eventData);
                        break;
                    case "BackKeyLight":
                        handleBackKeyLight(eventData);
                        break;
                    case "TimeChange":
                        handleTimeChange(eventData);
                        break;
                    case "NTPAdjustTime":
                        handleNTPAdjust(eventData);
                        break;
                    case "KeepLightOn":
                        handleKeepLightOn(eventData);
                        break;
                    case "VideoBlind":
                        handleVideoBlind(eventList);
                        break;
                    case "FingerPrintCheck":
                        handleFingerPrintCheck(eventData);
                        break;
                    case "DoorCard":
                        handleDoorCard(eventList, eventData);
                        break;
                    case "SIPRegisterResult":
                        handleSIPRegisterResult(eventList, eventData);
                        break;
                    case "AccessControl":
                        handleAccessControl(eventData);
                        break;
                    case "CallSnap":
                        handleCallSnap(eventData);
                        break;
                    case "HungupPhone":
                        handleHungupPhone(eventList, eventData);
                        break;
                    case "HangupPhone":
                        handleHangupPhone(eventList, eventData);
                        break;
                    case "Hangup":
                        handleHangup(eventList, eventData);
                        break;
                    case "Invite":
                        handleInvite(eventList, eventData);
                        break;
                    case "AlarmLocal":
                        handleAlarmLocal(eventList, eventData);
                        break;
                    case "AccessSnap":
                        handleAccessSnap(eventData);
                        break;
                    case "RequestCallState":
                        handleRequestCallState(eventList, eventData);
                        break;
                    case "PassiveHangup":
                        handlePassiveHangup(eventList, eventData);
                        break;
                    case "ProfileAlarmTransmit":
                        handleProfileAlarmTransit(eventList, eventData);
                        break;
                    case "NewFile":
                        handleNewFile(eventList, eventData);
                        break;
                    case "UpdateFile":
                        handleUpdateFile(eventList, eventData);
                        break;
                    case "Reboot":
                        handleReboot(eventList, eventData);
                        break;
                    case "SecurityImExport":
                        handleSecurityImport(eventList, eventData);
                        break;
                    case "DGSErrorReport":
                        handleDGSErrorReport(eventList, eventData);
                        break;
                    case "Upgrade":
                        handleUpgrade(eventList, eventData);
                        break;
                    case "SendCard":
                        handleSendCard(eventList, eventData);
                        break;
                    case "AddCard":
                        handleAddCard(eventList, eventData);
                        break;
                    case "DoorStatus":
                        handleDoorStatus(eventList, eventData);
                        break;
                    case "DoorControl":
                        handleDoorControl(eventList, eventData);
                        break;
                    case "DoorNotClosed":
                        handleDoorNotClosed(eventList, eventData);
                        break;
                    case "NetworkChange":
                        handleNetworkChanged(eventList, eventData);
                        break;
                    default:
                        logger.debug("Unknown event received. JSON{}", gson.toJson(data));
                }
            }
        } catch (Exception e) {
            logger.debug("Exception while handling DahuaDoor event. Raw payload: {}", data.toString(), e);
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
            if (eventData.get("Success").getAsBoolean()) {
                logger.debug("Event SIPRegisterResult, Success");
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
        logger.debug("Event Call from VTO - subclass should override this method");
    }

    /**
     * Abstract method to handle button press events with lock number identification.
     * Subclasses must implement this to handle single or multi-button devices.
     *
     * @param lockNumber The lock number from the Invite event (1 or 2)
     */
    protected abstract void onButtonPressed(int lockNumber);
}
