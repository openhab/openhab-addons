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
package org.openhab.binding.hdpowerview.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.dto.Firmware;
import org.openhab.binding.hdpowerview.internal.dto.HubFirmware;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for common discovery logic.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class HDPowerViewHubDiscoveryParticipant {

    protected static final String LABEL_KEY_HUB = "discovery.hub.label";

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubDiscoveryParticipant.class);

    private final HttpClient httpClient;

    protected HDPowerViewHubDiscoveryParticipant(@Reference HttpClientFactory httpClientFactory) {
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected String getGeneration(String host) {
        var webTargets = new HDPowerViewWebTargets(httpClient, host);
        try {
            HubFirmware firmware = webTargets.getFirmwareVersions();
            Firmware mainProcessor = firmware.mainProcessor;
            if (mainProcessor != null) {
                return String.valueOf(mainProcessor.revision);
            }
        } catch (HubException e) {
            logger.debug("Failed to discover hub firmware versions", e);
        }
        return "1/2";
    }
}
