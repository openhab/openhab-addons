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
package org.openhab.binding.verisure.internal.discovery;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.verisure.internal.VerisureHandlerFactory;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.dto.VerisureThingDTO;
import org.openhab.binding.verisure.internal.handler.VerisureBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The discovery service, notified by a listener on the VerisureSession.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Further development
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = VerisureThingDiscoveryService.class)
@NonNullByDefault
public class VerisureThingDiscoveryService extends AbstractThingHandlerDiscoveryService<VerisureBridgeHandler> {

    private static final int SEARCH_TIME_SECONDS = 60;
    private final Logger logger = LoggerFactory.getLogger(VerisureThingDiscoveryService.class);
    private @NonNullByDefault({}) ThingUID bridgeUID;

    public VerisureThingDiscoveryService() {
        super(VerisureBridgeHandler.class, VerisureHandlerFactory.SUPPORTED_THING_TYPES, SEARCH_TIME_SECONDS);
    }

    @Override
    public void startScan() {
        logger.debug("VerisureThingDiscoveryService:startScan");
        removeOlderResults(getTimestampOfLastScan());
        VerisureSession session = thingHandler.getSession();
        if (session != null) {
            Collection<VerisureThingDTO> verisureThings = session.getVerisureThings();
            verisureThings.stream().forEach(thing -> {
                logger.debug("Discovered thing: {}", thing);
                onThingAddedInternal(thing);
            });
        }
    }

    private void onThingAddedInternal(VerisureThingDTO thing) {
        logger.debug("VerisureThingDiscoveryService:OnThingAddedInternal");
        ThingUID thingUID = getThingUID(thing);
        String deviceId = thing.getDeviceId();
        if (thingUID != null) {
            String className = thing.getClass().getSimpleName();
            String label = "Type: " + className + " Device Id: " + deviceId;
            if (thing.getLocation() != null) {
                label += ", Location: " + thing.getLocation();
            }
            if (thing.getSiteName() != null) {
                label += ", Site name: " + thing.getSiteName();
            }
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).withProperty(VerisureThingConfiguration.DEVICE_ID_LABEL, deviceId)
                    .withRepresentationProperty(VerisureThingConfiguration.DEVICE_ID_LABEL).build();
            logger.debug("thinguid: {}, bridge {}, label {}", thingUID, bridgeUID, deviceId);
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered unsupported thing of type '{}' with deviceId {}", thing.getClass(), deviceId);
        }
    }

    private @Nullable ThingUID getThingUID(VerisureThingDTO thing) {
        ThingUID thingUID = null;
        String deviceId = thing.getDeviceId();
        // Make sure device id is normalized, i.e. replace all non character/digits with empty string
        deviceId = VerisureThingConfiguration.normalizeDeviceId(deviceId);
        thingUID = new ThingUID(thing.getThingTypeUID(), bridgeUID, deviceId);
        return thingUID;
    }

    @Override
    public void activate() {
        super.activate(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, true));
    }

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getUID();
        super.initialize();
    }
}
