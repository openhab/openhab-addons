/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.handler;

import static org.openhab.binding.powermax.PowermaxBindingConstants.*;

import java.util.EventObject;
import java.util.HashMap;
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
import org.openhab.binding.powermax.internal.PowermaxPanelSettingsListener;
import org.openhab.binding.powermax.internal.config.PowermaxIpConfiguration;
import org.openhab.binding.powermax.internal.config.PowermaxSerialConfiguration;
import org.openhab.binding.powermax.internal.connector.PowermaxEvent;
import org.openhab.binding.powermax.internal.connector.PowermaxEventListener;
import org.openhab.binding.powermax.internal.message.PowermaxBaseMessage;
import org.openhab.binding.powermax.internal.message.PowermaxCommDriver;
import org.openhab.binding.powermax.internal.message.PowermaxInfoMessage;
import org.openhab.binding.powermax.internal.message.PowermaxPowerlinkMessage;
import org.openhab.binding.powermax.internal.message.PowermaxReceiveType;
import org.openhab.binding.powermax.internal.message.PowermaxSendType;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxPanelType;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PowermaxBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxBridgeHandler extends BaseBridgeHandler implements PowermaxEventListener {

    private final Logger logger = LoggerFactory.getLogger(PowermaxBridgeHandler.class);

    private static final int ONE_MINUTE = 60000;

    /** Default delay in milliseconds to reset a motion detection */
    private static final int DEFAULT_MOTION_OFF_DELAY = 3 * ONE_MINUTE;

    private static final int NB_EVENT_LOG = 10;

    private static final PowermaxPanelType DEFAULT_PANEL_TYPE = PowermaxPanelType.POWERMAX_PRO;

    private static final int JOB_REPEAT = 20;

    private static final int MAX_DOWNLOAD_TRY = 3;

    private ScheduledFuture<?> globalJob;

    private List<PowermaxPanelSettingsListener> listeners = new CopyOnWriteArrayList<>();

    /** The serial port to use for connecting to the Powermax alarm system */
    private String serialPort;

    /** The IP address and TCP port to use for connecting to the Powermax alarm system */
    private String ipAddress;
    private int tcpPort = 0;

    /** The delay in milliseconds to reset a motion detection */
    private int motionOffDelay;

    /** Enable or disable arming the Powermax alarm system from openHAB */
    private boolean allowArming;

    /** Enable or disable disarming the Powermax alarm system from openHAB */
    private boolean allowDisarming;

    /** The PIN code to use for arming/disarming the Powermax alarm system from openHAB */
    private String pinCode;

    /** Force the standard mode rather than trying using the Powerlink mode */
    private boolean forceStandardMode = false;

    /** Panel type used when in standard mode */
    private PowermaxPanelType panelType = PowermaxPanelType.POWERMAX_PRO;

    /** Automatic sync time */
    private boolean autoSyncTime = false;

    /** The object to store the current state of the Powermax alarm system */
    private PowermaxState currentState;

    /** Boolean indicating whether or not connection is established with the alarm system */
    private boolean connected = false;

    private int remainingDownloadTry;

    private Runnable globalRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                logger.debug("Powermax job...");

                //
                // Off items linked to motion sensors after the delay defined by
                // the variable motionOffDelay
                //

                long now = System.currentTimeMillis();
                PowermaxPanelSettings settings = PowermaxPanelSettings.getThePanelSettings();
                PowermaxState updateState = null;
                if ((currentState != null) && (settings != null)) {
                    for (int i = 1; i <= settings.getNbZones(); i++) {
                        if ((settings.getZoneSettings(i) != null)
                                && settings.getZoneSettings(i).getSensorType().equalsIgnoreCase("Motion")
                                && (currentState.isSensorTripped(i) == Boolean.TRUE)
                                && (currentState.getSensorLastTripped(i) != null)
                                && ((now - currentState.getSensorLastTripped(i)) > motionOffDelay)) {
                            if (updateState == null) {
                                updateState = new PowermaxState();
                            }
                            updateState.setSensorTripped(i, false);
                        }
                    }
                }
                if (updateState != null) {
                    updateChannelsFromAlarmState(TRIPPED, updateState);
                    currentState.merge(updateState);
                }

                if (PowermaxCommDriver.getTheCommDriver() != null) {
                    connected = PowermaxCommDriver.getTheCommDriver().isConnected();
                }

                // Check that we receive a keep alive message during the last minute
                if (connected && Boolean.TRUE.equals(currentState.isPowerlinkMode())
                        && (currentState.getLastKeepAlive() != null)
                        && ((now - currentState.getLastKeepAlive()) > ONE_MINUTE)) {
                    // Let Powermax know we are alive
                    PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.RESTORE);
                    currentState.setLastKeepAlive(now);
                }

                //
                // Try to reconnect if disconnected
                //

                if (!connected) {
                    logger.debug("trying to reconnect...");
                    closeConnection();
                    currentState = new PowermaxState();
                    openConnection();
                    if (connected) {
                        updateStatus(ThingStatus.ONLINE);
                        if (forceStandardMode) {
                            currentState.setPowerlinkMode(false);
                            updateChannelsFromAlarmState(MODE, currentState);
                            settings = PowermaxPanelSettings.getThePanelSettings();
                            if (settings.process(false, panelType, null)) {
                                for (PowermaxPanelSettingsListener listener : listeners) {
                                    listener.onPanelSettingsUpdated(settings);
                                }
                            }
                            remainingDownloadTry = 0;
                            updatePropertiesFromPanelSettings();
                            logger.info("Powermax alarm binding: running in Standard mode");
                            PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.ZONESNAME);
                            PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.ZONESTYPE);
                            PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.STATUS);
                        } else {
                            PowermaxCommDriver.getTheCommDriver().startDownload();
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "reconnection failed");
                    }
                } else if ((remainingDownloadTry > 0) && !PowermaxCommDriver.getTheCommDriver().isDownloadRunning()
                        && ((PowermaxCommDriver.getTheCommDriver().getLastTimeDownloadRequested() == null) || ((now
                                - PowermaxCommDriver.getTheCommDriver().getLastTimeDownloadRequested()) >= 45000))) {
                    // We wait at least 45 seconds before each retry to download the panel setup
                    logger.info("Powermax alarm binding: try again downloading setup");
                    PowermaxCommDriver.getTheCommDriver().startDownload();
                }
            } catch (Exception e) {
                logger.warn("Exception in scheduled job: {}", e.getMessage(), e);
            } catch (Error e) {
                logger.warn("Error in scheduled job: {}", e.getMessage(), e);
            } catch (Throwable t) {
                logger.warn("Unexpected error in scheduled job", t);
            }
        }
    };

    public PowermaxBridgeHandler(Bridge thing) {
        super(thing);
    }

    public PowermaxState getCurrentState() {
        return currentState;
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        boolean validConfig = false;
        String errorMsg = "Unexpected thing type " + getThing().getThingTypeUID();

        if (getThing().getThingTypeUID().equals(BRIDGE_TYPE_SERIAL)) {
            PowermaxSerialConfiguration config = getConfigAs(PowermaxSerialConfiguration.class);
            if (StringUtils.isNotBlank(config.serialPort)) {
                validConfig = true;
                serialPort = config.serialPort;

                if (config.motionOffDelay != null) {
                    motionOffDelay = config.motionOffDelay.intValue() * ONE_MINUTE;
                } else {
                    motionOffDelay = DEFAULT_MOTION_OFF_DELAY;
                }

                if (config.allowArming != null) {
                    allowArming = config.allowArming.booleanValue();
                } else {
                    allowArming = false;
                }

                if (config.allowDisarming != null) {
                    allowDisarming = config.allowDisarming.booleanValue();
                } else {
                    allowDisarming = false;
                }

                pinCode = config.pinCode;

                if (config.forceStandardMode != null) {
                    forceStandardMode = config.forceStandardMode.booleanValue();
                } else {
                    forceStandardMode = false;
                }

                if (config.panelType != null) {
                    try {
                        panelType = PowermaxPanelType.fromLabel(config.panelType);
                    } catch (IllegalArgumentException e) {
                        panelType = DEFAULT_PANEL_TYPE;
                        logger.info("Powermax alarm binding: panel type not configured correctly");
                    }
                } else {
                    panelType = DEFAULT_PANEL_TYPE;
                }

                if (config.autoSyncTime != null) {
                    autoSyncTime = config.autoSyncTime.booleanValue();
                } else {
                    autoSyncTime = false;
                }

                PowermaxReceiveType.POWERLINK.setHandlerClass(
                        forceStandardMode ? PowermaxBaseMessage.class : PowermaxPowerlinkMessage.class);
                PowermaxPanelSettings.initPanelSettings(panelType);
            } else {
                errorMsg = "serialPort setting must be defined in thing configuration";
            }
        } else if (getThing().getThingTypeUID().equals(BRIDGE_TYPE_IP)) {
            PowermaxIpConfiguration config = getConfigAs(PowermaxIpConfiguration.class);
            if (StringUtils.isNotBlank(config.ip) && config.tcpPort != null) {
                validConfig = true;
                ipAddress = config.ip;
                tcpPort = config.tcpPort;

                if (config.motionOffDelay != null) {
                    motionOffDelay = config.motionOffDelay.intValue() * ONE_MINUTE;
                } else {
                    motionOffDelay = DEFAULT_MOTION_OFF_DELAY;
                }

                if (config.allowArming != null) {
                    allowArming = config.allowArming.booleanValue();
                } else {
                    allowArming = false;
                }

                if (config.allowDisarming != null) {
                    allowDisarming = config.allowDisarming.booleanValue();
                } else {
                    allowDisarming = false;
                }

                pinCode = config.pinCode;

                if (config.forceStandardMode != null) {
                    forceStandardMode = config.forceStandardMode.booleanValue();
                } else {
                    forceStandardMode = false;
                }

                if (config.panelType != null) {
                    try {
                        panelType = PowermaxPanelType.fromLabel(config.panelType);
                    } catch (IllegalArgumentException e) {
                        panelType = DEFAULT_PANEL_TYPE;
                        logger.info("Powermax alarm binding: panel type not configured correctly");
                    }
                } else {
                    panelType = DEFAULT_PANEL_TYPE;
                }

                if (config.autoSyncTime != null) {
                    autoSyncTime = config.autoSyncTime.booleanValue();
                } else {
                    autoSyncTime = false;
                }

                PowermaxReceiveType.POWERLINK.setHandlerClass(
                        forceStandardMode ? PowermaxBaseMessage.class : PowermaxPowerlinkMessage.class);
                PowermaxPanelSettings.initPanelSettings(panelType);
            } else {
                errorMsg = "ip and port settings must be defined in thing configuration";
            }
        }

        if (validConfig) {
            if (globalJob == null || globalJob.isCancelled()) {
                // Delay the startup in case the handler is restarted immediately
                globalJob = scheduler.scheduleWithFixedDelay(globalRunnable, 10, JOB_REPEAT, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
        closeConnection();
        super.dispose();
    }

    /**
     * Open a TCP or Serial connection to the Powermax Alarm Panel
     */
    private synchronized void openConnection() {
        PowermaxCommDriver.initTheCommDriver(serialPort, ipAddress, tcpPort);
        PowermaxCommDriver comm = PowermaxCommDriver.getTheCommDriver();
        if (comm != null) {
            comm.addEventListener(this);
            connected = comm.open();
            if (serialPort != null) {
                logger.info("Powermax alarm binding: serial connection ({}): {}", serialPort,
                        connected ? "connected" : "disconnected");
            } else if (ipAddress != null) {
                logger.info("Powermax alarm binding: TCP connection (IP {} port {}): {}", ipAddress, tcpPort,
                        connected ? "connected" : "disconnected");
            }
        } else {
            connected = false;
        }
        remainingDownloadTry = MAX_DOWNLOAD_TRY;
        logger.debug("openConnection(): {}", connected ? "connected" : "disconnected");
    }

    /**
     * Close TCP or Serial connection to the Powermax Alarm Panel and remove the Event Listener
     */
    private synchronized void closeConnection() {
        PowermaxCommDriver comm = PowermaxCommDriver.getTheCommDriver();
        if (comm != null) {
            comm.close();
            comm.removeEventListener(this);
        }
        connected = false;
        logger.debug("closeConnection(): disconnected");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} from channel {}", command, channelUID.getId());

        if (command instanceof RefreshType) {
            updateChannelsFromAlarmState(channelUID.getId(), currentState);
        } else {
            if (!connected) {
                logger.debug("Powermax alarm binding not connected. Command is ignored.");
                return;
            }

            switch (channelUID.getId()) {
                case ARM_MODE:
                    if (command instanceof StringType) {
                        armCommand(command.toString());
                    }
                    break;
                case SYSTEM_ARMED:
                    if (command instanceof OnOffType) {
                        armCommand(command.equals(OnOffType.ON) ? "Armed" : "Disarmed");
                    }
                    break;
                case PGM_STATUS:
                    if (command instanceof OnOffType) {
                        x10Command(null, command.toString());
                    }
                    break;
                case UPDATE_EVENT_LOGS:
                    if (command instanceof OnOffType) {
                        downloadEventLog();
                    }
                    break;
                case DOWNLOAD_SETUP:
                    if (command instanceof OnOffType) {
                        downloadSetup();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void armCommand(String armMode) {
        HashMap<String, Boolean> allowedModes = new HashMap<String, Boolean>();
        allowedModes.put("Disarmed", allowDisarming);
        allowedModes.put("Stay", allowArming);
        allowedModes.put("Armed", allowArming);
        allowedModes.put("StayInstant", allowArming);
        allowedModes.put("ArmedInstant", allowArming);
        allowedModes.put("Night", allowArming);
        allowedModes.put("NightInstant", allowArming);

        Boolean allowed = allowedModes.get(armMode);
        if (Boolean.FALSE.equals(allowed)) {
            logger.info("Powermax alarm binding: rejected command {}", armMode);
        } else {
            PowermaxCommDriver.getTheCommDriver().requestArmMode(armMode,
                    currentState.isPowerlinkMode() ? PowermaxPanelSettings.getThePanelSettings().getFirstPinCode()
                            : pinCode);
        }
    }

    public void x10Command(Byte deviceNr, String action) {
        PowermaxCommDriver.getTheCommDriver().sendPGMX10(action, deviceNr);
    }

    public void zoneBypassed(byte zoneNr, boolean bypassed) {
        if (!Boolean.TRUE.equals(currentState.isPowerlinkMode())) {
            logger.info("Powermax alarm binding: Bypass option only supported in Powerlink mode");
        } else if (!PowermaxPanelSettings.getThePanelSettings().isBypassEnabled()) {
            logger.info("Powermax alarm binding: Bypass option not enabled in panel settings");
        } else {
            PowermaxCommDriver.getTheCommDriver().sendZoneBypass(bypassed, zoneNr,
                    PowermaxPanelSettings.getThePanelSettings().getFirstPinCode());
        }
    }

    public void downloadEventLog() {
        PowermaxCommDriver.getTheCommDriver().requestEventLog(
                currentState.isPowerlinkMode() ? PowermaxPanelSettings.getThePanelSettings().getFirstPinCode()
                        : pinCode);
    }

    public void downloadSetup() {
        if (!Boolean.TRUE.equals(currentState.isPowerlinkMode())) {
            logger.info("Powermax alarm binding: download setup only supported in Powerlink mode");
        } else if (PowermaxCommDriver.getTheCommDriver().isDownloadRunning()) {
            logger.info("Powermax alarm binding: download setup not started as one is in progress");
        } else {
            PowermaxCommDriver.getTheCommDriver().startDownload();
            if (currentState.getLastKeepAlive() != null) {
                currentState.setLastKeepAlive(System.currentTimeMillis());
            }
        }
    }

    public String getInfoSetup() {
        return PowermaxPanelSettings.getThePanelSettings().getInfo();
    }

    /**
     * Powermax Alarm incoming message event handler
     *
     * @param event
     */
    @Override
    public void powermaxEventReceived(EventObject event) {
        PowermaxEvent powermaxEvent = (PowermaxEvent) event;
        PowermaxBaseMessage message = powermaxEvent.getPowermaxMessage();

        if (logger.isDebugEnabled()) {
            logger.debug("powermaxEventReceived(): received message {}",
                    (message.getReceiveType() != null) ? message.getReceiveType().toString()
                            : String.format("%02X", message.getCode()));
        }

        if (message instanceof PowermaxInfoMessage) {
            ((PowermaxInfoMessage) message).setAutoSyncTime(autoSyncTime);
        }

        PowermaxState updateState = message.handleMessage();
        if (updateState != null) {
            if (Boolean.TRUE.equals(currentState.isPowerlinkMode())
                    && Boolean.TRUE.equals(updateState.isDownloadSetupRequired())) {
                // After Enrolling Powerlink or if a reset is required
                logger.info("Powermax alarm binding: Reset");
                PowermaxCommDriver.getTheCommDriver().startDownload();
                if (currentState.getLastKeepAlive() != null) {
                    currentState.setLastKeepAlive(System.currentTimeMillis());
                }
            } else if (Boolean.FALSE.equals(currentState.isPowerlinkMode())
                    && (updateState.getLastKeepAlive() != null)) {
                // Were are in standard mode but received a keep alive message
                // so we switch in PowerLink mode
                logger.info("Powermax alarm binding: Switching to Powerlink mode");
                PowermaxCommDriver.getTheCommDriver().startDownload();
            }

            PowermaxPanelSettings settings = PowermaxPanelSettings.getThePanelSettings();

            boolean doProcessSettings = (updateState.isPowerlinkMode() != null);

            for (int i = 1; i <= settings.getNbZones(); i++) {
                if (Boolean.TRUE.equals(updateState.isSensorArmed(i))
                        && Boolean.TRUE.equals(currentState.isSensorBypassed(i))) {
                    updateState.setSensorArmed(i, false);
                }
            }

            updateState.keepOnlyDifferencesWith(currentState);
            updateChannelsFromAlarmState(updateState);
            currentState.merge(updateState);

            if (updateState.getUpdateSettings() != null) {
                settings.updateRawSettings(updateState.getUpdateSettings());
            }
            if (!updateState.getUpdatedZoneNames().isEmpty()) {
                for (Integer zoneIdx : updateState.getUpdatedZoneNames().keySet()) {
                    settings.updateZoneName(zoneIdx, updateState.getUpdatedZoneNames().get(zoneIdx));
                    if (settings.getZoneSettings(zoneIdx) != null) {
                        for (PowermaxPanelSettingsListener listener : listeners) {
                            listener.onZoneSettingsUpdated(zoneIdx, settings.getZoneSettings(zoneIdx));
                        }
                    }
                }
            }
            if (!updateState.getUpdatedZoneInfos().isEmpty()) {
                for (Integer zoneIdx : updateState.getUpdatedZoneInfos().keySet()) {
                    settings.updateZoneInfo(zoneIdx, updateState.getUpdatedZoneInfos().get(zoneIdx));
                }
            }

            if (doProcessSettings) {
                // There is a change of mode (standard or Powerlink)
                if (settings.process(currentState.isPowerlinkMode(), panelType,
                        PowermaxCommDriver.getTheCommDriver().getSyncTimeCheck())) {
                    for (PowermaxPanelSettingsListener listener : listeners) {
                        listener.onPanelSettingsUpdated(settings);
                    }
                    remainingDownloadTry = 0;
                } else {
                    logger.warn("Powermax alarm binding: setup download failed!");
                    // Set all things children of the bridge to OFFLINE
                    for (PowermaxPanelSettingsListener listener : listeners) {
                        listener.onPanelSettingsUpdated(null);
                    }
                    remainingDownloadTry--;
                }
                updatePropertiesFromPanelSettings();
                if (currentState.isPowerlinkMode()) {
                    logger.info("Powermax alarm binding: running in Powerlink mode");
                    PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.RESTORE);
                } else {
                    logger.info("Powermax alarm binding: running in Standard mode");
                    PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.ZONESNAME);
                    PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.ZONESTYPE);
                    PowermaxCommDriver.getTheCommDriver().sendMessage(PowermaxSendType.STATUS);
                }
                PowermaxCommDriver.getTheCommDriver().exitDownload();
            }
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
        PowermaxPanelSettings settings = PowermaxPanelSettings.getThePanelSettings();

        String value;
        boolean update = false;

        Map<String, String> properties = editProperties();

        value = (settings.getPanelType() != null) ? settings.getPanelType().getLabel() : null;
        if (StringUtils.isNotEmpty(value) && ((properties.get(Thing.PROPERTY_MODEL_ID) == null)
                || !properties.get(Thing.PROPERTY_MODEL_ID).equals(value))) {
            update = true;
            properties.put(Thing.PROPERTY_MODEL_ID, value);
        }

        value = settings.getPanelSerial();
        if (StringUtils.isNotEmpty(value) && ((properties.get(Thing.PROPERTY_SERIAL_NUMBER) == null)
                || !properties.get(Thing.PROPERTY_SERIAL_NUMBER).equals(value))) {
            update = true;
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, value);
        }

        value = settings.getPanelEprom();
        if (StringUtils.isNotEmpty(value) && ((properties.get(Thing.PROPERTY_HARDWARE_VERSION) == null)
                || !properties.get(Thing.PROPERTY_HARDWARE_VERSION).equals(value))) {
            update = true;
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, value);
        }

        value = settings.getPanelSoftware();
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

}
