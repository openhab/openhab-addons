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
package org.openhab.binding.tapocontrol.internal.discovery;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.wifi.hub.TapoHubHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO Smart Home thing discovery
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoChildDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(TapoChildDiscoveryService.class);
    protected @NonNullByDefault({}) TapoHubHandler hub;
    private boolean backgroundDiscoveryEnabled = false;
    private String uid = "";

    /***********************************
     *
     * INITIALIZATION
     *
     ************************************/

    /**
     * INIT CLASS
     * 
     */
    public TapoChildDiscoveryService() {
        super(SUPPORTED_HUB_CHILD_TYPES_UIDS, TAPO_DISCOVERY_TIMEOUT_S, false);
    }

    /**
     * deactivate
     */
    @Override
    public void deactivate() {
        super.deactivate();
        logger.trace("({}) DiscoveryService deactivated", uid);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof TapoHubHandler hubHandler) {
            TapoHubHandler tapoHub = hubHandler;
            tapoHub.setDiscoveryService(this);
            hub = tapoHub;
            uid = hub.getUID().toString();
            activate();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.hub;
    }

    /**
     * Enable or disable backgrounddiscovery
     */
    public void setBackGroundDiscovery(boolean enableService) {
        backgroundDiscoveryEnabled = enableService;
        if (enableService) {
            super.startBackgroundDiscovery();
        } else {
            super.stopBackgroundDiscovery();
        }
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return backgroundDiscoveryEnabled;
    }

    /***********************************
     *
     * SCAN HANDLING
     *
     ************************************/

    /**
     * Start scan manually
     */
    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        if (hub != null) {
            logger.trace("({}) DiscoveryService scan started", uid);
            hub.queryDeviceData();
        }
    }

    @Override
    public synchronized void stopScan() {
        super.stopScan();
        thingsDiscovered(hub.getChildDevices());
        logger.trace("({}) DiscoveryService scan stoped", uid);
    }

    /***********************************
     *
     * handle Results
     *
     ************************************/

    /*
     * create discoveryResults and discovered things
     */
    public void thingsDiscovered(List<TapoChildDeviceData> resultData) {
        resultData.forEach(child -> {
            ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, child.getModel());
            if (SUPPORTED_HUB_CHILD_TYPES_UIDS.contains(thingTypeUID)) {
                DiscoveryResult discoveryResult = createResult(child);
                thingDiscovered(discoveryResult);
            } else {
                logger.debug("({}) Discovered unsupportet ThingType '{}'", uid, thingTypeUID);
            }
        });
    }

    /**
     * create discoveryResult (Thing) from TapoChild Object
     */
    public DiscoveryResult createResult(TapoChildDeviceData child) {
        TapoHubHandler tapoHub = this.hub;
        String deviceModel = child.getModel();
        String deviceSerial = child.getDeviceId();
        String label = getDeviceLabel(child);
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, deviceModel);

        /* create properties */
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, DEVICE_VENDOR);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, formatMac(child.getMAC(), MAC_DIVISION_CHAR));
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, child.getFirmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, child.getHardwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceModel);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceSerial);

        logger.debug("({}) device of type '{}' discovered with serial'{}'", uid, deviceModel, deviceSerial);
        if (tapoHub != null) {
            ThingUID bridgeUID = tapoHub.getUID();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceSerial);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CHILD_REPRESENTATION_PROPERTY).withBridge(bridgeUID).withLabel(label)
                    .build();
        } else {
            ThingUID thingUID = new ThingUID(BINDING_ID, deviceSerial);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CHILD_REPRESENTATION_PROPERTY).withLabel(label).build();
        }
    }

    /**
     * Get devicelabel from from TapoChild Object
     */
    protected String getDeviceLabel(TapoChildDeviceData child) {
        try {
            String deviceLabel = "";
            String deviceModel = child.getModel();
            ThingTypeUID deviceUID = new ThingTypeUID(BINDING_ID, deviceModel);

            if (SUPPORTED_SMART_CONTACTS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_SMART_CONTACT;
            } else if (SUPPORTED_MOTION_SENSORS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_MOTION_SENSOR;
            }
            return DEVICE_VENDOR + " " + deviceModel + " " + deviceLabel;
        } catch (Exception e) {
            logger.debug("({}) error getDeviceLabel", uid, e);
            return "";
        }
    }
}
