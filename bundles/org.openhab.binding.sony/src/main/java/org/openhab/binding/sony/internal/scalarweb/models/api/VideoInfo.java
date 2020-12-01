/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The video information class used for deserialization only
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class VideoInfo {
    /** The video codec */
    private @Nullable String codec;

    /**
     * Constructor used for deserialization only
     */
    public VideoInfo() {
    }

    /**
     * Returns the video codec
     * 
     * @return the video codec
     */
    public @Nullable String getCodec() {
        return codec;
    }

    @Override
    public String toString() {
        return "VideoInfo [codec=" + codec + "]";
    }
}
