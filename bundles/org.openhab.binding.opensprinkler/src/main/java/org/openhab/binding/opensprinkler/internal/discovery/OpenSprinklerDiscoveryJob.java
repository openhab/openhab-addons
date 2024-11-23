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
package org.openhab.binding.opensprinkler.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnauthorizedApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSprinklerDiscoveryJob} class allow manual discovery of
 * OpenSprinkler devices for a single IP address. This is used
 * for threading to make discovery faster.
 *
 * @author Chris Graham - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerDiscoveryJob implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerDiscoveryJob.class);
    private OpenSprinklerDiscoveryService discoveryClass;
    private String ipAddress;

    public OpenSprinklerDiscoveryJob(OpenSprinklerDiscoveryService service, String ip) {
        this.discoveryClass = service;
        this.ipAddress = ip;
    }

    @Override
    public void run() {
        if (hasOpenSprinklerDevice(this.ipAddress)) {
            discoveryClass.submitDiscoveryResults(this.ipAddress);
        }
    }

    /**
     * Determines if an OpenSprinkler device is available at a given IP address.
     *
     * @param ip IP address of the OpenSprinkler device as a string.
     * @return True if a device is found, false if not.
     */
    private boolean hasOpenSprinklerDevice(String ip) {
        try {
            OpenSprinklerHttpInterfaceConfig config = new OpenSprinklerHttpInterfaceConfig();
            config.hostname = ip;
            discoveryClass.getApiFactory().getHttpApi(config);
        } catch (UnauthorizedApiException e) {
            return true;
        } catch (CommunicationApiException | GeneralApiException exp) {
            logger.debug("No OpenSprinkler device found at IP address ({}) because of error: {}", ip, exp.getMessage());
            return false;
        }
        return true;
    }
}
