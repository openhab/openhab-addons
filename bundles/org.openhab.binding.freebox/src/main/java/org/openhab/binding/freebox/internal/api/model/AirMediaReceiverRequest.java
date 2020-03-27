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
package org.openhab.binding.freebox.internal.api.model;

import org.openhab.binding.freebox.internal.api.RequestAnnotation;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirMediaReceiverRequest} is the Java class used to map the "AirMediaReceiverRequest"
 * structure used by the sending request to an AirMedia receiver API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@RequestAnnotation(responseClass = AirMediaActionResponse.class, relativeUrl = "airmedia/receivers/", retryAuth = true, method = "POST")
public class AirMediaReceiverRequest {
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
        PHOTO;
    }

    private MediaAction action = MediaAction.UNKNOWN;
    private MediaType mediaType = MediaType.UNKNOWN;
    private String password;
    private Integer position;
    private String media;

    public void setAction(MediaAction action) {
        this.action = action;
    }

    public void setType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setMedia(String media) {
        this.media = media;
    }
}
