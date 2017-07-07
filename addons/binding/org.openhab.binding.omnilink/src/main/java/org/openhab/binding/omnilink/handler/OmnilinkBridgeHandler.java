package org.openhab.binding.omnilink.handler;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.config.OmnilinkBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Connection;
import com.digitaldan.jomnilinkII.DisconnectListener;
import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.NotificationListener;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniNotConnectedException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.OtherEventNotifications;
import com.digitaldan.jomnilinkII.MessageTypes.SecurityCodeValidation;
import com.digitaldan.jomnilinkII.MessageTypes.SystemFormats;
import com.digitaldan.jomnilinkII.MessageTypes.SystemInformation;
import com.digitaldan.jomnilinkII.MessageTypes.SystemStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AudioZoneStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedThermostatStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ZoneStatus;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;

public class OmnilinkBridgeHandler extends BaseBridgeHandler implements NotificationListener {

    private Logger logger = LoggerFactory.getLogger(OmnilinkBridgeHandler.class);
    private Connection omniConnection;
    private TemperatureFormat temperatureFormat;
    private DisconnectListener retryingDisconnectListener;

    public OmnilinkBridgeHandler(Bridge bridge) {
        super(bridge);
        retryingDisconnectListener = new DisconnectListener() {
            @Override
            public void notConnectedEvent(Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                makeOmnilinkConnection();
            }
        };
    }

    public void sendOmnilinkCommand(final int message, final int param1, final int param2)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        try {
            omniConnection.controllerCommand(message, param1, param2);
        } catch (IOException | OmniNotConnectedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new BridgeOfflineException(e);
        }

    }

    public SecurityCodeValidation reqSecurityCodeValidation(int area, int digit1, int digit2, int digit3, int digit4)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return omniConnection.reqSecurityCodeValidation(area, digit1, digit2, digit3, digit4);
        } catch (IOException | OmniNotConnectedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new BridgeOfflineException(e);
        }

    }

    public void activateKeypadEmergency(int area, int emergencyType)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            omniConnection.activateKeypadEmergency(area, emergencyType);
        } catch (IOException | OmniNotConnectedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new BridgeOfflineException(e);
        }

    }

    public SystemInformation reqSystemInformation()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return omniConnection.reqSystemInformation();
        } catch (IOException | OmniNotConnectedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public SystemFormats reqSystemFormats()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return omniConnection.reqSystemFormats();
        } catch (IOException | OmniNotConnectedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand called); " + command);
        switch (channelUID.getId()) {
            case OmnilinkBindingConstants.CHANNEL_SYSTEMDATE:
                if (command instanceof DateTimeType) {
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(((DateTimeType) command).getCalendar().toInstant(),
                            ZoneId.systemDefault());
                    boolean inDaylightSavings = zdt.getZone().getRules().isDaylightSavings(zdt.toInstant());
                    try {
                        omniConnection.setTimeCommand(zdt.getYear() - 2000, zdt.getMonthValue(), zdt.getDayOfMonth(),
                                zdt.getDayOfWeek().getValue(), zdt.getHour(), zdt.getMinute(), inDaylightSavings);
                    } catch (IOException | OmniNotConnectedException | OmniInvalidResponseException
                            | OmniUnknownMessageTypeException e) {
                        logger.debug("Unable to set system date", e);
                    }
                } else {
                    logger.warn("Invalid command for system date, must be DateTimeType, instead was: {}", command);
                }
                break;
            case OmnilinkBindingConstants.CHANNEL_CONSOLE_ENABLE_BEEPER:
                if (command instanceof OnOffType) {
                    try {
                        sendOmnilinkCommand(CommandMessage.CMD_CONSOLE_ENABLE_DISABLE_BEEPER,
                                command.equals(OnOffType.OFF) ? 0 : 1, 0);
                    } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                            | BridgeOfflineException e) {
                        logger.debug("Could not send console command to omnilink", e);
                    }
                } else {
                    logger.warn("Invalid command {}, must be OnOffTYpe", command);
                }
                break;
            case OmnilinkBindingConstants.CHANNEL_CONSOLE_BEEP:
                if (command instanceof DecimalType) {
                    try {
                        sendOmnilinkCommand(CommandMessage.CMD_CONSOLE_BEEP, ((DecimalType) command).intValue(), 0);
                    } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                            | BridgeOfflineException e) {
                        logger.debug("Could not send console command to omnilink", e);
                    }
                } else {
                    logger.warn("Invalid command {}, must be DecimalType", command);
                }
                break;
            default:
                logger.error("Unknown channel {}", channelUID.getAsString());
        }
    }

    @Override
    public void initialize() {
        makeOmnilinkConnection();
        super.initialize();
    }

    private void makeOmnilinkConnection() {

        Retryer<Void> retryer = RetryerBuilder.<Void> newBuilder().retryIfExceptionOfType(IOException.class)
                .withWaitStrategy(WaitStrategies.exponentialWait(100, 5, TimeUnit.MINUTES))
                .withStopStrategy(StopStrategies.neverStop()).build();

        try {
            retryer.call(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    logger.debug("Attempting to connect to omnilink");
                    try {
                        OmnilinkBridgeConfig config = getThing().getConfiguration().as(OmnilinkBridgeConfig.class);
                        omniConnection = new Connection(config.getIpAddress(), config.getPort(),
                                config.getKey1() + ":" + config.getKey2());
                        omniConnection.addNotificationListener(OmnilinkBridgeHandler.this);
                        omniConnection.addDisconnectListener(retryingDisconnectListener);
                        omniConnection.enableNotifications();
                        temperatureFormat = TemperatureFormat.valueOf(reqSystemFormats().getTempFormat());

                        updateStatus(ThingStatus.ONLINE);
                    } catch (UnknownHostException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        logger.debug(e.toString());
                        throw e;
                    } catch (IOException e) {
                        if (e.getCause() != null && e.getCause().getMessage().contains("Connection timed out")) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "IP Address probably incorrect, timed out creating connection");
                        } else if (e.getCause() != null && e.getCause() instanceof SocketException) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    e.getCause().getMessage());
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    e.getCause().getMessage());
                        }
                        logger.debug(e.toString());
                        throw e;
                    } catch (Exception e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        logger.debug(e.toString());
                        throw e;
                    }
                    return null;
                }
            });
        } catch (ExecutionException e) {
            if ("Could not establish secure connection".equals(e.getCause().getMessage())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid Keys");
            }
            logger.debug("Received execution exception: {} ", e);
        } catch (RetryException e) {
            logger.error("Should never get retry exception, we are to retry forever", e);
        }

    }

    private void handleUnitStatus(UnitStatus stat) {
        logger.debug("received status update for unit: {}, status: {}", stat.getNumber(), stat.getStatus());
        Optional<Thing> theThing = getChildThing(OmnilinkBindingConstants.THING_TYPE_UNIT_UPB, stat.getNumber());
        if (theThing.isPresent() == false) {
            theThing = getChildThing(OmnilinkBindingConstants.THING_TYPE_ROOM, stat.getNumber());
        }

        if (theThing.isPresent()) {
            ((UnitHandler) theThing.get().getHandler()).handleUnitStatus(stat);
        }
    }

    @Override
    public void objectStatusNotification(ObjectStatus objectStatus) {
        Status[] statuses = objectStatus.getStatuses();
        for (Status status : statuses) {
            if (status instanceof UnitStatus) {
                handleUnitStatus((UnitStatus) status);
            } else if (status instanceof ZoneStatus) {
                ZoneStatus stat = (ZoneStatus) status;
                Integer number = new Integer(stat.getNumber());
                logger.debug("received status update for zone: {},status: {}", number, stat.getStatus());
                Optional<Thing> theThing = getChildThing(OmnilinkBindingConstants.THING_TYPE_ZONE, stat.getNumber());

                if (theThing.isPresent()) {
                    ((ZoneHandler) theThing.get().getHandler()).handleZoneStatus(stat);
                }
            } else if (status instanceof AreaStatus) {
                AreaStatus areaStatus = (AreaStatus) status;
                // TODO we shuold check if this is a lumina system and return that if so
                Optional<Thing> theThing = getChildThing(OmnilinkBindingConstants.THING_TYPE_OMNI_AREA,
                        status.getNumber());
                // logger.debug("AreaStatus: Mode={}, text={}", areaStatus.getMode(),
                // AreaAlarmStatus.values()[areaStatus.getMode()]);
                if (theThing.isPresent()) {
                    ((AreaHandler) theThing.get().getHandler()).handleAreaEvent(areaStatus);
                }
            } else if (status instanceof ExtendedThermostatStatus) {
                ExtendedThermostatStatus thermostatStatus = (ExtendedThermostatStatus) status;
                Optional<Thing> theThing = getChildThing(OmnilinkBindingConstants.THING_TYPE_THERMOSTAT,
                        status.getNumber());
                if (theThing.isPresent()) {
                    ((ThermostatHandler) theThing.get().getHandler()).handleThermostatStatus(thermostatStatus);
                }
            } else if (status instanceof AudioZoneStatus) {
                AudioZoneStatus audioZoneStatus = (AudioZoneStatus) status;
                Optional<Thing> theThing = getChildThing(OmnilinkBindingConstants.THING_TYPE_AUDIO_ZONE,
                        status.getNumber());
                if (theThing.isPresent()) {
                    ((AudioZoneHandler) theThing.get().getHandler()).handleAudioZoneStatus(audioZoneStatus);
                }
            } else {
                logger.debug("Received Object Status Notification that was not processed: {}", objectStatus);
            }

        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("childHandlerDisposed called with '{}', childThing '{}'", childHandler, childThing);
    }

    @Override
    public void otherEventNotification(OtherEventNotifications event) {
        logger.debug("Other event otification, type: {}", event.getMessageType());

        if (event.getNotifications() != null && event.getNotifications().length > 0) {
            logger.debug("First notification: {}", Integer.toString(event.getNotifications()[0], 2));
        } else {
            logger.debug("Event notification: {}", event.getNotifications());
        }

        // for a button, let's make sure we have only 1 notification
        if (Message.MESG_TYPE_OTHER_EVENT_NOTIFY == event.getMessageType() && event.getNotifications().length == 1) {
            int number = event.getNotifications()[0];
            if (number > 0 && number <= 256) {
                Optional<Thing> theThing = getChildThing(OmnilinkBindingConstants.THING_TYPE_BUTTON, number);
                logger.debug("Detect button push: number={}, thing: {}", number, theThing);
                if (theThing.isPresent()) {
                    logger.debug("thing for button press is: {}", theThing.get().getUID());
                    ((ButtonHandler) theThing.get().getHandler()).buttonPressed();
                } else {
                    logger.warn("Unhandled other event notification, type: {}, notification: {}",
                            event.getMessageType(), event.getNotifications());
                }
            }
        } else {
            logger.warn("Unhandled other event notification, type: {}, notification: {}", event.getMessageType(),
                    Integer.toString(event.getNotifications()[0], 2));
        }

    }

    private void getSystemStatus() throws IOException, OmniNotConnectedException, OmniInvalidResponseException,
            OmniUnknownMessageTypeException {

        if (omniConnection != null) {
            SystemStatus status = omniConnection.reqSystemStatus();
            logger.debug("received system status: {}", status);
            // let's update system time
            String dateString = new StringBuilder().append(2000 + status.getYear()).append("-")
                    .append(status.getMonth()).append("-").append(status.getDay()).append("T").append(status.getHour())
                    .append(":").append(status.getMinute()).append(":").append(status.getSecond()).toString();
            DateTimeType sysDateTime = new DateTimeType(dateString);

            updateState(OmnilinkBindingConstants.CHANNEL_SYSTEMDATE, new DateTimeType(dateString));
            logger.debug("System date is: {}", sysDateTime);

        }
    }

    public Message reqObjectProperties(int objectType, int objectNum, int direction, int filter1, int filter2,
            int filter3) throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return omniConnection.reqObjectProperties(objectType, objectNum, direction, filter1, filter2, filter3);
        } catch (OmniNotConnectedException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            throw new BridgeOfflineException(e);
        }

    }

    public ObjectStatus requestObjectStatus(final int objType, final int startObject, final int endObject,
            boolean extended)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return omniConnection.reqObjectStatus(objType, startObject, endObject, extended);
        } catch (OmniNotConnectedException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            throw new BridgeOfflineException(e);
        }
    }

    public TemperatureFormat getTemperatureFormat() {
        return temperatureFormat;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            getSystemStatus();
        } catch (IOException | OmniNotConnectedException | OmniInvalidResponseException
                | OmniUnknownMessageTypeException e) {
            logger.error("Unable to retrieve system info", e);
        }
    }

    @Override
    public void dispose() {
        updateStatus(ThingStatus.OFFLINE);
        if (omniConnection != null) {
            // must remove this before disconnect, as this tries to create another connection
            omniConnection.removeDisconnecListener(retryingDisconnectListener);
            omniConnection.disconnect();
        }
    }

    private Optional<Thing> getChildThing(ThingTypeUID type, int number) {
        Bridge bridge = getThing();

        List<Thing> things = bridge.getThings();
        for (Thing thing : things) {
            logger.debug("Thing type: {}", thing.getThingTypeUID());
            if (type.equals(thing.getThingTypeUID())) {
                if (getThingNumber(thing) == number) {
                    return Optional.of(thing);
                }
            }
            logger.debug("Thing: {}", thing);
        }
        return Optional.empty();
    }

    private int getThingNumber(Thing thing) {
        return ((Number) thing.getConfiguration().get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER)).intValue();
    }

}
