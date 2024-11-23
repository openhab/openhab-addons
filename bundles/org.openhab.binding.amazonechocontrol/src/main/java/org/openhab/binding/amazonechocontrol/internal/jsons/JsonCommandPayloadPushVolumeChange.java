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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonCommandPayloadPushVolumeChange} encapsulate the GSON data of automation query
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonCommandPayloadPushVolumeChange extends JsonCommandPayloadPushDevice {
    public @Nullable String destinationUserId;
    public @Nullable Boolean isMuted;
    public @Nullable Integer volumeSetting;
}
