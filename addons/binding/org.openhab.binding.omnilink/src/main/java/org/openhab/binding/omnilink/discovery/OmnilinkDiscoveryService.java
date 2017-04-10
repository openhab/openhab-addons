package org.openhab.binding.omnilink.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.config.OmnilinkUnitConfig;
import org.openhab.binding.omnilink.handler.OmnilinkBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Connection;
import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniNotConnectedException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ZoneProperties;
import com.google.common.collect.ImmutableSet;

public class OmnilinkDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(OmnilinkDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private OmnilinkBridgeHandler bridgeHandler;

    StringBuilder groups;
    StringBuilder items;
    HashMap<String, LinkedList<SiteItem>> rooms;
    LinkedList<SiteItem> lights;
    LinkedList<SiteItem> thermos;
    LinkedList<SiteItem> audioZones;
    LinkedList<SiteItem> audioSources;
    LinkedList<SiteItem> areas;
    LinkedList<SiteItem> zones;
    LinkedList<SiteItem> buttons;
    ArrayList<String> existingGroups;

    /**
     * Creates a IsyDiscoveryService.
     */
    public OmnilinkDiscoveryService(OmnilinkBridgeHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(OmnilinkBindingConstants.BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS,
                false);
        this.bridgeHandler = bridgeHandler;
    }

    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
        bridgeHandler.unregisterDiscoveryService();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scan");
        Connection c = bridgeHandler.getOmnilinkConnection();
        groups = new StringBuilder();
        items = new StringBuilder();
        rooms = new LinkedHashMap<String, LinkedList<SiteItem>>();
        lights = new LinkedList<SiteItem>();
        thermos = new LinkedList<SiteItem>();
        audioZones = new LinkedList<SiteItem>();
        audioSources = new LinkedList<SiteItem>();
        areas = new LinkedList<SiteItem>();
        zones = new LinkedList<SiteItem>();
        buttons = new LinkedList<SiteItem>();

        existingGroups = new ArrayList<String>();

        groups.append("Group All\n");
        try {
            generateUnits(c);
            generateZones(c);
            generateAreas(c);
        } catch (IOException | OmniNotConnectedException | OmniInvalidResponseException
                | OmniUnknownMessageTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void generateAreas(Connection c) throws IOException, OmniNotConnectedException,
            OmniInvalidResponseException, OmniUnknownMessageTypeException {

        int objnum = 0;
        Message m;
        // it seems that simple configurations of an omnilink have 1 area, without a name. So if there is no name for
        // the first area, we will call that Main. If other areas name is blank, we will not create a thing
        while ((m = c.reqObjectProperties(Message.OBJ_TYPE_AREA, objnum, 1, ObjectProperties.FILTER_1_NONE,
                ObjectProperties.FILTER_2_NONE, ObjectProperties.FILTER_3_NONE))
                        .getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
            AreaProperties o = ((AreaProperties) m);
            objnum = o.getNumber();

            String areaName = o.getName();
            if (o.getNumber() == 1 && "".equals(areaName)) {
                areaName = "Main Area";
            } else if ("".equals(areaName)) {
                break;
            }

            ThingUID thingUID = null;
            String thingID = "";
            String thingLabel = areaName;
            thingID = Integer.toString(objnum);

            Map<String, Object> properties = new HashMap<>(0);
            thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_AREA, thingID);
            properties.put(OmnilinkUnitConfig.NUMBER, objnum);
            properties.put(OmnilinkUnitConfig.NAME, thingLabel);

            DiscoveryResult discoveryResult;

            discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(thingLabel).build();
            thingDiscovered(discoveryResult);

        }
    }

    private void generateUnits(Connection c) throws IOException, OmniNotConnectedException,
            OmniInvalidResponseException, OmniUnknownMessageTypeException {
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        // Group Lights_GreatRoom "Great Room" (Lights)
        String groupString = "Group\t%s\t\"%s\"\t(%s)\n";

        // Dimmer Lights_GreatRoom_MainLights_Switch "Main Lights [%d%%]" (Lights_GreatRoom) {omnilink="unit:10"}
        String itemString = "%s\t%s\t\"%s\"\t(%s)\t{omnilink=\"unit:%d\"}\n";

        String groupName = "Lights";

        // Group Lights "Lights" (All)
        groups.append(String.format(groupString, groupName, "Lights", "All"));

        int objnum = 0;
        Message m;
        int currentRoom = 0;
        String currentRoomName = null;

        while ((m = c.reqObjectProperties(Message.OBJ_TYPE_UNIT, objnum, 1, ObjectProperties.FILTER_1_NAMED,
                ObjectProperties.FILTER_2_AREA_ALL, ObjectProperties.FILTER_3_ANY_LOAD))
                        .getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
            UnitProperties o = ((UnitProperties) m);
            objnum = o.getNumber();

            boolean isInRoom = false;
            boolean isRoomController = false;
            if (o.getUnitType() == UnitProperties.UNIT_TYPE_HLC_ROOM
                    || o.getObjectType() == UnitProperties.UNIT_TYPE_VIZIARF_ROOM) {
                currentRoom = objnum;

                // Lights_LivingRoom
                currentRoomName = cleanString(groupName + "_" + o.getName());

                // Make Sure we don't already have a group called this
                currentRoomName = addUniqueGroup(currentRoomName);

                groups.append(String.format(groupString, currentRoomName, o.getName(), groupName));
                rooms.put(currentRoomName, new LinkedList<SiteItem>());
                isInRoom = true;
                isRoomController = true;
            } else if (objnum < currentRoom + 8) {
                isInRoom = true;
            }

            // clean the name to remove things like spaces
            String objName = cleanString(o.getName());

            String group = isInRoom ? currentRoomName : groupName;

            // name will be the room name for the first device and roomName_deviceName for sub devices
            String name = isRoomController ? objName : group + "_" + objName;

            // the label does not have to be cleaned, so set it from the object
            String label = o.getName() + " [%d%%]";

            SiteItem light = new SiteItem(name, o.getName(), label);

            items.append(String.format(itemString, "Dimmer", name + "_Switch", label, group, objnum));

            if (isRoomController) {
                items.append(
                        String.format(itemString, "String", name + "_String", o.getName() + " [%s]", group, objnum));
            } else {
                ThingUID thingUID = null;
                String thingID = "";
                String thingLabel = name;
                thingID = Integer.toString(objnum);

                Map<String, Object> properties = new HashMap<>(0);
                thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_UNIT, thingID);
                properties.put(OmnilinkUnitConfig.NUMBER, objnum);
                properties.put(OmnilinkUnitConfig.NAME, name);

                DiscoveryResult discoveryResult;

                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(thingLabel).build();
                thingDiscovered(discoveryResult);
            }

            if (isInRoom) {
                rooms.get(currentRoomName).add(light);
            } else {
                lights.add(light);
            }

        }
    }

    /**
     * Generates zone items
     *
     * @throws IOException
     * @throws OmniNotConnectedException
     * @throws OmniInvalidResponseException
     * @throws OmniUnknownMessageTypeException
     */
    private void generateZones(Connection c) throws IOException, OmniNotConnectedException,
            OmniInvalidResponseException, OmniUnknownMessageTypeException {
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        String groupString = "Group\t%s\t\"%s\"\t(%s)\n";
        String itemString = "%s\t%s\t\"%s\"\t(%s)\t{omnilink=\"%s:%d\"}\n";
        String groupName = "Zones";
        groups.append(String.format(groupString, groupName, "Zones", "All"));

        int objnum = 0;
        Message m;
        while ((m = c.reqObjectProperties(Message.OBJ_TYPE_ZONE, objnum, 1, ObjectProperties.FILTER_1_NAMED,
                ObjectProperties.FILTER_2_AREA_ALL, ObjectProperties.FILTER_3_ANY_LOAD))
                        .getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
            ZoneProperties o = ((ZoneProperties) m);
            objnum = o.getNumber();

            ThingUID thingUID = null;
            String thingID = "";
            String thingLabel = o.getName();
            thingID = Integer.toString(objnum);

            Map<String, Object> properties = new HashMap<>(0);
            thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_ZONE, thingID);
            properties.put(OmnilinkUnitConfig.NUMBER, objnum);
            properties.put(OmnilinkUnitConfig.NAME, thingLabel);

            DiscoveryResult discoveryResult;

            discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withBridge(bridgeUID)
                    .withLabel(thingLabel).build();
            thingDiscovered(discoveryResult);
        }
    }

    private class SiteItem {
        private String name;
        private String objName;
        private String label;

        public SiteItem(String name, String objName, String label) {
            super();
            this.name = name;
            this.objName = objName;
            this.label = label;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getObjName() {
            return objName;
        }

        public void setObjName(String objName) {
            this.objName = objName;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

    }

    protected static String cleanString(String string) {
        return string.replaceAll("[^A-Za-z0-9_-]", "");
    }

    private String addUniqueGroup(String name) {
        // dont allow duplicate group names
        int i = 0;
        String tmpName = name;
        while (existingGroups.contains(tmpName)) {
            tmpName = name + (i++);
        }
        existingGroups.add(tmpName);
        return tmpName;
    }
}
