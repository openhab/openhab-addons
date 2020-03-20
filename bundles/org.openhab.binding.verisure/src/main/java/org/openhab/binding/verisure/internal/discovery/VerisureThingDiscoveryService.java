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
import org.openhab.binding.verisure.internal.model.VerisureThing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The discovery service, notified by a listener on the VerisureSession.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Further development
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
        logger.debug("VerisureThingDiscoveryService:startScan");
        removeOlderResults(getTimestampOfLastScan());
        if (verisureBridgeHandler != null) {
            VerisureSession session = verisureBridgeHandler.getSession();
            if (session != null) {
                HashMap<String, VerisureThing> verisureThings = session.getVerisureThings();
                verisureThings.forEach((deviceId, thing) -> {
                    logger.debug("Discovered thing: {}", thing);
                    onThingAddedInternal(thing);
                });
            }
        }
    }

    private void onThingAddedInternal(VerisureThing thing) {
        logger.debug("VerisureThingDiscoveryService:OnThingAddedInternal");
        ThingUID thingUID = getThingUID(thing);
        String deviceId = thing.getDeviceId();
        if (thingUID != null) {
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
            // Make sure device id is normalized, i.e. replace all non character/digits with empty string
            deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
            thingUID = new ThingUID(thing.getThingTypeUID(), bridgeUID, deviceId);
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
