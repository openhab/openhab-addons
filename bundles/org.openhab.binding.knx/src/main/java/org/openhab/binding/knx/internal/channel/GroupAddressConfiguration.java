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
package org.openhab.binding.knx.internal.channel;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXFormatException;

/**
 * Data structure representing the content of a channel's group address configuration.
 *
 * @author Simon Kaufmann - Initial contribution and API
 *
 */
@NonNullByDefault
public class GroupAddressConfiguration {
    public static final Logger LOGGER = LoggerFactory.getLogger(GroupAddressConfiguration.class);

    private static final Pattern PATTERN_GA_CONFIGURATION = Pattern.compile(
            "^((?<dpt>[1-9][0-9]{0,2}\\.[0-9]{3,5}):)?(?<modifier>[<>])?(?<mainGA>[0-9]{1,5}(/[0-9]{1,4}){0,2})(?<listenGAs>(\\+(<?[0-9]{1,5}(/[0-9]{1,4}){0,2}))*)$");
    private static final Pattern PATTERN_LISTEN_GA = Pattern
            .compile("\\+((?<read><)?(?<GA>[0-9]{1,5}(/[0-9]{1,4}){0,2}))");

    private final @Nullable String dpt;
    private final GroupAddress mainGA;
    private final Set<GroupAddress> listenGAs;
    private final Set<GroupAddress> readGAs;

    private GroupAddressConfiguration(@Nullable String dpt, GroupAddress mainGA, Set<GroupAddress> listenGAs,
            Set<GroupAddress> readGAs) {
        this.dpt = dpt;
        this.mainGA = mainGA;
        this.listenGAs = listenGAs;
        this.readGAs = readGAs;
    }

    public @Nullable String getDPT() {
        return dpt;
    }

    /**
     * Returns the main GA, which is the GA to send commands to.
     */
    public GroupAddress getMainGA() {
        return mainGA;
    }

    /**
     * Returns all GAs to listen to.
     * This includes the main GA (unless disabled by '>'), and additional listening GAs
     * (those after the "+" symbol).
     */
    public Set<GroupAddress> getListenGAs() {
        return listenGAs;
    }

    /**
     * Returns all GAs to read from.
     * Those GAs accept read requests to the KNX bus, i.e. they respond to a "GroupValueRead" with a
     * "GroupValueResponse".
     * The '&lt;' sign sets a GA as read GA.
     */
    public Set<GroupAddress> getReadGAs() {
        return readGAs;
    }

    public static @Nullable GroupAddressConfiguration parse(@Nullable Object configuration) {
        if (!(configuration instanceof String)) {
            return null;
        }

        Matcher matcher = PATTERN_GA_CONFIGURATION.matcher(((String) configuration).replace(" ", ""));
        if (matcher.matches()) {
            // Listen GAs
            String input = matcher.group("listenGAs");
            Matcher m2 = PATTERN_LISTEN_GA.matcher(input);
            Set<GroupAddress> listenGAs = new HashSet<>();
            Set<GroupAddress> readGAs = new HashSet<>();
            while (m2.find()) {
                String ga = m2.group("GA");
                try {
                    GroupAddress groupAddress = new GroupAddress(ga);
                    listenGAs.add(groupAddress);
                    if (m2.group("read") != null) {
                        readGAs.add(groupAddress);
                    }
                } catch (KNXFormatException e) {
                    LOGGER.warn("Failed to create GroupAddress from {}", ga);
                    return null;
                }
            }

            // Main GA
            String mainGA = matcher.group("mainGA");
            try {
                GroupAddress groupAddress = new GroupAddress(mainGA);
                @Nullable
                String modifier = matcher.group("modifier");
                if (modifier == null) {
                    // default: main GA address writes and listens
                    listenGAs.add(groupAddress);
                } else if ("<".equals(modifier)) {
                    // configured for read at startup
                    listenGAs.add(groupAddress);
                    readGAs.add(groupAddress);
                } // else (">").equals(modifier) -> write only, no action
                if (readGAs.size() > 1) {
                    LOGGER.info(
                            "Item with mainGA {} has more than one GA configured for read at startup, check configuration",
                            groupAddress);
                }
                return new GroupAddressConfiguration(matcher.group("dpt"), groupAddress, listenGAs, readGAs);
            } catch (KNXFormatException e) {
                LOGGER.warn("Failed to create GroupAddress from {}", mainGA);
                return null;
            }
        } else {
            LOGGER.warn("Failed parsing channel configuration '{}'.", configuration);
        }

        return null;
    }
}
