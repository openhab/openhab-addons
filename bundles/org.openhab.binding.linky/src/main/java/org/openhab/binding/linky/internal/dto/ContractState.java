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

public class ContractState {

    @SerializedName("usage_point_id")
    public String usagePointId;

    @SerializedName("contract_start")
    public String contractStart;

    @SerializedName("contract_type")
    public String contractType;

    @SerializedName("contractor")
    public String contractor;

    @SerializedName("balance_responsible_party")
    public String balanceResponsibleParty;

    @SerializedName("pricing_structure")
    public String pricingStructure;

    @SerializedName("distribution_tariff")
    public String distributionTariff;

    @SerializedName("distribution_tariff_profile")
    public TariffProfile[] distributionTariffProfile;

    @SerializedName("supplier_tariff_profile")
    public TariffProfile[] supplierTariffProfile;

    @SerializedName("subscribed_power")
    public Power subscribedPower;

    // supplier_mobile_peak
    // distribution_mobile_peak

    public String segment;

    // public Customer customer;
    public Organization organization;

    public class TariffProfile {
        public String name;
        public Power power;
    }

    public class Customer {
        public Adress adress;

    }

    public class Adress {
        public String line4;
        public String line6;
        public String line7;
    }

    public class Organization {
        public String name;

        @SerializedName("business_code")
        public String businessCode;

        @SerializedName("siret_number")
        public String siretNumber;

        @SerializedName("siren_number")
        public String sirenNumber;
    }

}
