/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hydrawise.internal.HydrawiseBindingConstants;
import org.openhab.binding.hydrawise.internal.HydrawiseControllerListener;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Controller;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Customer;
import org.openhab.binding.hydrawise.internal.handler.HydrawiseAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Dan Cunningham - Initial contribution
 *
 */

@NonNullByDefault
@Component(service = ThingHandlerService.class)
public class HydrawiseCloudControllerDiscoveryService extends AbstractDiscoveryService
        implements HydrawiseControllerListener, ThingHandlerService {

    private static final int TIMEOUT = 5;
    @Nullable
    HydrawiseAccountHandler handler;

    public HydrawiseCloudControllerDiscoveryService() {
        super(Collections.singleton(HydrawiseBindingConstants.THING_TYPE_CONTROLLER), TIMEOUT, true);
    }

    @Override
    protected void startScan() {
        HydrawiseAccountHandler localHandler = this.handler;
        if (localHandler != null) {
            Customer data = localHandler.lastData();
            if (data != null) {
                data.controllers.forEach(controller -> addDiscoveryResults(controller));
            }
        }
    }

    @Override
    public void deactivate() {
        HydrawiseAccountHandler localHandler = this.handler;
        if (localHandler != null) {
            removeOlderResults(new Date().getTime(), localHandler.getThing().getUID());
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        HydrawiseAccountHandler localHandler = this.handler;
        if (localHandler != null) {
            removeOlderResults(getTimestampOfLastScan(), localHandler.getThing().getUID());
        }
    }

    @Override
    public void onData(List<Controller> controllers) {
        controllers.forEach(controller -> addDiscoveryResults(controller));
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (HydrawiseAccountHandler) handler;
        this.handler.addControllerListeners(this);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    private void addDiscoveryResults(Controller controller) {
        HydrawiseAccountHandler localHandler = this.handler;
        if (localHandler != null) {
            String label = String.format("Hydrawise Controller %s", controller.name);
            int id = controller.id;
            ThingUID bridgeUID = localHandler.getThing().getUID();
            ThingUID thingUID = new ThingUID(HydrawiseBindingConstants.THING_TYPE_CONTROLLER, bridgeUID,
                    String.valueOf(id));
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withLabel(label).withBridge(bridgeUID)
                    .withProperty(HydrawiseBindingConstants.CONFIG_CONTROLLER_ID, id)
                    .withRepresentationProperty(String.valueOf(HydrawiseBindingConstants.CONFIG_CONTROLLER_ID))
                    .build());
        }
    }
}
