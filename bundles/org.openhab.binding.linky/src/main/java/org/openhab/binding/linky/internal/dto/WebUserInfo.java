/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserInfo} holds informations about the user account
 *
 * @author Gaël L'hopital - Initial contribution
 */

public class WebUserInfo {

    public record UserProperties(@SerializedName("av2_interne_id") String internId,
            @SerializedName("av2_prenom") String firstName, @SerializedName("av2_mail") String mail,
            @SerializedName("av2_nom") String name, @SerializedName("av2_infos_personnalisees") String personalInfo) {
    }

    public String username;
    public boolean connected;
    public UserProperties userProperties;
}
