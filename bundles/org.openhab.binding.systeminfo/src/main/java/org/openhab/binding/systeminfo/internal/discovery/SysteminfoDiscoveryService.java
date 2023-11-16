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
package org.openhab.binding.systeminfo.internal.discovery;

import static org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants.THING_TYPE_COMPUTER;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants;
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
 * Discovery service implementation for the Systeminfo binding. It creates {@link DiscoveryResult} with
 * {@link #DEFAULT_THING_LABEL}. The discovered Thing will have id - the hostname or {@link #DEFAULT_THING_ID}'
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.systeminfo")
public class SysteminfoDiscoveryService extends AbstractDiscoveryService {
    public static final String DEFAULT_THING_ID = "unknown";
    public static final String DEFAULT_THING_LABEL = "Local computer";

    private final Logger logger = LoggerFactory.getLogger(SysteminfoDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_COMPUTER);

    private static final int DISCOVERY_TIME_SECONDS = 30;
    private static final String THING_UID_VALID_CHARS = "A-Za-z0-9_-";
    private static final String HOST_NAME_SEPERATOR = "_";

    public SysteminfoDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting system information discovery !");
        String hostname;

        try {
            hostname = getHostName();
            if (hostname.isEmpty()) {
                throw new UnknownHostException();
            }
            if (!hostname.matches("[" + THING_UID_VALID_CHARS + "]*")) {
                hostname = hostname.replaceAll("[^" + THING_UID_VALID_CHARS + "]", HOST_NAME_SEPERATOR);
            }
        } catch (UnknownHostException ex) {
            hostname = DEFAULT_THING_ID;
            logger.info("Hostname can not be resolved. Computer name will be set to the default one: {}",
                    DEFAULT_THING_ID);
        }

        ThingTypeUID computerType = SysteminfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID computer = new ThingUID(computerType, hostname);
        thingDiscovered(DiscoveryResultBuilder.create(computer).withLabel(DEFAULT_THING_LABEL).build());
    }

    protected String getHostName() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostName();
    }
}
