package org.openhab.binding.omnilink.handler;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

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
import org.openhab.binding.omnilink.protocol.AreaAlarmStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Connection;
import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.NotificationListener;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.OtherEventNotifications;
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

    // private CacheHolder<Unit> nodes;

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
        OmnilinkBridgeConfig config = getThing().getConfiguration().as(OmnilinkBridgeConfig.class);

        try {
            omniConnection = new Connection(config.getIpAddress(), 4369, config.getKey1() + ":" + config.getKey2());
            omniConnection.enableNotifications();
            omniConnection.addNotificationListener(this);
            logger.debug("initialized omnilink connection");
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.error("Error connecting to omnilink", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        loadUnitStatuses();
    }

    @Override
    public void objectStausNotification(ObjectStatus status) {
        // logger.debug("status notification: {}", status);
        Status[] statuses = status.getStatuses();
        for (Status s : statuses) {
            if (s instanceof UnitStatus) {
                UnitStatus stat = (UnitStatus) s;
                logger.debug("Handle objectStatusNotification for: {}", stat);
                for (Thing thing : super.getThing().getThings()) {

                    if (OmnilinkBindingConstants.THING_TYPE_UNIT.equals(thing.getThingTypeUID())
                            || OmnilinkBindingConstants.THING_TYPE_FLAG.equals(thing.getThingTypeUID())) {
                        Object zoneId = thing.getConfiguration().getProperties().get("number");
                        int resolvedZoneId;
                        if (zoneId instanceof BigDecimal) {
                            resolvedZoneId = ((BigDecimal) zoneId).intValue();
                        } else {
                            resolvedZoneId = (int) zoneId;
                        }
                        Integer number = new Integer(((UnitStatus) s).getNumber());
                        logger.debug("received status update for unit: " + number + ", status: " + stat.getStatus());

                        if (zoneId instanceof BigDecimal) {
                            resolvedZoneId = ((BigDecimal) zoneId).intValue();
                        } else {
                            resolvedZoneId = (int) zoneId;
                        }
                        if (zoneId != null) {
                            if (number.intValue() == resolvedZoneId) {
                                ((UnitHandler) thing.getHandler()).handleUnitStatus(stat);
                                break;
                            }
                        }

                    }
                }
            } else if (s instanceof ZoneStatus) {
                for (Thing thing : super.getThing().getThings()) {
                    if (OmnilinkBindingConstants.THING_TYPE_ZONE.equals(thing.getThingTypeUID())) {
                        ZoneStatus stat = (ZoneStatus) s;
                        Integer number = new Integer(stat.getNumber());
                        logger.debug("received status update for zone: " + number + ",status: " + stat.getStatus());

                        Object zoneId = thing.getConfiguration().getProperties().get("number");
                        int resolvedZoneId;
                        if (zoneId instanceof BigDecimal) {
                            resolvedZoneId = ((BigDecimal) zoneId).intValue();
                        } else {
                            resolvedZoneId = (int) zoneId;
                        }
                        if (zoneId != null) {
                            if (number.intValue() == resolvedZoneId) {
                                ((ZoneHandler) thing.getHandler()).handleZoneStatus(stat);
                                break;
                            }
                        }

                    }
                }

            } else if (s instanceof AreaStatus) {
                AreaStatus areaStatus = (AreaStatus) s;
                logger.debug("AreaStatus: Mode={}, text={}", areaStatus.getMode(),
                        AreaAlarmStatus.values()[areaStatus.getMode()]);
                ((AreaHandler) areaThings.get(areaStatus.getNumber()).getHandler()).handleAreaEvent(areaStatus);
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
            areaThings.put(Integer.parseInt(childThing.getConfiguration().getProperties().get("number").toString()),
                    childThing);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("childHandlerDisposed called with '{}', childThing '{}'", childHandler, childThing);
    }

    @Override
    public void otherEventNotification(OtherEventNotifications arg0) {

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
