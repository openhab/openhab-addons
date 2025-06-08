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
 * The {@link Contract} holds informations about the supplier contract
 *
 * @author Laurent Arnal - Initial contribution
 */

public class Contract {
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

    public static Contract convertFromPrmDetail(PrmDetail prmDetail) {
        Contract result = new Contract();

        result.segment = prmDetail.segment;
        result.subscribedPower = prmDetail.situationContractuelleDtos[0].structureTarifaire().puissanceSouscrite()
                .valeur();
        result.lastActivationDate = "";
        result.distributionTariff = prmDetail.situationContractuelleDtos[0].structureTarifaire().grilleFournisseur()
                .calendrier().libelle();
        result.offpeakHours = "";
        result.contractStatus = prmDetail.situationContractuelleDtos[0].informationsContractuelles().etatContractuel()
                .code();
        result.contractType = prmDetail.situationContractuelleDtos[0].informationsContractuelles().contrat()
                .typeContrat().libelle();
        result.lastDistributionTariffChangeDate = "";

        return result;
    }
}
