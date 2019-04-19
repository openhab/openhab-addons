/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.discovery;

import static com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.SystemType;
import org.openhab.binding.omnilink.handler.BridgeOfflineException;
import org.openhab.binding.omnilink.handler.OmnilinkBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniNotConnectedException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.SystemInformation;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioSourceProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioZoneProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ButtonProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.LockProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ThermostatProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ZoneProperties;

/**
 *
 * @author Craig Hamilton
 *
 */
public class OmnilinkDiscoveryService extends AbstractDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(OmnilinkDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private OmnilinkBridgeHandler bridgeHandler;
    private SystemType systemType;
    private List<AreaProperties> areas;

    private final static Set<Integer> TEMP_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(AuxSensorProperties.SENSOR_TYPE_EXTENDED_RANGE_OUTDOOR_TEMPERATURE,
                    AuxSensorProperties.SENSOR_TYPE_EXTENDED_RANGE_TEMPERATURE,
                    AuxSensorProperties.SENSOR_TYPE_EXTENDED_RANGE_TEMPERATURE_ALARM,
                    AuxSensorProperties.SENSOR_TYPE_OUTDOOR_TEMPERATURE, AuxSensorProperties.SENSOR_TYPE_TEMPERATURE,
                    AuxSensorProperties.SENSOR_TYPE_TEMPERATURE_ALARM).collect(Collectors.toSet()));

    /**
     * Creates an OmnilinkDiscoveryService.
     */
    public OmnilinkDiscoveryService(OmnilinkBridgeHandler bridgeHandler) {
        super(Collections.singleton(new ThingTypeUID(OmnilinkBindingConstants.BINDING_ID, "-")),
                DISCOVER_TIMEOUT_SECONDS, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scan");
        try {
            SystemInformation info = bridgeHandler.reqSystemInformation();
            systemType = SystemType.getType(info.getModel());
            areas = discoverAreas();
            discoverUnits();
            discoverZones();
            discoverButtons();
            discoverThermostats();
            discoverAudioZones();
            discoverAudioSources();
            discoverTempSensors();
            discoverHumiditySensors();
            discoverLocks();
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received error during discovery", e);
        }
    }

    /**
     * Calculate the area filter the a supplied area
     *
     * @param area Area to calculate filter for.
     * @return Calculated Bit Filter for the supplied area. Bit 0 is area 1, bit 2 is area 2 and so on.
     */
    private static int bitFilterForArea(AreaProperties areaProperties) {
        return BigInteger.ZERO.setBit(areaProperties.getNumber() - 1).intValue();
    }

    private void discoverButtons()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        for (AreaProperties areaProperties : areas) {

            int areaFilter = bitFilterForArea(areaProperties);

            ObjectPropertyRequest<ButtonProperties> objectPropertyRequest = ObjectPropertyRequest
                    .builder(bridgeHandler, ObjectPropertyRequests.BUTTONS).selectNamed().areaFilter(areaFilter)
                    .build();

            for (ButtonProperties buttonProperties : objectPropertyRequest) {

                int objnum = buttonProperties.getNumber();
                Map<String, Object> properties = new HashMap<>();
                ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_BUTTON,
                        bridgeHandler.getThing().getUID(), Integer.toString(objnum));
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, buttonProperties.getName());
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(buttonProperties.getName())
                        .build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    private void discoverLocks()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        ObjectPropertyRequest<LockProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(bridgeHandler, ObjectPropertyRequests.LOCK).selectNamed().build();

        for (LockProperties objectProperties : objectPropertyRequest) {

            ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_LOCK,
                    Integer.toString(objectProperties.getNumber()));

            Map<String, Object> properties = new HashMap<>();
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objectProperties.getNumber());
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, objectProperties.getName());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(objectProperties.getName()).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void discoverAudioZones()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        ObjectPropertyRequest<AudioZoneProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(bridgeHandler, ObjectPropertyRequests.AUDIO_ZONE).selectNamed().build();

        for (AudioZoneProperties objectProperties : objectPropertyRequest) {

            ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_AUDIO_ZONE,
                    Integer.toString(objectProperties.getNumber()));

            Map<String, Object> properties = new HashMap<>();
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objectProperties.getNumber());
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, objectProperties.getName());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(objectProperties.getName()).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void discoverAudioSources()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        ObjectPropertyRequest<AudioSourceProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(bridgeHandler, ObjectPropertyRequests.AUDIO_SOURCE).selectNamed().build();

        for (AudioSourceProperties objectProperties : objectPropertyRequest) {

            ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_AUDIO_SOURCE,
                    Integer.toString(objectProperties.getNumber()));

            Map<String, Object> properties = new HashMap<>();
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objectProperties.getNumber());
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, objectProperties.getName());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(objectProperties.getName()).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void discoverTempSensors()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        for (AreaProperties areaProperties : areas) {

            int areaFilter = bitFilterForArea(areaProperties);

            ObjectPropertyRequest<AuxSensorProperties> objectPropertyRequest = ObjectPropertyRequest
                    .builder(bridgeHandler, ObjectPropertyRequests.AUX_SENSORS).selectNamed().areaFilter(areaFilter)
                    .build();

            for (AuxSensorProperties objectProperties : objectPropertyRequest) {

                if (TEMP_SENSOR_TYPES.contains(objectProperties.getSensorType())) {

                    ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_TEMP_SENSOR,
                            bridgeHandler.getThing().getUID(), Integer.toString(objectProperties.getNumber()));

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objectProperties.getNumber());
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, objectProperties.getName());
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(objectProperties.getName())
                            .build();
                    thingDiscovered(discoveryResult);
                }
            }
        }

    }

    private void discoverHumiditySensors()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        for (AreaProperties areaProperties : areas) {

            int areaFilter = bitFilterForArea(areaProperties);

            ObjectPropertyRequest<AuxSensorProperties> objectPropertyRequest = ObjectPropertyRequest
                    .builder(bridgeHandler, ObjectPropertyRequests.AUX_SENSORS).selectNamed().areaFilter(areaFilter)
                    .build();

            for (AuxSensorProperties objectProperties : objectPropertyRequest) {

                if (objectProperties.getSensorType() == AuxSensorProperties.SENSOR_TYPE_HUMIDITY) {

                    ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_HUMIDITY_SENSOR,
                            bridgeHandler.getThing().getUID(), Integer.toString(objectProperties.getNumber()));

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objectProperties.getNumber());
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, objectProperties.getName());
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(objectProperties.getName())
                            .build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

    private void discoverThermostats()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        for (AreaProperties areaProperties : areas) {
            int areaFilter = bitFilterForArea(areaProperties);

            ObjectPropertyRequest<ThermostatProperties> objectPropertyRequest = ObjectPropertyRequest
                    .builder(bridgeHandler, ObjectPropertyRequests.THERMOSTAT).selectNamed().areaFilter(areaFilter)
                    .build();

            for (ThermostatProperties thermostatProperties : objectPropertyRequest) {

                ThingUID thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_THERMOSTAT,
                        bridgeHandler.getThing().getUID(), Integer.toString(thermostatProperties.getNumber()));

                Map<String, Object> properties = new HashMap<>();
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, thermostatProperties.getNumber());
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, thermostatProperties.getName());
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(thermostatProperties.getName())
                        .build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    private List<AreaProperties> discoverAreas()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {

        ObjectPropertyRequest<AreaProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(bridgeHandler, ObjectPropertyRequests.AREA).build();

        List<AreaProperties> areas = new LinkedList<>();

        for (AreaProperties areaProperties : objectPropertyRequest) {

            int objnum = areaProperties.getNumber();

            // it seems that simple configurations of an omnilink have 1 area, without a name. So if there is no
            // name
            // for
            // the first area, we will call that Main. If other areas name is blank, we will not create a thing
            String areaName = areaProperties.getName();
            if (areaProperties.getNumber() == 1 && "".equals(areaName)) {
                areaName = "Main Area";
            } else if ("".equals(areaName)) {
                break;
            }

            Map<String, Object> properties = new HashMap<>();
            ThingUID thingUID;
            switch (systemType) {
                case LUMINA:
                    thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_LUMINA_AREA,
                            bridgeHandler.getThing().getUID(), Integer.toString(objnum));
                    break;
                case OMNI:
                    thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_OMNI_AREA,
                            bridgeHandler.getThing().getUID(), Integer.toString(objnum));
                    break;
                default:
                    throw new IllegalStateException("Unknown System Type");
            }

            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
            properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, areaName);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(this.bridgeHandler.getThing().getUID()).withLabel(areaName).build();
            thingDiscovered(discoveryResult);
            areas.add(areaProperties);
        }
        return areas;
    }

    private void discoverUnits()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        final ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();

        for (AreaProperties areaProperties : areas) {
            int areaFilter = bitFilterForArea(areaProperties);

            ObjectPropertyRequest<UnitProperties> objectPropertyRequest = ObjectPropertyRequest
                    .builder(bridgeHandler, ObjectPropertyRequests.UNIT).selectNamed().areaFilter(areaFilter)
                    .selectAnyLoad().build();

            String currentRoomName = "";

            for (UnitProperties unitProperties : objectPropertyRequest) {

                int objnum = unitProperties.getNumber();

                String thingLabel = unitProperties.getName();
                String thingID = Integer.toString(objnum);

                Map<String, Object> properties = new HashMap<>();

                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, unitProperties.getName());
                properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                ThingUID thingUID = null;

                switch (unitProperties.getUnitType()) {
                    case UNIT_TYPE_HLC_ROOM:
                    case UNIT_TYPE_VIZIARF_ROOM:
                        thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_ROOM,
                                bridgeHandler.getThing().getUID(), thingID);
                        currentRoomName = unitProperties.getName();
                        break;
                    case UNIT_TYPE_FLAG:
                        thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_FLAG,
                                bridgeHandler.getThing().getUID(), thingID);
                        break;
                    case UNIT_TYPE_OUTPUT:
                        thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_OUTPUT,
                                bridgeHandler.getThing().getUID(), thingID);
                        break;
                    case UNIT_TYPE_UPB:
                    case UNIT_TYPE_HLC_LOAD:
                        thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_UNIT_UPB,
                                bridgeHandler.getThing().getUID(), thingID);
                        break;
                    case UNIT_TYPE_CENTRALITE:
                    case UNIT_TYPE_RADIORA:
                    case UNIT_TYPE_VIZIARF_LOAD:
                    case UNIT_TYPE_COMPOSE:
                        thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_DIMMABLE,
                                bridgeHandler.getThing().getUID(), thingID);
                        break;
                    default:
                        thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_UNIT,
                                bridgeHandler.getThing().getUID(), thingID);
                        logger.debug("Generic unit type: {}", unitProperties.getUnitType());
                        break;

                }

                switch (unitProperties.getUnitType()) {
                    case UNIT_TYPE_UPB:
                    case UNIT_TYPE_HLC_LOAD:
                    case UNIT_TYPE_CENTRALITE:
                    case UNIT_TYPE_RADIORA:
                    case UNIT_TYPE_VIZIARF_LOAD:
                    case UNIT_TYPE_COMPOSE:
                        // let's prepend room name to unit name for label
                        // TODO could make this configurable
                        thingLabel = currentRoomName + ": " + unitProperties.getName();
                        break;
                }

                thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties).withBridge(bridgeUID)
                        .withLabel(thingLabel).build());

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
    private void discoverZones()
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();

        for (AreaProperties areaProperties : areas) {

            int areaFilter = bitFilterForArea(areaProperties);

            ObjectPropertyRequest<ZoneProperties> objectPropertyRequest = ObjectPropertyRequest
                    .builder(bridgeHandler, ObjectPropertyRequests.ZONE).selectNamed().areaFilter(areaFilter).build();

            for (ZoneProperties zoneProperties : objectPropertyRequest) {

                if (TEMP_SENSOR_TYPES.contains(zoneProperties.getZoneType()) == false
                        && zoneProperties.getZoneType() != AuxSensorProperties.SENSOR_TYPE_HUMIDITY) {

                    int objnum = zoneProperties.getNumber();

                    ThingUID thingUID = null;
                    String thingID = Integer.toString(objnum);
                    String thingLabel = zoneProperties.getName();

                    Map<String, Object> properties = new HashMap<>();
                    thingUID = new ThingUID(OmnilinkBindingConstants.THING_TYPE_ZONE, bridgeHandler.getThing().getUID(),
                            thingID);
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER, objnum);
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_NAME, thingLabel);
                    properties.put(OmnilinkBindingConstants.THING_PROPERTIES_AREA, areaProperties.getNumber());

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeUID).withLabel(thingLabel).build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

}
