/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atagone.internal.discovery;

import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * Discovers ATAG ONE thermostats by passively listening for their UDP broadcast on port 11000.
 * <p>
 * The thermostat broadcasts a 37-byte datagram every ~10 seconds:
 * {@code ONE <device_id>} (prefix "ONE " followed by the null-padded device identifier).
 * The source IP address of the datagram is used as the hostname for the discovered Thing.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.atagone")
public class AtagOneDiscoveryService extends AbstractDiscoveryService {

    private static final String REPRESENTATION_PROPERTY = "deviceId";
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_THERMOSTAT);

    private static final int DISCOVERY_PORT = 11000;
    /** Socket timeout — device broadcasts every ~10 s, so 15 s gives at least one window. */
    private static final int SOCKET_TIMEOUT_MS = 15_000;
    private static final int MANUAL_DISCOVERY_TIME_S = 30;
    private static final int BACKGROUND_SCAN_INTERVAL_S = 30;

    private static final byte[] BROADCAST_PREFIX = "ONE ".getBytes(StandardCharsets.US_ASCII);

    private final Logger logger = LoggerFactory.getLogger(AtagOneDiscoveryService.class);

    private @Nullable ScheduledFuture<?> scanJob = null;

    public AtagOneDiscoveryService() {
        super(SUPPORTED_THING_TYPES, MANUAL_DISCOVERY_TIME_S, true);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void startScan() {
        scheduler.execute(() -> listenUntilDeadline(System.currentTimeMillis() + MANUAL_DISCOVERY_TIME_S * 1000L));
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        ScheduledFuture<?> job = scanJob;
        if (job != null) {
            job.cancel(true);
        }
        scanJob = scheduler.scheduleWithFixedDelay(this::listenOnce, 0, BACKGROUND_SCAN_INTERVAL_S, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        ScheduledFuture<?> job = scanJob;
        if (job != null) {
            job.cancel(true);
        }
        scanJob = null;
    }

    // ── Discovery ─────────────────────────────────────────────────────────────

    private void listenOnce() {
        byte[] buf = new byte[64];
        // DatagramSocket(null) + explicit bind so setReuseAddress takes effect before binding.
        try (DatagramSocket socket = new DatagramSocket(null)) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(DISCOVERY_PORT));
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            parseAndAnnounce(packet);
        } catch (SocketTimeoutException e) {
            logger.debug("No ATAG ONE broadcast received within {}ms on port {}", SOCKET_TIMEOUT_MS, DISCOVERY_PORT);
        } catch (SocketException e) {
            logger.warn("Cannot open UDP port {} for ATAG ONE discovery — another process may hold it: {}",
                    DISCOVERY_PORT, e.getMessage());
        } catch (IOException e) {
            logger.debug("Error receiving ATAG ONE discovery broadcast: {}", e.getMessage());
        }
    }

    /**
     * Manual-scan variant of {@link #listenOnce()}: keeps receiving on a single bound socket until
     * {@code deadlineMs}, announcing every valid datagram, instead of returning after the first one.
     * The framework advertises a {@value #MANUAL_DISCOVERY_TIME_S}s manual scan window to the user —
     * without this loop, a scan would silently end after the first datagram (or after one
     * {@value #SOCKET_TIMEOUT_MS}ms timeout with none), missing any second device on the LAN or
     * recovering from a first packet that turned out to be noise. Background discovery doesn't need
     * this: it already gets repeated coverage over time via its own recurring schedule.
     */
    private void listenUntilDeadline(long deadlineMs) {
        byte[] buf = new byte[64];
        try (DatagramSocket socket = new DatagramSocket(null)) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(DISCOVERY_PORT));
            while (System.currentTimeMillis() < deadlineMs) {
                long remainingMs = deadlineMs - System.currentTimeMillis();
                socket.setSoTimeout((int) Math.max(1, Math.min(remainingMs, SOCKET_TIMEOUT_MS)));
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    parseAndAnnounce(packet);
                } catch (SocketTimeoutException e) {
                    // No broadcast in this window — keep listening until the deadline.
                    logger.trace("No ATAG ONE broadcast received within {}ms on port {}", SOCKET_TIMEOUT_MS,
                            DISCOVERY_PORT);
                }
            }
        } catch (SocketException e) {
            logger.warn("Cannot open UDP port {} for ATAG ONE discovery — another process may hold it: {}",
                    DISCOVERY_PORT, e.getMessage());
        } catch (IOException e) {
            logger.debug("Error receiving ATAG ONE discovery broadcast: {}", e.getMessage());
        }
    }

    private void parseAndAnnounce(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();

        if (length < BROADCAST_PREFIX.length) {
            logger.debug("ATAG ONE discovery packet too short ({} bytes), ignoring", length);
            return;
        }
        for (int i = 0; i < BROADCAST_PREFIX.length; i++) {
            if (data[i] != BROADCAST_PREFIX[i]) {
                logger.debug("ATAG ONE discovery packet has unexpected prefix, ignoring");
                return;
            }
        }

        // Payload after "ONE ": "<device_id> (ST)" — the device ID is the first space-delimited token.
        // The suffix " (ST)" is a status indicator (e.g. Standby); null bytes pad to exactly 37 bytes.
        String rest = new String(data, BROADCAST_PREFIX.length, length - BROADCAST_PREFIX.length,
                StandardCharsets.US_ASCII).replace("\0", "").trim();
        String deviceId = rest.contains(" ") ? rest.substring(0, rest.indexOf(' ')) : rest;
        String statusSuffix = rest.contains(" ") ? rest.substring(rest.indexOf(' ')).trim() : "";

        if (deviceId.isEmpty()) {
            logger.debug("ATAG ONE discovery packet contained empty device ID");
            return;
        }

        String host = packet.getAddress().getHostAddress();
        logger.debug("Discovered ATAG ONE: deviceId={} status={} host={}", deviceId, statusSuffix, host);
        announce(deviceId, host);
    }

    private void announce(String deviceId, String host) {
        // Thing UID suffix: replace characters not valid in a thing ID with underscores.
        String thingId = deviceId.replaceAll("[^A-Za-z0-9_-]", "_");
        ThingUID uid = new ThingUID(THING_TYPE_THERMOSTAT, thingId);

        Map<String, Object> properties = new HashMap<>();
        properties.put(REPRESENTATION_PROPERTY, deviceId);
        // Pre-populate hostname so the user doesn't have to type it when accepting from Inbox.
        properties.put("hostname", host);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withRepresentationProperty(REPRESENTATION_PROPERTY)
                .withProperties(properties).withLabel("ATAG ONE Thermostat (" + host + ")").build();

        thingDiscovered(result);
    }
}
