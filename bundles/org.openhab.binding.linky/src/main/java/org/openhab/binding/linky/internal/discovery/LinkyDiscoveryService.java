/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.discovery;

import static org.openhab.binding.linky.internal.constants.LinkyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.handler.BridgeLocalBaseHandler;
import org.openhab.binding.linky.internal.helpers.LinkyFrame;
import org.openhab.binding.linky.internal.helpers.LinkyListener;
import org.openhab.binding.linky.internal.types.LinkyChannel;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkyDiscoveryService} class is the service to discover a skeleton for controller handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 * @author Laurent Arnal - Refactor to integrate into Linky Binding
 */
@Component(scope = ServiceScope.PROTOTYPE, service = LinkyDiscoveryService.class)
@NonNullByDefault
public class LinkyDiscoveryService extends AbstractThingHandlerDiscoveryService<BridgeLocalBaseHandler>
        implements LinkyListener {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_LINKY_LOCAL);

    private static final int SCAN_DURATION_IN_S = 60;

    private final Logger logger = LoggerFactory.getLogger(LinkyDiscoveryService.class);

    public LinkyDiscoveryService() {
        super(BridgeLocalBaseHandler.class, SCAN_DURATION_IN_S);
    }

    public LinkyDiscoveryService(BridgeLocalBaseHandler controllerHandler) {
        this();
        setThingHandler(controllerHandler);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Linky device background discovery");
        thingHandler.addListener(this);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Linky device background discovery");
        thingHandler.removeListener(this);
    }

    @Override
    protected void startScan() {
        logger.debug("Linky discovery: Start {}", thingHandler.getThing().getUID());

        // Start the search for new devices
        thingHandler.addListener(this);
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("Linky discovery: Abort {}", thingHandler.getThing().getUID());
        thingHandler.removeListener(this);
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Linky discovery: Stop {}", thingHandler.getThing().getUID());
        // thingHandler.removeListener(this);
        super.stopScan();
    }

    // @Override
    @Override
    public void onFrameReceived(LinkyFrame frame) {
        detectNewElectricityMeterFromReceivedFrame(frame);
    }

    private void detectNewElectricityMeterFromReceivedFrame(final LinkyFrame frame) {
        logger.debug("New eletricity meter detection from frame {}", frame);
        if (frame.get(LinkyChannel.ADCO) == null && frame.get(LinkyChannel.ADSC) == null) {
            throw new IllegalStateException("Missing ADCO or ADSC key");
        }

        ThingTypeUID tpUid = THING_TYPE_LINKY_LOCAL;

        String adco = frame.get(LinkyChannel.ADCO) != null ? frame.get(LinkyChannel.ADCO)
                : frame.get(LinkyChannel.ADSC);
        String prmId = frame.get(LinkyChannel.PRM);
        if (adco != null && prmId != null) {

            ThingUID thingUID = new ThingUID(tpUid, adco, thingHandler.getThing().getUID().getId());

            // ThingUID thingUID = new ThingUID(getThingTypeUID(frameSample), adco,
            // thingHandler.getThing().getUID().getId());

            Map<String, Object> properties = new HashMap<>();
            properties.put(THING_ELECTRICITY_METER_PROPERTY_ADCO, adco);
            properties.put(PRM_ID, prmId);

            final String representationProperty = THING_ELECTRICITY_METER_PROPERTY_ADCO;
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel("Linky " + prmId).withThingType(tpUid).withBridge(thingHandler.getThing().getUID())
                    .withRepresentationProperty(representationProperty).build();

            thingDiscovered(discoveryResult);
        }
    }
}
