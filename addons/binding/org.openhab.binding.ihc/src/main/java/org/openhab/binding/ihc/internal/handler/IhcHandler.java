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
package org.openhab.binding.ihc.internal.handler;

import static org.openhab.binding.ihc.internal.IhcBindingConstants.*;

import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.ihc.internal.ButtonPressDurationDetector;
import org.openhab.binding.ihc.internal.ChannelUtils;
import org.openhab.binding.ihc.internal.EnumDictionary;
import org.openhab.binding.ihc.internal.SignalLevelConverter;
import org.openhab.binding.ihc.internal.config.ChannelParams;
import org.openhab.binding.ihc.internal.config.IhcConfiguration;
import org.openhab.binding.ihc.internal.converters.Converter;
import org.openhab.binding.ihc.internal.converters.ConverterAdditionalInfo;
import org.openhab.binding.ihc.internal.converters.ConverterFactory;
import org.openhab.binding.ihc.internal.ws.IhcClient;
import org.openhab.binding.ihc.internal.ws.IhcClient.ConnectionState;
import org.openhab.binding.ihc.internal.ws.IhcEventListener;
import org.openhab.binding.ihc.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.internal.ws.datatypes.WSRFDevice;
import org.openhab.binding.ihc.internal.ws.datatypes.WSSystemInfo;
import org.openhab.binding.ihc.internal.ws.datatypes.WSTimeManagerSettings;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.projectfile.IhcEnumValue;
import org.openhab.binding.ihc.internal.ws.projectfile.ProjectFileUtils;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The {@link IhcHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcHandler extends BaseThingHandler implements IhcEventListener {
    private final Logger logger = LoggerFactory.getLogger(IhcHandler.class);

    /** Maximum pulse width in milliseconds. */
    private static final int MAX_PULSE_WIDTH_IN_MS = 4000;

    /** Maximum long press time in milliseconds. */
    private static final int MAX_LONG_PRESS_IN_MS = 5000;

    /** Name of the local IHC / ELKO project file */
    private static final String LOCAL_IHC_PROJECT_FILE_NAME_TEMPLATE = "ihc-project-file-%s.xml";

    /** Holds runtime notification reorder timeout in milliseconds */
    private static final int NOTIFICATIONS_REORDER_WAIT_TIME = 1000;

    /** IHC / ELKO LS Controller client */
    private IhcClient ihc;

    /**
     * Reminder to slow down resource value notification ordering from
     * controller.
     */
    private ScheduledFuture<?> notificationsRequestReminder;

    /** Holds local IHC / ELKO project file */
    private Document projectFile;

    /**
     * Store current state of the controller, use to recognize when controller
     * state is changed
     */
    private String controllerState = "";

    private IhcConfiguration conf;
    private final Set<Integer> linkedResourceIds = Collections.synchronizedSet(new HashSet<>());
    private Map<Integer, LocalDateTime> lastUpdate = new HashMap<>();
    private EnumDictionary enumDictionary;

    private boolean connecting = false;
    private boolean reconnectRequest = false;
    private boolean valueNotificationRequest = false;

    private ScheduledFuture<?> controlJob;
    private ScheduledFuture<?> pollingJobRf;

    private Map<String, ScheduledFuture<?>> longPressFutures = new HashMap<>();

    public IhcHandler(Thing thing) {
        super(thing);
    }

    protected boolean isValueNotificationRequestActivated() {
        synchronized (this) {
            return valueNotificationRequest;
        }
    }

    protected void setValueNotificationRequest(boolean valueNotificationRequest) {
        synchronized (this) {
            this.valueNotificationRequest = valueNotificationRequest;
        }
    }

    protected boolean isReconnectRequestActivated() {
        synchronized (this) {
            return reconnectRequest;
        }
    }

    protected void setReconnectRequest(boolean reconnect) {
        synchronized (this) {
            this.reconnectRequest = reconnect;
        }
    }

    protected boolean isConnecting() {
        synchronized (this) {
            return connecting;
        }
    }

    protected void setConnectingState(boolean value) {
        synchronized (this) {
            this.connecting = value;
        }
    }

    private String getFilePathInUserDataFolder(String fileName) {
        String progArg = System.getProperty("smarthome.userdata");
        if (progArg != null) {
            return progArg + File.separator + fileName;
        }
        return fileName;
    }

    @Override
    public void initialize() {
        conf = getConfigAs(IhcConfiguration.class);
        logger.debug("Using configuration: {}", conf);

        linkedResourceIds.clear();
        linkedResourceIds.addAll(getAllLinkedChannelsResourceIds());
        logger.debug("Linked resources {}: {}", linkedResourceIds.size(), linkedResourceIds);

        if (controlJob == null || controlJob.isCancelled()) {
            logger.debug("Start control task, interval={}sec", 1);
            controlJob = scheduler.scheduleWithFixedDelay(this::reconnectCheck, 0, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Stopping thing");
        if (controlJob != null && !controlJob.isCancelled()) {
            controlJob.cancel(true);
            controlJob = null;
        }
        disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (ihc == null) {
            logger.warn("Connection is not initialized, abort resource value update for channel '{}'!", channelUID);
            return;
        }

        if (ihc.getConnectionState() != ConnectionState.CONNECTED) {
            logger.warn("Connection to controller is not open, abort resource value update for channel '{}'!",
                    channelUID);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_CONTROLLER_STATE:
                if (command.equals(RefreshType.REFRESH)) {
                    updateControllerStateChannel();
                }
                break;

            case CHANNEL_CONTROLLER_UPTIME:
                if (command.equals(RefreshType.REFRESH)) {
                    updateControllerInformationChannels();
                }
                break;

            case CHANNEL_CONTROLLER_TIME:
                if (command.equals(RefreshType.REFRESH)) {
                    updateControllerTimeChannels();
                }
                break;

            default:
                if (command.equals(RefreshType.REFRESH)) {
                    refreshChannel(channelUID);
                } else {
                    updateResourceChannel(channelUID, command);
                }
                break;
        }
    }

    private void refreshChannel(ChannelUID channelUID) {
        logger.debug("REFRESH channel {}", channelUID);
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel != null) {
            try {
                ChannelParams params = new ChannelParams(channel);
                logger.debug("Channel params: {}", params);
                if (params.isDirectionWriteOnly()) {
                    logger.warn("Write only channel, skip refresh command to {}", channelUID);
                    return;
                }
                WSResourceValue value = ihc.resourceQuery(params.getResourceId());
                resourceValueUpdateReceived(value);
            } catch (IhcExecption e) {
                logger.warn("Can't update channel '{}' value, reason: {}", channelUID, e.getMessage(), e);
            } catch (ConversionException e) {
                logger.warn("Channel param error, reason: {}.", e.getMessage(), e);
            }
        }
    }

    private void updateControllerStateChannel() {
        try {
            String state = ihc.getControllerState().getState();
            String value;

            switch (state) {
                case IhcClient.CONTROLLER_STATE_INITIALIZE:
                    value = "Initialize";
                    break;
                case IhcClient.CONTROLLER_STATE_READY:
                    value = "Ready";
                    break;
                default:
                    value = "Unknown state: " + state;
            }

            updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTROLLER_STATE), new StringType(value));
        } catch (IhcExecption e) {
            logger.warn("Controller state information fetch failed, reason: {}", e.getMessage(), e);
        }
    }

    private void updateControllerProperties() {
        try {
            WSSystemInfo systemInfo = ihc.getSystemInfo();
            logger.debug("Controller information: {}", systemInfo);
            WSProjectInfo projectInfo = ihc.getProjectInfo();
            logger.debug("Project information: {}", projectInfo);

            Map<String, String> properties = editProperties();
            properties.put(PROPERTY_MANUFACTURER, systemInfo.getBrand());
            properties.put(PROPERTY_SERIALNUMBER, systemInfo.getSerialNumber());
            properties.put(PROPERTY_SW_VERSION, systemInfo.getVersion());
            properties.put(PROPERTY_FW_VERSION, systemInfo.getHwRevision());
            properties.put(PROPERTY_APP_WITHOUT_VIEWER, Boolean.toString(systemInfo.getApplicationIsWithoutViewer()));
            properties.put(PROPERTY_SW_DATE,
                    systemInfo.getSwDate().withZoneSameInstant(ZoneId.systemDefault()).toString());
            properties.put(PROPERTY_PRODUCTION_DATE, systemInfo.getProductionDate());
            if (!systemInfo.getDatalineVersion().isEmpty()) {
                properties.put(PROPERTY_DATALINE_VERSION, systemInfo.getDatalineVersion());
            }
            if (!systemInfo.getRfModuleSerialNumber().isEmpty()) {
                properties.put(PROPERTY_RF_MODULE_SERIALNUMBER, systemInfo.getRfModuleSerialNumber());
            }
            if (!systemInfo.getRfModuleSoftwareVersion().isEmpty()) {
                properties.put(PROPERTY_RF_MODULE_VERSION, systemInfo.getRfModuleSoftwareVersion());
            }
            properties.put(PROPERTY_PROJECT_DATE,
                    projectInfo.getLastmodified().getAsLocalDateTime().atZone(ZoneId.systemDefault()).toString());
            properties.put(PROPERTY_PROJECT_NUMBER, projectInfo.getProjectNumber());
            updateProperties(properties);
        } catch (IhcExecption e) {
            logger.warn("Controller information fetch failed, reason:  {}", e.getMessage(), e);
        }
    }

    private void updateControllerInformationChannels() {
        try {
            WSSystemInfo systemInfo = ihc.getSystemInfo();
            logger.debug("Controller information: {}", systemInfo);

            updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTROLLER_UPTIME),
                    new DecimalType((double) systemInfo.getUptime() / 1000));
        } catch (IhcExecption e) {
            logger.warn("Controller uptime information fetch failed, reason: {}.", e.getMessage(), e);
        }
    }

    private void updateControllerTimeChannels() {
        try {
            WSTimeManagerSettings timeSettings = ihc.getTimeSettings();
            logger.debug("Controller time settings: {}", timeSettings);

            ZonedDateTime time = timeSettings.getTimeAndDateInUTC().getAsZonedDateTime(ZoneId.of("Z"))
                    .withZoneSameInstant(ZoneId.systemDefault());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTROLLER_TIME), new DateTimeType(time));
        } catch (IhcExecption e) {
            logger.warn("Controller uptime information fetch failed, reason: {}.", e.getMessage(), e);
        }
    }

    private void updateResourceChannel(ChannelUID channelUID, Command command) {
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel != null) {
            try {
                ChannelParams params = new ChannelParams(channel);
                logger.debug("Channel params: {}", params);
                if (params.isDirectionReadOnly()) {
                    logger.debug("Read only channel, skip the update to {}", channelUID);
                    return;
                }
                updateChannel(channelUID, params, command);
            } catch (IhcExecption e) {
                logger.error("Can't update channel '{}' value, cause ", channelUID, e.getMessage(), e);
            } catch (ConversionException e) {
                logger.debug("Conversion error for channel {}, reason: {}.", channelUID, e.getMessage(), e);
            }
        }
    }

    private void updateChannel(ChannelUID channelUID, ChannelParams params, Command command)
            throws IhcExecption, ConversionException {
        if (params.getCommandToReact() != null) {
            if (command.toString().equals(params.getCommandToReact())) {
                logger.debug("Command '{}' equal to channel reaction parameter '{}', execute it", command,
                        params.getCommandToReact());
            } else {
                logger.debug("Command '{}' doesn't equal to reaction trigger parameter '{}', skip it", command,
                        params.getCommandToReact());
                return;
            }
        }
        WSResourceValue value = ihc.getResourceValueInformation(params.getResourceId());
        if (value != null) {
            if (params.getPulseWidth() != null) {
                sendPulseCommand(channelUID, params, value, Math.min(params.getPulseWidth(), MAX_PULSE_WIDTH_IN_MS));
            } else {
                sendNormalCommand(channelUID, params, command, value);
            }
        }
    }

    private void sendNormalCommand(ChannelUID channelUID, ChannelParams params, Command command, WSResourceValue value)
            throws IhcExecption, ConversionException {
        logger.debug("Send command '{}' to resource '{}'", command, value.resourceID);
        ConverterAdditionalInfo converterAdditionalInfo = new ConverterAdditionalInfo(getEnumValues(value),
                params.isInverted(), getCommandLevels(params));
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(value.getClass(),
                command.getClass());
        if (converter != null) {
            WSResourceValue val = converter.convertFromOHType(command, value, converterAdditionalInfo);
            logger.debug("Update resource value (inverted output={}): {}", params.isInverted(), val);
            if (!updateResource(val)) {
                logger.warn("Channel {} update to resource '{}' failed.", channelUID, val);
            }
        } else {
            logger.debug("No converter implemented for {} <-> {}", value.getClass(), command.getClass());
        }
    }

    private ArrayList<IhcEnumValue> getEnumValues(WSResourceValue value) {
        if (value instanceof WSEnumValue) {
            return enumDictionary.getEnumValues(((WSEnumValue) value).definitionTypeID);
        }
        return null;
    }

    private void sendPulseCommand(ChannelUID channelUID, ChannelParams params, WSResourceValue value,
            Integer pulseWidth) throws IhcExecption, ConversionException {
        logger.debug("Send {}ms pulse to resource: {}", pulseWidth, value.resourceID);
        logger.debug("Channel params: {}", params);
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(value.getClass(),
                OnOffType.class);

        if (converter != null) {
            ConverterAdditionalInfo converterAdditionalInfo = new ConverterAdditionalInfo(null, params.isInverted(),
                    getCommandLevels(params));

            WSResourceValue valOn = converter.convertFromOHType(OnOffType.ON, value, converterAdditionalInfo);
            WSResourceValue valOff = converter.convertFromOHType(OnOffType.OFF, value, converterAdditionalInfo);

            // set resource to ON
            logger.debug("Update resource value (inverted output={}): {}", params.isInverted(), valOn);
            if (updateResource(valOn)) {
                scheduler.submit(() -> {
                    // sleep a while
                    try {
                        logger.debug("Sleeping: {}ms", pulseWidth);
                        Thread.sleep(pulseWidth);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                    // set resource back to OFF

                    logger.debug("Update resource value (inverted output={}): {}", params.isInverted(), valOff);
                    try {
                        if (!updateResource(valOff)) {
                            logger.warn("Channel {} update to resource '{}' failed.", channelUID, valOff);
                        }
                    } catch (IhcExecption e) {
                        logger.error("Can't update channel '{}' value, cause ", channelUID, e.getMessage(), e);
                    }
                });
            } else {
                logger.warn("Channel {} update failed.", channelUID);
            }
        } else {
            logger.debug("No converter implemented for {} <-> {}", value.getClass(), OnOffType.class);
        }
    }

    /**
     * Update resource value to IHC controller.
     */
    private boolean updateResource(WSResourceValue value) throws IhcExecption {
        boolean result = false;
        try {
            result = ihc.resourceUpdate(value);
        } catch (IhcExecption e) {
            logger.warn("Value could not be updated - retrying one time: {}.", e.getMessage(), e);
            result = ihc.resourceUpdate(value);
        }
        return result;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);

        switch (channelUID.getId()) {
            case CHANNEL_CONTROLLER_STATE:
                updateControllerStateChannel();
                break;

            case CHANNEL_CONTROLLER_UPTIME:
                updateControllerInformationChannels();
                break;

            case CHANNEL_CONTROLLER_TIME:
                updateControllerTimeChannels();
                break;

            default:
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    try {
                        ChannelParams params = new ChannelParams(channel);
                        if (params.getResourceId() != null) {
                            if (!linkedResourceIds.contains(params.getResourceId())) {
                                logger.debug("New channel '{}' found, resource id '{}'", channelUID.getAsString(),
                                        params.getResourceId());
                                linkedResourceIds.add(params.getResourceId());
                                updateNotificationsRequestReminder();
                            }
                        }
                    } catch (ConversionException e) {
                        logger.warn("Channel param error, reason: {}.", e.getMessage(), e);
                    }
                }
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.debug("channelUnlinked: {}", channelUID);

        switch (channelUID.getId()) {
            case CHANNEL_CONTROLLER_STATE:
            case CHANNEL_CONTROLLER_UPTIME:
            case CHANNEL_CONTROLLER_TIME:
                break;

            default:
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    try {
                        ChannelParams params = new ChannelParams(channel);
                        if (params.getResourceId() != null) {
                            linkedResourceIds.removeIf(c -> c.equals(params.getResourceId()));
                            updateNotificationsRequestReminder();
                        }
                    } catch (ConversionException e) {
                        logger.warn("Channel param error, reason: {}.", e.getMessage(), e);
                    }
                }
        }
    }

    /**
     * Initialize IHC client and open connection to IHC / ELKO LS controller.
     *
     */
    private void connect() throws IhcExecption {
        try {
            setConnectingState(true);
            logger.debug("Connecting to IHC / ELKO LS controller [hostname='{}', username='{}'].", conf.hostname,
                    conf.username);
            ihc = new IhcClient(conf.hostname, conf.username, conf.password, conf.timeout);
            ihc.openConnection();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "Initializing communication to the IHC / ELKO controller");
            loadProject();
            createChannels();
            updateControllerProperties();
            updateControllerStateChannel();
            updateControllerInformationChannels();
            updateControllerTimeChannels();
            ihc.addEventListener(this);
            ihc.startControllerEventListeners();
            updateNotificationsRequestReminder();
            startRFPolling();
            updateStatus(ThingStatus.ONLINE);
        } finally {
            setConnectingState(false);
        }
    }

    private void loadProject() throws IhcExecption {
        if (conf.loadProjectFile) {
            String fileName = String.format(LOCAL_IHC_PROJECT_FILE_NAME_TEMPLATE, thing.getUID().getId());
            String filePath = getFilePathInUserDataFolder(fileName);
            boolean loadProject = false;

            if (projectFile == null) {
                // try first load project file from local cache file.
                try {
                    projectFile = ProjectFileUtils.readFromFile(filePath);
                } catch (IhcExecption e) {
                    logger.debug("Error occured when read project file from file '{}', reason {}", filePath,
                            e.getMessage(), e);
                    loadProject = true;
                }
            }

            if (!ProjectFileUtils.projectEqualsToControllerProject(projectFile, ihc.getProjectInfo())) {
                logger.debug(
                        "Local project file is not same as in the controller, reload project file from controller!");
                loadProject = true;
            }

            if (loadProject) {
                logger.debug("Loading IHC /ELKO LS project file from controller...");
                byte[] data = ihc.getProjectFileFromController();
                logger.debug("Saving project file to local file '{}'", filePath);
                try {
                    ProjectFileUtils.saveToFile(filePath, data);
                } catch (IhcExecption e) {
                    logger.warn("Error occured when trying to write data to file '{}', reason {}", filePath,
                            e.getMessage(), e);
                }
                projectFile = ProjectFileUtils.converteBytesToDocument(data);
            }
        }

        enumDictionary = new EnumDictionary(ProjectFileUtils.parseEnums(projectFile));
    }

    private void createChannels() {
        if (conf.loadProjectFile && conf.createChannelsAutomatically) {
            logger.debug("Creating channels");
            List<Channel> thingChannels = new ArrayList<>();
            thingChannels.addAll(getThing().getChannels());
            ChannelUtils.addControllerChannels(getThing(), thingChannels);
            ChannelUtils.addChannelsFromProjectFile(getThing(), projectFile, thingChannels);
            printChannels(thingChannels);
            updateThing(editThing().withChannels(thingChannels).build());
        } else {
            logger.debug("Automatic channel creation disabled");
        }
    }

    private void printChannels(List<Channel> thingChannels) {
        if (logger.isDebugEnabled()) {
            thingChannels.forEach(channel -> {
                if (channel != null) {
                    String resourceId;
                    try {
                        Object id = channel.getConfiguration().get(PARAM_RESOURCE_ID);
                        resourceId = id != null ? "0x" + Integer.toHexString(((BigDecimal) id).intValue()) : "";
                    } catch (IllegalArgumentException e) {
                        resourceId = "";
                    }

                    String channelType = channel.getAcceptedItemType() != null ? channel.getAcceptedItemType() : "";
                    String channelLabel = channel.getLabel() != null ? channel.getLabel() : "";

                    logger.debug("Channel: {}", String.format("%-55s | %-10s | %-10s | %s", channel.getUID(),
                            resourceId, channelType, channelLabel));
                }
            });
        }
    }

    private void startRFPolling() {
        if (pollingJobRf == null || pollingJobRf.isCancelled()) {
            logger.debug("Start RF device refresh task, interval={}sec", 60);
            pollingJobRf = scheduler.scheduleWithFixedDelay(this::updateRfDeviceStates, 10, 60, TimeUnit.SECONDS);
        }
    }

    /**
     * Disconnect connection to IHC / ELKO LS controller.
     *
     */
    private void disconnect() {
        cancelAllLongPressTasks();
        if (pollingJobRf != null && !pollingJobRf.isCancelled()) {
            pollingJobRf.cancel(true);
            pollingJobRf = null;
        }
        if (ihc != null) {
            try {
                ihc.removeEventListener(this);
                ihc.closeConnection();
                ihc = null;
            } catch (IhcExecption e) {
                logger.warn("Couldn't close connection to IHC controller", e);
            }
        }
    }

    @Override
    public void errorOccured(IhcExecption e) {
        logger.warn("Error occurred on communication to IHC controller: {}", e.getMessage(), e);
        logger.debug("Reconnection request");
        setReconnectRequest(true);
    }

    @Override
    public void statusUpdateReceived(WSControllerState newState) {
        logger.debug("Controller state: {}", newState.getState());

        if (!controllerState.equals(newState.getState())) {
            logger.debug("Controller state change detected ({} -> {})", controllerState, newState.getState());

            switch (newState.getState()) {
                case IhcClient.CONTROLLER_STATE_INITIALIZE:
                    logger.info("Controller state changed to initializing state, waiting for ready state");
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTROLLER_STATE),
                            new StringType("initialize"));
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Controller is in initializing state");
                    break;
                case IhcClient.CONTROLLER_STATE_READY:
                    logger.info("Controller state changed to ready state");
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTROLLER_STATE), new StringType("ready"));
                    updateStatus(ThingStatus.ONLINE);
                    break;
                default:
            }

            if (controllerState.equals(IhcClient.CONTROLLER_STATE_INITIALIZE)
                    && newState.getState().equals(IhcClient.CONTROLLER_STATE_READY)) {
                logger.debug("Reconnection request");
                projectFile = null;
                setReconnectRequest(true);
            }
        }

        controllerState = newState.getState();
    }

    @Override
    public void resourceValueUpdateReceived(WSResourceValue value) {
        logger.debug("resourceValueUpdateReceived: {}", value);

        thing.getChannels().forEach(channel -> {
            try {
                ChannelParams params = new ChannelParams(channel);
                if (params.getResourceId() != null && params.getResourceId().intValue() == value.resourceID) {
                    updateChannelState(channel, params, value);
                }
            } catch (ConversionException e) {
                logger.warn("Channel param error, reason: {}.", e.getMessage(), e);
            } catch (RuntimeException e) {
                logger.warn("Unknown error occured, reason: {}.", e.getMessage(), e);
            }
        });

        checkPotentialButtonPresses(value);
    }

    private void updateChannelState(Channel channel, ChannelParams params, WSResourceValue value) {
        if (params.isDirectionWriteOnly()) {
            logger.debug("Write only channel, skip update to {}", channel.getUID());
        } else {
            if (params.getChannelTypeId() != null) {
                switch (params.getChannelTypeId()) {
                    case CHANNEL_TYPE_PUSH_BUTTON_TRIGGER:
                        break;

                    default:
                        try {
                            logger.debug("Update channel '{}' state, channel params: {}", channel.getUID(), params);
                            Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance()
                                    .getConverter(value.getClass(), channel.getAcceptedItemType());
                            if (converter != null) {
                                State state = (State) converter.convertFromResourceValue(value,
                                        new ConverterAdditionalInfo(null, params.isInverted(),
                                                getCommandLevels(params)));
                                updateState(channel.getUID(), state);
                            } else {
                                logger.debug("No converter implemented for {} <-> {}", value.getClass(),
                                        channel.getAcceptedItemType());
                            }
                        } catch (ConversionException e) {
                            logger.debug("Can't convert resource value '{}' to item type {}, reason: {}.", value,
                                    channel.getAcceptedItemType(), e.getMessage(), e);
                        }
                }
            }
        }
    }

    private void checkPotentialButtonPresses(WSResourceValue value) {
        if (value instanceof WSBooleanValue) {
            if (((WSBooleanValue) value).value) {
                // potential button press
                lastUpdate.put(value.resourceID, LocalDateTime.now());
                updateTriggers(value.resourceID, Duration.ZERO);
            } else {
                // potential button release
                LocalDateTime lastUpdateTime = lastUpdate.get(value.resourceID);
                if (lastUpdateTime != null) {
                    Duration duration = Duration.between(lastUpdateTime, LocalDateTime.now());
                    logger.debug("Time between uddates: {}", duration);
                    updateTriggers(value.resourceID, duration);
                }
            }
        }
    }

    private void updateTriggers(int resourceId, Duration duration) {
        thing.getChannels().forEach(channel -> {
            try {
                ChannelParams params = new ChannelParams(channel);
                if (params.getResourceId() != null && params.getResourceId().intValue() == resourceId) {
                    if (params.getChannelTypeId() != null) {
                        switch (params.getChannelTypeId()) {
                            case CHANNEL_TYPE_PUSH_BUTTON_TRIGGER:
                                logger.debug("Update trigger channel '{}', channel params: {}",
                                        channel.getUID().getId(), params);
                                if (duration.toMillis() == 0) {
                                    triggerChannel(channel.getUID().getId(), EVENT_PRESSED);
                                    createLongPressTask(channel.getUID().getId(), params.getLongPressTime());
                                } else {
                                    cancelLongPressTask(channel.getUID().getId());
                                    triggerChannel(channel.getUID().getId(), EVENT_RELEASED);
                                    triggerChannel(channel.getUID().getId(), String.valueOf(duration.toMillis()));
                                    ButtonPressDurationDetector button = new ButtonPressDurationDetector(duration,
                                            params.getLongPressTime(), MAX_LONG_PRESS_IN_MS);
                                    logger.debug("resourceId={}, ButtonPressDurationDetector={}", resourceId, button);
                                    if (button.isShortPress()) {
                                        triggerChannel(channel.getUID().getId(), EVENT_SHORT_PRESS);
                                    }
                                    break;
                                }
                        }
                    }
                }
            } catch (ConversionException e) {
                logger.warn("Channel param error, reason:  {}", e.getMessage(), e);
            }
        });
    }

    private void createLongPressTask(String channelId, long longPressTimeInMs) {
        if (longPressFutures.containsKey(channelId)) {
            cancelLongPressTask(channelId);
        }
        logger.debug("Create long press task for channel '{}'", channelId);
        longPressFutures.put(channelId, scheduler.schedule(() -> triggerChannel(channelId, EVENT_LONG_PRESS),
                longPressTimeInMs, TimeUnit.MILLISECONDS));
    }

    private void cancelLongPressTask(String channelId) {
        if (longPressFutures.containsKey(channelId)) {
            logger.debug("Cancel long press task for channel '{}'", channelId);
            longPressFutures.get(channelId).cancel(false);
            longPressFutures.remove(channelId);
        }
    }

    private void cancelAllLongPressTasks() {
        longPressFutures.entrySet().parallelStream().forEach(e -> e.getValue().cancel(true));
        longPressFutures.clear();
    }

    private void updateRfDeviceStates() {
        if (ihc != null) {
            if (ihc.getConnectionState() != ConnectionState.CONNECTED) {
                logger.debug("Controller is connecting, abort subscribe");
                return;
            }

            logger.debug("Update RF device data");
            try {
                List<WSRFDevice> devs = ihc.getDetectedRFDevices();
                logger.debug("RF data: {}", devs);

                devs.forEach(dev -> {
                    thing.getChannels().forEach(channel -> {
                        try {
                            ChannelParams params = new ChannelParams(channel);
                            if (params.getSerialNumber() != null
                                    && params.getSerialNumber().longValue() == dev.getSerialNumber()) {
                                String channelId = channel.getUID().getId();
                                if (params.getChannelTypeId() != null) {
                                    switch (params.getChannelTypeId()) {
                                        case CHANNEL_TYPE_RF_LOW_BATTERY:
                                            updateState(channelId,
                                                    dev.getBatteryLevel() == 1 ? OnOffType.OFF : OnOffType.ON);
                                            break;
                                        case CHANNEL_TYPE_RF_SIGNAL_STRENGTH:
                                            int signalLevel = new SignalLevelConverter(dev.getSignalStrength())
                                                    .getSystemWideSignalLevel();
                                            updateState(channelId, new StringType(String.valueOf(signalLevel)));
                                            break;
                                    }
                                }
                            }
                        } catch (ConversionException e) {
                            logger.warn("Channel param error, reason:  {}", e.getMessage(), e);
                        }
                    });
                });
            } catch (IhcExecption e) {
                logger.debug("Error occured when fetching RF device information, reason: : {} ", e.getMessage(), e);
                return;
            }
        }
    }

    private void reconnectCheck() {
        if (ihc == null || isReconnectRequestActivated()) {
            try {
                if (ihc != null) {
                    disconnect();
                }
                connect();
                setReconnectRequest(false);
            } catch (IhcExecption e) {
                logger.debug("Can't open connection to controller", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                setReconnectRequest(true);
                return;
            }
        }

        if (isValueNotificationRequestActivated() && !isConnecting()) {
            try {
                enableResourceValueNotifications();
            } catch (IhcExecption e) {
                logger.warn("Can't enable resource value notifications from controller. ", e);
            }
        }
    }

    private Set<Integer> getAllLinkedChannelsResourceIds() {
        Set<Integer> resourceIds = Collections.synchronizedSet(new HashSet<>());
        resourceIds.addAll(this.getThing().getChannels().stream().filter(c -> isLinked(c.getUID())).map(c -> {
            try {
                ChannelParams params = new ChannelParams(c);
                logger.debug("Linked channel '{}' found, resource id '{}'", c.getUID().getAsString(),
                        params.getResourceId());
                return params.getResourceId();
            } catch (ConversionException e) {
                logger.warn("Channel param error, reason: {}.", e.getMessage(), e);
                return null;
            }
        }).filter(c -> c != null && c != 0).collect(Collectors.toSet()));
        return resourceIds;
    }

    /**
     * Order resource value notifications from IHC controller.
     */
    private void enableResourceValueNotifications() throws IhcExecption {
        logger.debug("Subscribe resource runtime value notifications");

        if (ihc != null) {
            if (ihc.getConnectionState() != ConnectionState.CONNECTED) {
                logger.debug("Controller is connecting, abort subscribe");
                return;
            }
            setValueNotificationRequest(false);
            Set<Integer> resourceIds = ChannelUtils.getAllTriggerChannelsResourceIds(getThing());
            logger.debug("Enable runtime notfications for {} trigger(s)", resourceIds.size());
            logger.debug("Enable runtime notfications for {} channel(s)", linkedResourceIds.size());
            resourceIds.addAll(linkedResourceIds);
            resourceIds.addAll(getAllLinkedChannelsResourceIds());
            logger.debug("Enable runtime notfications for {} resources: {}", resourceIds.size(), resourceIds);
            if (resourceIds.size() > 0) {
                try {
                    ihc.enableRuntimeValueNotifications(resourceIds);
                } catch (IhcExecption e) {
                    logger.debug("Reconnection request");
                    setReconnectRequest(true);
                }
            }
        } else {
            logger.warn("Controller is not initialized!");
            logger.debug("Reconnection request");
            setReconnectRequest(true);
        }
    }

    private synchronized void updateNotificationsRequestReminder() {
        if (notificationsRequestReminder != null) {
            notificationsRequestReminder.cancel(false);
        }

        logger.debug("Rechedule resource runtime value notifications order by {}ms", NOTIFICATIONS_REORDER_WAIT_TIME);
        notificationsRequestReminder = scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                logger.debug("Delayed resource value notifications request is now enabled");
                setValueNotificationRequest(true);
            }
        }, NOTIFICATIONS_REORDER_WAIT_TIME, TimeUnit.MILLISECONDS);
    }

    private Map<Command, Object> getCommandLevels(ChannelParams params) {
        if (params.getOnLevel() != null) {
            Map<Command, Object> commandLevels = new HashMap<>();
            commandLevels.put(OnOffType.ON, params.getOnLevel());
            return Collections.unmodifiableMap(commandLevels);
        }
        return null;
    }
}
