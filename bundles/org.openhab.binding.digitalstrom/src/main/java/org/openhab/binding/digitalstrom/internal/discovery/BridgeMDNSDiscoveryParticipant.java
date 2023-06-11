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
package org.openhab.binding.digitalstrom.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl.DsAPIImpl;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeMDNSDiscoveryParticipant} is responsible for discovering digitalSTROM-Server. It uses the central
 * {@link MDNSDiscoveryService}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
@Component(service = MDNSDiscoveryParticipant.class)
public class BridgeMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(BridgeMDNSDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return "_tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        if (service.getApplication().contains("dssweb")) {
            ThingUID uid = getThingUID(service);

            if (uid != null) {
                String hostAddress = service.getName() + "." + service.getDomain() + ".";
                Map<String, Object> properties = new HashMap<>(2);
                properties.put(DigitalSTROMBindingConstants.HOST, hostAddress);
                return DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withRepresentationProperty(uid.getId()).withLabel("digitalSTROM-Server").build();
            }
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (service.getApplication().contains("dssweb")) {
            String hostAddress = service.getName() + "." + service.getDomain() + ".";
            DsAPI digitalSTROMClient = new DsAPIImpl(hostAddress, Config.DEFAULT_CONNECTION_TIMEOUT,
                    Config.DEFAULT_READ_TIMEOUT, true);
            Map<String, String> dsidMap = digitalSTROMClient.getDSID(null);
            String dSID = null;
            if (dsidMap != null) {
                dSID = dsidMap.get(JSONApiResponseKeysEnum.DSID.getKey());
            }
            if (dSID != null && !dSID.isBlank()) {
                return new ThingUID(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE, dSID);
            } else {
                logger.error("Can't get server dSID to generate thing UID. Please add the server manually.");
            }
        }
        return null;
    }
}
