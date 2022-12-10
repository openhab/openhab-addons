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
package org.openhab.binding.bosesoundtouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration class for soundtouch notification channel
 *
 * @author Ivaylo Ivanov - Initial contribution
 */
@NonNullByDefault
public class BoseSoundTouchNotificationChannelConfiguration {

    public static final String MIN_FIRMWARE = "14";
    public static final String MODEL_TYPE = "sm2";

    public static final String NOTIFICATION_VOLUME = "notificationVolume";
    public static final String NOTIFICATION_SERVICE = "notificationService";
    public static final String NOTIFICATION_REASON = "notificationReason";
    public static final String NOTIFICATION_MESSAGE = "notificationMessage";

    public @Nullable Integer notificationVolume;
    public @Nullable String notificationService;
    public @Nullable String notificationReason;
    public @Nullable String notificationMessage;

    public static boolean isSupportedFirmware(String firmware) {
        return firmware.compareTo(MIN_FIRMWARE) > 0;
    }

    public static boolean isSupportedHardware(String hardware) {
        return MODEL_TYPE.equals(hardware);
    }
}
