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
package org.openhab.binding.roborock.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper methods for cloud-only refresh behavior in direct communication mode.
 *
 * @author reyhard - Initial contribution
 */
@NonNullByDefault
public final class CloudRefreshPolicy {

    private CloudRefreshPolicy() {
    }

    public static boolean isCloudMapRefreshAllowed(RoborockCommunicationMode communicationMode,
            RoborockVacuumConfiguration config) {
        return communicationMode != RoborockCommunicationMode.DIRECT || config.isCloudMapRefreshEnabled();
    }

    public static boolean isCloudMetadataRefreshAllowed(RoborockCommunicationMode communicationMode,
            RoborockVacuumConfiguration config) {
        return communicationMode != RoborockCommunicationMode.DIRECT || config.isCloudMetadataRefreshEnabled();
    }

    public static boolean isCloudOnlyRefreshDue(long now, long lastCloudOnlyPollTimestamp,
            int cloudRefreshIntervalSeconds) {
        long secondsSinceLastCloudPoll = (now - lastCloudOnlyPollTimestamp) / 1000;
        return lastCloudOnlyPollTimestamp == 0 || secondsSinceLastCloudPoll >= cloudRefreshIntervalSeconds;
    }
}
