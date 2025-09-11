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
package org.openhab.binding.amazonechocontrol.internal.dto.push;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateInfoTO;

/**
 * The {@link NotifyNowPlayingUpdatedUpdateTO} encapsulates the inner update section of NotifyNowPlayingUpdated messages
 *
 * @author Jan N. Klug - Initial contribution
 */
public class NotifyNowPlayingUpdatedUpdateTO {
    public boolean playbackError;

    public String errorMessage;

    public String cause;

    public String type;

    public PlayerStateInfoTO nowPlayingData;

    @Override
    public @NonNull String toString() {
        return "NotifyNowPlayingUpdatedUpdateTO{playbackError=" + playbackError + ", errorMessage='" + errorMessage
                + "', cause='" + cause + "', type='" + type + "', nowPlayingData=" + nowPlayingData + "}";
    }
}
