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
package org.openhab.binding.insteon.internal.config;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link InsteonLegacyNetworkConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonLegacyNetworkConfiguration {
    private static final Pattern HUB1_PORT_PATTERN = Pattern
            .compile("/(?:hub|tcp)/(?<hostname>[^:]+)(?::(?<port>\\d+))?");
    private static final Pattern HUB2_PORT_PATTERN = Pattern.compile(
            "/hub2/(?<username>[^:]+):(?<password>[^@]+)@(?<hostname>[^:,]+)(?::(?<port>\\d+))?(?:,poll_time=(?<pollInterval>\\d+))?");
    private static final Pattern PLM_PORT_PATTERN = Pattern
            .compile("(?<serialPort>[^,]+)(?:,baudRate=(?<baudRate>\\d+))?");

    private String port = "";
    private @Nullable Integer devicePollIntervalSeconds;
    private @Nullable String additionalDevices;
    private @Nullable String additionalFeatures;

    public String getPort() {
        return port;
    }

    public String getRedactedPort() {
        return port.startsWith("/hub2/") ? port.replaceAll(":\\w+@", ":******@") : port;
    }

    public @Nullable Integer getDevicePollIntervalSeconds() {
        return devicePollIntervalSeconds;
    }

    public @Nullable String getAdditionalDevices() {
        return additionalDevices;
    }

    public @Nullable String getAdditionalFeatures() {
        return additionalFeatures;
    }

    public boolean isParsable() {
        try {
            parse();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public InsteonBridgeConfiguration parse() {
        Matcher hub1PortMatcher = HUB1_PORT_PATTERN.matcher(port);
        if (hub1PortMatcher.matches()) {
            return getHub1Config(hub1PortMatcher);
        }
        Matcher hub2PortMatcher = HUB2_PORT_PATTERN.matcher(port);
        if (hub2PortMatcher.matches()) {
            return getHub2Config(hub2PortMatcher);
        }
        Matcher plmPortMatcher = PLM_PORT_PATTERN.matcher(port);
        if (plmPortMatcher.matches()) {
            return getPLMConfig(plmPortMatcher);
        }
        throw new IllegalArgumentException("unable to parse bridge port parameter");
    }

    private InsteonHub1Configuration getHub1Config(Matcher matcher) {
        String hostname = matcher.group("hostname");
        Integer port = Optional.ofNullable(matcher.group("port")).map(Integer::parseInt).orElse(null);
        return InsteonHub1Configuration.valueOf(hostname, port);
    }

    private InsteonHub2Configuration getHub2Config(Matcher matcher) {
        String hostname = matcher.group("hostname");
        Integer port = Optional.ofNullable(matcher.group("port")).map(Integer::parseInt).orElse(null);
        String username = matcher.group("username");
        String password = matcher.group("password");
        Integer pollInterval = Optional.ofNullable(matcher.group("pollInterval")).map(Integer::parseInt).orElse(null);
        return InsteonHub2Configuration.valueOf(hostname, port, username, password, pollInterval);
    }

    private InsteonPLMConfiguration getPLMConfig(Matcher matcher) {
        String serialPort = matcher.group("serialPort");
        Integer baudRate = Optional.ofNullable(matcher.group("baudRate")).map(Integer::parseInt).orElse(null);
        return InsteonPLMConfiguration.valueOf(serialPort, baudRate);
    }
}
