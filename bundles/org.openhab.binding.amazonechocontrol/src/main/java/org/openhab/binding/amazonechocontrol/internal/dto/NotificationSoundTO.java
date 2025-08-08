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
package org.openhab.binding.amazonechocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link NotificationSoundTO} encapsulate a notification sound
 *
 * @author Michael Geramb - Initial contribution
 */
public class NotificationSoundTO {
    public String displayName;
    public String folder;
    public String id = "system_alerts_melodic_01";
    public String providerId = "ECHO";
    public String sampleUrl;

    @Override
    public @NonNull String toString() {
        return "NotificationSoundTO{displayName='" + displayName + "', folder='" + folder + "', id='" + id
                + "', providerId='" + providerId + "', sampleUrl='" + sampleUrl + "'}";
    }
}
