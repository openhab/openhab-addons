/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.AmpliPiBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * This is a discovery participant which finds AmpliPis on the local network
 * through their mDNS announcements.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public class AmpliPiMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(AmpliPiBindingConstants.THING_TYPE_CONTROLLER);
    }

    @Override
    public String getServiceType() {
        return "_http._tcp";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(service.getName())
                    .withProperty(AmpliPiBindingConstants.CFG_PARAM_HOSTNAME,
                            service.getInet4Addresses()[0].getHostAddress())
                    .withRepresentationProperty(AmpliPiBindingConstants.CFG_PARAM_HOSTNAME).build();
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        // TODO: Currently, the AmpliPi does not seem to announce any services.
        return null;
    }
}
