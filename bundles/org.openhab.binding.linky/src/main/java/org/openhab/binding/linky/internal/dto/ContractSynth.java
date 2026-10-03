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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Laurent Arnal - Initial contribution
 */

public class ContractSynth {

    @SerializedName("generation_last_activation_date")
    public String generationLastActivationDate;

    @SerializedName("consumption_last_activation_date")
    public String consumptionLastActivationDate;

    @SerializedName("last_subscribed_power_change_date")
    public String lastSubscribedPowerChangeDate;

    @SerializedName("services_level")
    public String serviceLevel;

    public Segment[] segments;

    public class Segment {
        public String segment;
    }
}
