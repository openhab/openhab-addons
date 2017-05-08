package org.openhab.binding.omnilink.handler;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.config.OmnilinkBridgeConfig;
import org.openhab.binding.omnilink.discovery.OmnilinkDiscoveryService;
import org.openhab.binding.omnilink.protocol.AreaAlarmStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Connection;
import com.digitaldan.jomnilinkII.DisconnectListener;
import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.NotificationListener;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.OtherEventNotifications;
import com.digitaldan.jomnilinkII.MessageTypes.SecurityCodeValidation;
import com.digitaldan.jomnilinkII.MessageTypes.SystemInformation;
import com.digitaldan.jomnilinkII.MessageTypes.SystemStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ZoneStatus;
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
    // private CacheHolder<Unit> nodes;
    private int secondsUntilReconnect = 1;

    public OmnilinkBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public void sendOmnilinkCommand(final int message, final int param1, final int param2) {

        try {
            listeningExecutor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    omniConnection.controllerCommand(message, param1, param2);
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Error sending command", e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand called); " + command);
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

        try {
            makeOmnilinkConnection();
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            doOmnilinkReconnect();
        }

    }

    private void makeOmnilinkConnection() throws Exception {
        OmnilinkBridgeConfig config = getThing().getConfiguration().as(OmnilinkBridgeConfig.class);
        omniConnection = new Connection(config.getIpAddress(), 4369, config.getKey1() + ":" + config.getKey2());
        omniConnection.enableNotifications();

        omniConnection.addNotificationListener(this);
        omniConnection.addDisconnectListener(new DisconnectListener() {
            @Override
            public void notConnectedEvent(Exception e) {
                doOmnilinkReconnect();
            }
        });
        updateStatus(ThingStatus.ONLINE);
        secondsUntilReconnect = 1;
        getSystemInfo();
        getSystemStatus();
    }

    private void doOmnilinkReconnect() {
        logger.debug("will try to establish another connection in {} seconds", secondsUntilReconnect);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    makeOmnilinkConnection();

                } catch (Exception e) {
                    logger.error("Error trying to reconnect", e);
                    if (secondsUntilReconnect < 300) {
                        secondsUntilReconnect = secondsUntilReconnect * 2;
                    }
                    doOmnilinkReconnect();
                }

            }
        }, secondsUntilReconnect, TimeUnit.SECONDS);
    }

    @Override
    public void objectStausNotification(ObjectStatus status) {
        Status[] statuses = status.getStatuses();
        for (Status s : statuses) {
            if (s instanceof UnitStatus) {
                UnitStatus stat = (UnitStatus) s;
                Integer number = stat.getNumber();
                Thing theThing = unitThings.get(number);
                logger.debug("received status update for unit: " + number + ", status: " + stat.getStatus());
                if (theThing != null) {
                    ((UnitHandler) theThing.getHandler()).handleUnitStatus(stat);
                    break;
                }
            } else if (s instanceof ZoneStatus) {
                ZoneStatus stat = (ZoneStatus) s;
                Integer number = new Integer(stat.getNumber());
                Thing theThing = zoneThings.get(number);
                logger.debug("received status update for zone: " + number + ",status: " + stat.getStatus());
                if (theThing != null) {
                    ((ZoneHandler) theThing.getHandler()).handleZoneStatus(stat);
                    break;
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
        Futures.addCallback(getUnitStatuses(), new FutureCallback<UnitStatus[]>() {

            @Override
            public void onFailure(Throwable arg0) {
                logger.error("Error getting unit statuses", arg0);
            }

            @Override
            public void onSuccess(UnitStatus[] status) {
                for (UnitStatus unitStatus : status) {
                    logger.debug("received unit status: {}", unitStatus);
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
        logger.debug("received other event notification: {}", event);
        logger.debug("Event type: {}", event.getMessageType());
        logger.debug("Event notification: {}", event.getNotifications());
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
                    event.getNotifications());
        }

    }

    private ListenableFuture<Integer> getMaxNumberUnit() {
        return listeningExecutor.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return omniConnection.reqObjectTypeCapacities(Message.OBJ_TYPE_UNIT).getCapacity();
            }
        });
    }

    private ListenableFuture<UnitStatus[]> getUnitStatuses() {

        ListenableFuture<ObjectStatus> getUnitsFuture = Futures.transform(getMaxNumberUnit(),
                new AsyncFunction<Integer, ObjectStatus>() {
                    @Override
                    public ListenableFuture<ObjectStatus> apply(Integer rowKey) {
                        return requestObjectStatus(Message.OBJ_TYPE_UNIT, 1, rowKey);
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
            }
        });

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

    private ListenableFuture<ObjectStatus> requestObjectStatus(final int arg1, final int arg2, final int arg3) {

        return listeningExecutor.submit(new Callable<ObjectStatus>() {

            @Override
            public ObjectStatus call() throws Exception {
                return omniConnection.reqObjectStatus(arg1, arg2, arg3, true);
            }
        });
    }

    public ListenableFuture<UnitStatus> getUnitStatus(final int address) {

        ListenableFuture<ObjectStatus> omniCall = listeningExecutor.submit(new Callable<ObjectStatus>() {

            @Override
            public ObjectStatus call() throws Exception {
                if (omniConnection == null) {
                    Thread.sleep(100);
                }
                return omniConnection.reqObjectStatus(Message.OBJ_TYPE_UNIT, address, address);
            }
        });
        return Futures.transform(omniCall, new Function<ObjectStatus, UnitStatus>() {

            @Override
            public UnitStatus apply(ObjectStatus t) {
                return (UnitStatus) t.getStatuses()[0];
            }
        }, listeningExecutor);
    }

    public ListenableFuture<AreaStatus> getAreaStatus(final int address) {

        ListenableFuture<ObjectStatus> omniCall = listeningExecutor.submit(new Callable<ObjectStatus>() {

            @Override
            public ObjectStatus call() throws Exception {
                return omniConnection.reqObjectStatus(Message.OBJ_TYPE_AREA, address, address);
            }
        });
        return Futures.transform(omniCall, new Function<ObjectStatus, AreaStatus>() {

            @Override
            public AreaStatus apply(ObjectStatus t) {
                return (AreaStatus) t.getStatuses()[0];
            }
        }, listeningExecutor);
    }
}
