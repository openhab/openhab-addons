/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal.services;

import static org.openhab.binding.ojelectronics.internal.BindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ojelectronics.internal.OJCloudHandler;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentModel;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;

/**
 * DiscoveryService for OJ Components
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.ojelectronics")
public final class OJDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_OJCLOUD);
    private @Nullable OJCloudHandler bridgeHandler;
    private @Nullable Collection<GroupContentModel> groupContents;

    /**
     * Creates a new instance of {@link OJDiscoveryService}
     *
     */
    public OJDiscoveryService() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 10);
    }

    /**
     * Sets the scan result for discovering
     *
     * @param groupContents Content from API
     */
    public void setScanResultForDiscovery(List<GroupContentModel> groupContents) {
        this.groupContents = groupContents;
    }

    @Override
    protected void startScan() {
        final OJCloudHandler bridgeHandler = this.bridgeHandler;
        final Collection<GroupContentModel> groupContents = this.groupContents;
        if (groupContents != null && bridgeHandler != null) {
            groupContents.stream().flatMap(content -> content.thermostats.stream())
                    .forEach(thermostat -> thingDiscovered(bridgeHandler.getThing().getUID(), thermostat.serialNumber));
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OJCloudHandler) {
            final OJCloudHandler bridgeHandler = (OJCloudHandler) handler;
            this.bridgeHandler = bridgeHandler;
            bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    private void thingDiscovered(ThingUID bridgeUID, String serialNumber) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_OWD5, bridgeUID, serialNumber))
                .withBridge(bridgeUID).withRepresentationProperty("serialNumber")
                .withProperty("serialNumber", serialNumber).withLabel("Thermostat " + serialNumber).build());
    }
}
