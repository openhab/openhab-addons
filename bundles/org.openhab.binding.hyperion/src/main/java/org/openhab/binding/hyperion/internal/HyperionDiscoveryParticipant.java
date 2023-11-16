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
package org.openhab.binding.hyperion.internal;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HyperionDiscoveryParticipant} class is responsible for listening
 * to MDNS responses and discovering Hyperion.ng servers.
 *
 * @author Daniel Walters - Initial contribution
 */

@Component
public class HyperionDiscoveryParticipant implements MDNSDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(HyperionBindingConstants.THING_TYPE_SERVER_NG);
    }

    @Override
    public String getServiceType() {
        return "_hyperiond-json._tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        DiscoveryResult result = null;

        // return null if the service info is invalid / not fully formed
        if (service.getHostAddresses().length == 0) {
            return null;
        }

        final Map<String, Object> properties = new HashMap<>(2);
        String host = service.getHostAddresses()[0];
        BigDecimal port = new BigDecimal(service.getPort());

        properties.put(HyperionBindingConstants.HOST, host);
        properties.put(HyperionBindingConstants.PORT, port);

        String longName = service.getName();
        int pos = longName.indexOf("@");
        if (pos < 0 || pos >= longName.length()) {
            return null;
        }

        String friendlyName = longName.substring(0, pos);
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            result = DiscoveryResultBuilder.create(uid).withThingType(HyperionBindingConstants.THING_TYPE_SERVER_NG)
                    .withProperties(properties).withLabel(friendlyName).build();
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        String uid = service.getPropertyString("id");
        if (uid == null) {
            String longName = service.getName();
            int hashCode = longName.hashCode();
            uid = Integer.toUnsignedString(hashCode);
        }
        return new ThingUID(HyperionBindingConstants.THING_TYPE_SERVER_NG, uid);
    }
}
