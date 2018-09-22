/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonyps4.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.openhab.binding.sonyps4.internal.SonyPS4BindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPS4Discovery} is responsible for discovering
 * all PS4 devices
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        SonyPS4Discovery.class }, immediate = true, configurationPid = "binding.sonyps4")
public class SonyPS4Discovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SonyPS4Discovery.class);

    private static final int DEFAULT_BROADCAST_PORT = 997;
    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;

    public SonyPS4Discovery() {
        super(SonyPS4BindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, true);
    }

    /**
     * Activates the Discovery Service.
     */
    public void activate() {
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
    }

    @Override
    protected void startScan() {
        logger.debug("Updating discovered things (new scan)");
    }

}
