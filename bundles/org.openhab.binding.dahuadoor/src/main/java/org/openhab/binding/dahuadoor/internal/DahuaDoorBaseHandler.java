/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.dahuadoor.internal.dahuaeventhandler.DHIPEventListener;
import org.openhab.binding.dahuadoor.internal.dahuaeventhandler.DahuaEventClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link DahuaDoorBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaDoorBaseHandler extends BaseThingHandler implements DHIPEventListener {

    private final Logger logger = LoggerFactory.getLogger(DahuaDoorBaseHandler.class);
    private @Nullable DahuaDoorConfiguration config;
    private @Nullable ScheduledFuture<?> connectorTask; // is used for reconnection if something goes wrong

    private Gson gson = new Gson();

    private @Nullable DahuaEventClient client = null;
    private @Nullable DahuaDoorHttpQueries queries = null;
    private @Nullable HttpClient httpClient = null;
    private @Nullable HttpClientFactory httpClientFactory = null;

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
        if (CHANNEL_BELL_BUTTON.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }
        }
        switch (channelUID.getId()) {
            case CHANNEL_OPENDOOR1:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        queries.OpenDoor(1);
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
                break;
            case CHANNEL_OPENDOOR2:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        queries.OpenDoor(2);
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
                break;
        }
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

    }

    public void errorInformer(String msgError) {

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msgError);
    }

    @Override
    public void initialize() {
        config = getConfigAs(DahuaDoorConfiguration.class);

        client = new DahuaEventClient(config.hostname, config.username, config.password, this, scheduler,
                this::errorInformer);
        queries = new DahuaDoorHttpQueries(httpClient, config);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.ONLINE);

        // Example for background initialization:
        /*
         * scheduler.execute(() -> {
         * boolean thingReachable = true; // <background task with long running initialization here>
         * // when done do:
         * if (thingReachable) {
         * updateStatus(ThingStatus.ONLINE);
         * } else {
         * updateStatus(ThingStatus.OFFLINE);
         * }
         * });
         *
         * connectorTask = scheduler.scheduleWithFixedDelay(
         * new DahuaEventClient(config.hostname, config.username, config.password, this, scheduler), 0, 60,
         * TimeUnit.SECONDS);
         */
        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        if (client != null) {
            client.dispose();
            client = null;
        }
    }

    public void saveSnapshot(byte @Nullable [] buffer) {

        config = getConfigAs(DahuaDoorConfiguration.class);
        if (config.snapshotpath == null || config.snapshotpath.isEmpty()) {
            logger.warn("Path for Snapshots is invald");
            errorInformer("Path for Snapshots is invald");
            return;
        }
        if (buffer == null) {
            logger.warn("cannot safe empty buffer");
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = config.snapshotpath + "/DoorBell_" + timestamp + ".jpg";

        try (FileOutputStream fos = new FileOutputStream(new File(filename))) {
            fos.write(buffer);
        } catch (Exception e) {
            logger.warn("Could not write image to file, check permissions and path");
        }

        try {
            Files.copy(Paths.get(filename), Paths.get(config.snapshotpath + "/Doorbell.jpg"));
        } catch (Exception e) {
            logger.warn("Could not copy file, check permissions and path");
        }
    }

    private void updateChannelImage(byte @Nullable [] buffer) {
        Channel channel;
        if (buffer.length > 0) {
            channel = this.getThing().getChannel(CHANNEL_DOOR_IMAGE);
            RawType image = new RawType(buffer, "image/jpg");
            updateState(CHANNEL_DOOR_IMAGE, (image != null) ? image : UnDefType.UNDEF);
        }
    }

    public void HandleButtonPressed() {

        Channel channel = this.getThing().getChannel(CHANNEL_BELL_BUTTON);
        triggerChannel(channel.getUID(), "PRESSED");
        byte[] buffer = queries.RequestImage();
        updateChannelImage(buffer);
        saveSnapshot(buffer);
    }

    @Override
    public void EventHandler(@Nullable JsonObject data) {
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
                        handleHungPhone(eventList, eventData);
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
                    case "PassiveHungup":
                        handlePassiveHungup(eventList, eventData);
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
                        handleDSGErrorReport(eventList, eventData);
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
        }
        ;
    }

    private void handleNetworkChanged(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: NetworkChange, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleDoorNotClosed(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DoorNotClosed, Action " + eventList.get("Action").getAsString() + ", Name"
                + eventData.get("Name").getAsString() + ", LocaleTime " + eventData.get("LocaleTime").getAsString());
    }

    private void handleDoorControl(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DoorControl, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleDoorStatus(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DoorStatus, Action " + eventList.get("Action").getAsString() + ", Status: "
                + eventData.get("Status").getAsString() + ", LocaleTime " + eventData.get("LocaleTime").getAsString());
    }

    private void handleAddCard(JsonObject eventList, JsonObject eventData) {
        JsonObject cardData = eventData.getAsJsonArray("Data").get(0).getAsJsonObject();
        logger.debug("Event: AddCard, Action " + eventList.get("Action").getAsString() + ": CardNo "
                + cardData.get("CardNo").getAsString() + ", UserID " + cardData.get("UserID").getAsString()
                + ", UserName " + cardData.get("UserName").getAsString() + ", CardStatus "
                + cardData.get("CardStatus").getAsString() + ", CardType " + cardData.get("CardType").getAsString()
                + ", Doors: Door 0=" + cardData.getAsJsonArray("Doors").get(0).getAsString() + ", Door1="
                + cardData.getAsJsonArray("Doors").get(1).getAsString());
    }

    private void handleSendCard(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: SendCard, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleUpgrade(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Upgrade, Action " + eventList.get("Action").getAsString() + ", with State"
                + eventData.get("State").getAsString() + ", LocaleTime " + eventData.get("LocaleTime").getAsString());
    }

    private void handleDSGErrorReport(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: DGSErrorReport, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleSecurityImport(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: SecurityImExport, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString() + ", Status " + eventData.get("Status").getAsString());
    }

    private void handleReboot(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Reboot, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleUpdateFile(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: UpdateFile, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleNewFile(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: NewFile, Action " + eventList.get("Action").getAsString() + ", File "
                + eventData.get("File").getAsString() + ", Folder " + eventData.get("Filter").getAsString()
                + ", LocaleTime " + eventData.get("LocaleTime").getAsString() + " Index "
                + eventData.get("Index").getAsString());
    }

    private void handleProfileAlarmTransit(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: ProfileAlarmTransmit, Action " + eventList.get("Action").getAsString() + ", AlarmType "
                + eventData.get("AlarmType").getAsString() + " DevSrcType " + eventData.get("DevSrcType").getAsString()
                + ", SenseMethod " + eventData.get("SenseMethod").getAsString());
    }

    private void handlePassiveHungup(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: PassiveHungup, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString() + " Index " + eventData.get("Index").getAsString());
    }

    private void handleRequestCallState(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: RequestCallState, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString() + " Index " + eventData.get("Index").getAsString());
    }

    private void handleAccessSnap(JsonObject eventData) {
        logger.debug("Event: AccessSnap, FTP upload to " + eventData.get("FtpUrl").getAsString());
    }

    private void handleAlarmLocal(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: AlarmLocal, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleInvite(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Invite, Action " + eventList.get("Action").getAsString() + ", CallID "
                + eventData.get("CallID").getAsString() + " Lock Number " + eventData.get("LockNum").getAsString());
    }

    private void handleHangup(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: Hungup, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleHungPhone(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event: HungupPhone, Action " + eventList.get("Action").getAsString() + ", LocaleTime "
                + eventData.get("LocaleTime").getAsString());
    }

    private void handleCallSnap(JsonObject eventData) {
        logger.debug("Event: CallSnap, DeviceType " + eventData.get("DeviceType").getAsString() + " RemoteID "
                + eventData.get("RemoteID").getAsString() + ", RemoteIP " + eventData.get("RemoteIP").getAsString()
                + " CallStatus " + eventData.getAsJsonArray("ChannelStates").get(0).getAsString());
    }

    private void handleAccessControl(JsonObject eventData) {
        logger.debug("Event: AccessControl, Name " + eventData.get("Name").getAsString() + " Method "
                + eventData.get("Method").getAsString() + ", ReaderID " + eventData.get("ReaderID").getAsString()
                + ", UserID " + eventData.get("UserID").getAsString());
    }

    private void handleSIPRegisterResult(JsonObject eventList, JsonObject eventData) {
        if ("Pulse".equals(eventList.get("Action").getAsString())) {
            if (eventData.get("Success").getAsBoolean()) {
                logger.debug("Event SIPRegisterResult, Success");
            } else {
                logger.debug("Event SIPRegisterResult, Failed");
            }
        }
    }

    private void handleDoorCard(JsonObject eventList, JsonObject eventData) {
        if ("Pulse".equals(eventList.get("Action").getAsString())) {
            logger.debug("DoorCard " + eventData.get("Number").getAsString() + " was used at door");
        }
    }

    private void handleFingerPrintCheck(JsonObject eventData) {
        if (eventData.get("FingerPrintID").getAsInt() > -1) {
            int finger = eventData.get("FingerPrintID").getAsInt();
            Map<Integer, String> users = Map.of(0, "Papa", 1, "Mama", 2, "Kind1", 3, "Kind2");
            String name = users.get(finger);
            logger.debug("Event FingerPrintCheck success, Finger number " + eventData.get("FingerPrintID").getAsInt()
                    + ", User " + name);
        } else {
            logger.debug("Event FingerPrintCheck failed, unknown Finger");
        }
    }

    private void handleVideoBlind(JsonObject eventList) {
        if ("Start".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event VideoBlind started");
        } else if ("Stop".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event VideoBlind stopped");
        }
    }

    private void handleKeepLightOn(JsonObject eventData) {
        if ("On".equals(eventData.get("Status").getAsString())) {
            logger.debug("Event KeepLightOn");
        } else if ("Off".equals(eventData.get("Status").getAsString())) {
            logger.debug("Event KeepLightOff");
        }
    }

    private void handleNTPAdjust(JsonObject eventData) {
        if (eventData.get("result").getAsBoolean()) {
            logger.debug("Event NTPAdjustTime with " + eventData.get("Address").getAsString() + " success");
        } else {
            logger.debug("Event NTPAdjustTime failed");
        }
    }

    private void handleTimeChange(JsonObject eventData) {
        logger.debug("Event TimeChange, BeforeModifyTime: " + eventData.get("BeforeModifyTime").getAsString()
                + ", ModifiedTime: " + eventData.get("ModifiedTime").getAsString());
    }

    private void handleBackKeyLight(JsonObject eventData) {
        logger.debug("Event BackKeyLight with State " + eventData.get("State").getAsString() + " ");
    }

    private void handleRTSPDisconnect(JsonObject eventList, JsonObject eventData) {
        if ("Start".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event Rtsp-Session from " + eventData.get("Device").getAsString().replace("::ffff:", "")
                    + " disconnected");
        } else if ("Stop".equals(eventList.get("Action").getAsString())) {
            logger.debug("Event Rtsp-Session from " + eventData.get("Device").getAsString().replace("::ffff:", "")
                    + " connected");
        }
    }

    private void handleMotionEvent() {
        logger.debug("Event VideoMotion");
        // saveSnapshot();
    }

    private void handleVTHAnswer() {
        logger.debug("Event VTH answered call from VTO");
    }

    private void handleVTOCall() {
        logger.debug("Event Call from VTO");
        HandleButtonPressed();
    }

}
