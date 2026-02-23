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
package org.openhab.binding.squeezebox.internal.dto;

import org.openhab.core.media.BaseDto;

/**
 * Squeezebox Playlist data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Playlist extends BaseDto {
    private String playlist;

    @Override
    public String getName() {
        return playlist;
    }

    public String getPlaylist() {
        return playlist;
    }

}
