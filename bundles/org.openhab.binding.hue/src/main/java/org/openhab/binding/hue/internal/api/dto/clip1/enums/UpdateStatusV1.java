/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.api.dto.clip1.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for bridge API v1.20+ software update status values.
 *
 * @see <a href="https://developers.meethue.com/develop/software-update/">Developer documentation</a>
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum UpdateStatusV1 {
    @SerializedName("notupdatable")
    NOT_UPDATABLE,

    @SerializedName("unknown")
    UNKNOWN,

    @SerializedName("noupdates")
    NO_UPDATES,

    @SerializedName("transferring")
    TRANSFERRING,

    @SerializedName("downloading")
    DOWNLOADING,

    @SerializedName("installing")
    INSTALLING,

    @SerializedName("allreadytoinstall")
    ALL_READY_TO_INSTALL,

    @SerializedName("anyreadytoinstall")
    ANY_READY_TO_INSTALL,

    @SerializedName("readytoinstall")
    READY_TO_INSTALL;
}
