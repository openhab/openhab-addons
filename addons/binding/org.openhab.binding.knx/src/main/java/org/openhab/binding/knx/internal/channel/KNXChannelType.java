/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Meta-data abstraction for the KNX channel configurations.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public abstract class KNXChannelType extends AbstractKNXChannelType {

    private static final Pattern PATTERN = Pattern.compile(
            "^((?<dpt>[0-9]{1,2}\\.[0-9]{3}):)?(?<read>\\<)?(?<mainGA>[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3})(?<listenGAs>(\\+(\\<?[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3}))*)$");

    private static final Pattern PATTERN_LISTEN = Pattern
            .compile("\\+((?<read>\\<)?(?<GA>[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3}))");

    protected KNXChannelType(String... channelTypeIDs) {
        super(channelTypeIDs);
    }

    @Override
    @Nullable
    protected ChannelConfiguration parse(@Nullable String fancy) {
        if (fancy == null) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(fancy);

        if (matcher.matches()) {
            // Listen GAs
            String input = matcher.group("listenGAs");
            Matcher m2 = PATTERN_LISTEN.matcher(input);
            List<GroupAddressConfiguration> listenGAs = new LinkedList<>();
            while (m2.find()) {
                listenGAs.add(new GroupAddressConfiguration(m2.group("GA"), m2.group("read") != null));
            }

            // Main GA
            GroupAddressConfiguration mainGA = new GroupAddressConfiguration(matcher.group("mainGA"),
                    matcher.group("read") != null);

            return new ChannelConfiguration(matcher.group("dpt"), mainGA, listenGAs);
        }
        return null;
    }
}
