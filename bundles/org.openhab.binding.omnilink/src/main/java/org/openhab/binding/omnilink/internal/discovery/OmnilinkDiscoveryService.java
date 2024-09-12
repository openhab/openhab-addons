/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal.discovery;

import static com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties.*;
import static com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties.*;
import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.SystemType;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.binding.omnilink.internal.handler.OmnilinkBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.SystemInformation;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AccessControlReaderProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioSourceProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioZoneProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ButtonProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ThermostatProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ZoneProperties;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link OmnilinkDiscoveryService} creates things based on the configured bridge.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@Component(scope = ServiceScope.PROTOTYPE, service = OmnilinkDiscoveryService.class)
@NonNullByDefault
public class OmnilinkDiscoveryService extends AbstractThingHandlerDiscoveryService<OmnilinkBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(OmnilinkDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private Optional<SystemType> systemType = Optional.empty();
    private @Nullable List<AreaProperties> areas;

    /**
     * Creates an OmnilinkDiscoveryService.
     */
    public OmnilinkDiscoveryService() {
        super(OmnilinkBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    protected synchronized void startScan() {
        logger.debug("Starting scan");
        try {
            SystemInformation systemInformation = thingHandler.reqSystemInformation();
            this.systemType = SystemType.getType(systemInformation.getModel());
            this.areas = discoverAreas();
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
            logger.debug("Received error during discovery: {}", e.getMessage());
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
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

    /**
     * Discovers OmniLink buttons
     */
    private void discoverButtons() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final List<AreaProperties> areas = this.areas;

        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<ButtonProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(thingHandler, ObjectPropertyRequests.BUTTONS, 0, 1).selectNamed()
                        .areaFilter(areaFilter).build();

                for (ButtonProperties buttonProperties : objectPropertyRequest) {
                    String thingName = buttonProperties.getName();
                    String thingID = Integer.toString(buttonProperties.getNumber());

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(THING_PROPERTIES_NAME, thingName);
                    properties.put(THING_PROPERTIES_AREA, areaProperties.getNumber());

                    ThingUID thingUID = new ThingUID(THING_TYPE_BUTTON, bridgeUID, thingID);

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withProperty(THING_PROPERTIES_NUMBER, thingID)
                            .withRepresentationProperty(THING_PROPERTIES_NUMBER).withBridge(bridgeUID)
                            .withLabel(thingName).build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

    /**
     * Discovers OmniLink locks
     */
    private void discoverLocks() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();

        ObjectPropertyRequest<AccessControlReaderProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(thingHandler, ObjectPropertyRequests.LOCK, 0, 1).selectNamed().build();

        for (AccessControlReaderProperties lockProperties : objectPropertyRequest) {
            String thingName = lockProperties.getName();
            String thingID = Integer.toString(lockProperties.getNumber());

            Map<String, Object> properties = Map.of(THING_PROPERTIES_NAME, thingName);

            ThingUID thingUID = new ThingUID(THING_TYPE_LOCK, bridgeUID, thingID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withProperty(THING_PROPERTIES_NUMBER, thingID).withRepresentationProperty(THING_PROPERTIES_NUMBER)
                    .withBridge(bridgeUID).withLabel(thingName).build();
            thingDiscovered(discoveryResult);
        }
    }

    /**
     * Discovers OmniLink audio zones
     */
    private void discoverAudioZones() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();

        ObjectPropertyRequest<AudioZoneProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(thingHandler, ObjectPropertyRequests.AUDIO_ZONE, 0, 1).selectNamed().build();

        for (AudioZoneProperties audioZoneProperties : objectPropertyRequest) {
            String thingName = audioZoneProperties.getName();
            String thingID = Integer.toString(audioZoneProperties.getNumber());

            Map<String, Object> properties = Map.of(THING_PROPERTIES_NAME, thingName);

            ThingUID thingUID = new ThingUID(THING_TYPE_AUDIO_ZONE, bridgeUID, thingID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withProperty(THING_PROPERTIES_NUMBER, thingID).withRepresentationProperty(THING_PROPERTIES_NUMBER)
                    .withBridge(bridgeUID).withLabel(thingName).build();
            thingDiscovered(discoveryResult);
        }
    }

    /**
     * Discovers OmniLink audio sources
     */
    private void discoverAudioSources() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();

        ObjectPropertyRequest<AudioSourceProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(thingHandler, ObjectPropertyRequests.AUDIO_SOURCE, 0, 1).selectNamed().build();

        for (AudioSourceProperties audioSourceProperties : objectPropertyRequest) {
            String thingName = audioSourceProperties.getName();
            String thingID = Integer.toString(audioSourceProperties.getNumber());

            Map<String, Object> properties = Map.of(THING_PROPERTIES_NAME, thingName);

            ThingUID thingUID = new ThingUID(THING_TYPE_AUDIO_SOURCE, bridgeUID, thingID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withProperty(THING_PROPERTIES_NUMBER, thingID).withRepresentationProperty(THING_PROPERTIES_NUMBER)
                    .withBridge(bridgeUID).withLabel(thingName).build();
            thingDiscovered(discoveryResult);
        }
    }

    /**
     * Discovers OmniLink temperature sensors
     */
    private void discoverTempSensors() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final List<AreaProperties> areas = this.areas;

        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<AuxSensorProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(thingHandler, ObjectPropertyRequests.AUX_SENSORS, 0, 1).selectNamed()
                        .areaFilter(areaFilter).build();

                for (AuxSensorProperties auxSensorProperties : objectPropertyRequest) {
                    if (auxSensorProperties.getSensorType() != SENSOR_TYPE_PROGRAMMABLE_ENERGY_SAVER_MODULE
                            && auxSensorProperties.getSensorType() != SENSOR_TYPE_HUMIDITY) {
                        String thingName = auxSensorProperties.getName();
                        String thingID = Integer.toString(auxSensorProperties.getNumber());

                        Map<String, Object> properties = new HashMap<>();
                        properties.put(THING_PROPERTIES_NAME, thingName);
                        properties.put(THING_PROPERTIES_AREA, areaProperties.getNumber());

                        ThingUID thingUID = new ThingUID(THING_TYPE_TEMP_SENSOR, bridgeUID, thingID);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                .withProperties(properties).withProperty(THING_PROPERTIES_NUMBER, thingID)
                                .withRepresentationProperty(THING_PROPERTIES_NUMBER).withBridge(bridgeUID)
                                .withLabel(thingName).build();
                        thingDiscovered(discoveryResult);
                    }
                }
            }
        }
    }

    /**
     * Discovers OmniLink humidity sensors
     */
    private void discoverHumiditySensors() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final List<AreaProperties> areas = this.areas;

        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<AuxSensorProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(thingHandler, ObjectPropertyRequests.AUX_SENSORS, 0, 1).selectNamed()
                        .areaFilter(areaFilter).build();

                for (AuxSensorProperties auxSensorProperties : objectPropertyRequest) {
                    if (auxSensorProperties.getSensorType() == SENSOR_TYPE_HUMIDITY) {
                        String thingName = auxSensorProperties.getName();
                        String thingID = Integer.toString(auxSensorProperties.getNumber());

                        Map<String, Object> properties = new HashMap<>();
                        properties.put(THING_PROPERTIES_NAME, thingName);
                        properties.put(THING_PROPERTIES_AREA, areaProperties.getNumber());

                        ThingUID thingUID = new ThingUID(THING_TYPE_HUMIDITY_SENSOR, bridgeUID, thingID);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                .withProperties(properties).withProperty(THING_PROPERTIES_NUMBER, thingID)
                                .withRepresentationProperty(THING_PROPERTIES_NUMBER).withBridge(bridgeUID)
                                .withLabel(thingName).build();
                        thingDiscovered(discoveryResult);
                    }
                }
            }
        }
    }

    /**
     * Discovers OmniLink thermostats
     */
    private void discoverThermostats() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final List<AreaProperties> areas = this.areas;

        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<ThermostatProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(thingHandler, ObjectPropertyRequests.THERMOSTAT, 0, 1).selectNamed()
                        .areaFilter(areaFilter).build();

                for (ThermostatProperties thermostatProperties : objectPropertyRequest) {
                    String thingName = thermostatProperties.getName();
                    String thingID = Integer.toString(thermostatProperties.getNumber());

                    ThingUID thingUID = new ThingUID(THING_TYPE_THERMOSTAT, bridgeUID, thingID);

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(THING_PROPERTIES_NAME, thingName);
                    properties.put(THING_PROPERTIES_AREA, areaProperties.getNumber());

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withProperty(THING_PROPERTIES_NUMBER, thingID)
                            .withRepresentationProperty(THING_PROPERTIES_NUMBER).withBridge(bridgeUID)
                            .withLabel(thingName).build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

    /**
     * Discovers OmniLink areas
     */
    private @Nullable List<AreaProperties> discoverAreas() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        List<AreaProperties> areas = new LinkedList<>();

        ObjectPropertyRequest<AreaProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(thingHandler, ObjectPropertyRequests.AREA, 0, 1).build();

        for (AreaProperties areaProperties : objectPropertyRequest) {
            int thingNumber = areaProperties.getNumber();
            String thingName = areaProperties.getName();
            String thingID = Integer.toString(thingNumber);

            /*
             * It seems that for simple OmniLink Controller configurations there
             * is only 1 area, without a name. So if there is no name for the
             * first area, we will call that Main Area. If other area's name is
             * blank, we will not create a thing.
             */
            if (thingNumber == 1 && "".equals(thingName)) {
                thingName = "Main Area";
            } else if ("".equals(thingName)) {
                break;
            }

            Map<String, Object> properties = Map.of(THING_PROPERTIES_NAME, thingName);

            final String name = thingName;
            systemType.ifPresentOrElse(t -> {
                ThingUID thingUID = null;
                switch (t) {
                    case LUMINA:
                        thingUID = new ThingUID(THING_TYPE_LUMINA_AREA, bridgeUID, thingID);
                        break;
                    default:
                        thingUID = new ThingUID(THING_TYPE_OMNI_AREA, bridgeUID, thingID);
                }
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withProperty(THING_PROPERTIES_NUMBER, thingID)
                        .withRepresentationProperty(THING_PROPERTIES_NUMBER).withBridge(bridgeUID).withLabel(name)
                        .build();
                thingDiscovered(discoveryResult);
            }, () -> {
                logger.warn("Unknown System Type");
            });

            areas.add(areaProperties);
        }
        return areas;
    }

    /**
     * Discovers OmniLink supported units
     */
    private void discoverUnits() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final List<AreaProperties> areas = this.areas;

        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<UnitProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(thingHandler, ObjectPropertyRequests.UNIT, 0, 1).selectNamed().areaFilter(areaFilter)
                        .selectAnyLoad().build();

                for (UnitProperties unitProperties : objectPropertyRequest) {
                    int thingType = unitProperties.getUnitType();
                    String thingName = unitProperties.getName();
                    String thingID = Integer.toString(unitProperties.getNumber());
                    ThingUID thingUID = null;

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(THING_PROPERTIES_NAME, thingName);
                    properties.put(THING_PROPERTIES_AREA, areaProperties.getNumber());

                    switch (thingType) {
                        case UNIT_TYPE_HLC_ROOM:
                        case UNIT_TYPE_VIZIARF_ROOM:
                            thingUID = new ThingUID(THING_TYPE_ROOM, bridgeUID, thingID);
                            break;
                        case UNIT_TYPE_FLAG:
                            thingUID = new ThingUID(THING_TYPE_FLAG, bridgeUID, thingID);
                            break;
                        case UNIT_TYPE_OUTPUT:
                            thingUID = new ThingUID(THING_TYPE_OUTPUT, bridgeUID, thingID);
                            break;
                        case UNIT_TYPE_UPB:
                        case UNIT_TYPE_HLC_LOAD:
                            thingUID = new ThingUID(THING_TYPE_UNIT_UPB, bridgeUID, thingID);
                            break;
                        case UNIT_TYPE_CENTRALITE:
                        case UNIT_TYPE_RADIORA:
                        case UNIT_TYPE_VIZIARF_LOAD:
                        case UNIT_TYPE_COMPOSE:
                            thingUID = new ThingUID(THING_TYPE_DIMMABLE, bridgeUID, thingID);
                            break;
                        default:
                            thingUID = new ThingUID(THING_TYPE_UNIT, bridgeUID, thingID);
                            logger.debug("Generic unit type: {}", thingType);
                            break;
                    }

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withProperty(THING_PROPERTIES_NUMBER, thingID)
                            .withRepresentationProperty(THING_PROPERTIES_NUMBER).withBridge(bridgeUID)
                            .withLabel(thingName).build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

    /**
     * Generates zone items
     */
    private void discoverZones() {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final List<AreaProperties> areas = this.areas;

        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<ZoneProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(thingHandler, ObjectPropertyRequests.ZONE, 0, 1).selectNamed().areaFilter(areaFilter)
                        .build();

                for (ZoneProperties zoneProperties : objectPropertyRequest) {
                    if (zoneProperties.getZoneType() <= SENSOR_TYPE_PROGRAMMABLE_ENERGY_SAVER_MODULE) {
                        String thingName = zoneProperties.getName();
                        String thingID = Integer.toString(zoneProperties.getNumber());

                        Map<String, Object> properties = new HashMap<>();
                        properties.put(THING_PROPERTIES_NAME, thingName);
                        properties.put(THING_PROPERTIES_AREA, areaProperties.getNumber());

                        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, bridgeUID, thingID);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                .withProperties(properties).withProperty(THING_PROPERTIES_NUMBER, thingID)
                                .withRepresentationProperty(THING_PROPERTIES_NUMBER).withBridge(bridgeUID)
                                .withLabel(thingName).build();
                        thingDiscovered(discoveryResult);
                    }
                }
            }
        }
    }
}
