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
package org.openhab.binding.freeboxos.internal.api.airmedia;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirMediaReceiverRequest} is the Java class used to map the "AirMediaReceiverRequest"
 * structure used by the sending request to an AirMedia receiver API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaActionData {
    public static enum MediaAction {
        UNKNOWN,
        @SerializedName("start")
        START,
        @SerializedName("stop")
        STOP;
    }

    public static enum MediaType {
        UNKNOWN,
        @SerializedName("video")
        VIDEO,
        @SerializedName("photo")
        PHOTO,
        @SerializedName("audio")
        AUDIO,
        @SerializedName("screen")
        SCREEN;
    }

    @SuppressWarnings("unused")
    private final MediaAction action;
    @SuppressWarnings("unused")
    private final MediaType mediaType;
    @SuppressWarnings("unused")
    private final String password;
    @SuppressWarnings("unused")
    private final int position;
    @SuppressWarnings("unused")
    private @Nullable String media;

    AirMediaActionData(String password, MediaAction action, MediaType type) {
        this.password = password;
        this.action = action;
        this.mediaType = type;
        this.position = 0;
    }

    AirMediaActionData(String password, MediaAction action, MediaType type, String url) {
        this(password, action, type);
        this.media = url;
    }
}
