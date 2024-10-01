/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.powermax.internal.handler;

import static org.openhab.binding.powermax.internal.PowermaxBindingConstants.*;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.config.PowermaxIpConfiguration;
import org.openhab.binding.powermax.internal.config.PowermaxSerialConfiguration;
import org.openhab.binding.powermax.internal.discovery.PowermaxDiscoveryService;
import org.openhab.binding.powermax.internal.message.PowermaxCommManager;
import org.openhab.binding.powermax.internal.message.PowermaxSendType;
import org.openhab.binding.powermax.internal.state.PowermaxArmMode;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettingsListener;
import org.openhab.binding.powermax.internal.state.PowermaxPanelType;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.binding.powermax.internal.state.PowermaxStateContainer.Value;
import org.openhab.binding.powermax.internal.state.PowermaxStateEvent;
import org.openhab.binding.powermax.internal.state.PowermaxStateEventListener;
import org.openhab.binding.powermax.internal.state.PowermaxZoneSettings;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PowermaxBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxBridgeHandler extends BaseBridgeHandler implements PowermaxStateEventListener {

    private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5);

    private static final int NB_EVENT_LOG = 10;

    private static final PowermaxPanelType DEFAULT_PANEL_TYPE = PowermaxPanelType.POWERMAX_PRO;

    private static final int JOB_REPEAT = 20;

    private static final int MAX_DOWNLOAD_ATTEMPTS = 3;

    private final Logger logger = LoggerFactory.getLogger(PowermaxBridgeHandler.class);

    private final SerialPortManager serialPortManager;
    private final TimeZoneProvider timeZoneProvider;

    private final List<PowermaxPanelSettingsListener> listeners = new CopyOnWriteArrayList<>();

    private @Nullable ScheduledFuture<?> globalJob;

    /** The delay in milliseconds to reset a motion detection */
    private long motionOffDelay;

    /** The PIN code to use for arming/disarming the Powermax alarm system from openHAB */
    private String pinCode;

    /** Force the standard mode rather than trying using the Powerlink mode */
    private boolean forceStandardMode;

    /** The object to store the current state of the Powermax alarm system */
    private PowermaxState currentState;

    /** The object in charge of the communication with the Powermax alarm system */
    private @Nullable PowermaxCommManager commManager;

    private int remainingDownloadAttempts;

    public PowermaxBridgeHandler(Bridge thing, SerialPortManager serialPortManager, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.serialPortManager = serialPortManager;
        this.timeZoneProvider = timeZoneProvider;
        this.pinCode = "";
        this.currentState = new PowermaxState(new PowermaxPanelSettings(DEFAULT_PANEL_TYPE), timeZoneProvider);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(PowermaxDiscoveryService.class);
    }

    public @Nullable PowermaxState getCurrentState() {
        PowermaxCommManager localCommManager = commManager;
        return (localCommManager == null) ? null : currentState;
    }

    public @Nullable PowermaxPanelSettings getPanelSettings() {
        PowermaxCommManager localCommManager = commManager;
        return (localCommManager == null) ? null : localCommManager.getPanelSettings();
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        commManager = null;

        String threadName = "OH-binding-" + getThing().getUID().getAsString();

        String errorMsg = String.format("@text/offline.config-error-unexpected-thing-type [ \"%s\" ]",
                getThing().getThingTypeUID().getAsString());
        if (getThing().getThingTypeUID().equals(BRIDGE_TYPE_SERIAL)) {
            errorMsg = initializeBridgeSerial(getConfigAs(PowermaxSerialConfiguration.class), threadName);
        } else if (getThing().getThingTypeUID().equals(BRIDGE_TYPE_IP)) {
            errorMsg = initializeBridgeIp(getConfigAs(PowermaxIpConfiguration.class), threadName);
        }

        if (errorMsg == null) {
            ScheduledFuture<?> job = globalJob;
            if (job == null || job.isCancelled()) {
                // Delay the startup in case the handler is restarted immediately
                globalJob = scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        logger.trace("Powermax job...");
                        updateMotionSensorState();
                        updateRingingState();
                        if (isConnected()) {
                            checkKeepAlive();
                            retryDownloadSetup();
                        } else {
                            tryReconnect();
                        }
                    } catch (Exception e) {
                        logger.warn("Exception in scheduled job: {}", e.getMessage(), e);
                    }
                }, 10, JOB_REPEAT, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    private @Nullable String initializeBridgeSerial(PowermaxSerialConfiguration config, String threadName) {
        String errorMsg = null;
        String serialPort = config.serialPort.trim();
        if (!serialPort.isEmpty() && !serialPort.startsWith("rfc2217")) {
            motionOffDelay = config.motionOffDelay * ONE_MINUTE;
            pinCode = config.pinCode;
            forceStandardMode = config.forceStandardMode;
            PowermaxPanelType panelType = getPanelTypeSetting(config.panelType, DEFAULT_PANEL_TYPE);

            PowermaxArmMode.DISARMED.setAllowedCommand(config.allowDisarming);
            PowermaxArmMode.ARMED_HOME.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_AWAY.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_HOME_INSTANT.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_AWAY_INSTANT.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_NIGHT.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_NIGHT_INSTANT.setAllowedCommand(config.allowArming);

            commManager = new PowermaxCommManager(serialPort, panelType, forceStandardMode, config.autoSyncTime,
                    serialPortManager, threadName, timeZoneProvider);
        } else {
            if (serialPort.startsWith("rfc2217")) {
                errorMsg = "@text/offline.config-error-invalid-thing-type";
            } else {
                errorMsg = "@text/offline.config-error-mandatory-serial-port";
            }
        }
        return errorMsg;
    }

    private @Nullable String initializeBridgeIp(PowermaxIpConfiguration config, String threadName) {
        String errorMsg = null;
        String ip = config.ip.trim();
        if (!ip.isEmpty() && config.tcpPort > 0) {
            motionOffDelay = config.motionOffDelay * ONE_MINUTE;
            pinCode = config.pinCode;
            forceStandardMode = config.forceStandardMode;
            PowermaxPanelType panelType = getPanelTypeSetting(config.panelType, DEFAULT_PANEL_TYPE);

            PowermaxArmMode.DISARMED.setAllowedCommand(config.allowDisarming);
            PowermaxArmMode.ARMED_HOME.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_AWAY.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_HOME_INSTANT.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_AWAY_INSTANT.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_NIGHT.setAllowedCommand(config.allowArming);
            PowermaxArmMode.ARMED_NIGHT_INSTANT.setAllowedCommand(config.allowArming);

            commManager = new PowermaxCommManager(ip, config.tcpPort, panelType, forceStandardMode, config.autoSyncTime,
                    threadName, timeZoneProvider);
        } else {
            errorMsg = "@text/offline.config-error-mandatory-ip-port";
        }
        return errorMsg;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());
        ScheduledFuture<?> job = globalJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            globalJob = null;
        }
        closeConnection();
        commManager = null;
        super.dispose();
    }

    /*
     * Set the state of items linked to motion sensors to OFF when the last trip is older
     * than the value defined by the variable motionOffDelay
     */
    private void updateMotionSensorState() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager != null) {
            long now = System.currentTimeMillis();
            boolean update = false;
            PowermaxState updateState = localCommManager.createNewState();
            PowermaxPanelSettings panelSettings = localCommManager.getPanelSettings();
            for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                PowermaxZoneSettings zoneSettings = panelSettings.getZoneSettings(i);
                if (zoneSettings != null && zoneSettings.isMotionSensor()
                        && currentState.getZone(i).isLastTripBeforeTime(now - motionOffDelay)) {
                    update = true;
                    updateState.getZone(i).tripped.setValue(false);
                }
            }
            if (update) {
                updateChannelsFromAlarmState(TRIPPED, updateState);
                currentState.merge(updateState);
            }
        }
    }

    /**
     * Turn off the Ringing flag when the bell time expires
     */
    private void updateRingingState() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager != null && Boolean.TRUE.equals(currentState.ringing.getValue())) {
            long now = System.currentTimeMillis();
            long bellTime = localCommManager.getPanelSettings().getBellTime() * ONE_MINUTE;

            Long ringingSince = currentState.ringingSince.getValue();
            if (ringingSince != null && (ringingSince + bellTime) < now) {
                PowermaxState updateState = localCommManager.createNewState();
                updateState.ringing.setValue(false);
                updateChannelsFromAlarmState(RINGING, updateState);
                currentState.merge(updateState);
            }
        }
    }

    /*
     * Check that we're actively communicating with the panel
     */
    private void checkKeepAlive() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long lastKeepAlive = currentState.lastKeepAlive.getValue();
        Long lastMessageTime = currentState.lastMessageTime.getValue();
        if (Boolean.TRUE.equals(currentState.powerlinkMode.getValue()) && (lastKeepAlive != null)
                && ((now - lastKeepAlive) > ONE_MINUTE)) {
            // In Powerlink mode: let Powermax know we are alive
            localCommManager.sendRestoreMessage();
            currentState.lastKeepAlive.setValue(now);
        } else if (!Boolean.TRUE.equals(currentState.downloadMode.getValue()) && (lastMessageTime != null)
                && ((now - lastMessageTime) > FIVE_MINUTES)) {
            // In Standard mode: ping the panel every so often to detect disconnects
            localCommManager.sendMessage(PowermaxSendType.STATUS);
        }
    }

    private void tryReconnect() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            return;
        }
        logger.info("Trying to connect or reconnect...");
        closeConnection();
        currentState = localCommManager.createNewState();
        try {
            openConnection();
            logger.debug("openConnection(): connected");
            updateStatus(ThingStatus.ONLINE);
            updateChannelsFromAlarmState(currentState);
            if (forceStandardMode) {
                currentState.powerlinkMode.setValue(false);
                updateChannelsFromAlarmState(MODE, currentState);
                processPanelSettings();
            } else {
                localCommManager.startDownload();
            }
        } catch (Exception e) {
            logger.debug("openConnection(): {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            setAllChannelsOffline();
        }
    }

    /**
     * Open a TCP or Serial connection to the Powermax Alarm Panel
     *
     * @return true if the connection has been opened
     */
    private synchronized void openConnection() throws Exception {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager != null) {
            localCommManager.addEventListener(this);
            localCommManager.open();
        }
        remainingDownloadAttempts = MAX_DOWNLOAD_ATTEMPTS;
    }

    /**
     * Close TCP or Serial connection to the Powermax Alarm Panel and remove the Event Listener
     */
    private synchronized void closeConnection() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager != null) {
            localCommManager.close();
            localCommManager.removeEventListener(this);
        }
        logger.debug("closeConnection(): disconnected");
    }

    private boolean isConnected() {
        PowermaxCommManager localCommManager = commManager;
        return localCommManager == null ? false : localCommManager.isConnected();
    }

    private void retryDownloadSetup() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager != null) {
            localCommManager.retryDownloadSetup(remainingDownloadAttempts);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} from channel {}", command, channelUID.getId());

        if (command instanceof RefreshType) {
            updateChannelsFromAlarmState(channelUID.getId(), getCurrentState());
        } else {
            switch (channelUID.getId()) {
                case ARM_MODE:
                    try {
                        PowermaxArmMode armMode = PowermaxArmMode.fromShortName(command.toString());
                        armCommand(armMode);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Powermax alarm binding: invalid command {}", command);
                    }
                    break;
                case SYSTEM_ARMED:
                    if (command instanceof OnOffType) {
                        armCommand(
                                command.equals(OnOffType.ON) ? PowermaxArmMode.ARMED_AWAY : PowermaxArmMode.DISARMED);
                    } else {
                        logger.debug("Command of type {} while OnOffType is expected. Command is ignored.",
                                command.getClass().getSimpleName());
                    }
                    break;
                case PGM_STATUS:
                    pgmCommand(command);
                    break;
                case UPDATE_EVENT_LOGS:
                    downloadEventLog();
                    break;
                case DOWNLOAD_SETUP:
                    downloadSetup();
                    break;
                default:
                    logger.debug("No available command for channel {}. Command is ignored.", channelUID.getId());
                    break;
            }
        }
    }

    private void armCommand(PowermaxArmMode armMode) {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            logger.debug("Powermax alarm binding not correctly initialized. Arm command is ignored.");
        } else if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Arm command is ignored.");
        } else {
            localCommManager.requestArmMode(armMode,
                    Boolean.TRUE.equals(currentState.powerlinkMode.getValue())
                            ? localCommManager.getPanelSettings().getFirstPinCode()
                            : pinCode);
        }
    }

    private void pgmCommand(Command command) {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            logger.debug("Powermax alarm binding not correctly initialized. PGM command is ignored.");
        } else if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. PGM command is ignored.");
        } else {
            localCommManager.sendPGMX10(command, null);
        }
    }

    public void x10Command(Byte deviceNr, Command command) {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            logger.debug("Powermax alarm binding not correctly initialized. X10 command is ignored.");
        } else if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. X10 command is ignored.");
        } else {
            localCommManager.sendPGMX10(command, deviceNr);
        }
    }

    public void zoneBypassed(byte zoneNr, boolean bypassed) {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            logger.debug("Powermax alarm binding not correctly initialized. Zone bypass command is ignored.");
        } else if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Zone bypass command is ignored.");
        } else if (!Boolean.TRUE.equals(currentState.powerlinkMode.getValue())) {
            logger.debug("Powermax alarm binding: Bypass option only supported in Powerlink mode");
        } else if (!localCommManager.getPanelSettings().isBypassEnabled()) {
            logger.debug("Powermax alarm binding: Bypass option not enabled in panel settings");
        } else {
            localCommManager.sendZoneBypass(bypassed, zoneNr, localCommManager.getPanelSettings().getFirstPinCode());
        }
    }

    private void downloadEventLog() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            logger.debug("Powermax alarm binding not correctly initialized. Event logs command is ignored.");
        } else if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Event logs command is ignored.");
        } else {
            localCommManager.requestEventLog(Boolean.TRUE.equals(currentState.powerlinkMode.getValue())
                    ? localCommManager.getPanelSettings().getFirstPinCode()
                    : pinCode);
        }
    }

    public void downloadSetup() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            logger.debug("Powermax alarm binding not correctly initialized. Download setup command is ignored.");
        } else if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Download setup command is ignored.");
        } else if (!Boolean.TRUE.equals(currentState.powerlinkMode.getValue())) {
            logger.debug("Powermax alarm binding: download setup only supported in Powerlink mode");
        } else if (localCommManager.isDownloadRunning()) {
            logger.debug("Powermax alarm binding: download setup not started as one is in progress");
        } else {
            localCommManager.startDownload();
            if (currentState.lastKeepAlive.getValue() != null) {
                currentState.lastKeepAlive.setValue(System.currentTimeMillis());
            }
        }
    }

    public String getInfoSetup() {
        PowermaxPanelSettings panelSettings = getPanelSettings();
        return (panelSettings == null) ? "" : panelSettings.getInfo();
    }

    @Override
    public void onNewStateEvent(EventObject event) {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            return;
        }

        PowermaxStateEvent stateEvent = (PowermaxStateEvent) event;
        PowermaxState updateState = stateEvent.getState();

        if (Boolean.TRUE.equals(currentState.powerlinkMode.getValue())
                && Boolean.TRUE.equals(updateState.downloadSetupRequired.getValue())) {
            // After Enrolling Powerlink or if a reset is required
            logger.debug("Powermax alarm binding: Reset");
            localCommManager.startDownload();
            updateState.downloadSetupRequired.setValue(false);
            if (currentState.lastKeepAlive.getValue() != null) {
                currentState.lastKeepAlive.setValue(System.currentTimeMillis());
            }
        } else if (Boolean.FALSE.equals(currentState.powerlinkMode.getValue())
                && updateState.lastKeepAlive.getValue() != null) {
            // Were are in standard mode but received a keep alive message
            // so we switch in PowerLink mode
            logger.debug("Powermax alarm binding: Switching to Powerlink mode");
            localCommManager.startDownload();
        }

        boolean doProcessSettings = (updateState.powerlinkMode.getValue() != null);

        PowermaxPanelSettings panelSettings = localCommManager.getPanelSettings();
        panelSettings.getZoneRange().forEach(i -> {
            if (Boolean.TRUE.equals(updateState.getZone(i).armed.getValue())
                    && Boolean.TRUE.equals(currentState.getZone(i).bypassed.getValue())) {
                updateState.getZone(i).armed.setValue(false);
            }
        });

        updateState.keepOnlyDifferencesWith(currentState);
        updateChannelsFromAlarmState(updateState);
        currentState.merge(updateState);

        if (!updateState.getUpdatedZoneNames().isEmpty()) {
            for (Integer zoneIdx : updateState.getUpdatedZoneNames().keySet()) {
                if (panelSettings.getZoneSettings(zoneIdx) != null) {
                    for (PowermaxPanelSettingsListener listener : listeners) {
                        listener.onZoneSettingsUpdated(zoneIdx, panelSettings);
                    }
                }
            }
        }

        if (doProcessSettings) {
            // There is a change of mode (standard or Powerlink)
            processPanelSettings();
            localCommManager.exitDownload();
        }
    }

    @Override
    public void onCommunicationFailure(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        setAllChannelsOffline();
    }

    private void processPanelSettings() {
        PowermaxCommManager localCommManager = commManager;
        if (localCommManager == null) {
            return;
        }

        if (localCommManager.processPanelSettings(Boolean.TRUE.equals(currentState.powerlinkMode.getValue()))) {
            for (PowermaxPanelSettingsListener listener : listeners) {
                listener.onPanelSettingsUpdated(localCommManager.getPanelSettings());
            }
            remainingDownloadAttempts = 0;
        } else {
            logger.info("Powermax alarm binding: setup download failed!");
            for (PowermaxPanelSettingsListener listener : listeners) {
                listener.onPanelSettingsUpdated(null);
            }
            remainingDownloadAttempts--;
        }
        updatePropertiesFromPanelSettings();
        if (Boolean.TRUE.equals(currentState.powerlinkMode.getValue())) {
            logger.info("Powermax alarm binding: running in Powerlink mode");
            localCommManager.sendRestoreMessage();
        } else {
            logger.info("Powermax alarm binding: running in Standard mode");
            localCommManager.getInfosWhenInStandardMode();
        }
    }

    /**
     * Update channels to match a new alarm system state
     *
     * @param state: the alarm system state
     */
    private void updateChannelsFromAlarmState(@Nullable PowermaxState state) {
        updateChannelsFromAlarmState(null, state);
    }

    /**
     * Update channels to match a new alarm system state
     *
     * @param channel: filter on a particular channel; if null, consider all channels
     * @param state: the alarm system state
     */
    private synchronized void updateChannelsFromAlarmState(@Nullable String channel, @Nullable PowermaxState state) {
        if (state == null || !isConnected()) {
            return;
        }

        for (Value<?> value : state.getValues()) {
            String vChannel = value.getChannel();

            if (((channel == null) || channel.equals(vChannel)) && isLinked(vChannel) && (value.getValue() != null)) {
                updateState(vChannel, value.getState());
            }
        }

        for (int i = 1; i <= NB_EVENT_LOG; i++) {
            String channel2 = String.format(EVENT_LOG, i);
            String log = state.getEventLog(i);
            if (((channel == null) || channel.equals(channel2)) && isLinked(channel2) && (log != null)) {
                updateState(channel2, new StringType(log));
            }
        }

        for (Thing thing : getThing().getThings()) {
            if (!thing.isEnabled()) {
                continue;
            }
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof PowermaxThingHandler powermaxThingHandler) {
                if (thing.getThingTypeUID().equals(THING_TYPE_ZONE)) {
                    // All of the zone state objects will have the same list of values.
                    // The use of getZone(1) here is just to get any PowermaxZoneState
                    // and use it to get the list of zone channels.

                    for (Value<?> value : state.getZone(1).getValues()) {
                        String channelId = value.getChannel();
                        if ((channel == null) || channel.equals(channelId)) {
                            powermaxThingHandler.updateChannelFromAlarmState(channelId, state);
                        }
                    }
                } else if (thing.getThingTypeUID().equals(THING_TYPE_X10)) {
                    if ((channel == null) || channel.equals(X10_STATUS)) {
                        powermaxThingHandler.updateChannelFromAlarmState(X10_STATUS, state);
                    }
                }
            }
        }
    }

    /**
     * Update all channels to an UNDEF state to indicate that communication with the panel is offline
     */
    private synchronized void setAllChannelsOffline() {
        getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }

    /**
     * Update properties to match the alarm panel settings
     */
    private void updatePropertiesFromPanelSettings() {
        PowermaxPanelSettings panelSettings = getPanelSettings();
        if (panelSettings == null) {
            return;
        }
        String value;
        Map<String, String> properties = editProperties();
        value = panelSettings.getPanelType().getLabel();
        if (!value.isEmpty()) {
            properties.put(Thing.PROPERTY_MODEL_ID, value);
        }
        value = panelSettings.getPanelSerial();
        if (value != null && !value.isEmpty()) {
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, value);
        }
        value = panelSettings.getPanelEprom();
        if (value != null && !value.isEmpty()) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, value);
        }
        value = panelSettings.getPanelSoftware();
        if (value != null && !value.isEmpty()) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, value);
        }
        updateProperties(properties);
    }

    public boolean registerPanelSettingsListener(PowermaxPanelSettingsListener listener) {
        boolean inList = true;
        if (!listeners.contains(listener)) {
            inList = listeners.add(listener);
        }
        return inList;
    }

    public boolean unregisterPanelSettingsListener(PowermaxPanelSettingsListener listener) {
        return listeners.remove(listener);
    }

    private PowermaxPanelType getPanelTypeSetting(String value, PowermaxPanelType defaultValue) {
        PowermaxPanelType result;
        try {
            result = PowermaxPanelType.fromLabel(value);
        } catch (IllegalArgumentException e) {
            result = defaultValue;
            logger.debug("Powermax alarm binding: panel type not configured correctly");
        }
        return result;
    }
}
