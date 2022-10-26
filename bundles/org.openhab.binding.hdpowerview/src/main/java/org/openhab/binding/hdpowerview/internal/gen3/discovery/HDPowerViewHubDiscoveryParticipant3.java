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
package org.openhab.binding.hdpowerview.internal.gen3.discovery;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.THING_TYPE_HUB_GEN3;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.internal.discovery.HDPowerViewHubDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView generation 3 hubs by means of mDNS.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
@Component
public class HDPowerViewHubDiscoveryParticipant3 extends HDPowerViewHubDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubDiscoveryParticipant3.class);

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        for (String host : service.getHostAddresses()) {
            if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                ThingUID thingUID = new ThingUID(THING_TYPE_HUB_GEN3, host.replace('.', '_'));
                DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.HOST, host)
                        .withRepresentationProperty(HDPowerViewHubConfiguration.HOST)
                        .withLabel("PowerView Hub (" + host + ")").build();
                logger.debug("mDNS discovered generation 3 hub on host '{}'", host);
                return hub;
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_powerview-g3._tcp.local.";
    }
}
