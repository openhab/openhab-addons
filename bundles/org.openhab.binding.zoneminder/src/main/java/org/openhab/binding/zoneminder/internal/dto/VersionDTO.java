/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VersionDTO} provides the software version and API version.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class VersionDTO extends AbstractResponseDTO {

    /**
     * Zoneminder version (e.g. "1.34.2")
     */
    @SerializedName("version")
    public String version;

    /**
     * API version number (e.g. "2.0")
     */
    @SerializedName("apiversion")
    public String apiVersion;
}
