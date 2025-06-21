/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a channel group, channels are divided into.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public enum ChannelGroup {
    MAIN("main"),
    TEMPS("temps"),
    HUMIDITY("humidity"),
    RECUPERATOR("recuperator"),
    SERVICE("service"),
    OPERATION("operation");

    private final String groupName;

    ChannelGroup(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
