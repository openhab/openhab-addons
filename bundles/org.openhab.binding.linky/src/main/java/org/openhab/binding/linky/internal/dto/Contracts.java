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

import org.eclipse.jetty.jaas.spi.UserInfo;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Laurent Arnal - Initial contribution
 */

public class Contracts {
    public String segment;

    @SerializedName("subscribed_power")
    public String subscribedPower;

    @SerializedName("last_activation_date")
    public String lastActivationDate;

    @SerializedName("distribution_tariff")
    public String distributionTariff;

    @SerializedName("offpeak_hours")
    public String offpeakHours;

    @SerializedName("contract_status")
    public String contractStatus;

    @SerializedName("contract_type")
    public String contractType;

    @SerializedName("last_distribution_tariff_change_date")
    public String lastDistributionTariffChangeDate;
}
