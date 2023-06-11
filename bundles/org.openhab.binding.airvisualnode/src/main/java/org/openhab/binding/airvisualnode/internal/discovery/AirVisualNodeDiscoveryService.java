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
package org.openhab.binding.airvisualnode.internal.discovery;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.airvisualnode.internal.AirVisualNodeBindingConstants;
import org.openhab.binding.airvisualnode.internal.config.AirVisualNodeConfig;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.netbios.NbtAddress;
import jcifs.smb.SmbFile;

/**
 * Autodiscovery for AirVisual Node by searching for a host advertised with the NetBIOS name 'AVISUAL-<SerialNumber>'.
 *
 * @author Victor Antonovich - Initial contribution
 */
@Component(service = DiscoveryService.class)
public class AirVisualNodeDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(AirVisualNodeDiscoveryService.class);

    public static final String AVISUAL_WORKGROUP_NAME = "MSHOME";

    private static final Pattern AVISUAL_NAME_PATTERN = Pattern.compile("^AVISUAL-([^/]+)$");

    private ScheduledFuture<?> backgroundDiscoveryFuture;

    public AirVisualNodeDiscoveryService() {
        super(Collections.singleton(AirVisualNodeBindingConstants.THING_TYPE_AVNODE), 600, true);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scan");
        scheduler.execute(this::scan);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery");
        backgroundDiscoveryFuture = scheduler.scheduleWithFixedDelay(this::scan, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping background discovery");
        cancelBackgroundDiscoveryFuture();
        super.stopBackgroundDiscovery();
    }

    private void cancelBackgroundDiscoveryFuture() {
        if (backgroundDiscoveryFuture != null && !backgroundDiscoveryFuture.isDone()) {
            backgroundDiscoveryFuture.cancel(true);
            backgroundDiscoveryFuture = null;
        }
    }

    private void scan() {
        // Get all workgroup members
        SmbFile[] workgroupMembers;
        try {
            String workgroupUrl = "smb://" + AVISUAL_WORKGROUP_NAME + "/";
            workgroupMembers = new SmbFile(workgroupUrl).listFiles();
        } catch (IOException e) {
            // Can't get workgroup member list
            return;
        }

        // Check found workgroup members for the Node devices
        for (SmbFile s : workgroupMembers) {
            String serverName = s.getServer();

            // Check workgroup member for the Node device name match
            Matcher m = AVISUAL_NAME_PATTERN.matcher(serverName);
            if (!m.find()) {
                // Workgroup member server name doesn't match the Node device name pattern
                continue;
            }

            // Extract the Node serial number from device name
            String nodeSerialNumber = m.group(1);

            // The Node Thing UID is serial number converted to lower case
            ThingUID thingUID = new ThingUID(AirVisualNodeBindingConstants.THING_TYPE_AVNODE,
                    nodeSerialNumber.toLowerCase());

            try {
                // Get the Node address by name
                NbtAddress nodeNbtAddress = NbtAddress.getByName(serverName);
                if (nodeNbtAddress == null) {
                    // The Node address not found by some reason, skip it
                    continue;
                }

                // Create discovery result
                String nodeAddress = nodeNbtAddress.getInetAddress().getHostAddress();
                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(AirVisualNodeConfig.ADDRESS, nodeAddress)
                        .withRepresentationProperty(AirVisualNodeConfig.ADDRESS)
                        .withLabel("AirVisual Node (" + nodeSerialNumber + ")").build();
                thingDiscovered(result);
            } catch (UnknownHostException e) {
                logger.debug("The Node address resolving failed ", e);
            }
        }
    }
}
