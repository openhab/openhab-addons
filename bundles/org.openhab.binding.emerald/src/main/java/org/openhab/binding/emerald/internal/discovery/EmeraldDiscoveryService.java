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
package org.openhab.binding.emerald.internal.discovery;

import static org.openhab.binding.emerald.internal.EmeraldBindingConstants.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emerald.internal.EmeraldAccountHandler;
import org.openhab.binding.emerald.internal.api.EmeraldList;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The EmeraldDiscoveryService is responsible for auto detecting an Emerald
 * HWS device via the cloud API.
 *
 * @author Paul Smedley - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = EmeraldDiscoveryService.class)
@NonNullByDefault
public class EmeraldDiscoveryService extends AbstractThingHandlerDiscoveryService<EmeraldAccountHandler> {

    private Logger logger = LoggerFactory.getLogger(EmeraldDiscoveryService.class);
    private @NonNullByDefault({}) ThingUID bridgeUid;

    public EmeraldDiscoveryService() {
        super(EmeraldAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5, false);
    }

    @Override
    public void initialize() {
        bridgeUid = thingHandler.getThing().getUID();
        super.initialize();
    }

    protected @Nullable EmeraldList getApi() {
        try {
            return thingHandler.getApi();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private void discover() {
        EmeraldList api = getApi();

        if (api != null) {
            HashMap<String, Object> properties = new HashMap<>();
            for (int i = 0; i < api.info.property.length; i++) {
                for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
                    properties.put("uuid", api.info.property[i].heatpump[j].id);
                    ThingUID uid = new ThingUID(THING_TYPE_HWS, bridgeUid, api.info.property[i].heatpump[j].id);
                    thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUid).withProperties(properties)
                            .withRepresentationProperty("uuid").withLabel("Emerald HWS").build());
                }
            }
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device discovery");
        discover();
    }
}
