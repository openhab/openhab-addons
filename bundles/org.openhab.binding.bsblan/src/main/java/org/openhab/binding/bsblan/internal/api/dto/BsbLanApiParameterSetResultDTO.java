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
package org.openhab.binding.bsblan.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BsbLanApiParameterSetResponseDTO} reflects the response received
 * when setting a parameter.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiParameterSetResultDTO {

    public enum Status {
        @SerializedName("0")
        ERROR(0),
        @SerializedName("1")
        SUCCESS(1),
        @SerializedName("2")
        READ_ONLY(2);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @SerializedName("status")
    public Status status;
}
