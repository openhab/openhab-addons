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
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.handler.OpenWebNetBridgeHandler;
import org.openwebnet4j.OpenDeviceType;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetDeviceDiscoveryService} is responsible for discovering OpenWebNet devices connected to a
 * bridge/gateway
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetDeviceDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.DEVICE_SUPPORTED_THING_TYPES;
    private static final int SEARCH_TIME_SEC = 60;

    private @NonNullByDefault({}) OpenWebNetBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    public OpenWebNetDeviceDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME_SEC);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return OpenWebNetDeviceDiscoveryService.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        logger.info("------ SEARCHING for DEVICES on bridge '{}' ({}) ...", bridgeHandler.getThing().getLabel(),
                bridgeUID);
        bridgeHandler.searchDevices();
    }

    @Override
    protected void stopScan() {
        logger.debug("------ stopScan() on bridge '{}'", bridgeUID);
        bridgeHandler.scanStopped();
    }

    @Override
    public void abortScan() {
        logger.debug("------ abortScan() on bridge '{}'", bridgeUID);
        bridgeHandler.scanStopped();
    }

    /**
     * Create and notify to Inbox a new DiscoveryResult based on WHERE, OpenDeviceType and BaseOpenMessage
     *
     * @param where the discovered device's address (WHERE)
     * @param deviceType {@link OpenDeviceType} of the discovered device
     * @param message the OWN message received that identified the device (optional)
     */
    public void newDiscoveryResult(Where where, OpenDeviceType deviceType, @Nullable BaseOpenMessage baseMsg) {
        logger.info("newDiscoveryResult() WHERE={}, deviceType={}", where, deviceType);
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
            default:
                logger.warn("Device type {} is not supported, default to GENERIC device (WHERE={})", deviceType, where);
                if (where instanceof WhereZigBee) {
                    thingLabel = "ZigBee " + thingLabel;
                }
                if (baseMsg != null) {
                    deviceWho = baseMsg.getWho();
                }
        }
        String tId = bridgeHandler.thingIdFromWhere(where);
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, tId);

        DiscoveryResult discoveryResult = null;

        String whereLabel = where.value();
        if (where instanceof WhereZigBee && WhereZigBee.UNIT_02.equals(((WhereZigBee) where).getUnit())) {
            logger.debug("UNIT=02 found (WHERE={})", where);
            logger.debug("will remove previous result if exists");
            thingRemoved(thingUID); // remove previously discovered thing
            // re-create thingUID with new type
            thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS;
            thingLabel = OpenWebNetBindingConstants.THING_LABEL_ZB_ON_OFF_SWITCH_2UNITS;
            thingUID = new ThingUID(thingTypeUID, bridgeUID, tId);
            whereLabel = whereLabel.replace("02#", "00#"); // replace unit '02' with all unit '00'
            logger.debug("UNIT=02, switching type from {} to {}",
                    OpenWebNetBindingConstants.THING_TYPE_ZB_ON_OFF_SWITCH,
                    OpenWebNetBindingConstants.THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS);
        }
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_WHERE, bridgeHandler.normalizeWhere(where));
        properties.put(OpenWebNetBindingConstants.PROPERTY_OWNID, bridgeHandler.ownIdFromWhoWhere(where, deviceWho));
        if (thingTypeUID == OpenWebNetBindingConstants.THING_TYPE_GENERIC_DEVICE) {
            thingLabel = thingLabel + " (WHO=" + deviceWho + ", WHERE=" + whereLabel + ")";
        } else {
            thingLabel = thingLabel + " (WHERE=" + whereLabel + ")";
        }
        discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                .withRepresentationProperty(OpenWebNetBindingConstants.PROPERTY_OWNID).withBridge(bridgeUID)
                .withLabel(thingLabel).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OpenWebNetBridgeHandler) {
            logger.debug("attaching {} to handler {} ", this, handler);
            bridgeHandler = (OpenWebNetBridgeHandler) handler;
            bridgeHandler.deviceDiscoveryService = this;
            bridgeUID = bridgeHandler.getThing().getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }
}
