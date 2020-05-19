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
package org.openhab.binding.powermax.internal.handler;

import static org.openhab.binding.powermax.internal.PowermaxBindingConstants.*;

import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.powermax.internal.config.PowermaxIpConfiguration;
import org.openhab.binding.powermax.internal.config.PowermaxSerialConfiguration;
import org.openhab.binding.powermax.internal.message.PowermaxCommManager;
import org.openhab.binding.powermax.internal.state.PowermaxArmMode;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettingsListener;
import org.openhab.binding.powermax.internal.state.PowermaxPanelType;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.binding.powermax.internal.state.PowermaxStateEvent;
import org.openhab.binding.powermax.internal.state.PowermaxStateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PowermaxBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxBridgeHandler extends BaseBridgeHandler implements PowermaxStateEventListener {

    private final Logger logger = LoggerFactory.getLogger(PowermaxBridgeHandler.class);

    private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);

    /** Default delay in milliseconds to reset a motion detection */
    private static final long DEFAULT_MOTION_OFF_DELAY = TimeUnit.MINUTES.toMillis(3);

    private static final int NB_EVENT_LOG = 10;

    private static final PowermaxPanelType DEFAULT_PANEL_TYPE = PowermaxPanelType.POWERMAX_PRO;

    private static final int JOB_REPEAT = 20;

    private static final int MAX_DOWNLOAD_ATTEMPTS = 3;

    private ScheduledFuture<?> globalJob;

    private List<PowermaxPanelSettingsListener> listeners = new CopyOnWriteArrayList<>();

    /** The delay in milliseconds to reset a motion detection */
    private long motionOffDelay;

    /** The PIN code to use for arming/disarming the Powermax alarm system from openHAB */
    private String pinCode;

    /** Force the standard mode rather than trying using the Powerlink mode */
    private boolean forceStandardMode;

    /** The object to store the current state of the Powermax alarm system */
    private PowermaxState currentState;

    /** The object in charge of the communication with the Powermax alarm system */
    private PowermaxCommManager commManager;

    private int remainingDownloadAttempts;
    private SerialPortManager serialPortManager;

    public PowermaxBridgeHandler(Bridge thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    public PowermaxState getCurrentState() {
        return currentState;
    }

    public PowermaxPanelSettings getPanelSettings() {
        return (commManager == null) ? null : commManager.getPanelSettings();
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        commManager = null;

        String errorMsg = null;
        if (getThing().getThingTypeUID().equals(BRIDGE_TYPE_SERIAL)) {
            errorMsg = initializeBridgeSerial(getConfigAs(PowermaxSerialConfiguration.class));
        } else if (getThing().getThingTypeUID().equals(BRIDGE_TYPE_IP)) {
            errorMsg = initializeBridgeIp(getConfigAs(PowermaxIpConfiguration.class));
        } else {
            errorMsg = "Unexpected thing type " + getThing().getThingTypeUID();
        }

        if (errorMsg == null) {
            if (globalJob == null || globalJob.isCancelled()) {
                // Delay the startup in case the handler is restarted immediately
                globalJob = scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        logger.debug("Powermax job...");
                        updateMotionSensorState();
                        if (isConnected()) {
                            checkKeepAlive();
                            commManager.retryDownloadSetup(remainingDownloadAttempts);
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

    private String initializeBridgeSerial(PowermaxSerialConfiguration config) {
        String errorMsg = null;
        if (StringUtils.isNotBlank(config.serialPort) && !config.serialPort.startsWith("rfc2217")) {
            motionOffDelay = getMotionOffDelaySetting(config.motionOffDelay, DEFAULT_MOTION_OFF_DELAY);
            boolean allowArming = getBooleanSetting(config.allowArming, false);
            boolean allowDisarming = getBooleanSetting(config.allowDisarming, false);
            pinCode = config.pinCode;
            forceStandardMode = getBooleanSetting(config.forceStandardMode, false);
            PowermaxPanelType panelType = getPanelTypeSetting(config.panelType, DEFAULT_PANEL_TYPE);
            boolean autoSyncTime = getBooleanSetting(config.autoSyncTime, false);

            PowermaxArmMode.DISARMED.setAllowedCommand(allowDisarming);
            PowermaxArmMode.ARMED_HOME.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_AWAY.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_HOME_INSTANT.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_AWAY_INSTANT.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_NIGHT.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_NIGHT_INSTANT.setAllowedCommand(allowArming);

            commManager = new PowermaxCommManager(config.serialPort, panelType, forceStandardMode, autoSyncTime,
                    serialPortManager);
        } else {
            if (StringUtils.isNotBlank(config.serialPort) && config.serialPort.startsWith("rfc2217")) {
                errorMsg = "Please use the IP Connection thing type for a serial over IP connection.";
            } else {
                errorMsg = "serialPort setting must be defined in thing configuration";
            }
        }
        return errorMsg;
    }

    private String initializeBridgeIp(PowermaxIpConfiguration config) {
        String errorMsg = null;
        if (StringUtils.isNotBlank(config.ip) && config.tcpPort != null) {
            motionOffDelay = getMotionOffDelaySetting(config.motionOffDelay, DEFAULT_MOTION_OFF_DELAY);
            boolean allowArming = getBooleanSetting(config.allowArming, false);
            boolean allowDisarming = getBooleanSetting(config.allowDisarming, false);
            pinCode = config.pinCode;
            forceStandardMode = getBooleanSetting(config.forceStandardMode, false);
            PowermaxPanelType panelType = getPanelTypeSetting(config.panelType, DEFAULT_PANEL_TYPE);
            boolean autoSyncTime = getBooleanSetting(config.autoSyncTime, false);

            PowermaxArmMode.DISARMED.setAllowedCommand(allowDisarming);
            PowermaxArmMode.ARMED_HOME.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_AWAY.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_HOME_INSTANT.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_AWAY_INSTANT.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_NIGHT.setAllowedCommand(allowArming);
            PowermaxArmMode.ARMED_NIGHT_INSTANT.setAllowedCommand(allowArming);

            commManager = new PowermaxCommManager(config.ip, config.tcpPort, panelType, forceStandardMode,
                    autoSyncTime);
        } else {
            errorMsg = "ip and port settings must be defined in thing configuration";
        }
        return errorMsg;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
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
        long now = System.currentTimeMillis();
        if (currentState != null) {
            boolean update = false;
            PowermaxState updateState = commManager.createNewState();
            PowermaxPanelSettings panelSettings = getPanelSettings();
            for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                if (panelSettings.getZoneSettings(i) != null && panelSettings.getZoneSettings(i).isMotionSensor()
                        && currentState.isLastTripBeforeTime(i, now - motionOffDelay)) {
                    update = true;
                    updateState.setSensorTripped(i, false);
                }
            }
            if (update) {
                updateChannelsFromAlarmState(TRIPPED, updateState);
                currentState.merge(updateState);
            }
        }
    }

    /*
     * Check that we receive a keep alive message during the last minute
     */
    private void checkKeepAlive() {
        long now = System.currentTimeMillis();
        if (Boolean.TRUE.equals(currentState.isPowerlinkMode()) && (currentState.getLastKeepAlive() != null)
                && ((now - currentState.getLastKeepAlive()) > ONE_MINUTE)) {
            // Let Powermax know we are alive
            commManager.sendRestoreMessage();
            currentState.setLastKeepAlive(now);
        }
    }

    private void tryReconnect() {
        logger.debug("trying to reconnect...");
        closeConnection();
        currentState = commManager.createNewState();
        if (openConnection()) {
            updateStatus(ThingStatus.ONLINE);
            if (forceStandardMode) {
                currentState.setPowerlinkMode(false);
                updateChannelsFromAlarmState(MODE, currentState);
                processPanelSettings();
            } else {
                commManager.startDownload();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Reconnection failed");
        }
    }

    /**
     * Open a TCP or Serial connection to the Powermax Alarm Panel
     *
     * @return true if the connection has been opened
     */
    private synchronized boolean openConnection() {
        if (commManager != null) {
            commManager.addEventListener(this);
            commManager.open();
        }
        remainingDownloadAttempts = MAX_DOWNLOAD_ATTEMPTS;
        logger.debug("openConnection(): {}", isConnected() ? "connected" : "disconnected");
        return isConnected();
    }

    /**
     * Close TCP or Serial connection to the Powermax Alarm Panel and remove the Event Listener
     */
    private synchronized void closeConnection() {
        if (commManager != null) {
            commManager.close();
            commManager.removeEventListener(this);
        }
        logger.debug("closeConnection(): disconnected");
    }

    private boolean isConnected() {
        return commManager == null ? false : commManager.isConnected();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} from channel {}", command, channelUID.getId());

        if (command instanceof RefreshType) {
            updateChannelsFromAlarmState(channelUID.getId(), currentState);
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
        if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Arm command is ignored.");
        } else {
            commManager.requestArmMode(armMode,
                    currentState.isPowerlinkMode() ? getPanelSettings().getFirstPinCode() : pinCode);
        }
    }

    private void pgmCommand(Command command) {
        if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. PGM command is ignored.");
        } else {
            commManager.sendPGMX10(command, null);
        }
    }

    public void x10Command(Byte deviceNr, Command command) {
        if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. X10 command is ignored.");
        } else {
            commManager.sendPGMX10(command, deviceNr);
        }
    }

    public void zoneBypassed(byte zoneNr, boolean bypassed) {
        if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Zone bypass command is ignored.");
        } else if (!Boolean.TRUE.equals(currentState.isPowerlinkMode())) {
            logger.debug("Powermax alarm binding: Bypass option only supported in Powerlink mode");
        } else if (!getPanelSettings().isBypassEnabled()) {
            logger.debug("Powermax alarm binding: Bypass option not enabled in panel settings");
        } else {
            commManager.sendZoneBypass(bypassed, zoneNr, getPanelSettings().getFirstPinCode());
        }
    }

    private void downloadEventLog() {
        if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Event logs command is ignored.");
        } else {
            commManager
                    .requestEventLog(currentState.isPowerlinkMode() ? getPanelSettings().getFirstPinCode() : pinCode);
        }
    }

    public void downloadSetup() {
        if (!isConnected()) {
            logger.debug("Powermax alarm binding not connected. Download setup command is ignored.");
        } else if (!Boolean.TRUE.equals(currentState.isPowerlinkMode())) {
            logger.debug("Powermax alarm binding: download setup only supported in Powerlink mode");
        } else if (commManager.isDownloadRunning()) {
            logger.debug("Powermax alarm binding: download setup not started as one is in progress");
        } else {
            commManager.startDownload();
            if (currentState.getLastKeepAlive() != null) {
                currentState.setLastKeepAlive(System.currentTimeMillis());
            }
        }
    }

    public String getInfoSetup() {
        return (getPanelSettings() == null) ? "" : getPanelSettings().getInfo();
    }

    @Override
    public void onNewStateEvent(EventObject event) {
        PowermaxStateEvent stateEvent = (PowermaxStateEvent) event;
        PowermaxState updateState = stateEvent.getState();

        if (Boolean.TRUE.equals(currentState.isPowerlinkMode())
                && Boolean.TRUE.equals(updateState.isDownloadSetupRequired())) {
            // After Enrolling Powerlink or if a reset is required
            logger.debug("Powermax alarm binding: Reset");
            commManager.startDownload();
            if (currentState.getLastKeepAlive() != null) {
                currentState.setLastKeepAlive(System.currentTimeMillis());
            }
        } else if (Boolean.FALSE.equals(currentState.isPowerlinkMode()) && updateState.getLastKeepAlive() != null) {
            // Were are in standard mode but received a keep alive message
            // so we switch in PowerLink mode
            logger.debug("Powermax alarm binding: Switching to Powerlink mode");
            commManager.startDownload();
        }

        boolean doProcessSettings = (updateState.isPowerlinkMode() != null);

        for (int i = 1; i <= getPanelSettings().getNbZones(); i++) {
            if (Boolean.TRUE.equals(updateState.isSensorArmed(i))
                    && Boolean.TRUE.equals(currentState.isSensorBypassed(i))) {
                updateState.setSensorArmed(i, false);
            }
        }

        updateState.keepOnlyDifferencesWith(currentState);
        updateChannelsFromAlarmState(updateState);
        currentState.merge(updateState);

        PowermaxPanelSettings panelSettings = getPanelSettings();
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
            commManager.exitDownload();
        }
    }

    private void processPanelSettings() {
        if (commManager.processPanelSettings(currentState.isPowerlinkMode())) {
            for (PowermaxPanelSettingsListener listener : listeners) {
                listener.onPanelSettingsUpdated(getPanelSettings());
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
        if (currentState.isPowerlinkMode()) {
            logger.debug("Powermax alarm binding: running in Powerlink mode");
            commManager.sendRestoreMessage();
        } else {
            logger.debug("Powermax alarm binding: running in Standard mode");
            commManager.getInfosWhenInStandardMode();
        }
    }

    /**
     * Update channels to match a new alarm system state
     *
     * @param state: the alarm system state
     */
    private void updateChannelsFromAlarmState(PowermaxState state) {
        updateChannelsFromAlarmState(null, state);
    }

    /**
     * Update channels to match a new alarm system state
     *
     * @param channel: filter on a particular channel; if null, consider all channels
     * @param state: the alarm system state
     */
    private synchronized void updateChannelsFromAlarmState(String channel, PowermaxState state) {
        if (state == null) {
            return;
        }

        if (((channel == null) || channel.equals(MODE)) && isLinked(MODE) && (state.getPanelMode() != null)) {
            updateState(MODE, new StringType(state.getPanelMode()));
        }
        if (((channel == null) || channel.equals(SYSTEM_STATUS)) && isLinked(SYSTEM_STATUS)
                && (state.getStatusStr() != null)) {
            updateState(SYSTEM_STATUS, new StringType(state.getStatusStr()));
        }
        if (((channel == null) || channel.equals(READY)) && isLinked(READY) && (state.isReady() != null)) {
            updateState(READY, state.isReady() ? OnOffType.ON : OnOffType.OFF);
        }
        if (((channel == null) || channel.equals(WITH_ZONES_BYPASSED)) && isLinked(WITH_ZONES_BYPASSED)
                && (state.isBypass() != null)) {
            updateState(WITH_ZONES_BYPASSED, state.isBypass() ? OnOffType.ON : OnOffType.OFF);
        }
        if (((channel == null) || channel.equals(ALARM_ACTIVE)) && isLinked(ALARM_ACTIVE)
                && (state.isAlarmActive() != null)) {
            updateState(ALARM_ACTIVE, state.isAlarmActive() ? OnOffType.ON : OnOffType.OFF);
        }
        if (((channel == null) || channel.equals(TROUBLE)) && isLinked(TROUBLE) && (state.isTrouble() != null)) {
            updateState(TROUBLE, state.isTrouble() ? OnOffType.ON : OnOffType.OFF);
        }
        if (((channel == null) || channel.equals(ALERT_IN_MEMORY)) && isLinked(ALERT_IN_MEMORY)
                && (state.isAlertInMemory() != null)) {
            updateState(ALERT_IN_MEMORY, state.isAlertInMemory() ? OnOffType.ON : OnOffType.OFF);
        }
        if (((channel == null) || channel.equals(SYSTEM_ARMED)) && isLinked(SYSTEM_ARMED)
                && (state.isArmed() != null)) {
            updateState(SYSTEM_ARMED, state.isArmed() ? OnOffType.ON : OnOffType.OFF);
        }
        if (((channel == null) || channel.equals(ARM_MODE)) && isLinked(ARM_MODE)
                && (state.getShortArmMode() != null)) {
            updateState(ARM_MODE, new StringType(state.getShortArmMode()));
        }
        if (((channel == null) || channel.equals(PGM_STATUS)) && isLinked(PGM_STATUS)
                && (state.getPGMX10DeviceStatus(0) != null)) {
            updateState(PGM_STATUS, state.getPGMX10DeviceStatus(0) ? OnOffType.ON : OnOffType.OFF);
        }
        for (int i = 1; i <= NB_EVENT_LOG; i++) {
            String channel2 = String.format(EVENT_LOG, i);
            if (((channel == null) || channel.equals(channel2)) && isLinked(channel2)
                    && (state.getEventLog(i) != null)) {
                updateState(channel2, new StringType(state.getEventLog(i)));
            }
        }

        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() != null) {
                PowermaxThingHandler handler = (PowermaxThingHandler) thing.getHandler();
                if (handler != null) {
                    if (thing.getThingTypeUID().equals(THING_TYPE_ZONE)) {
                        if ((channel == null) || channel.equals(TRIPPED)) {
                            handler.updateChannelFromAlarmState(TRIPPED, state);
                        }
                        if ((channel == null) || channel.equals(LAST_TRIP)) {
                            handler.updateChannelFromAlarmState(LAST_TRIP, state);
                        }
                        if ((channel == null) || channel.equals(BYPASSED)) {
                            handler.updateChannelFromAlarmState(BYPASSED, state);
                        }
                        if ((channel == null) || channel.equals(ARMED)) {
                            handler.updateChannelFromAlarmState(ARMED, state);
                        }
                        if ((channel == null) || channel.equals(LOW_BATTERY)) {
                            handler.updateChannelFromAlarmState(LOW_BATTERY, state);
                        }
                    } else if (thing.getThingTypeUID().equals(THING_TYPE_X10)) {
                        if ((channel == null) || channel.equals(X10_STATUS)) {
                            handler.updateChannelFromAlarmState(X10_STATUS, state);
                        }
                    }
                }
            }
        }
    }

    /**
     * Update properties to match the alarm panel settings
     */
    private void updatePropertiesFromPanelSettings() {
        String value;
        boolean update = false;

        Map<String, String> properties = editProperties();
        PowermaxPanelSettings panelSettings = getPanelSettings();

        value = (panelSettings.getPanelType() != null) ? panelSettings.getPanelType().getLabel() : null;
        if (StringUtils.isNotEmpty(value) && ((properties.get(Thing.PROPERTY_MODEL_ID) == null)
                || !properties.get(Thing.PROPERTY_MODEL_ID).equals(value))) {
            update = true;
            properties.put(Thing.PROPERTY_MODEL_ID, value);
        }

        value = panelSettings.getPanelSerial();
        if (StringUtils.isNotEmpty(value) && ((properties.get(Thing.PROPERTY_SERIAL_NUMBER) == null)
                || !properties.get(Thing.PROPERTY_SERIAL_NUMBER).equals(value))) {
            update = true;
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, value);
        }

        value = panelSettings.getPanelEprom();
        if (StringUtils.isNotEmpty(value) && ((properties.get(Thing.PROPERTY_HARDWARE_VERSION) == null)
                || !properties.get(Thing.PROPERTY_HARDWARE_VERSION).equals(value))) {
            update = true;
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, value);
        }

        value = panelSettings.getPanelSoftware();
        if (StringUtils.isNotEmpty(value) && ((properties.get(Thing.PROPERTY_FIRMWARE_VERSION) == null)
                || !properties.get(Thing.PROPERTY_FIRMWARE_VERSION).equals(value))) {
            update = true;
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, value);
        }

        if (update) {
            updateProperties(properties);
        }
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

    private boolean getBooleanSetting(Boolean value, boolean defaultValue) {
        return value != null ? value.booleanValue() : defaultValue;
    }

    private long getMotionOffDelaySetting(Integer value, long defaultValue) {
        return value != null ? value.intValue() * ONE_MINUTE : defaultValue;
    }

    private PowermaxPanelType getPanelTypeSetting(String value, PowermaxPanelType defaultValue) {
        PowermaxPanelType result;
        if (value != null) {
            try {
                result = PowermaxPanelType.fromLabel(value);
            } catch (IllegalArgumentException e) {
                result = defaultValue;
                logger.debug("Powermax alarm binding: panel type not configured correctly");
            }
        } else {
            result = defaultValue;
        }
        return result;
    }
}
