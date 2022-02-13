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
package org.openhab.binding.solarwatt.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper to handle different character cases between energy manager tagnames
 * and openhab channel names.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattTag {
    private final String tagName;
    private final String channelName;

    public SolarwattTag(String tagName) {
        this.tagName = tagName;
        char chars[] = tagName.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        this.channelName = new String(chars);
    }

    public String getTagName() {
        return this.tagName;
    }

    public String getChannelName() {
        return this.channelName;
    }
}
