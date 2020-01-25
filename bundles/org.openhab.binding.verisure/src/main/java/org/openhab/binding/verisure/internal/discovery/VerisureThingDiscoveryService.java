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
package org.openhab.binding.verisure.internal.discovery;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.verisure.internal.VerisureHandlerFactory;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.handler.VerisureBridgeHandler;
import org.openhab.binding.verisure.internal.model.VerisureAlarms;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnections;
import org.openhab.binding.verisure.internal.model.VerisureClimates;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindows;
import org.openhab.binding.verisure.internal.model.VerisureMiceDetection;
import org.openhab.binding.verisure.internal.model.VerisureSmartLocks;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugs;
import org.openhab.binding.verisure.internal.model.VerisureThing;
import org.openhab.binding.verisure.internal.model.VerisureUserPresences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The discovery service, notified by a listener on the VerisureSession.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureThingDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int SEARCH_TIME_SECONDS = 60;
    private final Logger logger = LoggerFactory.getLogger(VerisureThingDiscoveryService.class);

    private @NonNullByDefault({}) VerisureBridgeHandler verisureBridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    public VerisureThingDiscoveryService() {
        super(VerisureHandlerFactory.SUPPORTED_THING_TYPES, SEARCH_TIME_SECONDS);
    }

    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        logger.debug("VerisureThingDiscoveryService:startScan");
        if (verisureBridgeHandler != null) {
            VerisureSession session = verisureBridgeHandler.getSession();
            if (session != null) {
                HashMap<String, VerisureThing> verisureThings = session.getVerisureThings();
                for (Map.Entry<String, VerisureThing> entry : verisureThings.entrySet()) {
                    VerisureThing thing = entry.getValue();
                    if (thing != null) {
                        logger.debug("Thing: {}", thing);
                        onThingAddedInternal(thing);
                    }
                }
            }
        }
    }

    private void onThingAddedInternal(VerisureThing thing) {
        logger.debug("VerisureThingDiscoveryService:OnThingAddedInternal");
        ThingUID thingUID = getThingUID(thing);
        String deviceId = thing.getDeviceId();
        if (thingUID != null && deviceId != null) {
            if (verisureBridgeHandler != null) {
                String label = "Device Id: " + deviceId;
                if (thing.getLocation() != null) {
                    label += ", Location: " + thing.getLocation();
                }
                if (thing.getSiteName() != null) {
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

    private @Nullable ThingUID getThingUID(VerisureThing thing) {
        ThingUID thingUID = null;
        if (verisureBridgeHandler != null) {
            String deviceId = thing.getDeviceId();
            if (deviceId != null) {
                // Make sure device id is normalized, i.e. replace all non character/digits with empty string
                deviceId.replaceAll("[^a-zA-Z0-9]+", "");
                if (thing instanceof VerisureAlarms) {
                    thingUID = new ThingUID(THING_TYPE_ALARM, bridgeUID, deviceId);
                } else if (thing instanceof VerisureSmartLocks) {
                    thingUID = new ThingUID(THING_TYPE_SMARTLOCK, bridgeUID, deviceId);
                } else if (thing instanceof VerisureUserPresences) {
                    thingUID = new ThingUID(THING_TYPE_USERPRESENCE, bridgeUID, deviceId);
                } else if (thing instanceof VerisureDoorWindows) {
                    thingUID = new ThingUID(THING_TYPE_DOORWINDOW, bridgeUID, deviceId);
                } else if (thing instanceof VerisureSmartPlugs) {
                    thingUID = new ThingUID(THING_TYPE_SMARTPLUG, bridgeUID, deviceId);
                } else if (thing instanceof VerisureClimates) {
                    String type = ((VerisureClimates) thing).getData().getInstallation().getClimates().get(0)
                            .getDevice().getGui().getLabel();
                    if ("SMOKE".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_SMOKEDETECTOR, bridgeUID, deviceId);
                    } else if ("WATER".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_WATERDETECTOR, bridgeUID, deviceId);
                    } else if ("HOMEPAD".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_NIGHT_CONTROL, bridgeUID, deviceId);
                    } else if ("SIREN".equals(type)) {
                        thingUID = new ThingUID(THING_TYPE_SIREN, bridgeUID, deviceId);
                    } else {
                        logger.warn("Unknown climate device {}.", type);
                    }
                } else if (thing instanceof VerisureBroadbandConnections) {
                    thingUID = new ThingUID(THING_TYPE_BROADBAND_CONNECTION, bridgeUID, deviceId);
                } else if (thing instanceof VerisureMiceDetection) {
                    thingUID = new ThingUID(THING_TYPE_MICE_DETECTION, bridgeUID, deviceId);
                } else {
                    logger.debug("Unsupported JSON! thing {}", thing);
                }
            }
        }
        return thingUID;
    }

    @Override
    public void activate() {
        Map<String, @Nullable Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VerisureBridgeHandler) {
            verisureBridgeHandler = (VerisureBridgeHandler) handler;
            bridgeUID = verisureBridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return verisureBridgeHandler;
    }

}
