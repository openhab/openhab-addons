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
package org.openhab.binding.hydrawise.internal.discovery;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hydrawise.internal.HydrawiseBindingConstants;
import org.openhab.binding.hydrawise.internal.HydrawiseControllerListener;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Controller;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Customer;
import org.openhab.binding.hydrawise.internal.handler.HydrawiseAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 *
 * @author Dan Cunningham - Initial contribution
 *
 */

@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = ThingHandlerService.class)
public class HydrawiseCloudControllerDiscoveryService
        extends AbstractThingHandlerDiscoveryService<HydrawiseAccountHandler> implements HydrawiseControllerListener {
    private static final int TIMEOUT = 5;

    public HydrawiseCloudControllerDiscoveryService() {
        super(HydrawiseAccountHandler.class, Set.of(HydrawiseBindingConstants.THING_TYPE_CONTROLLER), TIMEOUT, true);
    }

    @Override
    protected void startScan() {
        Customer data = thingHandler.lastData();
        if (data != null) {
            data.controllers.forEach(controller -> addDiscoveryResults(controller));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now().toEpochMilli(), thingHandler.getThing().getUID());
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan(), thingHandler.getThing().getUID());
    }

    @Override
    public void onData(List<Controller> controllers) {
        controllers.forEach(controller -> addDiscoveryResults(controller));
    }

    @Override
    public void initialize() {
        thingHandler.addControllerListeners(this);
        super.initialize();
    }

    private void addDiscoveryResults(Controller controller) {
        String label = String.format("Hydrawise Controller %s", controller.name);
        int id = controller.id;
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(HydrawiseBindingConstants.THING_TYPE_CONTROLLER, bridgeUID,
                String.valueOf(id));
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withLabel(label).withBridge(bridgeUID)
                .withProperty(HydrawiseBindingConstants.CONFIG_CONTROLLER_ID, id)
                .withRepresentationProperty(HydrawiseBindingConstants.CONFIG_CONTROLLER_ID).build());
    }
}
