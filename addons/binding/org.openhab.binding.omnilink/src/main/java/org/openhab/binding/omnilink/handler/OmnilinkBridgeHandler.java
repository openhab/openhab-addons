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
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.config.OmnilinkBridgeConfig;
import org.openhab.binding.omnilink.discovery.OmnilinkDiscoveryService;
import org.openhab.binding.omnilink.protocol.AreaAlarmStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Connection;
import com.digitaldan.jomnilinkII.DisconnectListener;
import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.NotificationListener;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniNotConnectedException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
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
import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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

    public OmnilinkBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public void sendOmnilinkCommand(final int message, final int param1, final int param2) {

        listeningExecutor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    omniConnection.controllerCommand(message, param1, param2);
                } catch (IOException | OmniNotConnectedException | OmniInvalidResponseException
                        | OmniUnknownMessageTypeException e) {
                    logger.debug("Error in sendOmnilinkCommand", e);
                    // TODO should we be changing status of bridge on any of these errors?
                }
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand called); " + command);
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        if (OmnilinkBindingConstants.CHANNEL_SYSTEMDATE.equals(channelParts[3])) {
            if (command instanceof DateTimeType) {
                ZonedDateTime zdt = ZonedDateTime.ofInstant(((DateTimeType) command).getCalendar().toInstant(),
                        ZoneId.systemDefault());
                setOmnilinkSystemDate(zdt);
            } else if (command instanceof RefreshType) {
                getSystemStatus();
            } else {
                logger.warn("Invalid command for system date, must be DateTimeType, instead was: {}", command);
            }
        }
    }

    public Connection getOmnilinkConnection() {
        return omniConnection;
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
                        omniConnection = new Connection(config.getIpAddress(), 4369,

                                config.getKey1() + ":" + config.getKey2());
                        omniConnection.enableNotifications();

                        omniConnection.addNotificationListener(OmnilinkBridgeHandler.this);
                        omniConnection.addDisconnectListener(new DisconnectListener() {
                            @Override
                            public void notConnectedEvent(Exception e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        e.getMessage());
                                makeOmnilinkConnection();
                            }
                        });
                        updateStatus(ThingStatus.ONLINE);
                        getSystemInfo();
                        getSystemStatus();
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
                logger.debug("AreaStatus: Mode={}, text={}", areaStatus.getMode(),
                        AreaAlarmStatus.values()[areaStatus.getMode()]);
                if (theThing != null) {
                    ((AreaHandler) theThing.getHandler()).handleAreaEvent(areaStatus);
                }
            }
        }
    }

    private void loadUnitStatuses() {
        Futures.addCallback(getStatuses(Message.OBJ_TYPE_UNIT), new FutureCallback<UnitStatus[]>() {

            @Override
            public void onFailure(Throwable arg0) {
                logger.error("Error getting unit statuses", arg0);
            }

            @Override
            public void onSuccess(UnitStatus[] status) {
                for (UnitStatus unitStatus : status) {
                    handleUnitStatus(unitStatus);
                }
            }
        });
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

    private ListenableFuture<Integer> getMaxNumber(int objType) {
        return listeningExecutor.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return omniConnection.reqObjectTypeCapacities(objType).getCapacity();
            }
        });
    }

    private ListenableFuture<UnitStatus[]> getStatuses(int objType) {

        ListenableFuture<ObjectStatus> getUnitsFuture = Futures.transform(getMaxNumber(Message.OBJ_TYPE_UNIT),
                new AsyncFunction<Integer, ObjectStatus>() {
                    @Override
                    public ListenableFuture<ObjectStatus> apply(Integer rowKey) {
                        // TODO asking for extended because seemingly of bug in jomnilink
                        return requestObjectStatus(Message.OBJ_TYPE_UNIT, 1, rowKey,
                                objType == Message.OBJ_TYPE_UNIT ? true : false);
                    }
                }, listeningExecutor);

        return Futures.transform(getUnitsFuture, new Function<ObjectStatus, UnitStatus[]>() {
            @Override
            public UnitStatus[] apply(ObjectStatus t) {
                return (UnitStatus[]) t.getStatuses();
            }
        }, listeningExecutor);
    }

    private void getSystemInfo() {

        ListenableFuture<SystemInformation> systemStatus = listeningExecutor.submit(new Callable<SystemInformation>() {

            @Override
            public SystemInformation call() throws Exception {
                return omniConnection.reqSystemInformation();
            }
        });
        Futures.addCallback(systemStatus, new FutureCallback<SystemInformation>() {
            @Override
            public void onFailure(Throwable arg0) {
                logger.error("Error retrieving system status", arg0);
            }

            @Override
            public void onSuccess(SystemInformation status) {
                logger.debug("received system info: {}", status);
            }
        });

    }

    private void getSystemStatus() {
        if (omniConnection != null) {
            ListenableFuture<SystemStatus> systemStatus = listeningExecutor.submit(new Callable<SystemStatus>() {

                @Override
                public SystemStatus call() throws Exception {
                    return omniConnection.reqSystemStatus();
                }
            });
            Futures.addCallback(systemStatus, new FutureCallback<SystemStatus>() {
                @Override
                public void onFailure(Throwable arg0) {
                    logger.error("Error retrieving system status", arg0);
                }

                @Override
                public void onSuccess(SystemStatus status) {
                    logger.debug("received system status: {}", status);
                    // let's update system time
                    String dateString = new StringBuilder().append(2000 + status.getYear()).append("-")
                            .append(status.getMonth()).append("-").append(status.getDay()).append("T")
                            .append(status.getHour()).append(":").append(status.getMinute()).append(":")
                            .append(status.getSecond()).toString();
                    DateTimeType sysDateTime = new DateTimeType(dateString);

                    updateState(OmnilinkBindingConstants.CHANNEL_SYSTEMDATE, new DateTimeType(dateString));
                    logger.debug("System date is: {}", sysDateTime);
                }
            });
        }
    }

    public ListenableFuture<SecurityCodeValidation> validateSecurity(int area, final int code1, final int code2,
            final int code3, final int code4) {

        return listeningExecutor.submit(new Callable<SecurityCodeValidation>() {
            @Override
            public SecurityCodeValidation call() throws Exception {
                return omniConnection.reqSecurityCodeValidation(area, code1, code2, code3, code4);
            }
        });
    }

    private ListenableFuture<ObjectStatus> requestObjectStatus(final int objType, final int startObject,
            final int endObject, boolean extended) {

        return listeningExecutor.submit(new Callable<ObjectStatus>() {

            @Override
            public ObjectStatus call() throws Exception {
                return omniConnection.reqObjectStatus(objType, startObject, endObject, extended);
            }
        });
    }

    public ListenableFuture<UnitStatus> getUnitStatus(final int unitId) {
        return Futures.transform(requestObjectStatus(Message.OBJ_TYPE_UNIT, unitId, unitId, false),
                new Function<ObjectStatus, UnitStatus>() {

                    @Override
                    public UnitStatus apply(ObjectStatus t) {
                        return (UnitStatus) t.getStatuses()[0];
                    }
                }, listeningExecutor);
    }

    public ListenableFuture<ZoneStatus> getZoneStatus(final int address) {

        return Futures.transform(requestObjectStatus(Message.OBJ_TYPE_ZONE, address, address, false),
                new Function<ObjectStatus, ZoneStatus>() {

                    @Override
                    public ZoneStatus apply(ObjectStatus t) {
                        return (ZoneStatus) t.getStatuses()[0];
                    }
                }, listeningExecutor);
    }

    public ListenableFuture<AreaStatus> getAreaStatus(final int address) {

        return Futures.transform(requestObjectStatus(Message.OBJ_TYPE_AREA, address, address, false),
                new Function<ObjectStatus, AreaStatus>() {

                    @Override
                    public AreaStatus apply(ObjectStatus t) {
                        return (AreaStatus) t.getStatuses()[0];
                    }
                }, listeningExecutor);
    }

    public void setOmnilinkSystemDate(ZonedDateTime date) {

        boolean inDaylightSavings = date.getZone().getRules().isDaylightSavings(date.toInstant());

        try {
            listeningExecutor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    omniConnection.setTimeCommand(date.getYear() - 2000, date.getMonthValue(), date.getDayOfMonth(),
                            date.getDayOfWeek().getValue(), date.getHour(), date.getMinute(), inDaylightSavings);
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Error sending command", e);
        }
    }

    private void scheduleRefresh() {
        // TODO this could be configurable
        int interval = 60 * 60 * 6;
        logger.info("Scheduling refresh updates at {} seconds", interval);
        scheduledRefresh = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("Running scheduled refresh");
                getSystemStatus();
                loadUnitStatuses();
                // TODO add areas, zones, flags, etc
            }
        }, interval, interval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        updateStatus(ThingStatus.OFFLINE);
        if (omniConnection != null) {
            omniConnection.disconnect();
        }
        if (scheduledRefresh != null) {
            scheduledRefresh.cancel(true);
        }
        scheduledRefresh = null;
    }
}
