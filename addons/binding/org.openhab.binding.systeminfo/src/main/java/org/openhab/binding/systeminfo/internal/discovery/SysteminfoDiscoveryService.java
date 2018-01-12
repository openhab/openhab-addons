/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo.internal.discovery;

import static org.openhab.binding.systeminfo.SysteminfoBindingConstants.THING_TYPE_COMPUTER;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.systeminfo.SysteminfoBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service implementation for the Systeminfo binding. It creates {@link DiscoveryResult} with
 * {@link #DEFAULT_THING_LABEL}. The discovered Thing will have id - the hostname or {@link #DEFAULT_THING_ID}'
 *
 * @author Svilen Valkanov
 */
public class SysteminfoDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(SysteminfoDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_COMPUTER);

    private static final int DISCOVERY_TIME_SECONDS = 30;
    private static final String DEFAULT_THING_ID = "unknown";
    private static final String DEFAULT_THING_LABEL = "Local computer";
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
        String hostname = addr.getHostName();
        return hostname;
    }
}
