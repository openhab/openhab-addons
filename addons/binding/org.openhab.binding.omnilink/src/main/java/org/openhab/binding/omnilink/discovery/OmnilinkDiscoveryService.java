package org.openhab.binding.omnilink.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.handler.BridgeOfflineException;
import org.openhab.binding.omnilink.handler.OmnilinkBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniNotConnectedException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectProperties;
import com.digitaldan.jomnilinkII.MessageTypes.SystemInformation;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ButtonProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ConsoleProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ZoneProperties;
import com.google.common.collect.ImmutableSet;

public class OmnilinkDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(OmnilinkDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private OmnilinkBridgeHandler bridgeHandler;
    private boolean isLumina = false;
    private LinkedList<AreaProperties> areas;

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
        areas = new LinkedList<>();
        // OBJ_TYPE_BUTTON
        try {
            SystemInformation info = bridgeHandler.reqSystemInformation();
            isLumina = info.getModel() == 36 || info.getModel() == 37;
            generateAreas();
            generateUnits();
            generateZones();
            generateButtons();

            // generate consoles is throwing and error
            // generateConsoles();
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received error during discovery", e);
        }
    }

    private void generateButtons()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        for (AreaProperties areaProperties : areas) {
            int areaFilter = 0;
            // Area filter returns areas in each bit. So bit 0 is area 1, bit 2 is area 2 and so on.
            areaFilter |= 1 << (areaProperties.getNumber() - 1);

            int objnum = 0;
            Message m;

            while ((m = bridgeHandler.reqObjectProperties(Message.OBJ_TYPE_BUTTON, objnum, 1,
                    ObjectProperties.FILTER_1_NAMED, areaFilter, ObjectProperties.FILTER_3_NONE))
                            .getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
                ButtonProperties o = ((ButtonProperties) m);
                objnum = o.getNumber();
                Map<String, Object> properties = new HashMap<>(0);
                ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_BUTTON,
                        bridgeHandler.getThing().getUID(), Integer.toString(objnum));
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, o.getName());
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                DiscoveryResult discoveryResult;

                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(o.getName()).build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    private void generateConsoles()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        for (AreaProperties areaProperties : areas) {
            int areaFilter = 0;
            // Area filter returns areas in each bit. So bit 0 is area 1, bit 2 is area 2 and so on.
            areaFilter |= 1 << (areaProperties.getNumber() - 1);

            int objnum = 0;
            Message m;

            while ((m = bridgeHandler.reqObjectProperties(Message.OBJ_TYPE_CONSOLE, objnum, 1,
                    ObjectProperties.FILTER_1_NONE, areaFilter, ObjectProperties.FILTER_3_NONE))
                            .getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
                ConsoleProperties o = ((ConsoleProperties) m);
                objnum = o.getNumber();
                Map<String, Object> properties = new HashMap<>(0);
                ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_CONSOLE,
                        bridgeHandler.getThing().getUID(), Integer.toString(objnum));
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
                // properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, o.getName());
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                DiscoveryResult discoveryResult;

                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(o.getName()).build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    private void generateAreas()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        int objnum = 0;
        Message m;
        // it seems that simple configurations of an omnilink have 1 area, without a name. So if there is no name for
        // the first area, we will call that Main. If other areas name is blank, we will not create a thing
        while ((m = bridgeHandler.reqObjectProperties(Message.OBJ_TYPE_AREA, objnum, 1, ObjectProperties.FILTER_1_NONE,
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

            Map<String, Object> properties = new HashMap<>(0);
            ThingUID thingUID;
            if (isLumina) {
                thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_LUMINA_AREA,
                        bridgeHandler.getThing().getUID(), Integer.toString(objnum));
            } else {
                thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_OMNI_AREA,
                        bridgeHandler.getThing().getUID(), Integer.toString(objnum));
            }
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, areaName);

            DiscoveryResult discoveryResult;

            discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(areaName).build();
            thingDiscovered(discoveryResult);
            areas.add(o);

        }
    }

    private void generateUnits()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        // Group Lights_GreatRoom "Great Room" (Lights)
        // String groupString = "Group\t%s\t\"%s\"\t(%s)\n";

        // Dimmer Lights_GreatRoom_MainLights_Switch "Main Lights [%d%%]" (Lights_GreatRoom) {omnilink="unit:10"}
        // String itemString = "%s\t%s\t\"%s\"\t(%s)\t{omnilink=\"unit:%d\"}\n";

        // String groupName = "Lights";

        for (AreaProperties areaProperties : areas) {
            int areaFilter = 0;
            // Area filter returns areas in each bit. So bit 0 is area 1, bit 2 is area 2 and so on.
            areaFilter |= 1 << (areaProperties.getNumber() - 1);

            int objnum = 0;
            Message m;
            int currentRoom = 0;
            String currentRoomName = null;

            while ((m = bridgeHandler.reqObjectProperties(Message.OBJ_TYPE_UNIT, objnum, 1,
                    ObjectProperties.FILTER_1_NAMED, areaFilter, ObjectProperties.FILTER_3_ANY_LOAD))
                            .getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
                UnitProperties o = ((UnitProperties) m);
                objnum = o.getNumber();

                // boolean isInRoom = false;
                boolean isRoomController = false;
                logger.debug("Unit type: {}", o.getUnitType());
                if (o.getUnitType() == UnitProperties.UNIT_TYPE_HLC_ROOM
                        || o.getObjectType() == UnitProperties.UNIT_TYPE_VIZIARF_ROOM) {
                    currentRoom = objnum;
                    currentRoomName = o.getName();
                    isRoomController = true;
                } else if (objnum < currentRoom + 8) {
                    // isInRoom = true;
                }

                ThingUID thingUID = null;
                String thingID = "";
                String thingLabel = o.getName();
                thingID = Integer.toString(objnum);

                Map<String, Object> properties = new HashMap<>(0);

                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, o.getName());
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                DiscoveryResult discoveryResult;
                if (isRoomController) {
                    discoveryResult = DiscoveryResultBuilder
                            .create(new ThingUID(OmnilinkBindingConstants.THING_TYPE_ROOM,
                                    bridgeHandler.getThing().getUID(), thingID))
                            .withProperties(properties).withBridge(bridgeUID).withLabel(thingLabel).build();
                } else {
                    if (o.getUnitType() == UnitProperties.UNIT_TYPE_FLAG) {
                        thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_FLAG,
                                bridgeHandler.getThing().getUID(), thingID);

                    } else {
                        if (o.getUnitType() == UnitProperties.UNIT_TYPE_UPB
                                || o.getUnitType() == UnitProperties.UNIT_TYPE_HLC_LOAD) {
                            thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_UNIT_UPB,
                                    bridgeHandler.getThing().getUID(), thingID);
                        } else {
                            logger.debug("Unsupported unit type: {}", o.getUnitType());
                        }
                        // let's prepend room name to unit name for label
                        // TODO could make this configurable
                        thingLabel = currentRoomName + ": " + o.getName();
                    }
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeUID).withLabel(thingLabel).build();
                }
                thingDiscovered(discoveryResult);
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
     * @throws BridgeOfflineException
     */
    private void generateZones()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        // String groupString = "Group\t%s\t\"%s\"\t(%s)\n";
        // String itemString = "%s\t%s\t\"%s\"\t(%s)\t{omnilink=\"%s:%d\"}\n";
        // String groupName = "Zones";

        for (AreaProperties areaProperties : areas) {
            int objnum = 0;
            Message m;
            int areaFilter = 0;
            // Area filter returns areas in each bit. So bit 0 is area 1, bit 2 is area 2 and so on.
            areaFilter |= 1 << (areaProperties.getNumber() - 1);

            while ((m = bridgeHandler.reqObjectProperties(Message.OBJ_TYPE_ZONE, objnum, 1,
                    ObjectProperties.FILTER_1_NAMED, areaFilter, ObjectProperties.FILTER_3_ANY_LOAD))
                            .getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
                ZoneProperties o = ((ZoneProperties) m);
                objnum = o.getNumber();

                ThingUID thingUID = null;
                String thingID = "";
                String thingLabel = o.getName();
                thingID = Integer.toString(objnum);

                Map<String, Object> properties = new HashMap<>(0);
                thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_ZONE, bridgeHandler.getThing().getUID(),
                        thingID);
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, thingLabel);
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                DiscoveryResult discoveryResult;

                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(thingLabel).build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    protected static String cleanString(String string) {
        return string.replaceAll("[^A-Za-z0-9_-]", "");
    }

}
