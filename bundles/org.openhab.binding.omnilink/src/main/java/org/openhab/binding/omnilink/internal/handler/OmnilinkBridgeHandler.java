/*
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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.AudioPlayer;
import org.openhab.binding.omnilink.internal.SystemType;
import org.openhab.binding.omnilink.internal.TemperatureFormat;
import org.openhab.binding.omnilink.internal.action.OmnilinkActions;
import org.openhab.binding.omnilink.internal.config.OmnilinkBridgeConfig;
import org.openhab.binding.omnilink.internal.discovery.OmnilinkDiscoveryService;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Connection;
import com.digitaldan.jomnilinkII.DisconnectListener;
import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.EventLogData;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.SecurityCodeValidation;
import com.digitaldan.jomnilinkII.MessageTypes.SystemFeatures;
import com.digitaldan.jomnilinkII.MessageTypes.SystemFormats;
import com.digitaldan.jomnilinkII.MessageTypes.SystemInformation;
import com.digitaldan.jomnilinkII.MessageTypes.SystemStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAccessControlReaderLockStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAreaStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAudioZoneStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAuxSensorStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedThermostatStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedUnitStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedZoneStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.AllOnOffEvent;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.ButtonEvent;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.SwitchPressEvent;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.SystemEvent;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.UPBLinkEvent;
import com.digitaldan.jomnilinkII.NotificationListener;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniNotConnectedException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.google.gson.Gson;

/**
 * The {@link OmnilinkBridgeHandler} defines some methods that are used to
 * interface with an OmniLink Controller. This by extension also defines the
 * OmniLink bridge that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class OmnilinkBridgeHandler extends BaseBridgeHandler implements NotificationListener, DisconnectListener {
    private final Logger logger = LoggerFactory.getLogger(OmnilinkBridgeHandler.class);
    private @Nullable Connection omniConnection = null;
    private @Nullable ScheduledFuture<?> connectJob;
    private @Nullable ScheduledFuture<?> eventPollingJob;
    private Optional<AudioPlayer> audioPlayer = Optional.empty();
    private Optional<SystemType> systemType = Optional.empty();
    private final Gson gson = new Gson();
    private int eventLogNumber = 0;

    public OmnilinkBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(OmnilinkDiscoveryService.class, OmnilinkActions.class);
    }

    public void sendOmnilinkCommand(final int message, final int param1, final int param2)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            getOmniConnection().controllerCommand(message, param1, param2);
        } catch (IOException | OmniNotConnectedException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public SecurityCodeValidation reqSecurityCodeValidation(int area, int digit1, int digit2, int digit3, int digit4)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().reqSecurityCodeValidation(area, digit1, digit2, digit3, digit4);
        } catch (IOException | OmniNotConnectedException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public void activateKeypadEmergency(int area, int emergencyType)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            getOmniConnection().activateKeypadEmergency(area, emergencyType);
        } catch (IOException | OmniNotConnectedException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public SystemInformation reqSystemInformation()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().reqSystemInformation();
        } catch (IOException | OmniNotConnectedException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public SystemFormats reqSystemFormats()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().reqSystemFormats();
        } catch (IOException | OmniNotConnectedException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public void synchronizeControllerTime(ZonedDateTime zdt) {
        boolean inDaylightSavings = zdt.getZone().getRules().isDaylightSavings(zdt.toInstant());
        try {
            getOmniConnection().setTimeCommand(zdt.getYear() - 2000, zdt.getMonthValue(), zdt.getDayOfMonth(),
                    zdt.getDayOfWeek().getValue(), zdt.getHour(), zdt.getMinute(), inDaylightSavings);
            getSystemStatus();
        } catch (IOException | OmniNotConnectedException | OmniInvalidResponseException
                | OmniUnknownMessageTypeException e) {
            logger.debug("Could not send set date time command to OmniLink Controller: {}", e.getMessage());
        }
    }

    private SystemFeatures reqSystemFeatures()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().reqSystemFeatures();
        } catch (IOException | OmniNotConnectedException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            updateChannels();
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_CONSOLE_ENABLE_DISABLE_BEEPER:
                if (command instanceof StringType stringCommand) {
                    try {
                        sendOmnilinkCommand(CommandMessage.CMD_CONSOLE_ENABLE_DISABLE_BEEPER,
                                stringCommand.equals(StringType.valueOf("OFF")) ? 0 : 1, 0);
                        updateState(CHANNEL_CONSOLE_ENABLE_DISABLE_BEEPER, UnDefType.UNDEF);
                    } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                            | BridgeOfflineException e) {
                        logger.debug("Could not send Console command to OmniLink Controller: {}", e.getMessage());
                    }
                } else {
                    logger.debug("Invalid command: {}, must be StringType", command);
                }
                break;
            case CHANNEL_CONSOLE_BEEP:
                if (command instanceof DecimalType decimalCommand) {
                    try {
                        sendOmnilinkCommand(CommandMessage.CMD_CONSOLE_BEEP, decimalCommand.intValue(), 0);
                        updateState(CHANNEL_CONSOLE_BEEP, UnDefType.UNDEF);
                    } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                            | BridgeOfflineException e) {
                        logger.debug("Could not send Console command to OmniLink Controller: {}", e.getMessage());
                    }
                } else {
                    logger.debug("Invalid command: {}, must be DecimalType", command);
                }
                break;
            default:
                logger.warn("Unknown channel for Bridge thing: {}", channelUID);
        }
    }

    private void makeOmnilinkConnection() {
        final Connection connection = omniConnection;
        if (connection != null && connection.connected()) {
            return;
        }

        logger.debug("Attempting to connect to controller!");
        try {
            OmnilinkBridgeConfig config = getConfigAs(OmnilinkBridgeConfig.class);

            this.omniConnection = new Connection(config.getIpAddress(), config.getPort(),
                    config.getKey1() + ":" + config.getKey2());

            /*
             * HAI only supports one audio player - cycle through features until we find a
             * feature that is an audio player.
             */
            audioPlayer = Objects.requireNonNull(
                    reqSystemFeatures().getFeatures().stream().map(AudioPlayer::getAudioPlayerForFeatureCode)
                            .filter(Optional::isPresent).findFirst().orElse(Optional.empty()));

            systemType = SystemType.getType(reqSystemInformation().getModel());

            if (config.getLogPollingInterval() > 0) {
                startEventPolling(config.getLogPollingInterval());
            }

            final Connection connectionNew = omniConnection;
            if (connectionNew != null) {
                connectionNew.enableNotifications();
                connectionNew.addNotificationListener(OmnilinkBridgeHandler.this);
                connectionNew.addDisconnectListener(this);
            }

            updateStatus(ThingStatus.ONLINE);
            cancelReconnectJob(false);
            updateChannels();
            updateBridgeProperties();
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (IOException e) {
            final Throwable cause = e.getCause();
            if (cause != null) {
                final String causeMessage = cause.getMessage();

                if (causeMessage != null && causeMessage.contains("Connection timed out")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "IP Address probably incorrect, timed out creating connection!");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, causeMessage);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } catch (Exception e) {
            setOfflineAndReconnect(e.getMessage());
            logger.debug("Error connecting to OmniLink Controller: {}", e.getMessage());
        }
    }

    @Override
    public void objectStatusNotification(@Nullable ObjectStatus objectStatus) {
        if (objectStatus != null) {
            Status[] statuses = objectStatus.getStatuses();
            for (Status status : statuses) {
                switch (status) {
                    case ExtendedUnitStatus unitStatus -> {
                        int unitNumber = unitStatus.getNumber();

                        logger.debug("Received status update for Unit: {}, status: {}", unitNumber, unitStatus);
                        Optional<Thing> theThing = getUnitThing(unitNumber);
                        theThing.map(Thing::getHandler)
                                .ifPresent(theHandler -> ((UnitHandler) theHandler).handleStatus(unitStatus));
                    }
                    case ExtendedZoneStatus zoneStatus -> {
                        int zoneNumber = zoneStatus.getNumber();

                        logger.debug("Received status update for Zone: {}, status: {}", zoneNumber, zoneStatus);
                        Optional<Thing> theThing = getChildThing(THING_TYPE_ZONE, zoneNumber);
                        theThing.map(Thing::getHandler)
                                .ifPresent(theHandler -> ((ZoneHandler) theHandler).handleStatus(zoneStatus));
                    }
                    case ExtendedAreaStatus areaStatus -> {
                        int areaNumber = areaStatus.getNumber();

                        logger.debug("Received status update for Area: {}, status: {}", areaNumber, areaStatus);
                        systemType.ifPresent(t -> {
                            Optional<Thing> theThing = switch (t) {
                                case LUMINA -> getChildThing(THING_TYPE_LUMINA_AREA, areaNumber);
                                case OMNI -> getChildThing(THING_TYPE_OMNI_AREA, areaNumber);
                            };
                            theThing.map(Thing::getHandler).ifPresent(
                                    theHandler -> ((AbstractAreaHandler) theHandler).handleStatus(areaStatus));
                        });
                    }
                    case ExtendedAccessControlReaderLockStatus lockStatus -> {
                        int lockNumber = lockStatus.getNumber();

                        logger.debug("Received status update for Lock: {}, status: {}", lockNumber, lockStatus);
                        Optional<Thing> theThing = getChildThing(THING_TYPE_LOCK, lockNumber);
                        theThing.map(Thing::getHandler)
                                .ifPresent(theHandler -> ((LockHandler) theHandler).handleStatus(lockStatus));
                    }
                    case ExtendedThermostatStatus thermostatStatus -> {
                        int thermostatNumber = thermostatStatus.getNumber();

                        logger.debug("Received status update for Thermostat: {}, status: {}", thermostatNumber,
                                thermostatStatus);
                        Optional<Thing> theThing = getChildThing(THING_TYPE_THERMOSTAT, thermostatNumber);
                        theThing.map(Thing::getHandler).ifPresent(
                                theHandler -> ((ThermostatHandler) theHandler).handleStatus(thermostatStatus));
                    }
                    case ExtendedAudioZoneStatus audioZoneStatus -> {
                        int audioZoneNumber = audioZoneStatus.getNumber();

                        logger.debug("Received status update for Audio Zone: {}, status: {}", audioZoneNumber,
                                audioZoneStatus);
                        Optional<Thing> theThing = getChildThing(THING_TYPE_AUDIO_ZONE, audioZoneNumber);
                        theThing.map(Thing::getHandler)
                                .ifPresent(theHandler -> ((AudioZoneHandler) theHandler).handleStatus(audioZoneStatus));
                    }
                    case ExtendedAuxSensorStatus auxSensorStatus -> {
                        int auxSensorNumber = auxSensorStatus.getNumber();

                        // Aux Sensors can be either temperature or humidity, need to check both.
                        Optional<Thing> tempThing = getChildThing(THING_TYPE_TEMP_SENSOR, auxSensorNumber);
                        Optional<Thing> humidityThing = getChildThing(THING_TYPE_HUMIDITY_SENSOR, auxSensorNumber);
                        if (tempThing.isPresent()) {
                            logger.debug("Received status update for Temperature Sensor: {}, status: {}",
                                    auxSensorNumber, auxSensorStatus);
                            tempThing.map(Thing::getHandler).ifPresent(
                                    theHandler -> ((TempSensorHandler) theHandler).handleStatus(auxSensorStatus));
                        }
                        if (humidityThing.isPresent()) {
                            logger.debug("Received status update for Humidity Sensor: {}, status: {}", auxSensorNumber,
                                    auxSensorStatus);
                            humidityThing.map(Thing::getHandler).ifPresent(
                                    theHandler -> ((HumiditySensorHandler) theHandler).handleStatus(auxSensorStatus));
                        }
                    }
                    case null, default ->
                        logger.debug("Received Object Status Notification that was not processed: {}", objectStatus);
                }
            }
        } else {
            logger.debug("Received null Object Status Notification!");
        }
    }

    @Override
    public void systemEventNotification(@Nullable SystemEvent event) {
        if (event != null) {
            logger.debug("Received System Event Notification of type: {}", event.getType());
            switch (event.getType()) {
                case PHONE_LINE_DEAD:
                case PHONE_LINE_OFF_HOOK:
                case PHONE_LINE_ON_HOOK:
                case PHONE_LINE_RING:
                    ChannelUID channel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_PHONE_LINE_EVENT);
                    triggerChannel(channel, event.getType().toString().replaceAll("^PHONE_LINE_", ""));
                    break;
                case AC_POWER_OFF:
                case AC_POWER_RESTORED:
                    ChannelUID acChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_AC_POWER_EVENT);
                    triggerChannel(acChannel, event.getType().toString().replaceAll("^AC_POWER_", ""));
                    break;
                case BATTERY_LOW:
                case BATTERY_OK:
                    ChannelUID batteryChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_BATTERY_EVENT);
                    triggerChannel(batteryChannel, event.getType().toString().replaceAll("^BATTERY_", ""));
                    break;
                case DCM_OK:
                case DCM_TROUBLE:
                    ChannelUID dcmChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_DCM_EVENT);
                    triggerChannel(dcmChannel, event.getType().toString().replaceAll("^DCM_", ""));
                    break;
                case ENERGY_COST_CRITICAL:
                case ENERGY_COST_HIGH:
                case ENERGY_COST_LOW:
                case ENERGY_COST_MID:
                    ChannelUID energyChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_ENERGY_COST_EVENT);
                    triggerChannel(energyChannel, event.getType().toString().replaceAll("^ENERGY_COST_", ""));
                    break;
                case CAMERA_1_TRIGGER:
                case CAMERA_2_TRIGGER:
                case CAMERA_3_TRIGGER:
                case CAMERA_4_TRIGGER:
                case CAMERA_5_TRIGGER:
                case CAMERA_6_TRIGGER:
                    ChannelUID cameraChannel = new ChannelUID(getThing().getUID(),
                            TRIGGER_CHANNEL_CAMERA_TRIGGER_EVENT);
                    triggerChannel(cameraChannel, String.valueOf(event.getType().toString().charAt(8)));
                    break;
                case BUTTON:
                    Optional<Thing> buttonThing = getChildThing(THING_TYPE_BUTTON,
                            ((ButtonEvent) event).getButtonNumber());
                    buttonThing.map(Thing::getHandler)
                            .ifPresent(theHandler -> ((ButtonHandler) theHandler).buttonActivated());
                    break;
                case ALL_ON_OFF:
                    Optional<Thing> areaThing = getChildThing(THING_TYPE_OMNI_AREA, ((AllOnOffEvent) event).getArea());
                    if (areaThing.isPresent()) {
                        logger.debug("Thing for allOnOff event: {}", areaThing.get().getUID());
                        areaThing.map(Thing::getHandler).ifPresent(theHandler -> ((AbstractAreaHandler) theHandler)
                                .handleAllOnOffEvent((AllOnOffEvent) event));
                    }
                    break;
                case UPB_LINK:
                    UPBLinkEvent linkEvent = (UPBLinkEvent) event;
                    UPBLinkEvent.Command command = linkEvent.getLinkCommand();
                    int link = linkEvent.getLinkNumber();
                    handleUPBLink(link, command);
                    break;
                case ALC_UPB_RADIORA_STARLITE_SWITCH_PRESS:
                    SwitchPressEvent switchPressEvent = (SwitchPressEvent) event;
                    int unitNumber = switchPressEvent.getUnitNumber();

                    Optional<Thing> unitThing = getUnitThing(unitNumber);
                    unitThing.map(Thing::getHandler).ifPresent(
                            theHandler -> ((UnitHandler) theHandler).handleSwitchPressEvent(switchPressEvent));
                    break;
                default:
                    logger.warn("Ignoring System Event Notification of type: {}", event.getType());
            }
        } else {
            logger.debug("Received null System Event Notification!");
        }
    }

    private void handleUPBLink(int link, UPBLinkEvent.Command command) {
        final ChannelUID activateChannel;

        if (command == UPBLinkEvent.Command.ACTIVATED) {
            activateChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_UPB_LINK_ACTIVATED_EVENT);
        } else if (command == UPBLinkEvent.Command.DEACTIVATED) {
            activateChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_UPB_LINK_DEACTIVATED_EVENT);
        } else {
            logger.debug("Received unsupported UPB link event: {}", command);
            return;
        }
        triggerChannel(activateChannel, Integer.toString(link));
    }

    @Override
    public void notConnectedEvent(@Nullable Exception e) {
        if (e != null) {
            logger.debug("Received an OmniLink Controller not connected event: {}", e.getMessage());
            setOfflineAndReconnect(e.getMessage());
        }
    }

    private void getSystemStatus() throws IOException, OmniNotConnectedException, OmniInvalidResponseException,
            OmniUnknownMessageTypeException {
        SystemStatus status = getOmniConnection().reqSystemStatus();
        logger.debug("Received system status: {}", status);
        // Update controller's reported time
        String dateString = (2000 + status.getYear()) + "-" + String.format("%02d", status.getMonth()) + "-"
                + String.format("%02d", status.getDay()) + "T" + String.format("%02d", status.getHour()) + ":"
                + String.format("%02d", status.getMinute()) + ":" + String.format("%02d", status.getSecond());
        updateState(CHANNEL_SYSTEM_DATE, new DateTimeType(dateString));
    }

    public Message reqObjectProperties(int objectType, int objectNum, int direction, int filter1, int filter2,
            int filter3) throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().reqObjectProperties(objectType, objectNum, direction, filter1, filter2, filter3);
        } catch (OmniNotConnectedException | IOException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public Message requestAudioSourceStatus(final int source, final int position)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().reqAudioSourceStatus(source, position);
        } catch (OmniNotConnectedException | IOException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public ObjectStatus requestObjectStatus(final int objType, final int startObject, final int endObject,
            boolean extended)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().reqObjectStatus(objType, startObject, endObject, extended);
        } catch (OmniNotConnectedException | IOException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public Optional<TemperatureFormat> getTemperatureFormat() {
        try {
            return Optional.of(TemperatureFormat.valueOf(reqSystemFormats().getTempFormat()));
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Could not request temperature format from controller: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void updateChannels() {
        try {
            getSystemStatus();
            updateState(CHANNEL_CONSOLE_ENABLE_DISABLE_BEEPER, UnDefType.UNDEF);
            updateState(CHANNEL_CONSOLE_BEEP, UnDefType.UNDEF);
        } catch (IOException | OmniNotConnectedException | OmniInvalidResponseException
                | OmniUnknownMessageTypeException e) {
            logger.warn("Unable to update bridge channels: {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        cancelReconnectJob(true);
        cancelEventPolling();
        final Connection connection = omniConnection;
        if (connection != null) {
            connection.removeDisconnectListener(this);
            connection.disconnect();
        }
    }

    private Optional<Thing> getChildThing(ThingTypeUID type, int number) {
        Bridge bridge = getThing();
        return bridge.getThings().stream().filter(t -> t.getThingTypeUID().equals(type))
                .filter(t -> ((Number) t.getConfiguration().get(THING_PROPERTIES_NUMBER)).intValue() == number)
                .findFirst();
    }

    private Optional<Thing> getUnitThing(int unitId) {
        return SUPPORTED_UNIT_TYPES_UIDS.stream().map(uid -> getChildThing(uid, unitId)).flatMap(Optional::stream)
                .findFirst();
    }

    public Optional<AudioPlayer> getAudioPlayer() {
        return audioPlayer;
    }

    public Message readEventRecord(int eventNumber, int direction)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return getOmniConnection().readEventRecord(eventNumber, direction);
        } catch (OmniNotConnectedException | IOException e) {
            setOfflineAndReconnect(e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    private void updateBridgeProperties() {
        try {
            SystemInformation systemInformation = reqSystemInformation();
            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_MODEL_ID, Integer.toString(systemInformation.getModel()));
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemInformation.getMajor() + "."
                    + systemInformation.getMinor() + "." + systemInformation.getRevision());
            properties.put(THING_PROPERTIES_PHONE_NUMBER, systemInformation.getPhone());
            updateProperties(properties);
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Could not request system information from OmniLink Controller: {}", e.getMessage());
        }
    }

    @Override
    public void initialize() {
        scheduleReconnectJob();
    }

    private void scheduleReconnectJob() {
        ScheduledFuture<?> currentReconnectJob = connectJob;
        if (currentReconnectJob == null || currentReconnectJob.isDone()) {
            connectJob = super.scheduler.scheduleWithFixedDelay(this::makeOmnilinkConnection, 0, 60, TimeUnit.SECONDS);
        }
    }

    private void cancelReconnectJob(boolean kill) {
        ScheduledFuture<?> currentReconnectJob = connectJob;
        if (currentReconnectJob != null) {
            currentReconnectJob.cancel(kill);
        }
    }

    private void setOfflineAndReconnect(@Nullable String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        cancelEventPolling();
        final Connection connection = omniConnection;
        if (connection != null) {
            connection.removeDisconnectListener(this);
        }
        scheduleReconnectJob();
    }

    private void startEventPolling(int interval) {
        ScheduledFuture<?> eventPollingJobFuture = eventPollingJob;
        if (eventPollingJobFuture == null || eventPollingJobFuture.isDone()) {
            eventLogNumber = 0;
            eventPollingJob = super.scheduler.scheduleWithFixedDelay(this::pollEvents, 0, interval, TimeUnit.SECONDS);
        }
    }

    private void cancelEventPolling() {
        ScheduledFuture<?> eventPollingJobFuture = eventPollingJob;
        if (eventPollingJobFuture != null) {
            eventPollingJobFuture.cancel(true);
        }
    }

    private void pollEvents() {
        /*
         * On first run, direction is -1 (most recent event), after its 1 for the next
         * log message
         */
        try {
            Message message;
            do {
                logger.trace("Polling for event log messages.");
                int direction = eventLogNumber == 0 ? -1 : 1;
                message = readEventRecord(eventLogNumber, direction);
                if (message.getMessageType() == Message.MESG_TYPE_EVENT_LOG_DATA) {
                    EventLogData logData = (EventLogData) message;
                    logger.debug("Processing event log message number: {}", logData.getEventNumber());
                    eventLogNumber = logData.getEventNumber();
                    String json = gson.toJson(logData);
                    logger.debug("Received event log message: {}", json);
                    updateState(CHANNEL_EVENT_LOG, new StringType(json));
                }
            } while (message.getMessageType() != Message.MESG_TYPE_END_OF_DATA);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Exception received while polling for event log messages: {}", e.getMessage());
        }
    }

    private Connection getOmniConnection() throws OmniNotConnectedException {
        final Connection connection = omniConnection;
        if (connection != null) {
            return connection;
        } else {
            throw new OmniNotConnectedException("Connection not yet established!");
        }
    }
}
