package org.openhab.binding.omnilink.handler;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.config.OmnilinkBridgeConfig;
import org.openhab.binding.omnilink.discovery.OmnilinkDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Connection;
import com.digitaldan.jomnilinkII.NotificationListener;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.OtherEventNotifications;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ZoneStatus;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class OmnilinkBridgeHandler extends BaseBridgeHandler implements NotificationListener {
    private Logger logger = LoggerFactory.getLogger(OmnilinkBridgeHandler.class);
    private OmnilinkDiscoveryService bridgeDiscoveryService;
    private Connection omniConnection;
    private ListeningScheduledExecutorService listeningExecutor;

    public OmnilinkBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public void sendOmnilinkCommand(int message, int param1, int param2) {

        try {
            listeningExecutor.submit(new Callable<Boolean>() {

                @SuppressWarnings("unchecked")
                @Override
                public Boolean call() throws Exception {
                    omniConnection.controllerCommand(message, param1, param2);
                    return Boolean.TRUE;
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
        super.initialize();
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

    }

    @Override
    public void objectStausNotification(ObjectStatus status) {
        logger.debug("status notification: " + status);
        Status[] statuses = status.getStatuses();
        Thing updatedThing = null;

        for (Status s : statuses) {
            if (s instanceof UnitStatus) {
                for (Thing thing : super.getThing().getThings()) {
                    if (OmnilinkBindingConstants.THING_TYPE_UNIT.equals(thing.getThingTypeUID())) {
                        updatedThing = thing;
                        UnitStatus stat = (UnitStatus) s;
                        Integer number = new Integer(((UnitStatus) s).getNumber());
                        logger.debug("received status update for unit: " + number + ", status: " + stat.getStatus());
                        Object zoneId = updatedThing.getConfiguration().getProperties().get("number");
                        int resolvedZoneId;
                        if (zoneId instanceof BigDecimal) {
                            resolvedZoneId = ((BigDecimal) zoneId).intValue();
                        } else {
                            resolvedZoneId = (int) zoneId;
                        }
                        if (zoneId != null) {
                            if (number.intValue() == resolvedZoneId) {
                                ((UnitHandler) updatedThing.getHandler()).handleUnitStatus(stat);
                                break;
                            }
                        }

                    }
                }

            } else if (s instanceof ZoneStatus) {
                for (Thing thing : super.getThing().getThings()) {
                    if (OmnilinkBindingConstants.THING_TYPE_ZONE.equals(thing.getThingTypeUID())) {
                        updatedThing = thing;
                        ZoneStatus stat = (ZoneStatus) s;
                        Integer number = new Integer(stat.getNumber());
                        logger.debug("received status update for zone: " + number + ",status: " + stat.getStatus());

                        Object zoneId = updatedThing.getConfiguration().getProperties().get("number");
                        int resolvedZoneId;
                        if (zoneId instanceof BigDecimal) {
                            resolvedZoneId = ((BigDecimal) zoneId).intValue();
                        } else {
                            resolvedZoneId = (int) zoneId;
                        }
                        if (zoneId != null) {
                            if (number.intValue() == resolvedZoneId) {
                                ((ZoneHandler) updatedThing.getHandler()).handleZoneStatus(stat);
                                break;
                            }
                        }

                    }
                }

            }
        }

    }

    @Override
    public void otherEventNotification(OtherEventNotifications arg0) {

    }
}
