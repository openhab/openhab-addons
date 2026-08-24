/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ocpp.internal.discovery;

import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ocpp.internal.handler.OcppServerBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Passive discovery: charge points and connectors are offered to the inbox as they announce themselves.
 *
 * @author Stamate Viorel - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = OcppDiscoveryService.class, configurationPid = "discovery.ocpp")
@NonNullByDefault
public class OcppDiscoveryService extends AbstractThingHandlerDiscoveryService<OcppServerBridgeHandler> {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_CHARGEPOINT, THING_TYPE_CONNECTOR);
    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;
    private static final Pattern VALID_SEGMENT = Pattern.compile("[A-Za-z0-9_-]+");

    public OcppDiscoveryService() {
        super(OcppServerBridgeHandler.class, SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS, false);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.setDiscoveryService(null);
    }

    @Override
    protected void startScan() {
    }

    public void chargePointDiscovered(String chargePointId) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(THING_TYPE_CHARGEPOINT, bridgeUID, sanitize(chargePointId));
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperty(CONFIG_CHARGE_POINT_ID, chargePointId).withRepresentationProperty(CONFIG_CHARGE_POINT_ID)
                .withLabel("OCPP Charge Point " + chargePointId).build());
    }

    public void connectorDiscovered(ThingUID chargePointUID, String chargePointId, int connectorId) {
        ThingUID thingUID = new ThingUID(THING_TYPE_CONNECTOR, chargePointUID, "c" + connectorId);
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(chargePointUID)
                .withProperty(CONFIG_CONNECTOR_ID, connectorId)
                .withProperty(PROPERTY_UNIQUE_ID, uniqueConnectorId(chargePointId, connectorId))
                .withRepresentationProperty(PROPERTY_UNIQUE_ID).withLabel("OCPP Connector " + connectorId).build());
    }

    /**
     * Reduces a charge point id to a valid ThingUID segment; non-segment ids are Base64-encoded so they stay distinct.
     */
    static String sanitize(String id) {
        if (VALID_SEGMENT.matcher(id).matches()) {
            return id;
        }
        return "b64-" + Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }
}
