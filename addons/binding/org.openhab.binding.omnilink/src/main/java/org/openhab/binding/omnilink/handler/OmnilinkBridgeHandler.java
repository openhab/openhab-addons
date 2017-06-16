package org.openhab.binding.omnilink.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.config.OmnilinkBridgeConfig;
import org.openhab.binding.omnilink.discovery.OmnilinkDiscoveryService;
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
import com.digitaldan.jomnilinkII.MessageTypes.SystemInformation;
import com.digitaldan.jomnilinkII.MessageTypes.SystemStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ZoneStatus;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class OmnilinkBridgeHandler extends BaseBridgeHandler implements NotificationListener {
    private Logger logger = LoggerFactory.getLogger(OmnilinkBridgeHandler.class);
    private OmnilinkDiscoveryService bridgeDiscoveryService;
    private Connection omniConnection;
    private ListeningScheduledExecutorService listeningExecutor;
    private Map<Integer, Thing> areaThings = Collections.synchronizedMap(new HashMap<Integer, Thing>());
    private Map<Integer, Thing> unitThings = Collections.synchronizedMap(new HashMap<Integer, Thing>());
    private Map<Integer, Thing> zoneThings = Collections.synchronizedMap(new HashMap<Integer, Thing>());
    private Map<Integer, Thing> buttonThings = Collections.synchronizedMap(new HashMap<Integer, Thing>());

    private ScheduledFuture<?> scheduledRefresh;
    // private CacheHolder<Unit> nodes;
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

    public SystemInformation reqSystemInformation()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        try {
            return omniConnection.reqSystemInformation();
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

    public void registerDiscoveryService(OmnilinkDiscoveryService isyBridgeDiscoveryService) {
        this.bridgeDiscoveryService = isyBridgeDiscoveryService;

    }

    public void unregisterDiscoveryService() {
        this.bridgeDiscoveryService = null;

    }

    @Override
    public void initialize() {
        listeningExecutor = MoreExecutors.listeningDecorator(scheduler);
        makeOmnilinkConnection();

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
                        updateStatus(ThingStatus.ONLINE);
                        // let's start a task which refreshes status
                        scheduleRefresh();
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
        Integer number = stat.getNumber();
        Thing theThing = unitThings.get(number);
        logger.debug("received status update for unit: " + number + ", status: " + stat.getStatus());
        if (theThing != null) {
            ((UnitHandler) theThing.getHandler()).handleUnitStatus(stat);
        }
    }

    @Override
    public void objectStausNotification(ObjectStatus status) {
        Status[] statuses = status.getStatuses();
        for (Status s : statuses) {
            if (s instanceof UnitStatus) {
                handleUnitStatus((UnitStatus) s);
            } else if (s instanceof ZoneStatus) {
                ZoneStatus stat = (ZoneStatus) s;
                Integer number = new Integer(stat.getNumber());
                Thing theThing = zoneThings.get(number);
                logger.debug("received status update for zone: " + number + ",status: " + stat.getStatus());
                if (theThing != null) {
                    ((ZoneHandler) theThing.getHandler()).handleZoneStatus(stat);
                }
            } else if (s instanceof AreaStatus) {
                AreaStatus areaStatus = (AreaStatus) s;
                Integer number = new Integer(areaStatus.getNumber());
                Thing theThing = areaThings.get(number);
                // logger.debug("AreaStatus: Mode={}, text={}", areaStatus.getMode(),
                // AreaAlarmStatus.values()[areaStatus.getMode()]);
                if (theThing != null) {
                    ((AreaHandler) theThing.getHandler()).handleAreaEvent(areaStatus);
                }
            }
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("childHandlerInitialized called with '{}', childThing '{}'", childHandler, childThing);
        if (childHandler instanceof AreaHandler) {
            if (!childThing.getConfiguration().getProperties().containsKey("number")) {
                throw new IllegalArgumentException("childThing does not have required 'number' property");
            }
            int areaNumber;
            if (childThing.getConfiguration().getProperties().get("number") instanceof BigDecimal) {
                areaNumber = ((BigDecimal) childThing.getConfiguration().getProperties().get("number")).intValue();
            } else {
                areaNumber = Integer.parseInt(childThing.getConfiguration().getProperties().get("number").toString());
            }
            areaThings.put(areaNumber, childThing);
        } else if (childHandler instanceof UnitHandler) {
            if (!childThing.getConfiguration().getProperties().containsKey("number")) {
                throw new IllegalArgumentException("childThing does not have required 'number' property");
            }
            int unitNumber;
            if (childThing.getConfiguration().getProperties().get("number") instanceof BigDecimal) {
                unitNumber = ((BigDecimal) childThing.getConfiguration().getProperties().get("number")).intValue();
            } else {
                unitNumber = Integer.parseInt(childThing.getConfiguration().getProperties().get("number").toString());
            }
            unitThings.put(unitNumber, childThing);
        } else if (childHandler instanceof ZoneHandler) {
            if (!childThing.getConfiguration().getProperties().containsKey("number")) {
                throw new IllegalArgumentException("childThing does not have required 'number' property");
            }
            int zoneNumber;
            if (childThing.getConfiguration().getProperties().get("number") instanceof BigDecimal) {
                zoneNumber = ((BigDecimal) childThing.getConfiguration().getProperties().get("number")).intValue();
            } else {
                zoneNumber = Integer.parseInt(childThing.getConfiguration().getProperties().get("number").toString());
            }
            zoneThings.put(zoneNumber, childThing);
        } else if (childHandler instanceof ButtonHandler) {
            if (!childThing.getConfiguration().getProperties().containsKey("number")) {
                throw new IllegalArgumentException("childThing does not have required 'number' property");
            }
            int buttonNumber;
            if (childThing.getConfiguration().getProperties().get("number") instanceof BigDecimal) {
                buttonNumber = ((BigDecimal) childThing.getConfiguration().getProperties().get("number")).intValue();
            } else {
                buttonNumber = Integer.parseInt(childThing.getConfiguration().getProperties().get("number").toString());
            }
            buttonThings.put(buttonNumber, childThing);
        } else {
            logger.warn("Did not add childThing to a map: {}", childThing);
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
                Thing theThing = buttonThings.get(number);
                logger.debug("Detect button push: number={}, thing: {}", number, theThing);
                if (theThing != null) {
                    logger.debug("thing for button press is: {}", theThing.getUID());
                    ((ButtonHandler) theThing.getHandler()).buttonPressed();
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

    private UnitStatus[] getUnitStatuses() throws OmniInvalidResponseException, OmniUnknownMessageTypeException,
            BridgeOfflineException, IOException, OmniNotConnectedException {
        ObjectStatus val;
        val = requestObjectStatus(Message.OBJ_TYPE_UNIT, 1,
                omniConnection.reqObjectTypeCapacities(Message.OBJ_TYPE_UNIT).getCapacity(), false);
        return (UnitStatus[]) val.getStatuses();

    }

    /**
     * Every six hours let's poll the omnilink to make sure we have correct state
     */
    private void scheduleRefresh() {
        // TODO this could be configurable
        int interval = 60 * 60 * 6;
        logger.info("Scheduling refresh updates at {} seconds", interval);
        scheduledRefresh = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("Running scheduled refresh");
                try {
                    getSystemStatus();
                    for (UnitStatus unitStatus : getUnitStatuses()) {
                        handleUnitStatus(unitStatus);
                    }
                } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException
                        | IOException | OmniNotConnectedException e) {
                    logger.error("Unable to refresh unit statuses");
                }
                // TODO add areas, zones, flags, etc
            }
        }, interval, interval, TimeUnit.SECONDS);
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
        if (scheduledRefresh != null) {
            scheduledRefresh.cancel(true);
        }
        scheduledRefresh = null;
    }
}
