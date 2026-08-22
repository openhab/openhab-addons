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
 * Passive discovery for the OCPP binding: a charge point is offered to the inbox when it opens a
 * session whose id has no thing, and a connector is offered when it first reports a StatusNotification
 * under a known charge point. There is no active scan — chargers announce themselves.
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
        // Passive: results are raised from live charger connections, not an active scan.
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
     * A charge point id reduced to a valid ThingUID segment. A clean id is used as-is; one holding
     * characters a segment cannot carry is encoded reversibly (URL-safe Base64) rather than blanket-
     * replacing each with an underscore, so two distinct ids can never collide onto the same Thing.
     */
    static String sanitize(String id) {
        if (VALID_SEGMENT.matcher(id).matches()) {
            return id;
        }
        return "b64-" + Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }
}
