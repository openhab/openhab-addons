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
package org.openhab.binding.zoneminder.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RunStateDTO} contains a run state.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class RunStateDTO extends AbstractResponseDTO {

    /**
     * A run state
     */
    @SerializedName("State")
    public RunState runState;

    public class RunState {
        /**
         * ID of run state, typically "1", "2", etc.
         */
        @SerializedName("Id")
        public String id;

        /**
         * Name of run state
         */
        @SerializedName("Name")
        public String name;

        /**
         * Definition of the run state
         */
        @SerializedName("Definition")
        public String definition;

        /**
         * "1" if run state is active; "0" if not active
         */
        @SerializedName("IsActive")
        public String isActive;
    }
}
