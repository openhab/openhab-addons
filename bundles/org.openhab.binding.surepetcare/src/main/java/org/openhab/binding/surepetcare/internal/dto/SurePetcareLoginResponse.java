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
package org.openhab.binding.surepetcare.internal.dto;

/**
 * The {@link SurePetcareLoginResponse} is a Java class used as a DTO to hold the Sure Petcare API's login response.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareLoginResponse {

    public Data data;

    public String getToken() {
        return data.token;
    }

    public class Data {

        public SurePetcareUser user;

        /**
         * The Sure Petcare API authentication token returned from the login call
         */
        public String token;

        @Override
        public String toString() {
            return "Data [user=" + user + ", token=" + token + "]";
        }
    }

    @Override
    public String toString() {
        return "SurePetcareJsonLoginResponse [data=" + data + "]";
    }
}
