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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirMediaReceiverRequest} is the Java class used to map the "AirMediaReceiverRequest"
 * structure used by the sending request to an AirMedia receiver API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Laurent Garnier - Initial contribution
 */
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
        PHOTO;
    }

    protected MediaAction action = MediaAction.UNKNOWN;
    protected MediaType mediaType = MediaType.UNKNOWN;
    protected String password;
    protected Integer position;
    protected String media;

    public AirMediaActionData(String password, MediaAction action, MediaType type) {
        this.password = password;
        this.action = action;
        this.mediaType = type;
    }

    public AirMediaActionData(String password, MediaAction action, MediaType type, String url) {
        this(password, action, type);
        this.media = url;
    }
}
