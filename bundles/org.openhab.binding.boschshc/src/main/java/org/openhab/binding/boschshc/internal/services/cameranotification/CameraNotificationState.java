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
package org.openhab.binding.boschshc.internal.services.cameranotification;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

/**
 * Possible states for camera notifications.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public enum CameraNotificationState {
    ENABLED,
    DISABLED;

    /**
     * Converts an {@link OnOffType} state into a {@link CameraNotificationState}.
     * 
     * @param onOff the on/off state
     * @return the corresponding notification state
     */
    public static CameraNotificationState from(OnOffType onOff) {
        return onOff == OnOffType.ON ? ENABLED : DISABLED;
    }

    /**
     * Converts this {@link CameraNotificationState} into an {@link OnOffType}.
     * 
     * @return the on/off state corresponding to the notification state of this enumeration literal
     */
    public OnOffType toOnOffType() {
        return this == ENABLED ? OnOffType.ON : OnOffType.OFF;
    }
}
