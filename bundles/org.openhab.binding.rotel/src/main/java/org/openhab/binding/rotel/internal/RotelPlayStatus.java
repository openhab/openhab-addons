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
package org.openhab.binding.rotel.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different states of a source playback
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelPlayStatus {
    PLAYING,
    PAUSED,
    STOPPED
}
