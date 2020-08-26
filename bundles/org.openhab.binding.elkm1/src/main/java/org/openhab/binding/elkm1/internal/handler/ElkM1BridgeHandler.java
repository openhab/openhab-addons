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

package org.openhab.binding.elkm1.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.elkm1.internal.ElkM1BindingConstants;
import org.openhab.binding.elkm1.internal.ElkM1HandlerListener;
import org.openhab.binding.elkm1.internal.config.ElkAlarmConfig;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmArmedState;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmConnection;
import org.openhab.binding.elkm1.internal.elk.ElkDefinition;
import org.openhab.binding.elkm1.internal.elk.ElkListener;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkMessageFactory;
import org.openhab.binding.elkm1.internal.elk.ElkTypeToRequest;
import org.openhab.binding.elkm1.internal.elk.message.ArmAway;
import org.openhab.binding.elkm1.internal.elk.message.ArmToNight;
import org.openhab.binding.elkm1.internal.elk.message.ArmToNightInstant;
import org.openhab.binding.elkm1.internal.elk.message.ArmToStayHome;
import org.openhab.binding.elkm1.internal.elk.message.ArmToStayInstant;
import org.openhab.binding.elkm1.internal.elk.message.ArmToVacation;
import org.openhab.binding.elkm1.internal.elk.message.ArmingStatus;
import org.openhab.binding.elkm1.internal.elk.message.ArmingStatusReply;
import org.openhab.binding.elkm1.internal.elk.message.ControlOutputOn;
import org.openhab.binding.elkm1.internal.elk.message.Disarm;
import org.openhab.binding.elkm1.internal.elk.message.EthernetModuleTest;
import org.openhab.binding.elkm1.internal.elk.message.EthernetModuleTestReply;
import org.openhab.binding.elkm1.internal.elk.message.StringTextDescription;
import org.openhab.binding.elkm1.internal.elk.message.StringTextDescriptionReply;
import org.openhab.binding.elkm1.internal.elk.message.Version;
import org.openhab.binding.elkm1.internal.elk.message.VersionReply;
import org.openhab.binding.elkm1.internal.elk.message.ZoneChangeUpdate;
import org.openhab.binding.elkm1.internal.elk.message.ZoneDefinition;
import org.openhab.binding.elkm1.internal.elk.message.ZoneDefitionReply;
import org.openhab.binding.elkm1.internal.elk.message.ZonePartition;
import org.openhab.binding.elkm1.internal.elk.message.ZonePartitionReply;
import org.openhab.binding.elkm1.internal.elk.message.ZoneStatus;
import org.openhab.binding.elkm1.internal.elk.message.ZoneStatusReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElkM1BridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Bennett - Initial contribution
 */
public class ElkM1BridgeHandler extends BaseBridgeHandler implements ElkListener {
    private final Logger logger = LoggerFactory.getLogger(ElkM1BridgeHandler.class);

    private ElkAlarmConnection connection;
    private ElkMessageFactory messageFactory;
    private boolean[] areas = new boolean[ElkMessageFactory.MAX_AREAS];
    private List<ElkM1HandlerListener> listeners = new ArrayList<ElkM1HandlerListener>();

    public ElkM1BridgeHandler(Bridge thing) {
        super(thing);
        for (int i = 0; i < areas.length; i++) {
            areas[i] = false;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Needed to instantiate class
    }

    /**
     * Initialize the bridge to do stuff.
     */
    @Override
    public void initialize() {
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Opening server connection");

        // Load up the config and then get the connection to the elk setup.
        messageFactory = new ElkMessageFactory();
        ElkAlarmConfig config = getConfigAs(ElkAlarmConfig.class);
        connection = new ElkAlarmConnection(config, messageFactory);
        connection.addElkListener(this);
        if (connection.initialize()) {
            connection.sendCommand(new Version());
            connection.sendCommand(new ZoneDefinition());
            connection.sendCommand(new ZonePartition());
            connection.sendCommand(new ZoneStatus());
            connection.sendCommand(new ArmingStatus());
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Requesting version from alarm");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to open socket to alarm");
        }
    }

    /**
     * Called when the configuration is updated. We will reconnect to the elk at this point.
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        this.connection.removeElkListener(this);
        this.connection.shutdown();
        this.connection = new ElkAlarmConnection(getConfigAs(ElkAlarmConfig.class), messageFactory);
        connection.addElkListener(this);
        if (connection.initialize()) {
            connection.sendCommand(new Version());
            connection.sendCommand(new ZoneDefinition());
            connection.sendCommand(new ZonePartition());
            connection.sendCommand(new ZoneStatus());
            connection.sendCommand(new ArmingStatus());
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Requesting version from alarm");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to open socket to alarm");
        }
    }

    /**
     * Shutdown the bridge.
     */
    @Override
    public void dispose() {
        connection.shutdown();
        areas = null;
        connection = null;
        messageFactory = null;
        assert (listeners.isEmpty());
        super.dispose();
    }

    /**
     * Handlers an incoming message from the elk system.
     *
     * @param message The message from the elk to handle
     */
    @Override
    public void handleElkMessage(ElkMessage message) {
        logger.debug("Got Elk Message: {}", message.toString());
        if (message instanceof VersionReply) {
            VersionReply reply = (VersionReply) message;
            // Set the property.
            getThing().setProperty(ElkM1BindingConstants.PROPERTY_VERSION, reply.getElkVersion());
            updateStatus(ThingStatus.ONLINE);
        }
        if (message instanceof ZoneStatusReply) {
            ZoneStatusReply reply = (ZoneStatusReply) message;
            for (int i = 0; i < ElkMessageFactory.MAX_ZONES; i++) {
                Thing thing = getThingForType(ElkTypeToRequest.Zone, i + 1);
                if (thing != null) {
                    ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateZoneConfig(reply.getConfig()[i], reply.getStatus()[i]);
                    }
                }
            }
        }
        if (message instanceof ZonePartitionReply) {
            ZonePartitionReply reply = (ZonePartitionReply) message;
            for (int i = 0; i < ElkMessageFactory.MAX_ZONES; i++) {
                Thing thing = getThingForType(ElkTypeToRequest.Area, reply.getAreas()[i]);
                if (thing == null && reply.getAreas()[i] != 0 && !areas[reply.getAreas()[i] - 1]) {
                    // Request the area.
                    connection.sendCommand(new StringTextDescription(ElkTypeToRequest.Area, reply.getAreas()[i]));
                    areas[reply.getAreas()[i] - 1] = true;
                    logger.debug("Requesting Elk Area: {}", reply.getAreas()[i]);
                }
                thing = getThingForType(ElkTypeToRequest.Zone, i + 1);
                if (thing != null) {
                    ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateZoneArea(reply.getAreas()[i]);
                    }
                }
            }
        }
        if (message instanceof ZoneDefitionReply) {
            ZoneDefitionReply reply = (ZoneDefitionReply) message;
            for (int i = 0; i < ElkMessageFactory.MAX_ZONES; i++) {
                if (reply.getDefinition()[i] != ElkDefinition.Disabled) {
                    connection.sendCommand(new StringTextDescription(ElkTypeToRequest.Zone, i + 1));
                    logger.debug("Requesting Elk Zone: {}", i);
                }
                Thing thing = getThingForType(ElkTypeToRequest.Zone, i + 1);
                if (thing != null) {
                    ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateZoneDefinition(reply.getDefinition()[i]);
                    }
                }
            }
        }
        if (message instanceof ZoneChangeUpdate) {
            ZoneChangeUpdate reply = (ZoneChangeUpdate) message;
            Thing thing = getThingForType(ElkTypeToRequest.Zone, reply.getZoneNumber());
            if (thing != null) {
                ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateZoneConfig(reply.getConfig(), reply.getStatus());
                }
            }
        }
        if (message instanceof EthernetModuleTest) {
            connection.sendCommand(new EthernetModuleTestReply());
        }
        if (message instanceof ArmingStatusReply) {
            ArmingStatusReply reply = (ArmingStatusReply) message;
            // Do stuff.
            for (int i = 0; i < ElkMessageFactory.MAX_AREAS; i++) {
                Thing thing = getThingForType(ElkTypeToRequest.Area, i + 1);
                if (thing != null) {
                    ElkM1AreaHandler handler = (ElkM1AreaHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateArea(reply.getState()[i], reply.getArmed()[i], reply.getArmedUp()[i]);
                    }
                }
            }
        }
        if (message instanceof StringTextDescriptionReply) {
            StringTextDescriptionReply reply = (StringTextDescriptionReply) message;
            switch (reply.getTypeResponse()) {
                case Zone:
                    // Once we have a description, see if this zone exists.
                    Thing thing = getThingForType(ElkTypeToRequest.Zone, reply.getThingNum());
                    if (thing == null) {
                        for (ElkM1HandlerListener listener : this.listeners) {
                            listener.onZoneDiscovered(reply.getThingNum(), reply.getText());
                        }
                    }
                    break;
                case Area:
                    thing = getThingForType(ElkTypeToRequest.Area, reply.getThingNum());
                    if (thing == null) {
                        for (ElkM1HandlerListener listener : this.listeners) {
                            listener.onAreaDiscovered(reply.getThingNum(), reply.getText());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Adds a listener to this bridge.
     */
    public void addListener(ElkM1HandlerListener listener) {
        synchronized (listeners) {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes a listener from this bridge.
     */
    public void removeListener(ElkM1HandlerListener listener) {
        synchronized (listeners) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Gets the thing associated with the type/number.
     *
     * @param type the type to look for
     * @param num the number of the type to look for
     * @return the thing, null if not found
     */
    Thing getThingForType(ElkTypeToRequest type, int num) {
        for (Thing thing : getThing().getThings()) {
            Map<String, String> properties = thing.getProperties();
            if (properties.get(ElkM1BindingConstants.PROPERTY_TYPE_ID).equals(type.toString())) {
                if (properties.get(ElkM1BindingConstants.PROPERTY_ZONE_NUM).equals(Integer.toString(num))) {
                    return thing;
                }
            }
        }
        return null;
    }

    /**
     * Starts a scan by asking for the zone status. This is called from the discovery handler.
     */
    public void startScan() {
        connection.sendCommand(new ZoneStatus());
    }

    /**
     * Refreshes the zones.
     */
    public void refreshZones() {
        if (!connection.isSendingClass(ZoneStatus.class)) {
            connection.sendCommand(new ZoneStatus());
        }
    }

    /**
     * Refreshes the areas.
     */
    public void refreshArea() {
        if (!connection.isSendingClass(ArmingStatus.class)) {
            connection.sendCommand(new ArmingStatus());
        }
    }

    /**
     * Called when an area is added to ask for the defintion and details of it.
     *
     * @param elkM1AreaHandler The handler for the area that is added.
     */
    public void onAreaAdded(ElkM1AreaHandler elkM1AreaHandler) {
        connection.sendCommand(new ArmingStatus());
    }

    /**
     * Called when a zone is added to ask for the definition and details of it.
     *
     * @param elkM1ZoneHandler The zone handle for the zone.
     */
    public void onZoneAdded(ElkM1ZoneHandler elkM1ZoneHandler) {
        connection.sendCommand(new ZoneDefinition());
        connection.sendCommand(new ZonePartition());
        connection.sendCommand(new ZoneStatus());
    }

    /**
     * Sends the right command to the elk to change the alarmed state for the m1 gold.
     *
     * @param area The area to alarm
     * @param armed The state to set it to
     */
    public void updateArmedState(int area, ElkAlarmArmedState armed) {
        ElkAlarmConfig config = getConfigAs(ElkAlarmConfig.class);
        String pincode = String.format("%06d", config.pincode);
        switch (armed) {
            case ArmedAway:
                connection.sendCommand(new ArmAway(area, pincode));
                break;
            case Disarmed:
                connection.sendCommand(new Disarm(area, pincode));
                break;
            case ArmedStay:
                connection.sendCommand(new ArmToStayHome(area, pincode));
                break;
            case ArmedStayInstant:
                connection.sendCommand(new ArmToStayInstant(area, pincode));
                break;
            case ArmedToNight:
                connection.sendCommand(new ArmToNight(area, pincode));
                break;
            case ArmedToNightInstant:
                connection.sendCommand(new ArmToNightInstant(area, pincode));
                break;
            case ArmedToVacation:
                connection.sendCommand(new ArmToVacation(area, pincode));
                break;
        }
    }

    public void sendELKCommand(String command) {
        String messageType = command.substring(0, 2);
        switch (messageType) {
            case "cn":
                connection.sendCommand(new ControlOutputOn(command));
                break;
        }
    }

}
