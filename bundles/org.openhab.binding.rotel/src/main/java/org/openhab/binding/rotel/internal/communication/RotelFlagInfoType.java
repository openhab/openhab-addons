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
package org.openhab.binding.rotel.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different types of information that can be included in response flags (HEX protocol)
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelFlagInfoType {
    MULTI_INPUT,
    ZONE2,
    ZONE3,
    ZONE4,
    ZONE,
    CENTER,
    SURROUND_LEFT,
    SURROUND_RIGHT,
    SPEAKER_A,
    SPEAKER_B
}
