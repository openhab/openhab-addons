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
package org.openhab.binding.russound.internal.discovery;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.russound.internal.RussoundHandlerFactory;
import org.openhab.binding.russound.internal.net.SocketChannelSession;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.WaitingSessionListener;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.controller.RioControllerConfig;
import org.openhab.binding.russound.internal.rio.source.RioSourceConfig;
import org.openhab.binding.russound.internal.rio.system.RioSystemHandler;
import org.openhab.binding.russound.internal.rio.zone.RioZoneConfig;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of {@link DiscoveryService} will scan a RIO device for all controllers, source and zones attached
 * to it.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioSystemDeviceDiscoveryService extends AbstractDiscoveryService {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(RioSystemDeviceDiscoveryService.class);

    /** The system handler to scan */
    private final RioSystemHandler sysHandler;

    /** Pattern to identify controller notifications */
    private static final Pattern RSP_CONTROLLERNOTIFICATION = Pattern
            .compile("(?i)^[SN] C\\[(\\d+)\\]\\.(\\w+)=\"(.*)\"$");

    /** Pattern to identify source notifications */
    private static final Pattern RSP_SRCNOTIFICATION = Pattern.compile("(?i)^[SN] S\\[(\\d+)\\]\\.(\\w+)=\"(.*)\"$");

    /** Pattern to identify zone notifications */
    private static final Pattern RSP_ZONENOTIFICATION = Pattern
            .compile("(?i)^[SN] C\\[(\\d+)\\]\\.Z\\[(\\d+)\\]\\.(\\w+)=\"(.*)\"$");

    /**
     * The {@link SocketSession} that will be used to scan the device
     */
    private SocketSession session;

    /**
     * The {@link WaitingSessionListener} to the {@link #session} to receive/process responses
     */
    private WaitingSessionListener listener;

    /**
     * Create the discovery service from the {@link RioSystemHandler}
     *
     * @param sysHandler a non-null {@link RioSystemHandler}
     * @throws IllegalArgumentException if sysHandler is null
     */
    public RioSystemDeviceDiscoveryService(RioSystemHandler sysHandler) {
        super(RussoundHandlerFactory.SUPPORTED_THING_TYPES_UIDS, 30, false);

        if (sysHandler == null) {
            throw new IllegalArgumentException("sysHandler can't be null");
        }
        this.sysHandler = sysHandler;
    }

    /**
     * Activates this discovery service. Simply registers this with
     * {@link RioSystemHandler#registerDiscoveryService(RioSystemDeviceDiscoveryService)}
     */
    public void activate() {
        sysHandler.registerDiscoveryService(this);
    }

    /**
     * Deactivates the scan - will disconnect the session and remove the {@link #listener}
     */
    @Override
    public void deactivate() {
        if (session != null) {
            try {
                session.disconnect();
            } catch (IOException e) {
                // ignore
            }
            session.removeListener(listener);
            session = null;
            listener = null;
        }
    }

    /**
     * Overridden to do nothing - {@link #scanDevice()} is called by {@link RioSystemHandler} instead
     */
    @Override
    protected void startScan() {
        // do nothing - started by RioSystemHandler
    }

    /**
     * Starts a device scan. This will connect to the device and discover the controllers/sources/zones attached to the
     * device and then disconnect via {@link #deactivate()}
     */
    public void scanDevice() {
        try {
            final String ipAddress = sysHandler.getRioConfig().getIpAddress();
            session = new SocketChannelSession(ipAddress, RioConstants.RIO_PORT);
            listener = new WaitingSessionListener();
            session.addListener(listener);

            try {
                logger.debug("Starting scan of RIO device at {}", ipAddress);
                session.connect();
                discoverControllers();
                discoverSources();
            } catch (IOException e) {
                logger.debug("Trying to scan device but couldn't connect: {}", e.getMessage(), e);
            }
        } finally {
            deactivate();
        }
    }

    /**
     * Helper method to discover controllers - this will iterate through all possible controllers (6 of them )and see if
     * any respond to the "type" command. If they do, we initiate a {@link #thingDiscovered(DiscoveryResult)} for the
     * controller and then scan the controller for zones via {@link #discoverZones(ThingUID, int)}
     */
    private void discoverControllers() {
        for (int c = 1; c < 7; c++) {
            final String type = sendAndGet("GET C[" + c + "].type", RSP_CONTROLLERNOTIFICATION, 3);
            if (type != null && !type.isEmpty()) {
                logger.debug("Controller #{} found - {}", c, type);

                final ThingUID thingUID = new ThingUID(RioConstants.BRIDGE_TYPE_CONTROLLER,
                        sysHandler.getThing().getUID(), String.valueOf(c));

                final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(RioControllerConfig.CONTROLLER, c).withBridge(sysHandler.getThing().getUID())
                        .withLabel("Controller #" + c).build();
                thingDiscovered(discoveryResult);

                discoverZones(thingUID, c);
            }
        }
    }

    /**
     * Helper method to discover sources. This will iterate through all possible sources (8 of them) and see if they
     * respond to the "type" command. If they do, we retrieve the source "name" and initial a
     * {@link #thingDiscovered(DiscoveryResult)} for the source.
     */
    private void discoverSources() {
        for (int s = 1; s < 9; s++) {
            final String type = sendAndGet("GET S[" + s + "].type", RSP_SRCNOTIFICATION, 3);
            if (type != null && !type.isEmpty()) {
                final String name = sendAndGet("GET S[" + s + "].name", RSP_SRCNOTIFICATION, 3);
                logger.debug("Source #{} - {}/{}", s, type, name);

                final ThingUID thingUID = new ThingUID(RioConstants.THING_TYPE_SOURCE, sysHandler.getThing().getUID(),
                        String.valueOf(s));

                final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(RioSourceConfig.SOURCE, s).withBridge(sysHandler.getThing().getUID())
                        .withLabel((name == null || name.isEmpty() || name.equalsIgnoreCase("null") ? "Source" : name)
                                + " (" + s + ")")
                        .build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    /**
     * Helper method to discover zones. This will iterate through all possible zones (8 of them) and see if they
     * respond to the "name" command. If they do, initial a {@link #thingDiscovered(DiscoveryResult)} for the zone.
     *
     * @param controllerUID the {@link ThingUID} of the parent controller
     * @param c the controller identifier
     * @throws IllegalArgumentException if controllerUID is null
     * @throws IllegalArgumentException if c is < 1 or > 8
     */
    private void discoverZones(ThingUID controllerUID, int c) {
        if (controllerUID == null) {
            throw new IllegalArgumentException("controllerUID cannot be null");
        }
        if (c < 1 || c > 8) {
            throw new IllegalArgumentException("c must be between 1 and 8");
        }
        for (int z = 1; z < 9; z++) {
            final String name = sendAndGet("GET C[" + c + "].Z[" + z + "].name", RSP_ZONENOTIFICATION, 4);
            if (name != null && !name.isEmpty()) {
                logger.debug("Controller #{}, Zone #{} found - {}", c, z, name);

                final ThingUID thingUID = new ThingUID(RioConstants.THING_TYPE_ZONE, controllerUID, String.valueOf(z));

                final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(RioZoneConfig.ZONE, z).withBridge(controllerUID)
                        .withLabel((name.equalsIgnoreCase("null") ? "Zone" : name) + " (" + z + ")").build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    /**
     * Helper method to send a message, parse the result with the given {@link Pattern} and extract the data in the
     * specified group number.
     *
     * @param message the message to send
     * @param respPattern the response pattern to apply
     * @param groupNum the group # to return
     * @return a possibly null response (null if an exception occurs or the response isn't a match or the response
     *         doesn't have the right amount of groups)
     * @throws IllegalArgumentException if message is null or empty, if the pattern is null
     * @throws IllegalArgumentException if groupNum is less than 0
     */
    private @Nullable String sendAndGet(String message, Pattern respPattern, int groupNum) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("message cannot be a null or empty string");
        }
        if (respPattern == null) {
            throw new IllegalArgumentException("respPattern cannot be null");
        }
        if (groupNum < 0) {
            throw new IllegalArgumentException("groupNum must be >= 0");
        }
        try {
            session.sendCommand(message);
            final String r = listener.getResponse();
            final Matcher m = respPattern.matcher(r);
            if (m.matches() && m.groupCount() >= groupNum) {
                logger.debug("Message '{}' returned a valid response: {}", message, r);
                return m.group(groupNum);
            }
            logger.debug("Message '{}' returned an invalid response: {}", message, r);
            return null;
        } catch (InterruptedException e) {
            logger.debug("Sending message '{}' was interrupted and could not be completed", message);
            return null;
        } catch (IOException e) {
            logger.debug("Sending message '{}' resulted in an IOException and could not be completed: {}", message,
                    e.getMessage(), e);
            return null;
        }
    }
}
