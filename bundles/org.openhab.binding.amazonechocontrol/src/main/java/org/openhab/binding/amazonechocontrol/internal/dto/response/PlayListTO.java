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

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link PlayListTO} encapsulates a single playlist
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PlayListTO {
    public String playlistId;
    public String title;
    public int trackCount;

    @Override
    public @NonNull String toString() {
        return "PlayListTO{playlistId='" + playlistId + "', title='" + title + "', trackCount=" + trackCount + "}";
    }
}
