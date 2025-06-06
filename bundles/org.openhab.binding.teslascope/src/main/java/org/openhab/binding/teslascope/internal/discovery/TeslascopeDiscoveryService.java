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
package org.openhab.binding.teslascope.internal.discovery;

import static org.openhab.binding.teslascope.internal.TeslascopeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.teslascope.internal.TeslascopeAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TeslascopeDiscoveryService is responsible for auto detecting a Tesla
 * vehicle in the Teslascope service.
 *
 * @author paul@smedley.id.au - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = TeslascopeDiscoveryService.class)
@NonNullByDefault
public class TeslascopeDiscoveryService extends AbstractThingHandlerDiscoveryService<TeslascopeAccountHandler> {

    private Logger logger = LoggerFactory.getLogger(TeslascopeDiscoveryService.class);
    private @NonNullByDefault({}) ThingUID bridgeUid;

    public TeslascopeDiscoveryService() {
        super(TeslascopeAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5, false);
    }

    @Override
    public void initialize() {
        bridgeUid = thingHandler.getThing().getUID();
        super.initialize();
    }

    private void discover() {
        /* getapi() and parse the list..... */
        /*
         * VehicleList vehicleList = getApi();
         * int found = 0;
         * if (api != null) {
         * HashMap<String, Object> properties = new HashMap<>();
         * for (int i = 0; i < api.info.property.length; i++) {
         * for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
         * properties.put("uuid", api.info.property[i].heatpump[j].id);
         * ThingUID uid = new ThingUID(THING_TYPE_HWS, bridgeUid, api.info.property[i].heatpump[j].id);
         * thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUid).withProperties(properties)
         * .withRepresentationProperty("uuid").withLabel("Teslascope").build());
         * }
         * }
         * }
         */
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device discovery");
        discover();
    }
}
