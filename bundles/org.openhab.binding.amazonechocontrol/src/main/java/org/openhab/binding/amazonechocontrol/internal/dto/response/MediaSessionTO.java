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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateInfoTO;

/**
 * The {@link MediaSessionTO} encapsulates a single media session
 *
 * @author Jan N. Klug - Initial contribution
 */
public class MediaSessionTO {
    public EligibilityTO castEligibility;
    public List<MediaSessionEndpointTO> endpointList = List.of();
    public PlayerStateInfoTO nowPlayingData;

    @Override
    public @NonNull String toString() {
        return "MediaSessionTO{castEligibility=" + castEligibility + ", endpointList=" + endpointList
                + ", nowPlayingData=" + nowPlayingData + "}";
    }
}
