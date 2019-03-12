/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.discovery;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.verisure.internal.VerisureHandlerFactory;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.handler.VerisureBridgeHandler;
import org.openhab.binding.verisure.internal.model.VerisureAlarmJSON;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnectionJSON;
import org.openhab.binding.verisure.internal.model.VerisureClimateBaseJSON;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindowJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;
import org.openhab.binding.verisure.internal.model.VerisureUserPresenceJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The discovery service, notified by a listener on the VerisureSession.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureThingDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME = 60;
    private final Logger logger = LoggerFactory.getLogger(VerisureThingDiscoveryService.class);

    private @Nullable VerisureBridgeHandler verisureBridgeHandler;

    public VerisureThingDiscoveryService(VerisureBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(VerisureHandlerFactory.SUPPORTED_THING_TYPES, SEARCH_TIME);

        this.verisureBridgeHandler = bridgeHandler;

    }

    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        logger.debug("VerisureThingDiscoveryService:startScan");

        if (verisureBridgeHandler != null) {
            VerisureSession session = verisureBridgeHandler.getSession();
            if (session != null) {
                HashMap<String, VerisureThingJSON> verisureThings = session.getVerisureThings();
                for (Map.Entry<String, VerisureThingJSON> entry : verisureThings.entrySet()) {
                    VerisureThingJSON thing = entry.getValue();
                    if (thing != null) {
                        logger.info(thing.toString());
                        onThingAddedInternal(thing);
                    }
                }
            }
        }
    }

    private void onThingAddedInternal(VerisureThingJSON thing) {
        logger.debug("VerisureThingDiscoveryService:OnThingAddedInternal");
        ThingUID thingUID = getThingUID(thing);
        String deviceId = thing.getDeviceId();
        if (thingUID != null && deviceId != null) {
            if (verisureBridgeHandler != null) {
                ThingUID bridgeUID = verisureBridgeHandler.getThing().getUID();
                String label = "Device Id: " + deviceId;
                if (thing.getLocation() != null) {
                    label += ", Location: " + thing.getLocation();
                } else if (thing.getSiteName() != null) {
                    label += ", Site name: " + thing.getSiteName();
                }
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel(label).withProperty(VerisureThingConfiguration.DEVICE_ID_LABEL, deviceId).build();
                logger.debug("thinguid: {}, bridge {}, label {}", thingUID.toString(), bridgeUID, thing.getDeviceId());
                thingDiscovered(discoveryResult);
            }
        } else {
            logger.debug("Discovered unsupported thing of type '{}' with deviceId {}", thing.getClass(),
                    thing.getDeviceId());
        }

    }

    public void activate() {
    }

    private @Nullable ThingUID getThingUID(VerisureThingJSON thing) {
        ThingUID thingUID = null;
        if (verisureBridgeHandler != null) {
            ThingUID bridgeUID = verisureBridgeHandler.getThing().getUID();
            String deviceId = thing.getDeviceId();
            if (deviceId != null) {
                deviceId.replaceAll("[^a-zA-Z0-9_]", "_");
                if (thing instanceof VerisureAlarmJSON) {
                    String type = ((VerisureAlarmJSON) thing).getType();
                    if ("ARM_STATE".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_ALARM, bridgeUID, deviceId);
                    } else if ("DOOR_LOCK".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_SMARTLOCK, bridgeUID, deviceId);
                    } else {
                        logger.warn("Unknown alarm/lock device {}.", type);
                    }
                } else if (thing instanceof VerisureUserPresenceJSON) {
                    thingUID = new ThingUID(THING_TYPE_USERPRESENCE, bridgeUID, deviceId);
                } else if (thing instanceof VerisureDoorWindowJSON) {
                    thingUID = new ThingUID(THING_TYPE_DOORWINDOW, bridgeUID, deviceId);
                } else if (thing instanceof VerisureSmartPlugJSON) {
                    thingUID = new ThingUID(THING_TYPE_SMARTPLUG, bridgeUID, deviceId);
                } else if (thing instanceof VerisureClimateBaseJSON) {
                    String type = ((VerisureClimateBaseJSON) thing).getType();
                    if ("Smoke detector".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_SMOKEDETECTOR, bridgeUID, deviceId);
                    } else if ("Water detector".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_WATERDETETOR, bridgeUID, deviceId);
                    } else if ("Night Control".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_NIGHT_CONTROL, bridgeUID, deviceId);
                    } else if ("Siren".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_SIREN, bridgeUID, deviceId);
                    } else {
                        logger.warn("Unknown climate device {}.", type);
                    }
                } else if (thing instanceof VerisureBroadbandConnectionJSON) {
                    thingUID = new ThingUID(THING_TYPE_BROADBAND_CONNECTION, bridgeUID, deviceId);
                } else {
                    logger.warn("Unsupported JSON!");
                }
            }
        }
        return thingUID;
    }
}
