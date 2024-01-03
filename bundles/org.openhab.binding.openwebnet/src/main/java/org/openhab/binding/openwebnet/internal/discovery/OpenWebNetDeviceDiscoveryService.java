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
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openwebnet4j.OpenDeviceType;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAlarm;
import org.openwebnet4j.message.WhereThermo;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetDeviceDiscoveryService} is responsible for discovering
 * OpenWebNet devices connected to a bridge/gateway
 *
 * @author Massimo Valla - Initial contribution
 * @author Andrea Conte - Energy management, Thermoregulation
 * @author Gilberto Cocchi - Thermoregulation
 * @author Giovanni Fabiani - Aux support
 */
@Component(scope = ServiceScope.PROTOTYPE, service = OpenWebNetDeviceDiscoveryService.class)
@NonNullByDefault
public class OpenWebNetDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<OpenWebNetBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(OpenWebNetDeviceDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.DEVICE_SUPPORTED_THING_TYPES;
    private static final int SEARCH_TIME_SEC = 60;

    private @NonNullByDefault({}) ThingUID bridgeUID;

    private boolean cuFound = false;

    public OpenWebNetDeviceDiscoveryService() {
        super(OpenWebNetBridgeHandler.class, SUPPORTED_THING_TYPES, SEARCH_TIME_SEC);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        logger.info("------ SEARCHING for DEVICES on bridge '{}' ({}) ...", thingHandler.getThing().getLabel(),
                bridgeUID);
        cuFound = false;
        thingHandler.searchDevices();
    }

    @Override
    protected void stopScan() {
        logger.debug("------ stopScan() on bridge '{}'", bridgeUID);
        thingHandler.scanStopped();
    }

    @Override
    public void abortScan() {
        logger.debug("------ abortScan() on bridge '{}'", bridgeUID);
        thingHandler.scanStopped();
    }

    /**
     * Create and notify to Inbox a new DiscoveryResult based on WHERE,
     * OpenDeviceType and BaseOpenMessage
     *
     * @param where the discovered device's address (WHERE)
     * @param deviceType {@link OpenDeviceType} of the discovered device
     * @param baseMsg the OWN message received that identified the device
     *            (optional)
     */
    public void newDiscoveryResult(@Nullable Where where, OpenDeviceType deviceType,
            @Nullable BaseOpenMessage baseMsg) {
        logger.debug("newDiscoveryResult() WHERE={}, deviceType={}", where, deviceType);
        ThingTypeUID thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_GENERIC_DEVICE; // generic device
        String thingLabel = OpenWebNetBindingConstants.THING_LABEL_GENERIC_DEVICE;
        Who deviceWho = Who.UNKNOWN;
        switch (deviceType) {
            case ZIGBEE_ON_OFF_SWITCH:
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_ZB_ON_OFF_SWITCH;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_ZB_ON_OFF_SWITCH;
                deviceWho = Who.LIGHTING;
                break;
            case ZIGBEE_DIMMER_SWITCH:
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_ZB_DIMMER;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_ZB_DIMMER;
                deviceWho = Who.LIGHTING;
                break;
            case SCS_ON_OFF_SWITCH:
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_ON_OFF_SWITCH;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_ON_OFF_SWITCH;
                deviceWho = Who.LIGHTING;
                break;
            case SCS_DIMMER_SWITCH:
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_DIMMER;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_DIMMER;
                deviceWho = Who.LIGHTING;
                break;
            case SCS_SHUTTER_SWITCH:
            case SCS_SHUTTER_CONTROL: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_AUTOMATION;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_AUTOMATION;
                deviceWho = Who.AUTOMATION;
                break;
            }
            case ZIGBEE_SHUTTER_SWITCH:
            case ZIGBEE_SHUTTER_CONTROL: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_ZB_AUTOMATION;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_ZB_AUTOMATION;
                deviceWho = Who.AUTOMATION;
                break;
            }
            case SCS_THERMO_SENSOR: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_SENSOR;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_THERMO_SENSOR;
                deviceWho = Who.THERMOREGULATION;
                break;
            }
            case SCS_THERMO_ZONE: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_ZONE;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_THERMO_ZONE;
                deviceWho = Who.THERMOREGULATION;
                break;
            }
            case SCS_THERMO_CENTRAL_UNIT: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_CU;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_THERMO_CU;
                deviceWho = Who.THERMOREGULATION;
                break;
            }
            case SCS_ENERGY_METER: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_ENERGY_METER;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_ENERGY_METER;
                deviceWho = Who.ENERGY_MANAGEMENT;
                break;
            }
            case BASIC_SCENARIO: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_SCENARIO;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_SCENARIO;
                deviceWho = Who.SCENARIO;
                break;
            }
            case SCENARIO_CONTROL: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_CEN_SCENARIO_CONTROL;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_CEN_SCENARIO_CONTROL;
                deviceWho = Who.CEN_SCENARIO_SCHEDULER;
                break;
            }
            case SCS_DRY_CONTACT_IR: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_DRY_CONTACT_IR;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_DRY_CONTACT_IR;
                deviceWho = Who.CEN_PLUS_SCENARIO_SCHEDULER;
                break;
            }
            case MULTIFUNCTION_SCENARIO_CONTROL: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_CENPLUS_SCENARIO_CONTROL;
                deviceWho = Who.CEN_PLUS_SCENARIO_SCHEDULER;
                break;
            }
            case SCS_AUXILIARY_TOGGLE_CONTROL: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_AUX;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_AUX;
                deviceWho = Who.AUX;
                break;
            }
            case SCS_ALARM_CENTRAL_UNIT: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_ALARM_SYSTEM;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_ALARM_SYSTEM;
                deviceWho = Who.BURGLAR_ALARM;
                break;
            }
            case SCS_ALARM_ZONE: {
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_ALARM_ZONE;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_ALARM_ZONE;
                deviceWho = Who.BURGLAR_ALARM;
                break;
            }
            default:
                logger.warn("Device type {} is not supported, default to GENERIC device (WHERE={})", deviceType, where);
                if (where instanceof WhereZigBee) {
                    thingLabel = "Zigbee " + thingLabel;
                }
                if (baseMsg != null) {
                    deviceWho = baseMsg.getWho();
                }
        }
        Where w;
        if (where != null) {
            w = where;
        } else if (OpenWebNetBindingConstants.THING_TYPE_BUS_ALARM_SYSTEM.equals(thingTypeUID)) {
            w = new WhereAlarm("0");
        } else {
            logger.debug("ignoring newDiscoveryResult with null where: {}", baseMsg);
            return;
        }

        String ownId = thingHandler.ownIdFromWhoWhere(deviceWho, w);
        if (OpenWebNetBindingConstants.THING_TYPE_BUS_ON_OFF_SWITCH.equals(thingTypeUID)) {
            if (thingHandler.getRegisteredDevice(ownId) != null) {
                logger.debug("dimmer/switch with WHERE={} already registered, skipping this discovery result", w);
                return;
            }
        }

        String tId = thingHandler.thingIdFromWhere(w);
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, tId);

        DiscoveryResult discoveryResult = null;

        String whereConfig = w.value();

        // remove # from discovered alarm zone
        if (OpenWebNetBindingConstants.THING_TYPE_BUS_ALARM_ZONE.equals(thingTypeUID)) {
            whereConfig = "" + ((WhereAlarm) w).getZone();
        }

        Map<String, Object> properties = new HashMap<>(2);

        // detect Thermo CU type
        if (OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_CU.equals(thingTypeUID)) {
            cuFound = true;
            logger.debug("CU found: {}", w);
            if (w.value().charAt(0) == '#') { // 99-zone CU
                thingLabel += " 99-zone";
                logger.debug("@@@@@ THERMO CU found 99-zone: where={}, ownId={}, whereConfig={}", w, ownId,
                        whereConfig);
            } else {
                thingLabel += " 4-zone";
                whereConfig = "#" + w.value();
                logger.debug("@@@@ THERMO CU found 4-zone: where={}, ownId={}, whereConfig={}", w, ownId, whereConfig);
            }
        } else if (OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_ZONE.equals(thingTypeUID)) {
            if (cuFound) {
                // set param standalone = false for thermo zone
                properties.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_STANDALONE, false);
            }
            whereConfig = "" + ((WhereThermo) w).getZone();
            logger.debug("@@@@@ THERMO ZONE found: where={}, ownId={}, whereConfig={}, standalone={}", w, ownId,
                    whereConfig, properties.get(OpenWebNetBindingConstants.CONFIG_PROPERTY_STANDALONE));
        }

        if (w instanceof WhereZigBee whereZigBee && WhereZigBee.UNIT_02.equals(whereZigBee.getUnit())) {
            logger.debug("UNIT=02 found (WHERE={}) -> will remove previous result if exists", w);
            thingRemoved(thingUID); // remove previously discovered thing
            // re-create thingUID with new type
            thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS;
            thingLabel = OpenWebNetBindingConstants.THING_LABEL_ZB_ON_OFF_SWITCH_2UNITS;
            thingUID = new ThingUID(thingTypeUID, bridgeUID, tId);
            whereConfig = whereZigBee.valueWithUnit(WhereZigBee.UNIT_ALL); // replace unit '02' with '00'
            logger.debug("UNIT=02, switching type from {} to {}",
                    OpenWebNetBindingConstants.THING_TYPE_ZB_ON_OFF_SWITCH,
                    OpenWebNetBindingConstants.THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS);
        }
        properties.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_WHERE, whereConfig);
        properties.put(OpenWebNetBindingConstants.PROPERTY_OWNID, ownId);
        if (OpenWebNetBindingConstants.THING_TYPE_GENERIC_DEVICE.equals(thingTypeUID)) {
            thingLabel = thingLabel + " (WHO=" + deviceWho + ", WHERE=" + whereConfig + ")";
        } else {
            thingLabel = thingLabel + " (WHERE=" + whereConfig + ")";
        }
        discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                .withRepresentationProperty(OpenWebNetBindingConstants.PROPERTY_OWNID).withBridge(bridgeUID)
                .withLabel(thingLabel).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void initialize() {
        thingHandler.deviceDiscoveryService = this;
        bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }
}
