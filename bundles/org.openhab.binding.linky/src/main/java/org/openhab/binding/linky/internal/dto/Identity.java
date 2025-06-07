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
package org.openhab.binding.linky.internal.dto;

/**
 * The {@link Identity} holds the informations about the contractor identity
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class Identity {
    public String title;
    public String firstname;
    public String lastname;

    public String internId;

    public static Identity convertFromUserInfo(UserInfo userInfo) {
        Identity result = new Identity();

        result.firstname = userInfo.userProperties.firstName;
        result.lastname = userInfo.userProperties.name;
        result.title = "";
        result.internId = userInfo.userProperties.internId;

        return result;
    }
}
