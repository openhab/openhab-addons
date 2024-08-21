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
package org.openhab.binding.doorbird.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GetsessionDTO} models the JSON response returned by the Doorbird in response
 * to calling the getsession.cgi API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class GetsessionDTO {
    /**
     * Top level container of information about the Doorbird session
     */
    @SerializedName("BHA")
    public GetsessionBha bha;

    public class GetsessionBha {
        /**
         * Return code from the Doorbird
         */
        @SerializedName("RETURNCODE")
        public String returnCode;

        /**
         * Contains information about the Doorbird session
         */
        @SerializedName("SESSIONID")
        public String sessionId;

        /**
         * Contains the v2 decryption key for events
         */
        @SerializedName("NOTIFICATION_ENCRYPTION_KEY")
        public String decryptionKey;
    }
}
