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

import java.util.Arrays;

import org.eclipse.jetty.jaas.spi.UserInfo;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class Contracts {
    @SerializedName("customer_id")
    public String customerId;

    @SerializedName("usage_points")
    public UsagePoint[] usagePoints;

    public static Contracts fromWebPrmInfos(WebPrmInfo[] webPrmsInfo, String prmId) {
        Contracts result = new Contracts();

        WebPrmInfo webPrmInfo = Arrays.stream(webPrmsInfo).filter(x -> x.prmId.equals(prmId)).findAny().orElseThrow();

        result.usagePoints = new UsagePoint[1];
        result.usagePoints[0] = new UsagePoint();

        result.usagePoints[0].usagePoint = new UsagePointDetails();
        result.usagePoints[0].usagePoint.meterType = "";
        result.usagePoints[0].usagePoint.usagePointId = "";
        result.usagePoints[0].usagePoint.usagePointStatus = "";

        result.usagePoints[0].usagePoint.usagePointAddresses = new AddressInfo();
        result.usagePoints[0].usagePoint.usagePointAddresses.city = webPrmInfo.adresse.adresseLigneSix;
        result.usagePoints[0].usagePoint.usagePointAddresses.country = webPrmInfo.adresse.adresseLigneSept;
        result.usagePoints[0].usagePoint.usagePointAddresses.inseeCode = "";
        result.usagePoints[0].usagePoint.usagePointAddresses.locality = "";
        result.usagePoints[0].usagePoint.usagePointAddresses.postalCode = webPrmInfo.adresse.adresseLigneSix;
        result.usagePoints[0].usagePoint.usagePointAddresses.street = webPrmInfo.adresse.adresseLigneQuatre;

        result.usagePoints[0].contracts = new ContractDetails();
        result.usagePoints[0].contracts.contractStatus = "";
        result.usagePoints[0].contracts.contractType = "";
        result.usagePoints[0].contracts.distributionTariff = "";
        result.usagePoints[0].contracts.lastActivationDate = "";
        result.usagePoints[0].contracts.lastDistributionTariffChangeDate = "";
        result.usagePoints[0].contracts.offpeakHours = "";
        result.usagePoints[0].contracts.segment = webPrmInfo.segment;
        result.usagePoints[0].contracts.subscribedPower = "" + webPrmInfo.puissanceSouscrite;

        return result;
    }
}
