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
package org.openhab.binding.emotiva.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Emotiva subscription tag group. Each Emotiva subscription tag is assigned to group in
 * {@link EmotivaSubscriptionTags} for the possibility of handling via
 * {@link org.openhab.binding.emotiva.internal.EmotivaSubscriptionTagGroupHandler}.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum EmotivaSubscriptionTagGroup {
    AUDIO_ADJUSTMENT,
    AUDIO_INFO,
    GENERAL,
    SOURCES,
    TUNER,
    UI_DEVICE,
    UI_MENU,
    VIDEO_INFO,
    NONE,
    ZONE2_GENERAL
}
