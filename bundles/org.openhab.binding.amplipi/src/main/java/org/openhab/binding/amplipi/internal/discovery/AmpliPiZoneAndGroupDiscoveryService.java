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
package org.openhab.binding.amplipi.internal.discovery;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amplipi.internal.AmpliPiBindingConstants;
import org.openhab.binding.amplipi.internal.AmpliPiHandler;
import org.openhab.binding.amplipi.internal.AmpliPiStatusChangeListener;
import org.openhab.binding.amplipi.internal.model.Group;
import org.openhab.binding.amplipi.internal.model.Status;
import org.openhab.binding.amplipi.internal.model.Zone;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * This class discoveres the available zones and groups of the AmpliPi system.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AmpliPiZoneAndGroupDiscoveryService.class)
@NonNullByDefault
public class AmpliPiZoneAndGroupDiscoveryService extends AbstractThingHandlerDiscoveryService<AmpliPiHandler>
        implements AmpliPiStatusChangeListener {

    private static final int TIMEOUT = 10;

    private List<Zone> zones = List.of();
    private List<Group> groups = List.of();

    public AmpliPiZoneAndGroupDiscoveryService() throws IllegalArgumentException {
        super(AmpliPiHandler.class,
                Set.of(AmpliPiBindingConstants.THING_TYPE_GROUP, AmpliPiBindingConstants.THING_TYPE_ZONE), TIMEOUT,
                true);
    }

    @Override
    public void initialize() {
        thingHandler.addStatusChangeListener(this);
        super.initialize();
    }

    @Override
    protected void startScan() {
        for (Zone z : zones) {
            if (!z.getDisabled()) {
                createZone(z);
            }
        }
        for (Group g : groups) {
            createGroup(g);
        }
    }

    private void createZone(Zone z) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID uid = new ThingUID(AmpliPiBindingConstants.THING_TYPE_ZONE, bridgeUID, z.getId().toString());
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel("AmpliPi Zone '" + z.getName() + "'")
                .withProperty(AmpliPiBindingConstants.CFG_PARAM_ID, z.getId()).withBridge(bridgeUID)
                .withRepresentationProperty(AmpliPiBindingConstants.CFG_PARAM_ID).build();
        thingDiscovered(result);
    }

    private void createGroup(Group g) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID uid = new ThingUID(AmpliPiBindingConstants.THING_TYPE_GROUP, bridgeUID, g.getId().toString());
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel("AmpliPi Group '" + g.getName() + "'")
                .withProperty(AmpliPiBindingConstants.CFG_PARAM_ID, g.getId()).withBridge(bridgeUID)
                .withRepresentationProperty(AmpliPiBindingConstants.CFG_PARAM_ID).build();
        thingDiscovered(result);
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.removeStatusChangeListener(this);
    }

    @Override
    public void receive(Status status) {
        zones = status.getZones();
        groups = status.getGroups();
        if (isBackgroundDiscoveryEnabled()) {
            startScan();
        }
    }
}
