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
package org.openhab.binding.ojelectronics.internal.services;

import static org.openhab.binding.ojelectronics.internal.BindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ojelectronics.internal.OJCloudHandler;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentModel;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * DiscoveryService for OJ Components
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = OJDiscoveryService.class, configurationPid = "discovery.ojelectronics")
public final class OJDiscoveryService extends AbstractThingHandlerDiscoveryService<OJCloudHandler> {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_OJCLOUD);
    private @Nullable Collection<GroupContentModel> groupContents;

    /**
     * Creates a new instance of {@link OJDiscoveryService}
     *
     */
    public OJDiscoveryService() throws IllegalArgumentException {
        super(OJCloudHandler.class, SUPPORTED_THING_TYPES_UIDS, 10);
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
        final Collection<GroupContentModel> groupContents = this.groupContents;
        if (groupContents != null) {
            groupContents.stream().flatMap(content -> content.thermostats.stream())
                    .forEach(thermostat -> thingDiscovered(thingHandler.getThing().getUID(), thermostat.serialNumber));
        }
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    private void thingDiscovered(ThingUID bridgeUID, String serialNumber) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_OWD5, bridgeUID, serialNumber))
                .withBridge(bridgeUID).withRepresentationProperty("serialNumber")
                .withProperty("serialNumber", serialNumber).withLabel("Thermostat " + serialNumber).build());
    }
}
