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
package org.openhab.binding.etherrain.internal.discovery;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.etherrain.internal.EtherRainBindingConstants;
import org.openhab.binding.etherrain.internal.api.EtherRainCommunication;
import org.openhab.binding.etherrain.internal.api.EtherRainUdpResponse;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EtherrainDiscoveryService} class discovers Etherrain Device(s) and places them in the inbox.
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.etherrain")
public class EtherrainDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(EtherrainDiscoveryService.class);

    private static final int TIMEOUT = 15;

    public EtherrainDiscoveryService() {
        super(EtherRainBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return EtherRainBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        for (EtherRainUdpResponse rdp : EtherRainCommunication.autoDiscover()) {
            if (rdp.isValid()) {
                ThingUID uid = new ThingUID(EtherRainBindingConstants.ETHERRAIN_THING,
                        rdp.getAddress().replaceAll("[^A-Za-z0-9\\-_]", ""));
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                        .withLabel("Etherrain " + rdp.getType() + " " + rdp.getUnqiueName())
                        .withProperty("host", rdp.getAddress()).withProperty("port", rdp.getPort()).build();
                thingDiscovered(discoveryResult);
            } else {
                logger.debug("Nothing responded to request");
            }
        }
    }
}
