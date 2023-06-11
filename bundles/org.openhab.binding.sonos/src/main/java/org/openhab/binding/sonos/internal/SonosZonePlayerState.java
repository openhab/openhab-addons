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
package org.openhab.binding.sonos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SonosZonePlayerState} is data structure to describe
 * state of a Zone Player
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class SonosZonePlayerState {

    public @Nullable String transportState;
    public @Nullable String volume;
    public @Nullable String relTime;
    public @Nullable SonosEntry entry;
    public long track;
}
