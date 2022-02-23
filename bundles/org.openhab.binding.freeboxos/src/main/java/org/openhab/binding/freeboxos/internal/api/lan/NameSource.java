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
package org.openhab.binding.freeboxos.internal.api.lan;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NameSource} holds various classes of name sources
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum NameSource {
    @SerializedName("dhcp")
    DHCP,
    @SerializedName("netbios")
    NETBIOS,
    @SerializedName("mdns")
    MDNS,
    @SerializedName("upnp")
    UPNP;
}
