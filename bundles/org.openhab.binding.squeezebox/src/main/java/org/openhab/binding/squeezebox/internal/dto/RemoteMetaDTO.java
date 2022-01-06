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
package org.openhab.binding.squeezebox.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RemoteMetaDTO} contains remote metadata information, including button and
 * button override functionality.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class RemoteMetaDTO {

    /**
     * Contains button specifications for forward, rewind, repeat, shuffle
     */
    public ButtonsDTO buttons;

    /**
     * Currently unused
     */
    @SerializedName("id")
    public String id;

    /**
     * Currently unused
     */
    @SerializedName("title")
    public String title;

    /**
     * Currently unused
     */
    @SerializedName("artist")
    public String artist;

    /**
     * Currently unused
     */
    @SerializedName("album")
    public String album;

    /**
     * Currently unused
     */
    @SerializedName("artwork_url")
    public String artworkUrl;

    /**
     * Currently unused
     */
    @SerializedName("coverart")
    public String coverart;

    /**
     * Currently unused
     */
    @SerializedName("coverid")
    public String coverid;

    /**
     * Currently unused
     */
    @SerializedName("year")
    public String year;
}
