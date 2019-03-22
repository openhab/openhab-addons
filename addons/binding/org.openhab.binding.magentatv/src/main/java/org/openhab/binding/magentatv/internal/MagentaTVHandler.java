/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.magentatv.internal.network.MagentaTVNetwork;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link MagentaTVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Michels (markus7017) - Initial contribution
 */
public class MagentaTVHandler extends BaseThingHandler implements MagentaTVListener {
    private final MagentaTVLogger logger = new MagentaTVLogger(MagentaTVHandler.class, "Handler");
    private final MagentaTVConfiguration thingConfig = new MagentaTVConfiguration();
    private MagentaTVNetwork network;
    private MagentaTVControl control;
    private MagentaTVHandlerFactory handlerFactory;

    private volatile int idRefresh = 0;
    private ScheduledFuture<?> pairingWatchdogJob;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Constructor, save bindingConfig (services as default for thingConfig)
     *
     * @param thing
     * @param bindingConfig
     */
    public MagentaTVHandler(MagentaTVHandlerFactory handlerFactory, Thing thing, MagentaTVNetwork network) {
        super(thing);
        this.handlerFactory = handlerFactory;
        this.network = network;

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                renewEventSubscription();
            }
        }, 2 * 60, 25 * 60, TimeUnit.SECONDS);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setMagentaTVHandlerFactory(MagentaTVHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
        logger.trace("HandlerFactory bound to MagentaTVHandler");
    }

    public void unsetMagentaVHandlerFactory(MagentaTVHandlerFactory handlerFactory) {
        this.handlerFactory = null;
    }

    /**
     * Thing initialization
     */
    @SuppressWarnings("null")
    @Override
    public void initialize() {

        // The framework requires you to return from this method quickly. For that the initialization itself is executed
        // asynchronously
        logger.debug("Initialize Thing...");
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            String errorMessage = "";
            try {
                // Example for background initialization:
                // All relevant parameters will be derived from the thing config
                // the final result will be saved to the thing properties and can be viewed in
                // PaperUI
                thingConfig.initializeConfig(getConfig().getProperties());
                if (thingConfig.getUDN().isEmpty()) {
                    // get UDN from device name
                    String uid = this.getThing().getUID().getAsString();
                    String udn = StringUtils.substringAfterLast(uid, ":");
                    thingConfig.setUDN(udn);
                }
                if (thingConfig.getMacAddress().isEmpty()) {
                    // get MAC address from UDN (last 12 digits)
                    String macAddress = StringUtils.substringAfterLast(thingConfig.getUDN(), "_");
                    if (macAddress.isEmpty()) {
                        macAddress = StringUtils.substringAfterLast(thingConfig.getUDN(), "-");
                    }
                    thingConfig.setMacAddress(macAddress);
                }

                control = new MagentaTVControl(this.thingConfig, network);
                Map<String, Object> discoveredProperties = handlerFactory.getDiscoveredProperties(thingConfig.getUDN());
                if (discoveredProperties != null) {
                    thingConfig.updateConfig(discoveredProperties); // get network parameters from control
                }

                // If userID is empty and credentials are given the Telekom OAuth service is
                // used to query the userID
                if (thingConfig.getUserID().isEmpty() && !thingConfig.getAccountName().isEmpty()
                        && !thingConfig.getAccountName().equals("***") && !thingConfig.getAccountPassword().isEmpty()
                        && !thingConfig.getAccountPassword().equals("***")) {
                    // run OAuth authentication, this finally provides the userID
                    // if successful replace credentials in the thing config with the userID
                    logger.debug("AuthenticateUser");
                    String name = thingConfig.getAccountName();
                    String password = thingConfig.getAccountPassword();

                    String userID = control.authenticateUser(name, password);
                    if (!userID.isEmpty()) {
                        thingConfig.setUserID(userID);

                        // Update thing configuration (persistent) - remove credentials, add userID
                        Configuration configuration = this.getConfig();
                        configuration.remove(PROPERTY_ACCT_NAME);
                        configuration.remove(PROPERTY_ACCT_PWD);
                        configuration.remove(PROPERTY_USERID);
                        configuration.put(PROPERTY_ACCT_NAME, "***");
                        configuration.put(PROPERTY_ACCT_PWD, "***");
                        configuration.put(PROPERTY_USERID, userID);
                        thingConfig.setAccountName("");
                        thingConfig.setAccountPassword("");
                        this.updateConfiguration(configuration);
                    }
                }

                thingConfig.updateConfig(control.getConfig().getProperties()); // get network parameters from control

                if (thingConfig.getUserID().isEmpty()) {
                    errorMessage = "No userID nor account data given -> unable to pair";
                } else {
                    // wait for NotifyServlet to initialze
                    if (!handlerFactory.getNotifyServletStatus()) {
                        logger.debug("Waiting on NotifyServlet to start...");
                        int iRetries = 30;
                        while ((iRetries-- > 0) && !handlerFactory.getNotifyServletStatus()) {
                            logger.trace("Waiting for init, {} sec remaining", iRetries);
                            Thread.sleep(1000);
                        }
                        if ((iRetries <= 0) && !handlerFactory.getNotifyServletStatus()) {
                            errorMessage = "Can't initialize, NotifyServlet not started!";
                        }
                    } else {
                        connectReceiver(); // throws exception on error
                        // change to ThingStatus.ONLINE will be done when the pairing result is received
                        // (see onPairingResult())
                        // this.updateStatus(ThingStatus.ONLINE);
                    }
                }
            } catch (ConnectException e) {
                errorMessage = "Connection to the receiver failed:, check if stb is powered on: " + e.getMessage();
            } catch (IOException e) {
                errorMessage = "Network I/O failed: " + e.getMessage();
            } catch (Exception e) {
                errorMessage = "Unable to initialize thing: " + e.getMessage();
            } finally {
                if (!errorMessage.isEmpty()) {
                    logger.fatal("Initialization failed for device '{}' ({}): {}", thingConfig.getFriendlyName(),
                            thingConfig.getTerminalID(), errorMessage);
                    setOnlineState(ThingStatus.OFFLINE, errorMessage);
                }
            }
        });
    }

    /**
     * Copy thingConfig properties to Thing properties, omit values starting with $
     *
     * @param property map to copy
     */
    public void updateThingProperties(Map<String, Object> properties) {
        // update thing properties
        Map<String, String> map = new HashMap<String, String>();
        for (String key : properties.keySet()) {
            // skip all entries beginning with '$' and component.* (osgi)
            if ((key.charAt(0) != '$') && !key.contains("component.") && properties.get(key) != null) {
                map.put(key, properties.get(key).toString());
            }
        }
        this.updateProperties(map);
    }

    /**
     * Handle channel commands
     *
     * @param channelUID - the channel, which received the command
     * @param command    - the actual command (could be instance of StringType,
     *                       DecimalType or OnOffType)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command == RefreshType.REFRESH) {
            // currently no channels to be refreshed
            return;
        }

        try {
            if (!isOnline()) {
                // reconnect to MR
                logger.info("Device {} ({}) is offline, try to (re-)connect", thingConfig.getFriendlyName(),
                        thingConfig.getTerminalID());
                connectReceiver(); // throws an exception if this fails
            }

            logger.debug("Channel command for device {}: {} for channel {}", thingConfig.getFriendlyName(),
                    command.toString(), channelUID.getId().toString());

            switch (channelUID.getId()) {
                case CHANNEL_POWER: // toggle power
                    if (command instanceof OnOffType) {
                        logger.info("Toggle power state (send POWER)");
                        control.sendKey("POWER");
                    }

                    break;
                case CHANNEL_CHANNEL:
                    if (command instanceof StringType) {
                        selectChannel(command.toString());
                    } else if (command instanceof DecimalType) {

                        Integer newChan = ((DecimalType) command).intValue();
                        selectChannel(newChan.toString());
                    }
                    break;
                case CHANNEL_CHUP:
                    control.sendKey("CHUP");
                    updateState(CHANNEL_CHUP, OnOffType.OFF);
                    break;
                case CHANNEL_CHDOWN:
                    control.sendKey("CHDOWN");
                    updateState(CHANNEL_CHDOWN, OnOffType.OFF);
                    break;
                case CHANNEL_VOLUP:
                    control.sendKey("VOLUP");
                    updateState(CHANNEL_VOLUP, OnOffType.OFF);
                    break;
                case CHANNEL_VOLDOWN:
                    control.sendKey("VOLDOWN");
                    updateState(CHANNEL_VOLDOWN, OnOffType.OFF);
                    break;
                case CHANNEL_KEY:
                    String key = command.toString();
                    if (key.equalsIgnoreCase("PAIR")) {
                        // special key to re-pair receiver
                        logger.info("PAIRing key received, reconnect media receiver '{}' ({})",
                                thingConfig.getFriendlyName(), thingConfig.getTerminalID());
                        connectReceiver(); // throws exception on error
                    } else {
                        control.sendKey(key);
                    }
                    break;
                default:
                    logger.fatal("Command for unknown channel '{}'", channelUID.getAsString());
            }
        } catch (Exception e) {
            String errorMessage = MessageFormat.format(
                    "Channel operation failed: Command={0}, value={1}, error={2} ({3}", command.toString(),
                    channelUID.getId().toString());
            logger.exception(errorMessage, e);
            setOnlineState(ThingStatus.OFFLINE, errorMessage);
        }
    }

    /**
     * Connect to the receiver
     *
     * @throws Exception something failed
     */
    private void connectReceiver() throws Exception {
        if ((control != null) && control.checkDev()) {
            thingConfig.updateConfig(control.getConfig().getProperties()); // get description data
            updateThingProperties(thingConfig.getProperties());
            handlerFactory.registerDevice(thingConfig.getUDN(), thingConfig.getTerminalID(), thingConfig.getIpAddress(),
                    this);
            control.subscribePairingChannel();
            logger.debug("Thing successfully initialized, pairing...");
            control.sendPairingRequest();
            updateThingProperties(thingConfig.getProperties());

            // check for pairing for timeout
            final int iRefresh = ++idRefresh;
            pairingWatchdogJob = scheduler.schedule(() -> {
                if (iRefresh == idRefresh) { // Make a best effort to not run multiple deferred refresh
                    if (thingConfig.getVerificationCode().isEmpty()) {
                        setOnlineState(ThingStatus.OFFLINE, "Timeout on pairing request!");
                    }
                }
            }, 15, TimeUnit.SECONDS);
        }
    }

    /**
     * Update thing status
     *
     * @param mode new thing status
     * @return ON = power on, OFF=power off
     */
    public void setOnlineState(ThingStatus newStatus, String errorMessage) {
        ThingStatus status = this.getThing().getStatus();
        if (status != newStatus) {
            if (newStatus == ThingStatus.INITIALIZING) {
                logger.fatal("Invalid new thing state: ", newStatus.toString());
            }
            if (newStatus == ThingStatus.ONLINE) {
                updateStatus(newStatus);
                updateState(CHANNEL_POWER, OnOffType.ON);
            } else {
                if (!errorMessage.isEmpty()) {
                    logger.fatal("Communication Error - {}, switch Thing offline", errorMessage);
                    updateStatus(newStatus, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                } else {
                    updateStatus(newStatus);
                }
                updateState(CHANNEL_POWER, OnOffType.OFF);
            }
        }
    }

    private boolean isOnline() {
        return this.getThing().getStatus() == ThingStatus.ONLINE;
    }

    /**
     * Select channel for TV
     *
     * @param channel new channel
     * @return true:ok, false: failed
     */
    public boolean selectChannel(String channel) throws Exception {
        logger.info("Select channel {}", channel);
        for (int i = 0; i < channel.length(); i++) {
            if (!control.sendKey("" + channel.charAt(i))) {
                break;
            }
        }
        return true;
    }

    //
    // ------ Internal stuff
    //

    /**
     * Update device status (poll Rachio Cloud) in addition webhooks are used to get
     * events (if callbackUrl is configured)
     */
    public void renewEventSubscription() {
        if (control == null) {
            return;
        }

        String step = "";
        try {
            logger.debug("Check receiver status, current state  {}/{}",
                    this.getThing().getStatusInfo().getStatus().toString(),
                    this.getThing().getStatusInfo().getStatusDetail());

            step = "check_online";
            if ((this.getThing().getStatus() != ThingStatus.OFFLINE) && !thingConfig.getVerificationCode().isEmpty()) {
                // when pairing is completed re-new event channel subscription
                logger.debug("Renew MR event subscription for device '{}'", thingConfig.getFriendlyName());
                step = "subscribePairing";

                /*
                 * DISABLED for verification
                 *
                 * control.subscribePairingChannel();
                 */
            }
        } catch (Exception e) {
            String errorMessage = MessageFormat.format("Device {0} ({1}) is offline, {2}",
                    thingConfig.getFriendlyName(), thingConfig.getTerminalID(), step);
            logger.exception(errorMessage, e);
            // setOnlineState(ThingStatus.OFFLINE, errorMessage);
        }

        try {
            if ((this.getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)
                    && !thingConfig.getUserID().isEmpty()) {
                // if we have no userID the OAuth is not completed or pairing process got stuck

                logger.info("Reconnect media receiver '{}' ({})", thingConfig.getFriendlyName(),
                        thingConfig.getTerminalID());
                step = "re-connect to MR";
                connectReceiver(); // throws exception on error
                // if connect is successful the binding will receive the pairing result and
                // switches the thing to online state
            }
        } catch (Exception e) {
            String errorMessage = MessageFormat.format("Re-Connect for device {0} ({1}) failed",
                    thingConfig.getFriendlyName(), thingConfig.getTerminalID());
            logger.exception(errorMessage, e);
            // setOnlineState(ThingStatus.OFFLINE, errorMessage);
        }
    }

    @Override
    public void onWakeup(Map<String, Object> discoveredProperties) throws Exception {
        if (control == null) {
            // this should never happen
            logger.fatal("Unable to process wakeup (control == null)");
            return;
        }
        if ((this.getThing().getStatus() == ThingStatus.OFFLINE) || thingConfig.getVerificationCode().isEmpty()) {
            // the device work up and send a UPnP discovery information, use this as a
            // trigger to reconnect
            connectReceiver();
        } else {
            logger.debug("Refesh device status for '{}' (UDN='{}'", thingConfig.getFriendlyName(),
                    thingConfig.getUDN());
            setOnlineState(ThingStatus.ONLINE, "");
        }
    }

    @Override
    public void onPairingResult(@NonNull String pairingCode) throws Exception {
        if (control != null) {
            thingConfig.updateConfig(control.getConfig().getProperties()); // get description data

            if (control.generateVerificationCode(pairingCode)) {
                thingConfig.setPairingCode(pairingCode);
                logger.info(
                        "Pairing code received for '{}' (UDN '{}', terminalID '{}', pairingCode='{}', verificationCode='{}', userID='{}')",
                        thingConfig.getFriendlyName(), thingConfig.getUDN(), thingConfig.getTerminalID(),
                        thingConfig.getPairingCode(), thingConfig.getVerificationCode(), thingConfig.getUserID());

                // verify pairing completes the pairing process
                logger.info("Verify Pairing for device '{}' ({}), verificationCode='{}')",
                        thingConfig.getFriendlyName(), thingConfig.getTerminalID(), thingConfig.getVerificationCode());
                if (control.verifyPairing()) {
                    setOnlineState(ThingStatus.ONLINE, "");
                    logger.info("Pairing completed for device '{}' ({}), Thing now ONLINE",
                            thingConfig.getFriendlyName(), thingConfig.getTerminalID());

                    logger.trace("Pairing successful, stop watchdog");
                    cancelPairingCheck(); // stop timeout check
                }
            }
        } else {
            logger.fatal("Pairing: control is null!!");
        }
    }

    /**
     * An programInfo or playstatus event was received from the receiver.
     *
     * programInfo event format (JSON):
     * {"type":"EVENT_EIT_CHANGE","instance_id":26,"channel_code":"54","channel_num"
     * :"11","mediaId":"1221", "program_info": [ {"start_time":"2018/10/14
     * 10:21:59","event_id":"9581","duration":"00:26:47",
     * "free_CA_mode":false,"running_status":4, "short_event": [
     * {"event_name":"Mysticons","language_code":"DEU", "text_char":"Die Mysticons
     * verwandeln Zarya in ein Baby, um in ein Feen-Waisenhaus zu gelangen. Dabei
     * erfahren sie, wer Prinzessin Arkaynas Zwillingsschwester ist." } ]}, {
     * "start_time":"2018/10/14 10:48:46",
     * "event_id":"12204","duration":"00:23:54","free_CA_mode":false,
     * "running_status":1, "short_event": [ {"event_name":"Winx
     * Club","language_code":"DEU", "text_char":"Daphnes Eltern veranstalten zu
     * ihrer Wiederkehr eine Willkommensparty. Diese wird jedoch gestört. Die Winx
     * haben das Biest der Tiefe unter ihrem Befehl. Die Winx müssen die
     * „Inspiration des Sirenix“ finden und die Party retten!" }]} ] }
     *
     *
     * playStatus event format (JSON) playContent event, for details see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619231.html
     *
     * sample 1:
     * {"new_play_mode":4,"duration":0,"playBackState":1,"mediaType":1,"mediaCode":"3733","playPostion":0}
     * sample 2: {"new_play_mode":4,
     * "playBackState":1,"mediaType":1,"mediaCode":"3479"}
     */
    @Override
    public void onStbEvent(@NonNull String jsonEvent) {
        logger.trace("Process STB event for device {}, json='{}'", thingConfig.getFriendlyName(), jsonEvent);
        JsonReader reader = Json.createReader(new StringReader(jsonEvent));
        try {
            JsonObject event = reader.readObject();
            boolean flUpdatePower = false;
            if (jsonEvent.contains("EVENT_EIT_CHANGE")) {
                String channel_code = getJString(event, "channel_code", "");
                String channel_num = "";
                if (jsonEvent.contains("channel_num")) {
                    if (jsonEvent.contains("\"channel_num\":\"")) {
                        // MR401: channel_num is a string
                        channel_num = getJString(event, "channel_num", "");
                    } else {
                        // MR201: channel_num is an int
                        Integer chan = getJInt(event, "channel_num", 0);
                        channel_num = chan.toString();
                    }
                }

                if ((channel_num != null) && (channel_code != null)) {
                    logger.debug("EVENT_EIT_CHANGE for channel {}/{}", channel_num, channel_code);
                    updateState(CHANNEL_CHANNEL, new StringType(channel_num));
                    updateState(CHANNEL_CHANNEL_CODE, new StringType(channel_code));
                }
                JsonArray program_info = event.getJsonArray("program_info");
                if (program_info != null) {
                    int i = 0;
                    for (JsonValue pi : program_info) {
                        JsonObject pinfo = pi.asJsonObject();
                        if (pinfo.get("start_time") == null) {
                            logger.debug("EVENT_EIT_CHANGE: empty event data = '{}'", jsonEvent);
                            continue; // empty program_info
                        }

                        String start_time = getJString(pinfo, "start_time", "");
                        String duration = getJString(pinfo, "duration", "");
                        int running_status = getJInt(pinfo, "running_status", -1);
                        updateState(CHANNEL_RUN_STATUS, new StringType(control.getRunStatus(running_status)));

                        JsonArray short_event = pinfo.getJsonArray("short_event");
                        if (short_event != null) {
                            for (JsonValue si : short_event) {
                                JsonObject sevent = si.asJsonObject();
                                String event_name = getJString(sevent, "event_name", "");
                                String event_text = getJString(sevent, "text_char", "");

                                // Convert UTC to local time
                                // 2018/11/04 21:45:00 -> "2018-11-04T10:15:30.00Z"
                                String tsLocal = start_time.replace('/', '-').replace(" ", "T") + "Z";
                                Instant timestamp = Instant.parse(tsLocal);
                                ZonedDateTime localTime = timestamp.atZone(ZoneId.of("Europe/Berlin"));
                                tsLocal = StringUtils.substringBeforeLast(localTime.toString(), "[");
                                tsLocal = StringUtils.substringBefore(tsLocal.replace('-', '/').replace('T', ' '), "+");

                                logger.debug("Info for channel {} / {} - {} '{}'.'{}', start time={}, duration={}",
                                        channel_num, channel_code, control.getRunStatus(running_status), event_name,
                                        event_text, tsLocal, duration);
                                if (running_status != EV_EITCHG_RUNNING_NOT_RUNNING) {
                                    updateState(CHANNEL_PROG_TITLE, new StringType(event_name));
                                    updateState(CHANNEL_PROG_TEXT, new StringType(event_text));
                                    updateState(CHANNEL_PROG_START, new StringType(tsLocal));
                                    updateState(CHANNEL_PROG_DURATION, new StringType(duration));
                                    if (i++ == 0) {
                                        flUpdatePower = true;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (jsonEvent.contains("new_play_mode")) {
                Integer playMode = getJInt(event, "new_play_mode", -1);
                Integer duration = getJInt(event, "duration", -1);
                Integer playPostion = getJInt(event, "playPostion", -1);
                logger.debug("STB event playContent: playMode={}, duration={}, playPosition={}",
                        control.getPlayStatus(playMode), duration.toString(), playPostion.toString());

                // If we get a playConfig event there MR must be online. However it also sends a
                // plyMode stop before powering off the device, so we filter this.
                if ((playMode != EV_PLAYCHG_STOP) && this.isInitialized()) {
                    flUpdatePower = true;
                }

                if (playMode != -1) {
                    updateState(CHANNEL_PLAY_MODE, new StringType(control.getPlayStatus(playMode)));
                }
                if (duration > 0) {
                    updateState(CHANNEL_PROG_DURATION, new StringType(duration.toString()));
                }
                if (playPostion != -1) {
                    updateState(CHANNEL_PROG_POS, new StringType(Integer.toString(playPostion)));
                }
            } else {
                logger.debug("Unknown stb event, JSON='{}", jsonEvent);
            }
            if (flUpdatePower) {
                // We received a non-stopped event -> MR must be on
                updateState(CHANNEL_POWER, OnOffType.ON);
            }
        } catch (Exception e) {
            logger.exception("Unable to process STB event: JSON='{}'" + jsonEvent, e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public void onPowerOff() throws Exception {
        logger.info("Power-Off received for device '{}' ({})", thingConfig.getFriendlyName(),
                thingConfig.getTerminalID());
        // MR was powered off -> update pwoer status, reset items
        updateState(CHANNEL_POWER, OnOffType.OFF);
        updateState(CHANNEL_PROG_DURATION, new StringType(""));
        updateState(CHANNEL_PROG_POS, new StringType(""));
        updateState(CHANNEL_PROG_TITLE, new StringType(""));
        updateState(CHANNEL_PROG_TEXT, new StringType(""));
        updateState(CHANNEL_PROG_START, new StringType(""));
        updateState(CHANNEL_PROG_DURATION, new StringType(""));
    }

    @Override
    public void dispose() {
        cancelPairingCheck();
        if (handlerFactory != null) {
            handlerFactory.removeDevice(thingConfig.getTerminalID());
        }
        scheduler.shutdownNow();
        super.dispose();
    }

    private void cancelPairingCheck() {
        if (pairingWatchdogJob != null) {
            pairingWatchdogJob.cancel(true);
        }
    }

    private String getJString(JsonObject json, String key, String defaultString) {
        if (json != null) {
            return json.containsKey(key) ? json.getString(key) : defaultString;
        }
        return defaultString;
    }

    private int getJInt(JsonObject json, String key, int defaultInt) {
        if (json != null) {
            return json.containsKey(key) ? json.getInt(key) : defaultInt;
        }
        return defaultInt;
    }

    @Override
    public void onHeartbeat() throws Exception {
    }
}
