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
package org.openhab.binding.tibberpulselocal.internal.discovery;

import static org.openhab.binding.tibberpulselocal.internal.TibberPulseLocalBindingConstants.*;

import java.net.Inet4Address;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Offers the Tibber bridge in the inbox from the multicast DNS announcement of its web server. The bridge advertises
 * one {@code _http._tcp} service named {@code Tibber Bridge Web Server} with a host name of the form
 * {@code Bridge01234567.local.}. The service type is shared by many unrelated devices, so the instance name decides,
 * and the host label is the identity of the bridge (see ADR 001).
 *
 * The participant is stateless and only reads the {@link ServiceInfo} it is given: no thing registry, no name
 * resolution, no connection to the bridge. The framework calls it on its own threads for every HTTP service of the
 * network, so it must stay cheap, quiet, and free of exceptions.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "discovery.tibberpulselocal")
public class BridgeMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_http._tcp.local.";

    /**
     * Case-insensitive for ASCII letters only. {@code String.equalsIgnoreCase} would also accept look-alikes such as
     * the dotless i or the long s, because it folds case by Unicode rules.
     */
    private static final Pattern INSTANCE_NAME = Pattern.compile("Tibber Bridge Web Server", Pattern.CASE_INSENSITIVE);

    /**
     * The trailing dot is optional because the form of the server name was not observed in a running system. Group 1
     * is the host label. Matching is ASCII only, and the pattern is anchored by {@code matches()}, so a qualified
     * instance name in the server field of a removal record does not match.
     */
    private static final Pattern HOST_NAME = Pattern.compile("(bridge[0-9a-f]{8})\\.local\\.?",
            Pattern.CASE_INSENSITIVE);

    /** What the identity rule extracts from a service: the lower-case host label and the host name as advertised. */
    private record Bridge(String hostLabel, String hostName) {
    }

    private final Logger logger = LoggerFactory.getLogger(BridgeMDNSDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        Bridge bridge = identify(service);
        return bridge == null ? null : new ThingUID(THING_TYPE_BRIDGE, bridge.hostLabel());
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        Bridge bridge = identify(service);
        if (bridge == null) {
            return null;
        }
        String hostLabel = bridge.hostLabel();
        return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_BRIDGE, hostLabel))
                .withThingType(THING_TYPE_BRIDGE)
                .withProperties(Map.of(CONFIG_HOSTNAME, hostname(service, bridge), PROPERTY_HOST_LABEL, hostLabel))
                .withRepresentationProperty(PROPERTY_HOST_LABEL).withLabel("Tibber Pulse Bridge (" + hostLabel + ")")
                .build();
    }

    /**
     * The one rule that decides whether a service is a Tibber bridge and which one. Used for the result and for the
     * removal, so that both always name the same thing UID. Reads each getter once, because JmDNS updates the service
     * info while it resolves. The framework may pass records with missing parts (and a mock returns null where JmDNS
     * returns an empty string), so every value is checked.
     *
     * @return the bridge, or null when the service is not a Tibber bridge or has no usable host name
     */
    private @Nullable Bridge identify(ServiceInfo service) {
        @Nullable
        String name = service.getName();
        @Nullable
        String server = service.getServer();
        if (name == null || server == null || !INSTANCE_NAME.matcher(name).matches()) {
            logger.debug("Ignored a service that is not a Tibber bridge");
            return null;
        }
        Matcher matcher = HOST_NAME.matcher(server);
        if (!matcher.matches()) {
            logger.debug("Ignored a bridge service without a usable host name");
            return null;
        }
        return new Bridge(matcher.group(1).toLowerCase(Locale.ROOT), withoutTrailingDot(server));
    }

    /**
     * The first usable IPv4 address of the record, otherwise the host name as advertised. An IPv6 address is never
     * offered (a link-local one is unusable without its zone), and the name is never resolved: that can block or fail
     * on hosts without an mDNS-aware resolver.
     */
    private static String hostname(ServiceInfo service, Bridge bridge) {
        Inet4Address @Nullable [] addresses = service.getInet4Addresses();
        if (addresses != null) {
            for (Inet4Address address : addresses) {
                if (address != null && !address.isAnyLocalAddress() && !address.isMulticastAddress()) {
                    return address.getHostAddress();
                }
            }
        }
        return bridge.hostName();
    }

    private static String withoutTrailingDot(String hostName) {
        return hostName.endsWith(".") ? hostName.substring(0, hostName.length() - 1) : hostName;
    }
}
