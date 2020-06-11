/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.networkupstools.internal;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Test class to check the validity of the {@link NutName} enum.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class NutNameTest {

    private static final Pattern CHANNEL_PATTERN = Pattern.compile("(\\w+)\\.(\\w+)\\.?(\\w+)?\\.?(\\w+)?");

    /**
     * Tests if the name in {@link NutName} enum matches with the channelID in the enum.
     */
    @Test
    public void testChannelIdName() {
        for (final NutName nn : NutName.values()) {
            final Matcher matcher = CHANNEL_PATTERN.matcher(nn.getName());

            assertTrue("NutName name '" + nn + "' could not be matched with expected pattern.", matcher.find());
            final String expectedChannelId = matcher.group(1)
                    + StringUtils.capitalize(Optional.ofNullable(matcher.group(2)).orElse(""))
                    + StringUtils.capitalize(Optional.ofNullable(matcher.group(3)).orElse(""))
                    + StringUtils.capitalize(Optional.ofNullable(matcher.group(4)).orElse(""));
            assertEquals("Channel name not correct", expectedChannelId, nn.getChannelId());
        }
    }
}
